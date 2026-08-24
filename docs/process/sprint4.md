---
title: Sprint 4
layout: default
nav_order: 3
parent: Processo di Sviluppo
---

# Sprint 4 - Memoria, Point of Interest e Simulazioni Avanzate

## Obiettivo

L'obiettivo di questo Sprint è chiudere il progetto portando il framework al livello di capacità
richiesto dalle due simulazioni finali. La memoria degli agenti, finora un segnaposto senza
utilizzatore, viene realizzata come struttura effettiva; i Point of Interest, definiti nello Sprint 3
a livello di ambiente, vengono collegati al motore e resi configurabili dal DSL. Il vocabolario del
DSL si allarga di conseguenza con la riscrittura in forma infissa della sezione di configurazione
iniziale, con le aggiunte necessarie alle simulazioni avanzate e, infine, riscrittura delle parti
coinvolte delle simulazioni 1 e 2.

Allo stesso tempo lo Sprint prevede un refactoring del dominio e del DSL. 

Su queste basi vengono realizzate due nuove simulazioni dimostrative: *Formiche*, dove una rotta
collettiva verso il cibo emerge dallo scambio di informazioni fra agenti, e *Propagazione
dell'allarme*, dove la notizia di un pericolo si diffonde per sentito dire oltre il raggio percettivo. Viene inoltre arricchita la simulazione epidemiologica
realizzata nello Sprint 1, oggi troppo elementare per mostrare un fenomeno interessante. Viene infine
introdotta la raccolta di statistiche per tick e la loro visualizzazione, per osservare i fenomeni
emergenti senza doverli dedurre dal solo rendering.

## Deadline

La scadenza dello sprint è il 28/08/2026.

## Backlog

| Product Backlog Item                       | Sprint Task                                                          | Volontario | Stima iniziale (h) | Ore rimanenti a fine Sprint 4 |
|--------------------------------------------|----------------------------------------------------------------------|------------|--------------------|-------------------------------|
| **Memoria degli agenti**                   | Implementazione di `Memory` con capacità limitata                    | AB         | 3                  | 0                             |
|                                            | Definizione di `Belief` e `MemoryEvent`                              | AB         | 2                  | 0                             |
|                                            | Azioni di memoria: registrazione e scambio fra agenti                | AB         | 3                  | 0                             |
|                                            | Condizioni di regola basate sulla memoria                            | AB         | 2                  | 0                             |
| **Point of Interest**                      | Collegamento dei POI al ciclo del motore                             | AB         | 3                  | 0                             |
|                                            | Tracciamento della permanenza degli agenti nei POI                   | AB         | 2                  | 0                             |
|                                            | Condizioni di regola basate sui POI                                  | AB / SF    | 3                  | 0                             |
| **Refactoring del dominio**                | Unificazione di `Behavior` e `InteractionRule` con matcher esplicito | AB         | 2                  | 0                             |
|                                            | Rimozione di `Decision` e `Choice`                                   | AB         | 1.5                | 0                             |
|                                            | Riscrittura di `Action` come enum                                    | AB         | 1.5                | 0                             |
|                                            | Rimozione di `ActionHandler` e riorganizzazione del motore in fasi   | AB         | 2                  | 0                             |
|                                            | Allineamento di `Chance` e `Memory` alle convenzioni del progetto    | AB         | 1                  | 0                             |
| **Refactoring del DSL**                    | Introduzione di `Transition` per la costruzione delle regole         | AB         | 2                  | 0                             |
|                                            | Rimozione dei builder di singola regola                              | AB         | 1                  | 0                             |
|                                            | Uniformazione della sintassi fra comportamenti e regole              | AB         | 1.5                | 0                             |
|                                            | Adeguamento delle simulazioni esistenti alla nuova sintassi          | AB         | 1.5                | 0                             |
| **Estensione del DSL**                     | Riscrittura in forma infissa della configurazione iniziale           | AB         | 4                  | 0                             |
|                                            | Configurazione DSL per le nuove simulazioni (Environment e POI)      | SF         | 3                  | 0                             |
| **Statistiche**                            | Raccolta statistiche per tick nel motore di simulazione              | SF         | 3                  | 0                             |
|                                            | Visualizzazione a schermo delle statistiche                          | SF         | 3                  | 0                             |
| **Simulazione 1: revisione ed estensione** | Introduzione dello stato di immunità temporanea                      | AB         | 1.5                | 0                             |
|                                            | Contagio probabilistico e velocità differenziate per stato           | AB         | 1.5                | 0                             |
|                                            | Taratura dei parametri                                               | AB         | 1                  | 0                             |
| **Simulazione 3: Formiche**                | Logica di ricerca del cibo e ritorno al nido                         | AB         | 2                  | 0                             |
|                                            | Propagazione delle posizioni note fra formiche                       | AB         | 2                  | 0                             |
| **Simulazione 4: Propagazione allarme**    | Logica di allarme, fuga e uscita dalla simulazione                   | AB         | 2                  | 0                             |
|                                            | Propagazione della notizia fra agenti vicini                         | AB         | 2                  | 0                             |
|                                            | Taratura dei tempi di allarme e di rientro                           | AB         | 1                  | 0                             |
| **Test**                                   | Test delle entità di dominio riprogettate                            | AB         | 2                  | 0                             |
|                                            | Test delle condizioni di memoria e di POI                            | AB         | 2                  | 0                             |
|                                            | Test di non regressione sulle simulazioni esistenti                  | AB / SF    | 2                  | 0                             |
| **Documentazione**                         | ScalaDoc sulle astrazioni introdotte e riprogettate                  | AB / SF    | 4                  | 0                             |
|                                            | Aggiornamento del report                                             | AB / SF    | 4                  | 0                             |
|                                            | **Totale**                                                           |            | **72**             | **0**                         |
## Divisione del lavoro

