# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 4: each glyph's ink box on a page of Word's PDF of wv40-shapetypes (Word writes a path a glyph), its
centre, and its place on the box's ellipse (centre (288, 324), half-axes 144 and 72: the box at (144, 252), 288 by
144 pt): the parametric angle (y up) and its distance as a fraction of the ellipse's. For the Follow Path types.

    python3 -I glyphs.py <pdf> <page>
"""
import sys, re, subprocess, math
pdf, page = sys.argv[1], sys.argv[2]
out = subprocess.run(['mutool','trace',pdf,page],capture_output=True,text=True).stdout
glyphs=[]; cur=None; t=None
for line in out.splitlines():
    if '<fill_path' in line:
        cur=[]; t=[float(v) for v in re.search(r'transform="([^"]+)"',line).group(1).split()]; continue
    if cur is not None and '</fill_path>' in line:
        glyphs.append(cur); cur=None; continue
    if cur is not None:
        for x,y in re.findall(r'x\d?="([-\d.]+)" y\d?="([-\d.]+)"',line):
            x=float(x);y=float(y); cur.append((t[0]*x+t[2]*y+t[4], t[1]*x+t[3]*y+t[5]))
cx, cy, a, b = 288, 324, 144, 72
for i,g in enumerate(glyphs):
    xs=[p[0] for p in g]; ys=[p[1] for p in g]
    mx=(min(xs)+max(xs))/2; my=(min(ys)+max(ys))/2
    # parametric angle of the centre seen from the ellipse centre (y up)
    ang=math.degrees(math.atan2(-(my-cy)/b, (mx-cx)/a))
    r=math.hypot((mx-cx)/a,(my-cy)/b)
    print('%d  x %.1f..%.1f y %.1f..%.1f  centre (%.1f,%.1f) angle %.1f  r/ellipse %.3f' % (i, min(xs),max(xs),min(ys),max(ys), mx,my, ang, r))
