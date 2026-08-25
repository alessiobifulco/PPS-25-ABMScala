---
title: Design di Dettaglio
nav_order: 5
parent: Report
---

# Design di Dettaglio

## Panoramica

In questa sezione viene approfondito il design delle componenti chiave del progetto, illustrando le principali responsabilità funzionali, le scelte implementative e le interazioni tra i moduli. L'analisi segue la suddivisione in quattro livelli introdotta nel design architetturale: il **Domain**, che modella i concetti di agente, spazio e regola; il **DSL**, che espone la sintassi dichiarativa con cui l'utente descrive una simulazione; l'**Engine**, che esegue il ciclo di aggiornamento; e la **GUI**, che visualizza lo stato secondo il pattern **Model-View-Update (MVU)**.

Trasversalmente a tutti i livelli valgono due scelte di fondo: l'**immutabilità** delle strutture dati di dominio, per cui ogni passo di simulazione produce un nuovo stato senza modificare il precedente, e la **parametricità sullo stato dell'agente** (`S`), che rende il framework indipendente dal dominio applicativo concreto. L'unico stato mutabile presente è confinato nei builder del DSL e nei componenti Swing, dove serve rispettivamente ad accumulare la configurazione e a interfacciarsi con una libreria imperativa.

## Domain

Il Domain racchiude i concetti fondamentali del modello ad agenti. È completamente disaccoppiato dal DSL, dall'Engine e dalla GUI: definisce *cosa* sono un agente, uno spazio, un comportamento e una regola, senza sapere come vengano costruiti né come vengano eseguiti.

### Rappresentazione dello Spazio

Lo spazio della simulazione è descritto dal trait **`Space`**, che astrae la geometria del mondo e la gestione dei confini.

* **Responsabilità**:
    * Verificare l'appartenenza di una posizione al mondo (`contains`) e riportarla al suo interno (`clamp`)
    * Definire come una coppia posizione/velocità viene corretta al contatto con il bordo, secondo due modalità alternative: rimbalzo (`bounce`) e arresto (`stop`)
    * Generare posizioni casuali per il popolamento iniziale (`randomPosition`)
    * Esporre la propria geometria come valore (`shape`), disaccoppiando la rappresentazione grafica dai dettagli implementativi
* **Implementazioni**:
    * `RectangularSpace`: mondo rettangolare definito da larghezza e altezza, con origine nell'angolo superiore sinistro
    * `CircularSpace`: mondo circolare definito da centro e raggio, in cui il rimbalzo è calcolato per riflessione della velocità rispetto alla normale radiale
* **Scelte di design**: la toroidalità non è una proprietà di ogni spazio, ma un trait separato **`Toroidal`** che aggiunge l'operazione `wrap`. In questo modo la possibilità di attraversare i bordi è espressa a livello di tipo e non come metodo opzionale, e uno spazio non toroidale non può essere usato con una politica di attraversamento

La geometria concreta è modellata dall'`enum` **`Shape`**, i cui casi `Rectangle` e `Circle` trasportano i parametri necessari al rendering. La posizione è rappresentata dalla `case class` **`P2d`** e lo spostamento dalla `case class` **`V2d`**: entrambe sono arricchite tramite **extension methods** con gli operatori algebrici (`+`, `-`, `*`, `normalized`, `length`), così da rendere il codice del dominio vicino alla notazione matematica senza introdurre gerarchie di tipi.

### Politiche di Frontiera

L'`enum` **`BoundaryPolicy`** rende esplicita la strategia di gestione dei confini, con i tre casi `bounce`, `stop` e `wrap`.

* **Responsabilità**: dato uno stato di moto e uno spazio, restituire la coppia posizione/velocità corretta
* **Logica**: l'enum non contiene la matematica della correzione, ma la delega allo `Space` selezionando l'operazione appropriata. Il caso `wrap` verifica tramite pattern matching che lo spazio sia `Toroidal` e, in caso contrario, degrada al rimbalzo; un controllo equivalente è anticipato alla costruzione dell'`Environment` sotto forma di precondizione, così che l'incoerenza venga segnalata alla configurazione e non durante l'esecuzione
* **Scelte di design**: modellare la politica come enum con un metodo `apply` permette di trattarla come un valore configurabile e, al tempo stesso, di esporla nel DSL come semplice parola chiave (`withBoundary bounce`)

