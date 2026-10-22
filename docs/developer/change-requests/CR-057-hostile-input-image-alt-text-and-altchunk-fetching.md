# CR-057: Hostile input: alt text in createImageInline, and what an HTML altChunk fetches

Status: PROPOSED 2026-10-23, drafted by Claude Fable 5.1 (session docx4j-25) from two findings docx4j-ImportXHTML's
CR-005 (hostile input) measured on the released 17.3.4 jars and passed on at Jason's word ("pass docx4j-25 the
two docx4j findings"). Both confirmed in this repository's source (§1). ACCEPTED 2026-10-23 (Jason: "CR-057 D1 to D4 agree"), as
recommended; phases in §3, to start on Jason's word.
Companion to CR-056, which covers the output side (links and style IDs in HTML, PDF and Markdown).

## 0. Summary

1. **`BinaryPartAbstractImage.createImageInline` does not escape its `altText` and `filenameHint`.** The
   static overload substitutes them into an XML template (`descr="${altText}"`, `name="${filenameHint}"`
   twice) and unmarshals it, so `Profit & Loss` or `a < b` fails with an `UnmarshalException`, and
   `x" title="INJECTED` writes an extra attribute into the document. Every caller that passes a user's or a
   document's alt text is affected; ImportXHTML's `<img alt="Profit &amp; Loss">` comes out as
   `[MISSING IMAGE: …]`. Fix: escape at substitution. Small.
2. **`convertAltChunks` fetches what an HTML chunk refers to, and a caller cannot stop it.** The importer
   is built with `fetch = true`, as Word does on open; only the text model's `read()` builds it with
   `neverFetch`. Measured: a chunk naming `http://127.0.0.1:…/chunk.css` and `…/chunk.png` had them
   fetched (the image four times), and `<img src=file:///etc/hostname>` opened and read the local file,
   refused only because it is not an image; an image file would have been embedded in the output. A server
   that converts an uploaded docx's chunks makes requests to any URL the docx names (SSRF) and copies local
   image files into its output. Fix: a switch, with "never fetch" the default (D2), Word's fetching on request.

## 1. Measured and confirmed

ImportXHTML's probe: `~/claude-scratch-archive/docx4j-ImportXHTML/hostile-input-probes-2026-10-23/Probe6.java`
and `Probe6.out` (docx4j-core 17.3.4, ImportXHTML-core d33581f, jsoup 1.22.1).

| Call | Result |
|---|---|
| `createImagePart(pkg, png).createImageInline(null, "plain", 1, 2, false)` | `descr="plain"` |
| alt `Profit & Loss`, alt `a < b` | `jakarta.xml.bind.UnmarshalException` |
| alt `x" title="INJECTED` | `<wp:docPr id="1" name="" descr="x" title="INJECTED"/>` |
| `convertAltChunks` on a docx whose HTML chunk links a stylesheet and two images at `127.0.0.1` and one at `file:///etc/hostname` | `/chunk.css` fetched once, `/chunk.png` four times; the local file opened and read ("Unrecognized image format for: file:///etc/hostname", four times) |

In this repository (VERSION_17_3_5 at b52fbaa0b, unchanged from 17.3.4):
- `openpackaging/parts/WordprocessingML/BinaryPartAbstractImage.java` line 929, the static
  `createImageInline(filenameHint, altText, rEmbedId, id1, id2, cx, cy, link)`: the template at lines 952
  and 955, `mappings.put` at 965 and 966, `XmlUtils.unmarshallFromTemplate(ml, mappings)` at 971, which
  substitutes the strings as they are. Every other `createImageInline` overload ends here.
- `model/text/HtmlChunkImport.java`: `convert` (line 114) calls `imported(target, dom, true, where)`;
  `read` (133) calls it with `false`, which routes through `neverFetch` (363). `convert` is what
  `JaxbXmlPartAltChunkHost.convertAltChunks` (and `AltChunkInterface`) runs. The base URL is null, so only
  absolute URLs are reached. The fourfold fetch is ImportXHTML's `Docx4jUserAgent` not caching a failed
  image, fixed by its CR-005 phase 1; one image on a server that never answers held a conversion for 121 s.

## 2. Proposal

**createImageInline (D1).** Escape `altText` and `filenameHint` for an XML attribute (`&`, `<`, `>`, `"`,
and the control characters XML 1.0 forbids dropped) before they enter the template; `XmlUtils` gains the
one helper if it has none. The alternative, building the `Inline` with the object factory instead of a
template, is the better shape but a larger change to a method with eight overloads; the escape is the fix,
the rebuild can follow. Tests: the three measured inputs round-trip into `wp:docPr/@descr` and
`pic:cNvPr/@name` as given, and `x" title="INJECTED` yields no `title` attribute. ImportXHTML's CR-005
phase 1 works around it meanwhile (`""`, then `setDescr`); once this ships, both paths give the same.

