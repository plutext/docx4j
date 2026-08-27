# The OpenDoPE component model

*As implemented in docx4j; code references are to `org.docx4j.model.datastorage`
unless otherwise noted.  Last reviewed against docx4j 17.0.4 (August 2026).*

## Concept

A *component* is a reusable sub-template — a separate docx — referenced from a
host OpenDoPE template.  At document-generation time each reference is resolved,
the component docx is fetched, and its content is merged into the host, with the
component's own data bindings evaluated against an XPath *context* supplied by
the reference site.  It is the OpenDoPE answer to template composition /
inclusion.

## Declaring components

Two small pieces:

### The components part

A custom XML part in the `http://opendope.org/components` namespace, holding:

```xml
<components xmlns="http://opendope.org/components">
  <component id="comp1" iri="some://identifier/of/the/component.docx"/>
</components>
```

- `id` is what document tags reference.
- `iri` says where the component docx lives; the IRI scheme is yours to define
  (see `DocxFetcher` below).

JAXB model: `org.opendope.components.Components(.Component)`; part class
`org.docx4j.openpackaging.parts.opendope.ComponentsPart` (with
`getComponentById`), recognised at load time by its root element, like the
other opendope parts.

### The tag vocabulary

Constants in `OpenDoPEHandler`:

| tag | meaning |
|---|---|
| `od:component=<id>` | this sdt is a component reference |
| `od:context=<xpathId>` | optional: the XPath context the component's bindings should be evaluated in |
| `od:continuousBefore` / `od:continuousAfter` | historical section-continuity hints (make the join seamless rather than a page break); the `makeContinuous` machinery for these is currently commented out in the handler |

## Processing

Processing lives in `OpenDoPEHandlerComponents`.  It was redesigned in docx4j
6.1 (recorded in comments at the top of that class):

- components no longer need to sit at the top paragraph level;
- they can carry an XPath context;
- processing happens **before** condition/repeat processing, as a discrete step;
- processing is **not recursive** — a fetched component's own component
  references are not processed;
- it is **MainDocumentPart-only** (MergeDocx can concatenate into the MDP, but
  not into other parts).

The pipeline, as wired in `Docx4J.bind` (`FLAG_BIND_BIND_XML`):

1. **Off by default** — gated by the property
   `docx4j.model.datastorage.OpenDoPEHandlerComponents.enabled`.

2. A traversor walks the body finding `od:component` sdts.  **Context
   resolution precedence**: an explicit `od:context` on the tag wins; else the
   context is *inferred from the nearest enclosing `od:repeat`* (the traversor
   keeps a stack of repeat xpath ids as it descends); else none (root).
   Condition and repeat sdts themselves pass through untouched at this stage —
   they are `OpenDoPEHandler`'s business afterwards.

3. Each component sdt is **replaced by a `w:altChunk`**: the component docx
   bytes are obtained through the pluggable **`DocxFetcher`** interface
   (`InputStream getDocxFromIRI(String iri)`) — user-supplied via
   `OpenDoPEHandlerComponents.setDocxFetcher`, or as the `docxFetcher`
   parameter of the `Docx4J.bind(...)` overloads which take one — and stored
   in an `AlternativeFormatInputPart`.  A map of altChunk rel-id to
   context xpathId is accumulated alongside.

4. The actual merge of those altChunks into real WordML — including rewriting
   the component's bindings for the recorded contexts, using the answers
   document, xpaths map and conditions map — is **delegated via reflection to
   the commercial MergeDocx extension**
   (`com.plutext.merge.altchunk.ProcessAltChunk.process(pkg, answerDomDocs,
   xpathsMap, conditionsMap, altChunkXPathContexts)`).  Without MergeDocx on
   the classpath, processing degrades gracefully: the altChunks remain in the
   package (Word would flatten them on open, but without context rewriting) and
   a message explaining the extension is logged.

5. Because `Docx4J.bind` mutates the caller's package in place while the merge
   produces a new one, the merged result is round-tripped (save to bytes, then
   `Load3.reuseExistingOpcPackage`) back into the original package object.
   Only then does `OpenDoPEHandler.preprocess` run conditions and repeats over
   the now-composed document, followed by data binding proper.

## Design consequences

- Since fetching precedes condition evaluation, a component sitting inside a
  condition is fetched and merged even if the condition later evaluates false
  (the merged content is then removed along with the condition's content).
- Since processing is not recursive, nested composition (components referencing
  components) requires the pieces to be pre-flattened.

## Running the sample

`org.docx4j.samples.ContentControlBindingComponents` (in `docx4j-samples-docx4j`)
demonstrates the whole pipeline on two files in
`docx4j-samples-docx4j/sample-docs/databinding/`:

- **`component-host.xml`** — the host template, as Flat OPC XML (docx4j loads
  that format transparently).  Its body is six numbered paragraphs; paragraph 4
  is an sdt tagged `od:component=comp1`, and its components part maps `comp1`
  to the IRI `component-subdoc.docx`.  Its XPaths and conditions parts are
  empty: the host has no bindings of its own.
- **`component-subdoc.docx`** — the component: one paragraph of static text
  ("if you can see this, component insertion worked") ending in a content
  control bound to `/yourxml/magic`.

The sample turns component processing on, supplies a `DocxFetcher` that
resolves the IRI relative to the `sample-docs/databinding` directory, and calls
`Docx4J.bind` with `FLAG_BIND_INSERT_XML | FLAG_BIND_BIND_XML`, injecting

```xml
<yourxml>goes here<magic>xyzzy</magic></yourxml>
```

The magic word deliberately differs from the one saved inside the subdoc
(`abracadabra`): seeing *xyzzy* in the output proves the component's binding
was re-evaluated against the host's data, not just carried across.

To run it:

1. Run the `main` from your IDE (or however you usually run the samples), with
   the **working directory set to `docx4j-samples-docx4j`** — the input paths
   and output path are resolved against `user.dir`.  You need a docx4j JAXB
   implementation on the classpath (e.g. `docx4j-JAXB-ReferenceImpl`).
2. For the component to be merged into real WordML, the commercial **MergeDocx**
   extension must also be on the classpath, together with its
   `docx4j-Enterprise-License` and `license3j` dependencies and a
   `plutext-license.bin` licence file (a trial licence ships with the MergeDocx
   distribution).  Without MergeDocx the sample still runs, but the component
   is left in the output as a `w:altChunk` (step 4 above), and the binding is
   not re-evaluated.
3. Open the result, `OUT_ComponentsProcessed.docx`.  With MergeDocx you should
   see paragraphs 1–3, then "4 - if you can see this, component insertion
   worked! The magic word is: xyzzy", then 5 and 6.

Since the host's own XPaths part is empty, identifying the runtime data part
can't be done from XPaths entries; since 17.0.4,
`CustomXmlDataStoragePartSelector` falls back to the sole plain custom XML data
part in the package (the OpenDoPE and well-known Microsoft parts don't count).
This is what makes a *component-only* host like this one processable at all —
earlier versions failed with "Couldn't find CustomXmlDataStoragePart".

## Related, but orthogonal

The components part is one of a family in
`org.docx4j.openpackaging.parts.opendope`: `XPathsPart`, `ConditionsPart`,
`ComponentsPart`, `QuestionsPart`, `StandardisedAnswersPart`.  The
questions/answers pair belongs to the interview-style layer of OpenDoPE and is
independent of composition.

Components also resolve before data binding ever runs, so the choice of
binding traverser implementation (see `BindingHandler`'s javadoc and
`docs/developer/change-requests/CR-binding-traverser-parity.md`) does not
affect component processing.
