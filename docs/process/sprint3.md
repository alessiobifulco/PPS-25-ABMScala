---
title: Sprint 3
layout: default
nav_order: 2
parent: Processo di Sviluppo
---

# Sprint 3 - DSL Dichiarativo e View Monadica

## Obiettivo

L'obiettivo di questo Sprint è duplice e punta a migliorare radicalmente l'ergonomia e la purezza architetturale del framework.
In primo luogo, verrà sviluppato un DSL (Domain Specific Language) dichiarativo sfruttando le feature di Scala 3 (come le *context functions*), per consentire una configurazione fluida ed espressiva delle simulazioni, andando a sostituire la configurazione manuale di `SimulationConfig`.
In secondo luogo, il livello della View verrà refattorizzato applicando pattern funzionali puri: gli effetti collaterali legati all'interfaccia grafica (Swing) verranno isolati all'interno di un wrapper monadico (es. `IO` / `Task`), garantendo maggiore testabilità e separazione netta tra la logica di aggiornamento e il rendering effettivo. Infine, verranno introdotti i Point of Interest (POI) come nuovo elemento configurabile dell'ambiente di simulazione.

## Deadline

La scadenza dello sprint è il 15/08/2026.

## Backlog

| Product Backlog Item        | Sprint Task                                                                                      | Volontario | Stima iniziale (h) | Ore rimanenti a fine Sprint 3 |
|-----------------------------|--------------------------------------------------------------------------------------------------|------------|--------------------|-------------------------------|
| **DSL Core**                | Astrazione base del DSL (`SimulationBuilder` e context functions)                                | AB         | 4                  | 0                             |
|                             | DSL per la definizione di `Environment`, `Space` e `BoundaryPolicy`                              | AB         | 2                  | 0                             |
|                             | DSL per la definizione di Popolazione, Agenti e Stato Iniziale                                   | AB         | 3                  | 0                             |
|                             | DSL per la composizione di `Behaviour` e `InteractionRule`                                       | AB         | 3                  | 0                             |
| **Popolazione dinamica**    | Azioni `Spawn` e `Die` e interpretazione estendibile tramite `ActionHandler`                     | AB         | 2                  | 0                             |
|                             | Adattamento di `SimulationEngine` a una popolazione di dimensione variabile                      | AB         | 2                  | 0                             |
|                             | Costruttori del DSL per nascita e morte (`spawn`, `die`, `stopMoving`, `vanishingWith`)          | AB         | 0.5                | 0                             |
|                             | Stato `Dead` e decadimento dei cadaveri in *Epidemia*                                            | AB         | 0.5                | 0                             |
| **Refactoring del dominio** | Revisione di `InteractionRule`, `Decision`, `Behavior` ed estrazione di `AgentContext`           | AB         | 1                  | 0                             |
| **Functional View**         | Setup della Monade `IO` (o `Task`) per la gestione degli effetti collaterali                     | SF         | 4                  | 4                             |
|                             | Refactoring di `SimulationPanel` e `SimulationWindow` in ottica monadica                         | SF         | 5                  | 5                             |
|                             | Integrazione del loop di MVU con la nuova gestione pura del rendering                            | SF         | 3                  | 3                             |
| **Point of Interest**       | Definizione di `POI` (posizione, raggio, ritardo di attivazione) e integrazione in `Environment` | SF         | 4                  | 4                             |
| **Migrazione**              | Migrazione di *Epidemia* sul nuovo DSL                                                           | AB         | 0.5                | 0                             |
|                             | Migrazione di *Epidemia* sulla nuova View                                                        | SF         | 0.5                | 0.5                           |
|                             | Migrazione di *Opinion Dynamics* sul nuovo DSL                                                   | AB         | 0.5                | 0                             |
|                             | Migrazione di *Opinion Dynamics* sulla nuova View                                                | SF         | 0.5                | 0.5                           |
| **Documentazione**          | ScalaDoc sulle feature del DSL (builder, syntax, impliciti)                                      | AB         | 2                  | 0                             |
|                             | ScalaDoc sull'architettura monadica della View e gestione side-effects                           | SF         | 2                  | 0                             |
|                             | **Totale**                                                                                       |            | **40**             | **17**                        |

## Divisione del lavoro

- **AB**: Responsabile interamente della progettazione e realizzazione del DSL. Si occuperà di sfruttare i costrutti di Scala 3 per creare un vocabolario dichiarativo che copra tutto lo spettro della configurazione (Ambiente, Agenti, Comportamenti, Regole).
- **SF**: Responsabile del refactoring funzionale della View. Si occuperà di isolare gli effetti collaterali di Swing tramite l'introduzione di una Monade dedicata, riorganizzando il ciclo di update di MVU per restituire descrizioni di computazioni (`IO`) anziché eseguire effetti diretti. Si occuperà inoltre della progettazione e integrazione dei Point of Interest nell'ambiente di simulazione, e infine della migrazione delle simulazioni esistenti sulla nuova architettura.

## Definition of Done

