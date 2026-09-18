<div align="center">

# Akasia

**A libre, metric-compatible companion to Microsoft Aptos**  
in the tradition of Carlito for Calibri, Caladea for Cambria, and Arimo for Arial.

[![Licence: OFL-1.1](https://img.shields.io/badge/licence-OFL--1.1-2ea043)](OFL.txt)
&nbsp;![Styles: 12](https://img.shields.io/badge/styles-12-1d3b5e)
&nbsp;![Advance Δ: 0.00%](https://img.shields.io/badge/advance%20%CE%94-0.00%25-1d3b5e)

<img src="docs/specimens/hero.svg" width="780" alt="Akasia, the quick brown fox jumps over the lazy dog">

</div>

Akasia pairs libre outlines with Aptos's metrics. Across the gated conformance
corpus, substitution preserves every line and page break, yet every outline
comes from an OFL-licensed font rather than from Aptos.

## Specimen

Six weights, Light to Black, each matched to Aptos's stem by binary-searching the
donor's variable axis rather than by trusting a named instance.

![The six upright weights of Akasia, Light to Black](docs/specimens/waterfall.svg)

### Roman and italic

Twelve styles in all. Every weight has a true italic, grafted from the donors'
italic sources and never synthesised by slanting the upright.

![The six italic weights of Akasia](docs/specimens/italics.svg)

### Character set

Latin with its accents, figures and fractions, punctuation, currency,
mathematics, Greek and Cyrillic: about 99% of Aptos's repertoire.

![Akasia's character set](docs/specimens/glyphs.svg)

### In text

![A paragraph set in Akasia at reading size](docs/specimens/text.svg)

## Compared with Aptos

Set the same document in Aptos and in Akasia and superimpose the two. Black is
where they agree, colour only where the curves part. Every encoded advance and
every adjustment in the committed kerning repertoire is Aptos's, so the tested
pages break in the same places and only the shapes change.

![Akasia superimposed on Aptos: mostly black, with red and blue only where the outlines diverge](docs/comparison/detail.png)

<sub>**black**, identical. **red**, Aptos only. **blue**, Akasia only.</sub>

The whole booklet, [overlaid page by page](docs/comparison.md).

## What it is

Akasia is a composite of eight OFL-1.1 families:

- Source Sans 3 (Adobe) is the base. Most of the alphabet, where its shapes
  already sit close to Aptos, stays Source Sans.
- In the uprights, the 28 letters whose Source Sans shape strays furthest from
  Aptos are each replaced by the closest-matching donor. The italics independently
  select 24 of those outliers under a six-weight non-regression rule. Every
  graft is weight-matched and re-spaced to sit like the base. A conservative
  per-style optical pass then evens their stroke colour on the donors' real
  weight axes without accepting a final raster that moves farther from Aptos.
- Greek, Cyrillic, currency and symbols come from Open Sans, Arimo and Noto Sans
  Symbols.

The metrics are Aptos's, taken as data: advance widths, per-glyph ink-centre
seating, the committed kerning repertoire and vertical metrics. No Aptos outline
is ever copied, and that line is the whole legal basis of the face.

## The family

Twelve static styles: Light, Regular, SemiBold, Bold, ExtraBold and Black, each
with a true italic.

| Weight    | Class | Italic |
| --------- | ----- | ------ |
| Light     | 300   | yes    |
| Regular   | 400   | yes    |
| SemiBold  | 600   | yes    |
| Bold      | 700   | yes    |
| ExtraBold | 800   | yes    |
| Black     | 900   | yes    |

Weights are matched by stem, not by name: the build binary-searches each variable
axis for the instance whose `I`-stem equals Aptos's. Italics are matched
independently with an area/height thickness measure that excludes their slant.
Black exceeds the Source Sans axis, so each orientation extrapolates its own
weight model past the axis top, holding the shape topology, until the stem
matches. Italics graft from the donors' italic variable fonts, never by slanting
the upright; their native angles are normalised to Aptos's measured -12 degrees
before composition.

## Donors and the graft map

| Donor                | Upright map (28)       | Italic map (24)       |
| -------------------- | ---------------------- | --------------------- |
| Source Sans 3 (base) | all other letters      | all other letters     |
| Hanken Grotesk       | C G O U c g h j l m u  | C G h u               |
| Albert Sans          | N a s t w              | D W t                 |
| Figtree              | D K M k r              | H K M N O U k m w     |
| Open Sans            | H V W X                | none                  |
| Public Sans          | J Q                    | J X a c f g s         |
| Arimo                | f                      | Q                     |

The upright map lives in `data/graft.json` (regenerate with `just graft-map`).
`data/graft-italic.json` is regenerated with `just italic-graft-map`: each
candidate is tested at all six italic weights, must improve its mean divergence
by more than five percentage points, and may not regress at any weight. Both maps
cover base letters only. `data/graft-derived.json` carries 68 upright and 76
italic precomposed derivatives from those same donors. Each is a canonical
Unicode descendant of a mapped base and passes the same six-weight threshold and
non-regression rule; unproven descendants stay Source Sans. The rest of the
repertoire (Greek, Cyrillic, currency, symbols, box-drawing) is filled by
coverage, reaching ~99% of Aptos's codepoints. Eleven currency signs and three
dingbats that no audited OFL donor carries fall back to the system font.

`data/graft-optical.json` carries 67 per-style core-glyph weight corrections.
For each graft, `just optical-graft-map` compares a shape-aware stroke proxy
(`2 * outline area / complete contour perimeter`) with the same glyph in the
stem-matched Source base, then samples the donor's variable weight axis towards
that target. A correction is committed only if the actual final TTF is no worse
against Aptos at 64, 128 and 256 px, for both the base glyph and its committed
canonical derivatives. Normal builds consume this map without needing Aptos.

## Metric compatibility

Every encoded advance equals Aptos's, as does every adjustment in the committed
kerning repertoire. The tested documents keep the same line breaks, widows,
orphans and page count. The shapes read as a close relative, not a copy.

To see it, `just overlay` superimposes a built style on Aptos (agreement in
black, divergence in colour), `just mark-audit` renders canonical decomposed
mark-to-base sequences with composition disabled, and `just conform` checks
shaped runs plus both greedy and Knuth-Plass-style line breaks across all twelve
styles. These need a local copy of Aptos (see [Contributing](#contributing));
none is needed to build or use Akasia.

## Install

Download the latest [release](https://codeberg.org/bloudraad/akasia/releases) and
install the TTFs:

- **macOS**: open each `.ttf` and click *Install Font*, or drop them in `~/Library/Fonts`.
- **Linux**: copy them to `~/.local/share/fonts`, then run `fc-cache -f`.
- **Windows**: select all, right-click, *Install*.

Set your document's font to Akasia. Where Akasia lacks a codepoint, the renderer
falls back for that character alone.

## Build from source

The build is reproducible from the repository alone: committed metric data
(`data/`), the OFL donors pinned in `fetch_sources.py`, and the locked toolchain
(`requirements.lock`). No Aptos binary is needed, and the fonts are build
artifacts rather than committed files.

```sh
just setup          # install the locked Python toolchain
just fetch-sources  # download Source Sans 3, the graft donors and Noto Sans Symbols
just build          # build the twelve styles into build/
just check          # fontbakery profile with documented exclusions: 0 FAIL, 0 ERROR
just package        # zip the family into dist/ (what a release publishes)
```

`just specimens` regenerates the SVG specimens in this README from `build/`, and
`docs/methodology.md` walks the pipeline (instance, graft, cover, re-space,
subset, port kerning) end to end.

## Contributing

Issues and pull requests are welcome on
[Codeberg](https://codeberg.org/bloudraad/akasia): sharper graft choices, wider
coverage, and build or fontbakery fixes especially.

```sh
just setup && just fetch-sources   # once
just build && just check           # after each change
```

A change is done when:

- `just check` reports **0 FAIL and 0 ERROR** across all twelve styles. Its
  exclusions and the accepted WARNs are documented; any new
  result needs a justification in the commit message.
- the build reports **`max-Δ` 0.00%** for every style; advances must equal
  Aptos's exactly.
- prose and comments are British English, and commits follow
  [Conventional Commits](https://www.conventionalcommits.org).

Review shapes with `just overlay` (one style), `just overlays` (the family) or
`just overlay-grid` (every glyph), agreement in black and divergence in colour,
and line breaks with `just pagination`.

### Sourcing Aptos

The overlay and pagination tools, and regenerating the committed data, need a
copy of Aptos. It is proprietary: obtain a licensed copy yourself, out of band,
and place the TTFs in `sources/aptos/`. With it in place:

```sh
just extract-metrics  # re-derive data/aptos-*.json (metric numbers only)
just graft-map        # re-score donors and re-pick data/graft.json
just italic-graft-map # re-pick data/graft-italic.json across all six weights
just derived-graft-map # prove same-donor precomposed derivatives family-wide
just optical-graft-map # balance proven grafts on each donor's weight axis
```

Neither the Aptos binary nor any Aptos outline ever enters the repository.

## Licence and attribution

Akasia is licensed under the SIL Open Font License, Version 1.1 (`OFL.txt`), with
Reserved Font Name "Akasia". Every donor is OFL-1.1; `OFL.txt` carries the
combined copyright with the upstream notice for every donor, and `FONTLOG.txt`
records the changelog and acknowledgements.

| Family            | Author(s)                                                                                                  | Licence                | Upstream                                      |
| ----------------- | ---------------------------------------------------------------------------------------------------------- | ---------------------- | -------------------------------------------- |
| Source Sans 3     | Paul D. Hunt (Adobe)                                                                                       | OFL-1.1 (RFN "Source") | https://github.com/adobe-fonts/source-sans   |
| Hanken Grotesk    | Alfredo Marco Pradil (Hanken Design Co.)                                                                   | OFL-1.1                | https://github.com/marcologous/hanken-grotesk |
| Albert Sans       | Andreas Rasmussen (a.Foundry)                                                                              | OFL-1.1                | https://github.com/usted/Albert-Sans         |
| Figtree           | Erik Kennedy                                                                                               | OFL-1.1                | https://github.com/erikdkennedy/figtree      |
| Public Sans       | The Public Sans Project Authors (USWDS/GSA), a fork of Libre Franklin by Pablo Impallari & Rodrigo Fuenzalida | OFL-1.1            | https://github.com/uswds/public-sans         |
| Arimo             | Steve Matteson (Monotype)                                                                                  | OFL-1.1                | https://github.com/googlefonts/arimo         |
| Open Sans         | The Open Sans Project Authors; Monotype Design Team                                                        | OFL-1.1                | https://github.com/googlefonts/opensans      |
| Noto Sans Symbols | The Noto Project Authors                                                                                   | OFL-1.1                | https://github.com/notofonts/symbols         |

Among the donors only "Source" (Adobe) is a declared Reserved Font Name; it is
respected and never reused. Akasia declares its own, "Akasia". Redistribute
modifications under the OFL, not under the "Akasia" or "Source" names.