### Agente e Identità

Il trait **`Agent[S]`** rappresenta la singola entità simulata ed è definito come struttura puramente descrittiva: identità, posizione, velocità, stato di dominio e memoria opzionale.

* **Responsabilità**:
    * Aggregare le proprietà osservabili di un'entità, senza contenere logica comportamentale
    * Fornire operazioni di aggiornamento non distruttive tramite extension methods (`withMotion`, `withState`, `withMemory`), ciascuna delle quali restituisce una nuova istanza
    * Esporre in modo sicuro il contenuto della memoria (`remembers`), restituendo una lista vuota quando l'agente non è dotato di memoria
* **Scelte di design**:
    * L'implementazione `AgentImpl` è una `case class` **privata**, accessibile solo attraverso il companion object: il client dipende dall'astrazione e la rappresentazione interna resta libera di evolvere
    * L'identificatore è un **opaque type** `AgentId` su `Int`: garantisce type-safety a costo zero a runtime, impedendo di confondere un identificatore con un qualunque intero
    * La memoria è `Option[Memory]` perché è una capacità opzionale: le simulazioni che non ne hanno bisogno non pagano né in spazio né in complessità


### Contesto di Percezione

La `case class` **`AgentContext[S]`** è la fotografia locale del mondo su cui un agente decide: l'agente in esame, i suoi vicini, il tick corrente e la sua permanenza nei punti di interesse.

* **Responsabilità**:
    * Costituire l'unico canale attraverso cui comportamenti e regole accedono al mondo, garantendo che la decisione sia una funzione della sola informazione locale
    * Offrire interrogazioni derivate tramite extension methods: filtro dei vicini per distanza (`visibleWithin`), raccolta delle credenze udibili dai vicini (`heardBeliefs`), verifica della presenza in un punto di interesse (`isInside`) e della permanenza prolungata al suo interno (`hasSettledIn`)
* **Scelte di design**: l'alias `type Condition[S] = AgentContext[S] => Boolean` eleva il concetto di condizione a funzione di prima classe, rendendo possibile comporre i predicati del DSL con gli operatori `and` e `or` senza definire una gerarchia di classi dedicata

### Memoria e Credenze

La memoria dell'agente è modellata dal trait **`Memory`**, che conserva una lista di **`Belief`**, ciascuno costituito da un evento e dal tick in cui è stato registrato.

* **Responsabilità**:
    * Registrare un nuovo evento (`remember`) restituendo una nuova memoria
    * Applicare un limite di capacità, mantenendo solo le credenze più recenti
    * Esporre interrogazioni di uso comune: l'ultima credenza (`latest`) e le sole osservazioni di punti di interesse (`sightings`)
* **Tipi di evento**: l'`enum` **`MemoryEvent`** distingue l'osservazione diretta (`Sighting`, che trasporta identificatore e posizione del punto di interesse) dall'incontro con un altro agente (`Encounter`), lasciando la struttura aperta a ulteriori casi
* **Scelte di design**: la capacità limitata è ciò che rende la memoria un modello di *oblio* e non un semplice registro. Combinata con le condizioni temporali del DSL (`recentlySighted`, `nothingSightedIn`), consente di esprimere fenomeni come la propagazione e il progressivo esaurimento di un allarme

### Azioni e Comportamenti

L'`enum` **`Action[S]`** definisce l'insieme chiuso delle azioni che un agente può intraprendere: spostarsi (`Move`), registrare un evento nella propria memoria (`Remember`), comunicarlo a un destinatario (`Tell`), generare un nuovo agente (`Spawn`) e cessare di esistere (`Die`).

