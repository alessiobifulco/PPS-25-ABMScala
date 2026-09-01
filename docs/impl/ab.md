---
title: AB
nav_order: 1
parent: Implementazione
---

# Implementazione - AB

## Panoramica dei contributi

Il mio contributo al progetto si è focalizzato sulle seguenti aree:

* **Modello dell'agente**: definizione di `Agent`, `AgentId`, `P2d`, `V2d` e `Memory`, ovvero le strutture dati
  immutabili che descrivono un'entità simulata, la sua identità, il suo stato di moto e le sue credenze

* **Percezione e decisione**: implementazione di `AgentContext` e del tipo `Condition`, dell'`enum` `Action` e dei
  trait `Behavior` e `InteractionRule`, che insieme definiscono *come* un agente agisce e *come* cambia

* **DSL**: progettazione e implementazione dell'intero livello dichiarativo — `ConditionalBehavior`,
  `CompositeBehavior`, `DiscreteRules`, `ContinuousRules`, `Chance`, i builder `BehaviorsBuilder`, `RulesBuilder` e
  `SimulationBuilder`, e la facciata `Simulation`

* **Motore di simulazione**: implementazione di `SimulationEngine`, `SimulationConfig` e `SimulationState`, con la
  pipeline di aggiornamento del tick e l'interpretazione delle azioni dichiarate dagli agenti

* **Simulazioni di esempio**: realizzazione di `Epidemic`, `OpinionDynamics`, `AntColony` e `AlarmSpreading`, usate
  come banco di prova dell'espressività del DSL

## Principali sfide implementative

1. **Rendere il DSL leggibile senza sacrificare la type-safety**

   Una delle sfide principali è stata far sì che una simulazione si leggesse come un documento dichiarativo, e non
   come una sequenza di chiamate a metodi. Scrivere `Infected whenAgentIs Healthy iff atLeastNear(1, Infected)` come
   istruzione autonoma, senza che l'utente debba nominare alcun builder o alcuna variabile, ha richiesto di combinare
   tre costrutti di Scala 3: le **context function** per rendere il builder disponibile implicitamente all'interno di
   un blocco, gli **extension methods `infix`** per la sintassi in notazione naturale, e tipi intermedi come
   `Transition` per spezzare l'espressione in passi che il compilatore possa tipizzare correttamente. Il risultato è
   che gli errori di composizione vengono segnalati a tempo di compilazione.

2. **Mantenere pura la decisione dell'agente**

   Un comportamento deve poter far muovere un agente, farlo riprodurre, farlo morire e fargli inviare messaggi.
   Realizzare direttamente questi effetti all'interno dei comportamenti avrebbe reso ogni comportamento dipendente
   dall'intero stato della simulazione e praticamente impossibile da collaudare in isolamento. Ho quindi separato
   nettamente l'**intenzione** dall'**effetto**: i comportamenti producono valori di tipo `Action` e l'unico punto in
   cui tali valori diventano modifiche dello stato è il motore. Questa scelta ha però imposto di rendere il motore
   l'interprete completo del vocabolario di azioni, inclusi i casi non locali come la comunicazione tra agenti.

3. **Comunicazione tra agenti e indipendenza dall'ordine di elaborazione**

   L'azione `Tell` consente a un agente di scrivere nella memoria di un altro agente. Applicarla immediatamente
   durante l'elaborazione avrebbe reso il risultato dipendente dall'ordine in cui gli agenti vengono attraversati: un
   destinatario già elaborato non avrebbe potuto reagire nello stesso tick, uno non ancora elaborato sì. Ho risolto
   raccogliendo tutti i messaggi come dati durante la fase di decisione e recapitandoli in una **fase successiva e
   separata**, applicata uniformemente a tutta la popolazione. Lo stesso principio vale per la percezione, che avviene
   interamente sullo stato del tick precedente.

## Implementazione - Il modello dell'agente

### Agent e AgentId

**Problema:** l'agente è la struttura dati centrale del sistema — viene ricreata a ogni tick per ogni
entità della popolazione — e deve essere immutabile, aggiornabile in modo conciso e indipendente dal dominio
applicativo. Esporne direttamente la rappresentazione avrebbe però vincolato ogni futura modifica interna.

**Soluzione:** `Agent[S]` è un `trait` parametrico sullo stato di dominio, la cui unica implementazione è una
`case class` **privata** accessibile solo tramite il companion object. Gli aggiornamenti sono forniti come
**extension methods** che restituiscono sempre una nuova istanza.

```scala
extension [S](agent: Agent[S])

  def withMotion(position: P2d, velocity: V2d): Agent[S] =
    Agent(agent.id, position, velocity, agent.state, agent.memory)

  def withState(state: S): Agent[S] = Agent(agent.id, agent.position, agent.velocity, state, agent.memory)

  def withMemory(memory: Option[Memory]): Agent[S] =
    Agent(agent.id, agent.position, agent.velocity, state = agent.state, memory)
```

