# CR-054: a binding's own prefix mappings lose to docx4j's built-in prefix table

Status: PROPOSED 2026-10-22, drafted by Claude Fable 5.1 (session docx4j-25) at Jason's request ("make the
CR to fix the seventh defect"), from a defect found the same day building the drop-down binding fix
(CHANGELOG 17.3.4) against docx4j-portfolio's bind-typed probe. One phase (§5); decisions in §6.
ACCEPTED 2026-10-23 (Jason: "CR-054 D1 to D3 recs accepted"); DONE 2026-10-23 (record in §6a), for 17.3.5.

## 0. Summary

Every `w:dataBinding` (and `w15:dataBinding`, and every OpenDoPE `od:xpath` entry) carries its own prefix
mappings, `w:prefixMappings="xmlns:ns0='urn:x' "`, and its XPath is meant to be read with them. docx4j's
XPath namespace context for the custom XML parts, `org.docx4j.jaxb.NamespacePrefixMappings`, answers its
own built-in table of some two hundred prefixes first and the registered mappings last, so a document
that binds with a prefix in that table (`p`, `w`, `r`, `a`, `pkg`, `prop`, `w14`, `w15`, `x`, `s`, ...)
has its XPath evaluated against the wrong namespace, which selects nothing. The bound control then shows
its placeholder, where Word shows the value. The registered mappings also live in one static map shared
by every part and every document in the JVM.

Word's own templates escape it: Word names a bound namespace `ns0`, `ns1`, ... which the table does not
hold. A template written by anything else, or by hand, may use any prefix. The text model and the
inspector are not affected: they build a context from the binding's mappings alone
(`BoundValues.Mappings`, `InspectWalk.loadBindings`).

The fix: a context per evaluation, built from the binding's mappings, which win, with the built-in table
behind them for the prefixes docx4j's own expressions use (`xs` in the XPath 2 casts, for one) and that
templates have relied on without declaring. The shared static map stays for compatibility, deprecated.

## 1. The defect (measured 2026-10-22 on VERSION_17_3_4 at f62314d3b)

