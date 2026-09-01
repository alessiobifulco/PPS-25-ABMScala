---
title: Testing
nav_order: 7
parent: Report
---

# Testing

## Approccio

Considerata la natura del progetto, una libreria per simulazioni ad agenti, in cui il
comportamento osservabile emerge dalla composizione di molte unità indipendenti (spazio,
agenti, comportamenti, regole, motore di aggiornamento), è stato necessario verificare sia
la correttezza delle singole unità sia quella delle loro collaborazioni.

I test sono stati scritti con approccio **Test Driven Development (TDD)**, coerentemente con la
*definition of done* adottata dal team, che considera un task concluso solo quando i test
relativi alla funzionalità sono presenti e passano in CI. Ogni push sul repository attiva la
compilazione e l'esecuzione dell'intera suite tramite GitHub Actions.

La distinzione tra i due livelli di test segue il criterio presentato a lezione: un test è
unitario quando verifica una singola unità di comportamento, lo fa rapidamente e lo fa in
isolamento rispetto alle altre unità e agli altri test; quando una di queste tre condizioni
non è soddisfatta, il test ricade nella categoria dei test di integrazione. Nel progetto la
verifica delle collaborazioni è stata mantenuta a livello di integrazione e non di sistema:
i test isolano di volta in volta le sole collaborazioni necessarie a verificare un
comportamento multi-componente.

La scelta di modellare il dominio con strutture **immutabili** e funzioni pure ha semplificato
notevolmente questa attività: non esistendo stato globale condiviso, ogni caso di test può
costruire il proprio scenario e valutarne il risultato senza fasi di *teardown* e senza
dipendenze dall'ordine di esecuzione.

## Tecnologie utilizzate

Per la scrittura e l'esecuzione dei test è stato utilizzato **ScalaTest**, affiancato da
**Mockito**.

Di ScalaTest sono state sfruttate principalmente:

- lo stile **`AnyFlatSpec`**, che permette di esprimere ogni caso di test come una frase
  (`"A memory" should "start with no beliefs"`), rendendo il nome del test una descrizione
  leggibile della condizione verificata;
- i **matchers** (`shouldBe`, `should have size`, `should contain` per i confronti su
  valori in virgola mobile), che rendono le asserzioni più espressive dei semplici `assert`;
- il costrutto `an[...] should be thrownBy`, usato per verificare il rispetto delle
  precondizioni dei costruttori.

Di Mockito sono stati usati i costrutti di base:

- `mock(classOf[C])` per creare l'implementazione sostitutiva di una dipendenza;
- `when(...).thenReturn(...)` per configurare la risposta di uno **stub**;
- `verify(...)` per controllare che il SUT interagisca con il *depended-on component* nel modo
  atteso.

## Organizzazione delle suite

È stata definita una suite per ciascun componente logico, secondo la suddivisione in livelli
introdotta nel design architetturale.

| Livello                     | Suite                                                                                                                                                                |
|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Domain — spazio e geometria | `P2dTest`, `V2dTest`, `RectangularSpaceTest`, `CircularSpaceTest`, `BoundaryPolicyTest`, `NeighborStrategyTest`                                                      |
| Domain — agente e ambiente  | `AgentTest`, `ActionTest`, `MemoryTest`, `POITest`, `BehaviorTest`, `InteractionRuleTest`, `EnvironmentTest`                                                         |
| DSL                         | `BehaviorsBuilderTest`, `RulesBuilderTest`, `EnvironmentBuilderTest`, `CompositeBehaviorTest`, `ConditionalBehaviorTest`, `DiscreteRulesTest`, `ContinuousRulesTest` |
| Engine                      | `SimulationEngineTest`                                                                                                                                               |
| GUI                         | `ModelTest`, `MvuTest`, `MsgTest`, `StateTest`, `MonadTest`, `RenderableTest`, `POIRenderableTest`, `SimulationOptionTest`                                           |

## Uso dei test double

Diversi componenti del dominio dipendono da altri componenti per funzionare correttamente: in
questi casi la dipendenza è stata sostituita da un test double, così da verificare il SUT in
isolamento ed evitare che un fallimento riguardi la collaborazione anziché l'unità.

- **`Memory`** è sostituita da uno stub in `AgentTest`, `ConditionalBehaviorTest` e
  `DiscreteRulesTest`, dove serve a fornire un insieme di credenze noto e stabile senza
  dipendere dalla politica di scarto della memoria reale.
