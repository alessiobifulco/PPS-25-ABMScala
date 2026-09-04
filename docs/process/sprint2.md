---
title: Sprint 2
layout: default
nav_order: 1
parent: Processo di Sviluppo
---

# Sprint 2 - Engine di Simulazione e Prime Simulazioni

## Obiettivo

L'obiettivo di questo Sprint è rendere il framework eseguibile end-to-end: completare le
astrazioni di dominio rimaste (`InteractionRule`) e le correzioni ereditate dallo sprint
precedente, realizzare il `SimulationEngine`, e costruire l'interfaccia grafica sopra il
pattern Model-View-Update. Al termine dello sprint entrambe le simulazioni dimostrative
pianificate — *Epidemic* e *Opinion Dynamics* — dovranno essere avviabili e osservabili a
schermo, configurate direttamente tramite `SimulationConfig`.

Lo sprint recupera inoltre le 5 ore rimaste incompiute nello Sprint 1.

## Deadline

La scadenza dello sprint è il 01/08/2026.

## Backlog

| Product Backlog Item                | Sprint Task                                                                                                            | Volontario | Stima iniziale (h) | Ore rimanenti a fine Sprint 2 |
|-------------------------------------|------------------------------------------------------------------------------------------------------------------------|------------|--------------------|-------------------------------|
| **Debito Sprint 1**                 | Correzioni a `Space`: `randomPosition`, `shape`                                                                        | SF         | 1                  | 0                             |
|                                     | Correzione a `Environment`: esposizione di `neighborhoods`                                                             | SF         | 1                  | 0                             |
|                                     | Semplificazione di `NeighborStrategy` e fix di `buildIndex`                                                            | SF         | 1                  | 0                             |
| **Interaction Rule**                | `InteractionRule` (trait, composizione first-match) e test suite                                                       | AB         | 2                  | 0                             |
|                                     | `visibleWithin` su `AgentContext` e test                                                                               | AB         | 1                  | 0                             |
| **Engine di simulazione**           | `SimulationState` e `SimulationConfig`                                                                                 | AB         | 1                  | 0                             |
|                                     | `SimulationEngine`: `init` e pipeline di `tick`                                                                        | AB         | 4                  | 0                             |
|                                     | Test suite dell'engine                                                                                                 | AB         | 3                  | 0                             |
| **Vocabolario del dominio**         | Costruttori di movimento: `moveRandomly`, `moveHorizontally`                                                           | AB         | 1                  | 0                             |
|                                     | Combinatori di regole discrete: `atLeastNear`, `withState`, `whenAgentIs`, `chance`                                    | AB         | 3                  | 0                             |
| **Model-View-Update**               | `Model` (con factory `from`, integrazione con `SimulationEngine`)                                                      | SF         | 1                  | 0                             |
|                                     | `Msg` enum (Tick, ToggleRun, Restart)                                                                                  | SF         | 0.5                | 0                             |
|                                     | `Mvu`: `init` e `update`                                                                                               | SF         | 1                  | 0                             |
|                                     | Test suite di `Model` e `Mvu`                                                                                          | SF         | 1                  | 0                             |
| **Interfaccia grafica**             | `Renderable`: type class per la mappatura stato → colore                                                               | SF         | 1                  | 0                             |
|                                     | `SimulationPanel`: rendering degli `Agent` su canvas Swing                                                             | SF         | 2                  | 0                             |
|                                     | `SimulationWindow`: timer di frame, bottoni Stop/Resume/Restart/Back                                                   | SF         | 3                  | 0                             |
|                                     | `MainMenu`: schermata di scelta con titolo, bottoni, resize e navigazione                                              | SF         | 2                  | 0                             |
|                                     | `SimulationOption`: trait per la navigazione menu → simulazione                                                        | SF         | 0.5                | 0                             |
|                                     | Test suite di `Renderable`, `Msg`, `Model`, `Mvu`, `SimulationOption`                                                  | SF         | 1                  | 0                             |
| **Simulazione 1: Epidemic**         | Stato `Health`, `Behavior` e `InteractionRule` di contagio                                                             | AB         | 2                  | 0                             |
|                                     | Istanza di `Renderable` e integrazione nel menu                                                                        | SF         | 1                  | 0                             |
| **Simulazione 2: Opinion Dynamics** | Stato `Opinion`, `Behavior` di steering (coesione/separazione/allineamento/inerzia) e `InteractionRule` di convergenza | AB         | 4                  | 0                             |
|                                     | Istanza di `Renderable` e integrazione nel menu                                                                        | SF         | 1                  | 0                             |
| **Documentazione del codice**       | ScalaDoc su `InteractionRule`, `SimulationEngine`, `SimulationConfig`                                                  | AB         | 2                  | 0                             |
|                                     | ScalaDoc su MVU e componenti della view                                                                                | SF         | 1                  | 0                             |
| **Totale**                          |                                                                                                                        |            | **42**             | **0**                         |

