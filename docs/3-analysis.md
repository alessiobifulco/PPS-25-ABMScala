---
title: Analisi
nav_order: 3
parent: Report
---

# Analisi

L'analisi del problema svolta nella prima fase del progetto ha permesso di
evidenziare i requisiti elencati di seguito. Poiché il prodotto realizzato non è
un'applicazione finale a sé ma ideato come pseudo framework, l'utente a cui i requisiti fanno
riferimento è il **simulation developer** facente parte di un team di sociologi, ovvero chi utilizza le
astrazioni offerte per modellare un fenomeno. Lo scenario d'uso che abbiamo assunto è
quello di uno sviluppatore inserito in un gruppo di lavoro composto anche da esperti del
dominio, che del modello conoscono il fenomeno ma non il linguaggio: da qui l'attenzione
posta sulla leggibilità della definizione di una simulazione e sulla possibilità di
osservarne l'andamento senza leggere il codice.

## Requisiti di business

- **Realizzare un framework generico, non una singola simulazione**: il valore
  del progetto risiede nella possibilità di descrivere fenomeni diversi
  combinando le stesse astrazioni sullo stesso **engine** di esecuzione. Ogni
  costrutto offerto deve essere indipendente dal dominio applicativo e
  riutilizzabile.
- **Rendere la definizione di una simulazione dichiarativa**: l'utente deve
  poter esprimere *cosa* caratterizza il modello: chi sono gli **agent**, qual è il loro
  **behavior**, come cambiano **state**, senza doversi occupare di *come* la
  simulazione viene eseguita. Lo stesso vale per l'**environment**: la forma dello
  spazio, il comportamento al confine, la composizione della popolazione iniziale e
  i **point of interest** devono essere dichiarati come caratteristiche del modello,
  non costruiti passo per passo dall'utente.
- **Utilizzo di Scala 3 e del paradigma funzionale**: il progetto deve essere
  sviluppato applicando costrutti funzionali di alto livello, come richiesto
  dagli obiettivi del corso.
- **Dimostrare la flessibilità con simulazioni di esempio**: la genericità del
  framework deve essere sostanziata da simulazioni appartenenti a domini
  differenti, non da una sola estesa a più casi.
- **Rispetto della scadenza**: il progetto deve essere consegnato entro il termine
  concordato, pianificando gli sprint in modo da completare per prime le
  funzionalità essenziali e collocare nelle iterazioni finali quelle opzionali,
  così che una scadenza anticipata non comprometta la consegna di un prodotto
  funzionante.

**Criteri di successo del progetto.** Il progetto è considerato riuscito se, alla consegna,
sono verificate tutte le condizioni seguenti: esistono almeno quattro simulazioni eseguibili
appartenenti a domini distinti; ciascuna è definita interamente attraverso il DSL e nessuna
costruisce a mano la popolazione o istanzia direttamente comportamenti e regole; l'**engine**
non contiene alcun riferimento a un dominio specifico; tutti i requisiti funzionali elencati
di seguito sono soddisfatti e verificati come descritto nel capitolo dedicato al testing.

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
  propria rimozione dalla simulazione. Un'**action** è un dato che descrive
  l'intenzione, mentre la sua applicazione compete all'**engine**;
- non modifica lo **state** dell'**agent**: il cambio di **state** è responsabilità
  esclusiva delle **interaction rule**.

### Interaction Rule

Governa i cambi di **state** di un **agent**:

- è associata allo **state** di partenza a cui si applica;
- è subordinata a una condizione sulla **perception** dell'**agent**, tipicamente la
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
- stabilisce come si comporta un **agent** che ne raggiunge il confine, scegliendo
  fra il rimbalzo, l'arresto sul bordo e la ricomparsa dal lato opposto;
- contiene la popolazione corrente di **agent**;
- fornisce, dato un **agent**, l'insieme dei suoi **neighbor** entro il raggio di
  **perception**;
