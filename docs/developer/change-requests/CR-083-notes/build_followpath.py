# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 2: a round of probes for Follow Path (textArchUp, textArchDown, textCircle), so that its scale and
spacing are fitted to Word over the text's length, the text's height, the box's shape, the arc's length, the font size,
the lines and Word's own form, where phase 0 gave twelve points (phase1-2-findings.md section 4). Jason, 2026-11-13:
"commit then do the Follow Path probes".

The probes are build_wordart.py's (one to a page, the WordArt a wps text box anchored at 144 pt, 252 pt on its page,
wrapNone, a 0.25 pt grey line so that Word's box shows, Aptos 36 pt black centred). Unlike phase 0's these boxes are
wrap="square", which keeps the stored width (wrap="none" draws the box as wide as its text: phase 0, findings section
2), with texts that fit it, and have no insets unless the probe says so, so that the text rectangle is the box (less
half its line). Word's own form (wp60) is wrap="none" with spAutoFit, as Word writes it. Each family is run on the three
presets, default adjustments unless the family is the adjustments'.

Round cr083-followpath2 (prefix wp; the first staging, cr083-followpath, prefix fp, had wrap="none" boxes, and Word
ran its first four files, fp10 to fp20, before it was held: read_followpath.py reads those too, from their own docx):
  wp10  the text's length: "Wo" to "WordArt" three times, 432 by 144 pt
  wp11  the text's length at two other ink heights: "wave" (x-height alone) and "Warpy" (ascenders and descenders),
        one to four words, 432 by 144 pt
  wp12  the text's ink at much the same width: "wave", "WAVE", "wavy", "Wave", hyphens, underscores, degree signs
  wp20  the box: widths 144, 216, 288 and 432 pt by heights 36, 72, 144, 216 and 288 pt, "WordArt"
  wp30  the arc: each preset's adjustment swept (arc lengths 80 to 697 pt in the 288 by 144 pt box), "WordArt" and
        "WordArt WordArt"
  wp40  the font size: 9 to 96 pt, "WordArt", 432 by 144 pt
  wp50  the lines: one to four, lines of unequal length and of unequal size, an empty line
  wp60  Word's own form (spAutoFit, wrap none, stored 144 by 144 pt, Word's insets): texts of several lengths, 18 and
        72 pt, two lines
  wp70  the rest: alignment (left, right), letter spacing, bold, Aptos Narrow, Aptos Display and Georgia Pro, Word's
        insets, the box's vertical anchor, wrap="none" (the box as wide as the text)
  wp80  textButton (its three paths: does each line follow its own as Follow Path's do), one to three lines

usage: build_followpath.py <staging dir>   (writes probes-followpath.tsv beside this script)"""
import hashlib, json, os, sys

import build_wordart as bw

HERE = os.path.dirname(os.path.abspath(__file__))
EMU = bw.EMU
PRESETS = ('textArchUp', 'textArchDown', 'textCircle')
NO_INS = (0, 0, 0, 0)
BOX = (288 * EMU, 144 * EMU)
WIDE = (432 * EMU, 144 * EMU)
SET = 'cr083-followpath2'
TIMEOUT = 300


def art(note, paras, warp, **kw):
    kw.setdefault('ins', NO_INS)
    kw.setdefault('wrap', 'square')
    return bw.Art(note, paras, warp=warp, **kw)


def each(f):
    """A family run on each of the three presets: f(warp) gives (note, paras, options) triples"""
    def g():
        return [art(f'{w}, {note}', paras, w, **kw) for w in PRESETS for note, paras, kw in f(w)]
    g.__name__ = f.__name__
    return g


@each
def fam_length(w):
    texts = ['Wo', 'Word', 'WordArt', 'WordArt Wo', 'WordArt Word', 'WordArt WordArt', 'WordArt WordArt Wo',
             'WordArt WordArt Word', 'WordArt WordArt WordArt']
    return [(f'"{t}", 432 by 144 pt', bw.lines([t]), dict(size=WIDE)) for t in texts]


@each
def fam_ink_length(w):
    out = []
    for word in ('wave', 'Warpy'):
        for n in range(1, 5):
            t = ' '.join([word] * n)
            out.append((f'"{t}", 432 by 144 pt', bw.lines([t]), dict(size=WIDE)))
    return out


@each
def fam_ink(w):
    texts = ['wave', 'WAVE', 'wavy', 'Wave', '-----', '_____', '°°°°°']
    return [(f'"{t}"', bw.lines([t]), {}) for t in texts]


@each
def fam_box(w):
    out = []
    for cy in (36, 72, 144, 216, 288):
        for cx in (144, 216, 288, 432):
            out.append((f'"WordArt", a box {cx} by {cy} pt', bw.lines(['WordArt']), dict(size=(cx * EMU, cy * EMU))))
    return out


# each preset's adjustment, in degrees, the default among them (arc lengths in the 288 by 144 pt box: 80 to 697 pt)
SWEEP = {'textArchUp': (90, 120, 150, 180, 210, 240, 0),
         'textArchDown': (270, 300, 330, 0, 30, 60, 180),
         'textCircle': (0, 30, 60, 90, 120, 150, 180)}
DEFAULT = {'textArchUp': 180, 'textArchDown': 0, 'textCircle': 180}


@each
def fam_adj(w):
    out = []
    for t in ('WordArt', 'WordArt WordArt'):
        for deg in SWEEP[w]:
            d = ' (the default)' if deg == DEFAULT[w] else ''
            out.append((f'"{t}", adj {deg} degrees{d}', bw.lines([t]), dict(adj=[('adj', deg * 60000)])))
    return out


@each
def fam_size(w):
    return [(f'"WordArt" at {s} pt, 432 by 144 pt', bw.lines(['WordArt'], size=s), dict(size=WIDE))
            for s in (9, 12, 18, 24, 36, 48, 72, 96)]


@each
def fam_lines(w):
    R = bw.Run
    return [
        ('two lines "WordArt"', bw.lines(['WordArt'] * 2), {}),
        ('three lines "WordArt"', bw.lines(['WordArt'] * 3), {}),
        ('four lines "WordArt"', bw.lines(['WordArt'] * 4), {}),
        ('two lines "WordArt" then "Wo"', bw.lines(['WordArt', 'Wo']), {}),
        ('two lines "Wo" then "WordArt"', bw.lines(['Wo', 'WordArt']), {}),
        ('two lines "WordArt WordArt" then "Wo"', bw.lines(['WordArt WordArt', 'Wo']), {}),
        ('two lines, 24 pt then 48 pt', [[R('WordArt', size=24)], [R('WordArt', size=48)]], {}),
        ('two lines, 48 pt then 24 pt', [[R('WordArt', size=48)], [R('WordArt', size=24)]], {}),
        ('three lines, the middle one empty', [[R('WordArt')], [], [R('WordArt')]], {}),
    ]


WORD_FORM = dict(autofit='spAutoFit', size=bw.WORD_BOX, ins=bw.INS, wrap='none')


@each
def fam_form(w):
    texts = ['Wo', 'Word', 'WordArt', 'WordArt Word', 'WordArt WordArt', 'Hello World - follow path',
             'WordArt WordArt WordArt']
    out = [(f'"{t}", in Word\'s form', bw.lines([t]), WORD_FORM) for t in texts]
    out += [(f'"WordArt" at {s} pt, in Word\'s form', bw.lines(['WordArt'], size=s), WORD_FORM) for s in (18, 72)]
    out.append(('two lines "WordArt", "Two lines", in Word\'s form', bw.lines(bw.TWO), WORD_FORM))
    return out


def spaced(text, twips):
    """A run with letter spacing (w:spacing, in twentieths of a point)"""
    rpr = (f'<w:rFonts w:ascii="{bw.FONT}" w:hAnsi="{bw.FONT}" w:cs="{bw.FONT}"/><w:color w:val="000000"/>'
           f'<w:spacing w:val="{twips}"/><w:sz w:val="72"/><w:szCs w:val="72"/>')
    return [[bw.Run(text, raw=rpr)]]


@each
def fam_other(w):
    one = bw.lines(['WordArt'])
    return [
        ('"Word", aligned left', bw.lines(['Word']), dict(jc='left')),
        ('"Word", aligned right', bw.lines(['Word']), dict(jc='right')),
        ('"WordArt", letter spacing 5 pt', spaced('WordArt', 100), {}),
        ('"WordArt", letter spacing -1 pt', spaced('WordArt', -20), {}),
        ('"WordArt", bold', bw.lines(['WordArt'], bold=True), {}),
        ('"WordArt" in Aptos Narrow', bw.lines(['WordArt'], font='Aptos Narrow'), {}),
        ('"WordArt" in Aptos Display', bw.lines(['WordArt'], font='Aptos Display'), {}),
        ('"WordArt" in Georgia Pro', bw.lines(['WordArt'], font='Georgia Pro'), {}),
        ('"WordArt", Word\'s insets', one, dict(ins=bw.INS)),
        ('"WordArt", anchored at the middle', one, dict(anchor='ctr')),
        ('"WordArt", anchored at the foot', one, dict(anchor='b')),
        ('"WordArt", wrap none (the box as wide as the text)', one, dict(wrap='none')),
    ]


def fam_button():
    out = []
    for t in ('WordArt', 'WordArt WordArt'):
        for n in (1, 2, 3):
            out.append(art(f'textButton, {n} line{"s" if n > 1 else ""} "{t}"', bw.lines([t] * n), 'textButton'))
    return out


FILES = [
    ('wp10-length', fam_length),
    ('wp11-ink-length', fam_ink_length),
    ('wp12-ink', fam_ink),
    ('wp20-box', fam_box),
    ('wp30-adj', fam_adj),
    ('wp40-size', fam_size),
    ('wp50-lines', fam_lines),
    ('wp60-form', fam_form),
    ('wp70-other', fam_other),
    ('wp80-button', fam_button),
]


def main(staging):
    manifest = ['set\tfile\tpage\tprobe\tkind\tmode\tnote\tsummary\n']
    sums, order, count = [], [], 0
    for name, fam in FILES:
        prefix = name.split('-')[0]
        probes = [(f'{prefix}-{i + 1:02d}', p) for i, p in enumerate(fam())]
        count += len(probes)
        for page, (pid, p) in enumerate(probes, 1):
            manifest.append(f'{SET}\t{name}\t{page}\t{pid}\t{p.kind}\t15\t{p.note}\t{p.summary()}\n')
        d = os.path.join(staging, SET, name)
        os.makedirs(d, exist_ok=True)
        f = os.path.join(d, name + '.docx')
        bw.package(f, probes, 15)
        j = os.path.join(d, 'job.json')
        with open(j, 'w', newline='') as out:
            json.dump({'verb': 'save', 'input': name + '.docx', 'timeoutSeconds': TIMEOUT, 'formats': ['docx', 'pdf']}, out)
        for x in (j, f):
            sums.append(f'{hashlib.sha256(open(x, "rb").read()).hexdigest()}  {SET}/{name}/{os.path.basename(x)}\n')
        order.append(name + '\n')
    open(os.path.join(HERE, 'probes-followpath.tsv'), 'w', newline='').writelines(manifest)
    open(os.path.join(staging, SET + '.sha256'), 'w', newline='').writelines(sums)
    open(os.path.join(staging, SET + '.order.txt'), 'w', newline='').writelines(order)
    print(SET, len(order), 'files,', count, 'probes')


if __name__ == '__main__':
    main(sys.argv[1])
