---
title: Sprint 3
layout: default
nav_order: 2
parent: Processo di Sviluppo
---

# Sprint 3 - DSL Dichiarativo e View Monadica

## Obiettivo

L'obiettivo di questo Sprint è duplice e punta a migliorare radicalmente l'ergonomia e la purezza architetturale del framework.
In primo luogo, verrà sviluppato un DSL (Domain Specific Language) dichiarativo sfruttando le feature di Scala 3 (come le *context functions*), per consentire una configurazione fluida ed espressiva delle simulazioni, andando a sostituire la configurazione manuale di `SimulationConfig`.
In secondo luogo, il livello della View verrà refattorizzato applicando pattern funzionali puri: gli effetti collaterali legati all'interfaccia grafica (Swing) verranno isolati all'interno di un wrapper monadico (es. `IO` / `Task`), garantendo maggiore testabilità e separazione netta tra la logica di aggiornamento e il rendering effettivo. Infine, verranno introdotti i Point of Interest (POI) come nuovo elemento configurabile dell'ambiente di simulazione.

## Deadline

La scadenza dello sprint è il 15/08/2026.

## Backlog

| Product Backlog Item  | Sprint Task                                                                                      | Volontario | Stima iniziale (h) | Ore rimanenti a fine Sprint 3 |
|-----------------------|--------------------------------------------------------------------------------------------------|------------|--------------------|-------------------------------|
| **DSL Core**          | Astrazione base del DSL (`SimulationBuilder` e context functions)                                | AB         | 4                  | 4                             |
|                       | DSL per la definizione di `Environment`, `Space` e `BoundaryPolicy`                              | AB         | 3                  | 3                             |
|                       | DSL per la definizione di Popolazione, Agenti e Stato Iniziale                                   | AB         | 4                  | 4                             |
|                       | DSL per la composizione di `Behaviour` e `InteractionRule`                                       | AB         | 3                  | 3                             |
| **Functional View**   | Setup della Monade `IO` (o `Task`) per la gestione degli effetti collaterali                     | SF         | 4                  | 4                             |
|                       | Refactoring di `SimulationPanel` e `SimulationWindow` in ottica monadica                         | SF         | 5                  | 5                             |
|                       | Integrazione del loop di MVU con la nuova gestione pura del rendering                            | SF         | 3                  | 3                             |
| **Point of Interest** | Definizione di `POI` (posizione, raggio, ritardo di attivazione) e integrazione in `Environment` | SF         | 4                  | 4                             |
| **Migrazione**        | Migrazione di *Epidemia* sul nuovo DSL e sulla nuova View                                        | AB + SF    | 1                  | 1                             |
|                       | Migrazione di *Opinion Dynamics* sul nuovo DSL e sulla nuova View                                | AB + SF    | 1                  | 1                             |
| **Documentazione**    | ScalaDoc sulle feature del DSL (builder, syntax, impliciti)                                      | AB         | 2                  | 2                             |
|                       | ScalaDoc sull'architettura monadica della View e gestione side-effects                           | SF         | 2                  | 2                             |
|                       | **Totale**                                                                                       |            | **36**             | **36**                        |

## Divisione del lavoro

- **AB**: Responsabile interamente della progettazione e realizzazione del DSL. Si occuperà di sfruttare i costrutti di Scala 3 per creare un vocabolario dichiarativo che copra tutto lo spettro della configurazione (Ambiente, Agenti, Comportamenti, Regole).
- **SF**: Responsabile del refactoring funzionale della View. Si occuperà di isolare gli effetti collaterali di Swing tramite l'introduzione di una Monade dedicata, riorganizzando il ciclo di update di MVU per restituire descrizioni di computazioni (`IO`) anziché eseguire effetti diretti. Si occuperà inoltre della progettazione e integrazione dei Point of Interest nell'ambiente di simulazione, e infine della migrazione delle simulazioni esistenti sulla nuova architettura.

## Definition of Done

- Il DSL permette di configurare un'intera simulazione in modo dichiarativo e compila senza errori.
- La logica di rendering della View non esegue effetti diretti ma restituisce tipi monadici puri.
- I Point of Interest sono configurabili e integrati nell'ambiente di simulazione.
- Le simulazioni precedenti (Epidemia, Opinion Dynamics) funzionano perfettamente con il nuovo DSL e la nuova architettura della View.
- ScalaDoc è presente su tutte le nuove astrazioni (Builder del DSL, POI e classi della View monadica).
- PR di ogni branch feature verso `develop` con test verdi in CI.

## Sprint Review

## Sprint Retrospective

### Cosa è andato bene
- ...

### Cosa può essere migliorato
- ...

### Action items per il prossimo sprint
- ...