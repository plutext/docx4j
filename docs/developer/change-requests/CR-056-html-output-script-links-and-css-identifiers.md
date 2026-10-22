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
| 0 | The probes as red tests: core-tests (HTML both pathways, FO's `external-destination`), markdown, tika | 2 h |
| 1 | `LinkTargets` with the property; `HyperlinkUtil`, the picture link, `WmlToMarkdown`; tests green; CHANGELOG | ½ day |
| 2 | `CssIdentifiers`, the selectors in `HtmlCssHelper` and `XsltHTMLFunctions`, the `\3c ` guard in the style element handler, `TagClass` on DOM nodes; tests green | ½ day |
| 3 | docx4j-tika's `href` (on the `docx4j-tika` branch, with CR-055 phase 1); Getting Started; the advisory and the backport as decided (D9, D10) | ½ day |
| 4 | (optional, D8) the inspector's script-scheme finding, with the portfolio's WD change and the ports' hand-off | ½ day, with the portfolio |

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
