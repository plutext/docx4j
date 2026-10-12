#!/usr/bin/env python3
# Copyright 2026, Plutext Pty Ltd.
#
# This file is part of docx4j.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
# except in compliance with the License.  You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
"""
One drawing of a chart against another, as numbers (CR-037 phase 0).  Distances are points,
from the chart box's top left, y down; each command prints a summary, --json the detail.

  traces A.json B.json      two chart_trace.py readings (B against A): texts matched by string
                            (missing, extra, the distance between centres, size, font, colour);
                            the shapes' parts (each bar, slice, marker, gridline) matched by kind
                            and colour to the nearest (the largest distance between their boxes'
                            edges; colour or kind differing, missing, extra)
  layout DUMP.json B.json   an Office layout dump (office-chart-render-probes.ps1) against a
                            trace: the title's, axis titles', legend entries' and data labels'
                            boxes against the text drawn in them (centres); the plot area's
                            inside against the gridlines and axis lines drawn; each point's box
                            against the part drawn in its series' colour; the value axis's
                            bounds and unit against the numbers drawn on it
  raster A.png B            a picture against a picture (B a PNG of the same size, or a PDF, its
                            chart box (--box x,y,w,h, points, y down) drawn at A's scale): the
                            share of pixels differing by more than 32 and 64 (of 255, the largest
                            channel), each pixel against its best of B's 3x3 neighbours
  fixtures DIR OUT          the oracle for the build (review A3; docx4j-chart-render's
                            src/test/resources/charts/render), from a run whose baseline has
                            been made: probes.tsv; probes\\ the xlsx (the chart and its theme;
                            each docx's chart is the same chart), R1's pptx and R10's styled
                            xlsx (Excel's); excel-layout\\ and powerpoint-layout\\ the dumps
                            (nulls dropped, numbers to 0.001); word-pdf\\ and powerpoint-pdf\\ the
                            traces of Office's PDFs
  baseline DIR              Office against Office over a run of the probes (DIR as
                            office-chart-render-probes.ps1's -Dir: probes\\, excel\\, word\\,
                            powerpoint\\): each Word and PowerPoint PDF traced (DIR/traces/), then
                            Excel's layout against Word's PDF, Word's own layout against it,
                            PowerPoint's against its PDF, and Excel's picture against Word's PDF;
                            a row a probe and comparison (DIR/baseline.tsv) and the spread of each
                            measure (DIR/baseline-summary.txt), from which the tolerances are set

usage: chart_score.py traces A.json B.json [--json]
       chart_score.py layout DUMP.json TRACE.json [--json]
       chart_score.py raster A.png B.(png|pdf) [--box x,y,w,h] [--page N] [--diff out.png]
       chart_score.py baseline DIR
       chart_score.py fixtures DIR OUT
"""
import argparse
import csv
import json
import math
import os
import re
import subprocess
import sys
import tempfile

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))


def r2(v):
    return None if v is None else round(v, 2)


def norm(s):
    return re.sub(r"\s+", " ", s or "").strip()


def edge(a, b):
    """The largest distance between two boxes' edges"""
    return max(abs(a[i] - b[i]) for i in range(4))


