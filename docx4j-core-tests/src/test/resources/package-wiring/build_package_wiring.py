# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""The package-wiring probe family (CR-075 D9): a package whose wiring has two readings. Stored zips and fixed dates, so a
rebuild is byte-identical. Rounds 1 to 3 are the two-rels files of 4 November 2026, unchanged; the rest are docx4j's own.
usage: build_package_wiring.py <folder>      writes the files there
       build_package_wiring.py --stage <staging dir> <set>     writes a bridge set: job folders, .sha256, .order.txt"""
import hashlib, json, os, sys, zipfile, warnings
warnings.simplefilter('ignore')  # zipfile warns of a duplicate name, which is the point
W = 'xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"'
R = 'xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"'
DECL = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'
OD = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument'
HDR = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships/header'
MAIN = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml'
HEADER = 'application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml'
SECT = ('<w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" '
        'w:header="708" w:footer="708" w:gutter="0"/>')

def ct(*names, headers=()):
    return (DECL + '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
            '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
            '<Default Extension="xml" ContentType="application/xml"/>'
            + ''.join('<Override PartName="/%s" ContentType="%s"/>' % (n, MAIN) for n in names)
            + ''.join('<Override PartName="/%s" ContentType="%s"/>' % (n, HEADER) for n in headers) + '</Types>')

def doc(token):
    return (DECL + '<w:document %s><w:body><w:p><w:r><w:t>%s</w:t></w:r></w:p><w:sectPr>%s</w:sectPr></w:body></w:document>' % (W, token, SECT))

def doc_with_header(token):
    return (DECL + '<w:document %s %s><w:body><w:p><w:r><w:t>%s</w:t></w:r></w:p><w:sectPr><w:headerReference w:type="default" r:id="rId1"/>%s'
            '</w:sectPr></w:body></w:document>' % (W, R, token, SECT))

def header(token):
    return DECL + '<w:hdr %s><w:p><w:r><w:t>%s</w:t></w:r></w:p></w:hdr>' % (W, token)

def rels(*targets, kind=OD):
    return (DECL + '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
            + ''.join('<Relationship Id="rId%d" Type="%s" Target="%s"/>' % (i + 1, kind, t) for i, t in enumerate(targets))
            + '</Relationships>')

def write(path, entries, central_reversed=False, central_drops_first=False):
    with zipfile.ZipFile(path, 'w', zipfile.ZIP_STORED) as z:
        for name, data in entries:
            info = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
            info.external_attr = 0o644 << 16
            z.writestr(info, data.encode('utf-8'))
        if central_reversed:
            z.filelist.reverse()  # the central directory, written at close, lists the entries the other way from the local headers
        if central_drops_first:
            del z.filelist[-2]  # the central directory lists the second of the last two local entries alone

CT = ct('word/document.xml', 'word/other.xml')
CT1 = ct('word/document.xml')
RELS1 = rels('word/document.xml')
PARTS = [('word/document.xml', doc('MAIN_DOCUMENT_XML')), ('word/other.xml', doc('MAIN_OTHER_XML'))]
FILES = {
    # the control
    'control-one-main.docx': [('[Content_Types].xml', CT1), ('_rels/.rels', RELS1), ('word/document.xml', doc('CONTROL_WORDS'))],
    # round 1: two _rels/.rels entries, and one with two officeDocument relationships, in both orders
    'two-rels-entries-document-first.docx': [('[Content_Types].xml', CT), ('_rels/.rels', rels('word/document.xml')),
                                             ('_rels/.rels', rels('word/other.xml'))] + PARTS,
    'two-rels-entries-other-first.docx': [('[Content_Types].xml', CT), ('_rels/.rels', rels('word/other.xml')),
                                          ('_rels/.rels', rels('word/document.xml'))] + PARTS,
    'two-main-rels-document-first.docx': [('[Content_Types].xml', CT),
                                          ('_rels/.rels', rels('word/document.xml', 'word/other.xml'))] + PARTS,
    'two-main-rels-other-first.docx': [('[Content_Types].xml', CT),
                                       ('_rels/.rels', rels('word/other.xml', 'word/document.xml'))] + PARTS,
    # round 2: one factor at a time
    'two-main-rels-zip-other-first.docx': [('[Content_Types].xml', CT),
                                           ('_rels/.rels', rels('word/document.xml', 'word/other.xml'))] + PARTS[::-1],
    'two-main-rels-types-other-first.docx': [('[Content_Types].xml', ct('word/other.xml', 'word/document.xml')),
                                             ('_rels/.rels', rels('word/document.xml', 'word/other.xml'))] + PARTS,
    'two-main-rels-names-swapped.docx': [('[Content_Types].xml', ct('word/zeta.xml', 'word/alpha.xml')),
                                         ('_rels/.rels', rels('word/zeta.xml', 'word/alpha.xml')),
                                         ('word/zeta.xml', doc('MAIN_ZETA_XML')), ('word/alpha.xml', doc('MAIN_ALPHA_XML'))],
    # round 3: one relationship, and a second main part no relationship names, its override last
    'one-main-rel-other-override-last.docx': [('[Content_Types].xml', ct('word/document.xml', 'word/other.xml')),
                                              ('_rels/.rels', rels('word/document.xml'))] + PARTS,
    'one-main-rel-document-override-last.docx': [('[Content_Types].xml', ct('word/other.xml', 'word/document.xml')),
                                                 ('_rels/.rels', rels('word/other.xml'))] + PARTS,
    # docx4j's own: two entries of one name
    'dup-entry-document.docx': [('[Content_Types].xml', CT1), ('_rels/.rels', RELS1), ('word/document.xml', doc('FIRST_ENTRY')),
                                ('word/document.xml', doc('SECOND_ENTRY'))],
    'dup-entry-same-bytes.docx': [('[Content_Types].xml', CT1), ('_rels/.rels', RELS1), ('word/document.xml', doc('SAME_WORDS')),
                                  ('word/document.xml', doc('SAME_WORDS'))],
    'dup-entry-content-types.docx': [('[Content_Types].xml', CT1), ('[Content_Types].xml', ct('word/other.xml')),
                                     ('_rels/.rels', RELS1)] + PARTS,
    'dup-entry-part-rels.docx': [('[Content_Types].xml', ct('word/document.xml', headers=('word/header1.xml', 'word/header2.xml'))),
                                 ('_rels/.rels', RELS1), ('word/document.xml', doc_with_header('BODY_WORDS')),
                                 ('word/_rels/document.xml.rels', rels('header1.xml', kind=HDR)),
                                 ('word/_rels/document.xml.rels', rels('header2.xml', kind=HDR)),
                                 ('word/header1.xml', header('HEADER_ONE')), ('word/header2.xml', header('HEADER_TWO'))],
    # names that differ only in case, the exact name first and last
    'case-variant-exact-first.docx': [('[Content_Types].xml', CT1), ('_rels/.rels', RELS1), ('word/document.xml', doc('CASE_EXACT')),
                                      ('Word/Document.xml', doc('CASE_VARIANT'))],
    'case-variant-exact-last.docx': [('[Content_Types].xml', CT1), ('_rels/.rels', RELS1), ('Word/Document.xml', doc('CASE_VARIANT')),
                                     ('word/document.xml', doc('CASE_EXACT'))],
    # CR-075 section 7: the central directory the other way from the local headers
    'dup-central-reversed.docx': [('[Content_Types].xml', CT1), ('_rels/.rels', RELS1), ('word/document.xml', doc('FIRST_LOCAL')),
                                  ('word/document.xml', doc('SECOND_LOCAL'))],
}
# not in the Word round of 7 November, added on review: a file and a stream must read them alike
FILES.update({
    'dup-local-only.docx': [('[Content_Types].xml', CT1), ('_rels/.rels', RELS1), ('word/document.xml', doc('FIRST_LOCAL')),
                            ('word/document.xml', doc('SECOND_LOCAL'))],
    'dup-directory-entry.docx': [('[Content_Types].xml', CT1), ('_rels/.rels', RELS1), ('word/', ''), ('word/', ''),
                                 ('word/document.xml', doc('DIRECTORY_WORDS'))],
})
CENTRAL_REVERSED = {'dup-central-reversed.docx'}
CENTRAL_DROPS_FIRST = {'dup-local-only.docx'}

def build(path, name):
    write(path, FILES[name], name in CENTRAL_REVERSED, name in CENTRAL_DROPS_FIRST)

def stage(staging, setname):
    sums, order = [], []
    for name in FILES:
        short = name[:-len('.docx')]
        for copy in 'ab':
            job = 'pw-%s-%s' % (short, copy)
            d = os.path.join(staging, setname, job)
            os.makedirs(d, exist_ok=True)
            f = os.path.join(d, name)
            build(f, name)
            j = os.path.join(d, 'job.json')
            with open(j, 'w', newline='') as o:
                json.dump({'verb': 'save', 'input': name, 'timeoutSeconds': 120, 'formats': ['docx', 'txt', 'pdf']}, o)
            for x in (j, f):
                sums.append('%s  %s/%s/%s\n' % (hashlib.sha256(open(x, 'rb').read()).hexdigest(), setname, job, os.path.basename(x)))
            order.append(job + '\n')
    open(os.path.join(staging, setname + '.sha256'), 'w', newline='').writelines(sums)
    open(os.path.join(staging, setname + '.order.txt'), 'w', newline='').writelines(order)
    print(len(FILES), 'files,', len(order), 'jobs')

def stage_order(staging, setname):
    """A job opens the file once per output, in formats' order, in one Word process, so these separate an open's place from its format."""
    import itertools
    orders = [[f] for f in ('docx', 'txt', 'pdf')] + [list(o) for o in itertools.permutations(('docx', 'txt', 'pdf'))]
    sums, order = [], []
    for short, name in (('other-first', 'two-main-rels-other-first.docx'), ('types-other-first', 'two-main-rels-types-other-first.docx')):
        for formats in orders:
            for copy in 'ab':
                job = 'oo-%s-%s-%s' % (short, '-'.join(formats), copy)
                d = os.path.join(staging, setname, job)
                os.makedirs(d, exist_ok=True)
                f = os.path.join(d, name)
                build(f, name)
                j = os.path.join(d, 'job.json')
                with open(j, 'w', newline='') as o:
                    json.dump({'verb': 'save', 'input': name, 'timeoutSeconds': 120, 'formats': formats}, o)
                for x in (j, f):
                    sums.append('%s  %s/%s/%s\n' % (hashlib.sha256(open(x, 'rb').read()).hexdigest(), setname, job, os.path.basename(x)))
                order.append(job + '\n')
    open(os.path.join(staging, setname + '.sha256'), 'w', newline='').writelines(sums)
    open(os.path.join(staging, setname + '.order.txt'), 'w', newline='').writelines(order)
    print(len(order), 'jobs')

if __name__ == '__main__':
    if sys.argv[1] == '--stage':
        stage(sys.argv[2], sys.argv[3])
    elif sys.argv[1] == '--stage-order':
        stage_order(sys.argv[2], sys.argv[3])
    else:
        os.makedirs(sys.argv[1], exist_ok=True)
        for name in FILES:
            build(os.path.join(sys.argv[1], name), name)
        print(len(FILES), 'files')