L'identità è modellata da un **opaque type** su `Int`:

```scala
opaque type AgentId = Int

object AgentId:

  def apply(value: Int): AgentId = value

  extension (id: AgentId) def value: Int = id
```

Il tipo opaco impedisce di passare un intero qualsiasi dove è atteso un `AgentId`, senza introdurre un wrapper
a runtime: la rappresentazione resta quella di un intero.

La memoria è dichiarata come `Option[Memory]` perché è una capacità **opzionale**: le simulazioni che non ne fanno
uso non pagano né in occupazione né in complessità. Per evitare che questa opzionalità si propaghi a ogni punto di
utilizzo, l'accesso è mediato da un metodo che degrada silenziosamente alla lista vuota:

```scala
def remembers: List[Belief] = agent.memory match
  case Some(m) => m.beliefs
  case _       => List.empty
```

### P2d e V2d

**Problema:** il codice del dominio è ricco di operazioni geometriche — somme di vettori, differenze di posizioni,
normalizzazioni. Esprimerle con chiamate a metodi statici avrebbe reso illeggibile la logica dei comportamenti, dove
la direzione di movimento nasce dalla combinazione di più contributi.

**Soluzione:** posizione e spostamento sono due `case class` distinte, arricchite tramite extension methods con gli
operatori algebrici. La distinzione tra i due tipi non è formale: impedisce di sommare due posizioni, operazione priva
di significato, e rende la differenza tra posizioni un vettore.

```scala
extension (p: P2d)

  def +(v: V2d): P2d = P2d(p.x + v.x, p.y + v.y)

  def -(other: P2d): V2d = V2d(p.x - other.x, p.y - other.y)
```

La normalizzazione è definita per casi anziché con un confronto esplicito, così da trattare esplicitamente il
vettore nullo:

```scala
def normalized: V2d = v.length match
  case 0 => V2d.zero
  case l => V2d(v.x / l, v.y / l)
```

Grazie a questi operatori il calcolo di una direzione nel DSL resta vicino alla formula corrispondente.

### Memory: credenze e oblio

**Problema:** diverse simulazioni richiedono che un agente ricordi qualcosa — la posizione di una fonte di cibo, un
allarme percepito. Una memoria illimitata, però, non è solo inefficiente: è **semanticamente sbagliata**, perché
l'informazione non decade mai e fenomeni come il progressivo esaurimento di un allarme diventano inesprimibili.

**Soluzione:** `Memory` conserva una lista di `Belief`, ciascuno costituito da un evento e dal tick in cui è stato
registrato, ed è vincolata a una **capacità massima** fissata alla costruzione. La registrazione di un nuovo evento
scarta automaticamente i più vecchi.

```scala
private case class MemoryImpl(beliefs: List[Belief], capacity: Int) extends Memory:

  override def remember(tick: Int, event: MemoryEvent): Memory = copy(beliefs =
    (beliefs :+ Belief(event, tick)).takeRight(capacity)
  )

  override def latest: Option[Belief] = beliefs.lastOption

  override def sightings: List[Belief] = beliefs.collect:
    case belief @ Belief(_: MemoryEvent.Sighting, _) => belief
```

L'estrazione delle sole osservazioni tramite `collect` combina in un'unica operazione il filtro e il pattern matching
sul tipo di evento. È questo metodo a rendere possibile, nel DSL, il movimento verso un luogo ricordato senza che il
comportamento debba conoscere la struttura interna della memoria.

Il tipo di evento è un `enum` chiuso, il che consente al compilatore di verificare l'esaustività dell'analisi per
casi:

```scala
enum MemoryEvent:
  case Sighting(poi: PoiId, position: P2d)
  case Encounter(other: AgentId, positive: Boolean)
```

## Implementazione - Percezione e decisione

### AgentContext e Condition

**Problema:** un comportamento deve poter interrogare il mondo, ma dargli accesso all'intero stato della simulazione
avrebbe violato il principio fondante del modello ad agenti — la decisione deve dipendere dalla sola informazione
**locale** — e avrebbe reso impossibile collaudare un comportamento senza costruire una simulazione completa.

**Soluzione:** `AgentContext` è l'unico canale di accesso al mondo ed è deliberatamente ristretto: contiene l'agente
in esame, i suoi vicini, il tick corrente e la sua permanenza nei punti di interesse. Le interrogazioni derivate sono
extension methods, così che il contesto resti una struttura dati semplice.

