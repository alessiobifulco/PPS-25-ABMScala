---
title: Samuele Ferri
nav_order: 2
parent: Implementazione
---

# Implementazione - SF

## Panoramica dei contributi

Il mio contributo al progetto si è focalizzato sulle seguenti aree:

* **Spazio e ambiente**: implementazione di `Space`, `RectangularSpace`, `CircularSpace`, `Shape`, `BoundaryPolicy` ed `Environment`, che definiscono la geometria del mondo simulato, il comportamento degli agenti ai confini e la popolazione contenuta nell'ambiente

* **Ricerca dei vicini**: progettazione e implementazione di `NeighborStrategy`, con strategie brute-force e basate su griglia spaziale, per rendere intercambiabile l'algoritmo di ricerca degli agenti vicini

* **Point of Interest**: implementazione di `POI` e `PoiId`, per rappresentare regioni spaziali dotate di significato nel modello

* **DSL dell'ambiente**: implementazione di `EnvironmentBuilder`, responsabile della configurazione dello spazio, della popolazione iniziale, del posizionamento degli agenti, del raggio di percezione, della memoria e dei punti di interesse

* **Interfaccia grafica**: realizzazione dell'intero package `gui`, comprendente il menu principale, la finestra di simulazione, il rendering della scena, il pannello delle statistiche e l'implementazione del pattern Model-View-Update

## Principali sfide implementative

1. **Separare la geometria dell'ambiente dalla logica della simulazione**

   Lo spazio della simulazione può avere forme differenti e deve supportare operazioni comuni, come il controllo di appartenenza, la correzione di una posizione e la gestione del movimento in prossimità del confine. Inserire direttamente la logica relativa a rettangoli e cerchi nell'Engine avrebbe reso il motore dipendente dalla geometria concreta del mondo. Ho quindi definito il trait `Space`, delegando alle implementazioni `RectangularSpace` e `CircularSpace` le operazioni specifiche della forma.

2. **Rendere intercambiabile la ricerca dei vicini**

   La ricerca dei vicini è una delle operazioni più frequenti dell'Engine e il suo costo può diventare rilevante all'aumentare della popolazione. Ho introdotto il trait `NeighborStrategy`, fornendo una strategia `bruteForce` come implementazione predefinita e una strategia `grid` basata su una suddivisione spaziale, senza vincolare l'ambiente o il motore a un unico algoritmo.

3. **Configurare l'ambiente attraverso una sintassi dichiarativa**

   La configurazione dell'ambiente comprende spazio, politica dei confini, popolazione, stato e posizione iniziale degli agenti, raggio di percezione, memoria e punti di interesse. Ho realizzato `EnvironmentBuilder` e i relativi oggetti di configurazione intermedi, permettendo di esprimere queste impostazioni all'interno di un blocco dichiarativo del DSL.

4. **Collegare un modello immutabile a una libreria grafica imperativa**

   Swing richiede componenti con stato interno e aggiornamenti imperativi, mentre il modello della simulazione e l'aggiornamento del suo stato sono espressi in termini funzionali. Ho separato il `Model` immutabile dalla gestione dei componenti grafici: i messaggi vengono trasformati in nuove versioni del modello tramite MVU, mentre i pannelli conservano localmente soltanto le informazioni necessarie al rendering e alle statistiche.

5. **Visualizzare sia la scena sia l'evoluzione quantitativa**

   La sola animazione degli agenti non permette di osservare in modo completo l'andamento della simulazione. Ho quindi affiancato al pannello della scena un pannello statistico capace di mostrare la distribuzione degli stati, le transizioni e la densità spaziale, mantenendo uno storico limitato delle informazioni osservate.

## Implementazione - Spazio e ambiente

### Space e Shape

**Problema:** l'Engine deve poter lavorare con mondi di forma diversa senza conoscere i dettagli geometrici dello spazio. Inoltre, la GUI deve poter rappresentare correttamente il confine dell'ambiente.

