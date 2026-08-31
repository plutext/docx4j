# Using docx4j from Python

*Last reviewed against docx4j 17.0.4 (August 2026); examples tested with
Python 3.14, JPype 1.7.1, Java 21.*

*This is the maintained setup reference.  For the "should I?" question — when
bridging to docx4j beats python-docx or docxtpl — see
[docx4j from Python](https://www.docx4java.org/docx4j-from-python.html) on the
docx4j website.*

## Why would you, given python-docx / docxtpl?

For simple generation or text extraction you shouldn't: python-docx and
docxtpl are pleasant, deploy anywhere, and a JVM is real operational weight.
The case for docx4j from Python is the capabilities that don't exist natively
in Python:

- **Headless docx → PDF without Word or LibreOffice** — the XSL-FO / Apache FOP
  pathway, including PDF/A and PDF/UA output (see
  [PDF_FOP_Accessibility.md](PDF_FOP_Accessibility.md)).  Python answers are
  docx2pdf (requires Word), LibreOffice headless, or paid conversion APIs.
- **OpenDoPE data binding** — structured content-control binding with repeats,
  conditions, position conditions, components, and the reverter for
  round-tripping edited instances.  docxtpl's Jinja-tags-in-runs approach is
  string templating inside a format that fights it; there is no Python library
  in this territory.
- **Spec coverage** — docx4j's object model is generated from the OpenXML
  schemas, so essentially everything is addressable (sdts, numbering, fields,
  comments, tracked changes, DrawingML), where python-docx exposes a curated
  subset.
- **The surrounding machinery** — docx → HTML, XHTML import, diffing,
  anonymization, TOC generation and field updating, merge/concatenation
  (commercial MergeDocx), plus pptx and xlsx in the same object model.

Decision rule: if the requirement is "fill a template, simple formatting,
extract text" — stay native.  If it includes faithful PDF output without
Office, structured data binding, document surgery, or high-volume generation,
wrapping docx4j is less total effort than fighting the format in Python.

## Routes

1. **JPype (in-process JVM)** — a JVM inside the Python process; Java classes
   then look like Python objects.  The usual choice for scripts, notebooks and
   batch jobs; this page's examples use it.  Caveats: one JVM per Python
   process (it cannot be restarted), the JVM's memory sits alongside Python's,
   and Java exceptions surface as JPype exceptions.
2. **A small service or CLI wrapper** — the production answer.  Wrap the
   docx4j operations you need behind a tiny HTTP service (or a CLI invoked per
   job) and call it like any other service: clean process boundary, warm JVM,
   independent scaling.  (docx4j-documents4j-remote already uses this shape
   for Word-based conversion.)
3. **Py4J** — socket gateway to a separately-running JVM (PySpark's
   mechanism); useful when you want the JVM long-lived but out of process
   without designing a REST API.
4. **GraalPy** — Python on GraalVM with direct Java interop; check your
   C-extension needs before committing.  (Jython is Python 2 only — avoid.)

## Setup

```
pip install JPype1
```

You need a JDK installed (JPype locates it via `JAVA_HOME`), and the docx4j
jars:

- For loading / manipulating / OpenDoPE binding / saving docx-pptx-xlsx, the
  **shaded bundle jar** is a single self-contained file:
  `docx4j-bundle-<version>-shaded.jar` (build it with `mvn install` in the
  `docx4j-bundle` directory, after `mvn install` at the top level; the bundle
  is not deployed to Maven Central).  Note: the shaded jar only contains the
  JAXB runtime from 17.0.4 on.
- For **PDF output**, add `docx4j-export-fo` and its dependencies.  The
  simplest way to collect them is a throwaway Maven pom depending on
  `org.docx4j:docx4j-export-fo`, then
  `mvn dependency:copy-dependencies -DoutputDirectory=libs`, and put
  `libs/*.jar` on the classpath alongside the bundle.

## Example 1: OpenDoPE data binding (bundle jar only)

Tested against `invoice.docx` from docx4j's test resources (a template with a
table-row repeat and conditions):

```python
import sys, jpype, jpype.imports

jpype.startJVM(classpath=["docx4j-bundle-17.0.4-shaded.jar"])

from java.io import File, StringWriter
from org.docx4j.openpackaging.packages import WordprocessingMLPackage
from org.docx4j.model.datastorage import OpenDoPEHandler, BindingHandler
from org.docx4j import TextUtils

pkg = WordprocessingMLPackage.load(File("invoice.docx"))

odh = OpenDoPEHandler(pkg)                        # repeats + conditions
pkg = odh.preprocess()
bh = BindingHandler(pkg)                          # data binding
bh.setStartingIdForNewBookmarks(odh.getNextBookmarkId())
bh.applyBindings(pkg.getMainDocumentPart())

sw = StringWriter()
TextUtils.extractText(pkg.getMainDocumentPart().getContents(), sw)
print(str(sw))                                    # bound values are present

pkg.save(File("out.docx"))
```

## Example 2: docx → PDF (bundle + export-fo jars)

```python
import glob, jpype, jpype.imports

cp = ["docx4j-bundle-17.0.4-shaded.jar"] + glob.glob("libs/*.jar")
jpype.startJVM(classpath=cp)

from java.io import File, FileOutputStream
from org.docx4j.openpackaging.packages import WordprocessingMLPackage
from org.docx4j import Docx4J

pkg = WordprocessingMLPackage.load(File("in.docx"))
out = FileOutputStream("out.pdf")
Docx4J.toPDF(pkg, out)
out.close()
```

## Practical notes

- **Start the JVM once**, at process start, with everything on the classpath;
  `jpype.startJVM` cannot be called twice.
- **Logging**: docx4j uses slf4j.  With no provider on the classpath you get
  slf4j's NOP warning and silence; add a provider (eg `slf4j-simple` or
  logback) to the classpath and configure it if you want docx4j's logging.
- **Configuration**: put a `docx4j.properties` on the classpath (a directory
  on the JPype classpath works) to configure docx4j; see the reference file in
  `docx4j-samples-resources`.
- **Exceptions**: Java exceptions arrive as JPype exception objects; wrap
  docx4j calls and translate to your own error types at the boundary.
- **Long-lived processes**: the same housekeeping applies as from Java (eg
  deleting embedded-font temp files after PDF export; see the
  ConvertOutPDFviaXSLFO sample).
- **Threading**: use one package object per thread; for PDF, see the
  `docx4j.convert.out.fo.renderers.ConfiguredPDFDocumentHandler` property
  regarding FopFactory reuse.
