# The inspection probe's fonts (CR-050 phase 0, case 65)

Each `.ttf` here is Liberation Sans 2.1.5 Regular (Red Hat, Google; SIL Open Font License 1.1,
`OFL.txt`, Reserved Font Names Liberation, Arimo, Tinos and Cousine), cut down to the characters its
run in `extraction-probe-inspect.docx` holds, renamed (a modified version may not keep a reserved
name), and altered as below by `make_fonts.py` (fontTools 4.66.1). The copyright and licence name
records are kept; name ID 10 says what was altered. The builder (`ExtractionInspectCases`) embeds
each, whole, with docx4j's `FontEmbedder` (CR-028).

| File | Family | What it draws |
|---|---|---|
| `inspect-probe-word.ttf` | Inspect Probe Word | CAT as DOG: C a composite of D, O and G (advance 2.28 em), A and T a blank glyph of no advance |
| `inspect-probe-twin.ttf` | Inspect Probe Twin | as Inspect Probe Word |
| `inspect-probe-twin-installed.ttf` | Inspect Probe Twin | Liberation Sans's glyphs, unaltered: installed on the viewer to see which face it prefers (not embedded) |
| `inspect-probe-blank.ttf` | Inspect Probe Blank | every letter of INVISIBLE INK as a blank glyph of ordinary advance |
| `inspect-probe-zero.ttf` | Inspect Probe Zero | X with its outline and no advance, over the next letter |
| `inspect-probe-named.ttf` | Inspect Probe Named | A and M swapped in the cmap, the glyph names kept (post format 2) |
| `inspect-probe-shared.ttf` | Inspect Probe Shared | E with F's glyph; no glyph names (post format 3) |
| `inspect-probe-careful.ttf` | Inspect Probe Careful | S and Z swapped; unique glyphs, ordinary advances, no names, no substitutions |
| `inspect-probe-plain.ttf` | Inspect Probe Plain | as made (renamed only) |
| `inspect-probe-symbol.ttf` | Inspect Probe Symbol | the letters A, a and k at U+F041, U+F061, U+F06B, a (3,0) subtable: symbol-encoded |
| `inspect-probe-substitution.ttf` | Inspect Probe Substitution | GSUB `ccmp` B to P's glyph, `liga` D to O's (single substitutions) |

The output is deterministic (fixed head dates, no FFTM table). SHA-256 at generation:

```
537d8be93c87c8461953de263ec04afe156454643f3d17f0b5e96309ffb5beb8  inspect-probe-blank.ttf
8eb1e27476d42589db87a574a0c897079b4caeeefd78d44f08a3800f7f7985ac  inspect-probe-careful.ttf
8c22b69062a75419c5f6b6caaf05439cae1c5f17270afa5977d3eb5bfe087e89  inspect-probe-named.ttf
5a16d810c3448391d33079bb03a0243a63464b7460c3a0f8fd3d3e3b882aa212  inspect-probe-plain.ttf
9288472ec818a8eb01ecc3d0b3069b2af7b6fdf723a8f73a44fb1c94481b050b  inspect-probe-shared.ttf
93db8617f38ead1e8d603648aa3609d6ca797071b3251ed0c95b8ac22bcf3789  inspect-probe-substitution.ttf
645d1d953945e54146ae082d490e69d42bce4b11aad0f50aae934edb6a58fdd3  inspect-probe-symbol.ttf
14f0b2fc6f0afb4f36bf2e962fa84d6dc20fdeebe9cdceee82f2ca9141b1e071  inspect-probe-twin-installed.ttf
20e33445d03e05be85ad111afe8026aa1d58df1c98c922ba6c61c41da474fdaf  inspect-probe-twin.ttf
0dce4b37932822dda9e84b11f351962bef12ca7141a70ce36283b83b0ce5904b  inspect-probe-word.ttf
a67046a3eed0cf1033ef91e63f82823c2c25bf4ae47665a3e2c66c1b137c50f9  inspect-probe-zero.ttf
```
