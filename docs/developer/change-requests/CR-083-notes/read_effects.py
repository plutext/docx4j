# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 3: what Word's PDF does with a WordArt's shadow, glow and reflection, probe page by probe page.

    python3 read_effects.py <results dir> <job> [pages] [--text TEXT] [--detail]

<results dir>/<job>/<job>.pdf is Word's PDF of a probe set (one probe a page, the box drawn with a 0.25 pt grey line).
All places are PDF points, y up. For each page:
- the box: the grey line's bounds, and its height;
- the text: the first character's origin of the line reading TEXT (default "WordArt"; y down, as mutool's stext gives
  it), where Word keeps it as real text, and that line's stext box;
- the paths: the union of the filled paths within 40 pt of the box (Word's glyph paths where it draws the glyphs: a
  warp, an outline, a gradient), and their count;
- each picture with a soft mask, in drawing order, numbered: its pixel size, its place, pixels per point, filter, its
  alpha's maximum, and its alpha's bounds at 50% and at 5% of that maximum. A picture whose alpha is 0 everywhere is
  kept and flagged "empty" (Word emits them, for example a Follow Path glyph whose reflection has faded out), and
  "artefact" where it also covers most of the box (the transparent picture under a warped WordArt, phase 0 findings
  section 1), so that a count and order can be compared.

--detail adds, for each picture that isn't empty, measures against the text's ink, which is taken from the page
rendered at 4 pixels a point (`mutool draw`), its pixels darker than 90 of 255 within 40 pt of the box. So the text
must be dark and the effect light (a black shadow at an opacity above about 0.6 reads as ink): the round's probes keep
to that.
- offset: where the ink, moved, best matches the picture's alpha (the cross-correlation's peak, within 16 pt);
- contours: the distance outside the ink at which the picture's alpha, averaged over a band 0.5 pt wide, falls below
  50% and 5% of its maximum (for a glow, centred on the ink).

Built for the phase 3 design (phase3-findings.md section 1) and extended by its review (section 3). Reads pages with
CR-081's pdfdraws.py (a content-stream model; LESSON: a regex survey misses vector fills)."""
import os, re, subprocess, sys, tempfile

import numpy as np

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'CR-081-notes'))
import pdfdraws as P  # noqa: E402

PPT = 4          # pixels a point for --detail
DARK = 90        # an ink pixel is darker than this
NEAR = 40.0      # points around the box that belong to its probe

def stext(pdf, page):
    return subprocess.run(['mutool', 'draw', '-q', '-F', 'stext', '-o', '-', pdf, str(page)],
                          capture_output=True).stdout.decode('utf-8', 'replace')

def text_line(pdf, page, text):
    """(origin x, origin y down, the line's stext box y up) of the line reading text, or None."""
    out = stext(pdf, page)
    hm = re.search(r'<page [^>]*height="([\d.]+)"', out)
    i = out.find(f'text="{text}')
    if i < 0 or not hm:
        return None
    line = re.search(r'<line bbox="([-\d. ]+)"', out[out.rfind('<line ', 0, i + 1):])
    m = re.search(r'<char [^>]*>', out[i:])
    if not m or not line:
        return None
    x, y = re.search(r' x="([-\d.]+)"', m.group(0)), re.search(r' y="([-\d.]+)"', m.group(0))
    h = float(hm.group(1))
    b = [float(v) for v in line.group(1).split()]
    return float(x.group(1)), float(y.group(1)), (b[0], h - b[3], b[2], h - b[1])

def near(b, box):
    return box and b[0] > box[0] - NEAR and b[2] < box[2] + NEAR and b[1] > box[1] - NEAR and b[3] < box[3] + NEAR

def bounds(a, place, level):
    """The picture's alpha at or above level, as a box in page points (y up)."""
    rows, cols = np.where((a >= level).any(axis=1))[0], np.where((a >= level).any(axis=0))[0]
    if not len(rows):
        return None
    x0, y0, x1, y1 = place
    sx, sy = (x1 - x0) / a.shape[1], (y1 - y0) / a.shape[0]
    return (x0 + cols[0] * sx, y1 - (rows[-1] + 1) * sy, x0 + (cols[-1] + 1) * sx, y1 - rows[0] * sy)

def fmt(b):
    return '%.2f %.2f %.2f %.2f' % b if b else '-'

