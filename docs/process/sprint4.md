---
title: Sprint 4
layout: default
nav_order: 3
parent: Processo di Sviluppo
---

# Sprint 4 - Memory, Point of Interest e Simulazioni Avanzate

## Obiettivo

L'obiettivo di questo quarto e ultimo Sprint è chiudere il progetto portando il framework al
livello di capacità richiesto dalle due simulazioni finali. La `Memory` degli `Agent`, finora
un segnaposto senza utilizzatore, viene realizzata come struttura effettiva; i `POI`, definiti
nello Sprint 3 a livello di `Environment`, vengono collegati all'engine e resi configurabili
dal DSL. Il vocabolario del DSL si allarga di conseguenza con la riscrittura in forma infissa
della sezione di configurazione iniziale, con le aggiunte necessarie alle simulazioni avanzate
e, infine, la riscrittura delle parti coinvolte delle simulazioni 1 e 2.

Allo stesso tempo lo Sprint prevede un refactoring del dominio e del DSL, per rimuovere le
astrazioni introdotte negli sprint precedenti che nessuna simulazione ha poi esercitato e per
ricondurre `Behavior` e `InteractionRule` a una forma comune.

Su queste basi vengono realizzate due nuove simulazioni dimostrative: *Ant Colony*, dove una
rotta collettiva verso il cibo emerge dallo scambio di informazioni fra `Agent`, e
*Alarm Spreading*, dove la notizia di un pericolo si diffonde per sentito dire oltre
la perception del singolo agente. Viene inoltre arricchita la simulazione epidemiologica
realizzata nello Sprint 2, oggi troppo elementare per mostrare un fenomeno interessante. Viene
infine introdotta la raccolta di statistiche per tick e la loro visualizzazione, per osservare
i fenomeni emergenti senza doverli dedurre dal solo rendering.

Il pannello statistico, integrato nella `SimulationWindow` secondo l'architettura MVU già in
uso, mostra la distribuzione degli stati, le transizioni, lo storico temporale e la densità
spaziale degli agenti.

## Deadline

La scadenza dello sprint è il 28/08/2026.

## Backlog

| Product Backlog Item                       | Sprint Task                                                                | Volontario | Stima iniziale (h) | Ore rimanenti a fine Sprint 4 |
|--------------------------------------------|----------------------------------------------------------------------------|------------|--------------------|-------------------------------|
| **Agent Memory**                           | Implementazione di `Memory` con capacità limitata                          | AB         | 2                  | 0                             |
|                                            | Definizione di `Belief` e `MemoryEvent`                                    | AB         | 1                  | 0                             |
|                                            | `Action` di memoria: registrazione e scambio fra `Agent`                   | AB         | 1.5                | 0                             |
|                                            | `Condition` di regola basate sulla `Memory`                                | AB         | 1.5                | 0                             |
| **Point of Interest**                      | Collegamento dei `POI` al ciclo dell'engine                                | AB         | 1.5                | 0                             |
|                                            | Tracciamento della `Residency` degli `Agent` nei `POI`                     | AB         | 1.5                | 0                             |
|                                            | `Condition` di regola basate sui `POI`                                     | AB / SF    | 3                  | 0                             |
| **Refactoring del dominio**                | Unificazione di `Behavior` e `InteractionRule` con matcher esplicito       | AB         | 1.5                | 0                             |
|                                            | Rimozione di `Decision` e `Choice`                                         | AB         | 1                  | 0                             |
|                                            | Riscrittura di `Action` come enum                                          | AB         | 1                  | 0                             |
|                                            | Rimozione di `ActionHandler` e riorganizzazione dell'engine in fasi        | AB         | 1.5                | 0                             |
|                                            | Allineamento di `Chance` e `Memory` alle convenzioni del progetto          | AB         | 0.5                | 0                             |
| **Refactoring del DSL**                    | Introduzione di `Transition` per la costruzione delle `InteractionRule`    | AB         | 1.5                | 0                             |
|                                            | Rimozione dei builder di singola regola                                    | AB         | 0.5                | 0                             |
|                                            | Uniformazione della sintassi fra `Behavior` e `InteractionRule`            | AB         | 1                  | 0                             |
|                                            | Adeguamento delle simulazioni esistenti alla nuova sintassi                | AB         | 1                  | 0                             |
| **Estensione del DSL**                     | Riscrittura in forma infissa della configurazione iniziale                 | AB         | 3                  | 0                             |
|                                            | Configurazione DSL per ambiente, popolazione, percezione e `POI`           | SF         | 4                  | 0                             |
| **Statistiche**                            | Raccolta delle statistiche per tick nel `StatisticsPanel`                  | SF         | 5                  | 0                             |
|                                            | Visualizzazione della distribuzione degli stati e dello storico            | SF         | 3                  | 0                             |
|                                            | Visualizzazione delle transizioni e della densità spaziale                 | SF         | 4                  | 0                             |
|                                            | Categorizzazione degli stati continui tramite `Renderable.labelOf`         | SF         | 2                  | 0                             |
|                                            | Integrazione del pannello statistico nella `SimulationWindow`              | SF         | 3                  | 0                             |
|                                            | Refactoring del raggruppamento delle statistiche e delle linee del grafico | SF         | 2                  | 0                             |
| **Simulazione 1: revisione ed estensione** | Introduzione dello stato di immunità temporanea                            | AB         | 1                  | 0                             |
|                                            | Contagio probabilistico e velocità differenziate per stato                 | AB         | 0.5                | 0                             |
|                                            | Taratura dei parametri                                                     | AB         | 1                  | 0                             |
| **Simulazione 3: Formiche**                | Logica di ricerca del cibo e ritorno al nido                               | AB         | 1.5                | 0                             |
|                                            | Propagazione delle posizioni note fra `Agent`                              | AB         | 1.5                | 0                             |
| **Simulazione 4: Propagazione allarme**    | Logica di allarme, fuga e uscita dalla simulazione                         | AB         | 1.5                | 0                             |
|                                            | Propagazione della notizia fra `Agent` vicini                              | AB         | 1.5                | 0                             |
|                                            | Taratura dei tempi di allarme e di rientro                                 | AB         | 1                  | 0                             |
| **Test**                                   | Test delle entità di dominio riprogettate                                  | AB         | 1.5                | 0                             |
|                                            | Test delle `Condition` di `Memory` e di `POI`                              | AB         | 1.5                | 0                             |
|                                            | Refactor dei test di dominio                                               | SF         | 2                  | 0                             |
|                                            | Test di `Model`, `Mvu`, `State`, `Monad` e messaggi della GUI              | SF         | 4                  | 0                             |
|                                            | Test di `POI`, `EnvironmentBuilder`, `Renderable` e `POIRenderable`        | SF         | 3                  | 0                             |
| **Documentazione**                         | ScalaDoc sulle astrazioni introdotte e riprogettate                        | AB / SF    | 4                  | 0                             |
|                                            | Aggiornamento del report                                                   | AB / SF    | 4                  | 0                             |
| **Totale**                                 |                                                                            |            | **77**             | **0**                         |

