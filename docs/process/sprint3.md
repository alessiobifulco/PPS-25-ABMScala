---
title: Sprint 3
layout: default
nav_order: 2
parent: Processo di Sviluppo
---

# Sprint 3 - DSL Dichiarativo e View Monadica

## Obiettivo

L'obiettivo di questo Sprint è duplice e punta a migliorare l'ergonomia e l'architettura del
framework.

In primo luogo verrà sviluppato un DSL (Domain Specific Language) dichiarativo, sfruttando le
feature di Scala 3 come le *context function*, per consentire una configurazione fluida ed
espressiva delle simulazioni, andando a sostituire la configurazione manuale di
`SimulationConfig`.

In secondo luogo il livello della View verrà refattorizzato applicando pattern funzionali puri,
introducendo le *monad*. Infine, verranno introdotti i Point of Interest (`POI`) come nuovo
elemento configurabile dell'`Environment`.

## Deadline

La scadenza dello sprint è il 08/08/2026.

## Backlog

| Product Backlog Item        | Sprint Task                                                                             | Volontario | Stima iniziale (h) | Ore rimanenti a fine Sprint 3 |
|-----------------------------|-----------------------------------------------------------------------------------------|------------|--------------------|-------------------------------|
| **DSL Core**                | Astrazione base del DSL (`SimulationBuilder` e context function)                        | AB         | 4                  | 0                             |
|                             | DSL per la definizione di `Environment`, `Space` e `BoundaryPolicy`                     | AB         | 2                  | 0                             |
|                             | DSL per la definizione di population, `Agent` e stato iniziale                          | AB         | 3                  | 0                             |
|                             | DSL per la composizione di `Behavior` e `InteractionRule`                               | AB         | 3                  | 0                             |
| **Popolazione dinamica**    | `Action` `Spawn` e `Die` e interpretazione estendibile tramite `ActionHandler`          | AB         | 2                  | 0                             |
|                             | Adattamento di `SimulationEngine` a una popolazione di dimensione variabile             | AB         | 2                  | 0                             |
|                             | Costruttori del DSL per nascita e morte (`spawn`, `die`, `stopMoving`, `vanishingWith`) | AB         | 0.5                | 0                             |
|                             | Stato `Dead` e decadimento dei cadaveri in *Epidemic*                                   | AB         | 0.5                | 0                             |
| **Refactoring del dominio** | Sostituzione di `ActionGraph` con `Decision` e `Choice`                                 | AB         | 1                  | 0                             |
|                             | Revisione di `InteractionRule`, `Behavior` ed estrazione di `AgentContext`              | AB         | 1                  | 0                             |
| **Functional View**         | Definizione della type class `Monad[F[_]]` e implementazione della `State` monad        | SF         | 3                  | 0                             |
|                             | Refactoring di `Mvu.update` come `State[Model[S], Unit]`                                | SF         | 2                  | 0                             |
|                             | Aggiornamento di `SimulationWindow.dispatch` per esecuzione della `State` monad         | SF         | 2                  | 0                             |
|                             | Test suite per `Monad`, `State` e `Mvu` refattorizzato                                  | SF         | 2                  | 0                             |
| **Point of Interest**       | Definizione di `POI`, `POIEffect`, `Residency` nel package `domain`                     | SF         | 3                  | 0                             |
|                             | Definizione di `POIRenderable` con `given` di default per simulazioni senza `POI`       | SF         | 2                  | 0                             |
|                             | Rendering dei `POI` e dei confini dell'`Environment` in `SimulationPanel`               | SF         | 3                  | 0                             |
|                             | Implementazione del wrap toroidale su `CircularSpace`                                   | SF         | 1                  | 0                             |
| **Migrazione**              | Migrazione di *Epidemic* sul nuovo DSL                                                  | AB         | 0.5                | 0                             |
|                             | Migrazione di *Epidemic* sulla nuova View                                               | SF         | 0.5                | 0                             |
|                             | Migrazione di *Opinion Dynamics* sul nuovo DSL                                          | AB         | 0.5                | 0                             |
|                             | Migrazione di *Opinion Dynamics* sulla nuova View                                       | SF         | 0.5                | 0                             |
| **Documentazione**          | ScalaDoc sulle feature del DSL (builder, syntax, impliciti)                             | AB         | 2                  | 0                             |
|                             | ScalaDoc sulla `State` monad, `POI` e componenti della View                             | SF         | 2                  | 0                             |
| **Totale**                  |                                                                                         |            | **43**             | **0**                         |

## Divisione del lavoro

- **AB**: responsabile interamente della progettazione e realizzazione del DSL. Si è occupato
  di sfruttare i costrutti di Scala 3 per creare un vocabolario dichiarativo che copre tutto lo
  spettro della configurazione (`Environment`, `Agent`, `Behavior`, `InteractionRule`). Ha
  introdotto le `Action` `Spawn` e `Die` per la popolazione dinamica, condotto una revisione
  del modello di dominio e migrato entrambe le simulazioni sul nuovo DSL.
- **SF**: responsabile del refactoring funzionale della View tramite la `State` monad e
  dell'introduzione dei Point of Interest. Si è occupato di introdurre la type class
  `Monad[F[_]]` e la `State` monad nel package `gui`, refattorizzando il ciclo MVU per separare
  descrizione ed esecuzione delle transizioni di stato. Ha progettato e integrato i `POI`
  nell'`Environment`, curandone il rendering nell'interfaccia grafica e implementando il wrap
  toroidale su `CircularSpace`. Ha infine migrato le simulazioni esistenti sulla nuova
  architettura della View.