```scala
extension [S](ctx: AgentContext[S])

  def visibleWithin(radius: Double): List[Agent[S]] = ctx.neighbors
    .filter(n => (n.position - ctx.focus.position).length <= radius)

  def heardBeliefs: List[Belief] = ctx.neighbors.flatMap(_.remembers)
```

Il metodo `visibleWithin` merita una nota: il raggio di percezione della simulazione definisce l'insieme dei vicini
noti, ma un singolo comportamento può volerne considerare solo una parte. Fornire un filtro ulteriore sul contesto
consente di avere raggi diversi per scopi diversi — percepire da lontano ma essere influenzati solo da vicino — senza
moltiplicare i calcoli di vicinato.

Il concetto di condizione è espresso come **alias di funzione**:

```scala
type Condition[S] = AgentContext[S] => Boolean
```

Questa scelta, apparentemente minima, è ciò che rende possibile comporre i predicati con `and` e `or` e passarli come
valori: una condizione è una funzione di prima classe, non un'istanza di una gerarchia da estendere.

### Le azioni come dato

**Problema:** un comportamento deve poter produrre effetti eterogenei — muovere, ricordare, comunicare, generare,
morire — alcuni dei quali non sono nemmeno locali all'agente che li dichiara.

**Soluzione:** ho definito un `enum` chiuso che costituisce il **vocabolario dell'intenzione**. Un'azione è un valore, prodotto dal comportamento e interpretato altrove.

```scala
enum Action[S]:
  case Move(velocity: V2d) extends Action[S]
  case Remember(event: MemoryEvent) extends Action[S]
  case Tell(target: AgentId, event: MemoryEvent) extends Action[S]
  case Spawn(state: S) extends Action[S]
  case Die() extends Action[S]
```

Questa indirezione ha tre conseguenze: la decisione resta una funzione pura, collaudabile confrontando le liste di
azioni attese; le intenzioni possono essere **ispezionate prima di essere applicate**, cosa di cui il motore si avvale
per risolvere la morte prima degli altri effetti; e l'insieme chiuso permette al compilatore di segnalare i punti da
aggiornare quando si aggiunge una nuova azione.

### Behavior e InteractionRule

**Problema:** in una simulazione ad agenti convivono due dinamiche distinte: un agente *agisce* nello spazio e un
agente *cambia stato*. Trattarle con un unico meccanismo avrebbe intrecciato due assi indipendenti, costringendo a
riscrivere la logica di movimento ogni volta che si modifica quella di transizione.

**Soluzione:** ho introdotto due astrazioni separate. `Behavior` associa a un eventuale stato di attivazione le azioni
da produrre; `InteractionRule` associa allo stato di partenza e a una condizione il nuovo stato da assumere. Entrambe
espongono la propria applicabilità tramite un metodo con implementazione di default, così che le implementazioni
concrete debbano fornire la sola logica specifica.

```scala
trait Behavior[S]:
  def whenState: Option[S]
  def actions(ctx: AgentContext[S]): List[Action[S]]
  def appliesTo(ctx: AgentContext[S]): Boolean = whenState.forall(_ == ctx.focus.state)
```

L'uso di `Option` per lo stato di attivazione codifica il comportamento di default: `forall` su
`None` restituisce `true`, quindi un comportamento privo di stato di attivazione si applica a ogni agente senza
bisogno di alcun caso speciale.

Nel caso della regola, l'applicabilità combina lo stato di partenza con la condizione contestuale:

```scala
def appliesTo(ctx: AgentContext[S]): Boolean = whenState.forall(_ == ctx.focus.state) && context(ctx)
```

Il risultato pratico della separazione è visibile nella simulazione epidemica: un agente infetto si muove più
velocemente perché lo dice un comportamento, ma guarisce perché lo dice una regola. Modificare la velocità di
propagazione del contagio non richiede di toccare il movimento, e viceversa.

## Implementazione - Il DSL

### L'algebra delle sorgenti di azioni

**Problema:** definire un comportamento richiedeva di implementare un trait, dichiararne lo stato di attivazione e
registrarlo. Per una libreria il cui scopo è rendere immediata la descrizione di una simulazione, questa verbosità
era un ostacolo all'usabilità. Serviva inoltre un modo per **comporre** i comportamenti: un agente che si
muove *e* comunica, o che tenta un'azione *e altrimenti* ne compie un'altra.

**Soluzione:** ho introdotto un alias per la funzione che produce azioni e su di esso ho costruito un'algebra di
combinatori, tutti realizzati come extension methods `infix`.

```scala
type ActionSource[S] = AgentContext[S] => List[Action[S]]
```

