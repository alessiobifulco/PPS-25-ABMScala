---
title: Sprint 2
layout: default
nav_order: 1
parent: Processo di Sviluppo
---

# Sprint 2 - Motore di Simulazione e Prime Simulazioni

## Obiettivo

L'obiettivo di questo Sprint è rendere il framework eseguibile end-to-end: completare le
astrazioni di dominio rimaste (InteractionRule) e i fix dallo sprint precedente, realizzare
il motore di simulazione, e costruire l'interfaccia grafica sopra il pattern Model-View-Update.
Al termine dello sprint entrambe le simulazioni dimostrative pianificate — Epidemia e Opinion
Dynamics — dovranno essere avviabili e osservabili a schermo, configurate direttamente tramite
`SimulationConfig`.

Lo sprint recupera inoltre le 5 ore rimaste incompiute nello Sprint 1.

## Deadline

La scadenza dello sprint è il 01/08/2026.

## Backlog

| Product Backlog Item                | Sprint Task                                                                                                | Volontario | Stima iniziale (h) | Ore rimanenti a fine Sprint 2 |
|-------------------------------------|------------------------------------------------------------------------------------------------------------|------------|--------------------|-------------------------------|
| **Debito Sprint 1**                 | Correzioni a Space: `randomPosition`, `shape`                                                              | SF         | 1                  | 0                             |
|                                     | Correzione a Environment: esposizione di `neighborhoods`                                                   | SF         | 1                  | 0                             |
|                                     | Semplificazione di NeighborStrategy e fix di `buildIndex`                                                  | SF         | 1                  | 0                             |
| **Interaction Rule**                | InteractionRule (trait, composizione first-match) e test suite                                             | AB         | 2                  | 0                             |
|                                     | `visibleWithin` su AgentContext e test                                                                     | AB         | 1                  | 0                             |
| **Motore di simulazione**           | SimulationState e SimulationConfig                                                                         | AB         | 1                  | 0                             |
|                                     | SimulationEngine: `init` e pipeline di `tick`                                                              | AB         | 4                  | 0                             |
|                                     | Test suite del motore                                                                                      | AB         | 3                  | 0                             |
| **Vocabolario del dominio**         | Costruttori di movimento: `moveRandomly`, `moveHorizontally`                                               | AB         | 1                  | 0                             |
|                                     | Combinatori di regole discrete: `atLeastNear`, `withState`, `whenAgentIs`, `chance`                        | AB         | 3                  | 0                             |
| **Model-View-Update**               | Model (with `from` factory, integrazione con SimulationEngine)                                             | SF         | 1                  | 0                             |
|                                     | Msg enum (Tick, ToggleRun, Restart)                                                                        | SF         | 0.5                | 0                             |
|                                     | Mvu: `init` e `update`                                                                                     | SF         | 1                  | 0                             |
|                                     | Test suite di Model e Mvu                                                                                  | SF         | 1                  | 0                             |
| **Interfaccia grafica**             | Renderable: typeclass per la mappatura stato → colore                                                      | SF         | 1                  | 0                             |
|                                     | SimulationPanel: rendering degli agenti su canvas Swing                                                    | SF         | 2                  | 0                             |
|                                     | SimulationWindow: timer di frame, bottoni Stop/Resume/Restart/Back                                         | SF         | 3                  | 0                             |
|                                     | MainMenu: schermata di scelta con titolo, bottoni, resize e navigazione                                    | SF         | 2                  | 0                             |
|                                     | SimulationOption: trait per la navigazione menu → simulazione                                              | SF         | 0.5                | 0                             |
|                                     | Test suite di Renderable, Msg, Model, Mvu, SimulationOption                                                | SF         | 1                  | 0                             |
| **Simulazione 1: Epidemia**         | Stato `Health`, behaviour e regola di contagio                                                             | AB         | 2                  | 0                             |
|                                     | Istanza di Renderable e integrazione nel menu                                                              | SF         | 1                  | 0                             |
| **Simulazione 2: Opinion Dynamics** | Stato `Opinion`, behaviour di steering (coesione/separazione/allineamento/inerzia) e regola di convergenza | AB         | 4                  | 0                             |
|                                     | Istanza di Renderable e integrazione nel menu                                                              | SF         | 1                  | 0                             |
| **Documentazione del codice**       | ScalaDoc su InteractionRule, SimulationEngine, SimulationConfig                                            | AB         | 2                  | 0                             |
|                                     | ScalaDoc su MVU e componenti della view                                                                    | SF         | 1                  | 0                             |
|                                     | **Totale**                                                                                                 |            | **37.5**           | **0**                         |

