#!/usr/bin/env python3
# Copyright 2026, Plutext Pty Ltd.
#
# This file is part of docx4j.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
# except in compliance with the License.  You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
"""
WordprocessingML theme colours in a set of docx files, against the colour Word wrote beside
them (CR-034, docs/developer/change-requests/CR-034-theme-colours-resolved.md, section 3).

Word writes a resolved value beside every theme reference: w:color/@w:val beside
@w:themeColor (+ @w:themeTint / @w:themeShade), w:shd/@w:fill beside @w:themeFill, and so on.
This reads every such pair in document.xml, styles.xml, numbering.xml, headers, footers,
notes and comments, resolves the reference against the document's own theme and
w:clrSchemeMapping, and counts how often the written value is reproduced by the HSL
luminance rule (tint t: L' = L*t + 1 - t; shade t: L' = L*t), rounded or truncated.

Prints counts only.  The corpora are not redistributable; the two optional outputs carry no
document content:
  --triples FILE  the distinct (theme colour value, op, byte, Word's value) found, the test
                  vectors of CR-034 section 4.6
  --stale FILE    each attribute whose written value disagrees with its theme by more than one
                  unit: document, part, element, attributes and the run's first 40 characters,
                  for reading Word's own PDF of that document (CR-034 phase 0, P1)

usage: theme_colour_scan.py [--triples FILE] [--stale FILE] DOCX...
"""
import argparse
import collections
import colorsys
import posixpath
import re
import sys
import xml.etree.ElementTree as ET
import zipfile

W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
A = "http://schemas.openxmlformats.org/drawingml/2006/main"
R = "http://schemas.openxmlformats.org/package/2006/relationships"


def w(n):
    return "{%s}%s" % (W, n)


# w:themeColor values naming a scheme slot directly, and those going through w:clrSchemeMapping
DIRECT = {"dark1": "dk1", "light1": "lt1", "dark2": "dk2", "light2": "lt2",
          "accent1": "accent1", "accent2": "accent2", "accent3": "accent3",
          "accent4": "accent4", "accent5": "accent5", "accent6": "accent6",
          "hyperlink": "hlink", "followedHyperlink": "folHlink"}
MAPPED = {"text1": ("t1", "dark1"), "background1": ("bg1", "light1"),
          "text2": ("t2", "dark2"), "background2": ("bg2", "light2")}

# the main document part is word/document.xml, or another name Word sometimes gives it (document2.xml)
PARTS = re.compile(r"word/(document\d*|styles|numbering|header\d*|footer\d*|footnotes|endnotes|comments)\.xml$")


def main_part(z, names):
    """The main document part's name, from the package relationships"""
    if "_rels/.rels" in names:
        for rel in ET.fromstring(z.read("_rels/.rels")).iter("{%s}Relationship" % R):
            if rel.get("Type", "").endswith("/officeDocument"):
                return rel.get("Target").lstrip("/")
    return "word/document.xml"


def scheme(z, names):
    """The main document's theme colour scheme: slot -> hex (srgbClr val, or sysClr lastClr)"""
    main = main_part(z, names)
    rels = posixpath.join(posixpath.dirname(main), "_rels", posixpath.basename(main) + ".rels")
    if rels not in names:
        return None
    for rel in ET.fromstring(z.read(rels)).iter("{%s}Relationship" % R):
        if rel.get("Type", "").endswith("/theme"):
            target = rel.get("Target")
            # a Target may be absolute (/word/theme/theme1.xml) as well as relative to word/
            p = target.lstrip("/") if target.startswith("/") else posixpath.normpath(posixpath.join("word", target))
            if p not in names:
                return None
            cs = ET.fromstring(z.read(p)).find(".//{%s}clrScheme" % A)
            out = {}
            for slot in cs if cs is not None else []:
                c = slot[0]
                v = c.get("val") if c.tag.endswith("srgbClr") else c.get("lastClr")
                if v:
                    out[slot.tag.split("}")[1]] = v.upper()
            return out
    return None


def mapping(z, names):
    m = {}
    if "word/settings.xml" in names:
        e = ET.fromstring(z.read("word/settings.xml")).find(w("clrSchemeMapping"))
        if e is not None:
            for k, v in e.attrib.items():
                m[k.split("}")[1]] = v
    return m


def base(theme_color, sch, mp):
    if theme_color in DIRECT:
        slot = DIRECT[theme_color]
    elif theme_color in MAPPED:
        attr, default = MAPPED[theme_color]
        slot = DIRECT.get(mp.get(attr, default))
    else:
        return None
    return sch.get(slot) if slot else None


def rgb(h):
    return tuple(int(h[i:i + 2], 16) / 255.0 for i in (0, 2, 4))