**Soluzione:** ho definito il trait `Space`, che raccoglie le operazioni comuni a tutte le geometrie:

```scala
trait Space:
  def contains(position: P2d): Boolean
  def clamp(position: P2d): P2d
  def bounce(position: P2d, velocity: V2d): (P2d, V2d)
  def stop(position: P2d, velocity: V2d): (P2d, V2d)
  def randomPosition: P2d
  def shape: Shape
```

Il metodo `contains` verifica se una posizione appartiene allo spazio, `clamp` la riporta all'interno dei confini e le operazioni `bounce` e `stop` implementano due possibili comportamenti al raggiungimento dei confini. `randomPosition` viene utilizzato per generare posizioni iniziali casuali quando il DSL non ne specifica una.

La forma concreta dello spazio viene esposta tramite `Shape`, un enum che descrive rettangoli e cerchi. La GUI utilizza questo valore per decidere se disegnare un rettangolo o una circonferenza, evitando di duplicare la logica di riconoscimento della geometria.

### RectangularSpace

`RectangularSpace` rappresenta una regione delimitata da una larghezza e da un'altezza positive. Una posizione appartiene allo spazio quando entrambe le coordinate sono comprese nei rispettivi intervalli. Il metodo `clamp` limita ogni coordinata tra zero e la dimensione del lato corrispondente.

Nel caso di `bounce`, la posizione viene riportata all'interno del rettangolo e la componente della velocità diretta verso il bordo viene invertita. Nel caso di `stop`, la posizione viene ugualmente contenuta, ma la velocità viene azzerata quando l'agente sta uscendo dallo spazio.

Il rettangolo implementa anche `Toroidal`. L'operazione `wrap` riporta ogni coordinata nell'intervallo valido utilizzando il resto della divisione, rendendo possibile il passaggio continuo da un lato all'altro dell'ambiente.

### CircularSpace

`CircularSpace` rappresenta una regione circolare definita da un centro e da un raggio positivo. L'appartenenza viene verificata tramite la distanza dal centro. Il metodo `clamp` lascia inalterate le posizioni interne e proietta sulla circonferenza quelle esterne.

Nel comportamento `bounce`, la velocità viene riflessa rispetto alla normale al bordo. Questo permette di conservare la componente tangenziale e invertire quella diretta verso l'esterno. Il comportamento `stop` azzera la velocità quando l'agente oltrepassa il bordo o si sta muovendo verso l'esterno.

Anche lo spazio circolare implementa `Toroidal`: `wrap` trasferisce una posizione esterna sul punto opposto della circonferenza.

## Implementazione - Politica dei confini e ambiente

### BoundaryPolicy

**Problema:** il movimento di un agente può portarlo oltre i confini dello spazio e comportamenti diversi richiedono risposte differenti. La gestione dei confini non doveva essere duplicata nell'Engine per ogni forma geometrica.

**Soluzione:** ho definito `BoundaryPolicy` come enum con tre casi: `bounce`, che riflette il movimento dell'agente, `stop`, che lo arresta quando raggiunge il confine, e `wrap`, che lo trasferisce sul lato opposto quando lo spazio è toroidale.

Il metodo `apply` delega l'effettiva gestione alla geometria dello spazio. Se `wrap` viene utilizzato con uno spazio non toroidale, viene applicato il comportamento di rimbalzo. La politica richiesta e la geometria che la realizza restano così separate.

### Environment

**Problema:** l'ambiente deve aggregare tutti gli elementi condivisi della simulazione, ma deve rimanere immutabile e indipendente dall'algoritmo utilizzato per cercare i vicini.

**Soluzione:** `Environment[S]` è un trait parametrico sullo stato degli agenti e contiene lo spazio, la popolazione, la politica dei confini e i punti di interesse. Espone inoltre le operazioni `neighborsOf` e `neighborhoods`, che delegano la ricerca alla `NeighborStrategy` fornita.

