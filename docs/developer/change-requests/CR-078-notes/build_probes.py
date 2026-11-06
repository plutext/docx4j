# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Purpose-built probes for w:stylePaneFormatFilter: val against the fifteen attributes (CR-078).
Each file is one paragraph naming itself; only settings.xml's element differs (and, for m*, the compatibility mode)."""
import json, os, sys, zipfile

W = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
ATTRS = [(0x0001,'allStyles'),(0x0002,'customStyles'),(0x0004,'latentStyles'),(0x0008,'stylesInUse'),
         (0x0020,'headingStyles'),(0x0040,'numberingStyles'),(0x0080,'tableStyles'),
         (0x0100,'directFormattingOnRuns'),(0x0200,'directFormattingOnParagraphs'),
         (0x0400,'directFormattingOnNumbering'),(0x0800,'directFormattingOnTables'),
         (0x1000,'clearFormatting'),(0x2000,'top3HeadingStyles'),(0x4000,'visibleStyles'),(0x8000,'alternateStyleNames')]
NAMES = [n for _, n in ATTRS]

def bits(v):
    """All fifteen, in Word's order, as Word would write value v."""
    return [(n, '1' if v & b else '0') for b, n in ATTRS]

def el(pairs):
    return '<w:stylePaneFormatFilter' + ''.join(f' w:{k}="{x}"' for k, x in pairs) + '/>'

def val(v):
    return ('val', v)

P = {}  # name -> (element xml or None, mode)
def add(name, pairs, mode=15):
    assert name not in P
    P[name] = (None if pairs is None else el(pairs), mode)

# Controls
add('k-none', None)
add('k-agree-3F01', [val('3F01')] + bits(0x3F01))
add('k-agree-A5C2', [val('A5C2')] + bits(0xA5C2))
# w:val alone: does Word read it?
for v in ['0000'] + [f'{1 << i:04X}' for i in range(16)] + ['FFFF', '3F01', 'A5C2']:
    add(f'v-{v}', [val(v)])
# Attributes alone
add('a-empty', [])
for n in NAMES:
    add(f'a-{n}', [(n, '1')])
add('a-allStyles-0', [('allStyles', '0')])
add('a-all1', [(n, '1') for n in NAMES])
add('a-all0', [(n, '0') for n in NAMES])
add('a-as3F01', bits(0x3F01))
add('a-asA5C2', bits(0xA5C2))
# Both, disagreeing, w:val first as Word writes it
add('d-0000-all1', [val('0000')] + [(n, '1') for n in NAMES])
add('d-FFFF-all0', [val('FFFF')] + [(n, '0') for n in NAMES])
add('d-3F01-asA5C2', [val('3F01')] + bits(0xA5C2))
add('d-A5C2-as3F01', [val('A5C2')] + bits(0x3F01))
add('d-3F01-as3F00', [val('3F01')] + bits(0x3F00))
add('d-3F01-as3F03', [val('3F01')] + bits(0x3F03))
# w:val and some of the attributes: per bit, or all or nothing?
add('p-3F01-allStyles0', [val('3F01'), ('allStyles', '0')])
add('p-3F01-customStyles1', [val('3F01'), ('customStyles', '1')])
add('p-0000-allStyles1', [val('0000'), ('allStyles', '1')])
add('p-FFFF-allStyles0', [val('FFFF'), ('allStyles', '0')])
add('p-A5C2-low3F01', [val('A5C2')] + bits(0x3F01)[:7])
# Attribute order: w:val last, and in the middle
add('o-all1-0000', [(n, '1') for n in NAMES] + [val('0000')])
add('o-all0-FFFF', [(n, '0') for n in NAMES] + [val('FFFF')])
add('o-asA5C2-3F01', bits(0xA5C2) + [val('3F01')])
add('o-allStyles0-3F01', [('allStyles', '0'), val('3F01')])
add('o-mid-0000', [(n, '1') for n in NAMES[:7]] + [val('0000')] + [(n, '1') for n in NAMES[7:]])
# Compatibility mode: 12 is a file with no compatSetting
for m in (12, 14):
    add(f'm{m}-v-A5C2', [val('A5C2')], m)
    add(f'm{m}-d-3F01-asA5C2', [val('3F01')] + bits(0xA5C2), m)

REPEAT = ('d-', 'p-', 'o-', 'm1')

CT = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
      '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/>'
      '<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>'
      '<Override PartName="/word/settings.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml"/>'
      '<Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/></Types>')
RELS = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
        '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/></Relationships>')
DOCRELS = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
           '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings" Target="settings.xml"/>'
           '<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/></Relationships>')
STYLES = (f'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<w:styles xmlns:w="{W}">'
          '<w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/></w:style></w:styles>')

def document(name):
    return (f'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<w:document xmlns:w="{W}"><w:body><w:p><w:r><w:t>{name}</w:t></w:r></w:p>'
            '<w:sectPr><w:pgSz w:w="12240" w:h="15840"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="720" w:footer="720" w:gutter="0"/></w:sectPr>'
            '</w:body></w:document>')

def settings(element, mode):
    compat = '' if mode == 12 else ('<w:compat><w:compatSetting w:name="compatibilityMode" w:uri="http://schemas.microsoft.com/office/word" '
                                    f'w:val="{mode}"/></w:compat>')
    return (f'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n<w:settings xmlns:w="{W}"><w:zoom w:percent="100"/>{element or ""}'
            f'<w:defaultTabStop w:val="720"/><w:characterSpacingControl w:val="doNotCompress"/>{compat}</w:settings>')

def write(path, name, element, mode):
    with zipfile.ZipFile(path, 'w', zipfile.ZIP_DEFLATED) as z:
        for part, body in (('[Content_Types].xml', CT), ('_rels/.rels', RELS), ('word/document.xml', document(name)),
                           ('word/_rels/document.xml.rels', DOCRELS), ('word/settings.xml', settings(element, mode)), ('word/styles.xml', STYLES)):
            info = zipfile.ZipInfo(part, date_time=(2026, 11, 6, 0, 0, 0)); info.compress_type = zipfile.ZIP_DEFLATED
            z.writestr(info, body)

if __name__ == '__main__':
    out = sys.argv[1]
    os.makedirs(out + '/src', exist_ok=True)
    jobs = []
    for name, (element, mode) in P.items():
        f = f'f78-{name}'
        write(f'{out}/src/{f}.docx', f, element, mode)
        jobs.append(f'{f}-a')
        if name.startswith(REPEAT):
            jobs.append(f'{f}-b')
    json.dump({'probes': {f'f78-{k}': {'element': e, 'mode': m} for k, (e, m) in P.items()}, 'jobs': jobs}, open(out + '/probes.json', 'w'), indent=1)
    print(len(P), 'probes,', len(jobs), 'jobs')
