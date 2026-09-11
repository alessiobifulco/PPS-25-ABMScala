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

I test sono stati scritti in parallelo all'implementazione delle funzionalità, coerentemente
con la *definition of done* adottata dal team, che considera un task concluso solo quando i
test relativi alla funzionalità sono presenti e passano in CI. Ogni push e ogni pull request sui
branch `main` e `develop` attivano, tramite GitHub Actions, il controllo della formattazione e
l'esecuzione dell'intera suite con misura della copertura; sul branch `main` viene inoltre
generato il JAR.

La distinzione tra i due livelli di test segue questo criterio: un test è
unitario quando verifica una singola unità di comportamento, lo fa rapidamente e lo fa in
isolamento rispetto alle altre unità e agli altri test; quando una di queste tre condizioni
non è soddisfatta, il test ricade nella categoria dei test di integrazione. I test di
integrazione isolano di volta in volta le sole collaborazioni necessarie a verificare un
comportamento multi-componente, sostituendo con test double le dipendenze che non sono
oggetto della verifica.

Il dominio è modellato con strutture **immutabili** e funzioni pure: non esistendo stato globale
condiviso, ogni caso di test può costruire il proprio scenario e valutarne il risultato senza
fasi di *teardown* e senza dipendenze dall'ordine di esecuzione.

## Tecnologie utilizzate

Per la scrittura e l'esecuzione dei test è stato utilizzato **ScalaTest**, affiancato da
**Mockito**.

Di ScalaTest sono state sfruttate principalmente:

- lo stile **`AnyFlatSpec`**, che permette di esprimere ogni caso di test come una frase
  (`"A memory" should "start with no beliefs"`), rendendo il nome del test una descrizione
  leggibile della condizione verificata;
- i **matchers** (`shouldBe`, `should have size`, `should contain`) e il matcher di tolleranza
  `+-` per i confronti su valori in virgola mobile;
- il costrutto `an[...] should be thrownBy`, usato per verificare il rispetto delle
  precondizioni dei costruttori.

Di Mockito sono stati usati i costrutti di base:

- `mock(classOf[C])` per creare l'implementazione sostitutiva di una dipendenza;
- `when(...).thenReturn(...)` per configurare la risposta di uno **stub**;
- gli *argument matcher* `any`, `anyInt` e `anyDouble`, per configurare uno stub
  indipendentemente dal valore degli argomenti ricevuti;
- `verify(...)` per controllare che il SUT interagisca con il *depended-on component* nel modo
  atteso;
- `verifyNoMoreInteractions(...)` e `verifyNoInteractions(...)` per controllare che il SUT non
  effettui chiamate ulteriori rispetto a quelle verificate, o nessuna chiamata, verso una
  dipendenza.

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
  dipendere dalla politica di scarto della memoria reale. In `SimulationEngineTest` è invece un
  mock che restituisce sé stesso a ogni aggiornamento, e le verifiche riguardano quale evento
  il motore registra, con quale tick e su quale agente.
- **`Space`** è sostituito da un mock in `BoundaryPolicyTest`: le politiche di confine si
  limitano a delegare allo spazio, quindi il test verifica il valore restituito e, con
  `verify`, che la delega avvenga effettivamente.
- **`NeighborStrategy`** è sostituita da un mock in `EnvironmentTest`, per controllare che
  l'ambiente inoltri alla strategia l'agente, la popolazione e il raggio corretti, senza
  vincolare il test all'algoritmo di ricerca concreto. In `SimulationEngineTest` la strategia
  restituisce un vicinato fisso, e si verifica che venga preparata una sola volta per tick con
  la popolazione e il raggio configurato.
- **`Environment`** e **`SimulationConfig`** sono sostituiti da stub in `ModelTest` e
  `MvuTest`, così da poter costruire il modello della GUI senza allestire una simulazione
  completa.

## Test di integrazione

Le collaborazioni tra moduli sono verificate da due gruppi di test.

`SimulationEngineTest` verifica il ciclo di aggiornamento nel suo complesso, cioè
l'integrazione tra motore, comportamenti, regole di interazione, spazio, politica di
frontiera, memoria e punti di interesse, con i casi raggruppati secondo le fasi del tick. In
particolare sono verificati: l'inizializzazione, con la numerazione dei nuovi nati a partire
dall'identificatore più alto in uso; l'avanzamento del tick senza modifica dello stato
ricevuto; la preparazione della ricerca dei vicini una sola volta per tick; la selezione del
primo comportamento e della prima regola applicabili; la somma delle velocità richieste, o il
mantenimento di quella corrente in assenza di movimento, e la risoluzione dell'attraversamento
del confine da parte della politica di frontiera; l'assegnazione di identificatori freschi ai
nuovi nati, collocati nella posizione raggiunta dal genitore; la rimozione di un agente che
muore, con la conservazione degli agenti che ha generato nello stesso tick; la consegna di un
evento al solo agente destinatario; il conteggio delle permanenze consecutive in un POI e il
suo azzeramento all'uscita. Nei casi che coinvolgono la memoria e la ricerca dei vicini,
queste restano sostituite da mock e l'asserzione è espressa su come il motore le utilizza,
limitando così l'ampiezza dell'integrazione alle sole collaborazioni di interesse.

