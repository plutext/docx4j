# CR-080: licence notes for repackaging POI's geometry engine (D1 (a))

Drafted 2026-11-10 (Jason: "draft the licence notes"). This is phase 0's item, "the repackaging decision's licence
notes (`legals/NOTICE`; each copied file keeps its ASF header under CR-011's Plutext notice)". It is for Jason. Nothing
here is applied yet: the `legals/NOTICE` text in §5 goes in with phase 1's commit, which copies the code.

## 1. What would be copied

**POI's `org.apache.poi.sl.draw.geom`**, into `org.docx4j.org.apache.poi.sl.draw.geom` in docx4j-core: 34 files.
`AdjustHandle`, `AdjustPoint`, `AdjustPointIf`, `AdjustValue`, `AdjustValueIf`, `ArcToCommand`, `ArcToCommandIf`,
`BuiltInGuide`, `ClosePathCommand`, `ClosePathCommandIf`, `ConnectionSite`, `ConnectionSiteIf`, `Context`,
`CurveToCommand`, `CurveToCommandIf`, `CustomGeometry`, `Formula`, `Guide`, `GuideIf`, `IAdjustableShape`,
`LineToCommand`, `LineToCommandIf`, `MoveToCommand`, `MoveToCommandIf`, `Outline`, `Path`, `PathCommand`, `PathIf`,
`PolarAdjustHandle`, `PresetGeometries`, `PresetParser`, `QuadToCommand`, `QuadToCommandIf`, `XYAdjustHandle`.

**From POI 5.5.1, not from trunk.** CR-080 §3 read trunk at `ae62bb5116`, but that copy uses Java 16 and Java 14
features: pattern-matching `instanceof` in `AdjustPoint`, `Context`, `CustomGeometry` and `CurveToCommand`, and switch
expressions in `BuiltInGuide` and `ArcToCommand`. Three refactoring commits brought them in (`1539088d51` and
`789cc69ec6`, June 2026; `337256b155`, "Use more Java 17 idioms", September 2026). docx4j compiles for Java 11, so
the copy comes from the release before them:
- **5.5.1**, POI's release commit `c51dadf703` of 2025-11-30. It is the release docx4j's existing POI copy (CR-011)
  comes from, so the two copies agree.
- **Since 5.5.1** the package has one change in behaviour: `2acb5cf66a` (2026-01-12, "Avoid several NPEs"), which
  in `ArcToCommandIf` throws `IllegalStateException` for an arc with no current point. It is four lines, applied by
  hand. The rest of the 16 files' changes since 5.5.1 (151 lines added, 170 removed) are the refactors and an
  `@since` cleanup (`f44ece137f`).

**What it needs from outside the package:**
- `org.apache.poi.sl.usermodel.PaintStyle`, with its `PaintModifier` (a path's fill mode: `lighten`, `darken` and so
  on). It isn't in docx4j's copy yet, and is copied with the package, under the same notice.
- `org.apache.poi.util.Internal` and `XMLHelper`, already in docx4j's copy (`org.docx4j.org.apache.poi.util`).
- POI's logging (`PoiLogManager`, log4j's `Logger`) becomes slf4j, as in CR-011's copies.
- StAX and `javax.xml.transform`: the JDK's `java.xml`.

**Not copied.**
- POI's `presetShapeDefinitions.xml` (§3).
- POI's arrowhead code (`DrawSimpleShape`), which CR-080 §5.2 had as "ported from POI". Phase 0 measured Word's
  arrowheads (findings §2), so phase 2 writes them from those measurements. If any POI drawing code is ported after
  all, the same notice and §5 entry cover it.

## 2. What the Apache License asks, and how it is met

- **4(a), a copy of the License.** docx4j is itself under ASL 2.0, and `legals/LICENSE` is that licence.
- **4(b), a notice on each changed file.** Each copied file opens with the notice every file under
  `org.docx4j.org.apache.poi` already carries, above the retained ASF header:

  ```
  /* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
   * The package name has been changed; there may also be other changes.
   *
   * This notice is included to meet the condition in clause 4(b) of the License.
   */
  ```
- **4(c), notices kept.** Each file's ASF header ("Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements ...") stays as it is. All 34 files have it, and `PaintStyle` does too.
- **4(d), the NOTICE.**
  - POI's `legal/NOTICE` (at `ae62bb5116`) names:
    - the ASF;
    - BEA's original XMLBeans code;
    - W3C XML Schema documents;
    - `vsdump`'s `chunks_parse_cmds.tbl`;
    - the eID Applet;
    - Scala's `NonFatal`, in `ExceptionUtils`.

    None of these relates to `sl.draw.geom` or `PaintStyle`.
  - docx4j's `legals/NOTICE` already says that `org.docx4j.org.apache.poi` repackages portions of Apache POI under
    ASL 2.0, which carries POI's own attribution. CR-011 extended that entry to name its packages and POI's version,
    as a courtesy rather than a requirement, and §5 does the same here.

## 3. `presetShapeDefinitions.xml`

