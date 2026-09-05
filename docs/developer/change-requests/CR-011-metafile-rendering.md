# CR: WMF / EMF / EMF+ rendering via repackaged Apache POI HWMF and HEMF

Status: PROPOSED (2026-09-06) — analysis and plan; nothing implemented yet.
Converted from the maintainer's analysis note of 2026-09-04/06 (verified against
POI trunk at `../poi` and the 5.3.0 release jars, and this tree at 451f05172).
Scope: rendering Windows metafiles (WMF, EMF, EMF+) held in a package — inline and
floating pictures, VML `w:object`/`w:pict` previews, Equation Editor / MathType
previews, pasted Office content — into PDF (via FO) and HTML, plus a public
converter API on the metafile parts. Not in scope: DrawingML, VML geometry,
SmartArt, OMML, or OLE editing (§2).
Phases: 1. repackage HWMF/HEMF into `org.docx4j.org.apache.poi` with shims;
2. wire the renderer into `AbstractWordXmlPicture` for FO and HTML (SVG, PNG
fallback) and the parts' converter API, retiring wmf2svg; 3. `OlePres000`
fallback; 4. implement the missing EMF/EMF+ records, corpus-driven.

## 1. Background: what Apache POI provides

### 1.1 Packages and entry points

| Package | Contents |
|---|---|
| `org.apache.poi.hwmf.usermodel` | `HwmfPicture` (WMF entry point), embedded-object iteration |
| `org.apache.poi.hwmf.record` | one class per GDI record family (`HwmfDraw`, `HwmfFill`, `HwmfText`, `HwmfBitmap*`, ...) |
| `org.apache.poi.hwmf.draw` | `HwmfGraphics` (record playback onto `Graphics2D`), `HwmfImageRenderer` |
| `org.apache.poi.hemf.usermodel` | `HemfPicture` (EMF entry point) |
| `org.apache.poi.hemf.record.emf` | EMF records (`HemfDraw`, `HemfFill`, `HemfText`, `HemfComment`, ...) |
| `org.apache.poi.hemf.record.emfplus` | EMF+ records and objects (brush, pen, path, region, image, font, string format) |
| `org.apache.poi.hemf.draw` | `HemfGraphics` (extends `HwmfGraphics`), `HemfImageRenderer` |

Both live in the `poi-scratchpad` artifact, which depends on the `poi` core artifact. Both are
Apache License 2.0 with no third-party notices attached to these packages in POI's LICENSE or NOTICE.

Entry points, confirmed:

```java
HwmfPicture wmf = new HwmfPicture(inputStream);
wmf.draw(graphics2D);                       // WMF only has the one-arg form as well
wmf.draw(graphics2D, targetBounds);
wmf.getBounds(); wmf.getBoundsInPoints(); wmf.getSize(); wmf.getRecords();
wmf.getPlaceableHeader();                   // Aldus placeable header, as used by Word

HemfPicture emf = new HemfPicture(inputStream);
emf.draw(graphics2D, targetBounds);         // no one-arg draw on HemfPicture
emf.getBounds(); emf.getBoundsInPoints(); emf.getSize(); emf.getRecords();
emf.getEmbeddings();                        // nested WMF/EMF/bitmaps
```

Pipeline, confirmed: bytes, then `Hwmf/HemfPicture`, then a list of record objects, then
`HwmfGraphics`/`HemfGraphics` replays them onto any `Graphics2D`. `HemfGraphics.draw(HemfPlusRecord)`
exists, so EMF+ is rendered, not merely skipped. EMF+ records arrive inside `EMR_COMMENT` records
(`HemfComment.EmfCommentDataPlus`) and `HemfGraphics` has an explicit render-state machine
(`INITIAL`, `EMF_ONLY`, `EMFPLUS_ONLY`, `EMF_DCONTEXT`) for the "dual" files Office writes, which
carry both EMF+ records and EMF fallback records.

Corrections to the summary that prompted this document:

- `PPTX2PNG` is in `poi-ooxml` (`org.apache.poi.xslf.util`), not `poi-examples`. Formats are png,
  gif, jpg, svg, pdf. SVG output needs batik-svggen plus POI's `SVGPOIGraphics2D`; PDF needs
  pdfbox. `-inputType WMF|EMF` renders standalone metafiles.