```scala
extension [S](source: ActionSource[S])

  infix def to(other: ActionSource[S]): ActionSource[S] = ctx => source(ctx) ++ other(ctx)

  infix def orElse(other: ActionSource[S]): ActionSource[S] = ctx =>
    source(ctx) match
      case Nil     => other(ctx)
      case actions => actions

  infix def onlyIf(condition: Condition[S]): ActionSource[S] =
    ctx => if condition(ctx) then source(ctx) else List.empty

  infix def vanishingWith(c: Chance): ActionSource[S] = ctx => if c.happens then source(ctx) :+ Die() else source(ctx)
```

I due combinatori di composizione hanno significati diversi. `to` è **congiunzione**: unisce le azioni di entrambe
le sorgenti, e serve a comportamenti che fanno più cose contemporaneamente. `orElse` è **alternativa**: applica la
seconda sorgente solo se la prima non ha prodotto nulla, e serve a esprimere una preferenza con fallback. È il caso
della formica che cerca cibo, dove la ricerca casuale interviene soltanto in assenza di ricordi utili:

```scala
private def searchForFood: ActionSource[Task] = moveTowardsRemembered[Task](speed) orElse moveRandomly(speed)
```

Poiché la sorgente è un semplice alias di funzione e non un trait, tutte le sorgenti di base sono definite come
normali funzioni e ottengono i combinatori gratuitamente. Lo stesso vale per l'utente della libreria, che può definire
una propria sorgente personalizzata e comporla con quelle fornite senza estendere nulla.

La registrazione presso il builder è a sua volta un combinatore, il che permette a una dichiarazione di essere
un'espressione autonoma e completa:

```scala
infix def whenAgentIs(state: S)(using builder: BehaviorsBuilder[S]): Unit = builder
  .add(Behavior(Some(state))(source))
```

Ne risulta la forma finale, in cui una riga descrive per intero un comportamento:

```scala
tellNeighbours[Mood] to moveAwayFrom(danger.position, fleeSpeed) whenAgentIs Alarmed
```

### Il comportamento di stormo

**Problema:** il movimento coordinato non nasce da un'unica regola ma dalla somma pesata di più contributi
contrastanti — avvicinarsi al gruppo, allinearsi alla sua direzione, mantenere le distanze, conservare la propria
inerzia. Esporre questi quattro pesi come parametri posizionali di una funzione avrebbe prodotto chiamate illeggibili
e difficili da modificare; renderli obbligatori avrebbe reso oneroso il caso semplice.

**Soluzione:** ho realizzato una classe di configurazione i cui parametri sono metodi `infix` incatenabili, ciascuno
con un valore di default sensato, e che **estende essa stessa `ActionSource`**.

```scala
final class FlockConfig[S](isFollowed: (S, S) => Boolean) extends ActionSource[S]:

  infix def avoid(p: (S, S) => Boolean): FlockConfig[S] =
    isAvoided = p
    this

  infix def movingAt(s: Double): FlockConfig[S] =
    speed = s
    this
```

Poiché la configurazione è essa stessa una sorgente di azioni, lo stormo non è un caso speciale del DSL ed è
componibile con gli stessi operatori delle altre sorgenti. Chi lo usa può omettere i pesi e ottenere comunque un
comportamento ragionevole:

```scala
asDefault(follow[Opinion](similar) avoid different movingAt speed keepingApart separationRadius)
```

Va inoltre notato che l'appartenenza allo stormo non è determinata dall'uguaglianza degli stati ma da un **predicato
binario**. Questo consente di formare gruppi per similarità: nel modello di dinamica delle opinioni, agenti con
opinioni vicine ma non identiche si attraggono.

Infine, la combinazione delle forze è protetta da una funzione ausiliaria di normalizzazione con fallback, che evita
il caso degenere di un agente privo di stimoli:

```scala
private def normalizedOrElse(v: V2d, fallback: => V2d): V2d = if v.length > 0 then v.normalized else fallback
```

Il parametro passato per nome garantisce che il fallback — che può comportare la generazione di una direzione
casuale — venga valutato solo quando effettivamente necessario.

### La sintassi delle transizioni

**Problema:** una regola di transizione è concettualmente una frase: *questo stato diventa quello, quando accade
questo*. Esprimerla come costruzione di un oggetto avrebbe reso il file di configurazione di una simulazione un
elenco di chiamate difficile da leggere e da confrontare con il modello concettuale che rappresenta.

**Soluzione:** ho scomposto la frase in due passi, ciascuno realizzato da un extension method. Il primo produce un
tipo intermedio che rappresenta la sola transizione; il secondo vi applica la condizione e registra la regola presso
il builder implicito.

```scala
extension [S](result: S) infix def whenAgentIs(from: S): Transition[S] = Transition(result, from)

extension [S](transition: Transition[S])
  infix def iff(condition: Condition[S])(using builder: RulesBuilder[S]): Unit = builder
    .add(InteractionRule(Some(transition.from), condition)(_ => transition.result))
```