- può contenere **point of interest**, regioni circolari dotate di significato
  per il modello. Un **point of interest** ha un nome, una posizione, un raggio e una
  soglia di permanenza; gli **agent** percepiscono di trovarsi al suo interno e la
  soglia, se maggiore di zero, stabilisce per quanti tick un **agent** deve restarvi
  prima che la permanenza produca effetto, così da distinguere l'attraversamento
  occasionale dalla sosta. Non ha **behavior** proprio: costituisce una condizione
  utilizzabile dalle **interaction rule**, non un **agent**.

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
    - esprimere la comunicazione di un **memory record** ai **neighbor** e la
      lettura dei **memory record** che essi già possiedono;
    - esprimere la generazione di un nuovo **agent** e la rimozione dell'**agent**
      dalla simulazione.
- **La definizione delle interaction rule**:
    - dichiarare una transizione fra due **state** e la condizione che la abilita;
    - esprimere condizioni sui **neighbor** e sui **point of interest**;
    - definire **interaction rule** su **state** a valore continuo, non enumerabili, così come su **state** discreti, enumerabili.
- **La definizione della rappresentazione grafica**:
    - associare a ciascuno **state** il colore con cui gli **agent** che lo assumono
      sono disegnati, così che l'andamento della simulazione sia leggibile anche da
      chi non ha scritto il modello;
    - ottenere questa associazione senza modificare il tipo di **state**, che resta
      una scelta dell'utente.
- **L'esecuzione e l'osservazione**:
    - scegliere quale simulazione avviare e tornare alla scelta al termine;
    - osservare l'evoluzione della popolazione, con gli **agent** distinguibili
      graficamente in base al proprio **state**;
    - sospendere e riprendere l'esecuzione della simulazione;
    - riavviare la simulazione dalla configurazione iniziale senza uscire dalla
      finestra di esecuzione;
    - seguire l'andamento quantitativo della simulazione mentre questa procede,
      disponendo statistiche in tempo reale;
    - sospendere e riprendere la raccolta delle statistiche indipendentemente
      dall'esecuzione della simulazione.

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
    - far avanzare la simulazione di un tick alla volta;
    - selezionare, per ogni **agent**, il **behavior** applicabile al suo **state**;
    - selezionare la **interaction rule** applicabile e, se esiste, aggiornare
      lo **state** dell'**agent**;
    - garantire che tutti gli **agent** ricevano la stessa **perception** dell'istante della
      simulazione, senza che l'aggiornamento di uno influenzi la **perception** di
      un altro nello stesso tick.
- **Applicazione delle action**:
    - comporre in un unico spostamento le richieste di movimento di un **agent** e
      applicarvi la politica di confine;
    - registrare i **memory record** acquisiti e recapitare quelli comunicati fra **agent**,
      indipendentemente dall'ordine in cui gli **agent** vengono elaborati;
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
- **Statistiche**:
    - calcolare a ogni tick la composizione della popolazione per **state**;
    - contare gli **agent** presenti in ciascun **point of interest**;
    - determinare la distribuzione spaziale degli **agent**;
    - mantenere il costo di questi calcoli compatibile con l'aggiornamento a ogni tick.

### Requisiti opzionali

Le funzionalità elencate di seguito erano state individuate in analisi come
desiderabili ma non essenziali, e pianificate di conseguenza nelle iterazioni
finali. Sono state tutte realizzate, e i requisiti funzionali che le riguardano
compaiono quindi insieme agli altri:

- la **memory** degli **agent**, con capacità limitata e datazione dei **memory record**,
  insieme alla possibilità di comunicarne il contenuto ad altri **agent**;
- i **point of interest**, con la soglia di permanenza e le condizioni che vi si
  appoggiano;
- le **statistiche** in tempo reale sulla composizione della popolazione, sulla
  presenza nei **point of interest** e sulla distribuzione spaziale.

## Requisiti non funzionali

### Requisiti esterni

- **Prestazioni**:
    - sostenere una popolazione dell'ordine delle centinaia di **agent**
      mantenendo un'animazione fluida, ovvero completando l'aggiornamento di un tick
      e il conseguente ridisegno entro l'intervallo di aggiornamento
      dell'interfaccia, senza accumulare ritardo al crescere della durata
      dell'esecuzione;
    - rendere disponibile una strategia di ricerca dei **neighbor** il cui costo non
      cresca quadraticamente con la numerosità della popolazione, dato che tale
      ricerca è l'operazione dominante del ciclo di simulazione.
