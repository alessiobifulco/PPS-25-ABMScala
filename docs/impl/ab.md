---
title: AB
nav_order: 1
parent: Implementazione
---

# Implementazione AB

## Panoramica dei contributi

Il mio contributo al progetto si è focalizzato sulle seguenti aree:

* **Modello dell'agente**: definizione di `Agent`, `AgentId`, `P2d`, `V2d` e `Memory`, ovvero le strutture dati
  immutabili che descrivono un'entità simulata, la sua identità, il suo stato di moto e le sue credenze

* **Percezione e decisione**: implementazione di `AgentContext` e del tipo `Condition`, dell'`enum` `Action` e dei
  trait `Behavior` e `InteractionRule`, che insieme definiscono *come* un agente agisce e *come* cambia

* **DSL**: progettazione e implementazione dell'intero livello dichiarativo `ConditionalBehavior`,
  `CompositeBehavior`, `DiscreteRules`, `ContinuousRules`, `Chance`, i builder `BehaviorsBuilder`, `RulesBuilder` e
  `SimulationBuilder`, e la facciata `Simulation`

* **Motore di simulazione**: implementazione di `SimulationEngine`, `SimulationConfig` e `SimulationState`, con la
  pipeline di aggiornamento del tick e l'interpretazione delle azioni dichiarate dagli agenti

* **Simulazioni di esempio**: realizzazione di `Epidemic`, `OpinionDynamics`, `AntColony` e `AlarmSpreading`, usate
  come banco di prova dell'espressività del DSL

## Principali sfide implementative

1. **Progettare il modello senza una specifica di riferimento**

   In un progetto che riproduce qualcosa di già esistente il comportamento atteso è noto in partenza, e il lavoro
   consiste nel replicarlo. Qui non c'era un riferimento a cui confrontarsi: quali astrazioni introdurre, dove
   fermare i loro confini e con quale vocabolario esporle sono state tutte decisioni nostre. La difficoltà non è
   stata scrivere il codice di una singola astrazione, ma capire *quali* astrazioni servivano. Il metodo che abbiamo
   seguito è stato incrementale: ogni volta che una nuova simulazione di esempio risultava scomoda da scrivere,
   abbiamo trattato la scomodità come un difetto del modello e non come un limite da aggirare nel codice della
   simulazione.

2. **Riusabilità ed estendibilità come vincolo di progetto**

   Il prodotto non è un'applicazione ma una libreria, e serve a poco se descrive bene un dominio solo. Questo ha
   significato rendere generico tutto ciò che cambia da un dominio all'altro e impedire che il framework acquisisse
   conoscenza di un dominio particolare: il motore non sa cosa sia un contagio o un'opinione, sa solo che esiste un
   tipo di stato `S` deciso da chi usa la libreria. Il criterio concreto che ci siamo dati, e che è verificabile
   guardando il codice, è che aggiungere un comportamento, una condizione o un tipo di stato non debba comportare
   modifiche al motore.

3. **Un DSL che si legga come un enunciato del modello**

   Volevamo che una simulazione si leggesse come la descrizione del fenomeno, non come una sequenza di chiamate a
   metodi. Scrivere `Infected whenAgentIs Healthy iff atLeastNear(1, Infected)` come istruzione a sé, senza nominare
   builder né variabili di appoggio, ha richiesto di mettere insieme più meccanismi del linguaggio, e soprattutto di
   farlo senza rinunciare ai controlli del compilatore: la riga si legge come una frase, ma una composizione
   malformata resta un errore di compilazione e non un problema che emerge a esecuzione.

4. **Separazione fra intenzione, cambio di stato ed effetto**

   Un agente deve poter agire, cambiare stato e produrre effetti su altri agenti. Concentrare le tre cose in un
   unico costrutto le avrebbe legate fra loro: il comportamento avrebbe avuto bisogno dell'intera simulazione per
   funzionare, l'esito sarebbe dipeso dall'ordine di attraversamento della popolazione, e modificare il movimento
   avrebbe richiesto di toccare anche l'evoluzione dello stato.

   Ho quindi assegnato i tre compiti a punti distinti. Il `Behavior` dichiara l'**intenzione** producendo valori di
   tipo `Action`; l'`InteractionRule` dichiara il **cambio di stato**; il motore è l'unico punto in cui gli uni e
   l'altro diventano **effetto**, ed è l'unico a conoscere la popolazione intera. Ne segue che la decisione è una
   funzione pura del solo contesto locale, che le intenzioni possono essere ispezionate prima di essere applicate, e
   che gli effetti su più agenti vengono raccolti durante la decisione e applicati in una fase separata, così da non
   dipendere dall'ordine di elaborazione. Il prezzo è che il motore deve interpretare l'intero vocabolario delle
   azioni, comprese quelle che ricadono su agenti diversi da chi le ha dichiarate.

## Modello dell'agente

### Agent e AgentId

