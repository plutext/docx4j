# CR-029: pictures in PDF output as Word compresses them - resolution, and cropped areas deleted

Status: DONE 2026-09-29, all phases (§9). Proposed 2026-09-29 (Jason asked for the draft,
"reflecting Word", with Word's Compress Pictures dialog as the reference); accepted by Jason
Harrop the same day ("commit CR-029 then implement", with the crop drawing defect of §1 to be
fixed). Drafted and implemented with Claude Opus 5.5. Where the implementation departs from §3,
§9 says so and why (chiefly: the default follows Word's PDF export, which phase 0 found differs
from its Compress Pictures).
Owner: Jason Harrop.

Scope: `docx4j-export-fo` (PDF via XSL-FO). The picture docx4j hands FOP becomes the picture Word
would have: cropped to its `a:srcRect`, and resampled to the resolution Word's settings give it,
so a PDF is not several times the size of Word's for the same document. Also a package-level
equivalent of Word's Compress Pictures, for users who want the docx itself smaller. Not in scope:
vector pictures (EMF, WMF, SVG, which pass through as they do), and HTML output (it could share the
code later).

## 1. What docx4j does today (read from the code, 2026-09-29)

- **Every picture reaches FOP byte for byte.** `FileConversionImageHandler.createStoredImage`
  writes the part's bytes to a temporary file, and the FO references it. There is no resampling
  anywhere in docx4j, and FOP does none either. A 4000-pixel photograph shown 2 inches wide is
  embedded at its full 4000 pixels (2000 ppi).
- **Crops are not applied.** `AbstractWordXmlPicture` (around line 260) sets
  `scaling="non-uniform"` and records that "the crop itself is not reproduced (§9.1): the picture
  is stretched into the frame rather than cropped to it". So a cropped picture costs its whole
  bitmap, and it is **drawn wrong**: the whole image is squeezed into the frame, where Word shows
  the cropped part at its natural proportions. This is a fidelity defect as well as a size one.
- **FOP's handling by format**: a JPEG is passed through (DCT); a PNG is decoded and re-encoded
  losslessly (Flate), so a photograph stored as PNG stays large.
- **Repeats are not the problem.** The handler caches by part (`handledImagesMap`), so a logo on
  every page is embedded once.

## 2. What Word does

Word's Compress Pictures dialog (Picture Format > Compress Pictures) offers:

- "Apply only to this picture" / all pictures;
- **"Delete cropped areas of pictures"**;
- **Resolution**: High fidelity (preserves the original); HD (330 ppi); Print (220 ppi);
  Web (150 ppi); E-mail (96 ppi); Use default resolution.

The document-wide defaults are under File > Options > Advanced > Image Size and Quality, stored
in `word/settings.xml` and already bound in docx4j:

| setting | element | meaning (see §9.1: Word's PDF export ignores all three) |
|---|---|---|
| Do not compress images in file | `w:doNotAutoCompressPictures` | Word leaves pictures as inserted |
| Default resolution | `w14:defaultImageDpi w14:val=` | the ppi Word compresses to; 220 when absent (Word's shipped default) |
| Discard editing data | `w14:discardImageEditingData` | drops the pre-edit original Word keeps for undoing picture edits |

In docx4j's real-document corpus (`~/fidelity-real`, 194 documents): 12 carry
`w:doNotAutoCompressPictures`, 4 carry `w14:defaultImageDpi` 0, and 1 carries 300. What 0 means
(High fidelity, most likely) is for phase 0.

Word compresses when it saves a document, so a Word-authored docx usually holds pictures already
at the default resolution, with crops already deleted. The large PDFs come from documents whose
pictures were never through that: docx4j-generated documents, other producers, and documents
saved with "Do not compress". Whether Word's **PDF export** resamples on top of that (and whether
its "Minimum size" option goes further) is also for phase 0.

## 3. Design

### 3.1 Two properties, Word's vocabulary

- `docx4j.convert.out.fo.images.resolution`: `document` (the default), `high-fidelity`, `330`,
  `220`, `150`, `96`. `document` follows the document as Word would: `high-fidelity` when
  `w:doNotAutoCompressPictures` is set, else `w14:defaultImageDpi` (its 0 read as phase 0
  measures), else 220.
- `docx4j.convert.out.fo.images.deleteCroppedAreas`: `true` (the default, as Word's dialog is
  ticked by default): apply the `a:srcRect` crop to the bitmap. `false` embeds the whole bitmap
  and crops with FO instead (a clip), which fixes the drawing without saving the bytes.

Both are also exposed on `FOSettings`, for per-conversion control, overriding the properties.

### 3.2 Where

In `FOConversionImageHandler` (export-fo), which already receives the `AbstractWordXmlPicture`
with the picture's displayed extent and its `srcRect`:

1. Crop to `srcRect` (l, t, r, b in thousandths of a percent) when `deleteCroppedAreas` is on,
   and drop `scaling="non-uniform"` for that picture, since the bitmap's aspect now matches the
   frame.
2. The target is the displayed extent (inches) x the resolution. Downsample only when the bitmap
   has more pixels than that; never upsample.
3. Keep the format: a JPEG stays JPEG (re-encoded at a quality phase 0 matches to Word's); a PNG
   stays PNG (transparency kept). Whether Word turns an opaque PNG photograph into a JPEG is for
   phase 0; if it does, that becomes a third, opt-in setting.
4. The cache key becomes (part, target size), so a picture shown at two sizes gets two
   resamplings, and a picture shown once at one size is still stored once.
5. A picture ImageIO cannot read (a CMYK JPEG, some TIFF) passes through unchanged, with a log
   line, as today.

### 3.3 The package equivalent of Compress Pictures

`org.docx4j.model.images.CompressPictures`: the same crop-and-resample applied to the package's
picture parts, rewriting their bytes and zeroing the `srcRect`, so the docx itself is smaller
(what Word does on save). A picture used at two sizes keeps the larger. It takes the same
resolution values, and is useful before conversion, before sending a document on, and for the
anonymiser's placeholder pictures.

## 4. Phases

0. **Measure Word** (probes through WordGoldenRunner; `pdfimages -list` for the pixel size,
   format and bytes of each embedded picture; the resaved docx for what Word stored):
   - one 4000 x 3000 JPEG and one opaque PNG photograph shown 2 in wide, with the settings
     absent, `defaultImageDpi` 96/150/220/330/0, and `doNotAutoCompressPictures`;
   - the same picture cropped (`srcRect`) 50%;
   - a transparent PNG.

   **Gate**: the table of what Word stores and what its PDF embeds for each, recorded here. It
   decides the default, the meaning of 0, JPEG quality and the PNG question.
1. **Crops** (the fidelity fix): `srcRect` applied, drawn as Word draws it. Gate: the harness's
   probes and corpora, no page or line regression, and the cropped-picture documents closer to
   Word by pixel compare. The partition is every document with a `srcRect` (the 651-of-1737 note
   in `AbstractWordXmlPicture` says how common declared-extent mismatches are).
2. **Resolution**: the two properties and `FOSettings`. Gate: layout identical, since the
   displayed size doesn't change, so page and line parity must not move at all; PDF bytes
   measured per corpus against Word's goldens; a pixel compare at print resolution.
3. **`CompressPictures`** for the package, with tests; Getting Started paragraph; CHANGELOG.

## 5. Risks

- **JPEG re-encoding** loses quality; hence never upsample, only resample above the target, and
  match Word's quality. `high-fidelity` keeps today's behaviour exactly.
- **ImageIO gaps**: CMYK and YCCK JPEG, some TIFFs and ICC-profiled images. Pass through
  unchanged rather than risk colour shifts.
- **Time**: resampling large pictures costs CPU and memory per conversion; cache per part and size.
- **The default changes output.** From phase 2 a document with large pictures produces a
  smaller PDF by default. That's intended, but it's a visible change: CHANGELOG, with
  `high-fidelity` as the way back.

## 6. What a user can do today

- A custom `ConversionImageHandler`: subclass `FOConversionImageHandler`, override
  `createStoredImage` to resample, and pass it with `foSettings.setImageHandler(...)`.
- Pre-process the package: downsample the `BinaryPartAbstractImage` parts' bytes before
  converting.
- Post-process the PDF, e.g. Ghostscript `-dPDFSETTINGS=/ebook`.

## 7. Effort (rough)

Phase 0: a day, with Word goldens. Phase 1: two days. Phase 2: two days. Phase 3: a day.

## 8. References

- `docx4j-core/.../model/images/FileConversionImageHandler.java`, `AbstractWordXmlPicture.java`
  (the crop note), `AbstractConversionImageHandler.java` (the cache).
- `xsd/wml/wml.xsd` (`w:doNotAutoCompressPictures`), `xsd/wml/w14_word_2010_wordml.xsd`
  (`w14:defaultImageDpi`, `w14:discardImageEditingData`).
- Word: Picture Format > Compress Pictures; File > Options > Advanced > Image Size and Quality.

## 9. Record

### 9.1 Phase 0: what Word does (measured 2026-09-29)

Measured on Word's own PDFs of docx4j's corpora (the goldens, cut by WordGoldenRunner), rather
than on new probes: `pdfimages -list` for each picture's pixel size, encoding and bytes, paired
with the stored bitmaps. The 29 real documents with a crop (`a:srcRect` or a VML crop) were read
closely; the rule was then checked across the corpora.

- **Word's PDF export resamples to 200 ppi, and only above 300 ppi**, at the size shown. Of 124
  pictures paired with their stored bitmaps, every one Word resampled came out at 200 ppi (from
  398 ppi and up), and every one it kept was 300 ppi or less.
- **It rounds down**: a frame 402.5 px wide at 200 ppi is 402 px, and 902.97 is 902. `floor`
  matched 15 of 28 resampled pictures, and round-to-nearest matched 1; the misses are 1 px under.
- **It ignores the document's compression settings.** Documents with
  `w:doNotAutoCompressPictures` still have their pictures at 200 ppi in Word's PDF. Those settings
  govern Compress Pictures when Word saves, not the PDF export. In the corpora `w14:defaultImageDpi`
  occurs as 0 and as 32767 (both read here as High fidelity) and as 300.
- **JPEG quality 75**: the quantisation tables of Word's PDF JPEGs say quality 75, cropped ones
  included. **It re-encodes JPEGs it does not resample**: for 402 JPEGs at the same pixel size in
  Word's PDFs and docx4j's, Word's came to 15.1 MB and the stored originals to 22.6 MB.
- **PNGs mostly stay lossless.** Of about 360 kept pictures, 25 changed format (PNG to JPEG),
  nearly all one repeated picture in one document, so no general rule to follow.

### 9.2 What was built, and the departures from §3

- **Crops** (`PictureCrop`, read from `a:srcRect` in `WordXmlPictureE20` and from `v:imagedata`'s
  crop attributes in `WordXmlPictureE10`). The image handler cuts the bitmap by the edges which
  move in. An edge which moves out (a negative value: the picture inset in its frame) is not in
  the bitmap, so it becomes padding on the `fo:external-graphic`, with the content box reduced
  to keep the frame's size; `WordLayoutFixups` and `TableWriter` add the padding back where they
  read a picture's size. Word draws it that way (checked by eye on the corpus document with
  l -2.5%, t -33.1%: the picture sits below an empty band, as in Word).
- **Resolution** (`PictureCompression`): `docx4j.convert.out.fo.images.resolution`, **default
  `word`** (Word's PDF export: above 300 ppi to 200, rounded down, and a JPEG left at its size
  re-encoded at quality 75 where that saves a tenth of its bytes, which leaves one already at or
  below that quality alone). `document` is the §3.1 design (Compress Pictures with the document's
  settings: none for "do not compress", else `w14:defaultImageDpi`, else 220); `high-fidelity` the
  original bitmaps; a number, that resolution. **Departure**: §3.1 proposed `document` as the
  default; phase 0 showed that is not what Word's PDF has.
- **The crop switch** is `docx4j.convert.out.fo.images.crop` (default true), not
  `deleteCroppedAreas`. **Departure**: false restores the pre-17.3.0 drawing (the whole bitmap
  stretched into the frame) rather than cropping with an FO clip; a clip would keep the bytes and
  fix the drawing, but no reader of the output asked for that, and one switch back to exactly the
  old output is the safer escape hatch.
- **JPEG quality** `docx4j.convert.out.fo.images.jpegQuality`, default 0.75.
- **FOSettings.setImageResolution / setImageCrop** override the properties per conversion
  (through `FOConversionContext` onto the image handler).
- **Where**: in `AbstractConversionImageHandler` (docx4j-core) rather than `FOConversionImageHandler`,
  for pictures built for XSL-FO only, so HTML output is unchanged. The cropped or resampled
  bitmap is stored as a derived part of its own, keyed by part, crop, resolution and frame size,
  so two crops of one bitmap are two files and a repeated one is still stored once.
- **`CompressPictures`** (phase 3): Word's Compress Pictures on a package. DrawingML pictures in
  the document, headers, footers, footnotes, endnotes and comments; JPEG and PNG parts only
  (their format is kept); a bitmap which pictures crop differently keeps its crops and is
  resampled for the largest share any of them shows; what remains of a crop (edges which move
  out) is rewritten relative to the cut bitmap. Resolution default `document`.
- Documentation: `docx4j-fo.properties` (the three properties), Getting Started ("Pictures." in
  the PDF via XSL FO section; md, html and pdf regenerated), CHANGELOG.

### 9.3 Gate

Tests: `PictureCompressionTest` (export-fo, 19) and `CompressPicturesTest` (core-tests, 6); export-fo
227/227, core-tests 1335/0 (11 skipped, as before).

Corpora, the harness on Apache FOP, baseline `cr029-base` (crop off, high-fidelity: the
pre-17.3.0 output) against the candidate with the defaults, probes against `goldens-nofields`,
real/real2/real3 with `hyphenate=false`:

| check | result |
|---|---|
| layout (word positions, `pdftotext -bbox`) | 596 of 598 identical; the other 2 differ only in a clock field (TIME/PRINTDATE) that ticked between runs |
| scoreboards | 0 changed documents in every corpus |
| picture bytes in the PDFs | real 0.9 -> 0.7 MB (Word 0.7); real2 13.0 -> 7.6 (Word 6.7); real3 140.3 -> 64.7 (Word 48.5) |
| pictures matching Word's pixel size | real 122 -> 131 of 157; real2 507 -> 574; real3 3293 -> 3641 |
| the 29 crop documents | 295 -> 357 of Word's 563 pictures; drawn as Word draws them (checked by eye on two) |

The line score cannot see a picture's content, only where the text is, so the drawing fix shows
in the pixel sizes and by eye rather than on the scoreboards.

### 9.4 Left for later

- **Fonts, not pictures, are now most of the size gap.** Whole PDFs: real2 200.7 MB against Word's
  50.9, of which pictures are 7.6 against 6.7. docx4j's `+noliga` font copies are declared
  single-byte, and FOP embeds a single-byte font whole, not as a subset (one document: 982 KB;
  183 KB with no twin; Word 264 KB). With the fork's gsub-features hook (CR-020) the twin could
  give way to `-liga` on the CID declaration, which is subset. That belongs to CR-020's switch.
  Done there 2026-09-30 ("The twin gives way"): real 181.5 MB to 11.8, real2 200.7 to 19.3,
  real3 208.6 to 75.0, against Word's 39.1 / 50.9 / 99.7; docx4j's PDFs are now smaller than
  Word's on every corpus.
- A metafile picture's crop is not applied (it is drawn as SVG, whole).
- Word draws a picture's outline (`a:ln`); docx4j does not (older than this CR).
- VML pictures in `CompressPictures`.
- One corpus document draws, beside each of 75 pictures, a second small one Word does not
  (older than this CR; not investigated).

