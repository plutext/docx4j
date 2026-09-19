# Intos

A small typeface family designed to be metric-compatible with Microsoft's Aptos family.

Based on [Inter](https://github.com/rsms/inter) and [Gelasio](https://github.com/SorkinType/Gelasio), with a series of manual and programmatic alterations.

![Intos type specimen](preview.png)

Text set in Aptos keeps its line breaks and pagination when it is rendered with Intos.

- `Intos.glyphspackage` — **Intos** (sans), derived from [Inter](https://github.com/rsms/inter).
  Regular, Bold, Italic, Bold Italic, and Semibold / Semibold Italic review masters, in final font coordinates, TrueType
  outlines, with Aptos's kerning and the `ccmp`/`locl` features and mark anchors.
- `IntosDisplay.glyphspackage` — **Intos Display**, the four Intos masters with the
  Aptos → Aptos Display change replayed on them programmatically
- `IntosNarrow.glyphspackage` — **Intos Narrow**, likewise with the Aptos → Aptos Narrow change
  (narrower advances and counters, raised x-height, Aptos Narrow's metrics and kerning)
- `IntosSerif.glyphspackage` — **Intos Serif**, derived from [Gelasio](https://github.com/SorkinType/Gelasio):
  Gelasio's full glyph set (Latin, Cyrillic, small caps, figure styles) re-proportioned to
  Aptos Serif, with Gelasio's serifs squashed to Aptos Serif's depth and lengthened to its
  overhang, Aptos Serif's metrics and kerning.
- `fonts/` — the built TTFs (Intos, Intos Display, Intos Narrow and Intos Serif).

Each source is a native Glyphs package. Open the `.glyphspackage` in Glyphs 4
as usual; in Finder, choose **Show Package Contents** to browse its files.

- `glyphs/g.glyph` contains all masters of lowercase **g**; each glyph has its own file.
- `glyphs/G_.glyph` contains uppercase **G** (underscores distinguish uppercase filenames).
- `fontinfo.plist` contains shared metrics, kerning, features, and export settings.
- `order.plist` preserves glyph order; `UIState.plist` stores editor tabs separately.

Edit these packages, then rebuild without opening Glyphs:

```sh
./scripts/build-fonts.sh
./scripts/build-preview.sh
```

Licensed under the SIL Open Font License 1.1 — see `LICENSE.txt`.
