# CR: Math in PDF output (OMML → MathML → SVG → XSL-FO → FOP)

Status: **PROPOSED** 2026-09-02. No code yet. Follow-on to CR-math-omml-mathml
(which gave us native OMML⇄MathML with no Microsoft XSLT).

## 1. Background

docx→PDF goes docx → XSL-FO → Apache FOP → PDF. FOP renders ordinary FO plus
**SVG** inside `fo:instream-foreign-object`, but has **no built-in MathML
renderer** (SVG only; other namespaces need an extension). So equations don't
appear in docx4j's PDF today — the FO exporters have no `m:oMath` handling, so an
equation currently hits the "Need to handle org.docx4j.math.CTOMathPara" fallback
and is dropped/emitted as nothing.

The clean way to get math into FOP is to render the MathML to **SVG** before FOP
sees it, because SVG is already a first-class foreign object:

```
OMML (CTOMath)
  │  OmmlToMathML          (ours, native — CR-math-omml-mathml; no MS XSLT)
  ▼
MathML
  │  MathMLRenderer        (MathML -> SVG + width/height/baseline)
  ▼
SVG
  │  wrap in fo:instream-foreign-object, set alignment-adjust for the baseline
  ▼
Apache FOP (existing Batik/SVG path) -> PDF
```

We no longer need Microsoft's `OMML2MML.XSL` for the OMML→MathML step — that is
`OmmlToMathML` now. The only new capability is **MathML → SVG**.

**What docx4j already has that makes this cheap:**

- `docx4j-export-fo` already depends on Batik (`batik-dom`/`-bridge`/
  `-transcoder`) and FOP 2.11, so `fo:instream-foreign-object` holding SVG is a
  supported, exercised path (it's how images/DrawingML SVG reach PDF).
- Two FO pathways, as for HTML: the visitor `FOExporterVisitorGenerator` and the
  XSLT `docx2fo.xslt` + `XsltFOFunctions`. Per the FO-parity pattern the math→FO
  logic lands **once** in `XsltFOFunctions`, called by both — mirroring the HTML
  `XsltHTMLFunctions.convertMathML` we just added.
- `OmmlToMathML` (docx4j-core) already turns `CTOMath`/`CTOMathPara` into a MathML
  W3C DOM.

So the whole job reduces to: a pluggable **MathML→SVG renderer**, a small FO
emitter, and wiring into both FO pathways.

## 2. Goal / non-goals

Goal: equations appear in docx→PDF out of the box (subject to a renderer being on
the classpath), Apache-licensed, with a pluggable renderer so quality can be
improved without touching the exporter.

Non-goals: teaching FOP to lay out MathML directly (the `jeuclid-fop` extension —
rejected, see §4); a hard dependency on any heavyweight renderer; perfect visual
parity with Word on pathological equations in v1.

## 3. Design

### Renderer SPI

```java
package org.docx4j.convert.out.mathml;   // beside OmmlToMathML

public interface MathMLRenderer {
    /** @param mathML a MathML <math> DOM node; @param fontSizePt the run font size */
    MathGraphic render(org.w3c.dom.Node mathML, double fontSizePt) throws MathConversionException;
}

// Java 11 baseline -> a small immutable class, not a record
public final class MathGraphic {
    final org.w3c.dom.Document svg;   // an <svg> in the SVG namespace, viewport in pt
    final double widthPt;
    final double heightPt;
    final double baselineDepthPt;     // distance from bottom edge up to the baseline
    // + getters
}
```

The exporter is renderer-agnostic: it asks for a `MathGraphic` and drops the SVG
into `fo:instream-foreign-object` with
`alignment-adjust="-(baselineDepthPt/heightPt)%"`.

Renderer discovery: reflective/optional (docx4j's pattern for optional
exporters). If no renderer is on the classpath, log once and fall back to the
equation's text (the FO exporter must never fail because math can't render).

### Renderer 1 — JEuclid (JVM-native, first)

