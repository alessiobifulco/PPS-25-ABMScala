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
Behavior, AgentContext, InteractionRule, P2d, V2d) insieme all'ambiente di simulazione
(Space, BoundaryPolicy, Environment, NeighborStrategy). Al termine dello sprint il
dominio base dovrà compilare con test verdi, pronto per l'integrazione del motore di
simulazione negli sprint successivi.

## Deadline

La scadenza dello sprint è il 25/07/2026.

## Backlog

| Product Backlog Item          | Sprint Task                                                       | Volontario | Stima iniziale (h) | Ore rimanenti a fine Sprint 1 |
|-------------------------------|-------------------------------------------------------------------|------------|--------------------|-------------------------------|
| **Project Setup**             | Creazione repository GitHub PPS-25-ABMScala                       | AB + SF    | 0.5                | 0                             |
|                               | Configurazione SBT con Scala 3.3.8                                | AB + SF    | 0.5                | 0                             |
|                               | Configurazione plugin SBT (assembly, scoverage, scalafmt)         | AB + SF    | 1                  | 0                             |
|                               | Configurazione .scalafmt.conf                                     | AB + SF    | 0.5                | 0                             |
|                               | Configurazione ACT                                                | AB + SF    | 1                  | 0                             |
|                               | Invito SF al repository                                           | AB + SF    | 0.5                | 0                             |
| **CI/CD**                     | GitHub Action CI                                                  | AB + SF    | 1                  | 0                             |
|                               | GitHub Action Release                                             | AB + SF    | 1                  | 0                             |
|                               | GitHub Action Documentation e GitHub Pages                        | AB + SF    | 1                  | 0                             |
|                               | GitHub Action Release Sprint documentation                        | AB + SF    | 0.5                | 0                             |
| **Documentazione iniziale**   | Scrittura docs/1-intro.md                                         | AB         | 1                  | 0                             |
|                               | Scrittura docs/2-process.md                                       | AB         | 1                  | 0                             |
|                               | Scrittura docs/3-analysis.md                                      | AB         | 2                  | 0                             |
|                               | Scrittura process/backlog.md                                      | AB         | 1                  | 0                             |
|                               | Scrittura process/sprint1.md                                      | AB         | 1                  | 0                             |
| **Vettori e posizioni**       | V2d: somma, scala, lunghezza, normalizzazione, random             | AB         | 2                  | 0                             |
|                               | P2d: traslazione, differenza                                      | AB         | 1                  | 0                             |
| **Agent**                     | Agent (trait + companion), campi id/position/velocity/state       | AB         | 2                  | 0                             |
|                               | AgentId come opaque type su Int                                   | AB         | 1                  | 0                             |
|                               | Memory (versione minimale)                                        | AB         | 1                  | 0                             |
| **Action e Behavior**         | Action: trait + Move, ShareMemory, MultiAction; Action.flatten    | AB         | 2                  | 0                             |
|                               | ActionGraph: Leaf, Branch, resolve                                | AB         | 2                  | 0                             |
|                               | Behavior: trait, apply, fromGraph, andThen                        | AB         | 2                  | 0                             |
|                               | AgentContext (focus, neighbors, tick)                             | AB         | 1                  | 0                             |
| **Interaction Rule**          | InteractionRule (trait, composizione) e test suite                | AB         | 2                  | 2                             |
| **Environment**               | Space, Toroidal, RectangularSpace, CircularSpace e test suite     | SF         | 3                  | 1                             |
|                               | BoundaryPolicy (Bounce, Stop, Wrap) e test suite                  | SF         | 2                  | 0                             |
|                               | Environment (trait, companion, impl) e test suite                 | SF         | 2                  | 1                             |
|                               | NeighborStrategy (BruteForce, Grid) e test suite                  | SF         | 3                  | 1                             |
| **Documentazione del codice** | ScalaDoc su Agent, AgentId, Memory, Action, ActionGraph, Behavior | AB         | 2                  | 0                             |
|                               | ScalaDoc su Space, Environment, BoundaryPolicy, NeighborStrategy  | SF         | 1                  | 0                             |
|                               | **Totale**                                                        |            | **42**             | **5**                         |

## Sprint Review

Le fondamenta del dominio sono state realizzate quasi per intero. Sono implementati,
compilano e hanno test verdi in CI: P2d, V2d, Agent, AgentId, Memory in versione
minimale, Action, ActionGraph, Behavior, AgentContext; lato ambiente Space con
implementazioni rettangolare e circolare, BoundaryPolicy nelle varianti bounce/wrap/stop,
Environment e NeighborStrategy con brute force e griglia spaziale, con copertura
superiore al 90%.

Restano 5 ore di lavoro non completato. `InteractionRule`, una delle quattro astrazioni
fondamentali previste dai requisiti, non è stata realizzata: era priva di assegnatario
in fase di planning ed è emersa come scoperta solo a fine sprint. Le restanti 3 ore
riguardano correzioni emerse dalla revisione del codice dell'ambiente: l'introduzione di
`randomPosition` e `shape` su `Space`, l'esposizione di `neighborhoods` su `Environment`
perché il motore possa costruire l'indice dei vicini una volta per tick invece che una
volta per agente, e la semplificazione di `NeighborStrategy`.


## Sprint Retrospective

Lo sprint ha dedicato la prima fase alla configurazione del progetto (SBT, CI/CD,
scalafmt) e allo studio dell'architettura di base. La suddivisione dei task è risultata
equilibrata tra i due membri, con una separazione netta delle responsabilità: AB sulle
entità del dominio e sulle astrazioni comportamentali, SF sulla modellazione dello
spazio di simulazione, sulle regole di confine e sulle strategie di calcolo dei vicini.

### Cosa è andato bene

- La configurazione iniziale del progetto (repository, GitHub Actions, SBT) è stata
  completata rapidamente, permettendo a entrambi di iniziare lo sviluppo senza blocchi
  tecnici.
- La definizione dei trait e delle interfacce fin dall'inizio ha permesso di lavorare in
  parallelo senza conflitti di merge.
- L'approccio incrementale (versione minima, test, refactoring) ha reso i cambi di design
  emersi a metà sprint poco costosi da assorbire.

### Cosa può essere migliorato

- Il carico di lavoro si è rivelato eccessivo rispetto alla capacità del team: 5 ore su
  42 non sono state completate entro la deadline.
- Alcune astrazioni sono state implementate prima di avere un caso d'uso concreto,
  richiedendo poi una revisione che ha generato lavoro aggiuntivo non pianificato.

### Action items per il prossimo sprint

- Ridurre il numero di task pianificati, tenendo conto delle ore residue riportate da
  questo sprint.
- Completare `InteractionRule` come primo task, essendo prerequisito per il motore di
  simulazione.
- Completare le correzzioni come primo task.
- Introdurre nuove astrazioni solo a fronte di un utilizzatore concreto, per evitare
  revisioni successive.