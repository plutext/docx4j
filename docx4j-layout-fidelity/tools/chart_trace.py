#!/usr/bin/env python3
# Copyright 2026, Plutext Pty Ltd.
#
# This file is part of docx4j.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
# except in compliance with the License.  You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
"""
A chart's drawing in a PDF, as numbers (CR-037 phase 0): what Word, PowerPoint or docx4j drew in
the chart's box, read with `mutool trace`.

  texts   each line of text: its string, font (the subset prefix dropped), size, colour, the
          first glyph's origin (x, y on the baseline), the baseline's end (x1, y1: the last
          glyph's origin plus its advance), the baseline's angle (degrees, clockwise, 0 level),
          and its centre (cx, cy: the baseline's midpoint, raised a third of the size)
  shapes  each filled or stroked path: kind, colour (RRGGBB, /alpha below 1), line width, its
          box, and its parts (one per subpath: box and vertices, curves sampled), so that each
          bar, slice, marker and gridline is a part
  images  pictures and shadings: the box they fill, and the outline of the clip they fill (a
          gallery style's gradient bar or picture-filled slice), as a shape's parts

Coordinates are points, y down, from the chart box's top left.  Glyphs are joined into a line
where they share font, size, colour and baseline and follow one another (a space put in where
the gap is wider than 0.3 of the size and the PDF has none); text outside the box, and
whitespace, is left out.

The chart's box: --box x,y,w,h (points on the page, y down from its top), as probes.tsv gives
for a probe; a clip or filled path of that size near there (Word clips a chart to its box and
fills its chart area) is taken instead where there is one, and "boxFrom" says which.  Without
--box, the first clip on the page smaller than the page.

usage: chart_trace.py PDF [--page N] [--box x,y,w,h] > trace.json
"""
import argparse
import json
import math
import os
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from office_colours import colour, matrix, apply  # noqa: E402


def r2(v):
    return round(v, 2)


def bezier(p0, p1, p2, p3, tol=0.02):
    """A cubic's points, as many as keep the polyline within tol of the curve (a curve is within
    3/4 of its control points' distance from its chord, and n pieces bring that down n squared)"""
    def off(c):
        dx, dy = p3[0] - p0[0], p3[1] - p0[1]
        L = math.hypot(dx, dy)
        return math.hypot(c[0] - p0[0], c[1] - p0[1]) if L == 0 else abs((c[0] - p0[0]) * dy - (c[1] - p0[1]) * dx) / L
    d = 0.75 * max(off(p1), off(p2))
    n = max(1, min(64, int(math.ceil(math.sqrt(d / tol))))) if d > tol else 1
    out = []
    for i in range(1, n + 1):
        t = i / n
        u = 1 - t
        out.append((u * u * u * p0[0] + 3 * u * u * t * p1[0] + 3 * u * t * t * p2[0] + t * t * t * p3[0],
                    u * u * u * p0[1] + 3 * u * u * t * p1[1] + 3 * u * t * t * p2[1] + t * t * t * p3[1]))
    return out


def subpaths(el):
    """The path's subpaths in device space, each a list of points (curves sampled)"""
    m = matrix(el.get("transform"))
    parts, cur, last, start = [], [], None, None

    def pt(x, y):
        return apply(m, float(x), float(y))

    for p in el:
        t = p.tag
        if t == "moveto":
            if len(cur) > 1:
                parts.append(cur)
            last = start = pt(p.get("x"), p.get("y"))
            cur = [last]
        elif t == "lineto":
            last = pt(p.get("x"), p.get("y"))
            cur.append(last)
        elif t in ("curveto", "curvetov", "curvetoy"):
            if t == "curveto":
                c1, c2 = pt(p.get("x1"), p.get("y1")), pt(p.get("x2"), p.get("y2"))
            elif t == "curvetov":
                c1, c2 = last, pt(p.get("x2"), p.get("y2"))
            else:
                c1 = pt(p.get("x1"), p.get("y1"))
                c2 = None
            end = pt(p.get("x3"), p.get("y3"))
            if c2 is None:
                c2 = end
            cur.extend(bezier(last, c1, c2, end))
            last = end
        elif t == "closepath":
            if start is not None and cur and cur[-1] != start:
                cur.append(start)
            if len(cur) > 1:
                parts.append(cur)
            cur = [start] if start else []
        elif t == "rect":
            x, y, w, h = (float(p.get(k)) for k in ("x", "y", "w", "h"))
            if len(cur) > 1:
                parts.append(cur)
            parts.append([pt(x, y), pt(x + w, y), pt(x + w, y + h), pt(x, y + h), pt(x, y)])
            cur = []
    if len(cur) > 1:
        parts.append(cur)
    return parts


