# Keeping Word's PDF target still

A golden set is Word's PDFs of a corpus, cut on the Windows VM by `WordGoldenRunner`, with Word's
re-save of each document beside them. Every score the harness produces is docx4j measured against
that set. Word's output depends on much more than the docx: the machine, the printer, the fonts,
Word's own build, the script that drives it. If any of them changes between two cuts, the target
moves, and **a moved target cannot be told apart from a change in docx4j**. A scoreboard is only
comparable with another scored against goldens cut in the same environment.

So everything below is held constant. Each item says what it is now, what it has been measured to
do, and where the golden manifest (`golden-manifest.properties`, written beside the PDFs) records
it, so that a set says what cut it and a change shows up in a diff of two manifests.

## Summary

| What | Held at (2026-10-06) | Recorded as |
|---|---|---|
| Default printer | FX DocuPrint C2255 PCL 6, 300 dpi | `printer=` |
| Its driver | FX DocuPrint C2255 PCL 6 2.6.6.1, a v3 driver | `printerDriver=` |
| Its default page | A4, hard margins 11.5 / 11.5 / 12.0 / 11.5pt | `printerPage=` |
| Word | 16.0.20430.20118, Current Channel, x64 | `word=` |
| Fonts, and their files | whatever the VM has; never installed or removed during a set's life | `<id>.fonts=`, `fonts=`, `fontFile.<face>=` |
| Connected experiences | on (stated by the operator on the probe runs) | `connectedExperiences=` |
| Proofing languages | none installed, so no hyphenation | `proofingLanguages=` |
| Field update | off | `fieldUpdate=` |
| Review markup | hidden by the script | `markup=` |
| Conversion call | `SaveAs <pdf>, 17` (wdFormatPDF), Word's own export options | `pdfExportCall=`, `pdfExport=` |
| The runner and its docx4j | a build of current source | not yet recorded (see Gaps) |

## The default printer

**Word lays text out against the default printer's device**, even exporting to PDF with `SaveAs`,
and even in documents without `w:usePrinterMetrics` (no document in the four corpora has it).

- **Its resolution sets every font size Word writes.** Word sets each size in whole device pixels,
  so the size operand in the PDF is the nominal size rounded to 1/dpi inch: 10pt is 10.08 at 300 dpi
  (42 px) and 9.96 at 600 dpi (83 px), 14pt 13.92 and 14.04, 8pt 7.92 and 8.04. Layout moves a
  little with it: on corpus document 11875, cut at both, the same 10pt line is 0.33% wider at
  300 dpi, which is the order of the knife-edge line breaks the harness measures.
- **The printer can move the page itself.** An HP OfficeJet Pro 9010 at 600 dpi put 11875's header
  and body 31.2pt lower than a Fujifilm Apeos at 600 dpi did, and the document took 152 pages to
  144. Not because of its printable area: the HP's hard margins are 8.4pt, inside the document's
  18pt top margin. Why is not known. Its driver is a v4 driver, the C2255's a v3.
- **The C2255 is the standard** (decided 2026-10-06). The three corpora were cut on it on
  2026-09-09. Re-cutting 11875 on it reproduces that golden exactly: all 6002 word boxes in the same
  place, the same page breaks, the same size operands.
- **What drifted.** On 2026-09-29 the default printer became the Apeos without anyone deciding it,
  and everything cut after that is on a different basis: `X:\fidelity\scriptcheck\recut-on-c2255.tsv`
  lists the 212 goldens to re-cut on the C2255 (103 probes, all of real-c2, and 1372 and 11875).
- **Planned:** after the next release, every set is re-cut against Microsoft Print to PDF, whose
  characteristics are better understood and which other contributors can reproduce. Then re-baseline.
  Microsoft Print to PDF also has a v4 driver, so compare 11875's header position with the C2255 cut
  when that happens.

To hold it:
- Turn off Windows' **"Let Windows manage my default printer"** (Settings, Printers & scanners).
  With it on, Windows makes the last printer used the default.
- Do not change the printer's **printing preferences**, in particular its print quality, which is
  the resolution Word sees.
- Before a cut, check the default printer with `X:\fidelity\scriptcheck\machine-reading.ps1`.
  Its `PRINTER|` line names the printer and its resolution. It is the same reading the runner makes.

## Word's build

Word is Click-to-Run and updates itself, and its layout engine changes between builds. No build
change has yet been caught moving a golden here, but nothing pins the build either. One open
question may turn out to be one: whether a break-only paragraph ending a document gives Word an
empty last page (ledger9 §8).

To hold it:
- Note the build before a series of cuts (`word=`, or File > Account > About Word).
- Pause Office updates while a series of sets is being cut, so that all sets in a series share one
  build. If the build moves mid-series, re-cut the sets cut before it.

## Fonts

**Word lays a document out in the fonts the machine has, and the machine's fonts change by
themselves.**
- **Word fetches fonts on its own.** Office's cloud fonts and Windows' on-demand supplemental fonts
  arrive when a document that asks for them is opened on a machine that is online. So the first cut
  of a corpus can change the fonts for the second. Measured: two cuts four days apart embedded
  Calibri and Ebrima in one and Lato and Nyala in the other, for documents that had asked for Lato
  and Nyala all along, and each reflowed completely; 18 documents changed a face between those
  cuts.
- **A face's name does not say which file drew it.** A font update can move advance widths,
  kerning and vertical metrics under the same name.
