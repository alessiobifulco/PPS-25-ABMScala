# Proposta di Progetto

**Deadline**: 18/09/2026

**Titolo**: Agent-Based-Model Simulation (ABMScala)

## Componenti del Gruppo

- **Alessio Bifulco**: alessio.bifulco@studio.unibo.it
- **Samuele Ferri**: samuele.ferri3@studio.unibo.it

## Processo di Sviluppo

Il gruppo intende adottare il processo di sviluppo consigliato nel punto **P8** delle regole d'esame.

## Divisione del Lavoro

Nel primo meeting verranno definiti i requisiti del sistema che, successivamente, a ogni Sprint Planning saranno analizzati e suddivisi in task. L'assegnazione di questi ultimi verrà decisa secondo la metodologia **Agile/SCRUM**.

## Sintesi dei Requisiti di Sistema

Il progetto ha l'obiettivo di realizzare un mini-framework generico, attraverso una DSL, per la modellazione e l'esecuzione di simulazioni **Agent-Based (ABMS)**, sviluppato in **Scala 3** secondo il paradigma funzionale.

Il framework dovrà consentire di definire simulazioni componendo gli elementi fondamentali del sistema.

### Principali astrazioni offerte dal framework

- Agent
- Behavior
- Environment
- Interaction Rule

Per dimostrare la flessibilità del framework verranno realizzate alcune simulazioni di esempio, costruite combinando le diverse componenti sullo stesso motore di esecuzione.

## Principali elementi e funzionalità dell'applicazione

- Astrazione della definizione di **Agent**.
- Astrazione della definizione di **Behavior** per la gestione del movimento.
- Astrazione della definizione di **Interaction Rule**.
- Creazione e visualizzazione dell'**Environment**.
- Configurazione di simulazioni di esempio attraverso DSL.

## Funzionalità opzionali

- Memoria associata agli agenti.
- Punti di interesse con effetti configurabili sugli agenti.
- Statistiche in tempo reale sull'andamento della simulazione.
- Esportazione dei dati prodotti dalla simulazione.
- Astrazione di Path/WayPoint per instradare gli agenti.