**Problema:** l'agente è la struttura dati centrale del sistema, viene ricreata a ogni tick per ogni entità della
popolazione e deve essere immutabile, aggiornabile in modo conciso e indipendente dal dominio applicativo. Esporne la
rappresentazione avrebbe legato il codice cliente a una scelta implementativa.

**Soluzione:** `Agent[S]` è un `trait` parametrico sullo stato di dominio, la cui unica implementazione è una
`case class` **privata**, raggiungibile solo attraverso il companion object. Chi usa la libreria vede quindi un nome,
un costruttore e delle operazioni, ma non la struttura concreta: se domani cambiassimo il modo in cui un agente è
rappresentato, il codice delle simulazioni non ne risentirebbe.

Gli aggiornamenti sono forniti come **extension methods** che restituiscono sempre una nuova istanza. Li ho tenuti
fuori dal trait perché sono operazioni derivate, che si possono scrivere usando solo ciò che il trait già espone:
metterli dentro avrebbe obbligato ogni futura implementazione a reimplementarli.

```scala
extension [S](agent: Agent[S])

  def withMotion(position: P2d, velocity: V2d): Agent[S] =
    Agent(agent.id, position, velocity, agent.state, agent.memory)

  def withState(state: S): Agent[S] = Agent(agent.id, agent.position, agent.velocity, state, agent.memory)

  def withMemory(memory: Option[Memory]): Agent[S] =
    Agent(agent.id, agent.position, agent.velocity, agent.state, memory)
```

L'identità è modellata da un **opaque type** su `Int`:

```scala
opaque type AgentId = Int

object AgentId:

  def apply(value: Int): AgentId = value

  extension (id: AgentId) def value: Int = id
```

Un identificatore è concettualmente diverso da un numero: sommare due identificatori o usare un contatore come
destinatario di un messaggio sono operazioni prive di senso, ma se il tipo fosse `Int` il compilatore le
accetterebbe. Il tipo opaco li rende due tipi distinti in compilazione mantenendo un intero a runtime, quindi il
controllo non costa nulla in esecuzione.

La memoria è dichiarata `Option[Memory]` perché è una capacità opzionale: le simulazioni che non la usano non pagano
né in occupazione né in complessità. Per non trascinare quell'`Option` in ogni punto in cui la memoria viene letta,
l'accesso passa da un metodo che restituisce la lista vuota quando la memoria non c'è:

```scala
def remembers: List[Belief] = agent.memory match
  case Some(m) => m.beliefs
  case _       => List.empty
```

Così un comportamento che consulta i ricordi funziona anche su agenti senza memoria, restituendo semplicemente
nessun ricordo, invece di dover distinguere i due casi ogni volta.

### P2d e V2d

**Problema:** il codice del dominio è pieno di operazioni geometriche: somme di vettori, differenze di posizioni,
normalizzazioni. Scritte come chiamate a funzioni con nome, allontanano il testo del programma dalla formula che
rappresenta, proprio nel punto in cui la direzione di movimento nasce dalla combinazione di più contributi.

**Soluzione:** posizione e spostamento sono due `case class` distinte, a cui gli operatori algebrici sono aggiunti
dall'esterno con gli extension methods.

```scala
extension (p: P2d)

  def +(v: V2d): P2d = P2d(p.x + v.x, p.y + v.y)

  def -(other: P2d): V2d = V2d(p.x - other.x, p.y - other.y)
```

Tenere due tipi separati invece di uno solo con due nomi non è una duplicazione: le firme qui sopra dicono che una
posizione più un vettore dà una posizione, che la differenza fra due posizioni è un vettore, e non prevedono affatto
la somma di due posizioni, che nel modello non significa nulla. È lo stesso ragionamento fatto per `AgentId`,
applicato però alle operazioni ammesse invece che al tipo.

La normalizzazione è scritta per casi anziché con un `if`, così che il vettore nullo risulti uno dei casi previsti e
non un'eccezione da ricordarsi:

```scala
def normalized: V2d = v.length match
  case 0 => V2d.zero
  case l => V2d(v.x / l, v.y / l)
```

### Memory

**Problema:** diverse simulazioni richiedono che un agente ricordi qualcosa, ad esempio un luogo osservato o un
evento percepito. Una memoria illimitata cresce con la durata dell'esecuzione e, soprattutto, non permette di
descrivere fenomeni in cui l'informazione si consuma col tempo.

**Soluzione:** `Memory` conserva una lista di `Belief`, ciascuno formato da un evento e dal tick in cui è stato
registrato, ed è vincolata a una **capacità massima** fissata alla costruzione. Registrare un evento nuovo scarta i
più vecchi.

```scala
private case class MemoryImpl(beliefs: List[Belief], capacity: Int) extends Memory:

  override def remember(tick: Int, event: MemoryEvent): Memory = copy(beliefs =
    (beliefs :+ Belief(event, tick)).takeRight(capacity)
  )

  override def latest: Option[Belief] = beliefs.lastOption

  override def sightings: List[Belief] = beliefs.collect:
    case belief @ Belief(_: MemoryEvent.Sighting, _) => belief
```

