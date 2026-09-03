---
title: Retrospettiva
nav_order: 8
parent: Report
---

# Retrospettiva

## Analisi del processo di sviluppo

Il progetto si è svolto dal 14 luglio al 28 agosto 2026, articolato in quattro sprint, con una
pausa nella settimana di ferragosto dovuta alla chiusura del campus. Il carico previsto era di
20 ore settimanali per membro per sopperire all'assenza di un terzo membro e realizzare il progetto pensato
per il corso nella sua interezza.

La metodologia SCRUM-inspired ha funzionato bene nonostante la dimensione ridotta del team. La
suddivisione dei task non ha mai creato problemi di coordinamento: fin dallo Sprint 1 le
responsabilità sono state separate in modo netto:
* AB sulle entità del dominio, sulle astrazioni comportamentali e di interazione, sul motore e sul DSL;
* SF sullo spazio di simulazione, sull'ambiente di simulazione e i suoi componenti, sulle strategie di calcolo dei vicini, sull'architettura della view e sull'interfaccia grafica.

La definizione anticipata dei trait ha permesso di lavorare in parallelo senza conflitti di merge
e senza attese reciproche.

Una sola deadline non è stata rispettata, quella dello Sprint 1: delle 42 ore pianificate ne
sono rimaste 5 non completate, con `InteractionRule` non implementato e alcuni errori presenti nella parte di
Environment.
Il debito è stato interamente recuperato come primo task dello Sprint 2, che ha chiuso a zero
ore residue, così come i due sprint successivi. Il meccanismo delle Sprint Retrospective si è
quindi dimostrato efficace: gli action item lasciati alla chiusura di uno sprint sono stati
sistematicamente ripresi in quello seguente.

Due criticità di processo si sono ripetute più volte. La prima riguarda la **stima**: la
taratura dei parametri delle simulazioni è stata sottostimata sia nello Sprint 3 sia nello
Sprint 4, perché ottenere un fenomeno emergente leggibile a schermo richiede iterazioni che non
sono scrittura di codice e che in pianificazione tendono a non essere contate. La seconda
riguarda la **validazione**: in più occasioni difetti puramente comportamentali sono arrivati
fino all'esecuzione ed è stata l'osservazione a schermo, non la test suite, a rivelarli. 
La pratica di riservare un margine in fase di planning si è invece rivelata la scelta
più utile del processo: ha assorbito per intero il refactoring del dominio dello Sprint 3, non
previsto a specifica.

## Difficoltà incontrate

Per **AB**, la parte più difficile è stata quella iniziale. Trattandosi di un'idea progettuale
nuova, e non della riproduzione di un genere noto come può essere un videogioco, non esistevano
linee guida da seguire: il design del dominio è dovuto nascere immaginando le astrazioni da
zero, con il solo riferimento della prassi consolidata dei modelli agent-based. Questo spiega
perché lo Sprint 1 abbia prodotto astrazioni poi rimosse (`ActionGraph`, `Decision`, `Choice`,
`ActionHandler`): erano state introdotte prima di avere un utilizzatore concreto che le
richiedesse. La seconda difficoltà è stata il DSL, la cui sintassi è stata riscritta più volte
prima di risultare leggibile e la cui interazione con la costruzione dei builder ha imposto una
revisione dell'architettura a metà Sprint 3.

Vi è poi stata una curva di apprendimento su Scala 3: molte scelte sono state riviste una volta
compresi meglio i vantaggi offerti dal linguaggio. Le *context function* hanno permesso di
ottenere una configurazione dichiarativa senza passare esplicitamente alcun builder, e lo
stesso meccanismo è stato riusato invariato per i blocchi annidati dei comportamenti e delle
regole; le *type class* e i `given` hanno permesso di mantenere il framework generico sullo
stato dell'agente senza vincoli di ereditarietà. Queste possibilità non erano chiare all'inizio,
e averle comprese in corso d'opera ha portato a deviare dal design iniziale più di quanto
sarebbe accaduto partendo con la stessa consapevolezza.

Per **SF**, la difficoltà principale è stata collegare un modello immutabile a una libreria grafica imperativa come Swing.
Il pattern MVU ha fornito una struttura per separare modello, aggiornamento e visualizzazione, ma la sua applicazione ha
richiesto di gestire con attenzione il confine tra lo stato immutabile della simulazione e lo stato locale necessario ai
componenti grafici. L’introduzione della `State` monad ha permesso di rappresentare le trasformazioni del modello come
computazioni componibili, mantenendo la logica di aggiornamento separata dalla gestione della finestra e dei pannelli.

Una seconda difficoltà ha riguardato la progettazione delle astrazioni spaziali e della ricerca dei vicini. Il sistema
doveva supportare geometrie diverse, politiche di confine differenti e algoritmi alternativi per il calcolo dei vicini
senza vincolare l’Engine a una particolare implementazione. È stato quindi necessario coordinare `Space`, `BoundaryPolicy`,
`Toroidal`, `Environment` e `NeighborStrategy`, mantenendo separate la geometria dell’ambiente, la gestione del movimento e
la ricerca degli agenti vicini.