def to_hex(c, floor):
    f = (lambda x: int(x * 255 + 1e-4)) if floor else (lambda x: int(round(x * 255)))
    return "".join("%02X" % max(0, min(255, f(x))) for x in c)


def hsl(c, t, tint, floor):
    h, l, s = colorsys.rgb_to_hls(*c)
    return to_hex(colorsys.hls_to_rgb(h, l * t + (1 - t) if tint else l * t, s), floor)


def near(a, b, tol=1):
    return all(abs(int(a[i:i + 2], 16) - int(b[i:i + 2], 16)) <= tol for i in (0, 2, 4))


def run_text(el, parent_of):
    """The first 40 characters of the paragraph an element belongs to"""
    p = el
    while p is not None and p.tag != w("p"):
        p = parent_of.get(p)
    if p is None:
        return ""
    return "".join(t.text or "" for t in p.iter(w("t")))[:40]


def main():
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--triples")
    ap.add_argument("--stale")
    ap.add_argument("docx", nargs="+")
    args = ap.parse_args()

    n = collections.Counter()
    per_element = collections.Counter()
    triples = set()
    stale = []
    for path in args.docx:
        try:
            z = zipfile.ZipFile(path)
        except Exception:
            n["unreadable"] += 1
            continue
        names = set(z.namelist())
        sch, mp = scheme(z, names), mapping(z, names)
        n["documents"] += 1
        if mp and any(mp.get(k, d) != d for k, (_, d) in
                      {"bg1": (0, "light1"), "t1": (0, "dark1"), "bg2": (0, "light2"), "t2": (0, "dark2")}.items()):
            n["documents remapping clrSchemeMapping"] += 1
        found = False
        for part in sorted(x for x in names if PARTS.match(x)):
            root = ET.fromstring(z.read(part))
            parent_of = {c: p for p in root.iter() for c in p}
            for e in root.iter():
                # (theme attribute, tint, shade, the attribute holding Word's value)
                for tc, tt, ts, val in (("themeColor", "themeTint", "themeShade", "val" if e.tag == w("color") else "color"),
                                        ("themeFill", "themeFillTint", "themeFillShade", "fill")):
                    theme = e.get(w(tc))
                    if theme is None:
                        continue
                    found = True
                    per_element[e.tag.split("}")[1] + "@" + tc] += 1
                    tint, shade = e.get(w(tt)), e.get(w(ts))
                    op = "tint" if tint else "shade" if shade else "plain"
                    n["attributes " + op] += 1
                    if sch is None:
                        n["in a document with no theme part"] += 1
                        continue
                    b = base(theme, sch, mp)
                    v = e.get(w(val))
                    if b is None or v is None or not re.fullmatch(r"[0-9A-Fa-f]{6}", v):
                        n["no hex value or unresolvable reference"] += 1
                        continue
                    v = v.upper()
                    if op == "plain":
                        ok = v == b
                        n["plain exact" if ok else "plain differs"] += 1
                        if not ok:
                            stale.append((path, part, e.tag.split("}")[1], tc, theme, "", b, v, run_text(e, parent_of)))
                        continue
                    t = int(tint or shade, 16) / 255.0
                    rounded, floored = hsl(rgb(b), t, bool(tint), False), hsl(rgb(b), t, bool(tint), True)
                    n[op + " exact (HSL, rounded)"] += rounded == v
                    n[op + " exact (HSL, truncated)"] += floored == v
                    within = near(rounded, v)
                    n[op + " within one unit"] += within
                    if within:
                        triples.add((b, op, (tint or shade).upper(), v))
                    else:
                        stale.append((path, part, e.tag.split("}")[1], tc, theme, op + " " + (tint or shade), b, v,
                                      run_text(e, parent_of)))
        if found:
            n["documents with a theme attribute"] += 1

    for k in sorted(n):
        print("%-50s %d" % (k, n[k]))
    print("--- attributes per element")
    for k, c in per_element.most_common():
        print("%-50s %d" % (k, c))
    if args.triples:
        with open(args.triples, "w") as f:
            f.write("theme value\top\tbyte\tword\n")
            for t in sorted(triples):
                f.write("\t".join(t) + "\n")
        print("wrote %d distinct triples to %s" % (len(triples), args.triples))
    if args.stale:
        with open(args.stale, "w") as f:
            f.write("document\tpart\telement\tattribute\ttheme colour\top\ttheme value\twritten\ttext\n")
            for s in stale:
                f.write("\t".join(s) + "\n")
        print("wrote %d disagreeing attributes to %s" % (len(stale), args.stale))


if __name__ == "__main__":
    sys.exit(main())