## Divisione del lavoro

- **AB**: responsabile della `Memory` degli `Agent`, del collegamento dei `POI` all'engine, del
  refactoring del dominio e del DSL, dell'allargamento del DSL per le nuove simulazioni, della
  revisione della simulazione epidemiologica e della scrittura della logica delle due nuove
  simulazioni (*Formiche* e *Propagazione dell'allarme*).
- **SF**: responsabile della raccolta e della visualizzazione delle statistiche della
  simulazione, comprese la distribuzione degli stati, lo storico temporale, il conteggio delle
  transizioni e la densità spaziale. Si è inoltre occupato della categorizzazione degli stati
  continui tramite `Renderable.labelOf`, dell'integrazione del `StatisticsPanel` nella
  `SimulationWindow` e dei test relativi ai componenti della GUI e ai `POI`.

## Definition of Done

- La `Memory` degli `Agent` è una struttura effettiva ed è usata da almeno una simulazione.
- I `POI` sono percepibili dagli `Agent` e utilizzabili come `Condition` di regola dal DSL.
- `Behavior` e `InteractionRule` espongono il proprio criterio di applicabilità come dato
  ispezionabile, e l'engine le risolve con lo stesso meccanismo.
- Ogni famiglia di costrutti esposta dal DSL è esercitata da almeno una simulazione.
- La sezione di configurazione iniziale delle simulazioni è espressa nella stessa forma infissa
  del resto del DSL.
- Le simulazioni 1 e 2 sono state aggiornate per utilizzare il nuovo DSL per l'`Environment` e
  producono lo stesso comportamento osservabile di prima del refactoring.
- Le simulazioni *Ant Colony* e *Alarm Spreading* sono avviabili dal menu e mostrano il
  fenomeno emergente atteso.
- Le statistiche della simulazione in corso sono raccolte a ogni tick e osservabili a schermo,
  includendo distribuzione degli stati, storico temporale, transizioni e densità spaziale.
- ScalaDoc presente su tutte le astrazioni introdotte.
- PR di ogni branch feature verso `develop` con test verdi in CI.

## Sprint Review

Lo stakeholder riconosce che il framework è completo e pronto per la consegna. Il prodotto
finale copre tutti i requisiti principali stabiliti all'inizio del progetto oltre ad alcuni aspetti opzionali,
e le quattro simulazioni dimostrative appartengono a domini sufficientemente diversi
da rendere credibile la genericità dichiarata: contagio, dinamica delle opinioni,
foraggiamento collettivo e diffusione di una notizia sono descritti con lo
stesso vocabolario e girano sullo stesso engine.

Gli agent sono ora dotati di una memoria di capacità limitata, e questo abilita fenomeni che
prima non erano esprimibili: in *Ant Colony* una rotta collettiva verso il cibo si forma perché
le formiche si scambiano le posizioni che hanno visto, e in *Alarm Spreading* la
notizia di un pericolo raggiunge agent che non lo hanno mai incontrato, diffondendosi ben oltre
la perception del singolo. Lo stakeholder osserva che in entrambi i casi il comportamento
collettivo non è stato programmato da nessuna parte, ma emerge dalle regole locali: è
esattamente la proprietà che il progetto si proponeva di mostrare. I Point of Interest, che
allo Sprint 3 erano solo un elemento dell'ambiente, sono ora percepibili dagli agent e usati
da entrambe le nuove simulazioni.

La simulazione epidemiologica, giudicata troppo elementare, è stata rivista: l'immunità dopo la
guarigione è temporanea e decade e gli agent si muovono a velocità diverse secondo il loro stato.
Il risultato è una popolazione che attraversa ondate successive invece di stabilizzarsi, molto più
interessante da osservare. Le statistiche raccolte a ogni tick e mostrate a schermo permettono
infine di seguire l'andamento dei fenomeni senza doverlo dedurre dal solo rendering.

Il `StatisticsPanel`, integrato nella `SimulationWindow` secondo l'architettura MVU già introdotta nello Sprint 3,
mostra distribuzione degli stati, transizioni, storico temporale e densità spaziale, rendendo osservabili anche
gli aspetti quantitativi delle simulazioni.

Una parte consistente dello sprint non è visibile allo stakeholder: il refactoring del dominio
e del DSL, che ha rimosso le astrazioni introdotte negli sprint precedenti e mai esercitate da
alcuna simulazione e ha ricondotto behavior e interaction rule a una forma comune. Lo
stakeholder prende atto che questo lavoro non cambia l'esperienza d'uso ma apprezza che il team
abbia dedicato tempo alla qualità interna, verificando che tutte le simulazioni esistenti
continuino a comportarsi come prima.

## Sprint Retrospective

Lo sprint ha avuto una durata di due settimane circa, la maggiore di tutto il progetto, ed è stato
caratterizzato da tre attività di natura diversa condotte in parallelo: l'aggiunta di
funzionalità mancanti, un refactoring architetturale e la realizzazione di due nuove
simulazioni. Il refactoring, pur rischioso nell'ultimo sprint, si è rivelato necessario per
consegnare un framework con un dominio coerente. Le ore rimanenti a fine sprint sono a zero su tutti i task.

### Cosa è andato bene

- Il refactoring ha ridotto il numero di entità di dominio senza togliere espressività: le
  simulazioni esistenti sono state riscritte nella nuova sintassi mantenendo lo stesso
  comportamento osservabile, il che ha confermato che le astrazioni rimosse non stavano
  portando valore.
- La revisione del modo in cui le regole vengono registrate ha eliminato per costruzione la
  classe di difetti osservata nello Sprint 3, dove una regola poteva essere registrata prima di
  essere completamente configurata.
- Il criterio "ogni famiglia di costrutti esposta dal DSL deve essere esercitata da almeno una
  simulazione"
  si è rivelato utile come regola di decisione: ha permesso di risolvere rapidamente diversi
  dubbi di design che altrimenti sarebbero rimasti aperti, e ha motivato la rimozione di un
  punto di estensione introdotto nello Sprint 3 e mai usato.
- Gli action item lasciati dallo Sprint 3 sono stati completati tutti: la memoria degli agenti
  ha ora un utilizzatore concreto, i Point of Interest sono integrati in due simulazioni, e la
  configurazione iniziale è espressa nella stessa forma del resto del DSL.
- La separazione tra modello, aggiornamento dello stato e visualizzazione ha reso la GUI più
  facilmente testabile e ha permesso di integrare le statistiche senza accoppiare il pannello
  grafico alla logica delle singole simulazioni.

### Cosa può essere migliorato

- Il refactoring è stato affrontato nell'ultimo sprint: individuare prima la ridondanza fra
  comportamenti e regole di transizione avrebbe evitato l'adeguamento a valle, anche se ciò
  non ha portato a grossi cambiamenti ad alto livello, riguardando principalmente aspetti non
  utilizzati o ridondanti.
- Nella prima versione della quarta simulazione il vocabolario introdotto era modellato sul
  fenomeno specifico invece che sul framework, e ha richiesto più di una revisione prima di
  arrivare a costrutti riutilizzabili.
- La taratura dei parametri delle simulazioni è stata sottostimata: ottenere un fenomeno
  leggibile a schermo ha richiesto più iterazioni del previsto, come già osservato nello
  Sprint 3.

### Action items

Essendo l'ultimo sprint del progetto, gli spunti emersi non si traducono in action item per
un'iterazione successiva ma confluiscono nella retrospettiva finale del report.