* **Responsabilità**: costituire il vocabolario dell'intenzione. Un'azione è un **dato**, non un effetto: viene prodotta dal comportamento e interpretata dall'Engine, che è l'unico punto in cui l'intenzione diventa modifica dello stato
* **Vantaggi**: la decisione resta una funzione pura e facilmente collaudabile, ed è possibile ispezionare le intenzioni prima di applicarle, come avviene per la risoluzione della morte e delle nascite

Il trait **`Behavior[S]`** associa a un eventuale stato di attivazione la funzione che produce la lista di azioni.

* **Responsabilità**:
    * Dichiarare lo stato per cui il comportamento è pertinente (`whenState`) e produrre le azioni dato il contesto (`actions`)
    * Determinare la propria applicabilità (`appliesTo`): un comportamento privo di stato di attivazione è il comportamento di default e si applica a ogni agente
* **Scelte di design**: il metodo `appliesTo` ha un'implementazione di default nel trait, così che le implementazioni concrete debbano fornire la sola logica specifica

### Regole di Interazione

Il trait **`InteractionRule[S]`** governa l'evoluzione dello stato di dominio, separandola nettamente dal comportamento.

* **Responsabilità**:
    * Dichiarare lo stato di partenza (`whenState`) e la condizione contestuale (`context`) che ne abilitano l'applicazione
    * Calcolare il nuovo stato dell'agente a partire dal contesto (`newState`)
* **Scelte di design**: la distinzione tra `Behavior` e `InteractionRule` riflette la distinzione tra *come un agente agisce* e *come un agente cambia*. Un agente infetto si muove più velocemente perché lo dice un comportamento, ma guarisce perché lo dice una regola; le due dimensioni possono essere modificate indipendentemente


### Strategie di Vicinato

Il trait **`NeighborStrategy[S]`** astrae il calcolo dei vicini, che è l'operazione più onerosa dell'intera simulazione.

* **Responsabilità**:
    * Restituire, dato l'insieme degli agenti e un raggio, una **funzione** che associa a ciascun agente i propri vicini (`prepare`)
    * Offrire l'interrogazione puntuale (`neighborsOf`) come caso particolare della precedente
* **Implementazioni**:
    * `BruteForceStrategy`: confronto di ogni agente con tutti gli altri, adeguato a popolazioni contenute
    * `GridStrategy`: indicizzazione spaziale su celle di lato configurabile, con esame delle sole celle che intersecano il raggio di percezione
* **Scelte di design**:
    * La firma di `prepare` è la scelta centrale: separando la fase di preparazione da quella di interrogazione, l'indice spaziale viene costruito **una sola volta per tick** e non una volta per agente
    * Il confronto tra distanze avviene sui quadrati, evitando il calcolo della radice quadrata
    * Una **given instance** di default rende la strategia un parametro implicito: la simulazione funziona senza che l'utente debba occuparsene, ma resta possibile sostituirla

## DSL

Il DSL è il livello con cui l'utente del framework descrive una simulazione. L'obiettivo di design è che una simulazione sia leggibile come un documento dichiarativo, in cui la struttura del testo coincide con la struttura del modello.

### Struttura a Builder e Context Function

La costruzione avviene tramite quattro builder cooperanti — `SimulationBuilder`, `EnvironmentBuilder`, `BehaviorsBuilder` e `RulesBuilder` — attivati dai blocchi `environment`, `behavior` e `rules`.

* **Responsabilità**:
    * `EnvironmentBuilder`: raccogliere spazio, politica di frontiera, raggio di percezione, dimensione e stato iniziale della popolazione, criterio di posizionamento, capacità di memoria e punti di interesse, validandoli e producendo un `EnvironmentSpec`
    * `BehaviorsBuilder` e `RulesBuilder`: accumulare rispettivamente i comportamenti e le regole dichiarati all'interno del proprio blocco
    * `SimulationBuilder`: aggregare le tre parti e materializzarle nella `SimulationConfig` finale, istanziando la popolazione iniziale a partire dalle funzioni generatrici
