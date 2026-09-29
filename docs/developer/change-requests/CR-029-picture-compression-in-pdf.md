# CR-029: pictures in PDF output as Word compresses them - resolution, and cropped areas deleted

Status: ACTIVE. Proposed 2026-09-29 (Jason asked for the draft, "reflecting Word", with Word's
Compress Pictures dialog as the reference); accepted by Jason Harrop the same day ("commit CR-029
then implement", with the crop drawing defect of §1 to be fixed). Drafted with Claude Opus 5.5.
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

| setting | element | meaning (to be confirmed in phase 0) |
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
