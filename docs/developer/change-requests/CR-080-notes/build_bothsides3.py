# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Text on both sides of an object, the third round: the rules phase B's design review found still unmeasured (FOP
fork CR-024 section 12.6, b4973a8e3), on Jason's order in the fork's session: "Measure the things Fable identified".

fb10's geometry (build_bothsides.py, build_bothsides2.py): A4, 1in margins (the column x 72 to 523.3), Arial 10pt
single; the object a rectangle shape 150 x 120pt, square on both sides, distL and distR 9pt, distT and distB 0,
anchored in its paragraph 12pt below its top. One case to a page, its label stating its gaps and indents. Where a case
needs a paragraph to start in the band, the anchor paragraph holds fb10's 12 words (a line, then part of the first band
line) and the case's paragraph B follows it.

  E1   the left gap 16pt (under the 18pt minimum); B with a first-line indent of 24pt
  E2   the left gap 40pt; B with a first-line indent of 24pt, its first word 30pt wide (it fits the gap, not the gap
       less the indent)
  E3   as E2, B with a left indent of 36pt and a hanging indent of 24pt (as asked: the first line at 12pt)
  E4   as E2, B with a left indent of 36pt and a hanging indent of 36pt (the first line at the margin)
  F1   the left gap 40pt; B with a left indent of 24pt (16pt free); "a a a ..."
  F2   the same with a left indent of 20pt (20pt free)
  F3   the left gap 40pt; B with a first-line indent of 25pt (15pt free on its first line only)
  F4   the right gap 40pt (the wide gap on the left); B with a right indent of 24pt (16pt free)
  G1   equal gaps; a 200pt unbreakable word in the anchor paragraph, inside the band
  G2   equal gaps; the same word as B's first word, B starting in the band
  G3   equal gaps; a 200pt URL-like token with break opportunities at "/", inside the band
  H1   equal gaps; the anchor paragraph shaded (w:shd D9E2F3), its text through the band
  H2   equal gaps; the anchor paragraph boxed (w:pBdr, 0.5pt)
  H3   equal gaps; both, then an unshaded paragraph

usage: build_bothsides3.py <staging dir>   (also writes probes-bothsides3.tsv beside this script)"""
import hashlib, importlib.util, json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('bothsides2', os.path.join(HERE, 'build_bothsides2.py'))
b2 = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(b2)
bs, COL, OW = b2.bs, b2.COL, b2.OW

SET = 'fop-cr024-bothsides-r3'
CENTRED = b2.CENTRED

def tw(pt):
    return round(pt * 20)

def ind(left=0, right=0, first=0, hanging=0):
    a = []
    if left: a.append(f'w:left="{tw(left)}"')
    if right: a.append(f'w:right="{tw(right)}"')
    if first: a.append(f'w:firstLine="{tw(first)}"')
    if hanging: a.append(f'w:hanging="{tw(hanging)}"')
    return f'<w:ind {" ".join(a)}/>'

def anchor_and_b(b_text, b_ppr):
    """fb10's anchor paragraph (12 words), then paragraph B, then a paragraph of words"""
    def build(n, case):
        return (b2.p(b2.obj(n, case.x, case.dist_l, case.dist_r) + b2.run(bs.words_text(1, 12)))
                + b2.p(b2.run(b_text), b_ppr) + b2.p(b2.run(bs.words_text(200, 40))))
    return build

LONG = b2.unbreakable(200.0)

def url(target=200.0):
    parts, out = ['http://www.example.com'], 'http://www.example.com'
    k = 0
    while bs.width(out) < target - 30:
        seg = 'abcdefghij'[k % 10:] + 'abcdefghij'[:k % 10]
        out += '/' + seg
        k += 1
    return out

def anchor_with(text, ppr=''):
    def build(n, case):
        return b2.p(b2.obj(n, case.x, case.dist_l, case.dist_r) + b2.run(text), ppr) + b2.p(b2.run(bs.words_text(300, 40)))
    return build

def h3(n, case):
    ppr = ('<w:pBdr>' + ''.join(f'<w:{e} w:val="single" w:sz="4" w:space="1" w:color="000000"/>' for e in ('top', 'left', 'bottom', 'right'))
           + '</w:pBdr><w:shd w:val="clear" w:color="auto" w:fill="D9E2F3"/>')
    return (b2.p(b2.obj(n, case.x, case.dist_l, case.dist_r) + b2.run(bs.words_text(1, 60)), ppr)
            + b2.p(b2.run(bs.words_text(61, 60))))