* **Meccanismo**: ogni blocco è una **context function** (`Builder[S] ?=> Unit`). Il builder viene creato dal blocco stesso e reso disponibile come parametro `using` a tutte le costruzioni annidate, che possono quindi registrarsi senza mai essere nominate esplicitamente dall'utente. È questo il meccanismo che consente di scrivere `population(250) of Healthy withOne Infected` come istruzione autonoma
* **Scelte di design**:
    * Lo stato mutabile è **confinato** nelle implementazioni private dei builder e non sopravvive alla costruzione: l'esito è una `SimulationConfig` immutabile
    * Le classi di configurazione intermedie (`SpaceConfig`, `PopulationConfig`) esistono per rendere possibile l'incatenamento dei modificatori `infix`, mantenendo l'ordine di lettura naturale
    * `BehaviorsBuilder` ordina i comportamenti raccolti in modo che quello di default risulti sempre ultimo, rendendo l'esito indipendente dall'ordine in cui l'utente li ha scritti
    * Le configurazioni incomplete o incoerenti sono intercettate in fase di costruzione tramite precondizioni e messaggi espliciti


### Comportamenti Condizionali

L'object **`ConditionalBehavior`** introduce il tipo `ActionSource[S]`, alias per `AgentContext[S] => List[Action[S]]`, e su di esso costruisce un'algebra di composizione.

* **Sorgenti di base**: movimento casuale (`moveRandomly`), orizzontale (`moveHorizontally`), verso o lontano da un punto (`moveTowards`, `moveAwayFrom`) o da una posizione ricordata (`moveTowardsRemembered`, `moveAwayFromRemembered`), arresto (`stopMoving`), osservazione dei punti di interesse (`rememberSightings`), comunicazione (`tellNeighbours`, `learnFromNeighbours`), riproduzione (`spawn`) e morte (`die`)
* **Combinatori**, definiti come extension methods `infix`:
    * `to`: concatena due sorgenti, unendone le azioni
    * `orElse`: applica la seconda sorgente solo se la prima non produce azioni, realizzando un fallback
    * `onlyIf`: subordina l'esecuzione a una condizione sul contesto
    * `vanishingWith`: aggiunge la morte dell'agente con una data probabilità
    * `whenAgentIs`: registra la sorgente come comportamento associato a uno stato, mentre `asDefault` la registra come comportamento generale
* **Scelte di design**: modellare la sorgente di azioni come semplice alias di funzione, anziché come trait, rende gratuita la composizione e consente all'utente di definire sorgenti personalizzate come normali funzioni, ottenendo automaticamente tutti i combinatori

### Comportamenti Compositi

L'object **`CompositeBehavior`** implementa il comportamento di stormo, in cui la direzione di un agente nasce dalla somma pesata di più contributi.

* **Responsabilità**: calcolare le componenti di **coesione** verso il baricentro dei simili, **allineamento** alla loro velocità media, **separazione** dagli agenti troppo vicini o da evitare, e **mantenimento della direzione** corrente, combinandole in un'unica azione di movimento
* **Configurazione**: la classe `FlockConfig` espone i parametri come metodi `infix` incatenabili (`avoid`, `movingAt`, `keepingApart`, `withCohesion`, `withAlignment`, `withSeparation`, `withHeading`), ciascuno dotato di un valore di default sensato
* **Scelte di design**:
    * L'appartenenza allo stormo non è determinata dall'identità di stato ma da un **predicato binario** sugli stati, il che permette di formare gruppi per similarità e non solo per uguaglianza
    * `FlockConfig` estende `ActionSource`, quindi lo stormo è una sorgente di azioni come tutte le altre e può essere combinato con i medesimi operatori
    * Una funzione ausiliaria di normalizzazione con fallback protegge dai vettori nulli, evitando che un agente privo di stimoli resti immobile

### Regole Discrete

L'object **`DiscreteRules`** fornisce la sintassi per le transizioni di stato e il vocabolario di condizioni su cui si fondano.

