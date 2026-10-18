# CR-049: `XmlUtils`' marshal methods leave a `wml.Document`'s `mc:Ignorable` prefixes on the shared prefix mapper, and drop the Choice namespaces for every other root

Status: DONE 2026-10-19, the one phase (§5), for 17.3.4: implemented by the docx4j-25 session (Claude Opus 5.5) at Jason's request ("commit, then do CR-049 yourself"). ACCEPTED 2026-10-19 (Jason: "i accept D1 to D4.  please commit"), to implement as recommended. Drafted at Jason's request ("write the CR please") by Claude Fable 5.1
(session docx4j-43) from its assessment of the finding CR-046's phase 4 record lists under "Findings
outside CR-046, not fixed here" (the docx4j-25 session found it writing `TextsPhase2Test`'s REQ-204
snapshot, which strips namespace declarations to work around it). One phase (§5); decisions in §6.
Touches `XmlUtils`, which CLAUDE.md lists among the widely-shared code to change on Fable.

## 0. Summary

The prefix mapper every marshal uses is one static object (`NamespacePrefixMapperUtils.prefixMapper`).
Its Ignorable list, which the runtime reads through `getPreDeclaredNamespaceUris` to decide what to
declare on the root element, is a thread-local on that object. `XmlUtils`' marshal methods set it when
the root is a `wml.Document` and never clear it; `JaxbXmlPart`'s own marshal path clears it after a
successful marshal. So on any thread that has once marshalled a main document through `XmlUtils`, every
later `XmlUtils` marshal of anything else declares that document's whole Ignorable set at its root. The
mirror defect: for every root other than `wml.Document`, `XmlUtils.setMcIgnorable` sets nothing and
silently drops the Choice-namespaces argument its callers pass, so `JaxbXmlPart.getXML()` on a header,
footer, footnotes or settings part declares neither its root's Ignorable prefixes nor its Choice
prefixes, unless the leak happens to supply them.

