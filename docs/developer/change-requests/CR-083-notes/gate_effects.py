# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-083 phase 3's gate: Word's PDF against docx4j's, probe by probe, from read_phase3.py's JSON lines of each.

    python3 gate_effects.py <word.jsonl> <docx4j.jsonl> [--list] [--file PREFIX]

For each probe: the box's height and the text's origin (the room), the pictures that aren't empty (alpha peaking
below 0.01: Word's warped WordArt carries an empty one, docx4j none), and each picture's alpha bounds at 50% and 5% of its peak (the
greatest distance between a side of Word's and docx4j's) and its peak. Pictures are paired in drawing order. Per file:
the counts within 0.25 pt, the medians and the worst; --list prints every probe."""
import json, statistics, sys

EMPTY = 0.01        # a picture whose alpha peaks below this is empty (a reflection's endA held past endPos: 1 of 255)


def load(path):
    out = {}
    for line in open(path):
        r = json.loads(line)
        out[(r['file'], r['page'])] = r
    return out


def side(a, b):
    return max(abs(x - y) for x, y in zip(a, b)) if a and b else None


def compare(w, d):
    out = {'probe': w['probe'], 'file': w['file'], 'note': w['note']}
    if w['kind'] == 'body':
        out['body'] = True
        return out
    if w.get('height') is not None and d.get('height') is not None:
        out['dH'] = d['height'] - w['height']
    if w.get('origin') and d.get('origin'):
        out['dO'] = d['origin'][1] - w['origin'][1]
    wp = [p for p in w['pictures'] if p['alpha'] > EMPTY]
    dp = [p for p in d['pictures'] if p['alpha'] > EMPTY]
    out['n'] = (len(wp), len(dp))
    b50, b5, peak = [], [], []
    for a, b in zip(wp, dp):
        if a.get('b50') and b.get('b50'):
            b50.append(side(a['b50'], b['b50']))
            b5.append(side(a['b5'], b['b5']))
        peak.append(b['alpha'] - a['alpha'])
    out['b50'] = max(b50) if b50 else None
    out['b5'] = max(b5) if b5 else None
    out['peak'] = max(peak, key=abs) if peak else None
    return out


def main(word, docx4j, *args):
    listing = '--list' in args
    prefix = args[args.index('--file') + 1] if '--file' in args else ''
    W, D = load(word), load(docx4j)
    rows = [compare(W[k], D[k]) for k in sorted(W) if k in D and k[0].startswith(prefix)]
    files = sorted({r['file'] for r in rows})
    for f in files:
        fr = [r for r in rows if r['file'] == f and not r.get('body')]
        if not fr:
            continue
        def stat(key):
            v = [abs(r[key]) for r in fr if r.get(key) is not None]
            if not v:
                return '-'
            return f'{sum(x <= 0.25 for x in v)}/{len(v)} within 0.25, median {statistics.median(v):.2f}, worst {max(v):.2f}'
        counts = sum(r['n'][0] == r['n'][1] for r in fr)
        print(f'{f}: {len(fr)} probes; pictures counted alike {counts}/{len(fr)}')
        print(f'   box height  {stat("dH")}')
        print(f'   text origin {stat("dO")}')
        print(f'   bounds 50%  {stat("b50")}')
        print(f'   bounds 5%   {stat("b5")}')
        print(f'   alpha peak  {stat("peak")}')
        if listing:
            for r in fr:
                fmt = lambda v: '-' if v is None else f'{v:6.2f}'
                print(f'     {r["probe"]} {r["note"][:58]:58s} dH {fmt(r.get("dH"))} dO {fmt(r.get("dO"))} n {r["n"]} '
                      f'b50 {fmt(r["b50"])} b5 {fmt(r["b5"])} peak {fmt(r["peak"])}')


if __name__ == '__main__':
    main(*sys.argv[1:])
