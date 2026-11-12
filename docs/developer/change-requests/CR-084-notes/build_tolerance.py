# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-084: how near a colour has to be for Word's a:clrChange (Set Transparent Color) to take it. Jason, 2026-11-12:
"do the Set Transparent Color tolerance round".

Phase 0's probes (all PNG) found clrChange exact: the nearest probed colour, 4 levels off red, was left. The corpus says
otherwise (phase1-findings.md section 4): on a JPEG (13145) Word clears every pixel within 16 levels of white, on a PNG
(4102) most of those 1 level off and none at 2. So the tolerance may depend on the picture's format, on the colour, or
on both. This set measures it: grids of colours near a target, as PNG and as JPEG, through clrChange to transparent.

The pictures (256 by 256 px, 16 by 16 patches of 16 px, so that each patch is one JPEG MCU and decodes flat):
  tol-<base>.<fmt>, the base white (FFFFFF), red (FF0000), grey (808080) or blue (336699). The patches:
    white and red (a channel at 255 can only go down, at 0 only up):
      0-31    R moved by D32 (0 to 80)       32-63   G by D32       64-95   B by D32       96-127  all three by D32
    grey and blue (both ways):
      0-15 R+, 16-31 R-, 32-47 G+, 48-63 G-, 64-79 B+, 80-95 B-, 96-111 all +, 112-127 all -, each by D16 (0 to 64)
    128-255  two channels moved together, a by D16 and b by PAIR (the metric's shape: largest channel, sum, distance)
  Formats: PNG (RGB, as corpus 4102), JPEG at quality 75 with 4:2:0 (as the corpus's five), JPEG at 100 with 4:4:4
  (near-exact levels), and for white also JPEG at 50, PNG with an opaque alpha channel, a palette PNG and a GIF.

The set (cr084-clrchange-tol):
  rk90  white: each format unadjusted and to transparent; PNG and JPEG 75 to blue (opaque), and PNG with useA 0
  rk91  red, rk92 grey, rk93 blue: PNG, JPEG 75 and JPEG 100, unadjusted and to transparent
The first page of each file is its PNG unadjusted. Readers: read the patch centres as read_recolour.py does; for a
JPEG, the source's levels are its decoded patch centres (tolerance-sources.json, written here by PIL's decoder).

usage: build_tolerance.py <staging dir>   (writes tolerance-probes.tsv and tolerance-sources.json beside this script)"""
import hashlib, importlib.util, io, json, os, sys, zipfile
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('recolour', os.path.join(HERE, 'build_recolour.py'))
rc = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(rc)
fx = rc.fx

SET = 'cr084-clrchange-tol'
PATCH, GRID = rc.PATCH, rc.GRID
D32 = list(range(16)) + [16, 18, 20, 22, 24, 26, 28, 30, 32, 36, 40, 44, 48, 56, 64, 80]
D16 = [0, 1, 2, 3, 4, 5, 6, 8, 10, 12, 16, 20, 24, 32, 48, 64]
PAIR = [0, 1, 2, 4, 8, 12, 16, 32]
BASES = {'white': (255, 255, 255), 'red': (255, 0, 0), 'grey': (128, 128, 128), 'blue': (0x33, 0x66, 0x99)}

def clamp(v):
    return max(0, min(255, v))

def levels(base):
    """The 256 patch colours near base (see the module's docstring)."""
    out = []
    edge = all(c in (0, 255) for c in base)
    step = [-1 if c == 255 else 1 for c in base]          # the way a channel at an edge can move
    if edge:
        for ch in (0, 1, 2):
            for d in D32:
                c = list(base)
                c[ch] = clamp(base[ch] + step[ch] * d)
                out.append(tuple(c))
        for d in D32:
            out.append(tuple(clamp(base[ch] + step[ch] * d) for ch in range(3)))
    else:
        for ch in (0, 1, 2):
            for s in (1, -1):
                for d in D16:
                    c = list(base)
                    c[ch] = clamp(base[ch] + s * d)
                    out.append(tuple(c))
        for s in (1, -1):
            for d in D16:
                out.append(tuple(clamp(base[ch] + s * d) for ch in range(3)))
    # two channels together: the first two that can move (white and red: R and G, each its own way; grey and blue: R+, G-)
    sa, sb = (step[0], step[1]) if edge else (1, -1)
    for a in D16:
        for b in PAIR:
            out.append((clamp(base[0] + sa * a), clamp(base[1] + sb * b), base[2]))
    assert len(out) == 256, len(out)
    return out

def image(base, mode='RGB'):
    img = Image.new('RGB', (PATCH * GRID, PATCH * GRID))
    d = ImageDraw.Draw(img)
    for k, c in enumerate(levels(base)):
        x, y = (k % GRID) * PATCH, (k // GRID) * PATCH
        d.rectangle([x, y, x + PATCH - 1, y + PATCH - 1], fill=c)
    return img if mode == 'RGB' else img.convert(mode)

def encode(base, fmt):
    """The picture of BASES[base] in the format fmt, as bytes"""
    img, out = image(BASES[base]), io.BytesIO()
    if fmt == 'png':
        img.save(out, 'PNG', optimize=False)
    elif fmt == 'rgba.png':
        image(BASES[base], 'RGBA').save(out, 'PNG')
    elif fmt == 'pal.png':
        img.quantize(colors=256, method=Image.Quantize.MAXCOVERAGE, dither=Image.Dither.NONE).save(out, 'PNG')
    elif fmt == 'gif':
        img.quantize(colors=256, method=Image.Quantize.MAXCOVERAGE, dither=Image.Dither.NONE).save(out, 'GIF')
    else:                                                  # q75.jpeg, q100.jpeg, q50.jpeg
        q = int(fmt.split('.')[0][1:])
        img.save(out, 'JPEG', quality=q, subsampling=0 if q == 100 else 2)
    return out.getvalue()

# a picture's file name ends in its real extension (tol-white-q75.jpeg, tol-white-pal.png, tol-white.png)
def fname(base, fmt):
    return f'tol-{base}.{fmt}' if fmt in ('png', 'gif') else f'tol-{base}-{fmt}'

def to_transparent(base):
    h = '%02X%02X%02X' % BASES[base]
    return rc.clr_change(rc.srgb(h), rc.srgb(h, 0))

def to_blue(base, use_a=None):
    h = '%02X%02X%02X' % BASES[base]
    return rc.clr_change(rc.srgb(h), rc.srgb('0000FF'), use_a)

FMT_NOTE = {'png': 'PNG', 'q75.jpeg': 'JPEG 75 4:2:0', 'q100.jpeg': 'JPEG 100 4:4:4', 'q50.jpeg': 'JPEG 50 4:2:0',
            'rgba.png': 'PNG with an alpha channel', 'pal.png': 'palette PNG', 'gif': 'GIF'}

def fam(base):
    out = []
    fmts = ['png', 'q75.jpeg', 'q100.jpeg'] + (['q50.jpeg', 'rgba.png', 'pal.png', 'gif'] if base == 'white' else [])
    for fmt in fmts:
        pic = fname(base, fmt)
        out.append(rc.Pic(f'{base}, {FMT_NOTE[fmt]}, unadjusted', pic))
        out.append(rc.Pic(f'{base}, {FMT_NOTE[fmt]}, clrChange {base} to transparent', pic, to_transparent(base)))
        if base == 'white' and fmt in ('png', 'q75.jpeg'):
            out.append(rc.Pic(f'{base}, {FMT_NOTE[fmt]}, clrChange {base} to blue', pic, to_blue(base)))
        if base == 'white' and fmt == 'png':
            out.append(rc.Pic(f'{base}, {FMT_NOTE[fmt]}, clrChange {base} to blue, useA 0', pic, to_blue(base, 0)))
    return out

FILES = [('rk90-white', 'white'), ('rk91-red', 'red'), ('rk92-grey', 'grey'), ('rk93-blue', 'blue')]
PICTURES = {}
for _, b in FILES:
    for f in ('png', 'q75.jpeg', 'q100.jpeg', 'q50.jpeg', 'rgba.png', 'pal.png', 'gif'):
        PICTURES[fname(b, f)] = (b, f)

CT_EXT = {'png': 'image/png', 'jpeg': 'image/jpeg', 'gif': 'image/gif'}

def package(path, probes):
    """As build_recolour.package, with each picture's bytes from here and a content type for each extension."""
    pics = sorted({p.picture for _, p in probes})
    rids = {n: f'rId{10 + i}' for i, n in enumerate(pics)}
    rels = [f'<Relationship Id="rId1" Type="{fx.REL}settings" Target="settings.xml"/>',
            f'<Relationship Id="rId2" Type="{fx.REL}theme" Target="theme/theme1.xml"/>']
    rels += [f'<Relationship Id="{rids[n]}" Type="{fx.REL}image" Target="media/{n}"/>' for n in pics]
    ct = fx.CT
    for ext in sorted({n.rsplit('.', 1)[1] for n in pics} - {'png'}):
        ct = ct.replace('<Override ', f'<Default Extension="{ext}" ContentType="{CT_EXT[ext]}"/><Override ', 1)
    with zipfile.ZipFile(path, 'w') as z:
        fx.entry(z, '[Content_Types].xml', fx.DECL + ct)
        fx.entry(z, '_rels/.rels', fx.DECL + fx.RELS)
        fx.entry(z, 'word/document.xml', rc.document(probes, rids))
        fx.entry(z, 'word/_rels/document.xml.rels', fx.DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/'
                 'package/2006/relationships">' + ''.join(rels) + '</Relationships>')
        fx.entry(z, 'word/settings.xml', fx.SETTINGS)
        fx.entry(z, 'word/theme/theme1.xml', open(os.path.join(fx.THEMES, fx.THEME_2023), encoding='utf-8').read())
        for n in pics:
            fx.entry(z, f'word/media/{n}', encode(*PICTURES[n]))      # its fixed date, so a rebuild is byte for byte

def decoded_centres(data):
    img = Image.open(io.BytesIO(data)).convert('RGB')
    return [list(img.getpixel(((k % GRID) * PATCH + PATCH // 2, (k // GRID) * PATCH + PATCH // 2))) for k in range(256)]

def main(staging):
    manifest = ['set\tfile\tpage\tprobe\tpicture\tnote\tsummary\n']
    sources = {}
    sums, order, count = [], [], 0
    for fname_, base in FILES:
        prefix = fname_.split('-')[0]
        probes = [(f'{prefix}-{i + 1:02d}', p) for i, p in enumerate(fam(base))]
        for i, (pid, p) in enumerate(probes):
            manifest.append(f'{SET}\t{fname_}\t{i + 1}\t{pid}\t{p.picture}\t{p.note}\t{p.summary()}\n')
            if p.picture not in sources:
                sources[p.picture] = {'intended': [list(c) for c in levels(BASES[base])],
                                      'decoded': decoded_centres(encode(*PICTURES[p.picture]))}
        count += len(probes)
        d = os.path.join(staging, SET, fname_)
        os.makedirs(d, exist_ok=True)
        f = os.path.join(d, fname_ + '.docx')
        package(f, probes)
        j = os.path.join(d, 'job.json')
        with open(j, 'w', newline='') as out:
            json.dump({'verb': 'save', 'input': fname_ + '.docx', 'timeoutSeconds': 300, 'formats': ['docx', 'pdf']}, out)
        for x in (j, f):
            sums.append(f'{hashlib.sha256(open(x, "rb").read()).hexdigest()}  {SET}/{fname_}/{os.path.basename(x)}\n')
        order.append(fname_ + '\n')
    open(os.path.join(staging, SET + '.sha256'), 'w', newline='').writelines(sums)
    open(os.path.join(staging, SET + '.order.txt'), 'w', newline='').writelines(order)
    open(os.path.join(HERE, 'tolerance-probes.tsv'), 'w', newline='').writelines(manifest)
    with open(os.path.join(HERE, 'tolerance-sources.json'), 'w', encoding='utf-8') as out:
        json.dump(sources, out, separators=(',', ':'))
    print(SET, len(order), 'files,', count, 'probes')

if __name__ == '__main__':
    main(sys.argv[1])
