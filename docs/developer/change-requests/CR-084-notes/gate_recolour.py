# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-084 phase 1's gate: docx4j's PDFs of phase 0's probes against Word's (phase0-word.json), patch by patch.

Each probe page is read as read_recolour.py reads Word's (the picture at its frame, its 256 patch centres), and each
patch compared with Word's. The tolerance is the gate's (CR-084 section 6): 2 levels of 255; 7 for tint and for a
semi-transparent pixel's colour, before or after (Word's premultiplied storage); 5 where Word's picture is a JPEG (its
re-encoding). A colour is compared where Word's alpha is at least a quarter. Departures decided in phase 0 are listed,
not failed: hsl (the standard's, where Word draws black), blur (Word draws nothing), clrChange on a semi-transparent
pixel (Word's premultiplied storage misses some). The VML probes (rk70) are phase 3's: listed, not failed.

Also: the pictures docx4j drew on each page (filter, size, soft mask), for the photo and EMF probes, which have no
patches.

usage: gate_recolour.py <docx4j results dir: <job>/<job>.pdf> [out.json]"""
import csv, importlib.util, json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('read_recolour', os.path.join(HERE, 'read_recolour.py'))
rr = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(rr)

def tolerance(note, summary, word_filter, src_a, word_a):
    if 'tint' in note or src_a < 255 or word_a < 255:
        return 7
    return 5 if word_filter == 'DCTDecode' else 2

def main(results, out=None):
    word = json.load(open(os.path.join(HERE, 'phase0-word.json'), encoding='utf-8'))
    probes = list(csv.DictReader(open(os.path.join(HERE, 'probes.tsv'), encoding='utf-8'), delimiter='\t'))
    ours, rows = {}, []
    fails = 0
    for job in sorted({p['file'] for p in probes if p['page'] != '-'}):
        pdf = os.path.join(results, job, job + '.pdf')
        if not os.path.isfile(pdf):
            print('no pdf', job)
            continue
        objs = rr.pd.pages(pdf)
        for p in [p for p in probes if p['file'] == job]:
            k = p['probe']
            pics, fills = rr.page(pdf, objs[int(p['page']) - 1], p['picture'], rr.crop_of(p['summary']))
            ours[k] = {'pictures': pics, 'fills': fills}
            w = word.get(k)
            drawn = ' '.join(f"{q.get('filter') or 'form'}:{q.get('w')}x{q.get('h')}{'+smask' if q.get('smask') else ''}"
                             for q in pics)
            our = [q for q in pics if q.get('patches')]
            wp = [q for q in (w or {}).get('pictures', []) if q.get('patches')]
            note = p['note']
            if not w or not w['source'] or not wp:
                rows.append((k, note, 'no patches', drawn, ''))
                continue
            if not our:
                verdict = 'DEPARTURE' if note.startswith('blur') else 'FAIL'
                fails += verdict == 'FAIL'
                rows.append((k, note, verdict + ' no picture read', drawn, ''))
                continue
            departure = note.startswith('hsl') or job.startswith('rk70')
            worst, bad = 0, []
            for i, (s, a, b) in enumerate(zip(w['source'], wp[0]['patches'], our[0]['patches'])):
                if not a or not b:
                    continue
                da = abs(a[3] - b[3])
                if da > 2:
                    bad.append((i, 'alpha', s, a, b))
                if a[3] < 64:
                    continue
                if 'clrChange' in note and 0 < s[3] < 255:
                    continue
                d = max(abs(a[j] - b[j]) for j in range(3))
                worst = max(worst, d)
                if d > tolerance(note, p['summary'], wp[0]['filter'], s[3], a[3]):
                    bad.append((i, 'colour', s, a, b))
            verdict = 'ok' if not bad else ('DEPARTURE' if departure else 'FAIL')
            fails += verdict == 'FAIL'
            rows.append((k, note, f'{verdict} worst {worst}' + (f', {len(bad)} patches off' if bad else ''), drawn,
                         '; '.join(f'{i} {kind} src {s} word {a} ours {b}' for i, kind, s, a, b in bad[:3])))
    for r in rows:
        print(f'{r[0]} {r[1][:52]:52s} {r[2]:28s} {r[3][:48]:48s} {r[4][:150]}')
    print(f'{len(rows)} probes, {fails} failing')
    if out:
        with open(out, 'w', encoding='utf-8') as f:
            json.dump(ours, f, separators=(',', ':'))

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2] if len(sys.argv) > 2 else None)