def bbox(points):
    xs, ys = [p[0] for p in points], [p[1] for p in points]
    return min(xs), min(ys), max(xs), max(ys)


def union(boxes):
    return min(b[0] for b in boxes), min(b[1] for b in boxes), max(b[2] for b in boxes), max(b[3] for b in boxes)


def read(pdf, page_no):
    """The page's drawing in painting order: clips, paths, glyphs, images"""
    trace = subprocess.run(["mutool", "trace", pdf, str(page_no)], capture_output=True, text=True, check=True).stdout
    root = ET.fromstring(trace)
    page = next(root.iter("page"))
    mb = [float(v) for v in page.get("mediabox").split()]
    items = []
    # the clips in force (device space: box, and a path clip's subpaths), each popped by its pop_clip
    clips = [((0.0, 0.0, mb[2] - mb[0], mb[3] - mb[1]), None)]
    for el in page.iter():
        tag = el.tag
        if tag == "pop_clip":
            if len(clips) > 1:
                clips.pop()
            continue
        if tag in ("clip_text", "clip_stroke_text", "clip_image_mask"):
            clips.append(clips[-1])
        if tag in ("fill_path", "stroke_path", "clip_path", "clip_stroke_path"):
            parts = subpaths(el)
            if tag in ("clip_path", "clip_stroke_path"):
                c = clips[-1][0]
                b = union([bbox(p) for p in parts]) if parts else c
                clips.append(((max(b[0], c[0]), max(b[1], c[1]), min(b[2], c[2]), min(b[3], c[3])), parts or clips[-1][1]))
            if not parts:
                continue
            item = {"tag": tag, "parts": parts, "box": union([bbox(p) for p in parts])}
            if tag in ("fill_path", "stroke_path"):
                item["colour"] = colour(el)
                alpha = el.get("alpha")
                item["alpha"] = float(alpha) if alpha is not None else 1.0
                if tag == "stroke_path":
                    item["width"] = float(el.get("linewidth", "1"))
            items.append(item)
        elif tag == "fill_text":
            run_no = len(items)
            m = matrix(el.get("transform"))
            col = colour(el)
            alpha = el.get("alpha")
            for span in el.iter("span"):
                trm = matrix(span.get("trm"))
                face = (span.get("font") or "").split("+")[-1]
                # the baseline's direction on the page: the text matrix's x axis through the transform
                dx, dy = trm[0] * m[0] + trm[1] * m[2], trm[0] * m[1] + trm[1] * m[3]
                size = math.hypot(trm[0], trm[1])
                for g in span.iter("g"):
                    x, y = apply(m, float(g.get("x")), float(g.get("y")))
                    adv = float(g.get("adv", "0")) * size
                    items.append({"tag": "glyph", "char": g.get("unicode", ""), "x": x, "y": y, "adv": adv,
                                  "dir": (dx, dy), "size": size, "font": face, "colour": col, "run": run_no,
                                  "alpha": float(alpha) if alpha is not None else 1.0})
        elif tag in ("fill_image", "fill_shade", "fill_image_mask"):
            # a shading fills its clip; a picture its unit square, within its clip; the clip's outline
            # is the shape (a gallery style's gradient bar, or a slice filled with a picture)
            c, outline = clips[-1]
            if tag == "fill_shade":
                b = c
            else:
                m = matrix(el.get("transform"))
                p = bbox([apply(m, x, y) for x, y in ((0, 0), (1, 0), (1, 1), (0, 1))])
                b = (max(p[0], c[0]), max(p[1], c[1]), min(p[2], c[2]), min(p[3], c[3]))
            items.append({"tag": tag, "box": b, "parts": outline or []})
    return mb, items