`MvuTest` e `ModelTest` verificano l'integrazione tra la GUI e il motore, controllando che le
funzioni `init` e `update` del ciclo Model-View-Update producano il modello atteso in risposta
ai messaggi ricevuti e che lo stato non avanzi quando la simulazione è in pausa.

## Test di accettazione

Le funzionalità che riguardano direttamente l'esperienza d'uso sono state verificate eseguendo
le quattro simulazioni di esempio e osservandone il comportamento a schermo, secondo i
requisiti di utente raccolti in analisi: l'avvio di una simulazione dal menu e il ritorno alla
scelta, la sospensione e la ripresa dell'esecuzione, il riavvio dalla configurazione iniziale,
la distinguibilità degli agenti per colore in base al proprio stato, la comparsa dei punti di
interesse e dei confini dello spazio, e la sospensione della sola raccolta delle statistiche
senza interruzione della simulazione. La verifica è stata ripetuta alla chiusura di ogni
sprint, in sede di Sprint Review, sulle simulazioni disponibili in quel momento.

Nella stessa sede è stato verificato il requisito di prestazioni: le simulazioni sono state
eseguite con popolazioni dell'ordine delle centinaia di agenti, controllando che
l'aggiornamento del tick e il conseguente ridisegno restassero allineati all'intervallo del
timer di aggiornamento e che l'esecuzione prolungata non producesse rallentamenti progressivi.

## Grado di copertura

La suite è composta da **29 classi di test** per un totale di **232 casi di test**; il report
di copertura generato in CI indica una copertura delle istruzioni dell'85%. I casi sono
distribuiti sulle seguenti aree:

- **Geometria e spazio**: operazioni su posizioni e vettori, appartenenza allo spazio,
  politiche di confine, ricerca dei vicini;
- **Modello dell'agente**: aggiornamenti non distruttivi di moto, stato e memoria, azioni
  producibili, gestione della memoria a capacità limitata;
- **DSL**: accumulo ordinato di comportamenti e regole nei builder, costruzione della
  specifica di ambiente, comportamenti composti e condizionali, regole discrete e continue;
- **Engine**: inizializzazione, avanzamento del tick, percezione, applicazione di comportamenti
  e regole, risoluzione del movimento e dei confini, nascita e morte degli agenti, recapito
  dei messaggi, residenza nei POI;
- **GUI**: modello e funzione di aggiornamento MVU, monade di stato, associazione tra stato di
  dominio e rappresentazione grafica.

Sono inoltre verificati i **casi limite e gli input non validi**: il rifiuto
di dimensioni o raggi non positivi per gli spazi, il rifiuto di una capacità di memoria non
positiva, la normalizzazione del vettore nullo, il comportamento di un agente privo di vicini e
la posizione esattamente sul confine dello spazio o sul bordo del raggio di percezione.

I componenti Swing di sola presentazione
(`SimulationWindow`, `MainMenu`, `SimulationPanel`, `StatisticsPanel`) contengono stato mutabile e
dipendono direttamente dal toolkit grafico: non sono coperti da test automatici e sono stati
verificati con i test di accettazione descritti sopra; le parti della GUI indipendenti dal disegno
(`Renderable`, `POIRenderable`, `Msg`, `SimulationOption`) sono invece coperte da test
automatici. Le simulazioni di esempio (`Epidemic`, `AlarmSpreading`, `AntColony`,
`OpinionDynamics`, `Main`) sono programmi d'uso del DSL, le cui costruzioni sono già coperte
dalle suite del livello DSL: quello che le riguarda in proprio, ovvero il fenomeno emergente
atteso, è per sua natura oggetto di osservazione e non di asserzione.

La suite è stata eseguita in CI durante tutto il ciclo di sviluppo, così che le regressioni
rilevabili dai test emergessero prima dell'integrazione nei branch `main` e `develop`.

[Indice](0-index.md) | [Capitolo Precedente](6-implementation.md) | [Capitolo Successivo](8-retroprospective.md)