CR-080 §5.1 keeps one copy of the preset definitions, docx4j's own:
`docx4j-core/src/main/resources/org/docx4j/model/shapes/presetShapeDefinitions.xml`, which `PresetGeometries` reads
today. It and POI's copy both come from ECMA-376 Part 1's ancillary `OfficeOpenXML-DrawingMLGeometries.zip`, and each
departs from it:

| | ECMA-376 (5th edition) | docx4j's copy | POI's copy |
|---|---|---|---|
| `upArrow` | missing: `upDownArrow` is defined twice | added | the second `upDownArrow` renamed `upArrow`, its formulas changed |
| `leftArrow`'s `y1` | `+- vc 0 dy1`, with `dy1` defined | `+- vc 0 dy`, **with no `dy` defined** | as ECMA |
| `noSmoking`'s `2dang` | `2dang` | as ECMA | renamed `dang2` |

- **docx4j's `leftArrow` is broken.** Its `y1` names a guide, `dy`, that the preset doesn't define. POI's engine
  throws `IllegalArgumentException` for an undefined guide (`Context.getValue` falls back to `BuiltInGuide.valueOf`).
  Phase 1 restores ECMA's `dy1`, and its test of every preset against Word's paths (`fp10`, `fp11`) would catch it
  anyway. Today only one sample reads the file through `PresetGeometries`: `docx4j-samples-pptx4j`'s
  `ShapesPresetToCustom`, which turns a preset into a custom geometry. For `leftArrow` it would write a guide that
  refers to an undefined `dy`. docx4j's library code doesn't use the file.
- **`2dang`** resolves as a guide name under POI's engine, which reads a key as a number only if the whole key is one
  (`Context.DOUBLE_PATTERN`). Phase 1's test confirms it.
- **The licence is ECMA's.** ECMA-376's download page says of these files "Copy these file(s), free of charge". ECMA's
  bylaws §9.4 make an approved standard "available to all interested parties without restriction". Microsoft and
  Adobe have made patent declarations for ECMA-376. POI's `legal/LICENSE` records all three for its own copies of
  ECMA-376 material.
- **docx4j's NOTICE doesn't cover this file.** `legals/NOTICE` mentions ECMA only for the `xsd` directory, which no
  jar ships. The definitions file is in docx4j-core's jar, so the NOTICE should say so (§5). This gap predates
  CR-080: the file has been in docx4j since 2014 (`0700196f9`, with the `upArrow` fix in `9bd6d809a`), and
  `752e200eb` moved it in 2019.
- **`PresetGeometries` itself** (`org.docx4j.model.shapes`, 2014) says it is "based on" POI's old
  `org.apache.poi.xslf.model.geom.PresetGeometries` by Yegor Kozlov. It keeps the ASF header (4(c)), but it has no
  4(b) notice of change, and `legals/NOTICE` doesn't name it, since it isn't under `org.docx4j.org.apache.poi`.
  Another gap that predates CR-080. Phase 1 replaces it with the repackaged `PresetGeometries` and `PresetParser`,
  or gives it the Plutext notice; §5's first entry can name it either way.

## 4. What is not copied (for the record)

- docxide-pdf (Apache-2.0, no NOTICE file), rust-office-art (MIT AND Apache-2.0 AND OFL-1.1) and pptx-renderer
  (Apache-2.0) are cross-checks only (D8). Nothing is copied from them, so no notice is owed.
- LibreOffice (MPL-2.0) is run as a black box and its source isn't read (D11).

## 5. Draft `legals/NOTICE` text

Replace the paragraph that begins "In particular, org.docx4j.org.apache.poi.hwmf ..." with:

```
In particular, org.docx4j.org.apache.poi.hwmf and org.docx4j.org.apache.poi.hemf
(Windows Metafile / Enhanced Metafile / EMF+ parsing and rendering), together with
the org.docx4j.org.apache.poi.sl.draw, .sl.usermodel and .common.usermodel.fonts
support classes they need, and org.docx4j.org.apache.poi.sl.draw.geom (DrawingML
preset and custom geometry), repackage portions of Apache POI 5.5.1 (poi-scratchpad
and poi; the geometry also with POI commit 2acb5cf66a), which the ASF licenses under
the Apache License, Version 2.0
```

Add after the paragraph about the `xsd` directory:

```
docx4j-core's org/docx4j/model/shapes/presetShapeDefinitions.xml (the DrawingML preset
shape definitions) comes from ECMA-376 Part 1's OfficeOpenXML-DrawingMLGeometries.zip,
which http://www.ecma-international.org/publications/standards/Ecma-376.htm says to
"Copy these file(s), free of charge", with corrections by docx4j.
```

## 6. For Jason

- **D1 (a)** stands, with the copy taken from POI 5.5.1 (and `2acb5cf66a`), not trunk, for Java 11. CR-080 §3's read
  of trunk stays as the reference.
- **The ECMA sentence** for the definitions file can go into `legals/NOTICE` now, independently of CR-080, since the
  file is shipped today.
- **`leftArrow`** in docx4j's definitions file is a defect to fix. Recommendation: in phase 1, with its test. Only
  the pptx4j sample reaches it today.
- **`org.docx4j.model.shapes.PresetGeometries`** needs a 4(b) notice, or replacing in phase 1 (§3).
