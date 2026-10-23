# CR-056: HTML, PDF and Markdown output: no script-scheme links, no style ID in the stylesheet unescaped

Status: PROPOSED 2026-10-23, drafted by Claude Fable 5.1 (session docx4j-25) at Jason's request ("write the CR
for the fix"), after Jason pointed at apache/poi#1455 and asked whether docx4j has the same ("check whether there
are any similar vulnerabilities we might have anywhere we handle html"). Measured the same day on the released
17.3.4 jars (§1). ACCEPTED 2026-10-23 (Jason: "D1 to D8 agree with recommendations; D9: no advisory, CHANGELOG
only; D10: no backport"); decisions in §6; phases in §5, to start on Jason's word.

## 0. Summary

Two ways a crafted docx makes docx4j's HTML output run script in the page that serves it (CWE-79, stored XSS):

1. **A link's target is written as it is.** A `w:hyperlink` whose relationship target is
   `javascript:alert(1)`, or a HYPERLINK field with that target, becomes `<a href="javascript:alert(1)">`.
   Clicking it runs the script on the serving application's origin. This is the defect apache/poi#1455 fixed in
   POI's `WordToHtmlConverter` ("the serializer escapes the attribute value, so the problem is the scheme, not
   markup breakout"). docx4j's PDF carries the same target as a `/URI` action, and its Markdown as the link
   destination.
2. **A style ID is written into the stylesheet as it is.** A paragraph or table style whose ID is
   `x</style><script>alert(1)</script>` puts that text into the `<style>` element, where a browser ends the
   style at `</style>` and runs the script when the page loads, no click needed. POI's fix does not cover this.

The fix: one scheme allowlist applied wherever an output writes a link target (HTML and FO through
`HyperlinkUtil`, a picture's click target, the Markdown destination, docx4j-tika's `href`), keeping the link's
text and dropping its target otherwise, with the list extendable by a property; and one CSS escaping function
applied wherever a style ID becomes a selector, so the stylesheet and the `class` attributes still agree. Plus
the one content-control tag handler that builds HTML by string concatenation, rebuilt on DOM nodes. Tests from
the probes. An advisory and a backport are Jason's decisions (D9, D10).

Word itself does neither: a `javascript:` HYPERLINK does nothing when clicked in Word, and a style ID is never
rendered. So the document behaves one way in Word and another in docx4j's output, the class of divergence the
text-model work has been closing.

## 1. Measured (2026-10-23, the 17.3.4 bundle jars)

Probes `XssProbe.java` and `Probe3.java` build a docx with docx4j, then export it through `Docx4J.toHTML`
(the visitor pathway, `FLAG_EXPORT_PREFER_NONXSL`, and the XSLT pathway), `Docx4J.toMarkdown` and
`Docx4J.toFO`. Probes and outputs: `~/claude-scratch-archive/docx4j/html-output-xss-probes-2026-10-23`.

| Payload, where an author puts it | HTML, visitor | HTML, XSLT | Markdown | PDF |
|---|---|---|---|---|
| `w:hyperlink`, External relationship target `javascript:alert('H1')` | `<a href="javascript:alert('H1')">` | same | `[click me](<javascript:alert('H1')>)` | `/URI (javascript:alert('H1')` action |
| HYPERLINK field, `" HYPERLINK "javascript:alert('H2')" "` | same | same | same | same |
| paragraph style ID `x</style><script>alert('H4')</script>`, used by a paragraph | stylesheet line 7: `.x</style><script>alert('H4')</script> {display:block;…}` | same | n/a | n/a |
| table style ID `t</style><script>…`, used by a table | `table.t</style><script>…` and the derived `.Normal-t</style><script>…-BR` | same | n/a | n/a |
| character style, font name `Arial</style><script>…` | only an inline `style` attribute, escaped | same | n/a | n/a |
| bookmark name `bm"><script>…` | `name` attribute, escaped | same | n/a | n/a |
| content control, tag `@class=collapse`, alias `x</a><script>…</script><a>` | nothing: `TagClass` is not registered | same | n/a | n/a |

The `class` attributes carrying a hostile style ID are DOM-escaped (`class="x&lt;/style&gt;…"`), so the
stylesheet text is the only sink for that payload. The PDF's `/URI` action is not run by PDF viewers, which
execute script only through `/JavaScript` actions; it is still a target docx4j should not write.

The inspector (CR-050) already reports both link cases as `field.external` and `relationship.external`
in the `active` group, with the target as the excerpt; it has no view of style IDs.

## 2. Where the sinks are

- **Links.** `org.docx4j.convert.out.common.writer.HyperlinkUtil`: `toHtmlNode` sets `href` to
  `model.getExternalTarget()`; `toFoNode` sets `external-destination` to `url(` + target + `)`. The model
  (`AbstractHyperlinkWriterModel`) takes the target from the relationship (`w:hyperlink/@r:id`) or, for a
  HYPERLINK field, from the field's instruction. Both HTML pathways and the FO pathway use this one class
  (the XSLT's `w:hyperlink` template calls `XsltCommonFunctions.toNode`, which reaches it).
  `org.docx4j.model.images.AbstractWordXmlPicture.createHtmlImageElement` sets `href` to `hlinkRef`, a
  picture's `a:hlinkClick` target, the same way. `org.docx4j.markdown.WmlToMarkdown` makes a commonmark
  `Link` with `TextRun.getLink()` as its destination. docx4j-tika's `Docx4jWordParser.run` (branch
  `docx4j-tika`, CR-055 phase 0) starts an `a` element with `href` = `TextRun.getLink()`.
- **Style IDs.** `org.docx4j.convert.out.html.HtmlCssHelper.createCssForStyles` appends `"table." +
  styleId`, `"." + styleId` and `"span." + styleId` as selectors, and `XsltHTMLFunctions` writes the derived
  names (`<style>-<tbl>-BR` and the like). The stylesheet string goes into `<style>` through
  `HTMLConversionContext.DEFAULT_STYLE_ELEMENT_HANDLER` as a DOM comment node, which the serializer writes
  verbatim; the HTML parser reads a `<style>` element's content as raw text up to the first `</style`, comment
  or not.
- **String-built HTML.** `org.docx4j.convert.out.html.TagClass.createDiv` concatenates the content
  control's alias into `<a onmousedown=…>Toggle:` + alias + `</a>` and parses it with
  `XmlUtils.appendXmlFragment`. The handler is registered only by an application calling
  `SdtWriter.registerTagHandler`, so this is latent, and the only such construction in the exporters
  (`appendUserMarkup` and `userCSS`/`userScript` take developer-supplied strings, by design).

Checked and clean: font names (inline `style` attributes only, escaped; D5 adds a guard anyway), alt text,
bookmark names and `id`/`name`/`target` attributes (DOM-escaped), image `src` (data URIs, or an external
target in an `img`, where a script scheme does nothing), the metafile SVG (generated by Batik from the
metafile's records, not taken from the document), SVG image parts (not inlined; #699), chart SVG (generated),
the pptx SVG-in-HTML exporter (writes no links).

## 3. What the rule should be

**Links: an allowlist of schemes, as POI chose.** A denylist (`javascript`, `vbscript`, `data`) misses the
next scheme a browser treats as code, and a browser's own parsing is lenient: leading control characters and
spaces are ignored, and tabs and line breaks anywhere in the scheme are dropped, so `java\tscript:` is
`javascript:`. POI's `WordToHtmlUtils.isSafeHyperlink` reads the scheme that way and accepts: no scheme
(a relative path, a fragment, a UNC path), a Windows drive path (`C:\`), and `http`, `https`, `ftp`,
`mailto`, `file`, `news`, `tel`. docx4j's inspector already has the browser's reading of a `javascript:`
URI (`InspectPayloads.javascript`); the allowlist test generalises it to "the scheme, read as a browser
reads it, is in the set".

What to do with a refused target: keep the text, write no link (POI's choice). In HTML the content is
written without the `a`; in FO as `fo:inline` (what `toFoNode` already does for a link with no target); in
Markdown the text alone; in Tika's XHTML the characters alone. Logged once per document at WARN with the
address, and the inspector's existing `field.external` / `relationship.external` findings keep saying where.

**Style IDs: escape, do not replace.** CSS has an escaping syntax for identifiers (`CSS.escape` in CSSOM:
a non-identifier character becomes `\` + its hex code + a space), and a browser matches a `class` attribute
against the escaped selector by the decoded name. So the selector `.x\3c \/style\3e …` matches
`class="x</style>…"` exactly as written today: the `class` attributes need no change, no two IDs collide,
and a style ID with a space or a dot, which produces a broken selector today, starts working. The function is
applied to every selector docx4j writes from a document value, including the derived names.

**Defence in depth for the stylesheet.** Whatever the selectors, the stylesheet text must not be able to
end its own element: in the style element handler, `<` in the CSS text is written as `\3c ` (valid in CSS
identifiers and strings alike), so no value that reaches the stylesheet by a path this CR has not seen can
close it. The same for `userCSS`? No: it is the developer's.

## 4. Proposal

- `org.docx4j.convert.out.common.writer.LinkTargets` (name D2): `static boolean isSafe(String target)` and
  `static Set<String> schemes()`, the default set of §3, extended by the property
  `docx4j.convert.out.linkSchemes` (D3; a comma-separated list that is added to the default, so a deployment
  with `ms-word:`, `onenote:` or `notes:` links keeps them). The scheme is read as a browser reads it.
- `HyperlinkUtil.toHtmlNode` and `toFoNode`, `AbstractWordXmlPicture.createHtmlImageElement`,
  `WmlToMarkdown`'s link and docx4j-tika's `run` call it; a refused target gives the text without a link,
  and one WARN per document naming the first address.
- `org.docx4j.convert.out.html.CssIdentifiers.escape(String)` (D2): CSS.escape semantics; applied in
  `HtmlCssHelper.createCssForStyles` and wherever `XsltHTMLFunctions` composes a selector from a style ID;
  `HTMLConversionContext.DEFAULT_STYLE_ELEMENT_HANDLER` writes `<` as `\3c ` in the stylesheet text (D5).
- `TagClass.createDiv` builds its `p` and `a` with DOM calls (the alias as a text node).
- Tests in docx4j-core-tests from the probes, one per row of §1's table, each asserting the output contains
  no `javascript:` and no `<script`, plus the positive cases: `http`, `https`, `mailto`, a relative path, a
  fragment, a `file:` and a drive path keep their links; a style ID with a space gets a working selector;
  the property adds a scheme. The markdown and tika tests in their modules.
- The inspector (optional, D8): a `hyperlink.script` finding, or the scheme named in the existing
  `relationship.external` / `field.external` summary, so an agent sees "script" without parsing the target.
  A change to the inspection oracle is a text-extraction specification matter (docx4j-portfolio, WD), so it
  is a phase of its own, with the ports' hand-off.
- Documentation: CHANGELOG under 17.3.5 (and the advisory's text, D9); Getting Started's "docx to (X)HTML"
  section gains a paragraph on what the output refuses and the property.

Out of scope: docx4j-ImportXHTML, which turns an HTML chunk's `<a href="javascript:…">` into a `w:hyperlink`
with that target when it imports (Word keeps such a target too); the sink is this CR's. The MCP server, which
returns this HTML and Markdown. `userBodyTop`, `userBodyTail`, `userCSS`, `userScript`: developer-supplied.

## 5. Phases

| Phase | What | Size |
|---|---|---|
| 0 | The probes as red tests: core-tests (HTML both pathways, FO's `external-destination`), markdown, tika. **DONE 2026-10-23** (record in §9); the tika test goes with phase 3 on the `docx4j-tika` branch | 2 h |
| 1 | `LinkTargets` with the property; `HyperlinkUtil`, the picture link, `WmlToMarkdown`; tests green; CHANGELOG. **DONE 2026-10-23** (record in §10) | ½ day |
| 2 | `CssIdentifiers`, the selectors in `HtmlCssHelper` and `XsltHTMLFunctions`, the `\3c ` guard in the style element handler, `TagClass` on DOM nodes; tests green. **DONE 2026-10-23** (record in §11; two departures and D11 there) | ½ day |
| 3 | docx4j-tika's `href` (on the `docx4j-tika` branch, with CR-055 phase 1); Getting Started; the advisory and the backport as decided (D9, D10). **DONE 2026-10-23** (record in §12; D9 and D10 are "no", so nothing to do there) | ½ day |
| 4 | (optional, D8) the inspector's script-scheme finding, with the portfolio's WD change and the ports' hand-off. **Considered and DEFERRED 2026-10-23** (§6 under D10: the kind `hyperlink.script` defined in the WD now, implemented by the three inspectors together when wanted) | ½ day, with the portfolio |

## 6. Decisions (D1 to D8 accepted as recommended, 2026-10-23; D9 and D10 decided otherwise, below)

| # | Decision | Recommendation |
|---|---|---|
| D1 | Allowlist or denylist | Allowlist, read as a browser reads a scheme: no scheme, a drive path, `http`, `https`, `ftp`, `mailto`, `file`, `news`, `tel` (POI's set) |
| D2 | Where the rules live | `LinkTargets` beside `HyperlinkUtil` in docx4j-core, and `CssIdentifiers` in `convert.out.html`; markdown and tika depend on core already |
| D3 | Extending the schemes | the property `docx4j.convert.out.linkSchemes`, added to the default set; no way to remove a default |
| D4 | A refused target | the text, no link (HTML, FO, Markdown, Tika alike); one WARN per document with the first address |
| D5 | Style IDs | escape (CSS.escape semantics) in every selector; `class` attributes unchanged; `<` written as `\3c ` in the whole stylesheet as a second line |
| D6 | `TagClass` | rebuilt on DOM nodes, kept (an application may register it) |
| D7 | Versions | fix on VERSION_17_3_5; the `docx4j-tika` branch takes its part with CR-055 phase 1 |
| D8 | The inspector | optional phase 4: name the scheme in the existing findings' summary rather than a new kind, unless the WD prefers a kind |
| D9 | Advisory | recommended a GitHub security advisory (CWE-79). **Decided (Jason, 2026-10-23): no advisory; the CHANGELOG entry under 17.3.5 says what was fixed** |
| D10 | Backport | recommended the 11.5.x line too. **Decided (Jason, 2026-10-23): no backport; 17.3.5 only** |

**D8, phase 4: considered and deferred (Jason, 2026-10-23).** After phases 1 to 3 the inspector says this of a
script-scheme link: a `w:hyperlink` whose target is `javascript:alert(1)` is `relationship.external`, the kind of a
linked image, an attached template or a `C:\` path; a HYPERLINK field with that target is `field.external`, the kind
of INCLUDETEXT or DDE; a `javascript:` href inside an HTML chunk is `altchunk.script`, by the chunk scan, with or
without a reader (measured, §12). The target is the finding's excerpt and subject in every case. The gap is triage by
kind alone: a script link outside a chunk is not named, while the same link inside one is. Deferred because: the sink is
closed (phases 1 to 3 refuse the target in HTML, PDF, Markdown and tika, and the conversion logs a WARN naming the first
refused address); the inspector's basis is Word, where a `javascript:` hyperlink is inert (a `file:` or UNC link is more
active in Word than a `javascript:` one, so promoting the script scheme would describe a browser's behaviour, not the
document's); the target is already in the finding; and the cost lands mostly in the WD and the two ports (a WD revision,
an oracle re-baseline, two hand-offs) for a field no consumer has asked for. So that the three inspectors do not each
invent a kind when it is wanted, the WD takes an item now, proposed to docx4j-portfolio the same day and taken as WD 0.32
(docx4j-portfolio 55fab92; the registry has CR-056.4 deferred, 8e0ba6d): a `hyperlink.script`
kind, in the active group with `relationship.external` and `field.external` and taking their place for that target: a
hyperlink in either form (the relationship's or the field's) whose target is a `javascript:` URI read as `altchunk.script`
reads one (leading C0 controls and spaces ignored, tab, LF and CR dropped anywhere, the scheme compared
case-insensitively; everything up to the first `:` that comes before any `/`, `?` or `#`), its subject the target;
one finding a link; a bookmark is still neither. The kind is defined and not yet implemented by any inspector; when it
is, all three at once, with the oracle re-baselined. docx4j's reading is `LinkTargets.scheme` (phase 1), the ports have
it from the hand-off.

## 7. Risks

- **Links lost.** A document with a link in a scheme outside the list loses the link in every output: an
  intranet `notes://` or `onenote:` link, a `ms-word:ofe|u|…` link, a `sms:` or `callto:`. The property
  (D3) restores a scheme in one line of docx4j.properties, and the WARN names the first address. The default
  set could be wider (`ftps`, `sftp`, `callto`, `sms`, `skype`?); the CR keeps POI's set and lets D3 grow it.
- **CSS escaping changes selectors** for any style ID that is not a plain identifier. Today those
  selectors are broken anyway (`.Normal (Web)` is two simple selectors), so a change there is a fix; a
  consumer's own stylesheet that targeted docx4j's class names by the raw ID keeps working, since the
  `class` attributes do not change.
- **The guard rewrites `<` in user CSS?** No: D5 applies it to the stylesheet docx4j composes; `userCSS`
  is appended after and left alone, as the developer's.
- **Coverage.** The probes cover the sinks found by reading `convert/out`, `model/images`, docx4j-markdown
  and docx4j-tika. A sink this reading missed is what the `\3c ` guard and the one `LinkTargets` entry
  point are for; a new output that writes a link should call it, which the CR's tests cannot enforce.

## 8. Sources

- apache/poi#1455, "WordToHtmlConverter: don't link HYPERLINK fields with script targets" (merged):
  the repro, the cause, `WordToHtmlUtils.isSafeHyperlink` and its scheme set.
- WHATWG URL Standard, "URL parsing": leading and trailing C0 controls and spaces removed, ASCII tab and
  newline removed anywhere, scheme compared case-insensitively.
- CSSOM, `CSS.escape()`: the identifier escaping this CR adopts.
- HTML Standard, the `style` element's raw text content model (ends at `</style`).
- This repository: `convert/out/common/writer/HyperlinkUtil.java`, `AbstractHyperlinkWriterModel.java`,
  `model/images/AbstractWordXmlPicture.java`, `convert/out/html/HtmlCssHelper.java`, `XsltHTMLFunctions.java`,
  `HTMLConversionContext.java`, `TagClass.java`, `SdtWriter.java`, `model/text/InspectPayloads.java`
  (`javascript`, `external`); docx4j-markdown `WmlToMarkdown.java`; docx4j-tika `Docx4jWordParser.java`.
- The probes and their outputs: `~/claude-scratch-archive/docx4j/html-output-xss-probes-2026-10-23`.

## 9. Phase 0 record (2026-10-23, docx4j-25)

Tests written and run against the released 17.3.4 artifacts (`-Drevision=17.3.4`, the 17.3.5 modules not yet
installed); the red ones carry `@Ignore` naming the phase that turns them on, so the tree stays green until then.

- `docx4j-core-tests`: `org.docx4j.convert.out.html.HostileDocx` builds the document (four script-scheme links by
  relationship, incl. the sneaky ` \u0001java\tscri\npt:` form, `vbscript:` and `data:`; a script-scheme HYPERLINK
  field; eleven benign targets: http, https, mailto, ftp, file, tel, news, a relative path, a fragment, a drive path, a
  UNC path; a hostile paragraph style ID, a hostile table style ID, a style ID with spaces).
  `HtmlOutputHostileTest`, both pathways: `…WritesNoScriptSchemeLink` RED (a `javascript:` href), `…EscapesStyleIds`
  RED (`</style><script>` in the stylesheet), `…KeepsBenignLinks` GREEN (every benign href present, and must stay).
- `docx4j-markdown`: `MarkdownHostileLinksTest.noScriptSchemeDestination` RED (`[text](<javascript:alert(1)>)`),
  `benignLinksKept` GREEN.
- `docx4j-export-fo-tests`: `FoHostileLinksTest.noScriptSchemeExternalDestination` RED
  (`external-destination="url(javascript:alert(1))"`), `benignLinksKept` GREEN.
- Not in phase 0: the positive cases that need the implementation (a spaced style ID's selector works; the property adds
  a scheme) are phase 1 and 2 tests; docx4j-tika's test is phase 3, on its branch.
- ImportXHTML's CR-005 phases 1 and 2 are committed there (ca10f61, 2026-10-23, not installed): its link check is on by
  default, so phase 3 adds the reflective `setLinkSchemesChecked(false)` in `HtmlChunkImport` (the inspector must still
  see a chunk's `javascript:` target as `relationship.external`); it throws `Docx4JException` for malformed input, which
  `guarded()` already catches; it escapes alt text on its side, so CR-057's escaping escapes nothing twice.

## 10. Phase 1 record (2026-10-23, docx4j-25)

- `org.docx4j.convert.out.common.writer.LinkTargets` (docx4j-core): `isSafe(target)`, `scheme(target)`, `schemes()`,
  `defaultSchemes()`, `excerpt(target)` and `refusedMessage(target)` for the callers' log line. The scheme is read as a
  browser reads it (leading controls and spaces ignored, tabs and line breaks dropped anywhere, case-insensitive) and is
  everything up to the first `:` that comes before any `/`, `?` or `#`; a single letter followed by `\` or `/` is a drive
  path. One reading stricter than a browser's, on purpose: a target whose scheme a browser would reject as malformed and
  resolve as a relative URL (`java script:x`, a C0 control inside the scheme) is refused rather than second-guessed, since
  a PDF viewer or a Markdown renderer need not read it the browser's way. The property `docx4j.convert.out.linkSchemes` is
  read on each call (a comma-separated list, trimmed, lower-cased, a trailing `:` tolerated), so a test can set and restore
  it; it only adds (D3).
- `HyperlinkUtil.toHtmlNode` returns a `span`, and `toFoNode` an `fo:inline` (what it already returns for a link with no
  target), for a refused external target; `toNode` copies the content into either. `AbstractWordXmlPicture
  .createHtmlImageElement` writes the `img` with no `a` for a refused click target. `WmlToMarkdown.linked` treats a refused
  target as no target. Each logs once per document at WARN (`AbstractWmlConversionContext.linkRefused`, a field on
  `WmlToMarkdown`; the picture per picture) and the rest at DEBUG, with `excerpt` (80 characters, controls as spaces).
- Tests: `LinkTargetsTest` (safe, refused, the scheme reading, the property's add-only semantics, the excerpt);
  `HtmlOutputHostileTest`'s link rows on, now checking every `href` attribute's scheme rather than the page text (the
  refused link's text, `java\tscri\npt:…`, is kept in the page, as it should be, so the phase 0 substring assertion could
  not go green); `propertyAddsAScheme` on both pathways with `HostileDocx.CUSTOM_LINK` (`onenote:`), refused by default;
  `MarkdownHostileLinksTest` and `FoHostileLinksTest` on, their assertions likewise narrowed to the destination.
  Green: core-tests `convert.out.html.*`, the hyperlink and field tests, `HtmlVisitorParityTest`; docx4j-markdown (169);
  `FoHostileLinksTest`. The style-ID tests stay ignored until phase 2.
- CHANGELOG under 17.3.5; the property documented in `docx4j-samples-resources`' `docx4j.properties`.

## 11. Phase 2 record (2026-10-23, docx4j-25)

- `org.docx4j.convert.out.html.CssIdentifiers`: `escape(id)` with CSSOM `CSS.escape()` semantics (NUL to U+FFFD; a
  control, a leading digit and a digit after a leading `-` as hex escapes; `-` alone as `\-`; `[A-Za-z0-9_-]` and
  U+0080 and above as they are; the rest backslash-escaped), with one deviation: `<` is a hex escape (`\3c `) rather
  than `\<`, so that the guard finds nothing to touch in an escaped identifier. `guard(css)` writes every `<` as `\3c `.
- Applied in `HtmlCssHelper.createCssForStyles` at its three selector sites (`table.`, `.`, `span.`). The derived
  `<style>-<tbl>-BR` names are synthetic styles in the style tree (`PropertyResolver`, `ParagraphStylesInTableFix`), so
  they come through the same three sites: `XsltHTMLFunctions` composes no selector of its own (§4's reading was wrong
  there). Departure from D5's placement: the guard runs inside `createCssForStyles`, on the text it appends, not in
  `HTMLConversionContext.DEFAULT_STYLE_ELEMENT_HANDLER`. The handler receives the composed buffer with `userCSS` already
  appended, so guarding there would touch the developer's CSS or need the buffer split; guarding at the source leaves
  `userCSS` alone, covers a caller's own `ConversionHTMLStyleElementHandler`, and covers the other two callers
  (`XsltHTMLFunctions.getCssForStyles` for a customised XSLT, and the legacy `HtmlExporterNonXSLT.getCss`).
- `TagClass.createDiv` builds its `p` and `a` with DOM calls, the alias as a text node (D6).
- Measured on the probe document: `.x\3c \/style\>\3c script\>alert\(\'H6_STYLEID\'\)\3c \/script\> {display:block;…}`,
  `table.t\3c \/style\>…`, `.Normal-t\3c \/style\>…-BR`, `.\31 Heading {…}` for a style ID `1Heading` (an invalid
  selector before, so its rule was dropped; now it matches `class="1Heading …"`), `.Body\ Text\ 2 {…}`; the `class`
  attributes unchanged; no `<` inside the `<style>` element on either pathway.
- **D11 (decided, Jason 2026-10-23: leave as is): a style ID with spaces still matches nothing.** The `class` attribute is the style's
  ancestry joined by spaces (`StyleTree.getHtmlClassAttributeValue`: `class="Body Text 2 DocDefaults "`), so the browser
  sees the tokens `Body`, `Text`, `2`, and the escaped selector `.Body\ Text\ 2` is a single token. §3's "starts
  working" was wrong. Making it match needs a class-token mapping applied on both sides (say whitespace to `_`), which
  changes the `class` attributes a consumer's own stylesheet may target by the raw ID, against D5's "class attributes
  unchanged". Word never writes a space in a style ID (it strips them), so this is third-party producers only.
  Recommendation: leave as is (an unmatched rule, as before, now at least not two selectors), and note it here.
- Tests: `CssIdentifiersTest` (the CSSOM examples, the `<` deviation, the guard); `HtmlOutputHostileTest`'s style rows
  on, asserting the escaped selectors, the unchanged `class` attributes, and no `<` inside the style element;
  `userCssUntouched` (a `p > span` rule in `setUserCSS` survives). One phase 0 assertion corrected: "0 script elements"
  could never hold, since docx4j's own `toggleDiv` script element is always in the head; it is now "1, and none from the
  document". Green: core-tests `convert.out.html.*`, hyperlink, field, sdt and CSS tests (110).
- CHANGELOG under 17.3.5.

## 12. Phase 3 record (2026-10-23, docx4j-25)

- **docx4j-tika** (branch `docx4j-tika`, rebased onto VERSION_17_3_5 at 67c44d66a so that its core has `LinkTargets`;
  it was one commit on 74ca053d5, so the rebase was clean and its merge back will be a fast-forward): `Docx4jWordParser.run`
  starts an `a` only where `LinkTargets.isSafe` allows the target; otherwise the characters alone, as the other outputs.
  `HostileLinksTest` (a unit test beside `TikaParityIT`: builds the document, saves and reloads it, parses with
  `ToXMLContentHandler`; the sneaky target drops its U+0001, which cannot be saved to XML) and the README's "Limits and
  safety" bullet. `TikaParityIT` unchanged and green (phase 0 tokens 177/177).
- **`HtmlChunkImport`** (docx4j-core): calls docx4j-ImportXHTML's `setLinkSchemesChecked(false)` reflectively, beside
  `setDisplayNoneAsHidden`, so a chunk's `javascript:` link is kept with that target, as Word keeps it: the text
  model's run carries it and the outputs refuse it. An ImportXHTML without the method (before its CR-005) logs at
  DEBUG and behaves as before. Measured the same day with ImportXHTML ca10f61 (installed by its session as 17.1.1-SNAPSHOT,
  run with `-Dmaven.test.additionalClasspath=<its bundle jar>`, the CR-053 §11 recipe): the run is
  `CLICK -> javascript:alert(1)`, and the inspector's findings for the chunk are `altchunk`, `altchunk.script` (the chunk
  scan, which sees the `javascript:` href whether or not a reader is present) and `altchunk.external_ref`; **not**
  `relationship.external`, which §9 and the phase 3 commit message assumed: the import's hyperlinks live in the
  extraction's scratch package, whose relationships the inspector does not walk. `AltChunkHtmlInspectTest
  .aScriptSchemeLinkIsKeptForTheOutputsToRefuse` records this (skipped without a reader). The importer-backed altChunk
  tests: 84 run, 0 failures, 4 skipped (the no-reader variants).
- **Getting Started**, "docx to (X)HTML": one paragraph (the schemes written, the refusal and why, the property, the
  CSS escaping), inserted in the canonical docx by XML (the paragraph before "If you have output logging enabled",
  the section's spacing, Consolas runs for the code tokens) and by hand in the md; html and pdf are regenerated at
  release.
- D9 (advisory) and D10 (backport) were decided "no", so the phase has nothing for them; the CHANGELOG entries say
  what was fixed.

## 13. Markdown's other sinks (2026-10-23, Jason: "should CR-056 be addressing any issues in markdown export?")

Measured with a probe document (now `docx4j-markdown`'s `MarkdownHostileMarkupTest`): body text, a link's text, a
picture's alt text and a comment's text containing `<script>…</script>`, `<img … onerror=…>`, `[x](javascript:…)` or
`](x)` are all escaped by the commonmark renderer (`\<script\>`, `\[x\]`, `a\](…`), so they reach a downstream
renderer as text; the image destination is a data URI or a file docx4j wrote, never a document value. One sink was
open: a revision's or a comment's author and date, under `markup_detail: author` or `author_date`, went into the
CriticMarkup marker as one raw `HtmlInline` with the braces (`{++<script>alert(1)</script>: inserted++}`), so a renderer
that passes raw HTML through (commonmark-java's `HtmlRenderer` by default) had the author as markup. Fixed: the
braces alone are raw; the detail is a text node, escaped like any other (`{++\<script\>…: inserted++}`). The markers'
`<br>` is a constant. Out of this CR: the OMML-to-LaTeX text (`\text{…}` carries document text into a math renderer,
which renders it as text) and the Markdown importer (it builds WML from objects).

## 14. Post-ship finding: character references in a link destination (2026-10-23)

Reported by docx4j-core-ts (Jason's go), found by docx4j-python's CR-015 draft (its Q3), confirmed by both ports and
by reading docx4j at 30fb41024. A CommonMark renderer decodes entity and numeric character references in a link
destination; `WmlToMarkdown` writes the target's `&` as it is (as commonmark-java does) and `LinkTargets.isSafe` read
the target undecoded, so four forms passed and came out of a renderer as `href="javascript:alert(1)"`:
`javascript&#58;alert(1)` (the `#` comes before any `:`, so no scheme), `&#106;avascript:alert(2)` and
`&#x6A;avascript:alert(4)` (an `&` begins no scheme), `javascript&colon;alert(3)` (no `:` at all). docx4j-python saw
it with markdown-it-py run with no URL check. The HTML and FO outputs write the `&` escaped (`&amp;#58;`), which a
browser decodes to the literal `&#58;`, so they were inert there; the Markdown output was the hole.

Fix (this repository, with CR-054 in the same session): `LinkTargets.isSafe` now requires both readings to be safe,
the target as written and with its character references decoded as CommonMark decodes them
(`decodeCharacterReferences`, public: decimal and hex references, U+FFFD for zero or out of range; the 45 HTML5 named
references that stand for one ASCII character, `&colon;` `&sol;` `&Tab;` `&NewLine;` and the rest, from commonmark's
entity table; other named references and a bare `&` left as written, since they can only make the scheme malformed).
Decoded once, as a renderer decodes once (`&amp;#58;` stays `&#58;`). The rule is in `isSafe` itself rather than a
separate `isSafeDestination` (core-ts's uncommitted shape) so that every output and docx4j-tika refuse the same
targets, and the ports have one oracle; `isSafe`'s existing cases are unchanged. Still safe: `R&D/report.html`,
`page.html?a=1&amp;b=2`, `https&#58;//example.org`, `&unknown;avascript:x`.

Tests: `LinkTargetsTest.characterReferencesDecodedAsACommonMarkRendererDecodesThem` (ten refused, seven kept, the
decoder's cases); `HostileDocx` gains `ENTITY_LINK` and `ENTITY_LINK_2`, asserted in `HtmlOutputHostileTest`;
`MarkdownHostileLinksTest.noEntityEncodedScriptSchemeDestination` and three ampersand targets kept as links;
`FoHostileLinksTest` one entity-encoded target. Hand-off: core-ts and python told the rule lives in `isSafe`.
