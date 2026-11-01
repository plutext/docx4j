# org.docx4j.org.apache.poi.hwmf / .hemf — repackaged Apache POI HWMF & HEMF

WMF, EMF and EMF+ parsing plus playback onto any `java.awt.Graphics2D`, repackaged
from Apache POI. This README covers **both** the `hwmf` and the `hemf` package
trees, and the support classes copied alongside them.

Landed by CR-011 phase 1 (`docs/developer/change-requests/CR-011-metafile-rendering.md`),
docx4j 17.1.0. Phase 1 is repackaging only: nothing in docx4j calls this code yet.

## Upstream baseline

Copied from the **Apache POI 5.5.1 release source jars** (not trunk — trunk is
6.0.0-SNAPSHOT and needs Java 17; docx4j targets Java 11).

| Jar | SHA-1 |
|---|---|
| `poi-scratchpad-5.5.1-sources.jar` | `b84066a4645892f5ca86728600e2469a7b2be829` |
| `poi-5.5.1-sources.jar` | `3801900180c5d56c05837e29dd6850df7e39e6ae` |

(both from Maven Central, `org/apache/poi/`; the sha1s are the ones Central publishes.)

## What was copied

**Verbatim from `poi-scratchpad`, package-renamed** — 68 files:

- `org/apache/poi/hwmf/{usermodel,record,draw}` → `org/docx4j/org/apache/poi/hwmf/...`
- `org/apache/poi/hemf/{usermodel,record/emf,record/emfplus,draw}` → `org/docx4j/org/apache/poi/hemf/...`

**Support classes newly copied from `poi` core**, package-renamed:

- `util/Dimension2DDouble`, `util/Units`
- `common/Duplicatable`
- `common/usermodel/fonts/{FontCharset, FontFacet, FontFamily, FontGroup, FontHeader, FontInfo, FontPitch}`
- `sl/usermodel/PictureData` (interface + `PictureType` enum)
- `sl/draw/{Drawable, ImageRenderer, BitmapImageRenderer, EmbeddedExtractor, DrawFontManager, DrawFontInfo, DrawFontManagerDefault}`

**Support classes already present in docx4j's POI copy and reused unchanged**:
`util/{Beta, BitField, BitFieldFactory, GenericRecord*, IOUtils, Internal, LittleEndian,
LittleEndianConsts, LittleEndianInputStream, LocaleUtil, RecordFormatException, StringUtil}`,
`common/usermodel/GenericRecord`, `poifs/filesystem/FileMagic`.

Those were diffed against 5.5.1 before the copy. `IOUtils`, `LittleEndianInputStream`,
`StringUtil`, `GenericRecordUtil`, `GenericRecordJsonWriter` and `Beta` are already
at the 5.5.1 level (only the logging backend and `@Internal` differ), so **no util
class had to be upgraded** and no existing POIFS/HPSF caller was touched.
`FileMagic` is byte-identical to 5.5.1 and already carries the WMF/EMF signatures.
The remaining differences are cosmetic (brace style, `@author` tags) plus:

- `LittleEndian` keeps a `getByteArray(byte[],int,int)` method upstream has dropped;
- `LocaleUtil` does not register a `ThreadLocalUtil` cleaner (not used here).

## What was changed

1. **Package rename.** Every `org.apache.poi` → `org.docx4j.org.apache.poi`. Nothing else,
   in the great majority of files.
2. **Logging.** log4j2 (`org.apache.logging.log4j.Logger` + `org.apache.poi.logging.PoiLogManager`)
   → slf4j (`org.slf4j.Logger` + `LoggerFactory`), as the rest of docx4j's POI copy does.
   slf4j 2.x has the same fluent builder (`LOG.atWarn().log("… {}", x)`), so the call
   sites are unchanged. The single `Unbox.box(x)` argument (in `HemfComment`) became a
   plain `x`.
3. **commons-math3 removed.** `HemfPlusDraw.EmfPlusDrawImagePoints` used
   `MatrixUtils`/`LUDecomposition`/`RealMatrix` to invert a 3×3 matrix. Replaced with
   local `invert3x3` / `multiply3x3` helpers in the same class (adjugate / determinant).
   The matrices have a `{1,1,1}` bottom row, so `AffineTransform.createInverse()` is
   not applicable. A singular matrix now raises `RecordFormatException` where
   commons-math3 raised `SingularMatrixException` (both unchecked).
   *(Note: CR-011 §1.3 attributed these three uses to `HemfPlusBrush`; in 5.5.1 they
   are in `HemfPlusDraw`.)*