- **Affidabilità**:
    - una configurazione incompleta o incoerente deve essere segnalata alla
      costruzione della simulazione, non manifestarsi durante l'esecuzione;
    - l'esecuzione prolungata di una simulazione non deve degradare né
      interrompersi.
- **Usabilità**:
    - la definizione di una simulazione deve risultare leggibile anche a chi
      non conosce l'implementazione del framework: ogni dichiarazione deve
      costituire una singola espressione e non deve richiedere all'utente di
      nominare le strutture interne che la raccolgono;
    - il vocabolario del DSL deve essere uniforme: costrutti che esprimono concetti
      analoghi devono avere la stessa forma sintattica.

### Requisiti interni

- **Genericità**:
    - lo **state** degli **agent** deve poter assumere qualunque forma decisa
      dall'utente, senza vincoli di ereditarietà né conversioni esplicite;
    - l'**engine** non deve contenere alcuna conoscenza dei domini simulati.
- **Estensibilità**:
    - aggiungere un nuovo **behavior** o una nuova condizione deve richiedere
      la sola definizione della funzione corrispondente;
    - ciascuna famiglia di costrutti offerta dal DSL deve comparire in almeno una
      simulazione di esempio, così che l'estensibilità offerta sia dimostrata e non
      soltanto dichiarata. Non è invece richiesto che ogni singolo costrutto sia
      esercitato: quando più costrutti condividono la stessa forma d'uso, e si
      distinguono solo per la direzione o per il criterio applicato, esercitarne uno
      dimostra anche gli altri.
- **Manutenibilità**:
    - il modulo che definisce il dominio non deve avere dipendenze verso il DSL,
      l'**engine** o l'interfaccia grafica, così che il modello resti utilizzabile e
      verificabile indipendentemente da come viene dichiarato, eseguito e mostrato;
    - le strutture dati del dominio devono essere immutabili; lo stato mutabile
      deve restare confinato alla costruzione della configurazione e ai componenti
      dell'interfaccia grafica, dove l'interazione con una libreria imperativa lo
      rende necessario;
    - le astrazioni pubbliche devono essere documentate tramite ScalaDoc.
- **Testabilità**:
    - comportamenti e regole di interazione devono poter essere verificati costruendo
      direttamente un contesto di prova, senza eseguire una simulazione completa;
    - l'avanzamento della simulazione deve essere verificabile a partire da uno stato
      noto e confrontabile con lo stato atteso, indipendentemente dalla presenza di
      una interfaccia grafica;
    - le dipendenze verso componenti esterne al modulo sotto test devono poter essere
      sostituite da implementazioni di prova, così da isolare la logica verificata;
    - le parti non deterministiche devono essere circoscritte, in modo che il resto
      del comportamento resti verificabile in modo ripetibile.

## Validazione dei requisiti

I requisiti di utente sono verificati eseguendo le simulazioni di esempio, che nel loro
insieme esercitano ogni famiglia di costrutti offerta dal DSL secondo il criterio fissato
dal requisito di estensibilità. I requisiti di sistema e i requisiti non funzionali interni
sono verificati dalla suite di test automatici, la cui organizzazione e copertura sono
descritte nel capitolo dedicato al testing. I requisiti di prestazione sono verificati per
osservazione diretta sulle simulazioni di esempio al crescere della numerosità della
popolazione.

## Requisiti di implementazione

- **Metodologia di sviluppo**: Agile SCRUM-inspired, con sprint settimanali, backlog di
  prodotto e di sprint, e una relazione breve al termine di ogni sprint mantenuta sotto
  controllo di versione.
- **Linguaggio**: Scala 3, con paradigma prevalentemente funzionale.
- **Build tool**: SBT.
- **Testing**: ScalaTest, con Mockito per la sostituzione delle dipendenze richiesta dai
  requisiti di testabilità.
- **Versioning e collaborazione**: Git e GitHub, con GitHub Actions per l'esecuzione
  automatica della suite di test a ogni integrazione.

[Indice](0-index.md) | [Capitolo Precedente](2-process.md) | [Capitolo Successivo](4-architecture.md)