- **`Space`** è sostituito da un mock in `BoundaryPolicyTest`: le politiche di confine si
  limitano a delegare allo spazio, quindi il test verifica il valore restituito e, con
  `verify`, che la delega avvenga effettivamente.
- **`NeighborStrategy`** è sostituita da un mock in `EnvironmentTest`, per controllare che
  l'ambiente inoltri alla strategia l'agente, la popolazione e il raggio corretti, senza
  vincolare il test all'algoritmo di ricerca concreto.
- **`Environment`** e **`SimulationConfig`** sono sostituiti da stub in `ModelTest` e
  `MvuTest`, così da poter costruire il modello della GUI senza allestire una simulazione
  completa.

## Test di integrazione

Le collaborazioni tra moduli sono verificate da due gruppi di test.

`SimulationEngineTest` verifica il ciclo di aggiornamento nel suo complesso, cioè
l'integrazione tra motore, comportamenti, regole di interazione, spazio, memoria e punti di
interesse. In particolare sono verificati: l'avanzamento del tick, l'applicazione dei
comportamenti al moto degli agenti, il cambio di stato prodotto dalle regole, la rimozione
degli agenti che muoiono e l'inserimento di quelli generati con un identificatore fresco, la
consegna di un evento all'agente destinatario e il conteggio della permanenza all'interno di
un POI. Nei casi che coinvolgono la memoria, quest'ultima resta sostituita da un mock e
l'asserzione è espressa su come il motore la utilizza, limitando così l'ampiezza
dell'integrazione alle sole collaborazioni di interesse.

`MvuTest` e `ModelTest` verificano l'integrazione tra la GUI e il motore, controllando che le
funzioni `init` e `update` del ciclo Model-View-Update producano il modello atteso in risposta
ai messaggi ricevuti e che lo stato non avanzi quando la simulazione è in pausa.

## Grado di copertura

La suite è composta da **29 classi di test** per un totale di **235 casi di test**, che
coprono tutte le funzionalità principali della libreria:

- **Geometria e spazio**: operazioni su posizioni e vettori, appartenenza allo spazio,
  politiche di confine, ricerca dei vicini;
- **Modello dell'agente**: aggiornamenti non distruttivi di moto, stato e memoria, azioni
  producibili, gestione della memoria a capacità limitata;
- **DSL**: accumulo ordinato di comportamenti e regole nei builder, costruzione della
  specifica di ambiente, comportamenti composti e condizionali, regole discrete e continue;
- **Engine**: inizializzazione, avanzamento del tick, applicazione di comportamenti e regole,
  nascita e morte degli agenti, residenza nei POI;
- **GUI**: modello e funzione di aggiornamento MVU, monade di stato, associazione tra stato di
  dominio e rappresentazione grafica.

Particolare attenzione è stata dedicata ai **casi limite e agli input non validi**: il rifiuto
di dimensioni o raggi non positivi per gli spazi, il rifiuto di una capacità di memoria non
positiva, la normalizzazione del vettore nullo, il comportamento di un agente privo di vicini e
la posizione esattamente sul confine dello spazio o sul bordo del raggio di percezione.

Restano fuori dalla verifica automatica i componenti Swing di sola presentazione
(`SimulationWindow`, `MainMenu`, `SimulationPanel`, `StatisticsPanel`) e le simulazioni di
esempio (`Epidemic`, `AlarmSpreading`, `AntColony`, `OpinionDynamics`, `Main`): i primi
contengono l'unico stato mutabile del sistema e sono stati validati manualmente durante
l'esecuzione, mentre le seconde costituiscono programmi d'uso del DSL, le cui costruzioni sono
già coperte dalle suite del livello DSL. Le classi effettivamente sostituibili da test double
(`Renderable`, `POIRenderable`, `Msg`, `SimulationOption`) sono invece testate, così da coprire
la parte di logica della GUI indipendente dal disegno.

L'esecuzione regolare della suite in CI durante tutto il ciclo di sviluppo ha garantito
correttezza logica, robustezza rispetto agli input non validi e assenza di regressioni dopo
ogni refactoring.

[Indice](0-index.md) | [Capitolo Precedente](6-implementation.md) | [Capitolo Successivo](8-retroprospective.md)