4. **`sl.*` shims.** POI's `hwmf`/`hemf` reach into the slideshow layer at three points
   (§1.3 of the CR). Rather than copy ~100 `sl.draw`/`sl.draw.geom`/`sl.usermodel`
   classes, docx4j provides:
   - `sl/draw/DrawFactory` — **docx4j shim.** Only `getInstance(Graphics2D)` and
     `getFontManager(Graphics2D)`, resolved through the `Drawable.FONT_HANDLER`
     rendering hint exactly as POI does. Replaces POI's full `DrawFactory`.
   - `sl/draw/Docx4jDrawFontManager` — **docx4j class.** The default font manager:
     maps a GDI face name through `org.docx4j.fonts.PhysicalFonts` and, if AWT does not
     know the resulting family, builds the `java.awt.Font` from docx4j's font file with
     `Font.createFont` (cached). Falls back to POI's `DrawFontManagerDefault` behaviour.
   - `sl/draw/ImageRendererFactory` — **docx4j shim.** `getImageRenderer(Graphics2D, contentType)`,
     replacing `DrawPictureShape.getImageRenderer(..)` (POI itself carries a TODO to move
     that method out of the slideshow code). Dispatches to the `Drawable.IMAGE_RENDERER`
     hint, then `BitmapImageRenderer`, `HwmfImageRenderer`, `HemfImageRenderer`.
     POI used a `ServiceLoader`, which does not work on the module path.
   - `sl/draw/DrawPaint` — **trimmed copy**: only `RGB2SCRGB` / `SCRGB2RGB`, used by the
     EMF+ gradient code.
   - `sl/draw/Drawable`, `ImageRenderer`, `BitmapImageRenderer`, `EmbeddedExtractor`,
     `DrawFontManager`, `DrawFontInfo`, `DrawFontManagerDefault`, `sl/usermodel/PictureData`
     — verbatim copies (nothing in them touches the rest of the slideshow layer).

   The only edits inside the 68 copied files for this are two `DrawPictureShape` →
   `ImageRendererFactory` references (`HwmfGraphics.getImageRenderer`, and an import +
   javadoc link in `HwmfImageRenderer`).
5. **Attribution.** Every copied file opens with the Plutext notice above the retained
   ASF header (ASL 2.0 clauses 4(b)/4(c)). docx4j-authored shim files say so explicitly
   in their notice. `legals/NOTICE` names both packages and POI 5.5.1.

6. **Post-5.5.1 upstream fixes back-ported.** POI 5.5.1 was released 2025-11-30. Between
   then and this copy, upstream fixed an NPE and a run of unbounded-allocation defects in
   exactly these packages — the ones CR-011 §7 calls out. They are back-ported here,
   verbatim apart from the rename, because these classes parse untrusted bytes:

   | POI commit | date | what |
   |---|---|---|
   | `9c2f487c98` | 2026-01-16 | Bug 69927: `HwmfBitmapDib` NPE when `headerBitCount` is unset (this is what stopped `file-45.wmf` parsing at all) |
   | `22531fe638` | 2026-01-16 | `HwmfBitmapDib.MAX_HEIGHT_WIDTH` (10 000 px, settable): stop a bogus DIB header allocating a huge `BufferedImage` |
   | `0ae15edb2c` | 2026-01-20 | `HemfDraw.MAX_NUMBER_OF_POLYGONS` allocation check in `EmfPolyPolygon` |
   | `d80ff2dbfb` | 2026-05-11 | `HemfHeader`: validate the EMF description bounds before allocating |
   | `874b21c5bd` | 2026-05-26 | `HemfPlusPath`: out-of-bounds `Arrays.fill` in the RLE point-type expansion |
   | `d290db190b` | 2026-06-03 | allocation bounds checks on EMF+ path and brush element counts |
   | `c60d26562d` | 2026-06-04 | allocation bounds checks in `EmfPolyDraw` and `WmfCreateRegion` |

   Deliberately **not** back-ported:
   - `b7f1f55a47` ("hemf specific limits") — it changes the shared `org.apache.poi.util.IOUtils`
     and adds `ArrayUtil`; `IOUtils` is shared with docx4j's POIFS/HPSF copy, so touching it
     is out of scope here. Its `HemfPicture.safelyAllocateCheck` helper is replaced in this
     copy by the equivalent `IOUtils.safelyAllocateCheck(n, HwmfPicture.getMaxRecordLength())`
     calls that `d290db190b` used.
   - `103dd47963` — only re-routes the check `d290db190b` added through that new helper.
   - `ffd05bbaae` — adds diagnostic strings to `IOUtils.safelyAllocate` overloads that
     `b7f1f55a47` introduced; no behavioural change.

   These become no-ops on the next sync, once the baseline is a POI release that contains them.

