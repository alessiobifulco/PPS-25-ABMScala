# Analisi dei Requisiti

## Requisiti di Business

L'obiettivo del progetto è la realizzazione di un framework generico per la
modellazione e l'esecuzione di simulazioni Agent-Based in Scala 3, secondo il
paradigma funzionale. Il framework dovrà consentire a un utente di definire
simulazioni componendo dichiarativamente le astrazioni fondamentali del sistema
attraverso un DSL interno.

Il sistema dovrà dimostrare la propria flessibilità attraverso simulazioni
dimostrative in domini applicativi differenti, costruite combinando le stesse
componenti sullo stesso motore di esecuzione. L'obiettivo è evidenziare come
comportamenti complessi — non programmati esplicitamente — emergano dalle
interazioni locali tra gli agenti.


## Modello di Dominio

Il dominio del framework è composto dalle seguenti entità fondamentali.

### Agent

Un agente è l'entità autonoma della simulazione. Ogni agente è caratterizzato da:

- un **identificatore univoco** (`AgentId`), un tipo distinto da un semplice
  `Int` per evitare di confondere accidentalmente l'id di un agente con altri
  identificatori numerici presenti nel sistema (es. l'id di un punto di
  interesse);
- una **posizione** nello spazio bidimensionale continuo (`P2d`);
- un **vettore velocità/spostamento** (`V2d`) che determina il movimento ad ogni
  intervallo temporale discreto;
- uno **stato generico** di tipo `S`, definito dall'utente per il dominio
  specifico;
- una **memoria** opzionale (`Option[Memory[S]]`), assente di default, che
  l'agente può utilizzare per ricordare eventi passati (si veda la sezione
  *Memory*).

Lo stato `S` è il tipo parametrico che garantisce la genericità del framework.
Ad esempio, in una simulazione epidemiologica `S` potrebbe essere un ADT con
casi `Susceptible`, `Infected` e `Recovered`; in una simulazione di opinion
dynamics potrebbe essere un `Double` nell'intervallo `[0, 10]`.

### Environment

L'ambiente è lo spazio bidimensionale continuo in cui gli agenti esistono e si
muovono. È caratterizzato da:

- dei **confini** (`Bounds`) che definiscono le dimensioni dello spazio;
- una **politica di confine** (`BoundaryPolicy`) che determina cosa accade a un
  agente che raggiunge il bordo dello spazio (rimbalzo o avvolgimento
  toroidale);
- la lista degli agenti presenti nella simulazione;
- un metodo per calcolare i **vicini** di un agente entro un determinato raggio
  euclideo, delegato a una strategia (`NeighborStrategy`) configurabile.

### Action

Un'action rappresenta un effetto che un agente può produrre durante un tick,
distinto dal semplice movimento. Il framework definisce quattro tipologie di
azione:

- **Move** — modifica la velocità dell'agente stesso, determinandone lo
  spostamento;
- **Nudge** — applica una trasformazione allo stato di un altro agente
  identificato tramite `AgentId` (es. contagio, influenza reciproca);
- **ShareMemory** — trasmette un evento a un altro agente, che verrà registrato
  nella sua memoria;
- **MultiAction** — compone più azioni in un'unica azione, per rappresentare
  effetti multipli prodotti nello stesso tick (es. muoversi e comunicare
  contemporaneamente).

La distinzione tra i quattro tipi consente al motore di interpretare in modo
non ambiguo l'intento di un'azione, separando movimento, modifica di stato
altrui e comunicazione come effetti concettualmente diversi.

Le azioni possono essere costruite dichiarativamente tramite un `ActionGraph`,
un albero di decisione che permette di esprimere logiche condizionali del tipo
*"se la condizione X è vera, produci l'azione Y, altrimenti valuta il branch
successivo"*, senza ricorrere a `match` annidati all'interno del `Behavior`.

### Behavior

Un behavior è una funzione pura con la seguente firma:

* AgentContext[S] => List[Action[S]]

Dato il contesto locale di un agente, restituisce l'insieme di azioni che
l'agente intende produrre in quel tick (movimento, modifiche allo stato di
altri agenti, comunicazione). Un behavior può anche essere costruito a partire
da un `ActionGraph`, delegando ad esso la logica decisionale.

