# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Word's reading of the absent-properties files, from the bridge's results: each field's result in each file, from Word's text
save, with a run that differs from its twin marked. usage: read_absent_properties.py <bridge dir>"""
import glob, json, os, re, sys

NAMES = ['fp-00-control', 'fp-01-no-core', 'fp-02-no-app', 'fp-03-no-custom', 'fp-04-no-properties', 'fp-05-properties-missing']

def read(bridge, job, name):
    d = '%s/portfolio/%s' % (bridge, job)
    t = '%s/%s.txt' % (d, name)
    out = {}
    if not os.path.exists(t):
        return out
    raw = open(t, 'rb').read()
    for enc in ('utf-8-sig', 'cp1252'):
        try:
            text = raw.decode(enc)
            break
        except UnicodeDecodeError:
            continue
    for line in text.splitlines():
        at = line.find(' =')
        if at > 0:
            out[line[:at]] = line[at + 2:].strip()
    return out

def main(bridge):
    cols = []
    for n in NAMES:
        a, b = read(bridge, n + '-a', n), read(bridge, n + '-b', n)
        diff = [k for k in a if a.get(k) != b.get(k)]
        cols.append((n, a, diff))
    keys = list(cols[0][1].keys())
    print('field\t' + '\t'.join(n[3:5] for n, a, d in cols))
    for k in keys:
        print(k + '\t' + '\t'.join(repr(a.get(k, '<none>'))[1:-1][:34] + ('*' if k in d else '') for n, a, d in cols))
    print('fields read:', [len(a) for n, a, d in cols])

if __name__ == '__main__':
    main(sys.argv[1])