7. **Second round of back-ports (November 2026).** Against POI trunk `521b26fad9`
   (2026-11-01), after POI 6.0.0 (2026-10-19), which needs Java 17 and so cannot be the
   baseline. Taken: every upstream change since June to these packages that fixes a named
   defect, with its test (docx4j-core-tests `org.docx4j.metafiles`, and
   `org.docx4j.org.apache.poi.hemf.record.emf.HemfRecordBoundsTest` for the package-private
   ones):

   | POI commit | date | what |
   |---|---|---|
   | `e54ba888e2`, `ab196a7441`, `23369586da`, `1d9425abb3`, `03d1ec1670`, `f6f957bb5d` | 2026-01-18 .. 11-01 | a bare `assert` becomes a logged warning (`HwmfEscape`, `HemfPlusHeader`, `HemfFill`, `HwmfHeader`, `HemfPlusMisc`); `23369586da` also drops two overrides identical to their parents' |
   | `d4178c1d5c`, `5fb3244453` | 2026-09-12, 10-22 | a bare `assert` becomes an exception (`HemfFill.readBitmap`, `HemfMisc.EmfEof`) |
   | `bc02ef0703`, `3ebc5e5d97` | 2026-02-14, 10-11 | `HwmfMisc`: an unknown brush style or colour usage is an `IllegalArgumentException`, not an NPE later |
   | `1a91f5cdfc` | 2026-04-20 | `HwmfPlaceableHeader`: `unitsPerInch` read unsigned, and 0 rejected (it scaled without bound) |
   | `e5d0cad1d5`, `69f8eb6fb8` | 2026-06-01, 06-04 | `EmfPlusDrawDriverString`'s glyph byte length as a long; `EmfExtCreatePen`'s style entries bounded |
   | `ab28e7eaec`, `45baf19dcc` | 2026-06-03, 09-22 | `HwmfBitmapDib`: the image size bounded before it is narrowed to an int |
   | `dfa53082b3` | 2026-07-09 | `BitmapImageRenderer`: an image, crop or anchor of size 0 draws nothing (a crop to 0 put an infinite scale into the transform, which Batik writes as invalid SVG) |
   | `373fd4a486` | 2026-09-20 | `HwmfHeader`: an invalid metafile type is an `IOException`, not an AIOOBE (POI bug 69924) |
   | `941f09ecce` | 2026-09-22 | unsigned 32-bit counts checked while still longs (`HemfComment`, `HemfDraw`) |
   | `e9e9612a1f` | 2026-02-21 | `HemfPicture.getHeader()`: a first record that is not the header is a `RecordFormatException`, not a `ClassCastException` |
   | `640ae9d3ac`, `440834229f`, `aa7dc402b2` | 2026-10-16 | `HemfFill.readBitmap`: the bitmap must end within its record, its size must fit an int, the buffer presize is capped; `HemfEmbeddedIterator` presizes at most 4 MiB |

   Departures from upstream, each marked `docx4j:` in the code:
   - `5fb3244453`'s third check, an EOF record whose size disagrees with its fields, **logs**
     rather than throws: the record's remaining bytes are skipped either way, and since
     `PoiMetafileRenderer` parses every record before drawing, an exception in the last record
     would lose a picture that otherwise draws whole.
   - Where upstream calls `HemfPicture.getMaxRecordLength()` or `HemfPicture.safelyAllocateCheck`
     (both from `b7f1f55a47`, not taken), this copy uses `HwmfPicture.getMaxRecordLength()` and
     `IOUtils.safelyAllocateCheck(n, HwmfPicture.getMaxRecordLength())`, as in round one.

   Not taken:
   - `140db93794` (`HemfComment`'s limit from 1 MB to 100 MB): the 1 MB limit came with
     `b7f1f55a47`, so this copy never had it; `HemfRecordBoundsTest.embeddedWmfOverOneMegabyte`
     pins the 100 MB.
   - `372388b7ed` (`BitmapImageRenderer.setAlpha` treating 0 as transparent): no docx4j path
     calls `setAlpha`. Taken as it stands it also makes `HwmfImageRenderer.getImage()` and
     `HemfImageRenderer.getImage()` return a transparent image, since their `alpha` defaults to 0.
   - The integer-hardening series (`b7f72c8f32` `MathUtil`, `cca6d74ba0`, `702aca1a9f`,
     `9727f3eade`, `720b6321d3`, `2344a81268`, `d0eb055f99`): silent narrowing becomes an
     exception with no named defect, and one exception aborts a WMF (below), so it could blank
     pictures that draw today.
   - The Java 17 refactors (`337256b155`, `2ed376731f`, `7d818ee3f9`, `1539088d51`,
     `7bff84157f`, `46b25f6063`, `789cc69ec6`, `c192e8c54d`, `413719df29`): no behaviour change,
     and switch expressions do not compile at release 11.
   - `24fa97f624` (public mutable statics made private): API tidying only.

No other functional changes were made: no records were implemented.

**How much this copy differs from upstream:** 12 of the 68 hwmf/hemf files differed from
5.5.1 beyond the package rename after round one; round two (item 7) touched 14 more, and every
difference is one of the seven categories above. Of the copied support classes, only
`common/usermodel/fonts/FontCharset` and `sl/draw/BitmapImageRenderer` differ (the logging swap,
and in `BitmapImageRenderer` `dfa53082b3`). To check this yourself:

```bash
# un-rename a copied file and strip the 6-line Plutext notice, then diff
sed 's/org\.docx4j\.org\.apache\.poi/org.apache.poi/g' <file> | tail -n +7 \
  | diff - <5.5.1 sources>/org/apache/poi/<same path>
```

## Known gaps found while testing (see docx4j-core-tests `org.docx4j.metafiles`)

- `wrench.emf` (a POI test file): its 35 `polyPolygon16` records parse and replay without
  throwing, but nothing reaches the canvas. An upstream rendering gap; pinned in
  `MetafileTestFiles.RENDERS_BLANK`.
- `VHZ2NYFUYUUJNGLABL26ORTQZA76FJEW.emf`: its first parsed record is an `EmfPolyline`, so
  `HemfPicture.getHeader()` throws, and with it `getBounds()`/`getSize()`: a
  `RecordFormatException` since `e9e9612a1f` (round two), a `ClassCastException` before.
- `HwmfPicture.draw` does **not** swallow per-record exceptions, whereas `HemfPicture.draw`
  does. So one bad record aborts a whole WMF (`file-45.wmf` demonstrates this once the
  `MAX_HEIGHT_WIDTH` guard fires). CR-011 phase 2 must catch this at the docx4j boundary
  rather than let a picture take down a conversion.
- The parsers use a bare **`assert`** for "this cannot be right" checks: 48 after round two,
  which took upstream's replacement of 15 of them (`HwmfRegionMode`, `HemfComment`,
  `HemfEmbeddedIterator`, `HemfPlusDraw`, ... keep theirs, as upstream does). With
  `-ea` — which Surefire enables — a malformed file raises `AssertionError` well before the
  bounds check that would otherwise have caught it; in production, assertions are off and
  those checks do nothing. So the class of failure a bad metafile produces differs between
  docx4j's test runs and a user's JVM. Callers must therefore catch `Throwable`, not just
  `RuntimeException`, around a parse of untrusted bytes.

