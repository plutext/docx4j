# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-078 section 1's rule, and the rules it beat, against results.tsv: python3 check_rule.py"""
import csv, os, re

ATTRS = [(0x0001, 'allStyles'), (0x0002, 'customStyles'), (0x0004, 'latentStyles'), (0x0008, 'stylesInUse'),
         (0x0020, 'headingStyles'), (0x0040, 'numberingStyles'), (0x0080, 'tableStyles'),
         (0x0100, 'directFormattingOnRuns'), (0x0200, 'directFormattingOnParagraphs'),
         (0x0400, 'directFormattingOnNumbering'), (0x0800, 'directFormattingOnTables'),
         (0x1000, 'clearFormatting'), (0x2000, 'top3HeadingStyles'), (0x4000, 'visibleStyles'), (0x8000, 'alternateStyleNames')]
ALL = sum(b for b, _ in ATTRS)  # FFEF: 0x0010 has no attribute, and Word drops it


def attributes_absent_on(a):
    return sum(b for b, n in ATTRS if a.get(n, '1') == '1')


def rule(a):
    """w:val counts only with no attribute beside it; else the attributes, one left out reading as on."""
    return int(a['val'], 16) & ALL if list(a) == ['val'] else attributes_absent_on(a)


def val_wins(a):
    return int(a['val'], 16) & ALL if 'val' in a else attributes_absent_on(a)


def attributes_over_val_by_bit(a):
    v = int(a.get('val', 'FFFF'), 16)
    for b, n in ATTRS:
        if n in a:
            v = v | b if a[n] == '1' else v & ~b
    return v & ALL


def attribute_left_out_is_off(a):
    return int(a['val'], 16) & ALL if list(a) == ['val'] else sum(b for b, n in ATTRS if a.get(n) == '1')


with open(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'results.tsv'), newline='') as f:
    rows = [r for r in csv.DictReader(f, delimiter='\t', quoting=csv.QUOTE_NONE) if r['given'] != 'none']
for f in (rule, val_wins, attributes_over_val_by_bit, attribute_left_out_is_off):
    fits = sum(f(dict(re.findall(r'(\w+)="([^"]*)"', r['given']))) == int(r['word_val'], 16) for r in rows)
    print(f'{f.__name__}: {fits} of {len(rows)} files')