* **Sintassi**: la forma `NuovoStato whenAgentIs StatoPrecedente iff condizione` è ottenuta in due passi. Un extension method su un valore di stato produce una `Transition`, che rappresenta la coppia stato di partenza/stato di arrivo; un secondo extension method sulla transizione accetta la condizione e registra la regola risultante presso il `RulesBuilder` implicito
* **Condizioni disponibili**: conteggio dei vicini in un dato stato (`atLeastNear`, `exactlyNear`, `fewerNear`), probabilità (`chanceOf`), relazione con i punti di interesse (`inside`, `settledIn`), distanza da un punto (`farFrom`) e condizioni sulla memoria (`recentlySighted`, `nothingSightedIn`)
* **Composizione**: gli operatori `infix` `and` e `or` combinano le condizioni preservandone il tipo, così che una condizione composta resti utilizzabile ovunque lo sia una condizione semplice
* **Scelte di design**: `Transition` è un trait pubblico con implementazione privata, ma la sua costruzione è riservata all'interno del modulo: l'utente lo incontra solo come passaggio intermedio della sintassi, mai come tipo da nominare

### Regole Continue

L'object **`ContinuousRules`** affronta il caso in cui lo stato non sia un insieme finito di casi ma una grandezza numerica.

* **Meccanismo**: il trait **`Continuous[S]`** è una **type class** che estrae un valore numerico dallo stato e vi reinserisce un valore aggiornato. Definendone una given instance, un qualunque tipo di stato diventa idoneo alle regole continue
* **Regola fornita**: `convergeTowardsAverage` sposta il valore dell'agente verso la media dei vicini influenti, con parametri per il raggio di influenza, il criterio di affinità e il tasso di convergenza, tutti dotati di valori di default
* **Scelte di design**: l'uso di una type class anziché di un vincolo di ereditarietà mantiene il tipo di stato dell'utente completamente libero: `Opinion` resta una normale `case class`, e l'adattamento al framework è esterno e non invasivo

### Probabilità e Facciata

L'**opaque type `Chance`** incapsula una probabilità, validandone l'appartenenza all'intervallo unitario alla costruzione ed esponendo l'estrazione tramite `happens`. Il tipo opaco impedisce di passare un `Double` qualsiasi dove è attesa una probabilità.

L'object **`Simulation`** è la facciata del DSL: tramite la clausola **`export`** ripubblica in un unico punto tutte le costruzioni dei moduli sottostanti, così che l'utente debba importare un solo namespace. Il metodo `of` apre il blocco di configurazione e restituisce la `SimulationConfig` risultante.

## Engine

L'Engine è il livello che trasforma una configurazione dichiarativa in un'evoluzione temporale. È realizzato come object senza stato: l'intero stato della simulazione è trasportato dal valore `SimulationState`.

### Stato e Configurazione

* **`SimulationConfig[S]`**: aggrega ambiente iniziale, comportamenti, raggio di percezione, regole e strategia di vicinato. È il prodotto del DSL e l'input dell'Engine
* **`SimulationState[S]`**: contiene l'ambiente corrente, il tick, il prossimo identificatore disponibile e la mappa delle permanenze nei punti di interesse, esponendo l'accesso sicuro a queste ultime tramite `residencyOf`
* **Scelte di design**: separare configurazione e stato distingue ciò che è fisso per l'intera simulazione da ciò che evolve, e rende il riavvio un'operazione elementare, ottenuta rigenerando lo stato dalla configurazione immutata

### Pipeline del Tick

Il metodo `tick` è il cuore del sistema e organizza l'aggiornamento in fasi successive, ciascuna affidata a una funzione privata dedicata.