Il tipo `Transition` non ha altra funzione che rendere tipizzabile il passaggio intermedio: l'utente non lo nomina
mai, e la sua implementazione è privata. Il risultato è una sintassi che coincide con l'enunciato del modello:

```scala
Infected whenAgentIs Healthy iff atLeastNear(1, Infected)
Dead whenAgentIs Infected iff chanceOf(mortalityChance)
```

Il vocabolario di condizioni è stato progettato per coprire le forme ricorrenti nei modelli ad agenti — conteggio dei
vicini in un dato stato, probabilità, relazione con i punti di interesse, distanza, condizioni temporali sulla
memoria — e tutte le condizioni sono componibili perché condividono lo stesso tipo:

```scala
extension [S](condition: Condition[S])

  infix def and(other: Condition[S]): Condition[S] = ctx => condition(ctx) && other(ctx)

  infix def or(other: Condition[S]): Condition[S] = ctx => condition(ctx) || other(ctx)
```

Poiché la composizione preserva il tipo, una condizione composta resta utilizzabile ovunque lo sia una condizione
semplice, e può essere composta a sua volta senza limiti di annidamento:

```scala
Carrying whenAgentIs Foraging iff (settledIn(nearFood) or settledIn(farFood))
```

Le condizioni sulla memoria meritano una nota implementativa, perché sono le uniche a dipendere dal tempo. La verifica
confronta il tick di registrazione della credenza con il tick corrente del contesto, e le due condizioni sono duali:

```scala
def recentlySighted[S](within: Int): Condition[S] =
  ctx => ctx.focus.remembers.exists(belief => isSighting(belief.event) && belief.at >= ctx.tick - within)

def nothingSightedIn[S](ticks: Int): Condition[S] =
  ctx => ctx.focus.remembers.forall(belief => !isSighting(belief.event) || belief.at < ctx.tick - ticks)
```

`exists` e `forall` traducono i due quantificatori logici e danno il comportamento atteso anche sulla memoria vuota,
dove la prima condizione è falsa e la seconda vera.

### Stati continui e type class

**Problema:** non tutti i modelli ad agenti hanno stati discreti. Nella dinamica delle opinioni lo stato è una
grandezza numerica che converge gradualmente, e il meccanismo delle transizioni per casi è inapplicabile. Imporre
all'utente di estendere un trait della libreria per il proprio tipo di stato sarebbe stato invasivo e avrebbe
precluso l'uso di tipi non modificabili.

**Soluzione:** ho definito una **type class** che descrive come estrarre un valore numerico da uno stato e come
reinserirvelo, e una regola parametrica che ne fa uso.

```scala
trait Continuous[S]:
  def extract(state: S): Double
  def update(state: S, value: Double): S
```

L'adattamento è esterno e non invasivo: `Opinion` resta una normale `case class`, e l'utente si limita a dichiarare
una given instance perché il proprio tipo diventi idoneo alle regole continue.

```scala
given Continuous[Opinion] with
  override def extract(state: Opinion): Double = state.value
  override def update(state: Opinion, value: Double): Opinion = state.copy(value = value)
```

La regola di convergenza è definita con parametri di default per il raggio di influenza, il criterio di affinità e il
tasso di convergenza, così che il caso semplice non richieda alcuna configurazione:

```scala
def convergeTowardsAverage[S](
    within: Double = Double.PositiveInfinity,
    among: (S, S) => Boolean = (_: S, _: S) => true,
    atRate: Double = 1.0
)(using continuous: Continuous[S], builder: RulesBuilder[S]): Unit =
```

Il corpo calcola la media dei vicini influenti e sposta il valore dell'agente verso di essa in misura proporzionale al
tasso indicato, con la regola condizionata alla presenza di almeno un vicino influente:

```scala
builder.add(InteractionRule(Option.empty[S], influencing(_).nonEmpty)(averaged))
```

La condizione di non vuotezza evita che il calcolo della media produca un valore non definito per gli agenti
isolati.

### Builder

**Problema:** i blocchi `environment`, `behavior` e `rules` devono raccogliere le dichiarazioni scritte al loro
interno, ma l'utente non deve mai nominare né passare esplicitamente l'oggetto che le raccoglie.

**Soluzione:** ogni blocco è una **context function**. Il builder viene creato dal blocco stesso e reso disponibile
come parametro `using` a tutte le costruzioni annidate, che possono quindi registrarsi autonomamente.

```scala
def behavior[S](block: BehaviorsBuilder[S] ?=> Unit)(using simBuilder: SimulationBuilder[S]): Unit =
  val builder = BehaviorsBuilder[S]()
  block(using builder)
  builder.behaviors.sortBy(_.whenState.isEmpty).foreach(simBuilder.addBehavior)
```

