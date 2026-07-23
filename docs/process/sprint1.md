---
title: Sprint 1
layout: default
nav_order: 0
parent: Processo di Sviluppo
---

# Sprint 1 - Setup & Dominio Base

## Obiettivo

L'obiettivo di questo primo Sprint è configurare il progetto e le pipeline di sviluppo,
e realizzare le fondamenta del dominio del framework (Agent, Action, ActionGraph,
Behavior, AgentContext, P2d, V2d) insieme all'ambiente di simulazione (Environment,
Bounds, BoundaryPolicy, NeighborStrategy). Al termine dello sprint, il dominio base
dovrà compilare con test verdi e la struttura iniziale della view dovrà essere pronta
per l'integrazione del motore di simulazione negli sprint successivi.

## Deadline

La scadenza dello sprint è il 25/07/2026.

## Backlog

| Nome                          | Descrizione                                                                                                   | Sprint Task                                                                                       | Developer |
|-------------------------------|---------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|-----------|
| **Project Setup**             | Configurazione iniziale del repository, build tool e strumenti di sviluppo                                    | Creazione repository GitHub PPS-25-ABMScala                                                       | AB + SF   |
|                               |                                                                                                               | Configurazione SBT con Scala 3.3.8                                                                | AB + SF   |
|                               |                                                                                                               | Configurazione plugin SBT (assembly, scoverage, scalafmt)                                         | AB + SF   |
|                               |                                                                                                               | Configurazione .scalafmt.conf                                                                     | AB + SF   |
|                               |                                                                                                               | Configurazione ACT                                                                                | AB + SF   |
|                               |                                                                                                               | Invito SF al repository                                                                           | AB + SF   |
| **CI/CD**                     | Automazione dei processi di build, test e pubblicazione della documentazione                                  | GitHub Action CI                                                                                  | AB + SF   |
|                               |                                                                                                               | GitHub Action Release                                                                             | AB + SF   |
|                               |                                                                                                               | GitHub Action Documentation e GitHub Pages                                                        | AB + SF   |
|                               |                                                                                                               | GitHub Action Release Sprint documentation                                                        | AB + SF   |
| **Documentazione iniziale**   | Stesura della documentazione di progetto e di processo                                                        | Scrittura docs/1-intro.md                                                                         | AB        |
|                               |                                                                                                               | Scrittura docs/2-process.md                                                                       | AB        |
|                               |                                                                                                               | Scrittura docs/3-analysis.md                                                                      | AB        |
|                               |                                                                                                               | Scrittura process/backlog.md                                                                      | AB        |
|                               |                                                                                                               | Scrittura process/sprint1.md                                                                      | AB        |
| **Vettori e posizioni**       | Astrazioni geometriche di base del dominio                                                                    | V2d: somma, scala, lunghezza, normalizzazione, random                                             | AB        |
|                               |                                                                                                               | P2d: traslazione, differenza                                                                      | AB        |
| **Agent**                     | Entità agente del framework, con identificatore type-safe e memoria opzionale                                 | Agent (trait + companion), campi id/position/velocity/state                                       | AB        |
|                               |                                                                                                               | AgentId come opaque type su Int                                                                   | AB        |
|                               |                                                                                                               | Memory (versione minimale)                                                                        | AB        |
| **Action e Behavior**         | Astrazioni dichiarative per gli effetti e le decisioni di un agente                                           | Action: trait + Move, Nudge, ShareMemory, MultiAction; Action.flatten                             | AB        |
|                               |                                                                                                               | ActionGraph: Leaf, Branch, resolve                                                                | AB        |
|                               |                                                                                                               | Behavior: trait, apply, fromGraph, andThen                                                        | AB        |
|                               |                                                                                                               | AgentContext (focus, neighbors, tick)                                                             | AB        |
| **Environment**               | Spazio di simulazione, confini, strategie di calcolo dei vicini e relative suite di test (coverage > 90%)     | Space, Toroidal, RectangularSpace, CircularSpace e test suite (coverage > 90%)                    | SF        |
|                               |                                                                                                               | BoundaryPolicy (Bounce, Stop, Wrap) e test suite (coverage > 90%)                                 | SF        |
|                               |                                                                                                               | Environment (trait, companion, impl) e test suite (coverage > 90%)                                | SF        |
|                               |                                                                                                               | NeighborStrategy (BruteForce, Grid, SpatialHash) e test suite (coverage > 90%)                    | SF        |
| **Documentazione del codice** | ScalaDoc sui membri pubblici introdotti nello sprint                                                          | ScalaDoc su Agent, AgentId, Memory, Action, ActionGraph, Behavior, AgentContext                   | AB        |
|                               |                                                                                                               | ScalaDoc su Space, RectangularSpace, CircularSpace, Environment, BoundaryPolicy, NeighborStrategy | SF        |

## Sprint Review

Lo stakeholder si ritiene soddisfatto del lavoro svolto durante il primo sprint. Gli obiettivi prefissati sono stati pienamente raggiunti: le fondamenta del dominio del framework (Agent, Action, Behavior) e l'ambiente di simulazione (Environment, Space, BoundaryPolicy, NeighborStrategy) sono stati implementati con successo. Il codice compila senza errori, l'integrazione continua funziona regolarmente, la suite di test ha superato l'obiettivo del 90% di coverage.

## Sprint Retrospective

Lo sprint ha dedicato la sua prima fase alla configurazione del progetto (SBT, CI/CD, scalafmt) e allo studio dell'architettura di base. La suddivisione dei task è risultata nel complesso equilibrata tra i due membri del team, con una chiara separazione delle responsabilità: SF ha gestito con successo la modellazione dello spazio di simulazione, le regole di confine e le strategie di calcolo dei vicini.


## Cosa è andato bene
• La configurazione iniziale del progetto (repository, GitHub Actions, SBT) è stata completata rapidamente, permettendo a entrambi di iniziare lo sviluppo senza blocchi tecnici.

• La definizione chiara dei trait e delle interfacce fin dall'inizio ha facilitato il lavoro parallelo senza causare conflitti di merge.

• L'approccio rigoroso ai test ha garantito una codebase solida e affidabile fin dai primissimi giorni di sviluppo.

## Cosa può essere migliorato
• Il carico di lavoro per questo sprint si è rivelato leggermente troppo basso; il team ha sottostimato la propria capacità di sviluppo e la rapidità di esecuzione.

## Action items per il prossimo sprint
• Calibrare meglio le stime dei task, aumentando leggermente il carico di lavoro complessivo per sfruttare appieno il potenziale e il ritmo del team.