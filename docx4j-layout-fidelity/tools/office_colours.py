#!/usr/bin/env python3
# Copyright 2026, Plutext Pty Ltd.
#
# This file is part of docx4j.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
# except in compliance with the License.  You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
"""
The colours Office drew for each labelled case of a CR-034 probe (ThemeColourProbes), read from
Office's PDF with `mutool trace`.

Each case's label (T001, X042, P1-03, ...) is text Office drew inside or beside the thing
coloured.  For each label this reports:
  fill    the smallest filled path (fill_path) whose box holds the label's first glyph and that
          was painted before it: a shape's or a cell's fill, or a paragraph's shading
  stroke  a stroked path (stroke_path) whose box is the fill's box, to 2pt: a shape's outline
  text    the colour the label itself was drawn in
  rules   thin filled or stroked paths (under 4pt high) from 22pt above the label's baseline to
          8pt below it, overlapping it: paragraph borders and underlines
Colours are RRGGBB, with /alpha where the PDF gives one below 1; DeviceGray is written as RGB.

usage: office_colours.py CASES.TSV PROBE PDF > readings.tsv
       (PROBE is the probe's file name in cases.tsv, e.g. tc-p3-transforms.pptx)
"""
import re
import subprocess
import sys
import xml.etree.ElementTree as ET


def matrix(s):
    return [float(x) for x in s.split()] if s else [1, 0, 0, 1, 0, 0]


def apply(m, x, y):
    a, b, c, d, e, f = m
    return a * x + c * y + e, b * x + d * y + f


def colour(el):
    space, vals = el.get("colorspace", ""), el.get("color", "")
    try:
        v = [float(x) for x in vals.split()]
    except ValueError:
        return "?"
    if space.endswith("Gray") and len(v) == 1:
        v = v * 3
    elif space.endswith("CMYK") and len(v) == 4:
        c, m, y, k = v
        v = [(1 - c) * (1 - k), (1 - m) * (1 - k), (1 - y) * (1 - k)]
    elif len(v) != 3:
        return space + ":" + vals
    s = "".join("%02X" % max(0, min(255, int(round(x * 255)))) for x in v)
    alpha = el.get("alpha")
    if alpha is not None and float(alpha) < 0.999:
        s += "/%.3f" % float(alpha)
    return s


def box(el):
    m = matrix(el.get("transform"))
    pts = []
    for p in el:
        for i in ("", "1", "2", "3"):
            if p.get("x" + i) is not None and p.get("y" + i) is not None:
                pts.append(apply(m, float(p.get("x" + i)), float(p.get("y" + i))))
    if not pts:
        return None
    xs, ys = [p[0] for p in pts], [p[1] for p in pts]
    return min(xs), min(ys), max(xs), max(ys)


def pages(pdf):
    """Per page, in painting order: ('fill'|'stroke', colour, box) and ('text', colour, text, [(x, y)])"""
    trace = subprocess.run(["mutool", "trace", pdf], capture_output=True, text=True, check=True).stdout
    root = ET.fromstring(trace)
    out = []
    for page in root.iter("page"):
        items = []
        for el in page.iter():
            if el.tag in ("fill_path", "stroke_path"):
                b = box(el)
                if b:
                    items.append(("fill" if el.tag == "fill_path" else "stroke", colour(el), b))
            elif el.tag == "fill_text":
                m = matrix(el.get("transform"))
                chars, pos = [], []
                for g in el.iter("g"):
                    chars.append(g.get("unicode", ""))
                    pos.append(apply(m, float(g.get("x")), float(g.get("y"))))
                items.append(("text", colour(el), "".join(chars), pos))
        out.append(items)
    return out


def inside(b, x, y, slack=0.5):
    return b[0] - slack <= x <= b[2] + slack and b[1] - slack <= y <= b[3] + slack


def main():
    cases_file, probe, pdf = sys.argv[1:4]
    labels = [l.split("\t")[1] for l in open(cases_file, encoding="utf-8").read().splitlines()[1:]
              if l.split("\t")[0] == probe]
    found = {}
    for pn, items in enumerate(pages(pdf), 1):
        # the page's text as one stream: Word splits a label like P1-01 into "P1", "-" and "01"
        chars = []   # (character, item index, x, y, colour)
        for i, it in enumerate(items):
            if it[0] == "text":
                for ch, (x, y) in zip(it[2], it[3]):
                    chars.append((ch, i, x, y, it[1]))
        stream = "".join(c[0] for c in chars)
        for lab in labels:
            if lab in found:
                continue
            k = stream.find(lab)
            while k >= 0 and k + len(lab) < len(stream) and stream[k + len(lab)].isdigit():
                k = stream.find(lab, k + 1)
            if k < 0:
                continue
            _, i, x, y, text_colour = chars[k]
            it = ("text", text_colour, lab, [(c[2], c[3]) for c in chars[k:k + len(lab)]])
            before = [p for p in items[:i] if p[0] == "fill" and inside(p[2], x, y)]
            fill = min(before, key=lambda p: (p[2][2] - p[2][0]) * (p[2][3] - p[2][1])) if before else None
            stroke = None
            if fill:
                for p in items:
                    if p[0] == "stroke" and all(abs(p[2][j] - fill[2][j]) <= 2 for j in range(4)):
                        stroke = p
                        break
            xs = [q[0] for q in it[3]]
            rules = sorted({p[1] for p in items if p[0] in ("fill", "stroke")
                            and p[2][3] - p[2][1] < 4 and -22 < (p[2][1] + p[2][3]) / 2 - y < 8
                            and p[2][0] < max(xs) and p[2][2] > min(xs)})
            found[lab] = (pn, fill[1] if fill else "", stroke[1] if stroke else "", it[1], " ".join(rules))
    print("label\tpage\tfill\tstroke\ttext\trules")
    for lab in labels:
        r = found.get(lab)
        print(lab + "\t" + ("\t".join(str(x) for x in r) if r else "NOT FOUND"))


if __name__ == "__main__":
    main()