- `org.apache.poi.hslf.blip.WMF`/`EMF` (PowerPoint picture containers, deflate-compressed with a
  34-byte header) are entirely separate from HWMF/HEMF and are not needed.
- Text rendering resolves fonts through `DrawFactory.getInstance(g).getFontManager(g)`, i.e. POI's
  `DrawFontManager`, which can be overridden per `Graphics2D` via the `Drawable.FONT_HANDLER`
  rendering hint. This is the hook for docx4j's own font substitution.
- POI 5.x jars are Java 8 bytecode (class major version 52 checked in poi-scratchpad 5.3.0).
  POI trunk is 6.0.0-SNAPSHOT, requires Java 17, and commits of 2026-09-03 ("Use more Java 17
  idioms in poi-scratchpad") already touched these packages. Copy from the 5.5.1 release, not trunk.

### 1.2 Coverage

| Format | Record types registered | Not implemented |
|---|---|---|
| WMF | 69 | 0 |
| EMF | 119 | 26 |
| EMF+ | 58 | 29 |

Unimplemented EMF records (`UnimplementedHemfRecord`): SETCOLORADJUSTMENT, SETMETARGN, ANGLEARC,
SETARCDIRECTION, MASKBLT, PLGBLT, CREATECOLORSPACE, SETCOLORSPACE, DELETECOLORSPACE, GLSRECORD,
GLSBOUNDEDRECORD, PIXELFORMAT, DRAWESCAPE, EXTESCAPE, SMALLTEXTOUT, FORCEUFIMAPPING, NAMEDESCAPE,
COLORCORRECTPALETTE, SETICMPROFILEA/W, SETLAYOUT, TRANSPARENTBLT, GRADIENTFILL, SETLINKDUFIS,
COLORMATCHTARGETW, CREATECOLORSPACEW. Most are obscure; GRADIENTFILL, TRANSPARENTBLT, MASKBLT,
PLGBLT, ANGLEARC, SMALLTEXTOUT and SETLAYOUT (RTL) are the ones likely to show up in documents.

Unimplemented EMF+ records: Comment, MultiFormatStart/Section/End, Clear, FillPolygon, DrawLines,
FillEllipse, DrawEllipse, FillPie, DrawPie, DrawArc, FillClosedCurve, DrawClosedCurve, DrawCurve,
DrawBeziers, DrawString, SetTextContrast, BeginContainer, BeginContainerNoParams, EndContainer,
TranslateWorldTransform, ScaleWorldTransform, RotateWorldTransform, OffsetClip, StrokeFillPath,
SerializableObject, SetTSGraphics, SetTSClip.

That EMF+ list is the important caveat. What is implemented on the EMF+ side is paths, rectangles,
regions, images, world transform set/reset/multiply, clipping by rect/path/region, and the object
records. Files that are EMF+ dual (Office "Paste as Enhanced Metafile", Office charts) usually
render through the EMF fallback records. Files that are EMF+ only (typically from .NET / GDI+
applications) will lose ellipses, arcs, curves, text drawn via DrawString, and container-based
transforms. Implementing these in the repackaged copy is the stated reason for repackaging rather
than depending on the jars.

There are 45 `TODO` / "to be validated" / "not supported" markers across the two packages, notably
line-width calculation, hatch brushes, gradient colour lists, and `HwmfBitmap16`.

### 1.3 Dependency footprint

The two packages are 68 source files, about 23,600 lines. Imports outside themselves:

| From | Classes | Notes |
|---|---|---|
| `org.apache.poi.util` | 17 | `LittleEndian*`, `IOUtils`, `BitField*`, `RecordFormatException`, `StringUtil`, `LocaleUtil`, `Units`, `MathUtil`, `Dimension2DDouble`, `GenericRecord*`, annotations |
| `org.apache.poi.common.usermodel` | `GenericRecord`, `Duplicatable`, 7 `fonts.*` classes | small, self-contained |
| `org.apache.poi.sl.draw` | `DrawFactory`, `DrawPictureShape`, `DrawPaint`, `DrawFontManager`, `Drawable`, `ImageRenderer`, `BitmapImageRenderer`, `EmbeddedExtractor` | the three in bold below drag in the slideshow layer |
| `org.apache.poi.sl.usermodel` | `PictureData.PictureType` | enum only |
| `org.apache.poi.poifs.filesystem` | `FileMagic` | already in docx4j's copy |
| `org.apache.poi.logging` | `PoiLogManager` | log4j-api wrapper |
| commons-io | `UnsynchronizedByteArrayInput/OutputStream` | docx4j already depends on commons-io |
| commons-math3 | `LUDecomposition`, `MatrixUtils`, `RealMatrix` | three uses in the EMF+ brush code |
| log4j-api | `Logger`, `LogManager`, `Unbox` | replace with slf4j, as the POIFS copy did |
| `javax.imageio` | `ImageIO` | JDK |

Following imports transitively from hwmf and hemf reaches 256 POI classes. About 100 of those are
the whole `sl.draw`, `sl.draw.geom` and `sl.usermodel` layer, pulled in by exactly three call sites:

- `HwmfGraphics` line 468: `DrawFactory.getInstance(g).getFontManager(g)` for font mapping;
- `HwmfGraphics` line 885: `DrawPictureShape.getImageRenderer(g, contentType)` for embedded bitmaps
  (with a POI-side TODO to move it out of the slideshow code);
- `HemfPlusBrush` lines 679 to 688: `DrawPaint.RGB2SCRGB` / `SCRGB2RGB` for gradient interpolation.

Replacing those three plus the small interfaces they touch (12 classes in all: `DrawFactory`,
`DrawPictureShape`, `DrawPaint`, `DrawFontManager`, `Drawable`, `ImageRenderer`,
`BitmapImageRenderer`, `EmbeddedExtractor`, `PictureData`, `FileMagic`, `GenericRecordJsonWriter`,
`GenericRecordUtil`) with docx4j-local shims leaves a residue of 33 support classes. docx4j's
existing `org.docx4j.org.apache.poi` copy (207 files, taken from POI 3.x in 2015 and already
converted to slf4j) provides most of the `util` ones. Genuinely new to docx4j are five `util`
classes (`Dimension2DDouble`, `MathUtil`, `Units`, `ArrayUtil`, `LittleEndianInputStream` if the
copy's version differs), the seven `common.usermodel.fonts` classes, `GenericRecord`,
`Duplicatable`, `EmptyFileException` and a logging shim.

Size for reference: hwmf plus hemf is 395 class files and 1.27 MB uncompressed in the 5.3.0 jar.

## 2. Gap: what docx4j does with metafiles today

Verified against this tree (commit 451f05172, 2026-09-06):

- `ContentTypes` and `BinaryPartAbstractImage` treat WMF and EMF as formats Word supports natively.
  On import they are stored as-is. ImageMagick / GraphicsMagick is only invoked, optionally, for
  formats Word cannot store (EPS, PDF, and WebP without the imageio plugin). The comment in
  `BinaryPartAbstractImage` that Word converts EPS to EMF on import is present.
- Parts: `MetafileWmfPart` (has `toSVG()` using wmf2svg 0.10.6, declared in `docx4j-core/pom.xml`
  and in `module-info.java`) and `MetafileEmfPart` (no conversion; a 2010 comment surveys the
  options and concludes OpenOffice was the only workable EMF converter at the time).
- Rendering: `AbstractWordXmlPicture` has a branch that converts `MetafileWmfPart` to SVG and emits a
  literal `[TODO emf image]` placeholder for `MetafileEmfPart`. **That branch is dead code.** The
  `metaFile` field it tests is declared and compared but never assigned anywhere in docx4j-core or
  docx4j-export-fo. In practice both WMF and EMF fall through to `createHtmlImageElement()`, so the
  HTML output references a `.wmf`/`.emf` the browser cannot show, and the FO output hands FOP an
  `fo:external-graphic` it cannot decode.
- VML: `WordXmlPictureE10` resolves `v:imagedata/@r:id` to the image part, so `w:object` / `w:pict`
  previews already reach the same picture pipeline. Geometry comes from VML; the picture bytes are
  whatever the part holds, so today an object preview stored as WMF or EMF is lost the same way.
- OLE: `OleObjectBinaryPart` wraps the repackaged POIFS and understands `Ole10Native` and
  `CompObj`. Neither docx4j nor POI parses `OlePres000` presentation streams (MS-OLEDS
  `OLEPresentationStream`). POI's only nearby code is `hpsf.Thumbnail`, which decodes the
  `CF_METAFILEPICT` thumbnail in a SummaryInformation stream; that class is already in docx4j's copy.
- Fonts: `PhysicalFonts` / font substitution is docx4j's own; nothing maps GDI `LOGFONT` names yet.
- Sample inputs: `docx4j-samples-docx4j/sample-docs/metafile-samples/` holds `EMF.docx`, `WMF.docx`
  and seven loose `.emf`/`.wmf` files. POI's `test-data/` has 22 more `.wmf`/`.emf` files and 16
  test classes under `poi-scratchpad/src/test` for hwmf/hemf.

**Update (2026-09-06, after 3b11fbb59):** the FO pathway no longer hands FOP an
undecodable graphic in every case. WMF is painted by FOP through Batik's WMF
loader (when batik is on the classpath, which `docx4j-export-fo` guarantees);
EMF is detected by signature and either converted with ImageMagick /
GraphicsMagick when `BinaryPartAbstractImage.ImageMagickExecutable` is set
(`docx4j.convert.out.fo.pictures.convertDensity`, default 300) or, otherwise,
replaced by a transparent placeholder of the declared extent so the layout
holds (`WordLayoutFixups.reserveUnpaintablePictures`). So today's PDF gap is
EMF without ImageMagick (drawn as nothing, at the right size); HTML output
still references the raw `.wmf`/`.emf`. This CR replaces both stopgaps with a
pure-Java renderer.

## 3. Value assessment

The external advice framed this as "native Java rendering of Windows metafile graphics and legacy
OLE presentations" rather than "add WMF/EMF support", ranked the gains as PDF/HTML fidelity first,
legacy OLE and equation rendering second, and pure-Java elimination of ImageMagick third, and
proposed three stages (image rendering; VML/OLE preview integration; OLE presentation extraction).

Sanity check against the code, point by point.

| Claim in the advice | Verdict | Notes |
|---|---|---|
| It is a rendering capability, not a packaging one | Correct | Storage and content types already work. |
| DOCX to PDF and HTML fidelity is the immediate win | Correct, and understated | Not only EMF is broken. The WMF-to-SVG branch is dead, so WMF is broken in output today too. Both formats gain. |
| SVG route keeps vectors in PDF; PNG as fallback | Correct | `docx4j-export-fo` already depends on batik-transcoder, which brings batik-svggen, so `SVGGraphics2D` is available there with no new dependency. HTML can use SVG inline or PNG data URIs. |
| Thumbnail / preview generation | Correct but not a current feature | docx4j has no thumbnail API. It becomes easy once a `BufferedImage` route exists. |
| OLE object previews (Excel, Visio) | Correct, mostly via stage 2 | In DOCX, Word always writes the preview as a separate image part referenced from `v:imagedata`. So the common case is "VML object plus metafile renderer", which needs no OLE parsing at all. |
| Equation Editor 3 / MathType display | Correct and valuable | Word stores an Equation Editor preview as a **WMF** part in most files, MathType more often as EMF. So WMF matters more for equations than the advice's "EMF is the more valuable" suggests. Charts and pasted content are where EMF dominates. |
| "Paste as Enhanced Metafile" content | Correct | Modern Office writes EMF+ dual, so the EMF fallback records render even where EMF+ records are stubs. |
| Historical EPS imported as EMF | Correct | Confirmed by the `BinaryPartAbstractImage` comment. |
| VML `v:imagedata` completion | Correct | `WordXmlPictureE10` already resolves the relationship; only the bytes were undecodable. |
| Image extraction as SVG/PNG | Correct | Falls out of the converter API. |
| Text in metafiles becomes searchable PDF text | Plausible, unverified | `SVGGraphics2D` emits `<text>` unless `textAsShapes` is set, and FOP keeps SVG text as text when it can resolve the font. Needs testing with docx4j's font configuration. |
| Replaces ImageMagick for WMF/EMF | Overstated | docx4j never used ImageMagick for WMF/EMF; it only uses it for EPS/PDF/WebP import conversion. The real benefit is that EMF-to-PNG becomes possible at all in pure Java. Rank this lower than the advice does. |
| OLE presentation extraction from `oleObject*.bin` (stage 3) | Correct in principle, lower priority | Neither POI nor docx4j parses `OlePres` streams. MS-OLEDS `OLEPresentationStream` is simple (format tag, extents, size, then raw WMF/EMF/DIB) and would be about a hundred lines on top of the existing POIFS copy. It only matters for documents whose producer omitted the `v:imagedata` preview, so treat it as a fallback, not a headline. |
| Does not unlock DrawingML, VML geometry, SmartArt, OMML, OLE editing | Correct | Worth keeping in the feature description to set expectations. |
| Graphics normalisation layer (ImageIO / Batik / HWMF / HEMF behind one `Graphics2D`-based interface) | Correct, and the right architecture | See §4.2. |

Net: the advice is sound. Two adjustments. First, WMF is as broken as EMF today, so the first stage
fixes both and can retire wmf2svg. Second, ranking: PDF/HTML fidelity for pictures and VML object
previews first (these are one piece of work), equation and pasted-content coverage as the concrete
payoff of that same work, `BufferedImage`/PNG and thumbnails next, OLE presentation-stream parsing
last.

One caveat the advice did not raise: quality is bounded by the EMF+ gaps in section 1.2. Documents
with EMF+ only content from GDI+ applications will render incompletely until those records are
implemented. That is the case for repackaging.

## 4. Design

### 4.1 Repackaging plan

Decision: repackage into `org.docx4j.org.apache.poi.hwmf` and `org.docx4j.org.apache.poi.hemf`,
following the 2015 POIFS precedent, because it allows implementing the missing EMF/EMF+ records on
docx4j's release cadence, avoids a 5 MB dependency that clashes with users' own POI versions, and
avoids log4j-api and commons-math3.

Steps:

1. **Source baseline.** Copy from the POI 5.5.1 release tag (Java 8 bytecode; docx4j-core targets
   Java 11). Record the upstream commit hash in a `README` in the package so later syncs have a
   base. Do not copy from trunk.
2. **Files.** The 68 hwmf/hemf files, plus the support classes listed in section 1.3 that are not
   already in the copy. Diff the `util` classes that are already present against 5.5.1 first;
   `IOUtils` and `LittleEndianInputStream` in particular gained allocation-limit methods since 3.x.
3. **Shims** (docx4j-local replacements, all small):
   - `DrawFontManager` interface plus a default that maps `HwmfFont` face names through
     `org.docx4j.fonts.PhysicalFonts`, installed via the `Drawable.FONT_HANDLER` rendering hint;
   - `ImageRenderer` interface, `BitmapImageRenderer` (ImageIO), and a static
     `getImageRenderer(contentType)` replacing the `DrawPictureShape` call, dispatching to
     bitmap, `HwmfImageRenderer` or `HemfImageRenderer`;
   - `DrawPaint.RGB2SCRGB`/`SCRGB2RGB`: copy the two colour-space functions only;
   - `Drawable` hint keys (`DEFAULT_CHARSET`, `EMF_FORCE_HEADER_BOUNDS`, `FONT_HANDLER`,
     `IMAGE_RENDERER`);
   - `PictureData.PictureType` enum subset; `EmbeddedExtractor`; `FileMagic` already exists.
4. **Logging.** Replace `PoiLogManager`/log4j with slf4j `LoggerFactory`, as done in the POIFS copy.
   `Unbox.box` calls become plain arguments.
5. **commons-math3.** The three uses in `HemfPlusBrush` invert a small matrix; replace with
   `java.awt.geom.AffineTransform.createInverse()` or a hand-written 3x3 inverse.
6. **Safety limits.** Keep POI's `IOUtils.safelyAllocate` / `setByteArrayMaxOverride` paths intact
   and set an explicit default. These packages parse untrusted bytes and received a run of
   bounds-check fixes upstream in May and June 2026 (`HemfPlusPath`, `EmfPlusDrawDriverString`,
   `EmfExtCreatePen`, EMF description bounds, and others).
7. **Attribution.** Follow the existing convention for the POIFS copy. Each of the 207 files under
   `org.docx4j.org.apache.poi` opens with a Plutext notice ("This file has been changed by Plutext
   Pty Ltd for use in docx4j. The package name has been changed; there may also be other changes.
   This notice is included to meet the condition in clause 4(b) of the License.") above the
   retained ASF header, and `legals/NOTICE` states that `org.docx4j.org.apache.poi` repackages
   portions of Apache POI under ASL 2.0. Add the same header to every copied hwmf/hemf file and
   keep the ASF header intact (clauses 4(b) and 4(c)). For clause 4(d), POI's own NOTICE lists
   several third-party attributions (BEA/XMLBeans, W3C schemas, vsdump, eID Applet, Scala's
   NonFatal), none of which relate to the hwmf/hemf code, so the existing one-line entry in
   `legals/NOTICE` remains sufficient; extending it to name the two new packages and the POI
   version copied would be a courtesy, not a requirement.
8. **Upstream sync.** Watch `poi-scratchpad/src/main/java/org/apache/poi/h{w,e}mf` upstream.
   Security fixes should be back-ported promptly. Newly implemented records should be offered
   upstream as PRs; it keeps the two copies converging and is the cheapest way to get review from
   the people who wrote the parser.
9. **Module descriptor.** `docx4j-core` already `requires transitive java.desktop`; nothing new is
   needed for AWT. Drop `requires wmf2svg` once the WMF path is switched.
10. **Tests.** Import POI's 22 `.wmf`/`.emf` test files and the relevant assertions from its 16
    hwmf/hemf test classes (same licence), plus the nine files under `metafile-samples/`. Add
    render-to-PNG golden tests at a fixed DPI.

### 4.2 Proposed design in docx4j

A single conversion entry point in docx4j-core:

```java
public interface MetafileRenderer {
    boolean canRender(String contentType);            // image/x-wmf, image/x-emf, image/emf
    Dimension2D getSizeInPoints(byte[] data);
    void draw(byte[] data, Graphics2D target, Rectangle2D bounds);
    BufferedImage toImage(byte[] data, double dpi);   // convenience over draw()
}
```

with one implementation over the repackaged HWMF/HEMF, and an SVG helper in `docx4j-export-fo`
(`SVGGraphics2D` from batik-svggen) that turns `draw()` into an SVG DOM for
`fo:instream-foreign-object` or inline HTML `<svg>`.

Wiring, in order of value:

1. `AbstractWordXmlPicture`: replace the dead `metaFile` branch. Detect `MetafileWmfPart` /
   `MetafileEmfPart` from the resolved image part, render to SVG for FO and HTML (PNG data URI as
   a fallback for the HTML `ConversionImageHandler` implementations that write files). This fixes
   inline pictures, floating pictures, and every `w:object` / `w:pict` whose `v:imagedata` points
   at a metafile, which includes Equation Editor and MathType previews and pasted Office content.
2. Public converter API on the parts: `MetafileWmfPart.toSVG()` reimplemented over HWMF,
   `MetafileEmfPart.toSVG()` and `toPNG(dpi)` added. Retire wmf2svg.
3. Optional: `OleObjectBinaryPart.getPresentation()` parsing `OlePres000` per MS-OLEDS for
   documents that lack a `v:imagedata` preview, returning WMF/EMF/DIB bytes to the same renderer.
4. Implement the EMF and EMF+ records from section 1.2 in the copy, prioritised by what the corpus
   actually contains (log unimplemented record types at debug level and count them across the
   test corpus first).

## 5. Alternatives rejected

- **Depend on `poi-scratchpad` directly.** Rejected: a 5 MB dependency that clashes
  with users' own POI versions, drags in log4j-api and commons-math3, and leaves
  the EMF+ gaps (§1.2) on POI's release cadence rather than docx4j's. The 2015
  POIFS precedent (`org.docx4j.org.apache.poi`) is the model.
- **Keep wmf2svg for WMF and add a converter only for EMF.** Rejected: the WMF
  branch in `AbstractWordXmlPicture` is dead code today, wmf2svg has no EMF
  path, and one renderer for both formats (with EMF+ dual fallback) is less
  code than two.
- **ImageMagick / GraphicsMagick for EMF (the current optional path).** Kept only
  as the interim stopgap: it needs a native install, rasterises (no vectors in
  the PDF, no searchable text), and is unavailable on most servers.
- **Copy from POI trunk.** Rejected: trunk is 6.0.0-SNAPSHOT and requires Java
  17; docx4j-core targets Java 11. Copy from the 5.5.1 release tag.

## 6. Phases

1. **Repackage** (§4.1 steps 1–9): the 68 hwmf/hemf files plus the support
   classes not already in the copy, shims for the twelve `sl.*` touch points,
   slf4j logging, no commons-math3, POI's allocation limits kept, Plutext
   notices on every file, `README` recording the upstream tag and commit.
   Acceptance: the copied POI test files render to PNG at a fixed DPI in a
   golden test; `mvn install` clean on the module path (no new `requires`).
2. **Wire in** (§4.2 items 1–2): `MetafileRenderer` in docx4j-core with the
   HWMF/HEMF implementation; the SVG helper in `docx4j-export-fo`
   (`SVGGraphics2D` from batik-svggen, already present) feeding
   `fo:instream-foreign-object`; HTML inline `<svg>` or PNG data URI per the
   image handler; `AbstractWordXmlPicture`'s dead `metaFile` branch replaced;
   `MetafileWmfPart.toSVG()` over HWMF, `MetafileEmfPart.toSVG()`/`toPNG(dpi)`
   added; wmf2svg retired from `docx4j-core/pom.xml` and `module-info.java`;
   `reserveUnpaintablePictures` and the ImageMagick EMF path demoted to a
   fallback for whatever the renderer cannot draw. Acceptance: the nine
   `metafile-samples` files and the Equation Editor / MathType previews in
   the layout-fidelity corpora paint in PDF and HTML; the fidelity
   scoreboards do not fall; font names in metafile text map through
   `PhysicalFonts` (`Drawable.FONT_HANDLER`).
3. **OLE presentation fallback** (§4.2 item 3): `OleObjectBinaryPart
   .getPresentation()` parsing `OlePres000` per MS-OLEDS for objects without a
   `v:imagedata` preview. Acceptance: a document with such an object renders
   its preview.
4. **Missing records** (§1.2, §4.2 item 4): log unimplemented record types at
   debug, count them over the corpora, implement in that order, and offer
   each upstream as a PR. Open-ended; each record shipped is noted here.

## 7. Risks and notes

- Rendering quality is bounded by the EMF+ gaps in §1.2 until phase 4 lands:
  EMF+-only files from GDI+ applications lose ellipses, arcs, curves,
  `DrawString` text and container transforms; EMF+ dual files (Office) fall
  back to their EMF records and are fine.
- These packages parse untrusted bytes; POI shipped a run of bounds-check
  fixes in May–June 2026. The copy must keep the allocation limits and track
  upstream security fixes (§4.1 step 8).
- Searchable text in PDF from metafile `<text>` depends on FOP resolving the
  font; unverified until phase 2.
- Licence: ASL 2.0 clauses 4(b)–4(d) as for the POIFS copy; the one-line
  `legals/NOTICE` entry remains sufficient, extending it to name the two
  packages and the POI version is a courtesy.

## 8. References

- POI HWMF/HEMF source: `poi-scratchpad/src/main/java/org/apache/poi/{hwmf,hemf}` (POI 5.5.1 tag).
- POI rendering documentation: `src/documentation/content/xdocs/components/slideshow/ppt-wmf-emf-renderer.xml`
  (published as "Rendering slideshows, WMF, EMF and EMF+" on poi.apache.org).
- `PPTX2PNG`, `SVGFormat`, `EMFHandler`, `WMFHandler`: `poi-ooxml/src/main/java/org/apache/poi/xslf/util/`.
- MS-WMF, MS-EMF, MS-EMFPLUS: Microsoft Open Specifications for the record formats.
- MS-OLEDS `OLEPresentationStream` and `MetaFilePresentationObject`: presentation streams inside OLE
  compound files (WMF via `CF_METAFILEPICT`, EMF via `CF_ENHMETAFILE`, DIB).
- MS-OE376 Part 4 section 2.3.3.19: permitted VML children of `w:object`.
- docx4j code touched: `org.docx4j.model.images.AbstractWordXmlPicture`, `WordXmlPictureE10`,
  `WordXmlPictureE20`, `org.docx4j.openpackaging.parts.WordprocessingML.Metafile{Wmf,Emf}Part`,
  `BinaryPartAbstractImage`, `OleObjectBinaryPart`, `org.docx4j.org.apache.poi.*`.