Gli aggiornamenti della popolazione e dei punti di interesse non modificano l'istanza esistente, ma producono una nuova versione dell'ambiente tramite `withAgents` e `withPois`. La costruzione verifica la compatibilità tra politica dei confini e spazio e controlla che i punti di interesse siano contenuti nell'ambiente.

## Implementazione - Ricerca dei vicini

### NeighborStrategy

**Problema:** l'algoritmo brute-force è semplice, ma confronta ogni agente con tutta la popolazione. Il numero di confronti cresce quindi con il numero di agenti e può diventare il costo dominante del ciclo di simulazione.

**Soluzione:** ho definito il trait `NeighborStrategy[S]`, il cui metodo `prepare` riceve la popolazione e il raggio di ricerca e restituisce una funzione che individua i vicini di uno specifico agente.

L'implementazione `bruteForce`, utilizzata come strategia predefinita, esamina tutti gli agenti, esclude l'agente osservatore e conserva quelli la cui distanza non supera il raggio indicato. Il raggio viene validato per impedire valori negativi o non finiti.

L'implementazione `grid` suddivide il piano in celle di dimensione configurabile e costruisce un indice che associa ogni cella agli agenti che vi appartengono. Per cercare i vicini vengono considerate soltanto le celle potenzialmente rilevanti.

La strategia a griglia può essere selezionata esplicitamente, mentre quella brute-force è fornita come default tramite una given instance. La scelta di utilizzare una type class permette all'Engine di dipendere soltanto dall'astrazione della strategia.

## Implementazione - Point of Interest

### POI e PoiId

**Problema:** alcuni modelli richiedono regioni spaziali con un significato specifico, come fonti di cibo, ospedali o luoghi pericolosi. Tali regioni non devono diventare agenti, perché non possiedono comportamento autonomo.

**Soluzione:** ho definito `POI` come una `case class` composta da identificatore, nome, posizione, raggio e ritardo di attivazione:

```scala
case class POI(
    id: PoiId,
    name: String,
    position: P2d,
    radius: Double,
    activationDelay: Int = 0
)
```

Il metodo `contains` verifica se una posizione si trova all'interno della regione circolare confrontando la distanza dal centro con il raggio. `PoiId` è un opaque type basato su `Int`, utilizzato per distinguere gli identificatori dei punti di interesse dagli interi ordinari.

Il raggio deve essere positivo e il ritardo di attivazione non può essere negativo. Il `POI` resta un elemento passivo dell'ambiente: viene osservato attraverso il contesto dell'agente e può essere utilizzato dalle condizioni del DSL, ma non contiene alcuna logica comportamentale.

## Implementazione - Il DSL dell'ambiente

### EnvironmentBuilder

**Problema:** la definizione dell'ambiente richiede numerose impostazioni e deve rimanere leggibile all'interno della dichiarazione della simulazione. L'utente non dovrebbe dover creare manualmente l'ambiente né gestire direttamente la popolazione iniziale.

**Soluzione:** ho realizzato `EnvironmentBuilder`, un builder parametrico sul tipo di stato degli agenti. Il builder espone operazioni per configurare spazio e politica dei confini, popolazione e stato degli agenti, posizionamento, raggio di percezione, capacità della memoria e punti di interesse.

Le configurazioni intermedie `SpaceConfig` e `PopulationConfig` permettono di completare le impostazioni con una sintassi concatenabile, grazie all'uso di `infix`.

La funzione `environment` accetta un blocco di tipo `EnvironmentBuilder[S] ?=> Unit`, una context function di Scala 3. Questo significa che il builder non viene mai passato esplicitamente dall'utente: il compilatore lo inietta automaticamente nel contesto del blocco, rendendo disponibili le parole del DSL senza alcun riferimento esplicito all'oggetto di configurazione:

```scala
environment:
  space(RectangularSpace(800, 600)) withBoundary BoundaryPolicy.bounce
  population(100) of Healthy
  perception(15.0)
```

