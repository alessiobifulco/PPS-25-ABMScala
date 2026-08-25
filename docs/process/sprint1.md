---
title: Sprint 1
layout: default
nav_order: 0
parent: Processo di Sviluppo
---

# Sprint 1 - Setup & Dominio Base

## Obiettivo

L'obiettivo di questo primo Sprint è configurare il progetto e le pipeline di sviluppo, e
realizzare le fondamenta del dominio del framework — `Agent`, `Action`, `ActionGraph`,
`Behavior`, `AgentContext`, `InteractionRule`, `P2d`, `V2d` — insieme all'`Environment` di
simulazione — `Space`, `BoundaryPolicy`, `Environment`, `NeighborStrategy`. Al termine dello
sprint il dominio base dovrà compilare con test verdi, pronto per l'integrazione del
`SimulationEngine` negli sprint successivi.

## Deadline

La scadenza dello sprint è il 25/07/2026.

## Backlog

| Product Backlog Item          | Sprint Task                                                                   | Volontario | Stima iniziale (h) | Ore rimanenti a fine Sprint 1 |
|-------------------------------|-------------------------------------------------------------------------------|------------|--------------------|-------------------------------|
| **Project Setup**             | Creazione repository GitHub PPS-25-ABMScala                                   | AB + SF    | 0.5                | 0                             |
|                               | Configurazione SBT con Scala 3.3.8                                            | AB + SF    | 0.5                | 0                             |
|                               | Configurazione plugin SBT (assembly, scoverage, scalafmt)                     | AB + SF    | 1                  | 0                             |
|                               | Configurazione .scalafmt.conf                                                 | AB + SF    | 0.5                | 0                             |
|                               | Configurazione ACT                                                            | AB + SF    | 1                  | 0                             |
|                               | Invito SF al repository                                                       | AB + SF    | 0.5                | 0                             |
| **CI/CD**                     | GitHub Action CI                                                              | AB + SF    | 1                  | 0                             |
|                               | GitHub Action Release                                                         | AB + SF    | 1                  | 0                             |
|                               | GitHub Action Documentation e GitHub Pages                                    | AB + SF    | 1                  | 0                             |
|                               | GitHub Action Release Sprint documentation                                    | AB + SF    | 0.5                | 0                             |
| **Documentazione iniziale**   | Scrittura docs/1-intro.md                                                     | AB         | 1                  | 0                             |
|                               | Scrittura docs/2-process.md                                                   | AB         | 1                  | 0                             |
|                               | Scrittura docs/3-analysis.md                                                  | AB         | 2                  | 0                             |
|                               | Scrittura process/backlog.md                                                  | AB         | 1                  | 0                             |
|                               | Scrittura process/sprint1.md                                                  | AB         | 1                  | 0                             |
| **Vettori e posizioni**       | `V2d`: somma, scala, lunghezza, normalizzazione, random                       | AB         | 2                  | 0                             |
|                               | `P2d`: traslazione, differenza                                                | AB         | 1                  | 0                             |
| **Agent**                     | `Agent` (trait + companion), campi id/position/velocity/state                 | AB         | 2                  | 0                             |
|                               | `AgentId` come opaque type su Int                                             | AB         | 1                  | 0                             |
|                               | `Memory` (versione minimale)                                                  | AB         | 1                  | 0                             |
| **Action e Behavior**         | `Action`: trait + Move, ShareMemory, MultiAction; `Action.flatten`            | AB         | 2                  | 0                             |
|                               | `ActionGraph`: Leaf, Branch, resolve                                          | AB         | 2                  | 0                             |
|                               | `Behavior`: trait, apply, fromGraph, andThen                                  | AB         | 2                  | 0                             |
|                               | `AgentContext` (focus, neighbors, tick)                                       | AB         | 1                  | 0                             |
| **Interaction Rule**          | `InteractionRule` (trait, composizione) e test suite                          | AB         | 2                  | 2                             |
| **Environment**               | `Space`, Toroidal, `RectangularSpace`, `CircularSpace` e test suite           | SF         | 3                  | 1                             |
|                               | `BoundaryPolicy` (Bounce, Stop, Wrap) e test suite                            | SF         | 2                  | 0                             |
|                               | `Environment` (trait, companion, impl) e test suite                           | SF         | 2                  | 1                             |
|                               | `NeighborStrategy` (BruteForce, Grid) e test suite                            | SF         | 3                  | 1                             |
| **Documentazione del codice** | ScalaDoc su `Agent`, `AgentId`, `Memory`, `Action`, `ActionGraph`, `Behavior` | AB         | 2                  | 0                             |
|                               | ScalaDoc su `Space`, `Environment`, `BoundaryPolicy`, `NeighborStrategy`      | SF         | 1                  | 0                             |
| **Totale**                    |                                                                               |            | **42**             | **5**                         |

