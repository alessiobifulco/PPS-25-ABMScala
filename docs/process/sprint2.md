---
title: Sprint 2
layout: default
nav_order: 1
parent: Processo di Sviluppo
---

# Sprint 2 - Motore di Simulazione e Prime Simulazioni

## Obiettivo

L'obiettivo di questo Sprint è rendere il framework eseguibile end-to-end: completare le
astrazioni di dominio rimaste (InteractionRule) e i fix dallo sprint precedente, realizzare il motore di simulazione e il
DSL di configurazione, e costruire l'interfaccia grafica sopra il pattern Model-View-Update.
Al termine dello sprint la prima simulazione dimostrativa (Epidemia) dovrà essere avviabile
e osservabile a schermo; la seconda (Opinion Dynamics) è pianificata come obiettivo
secondario, da completare solo se il tempo lo consente.

Lo sprint recupera inoltre le 5 ore rimaste incompiute nello Sprint 1.

## Deadline

La scadenza dello sprint è il 01/08/2026.

## Backlog

| Product Backlog Item          | Sprint Task                                                                         | Volontario | Stima iniziale (h) | Ore rimanenti a fine Sprint 2 |
|-------------------------------|-------------------------------------------------------------------------------------|------------|--------------------|-------------------------------|
| **Debito Sprint 1**           | Correzioni a Space: `randomPosition`, `shape`                                       | SF         | 1                  | 0                             |
|                               | Correzione a Environment: esposizione di `neighborhoods`                            | SF         | 1                  | 0                             |
|                               | Semplificazione di NeighborStrategy e fix di `buildIndex`                           | SF         | 1                  | 0                             |
| **Interaction Rule**          | InteractionRule (trait, composizione first-match) e test suite                      | AB         | 2                  | TBD                           |
|                               | `visibleWithin` su AgentContext e test                                              | AB         | 1                  | TBD                           |
| **Motore di simulazione**     | SimulationState e SimulationConfig                                                  | AB         | 1                  | TBD                           |
|                               | SimulationEngine: `init` e pipeline di `tick`                                       | AB         | 4                  | TBD                           |
|                               | Test suite del motore su stati iniziali deterministici                              | AB         | 3                  | TBD                           |
| **DSL di configurazione**     | SimulationBuilder: metodi fluenti e `build()` con spawn                             | AB         | 2                  | TBD                           |
|                               | Test suite di SimulationBuilder                                                     | AB         | 1                  | TBD                           |
| **Vocabolario del dominio**   | Costruttori di movimento: `moveRandomly`, `moveHorizontally`                        | AB         | 1                  | TBD                           |
|                               | Combinatori di regole discrete: `atLeastNear`, `withState`, `whenAgentIs`, `chance` | AB         | 3                  | TBD                           |
| **Model-View-Update**         | Model (with `from` factory, integrazione con SimulationEngine)                      | SF         | 1                  | 0                             |
|                               | Msg enum (Tick, ToggleRun, Restart)                                                 | SF         | 0.5                | 0                             |
|                               | Mvu: `init` e `update`                                                              | SF         | 1                  | 0                             |
|                               | Test suite di Model e Mvu                                                           | SF         | 1                  | 0                             |
| **Interfaccia grafica**       | Renderable: typeclass per la mappatura stato → colore                               | SF         | 1                  | 0                             |
|                               | SimulationPanel: rendering degli agenti su canvas Swing                             | SF         | 2                  | 0                             |
|                               | SimulationWindow: timer di frame, bottoni Stop/Resume/Restart/Back                  | SF         | 3                  | 0                             |
|                               | MainMenu: schermata di scelta con titolo, bottoni, resize e navigazione             | SF         | 2                  | 0                             |
|                               | SimulationOption: trait per la navigazione menu → simulazione                       | SF         | 0.5                | 0                             |
|                               | Test suite di Renderable, Msg, Model, Mvu, SimulationOption                         | SF         | 1                  | 0                             |
| **Simulazione 1: Epidemia**   | Stato `Health`, behaviour e regola di contagio                                      | AB         | 2                  | TBD                           |
|                               | Istanza di Renderable e integrazione nel menu                                       | SF         | 1                  | 0                             |
| **Documentazione del codice** | ScalaDoc su InteractionRule, SimulationEngine, SimulationConfig, Builder            | AB         | 2                  | TBD                           |
|                               | ScalaDoc su MVU e componenti della view                                             | SF         | 1                  | TBD                           |
|                               | **Totale**                                                                          |            | **38**             | **TBD**                       |

## Divisione del lavoro

- **AB**: completamento delle astrazioni di dominio (InteractionRule), motore di
  simulazione, DSL di configurazione, vocabolario dei behaviour e delle regole,
  simulazioni dimostrative.
- **SF**: correzioni all'ambiente di simulazione ereditate dallo Sprint 1; realizzazione
  completa del pattern Model-View-Update (Model, Msg, Mvu) e della relativa test suite;
  interfaccia grafica Swing (Renderable, SimulationPanel, SimulationWindow, MainMenu,
  SimulationOption) con navigazione menu → simulazione → menu; integrazione di Renderable per la simulazione
  Epidemia.

## Definition of Done

- Il motore compila e ha test verdi su stati iniziali deterministici
- La simulazione Epidemia è avviabile dal menu e osservabile a schermo
- ScalaDoc presente su tutti i membri pubblici introdotti
- PR di ogni branch feature verso `develop` con test verdi in CI

## Sprint Review

TBD

## Sprint Retrospective

TBD