L'ordinamento finale ha una motivazione precisa. Poiché il motore seleziona il **primo** comportamento applicabile e
il comportamento di default si applica a qualunque agente, dichiararlo prima degli altri lo renderebbe l'unico mai
eseguito. Ordinare i comportamenti in modo che il default resti ultimo rende l'esito indipendente dall'ordine di
scrittura.

Lo stato mutabile presente nelle implementazioni dei builder è **confinato**: non sopravvive alla costruzione, poiché
l'esito è una `SimulationConfig` immutabile. Le configurazioni incomplete sono intercettate in questa fase con
messaggi espliciti, così che l'errore emerga alla costruzione e non durante l'esecuzione:

```scala
override def build(): SimulationConfig[S] =
  require(environment.nonEmpty, "Cannot build the simulation: environment is missing")
```

Per le probabilità ho adottato un **opaque type** che valida il proprio dominio alla costruzione:

```scala
opaque type Chance = Double

object Chance:

  def apply(probability: Double): Chance =
    require(probability >= 0.0 && probability <= 1.0, "Probability must be between 0 and 1")
    probability

  extension (c: Chance)
    def value: Double = c
    def happens: Boolean = math.random() < c
```

Un valore di tipo `Chance` è per costruzione una probabilità valida, e non è possibile passare al suo posto un
`Double` arbitrario: la validazione avviene una sola volta, nel punto in cui il valore entra nel sistema.

Infine, `Simulation` funge da facciata e, tramite la clausola `export`, ripubblica in un unico punto tutte le
costruzioni dei moduli sottostanti, così che l'utente debba conoscere un solo namespace a fronte di un DSL distribuito
su più file.

## Implementazione - Il motore di simulazione

### Configurazione e stato

**Problema:** far avanzare una simulazione richiede di distinguere ciò che è fisso per l'intera esecuzione da ciò che
evolve a ogni passo. Mescolare le due categorie in un'unica struttura avrebbe reso oneroso ogni aggiornamento e
complicato operazioni elementari come il riavvio.

**Soluzione:** ho separato `SimulationConfig`, prodotta dal DSL e mai modificata, da `SimulationState`, che contiene
l'ambiente corrente, il tick, il prossimo identificatore disponibile e le permanenze nei punti di interesse. Il
riavvio diventa così la semplice rigenerazione dello stato dalla configurazione immutata, ed è esattamente ciò che fa
il comando corrispondente dell'interfaccia.

L'inizializzazione calcola il primo identificatore libero a partire dalla popolazione esistente, in modo che gli
agenti generati durante l'esecuzione non possano collidere con quelli iniziali:

```scala
private def nextAvailableId[S](agents: List[Agent[S]]): Int = agents
  .foldLeft(0)((next, agent) => next.max(agent.id.value + 1))
```

### La pipeline del tick

**Problema:** un passo di simulazione deve far percepire, decidere, muovere, nascere, morire e comunicare l'intera
popolazione. Realizzarlo come un'unica attraversata avrebbe reso il risultato dipendente dall'ordine di elaborazione:
un agente aggiornato all'inizio della lista avrebbe percepito lo stato vecchio, uno alla fine quello nuovo.

**Soluzione:** il tick è organizzato in **fasi successive e nettamente separate**, ciascuna applicata all'intera
popolazione prima che inizi la successiva.

```scala
def tick[S](state: SimulationState[S], config: SimulationConfig[S]): SimulationState[S] =
  val intents = perceive(state, config).map(decide(state.environment, config))
  val population = grow(intents, state)
  val agents = deliver(population.agents, messages(intents), state.tick)
  SimulationState(
    state.environment.withAgents(agents),
    state.tick + 1,
    population.nextId,
    residenciesOf(agents, state)
  )
```

La fase di percezione contiene un'ottimizzazione strutturale: la funzione di ricerca dei vicini viene
preparata **una sola volta per tick** e poi applicata a ciascun agente, invece di essere ricostruita a ogni
interrogazione.

```scala
private def perceive[S](state: SimulationState[S], config: SimulationConfig[S]): List[AgentContext[S]] =
  val findNeighbors = state.environment.neighborhoods(config.perceptionRadius)(using config.neighborStrategy)
  state.environment.agents
    .map(agent => AgentContext(agent, findNeighbors(agent), state.tick, state.residencyOf(agent.id)))
```

La fase di decisione seleziona il primo comportamento e la prima regola applicabili, applica il movimento e produce
una struttura privata che accoppia l'agente aggiornato alle azioni che ha dichiarato:

```scala
private def decide[S](environment: Environment[S], config: SimulationConfig[S])(ctx: AgentContext[S]): Intent[S] =
  val actions = config.behaviors.find(_.appliesTo(ctx)).map(_.actions(ctx)).getOrElse(List.empty)
  val moved = move(ctx.focus, actions, environment)
  Intent(config.rules.find(_.appliesTo(ctx)).map(_.newState(ctx)).fold(moved)(moved.withState), actions)
```