## Divisione del lavoro

- **AB**: setup del progetto e delle pipeline di sviluppo; entità del dominio (`Agent`,
  `AgentId`, `Memory`) e astrazioni comportamentali (`Action`, `ActionGraph`, `Behavior`,
  `AgentContext`, `InteractionRule`); primitive geometriche `P2d` e `V2d`; documentazione
  iniziale del progetto.
- **SF**: modellazione dello `Space` di simulazione nelle varianti rettangolare e circolare;
  `BoundaryPolicy` per il comportamento ai confini; `Environment` come contenitore della
  popolazione; `NeighborStrategy` per il calcolo dei vicini, con implementazione brute force e
  a griglia spaziale.

## Definition of Done

- Le astrazioni del dominio compilano e hanno test verdi in CI.
- Le pipeline di CI, release e pubblicazione della documentazione sono funzionanti.
- ScalaDoc presente su tutti i membri pubblici introdotti.
- PR di ogni branch feature verso `develop` con test verdi in CI.

## Sprint Review

Lo stakeholder si ritiene soddisfatto solo in parte del lavoro svolto durante il primo sprint.
Le fondamenta del dominio sono state realizzate quasi per intero: sono definite e verificate le
entità che descrivono un agente, le sue azioni e il suo comportamento, così come lo spazio di
simulazione nelle varianti rettangolare e circolare, il comportamento degli agenti ai confini e
le due strategie di calcolo dei vicini. La copertura dei test supera il 90% e le pipeline di
integrazione continua, di release e di pubblicazione della documentazione sono operative.

Lo stakeholder rileva però che `InteractionRule`, una delle quattro astrazioni fondamentali
previste dai requisiti, non è stata realizzata, e che alcune correzioni all'`Environment`
emerse dalla revisione del codice non sono state applicate. Restano quindi 5 ore di lavoro non
completato, riportate come debito allo sprint successivo.

Trattandosi di uno sprint interamente infrastrutturale, non vi è ancora nulla di osservabile a
schermo: la verifica si è basata sulla test suite e sulla revisione del codice.

## Sprint Retrospective

Lo sprint ha avuto una durata di due settimane e ha dedicato la prima fase alla configurazione
del progetto e allo studio dell'architettura di base. La suddivisione dei task è risultata
equilibrata tra i due membri, con una separazione netta delle responsabilità: AB sulle entità
del dominio e sulle astrazioni comportamentali, SF sulla modellazione dello spazio di
simulazione, sulle politiche di confine e sulle strategie di calcolo dei vicini.

### Cosa è andato bene

- La configurazione iniziale del progetto (repository, GitHub Actions, SBT) è stata completata
  rapidamente, permettendo a entrambi di iniziare lo sviluppo senza blocchi tecnici
- La definizione dei trait e delle interfacce fin dall'inizio ha permesso di lavorare in
  parallelo senza conflitti di merge
- L'approccio incrementale (versione minima, test, refactoring) ha reso i cambi di design
  emersi a metà sprint poco costosi da assorbire

### Cosa può essere migliorato

- Il carico di lavoro si è rivelato eccessivo rispetto alla capacità del team: 5 ore su 42 non
  sono state completate entro la deadline
- Alcune astrazioni sono state implementate prima di avere un caso d'uso concreto, con il
  rischio di sottrarre tempo a quelle effettivamente necessarie
- In fase di planning un Product Backlog Item è rimasto privo di assegnatario, e questo è stato
  rilevato solo a fine sprint

### Action items per il prossimo sprint

- Ridurre il numero di task pianificati, tenendo conto delle ore residue riportate da questo
  sprint
- Completare `InteractionRule` e le correzioni all'`Environment` come primi task dello sprint
  successivo, essendo prerequisito per il motore di simulazione
- Verificare in chiusura del planning che ogni Sprint Task abbia un assegnatario
- Introdurre nuove astrazioni solo a fronte di un utilizzatore concreto, per evitare di non
  riuscire a completare gli aspetti concreti necessari