Il framework fornisce behavior predefiniti, tra cui:

- **random walk**: movimento casuale nello spazio;
- **flocking**: comportamento a sciame che combina coesione, allineamento,
  separazione e inerzia rispetto agli agenti vicini.

L'utente può utilizzare i behavior predefiniti oppure definirne di nuovi senza
modificare il motore della simulazione.

### Interaction Rule

Una interaction rule è una funzione pura con la seguente firma:

Dato il contesto locale di un agente, restituisce opzionalmente un nuovo stato.
Se la regola non si applica viene restituito `None` e lo stato dell'agente
rimane invariato.

Le regole sono **composabili**: il framework permette di definire sequenze di
regole applicate in ordine deterministico. Questo permette di modellare fenomeni
complessi combinando interazioni locali semplici.

### AgentContext

`AgentContext[S]` rappresenta la **percezione locale dell'agente** durante una
valutazione. Non è una vista globale dell'ambiente, ma l'insieme delle
informazioni che un agente può utilizzare per prendere decisioni. Contiene:

- l'agente corrente su cui viene valutato il comportamento (`focus`);
- la lista degli agenti vicini entro il raggio configurato (`neighbors`);
- il tempo discreto corrente della simulazione (`tick`).

La separazione tramite `AgentContext` consente di modellare il comportamento
degli agenti come **locale ed emergente**.

### Memory

La memoria rappresenta la capacità di un agente di ricordare eventi passati.
È un campo opzionale di `Agent`, assente di default: un agente senza memoria
si comporta in modo puramente reattivo rispetto al proprio contesto attuale.

Nella sua forma completa, la memoria conterrà una collezione di eventi
(incontri con altri agenti, segnalazioni ricevute, avvistamenti di punti di
interesse), con una finestra di eventi ricordati limitata da una capacità
configurabile. Il modello dettagliato degli eventi e delle operazioni di
interrogazione della memoria è in fase di sviluppo (si veda *Requisiti
Opzionali*).

### P2d e V2d

Il framework distingue esplicitamente tra punti e vettori nello spazio
bidimensionale.

- **`P2d`** rappresenta una posizione assoluta, ovvero una coordinata nello
  spazio. Una posizione può essere traslata applicando un vettore.
- **`V2d`** rappresenta una quantità vettoriale, ovvero una direzione e
  un'intensità di movimento.

Questa separazione evita ambiguità matematiche (due posizioni non devono
essere sommate direttamente) e rende il modello più vicino alla
rappresentazione reale dei fenomeni fisici.


## SimulationEngine

Il `SimulationEngine` è il motore responsabile dell'evoluzione della
simulazione. È distinto dalle astrazioni di dominio (`Behavior`,
`InteractionRule`) che orchestra: il suo compito è eseguire la pipeline di
aggiornamento ad ogni tick, delegando la logica comportamentale alle funzioni
pure fornite dall'utente.

Ad ogni tick applica la seguente pipeline in modo deterministico:

1. calcolo delle azioni prodotte da ogni agente, delegando al `Behavior`
   configurato, ed espansione delle eventuali `MultiAction` in azioni singole;
2. applicazione delle azioni `Move`: aggiornamento della posizione e velocità
   di ogni agente secondo la `BoundaryPolicy` configurata;
3. applicazione delle azioni `Nudge` ricevute da ciascun agente, con
   trasformazione dello stato;
4. applicazione delle azioni `ShareMemory` ricevute, con aggiornamento della
   memoria del destinatario;
5. valutazione delle `InteractionRule` rispetto ai vicini, in sequenza
   deterministica, per il calcolo dello stato finale;
6. produzione del nuovo stato immutabile della simulazione (`SimulationState`).

Il motore non modifica direttamente gli agenti né l'ambiente corrente: ogni
tick produce un nuovo `SimulationState`, garantendo l'immutabilità del sistema.

