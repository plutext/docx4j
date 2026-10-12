#!/usr/bin/env python3
# Copyright 2026, Plutext Pty Ltd.
#
# This file is part of docx4j.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
# except in compliance with the License.  You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
"""
CR-037 phase 3: the size of a chart an xlsx two-cell anchor places, as Excel lays the cells out
(docx4j's ChartAnchorSize sums the cells by ECMA-376's column width rule).  Each probe is Excel's
own column chart (CR-036's P1, the oracle's probes/p1-column-clustered.xlsx: Aptos Narrow 11,
default widths, 14.4pt rows) with one thing changed in its XML; excel-chart-size-check.ps1 then
has Excel report each chart's box and each column's and row's size.

usage: chart_size_probes.py BASE.xlsx OUTDIR
"""
import os
import re
import sys
import zipfile

SHEET, STYLES, DRAWING = "xl/worksheets/sheet1.xml", "xl/styles.xml", "xl/drawings/drawing1.xml"


def cols(xml, extra):
    """extra <col> elements added to the sheet's <cols> (the chart's columns are G to O)"""
    return xml.replace("</cols>", extra + "</cols>")


def rows(xml, extra):
    """extra <row> elements after the data (rows 1 to 5); r must be above 5"""
    return xml.replace("</sheetData>", extra + "</sheetData>")


def font(styles, name, size):
    """the Normal font (the first) as another face and size, not the theme's"""
    return re.sub(r"<font>.*?</font>", '<font><sz val="%s"/><color theme="1"/><name val="%s"/><family val="2"/></font>' % (size, name),
                  styles, count=1, flags=re.S)


def fmt(xml, attrs):
    return re.sub(r"<sheetFormatPr ", "<sheetFormatPr " + attrs + " ", xml, count=1)


PROBES = [
    ("s0-base", "Excel's own, unchanged", {}),
    ("s1-widths", "columns H and I 20.78 characters, L 3", {SHEET: lambda x: cols(x,
        '<col min="8" max="9" width="20.77734375" customWidth="1"/><col min="12" max="12" width="3" customWidth="1"/>')}),
    ("s2-heights", "rows 7 and 8 30pt, row 11 hidden", {SHEET: lambda x: rows(x,
        '<row r="7" ht="30" customHeight="1"/><row r="8" ht="30" customHeight="1"/><row r="11" hidden="1"/>')}),
    ("s3-hidden-column", "column K hidden", {SHEET: lambda x: cols(x, '<col min="11" max="11" width="9.44140625" hidden="1"/>')}),
    ("s4-calibri-14", "the Normal font Calibri 14 (the sheet's default row height left at 14.4)", {STYLES: lambda x: font(x, "Calibri", 14)}),
    ("s5-arial-10", "the Normal font Arial 10", {STYLES: lambda x: font(x, "Arial", 10)}),
    ("s6-default-width", "the sheet's defaultColWidth 15.78", {SHEET: lambda x: fmt(x, 'defaultColWidth="15.77734375"')}),
    ("s7-base-width", "the sheet's baseColWidth 12", {SHEET: lambda x: fmt(x, 'baseColWidth="12"')}),
    ("s8-default-height", "the sheet's defaultRowHeight 20 with customHeight", {SHEET: lambda x: re.sub(
        r'defaultRowHeight="[^"]*"', 'defaultRowHeight="20" customHeight="1"', x, count=1)}),
    ("s9-offsets", "the anchor's offsets: from G+30pt, row 2+10pt to O+40pt, row 19+2pt", {DRAWING: lambda x: re.sub(
        r"<xdr:from>.*?</xdr:to>",
        "<xdr:from><xdr:col>6</xdr:col><xdr:colOff>381000</xdr:colOff><xdr:row>1</xdr:row><xdr:rowOff>127000</xdr:rowOff></xdr:from>"
        "<xdr:to><xdr:col>15</xdr:col><xdr:colOff>508000</xdr:colOff><xdr:row>18</xdr:row><xdr:rowOff>25400</xdr:rowOff></xdr:to>",
        x, count=1, flags=re.S)}),
]


def main():
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    base, out = sys.argv[1], sys.argv[2]
    os.makedirs(out, exist_ok=True)
    with open(os.path.join(out, "probes.tsv"), "w") as tsv:
        tsv.write("name\twhat\n")
        for name, what, edits in PROBES:
            with zipfile.ZipFile(base) as zin, zipfile.ZipFile(os.path.join(out, name + ".xlsx"), "w", zipfile.ZIP_DEFLATED) as zout:
                for item in zin.infolist():
                    data = zin.read(item.filename)
                    if item.filename in edits:
                        changed = edits[item.filename](data.decode("utf-8"))
                        if changed == data.decode("utf-8"):
                            sys.exit("%s: %s unchanged" % (name, item.filename))
                        data = changed.encode("utf-8")
                    zout.writestr(item, data)
            tsv.write("%s\t%s\n" % (name, what))
            print(name, what)


if __name__ == "__main__":
    main()
