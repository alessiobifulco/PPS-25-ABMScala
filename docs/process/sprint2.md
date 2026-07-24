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
| **Debito Sprint 1**           | Correzioni a Space: `randomPosition`, `shape`                                       | SF         | 1                  | TBD                           |
|                               | Correzione a Environment: esposizione di `neighborhoods`                            | SF         | 1                  | TBD                           |
|                               | Semplificazione di NeighborStrategy e fix di `buildIndex`                           | SF         | 1                  | TBD                           |
| **Interaction Rule**          | InteractionRule (trait, composizione first-match) e test suite                      | AB         | 2                  | TBD                           |
|                               | `visibleWithin` su AgentContext e test                                              | AB         | 1                  | TBD                           |
| **Motore di simulazione**     | SimulationState e SimulationConfig                                                  | AB         | 1                  | TBD                           |
|                               | SimulationEngine: `init` e pipeline di `tick`                                       | AB         | 4                  | TBD                           |
|                               | Test suite del motore su stati iniziali deterministici                              | AB         | 3                  | TBD                           |
| **DSL di configurazione**     | SimulationBuilder: metodi fluenti e `build()` con spawn                             | AB         | 2                  | TBD                           |
|                               | Test suite di SimulationBuilder                                                     | AB         | 1                  | TBD                           |
| **Vocabolario del dominio**   | Costruttori di movimento: `moveRandomly`, `moveHorizontally`                        | AB         | 1                  | TBD                           |
|                               | Combinatori di regole discrete: `atLeastNear`, `withState`, `whenAgentIs`, `chance` | AB         | 3                  | TBD                           |
| **Model-View-Update**         | Model, Msg (Tick, ToggleRun, Restart) e funzione `update` pura                      | AB         | 2                  | TBD                           |
|                               | Test suite di `update`                                                              | AB         | 1                  | TBD                           |
| **Interfaccia grafica**       | Renderable: type class per la mappatura stato → colore                              | SF         | 1                  | TBD                           |
|                               | Canvas: rendering di agenti e confini a partire da `Shape`                          | SF         | 3                  | TBD                           |
|                               | SimulationWindow: timer di frame, dispatch dei messaggi, controlli                  | SF         | 3                  | TBD                           |
|                               | MainMenu: schermata di scelta della simulazione                                     | SF         | 2                  | TBD                           |
| **Simulazione 1: Epidemia**   | Stato `Health`, behaviour e regola di contagio                                      | AB         | 2                  | TBD                           |
|                               | Istanza di Renderable e integrazione nel menu                                       | SF         | 1                  | TBD                           |
| **Documentazione del codice** | ScalaDoc su InteractionRule, SimulationEngine, SimulationConfig, Builder            | AB         | 2                  | TBD                           |
|                               | ScalaDoc su MVU e componenti della view                                             | SF         | 1                  | TBD                           |
|                               | **Totale**                                                                          |            | **38**             | **TBD**                       |


## Divisione del lavoro

- **AB**: completamento delle astrazioni di dominio (InteractionRule), motore di
  simulazione, DSL di configurazione, vocabolario dei behaviour e delle regole, nucleo
  del pattern MVU, simulazioni dimostrative.
- **SF**: correzioni all'ambiente di simulazione ereditate dallo Sprint 1 e realizzazione
  dell'interfaccia grafica (rendering, finestra, menu).

## Definition of Done

- Il motore compila e ha test verdi su stati iniziali deterministici
- La simulazione Epidemia è avviabile dal menu e osservabile a schermo
- ScalaDoc presente su tutti i membri pubblici introdotti
- PR di ogni branch feature verso `develop` con test verdi in CI

## Sprint Review

TBD

## Sprint Retrospective

TBD