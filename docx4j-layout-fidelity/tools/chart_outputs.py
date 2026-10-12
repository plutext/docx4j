#!/usr/bin/env python3
# Copyright 2026, Plutext Pty Ltd.
#
# This file is part of docx4j.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
# except in compliance with the License.  You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
"""
CR-037 phase 3's gate: Office's own charts through docx4j's outputs, against Office's PDFs of them.

Word's Insert Chart of each type (P3, docx) through docx4j's PDF output, by the visitor and by
the XSLT; PowerPoint's (P4, pptx) through docx4j's SVG-in-HTML output, printed by headless Chrome
with docx4j's font files given to it (--fonts: the font jars' files, unzipped; the HTML's text is
drawn in whatever fonts the browser has, so without them it measures the browser, not docx4j).
Each output's chart is read with chart_trace.py and compared with Office's reading (the oracle's
word-pdf/ and powerpoint-pdf/) by chart_score.py's traces, held to CR-037 D4's tolerances: every
text Office draws, its centre within 0.5pt, its size within 0.15pt, its colour; every shape's
part within 0.25pt, its colour; nothing missing or extra (but a fill larger than the chart: a
page's background).

usage: chart_outputs.py --cp CLASSPATH --out DIR [--oracle DIR] [--fonts DIR] [--only p3|p4]
       (CLASSPATH: docx4j-layout-fidelity's classes and docx4j with docx4j-chart-render; the
       oracle: docx4j-chart-render/src/test/resources/charts/render)
"""
import argparse
import csv
import json
import os
import subprocess
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from chart_trace import trace  # noqa: E402
from chart_score import score_traces  # noqa: E402

HERE = os.path.dirname(os.path.abspath(__file__))
ORACLE = os.path.join(HERE, "..", "..", "docx4j-chart-render", "src", "test", "resources", "charts", "render")
TEXT, SIZE, PART = 0.5, 0.15, 0.25


def failures(s, w, h):
    f = []
    for t in s["texts"]:
        if t["d"] is not None and t["d"] > TEXT:
            f.append("text %r off %.2f" % (t["text"], t["d"]))
        if t.get("size") and abs(t["size"][0] - t["size"][1]) > SIZE:
            f.append("text %r size %s" % (t["text"], t["size"]))
        if t.get("colour"):
            f.append("text %r colour %s" % (t["text"], t["colour"]))
    for t in s["missing"]:
        f.append("missing %r" % (t.get("text") if isinstance(t, dict) else t))
    for t in s["extra"]:
        f.append("extra %r" % (t.get("text") if isinstance(t, dict) else t))
    for q in s["parts"]:
        if q["d"] > PART:
            f.append("part %s %s off %.2f" % (q["kind"], q["colour"], q["d"]))
    for q in s["recoloured"]:
        f.append("recoloured %s" % json.dumps(q)[:120])
    for q in s["missingParts"]:
        b = q.get("box")
        if not (b and (b[0] < -1 or b[1] < -1 or b[2] > w + 1 or b[3] > h + 1)):
            f.append("missing part %s" % json.dumps(q)[:120])
    for q in s["extraParts"]:
        f.append("extra part %s" % json.dumps(q)[:120])
    return f


def java(cp, mode, pairs, props=()):
    out = subprocess.run(["java", "-Djava.awt.headless=true"] + list(props) + ["-cp", cp, "org.docx4j.fidelity.corpus.ChartOutputs", mode]
                         + ["%s=%s" % p for p in pairs], capture_output=True, text=True)
    times = {}
    for line in out.stdout.splitlines():
        if "\t" in line:
            n, ms = line.rsplit("\t", 1)
            times[n] = int(ms)
    if out.returncode != 0:
        sys.stderr.write(out.stderr[-3000:])
    return times


def chrome(xhtml, pdf, fonts):
    env = dict(os.environ)
    if fonts:
        conf = os.path.join(os.path.dirname(pdf), "fonts.conf")
        with open(conf, "w") as f:
            f.write('<?xml version="1.0"?><!DOCTYPE fontconfig SYSTEM "fonts.dtd"><fontconfig>'
                    '<include ignore_missing="yes">/etc/fonts/fonts.conf</include><dir>%s</dir>'
                    '<cachedir>%s</cachedir></fontconfig>' % (os.path.abspath(fonts), os.path.join(os.path.dirname(pdf), "fc-cache")))
        env["FONTCONFIG_FILE"] = conf
    for exe in ("google-chrome-stable", "google-chrome", "chromium", "chromium-browser"):
        try:
            subprocess.run([exe, "--headless=new", "--disable-gpu", "--no-sandbox", "--no-pdf-header-footer",
                            "--print-to-pdf=" + pdf, "file://" + os.path.abspath(xhtml)],
                           env=env, capture_output=True, timeout=120)
            return
        except FileNotFoundError:
            continue
    raise SystemExit("no Chrome or Chromium found")


def main():
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--cp", required=True)
    ap.add_argument("--out", required=True)
    ap.add_argument("--oracle", default=ORACLE)
    ap.add_argument("--fonts")
    ap.add_argument("--only", choices=["p3", "p4"])
    a = ap.parse_args()
    office = os.path.join(a.oracle, "office")
    os.makedirs(a.out, exist_ok=True)
    rows = list(csv.DictReader(open(os.path.join(office, "office.tsv")), delimiter="\t"))
    runs = []
    if a.only != "p4":
        runs += [("visitor", "pdf", "word-pdf", ()), ("xslt", "pdf", "word-pdf", ("-Dxslt=true",))]
    if a.only != "p3":
        runs += [("html", "html", "powerpoint-pdf", ())]
    bad = 0
    for label, mode, oracle, props in runs:
        mine = [r for r in rows if r["file"].endswith(".docx" if mode == "pdf" else ".pptx")]
        d = os.path.join(a.out, label)
        os.makedirs(d, exist_ok=True)
        ext = ".pdf" if mode == "pdf" else ".xhtml"
        times = java(a.cp, mode, [(os.path.join(office, r["file"]), os.path.join(d, r["name"] + ext)) for r in mine], props)
        print("%s (%s)" % (label, "docx4j's PDF" if mode == "pdf" else "docx4j's SVG-in-HTML, printed by Chrome"))
        for r in mine:
            n, w, h = r["name"], float(r["width"]), float(r["height"])
            x, y = (float(v) for v in r["pdf_at"].split(","))
            pdf = os.path.join(d, n + ".pdf")
            if mode == "html":
                chrome(os.path.join(d, n + ".xhtml"), pdf, a.fonts)
            ref = json.load(open(os.path.join(office, oracle, n + ".json")))
            ours = trace(pdf, 1, (x, y, w, h))
            json.dump(ours, open(os.path.join(d, n + ".json"), "w"))
            s = score_traces(ref, ours)
            f = failures(s, w, h)
            bad += bool(f)
            tm = max([t["d"] for t in s["texts"] if t["d"] is not None] or [0])
            pm = max([q["d"] for q in s["parts"]] or [0])
            ms = times.get(os.path.basename(n + ext))
            print("  %-24s texts %3d parts %3d  text %.2f part %.2f  %5s ms  %s" % (
                n, len(s["texts"]), len(s["parts"]), tm, pm, ms if ms is not None else "?",
                "ok" if not f else "FAIL %d: %s" % (len(f), "; ".join(f[:4]))))
    print("failing", bad)
    sys.exit(1 if bad else 0)


if __name__ == "__main__":
    main()
