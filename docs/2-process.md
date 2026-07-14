# Processo di Sviluppo

## Metodologia

Il gruppo ha adottato una metodologia di sviluppo ispirata a **SCRUM**, come
consigliato dalle regole d'esame al punto P8. Data la composizione ridotta del
team (due persone), i ruoli sono stati assegnati in modo flessibile, con
entrambi i membri che contribuiscono attivamente allo sviluppo.

### Ruoli

- **Product Owner / Esperto di dominio** — Alessio Bifulco: responsabile della
  visione del prodotto, della qualità del risultato finale e della coerenza
  con i requisiti definiti.
- **Scrum Master / Sviluppatore** — Samuele Ferri: responsabile del
  coordinamento del processo di sviluppo, della gestione del backlog e della
  cadenza degli sprint.

Entrambi i membri ricoprono il ruolo di **Development Team** e contribuiscono
in modo equilibrato all'implementazione del sistema.

## Organizzazione degli Sprint

Il lavoro è organizzato in **sprint settimanali** della durata di circa una
settimana, con un carico di lavoro previsto di **15 ore per membro per sprint**.

Per ogni sprint sono previsti due momenti formali:

- **Sprint Planning** (inizio sprint): definizione dei task da completare,
  stima dell'effort e assegnazione delle responsabilità;
- **Sprint Review** (fine sprint): verifica di quanto prodotto, confronto con
  la definition of done e pianificazione dello sprint successivo.

### Definition of Done

Un task si considera completato quando:

- il codice compila senza warning;
- i test relativi alla funzionalità sono presenti e passano in CI;
- il codice è documentato con ScalaDoc sui trait e i metodi pubblici;
- la Pull Request è stata revisionata dall'altro membro e approvata.

## Strumenti

| Strumento       | Utilizzo                         |
|-----------------|----------------------------------|
| Git + GitHub    | Versionamento del codice         |
| SBT             | Build tool e gestione dipendenze |
| GitHub Actions  | Continuous Integration           |
| GitHub Projects | Gestione dei task e del backlog  |
| ScalaTest       | Testing automatizzato            |
| IntelliJ IDEA   | IDE di sviluppo                  |

## Versionamento

Per i commit si adotta la convenzione **Conventional Commits**:

- feat: nuova funzionalità
- fix: correzione di bug
- test: aggiunta property-based test
- docs: aggiornamento documenti relativi progeto
- chore: manutenzione varia, configurazioni, dipendenze
- ci: continuous Integration / pipeline

Per il versionamento del software si adotta **Semantic Versioning**
(`MAJOR.MINOR.PATCH`).

## Continuous Integration

Ad ogni push sul repository vengono eseguiti automaticamente tramite
GitHub Actions:

- compilazione del progetto con SBT;
- esecuzione di tutti i test.
- generazione JAR in release

## Backlog e Report degli Sprint

Il product backlog e i report di ogni sprint sono mantenuti in versione
nella cartella `process/` del repository, in modo da rendere verificabile
a posteriori la storia del progetto.

[Indice](0-index.md) | [Capitolo Precedente](1-intro.md) | [Capitolo Successivo](3-analysis.md)