## Divisione del lavoro

- **AB**: completamento delle astrazioni di dominio (`InteractionRule`), `SimulationEngine`,
  vocabolario dei `Behavior` e delle `InteractionRule`, simulazioni dimostrative *Epidemic* e
  *Opinion Dynamics* (configurate direttamente tramite `SimulationConfig`, senza builder
  fluente: il DSL dichiarativo è previsto allo Sprint 3).
- **SF**: correzioni all'`Environment` ereditate dallo Sprint 1; realizzazione completa del
  pattern Model-View-Update (`Model`, `Msg`, `Mvu`) e della relativa test suite; interfaccia
  grafica Swing (`Renderable`, `SimulationPanel`, `SimulationWindow`, `MainMenu`,
  `SimulationOption`) con navigazione menu → simulazione → menu; integrazione di `Renderable`
  per entrambe le simulazioni.

## Definition of Done

- Il `SimulationEngine` compila e ha test verdi.
- La simulazione *Epidemic* è avviabile dal menu e osservabile a schermo.
- La simulazione *Opinion Dynamics* è avviabile dal menu e osservabile a schermo.
- ScalaDoc presente su tutti i membri pubblici introdotti.
- PR di ogni branch feature verso `develop` con test verdi in CI.

## Sprint Review

Lo stakeholder esprime soddisfazione per i progressi realizzati durante il secondo sprint.
Tutti gli obiettivi chiave sono stati raggiunti: il framework è per la prima volta eseguibile
dall'inizio alla fine, e una simulazione può essere avviata dal menu, osservata a schermo,
messa in pausa, ripresa e riavviata.

Entrambe le simulazioni dimostrative sono state completate, e non solo *Epidemic* come da
obiettivo minimo iniziale: in *Epidemic* il contagio si propaga per contatto fra agenti vicini,
in *Opinion Dynamics* gli agenti si aggregano per affinità di opinione e le opinioni convergono
progressivamente. Lo stakeholder apprezza in particolare che due fenomeni tanto diversi siano
descritti con le stesse astrazioni e girino sullo stesso motore, che è l'obiettivo di fondo del
progetto.

Il debito ereditato dallo Sprint 1 è stato interamente recuperato: `InteractionRule` è ora
disponibile e completa le quattro astrazioni fondamentali previste dai requisiti, e le
correzioni all'ambiente di simulazione sono state applicate. Lo sprint chiude senza ore
residue.

Lo stakeholder osserva infine che la configurazione di una simulazione è ancora poco leggibile,
dovendo passare per la costruzione diretta dell'oggetto di configurazione, e chiede che il
linguaggio dichiarativo previsto dai requisiti venga affrontato nello sprint successivo.

## Sprint Retrospective

Lo sprint ha avuto una durata di una settimana. Lo Sprint 1 aveva lasciato 5 ore di debito per
sottostima del lavoro: `InteractionRule`, in carico ad AB, e alcune correzioni al codice
dell'ambiente, in carico a SF. In questo sprint le stime iniziali sono state rispettate su
tutti i task assegnati: zero ore rimanenti a fine sprint, nessuna nuova ora di debito
accumulata. La suddivisione delle responsabilità è rimasta netta: AB sul motore di simulazione
e sulla logica delle due simulazioni, SF sull'architettura della view e sull'interfaccia
grafica.

### Cosa è andato bene

- Il debito dello Sprint 1 è stato completato come primo task, secondo l'action item lasciato
  in chiusura del primo sprint
- La pipeline del motore, progettata prima di scrivere codice, non ha richiesto revisioni
  durante l'implementazione
- Il perimetro dello sprint è stato ristretto consapevolmente per concentrarsi sul motore e
  sulle simulazioni dimostrative: questo ha lasciato margine sufficiente per completare anche
  *Opinion Dynamics*

### Cosa può essere migliorato

- Due difetti comportamentali, non di compilazione, sono stati individuati solo osservando le
  simulazioni a schermo: il moto collettivo degli agenti in *Opinion Dynamics*, che non
  produceva l'aggregazione attesa, e la mancata ripresa del comportamento ordinario dopo un
  cambio di stato in *Epidemic*. Verificarli a livello di test sarebbe stato preferibile
- La stima di una simulazione è dovuta essere rivista in corso d'opera, perché il comportamento
  previsto si è rivelato più delicato da modellare correttamente di quanto valutato in fase di
  pianificazione

### Action items per il prossimo sprint

- Quando un comportamento simulato ha una componente visiva o dinamica non banale, prevedere
  fin dalla stima un margine per l'osservazione e l'aggiustamento a schermo, non solo per la
  scrittura del codice
- Affrontare nello sprint successivo il linguaggio dichiarativo di configurazione, come
  richiesto dallo stakeholder in sede di review
- Introdurre nuove astrazioni solo a fronte di un utilizzatore concreto che le richieda,
  riprendendo l'action item già lasciato dallo Sprint 1