**altChunk fetching (D2, D3).** A `Docx4jProperties` setting, `docx4j.altchunk.html.fetch` (D3), read by
`HtmlChunkImport.convert`: `false` builds the importer through `neverFetch`, as `read()` does, so no
stylesheet, image or other reference is fetched and no local file is opened; `true` keeps Word's
behaviour. Recommended default `false` from 17.3.5 (D2): a docx is untrusted input wherever
`convertAltChunks` runs on a server, Word's fetching is a desktop assumption, and the text model has been
"never fetch" since CR-046 phase 3 without complaint. A deployment that wants remote images in its chunks
sets the property; a per-call override on `AltChunkInterface` (D4) lets one conversion differ. The chunk's
own text is unaffected; an image that is not fetched is the alt text or nothing, as in the text model.
Tests: the probe's chunk converted with the default makes no request and opens no file (an `http` listener
that must not be hit; the `file:` URL to a file that must stay unread), and with `true` makes them.

**CHANGELOG** under 17.3.5 names both; the fetch default is a behaviour change and says so. Getting Started's
"Importing XHTML"/altChunk text gains the property.

Out of scope: ImportXHTML's own hostile-input work (its CR-005: the user agent's caching, timeouts, link
schemes), and the output side (CR-056).

## 3. Phases

| Phase | What | Size |
|---|---|---|
| 0 | Red tests for both (core-tests; the altChunk test needs ImportXHTML and jsoup on the test class path, as the existing `AltChunkHtml*Test`s do). **DONE 2026-10-23** (record in §7) | 2 h |
| 1 | The escape in `createImageInline`; tests green; CHANGELOG | 1 h |
| 2 | The property, the override, `HtmlChunkImport.convert`; tests green; CHANGELOG, Getting Started; the ports' hand-off (their altChunk conversion, if any, mirrors the default) | ½ day |

## 4. Decisions (D1 to D4 accepted as recommended, 2026-10-23)

| # | Decision | Recommendation |
|---|---|---|
| D1 | createImageInline | escape at substitution now; the object-factory rebuild later, if at all |
| D2 | Fetch default | `false` from 17.3.5 (never fetch), Word's behaviour by property |
| D3 | The property | `docx4j.altchunk.html.fetch`, in the reference docx4j.properties with the reason |
| D4 | Per-call override | a parameter on `convertAltChunks` / a setter on `AltChunkInterface`, property wins where unset |

## 5. Risks

- **Lost remote images** for a user whose altChunks reference images by URL and who relied on
  `convertAltChunks` fetching them: the property restores it in one line, and the CHANGELOG says so.
- **Double escaping** for a caller that already escapes its alt text before `createImageInline`: today such
  a caller gets `&amp;amp;` into the document only if it escaped twice already; after the fix, a caller that
  escaped once (to survive the bug) sees `&amp;` in Word. The CHANGELOG names it; ImportXHTML (the known
  caller) does not escape.

## 6. Sources

- docx4j-ImportXHTML `docs/change-requests/CR-005-hostile-input.md` §5 and §8 (uncommitted there,
  2026-10-23), its Probe6 and the message from its session of 2026-10-23.
- This repository: `BinaryPartAbstractImage.java`, `XmlUtils.unmarshallFromTemplate`,
  `model/text/HtmlChunkImport.java` (`convert`, `read`, `neverFetch`), `JaxbXmlPartAltChunkHost.java`,
  `AltChunkInterface.java`; CR-046 phase 3 (the text model's no-fetch rule, WD §11).

## 7. Phase 0 record (2026-10-23, docx4j-25)

Run against the released 17.3.4 artifacts; the red tests carry `@Ignore` naming their phase.

- `CreateImageInlineEscapingTest` (core-tests, `openpackaging.parts.WordprocessingML`): `plainTextRoundTrips` GREEN;
  `ampersandAndAngleBracketsRoundTrip` RED (`UnmarshalException`); `quoteInjectsNoAttribute` RED (`descr` came back as
  `x`, the rest became a `title` attribute).
- `AltChunkConvertFetchTest` (core-tests, `model.text`): a loopback `HttpServer` records requests while
  `convertAltChunks` runs on a chunk naming a stylesheet, an image over http and a `file:` image; asserts no request and
  no image part. It assumes an HTML reader on the class path (`HtmlChunkImport.readerPresent()`), which docx4j-core-tests
  does not carry, so it is skipped there. Its red result, measured the same day by the same scenario as a JEP 330 run
  against docx4j-bundle 17.3.4 with docx4j-ImportXHTML-bundle 17.1.1-SNAPSHOT (CR-004 phase 1, core 34089b9) on the
  class path: `convertAltChunks` requested `/chunk.css` and `/chunk.png` from the loopback server (503 ms) and left two
  image parts in the package, the one fetched over http and the one read from the local `file:` URL. The test turns on
  in phase 2, run that way (or with the reader on the test class path).