def cases():
    A = ' '.join(['a'] * 400)
    words_b = 'abcdef ' + bs.words_text(13, 80)
    shd = '<w:shd w:val="clear" w:color="auto" w:fill="D9E2F3"/>'
    box = '<w:pBdr>' + ''.join(f'<w:{e} w:val="single" w:sz="4" w:space="1" w:color="000000"/>' for e in ('top', 'left', 'bottom', 'right')) + '</w:pBdr>'
    long_mid = bs.words_text(1, 21) + ' ' + LONG + ' ' + bs.words_text(22, 50)
    url_mid = bs.words_text(1, 21) + ' ' + url() + ' ' + bs.words_text(22, 50)
    C = b2.Case
    return [
        C('E1', 'left gap 16pt; paragraph B first-line indent 24pt, starting in the band', 16 + 9, 9, 9, anchor_and_b(bs.words_text(13, 80), ind(first=24))),
        C('E2', 'left gap 40pt; B first-line indent 24pt, its first word (abcdef) 30pt wide', 40 + 9, 9, 9, anchor_and_b(words_b, ind(first=24))),
        C('E3', 'left gap 40pt; B left indent 36pt, hanging 24pt (first line at 12pt), first word 30pt', 40 + 9, 9, 9, anchor_and_b(words_b, ind(left=36, hanging=24))),
        C('E4', 'left gap 40pt; B left indent 36pt, hanging 36pt (first line at the margin), first word 30pt', 40 + 9, 9, 9, anchor_and_b(words_b, ind(left=36, hanging=36))),
        C('F1', 'left gap 40pt; B left indent 24pt (16pt free); a a a', 40 + 9, 9, 9, anchor_and_b(A, ind(left=24))),
        C('F2', 'left gap 40pt; B left indent 20pt (20pt free); a a a', 40 + 9, 9, 9, anchor_and_b(A, ind(left=20))),
        C('F3', 'left gap 40pt; B first-line indent 25pt (15pt free on its first line); a a a', 40 + 9, 9, 9, anchor_and_b(A, ind(first=25))),
        C('F4', 'right gap 40pt; B right indent 24pt (16pt free); a a a', COL - 40 - 9 - OW, 9, 9, anchor_and_b(A, ind(right=24))),
        C('G1', 'equal gaps; a 200pt unbreakable word mid-paragraph, in the band', CENTRED, 9, 9, anchor_with(long_mid)),
        C('G2', 'equal gaps; the 200pt word as paragraph B\'s first word, B starting in the band', CENTRED, 9, 9,
          anchor_and_b(LONG + ' ' + bs.words_text(13, 60), '')),
        C('G3', 'equal gaps; a 200pt URL-like token (breaks at /) mid-paragraph, in the band', CENTRED, 9, 9, anchor_with(url_mid)),
        C('H1', 'equal gaps; the anchor paragraph shaded (D9E2F3)', CENTRED, 9, 9, anchor_with(bs.words_text(1, 120), shd)),
        C('H2', 'equal gaps; the anchor paragraph boxed (0.5pt)', CENTRED, 9, 9, anchor_with(bs.words_text(1, 120), box)),
        C('H3', 'equal gaps; the anchor paragraph shaded and boxed, then an unshaded paragraph', CENTRED, 9, 9, h3),
    ]

def main(staging):
    name = 'fb40-rules'
    cs = cases()
    d = os.path.join(staging, SET, name)
    os.makedirs(d)
    f = os.path.join(d, name + '.docx')
    b2.package(f, cs)
    j = os.path.join(d, 'job.json')
    with open(j, 'w', newline='') as out:
        json.dump({'verb': 'save', 'input': name + '.docx', 'timeoutSeconds': 300, 'formats': ['docx', 'pdf']}, out)
    sums = [f'{hashlib.sha256(open(x, "rb").read()).hexdigest()}  {SET}/{name}/{os.path.basename(x)}\n' for x in (j, f)]
    open(os.path.join(staging, SET + '.sha256'), 'w', newline='').writelines(sums)
    open(os.path.join(staging, SET + '.order.txt'), 'w', newline='').writelines([name + '\n'])
    rows = ['set\tfile\tpage\tcase\tnote\tgap_left\tgap_right\n']
    for i, c in enumerate(cs):
        gl, gr = c.gaps()
        rows.append(f'{SET}\t{name}\t{i + 1}\t{c.cid}\t{c.note}\t{gl:.2f}\t{gr:.2f}\n')
    open(os.path.join(HERE, 'probes-bothsides3.tsv'), 'w', newline='').writelines(rows)
    print(name, len(cs), 'cases; the long word', round(bs.width(LONG), 1), 'pt; the URL', round(bs.width(url()), 1), 'pt')

if __name__ == '__main__':
    main(sys.argv[1])