Il limite è imposto dentro `remember` invece che lasciato a chi la usa: in questo modo non esiste alcun percorso che
produca una memoria oltre capacità, e chi scrive un comportamento non deve ricordarsi di potarla.

Il metodo `sightings` usa `collect` per filtrare e riconoscere il tipo di evento in un solo passaggio. Serve al DSL:
è grazie a questo metodo che il movimento verso un luogo ricordato può essere scritto senza che il comportamento
sappia com'è fatta la memoria dentro.

Il tipo di evento è un `enum`, cioè un insieme chiuso: un `MemoryEvent` è *o* un'osservazione diretta *o* un
incontro, e nient'altro.

```scala
enum MemoryEvent:
  case Sighting(poi: PoiId, position: P2d)
  case Encounter(other: AgentId, positive: Boolean)
```

I due casi rappresentano le due sole vie per cui un'informazione entra nella memoria di un agente. `Sighting` è
l'osservazione di un punto di interesse, e porta con sé l'identificatore del punto e la sua posizione, cioè quello
che serve a un comportamento per tornarci. `Encounter` è l'incontro con un altro agente, di cui registra l'identità
e un booleano sull'esito, che ciascun dominio interpreta come preferisce. Il fatto che i casi siano solo questi
permette al compilatore di avvisarci se un `match` sugli eventi ne dimentica uno.

## Percezione e decisione

### AgentContext e Condition

**Problema:** un comportamento deve poter guardare il mondo, ma dargli in mano l'intero stato della simulazione
avrebbe contraddetto il principio su cui si regge il modello ad agenti, cioè che la decisione dipenda solo da
informazione **locale**.

**Soluzione:** `AgentContext` è l'unica finestra sul mondo, e contiene poco per scelta: l'agente in esame, i suoi
vicini, il tick corrente e la sua permanenza nei punti di interesse. Non contiene un riferimento all'ambiente, così
non è nemmeno possibile scrivere un comportamento che spii la popolazione intera.

Le interrogazioni derivate sono extension methods, per lo stesso motivo visto per l'agente: sono cose che si possono
calcolare a partire dal contesto, quindi non c'è ragione di farle entrare nella struttura dati.

```scala
extension [S](ctx: AgentContext[S])

  def visibleWithin(radius: Double): List[Agent[S]] = ctx.neighbors
    .filter(n => (n.position - ctx.focus.position).length <= radius)

  def heardBeliefs: List[Belief] = ctx.neighbors.flatMap(_.remembers)
```

`visibleWithin` merita una nota. Il raggio di percezione della simulazione stabilisce quali vicini un agente conosce,
ma un singolo comportamento può volerne considerare solo una parte, ad esempio percepire da lontano ma farsi
influenzare solo da vicino. Filtrare il contesto invece di calcolare un secondo vicinato riusa il lavoro già fatto
per il tick.

La condizione è un semplice alias di funzione:

```scala
type Condition[S] = AgentContext[S] => Boolean
```

Essendo un alias e non un tipo nuovo, una condizione è a tutti gli effetti una funzione: si può passare come valore,
restituire, e comporre con `and` e `or`, senza dover definire una gerarchia di classi solo per rappresentare l'idea
di predicato.

### Action

**Problema:** un comportamento deve poter produrre effetti diversi fra loro: muoversi, ricordare, comunicare,
generare un nuovo agente, morire. Se li eseguisse direttamente, la sua esecuzione non sarebbe più una funzione del
solo contesto e non potremmo collaudarla senza far girare una simulazione.

**Soluzione:** ho definito un `enum` che è il **vocabolario delle intenzioni**. Un'azione è un valore: il
comportamento lo produce, qualcun altro lo esegue.

```scala
enum Action[+S]:
  case Move(velocity: V2d)
  case Remember(event: MemoryEvent)
  case Tell(target: AgentId, event: MemoryEvent)
  case Spawn(state: S)
  case Die()
```

I cinque casi coprono tutto ciò che un agente può chiedere al mondo. `Move` esprime l'intenzione di spostarsi e porta
una velocità, non una posizione di arrivo, così che più richieste di movimento nello stesso tick possano essere
sommate fra loro. `Remember` chiede di scrivere un evento nella propria memoria, `Tell` di scriverlo in quella di un
destinatario indicato: la stessa informazione può quindi essere osservata o ricevuta, e il comportamento non deve
distinguere i due casi. `Spawn` chiede l'ingresso di un nuovo agente e ne indica solo lo stato iniziale, perché
l'identificatore lo assegna il motore, che è l'unico a sapere quali sono già in uso. `Die` chiede l'uscita
dell'agente e non ha parametri perché non ammette varianti.

Il parametro di tipo è **covariante**. L'unico caso che porta con sé uno stato di dominio è `Spawn`; tutti gli altri
non dipendono da `S`, e la covarianza permette di usarli qualunque sia lo stato della simulazione. Senza di essa una
funzione che produce, per esempio, solo `Die()` non sarebbe utilizzabile dove ci si aspetta una lista di
`Action[S]`, e il DSL avrebbe dovuto chiedere all'utente di annotare il tipo anche dove non serve.