## Divisione del lavoro

- **AB**: completamento delle astrazioni di dominio (InteractionRule), motore di
  simulazione, vocabolario dei behaviour e delle regole, simulazioni dimostrative Epidemia e
  Opinion Dynamics (configurate direttamente tramite `SimulationConfig`, senza builder fluente:
  il DSL dichiarativo è previsto allo Sprint 3).
- **SF**: correzioni all'ambiente di simulazione ereditate dallo Sprint 1; realizzazione
  completa del pattern Model-View-Update (Model, Msg, Mvu) e della relativa test suite;
  interfaccia grafica Swing (Renderable, SimulationPanel, SimulationWindow, MainMenu,
  SimulationOption) con navigazione menu → simulazione → menu; integrazione di Renderable per
  entrambe le simulazioni.
## Definition of Done

- Il motore compila e ha test verdi
- La simulazione Epidemia è avviabile dal menu e osservabile a schermo
- La simulazione Opinion Dynamics è avviabile dal menu e osservabile a schermo
- ScalaDoc presente su tutti i membri pubblici introdotti
- PR di ogni branch feature verso `develop` con test verdi in CI
## Sprint Review

Il motore di simulazione è stato realizzato per intero e compila con test verdi su stati
iniziali deterministici. `InteractionRule`, prerequisito ereditato come debito dallo Sprint 1,
è stata completata come primo task insieme a `visibleWithin` su `AgentContext`, seguendo
l'action item lasciato in chiusura del primo sprint. `SimulationEngine` implementa la pipeline
a snapshot unico pianificata: i vicini si calcolano una sola volta per tick, behaviour e regola
di stato leggono lo stesso `AgentContext`, il movimento è applicato prima e la regola valutata
sul contesto originale.

Entrambe le simulazioni dimostrative sono avviabili dal menu e osservabili a schermo, non solo
Epidemia come da obiettivo minimo iniziale: Opinion Dynamics, pianificata come secondaria, è
stata completata nello stesso sprint. Entrambe sono configurate direttamente tramite
`SimulationConfig`.

Durante lo sviluppo delle due simulazioni sono emersi due difetti comportamentali, non di
compilazione, risolti prima della chiusura dello sprint:


## Sprint Retrospective

Lo Sprint 1 aveva lasciato 5 ore di debito per sottostima del lavoro, con `InteractionRule`
priva di assegnatario in fase di planning e diversi fix previsti al codice. In questo Sprint le stime iniziali sono state
rispettate su tutti i task assegnati: zero ore rimanenti a fine sprint, nessuna nuova ora di
debito accumulata.

### Cosa è andato bene

- Il debito dello Sprint 1 (`InteractionRule` e le correzioni all'ambiente) è stato completato
  come primo task, secondo l'action item lasciato in chiusura del primo sprint.
- La pipeline a snapshot unico del motore, progettata prima di scrivere codice, non ha richiesto
  revisioni durante l'implementazione.
- Il perimetro dello sprint è stato ristretto consapevolmente per concentrarsi sul motore e
  sulle simulazioni dimostrative: questo ha lasciato margine sufficiente per completare anche
  Opinion Dynamics.

### Cosa può essere migliorato

- Alcuni comportamenti previsti dalla specifica di simulazione (es. il moto di un gruppo di
  agenti, o la ripresa di un comportamento normale dopo un cambio di stato) sono stati verificati
  solo osservando la simulazione a schermo, verificare prima a livello di test sarebbe stato preferibile.
- La stima di una simulazione è dovuta essere rivista in corso d'opera, perché il comportamento
  previsto si è rivelato più delicato da modellare correttamente di quanto valutato in fase di
  pianificazione.

### Action items per il prossimo sprint

- Quando un comportamento simulato ha una componente visiva o dinamica non banale, prevedere fin
  dalla stima un margine per l'osservazione e l'aggiustamento a schermo, non solo per la scrittura
  del codice.
- Introdurre nuove astrazioni (memoria con belief, DSL di configurazione) solo a fronte di un
  utilizzatore concreto che le richieda, riprendendo l'action item già lasciato dallo Sprint 1.