- **Connected experiences decide whether a cloud font counts as installed.** With them off, Word
  set Aptos in Calibri; with them on, it set the body in Aptos and every line broke elsewhere, on
  the same 27-page document (RULE-CLASSES.md §1).

To hold it:
- Install and remove no fonts on the VM while a set is in use. If one must be, re-cut the sets.
- Keep connected experiences as they are (on, as the probe runs state).
- After a cut, read the manifest:
  - `<id>.fonts=`: the faces each PDF embeds.
  - `fonts=`: the faces over the whole set.
  - `fontFile.<face>=`: the SHA-256 of each file behind a face. Several lines for one face mean
    several copies, of which Word took one.
  - `fontsChangedSincePreviousRun=` and the runner's `FONTS MOVED <id>` lines: faces that differ
    from the previous cut recorded in the same manifest.

  A face that moved means the rows of those documents are not comparable with the previous cut.

## Proofing languages

The proofing tools installed decide hyphenation. The VM has none (the registry's `Override` key has
no subkeys), so Word hyphenates nothing, and the corpora are scored with `-Dfidelity.hyphenate=false`
to match. Installing a language's proofing tools would hyphenate that language's documents on the
next cut.

## The conversion script

`ConversionScript` writes the VBS that Word runs, into the goldens directory beside the PDFs, and
the manifest names it (`wordConvertScript=`).
- **Field update off** (`-Dfidelity.updateFields=false`, `fieldUpdate=off`). Measured: a document cut
  with the update on differs from the same document cut without it by hundreds of lines. With it
  off, both sides show what the file stores. The fields Word evaluates anyway are PAGE, NUMPAGES,
  SECTIONPAGES, PAGEREF, PRINTDATE, and DATE and TIME on open; docx4j evaluates the same ones.
- **Review markup hidden** (`markup=off`). Word paints comment balloons and tracked changes into
  its PDF by the application's display-for-review state, not the document's. A cut once put balloon
  text into eight commented documents and scaled their pages to make room. The script turns the
  display off before it converts.
- **`SaveAs <pdf>, 17`** (wdFormatPDF), as documents4j's own script does. It passes no export options,
  so Word's own apply. `pdfExport=` records what the PDFs show of them: PDF version, producer,
  tagged or not, bookmarks, PDF/A, and any fonts left unembedded. Moving to `ExportAsFixedFormat` or
  to printing through a PDF driver would be a change of basis and needs a full re-cut.
- **What differs on every cut regardless:** the date and time painted by DATE, TIME and PRINTDATE
  (the cut's own), and FILENAME, which Word paints as the temporary file's name.
  `-Ddocx4j.convert.out.fields.dateCachedResult=true` lets docx4j paint the date the re-saved file
  stores, which is the cut's.

## Word's other settings

Word's application options (File > Options) and Windows' regional format are not in the docx and
not in the manifest. None has been measured moving a golden here. Leave them alone. If one has to
change, re-cut and note it in the ledger.

The regional format is the one most likely to matter: it is the default date format of a DATE field
with no picture switch.

## The input Word is given

- **The golden PDF is cut from docx4j's re-save of the corpus file.** The runner loads each document
  with docx4j and documents4j hands Word a temporary copy (README, "Having Word save the docx too").
  So the docx4j build the runner runs on is part of the input: a change to what docx4j's load and
  save keep (`mc:AlternateContent`, extension attributes) changes what Word lays out.
- **The scored docx is Word's re-save** (`resaved-nofields`), not the corpus file. A golden set and
  its resaved directory are cut together and travel together.
- Never edit a corpus file in place. A changed document is a new document.

## Before a cut

1. The default printer is the standard (`machine-reading.ps1`, its `PRINTER|` line), and Windows is
   not managing the default printer.
2. Word's build is the series' (`WORD|`), and Office updates are paused.
3. No fonts have been installed or removed since the set's last cut, and connected experiences are
   unchanged.
4. The runner on the VM's classpath is a build of current source (README, "Build and run"). Its run
   header should print `connectedExperiences=`, `proofingLanguages=`, `printer=`, `printerDriver=`,
   `printerPage=` and `word=`. A header without them is an old build.
5. Run it with `-Dfidelity.updateFields=false`, leaving `-Dfidelity.showMarkup` unset.

## After a cut

- Diff the run's header lines against the previous run's in the same manifest (and against the other
  sets'): `printer=`, `printerDriver=`, `printerPage=`, `word=`, `connectedExperiences=`,
  `proofingLanguages=`, `pdfExport=`.
- Read `fontsChangedSincePreviousRun=` and the `fontFile.<face>=` hashes.
- **If anything differs, the set is on a new basis.** Re-cut every set, not only the one being cut,
  and re-baseline: a scoreboard is comparable only with one scored against goldens from the same
  environment. Move the superseded goldens to `X:\fidelity\superseded`, and record the change and
  why in the ledger.

## Gaps

What is not yet recorded, and would be the next additions to the manifest:
- **The docx4j version the runner ran on**, which made the re-save Word was given.
- **Windows' build.** `os=` says only "Windows 10 10.0".
- **Windows' regional format.**
- **Word's application options.**
- **The machine-state lines from the September corpora.** Those sets record neither connected
  experiences nor proofing languages. The VM's newest runs (2026-10-06) record no machine state at
  all either, though the runner has written it since 2026-09-18, so the VM is running a build from
  before that.