Essendo l'insieme chiuso, aggiungere un tipo di effetto si riduce a un caso in più da interpretare in un solo punto,
e il compilatore segnala i `match` rimasti indietro.

### Behavior e InteractionRule

**Problema:** stabilito che agire e cambiare stato sono due assi distinti, resta da decidere come ciascun costrutto
dichiari quando si applica, e come il motore lo scopra senza conoscere il dominio.

**Soluzione:** due astrazioni separate, in cui l'applicabilità è un metodo con implementazione di default, così chi
ne scrive una nuova deve fornire soltanto la parte specifica. `Behavior` associa a un eventuale stato di attivazione
le azioni da produrre:

```scala
trait Behavior[S]:
  def whenState: Option[S]
  def actions(ctx: AgentContext[S]): List[Action[S]]
  def appliesTo(ctx: AgentContext[S]): Boolean = whenState.forall(_ == ctx.focus.state)
```

L'`Option` sullo stato di attivazione è il modo in cui abbiamo codificato il comportamento di default: `forall` su
`None` è `true`, quindi un comportamento senza stato di attivazione si applica a chiunque. Il vantaggio è che il
motore non ha bisogno di sapere che esiste la nozione di "default": per lui è un comportamento come gli altri, che
semplicemente risulta sempre applicabile.

`InteractionRule` associa allo stato di partenza e a una condizione il nuovo stato da assumere, e la sua
applicabilità mette insieme le due cose:

```scala
def appliesTo(ctx: AgentContext[S]): Boolean = whenState.forall(_ == ctx.focus.state) && context(ctx)
```

Il motore, che non conosce né gli stati né le condizioni del dominio, si limita quindi a interrogare `appliesTo` su
entrambi i costrutti con lo stesso identico meccanismo.

## DSL

### Sorgenti di azioni e combinatori

**Problema:** definire un comportamento richiedeva tre passi distinti, cioè implementare un trait, dichiararne lo
stato di attivazione e registrarlo, per esprimere un'unica idea. Serviva inoltre un modo per **comporre** i comportamenti:
un agente che si muove *e* comunica, oppure che tenta una cosa *e altrimenti* ne fa un'altra.

**Soluzione:** ho dato un nome alla funzione che produce azioni e ci ho costruito sopra un insieme di combinatori,
scritti come extension methods `infix`.

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

Ogni combinatore prende sorgenti e restituisce una sorgente, cioè qualcosa dello stesso tipo di ciò che ha ricevuto.
È questa proprietà a rendere le composizioni annidabili senza limiti: il risultato di una composizione è un
argomento valido per la successiva, e non serve prevedere i casi.

I due combinatori di composizione dicono cose diverse. `to` è **congiunzione**: unisce le azioni di entrambe le
sorgenti, e serve quando un agente fa più cose nello stesso tick. `orElse` è **alternativa**: usa la seconda
sorgente solo se la prima non ha prodotto nulla, e serve a esprimere una preferenza con ripiego, come un agente che
punta a un obiettivo noto e ripiega su una strategia generica quando quell'obiettivo non c'è.

Aver scelto un alias invece di un trait ha un effetto concreto sull'estendibilità: il tipo delle sorgenti coincide
con un normale tipo funzione, quindi qualunque funzione con quella firma è già una sorgente, senza dichiarazioni
aggiuntive. Le sorgenti della libreria sono normali funzioni, e una sorgente scritta da chi usa il framework ottiene
tutti i combinatori solo per il fatto di avere il tipo giusto. In cambio abbiamo rinunciato a poter vincolare il
tipo: qualsiasi funzione da contesto a lista di azioni viene accettata, anche se scritta per tutt'altro scopo.

Anche la registrazione presso il builder è un combinatore, ed è ciò che permette a una dichiarazione di essere
un'espressione completa e autonoma:

```scala
infix def whenAgentIs(state: S)(using builder: BehaviorsBuilder[S]): Unit = builder
  .add(Behavior(Some(state))(source))
```

Il risultato è che una sola riga descrive un comportamento per intero: la composizione delle sorgenti, l'eventuale
condizione che la subordina e lo stato a cui si applica stanno nella stessa frase, letta da sinistra a destra.

### Comportamento di stormo

**Problema:** il movimento coordinato non nasce da una regola sola, ma dalla somma di più spinte che si contrastano:
avvicinarsi al gruppo, allinearsi alla sua direzione, tenere le distanze, conservare la propria inerzia. Se i quattro
pesi fossero parametri posizionali, il significato di ciascun argomento dipenderebbe dal punto in cui si trova nella
chiamata; se fossero obbligatori, andrebbero scritti anche quando non si vuole cambiarli.

**Soluzione:** una classe di configurazione i cui parametri si impostano con metodi `infix` incatenabili, ciascuno
con un valore di partenza, e che **è essa stessa una `ActionSource`**.

```scala
final class FlockConfig[S](isFollowed: (S, S) => Boolean) extends ActionSource[S]
```