class Grid:
    """A region of the page at PPT pixels a point: the text's ink, and pictures placed on it."""
    def __init__(self, pdf, page, box, height):
        self.x0, self.y1 = box[0] - NEAR, box[3] + NEAR
        self.w, self.h = int((box[2] - box[0] + 2 * NEAR) * PPT), int((box[3] - box[1] + 2 * NEAR) * PPT)
        from PIL import Image
        with tempfile.TemporaryDirectory() as d:
            png = os.path.join(d, 'page.png')
            subprocess.run(['mutool', 'draw', '-q', '-r', str(PPT * 72), '-o', png, pdf, str(page)], check=True)
            im = np.asarray(Image.open(png).convert('L'))
        top, left = int(round((height - self.y1) * PPT)), int(round(self.x0 * PPT))
        ink = np.zeros((self.h, self.w), bool)          # the page's edge may cut the region: what is off it is no ink
        cut = im[max(0, top):top + self.h, max(0, left):left + self.w] < DARK
        ink[max(0, -top):max(0, -top) + cut.shape[0], max(0, -left):max(0, -left) + cut.shape[1]] = cut
        self.ink = ink

    def place(self, a, place):
        """The picture's alpha on this grid (nearest pixel)."""
        x0, y0, x1, y1 = place
        px = self.x0 + (np.arange(self.w) + 0.5) / PPT
        py = self.y1 - (np.arange(self.h) + 0.5) / PPT
        rx = np.floor((px - x0) / (x1 - x0) * a.shape[1]).astype(int)
        ry = np.floor((y1 - py) / (y1 - y0) * a.shape[0]).astype(int)
        mx, my = (rx >= 0) & (rx < a.shape[1]), (ry >= 0) & (ry < a.shape[0])
        out = np.zeros((self.h, self.w))
        out[np.ix_(my, mx)] = a[np.ix_(ry[my], rx[mx])]
        return out

    def offset(self, alpha, reach=16):
        """(dx right, dy down) in points at which the ink best matches alpha: the cross-correlation's peak."""
        f = np.fft.irfft2(np.fft.rfft2(alpha) * np.conj(np.fft.rfft2(self.ink.astype(float))), s=alpha.shape)
        r = reach * PPT
        best, at = -1, (0, 0)
        for dy in range(-r, r + 1):
            row = f[dy % self.h]
            for dx in range(-r, r + 1):
                if row[dx % self.w] > best:
                    best, at = row[dx % self.w], (dx, dy)
        return at[0] / PPT, at[1] / PPT

    def contours(self, alpha, top):
        """The distance outside the ink at which the band's mean alpha falls below 50% and 5% of top."""
        dist = np.full(self.ink.shape, np.inf)
        cur = self.ink.copy()
        dist[cur] = 0
        for i in range(1, int(30 * PPT)):           # dilation, 4- and 8-neighbourhoods in turn: an octagonal distance
            n = cur.copy()
            n[1:, :] |= cur[:-1, :]; n[:-1, :] |= cur[1:, :]; n[:, 1:] |= cur[:, :-1]; n[:, :-1] |= cur[:, 1:]
            if i % 2 == 0:
                n[1:, 1:] |= cur[:-1, :-1]; n[:-1, :-1] |= cur[1:, 1:]; n[1:, :-1] |= cur[:-1, 1:]; n[:-1, 1:] |= cur[1:, :-1]
            dist[n & ~cur] = i / PPT
            cur = n
        half = five = None
        for r in np.arange(0.25, 30, 0.5):
            band = (dist >= r - 0.25) & (dist < r + 0.25)
            v = alpha[band].mean() / top if band.any() else 0
            if half is None and v < 0.5:
                half = r
            if v < 0.05:
                five = r
                break
        return half, five

def main(results, job, *args):
    pages, text, detail = None, 'WordArt', False
    rest = list(args)
    while rest:
        a = rest.pop(0)
        if a == '--text':
            text = rest.pop(0)
        elif a == '--detail':
            detail = True
        else:
            pages = a
    pdf = os.path.join(results, job, job + '.pdf')
    objs = P.pages(pdf)
    height = float(re.search(r'/MediaBox \[\s*[-\d.]+ [-\d.]+ [-\d.]+ ([-\d.]+)', P.show(pdf, objs[0]).decode('latin-1')).group(1))
    for n in (range(1, len(objs) + 1) if not pages else [int(p) for p in pages.split(',')]):
        draws = P.page_draws(pdf, objs[n - 1])
        strokes = [d for d in draws if d.get('kind') == 'stroke']
        box = strokes[0]['box'] if strokes else None
        fills = [d['box'] for d in draws if d.get('kind') == 'fill' and near(d['box'], box)]
        paths = (min(b[0] for b in fills), min(b[1] for b in fills), max(b[2] for b in fills), max(b[3] for b in fills)) if fills else None
        t = text_line(pdf, n, text)
        print(f'--- page {n}')
        print(f'  box {fmt(box)}' + (f' height {box[3] - box[1]:.2f}' if box else ''))
        print(f'  text ' + (f'origin {t[0]:.2f} {t[1]:.2f} (y down), line {fmt(t[2])}' if t else 'none'))
        print(f'  paths {len(fills)}, union {fmt(paths)}')
        grid = Grid(pdf, n, box, height) if detail and box else None
        k = 0
        for d in draws:
            if d.get('kind') != 'image' or not d.get('smask'):
                continue
            k += 1
            a, _ = P.alpha_of(pdf, d['obj'])
            b = d['box']
            top = float(a.max()) if a is not None else 0.0
            flags = ''
            if top == 0:
                area = (b[2] - b[0]) * (b[3] - b[1])
                flags = ' empty' + (' artefact' if box and area > 0.8 * (box[2] - box[0]) * (box[3] - box[1]) else '')
            print(f'  picture {k}: {d["w"]}x{d["h"]} at {fmt(b)}, {d["w"] / (b[2] - b[0]):.3f} px/pt, {d.get("filter")}, '
                  f'alpha max {top:.3f}{flags}')
            if top > 0:
                print(f'    alpha 50% {fmt(bounds(a, b, 0.5 * top))}; 5% {fmt(bounds(a, b, 0.05 * top))}')
                if grid is not None:
                    al = grid.place(a, b)
                    dx, dy = grid.offset(al)
                    half, five = grid.contours(al, top)
                    print(f'    offset {dx:.2f} right {dy:.2f} down; contours outside the ink: 50% {half} pt, 5% {five} pt')

if __name__ == '__main__':
    main(*sys.argv[1:])