La separazione tra `SimulationEngine` e le astrazioni comportamentali
(`Behavior`, `InteractionRule`) è intenzionale: il motore è un componente
generico e riusabile, indipendente dal dominio della simulazione. Tutta la
logica specifica del dominio risiede nelle funzioni pure che gli vengono
passate come configurazione.


## GameLoop — Pattern Model-View-Update (MVU)

Il sistema grafico adotta il pattern **Model-View-Update (MVU)** per separare
la logica della simulazione dagli effetti legati all'interfaccia utente.

### Model

Il Model rappresenta lo stato corrente della simulazione in un dato istante.
Contiene:

- lo stato della simulazione (`SimulationState`), a sua volta comprendente
  l'ambiente e il tick corrente;
- la configurazione della simulazione (`SimulationConfig`);
- un flag che indica se la simulazione è in esecuzione o in pausa.

Il Model appartiene al dominio puro e non contiene riferimenti alla GUI.

### Update

L'Update è una funzione pura che descrive la transizione di stato. Riceve un
messaggio e produce un nuovo Model. I messaggi disponibili sono:

- `Tick` — avanza la simulazione di un passo temporale, se in esecuzione;
- `ToggleRun` — alterna lo stato tra esecuzione e pausa;
- `Restart` — riporta la simulazione allo stato iniziale, riavviandola.

L'Update non esegue operazioni grafiche e non modifica dati esistenti.

### View

La View è l'unica componente con side-effect. Riceve il Model corrente e
aggiorna la rappresentazione grafica. Il suo compito è esclusivamente
visualizzare lo stato della simulazione: non contiene logica di dominio e non
modifica il Model.

Questa architettura garantisce che:

- il motore della simulazione rimanga completamente puro e testabile;
- la GUI sia separata dalla logica applicativa;
- gli aggiornamenti siano prevedibili e riproducibili;
- il framework possa essere utilizzato anche senza interfaccia grafica.


## Requisiti Funzionali

### Di sistema

- Il sistema deve permettere la definizione di agenti con stato generico
  parametrico `S`.
- Il sistema deve supportare behavior come funzioni pure
  `AgentContext[S] => List[Action[S]]`.
- Il sistema deve supportare azioni tipizzate (`Move`, `Nudge`, `ShareMemory`,
  `MultiAction`) come effetti distinti prodotti da un agente in un tick.
- Il sistema deve permettere la costruzione dichiarativa di behavior tramite
  un `ActionGraph` a foglie e branch condizionali.
- Il sistema deve supportare interaction rule come funzioni pure
  `AgentContext[S] => Option[S]`.
- Il sistema deve permettere la composizione di interaction rule in sequenza
  deterministica.
- Il `SimulationEngine` deve orchestrare la pipeline di aggiornamento ad ogni
  tick, delegando la logica comportamentale a `Behavior` e `InteractionRule`.
- Il sistema deve gestire il comportamento sui confini dell'ambiente tramite
  una `BoundaryPolicy` configurabile (rimbalzo o avvolgimento toroidale).
- Il sistema deve calcolare i vicini tramite distanza euclidea entro un raggio
  configurabile, delegando a una `NeighborStrategy`.


## Requisiti Non Funzionali

- **Copertura test**: superiore al 70%, misurata tramite SCoverage.
- **Genericità**: il framework deve supportare stati arbitrari `S`, senza
  vincoli sul tipo.


## Requisiti di Implementazione

- Linguaggio: Scala 3.x.
- Build: SBT.
- Testing: ScalaTest.
- Compatibilità: Linux, Windows e macOS.
- Documentazione: ScalaDoc.


## Requisiti Opzionali

- Modello completo della memoria: tipologie di evento (incontri, segnalazioni,
  avvistamenti), capacità configurabile, operazioni di interrogazione.
- Punti di Interesse (POI) con effetti configurabili sugli agenti ed eventuale
  ritardo di attivazione (`Residency`).
- Statistiche in tempo reale sull'andamento della simulazione.
- Esportazione dei dati prodotti dalla simulazione.
- Astrazione di Path/WayPoint per instradare gli agenti.


[Indice](0-index.md) | [Capitolo Precedente](2-process.md) | [Capitolo Successivo]()