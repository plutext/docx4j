# OpenDoPE hyperlink handling options

How to make a hyperlink data-driven — the displayed text (the `w:t` on the
document surface), the target (normally a relationship, via `@r:id`), or
both.  Assumes docx4j **17.0.4 or later**: everything below works on all
three binding traversers, because since 17.0.4 they share one text-binding
implementation (the `ValueInserterPlainText` / hyperlink-resolver pathway;
see CR-001).  On 8.3.x the same extension points exist but are only wired
to the XSLT traverser (which is that line's default).

## Option 0: conditional content controls (the baseline)

Author each hyperlink variant, wrap each in a conditional content control,
and let the data pick one.

Right when the links form a **small, fixed, editorially distinct set** (a
per-locale support page, say): the template author sees and styles each
variant, and nothing else is needed.  Wrong when the URL comes *from the
data*: conditions can only select among pre-authored variants, so an
open-ended URL set is not merely clumsy — it is impossible.  For that, read
on.

## Option 1: bind the URL as plain text (built in)

Put the URL in the instance XML, bind an ordinary text content control to
it, and enable hyperlink conversion before binding:

```java
BindingHandler.setHyperlinkStyle("Hyperlink");   // the Word style name
Docx4J.bind(pkg, xmlData, Docx4J.FLAG_NONE);
```

A bound value containing `http://`, `https://` or `mailto:` becomes a live
`w:hyperlink`: the relationship is created at bind time, and the link text
is a run styled with the given character style
(`BindingHyperlinkResolver.generateHyperlink`).  Text before the URL in the
same value is emitted as plain text.

Limitation: the **display text is the URL** — `generateHyperlink(relId, url)`
receives only those two strings.

## Option 2: display text ≠ URL, via a resolver subclass

`BindingHandler.setHyperlinkResolver(...)` accepts a subclass of
`BindingHyperlinkResolver`.  Adopt a convention such as `display|url` in the
bound value and override `getIndexOfURL` / `generateHyperlink` to split it.

To source the two halves from **sibling elements** in the instance XML, do
the join in the XPath rather than the Java — od:xpath is an arbitrary XPath
string expression:

```
concat(../displayText, '|', ../url)
```

The resolver then stays a dumb splitter.  One trade-off: `concat(...)` is
not a node path, so Word's own `w:dataBinding` cannot mirror it — this is an
od-only extended bind (the same territory as position predicates like
`last()`).  For hyperlinks that costs nothing in practice, since Word would
not live-update a control whose content has been replaced by a
`w:hyperlink` anyway.

## Option 3: a custom ValueInserterPlainText (the rich extension point)

Where the mapping logic is app-specific — or you'd rather not encode `|`
conventions in the data — step up one layer.  The pluggable inserter
(`BindingHandler.setValueInserterPlainText`) has a much richer contract:

```java
DocumentFragment toOpenXml(Xpaths.Xpath.DataBinding dataBinding, RPr rPr,
        boolean multiLine, BookmarkCounter bookmarkCounter, String result,
        JaxbXmlPart sourcePart);
```

`dataBinding` carries the bound XPath and the `storeItemID`;
`sourcePart.getPackage()` reaches the `CustomXmlDataStorageParts` — so the
inserter can derive a sibling path from the binding's own XPath, evaluate
it against the right storage part, and emit a `w:hyperlink` with whatever
display text it found (reuse
`BindingHandler.getHyperlinkResolver().generateHyperlink(...)` for the
plumbing, or build the run yourself).
`ValueInserterPlainTextForOpenAPI3` is an existing example of an inserter
with its own interpretation of the bound value.

## Option 4: escaped-XHTML bind (most flexible, declaratively)

Give the content control `od:ContentType=application/xhtml+xml` and put the
whole link in the data:

```xml
<a href="https://example.org/help">whatever display text</a>
```

docx4j-ImportXHTML converts it to a live hyperlink with independently
data-driven text and target — no resolver subclass, no conventions;
the template author controls everything from the data side.  Requires
docx4j-ImportXHTML on the classpath.  The importer inherits the hyperlink
style set via `BindingHandler.setHyperlinkStyle`.

## Option 5: a HYPERLINK field instead of an r:id hyperlink

`<w:fldSimple w:instr='HYPERLINK "https://…"'>` keeps the URL in *document
text* rather than the relationships part, so both the URL (field
instruction) and the display text (field result) are reachable by surface
mechanisms — `VariableReplace`, or a bound sdt around the field result plus
a trivial instruction rewrite.  No relationships surgery; Word renders the
field live; docx4j's HTML/PDF output resolves HYPERLINK fields.  Niche, but
a good fit for templates already built around field machinery.

## Option 6: post-bind relationship surgery

Author one hyperlink; bind its display text with a run-level sdt *inside*
it (`w:hyperlink/w:sdt` is schema-valid); after `bind()`, retarget the
relationship from host code:

```java
part.getRelationshipsPart().getRelationshipByID(rid).setTarget(url);
```

Caveats: the URL half lives in host code rather than the template model;
and while sdt-inside-hyperlink is legal, it is a shape Word's own authoring
rarely produces — test your full pipeline (binding, then any conversion)
with it before committing.

## Cross-cutting notes

- **Static configuration**: `setHyperlinkStyle`, `setHyperlinkResolver` and
  `setValueInserterPlainText` are static on `BindingHandler` — JVM-global.
  Fine for a single-tenant service; two concurrent binds wanting different
  resolvers will fight.
- **Traverser parity** (17.0.4+): options 1–4 behave identically on
  `BindingTraverserNonXSLT` (the default), `BindingTraverserStAX` and
  `BindingTraverserXSLT`, because all three route text and XHTML binds
  through the same shared implementation.

## Choosing

| Situation | Option |
|---|---|
| Small fixed set of editorially distinct links | 0 (conditions) |
| URL from data, display text = URL acceptable | 1 |
| URL and display text from data, simple convention OK | 2 |
| App-specific mapping logic, or fixed data shape | 3 |
| Both halves data-driven, ImportXHTML already present | 4 |
| Template built around fields | 5 |
| One link, host code owns the URL | 6 |