Ogni metodo imposta un parametro e restituisce la configurazione, così le impostazioni si concatenano in un'unica
espressione e quelle non scritte restano al valore iniziale: chi non ha esigenze particolari dichiara solo il
criterio di appartenenza al gruppo, chi ne ha aggiunge quello che gli serve, e ogni parametro si riconosce dal nome
del metodo invece che dalla posizione.

Il fatto che la configurazione sia già una sorgente di azioni è il punto che rende lo stormo non un caso speciale:
si compone con gli stessi operatori delle altre sorgenti e si registra allo stesso modo, quindi il DSL non ha
bisogno di una sintassi dedicata per il movimento coordinato.

Va detto che questa è l'unica parte del DSL costruita per assegnazioni successive invece che per sole espressioni.
Lo stato mutabile resta chiuso dentro l'istanza di configurazione e non è visibile da fuori, ma è corretto notare
che qui l'immutabilità vale in forma più debole che nel resto del dominio.

L'appartenenza allo stormo non è decisa dall'uguaglianza degli stati ma da un **predicato binario**, cioè da una
funzione che dice se un agente segue un altro. Serve perché con stati continui l'uguaglianza esatta seleziona quasi
sempre un insieme vuoto, mentre quello che interessa è raggrupparsi per somiglianza.

La combinazione delle spinte passa da una funzione ausiliaria che gestisce il caso dell'agente senza stimoli:

```scala
private def normalizedOrElse(v: V2d, fallback: => V2d): V2d = if v.length > 0 then v.normalized else fallback
```

Il ripiego è dichiarato **by-name**, quindi viene valutato solo se serve davvero. Non è un dettaglio: in alcuni punti
il ripiego genera una direzione casuale, e valutarlo comunque significherebbe estrarre numeri casuali anche quando
il risultato viene buttato via.

### Regole discrete

**Problema:** quando gli stati sono un insieme enumerabile, una regola di transizione è di fatto una frase: *questo
stato diventa quello, quando accade questo*. Scritta come costruzione di un oggetto, si allontana dall'enunciato del
modello, e il confronto fra il codice e il fenomeno descritto diventa meno immediato.

**Soluzione:** ho spezzato la frase in due passi, ciascuno realizzato da un extension method. Il primo produce un
tipo intermedio che rappresenta la sola transizione, il secondo vi applica la condizione e registra la regola presso
il builder.

```scala
extension [S](result: S) infix def whenAgentIs(from: S): Transition[S] = Transition(result, from)

extension [S](transition: Transition[S])
  infix def iff(condition: Condition[S])(using builder: RulesBuilder[S]): Unit = builder
    .add(InteractionRule(Some(transition.from), condition)(_ => transition.result))
```

Il tipo intermedio serve solo a dare un tipo al passaggio a metà frase: senza di esso il compilatore non avrebbe
nulla su cui agganciare il secondo extension method. L'utente non lo nomina mai e non può costruirlo, perché la sua
implementazione è privata. Il risultato è una riga che ricalca l'enunciato:

```scala
Infected whenAgentIs Healthy iff atLeastNear(1, Infected)
```

Il vocabolario di condizioni l'abbiamo ricavato dalle forme che ricorrono nei modelli ad agenti: conteggio dei vicini
in un dato stato, probabilità, relazione con i punti di interesse, distanza, condizioni temporali sulla memoria.
Tutte sono componibili perché condividono lo stesso tipo:

```scala
extension [S](condition: Condition[S])

  infix def and(other: Condition[S]): Condition[S] = ctx => condition(ctx) && other(ctx)

  infix def or(other: Condition[S]): Condition[S] = ctx => condition(ctx) || other(ctx)
```

Vale lo stesso discorso fatto per le sorgenti: la composizione restituisce una condizione, quindi una condizione
composta si usa dove si usa una condizione semplice e si può comporre di nuovo.

Le condizioni sulla memoria sono le uniche che dipendono dal tempo. Confrontano il tick in cui la credenza è stata
registrata con il tick corrente, e sono l'una il contrario dell'altra:

```scala
def recentlySighted[S](within: Int): Condition[S] =
  ctx => ctx.focus.remembers.exists(belief => isSighting(belief.event) && belief.at >= ctx.tick - within)

def nothingSightedIn[S](ticks: Int): Condition[S] =
  ctx => ctx.focus.remembers.forall(belief => !isSighting(belief.event) || belief.at < ctx.tick - ticks)
```

`exists` e `forall` decidono anche cosa succede sulla memoria vuota: la prima condizione risulta falsa, la seconda
vera. È il comportamento che ci aspettavamo, perché un agente che non ricorda nulla non ha visto nulla di recente e
al tempo stesso non ha visto nulla da un pezzo.

### Regole continue

**Problema:** non tutti i modelli hanno stati discreti. Quando lo stato è un numero che converge poco alla volta, le
transizioni per casi non servono, perché i casi non sono elencabili. D'altra parte, chiedere a chi usa la libreria di
estendere un nostro trait per il proprio tipo di stato significherebbe imporgli un vincolo di ereditarietà, e
renderebbe impossibile usare un tipo che non può modificare.