All'interno del blocco, ogni chiamata a `space`, `population` e `perception` trova il builder nel contesto implicito tramite `using`. La stessa tecnica è utilizzata dal `SimulationBuilder` per i blocchi `behaviour` e `rules`.

`withOne` permette di assegnare uno stato differente al primo agente, mentre `eachBeing` consente di generare lo stato attraverso un valore valutato per ciascun agente. Se non viene specificata una funzione di posizionamento, il builder utilizza `space.randomPosition`.

Internamente, il builder conserva temporaneamente le impostazioni in campi mutabili privati. Al termine del blocco, `build()` controlla la presenza dello spazio e della popolazione e verifica che la numerosità sia positiva. Il risultato è un `EnvironmentSpec`, visibile soltanto all'interno del package `dsl`, utilizzato dal `SimulationBuilder` per costruire l'ambiente e gli agenti iniziali.

La responsabilità del builder è quindi raccogliere e validare la configurazione necessaria. Le ulteriori invarianti dell'ambiente, come la compatibilità tra politica `wrap` e spazio toroidale o la posizione valida dei POI, vengono verificate durante la costruzione di `Environment`.

## Implementazione - La GUI

### Model-View-Update

**Problema:** la GUI deve aggiornare periodicamente la simulazione, reagire ai comandi dell'utente e ridisegnare i componenti Swing senza introdurre logica di simulazione nei pannelli.

**Soluzione:** ho organizzato l'interfaccia secondo il pattern Model-View-Update. Il `Model` è una struttura dati immutabile che contiene la configurazione, lo stato corrente della simulazione e l'informazione relativa all'esecuzione.

Gli eventi sono rappresentati dall'enum `Msg`, che comprende `Tick`, `ToggleRun` e `RestartAndRun`. `Mvu.update` trasforma ogni messaggio in una computazione di tipo `State[Model[S], Unit]`. La computazione riceve il modello corrente e restituisce il modello aggiornato senza modificare direttamente l'istanza precedente.

```scala
def update[S](msg: Msg): State[Model[S], Unit] = msg match
  case Msg.Tick          => tick
  case Msg.ToggleRun     => toggleRun
  case Msg.RestartAndRun => restart.flatMap(_ => run)
```

La scelta di rappresentare `update` come `State[Model[S], Unit]` invece di una semplice funzione `(Model[S], Msg) => Model[S]` separa la descrizione della trasformazione dalla sua esecuzione. La computazione viene costruita e restituita senza essere applicata immediatamente: è `SimulationWindow` a decidere quando eseguirla chiamando `apply` sul modello corrente. Questo rende `Mvu` completamente puro e testabile senza aprire alcuna finestra.

La type class `Monad[F[_]]` definisce le operazioni `unit`, `flatMap` e `map` su un tipo costruttore generico. La `State` monad ne è un'implementazione concreta: `unit` restituisce una computazione che non modifica lo stato, mentre `flatMap` concatena due computazioni in sequenza, passando lo stato prodotto dalla prima alla seconda. Questo permette di comporre trasformazioni come `restart.flatMap(_ => run)` in modo dichiarativo.

### SimulationWindow

La `SimulationWindow` coordina il ciclo di vita della GUI. Crea i pannelli, inizializza il modello tramite `Mvu.init` e collega i controlli alle trasformazioni MVU.

Un timer Swing con intervallo di 30 millisecondi invia periodicamente `Msg.Tick`. Il messaggio produce un nuovo tick soltanto quando la simulazione è in esecuzione. Il metodo `dispatch` applica la trasformazione al modello corrente e aggiorna entrambi i pannelli.

I pulsanti permettono di arrestare o riprendere l'esecuzione, riavviare la simulazione e tornare al menu principale. Quando la finestra viene chiusa, il timer viene arrestato, la finestra viene eliminata e viene invocata la callback fornita dal chiamante.