`de.rototor.jeuclid:jeuclid-core:3.1.14` — a JDK-11/Batik-1.x fork of JEuclid,
**Apache-2.0**, on Maven Central. Use `jeuclid-core`'s converter (MathML DOM →
SVG DOM) — NOT `jeuclid-fop` (which pins FOP 2.3-era extension APIs; we feed FOP
plain SVG instead, so FOP's version is irrelevant). JEuclid already computes the
baseline; we normalise its SVG viewport to points and fill in `MathGraphic`.
Optional dependency, isolated (see Placement).

### Renderer 2 — MathJax (higher fidelity, later)

MathJax 4.x, Apache-2.0, `mathml2svg` is first-class and emits glyphs as **SVG
paths** (font-independent — good for PDF determinism). Two ways to run it in a
JVM stack, both deferred behind the same SPI:

- external Node process (simple, but a runtime dependency and process boundary);
- **GraalJS in-process** (`org.graalvm.js`) — keeps it JVM-native, no subprocess,
  at the cost of a heavy dependency and JS-engine warm-up. This is the option the
  common "JEuclid or Node" framing misses, and the more attractive one for a
  library.

MathJax's SVG carries `h` (height above baseline) and `d` (depth) via its
`viewBox`; `baselineDepthPt` falls straight out. Chosen only if the JEuclid
regression corpus shows JEuclid isn't faithful enough.

### SVG sizing (do this regardless of renderer)

Normalise the SVG's own viewport to **points** and keep the renderer's `viewBox`
coordinate system, so Batik never has to agree with the renderer about what an
`ex`/`em` is:

```
width  = viewBox.width  / 1000 * fontSizePt
height = viewBox.height / 1000 * fontSizePt
depth  = (viewBox.height + viewBox.minY) / 1000 * fontSizePt
alignment-adjust = -(depth / height) * 100 %
```

### Placement

- SPI (`MathMLRenderer`, `MathGraphic`) in `docx4j-core`
  (`org.docx4j.convert.out.mathml`), beside `OmmlToMathML` — no new deps.
- The JEuclid implementation + the FO emitter helper in `docx4j-export-fo`
  (which already owns FOP/Batik), with `jeuclid-core` as an **optional**
  dependency loaded reflectively. (Alternative: a `docx4j-export-fo-math`
  satellite module. Decide in phase 1 — reflective-optional keeps module count
  down and matches the documents4j/microsoft-graph precedent.)

### Wiring (both FO pathways, once)

- `XsltFOFunctions.mathToFO(context, node, fontSizePt)` — the shared helper:
  unmarshal `m:oMath`/`m:oMathPara` → `OmmlToMathML` → `MathMLRenderer` →
  `fo:instream-foreign-object`. Returns a `DocumentFragment`.
- XSLT pathway: `docx2fo.xslt` gets `m:oMath` / `m:oMathPara` templates that
  `xsl:copy-of` the helper's result (exactly like the HTML `convertMathML`).
- Visitor pathway: `FOExporterVisitorGenerator` gets the `m:oMath` /
  `m:oMathPara` case calling the same helper; mark them non-traversable.
- Font size: read from the run/paragraph rPr; default 12pt if absent.

## 4. Alternatives rejected

- **`jeuclid-fop` extension** — registers MathML as a foreign namespace in FOP.
  Couples us to FOP 2.3-era extension APIs and to a stale artifact; gains nothing
  over rendering to SVG ourselves. Rejected.
- **Fake it with FO primitives** (`baseline-shift`, inline tables) — fine for
  `x²`/`H₂O`, hopeless for fractions/radicals/stretchy fences/matrices/limits.
  Rejected.
- **Teach FOP OMML directly** — no.
- **Temml / LaTeXML** — TeX-oriented; wrong direction (we already have MathML).

## 5. Phases

1. **SPI + JEuclid renderer + sizing.** `MathMLRenderer`/`MathGraphic` in core;
   `JEuclidMathMLRenderer` in docx4j-export-fo (optional `jeuclid-core`, reflective
   load); point-normalised SVG + baseline. Unit test: MathML → non-empty SVG with
   sane width/height/baseline for the corpus.
2. **FO emitter + wiring.** `XsltFOFunctions.mathToFO`; wire the visitor and XSLT
   FO pathways; fall back to text when no renderer/render fails. Test: a docx with
   an equation produces a PDF containing an SVG (FOP runs clean); both pathways.
3. **Regression corpus.** Extend the CR-math-omml-mathml corpus toward ~50–100
   deliberately nasty equations (stretchy operators, nested radicals/fractions,
   matrices, n-ary limits, accents, math fonts). Render to PDF, eyeball vs Word's
   PDF; capture known-weak cases.
4. **Decide on MathJax.** If JEuclid's fidelity is inadequate on the corpus,
   add `MathJaxMathMLRenderer` (GraalJS first) behind the same SPI. Otherwise
   record JEuclid as sufficient and defer.
5. **Docs + CHANGELOG.** How to enable (add `jeuclid-core`), the fallback
   behaviour, and the renderer SPI for anyone wanting MathJax/other.

## 6. Risks / notes

- **JEuclid layout quality.** 2010-era engine, MathML 1/2. Encouragingly, our
  `OmmlToMathML` emits conventional Presentation MathML (not MathML-Core-only
  features), which is JEuclid's sweet spot. Still, matrices, stretchy fences and
  spacing are the likely weak spots — hence the corpus gate (phase 3) before
  committing.
- **Math fonts.** JEuclid may reference math fonts rather than emitting paths; if
  so those fonts must be available to the SVG rasteriser. MathJax emits paths and
  sidesteps this. Check in phase 1.
- **Baseline alignment in FOP.** Confirm FOP honours `alignment-adjust` on an
  `fo:instream-foreign-object`; if not, fall back to `baseline-shift` or a
  wrapping inline box. Verify in phase 2.
- **Optional dependency ergonomics.** With the reflective-optional approach,
  users who want math in PDF add `de.rototor.jeuclid:jeuclid-core`; without it,
  equations degrade to text with a one-time warning. Document clearly.
- **Licensing.** JEuclid (de.rototor) and MathJax are both Apache-2.0 — clean.
  The whole pipeline is now free of Microsoft's non-redistributable XSLT.
- **Scope.** HTML already emits MathML natively (browsers render it); this CR is
  only about the PDF/FO pathway, where FOP can't.