* **Percezione (`perceive`)**: prepara **una sola volta** la funzione di ricerca dei vicini tramite la strategia configurata, e costruisce per ogni agente il proprio `AgentContext`, completo di vicini, tick e permanenze
* **Decisione (`decide`)**: seleziona il **primo** comportamento applicabile e ne raccoglie le azioni, applica lo spostamento risultante e infine la **prima** regola applicabile per aggiornare lo stato di dominio. Il risultato è un `Intent`, struttura privata che accoppia l'agente aggiornato alle azioni che ha dichiarato
* **Evoluzione della popolazione (`grow`)**: attraversa gli intenti accumulando i sopravvissuti e i nuovi nati in una struttura `Population`, che incapsula anche l'assegnazione progressiva degli identificatori garantendone l'unicità
* **Comunicazione (`deliver`)**: estrae dalle azioni tutti i messaggi diretti e li recapita ai rispettivi destinatari, registrandoli nella loro memoria
* **Permanenze (`residenciesOf`)**: per ogni agente e per ogni punto di interesse, incrementa il contatore di permanenza se l'agente si trova all'interno, azzerandolo altrimenti. È questo meccanismo a rendere esprimibile la condizione `settledIn`, che distingue il transito occasionale dalla sosta effettiva
* **Scelte di design**:
    * La scelta del **primo** comportamento e della **prima** regola applicabili rende l'esito deterministico e attribuisce all'ordine di dichiarazione il significato di priorità, coerentemente con l'ordinamento operato dal `BehaviorsBuilder`
    * Le fasi sono nettamente separate: tutti gli agenti percepiscono lo stato del tick precedente prima che qualunque aggiornamento sia applicato, evitando che l'ordine di elaborazione influenzi il risultato
    * L'intero tick è una funzione pura da stato a stato, il che rende la simulazione riproducibile e collaudabile senza alcuna infrastruttura di supporto


### Interpretazione delle Azioni

L'Engine è l'unico interprete del vocabolario definito da `Action`.

* **`Move`**: le velocità dichiarate vengono sommate; in assenza di azioni di movimento viene conservata la velocità corrente. La posizione risultante è poi filtrata dalla politica di frontiera dell'ambiente, che restituisce la coppia posizione/velocità definitiva
* **`Remember`**: l'evento è registrato nella memoria dell'agente con il tick corrente, senza effetto se l'agente non è dotato di memoria
* **`Tell`**: il messaggio è raccolto e recapitato nella fase di comunicazione, in modo che un agente possa ricevere informazioni anche da agenti elaborati dopo di lui nello stesso tick
* **`Spawn`**: genera nuovi agenti nella posizione del genitore, con identificatori progressivi e direzione iniziale casuale
* **`Die`**: la presenza dell'azione esclude l'agente dalla popolazione del tick successivo; poiché la verifica precede l'applicazione delle altre azioni, la morte prevale su qualunque altro effetto dichiarato nello stesso tick

## GUI

La GUI è realizzata in **Swing** e organizzata secondo il pattern **Model-View-Update**, che ne mantiene la logica funzionale nonostante la natura imperativa della libreria grafica sottostante.

### Ciclo Model-View-Update

* **`Model[S]`**: aggrega lo stato della simulazione, la configurazione e il flag di esecuzione; è costruito a partire dalla sola `SimulationConfig`
* **`Msg`**: `enum` che elenca gli eventi possibili — avanzamento (`Tick`), avvio/arresto (`ToggleRun`) e riavvio (`RestartAndRun`)
* **`Mvu`**: definisce la funzione di aggiornamento come pattern matching sul messaggio, restituendo per ciascuno la corrispondente trasformazione del modello. L'avanzamento è ignorato quando la simulazione è in pausa, e il riavvio è ottenuto **componendo** le trasformazioni elementari di reinizializzazione e ripresa
* **Scelte di design**: la trasformazione è espressa come **monade di stato** `State[Model[S], Unit]`, con il trait `Monad` e la given instance `stateMonad` definiti nel modulo. Questo permette di comporre più aggiornamenti tramite `flatMap` mantenendo il passaggio del modello implicito, e mantiene la logica dell'interfaccia pura e verificabile indipendentemente da Swing


### Finestra di Simulazione

