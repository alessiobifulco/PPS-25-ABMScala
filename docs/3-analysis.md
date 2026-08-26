# Analisi

L'analisi del problema svolta nella prima fase del progetto ha permesso di
evidenziare i requisiti elencati di seguito. Poiché il prodotto realizzato non è
un'applicazione finale a sé ma ideato come pseudo framework, l'utente a cui i requisiti fanno
riferimento è il **simulation developer**, ovvero chi utilizza le
astrazioni offerte per modellare un fenomeno.

## Requisiti di business

- **Realizzare un framework generico, non una singola simulazione**: il valore
  del progetto risiede nella possibilità di descrivere fenomeni diversi
  combinando le stesse astrazioni sullo stesso **engine** di esecuzione. Ogni
  costrutto offerto deve essere indipendente dal dominio applicativo e
  riutilizzabile.
- **Rendere la definizione di una simulazione dichiarativa**: l'utente deve
  poter esprimere *cosa* caratterizza il modello — chi sono gli **agent**, qual è il loro
  **behavior**, come cambiano **state** — senza doversi occupare di *come* la
  simulazione viene eseguita. Questo requisito motiva la scelta di un Domain
  Specific Language interno come interfaccia principale del framework.
- **Utilizzo di Scala 3 e del paradigma funzionale**: il progetto deve essere
  sviluppato applicando costrutti funzionali di alto livello, come richiesto
  dagli obiettivi del corso.
- **Dimostrare la flessibilità con simulazioni di esempio**: la genericità del
  framework deve essere sostanziata da simulazioni appartenenti a domini
  differenti, non da una sola estesa a più casi.

## Modello di dominio

Il dominio del progetto ruota attorno alle quattro astrazioni fondamentali del
framework: **Agent**, **Behavior**, **Interaction Rule** ed **Environment**.

### Agent

Entità autonoma che popola la simulazione:

- possiede un identificativo univoco, stabile per tutta la sua vita;
- possiede una posizione e una velocità nello spazio continuo;
- possiede uno **state** di tipo generico, definito dall'utente del framework;
- può possedere una **memory** di ciò che ha percepito. La **memory** ha capacità
  limitata, definita alla configurazione della simulazione: i **memory record** più
  vecchi vengono dimenticati per fare spazio a quelli nuovi. Ogni **memory record** è
  datato con il tick in cui è stato acquisito, così che una condizione possa distinguere 
  le informazioni recenti da quelle obsolete, e può essere acquisito
  per **perception** diretta oppure ricevuto da un altro **agent**, il che rende
  possibile la propagazione di informazione oltre il raggio di **perception**;
- non modifica direttamente né sé stesso né gli altri: esprime intenzioni sotto
  forma di **action**, che l'**engine** applica.

### Behavior

Descrive che cosa fa un **agent** in un dato **state**:

- è associato a uno **state** specifico, oppure a nessuno se rappresenta il
  **behavior** predefinito;
- riceve la **perception** dell'**agent**, ovvero la porzione di mondo a lui
  osservabile: l'**agent** stesso, i **neighbor** entro il raggio di **perception**, il
  tempo corrente e la sua permanenza nei **point of interest**. È l'unico canale
  attraverso cui un **agent** conosce il mondo: nessun **behavior** accede allo
  **state** globale della simulazione;
- produce la lista delle **action** che l'**agent** intende compiere. Le **action**
  previste sono il movimento, la registrazione di un **memory record**, la comunicazione
  di un **memory record** a un altro **agent**, la generazione di un nuovo **agent** e la
  propria rimozione dalla simulazione. Un'**action** è un dato inerte: descrive
  l'intenzione, mentre la sua applicazione compete all'**engine**;
- non modifica lo **state** dell'**agent**: il cambio di **state** è responsabilità
  esclusiva delle **interaction rule**.

### Interaction Rule

Governa i cambi di **state** di un **agent**:

- è associata allo **state** di partenza a cui si applica;
- è subordinata a una condizione sulla **perception** dell'**agent** — tipicamente la
  presenza di **neighbor** in un certo **state**, la posizione rispetto a un **point of interest**,
  il contenuto della **memory** o un evento probabilistico;