`BindingHandler.xpathGetString(pkg, parts, storeItemId, "/p:root[1]/p:dd[1]", "xmlns:p='urn:probe:bind'")`
on docx4j-portfolio's `bind-typed.docx` (one custom XML part, `<root xmlns="urn:probe:bind">...<dd>v2</dd>`)
returns `""`. With the prefix rewritten to `ns0` throughout, it returns `v2`. Bound through either
traverser, the four typed controls in that file all came out as their placeholders ("Click here to enter
text.") with `w:showingPlcHdr`; Word 365 shows their values (the portfolio's Word measurements, 22 October).
The fixture `OpenDoPE/typed-controls.docx` in docx4j-core-tests is that probe with the rewrite; the probe
itself, with `p`, is on the share at `Office 2016/bind-fun/bind-typed.docx`.

The cause, by reading `NamespacePrefixMappings`:

- `getNamespaceURI(prefix)` tests `s`, then `getNamespaceURIStatic` tests `x`, `w`, `r`, `pkg`, `p`,
  `prop`, and some two hundred more in a chain of `if (prefix.equals(...))`, and only at the end looks
  in `namespaces`, the map `registerPrefixMappings` fills. A prefix in the table never reaches the map.
- `namespaces` is `private static`, and `registerPrefixMappings` is static: one map for the JVM. The
  parts each hold their own `NamespacePrefixMappings` instance (`XmlPart.getNamespaceContext()`,
  `JaxbCustomXmlDataStoragePart`, `DocPropsCorePart`, `DocPropsExtendedPart`), but the instance holds
  no state of its own, so every registration is global, and the last document to register a prefix sets
  it for every other.
- `registerPrefixMappings` tokenises on whitespace and takes `token.substring(6, pos)` for the prefix
  and the text between `='` and the last `'` for the URI: single quotes only (Word writes single quotes;
  the schema's `ST_String` allows either), and a mapping without a space before the next is not split.

The same table is right for what it was built for, the prefixes docx4j itself writes and reads
(`NamespacePrefixMapperUtils`, `XmlUtils.prepareJAXBResult`); it is the lookup order for a document's
declared mappings that is wrong.

## 2. What it affects, and what it does not

Affected: every XPath evaluated through a part's `xpathGetString`, `cachedXPathGetString`,
`cachedXPathGetBoolean` and `xpathGetNodes` with a `prefixMappings` argument whose prefix is in the
table. That is the binding generator (both traversers, through `BindingTraverserXSLT.xpathGenerateRuns`
and the date, checkbox, picture and XHTML paths), OpenDoPE conditions (`Xpathref`), repeats
(`OpenDoPEHandler.processRepeat` counting the node set) and `UpdateXmlFromDocumentSurface`. A collision
with a Word prefix (`w`, `w14`, `w15`) is unlikely in a binding; `p`, `a`, `x`, `s`, `r` are ordinary
choices for a hand-written or generated template and the collision is silent, the XPath yielding
nothing.

Also affected, by the shared map: two documents bound in one JVM with the same prefix for different
namespaces; the second's registration wins for both once the first's cache (`XmlPart.setupCache`,
which re-registers only when the mappings string changes) stops re-registering.

Not affected: the text model (`Texts.of`, `BoundValues.Mappings`) and the inspector
(`InspectWalk.loadBindings`), each with a context from the binding's own mappings, measured on
`bind-plain.docx` with `p` (the bound value shows). Not affected: docx4j's own XPaths over its own parts
(the table was built for them). Saved files are unaffected except through the binding result.

## 3. What the specifications say

ECMA-376 `w:dataBinding/@w:prefixMappings` (17.5.2.24): "the set of prefix mappings which shall be used
to interpret the XPath expression", in the form `xmlns:prefix='uri'`, whitespace-delimited. The
expression is read with those mappings and no others. OpenDoPE v3's `od:xpath` entries carry the same
attribute with the same meaning (REQ-3xx, the xpaths part). Word follows it; so do the text model and
the inspector here (WD REQ-509's bound view).

## 4. Proposal

One phase. A context per evaluation, the binding's mappings first:

- `NamespacePrefixMappings` gains a constructor `NamespacePrefixMappings(String prefixMappings)` (and a
  static factory) holding the parsed mappings in an instance map; `getNamespaceURI` consults the
  instance map first, then the built-in table, then the shared static map. The parser becomes the one
  the text model uses (`BoundValues.Mappings`'s pattern: either quote, no dependence on spacing), shared.
- `XmlPart` (and `JaxbCustomXmlDataStoragePart`, `DocPropsCorePart`, `DocPropsExtendedPart`) evaluate
  with a context built from the call's `prefixMappings`: `xPath.setNamespaceContext(context)` inside the
  existing `synchronized(xPath)`, and `cachedXPathAPI.eval(doc, xpath, context)`, whose resolver is a
  per-call argument already. `setupCache`'s re-registration logic goes; the cache object itself stays.
- `registerPrefixMappings` stays, deprecated, still filling the shared map (third parties call it),
  which the per-call context falls back to.
- `BoundValues.Mappings` and `InspectWalk.loadBindings` are left as they are (they are right), or made to
  share the new parser; the latter is a tidy-up, not required.

Why the table stays behind the mappings rather than going: templates exist whose XPaths use `xs:` casts
or docx4j-documented prefixes without declaring them, and docx4j's own expressions over its own parts
use the table; with the mappings first, a declared prefix always wins and nothing declared is lost.

Why not simply consult the shared map first: one document's `xmlns:w='urn:other'` would then redefine
`w` for every other document in the JVM, including docx4j's own XPaths over WordprocessingML parts, for
the rest of the process. The per-evaluation context is the whole fix.

## 5. The phase

1. `NamespacePrefixMappings`: instance mappings, the shared parser, the lookup order; `registerPrefixMappings`
   deprecated with a pointer.
2. The four parts: per-call contexts; `setupCache` simplified.
3. Tests, in docx4j-core-tests:
   - `XmlPart.xpathGetString` with `xmlns:p='urn:probe:bind'` on a part whose root is in that namespace
     returns the value (today `""`); the same with `xmlns:w='urn:probe:bind'`; a mapping in double quotes;
     two mappings with no space between.
   - Both traversers bind docx4j-portfolio's `bind-typed.docx` as it is, prefix `p` (added as the fixture
     `OpenDoPE/typed-controls-p-prefix.docx`), with the same assertions `TypedControlBindingTest` makes
     on the `ns0` fixture.
   - Two parts bound in one test with `p` mapped to different namespaces, each resolving its own.
   - An expression using `xs:` with no mapping for it still evaluates (the table behind).
   - The OpenDoPE datastorage suite and the text-extraction suite unchanged.
4. CHANGELOG line; this CR's Status; the registry (docx4j/CR-054) through the portfolio session.
5. Hand-offs: docx4j-core-ts and docx4j-python, whose binders evaluate the same XPaths, asked whether a
   declared prefix wins there (it should); docx4j-portfolio, for its paper's §12a.6 list of the generator's
   departures from Word, where this one is already recorded from the measurement.

Acceptance: the tests above green; `typed-controls-p-prefix.docx` and `typed-controls.docx` bind alike.

**Hand-offs answered (2026-10-22, the same day):** neither port has the defect. core-ts evaluates a
binding's XPath with its own `w:prefixMappings` alone (`CustomXmlPart.selectSingleNode`, `mappingsFor`;
no built-in table; with the attribute absent it falls back to the data part's own root declarations),
probed on its main a85421d with `p` mapped to `urn:mine` on a part that itself binds `p` to
PresentationML: the `urn:mine` node binds. docx4j-python's `model/customxml/xpath.py` `select()` likewise
uses the binding's mappings and nothing else (0e05d9d), falling back to the part's declared prefixes when
the attribute is absent. Both already show a drop-down's or combo box's item text and write `w:lastValue`
(core-ts `bindings.mts`, python `bindings.py`), and leave an empty `w15:repeatingSection` as it is.
core-ts's date binding has the zone defect docx4j's had, in its own form (`dateOf()` strips a trailing
`Z` and parses with `new Date()`, so the shown day moves with the machine's zone); reported to Jason by
that session, not fixed yet. python reads the nominal day.

## 6. Decisions

- D1 (proposed): the lookup order is binding's mappings, then the built-in table, then the shared map.
  Alternative: mappings then shared map then table; rejected, since a registration from another document
  would then shadow the table for this one.
- D2 (proposed): the shared static map and `registerPrefixMappings` stay, deprecated, rather than being
  removed in 17.3.4; removal is a 17.4.0 question.
- D3 (proposed): the parser accepts either quote and any whitespace (the text model's regex), and so
  reads what the schema allows, not only what Word writes.

## 6a. Record (2026-10-23, docx4j-25; D1 to D3 accepted by Jason the same day, as recommended)

- `NamespacePrefixMappings`: an instance map `declared`, filled by the new constructor
  `NamespacePrefixMappings(String prefixMappings)` and the factory `of(String)`; the no-arg constructor
  keeps the table alone. `getNamespaceURI` consults `declared`, then the `s` special case, then the
  table (`getNamespaceURIStatic`, whose tail is the shared map); `getPrefix` answers a declared prefix for
  its URI before `getPreferredPrefixStatic` (so a template that declares `xmlns:xs` gets its `xs:boolean`
  cast with its own prefix). `parse(String)` is the one parser (D3: the text model's regex, either quote,
  any whitespace, `LinkedHashMap` in order); `BoundValues.Mappings` now calls it. `registerPrefixMappings`
  is `@Deprecated`, still filling the shared map, now a `ConcurrentHashMap` (D2); `StrTokenizer` gone.
- The four parts (`XmlPart`, `JaxbCustomXmlDataStoragePart`, `DocPropsCorePart`, `DocPropsExtendedPart`):
  the per-instance `nsContext` and `getNamespaceContext()` are gone; each evaluation does
  `xPath.setNamespaceContext(NamespacePrefixMappings.of(prefixMappings))` inside the existing
  `synchronized(xPath)` (the diagnostic NODESET re-evaluation in `XmlPart.xpathGetString` moved inside
  that block, where it belongs), `cachedXPathAPI.eval(doc, xpath, NamespacePrefixMappings.of(prefixMappings))`
  for the cached path, and `XmlUtils.xpath(doc, xpath, context)` for the node lists. `XmlPart.setupCache`
  now only makes the `CachedXPathAPI`; `cachedPrefixMappings` is gone.
- Tests: `PrefixMappingsWinTest` (core-tests, `openpackaging.parts`: `p` and `w` as the binding's prefix
  on all four evaluation paths; double quotes and no whitespace between mappings; two parts with `p`
  meaning two namespaces, neither leaking; an undeclared table prefix still resolving; the registered map
  consulted last and beaten by a declared mapping; the parser's cases). `TypedControlBindingTest` gains the
  fixture `OpenDoPE/typed-controls-p-prefix.docx` (the `ns0` fixture with `ns0` rewritten to `p` in
  `word/document.xml`, 12 tokens; the probe as the portfolio's bind-typed.docx has it) and binds it through
  both traversers with the same assertions as the `ns0` fixture.
- Not done: removing the shared map (D2: a 17.4.0 question); `InspectWalk.loadBindings` keeps its own
  `BoundValues.Mappings` context (right already; now parsed by the shared parser).

## 7. Risks

- A template that relied on a built-in prefix meaning docx4j's namespace while declaring the same prefix
  for another: its XPath changes meaning, to the one the template declares. That is the fix; the risk is
  a template that was accidentally working. Judged acceptable: the declared meaning is the specified one.
- Performance: a context object per evaluation. The parse is a short regex over a short string, once per
  call; `XmlPart.setupCache` already re-registered on every change of mappings. Measure on the binding
  benchmark if one exists; otherwise note it.
- Thread safety improves: the per-call context removes the only writer of shared state during
  evaluation; `xPath.setNamespaceContext` stays under the existing `synchronized(xPath)`.

## 8. Sources

- `docx4j-core/src/main/java/org/docx4j/jaxb/NamespacePrefixMappings.java` (`getNamespaceURI`,
  `getNamespaceURIStatic`, `registerPrefixMappings`, the static `namespaces` map).
- `docx4j-core/src/main/java/org/docx4j/openpackaging/parts/XmlPart.java` (`xpathGetString`,
  `setupCache`, `cachedXPathGetString`, `cachedXPathGetBoolean`, `xpathGetNodes`).
- `docx4j-core/src/main/java/org/docx4j/model/text/BoundValues.java` (`Mappings`), the per-binding
  context already in the tree.
- docx4j-portfolio's bind-fun probes and Word 365 measurements (22 October 2026); its text-extraction
  paper §12a.6.
- ECMA-376 Part 1, 17.5.2.24 (`prefixMappings`); OpenDoPE v3 WD, the xpaths part.
