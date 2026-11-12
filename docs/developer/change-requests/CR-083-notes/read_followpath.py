# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 2, the Follow Path round (build_followpath.py): each probe's parameters and the glyphs Word (or docx4j)
drew, as JSON, for the fitting.

Word's Follow Path text is invisible text a glyph, at the glyph's place, size and turn (phase 1-2 findings section 3),
so each glyph's origin, size and turn come from mutool's stext (origin, size, quad). Each probe's parameters come from
the job's own docx (its wps text boxes in order, a page each), so that both stagings read alike (cr083-followpath's
wrap="none" files and cr083-followpath2's). Every character below the probe's label is the WordArt's (Word draws the
glyphs as paths, its only text there the invisible text).

usage: read_followpath.py <results dir> <out.json> <job> ...
       a job's PDF and docx are <dir>/<job>/<job>.pdf and .docx (Word's results), or <dir>/<job>.pdf beside a docx dir
       given by FP_DOCX (docx4j's renders); the kerned positions and ink (KernInk.java) of each line's text from the
       fonts dir FP_FONTS (Word's files: Aptos, Aptos Narrow, Aptos Display, Georgia Pro)"""
import functools, json, math, os, re, subprocess, sys, zipfile
import xml.etree.ElementTree as ET

import warp_model as wm

HERE = os.path.dirname(os.path.abspath(__file__))
EMU = 12700
W = '{http://schemas.openxmlformats.org/wordprocessingml/2006/main}'
A = '{http://schemas.openxmlformats.org/drawingml/2006/main}'
WP = '{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}'
WPS = '{http://schemas.microsoft.com/office/word/2010/wordprocessingShape}'
wm.pd.pages = functools.lru_cache(maxsize=64)(wm.pd.pages)

# the face's regular (or bold) file, by its full name (fc-scan)
FACES = {('Aptos', False): 'Aptos', ('Aptos', True): 'Aptos Bold', ('Aptos Narrow', False): 'Aptos Narrow',
         ('Aptos Display', False): 'Aptos Display', ('Georgia Pro', False): 'Georgia Pro'}


@functools.lru_cache(maxsize=None)
def font_files():
    d = os.environ['FP_FONTS']
    out = {}
    for f in sorted(os.listdir(d)):
        p = os.path.join(d, f)
        name = subprocess.run(['fc-scan', '--format', '%{fullname[0]}', p], capture_output=True, text=True).stdout
        out.setdefault(name, p)
    return out


@functools.lru_cache(maxsize=None)
def kern_ink(face, bold, text):
    """(positions in ems, (ink top, ink bottom, ink left, ink right) in ems), kerned as docx4j lays WordArt out"""
    f = font_files()[FACES[(face, bold)]]
    line = subprocess.run(['java', os.path.join(HERE, 'KernInk.java'), f, text], capture_output=True, text=True,
                          check=True).stdout.rstrip('\n')
    _, pos, ink = line.split('\t')
    return [float(v) for v in pos.split()], [float(v) for v in ink.split()]


def boxes(docx):
    """Each WordArt's parameters, in the document's order (a page each)"""
    root = ET.fromstring(zipfile.ZipFile(docx).read('word/document.xml'))
    out = []
    for anchor in root.iter(WP + 'anchor'):
        ext = anchor.find(WP + 'extent')
        wsp = anchor.find('.//' + WPS + 'wsp')
        body = wsp.find(WPS + 'bodyPr')
        warp = body.find(A + 'prstTxWarp')
        adj = {g.get('name'): int(g.get('fmla').split()[1]) for g in warp.iter(A + 'gd')}
        fit = [t for t in ('spAutoFit', 'noAutofit', 'normAutofit') if body.find(A + t) is not None]
        ins = [int(body.get(k, d)) / EMU for k, d in (('lIns', 91440), ('tIns', 45720), ('rIns', 91440), ('bIns', 45720))]
        lines, jc = [], None
        for p in wsp.find(WPS + 'txbx').iter(W + 'p'):
            j = p.find(W + 'pPr/' + W + 'jc')
            jc = j.get(W + 'val') if j is not None else 'left'
            runs = p.findall(W + 'r')
            if not runs:
                lines.append(None)
                continue
            rpr = runs[0].find(W + 'rPr')
            face = rpr.find(W + 'rFonts').get(W + 'ascii')
            size = int(rpr.find(W + 'sz').get(W + 'val')) / 2
            bold = rpr.find(W + 'b') is not None
            sp = rpr.find(W + 'spacing')
            spacing = int(sp.get(W + 'val')) / 20 if sp is not None else 0.0
            text = ''.join(t.text or '' for r in runs for t in r.iter(W + 't'))
            pos, ink = kern_ink(face, bold, text.strip())
            lines.append(dict(text=text, size=size, face=face, bold=bold, spacing=spacing, pos=pos, ink=ink, jc=jc))
        out.append(dict(warp=warp.get('prst'), adj=adj.get('adj'), box=[int(ext.get('cx')) / EMU, int(ext.get('cy')) / EMU],
                        ins=ins, autofit=fit[0] if fit else None, wrap=body.get('wrap'), anchor=body.get('anchor'), lines=lines))
    return out


def glyphs(pdf, page):
    w = wm.word_page(pdf, page)
    out = []
    for c in w['text']:
        if not c['c'].strip():
            continue
        q = c['quad']
        out.append(dict(c=c['c'], size=c['size'], x=c['x'], y=c['y'], alpha=c['alpha'],
                        turn=math.atan2(q[3] - q[1], q[2] - q[0])))
    return w['box'], out


def main(results, out, jobs):
    probes = []
    for name in jobs:
        pdf = os.path.join(results, name, name + '.pdf')
        docx = os.path.join(results, name, name + '.docx')
        if not os.path.exists(pdf):
            pdf = os.path.join(results, name + '.pdf')
            docx = os.path.join(os.environ.get('FP_DOCX', results), name + '.docx')
        if not (os.path.exists(pdf) and os.path.exists(docx)):
            print('missing', name)
            continue
        prefix = name.split('-')[0]
        for i, par in enumerate(boxes(docx)):
            box, g = glyphs(pdf, i + 1)
            probes.append(dict(probe=f'{prefix}-{i + 1:02d}', file=name, page=i + 1, word_box=box, glyphs=g, **par))
        print(name, len(probes))
    with open(out, 'w', encoding='utf-8') as f:
        json.dump(probes, f, indent=0)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2], sys.argv[3:])