**Soluzione:** una **type class** che descrive come leggere un numero da uno stato e come rimettercelo dentro, più
una regola parametrica che la usa.

```scala
trait Continuous[S]:
  def extract(state: S): Double
  def update(state: S, value: Double): S
```

L'adattamento avviene fuori dal tipo dell'utente: lui scrive una given instance, e il compilatore la trova da solo
nel punto in cui serve. Il suo tipo di stato resta una normale `case class` e non acquisisce nessuna dipendenza verso
la nostra libreria. Il vincolo non sparisce, si sposta: se l'istanza non c'è, l'errore arriva in compilazione nel
punto in cui si usa la regola continua, non a esecuzione.

La regola di convergenza ha valori di default per il raggio di influenza, il criterio di affinità e il tasso:

```scala
def convergeTowardsAverage[S](
    within: Double = Double.PositiveInfinity,
    among: (S, S) => Boolean = (_: S, _: S) => true,
    atRate: Double = 1.0
)(using continuous: Continuous[S], builder: RulesBuilder[S]): Unit =
```

I due parametri `using` stanno in una lista separata perché non sono dati da trasformare ma contesto: la type class
dice come leggere lo stato e il builder dice dove registrare la regola, e in entrambi i casi l'utente non ha motivo
di scriverli a mano.

Il corpo calcola la media dei vicini influenti e sposta il valore dell'agente verso di essa in proporzione al tasso.
La regola è subordinata alla presenza di almeno un vicino influente, perché altrimenti la media andrebbe calcolata
su un insieme vuoto.

### Builder e facciata

**Problema:** i blocchi del DSL devono raccogliere le dichiarazioni scritte al loro interno, ma se l'utente dovesse
nominare o passare l'oggetto che le raccoglie perderemmo proprio la forma dichiarativa che volevamo ottenere.

**Soluzione:** ogni blocco è una **context function**. Il builder viene creato dal blocco stesso e passato
implicitamente a tutte le costruzioni che stanno dentro, che possono quindi registrarsi da sole.

```scala
def behavior[S](block: BehaviorsBuilder[S] ?=> Unit)(using simBuilder: SimulationBuilder[S]): Unit =
  val builder = BehaviorsBuilder[S]()
  block(using builder)
  builder.behaviors.sortBy(_.whenState.isEmpty).foreach(simBuilder.addBehavior)
```

L'ordinamento finale ha una ragione precisa. Il motore prende il primo comportamento applicabile, e il comportamento
di default si applica a chiunque: se l'utente lo scrivesse per primo, sarebbe l'unico a essere mai eseguito.
Ordinandoli qui, il risultato non dipende dall'ordine in cui sono stati scritti, e l'utente non deve conoscere una
regola implicita per ottenere il comportamento che si aspetta.

Lo stato mutabile dei builder è **confinato**: non sopravvive alla costruzione, perché il risultato è una
`SimulationConfig` immutabile. Le configurazioni incomplete vengono fermate qui, in modo che l'errore compaia mentre
si costruisce la simulazione e non mentre la si guarda girare:

```scala
override def build(): SimulationConfig[S] =
  require(environment.nonEmpty, "Cannot build the simulation: environment is missing")
```

Per le probabilità ho usato un **opaque type** che si valida da solo alla costruzione:

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

Siccome il costruttore è l'unico modo di ottenere un `Chance`, un valore di quel tipo è per costruzione una
probabilità valida, e il controllo sull'intervallo si paga una volta sola invece che a ogni utilizzo. Le condizioni
probabilistiche del DSL accettano un `Double` per non obbligare l'utente a una conversione esplicita, ma costruiscono
il `Chance` internamente, quindi il controllo viene fatto comunque.

Infine `Simulation` fa da facciata: con `export` ripubblica sotto un unico nome le costruzioni definite nei vari
moduli. Non le eredita e non le riscrive, le rende solo visibili da un punto solo, così l'utente ha un import
soltanto anche se il DSL è distribuito su più file.

## Motore di simulazione

### Configurazione e stato

**Problema:** far avanzare una simulazione richiede di distinguere ciò che resta fisso per tutta l'esecuzione da ciò
che cambia a ogni passo. Tenendoli insieme, ogni tick ricostruirebbe anche la parte che non cambia, e il riavvio
sarebbe un ripristino invece che una rigenerazione.

**Soluzione:** `SimulationConfig`, prodotta dal DSL e mai modificata, è separata da `SimulationState`, che contiene
l'ambiente corrente, il tick, il prossimo identificatore disponibile e le permanenze nei punti di interesse. Il
riavvio diventa così semplicemente rigenerare lo stato dalla configurazione, che è rimasta intatta.

L'inizializzazione calcola il primo identificatore libero guardando la popolazione esistente, così gli agenti nati
durante l'esecuzione non possono collidere con quelli iniziali:

```scala
private def nextAvailableId[S](agents: List[Agent[S]]): Int = agents
  .foldLeft(0)((next, agent) => next.max(agent.id.value + 1))
```

### Pipeline del tick

**Problema:** un passo di simulazione deve far percepire, decidere, muovere, nascere, morire e comunicare tutta la
popolazione. Farlo in un'unica attraversata renderebbe il risultato dipendente dall'ordine: un agente all'inizio
della lista percepirebbe lo stato del tick precedente, uno alla fine uno stato già mezzo aggiornato.

**Soluzione:** il tick è diviso in **fasi separate**, ciascuna applicata a tutta la popolazione prima che cominci la
successiva.

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

Nella percezione, la funzione di ricerca dei vicini viene preparata **una volta sola per tick** e poi applicata a
ciascun agente, invece di essere ricostruita a ogni interrogazione:

```scala
private def perceive[S](state: SimulationState[S], config: SimulationConfig[S]): List[AgentContext[S]] =
  val findNeighbors = state.environment.neighborhoods(config.perceptionRadius)(using config.neighborStrategy)
  state.environment.agents
    .map(agent => AgentContext(agent, findNeighbors(agent), state.tick, state.residencyOf(agent.id)))
```

La decisione sceglie il comportamento e la regola applicabili, applica il movimento e produce una struttura privata
che tiene insieme l'agente aggiornato e le azioni che ha dichiarato:

```scala
private def decide[S](environment: Environment[S], config: SimulationConfig[S])(ctx: AgentContext[S]): Intent[S] =
  val actions = config.behaviors.find(_.appliesTo(ctx)).map(_.actions(ctx)).getOrElse(List.empty)
  val moved = move(ctx.focus, actions, environment)
  Intent(config.rules.find(_.appliesTo(ctx)).map(_.newState(ctx)).fold(moved)(moved.withState), actions)
```

La scelta è **una sola**: a ogni agente si applicano un comportamento e una regola, i primi dichiarati fra quelli
compatibili con il suo stato. L'ordine di dichiarazione diventa così una priorità, ed è il motivo per cui il builder
mette il default in fondo. L'alternativa, applicare tutti i costrutti compatibili, avrebbe messo nel motore una
decisione su come combinare fra loro azioni provenienti da comportamenti diversi: preferiamo che quella
combinazione la scriva l'utente con i combinatori, dove è visibile. Se nessuna regola è applicabile l'agente resta
nello stato in cui è, e i due casi sono gestiti nella stessa espressione.

### Nascite, morti e messaggi

**Problema:** le azioni di ciclo di vita hanno effetti che vanno oltre il singolo agente. Una nascita ha bisogno di
un identificatore che non collida con quelli assegnati nello stesso tick; una morte deve togliere l'agente dalla
popolazione senza però cancellare ciò che ha già fatto agli altri; un messaggio deve arrivare a un destinatario che
potrebbe non essere ancora stato elaborato.

**Soluzione:** l'evoluzione della popolazione è un `foldLeft` sugli intenti, che accumula sopravvissuti e nuovi nati
in una struttura dedicata:

```scala
private def grow[S](intents: List[Intent[S]], state: SimulationState[S]): Population[S] = intents
  .foldLeft(Population(List.empty[Agent[S]], state.nextId)): (population, intent) =>
    population.joinedBy(survivors(intent, state.tick), newborns(intent, population.newId))
```

La struttura che accumula tiene anche il contatore degli identificatori, così l'aggiunta degli agenti e
l'assegnazione dei nuovi identificatori avvengono nella stessa operazione e non possono andare fuori sincrono:

```scala
private case class Population[S](agents: List[Agent[S]], nextId: Int):

  def newId: AgentId = AgentId(nextId)

  def joinedBy(survivors: List[Agent[S]], newborns: List[Agent[S]]): Population[S] =
    Population(agents ++ survivors ++ newborns, nextId + newborns.size)
```

La morte viene cercata **prima** di applicare le altre azioni all'agente, così un agente che sta per sparire non
viene aggiornato inutilmente:

```scala
private def survivors[S](intent: Intent[S], tick: Int): List[Agent[S]] =
  if intent.actions.exists(isDeath) then List.empty
  else List(intent.actions.foldLeft(intent.agent)((agent, action) => applying(agent, action, tick)))
```

Restituire una lista invece di un `Option` permette di trattare la morte come lista vuota e di concatenare i
risultati senza conversioni.

La precedenza della morte riguarda però solo gli effetti sull'agente stesso, cioè il movimento e la memoria: le
nascite e i messaggi sono ricavati dagli intenti indipendentemente dal fatto che chi li ha prodotti sopravviva, e
quindi valgono comunque. È voluto: un agente che sparisce dopo aver avvertito i vicini o generato un discendente
quell'effetto lo ha già prodotto, e annullarlo farebbe dipendere il risultato dal fatto che la morte capiti nello
stesso tick o in quello dopo.

La comunicazione avviene in due tempi: prima si estraggono i messaggi da tutte le intenzioni, poi si recapitano.