### SimulationPanel

**Problema:** la scena deve rappresentare spazi di forma differente e rendere riconoscibili gli agenti anche quando il tipo di stato è definito dall'utente.

**Soluzione:** `SimulationPanel` riceve le type class `Renderable[S]` e `POIRenderable`. Il pannello disegna il confine rettangolare o circolare in base a `Shape`, rappresenta i punti di interesse come regioni circolari semitrasparenti e disegna ogni agente come un elemento colorato con contorno nero.

Il colore associato allo stato dell'agente è delegato a `Renderable[S]`, mentre il colore dei punti di interesse è delegato a `POIRenderable`. Il metodo `render` conserva il modello da visualizzare e richiede il ridisegno del pannello tramite `repaint()`.

La visualizzazione dei `Point of Interest` è condizionale alla loro presenza nella simulazione. Il `SimulationPanel` attraversa la lista dei POI contenuti nell'ambiente e, quando la lista è vuota, non disegna alcuna regione aggiuntiva. Anche `StatisticsPanel` adatta la propria interfaccia allo stesso principio: l'informazione relativa ai POI viene nascosta quando la simulazione non ne definisce alcuno e viene mostrata soltanto quando esistono punti di interesse da osservare.
Questa scelta permette di utilizzare la stessa GUI sia per simulazioni che fanno uso di regioni spaziali significative sia per simulazioni che ne sono prive, senza mostrare elementi vuoti o non pertinenti.

### StatisticsPanel

**Problema:** la scena grafica mostra l'evoluzione spaziale, ma non rende immediatamente osservabili la distribuzione degli stati, le transizioni e la concentrazione degli agenti.

**Soluzione:** `StatisticsPanel` raccoglie e mostra il tick corrente, la numerosità della popolazione, il numero complessivo delle transizioni di stato, il numero di agenti presenti nei `Point of Interest`, la distribuzione percentuale degli stati e la densità spaziale tramite una griglia.

La distribuzione viene calcolata raggruppando gli agenti in base all'etichetta prodotta da `Renderable`. I dati vengono conservati in uno storico limitato, utilizzato dal grafico per tracciare l'andamento delle diverse categorie nel tempo. Il pannello mantiene inoltre una mappa degli stati precedenti degli agenti per contare le transizioni avvenute durante l'evoluzione.

Una difficoltà particolare ha riguardato la visualizzazione delle statistiche nelle simulazioni con stato continuo. Quando lo stato di un agente è rappresentato da un valore numerico, utilizzare direttamente il valore come etichetta produrrebbe una categoria distinta per quasi ogni agente e una legenda inutilmente estesa. Il problema non è legato al non determinismo della simulazione, ma alla natura continua dello stato: valori `Double` diversi possono rappresentare variazioni quantitative dello stesso fenomeno e non devono necessariamente essere visualizzati come categorie separate.
Per risolvere il problema, `Renderable[S]` separa il valore dello stato dalla sua rappresentazione. Il metodo `colorOf` associa un colore al valore, mentre `labelOf` determina l'etichetta utilizzata dal pannello statistico per raggruppare gli agenti:

```scala
trait Renderable[S]:
def colorOf(state: S): Color
def labelOf(state: S): String = state.toString
```

Per gli stati discreti è sufficiente utilizzare un'etichetta per ciascuna categoria. Nel caso della simulazione `OpinionDynamics`, il colore viene calcolato in modo continuo a partire dal valore dell'opinione. Il rapporto tra il valore corrente e `opinionRange` viene limitato all'intervallo unitario e utilizzato per interpolare il colore tra il blu, associato ai valori più bassi, e il rosso, associato ai valori più alti. L'etichetta, invece, non conserva il valore numerico preciso, ma lo colloca in una delle cinque fasce `Very Low`, `Low`, `Medium`, `High` e `Very High`. In questo modo il grafico mantiene un gradiente cromatico continuo nella scena e, contemporaneamente, raggruppa gli agenti in un numero limitato di categorie nella legenda.

