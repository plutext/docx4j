# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""CR-084 phase 1: Word's measured colours for docx4j's unit test of PictureRecolour (PictureRecolourWordTest), from
phase 0's probes (phase0-word.json, read by read_recolour.py from Word's PDFs) and their XML (build_recolour.py).

Writes, into docx4j-core-tests/src/test/resources/images/recolour/:
  sources.tsv       each calibration picture's 256 patch colours, RRGGBBAA
  word-patches.tsv  each DrawingML probe with patches: probe, note, picture, Word's filter (DCTDecode is a JPEG, whose
                    re-encoding adds a level or two), the a:blip's children as written, and Word's 256 patch colours
                    (RRGGBBAA; '-' where a patch isn't in the picture, as outside a crop)

The VML probes (rk70) are phase 3's; the photo's (rk60, rk82) have no patches.

usage: build_test_table.py"""
import importlib.util, json, os

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, '..', '..', '..', '..', 'docx4j-core-tests', 'src', 'test', 'resources', 'images', 'recolour')
_spec = importlib.util.spec_from_file_location('recolour', os.path.join(HERE, 'build_recolour.py'))
rc = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(rc)

def hexc(c):
    return '%02X%02X%02X%02X' % tuple(c) if c else '-'

def main():
    word = json.load(open(os.path.join(HERE, 'phase0-word.json'), encoding='utf-8'))
    blips = {}
    for files in rc.SETS.values():
        for name, fam in files:
            prefix = name.split('-')[0]
            for i, p in enumerate(fam()):
                if p.vml is None:
                    blips[f'{prefix}-{i + 1:02d}'] = p
    os.makedirs(OUT, exist_ok=True)
    sources = {}
    rows = []
    for k, v in sorted(word.items()):
        p = blips.get(k)
        pics = [q for q in v['pictures'] if q.get('patches')]
        if p is None or not pics or not v['source']:
            continue
        sources[v['picture']] = v['source']
        rows.append([k, v['note'], v['picture'], pics[0]['filter'], p.blip, ' '.join(hexc(c) for c in pics[0]['patches'])])
    with open(os.path.join(OUT, 'sources.tsv'), 'w', encoding='utf-8', newline='') as f:
        f.write('# CR-084 phase 0: the calibration pictures\' patch colours (RRGGBBAA), from build_recolour.py; '
                'written by CR-084-notes/build_test_table.py\n')
        for name, src in sorted(sources.items()):
            f.write(name + '\t' + ' '.join(hexc(c) for c in src) + '\n')
    with open(os.path.join(OUT, 'word-patches.tsv'), 'w', encoding='utf-8', newline='') as f:
        f.write('# CR-084 phase 0: Word\'s PDF\'s patch colours (RRGGBBAA) for each probe; written by '
                'CR-084-notes/build_test_table.py from phase0-word.json\n')
        for r in [['probe', 'note', 'picture', 'filter', 'blip', 'word']] + rows:   # no field holds a tab or a newline
            f.write('\t'.join(r) + '\n')
    print(len(rows), 'probes,', len(sources), 'pictures ->', os.path.normpath(OUT))

if __name__ == '__main__':
    main()