Infine, l’integrazione dei `Point of Interest` ha richiesto di coordinare il modello di dominio, l’Engine, il DSL e la GUI.
I POI dovevano rimanere elementi passivi dell’ambiente, ma al tempo stesso essere utilizzabili dalle condizioni del DSL,
tenere conto della permanenza degli agenti e risultare visibili nell’interfaccia grafica. La definizione di `POI` e `Residency`
ha permesso di separare la regione spaziale dai contatori di permanenza associati agli agenti, rendendo possibile distinguere il
semplice attraversamento dalla permanenza prolungata.

## Stato attuale

Tutti i requisiti principali della specifica sono stati soddisfatti: le quattro astrazioni
fondamentali (`Agent`, `Behavior`, `Environment`, `InteractionRule`) sono realizzate e
generiche sullo stato, la configurazione delle simulazioni avviene interamente attraverso il
DSL dichiarativo, e l'ambiente è visualizzabile a schermo. Le quattro simulazioni dimostrative:
*Epidemic*, *Opinion Dynamics*, *Ant Colony*, *Alarm Spreading* appartengono a domini
sufficientemente distanti da rendere credibile la genericità dichiarata: contagio, dinamica
delle opinioni, foraggiamento collettivo e diffusione di una notizia sono descritti con lo
stesso vocabolario e girano sullo stesso engine.

Tre delle cinque funzionalità opzionali previste sono state realizzate: la **memoria associata
agli agenti**, con capacità limitata e scambio di informazioni fra agenti, i **punti di
interesse** percepibili e utilizzabili come condizione di regola, e le **statistiche in tempo
reale** sull'andamento della simulazione. Restano non implementate l'**esportazione dei dati**
prodotti dalla simulazione e l'astrazione di **Path/WayPoint** per instradare gli agenti.

## Testing

L'approccio ai test è cambiato nel corso del progetto. Si è partiti con un TDD in senso
stretto, si è passati per una fase intermedia in cui i test venivano scritti in parallelo
all'implementazione, e si è tornati al TDD nella fase finale. La fase intermedia è quella che
ha prodotto i risultati peggiori, ed è la stessa in cui i difetti del DSL sono arrivati fino
all'esecuzione: quando il test segue il codice invece di precederlo, tende a legarsi ai
dettagli implementativi e a non coprire i casi che il codice non aveva già previsto..

Mockito è stato usato per sostituire le dipendenze e verificare in isolamento le unità di base,
mentre `scoverage`, configurato fin dallo Sprint 1, è servito come strumento diagnostico per
individuare le porzioni di codice rimaste scoperte e indirizzare la scrittura dei test
successivi, più che come metrica da esibire.

## Migliorie e lavori futuri

Diverse estensioni sono già abilitate dalla struttura modulare del framework e richiederebbero
la sola definizione dei costrutti corrispondenti, senza modifiche all'engine:

- **Esportazione dei dati** prodotti dalla simulazione, naturale prosecuzione della raccolta di
  statistiche per tick già presente;
- **Astrazione di Path/WayPoint** per instradare gli agenti lungo percorsi dichiarati, ultima
  funzionalità opzionale rimasta;
- **City Simulation**: un'ultima simulazione dimostrativa che con l'aggiunta dei path avrebbe
  permesso di simulare il traffico cittadino;

La direzione di sviluppo più interessante, e quella che il progetto aveva come orizzonte fin
dall'inizio, è però l'introduzione di **agenti guidati da modelli di intelligenza artificiale**:
sostituire, per una parte della popolazione, il comportamento descritto da funzioni pure con una
decisione delegata a un modello esterno, mantenendo invariato il resto del framework. La
separazione già presente fra la produzione delle intenzioni e la loro applicazione da parte
dell'engine rende questa estensione compatibile con l'architettura attuale, che tratta il
comportamento come una funzione dal contesto locale alla lista delle azioni desiderate.

## Conclusioni

Il progetto ABMScala ha rappresentato un'occasione concreta per applicare tecniche e processi
di sviluppo studiati durante il corso a un problema privo di un modello di riferimento
immediato. Costruire un framework anziché un'applicazione ha spostato l'attenzione dalla
quantità di funzionalità alla qualità delle astrazioni: il valore del risultato non sta nelle
quattro simulazioni realizzate, ma nel fatto che siano descritte con lo stesso vocabolario e
girino sullo stesso motore.

L'adozione di Scala 3 e del paradigma funzionale ha inciso profondamente sul design. Le
strutture immutabili hanno eliminato per costruzione un'intera classe di difetti e hanno reso
ogni componente verificabile in isolamento, mentre le context function e le type class hanno
permesso di ottenere un DSL leggibile senza rinunciare alla genericità.

Il bilancio complessivo è positivo. Il framework consegnato copre tutti i requisiti principali
e buona parte di quelli opzionali, e il dominio è più coerente alla fine del progetto di quanto
lo fosse a metà, grazie al refactoring condotto nell'ultimo sprint. Resta la consapevolezza di
aver impiegato più tempo del necessario in alcune fasi: la ricerca del design iniziale, le
riscritture della sintassi del DSL e la taratura dei parametri hanno assorbito ore che una
pianificazione più consapevole avrebbe potuto contenere. Ciò è principalmente dovuto al fatto che 
il sistema fosse un astrazione partendo da zero senza una possiible base da seguire, e l'esperienza 
acquisita da tale sfida è essa stessa un risultato del progetto.

[Indice](0-index.md) | [Capitolo Precedente](7-testing.md)