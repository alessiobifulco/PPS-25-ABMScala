# Sprint 1 — Planning

**Data inizio:** 21/07/2026
**Data fine prevista:** 25/07/2026
**Obiettivo:** Costruire gli elementi di base del dominio (Agent, Behavior, Action, P2d, V2d)
e l'ambiente di simulazione (Environment, Bounds, BoundaryPolicy, NeighborStrategy), oltre
alla base della view.

## Legenda
- **AB** — Alessio Bifulco
- **SF** — Samuele Ferri

## Backlog

| ID    | PB    | Task                                                              | Assegnato | Stima (h) | Stato |
|-------|-------|-------------------------------------------------------------------|-----------|-----------|-------|
| S1-01 | PB-02 | V2d: somma, scala, lunghezza, normalizzazione, random             | AB        | 2         | Done  |
| S1-02 | PB-02 | P2d: traslazione, differenza                                      | AB        | 1         | Done  |
| S1-03 | PB-02 | Agent (trait + companion), campi id/position/velocity/state       | AB        | 2         | Done  |
| S1-04 | PB-02 | AgentId come opaque type su Int                                   | AB        | 1         | Done  |
| S1-05 | PB-12 | Memory (versione minimale)                                        | AB        | 1         | Done  |
| S1-06 | PB-03 | Action:                                                           | AB        | 1         | Done  |
| S1-07 | PB-04 | Behavior:                                                         | AB        | 2         | To do |
| S1-08 | PB-22 | ScalaDoc                                                          | AB        | 1         | To do |
| S1-09 | PB-05 | Bounds:                                                           | SF        | 1.5       | To do |
| S1-10 | PB-05 | BoundaryPolicy:                                                   | SF        | 1         | To do |
| S1-11 | PB-05 | Environment                                                       | SF        | 2         | To do |
| S1-12 | PB-06 | NeighborStrategy:                                                 | SF        | 1.5       | To do |
| S1-13 | PB-10 | Base della view (struttura iniziale, no rendering completo)       | SF        | 2         | To do |
| S1-14 | PB-22 | ScalaDoc su Environment, Bounds, BoundaryPolicy, NeighborStrategy | SF        | 1         | To do |

## Definition of Done
- Tutti i tipi di dominio elencati compilano e hanno test verdi
- ScalaDoc presente su tutti i membri pubblici introdotti
- PR di ogni branch feature verso `develop` con test verdi in CI

## Link to Review
- [Sprint 1 Review](sprint1_rw.md)

## Link to Backlog
- [Backlog](backlog.md)