L'object **`SimulationWindow`** costruisce l'interfaccia di una singola simulazione e realizza il collegamento con il ciclo MVU.

* **Responsabilità**:
    * Comporre il pannello di rendering, quello statistico e la barra dei comandi
    * Dimensionare l'area di disegno a partire dalla `Shape` dello spazio, adattandosi automaticamente a mondi rettangolari e circolari
    * Generare l'avanzamento temporale mediante un `Timer` a intervallo fisso, che si limita a inviare un messaggio di avanzamento
    * Instradare ogni interazione dell'utente in un messaggio, applicare l'aggiornamento e ridisegnare
    * Gestire il ritorno al menu, arrestando il timer e liberando la finestra
* **Scelte di design**: la funzione di dispatch è l'**unico** punto in cui il modello viene riassegnato. Tutti i controlli producono messaggi e nessuno modifica direttamente lo stato, per cui il flusso resta unidirezionale anche in un contesto a callback

### Rendering e Statistiche

* **`SimulationPanel`**: disegna il contorno dello spazio, i punti di interesse e gli agenti, centrando il mondo nell'area disponibile mediante una traslazione del contesto grafico. Non conserva alcuno stato proprio se non l'ultimo modello ricevuto, e ridisegna interamente la scena a ogni aggiornamento
* **`StatisticsPanel`**: affianca alla visualizzazione l'andamento quantitativo della simulazione, mostrando tick, numero di agenti, numero di transizioni di stato e punti di interesse, insieme al grafico storico della composizione della popolazione e alla mappa di densità spaziale. Mantiene la storia delle composizioni e le etichette precedenti dei singoli agenti, necessarie a rilevare le transizioni, e permette di sospendere la raccolta indipendentemente dalla simulazione
* **`Renderable[S]` e `POIRenderable`**: **type class** che definiscono l'aspetto di uno stato e di un punto di interesse. Il colore è obbligatorio, mentre l'etichetta usata dalle statistiche ha un'implementazione di default basata sulla rappresentazione testuale dello stato. `POIRenderable` fornisce inoltre una given instance di default, così che i punti di interesse siano visualizzabili senza alcuna configurazione
* **Scelte di design**: l'uso di type class per la presentazione è il complemento della parametricità sul tipo di stato. Il framework non conosce i colori di alcun dominio applicativo; è la simulazione a dichiararli, e il compilatore a verificare che tale dichiarazione esista

### Menu Principale

L'object **`MainMenu`** presenta l'elenco delle simulazioni disponibili. Ciascuna voce è descritta dal trait **`SimulationOption`**, che espone un nome e la procedura di avvio, la quale riceve la funzione di ritorno al menu. In questo modo il menu non conosce né le configurazioni né i tipi di stato delle simulazioni che elenca, e l'aggiunta di una nuova simulazione non richiede alcuna modifica al codice esistente.

## Simulazioni di Esempio

Le quattro simulazioni incluse non sono soltanto dimostrazioni, ma il principale strumento di validazione dell'espressività del DSL: ciascuna è stata scelta per esercitare una diversa combinazione di funzionalità.

* **Epidemic**: modello a stati discreti su spazio rettangolare. Esercita le transizioni probabilistiche, le condizioni sul vicinato, la differenziazione del movimento per stato e la scomparsa graduale degli agenti
* **Opinion Dynamics**: modello a stato continuo su spazio circolare toroidale. Esercita la type class `Continuous`, la regola di convergenza verso la media e il comportamento di stormo con affinità basata su un predicato anziché sull'uguaglianza
* **Ant Colony**: esercita i punti di interesse con ritardo di attivazione, la memoria con capacità limitata, la comunicazione tra agenti e la composizione di sorgenti di azioni con fallback, in cui la ricerca casuale interviene solo in assenza di ricordi utili
* **Alarm Spreading**: esercita la propagazione dell'informazione e il suo esaurimento, combinando le condizioni temporali sulla memoria, il posizionamento iniziale personalizzato e la fuga da un punto di interesse