## Safety limits

These packages parse untrusted bytes, so POI's allocation guards are kept intact:

- `IOUtils.safelyAllocate(long, int)` / `safelyAllocateCheck` — 11 call sites, unchanged.
- `HwmfPicture.MAX_RECORD_LENGTH` and `HemfEmbeddedIterator.MAX_RECORD_LENGTH` — POI's
  default of **100 MB** each, settable with `setMaxRecordLength(int)`.
- `HemfPlusBrush.MAX_OBJECT_SIZE` / `HemfPlusDraw.MAX_OBJECT_SIZE` — 1 MB.
- `IOUtils.setByteArrayMaxOverride(int)` — a global, shared with the POIFS copy;
  left at POI's default of `-1` (no override, so the per-call `maxLength` applies).
  Do **not** change that default here; the POIFS/HPSF code uses the same class.

POI shipped a run of bounds-check fixes to `HwmfBitmapDib`, `HemfDraw`, `HemfHeader`,
`HemfPlusPath`, `HemfPlusBrush` and `HwmfWindowing` between January and June 2026 —
i.e. **after** the 5.5.1 release. They are back-ported here; see the table under
"What was changed".

## How to sync with upstream

1. Fetch the new `poi-scratchpad-<v>-sources.jar` and `poi-<v>-sources.jar` from Central
   and verify their sha1s.
