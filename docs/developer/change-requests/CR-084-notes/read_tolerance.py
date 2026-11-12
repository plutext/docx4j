# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-084 phase 1b: reads Word's PDFs of the clrChange tolerance round (cr084-clrchange-tol, build_tolerance.py) into
tolerance-word.json: for each probe page, the picture Word drew and its 256 patch centres (RGBA, as read_recolour.py
reads them: un-premultiplied, alpha 0..255), beside each patch's source colour, as intended and as decoded
(tolerance-sources.json; for a lossless picture the two are the same).

usage: read_tolerance.py <results dir: one folder a job, <job>/<job>.pdf> [out.json, default tolerance-word.json]"""
import csv, importlib.util, json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location('read_recolour', os.path.join(HERE, 'read_recolour.py'))
rr = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(rr)

def main(results, out_path=None):
    probes = list(csv.DictReader(open(os.path.join(HERE, 'tolerance-probes.tsv'), encoding='utf-8'), delimiter='\t'))
    sources = json.load(open(os.path.join(HERE, 'tolerance-sources.json'), encoding='utf-8'))
    for name, v in sources.items():                      # read_recolour samples the patches of a picture it knows
        rr.LEVELS[name] = (lambda v=v: v['decoded'])
    out = {}
    for job in sorted({p['file'] for p in probes}):
        pdf = os.path.join(results, job, job + '.pdf')
        if not os.path.isfile(pdf):
            print('no pdf', job)
            continue
        objs = rr.pd.pages(pdf)
        for p in [p for p in probes if p['file'] == job]:
            pics, fills = rr.page(pdf, objs[int(p['page']) - 1], p['picture'], None)
            src = sources[p['picture']]
            out[p['probe']] = {'note': p['note'], 'picture': p['picture'], 'summary': p['summary'], 'pictures': pics,
                               'intended': src['intended'], 'decoded': src['decoded']}
        print(job, len([p for p in probes if p['file'] == job]), 'probes')
    out_path = out_path or os.path.join(HERE, 'tolerance-word.json')
    with open(out_path, 'w', encoding='utf-8') as f:
        json.dump(out, f, separators=(',', ':'))
    print(out_path, len(out), 'probes')

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2] if len(sys.argv) > 2 else None)