```scala
given Renderable[Opinion] with

override def colorOf(state: Opinion): Color =
 val ratio = (state.value / opinionRange).max(0).min(1)
 Color(ratio.toFloat, 0f, (1 - ratio).toFloat)

override def labelOf(state: Opinion): String = state.value match
 case v if v < 2.0 => "Very Low"
 case v if v < 4.0 => "Low"
 case v if v < 6.0 => "Medium"
 case v if v < 8.0 => "High"
 case _            => "Very High"
```

Lo `StatisticsPanel` raggruppa quindi gli agenti in base al risultato di `labelOf`, anziché in base al valore concreto dello stato:

```scala
 val groupedAgents = agents.groupBy(agent => renderable.labelOf(agent.state))
 val snapshot = groupedAgents.map((label, group) => label -> group.size.toDouble / total * 100)
```

In questo modo tutti gli agenti appartenenti alla stessa fascia vengono rappresentati sotto un'unica voce della legenda e con un unico colore. La statistica descrive così l'evoluzione delle categorie significative del modello invece di produrre una voce per ogni valore numerico osservato.

`StatisticsPanel` non costituisce soltanto un elemento accessorio della GUI, ma uno strumento di analisi dell'evoluzione del modello. Il grafico storico permette di osservare come cambiano nel tempo le proporzioni delle categorie, mentre il conteggio delle transizioni evidenzia quante variazioni di stato sono avvenute durante l'esecuzione. La visualizzazione della densità aggiunge una prospettiva spaziale alle statistiche aggregate, mostrando come la popolazione si distribuisce nell'ambiente.
La combinazione di queste viste consente di leggere la simulazione secondo tre dimensioni complementari: composizione della popolazione, evoluzione temporale e distribuzione spaziale.

La scelta di utilizzare `Renderable.labelOf` come chiave di raggruppamento separa la rappresentazione statistica dal valore concreto dello stato. Questa separazione è particolarmente importante nel caso di stati continui, perché permette di modificare la granularità dell'analisi senza modificare né il modello né l'Engine. Il framework può quindi rappresentare lo stesso stato numerico con diversi livelli di dettaglio, ad esempio utilizzando intervalli più o meno ampi.
Il pannello mantiene inoltre coerenti i colori e le categorie durante l'intera esecuzione. Quando una categoria non è presente in un determinato tick, essa non viene eliminata dalla legenda, ma viene registrata con percentuale nulla. In questo modo il grafico conserva la continuità visiva delle serie e rende confrontabile l'evoluzione delle categorie anche quando alcuni stati scompaiono temporaneamente dalla popolazione.

Le transizioni vengono conteggiate confrontando ad ogni tick l'etichetta corrente di ogni agente con quella registrata al tick precedente:

```scala
val currentLabels = agents.map(agent => agent.id -> renderable.labelOf(agent.state)).toMap
val transitions = currentLabels.count((id, label) => previousLabels.get(id).exists(_ != label))
stateTransitions += transitions
previousLabels = currentLabels
```

La densità spaziale viene calcolata suddividendo lo spazio in una griglia di `GridSize × GridSize` celle e incrementando la cella corrispondente alla posizione di ogni agente. La dimensione della griglia è configurabile tramite una costante. Ogni cella viene colorata in base alla percentuale di agenti che contiene rispetto alla popolazione totale, seguendo una scala cromatica dal bianco, nessun agente, all'arancione scuro e al rosso per le concentrazioni più elevate. La legenda affiancata alla griglia associa ogni colore all'intervallo percentuale corrispondente. Un controllo indipendente permette di sospendere o riattivare l'aggiornamento delle statistiche senza arrestare la simulazione.

### Renderable e POIRenderable

**Problema:** il package `gui` deve poter visualizzare stati e punti di interesse di simulazioni diverse senza imporre ereditarietà o modifiche ai tipi definiti dall'utente.