Saved files are unaffected: the save path overwrites the value with the part's own prefixes. What is
affected is every XML string or DOM `XmlUtils` produces: its namespace declarations depend on what the
thread marshalled before. The fix is small: each marshal leaves the mapper as it found it, and the
CR-024 general rule (declare what the root's own `mc:Ignorable` names, plus the Choice namespaces) is
applied to every root, not only `wml.Document`.

## 1. The defect (read 2026-10-19 on VERSION_17_3_4 at a41de133b)

- `XmlUtils.setMcIgnorable(prefixMapper, o, mcChoiceNamespace)` (lines 816-838): if `o` is a
  `wml.Document`, sets the mapper to the document's `getIgnorable()` with `mcChoiceNamespace` appended
  and returns that string; for any other root it logs at debug ("Not declaring mc ignorable or choice
  namespaces on ...") and returns null. Nothing is cleared afterwards. Its five callers are the three
  `marshaltoString` / `marshaltoInputStream` / `marshaltoW3CDomDocument` families (lines 899, 977,
  1039, 1093, 1149), which every `marshaltoString(o)` and `marshaltoW3CDomDocument(o)` overload reaches.
- Both runtime mappers keep the value in a `ThreadLocal<String>` (`docx4j-JAXB-ReferenceImpl`'s
  `NamespacePrefixMapper` line 31; `docx4j-JAXB-MOXy`'s line 36) and read it in
  `getPreDeclaredNamespaceUris` and `getPreDeclaredNamespaceUris2`, which the runtime calls on every
  marshal with that mapper. `NamespacePrefixMapperUtils.getPreDeclaredNamespaceUris` (lines 198-221)
  maps each prefix through the table and warns `No mapping for prefix '...'` for one it lacks.
- `JaxbXmlPart.marshal(OutputStream)` (lines 924-936) sets the value unconditionally to the part's
  Ignorable plus its Choice namespaces before marshalling and clears it after a successful marshal, not
  in a `finally`. `JaxbXmlPart.marshal(Node, mapper)` (lines 717-734) calls `setMceIgnorable`, whose
  default (lines 1042-1045, the CR-024 general rule via `ignorableOf`) sets only when the root's
  Ignorable is non-empty, and clears after success. So the Node path, used by
  `JaxbXmlPartXPathAware.getBinder` (line 143) to build a binder DOM, marshals a part whose root has no
  Ignorable with whatever the thread left.
- `FlatOpcXmlCreator.marshalBytes` (line 261) and its deprecated `marshaltoW3CDomDocument` (line 415)
  set the value per part explicitly and never clear it, so a Flat OPC export leaves the last part's
  prefixes on the thread.
- `JaxbXmlPart.getXML()` (line 255) passes `getMcChoiceNamespaces()` to `XmlUtils.marshaltoString`;
  for every root but `wml.Document` that argument is dropped.

## 2. What it affects, and what it does not

**Not affected: saved packages.** `Save` marshals through `JaxbXmlPart.marshal(OutputStream)`, which
overwrites the value. The leak adds namespace declarations only; it never adds a prefix to the
`mc:Ignorable` attribute, so no file Word opens gains an undeclared Ignorable prefix from it.
docx4j-digsig signs parts through the same path (`OOXMLURIDereferencer` line 212).

**Affected:**

- **Order-dependent output.** After a main document is marshalled through `XmlUtils` on a thread (the
  docx4j-mcp server does it at `Docx4jMcpServer` line 103; debug logging in `FieldsCombiner`,
  `VariablePrepare` and `FromMergeFields` does; any test does), every later `XmlUtils` marshal on that
  thread of a paragraph, a table, a header root, a relationships part, declares the document's whole
  Ignorable set (ten prefixes on Word 365's save of the extraction probe: `w14 w15 w16se w16cid w16 w16cex w16sdtdh w16sdtfl w16du wp14`) at
  its root. The output of `marshaltoString` is a function of the thread's history. CR-046's REQ-204
  snapshot (`TextsPhase2Test.snapshot`, lines 762-781) strips every `xmlns` declaration for this
  reason, and so cannot catch a regression in what a part declares. Any golden cut from
  `marshaltoString` output inherits the dependence, which matters to the ports that take docx4j as
  their oracle.
- **The masked mirror defect.** `getXML()` on a `HeaderPart`, `FooterPart`, `FootnotesPart`,
  `EndnotesPart`, `DocumentSettingsPart` or comments part is Word-safe (every Ignorable prefix and
  Choice prefix declared) only if a `wml.Document` was marshalled earlier on the thread and its set
  covers the part's. docx4j-mcp's `TemplateInspector` (line 156) shows part XML through `getXML()`
  after the server marshalled the main document, so there the leak is what makes the output look
  complete.
- **A diagnostic trap.** A document whose Ignorable names a prefix the table lacks makes every later
  marshal on that thread log `No mapping for prefix`, attributed to unrelated work; in a thread pool
  the value outlives the request.
- **Signature bytes.** The digsig facets build their signature objects through
  `XmlUtils.marshaltoW3CDomDocument` (`OOXMLSignatureFacet` lines 313 and 381, `XAdESSignatureFacet`
  line 345). With a main document marshalled earlier on the thread the objects carry the extra
  declarations, which `SignatureConfig`'s default inclusive canonicalization (line 160) keeps. The
  signature verifies, since digest and stored content agree, but its bytes are not reproducible.
- **The binder DOM.** A part without an Ignorable gets the stale declarations in the DOM its binder is
  built from. XPath and the re-unmarshal ignore them.

## 3. What the specifications say

ECMA-376 Part 3 §10.1.1: each prefix `mc:Ignorable` names must be bound in scope, else the document is
non-conformant (Word 2010 and later report it corrupt, as `JaxbXmlPart.setMceIgnorable`'s comment
records). Namespace declarations beyond those are permitted and carry no meaning (Namespaces in XML
1.0 §6.1); exclusive canonicalization drops the unused ones, inclusive keeps them. So the leak produces
conformant XML, and the mirror defect produces non-conformant XML when the string is handed to Word
or to an MCE processor.

## 4. Proposal

One contract: **a marshal leaves the prefix mapper as it found it, and declares what its own root's
`mc:Ignorable` names plus the Choice namespaces its caller passes.**

1. `XmlUtils.setMcIgnorable` applies the CR-024 general rule to any root: the root's `getIgnorable()`
   when its class has one (the reflective, per-class-cached `ignorableOf` now in `JaxbXmlPart`,
   moved to `NamespacePrefixMapperUtils` as a public static and delegated to from `JaxbXmlPart`),
   `XmlUtils.unwrap`ping a `JAXBElement` first, with `mcChoiceNamespace` appended as today. A root
   with neither sets null for the duration of its own marshal, so nothing is borrowed from an
   enclosing marshal or from the thread; the previous value comes back in the `finally` (§4.2).
   The returned string still feeds `trimNamespaces` on the canonicalize path, and the canonicalize
   paths' nested `marshaltoW3CDomDocument` call receives the caller's `mcChoiceNamespace`, which it
   does not today: under the restore the inner marshal's value is the one the written DOM sees
   (docx4j-25, 2026-10-19).
2. Each of the five `XmlUtils` methods reads the mapper's current value before setting it and restores
   it in a `finally`. `McIgnorableNamespaceDeclarator` gains `String getMcIgnorable()` for that, a
   default method returning null so an implementor outside docx4j keeps compiling; the two runtime
   mappers and the two generic ones override it, the relationships-part mappers keep the default.
   Restoring the previous value rather than clearing to null keeps nesting safe: the canonicalize
   path of `marshaltoString` calls `marshaltoW3CDomDocument` inside its own set.
3. `JaxbXmlPart.marshal(Node, mapper)` and `marshal(OutputStream)` move their clearing into a
   `finally`, so a marshal that throws leaves nothing behind either, and the Node path sets null
   before `setMceIgnorable`, so a part whose root has no Ignorable declares nothing borrowed (the
   subclass overrides still set their own value). `FlatOpcXmlCreator`'s two methods
   restore the same way.
4. `TextsPhase2Test.snapshot` stops stripping declarations, so REQ-204 compares every part's
   declarations too. CR-046's "not fixed here" line names this CR.

Nothing changes in what `Save` writes: the OutputStream path already set the value unconditionally and
still does. What changes is `XmlUtils` output: fragments marshalled after a document lose declarations
they never should have had, and a non-`Document` part root marshalled through `XmlUtils` (so
`getXML()`) gains the declarations its Ignorable and Choice namespaces require.

## 5. The phase

**Build:** §4, one commit.

**Tests** (docx4j-core-tests, `org.docx4j.jaxb.McIgnorableMarshalStateTest`), each stated so that it
fails on a41de133b:
- A `wml.Document` with `mc:Ignorable="w14 w15"` marshalled through `marshaltoString`, then a bare
  `w:p` through `marshaltoString`: the paragraph's start tag declares neither `w14` nor `w15`. The same
  with `docx4j.jaxb.marshal.canonicalize` true (the nested call), and the same through
  `marshaltoW3CDomDocument` and `marshaltoInputStream`.
- A `Hdr` root with `mc:Ignorable="w14"` and no `w14` content, marshalled through `marshaltoString` on
  a thread that has marshalled nothing: its start tag declares `xmlns:w14`. The same for `CTSettings`
  and `CTFootnotes`.
- A `HeaderPart` whose `addMcChoiceNamespace("wps")` was called (as `Docx4jUnmarshallerListener` does
  for a kept Choice): `getXML()` declares `wps`.
- After a Flat OPC export of a package, a bare `w:p` through `marshaltoString` declares nothing.
- A bare `w:p` marshalled while the mapper holds another value declares none of it, and the value is
  there again afterwards (the contract of §4.1's "sets null for its own duration").
- The mapper's value after each of these is what it was before (null on a fresh thread).

Existing tests that pin the part path and must stay green: `W15Test` (settings' `w15` declared on the
root, with and without canonicalization), `IgnorablePrefixesDeclaredTest` (CR-024, eight xlsx
fixtures re-saved), `CommentsExtensiblePartTest`, `SettingsDocIdOrderTest`, `MarkupCompatibilityTest`
and `McSelectionTest` (CR-046's deep copies match Requires by the prefix table, not by the mapper's
state), the docx4j-digsig suite and its goldens (a golden cut with stale declarations would change;
any change is examined and the golden re-cut with the reason recorded here). core-tests runs the
reference implementation; the new test is also run once by hand with the MOXy selector on the
classpath, as CR-048's were, and the result recorded here.

**No Office check**: no saved byte changes (§4). No fidelity gate: nothing in layout is touched.

**Hand-offs**, with the commit hash:
- docx4j-mcp: `TemplateInspector`'s part XML changes (no borrowed declarations on fragments; a header
  part's own Ignorable and Choice prefixes declared). Its session checks any fixture that pins that
  output.
- docx4j-core-ts and docx4j-python: a question, not a requirement. Does their marshal path keep the
  Ignorable prefixes to declare in state shared between marshals (a module-level or thread-level
  value), and does a fragment marshalled after a document declare the document's prefixes? If so the
  same contract applies; if their declarations come from the root object alone, nothing to do. The
  REQ-204 snapshot with declarations is the oracle either way.
- docx4j-portfolio: this CR's entry.

**Hand-offs sent 19 October 2026 with ba40c53f4:**

| Receiver | Session | Answer |
|---|---|---|
| docx4j-mcp | docx4j-mcp-a1 | nothing changes. Its one piece of part XML, describe_template's skeleton_xml, is a custom XML part's DOM through w3CDomNodeToString, no marshal; no fixture pins declarations. Confirmed by a run against the snapshot holding this change: 33 tests pass, skeleton_xml identical to 17.1.0's for its three templates after the warm-up marshal, the twelve goldens pass |
| docx4j-python | docx4j-python-cd | no shared state: each call renders from a module-constant prefix table and its own tree's mc:Ignorable, then lxml's namespace cleanup. Measured on core-ts check28's 28a-changes.docx: a paragraph after the document declares w and w14 only; styles.xml its own nine prefixes. Its own gap: a Choice prefix only Requires names is dropped by the cleanup (its CR-011, Q11), waiting on Jason |
| docx4j-core-ts | docx4j-core-ts-5b | no shared state: each marshal takes a fresh prefix table and its own derived context, and mc:Ignorable comes from the root being marshalled. But the question found a defect there: since its CR-010 phase 4 (3228fef, unreleased) keeps every mc:AlternateContent, the save's cleanup drops a declaration that only a Choice's Requires names (waterfall.docx's cx1, content in cx). Measured over 483 packages: 18 of 297 Requires prefixes left undeclared in 11 (cx1 11, x14 4, a14, sle15, tsle), none under its resolving load. The fix goes to the objects session: a Requires prefix counts as used. What Office does with such a file is not checked. Python has the same gap (its Q11) |
| docx4j-portfolio | docx4j-portfolio-9d | in progress 77356f7, done 7bbd56d |

**Implemented 19 October 2026** (docx4j-25, on top of 3dcbb3764), as §4 has it, with the two
amendments above (a root with neither sets null for its own marshal; the nested canonicalize call gets
the Choice namespaces):
- `XmlUtils.setMcIgnorable` reads `NamespacePrefixMapperUtils.ignorableOf(o)` and appends the Choice
  namespaces; each of the five methods takes the mapper's value first and puts it back in a `finally`.
- `NamespacePrefixMapperUtils.ignorableOf` is the moved method (its per-class cache with it);
  `JaxbXmlPart.ignorableOf` delegates to it.
- `McIgnorableNamespaceDeclarator.getMcIgnorable()` is a default method returning null. Three mappers
  keep a value and override it: the RI's and MOXy's (their thread-locals) and the generic
  `NamespacePrefixMapper` (a field). The generic relationships mapper keeps the default along with the
  RI's and MOXy's relationships mappers, so "the two generic ones" of §4.2 is one in the code.
- `JaxbXmlPart.marshal(Node, mapper)` starts from null, then `setMceIgnorable`; it and
  `marshal(OutputStream, mapper)` put the previous value back in a `finally` (they cleared it to null
  after a successful marshal). The OutputStream path's canonicalize branch marshals through
  `XmlUtils.marshaltoW3CDomDocument`, which now sets the root's own value and restores it.
- `FlatOpcXmlCreator.marshalBytes` and its deprecated `marshaltoW3CDomDocument` restore the same way.
- `TextsPhase2Test.snapshot` compares every part's declarations again (D4), and passes; CR-046's
  "not fixed here" line names this CR.

**What the tests had to observe.** The reference implementation declares every namespace of the main
context (`Context.jc`) on a root it marshals, `w14` and `w15` among them, so a bare `w:p` shows no
borrowed declaration either way: the leak shows on a root of another context, and the mirror defect in
the canonicalize path, which trims each unused namespace except those the mapper's value names.
`McIgnorableMarshalStateTest`'s six cases (§5's five and the nested one) therefore use a relationships
part's `Relationships` (`Context.jcRelationships`) for "borrows nothing", and canonicalization for "its
own Ignorable" (Hdr, CTSettings, CTFootnotes) and for `getXML()`'s Choice namespace; the Flat OPC case
writes the main document part through `createRawXmlPart` (a whole Flat OPC export leaves only its last
part's prefixes, which may be none), then a whole Flat OPC save. All six fail against a pre-CR-049
build (the phase 3 gate's jars, whose marshal code is a41de133b's; the variant run without the
`getMcIgnorable()` checks, which the old interface lacks) and pass on the new code, with the RI and,
run once by hand, with the MOXy selector (6 of 6; `org.eclipse.persistence.jaxb.JAXBContext` and
`moxy.NamespacePrefixMapper` confirmed on that class path).

**One test changed with it.** `AnonymizeXlsxCorpusTest.pivotsSlicersTimelinesAndTheirReferencesGo`
looked for `/slicer` and `timeslicer` in each drawing part's `getXML()` to find a slicer still drawn.
`getXML()` now declares the part's Choice namespaces, which the parts recorded when read (`sle15`, `tsle`,
whose URIs contain those strings), as a save always has; nothing is drawn. The check now strips
namespace declarations first. Whether the anonymiser should forget a Choice namespace once the content
requiring it is gone is a separate question (the declaration is harmless).

**Tests:** docx4j-core-tests 1750 run (11 skipped; 1744 before, the six new), all passing with the
anonymiser check corrected; W15Test, IgnorablePrefixesDeclaredTest, CommentsExtensiblePartTest,
SettingsDocIdOrderTest, MarkupCompatibilityTest and McSelectionTest among them; the text-extraction
suite 972 pass, 51 pending. docx4j-digsig 138 (1 skipped), its goldens unchanged; docx4j-markdown 166;
docx4j-export-fo-tests 853 (9 skipped); docx4j-diffx-tests 8 (1 skipped). No saved byte changed, so
no Office check and no fidelity gate (§5).

## 6. Decisions

Each with a recommendation; Jason decides.

- **D1. Restore the previous value, or clear to null.** Options: (a) save and restore in a `finally`;
  (b) clear to null in a `finally`, as `JaxbXmlPart` does today. Recommended: (a). It is the only
  choice under which a marshal nested in another (the canonicalize path; a marshaller listener that
  marshals) leaves the outer one intact, and it makes `JaxbXmlPart`'s clearing a special case of the
  same rule rather than a second rule. Cost: one getter on `McIgnorableNamespaceDeclarator` (§4.2).
- **D2. Apply the general rule to every root in `XmlUtils`, or only fix the leak.** Options: (a) both,
  as §4.1; (b) the leak only, leaving `getXML()` of non-`Document` parts as it is. Recommended: (a).
  Fixing the leak alone removes the accident that today makes `getXML()` on a header part Word-safe
  some of the time, so (b) would make docx4j-mcp's output worse, not better. (a) is the rule
  `JaxbXmlPart` has applied since CR-024 and `W15Test` has expected since 3.3.1; the cost is the
  `ignorableOf` move. A root whose Ignorable names a prefix the table lacks now warns `No mapping for
  prefix` from `XmlUtils` as it does from the part path; that is the same warning, now attributed to
  the right root.
- **D3. Where `ignorableOf` lives.** Options: `NamespacePrefixMapperUtils` (recommended: it is the
  mapper plumbing both callers use, in docx4j-core, no new class); or a new helper beside
  `McIgnorableNamespaceDeclarator`. `JaxbXmlPart.ignorableOf` stays as a protected delegate so
  subclasses that call it (none in docx4j today; the method is 17.2.0) keep compiling.
- **D4. The REQ-204 strip.** Options: remove it in this commit (recommended; it exists only for this
  defect, and the snapshot then pins the declarations of every part of the probe under every
  option and view), or leave CR-046's test alone. If a declaration differs between before and after
  an extraction under the restored comparison, that is a new finding for CR-046, not a reason to keep
  the strip.

## 7. Risks

- **A caller that relied on the leak.** The only one found is `getXML()` on non-`Document` parts, which
  D2 fixes properly. A consumer outside docx4j that marshalled a document and then fragments, expecting
  the fragments to carry the document's declarations, loses them; such a consumer was already broken on
  a fresh thread. Noted in CHANGELOG.
- **MOXy.** The MOXy mapper's `getPreDeclaredNamespaceUris` path is the same code, but MOXy's handling
  of a pre-declared namespace that the content also declares was measured only on the RI for CR-024.
  The hand run with the MOXy selector (§5) covers it.
- **The digsig goldens.** If one was cut with borrowed declarations it changes; §5 says what to do.
- **`ignorableOf` on roots with a `getIgnorable()` that is not `mc:Ignorable`.** The reflective rule
  takes any `String getIgnorable()`. Every such getter in `org.docx4j.*` is XJC's binding of
  `mc:Ignorable` (checked for CR-024); a hand-written class with one would be declared for, which is
  what the name asks.

## 8. Sources

- `docx4j-core/src/main/java/org/docx4j/XmlUtils.java` (setMcIgnorable and its five callers);
  `openpackaging/parts/JaxbXmlPart.java` (marshal paths, setMceIgnorable, ignorableOf, getXML);
  `jaxb/NamespacePrefixMapperUtils.java` (getPreDeclaredNamespaceUris, getPreDeclaredNamespaceUris2);
  `convert/out/flatOpcXml/FlatOpcXmlCreator.java`; the RI and MOXy `NamespacePrefixMapper`s.
- CR-024 phase 1 (the general rule, `getPreDeclaredNamespaceUris2`, `IgnorablePrefixesDeclaredTest`).
- CR-046's phase 4 record, "Findings outside CR-046, not fixed here"; `TextsPhase2Test.snapshot`.
- ECMA-376 Part 3 (Markup Compatibility and Extensibility) §10.1.1; Namespaces in XML 1.0 §6.1.