- Il DSL permette di configurare un'intera simulazione in modo dichiarativo e compila senza errori.
- La logica di rendering della View non esegue effetti diretti ma restituisce tipi monadici puri.
- I Point of Interest sono configurabili e integrati nell'ambiente di simulazione.
- Le simulazioni precedenti (Epidemia, Opinion Dynamics) funzionano perfettamente con il nuovo DSL e la nuova architettura della View.
- ScalaDoc è presente su tutte le nuove astrazioni (Builder del DSL, POI e classi della View monadica).
- PR di ogni branch feature verso `develop` con test verdi in CI.

## Sprint Review

Il DSL dichiarativo è stato completato su tutto lo spettro della configurazione. `Simulation.of`
riceve una *context function* sul `SimulationBuilder`, così che spazio, politica di bordo, raggio
di percezione e popolazione si dichiarino come comandi liberi anziché come chiamate su un oggetto.
La composizione di comportamenti e regole avviene nei due blocchi `behaviour:` e `rules:`, ciascuno
dei quali introduce un builder proprio: le parole del DSL sono quindi legate al blocco in cui hanno
senso, e il compilatore rifiuta un costrutto scritto nel blocco sbagliato.

L'architettura dei builder è a due fasi. Una riga come
`Infected when atLeastNear(1) withState Infected whenAgentIs Healthy` valuta `when` per prima e
completa la configurazione solo alla fine della catena infissa: i builder accumulano quindi gli
oggetti di configurazione e li materializzano alla chiusura del blocco, non alla registrazione.
Questo ha reso possibile validare i parametri obbligatori di ogni regola al momento della
costruzione, con un fallimento esplicito al posto di una regola incompleta e silente. Lo stesso
principio si applica alle scelte di comportamento: la scelta di default è tenuta da parte e
appesa in coda, così che scriverla per prima non renda irraggiungibili tutte le altre.

Nel corso dello sprint è emerso un limite del motore non previsto in fase di planning:
l'aggiornamento degli agenti era una mappatura uno-a-uno, quindi la popolazione non poteva variare
e nessun modello con mortalità o riproduzione era esprimibile dal DSL. Sono state introdotte le
azioni `Spawn` e `Die`, l'interpretazione delle azioni è stata resa estendibile tramite
`ActionHandler`, e `SimulationEngine` è stato adattato a una popolazione di dimensione variabile
mantenendo l'unicità degli identificatori. La simulazione Epidemia è stata estesa con uno stato
`Dead` e il decadimento dei cadaveri, che ne è il primo utilizzatore concreto. In coda è stata
condotta una revisione del modello di dominio, con l'estrazione di `AgentContext` in un'unità
propria. Questo lavoro è stato assorbito dal margine che il team riserva in fase di planning e
non ha generato debito.

Entrambe le simulazioni dimostrative sono state migrate sul nuovo DSL e restano avviabili e
osservabili a schermo. La parte di migrazione relativa alla nuova View resta aperta, in attesa
del refactoring monadico.

## Sprint Retrospective

Le ore rimanenti a fine sprint sono a zero su tutti i task assegnati ad AB, comprese le voci
emerse in corso d'opera. Il blocco DSL Core ha richiesto più iterazioni del previsto, perché la
sintassi infissa è stata riscritta più volte prima di risultare leggibile e l'interazione fra
catene infisse e costruzione dei builder ha imposto una revisione dell'architettura a metà sprint;
il margine riservato in planning è stato sufficiente ad assorbirla.

### Cosa è andato bene

- Le *context functions* hanno permesso di ottenere una configurazione dichiarativa senza passare
  esplicitamente alcun builder, e lo stesso meccanismo si è riusato invariato per i blocchi
  annidati `behaviour:` e `rules:`.
- Il vocabolario del DSL è rimasto aperto all'estensione: un comportamento è un semplice alias di
  funzione e una regola un `RuleBuilder`, quindi una simulazione esterna può definirne di propri
  senza modificare il package `dsl`.
- Il margine tenuto a backlog fin dallo Sprint 2 ha coperto per intero il lavoro di refactoring, che a specifica non era previsto.

### Cosa può essere migliorato

- Il DSL è stato validato eseguendo le simulazioni, non con test. Due difetti sono arrivati fino
  all'esecuzione: le regole venivano costruite prima di essere configurate, e in una revisione
  intermedia il blocco di configurazione scriveva su un builder diverso da quello poi usato per
  costruire. Il secondo produceva una simulazione completamente vuota senza alcun errore, ed è
  stato individuato solo osservando una finestra bianca.
- Una configurazione incompleta veniva propagata fino alla View invece di fallire subito, il che ha
  reso la diagnosi più lunga di quanto il difetto meritasse.
- L'astrazione `Memory` resta un segnaposto senza utilizzatore, e con essa l'azione `ShareMemory`
  che il motore non interpreta.

### Action items per il prossimo sprint


- Estendere la parte di DSL relativa a spazio e popolazione alla stessa forma infissa del resto del
  vocabolario, sostituendo gli argomenti posizionali e il generatore per indice con costrutti del
  tipo `space(...) withBoundary bounce` e `population(n) of Healthy withOne Infected`.
- Implementare `Memory` e darle un utilizzatore concreto, facendo interpretare `ShareMemory`
  all'`ActionHandler`.
- Integrare i Point of Interest in almeno una simulazione, e non solo nell'ambiente, per validarne
  la configurabilità dal DSL.