def stats(values):
    v = sorted(x for x in values if x is not None)
    if not v:
        return {"n": 0}
    p95 = v[min(len(v) - 1, int(math.ceil(0.95 * len(v))) - 1)]
    return {"n": len(v), "max": r2(v[-1]), "p95": r2(p95), "median": r2(v[len(v) // 2]), "mean": r2(sum(v) / len(v))}


def greedy(a, b, dist, limit):
    """Pairs of a and b, nearest first, each used once, no farther than limit"""
    cand = []
    for i, x in enumerate(a):
        for j, y in enumerate(b):
            d = dist(x, y)
            if d is not None and d <= limit:
                cand.append((d, i, j))
    cand.sort()
    used_a, used_b, pairs = set(), set(), []
    for d, i, j in cand:
        if i in used_a or j in used_b:
            continue
        used_a.add(i)
        used_b.add(j)
        pairs.append((i, j, d))
    return pairs, [i for i in range(len(a)) if i not in used_a], [j for j in range(len(b)) if j not in used_b]


# ------------------------------------------------------------------ traces against traces

def parts(trace):
    out = []
    for s in trace["shapes"]:
        if s.get("alpha", 1) == 0:
            continue
        for p in s["parts"]:
            out.append({"kind": s["kind"], "colour": s["colour"], "box": p["box"], "points": p["points"]})
    return out


def score_traces(a, b):
    res = {"texts": [], "missing": [], "extra": []}
    ta, tb = a["texts"], b["texts"]
    pairs, miss, extra = greedy(ta, tb, lambda x, y: math.hypot(x["cx"] - y["cx"], x["cy"] - y["cy"])
                                if norm(x["text"]) == norm(y["text"]) else None, 1e9)
    for i, j, d in pairs:
        x, y = ta[i], tb[j]
        res["texts"].append({"text": x["text"], "d": r2(d), "dx": r2(y["cx"] - x["cx"]), "dy": r2(y["cy"] - x["cy"]),
                             "size": [x["size"], y["size"]], "font": [x["font"], y["font"]] if x["font"] != y["font"] else None,
                             "colour": [x["colour"], y["colour"]] if x["colour"] != y["colour"] else None,
                             "angle": [x["angle"], y["angle"]] if abs(x["angle"] - y["angle"]) > 0.5 else None})
    res["missing"] = [ta[i]["text"] for i in miss]
    res["extra"] = [tb[j]["text"] for j in extra]
    pa, pb = parts(a), parts(b)
    same = lambda x, y: edge(x["box"], y["box"]) if x["kind"] == y["kind"] and x["colour"] == y["colour"] else None
    pairs, miss, extra = greedy(pa, pb, same, 30)
    res["parts"] = []
    for i, j, d in pairs:
        x, y = pa[i], pb[j]
        r = {"kind": x["kind"], "colour": x["colour"], "box": x["box"], "d": r2(d)}
        if len(x["points"]) == len(y["points"]) and len(x["points"]) > 2:
            r["vertices"] = r2(max(math.hypot(p[0] - q[0], p[1] - q[1]) for p, q in zip(x["points"], y["points"])))
        res["parts"].append(r)
    # what is left, against any colour: a part drawn in another colour
    left_a, left_b = [pa[i] for i in miss], [pb[j] for j in extra]
    pairs, miss, extra = greedy(left_a, left_b, lambda x, y: edge(x["box"], y["box"]) if x["kind"] == y["kind"] else None, 3)
    res["recoloured"] = [{"box": left_a[i]["box"], "colour": [left_a[i]["colour"], left_b[j]["colour"]], "d": r2(d)}
                         for i, j, d in pairs]
    res["missingParts"] = [{"kind": left_a[i]["kind"], "colour": left_a[i]["colour"], "box": left_a[i]["box"]} for i in miss]
    res["extraParts"] = [{"kind": left_b[j]["kind"], "colour": left_b[j]["colour"], "box": left_b[j]["box"]} for j in extra]
    res["images"] = [len(a.get("images", [])), len(b.get("images", []))]
    td = [t["d"] for t in res["texts"]]
    pd = [p["d"] for p in res["parts"]]
    res["summary"] = {"texts": len(ta), "matched": len(res["texts"]), "missing": len(res["missing"]),
                      "extra": len(res["extra"]), "textDistance": stats(td),
                      "sizeDiffers": sum(1 for t in res["texts"] if abs(t["size"][0] - t["size"][1]) > 0.01),
                      "fontDiffers": sum(1 for t in res["texts"] if t["font"]),
                      "colourDiffers": sum(1 for t in res["texts"] if t["colour"]),
                      "parts": len(pa), "partsMatched": len(res["parts"]), "partDistance": stats(pd),
                      "recoloured": len(res["recoloured"]), "missingParts": len(res["missingParts"]),
                      "extraParts": len(res["extraParts"])}
    return res


# ------------------------------------------------------------------ a layout dump against a trace

# Office's object model reports a chart's elements (the plot area, titles, the legend and its
# entries, axes, data labels) from an origin 4pt inside the chart's box, and points from that
# origin in Excel but from the box itself in Word and PowerPoint (measured on all 102 probes and
# 12 slides, 2026-10-12: Word and PowerPoint report ChartArea.Left and Top as 4; Excel its
# ChartArea 5pt narrower and shorter than the chart)
ORIGIN = 4.0


def dbox(o, off=ORIGIN):
    """A dumped box as x0, y0, x1, y1 from the chart box's top left"""
    if not o or o.get("left") is None or o.get("width") is None:
        return None
    return [o["left"] + off, o["top"] + off, o["left"] + o["width"] + off, o["top"] + o["height"] + off]


def centre(b):
    return ((b[0] + b[2]) / 2, (b[1] + b[3]) / 2)


def ink(t):
    """A text's ink, roughly: its baseline from x to x1, a size high (0.75 above, 0.25 below)"""
    xs = [t["x"], t["x1"]]
    ys = [t["y"], t["y1"]]
    s = t["size"]
    return [min(xs), min(ys) - 0.75 * s, max(xs), max(ys) + 0.25 * s]


def number(s):
    """A label's number: "$1,234.50" 1234.5, "40%" 0.4; None for anything else"""
    t = s.replace(",", "").replace("$", "").replace(" ", "").strip()
    pct = t.endswith("%")
    t = t.rstrip("%")
    neg = t.startswith("(") and t.endswith(")")
    t = t.strip("()")
    try:
        v = float(t.replace("−", "-"))
    except ValueError:
        return None
    if neg:
        v = -v
    return v / 100 if pct else v


def text_in(trace, box, text=None, slack=4):
    """The trace's texts whose centre is in the box (and of that string, if given)"""
    out = []
    for t in trace["texts"]:
        if box[0] - slack <= t["cx"] <= box[2] + slack and box[1] - slack <= t["cy"] <= box[3] + slack:
            if text is None or norm(t["text"]) == norm(text):
                out.append(t)
    return out


def labelled(dumped, trace, kind, res):
    """A dumped box with text (a title, a data label) against the text drawn there"""
    b = dbox(dumped)
    if not b:
        return
    want = norm(dumped.get("text"))
    lines = text_in(trace, b)
    if want:
        exact = [t for t in trace["texts"] if norm(t["text"]) == want]
        if exact:
            c = centre(b)
            lines = [min(exact, key=lambda t: math.hypot(t["cx"] - c[0], t["cy"] - c[1]))]
    if not lines:
        res["missing"].append({"kind": kind, "text": dumped.get("text"), "box": [r2(v) for v in b]})
        return
    ib = [min(ink(t)[0] for t in lines), min(ink(t)[1] for t in lines), max(ink(t)[2] for t in lines),
          max(ink(t)[3] for t in lines)]
    cb, ci = centre(b), centre(ib)
    font = dumped.get("font") or {}
    res["labels"].append({"kind": kind, "text": dumped.get("text"), "dx": r2(ci[0] - cb[0]), "dy": r2(ci[1] - cb[1]),
                          "d": r2(math.hypot(ci[0] - cb[0], ci[1] - cb[1])), "box": [r2(v) for v in b],
                          "ink": [r2(v) for v in ib], "size": [font.get("size") or font.get("size2"), lines[0]["size"]]})


def lines_drawn(trace, chart_w, chart_h):
    """The trace's gridlines and axis lines: level or upright lines at least half as long as the
    longest of their direction (not a legend's line keys, nor the chart area's border)"""
    out = []
    for p in parts(trace):
        if p["kind"] != "stroke" and not (p["kind"] == "fill" and (p["box"][2] - p["box"][0] < 1.6 or p["box"][3] - p["box"][1] < 1.6)):
            continue
        b = p["box"]
        w, h = b[2] - b[0], b[3] - b[1]
        if w > chart_w - 2 and h > chart_h - 2:
            continue
        if (h < 0.01 and w > 10) or (w < 0.01 and h > 10):
            out.append(b)
    longest_w = max([b[2] - b[0] for b in out if b[3] - b[1] < 0.01] or [0])
    longest_h = max([b[3] - b[1] for b in out if b[2] - b[0] < 0.01] or [0])
    return [b for b in out if (b[3] - b[1] < 0.01 and b[2] - b[0] >= longest_w / 2)
            or (b[2] - b[0] < 0.01 and b[3] - b[1] >= longest_h / 2)]


def to_segment(p, a, b):
    """A point's distance from a segment"""
    dx, dy = b[0] - a[0], b[1] - a[1]
    L = dx * dx + dy * dy
    t = 0 if L == 0 else max(0, min(1, ((p[0] - a[0]) * dx + (p[1] - a[1]) * dy) / L))
    return math.hypot(p[0] - a[0] - t * dx, p[1] - a[1] - t * dy)


def to_outline(p, points):
    return min(to_segment(p, points[i], points[i + 1]) for i in range(len(points) - 1)) if len(points) > 1 else 1e9


PIES = (5, -4120, 69, 80, -4102, 70)
DATE = re.compile(r'[dmy]', re.I)


def score_layout(dump, trace):
    res = {"labels": [], "missing": [], "points": [], "axes": []}
    cw, ch = dump["size"]
    ca = dump.get("chartArea") or {}
    res["chartArea"] = [ca.get("left"), ca.get("top"), ca.get("width"), ca.get("height")]
    if dump.get("hasTitle"):
        labelled(dump.get("title"), trace, "title", res)
    for ax in dump.get("axes", []):
        if ax.get("hasTitle"):
            labelled(ax.get("title"), trace, "axis title %d" % ax["type"], res)
    for s in dump.get("series", []):
        for p in s.get("points", []):
            if p.get("dataLabel"):
                labelled(p["dataLabel"], trace, "data label", res)
    # the legend's entries: each entry's box against the series' name drawn in it
    if dump.get("hasLegend") and dump.get("legend"):
        names = [norm(s.get("name")) for s in dump.get("series", [])]
        entries = dump["legend"].get("entries") or []
        for e in entries:
            b = dbox(e)
            if not b:
                continue
            ts = text_in(trace, b, slack=1)
            if not ts:
                res["missing"].append({"kind": "legend entry", "box": [r2(v) for v in b]})
                continue
            t = ts[0]
            cb, ci = centre(b), centre(ink(t))
            res["labels"].append({"kind": "legend entry", "text": t["text"], "dx": r2(ci[0] - cb[0]), "dy": r2(ci[1] - cb[1]),
                                  "d": r2(abs(ci[1] - cb[1])), "box": [r2(v) for v in b], "ink": [r2(v) for v in ink(t)],
                                  "named": norm(t["text"]) in names})
    # the plot area's inside against the lines drawn
    pa = dump.get("plotArea") or {}
    inside = None
    if pa.get("insideLeft") is not None:
        inside = [pa["insideLeft"] + ORIGIN, pa["insideTop"] + ORIGIN, pa["insideLeft"] + pa["insideWidth"] + ORIGIN,
                  pa["insideTop"] + pa["insideHeight"] + ORIGIN]
        ls = lines_drawn(trace, cw, ch)
        if ls:
            grid = [min(b[0] for b in ls), min(b[1] for b in ls), max(b[2] for b in ls), max(b[3] for b in ls)]
            res["inside"] = {"dumped": [r2(v) for v in inside], "drawn": [r2(v) for v in grid],
                             "d": [r2(grid[i] - inside[i]) for i in range(4)]}
    # each point against what is drawn in its colour: a bar's box against the part's; a pie's slice
    # locations (Point.PieSliceLocation: the radii's outer ends, the arc's middle, the centre; a
    # doughnut's inner points) against the slice's outline; an area's or a line's point (a box
    # of no width) against the nearest vertex.  A box off the chart (Excel's for a blank point)
    # has no geometry; a point with nothing drawn near it (a zero bar) is undrawn.
    ps = parts(trace)
    shades = [{"kind": "image", "colour": "", "box": q["box"], "points": q["points"]}
              for i in trace.get("images", []) for q in (i.get("parts") or [{"box": i["box"], "points": []}])]
    point_origin = ORIGIN if dump.get("app") == "excel" else 0.0
    pie = dump.get("chartType") in PIES
    res["noGeometry"] = res["undrawn"] = res["raster"] = 0
    # a part as big as the chart (its area, or the chart's clip) is not a point's
    small = lambda q: q["box"][2] - q["box"][0] < cw - 2 or q["box"][3] - q["box"][1] < ch - 2
    for s in dump.get("series", []):
        colour = s.get("fill")
        for k, p in enumerate(s.get("points", [])):
            b = dbox(p, point_origin)
            if not b:
                continue
            if b[0] < -1 or b[1] < -1 or b[2] > cw + 1 or b[3] > ch + 1:
                res["noGeometry"] += 1
                continue
            want = p.get("fill") or colour
            mine = [q for q in ps if want and q["colour"].split("/")[0] == want]
            if pie and p.get("slice"):
                # (a doughnut's centre, 5, is in its hole)
                locs = [(v[0] + point_origin, v[1] + point_origin) for i, v in p["slice"].items()
                        if v and len(v) == 2 and v[0] is not None and v[1] is not None
                        and not (i == "5" and dump.get("chartType") in (-4120, 80))]
                # a gallery style's slice is a shading in its outline's clip: the outline, then; a
                # slice drawn as a picture through a soft mask (style 258) has none
                cand = mine or [q for q in ps + shades if q["points"] and small(q)]
                if not locs:
                    continue
                if not cand:
                    res["raster"] += 1
                    continue
                best = min(cand, key=lambda q: max(to_outline(l, q["points"]) for l in locs))
                res["points"].append({"series": s.get("index"), "point": k + 1, "d": r2(max(to_outline(l, best["points"]) for l in locs)),
                                      "slice": True, "colour": want})
                continue
            if b[2] - b[0] < 0.01 or b[3] - b[1] < 0.01:
                corners = [(b[0], b[1]), (b[2], b[3])]
                # (a line's points have no fill of their own: Office reports black)
                near = mine or [q for q in ps if small(q)]
                verts = [v for q in near for v in q["points"]]
                # and a marker's centre (a scatter's point is its marker's centre)
                verts += [centre(q["box"]) for q in near if q["box"][2] - q["box"][0] < 15 and q["box"][3] - q["box"][1] < 15]
                if not verts:
                    res["undrawn"] += 1
                    continue
                d = min(min(math.hypot(c[0] - v[0], c[1] - v[1]) for v in verts) for c in corners)
                if d > 10:
                    res["undrawn"] += 1
                    continue
                res["points"].append({"series": s.get("index"), "point": k + 1, "d": r2(d), "vertex": True, "colour": want})
                continue
            cand = mine or ps + shades
            best = min(cand, key=lambda q: edge(q["box"], b)) if cand else None
            if best is None:
                continue
            res["points"].append({"series": s.get("index"), "point": k + 1, "d": r2(edge(best["box"], b)),
                                  "dumped": [r2(v) for v in b], "drawn": best["box"], "colour": want or best["kind"]})
    # the value axes' bounds and unit against the numbers drawn beside the plot area
    numbers = [(number(t["text"]), t) for t in trace["texts"]]
    numbers = [(v, t) for v, t in numbers if v is not None]
    for ax in dump.get("axes", []):
        # a value axis (a category axis has a categoryType, and over dates bounds and a unit too)
        # (and not a deleted one: no tick labels, though Office still reports its bounds)
        if (ax.get("minimumScale") is None or ax.get("majorUnit") in (None, 0) or ax.get("categoryType") is not None
                or ax.get("tickLabels") is None):
            continue
        lo, hi, unit = ax["minimumScale"], ax["maximumScale"], ax["majorUnit"]
        log = ax.get("scaleType") == -4133
        want = []
        v = lo
        while v <= hi * (1 + 1e-9) + 1e-12 and len(want) < 200:
            want.append(v)
            v = v * (ax.get("logBase") or 10) if log else lo + unit * len(want)
        # the labels along the axis: in the axis's box when it has one
        b = dbox(ax)
        near = lambda t: not b or (b[0] - 2 <= t["cx"] <= b[2] + 2 and b[1] - 2 <= t["cy"] <= b[3] + 2)
        fmt = (ax.get("tickLabels") or {}).get("numberFormat") or "General"
        if DATE.search(re.sub(r'"[^"]*"', "", fmt)) and fmt != "General":
            # dates: as many labels as the bounds and unit make
            drawn = [t["text"] for t in trace["texts"] if near(t)]
            res["axes"].append({"type": ax["type"], "group": ax["group"], "min": lo, "max": hi, "unit": unit, "log": log,
                                "expected": len(want), "drawn": drawn[:40], "agree": len(drawn) == len(want), "dates": True})
            continue
        drawn = sorted({round(n, 9) for n, t in numbers if near(t)})
        got = all(any(abs(w - d) <= 1e-6 * max(1, abs(w)) for d in drawn) for w in want)
        res["axes"].append({"type": ax["type"], "group": ax["group"], "min": lo, "max": hi, "unit": unit, "log": log,
                            "expected": len(want), "drawn": drawn[:40], "agree": got and len(drawn) == len(want)})
    ld = [x["d"] for x in res["labels"]]
    pd = [x["d"] for x in res["points"]]
    res["summary"] = {"labels": len(res["labels"]), "labelDistance": stats(ld), "missing": len(res["missing"]),
                      "points": len(res["points"]), "pointDistance": stats(pd), "noGeometry": res["noGeometry"],
                      "undrawn": res["undrawn"], "raster": res["raster"],
                      "inside": res.get("inside", {}).get("d"),
                      "axesAgree": all(a["agree"] for a in res["axes"]) if res["axes"] else None}
    return res


# ------------------------------------------------------------------ pictures

def load_rgb(path):
    """A picture as RGB, what is transparent (Excel's chart area outside its corners) on white"""
    from PIL import Image
    im = Image.open(path)
    if im.mode == "P" and "transparency" in im.info:
        im = im.convert("RGBA")
    if im.mode in ("RGBA", "LA"):
        bg = Image.new("RGBA", im.size, (255, 255, 255, 255))
        im = Image.alpha_composite(bg, im.convert("RGBA"))
    return im.convert("RGB")


def render(pdf, page, box, width_px, height_px):
    """The PDF's chart box drawn at width_px by height_px"""
    x, y, w, h = box
    scale = width_px / w
    dpi = 72 * scale
    with tempfile.TemporaryDirectory() as d:
        out = os.path.join(d, "r")
        subprocess.run(["pdftoppm", "-png", "-singlefile", "-f", str(page), "-l", str(page), "-r", "%.4f" % dpi,
                        "-x", str(int(round(x * scale))), "-y", str(int(round(y * scale))),
                        "-W", str(width_px), "-H", str(height_px), pdf, out], check=True)
        return load_rgb(out + ".png").copy()


def score_raster(a_path, b_path, box=None, page=1, diff_out=None):
    import numpy as np
    a = load_rgb(a_path)
    if b_path.lower().endswith(".pdf"):
        if not box:
            raise SystemExit("raster against a PDF needs --box")
        b = render(b_path, page, box, a.width, a.height)
    else:
        b = load_rgb(b_path)
    if b.size != a.size:
        b = b.resize(a.size)
    A = np.asarray(a).astype(np.int16)
    B = np.asarray(b).astype(np.int16)
    plain = np.abs(A - B).max(axis=2)
    pad = np.pad(B, ((1, 1), (1, 1), (0, 0)), mode="edge")
    best = None
    for dy in (0, 1, 2):
        for dx in (0, 1, 2):
            d = np.abs(A - pad[dy:dy + A.shape[0], dx:dx + A.shape[1]]).max(axis=2)
            best = d if best is None else np.minimum(best, d)
    if diff_out:
        from PIL import Image
        Image.fromarray((255 - np.clip(best * 3, 0, 255)).astype(np.uint8)).save(diff_out)
    n = float(best.size)
    return {"size": [a.width, a.height], "over32": r2(100 * (best > 32).sum() / n), "over64": r2(100 * (best > 64).sum() / n),
            "over32Unshifted": r2(100 * (plain > 32).sum() / n), "mean": r2(float(best.mean()))}


# ------------------------------------------------------------------ Office against Office

def baseline(d):
    import chart_trace
    rows = []
    probes = list(csv.DictReader(open(os.path.join(d, "probes", "probes.tsv"), encoding="utf-8"), delimiter="\t"))
    for app in ("word", "powerpoint"):
        os.makedirs(os.path.join(d, "traces", app), exist_ok=True)

    def load(p):
        return json.load(open(p, encoding="utf-8-sig")) if os.path.exists(p) else None

    for p in probes:
        name, w, h = p["name"], float(p["width"]), float(p["height"])
        traces = {}
        for app, at in (("word", p["docx_at"]), ("powerpoint", p["pptx_at"])):
            pdf = os.path.join(d, app, name + ".pdf")
            if not at or not os.path.exists(pdf):
                continue
            x, y = (float(v) for v in at.split(","))
            t = chart_trace.trace(pdf, 1, (x, y, w, h))
            with open(os.path.join(d, "traces", app, name + ".json"), "w", encoding="utf-8") as f:
                f.write(chart_trace.dumps(t))
            traces[app] = t
        dumps = {app: load(os.path.join(d, app, name + ".json")) for app in ("excel", "word", "powerpoint")}
        for dump_app, trace_app in (("excel", "word"), ("word", "word"), ("powerpoint", "powerpoint")):
            if dumps[dump_app] and traces.get(trace_app):
                s = score_layout(dumps[dump_app], traces[trace_app])["summary"]
                inside = s.get("inside") or [None] * 4
                rows.append({"probe": name, "set": p["set"], "compare": "%s layout / %s pdf" % (dump_app, trace_app),
                             "labels": s["labels"], "labelMax": s["labelDistance"].get("max"),
                             "labelP95": s["labelDistance"].get("p95"), "missing": s["missing"], "points": s["points"],
                             "pointMax": s["pointDistance"].get("max"), "pointP95": s["pointDistance"].get("p95"),
                             "insideL": inside[0], "insideT": inside[1], "insideR": inside[2], "insideB": inside[3],
                             "axesAgree": s["axesAgree"], "noGeometry": s["noGeometry"], "undrawn": s["undrawn"],
                             "raster": s["raster"]})
        png = os.path.join(d, "excel", name + ".png")
        if os.path.exists(png) and traces.get("word"):
            bx = traces["word"]["box"]
            r = score_raster(png, os.path.join(d, "word", name + ".pdf"), bx)
            rows.append({"probe": name, "set": p["set"], "compare": "excel png / word pdf", "over32": r["over32"],
                         "over64": r["over64"], "mean": r["mean"]})
    cols = ["probe", "set", "compare", "labels", "labelMax", "labelP95", "missing", "points", "pointMax", "pointP95",
            "insideL", "insideT", "insideR", "insideB", "axesAgree", "noGeometry", "undrawn", "raster", "over32", "over64", "mean"]
    with open(os.path.join(d, "baseline.tsv"), "w", encoding="utf-8", newline="") as f:
        wr = csv.DictWriter(f, cols, delimiter="\t", extrasaction="ignore")
        wr.writeheader()
        for r in rows:
            wr.writerow(r)
    out = []
    for comp in sorted({r["compare"] for r in rows}):
        rs = [r for r in rows if r["compare"] == comp]
        out.append("%s: %d probes" % (comp, len(rs)))
        for k in ("labelMax", "pointMax", "insideL", "insideT", "insideR", "insideB", "over32", "over64", "mean"):
            vals = [abs(r[k]) for r in rs if r.get(k) is not None]
            if vals:
                out.append("  %-9s %s" % (k, json.dumps(stats(vals))))
        if any("axesAgree" in r for r in rs):
            bad = [r["probe"] for r in rs if r.get("axesAgree") is False]
            out.append("  axes disagree: %d %s" % (len(bad), " ".join(bad)))
        miss = [r["probe"] for r in rs if r.get("missing")]
        if miss:
            out.append("  something not found: %s" % " ".join(miss))
    with open(os.path.join(d, "baseline-summary.txt"), "w", encoding="utf-8") as f:
        f.write("\n".join(out) + "\n")
    print("\n".join(out))


def compact(v):
    """A dump without its nulls, its numbers to 0.001"""
    if isinstance(v, dict):
        return {k: compact(x) for k, x in v.items() if x is not None and compact(x) is not None}
    if isinstance(v, list):
        return [compact(x) for x in v]
    if isinstance(v, float):
        return round(v, 3)
    return v


def fixtures(d, out):
    import shutil
    probes = list(csv.DictReader(open(os.path.join(d, "probes", "probes.tsv"), encoding="utf-8"), delimiter="\t"))
    for sub in ("probes", "excel-layout", "powerpoint-layout", "word-pdf", "powerpoint-pdf"):
        os.makedirs(os.path.join(out, sub), exist_ok=True)
    shutil.copy(os.path.join(d, "probes", "probes.tsv"), out)
    n = 0
    for p in probes:
        name = p["name"]
        shutil.copy(os.path.join(d, "probes", name + ".xlsx"), os.path.join(out, "probes"))
        if "pptx" in p["hosts"]:
            shutil.copy(os.path.join(d, "probes", name + ".pptx"), os.path.join(out, "probes"))
        styled = os.path.join(d, "excel", name + "-styled.xlsx")
        if os.path.exists(styled):
            shutil.copy(styled, os.path.join(out, "probes"))
        for app, sub in (("excel", "excel-layout"), ("powerpoint", "powerpoint-layout")):
            src = os.path.join(d, app, name + ".json")
            if os.path.exists(src):
                j = compact(json.load(open(src, encoding="utf-8-sig")))
                with open(os.path.join(out, sub, name + ".json"), "w", encoding="utf-8") as f:
                    f.write("{\n" + ",\n".join(" %s: %s" % (json.dumps(k), json.dumps(v, separators=(",", ":")))
                                                for k, v in j.items()) + "\n}\n")
        # R14's sweeps: one row a size
        src = os.path.join(d, "excel", name + ".sweep.json")
        if os.path.exists(src):
            j = compact(json.load(open(src, encoding="utf-8-sig")))
            with open(os.path.join(out, "excel-layout", name + ".sweep.json"), "w", encoding="utf-8") as f:
                f.write('{"probe": %s, "sweep": %s, "rows": [\n' % (json.dumps(j["probe"]), json.dumps(j["sweep"]))
                        + ",\n".join(json.dumps(r, separators=(",", ":")) for r in j.get("rows", [])) + "\n]}\n")
        for app, sub in (("word", "word-pdf"), ("powerpoint", "powerpoint-pdf")):
            src = os.path.join(d, "traces", app, name + ".json")
            if os.path.exists(src):
                shutil.copy(src, os.path.join(out, sub))
        n += 1
    print("%d probes into %s" % (n, out))


def main():
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    sub = ap.add_subparsers(dest="cmd", required=True)
    t = sub.add_parser("traces")
    t.add_argument("a")
    t.add_argument("b")
    t.add_argument("--json", action="store_true")
    l = sub.add_parser("layout")
    l.add_argument("dump")
    l.add_argument("trace")
    l.add_argument("--json", action="store_true")
    r = sub.add_parser("raster")
    r.add_argument("a")
    r.add_argument("b")
    r.add_argument("--box")
    r.add_argument("--page", type=int, default=1)
    r.add_argument("--diff")
    b = sub.add_parser("baseline")
    b.add_argument("dir")
    x = sub.add_parser("fixtures")
    x.add_argument("dir")
    x.add_argument("out")
    a = ap.parse_args()
    if a.cmd == "traces":
        res = score_traces(json.load(open(a.a)), json.load(open(a.b)))
        print(json.dumps(res if a.json else res["summary"], indent=1))
    elif a.cmd == "layout":
        res = score_layout(json.load(open(a.dump, encoding="utf-8-sig")), json.load(open(a.trace)))
        print(json.dumps(res if a.json else res["summary"], indent=1))
    elif a.cmd == "raster":
        box = tuple(float(v) for v in re.split(r"[,\s]+", a.box.strip())) if a.box else None
        print(json.dumps(score_raster(a.a, a.b, box, a.page, a.diff), indent=1))
    elif a.cmd == "fixtures":
        fixtures(a.dir, a.out)
    else:
        baseline(a.dir)


if __name__ == "__main__":
    main()