- produce il nuovo **state** dell'**agent**.

La separazione fra **behavior** e **interaction rule** riflette una scelta di
modellazione precisa: un **agent** decide autonomamente come agire in base a chi è,
ma cambia ciò che è solo in conseguenza di un incontro o di una condizione
dell'**environment**.

### Environment

Spazio condiviso in cui gli **agent** vivono:

- definisce la regione di piano in cui gli **agent** possono trovarsi, in forma
  rettangolare o circolare;
- stabilisce come si comporta un **agent** che ne raggiunge il confine;
- contiene la popolazione corrente di **agent**;
- fornisce, dato un **agent**, l'insieme dei suoi **neighbor** entro il raggio di
  **perception**;
- può contenere **point of interest**, regioni circolari dotate di significato
  per il modello. Un **point of interest** ha una posizione, un raggio e un nome;
  gli **agent** percepiscono di trovarsi al suo interno e può richiedere, secondo una soglia configurabile dall'utente,
  una permanenza minima prima di produrre effetto, così da distinguere
  l'attraversamento occasionale dalla sosta. Non ha **behavior** proprio:
  costituisce una condizione utilizzabile dalle **interaction rule**, non un **agent**.

### La simulazione come composizione

Una simulazione è data dalla configurazione iniziale dell'**environment** e della
popolazione, dall'insieme dei **behavior** e dall'insieme delle **interaction rule**. La sua
esecuzione procede per **tick** discreti: a ogni tick ciascun **agent** riceve la propria **perception**
del proprio intorno, sceglie le proprie **action** ed eventualmente cambia **state**,
producendo lo **state** successivo della simulazione.

## Requisiti funzionali

### Requisiti di utente

Dal punto di vista di chi utilizza il framework, il sistema deve consentire:

- **La definizione dell'environment**:
    - scegliere la forma e la dimensione dello spazio;
    - stabilire il comportamento al confine;
    - definire la numerosità della popolazione iniziale e lo **state** dei suoi
      **agent**, potendo differenziare uno o più individui dal resto;
    - stabilire il raggio di **perception** degli **agent**;
    - dotare gli **agent** di una **memory** di capacità definita;
    - collocare **point of interest** nello spazio.
- **La definizione del behavior degli agent**:
    - associare a ciascuno **state** le **action** che l'**agent** compie;
    - definire un **behavior** predefinito per gli **state** non altrimenti
      trattati;
    - comporre più **action** elementari in un **behavior** unico;
    - esprimere movimento casuale, diretto verso un punto o in allontanamento da
      esso, e movimento coordinato rispetto ai **neighbor**;
    - esprimere **action** sulla **memory** e sulla popolazione.
- **La definizione delle interaction rule**:
    - dichiarare una transizione fra due **state** e la condizione che la abilita;
    - esprimere condizioni sui **neighbor**, sui **point of interest**, sulla **memory** e
      sul caso, e comporle fra loro;
    - definire **interaction rule** su **state** a valore continuo, non enumerabili.
- **L'esecuzione e l'osservazione**:
    - scegliere quale simulazione avviare;
    - osservare l'evoluzione della popolazione, con gli **agent** distinguibili
      graficamente in base al proprio **state**.

### Requisiti di sistema

Il sistema dovrà occuparsi di:

- **Gestione della popolazione**:
    - costruire la popolazione iniziale secondo la configurazione fornita,
      assegnando a ogni **agent** un identificativo univoco;
    - mantenere l'insieme degli **agent** vivi in ogni istante;
    - garantire l'univocità degli identificativi anche dopo la rimozione e la
      creazione di **agent**.
- **Gestione della perception**:
    - determinare, per ciascun **agent**, l'insieme dei **neighbor** entro il raggio di
      **perception**;
    - costruire il contesto locale su cui **behavior** e **interaction rule** operano.