- **AB**: Responsabile della memoria degli agenti, del collegamento dei Point of Interest al engine,
  del refactoring del dominio e del DSL, dell'allargamento del DSL per le nuove simulazioni, della
  revisione della simulazione epidemiologica e della scrittura della logica delle due nuove
  simulazioni (*Formiche* e *Propagazione dell'allarme*).
- **SF**: Responsabile dell'allargamento del DSL per la configurazione dell'environment e dei POI,
  dell'adattamento delle simulazioni 1 e 2 alla nuova sintassi del DSL, e della parte di raccolta
  delle statistiche della simulazione con relativa visualizzazione a schermo.

## Definition of Done

- La memoria degli agenti è una struttura effettiva ed è usata da almeno una simulazione.
- I Point of Interest sono percepibili dagli agenti e utilizzabili come condizione di regola dal DSL.
- Le astrazioni di `Behavior` e `InteractionRule` espongono il proprio criterio di applicabilità come
  dato ispezionabile, e il motore le risolve con lo stesso meccanismo.
- Ogni costrutto esposto dal DSL è esercitato da almeno una simulazione.
- La sezione di configurazione iniziale delle simulazioni è espressa nella stessa forma infissa del
  resto del DSL.
- Le simulazioni 1 e 2 sono state aggiornate per utilizzare il nuovo DSL per l'environment e
  producono lo stesso comportamento osservabile di prima del refactoring.
- Le simulazioni *Formiche* e *Propagazione dell'allarme* sono avviabili dal menu e mostrano il
  fenomeno emergente atteso.
- Le statistiche della simulazione in corso sono osservabili a schermo.
- ScalaDoc presente su tutte le astrazioni introdotte.
- PR di ogni branch feature verso `develop` con test verdi in CI.

## Sprint Review

## Sprint Retrospective

### Cosa è andato bene

- Il refactoring ha ridotto il numero di entità di dominio senza togliere espressività: le
  simulazioni esistenti sono state riscritte nella nuova sintassi mantenendo lo stesso comportamento
  osservabile, il che ha confermato che le astrazioni rimosse non stavano portando valore.
- Spostare la registrazione delle regole all'ultimo passo della catena fluente ha eliminato per
  costruzione una classe di errori che prima poteva emergere solo a runtime.
- Il criterio "ogni costrutto esposto dal DSL deve essere esercitato da almeno una simulazione" si è
  rivelato utile come regola di decisione: ha permesso di risolvere rapidamente diversi dubbi di
  design che altrimenti sarebbero rimasti aperti.

### Cosa può essere migliorato

- Il refactoring è stato affrontato nell'ultimo Sprint, quando alcune delle scelte da correggere
  erano già state replicate in tre simulazioni: individuare prima la ridondanza fra comportamenti e
  regole avrebbe evitato l'adeguamento a valle.
- Nella prima versione della quarta simulazione il vocabolario introdotto era modellato sul fenomeno
  specifico invece che sul framework, e ha richiesto più di una revisione prima di arrivare a
  costrutti riutilizzabili.
- La taratura dei parametri delle simulazioni è stata sottostimata: ottenere un fenomeno leggibile a
  schermo ha richiesto più iterazioni del previsto, come già osservato nello Sprint 3.