```scala
private def messages[S](intents: List[Intent[S]]): List[(AgentId, MemoryEvent)] = intents.flatMap(_.actions)
  .collect { case Tell(target, event) => (target, event) }

private def deliver[S](agents: List[Agent[S]], messages: List[(AgentId, MemoryEvent)], tick: Int): List[Agent[S]] =
  agents.map: agent =>
    messages.collect { case (target, event) if target == agent.id => event }
      .foldLeft(agent)((recipient, event) => recording(recipient, event, tick))
```

Così un agente riceve informazioni anche da agenti elaborati dopo di lui, e il recapito non dipende da dove si trova
nella lista.

### Movimento e frontiera

**Problema:** un comportamento può dichiarare più movimenti nello stesso tick, come fa lo stormo che somma più
spinte, oppure non dichiararne nessuno. In più la posizione risultante deve rispettare i confini del mondo, che però
sono gestiti dall'ambiente e non dal motore.

**Soluzione:** le velocità dichiarate si sommano, e se non ce ne sono si tiene quella corrente, così un agente senza
istruzioni prosegue per inerzia invece di fermarsi di colpo:

```scala
private def velocityOf[S](actions: List[Action[S]], current: V2d): V2d = moves(actions) match
  case Nil        => current
  case velocities => velocities.foldLeft(V2d.zero)(_ + _)
```

La posizione così ottenuta viene passata alla politica di frontiera, che restituisce la coppia posizione/velocità
definitiva:

```scala
private def move[S](agent: Agent[S], actions: List[Action[S]], environment: Environment[S]): Agent[S] =
  val velocity = velocityOf(actions, agent.velocity)
  val (position, resolved) = environment.boundaryPolicy(agent.position + velocity, velocity, environment.space)
  agent.withMotion(position, resolved)
```

Il motore non contiene nessun dettaglio della geometria del mondo: propone una posizione e accetta la correzione che
gli torna indietro. Aggiungere una forma di spazio o una politica di frontiera non richiede quindi di toccarlo.

## Simulazioni di esempio

Le quattro simulazioni sono servite a mettere alla prova l'espressività del DSL. Ognuna esercita una combinazione
diversa di funzionalità, e più di una volta scriverle ha fatto emergere lacune che hanno guidato l'evoluzione del DSL
stesso: il combinatore `orElse`, per esempio, è nato dal bisogno di esprimere una ricerca con ripiego, dove la
strategia generica interviene solo se quella preferita non produce nulla.

* **Epidemic** esercita le transizioni probabilistiche, le condizioni sul vicinato e il movimento differenziato per
  stato. La scomparsa graduale dei morti è ottenuta componendo l'arresto del movimento con la probabilità di
  dissolvimento

* **OpinionDynamics** esercita lo stato continuo, la type class `Continuous` e lo stormo con affinità definita da un
  predicato anziché dall'uguaglianza

* **AntColony** esercita la memoria a capacità limitata, la comunicazione tra agenti e la composizione con ripiego.
  Il ciclo completo di ricerca, ritrovamento, ritorno al nido e ripartenza è descritto da due comportamenti e due
  regole

* **AlarmSpreading** esercita la propagazione e l'esaurimento dell'informazione, combinando le condizioni temporali
  sulla memoria con la fuga da un punto di interesse

Limitandoci ai costrutti descritti in questa sezione, la parte comportamentale di una simulazione si presenta così:

```scala
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

Le stesse dichiarazioni, scritte usando direttamente le astrazioni del dominio e senza passare per il DSL, richiedono
di istanziare ogni comportamento e ogni regola come oggetto e di raccoglierli in liste:

```scala
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
```

La versione senza DSL è qui semplificata sul movimento, che estrae una direzione nuova a ogni tick invece di
riprodurre la persistenza della direzione implementata da `moveRandomly`: scriverla per intero avrebbe allungato il
confronto senza cambiarne la conclusione.

Il confronto mostra dove interviene il DSL. La seconda versione è più lunga, ma soprattutto mette sotto gli occhi
dell'utente cose che non riguardano il fenomeno che sta descrivendo: cosa significa l'`Option` nello stato di
attivazione, come si trasforma una transizione in una funzione da contesto a stato, in che ordine vanno messi i
comportamenti perché il default non prevalga. Sono dettagli della libreria, non dell'epidemia.

Cambia anche l'insieme degli errori possibili. Senza DSL è lecito scrivere una lista con il default in prima
posizione, o una regola la cui condizione ignora lo stato di partenza: il compilatore le accetta entrambe e il
problema si vede solo guardando l'esecuzione. Con il DSL quelle due situazioni non sono esprimibili, perché la
registrazione passa dai combinatori e l'ordinamento lo fa il builder. Restano naturalmente possibili altri errori,
per esempio una condizione logicamente sbagliata, che nessun controllo di tipo può intercettare.

[Indice](../0-index.md) | [Capitolo Precedente](../5-design.md) | [Capitolo Successivo](../7-testing.md)