def find_box(items, page_w, page_h, given):
    """The chart's box: a clip or fill of the given box's size near it, else the given box"""
    if given:
        gx, gy, gw, gh = given
        best = None
        for it in items:
            if it["tag"] not in ("clip_path", "fill_path"):
                continue
            x0, y0, x1, y1 = it["box"]
            if abs((x1 - x0) - gw) <= 1.0 and abs((y1 - y0) - gh) <= 1.0 and abs(x0 - gx) <= 24 and abs(y0 - gy) <= 24:
                d = abs(x0 - gx) + abs(y0 - gy)
                if best is None or d < best[0]:
                    best = (d, it)
        if best:
            x0, y0, x1, y1 = best[1]["box"]
            return (x0, y0, x1 - x0, y1 - y0), "clip" if best[1]["tag"] == "clip_path" else "fill"
        return given, "given"
    for it in items:
        if it["tag"] == "clip_path":
            x0, y0, x1, y1 = it["box"]
            if (x1 - x0) < page_w - 1 or (y1 - y0) < page_h - 1:
                return (x0, y0, x1 - x0, y1 - y0), "clip"
    return (0, 0, page_w, page_h), "page"


def lines(glyphs):
    """Glyphs joined into lines of text"""
    out, cur = [], None
    for g in glyphs:
        if cur is not None:
            p = cur["glyphs"][-1]
            ux, uy = p["dir"][0] / (math.hypot(*p["dir"]) or 1), p["dir"][1] / (math.hypot(*p["dir"]) or 1)
            along = (g["x"] - p["x"]) * ux + (g["y"] - p["y"]) * uy
            across = -(g["x"] - p["x"]) * uy + (g["y"] - p["y"]) * ux
            same = (g["font"] == p["font"] and abs(g["size"] - p["size"]) < 0.01 and g["colour"] == p["colour"]
                    and abs(across) < 0.5 and abs(g["dir"][0] * p["dir"][1] - g["dir"][1] * p["dir"][0]) < 1e-3)
            gap = along - p["adv"]
            # glyphs of another text run join only where they touch: Word draws each category
            # label as a run, and narrow categories set them 2.4pt apart (R12)
            limit = 0.6 * p["size"] if g["run"] == p["run"] else 0.1 * p["size"]
            if same and -0.3 * p["size"] <= gap <= limit:
                # (Word writes its spaces; a smaller gap is letter spacing, a gallery style's capitals)
                if gap > 0.3 * p["size"] and g["char"] != " " and p["char"] != " ":
                    cur["text"] += " "
                cur["text"] += g["char"]
                cur["glyphs"].append(g)
                continue
            out.append(cur)
        cur = {"text": g["char"], "glyphs": [g]}
    if cur is not None:
        out.append(cur)
    return out