**Soluzione:** `Renderable[S]` definisce il colore e l'etichetta di uno stato, mentre `POIRenderable` definisce il colore dei punti di interesse. Entrambe le type class disaccoppiano il tipo dei dati dalla loro rappresentazione e permettono di fornire implementazioni differenti per simulazioni differenti.

Quando non viene fornita una personalizzazione per i POI, viene utilizzata un'istanza di default che li rappresenta in colore grigio.

### MainMenu

Il `MainMenu` costituisce il punto di ingresso dell'applicazione. Riceve una lista di `SimulationOption` e crea un pulsante per ciascuna simulazione disponibile.

Ogni opzione espone un nome e una funzione `start`, che avvia la relativa simulazione. Quando l'utente seleziona un pulsante, il menu viene nascosto e viene aperta la finestra della simulazione. Una callback permette di rendere nuovamente visibile il menu quando la finestra viene chiusa.

Questa separazione permette di aggiungere una nuova simulazione al menu senza modificare la logica dei componenti grafici esistenti: è sufficiente fornire una nuova implementazione di `SimulationOption`.

## Implementazione - Integrazione dei componenti

La GUI non accede direttamente ai dettagli interni del dominio, ma riceve la configurazione e lo stato prodotti dall'Engine. Il flusso complessivo è il seguente:

1. `MainMenu` presenta le simulazioni disponibili;
2. `SimulationWindow` inizializza il modello MVU;
3. il timer invia periodicamente `Msg.Tick`;
4. `Mvu.update` produce una nuova versione del `Model`;
5. `SimulationPanel` ridisegna la scena;
6. `StatisticsPanel` aggiorna i dati quantitativi.

La separazione tra modello, aggiornamento e visualizzazione consente di verificare la logica MVU indipendentemente dai componenti Swing, mentre le type class di rendering mantengono la GUI indipendente dal tipo concreto dello stato degli agenti.

## Dettagli implementativi trasversali

### Validazione delle configurazioni geometriche

Le implementazioni dello spazio verificano le proprie precondizioni al momento della costruzione. Un rettangolo richiede larghezza e altezza positive, mentre un cerchio richiede un raggio positivo. In questo modo una geometria non valida non può entrare nell'ambiente e produrre errori soltanto durante l'esecuzione.

La stessa scelta viene applicata alle altre configurazioni geometriche: il raggio della ricerca dei vicini deve essere finito e non negativo, la dimensione della cella della griglia deve essere positiva e un `POI` deve avere raggio positivo e ritardo di attivazione non negativo.

Queste verifiche sono concentrate nei punti di costruzione dei valori interessati. Il resto del sistema può quindi assumere che le istanze già create rispettino le invarianti fondamentali del dominio.

### Calcolo delle distanze

La ricerca dei vicini utilizza la distanza euclidea tra le posizioni degli agenti. Per evitare il calcolo della radice quadrata durante i confronti, le strategie confrontano la distanza quadratica con il quadrato del raggio:

```scala
private def squaredDistance(first: P2d, second: P2d): Double =
  val deltaX = first.x - second.x
  val deltaY = first.y - second.y
  deltaX * deltaX + deltaY * deltaY
```

La strategia brute-force può quindi filtrare gli agenti con un confronto numerico semplice. La stessa impostazione viene utilizzata per il controllo di appartenenza agli spazi e ai punti di interesse, adattandola alla geometria specifica.

### Preparazione della strategia a griglia

La strategia `grid` associa ogni agente alla cella che contiene la sua posizione. La cella viene identificata trasformando le coordinate in indici interi tramite la dimensione configurata:

```scala
private def cellOf(position: P2d, cellSize: Double): (Int, Int) =
  (math.floor(position.x / cellSize).toInt,
   math.floor(position.y / cellSize).toInt)
```