- **Ciclo di simulazione**:
    - far avanzare la simulazione di un tick alla volta, in modo deterministico
      rispetto alla configurazione e alle sole sorgenti di casualità dichiarate;
    - selezionare, per ogni **agent**, il **behavior** applicabile al suo **state**;
    - selezionare la **interaction rule** applicabile e, se esiste, aggiornare
      lo **state** dell'**agent**;
    - garantire che tutti gli **agent** ricevano la stessa **perception** dell'istante della
      simulazione, senza che l'aggiornamento di uno influenzi la **perception** di
      un altro nello stesso tick.
- **Applicazione delle action**:
    - comporre in un unico spostamento le richieste di movimento di un **agent** e
      applicarvi la politica di confine;
    - registrare i **memory record** acquisiti e recapitare quelli comunicati fra **agent**;
    - inserire nella popolazione gli **agent** generati e rimuovere quelli
      eliminati.
- **Gestione della memory**:
    - rispettare la capacità massima configurata, dimenticando i **memory record** meno
      recenti;
    - datare ogni **memory record** con il tick di acquisizione.
- **Gestione dei point of interest**:
    - rilevare quali **agent** si trovano all'interno di ciascun **point of interest**;
    - tenere traccia della durata della permanenza e azzerarla all'uscita.
- **Visualizzazione**:
    - rappresentare a schermo la popolazione corrente, con il colore di ciascun
      **agent** determinato dal suo **state**;
    - disegnare i confini dello spazio e i **point of interest**;
    - aggiornare la rappresentazione al procedere dei tick.

## Requisiti non funzionali

### Requisiti esterni

- **Prestazioni**:
    - sostenere una popolazione dell'ordine delle centinaia di **agent**
      mantenendo un'animazione fluida;
    - contenere il costo della ricerca dei **neighbor**, che rappresenta l'operazione
      dominante del ciclo di simulazione.
- **Affidabilità**:
    - una configurazione incompleta o incoerente deve essere segnalata alla
      costruzione della simulazione, non manifestarsi durante l'esecuzione;
    - l'esecuzione prolungata di una simulazione non deve degradare né
      interrompersi.
- **Usabilità**:
    - la definizione di una simulazione deve risultare leggibile anche a chi
      non conosce l'implementazione del framework;
    - il vocabolario del DSL deve essere uniforme: costrutti analoghi devono
      esprimersi in forma analoga.

### Requisiti interni

- **Genericità**:
    - lo **state** degli **agent** deve poter assumere qualunque forma decisa
      dall'utente, senza vincoli di ereditarietà né conversioni esplicite;
    - l'**engine** non deve contenere alcuna conoscenza dei domini simulati.
- **Estensibilità**:
    - aggiungere un nuovo **behavior** o una nuova condizione deve richiedere
      la sola definizione della funzione corrispondente;
    - ogni costrutto esposto dal DSL deve essere esercitato da almeno una
      simulazione, così che l'estensibilità offerta sia dimostrata e non
      soltanto dichiarata.
- **Manutenibilità**:
    - separazione netta fra la definizione del modello, la sua esecuzione e la
      sua rappresentazione grafica;
    - utilizzo di strutture dati immutabili per eliminare gli effetti
      collaterali;
    - codice documentato tramite ScalaDoc sulle astrazioni pubbliche.
- **Testabilità**:
    - **behavior** e **interaction rule**, essendo funzioni pure del contesto locale, devono
      essere verificabili singolarmente senza avviare una simulazione;
    - l'avanzamento della simulazione deve essere verificabile in isolamento
      dalla rappresentazione grafica;
    - buona copertura dei test sulle logiche critiche dell'**engine**.

## Requisiti di implementazione

- **Metodologia di sviluppo**: Agile SCRUM-inspired
- **Linguaggio**: Scala 3, con paradigma prevalentemente funzionale
- **Interfaccia**: DSL interno per la definizione delle simulazioni, interfaccia
  grafica per la loro osservazione
- **Build tool**: SBT
- **Testing**: ScalaTest
- **Versioning e collaborazione**: Git, GitHub, GitHub Actions per la CI

[Indice](0-index.md) | [Capitolo Precedente](2-process.md) | [Capitolo Successivo](4-architecture.md)