L'uso di `find` anziché di `filter` attribuisce all'ordine di dichiarazione il significato di **priorità**: per ogni
agente vengono applicati un solo comportamento e una sola regola. Il `fold` su `Option` esprime nella stessa
espressione i due casi, regola applicabile e regola non applicabile.

### Nascite, morti e messaggi

**Problema:** le azioni di ciclo di vita hanno effetti che eccedono il singolo agente: una nascita richiede un
identificatore univoco che non collida con quelli generati nello stesso tick, una morte deve prevalere su qualunque
altro effetto dichiarato, un messaggio deve raggiungere un destinatario che potrebbe non essere ancora stato
elaborato.

**Soluzione:** l'evoluzione della popolazione è un `foldLeft` che accumula sopravvissuti e nuovi nati in una struttura
dedicata, la quale incapsula anche l'avanzamento del contatore degli identificatori:

```scala
private case class Population[S](agents: List[Agent[S]], nextId: Int):

  def newId: AgentId = AgentId(nextId)

  def joinedBy(survivors: List[Agent[S]], newborns: List[Agent[S]]): Population[S] =
    Population(agents ++ survivors ++ newborns, nextId + newborns.size)
```

Incapsulare il contatore nella struttura che accumula la popolazione fa sì che l'aggiunta degli agenti e
l'avanzamento del contatore avvengano nello stesso punto.

La morte è risolta **prima** dell'applicazione delle altre azioni, così che le azioni dichiarate da un agente che
muore nello stesso tick non vengano applicate:

```scala
private def survivors[S](intent: Intent[S], tick: Int): List[Agent[S]] =
  if intent.actions.exists(isDeath) then List.empty
  else List(intent.actions.foldLeft(intent.agent)((agent, action) => applying(agent, action, tick)))
```

Restituire una lista anziché un `Option` permette di trattare uniformemente il caso della morte come lista vuota e di
concatenare i risultati senza ulteriori conversioni.

La comunicazione, come anticipato tra le sfide, è realizzata in due tempi: i messaggi vengono prima estratti da tutte
le intenzioni e poi recapitati in un passaggio separato.

```scala
private def messages[S](intents: List[Intent[S]]): List[(AgentId, MemoryEvent)] = intents.flatMap(_.actions)
  .collect { case Tell(target, event) => (target, event) }

private def deliver[S](agents: List[Agent[S]], messages: List[(AgentId, MemoryEvent)], tick: Int): List[Agent[S]] =
  agents.map: agent =>
    messages.collect { case (target, event) if target == agent.id => event }
      .foldLeft(agent)((recipient, event) => recording(recipient, event, tick))
```

In questo modo un agente può ricevere informazioni anche da agenti elaborati dopo di lui, e la propagazione
dell'allarme nella simulazione corrispondente avviene in modo uniforme, indipendentemente dalla posizione degli agenti
nella lista.

### Movimento e frontiera

**Problema:** un comportamento può dichiarare più azioni di movimento nello stesso tick — è il caso dello stormo, che
somma più contributi — e può non dichiararne alcuna. Inoltre la posizione risultante deve rispettare i confini del
mondo, la cui gestione appartiene però all'ambiente e non al motore.

**Soluzione:** le velocità dichiarate vengono sommate, e in loro assenza si conserva quella corrente, così che un
agente senza istruzioni di movimento prosegua per inerzia anziché arrestarsi:

```scala
private def velocityOf[S](actions: List[Action[S]], current: V2d): V2d = moves(actions) match
  case Nil        => current
  case velocities => velocities.foldLeft(V2d.zero)(_ + _)
```

La posizione risultante viene poi affidata alla politica di frontiera configurata, che restituisce la coppia
posizione/velocità definitiva:

```scala
private def move[S](agent: Agent[S], actions: List[Action[S]], environment: Environment[S]): Agent[S] =
  val velocity = velocityOf(actions, agent.velocity)
  val (position, resolved) = environment.boundaryPolicy(agent.position + velocity, velocity, environment.space)
  agent.withMotion(position, resolved)
```

Il motore non conosce alcun dettaglio della geometria del mondo: si limita a proporre una posizione e ad accettare la
correzione. Aggiungere una nuova forma di spazio o una nuova politica di frontiera non richiede quindi alcuna modifica
al motore.

## Implementazione - Le simulazioni di esempio

Le quattro simulazioni sono state usate per validare l'espressività del DSL. Ciascuna è stata scelta per esercitare
una combinazione di funzionalità differente, e più volte la loro scrittura ha evidenziato lacune che hanno guidato
l'evoluzione del DSL stesso: il combinatore `orElse`, per esempio, è nato dalla necessità di esprimere la ricerca del
cibo nella colonia di formiche.