La costruzione dell'indice viene separata dalla ricerca vera e propria. Per ogni agente, la strategia calcola quindi le celle che possono contenere un vicino entro il raggio e filtra gli agenti presenti in quelle celle usando comunque la distanza effettiva. La griglia riduce il numero di candidati senza modificare il risultato semantico della ricerca.

### Funzioni di aggiornamento dello spazio

Le operazioni `bounce`, `stop` e `wrap` restituiscono una posizione e una velocità, invece di modificare direttamente l'agente. L'Engine può quindi applicare la politica al risultato del movimento e creare successivamente una nuova istanza dell'agente.

Questa separazione è particolarmente utile per la politica `bounce`: la geometria decide come correggere la velocità, mentre l'Engine non deve conoscere se la riflessione avvenga contro un lato del rettangolo o contro la normale della circonferenza.

### Gestione del flusso grafico

La finestra non aggiorna direttamente i campi del modello della simulazione. Gli eventi grafici vengono convertiti in messaggi e passano attraverso `Mvu.update`. La funzione `refreshView` propaga poi il modello risultante ai pannelli:

```scala
def refreshView(): Unit =
  simulationPanel.render(model)
  statisticsPanel.update(model)
  toggleButton.setText(if model.running then "Stop" else "Resume")
```

Il pulsante di esecuzione riflette il valore `running` del modello. Quando il modello è in pausa, il timer continua a inviare messaggi, ma la trasformazione associata a `Tick` restituisce lo stesso modello senza avanzare la simulazione.

### Storico e aggregazione delle statistiche

Lo storico del `StatisticsPanel` è limitato a una finestra temporale configurata tramite una costante. A ogni aggiornamento viene aggiunto uno snapshot della distribuzione e vengono rimossi i valori più vecchi oltre il limite. In questo modo la dimensione dei dati visualizzati non cresce indefinitamente durante un'esecuzione prolungata.

Quando uno stato non è più presente nella popolazione corrente, la relativa categoria viene comunque conservata nella legenda e nello storico con valore percentuale pari a zero. Questo evita che una linea del grafico scompaia improvvisamente e mantiene confrontabili i diversi stati nel tempo.

### Separazione tra stato della simulazione e stato della visualizzazione

Il `Model` contiene esclusivamente i dati necessari a descrivere la simulazione e il suo stato di esecuzione. I pannelli Swing conservano invece informazioni locali alla visualizzazione: il modello corrente nel `SimulationPanel`, lo storico e le aggregazioni nel `StatisticsPanel`.

Questi dati locali non modificano la popolazione né la configurazione. Sono ricostruiti o aggiornati in funzione dei modelli ricevuti e servono soltanto a rendere possibile il disegno della scena, del grafico e della mappa di densità.

## Risultato dell'implementazione

Le componenti realizzate collaborano mantenendo separate le responsabilità. `Space` e le sue implementazioni definiscono la geometria, `BoundaryPolicy` decide il comportamento al confine, `Environment` aggrega gli elementi del mondo e `NeighborStrategy` determina come cercare gli agenti vicini. `EnvironmentBuilder` espone queste capacità attraverso una sintassi dichiarativa e produce la configurazione utilizzata dal resto del sistema.

Sul lato grafico, `SimulationWindow` coordina il ciclo di esecuzione, `Mvu` gestisce le trasformazioni del modello, `SimulationPanel` rappresenta la scena e `StatisticsPanel` fornisce una lettura quantitativa dell'evoluzione. Le type class `Renderable` e `POIRenderable` mantengono la rappresentazione indipendente dal dominio applicativo.

Il risultato è una GUI riutilizzabile per simulazioni con stati, geometrie e punti di interesse differenti, senza duplicare la logica dell'Engine e senza imporre vincoli di ereditarietà ai tipi definiti dall'utente.

[Indice](0-index.md) | [Capitolo Precedente](5-design.md) | [Capitolo Successivo](7-testing.md)