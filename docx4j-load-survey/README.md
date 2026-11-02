# docx4j load survey

Loads every docx in a corpus with docx4j 17.3.5 (from Maven Central) and writes one row per document
for the catalogue's `docx4j_load` table (docx4j-portfolio `scripts/corpus`, runbook step 5), plus the
WARN and ERROR messages each document raised. Pure Java: one jar, nothing native, runs on arm64.

**Build** (any machine with Maven and Java 11+): `mvn -f docx4j-load-survey/pom.xml package` gives
`docx4j-load-survey/target/docx4j-load-survey.jar` (23 MB). `-Ddocx4j.version=…` builds it on another release.

**Run** on the gather host, with Java 17 or 21. On Java 25, XML deeper than 100 elements is refused,
and docx4j sets its own limit only from 17.3.6.

    java -jar docx4j-load-survey.jar run --corpus /data/corpus --out /data/docx4j-load --limit 20000   # pilot: note the rate
    java -jar docx4j-load-survey.jar run --corpus /data/corpus --out /data/docx4j-load                 # the rest
    sqlite3 /data/catalogue.db < /data/docx4j-load/import.sql

- `--workers 8 --heap 2g` are the defaults: eight worker JVMs of 2 GB, 16 GB in all. With the scan
  finished, `--workers 14` suits a 16-vCPU host with 32 GB. Each document has `--timeout 60` seconds.
  `--save` also saves each loaded document (to nowhere), which roughly doubles the time.
- **Resumable.** Interrupt it however you like, then run the same command again: documents that
  already have a row are skipped. A line left unfinished is cut off and that document is surveyed again.
- **The corpus is only read.** `--out` may not be inside it. The walk follows no links, the workers
  run in `--out/workers`, and they are handed absolute paths. Names that are SHA-256s are taken as
  the hash; any other name is hashed. `--list FILE` (one path per line) replaces `--corpus` for a sample.

**What a row measures.** First `OpcPackage.load(file, McLoadMode.WORD)`. Load leaves the JAXB parts
unread, so every one is then read, and the main part's fate is `main_ok`. The row also counts the
`mc:AlternateContent` branches read in the text stories (CR-051's rule), CR-065's lenient integers and
CR-066's out-of-range values for the whole document, the parts that needed mc-preprocessor.xslt, and
the WARN and ERROR log events. `outcome` is one of `ok`, `main_unreadable`, `load_failed`, `timeout`,
`out_of_memory` or `crashed`. After a timeout or out-of-memory error the worker is replaced. A
document that kills its worker is tried once more before it is recorded as `crashed`. The comments in
`import.sql` describe every column.

**Output in `--out`.**

| File | Holds |
|---|---|
| `docx4j_load.tsv` | One row per document. |
| `docx4j_load_messages.tsv` | Each document's distinct messages with their counts, at most 50. |
| `summary.txt` | The counts step 5 reports: loaded, failures by exception, `mc` choices, the slowest document, the commonest messages. `run` writes it, and so does `summary --out DIR`. |
| `import.sql` | Replaces both tables in the catalogue from the TSVs. |
| `workers/worker-N.log` | Each worker's standard error, capped at 100 MB. |

The TSVs need no quoting: a tab, line break or double quote inside a text value is written as a
space or an apostrophe, and an empty field is NULL.

`misplaced_children` is not measured: CR-064 refuses misplaced children when they are added, and
JAXB's unmarshal never makes one, so a load meets none.
