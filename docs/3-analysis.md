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

- un **identificatore univoco** (`AgentId`);
- una **posizione** nello spazio bidimensionale continuo (`P2d`);
- un **vettore velocità/spostamento** (`V2d`) che determina il movimento ad ogni
  intervallo temporale discreto;
- uno **stato generico** di tipo `S`, definito dall'utente per il dominio
  specifico.

Lo stato `S` è il tipo parametrico che garantisce la genericità del framework.
Ad esempio, in una simulazione epidemiologica `S` potrebbe essere un ADT con
casi `Susceptible`, `Infected` e `Recovered`; in una simulazione di opinion
dynamics potrebbe essere un `Double` nell'intervallo `[0, 10]`.

### Environment

L'ambiente è lo spazio bidimensionale continuo in cui gli agenti esistono e si
muovono. È caratterizzato da:

- dei **confini** (`Bounds`) che definiscono le dimensioni dello spazio;
- la lista degli agenti presenti nella simulazione;
- un metodo per calcolare i **vicini** di un agente entro un determinato raggio
  euclideo.

### Behavior

Un behavior è una funzione pura con la seguente firma:

```
AgentContext[S] => V2d
```

Dato il contesto locale di un agente, restituisce un nuovo vettore velocità che
determina come l'agente si muoverà al tick successivo.

Il framework fornisce behavior predefiniti, tra cui:

- **random walk**: movimento casuale nello spazio;
- **attrazione verso un punto**: l'agente si orienta verso una posizione target.

L'utente può utilizzare i behavior predefiniti oppure definirne di nuovi senza
modificare il motore della simulazione.

### Interaction Rule

Una interaction rule è una funzione pura con la seguente firma:

```
AgentContext[S] => Option[S]
```

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

### P2d e V2d

Il framework distingue esplicitamente tra punti e vettori nello spazio
bidimensionale.

- **`P2d`** rappresenta una posizione assoluta, ovvero una coordinata nello
  spazio. Una posizione può essere traslata applicando un vettore.
- **`V2d`** rappresenta una quantità vettoriale, ovvero una direzione e
  un'intensità di movimento.

Questa separazione — che sostituisce il precedente modello basato su `Vector2D`
— evita ambiguità matematiche (due posizioni non devono essere sommate
direttamente) e rende il modello più vicino alla rappresentazione reale dei
fenomeni fisici.


## SimulationEngine

Il `SimulationEngine` è il motore responsabile dell'evoluzione della
simulazione. È distinto dalle astrazioni di dominio (`Behavior`,
`InteractionRule`) che orchestra: il suo compito è eseguire la pipeline di
aggiornamento ad ogni tick, delegando la logica comportamentale alle funzioni
pure fornite dall'utente.

Ad ogni tick applica la seguente pipeline in modo deterministico:

1. calcolo della nuova velocità per ogni agente, delegando al `Behavior`
   configurato;
2. aggiornamento della posizione applicando il vettore velocità (`P2d + V2d`);
3. gestione del rimbalzo sui confini dell'ambiente (`Bounds`);
4. valutazione delle `InteractionRule` rispetto ai vicini, in sequenza
   deterministica;
5. produzione del nuovo stato immutabile della simulazione (`SimulationState`).

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

- l'ambiente;
- la lista degli agenti;
- il tick corrente;
- la configurazione della simulazione.

Il Model appartiene al dominio puro e non contiene riferimenti alla GUI.

### Update

L'Update è una funzione pura che descrive la transizione di stato. Riceve un
messaggio e produce un nuovo Model. I messaggi disponibili sono:

- `StartMsg` — avvia la simulazione;
- `StopMsg` — mette in pausa la simulazione;
- `ResetMsg` — riporta la simulazione allo stato iniziale;
- `TickMsg` — avanza la simulazione di un passo temporale.

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
  `AgentContext[S] => V2d`.
- Il sistema deve supportare interaction rule come funzioni pure
  `AgentContext[S] => Option[S]`.
- Il sistema deve permettere la composizione di interaction rule in sequenza
  deterministica.
- Il `SimulationEngine` deve orchestrare la pipeline di aggiornamento ad ogni
  tick, delegando la logica comportamentale a `Behavior` e `InteractionRule`.
- Il sistema deve gestire il rimbalzo degli agenti sui confini dell'ambiente.
- Il sistema deve calcolare i vicini tramite distanza euclidea entro un raggio
  configurabile.


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

- Memoria associata agli agenti.
- Registrazione degli incontri tra agenti.
- Punti di Interesse (POI) con effetti configurabili sugli agenti.
- Statistiche in tempo reale sull'andamento della simulazione.


[Indice](0-index.md) | [Capitolo Precedente](2-process.md) | [Capitolo Successivo]()