def trace(pdf, page_no=1, given=None):
    mb, items = read(pdf, page_no)
    page_w, page_h = mb[2] - mb[0], mb[3] - mb[1]
    (bx, by, bw, bh), box_from = find_box(items, page_w, page_h, given)

    def inside(x, y, slack=0.5):
        return bx - slack <= x <= bx + bw + slack and by - slack <= y <= by + bh + slack

    def overlaps(b, slack=1.0):
        return b[2] >= bx - slack and b[0] <= bx + bw + slack and b[3] >= by - slack and b[1] <= by + bh + slack

    def rel(b):
        return [r2(b[0] - bx), r2(b[1] - by), r2(b[2] - bx), r2(b[3] - by)]

    texts = []
    for ln in lines([it for it in items if it["tag"] == "glyph"]):
        s = ln["text"].strip()
        g0, gl = ln["glyphs"][0], ln["glyphs"][-1]
        if not s or not inside(g0["x"], g0["y"]):
            continue
        ux, uy = g0["dir"][0] / (math.hypot(*g0["dir"]) or 1), g0["dir"][1] / (math.hypot(*g0["dir"]) or 1)
        # without the spaces at either end
        gs = [g for g in ln["glyphs"]]
        while gs and gs[0]["char"] == " ":
            gs.pop(0)
        while gs and gs[-1]["char"] == " ":
            gs.pop()
        g0, gl = gs[0], gs[-1]
        x1, y1 = gl["x"] + gl["adv"] * ux, gl["y"] + gl["adv"] * uy
        # the centre: the baseline's midpoint, raised (against the baseline's normal) a third of the size
        mx, my = (g0["x"] + x1) / 2, (g0["y"] + y1) / 2
        nx, ny = uy, -ux
        size = g0["size"]
        cx, cy = mx + nx * size / 3, my + ny * size / 3
        texts.append({"text": s, "font": g0["font"], "size": r2(size), "colour": g0["colour"],
                      "x": r2(g0["x"] - bx), "y": r2(g0["y"] - by), "x1": r2(x1 - bx), "y1": r2(y1 - by),
                      "angle": r2(math.degrees(math.atan2(uy, ux))), "cx": r2(cx - bx), "cy": r2(cy - by)})

    shapes, images = [], []
    for it in items:
        if it["tag"] in ("fill_path", "stroke_path"):
            if not overlaps(it["box"]):
                continue
            sh = {"kind": "fill" if it["tag"] == "fill_path" else "stroke", "colour": it["colour"],
                  "box": rel(it["box"]), "parts": []}
            if it["alpha"] < 0.999:
                sh["alpha"] = r2(it["alpha"])
            if "width" in it:
                sh["width"] = r2(it["width"])
            for p in it["parts"]:
                q = [p[0]]
                for v in p[1:]:
                    if abs(v[0] - q[-1][0]) > 0.005 or abs(v[1] - q[-1][1]) > 0.005:
                        q.append(v)
                sh["parts"].append({"box": rel(bbox(q)), "points": [[r2(v[0] - bx), r2(v[1] - by)] for v in q]})
            shapes.append(sh)
        elif it["tag"] in ("fill_image", "fill_shade", "fill_image_mask"):
            if overlaps(it["box"]):
                images.append({"kind": it["tag"], "box": rel(it["box"]),
                               "parts": [{"box": rel(bbox(p)), "points": [[r2(v[0] - bx), r2(v[1] - by)] for v in p]}
                                         for p in it["parts"]]})
    return {"source": os.path.basename(pdf), "page": page_no, "pageSize": [r2(page_w), r2(page_h)],
            "box": [r2(bx), r2(by), r2(bw), r2(bh)], "boxFrom": box_from,
            "texts": texts, "shapes": shapes, "images": images}


def dumps(t):
    """One text, shape or image a line, so that a diff of two traces reads"""
    head = {k: v for k, v in t.items() if k not in ("texts", "shapes", "images")}
    out = ["{"]
    for k, v in head.items():
        out.append(" %s: %s," % (json.dumps(k), json.dumps(v)))
    for k in ("texts", "shapes", "images"):
        rows = [" " + json.dumps(x, separators=(",", ":")) for x in t[k]]
        out.append(' "%s": [' % k + ("\n" + ",\n".join(rows) + "\n " if rows else "") + "]" + ("," if k != "images" else ""))
    out.append("}")
    return "\n".join(out) + "\n"


def main():
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("pdf")
    ap.add_argument("--page", type=int, default=1)
    ap.add_argument("--box", help="x,y,w,h in points on the page, y down")
    a = ap.parse_args()
    given = tuple(float(v) for v in re.split(r"[,\s]+", a.box.strip())) if a.box else None
    sys.stdout.write(dumps(trace(a.pdf, a.page, given)))


if __name__ == "__main__":
    main()