2. Diff `org/apache/poi/h{w,e}mf` in the new jar against the 5.5.1 baseline (**not**
   against this tree — this tree has the package rename applied).
3. Apply that diff here after `sed 's/org\.apache\.poi/org.docx4j.org.apache.poi/g'`, then
   re-apply the deltas listed under "What was changed" (they are small and localised:
   grep for `docx4j:` comments, `LoggerFactory`, `ImageRendererFactory`, `invert3x3`).
4. Re-check the `util` classes with the same diff-before-copy procedure.
5. Run `MetafileParseTest`, `MetafileRenderTest`, `MetafileGoldenImageTest` and
   `HwmfParsingTest` / `HemfPictureTest` in docx4j-core-tests.
6. Anything docx4j implements here that upstream lacks (see below) should be offered
   upstream as a PR — it keeps the copies converging.

## Not implemented (inherited from POI 5.5.1)

WMF: all 69 record types are implemented.

**EMF — 26 record types parse but do nothing** (`UnimplementedHemfRecord`):
SETCOLORADJUSTMENT, SETMETARGN, ANGLEARC, SETARCDIRECTION, MASKBLT, PLGBLT,
CREATECOLORSPACE, SETCOLORSPACE, DELETECOLORSPACE, GLSRECORD, GLSBOUNDEDRECORD,
PIXELFORMAT, DRAWESCAPE, EXTESCAPE, SMALLTEXTOUT, FORCEUFIMAPPING, NAMEDESCAPE,
COLORCORRECTPALETTE, SETICMPROFILEA, SETICMPROFILEW, SETLAYOUT, TRANSPARENTBLT,
GRADIENTFILL, SETLINKDUFIS, COLORMATCHTARGETW, CREATECOLORSPACEW.

Of these, GRADIENTFILL, TRANSPARENTBLT, MASKBLT, PLGBLT, ANGLEARC, SMALLTEXTOUT and
SETLAYOUT (RTL) are the ones likely to appear in real documents.

**EMF+ — 29 record types parse but do nothing** (`UnimplementedHemfPlusRecord`):
Comment, MultiFormatStart, MultiFormatSection, MultiFormatEnd, Clear, FillPolygon,
DrawLines, FillEllipse, DrawEllipse, FillPie, DrawPie, DrawArc, FillClosedCurve,
DrawClosedCurve, DrawCurve, DrawBeziers, DrawString, SetTextContrast, BeginContainer,
BeginContainerNoParams, EndContainer, TranslateWorldTransform, ScaleWorldTransform,
RotateWorldTransform, OffsetClip, StrokeFillPath, SerializableObject, SetTSGraphics,
SetTSClip.

What EMF+ *does* implement: paths, rectangles, regions, images, world transform
set/reset/multiply, clipping by rect/path/region, and the object records. Office's
"dual" EMF+ files (which carry EMF fallback records alongside) therefore render;
EMF+-only files from GDI+/.NET applications lose ellipses, arcs, curves, `DrawString`
text and container-based transforms. Closing that gap is CR-011 phase 4.

There are also ~45 `TODO` / "to be validated" / "not supported" markers in the copied
code, notably around line-width calculation, hatch brushes, gradient colour lists and
`HwmfBitmap16`.

## Entry points

```java
HwmfPicture wmf = new HwmfPicture(inputStream);
wmf.draw(graphics2D);                  // or draw(graphics2D, targetBounds)
wmf.getBounds(); wmf.getBoundsInPoints(); wmf.getSize(); wmf.getRecords();
wmf.getPlaceableHeader();              // the Aldus placeable header Word writes

HemfPicture emf = new HemfPicture(inputStream);
emf.draw(graphics2D, targetBounds);    // HemfPicture has no one-arg draw
emf.getBounds(); emf.getBoundsInPoints(); emf.getSize(); emf.getRecords();
emf.getEmbeddings();                   // nested WMF/EMF/bitmaps
```

Rendering hints that matter (`org.docx4j.org.apache.poi.sl.draw.Drawable`):
`FONT_HANDLER` (a `DrawFontManager`), `DEFAULT_CHARSET` (a `java.nio.charset.Charset`,
defaults to windows-1252), `EMF_FORCE_HEADER_BOUNDS` (`Boolean`), `IMAGE_RENDERER`.
