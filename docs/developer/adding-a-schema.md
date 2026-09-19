# Adding support for a new schema to docx4j

The recipe for binding a schema (a namespace Office writes) that docx4j does not
yet know, so that its content is typed in the JAXB model, survives a load and a
save, and - where it is a part of its own - is a Part. Jason Harrop set the steps
down on 2026-09-20; each is checked against the commit that added the
`w16cex` schema (CR-018, `33e9cb411`, 2026-09-16), which is the worked example
to read alongside this. Everything lands in a single git commit (step 12).

## 1. Identify the schema, and an Office file exercising it

The schema comes from ECMA-376 or from a Microsoft open specification
([MS-DOCX], [MS-XLSX], [MS-PPTX], [MS-ODRAWXML] and their kin on
learn.microsoft.com); record the source and the date fetched in a comment at the
top of the xsd, with every change made to it (schemaLocation names, annotations,
attributes Office writes that the source omits - see the head of
`xsd/wml/w16cex.xsd`).

Have a file Office wrote that carries the content, small, and one you may commit
as a test resource in `docx4j-core-tests/src/test/resources/` (not a document
from the layout-fidelity corpora, which are not redistributable). Where the
element sits in that file - which part, which parent - decides steps 2 and 3.

### Obtaining the schema

For an ECMA-376 schema, the xsd files ship with the standard (Part 1's
annexes and Part 4's transitional set); this tree already carries them.

For a Microsoft extension, the schema is an appendix of the open
specification ("Full XML Schema", one numbered subsection per namespace). Three
sources, in order of preference:

1. **The HTML section page** on learn.microsoft.com: each subsection of the
   appendix is its own page, with the XSD in a code block - exact whitespace,
   one namespace per page, and a URL that names the section to cite in the
   xsd's header comment. This is what `xsd/wml/w16cex.xsd` was taken from (its
   header quotes the page). Not every specification's appendix is rendered
   that way; check for the section page first.
2. **The docx download** from the specification's landing page (the "Published
   Version" table offers PDF and DOCX per revision, the DOCX named with its
   date, `[MS-XLSX]-260519.docx` for revision 29.1 of 2026-05-19): the schema
   sits in code-styled paragraphs with its line structure intact, and each
   namespace's subsection is a heading, so docx4j itself can split the appendix
   into one file per namespace by heading. Cite the revision.
3. **The PDF**, last: its text layer cuts every page with the running header,
   footer and page number, and extraction can merge wrapped lines, so a long
   schema needs stitching by hand.

Whichever the source, record it, the revision or the fetch date, and every
change made to the text (step 3) at the top of the xsd, as `w16cex.xsd` does.

## 2. Find where it belongs in the schema tree

`xsd/ROOT.xsd` imports the roots of each format's tree; a schema joins the tree
by an `xsd:import` from the schema whose content model references it, and, if it
is a part's own namespace, from ROOT.xsd directly. Word schemas hang off
`xsd/wml/wml.xsd` (extensions `w14`, `w15`, `w16*` beside it); Excel's off
`xsd/sml/`; PowerPoint's off `xsd/pml/`; DrawingML and its 2010+ extensions
under `xsd/dml/` and `xsd/odrawxml/`; the markup-compatibility schema is
`xsd/mce/markup-compatibility-2006-MINIMAL.xsd`.

Two cautions from the schema work of September 2026 (CR-018, CR-021):

- **Admit what Office writes, not what the standard allows.** Measure the
  parents on real files (the survey scripts of CR-021 phase 0 parsed every part
  of 835 documents by parent and `Requires`) and add `xsd:element ref` only for
  those; a kept element is only lossless if the schema binds its whole content,
  so check the branch content too (CR-021 §8.6 item 10: an `a14:m` equation
  dropped inside a kept Choice until `EG_TextRun` admitted it).
