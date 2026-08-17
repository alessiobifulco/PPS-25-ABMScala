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
utilizzatore, viene realizzata come struttura effettiva; i
Point of Interest, definiti nello Sprint 3 a livello di ambiente, vengono collegati al motore e resi
configurabili dal DSL. Il vocabolario del DSL si allarga di conseguenza con la riscrittura in forma infissa della sezione di
configurazione iniziale, con le aggiunte necessarie alle simulazioni avanzate e, infine, riscrittura delle parti coinvolte delle simulazioni 1 e 2.

Su queste basi vengono realizzate due nuove simulazioni dimostrative: *Formiche*, dove una rotta
collettiva verso il cibo emerge dallo scambio di informazioni fra agenti, e *Reputazione*, dove la
fiducia si propaga per sentito dire e i rancori scadono con la memoria. Viene infine introdotta la
raccolta di statistiche per tick e la loro visualizzazione, per osservare i fenomeni emergenti
senza doverli dedurre dal solo rendering.

## Deadline

La scadenza dello sprint è il **/08/2026.

## Backlog

| Product Backlog Item           | Volontario | Stima iniziale (h) | Ore rimanenti a fine Sprint 4 |
|--------------------------------|------------|--------------------|-------------------------------|
| **Memoria degli agenti**       | AB         | 10                 |                               |
| **Point of Interest**          | AB / SF    | 8                  |                               |
| **Estensione del DSL**         | AB / SF    | 7                  |                               |
| **Statistiche**                | SF         | 6                  |                               |
| **Simulazione 3: Formiche**    | AB         | 4                  |                               |
| **Simulazione 4: Reputazione** | AB         | 5                  |                               |
| **Test**                       | AB / SF    | 6                  |                               |
| **Documentazione**             | AB / SF    | 8                  |                               |
| **Totale**                     |            | **54**             |                               |

## Divisione del lavoro

- **AB**: Responsabile della memoria degli agenti, del collegamento dei Point of Interest al motore, dell'allargamento del DSL per le nuove simulazioni e della scrittura della logica delle due nuove simulazioni (*Formiche* e *Reputazione*).
- **SF**: Responsabile dell'allargamento del DSL per la configurazione dell'environment e dei POI, dell'adattamento delle simulazioni 1 e 2 alla nuova sintassi del DSL, e della parte di raccolta delle statistiche della simulazione con relativa visualizzazione a schermo.

## Definition of Done

- La memoria degli agenti è una struttura effettiva ed è usata da almeno una
  simulazione.
- I Point of Interest sono percepibili dagli agenti e utilizzabili come condizione di regola dal DSL.
- La sezione di configurazione iniziale delle simulazioni è espressa nella stessa forma infissa del
  resto del DSL.
- Le simulazioni 1 e 2 sono state aggiornate per utilizzare il nuovo DSL per l'environment.
- Le simulazioni *Formiche* e *Reputazione* sono avviabili dal menu e mostrano il fenomeno emergente
  atteso.
- Le statistiche della simulazione in corso sono osservabili a schermo.
- ScalaDoc presente su tutte le astrazioni introdotte.
- PR di ogni branch feature verso `develop` con test verdi in CI.

## Sprint Review

## Sprint Retrospective

### Cosa è andato bene
- ...

### Cosa può essere migliorato
- ...

### Action items per il prossimo sprint
- ...