* **Epidemic** esercita le transizioni probabilistiche, le condizioni sul vicinato e il movimento differenziato per
  stato. La scomparsa graduale dei morti è ottenuta componendo l'arresto del movimento con la probabilità di
  dissolvimento

* **OpinionDynamics** esercita lo stato continuo, la type class `Continuous` e lo stormo con affinità definita da un
  predicato anziché dall'uguaglianza

* **AntColony** esercita la memoria a capacità limitata, la comunicazione tra agenti e la composizione con fallback.
  Il ciclo completo — cercare, trovare, tornare al nido, ripartire — è descritto da due comportamenti e due regole

* **AlarmSpreading** esercita la propagazione e l'esaurimento dell'informazione, combinando le condizioni temporali
  sulla memoria con la fuga da un punto di interesse

Ciascuna simulazione occupa poche decine di righe, di cui buona parte costituita da costanti di configurazione:

```scala
val config: SimulationConfig[Health] = Simulation.of[Health]:
  environment:
    space(RectangularSpace(width, height)) withBoundary bounce
    perception(perceptionRadius)
    population(populationSize) of Healthy withOne Infected
  behavior:
    stopMoving[Health] vanishingWith chance(decayChance) whenAgentIs Dead
    moveRandomly[Health](infectedSpeed) whenAgentIs Infected
    moveRandomly[Health](recoveredSpeed) whenAgentIs Recovered
    asDefault(moveRandomly[Health](healthySpeed))
  rules:
    Infected whenAgentIs Healthy iff atLeastNear(1, Infected)
    Dead whenAgentIs Infected iff chanceOf(mortalityChance)
    Recovered whenAgentIs Infected iff chanceOf(recoveryChance)
    Healthy whenAgentIs Recovered iff chanceOf(immunityLoss)
```

La stessa simulazione, scritta usando direttamente le astrazioni del dominio e senza passare per il DSL, richiede di
costruire a mano la popolazione, di istanziare ogni comportamento e ogni regola come oggetto e di assemblare la
configurazione finale:

```scala
val space = RectangularSpace(width, height)

val agents: List[Agent[Health]] = (0 until populationSize).toList.map: i =>
  Agent(AgentId(i), space.randomPosition, V2d.random(), if i == 0 then Infected else Healthy, None)

val behaviors: List[Behavior[Health]] = List(
  Behavior(Some(Dead)): _ =>
    if Chance(decayChance).happens then List(Move(V2d.zero), Die()) else List(Move(V2d.zero))
  ,
  Behavior(Some(Infected))(_ => List(Move(V2d.random() * infectedSpeed))),
  Behavior(Some(Recovered))(_ => List(Move(V2d.random() * recoveredSpeed))),
  Behavior(None)(_ => List(Move(V2d.random() * healthySpeed)))
)

val rules: List[InteractionRule[Health]] = List(
  InteractionRule(Some(Healthy), _.neighbors.count(_.state == Infected) >= 1)(_ => Infected),
  InteractionRule(Some(Infected), _ => Chance(mortalityChance).happens)(_ => Dead),
  InteractionRule(Some(Infected), _ => Chance(recoveryChance).happens)(_ => Recovered),
  InteractionRule(Some(Recovered), _ => Chance(immunityLoss).happens)(_ => Healthy)
)

val config = SimulationConfig(
  Environment(space, agents, BoundaryPolicy.bounce),
  behaviors,
  perceptionRadius,
  rules
)
```

Il confronto tra le due versioni mostra dove interviene il DSL. La seconda è più lunga, ma soprattutto espone
all'utente dettagli che non riguardano il modello che sta descrivendo: la costruzione degli identificatori, il
significato dell'`Option` nello stato di attivazione, la conversione di una transizione in una funzione da contesto a
stato, l'ordine in cui i comportamenti vanno inseriti nella lista perché il default non prevalga. Sono tutte
informazioni che appartengono all'implementazione della libreria, non alla descrizione di un'epidemia.

C'è inoltre una differenza nel tipo di errori possibili. Nella versione senza DSL è lecito scrivere una lista di
comportamenti con il default in prima posizione, o una regola la cui condizione ignora lo stato di partenza: il
compilatore accetta entrambe, e il problema si manifesta solo osservando una simulazione che si comporta in modo
inatteso. Il DSL rende alcune di queste situazioni non rappresentabili, perché la registrazione avviene attraverso i
combinatori e l'ordinamento è a carico del builder.

[Indice](0-index.md) | [Capitolo Precedente](5-design.md) | [Capitolo Successivo](7-testing.md)