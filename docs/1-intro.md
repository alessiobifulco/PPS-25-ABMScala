# Introduzione

Il progetto **ABMScala** si pone l'obiettivo di realizzare un framework generico
per la modellazione e l'esecuzione di simulazioni di tipo **Agent-Based** (ABMS),
sviluppato in Scala 3 secondo il paradigma della programmazione funzionale.

## Cos'è una simulazione Agent-Based

Una simulazione Agent-Based è un modello computazionale in cui entità autonome,
dette **agenti**, interagiscono tra loro e con l'ambiente circostante seguendo
regole locali definite dal modellatore. A differenza dei modelli matematici
tradizionali, che descrivono il comportamento di un sistema attraverso equazioni
globali, le simulazioni agent-based costruiscono il comportamento del sistema
dal basso verso l'alto: ogni agente segue regole semplici e locali, e i
comportamenti complessi emergono spontaneamente dalle interazioni tra gli agenti
stessi. Questo fenomeno è noto come **comportmento emergente**.

Un agente è un'entità autonoma caratterizzata da:

- uno **stato** interno, che può variare nel tempo;
- un **comportamento**, che determina come l'agente si muove e agisce;
- la capacità di **interagire** con altri agenti nelle proprie vicinanze.

L'ambiente è lo spazio condiviso in cui gli agenti esistono e si muovono.
Nel framework realizzato, lo spazio è continuo e bidimensionale, con agenti
che si spostano mediante vettori di velocità.

## Obiettivi del progetto

Il framework ABMScala si propone di offrire un insieme di astrazioni generiche
e componibili che permettano all'utente di definire simulazioni agent-based
in modo dichiarativo, attraverso un Domain Specific Language (DSL) interno
realizzato in Scala 3.

Le astrazioni fondamentali offerte dal framework sono:

- **Agent**: entità autonoma con stato generico, posizione e velocità;
- **Behavior**: funzione pura che determina il movimento dell'agente;
- **Interaction Rule**: regola che governa i cambi di stato tra agenti vicini;
- **Environment**: spazio bidimensionale continuo che contiene gli agenti.

La genericità del framework è garantita dal tipo parametrico `S`, che rappresenta
lo stato dell'agente e può essere definito liberamente dall'utente per adattarsi
a domini applicativi diversi.

Per dimostrare la flessibilità e la componibilità del framework, verranno
realizzate simulazioni dimostrative in domini differenti, costruite combinando
le stesse componenti sullo stesso motore di esecuzione.

## Struttura del documento

Nel corso di questo documento verrà illustrato il processo di sviluppo adottato,
l'analisi dei requisiti, il design architetturale e di dettaglio, le scelte
implementative rilevanti, la strategia di testing adottata e una retrospettiva
finale sull'andamento del progetto.

[Indice](0-index.md) | [Capitolo Successivo](2-process.md)