# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""What Word reads of a relationships part that is well formed in the wrong namespace (CR-075 section 6, docx4j-python's check at
9077b95). A plain body names no relationship, so a repair can only be over the namespace; a rich body names a header and has a note, and is
read against the same body with no relationships part at all. No relationship here is External. Each part's words say which relationship reached it.
usage: build_rels_namespace.py <staging dir> <set>"""
import hashlib, json, os, sys, zipfile

DECL = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
W = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
R = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships'
PKG = 'http://schemas.openxmlformats.org/package/2006/relationships'
WRONG = 'urn:docx4j:wrong'
CTW = 'application/vnd.openxmlformats-officedocument.wordprocessingml.'
NS = 'xmlns:w="%s" xmlns:r="%s"' % (W, R)
PAGE = '<w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/>'
STYLED = '<w:p><w:pPr><w:pStyle w:val="Probe"/></w:pPr><w:r><w:t>STYLED_WORDS</w:t></w:r></w:p>'

PLAIN = DECL + '<w:document %s><w:body>%s<w:sectPr>%s</w:sectPr></w:body></w:document>' % (NS, STYLED, PAGE)
RICH = DECL + ('<w:document %s><w:body>%s<w:p><w:r><w:t>NOTED_WORDS</w:t></w:r><w:r><w:footnoteReference w:id="1"/></w:r></w:p>'
               '<w:sectPr><w:headerReference w:type="default" r:id="rId4"/>%s</w:sectPr></w:body></w:document>') % (NS, STYLED, PAGE)

STYLES = DECL + ('<w:styles xmlns:w="%s"><w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/></w:style>'
                 '<w:style w:type="paragraph" w:styleId="Probe"><w:name w:val="Probe"/><w:basedOn w:val="Normal"/><w:pPr><w:jc w:val="right"/></w:pPr></w:style></w:styles>') % W
SETTINGS = DECL + '<w:settings xmlns:w="%s"><w:defaultTabStop w:val="1000"/></w:settings>' % W
HEADER = DECL + '<w:hdr xmlns:w="%s"><w:p><w:r><w:t>HEADER_WORDS</w:t></w:r></w:p></w:hdr>' % W
NOTES = DECL + ('<w:footnotes xmlns:w="%s"><w:footnote w:type="separator" w:id="-1"><w:p><w:r><w:separator/></w:r></w:p></w:footnote>'
                '<w:footnote w:type="continuationSeparator" w:id="0"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:footnote>'
                '<w:footnote w:id="1"><w:p><w:r><w:t>NOTE_WORDS</w:t></w:r></w:p></w:footnote></w:footnotes>') % W

STYLES_REL = ('rId1', R + '/styles', 'styles.xml', '')
SETTINGS_REL = ('rId2', R + '/settings', 'settings.xml', '')
RICH_RELS = [('rId4', R + '/header', 'header1.xml', ''), ('rId5', R + '/footnotes', 'footnotes.xml', '')]

def rel(r, prefix=''):
    return '<%sRelationship Id="%s" Type="%s" Target="%s"%s/>' % ((prefix,) + r)

def rels(items, shape):
    """The part as a shape has it: right, wrong, none, root-wrong, children-wrong, one-wrong."""
    if shape == 'right':
        return DECL + '<Relationships xmlns="%s">%s</Relationships>' % (PKG, ''.join(rel(r) for r in items))
    if shape == 'wrong':
        return DECL + '<Relationships xmlns="%s">%s</Relationships>' % (WRONG, ''.join(rel(r) for r in items))
    if shape == 'none':
        return DECL + '<Relationships>%s</Relationships>' % ''.join(rel(r) for r in items)
    if shape == 'root-wrong':
        return DECL + '<x:Relationships xmlns:x="%s" xmlns="%s">%s</x:Relationships>' % (WRONG, PKG, ''.join(rel(r) for r in items))
    if shape == 'children-wrong':
        return DECL + '<Relationships xmlns="%s" xmlns:x="%s">%s</Relationships>' % (PKG, WRONG, ''.join(rel(r, 'x:') for r in items))
    if shape == 'one-wrong':
        return DECL + '<Relationships xmlns="%s" xmlns:x="%s">%s%s</Relationships>' % (PKG, WRONG, rel(items[0]), ''.join(rel(r, 'x:') for r in items[1:]))
    raise ValueError(shape)

# name, body, the document's relationships (a shape, or None for no part), the package's
FILES = [
    ('rn-00-control-plain', 'plain', 'right', 'right'),
    ('rn-01-control-rich', 'rich', 'right', 'right'),
    ('rn-02-plain-wrong', 'plain', 'wrong', 'right'),
    ('rn-03-rich-wrong', 'rich', 'wrong', 'right'),
    ('rn-04-plain-no-namespace', 'plain', 'none', 'right'),
    ('rn-05-plain-root-wrong', 'plain', 'root-wrong', 'right'),
    ('rn-06-plain-children-wrong', 'plain', 'children-wrong', 'right'),
    ('rn-07-plain-one-wrong', 'plain', 'one-wrong', 'right'),
    ('rn-08-rich-no-part', 'rich', None, 'right'),
    ('rn-09-package-rels-wrong', 'plain', 'right', 'wrong'),
]

def entry(z, name, text):
    i = zipfile.ZipInfo(name, (2026, 11, 7, 0, 0, 0))
    i.compress_type = zipfile.ZIP_STORED
    z.writestr(i, text.encode('utf-8'))

def build(path, body, shape, package):
    rich = body == 'rich'
    overrides = [('/word/document.xml', CTW + 'document.main+xml'), ('/word/styles.xml', CTW + 'styles+xml'), ('/word/settings.xml', CTW + 'settings+xml')]
    if rich:
        overrides += [('/word/header1.xml', CTW + 'header+xml'), ('/word/footnotes.xml', CTW + 'footnotes+xml')]
    ct = ('<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
          '<Default Extension="xml" ContentType="application/xml"/>' + ''.join('<Override PartName="%s" ContentType="%s"/>' % o for o in overrides) + '</Types>')
    with zipfile.ZipFile(path, 'w', zipfile.ZIP_STORED) as z:
        entry(z, '[Content_Types].xml', DECL + ct)
        entry(z, '_rels/.rels', rels([('rId1', R + '/officeDocument', 'word/document.xml', '')], package))
        entry(z, 'word/document.xml', RICH if rich else PLAIN)
        if shape is not None:
            entry(z, 'word/_rels/document.xml.rels', rels([STYLES_REL, SETTINGS_REL] + (RICH_RELS if rich else []), shape))
        entry(z, 'word/styles.xml', STYLES)
        entry(z, 'word/settings.xml', SETTINGS)
        if rich:
            entry(z, 'word/header1.xml', HEADER)
            entry(z, 'word/footnotes.xml', NOTES)

def main(staging, setname):
    sums, order = [], []
    for name, body, shape, package in FILES:
        for copy in 'ab':
            job = '%s-%s' % (name, copy)
            d = os.path.join(staging, setname, job)
            os.makedirs(d, exist_ok=True)
            f = os.path.join(d, name + '.docx')
            build(f, body, shape, package)
            j = os.path.join(d, 'job.json')
            with open(j, 'w', newline='') as o:
                json.dump({'verb': 'save', 'input': name + '.docx', 'timeoutSeconds': 120, 'formats': ['docx', 'txt', 'pdf']}, o)
            for x in (j, f):
                sums.append('%s  %s/%s/%s\n' % (hashlib.sha256(open(x, 'rb').read()).hexdigest(), setname, job, os.path.basename(x)))
            order.append(job + '\n')
    open(os.path.join(staging, setname + '.sha256'), 'w', newline='').writelines(sums)
    open(os.path.join(staging, setname + '.order.txt'), 'w', newline='').writelines(order)
    print(len(FILES), 'files,', len(order), 'jobs')

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