- **Mind the JAXB context boundaries.** docx4j has separate contexts for
  WordprocessingML (`org.docx4j.jaxb.Context.jc`), PresentationML
  (`org.pptx4j.jaxb.Context.jcPML`) and SpreadsheetML
  (`org.xlsx4j.jaxb.Context.jcSML`), each a package list. A schema that
  references types of another context's packages (an OMML `m:r` holds a
  `w:rPr`) will make the smaller context fail to initialise with an
  `IllegalAnnotationsException`; use `xsd:any processContents="lax"` over the
  foreign namespace instead of element references there (`CT_TextMath` in
  `xsd/odrawxml/oart14docprop.xsd` is the example), which is typed where the
  context knows the element and DOM where it does not.

## 3. Add the xsd, with its JAXB annotations

Put the file under the tree it belongs to and import it from its parent schema
(and from `xsd/ROOT.xsd` when it is a part's namespace). In the new file:

1. the JAXB namespace declarations on `xsd:schema`, plus `annox` if a root
   element is needed (step 3.4):

       xmlns:jaxb="https://jakarta.ee/xml/ns/jaxb"
       xmlns:xjc="https://jakarta.ee/xml/ns/jaxb/xjc"
       xmlns:annox="urn:jaxb.jvnet.org:annox"
       jaxb:extensionBindingPrefixes="annox"

2. `jaxb:version="3.0"` on `xsd:schema`;

3. the package name, in an `xsd:annotation` directly under `xsd:schema`. The
   package is `org.docx4j.<prefix>` for Word extensions (`org.docx4j.w16cex`),
   `org.xlsx4j...` / `org.pptx4j...` / `org.docx4j.com.microsoft.schemas...`
   for the others, following the neighbouring schemas of the same tree:

       <xsd:annotation>
           <xsd:appinfo>
               <xsd:appinfo>
                   <jaxb:globalBindings>
                   </jaxb:globalBindings>
               </xsd:appinfo>
               <jaxb:schemaBindings>
                   <jaxb:package name="org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2014.revision"/>
               </jaxb:schemaBindings>
           </xsd:appinfo>
       </xsd:annotation>

4. for a type that is a part's root, or that must be a root element for
   `xsd:any` content to unmarshal typed, the annox root-element annotation on
   the complex type:

       <xsd:annotation>
           <xsd:appinfo>
               <annox:annotate target="class">@jakarta.xml.bind.annotation.XmlRootElement(name="commentsExtensible")</annox:annotate>
           </xsd:appinfo>
       </xsd:annotation>

5. `<xsd:attribute ref="mc:Ignorable" use="optional"/>` on a part's root type
   when Office writes it (it does on most 2010+ parts), with the import of the
   minimal mce schema.

## 4. Regenerate docx4j-generated-objects

    mvn clean install -DskipTests -Dgpg.skip=true -pl docx4j-generated-objects,docx4j-core

`clean` is required: the XJC plugin checks only `xsd/ROOT.xsd` for staleness,
so an incremental build keeps the old generated classes. The build takes some
minutes; the deep-copy (`docx4j-xjc-copy`) and parent-pointer plugins run by
themselves. Check the generated package under
`docx4j-generated-objects/target/generated-sources/xjc/` for the classes and
accessors you expect before going on.

## 5. Export and open the package in the module system

`docx4j-generated-objects/src/main/java/module-info.java` needs the new
package twice, as every generated package has it:

    exports org.docx4j.w16cex;
    opens org.docx4j.w16cex; //  to java.xml.bind, com.sun.xml.bind, org.eclipse.persistence.moxy, org.eclipse.persistence.core;

Without the `opens`, JAXB cannot reflect on the classes on the module path;
without the `exports`, docx4j-core cannot see them.

## 6. Add the package to the JAXB context path

The Word context is the `jcString` in
`docx4j-core/src/main/java/org/docx4j/jaxb/Context.java` (colon-separated
package names; there is a commented copy of the same list beside it, keep the
two in step). A namespace that appears in presentations or workbooks goes into
`org/pptx4j/jaxb/Context.java` (`jcPML`) or `org/xlsx4j/jaxb/Context.java`
(`jcSML`) as well - they are separate lists, and a package missing from the
list its content is unmarshalled under is an "unexpected element" at load.

## 7. Add the namespace to the prefix table

`docx4j-core/src/main/java/org/docx4j/jaxb/NamespacePrefixMappings.java`, in
both directions: `getPreferredPrefixStatic` (namespace URI to prefix) and
`getNamespaceURIStatic` (prefix to URI). Both are chains of string comparisons
walked from the top for every prefix resolved, so, as in step 9, the common
namespaces stay at the top and a more obscure one goes further down beside its
kin (a new SpreadsheetML revision namespace with the `xr*` entries, a new
Word 2018 extension with the `w16*` ones), in both chains. The prefix must be the one Office
writes (`w16cex`, `xr2`, `cx1`, ...): `mc:Ignorable` and `mc:Choice/@Requires`
name prefixes, and on save docx4j declares every prefix a part's `mc:Ignorable`
names from this table (`NamespacePrefixMapperUtils.declareNamespaces`, which
warns "No mapping for prefix" for one it lacks - and Office offers to repair a
file whose `mc:Ignorable` names an undeclared prefix). The JAXB-ReferenceImpl
and MOXy modules' prefix mappers delegate to this table; nothing to add there.

The TypeScript objects package keeps a copy of this table (124 entries as of
2026-09-19, level with docx4j's); a new entry here is part of the hand-off in
step 11.

## 8. A Part class, if the namespace is a part's

Not every namespace is a part (`w14` is content inside `document.xml`; `w16cex`
is the part `/word/commentsExtensible.xml`). For a part, add a class under
`docx4j-core/src/main/java/org/docx4j/openpackaging/parts/` (in the format's
subpackage) extending `JaxbXmlPart<T>` with the generated root type as `T`,
whose `init()` sets the content type and the relationship type, following
`CommentsExtensiblePart`:

- the content type constant in
  `openpackaging/contenttype/ContentTypes.java` (with a `PartName` comment and
  `@since`);
- the relationship type constant in
  `openpackaging/parts/relationships/Namespaces.java`;
- the constructors: `(PartName)` and a no-argument one with the part's usual
  name.

For a part whose JAXB context is not the Word one, override `getJAXBContext()`
(see the DrawingML and SpreadsheetML parts).

## 9. Register the part by content type

`docx4j-core/src/main/java/org/docx4j/openpackaging/contenttype/ContentTypeManager.java`,
`newPartForContentType`: an `else if` on the new content-type constant returning
the new part, so that a package being loaded gets the typed part rather than a
generic one. The chain is walked in order for every part of every package
loaded, so its order is by frequency: the common parts (the main document,
styles, the workbook, slides) stay at the top, and a more obscure namespace
goes further down, beside the parts most like it (a Word 2018 comments
extension next to the other comments parts, a spreadsheet revision part next to
the other SpreadsheetML extras) - not appended at the end and not put first.
If the part is reached from a particular parent by relationship
type (comments from the main document part, say), a typed getter on that parent
part is the convenience `DocumentPart.getCommentsExtensiblePart` gives.

## 10. Tests

New unit tests in `docx4j-core-tests` (JUnit 4; load resources by classpath
with `ResourceUtils.getResource`), in the style of `CommentsExtensiblePartTest`
and `SchemaGapsTest`:

- the Office file of step 1 loads with the part typed (`instanceof` the new
  Part class) and reachable by relationship type;
- the content unmarshals to the expected Java objects (the values Office wrote);
- a save and a reload keep them (the round trip), and the saved part's root
  declares the prefixes its `mc:Ignorable` names;
- a document docx4j creates with the new content, or the round-tripped Office
  file, **opens in Microsoft Office without a repair prompt**. That check is
  manual: put the file on the share for Jason (the `cr021-phase2-check` folder
  is the pattern - a README line saying what to look at), and record the
  verdict in the CR. It is the check that catches what no unit test can: a
  kept branch that lost its content, a prefix Office wanted declared.

Run the whole `docx4j-core-tests` suite (surefire forks one JVM per class, so
JAXB context problems show as failures in unrelated classes), and
`docx4j-export-fo-tests` when the schema touches anything the exporters read.

## 11. CHANGELOG, CR and the hand-off

- CHANGELOG under a "Schema" heading of the coming release: what is admitted
  or bound, and what a user sees (a part that used to be generic is typed; a
  value that was dropped survives).
- The CR that asked for it (or a new one under
  `docs/developer/change-requests/` if the work is non-trivial) records the
  commit hash against the phase.
- The ports regenerate from this repository's xsd, each its own way; message
  each with the CR number and the commit hash, and what follows. The portfolio
  registry (`../docx4j-portfolio/tasks.yaml`) carries the dependency, and the
  core-ts port, which treats this repository as its behavioural oracle, is told
  when the change is one its behaviour follows.

  - **TypeScript objects** (`../docx4j-generated-objects-ts`): regenerates its
    modules from `xsd/ROOT.xsd` at the commit, in one JAXB-like context over all
    modules (so a lax wildcard comes back typed there where docx4j keeps DOM - a
    recorded divergence, not a mismatch). Tell it every xsd touched, the packages
    and their effect on the model (a new property, a new admission), and any
    prefix-table entry: it keeps a copy of `NamespacePrefixMappings` (level at
    124 entries on 2026-09-19). It regenerates once from the commit that carries
    everything (CR-018, CR-021 §8.8 to §8.12 are the precedents).

  - **Python** (`../docx4j-python`; facts from its session, 2026-09-20; its
    design is that repository's CR-001 and the "How regeneration works" section
    of its CLAUDE.md): it does not read `xsd/ROOT.xsd`. It generates from a
    **copy** of this tree under `schemas/` - the 91-file transitive closure of
    `wml/wml.xsd` plus `relationships.xsd` and `docProps/`, collected by its own
    entry point `schemas/docx4j_python__ROOT.xsd` - taken by hand from a named
    docx4j commit (6b8048aab as of 2026-09-20) with five patches of its own,
    each marked in the xsd by a comment beginning `docx4j-python` and listed in
    `schemas/PATCHES.md`; a patch docx4j later makes itself is retired at the
    next copy. PML, SML and VML are not generated yet (its CR-001 phase D), so a
    schema outside WML's closure does not reach it until then. Its generator is
    a fork of xsdata (`../docx4j-xsdata`, branch `docx4j`), run as
    `codegen/generate.sh` (`--check` proves the output reproducible); it needs
    only the xsd files - a docx4j build is needed only for
    `codegen/derive_names.py`, which derives the Java class names from this
    repository's XJC output (`docx4j-generated-objects/target/generated-sources/xjc`)
    into `codegen/names/*.json`, so a **new namespace** needs that output (at
    least its `ObjectFactory`), an include in its ROOT, a namespace-to-module
    line in `.xsdata.phase-b.xml`, and a prefix entry in
    `docx4j_py/namespaces.py` (transcribed from `NamespacePrefixMappings`). A
    **new part** needs its class, a content type
    (`openpackaging/content_types.py`), a relationship type
    (`openpackaging/parts/namespaces.py`) and a `parts/registry.py` entry, all
    transcribed from docx4j's. So the hand-off to Python is: the commit hash,
    the list of xsd files touched (it has no re-copy script; the copy is
    re-taken by hand), whether a new namespace should be generated at all, the
    XJC output for anything new, and the prefix, content-type and
    relationship-type entries. Its gate afterwards: `generate.sh --check`, its
    corpus round trip canonically identical with an empty skipped-content
    report, the parity goldens (consumed from core-ts, never copied), and a
    Word check of its acceptance artefacts by Jason. It also keeps docx4j's
    default `styles`, `numbering`, `fontTable` and `KnownStyles` resources,
    the three theme parts, and font tables generated from docx4j's font data
    and jars - a change to any of those is a hand-off too, though not a schema
    one.

## 12. One commit

All of the above in a single git commit - the xsd, the regeneration is not
committed (generated sources are not checked in), the module-info, the two
Context lists, the prefix table, the part, its registration, the tests, the
CHANGELOG and the CR - so that a checkout at that commit builds and the
TypeScript side has one hash to regenerate from. The commit message names the
schema, its source, the parents admitted and the Office file that exercises it.
Line endings: LF (normalise a CRLF file you edit, and say so in the message).
