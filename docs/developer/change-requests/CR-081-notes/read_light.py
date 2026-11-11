# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-081 phase 5b-1: Word's 3-D pictures read for their shading (cr081-phase5b-r1, fxg1). A picture's colours, Flate or
JPEG (Word writes either), with their alpha; and, for the front-on probes of the grey rectangle with its 36 by 18 pt
circle bevel, each pixel's normal from 5a's geometry: the distance in from the outline, the circle's measured profile.

usage: read_light.py <results dir>   (prints the colour along each side's bevel and on the face, for each probe)"""
import csv, os, sys
import numpy as np
import pdfdraws as pd, read_3d as r3

HERE = os.path.dirname(os.path.abspath(__file__))
W, H, BW, BH = 144.0, 108.0, 36.0, 18.0

def colours(pdf, page):
    """The 3-D picture's rgb (0..1, rows top first), alpha, and its box (x0, top, x1, bottom), points from the frame"""
    d = r3.the_picture(pdf, page)
    info = pd.image_info(pdf, d['obj'])
    rgb, a = pd.pixels(pdf, d['obj'])
    if rgb is None and info['filter'] == 'DCTDecode':
        # mutool decodes the JPEG: its samples, un-premultiplied where the soft mask has /Matte, as pixels() does a Flate
        raw = np.frombuffer(pd.show(pdf, d['obj'], raw=True), np.uint8)[:info['w'] * info['h'] * 3]
        rgb = raw.reshape(info['h'], info['w'], 3) / 255.0
        _, matte = pd.alpha_of(pdf, d['obj'])
        if matte and a is not None:
            with np.errstate(divide='ignore', invalid='ignore'):
                rgb = np.where(a[..., None] > 0, np.clip(rgb / a[..., None], 0, 1), 0)
    x0, y0, x1, y1 = d['box']
    return rgb, a, (x0 - r3.FIXED[0], r3.PAGE_H - y1 - r3.FIXED[1], x1 - r3.FIXED[0], r3.PAGE_H - y0 - r3.FIXED[1]), info['filter']

def slope(d):
    """The circle bevel's dz/dd at d in from the outline: a quarter ellipse, BW by BH (5a's measured circle reads as one
    within its pixels, 0.02 of its height)"""
    t = np.clip(d / BW, 1e-6, 1.0)
    return np.where(d < BW, BH / BW * (1 - t) / np.sqrt(np.maximum(1e-12, 1 - (1 - t) ** 2)), 0.0)

def normals(xs, ys, w=W, h=H):
    """The front-on rectangle's normals at frame points (x right, y down, z towards the viewer), and d"""
    X, Y = np.meshgrid(xs, ys)
    dl, dr, dt, db = X, w - X, Y, h - Y
    d = np.minimum(np.minimum(dl, dr), np.minimum(dt, db))
    ux = np.where(d == dl, 1.0, np.where(d == dr, -1.0, 0.0))
    uy = np.where((d == dt) & (ux == 0), 1.0, np.where((d == db) & (ux == 0), -1.0, 0.0))
    s = slope(d)
    n = np.stack([-s * ux, -s * uy, np.ones_like(s)], axis=-1)
    return n / np.linalg.norm(n, axis=-1, keepdims=True), d

def ellipse_normals(xs, ys, w=W, h=H):
    """The front-on ellipse's normals (its bevel by the distance in from the outline), d, and whether inside"""
    X, Y = np.meshgrid(xs, ys)
    P = np.stack([X.ravel() - w / 2, Y.ravel() - h / 2], axis=1)
    phi = np.linspace(0, 2 * np.pi, 4000, endpoint=False)
    C = np.stack([w / 2 * np.cos(phi), h / 2 * np.sin(phi)], axis=1)
    near = np.empty((len(P), 2))
    for k in range(0, len(P), 1500):
        d2 = ((P[k:k + 1500, None, :] - C[None]) ** 2).sum(-1)
        near[k:k + 1500] = C[d2.argmin(1)]
    # one Newton step onto the curve's nearest point, from the sampled one
    inside = (P[:, 0] / (w / 2)) ** 2 + (P[:, 1] / (h / 2)) ** 2 < 1
    v = near - P
    d = np.linalg.norm(v, axis=1)
    u = -v / np.maximum(d, 1e-9)[:, None]                 # inwards
    sl = slope(d)
    n = np.stack([-sl * u[:, 0], -sl * u[:, 1], np.ones_like(sl)], axis=-1)
    n /= np.linalg.norm(n, axis=-1, keepdims=True)
    return n.reshape(X.shape + (3,)), d.reshape(X.shape), inside.reshape(X.shape)

def samples(pdf, page, step=0.36):
    """(normal, rgb) at the probe's pixels inside the frame, away from its edge"""
    rgb, a, box, filt = colours(pdf, page)
    h, w = a.shape
    xs = np.arange(1.0, W - 1.0, step)
    ys = np.arange(1.0, H - 1.0, step)
    n, d = normals(xs, ys)
    X, Y = np.meshgrid(xs, ys)
    ix = np.floor((X - box[0]) / (box[2] - box[0]) * w).astype(int)
    iy = np.floor((Y - box[1]) / (box[3] - box[1]) * h).astype(int)
    c = rgb[iy, ix]
    al = a[iy, ix]
    ok = al > 0.99
    return n[ok], c[ok], d[ok], filt

if __name__ == '__main__':
    pdf = os.path.join(sys.argv[1], 'fxg1-light-a', 'fxg1-light.pdf')
    for p in csv.DictReader(open(os.path.join(HERE, 'probes.tsv')), delimiter='\t'):
        if not p['probe'].startswith('fxg1') or any(k in p['note'] for k in ('iso', 'perspective', 'photo')):
            continue
        rgb, a, box, filt = colours(pdf, int(p['page']))
        h, w = a.shape
        def at(x, y):
            return np.round(rgb[int((y - box[1]) / (box[3] - box[1]) * h), int((x - box[0]) / (box[2] - box[0]) * w)] * 255).astype(int)
        ts = [0.05, 0.2, 0.4, 0.6, 0.8]
        sides = {'t': lambda t: (72, t * 36), 'b': lambda t: (72, 108 - t * 36), 'l': lambda t: (t * 36, 54), 'r': lambda t: (144 - t * 36, 54)}
        row = '  '.join(f'{k}:' + ','.join(f'{at(*f(t))[0]:3d}' for t in ts) for k, f in sides.items())
        print(f"{p['probe']} {p['note'][:34]:34s} {filt[:4]} face {tuple(int(v) for v in at(72, 54))}  {row}")