## Definition of Done

- Il DSL permette di configurare un'intera simulazione in modo dichiarativo e compila senza
  errori.
- La logica di transizione del `Model` è espressa tramite la `State` monad, separando
  descrizione ed esecuzione.
- I `POI` sono definiti nel dominio e renderizzati nell'interfaccia grafica.
- Le simulazioni precedenti (*Epidemic*, *Opinion Dynamics*) funzionano con il nuovo DSL e la
  nuova architettura della View.
- ScalaDoc presente su tutte le nuove astrazioni (builder del DSL, `POI` e `State` monad).
- PR di ogni branch feature verso `develop` con test verdi in CI.

## Sprint Review

Lo stakeholder si dichiara molto soddisfatto dei risultati ottenuti in questo terzo sprint, che
è quello che ha cambiato di più il volto del prodotto. Una simulazione si scrive ora in forma
dichiarativa: spazio, comportamento ai confini, perception e population si dichiarano per
nome, e i comportamenti e le regole di transizione si raccolgono in due blocchi distinti, in
cui sono disponibili soltanto le parole che hanno senso in quel punto. Il risultato è una
descrizione che si legge come un elenco di affermazioni sul modello e non più come la
costruzione di un oggetto di configurazione, che era la richiesta lasciata dallo stakeholder
alla chiusura dello Sprint 2.

Un limite del motore non previsto in fase di planning è emerso durante il lavoro: la
popolazione era di dimensione fissa, quindi nessun modello con mortalità o riproduzione era
esprimibile. Sono state introdotte la nascita e la morte di un agente come azioni del
vocabolario, e il motore è stato adattato di conseguenza. *Epidemic* è stata estesa con uno
stato di morte e con il decadimento degli agent, che ne è il primo utilizzatore concreto e
rende la simulazione visibilmente più interessante. In coda è stata condotta una revisione del
modello di dominio, che ha sostituito la struttura ad albero con cui i comportamenti erano
inizialmente descritti con una rappresentazione a elenco di alternative, ciò è dovuto al fatto
che l'idea iniziale era di imitare un albero decisionale ciò però non comportava alcun vantaggio quindi si è scelto di deviare verso una solzuione
più diretta da costruire dal DSL. Questo lavoro è stato assorbito dal margine che il team riserva in fase di
planning e non ha generato debito.

Sul fronte dell'interfaccia, lo stakeholder rileva che l'esperienza d'uso non è cambiata, ma
prende atto che la logica di aggiornamento della view è ora espressa in forma puramente
funzionale e verificabile senza aprire una finestra. Sono stati infine introdotti i Point of
Interest come elemento configurabile dell'ambiente, visibili a schermo insieme ai confini dello
spazio, e il comportamento ai bordi dello spazio circolare è stato completato con il wrap
toroidale. Entrambe le simulazioni sono state migrate sulla nuova architettura.

## Sprint Retrospective

Lo sprint ha avuto una durata di una settimana e le ore rimanenti a fine sprint sono a zero su
tutti i task. Il blocco DSL Core ha richiesto più iterazioni del previsto, perché la sintassi è
stata riscritta più volte prima di risultare leggibile e la sua interazione con la costruzione
dei builder ha imposto una revisione dell'architettura a metà sprint; il margine riservato in
planning è stato sufficiente ad assorbirla.

### Cosa è andato bene

- Le *context function* hanno permesso di ottenere una configurazione dichiarativa senza
  passare esplicitamente alcun builder, e lo stesso meccanismo si è riusato invariato per i
  blocchi annidati dei behavior e delle regole
- Il vocabolario del DSL è rimasto aperto all'estensione: una simulazione può definire propri
  costrutti senza modificare il framework
- La `State` monad ha permesso di separare nettamente la logica di transizione del modello
  dalla sua esecuzione, rendendo il ciclo di aggiornamento puro e testabile senza aprire
  finestre
- Il margine tenuto a backlog fin dallo Sprint 2 ha coperto per intero il lavoro di
  refactoring, che a specifica non era previsto

### Cosa può essere migliorato

- Il DSL è stato validato eseguendo le simulazioni, non con test. Due difetti sono arrivati
  fino all'esecuzione: una regola poteva essere registrata prima di essere completamente
  configurata, e in una revisione intermedia il blocco di configurazione scriveva su un builder
  diverso da quello poi usato per costruire. Il secondo produceva una simulazione
  completamente vuota senza alcun errore, ed è stato individuato solo osservando una finestra
  bianca
- Una configurazione incompleta veniva propagata fino alla view invece di fallire subito, il
  che ha reso la diagnosi più lunga di quanto il difetto meritasse
- L'astrazione `Memory` resta un segnaposto senza utilizzatore, e con essa l'azione di scambio
  di informazioni fra agenti che il motore non interpreta

### Action items per il prossimo sprint

- Estendere la parte di DSL relativa allo spazio e alla population alla stessa forma infissa
  del resto del vocabolario, sostituendo gli argomenti posizionali e il generatore per indice
- Implementare `Memory` e darle un utilizzatore concreto, facendo interpretare al motore lo
  scambio di informazioni fra agenti
- Integrare i `POI` in almeno una simulazione, e non solo nell'`Environment`, per validarne la
  configurabilità dal DSL
- Prevedere test sul DSL e non solo l'osservazione a schermo, dato che due difetti su due sono
  arrivati fino all'esecuzione