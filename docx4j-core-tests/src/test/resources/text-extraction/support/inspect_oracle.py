#!/usr/bin/env python3
# Copyright 2026, Plutext Pty Ltd.
#
# This file is part of docx4j.
#
# docx4j is licensed under the Apache License, Version 2.0; see
# http://www.apache.org/licenses/LICENSE-2.0
"""The reference generator of CR-050's inspection oracle.

It applies the rules of ../INSPECTION.md to the suite's fixtures and writes
phase0/expected-inspection.tsv (one row a finding) and phase0/expected-inspection-status.tsv
(one row a fixture). It is a second, independent reading of the contract, in Python (lxml,
fontTools), so that docx4j's inspector (CR-050 phase 1) and the ports' are compared with
something other than themselves; its output was reviewed by hand, case by case, before it was
committed. The extraction address column is left empty here: ExtractionInspection (Java) fills it
from docx4j's text model.

    python3 docx4j-core-tests/src/test/resources/text-extraction/support/inspect_oracle.py \
        docx4j-core-tests/src/test/resources/text-extraction/phase0
"""
import codecs
import io
import posixpath
import re
import sys
import unicodedata
import zipfile
from email import message_from_bytes

from fontTools import agl
from fontTools.ttLib import TTFont
from lxml import etree

FIXTURES = [
    'extraction-probe.docx', 'extraction-probe-external.docm', 'extraction-probe-structure.docx',
    'extraction-probe-inspect.docx']

PREFIX = {
    'http://schemas.openxmlformats.org/wordprocessingml/2006/main': 'w',
    'http://schemas.openxmlformats.org/officeDocument/2006/relationships': 'r',
    'http://schemas.openxmlformats.org/package/2006/relationships': 'rel',
    'http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing': 'wp',
    'http://schemas.openxmlformats.org/drawingml/2006/main': 'a',
    'http://schemas.openxmlformats.org/drawingml/2006/picture': 'pic',
    'http://schemas.openxmlformats.org/drawingml/2006/chart': 'c',
    'http://schemas.openxmlformats.org/drawingml/2006/diagram': 'dgm',
    'http://schemas.microsoft.com/office/word/2010/wordprocessingShape': 'wps',
    'http://schemas.microsoft.com/office/word/2010/wordprocessingGroup': 'wpg',
    'http://schemas.openxmlformats.org/markup-compatibility/2006': 'mc',
    'urn:schemas-microsoft-com:vml': 'v',
    'urn:schemas-microsoft-com:office:office': 'o',
    'urn:schemas-microsoft-com:office:word': 'w10',
    'http://schemas.microsoft.com/office/word/2010/wordml': 'w14',
    'http://schemas.microsoft.com/office/word/2012/wordml': 'w15',
    'http://schemas.openxmlformats.org/officeDocument/2006/math': 'm',
    'http://schemas.openxmlformats.org/spreadsheetml/2006/main': 'x',
    'http://schemas.openxmlformats.org/presentationml/2006/main': 'p',
    'http://schemas.openxmlformats.org/package/2006/metadata/core-properties': 'cp',
    'http://purl.org/dc/elements/1.1/': 'dc',
    'http://purl.org/dc/terms/': 'dcterms',
    'http://schemas.openxmlformats.org/officeDocument/2006/extended-properties': 'ep',
    'http://schemas.openxmlformats.org/officeDocument/2006/custom-properties': 'op',
    'http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes': 'vt',
    'http://schemas.microsoft.com/office/2006/xmlPackage': 'pkg',
}
W = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
R = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships'
MC = 'http://schemas.openxmlformats.org/markup-compatibility/2006'
WP = 'http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing'
A = 'http://schemas.openxmlformats.org/drawingml/2006/main'
DGM = 'http://schemas.openxmlformats.org/drawingml/2006/diagram'
DSP = 'http://schemas.microsoft.com/office/drawing/2008/diagram'
V = 'urn:schemas-microsoft-com:vml'
O = 'urn:schemas-microsoft-com:office:office'
M = 'http://schemas.openxmlformats.org/officeDocument/2006/math'
X = 'http://schemas.openxmlformats.org/spreadsheetml/2006/main'
P = 'http://schemas.openxmlformats.org/presentationml/2006/main'
W14 = 'http://schemas.microsoft.com/office/word/2010/wordml'
W15 = 'http://schemas.microsoft.com/office/word/2012/wordml'
REL = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships/'
DECORATIVE = 'http://schemas.microsoft.com/office/drawing/2017/decorative'
PIC = 'http://schemas.openxmlformats.org/drawingml/2006/picture'
WPS = 'http://schemas.microsoft.com/office/word/2010/wordprocessingShape'
OFFICE_DOC = REL + 'officeDocument'

TOKEN = re.compile(r'[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)+')

# the namespaces docx4j's reader understands for text (McSelection, understood-namespaces.txt)
UNDERSTOOD = set()

ACTIVE = {'field.external', 'settings.update_fields', 'relationship.external', 'part.macro', 'part.activex',
          'altchunk.script', 'altchunk.external_ref'}
INVENTORY = {'altchunk', 'object.embedded', 'font.embedded', 'ink', 'revision.author', 'rsids', 'people',
             'part.unknown', 'hyperlink.web'}
VISIBLE = {
    'text.near_background': 'differently', 'text.tiny': 'differently', 'chars.zero_width': 'no', 'chars.tag': 'no',
    'chars.bidi': 'no', 'chars.private_use': 'no', 'font.glyph_blank': 'no', 'font.glyph_mismatch': 'differently',
    'paragraph.collapsed': 'differently', 'shape.zero_size': 'no', 'shape.off_page': 'no', 'shape.hidden': 'no',
    'smartart.data': 'no', 'text.hidden': 'no', 'paragraph.hidden': 'no', 'revision.deleted': 'no',
    'revision.moved_from': 'no', 'mce.branch': 'no', 'story.unused': 'no', 'sdt.stored_surface': 'no',
    'field.code': 'no', 'glossary': 'no', 'alt_text.description': 'no', 'alt_text.title': 'no', 'alt_text.name': 'no',
    'embedded.sheet_hidden': 'no', 'embedded.sheet_very_hidden': 'no', 'embedded.slide_hidden': 'no',
    'embedded.row_hidden': 'no', 'embedded.column_hidden': 'no', 'embedded.notes': 'no', 'field.external': '-',
    'settings.update_fields': '-', 'relationship.external': '-', 'hyperlink.web': 'yes', 'part.macro': '-',
    'part.activex': '-', 'altchunk.script': '-', 'altchunk.external_ref': '-', 'comment': 'yes',
    'header_footer': 'yes', 'watermark': 'yes', 'note': 'yes', 'sdt.placeholder': 'yes', 'chart.cached': 'yes',
    'text.web_hidden': 'yes', 'altchunk': 'yes', 'object.embedded': 'yes', 'properties': 'no', 'font.embedded': '-',
    'ink': 'yes', 'doc_vars': 'no', 'custom_xml': 'no', 'people': 'no', 'revision.author': 'no', 'rsids': 'no',
    'part.unknown': '-',
}

# the named outputs of expected-inspection.tsv: the options each sets beyond the defaults, and its format
OUTPUTS = [('plain', set(), 'plain'), ('markdown', set(), 'markdown'), ('blocks', set(), 'blocks'),
           ('hidden', {'hidden'}, 'plain'), ('embedded', {'embedded'}, 'plain'), ('comments', {'comments'}, 'plain')]
# 'not:original' is inserted or moved-to content's (WD REQ-905; CR-050 phase 1): the defaults meet it
DEFAULT_ON = {'notes', 'text_boxes', 'placeholders', 'altchunks', 'charts', 'smartart', 'not:original'}
# option order, WD section 10; each need's option text in a reveal
REVEAL = [('view:original', 'view: original'), ('view:markup', 'view: markup'), ('include:headers', 'include: headers'),
          ('include:footers', 'include: footers'), ('include:glossary', 'include: glossary'),
          ('text_boxes', 'text_boxes: true'), ('notes', 'notes: true'), ('comments', 'comments: true'),
          ('hidden', 'hidden: true'), ('field_codes', 'field_codes: true'), ('toc', 'toc: true'),
          ('placeholders', 'placeholders: true'),
          ('bindings:surface', 'bindings: surface'), ('charts', 'charts: true'), ('smartart', 'smartart: true'),
          ('embedded', 'embedded: true'), ('altchunks', 'altchunks: true'), ('alt_text', 'alt_text: true'),
          ('metadata', 'metadata: true'), ('markup_detail', 'markup_detail: author_date'),
          ('html_reader', 'html reader: present')]

SYMBOL_FONTS = {'symbol', 'webdings', 'wingdings', 'wingdings 2', 'wingdings 3', 'marlett', 'mt extra',
                'bookshelf symbol 7', 'ms outlook', 'ms reference specialty', 'segoe mdl2 assets', 'segoe fluent icons'}
HIGHLIGHT = {'black': '000000', 'blue': '0000FF', 'cyan': '00FFFF', 'green': '00FF00', 'magenta': 'FF00FF',
             'red': 'FF0000', 'yellow': 'FFFF00', 'white': 'FFFFFF', 'darkBlue': '000080', 'darkCyan': '008080',
             'darkGreen': '008000', 'darkMagenta': '800080', 'darkRed': '800000', 'darkYellow': '808000',
             'darkGray': '808080', 'lightGray': 'C0C0C0'}
KNOWN_TYPES = ('application/vnd.openxmlformats-', 'application/vnd.ms-', 'image/', 'audio/', 'video/', 'text/',
               'message/rfc822', 'application/xml', 'application/xhtml+xml', 'application/rtf', 'application/inkml+xml', 'font/',
               'application/x-font', 'application/pdf', 'application/vnd.oasis.opendocument.')
EMU_PER_PT = 12700
# D19 (b): a name of this form ("Text Box 3", "Picture 7") is no finding unless extraction carries it
DEFAULT_NAME = re.compile(r'[^0-9]+ [0-9]+')
WHITE = 'FFFFFF'


# ---------------------------------------------------------------- small helpers

def q(el):
    ns, _, local = el.tag[1:].partition('}')
    return PREFIX.get(ns, ns) + ':' + local


def reach_rank(needs, story):
    """A note's references in order of preference (REQ-905; phase 2): the fewest options beyond the defaults;
    then a live reference before an inserted one, an inserted before a deleted, a deleted before one deleted
    inside an insertion; then the main document before a header before a footer; then the fewest needs. The
    first met in document order wins a tie."""
    revision = 3 if 'view:markup' in needs else 2 if 'view:original' in needs else 1 if 'not:original' in needs else 0
    return (len(needs - DEFAULT_ON), revision, ('main', 'header', 'footer').index(story), len(needs))


def paragraph_of(r):
    return next((a for a in r.iterancestors() if a.tag in (wq('p'), '{%s}p' % A)), None)


def path_of(el):
    steps = []
    while el is not None:
        parent = el.getparent()
        n = 1
        if parent is not None:
            for s in parent:
                if s is el:
                    break
                if isinstance(s.tag, str) and s.tag == el.tag:
                    n += 1
        steps.append('/%s[%d]' % (q(el), n))
        el = parent
    return ''.join(reversed(steps))


def on(el, attr='{%s}val' % W):
    """An ST_OnOff element: present and not false"""
    if el is None:
        return None
    v = el.get(attr)
    return v is None or v.lower() not in ('0', 'false', 'off')


def cps(s):
    return len(s)  # Python strings are code points


def wq(local):
    return '{%s}%s' % (W, local)


def luminance(hexrgb):
    def lin(c):
        c = c / 255
        return c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4
    r, g, b = (int(hexrgb[i:i + 2], 16) for i in (0, 2, 4))
    return 0.2126 * lin(r) + 0.7152 * lin(g) + 0.0722 * lin(b)


def contrast(a, b):
    la, lb = sorted((luminance(a), luminance(b)), reverse=True)
    return (la + 0.05) / (lb + 0.05)


def zero_width(c):
    return 0x200B <= ord(c) <= 0x200F or ord(c) in (0x2060, 0xFEFF)


def tag(c):
    return 0xE0000 <= ord(c) <= 0xE007F


def bidi(c):
    return 0x202A <= ord(c) <= 0x202E or 0x2066 <= ord(c) <= 0x2069


def private_use(c):
    o = ord(c)
    return 0xE000 <= o <= 0xF8FF or 0xF0000 <= o <= 0xFFFFD or 0x100000 <= o <= 0x10FFFD


def complex_script(c):
    o = ord(c)
    return 0x0590 <= o <= 0x08FF or 0x0900 <= o <= 0x0DFF or 0x0E00 <= o <= 0x0EFF or 0xFB1D <= o <= 0xFDFF \
        or 0xFE70 <= o <= 0xFEFF


def symbol_font(name):
    if not name:
        return False
    n = name.strip().lower()
    return n in SYMBOL_FONTS or n.startswith('font awesome') or n.startswith('fontawesome')


def length_unit(value):
    """A VML length in points"""
    m = re.fullmatch(r'\s*(-?[0-9.]+)\s*(pt|in|cm|mm|px|emu)?\s*', value or '')
    if not m:
        return None
    n = float(m.group(1))
    return n * {'pt': 1, None: 0.75, 'in': 72, 'cm': 72 / 2.54, 'mm': 72 / 25.4, 'px': 0.75,
                'emu': 1 / EMU_PER_PT}[m.group(2)]


def vml_style(el):
    style = {}
    for item in (el.get('style') or '').split(';'):
        if ':' in item:
            k, v = item.split(':', 1)
            style[k.strip().lower()] = v.strip()
    return style


# ---------------------------------------------------------------- the package

class Package:
    def __init__(self, data, chain):
        self.zip = zipfile.ZipFile(io.BytesIO(data))
        self.names = {n for n in self.zip.namelist()}
        self.chain = chain
        self.types = {}
        self.defaults = {}
        ct = etree.fromstring(self.zip.read('[Content_Types].xml'))
        for e in ct:
            if not isinstance(e.tag, str):
                continue
            if e.tag.endswith('Override'):
                self.types[e.get('PartName')] = e.get('ContentType')
            elif e.tag.endswith('Default'):
                self.defaults[e.get('Extension').lower()] = e.get('ContentType')
        self._xml = {}

    def content_type(self, part):
        if part in self.types:
            return self.types[part]
        return self.defaults.get(part.rsplit('.', 1)[-1].lower())

    def has(self, part):
        return part.lstrip('/') in self.names

    def read(self, part):
        return self.zip.read(part.lstrip('/'))

    def xml(self, part):
        if part not in self._xml:
            self._xml[part] = etree.fromstring(self.read(part)) if self.has(part) else None
        return self._xml[part]

    def rels_part(self, part):
        d, f = posixpath.split(part)
        return posixpath.join(d, '_rels', f + '.rels') if part != '/' else '/_rels/.rels'

    def rels(self, part):
        """[(id, type, target, external, element)] of a part ('/' the package)"""
        rp = self.rels_part(part)
        root = self.xml(rp)
        out = []
        if root is None:
            return out
        base = posixpath.dirname(part) if part != '/' else '/'
        for e in root:
            if not isinstance(e.tag, str):
                continue
            external = e.get('TargetMode') == 'External'
            target = e.get('Target')
            if not external:
                target = target if target.startswith('/') else posixpath.normpath(posixpath.join(base, target))
            out.append((e.get('Id'), e.get('Type'), target, external, e))
        return out

    def target(self, part, rid):
        for i, t, target, external, e in self.rels(part):
            if i == rid:
                return target
        return None

    def by_type(self, part, suffix):
        return [target for i, t, target, external, e in self.rels(part) if t.endswith(suffix) and not external]

    def main(self):
        for i, t, target, external, e in self.rels('/'):
            if t == OFFICE_DOC or t.endswith('/officeDocument'):
                return target
        return None


# ---------------------------------------------------------------- findings

class Finding:
    def __init__(self, kind, chain, part, path='', start='', end_path='', end='', excerpt=None, needs=(),
                 visible=None, selector=None, summary=''):
        self.kind, self.chain, self.part, self.path = kind, chain, part, path
        self.start, self.end_path, self.end = start, end_path, end
        self.excerpt = excerpt
        self.needs = set(needs)
        self.visible = visible or VISIBLE[kind]
        self.selector = selector
        self.summary = summary

    def key(self):
        return (self.kind, self.chain, self.part, self.path, str(self.start), self.end_path, str(self.end))

    def member(self, options, fmt):
        if 'never' in self.needs:
            return False
        for need in self.needs:
            if need in DEFAULT_ON:
                continue
            if need == 'alt_text':
                if fmt == 'plain' and 'alt_text' not in options:
                    return False
            elif need == 'metadata':
                if fmt != 'blocks' and 'metadata' not in options:
                    return False
            elif need == 'comments':
                if 'comments' not in options:
                    return False
            elif need not in options:
                return False
        return True

    def group(self, options, fmt):
        if self.kind in ACTIVE:
            return 'active'
        if self.kind in INVENTORY:
            return 'info'
        inside = self.member(options, fmt)
        text = len(self.excerpt or '') > 0
        if self.kind == 'properties':
            return 'info' if inside else 'omitted'
        if inside and self.visible in ('no', 'differently', 'indeterminate'):
            return 'unseen'
        if not inside and text:
            return 'omitted'
        return 'info'

    def reveal(self):
        if 'never' in self.needs:
            return 'unextractable'
        needed = [text for need, text in REVEAL if need in self.needs and need not in DEFAULT_ON]
        return 'options: ' + '; '.join(needed) if needed else 'included'


# ---------------------------------------------------------------- property resolution (WordprocessingML)

class Styles:
    def __init__(self, root):
        self.styles = {}
        self.default_rpr = None
        self.default_ppr = None
        self.default_para = None
        if root is None:
            return
        dd = root.find(wq('docDefaults'))
        if dd is not None:
            self.default_rpr = dd.find('%s/%s' % (wq('rPrDefault'), wq('rPr')))
            self.default_ppr = dd.find('%s/%s' % (wq('pPrDefault'), wq('pPr')))
        for s in root.findall(wq('style')):
            self.styles[s.get(wq('styleId'))] = s
            if s.get(wq('type')) == 'paragraph' and on(s, wq('default')) and s.get(wq('default')) is not None:
                self.default_para = s.get(wq('styleId'))

    def chain(self, sid):
        out, seen = [], set()
        while sid and sid in self.styles and sid not in seen:
            seen.add(sid)
            out.append(self.styles[sid])
            b = self.styles[sid].find(wq('basedOn'))
            sid = b.get(wq('val')) if b is not None else None
        return list(reversed(out))  # base first


def rpr_layers(styles, p, r, table_style=None):
    """The rPr elements that apply to a run, weakest first"""
    layers = []
    if styles.default_rpr is not None:
        layers.append(styles.default_rpr)
    if table_style:
        for s in styles.chain(table_style):
            x = s.find(wq('rPr'))
            if x is not None:
                layers.append(x)
    pstyle = None
    if p is not None:
        ppr = p.find(wq('pPr'))
        ps = ppr.find(wq('pStyle')) if ppr is not None else None
        pstyle = ps.get(wq('val')) if ps is not None else styles.default_para
    for s in styles.chain(pstyle):
        x = s.find(wq('rPr'))
        if x is not None:
            layers.append(x)
    rpr = r.find(wq('rPr')) if r is not None else None
    if rpr is not None:
        rs = rpr.find(wq('rStyle'))
        if rs is not None:
            for s in styles.chain(rs.get(wq('val'))):
                x = s.find(wq('rPr'))
                if x is not None:
                    layers.append(x)
        layers.append(rpr)
    return layers


def mark_layers(styles, p):
    layers = []
    if styles.default_rpr is not None:
        layers.append(styles.default_rpr)
    ppr = p.find(wq('pPr'))
    ps = ppr.find(wq('pStyle')) if ppr is not None else None
    for s in styles.chain(ps.get(wq('val')) if ps is not None else styles.default_para):
        x = s.find(wq('rPr'))
        if x is not None:
            layers.append(x)
    if ppr is not None and ppr.find(wq('rPr')) is not None:
        layers.append(ppr.find(wq('rPr')))
    return layers


def prop(layers, local):
    found = None
    for layer in layers:
        e = layer.find(wq(local))
        if e is not None:
            found = e
    return found


def ppr_prop(styles, p, local):
    found = None
    if styles.default_ppr is not None and styles.default_ppr.find(wq(local)) is not None:
        found = styles.default_ppr.find(wq(local))
    ppr = p.find(wq('pPr'))
    ps = ppr.find(wq('pStyle')) if ppr is not None else None
    for s in styles.chain(ps.get(wq('val')) if ps is not None else styles.default_para):
        x = s.find(wq('pPr'))
        if x is not None and x.find(wq(local)) is not None:
            found = x.find(wq(local))
    if ppr is not None and ppr.find(wq(local)) is not None:
        found = ppr.find(wq(local))
    return found


def shading(shd):
    """(colour, decided) of a w:shd: None for no fill"""
    if shd is None:
        return None, True
    val = (shd.get(wq('val')) or 'clear')
    fill = shd.get(wq('fill'))
    if val == 'nil':
        return None, True
    if val == 'clear':
        if fill is None or fill.lower() == 'auto':
            return None, True
        return fill.upper(), True
    if val == 'solid':
        colour = shd.get(wq('color'))
        if colour is None or colour.lower() == 'auto':
            return None, False
        return colour.upper(), True
    return None, False   # a pattern: a mix docx4j cannot determine


# ---------------------------------------------------------------- the walk of one package

class Inspector:
    def __init__(self, pkg, out, status, depth=0, needs=()):
        self.pkg = pkg
        self.chain = pkg.chain
        self.out = out
        self.status = status
        self.depth = depth
        self.base_needs = set(needs)
        self.main = pkg.main()
        self.styles = Styles(self._styles_root())
        self.faces = {}
        self.used_faces = {}
        self.authors = {}

    def _styles_root(self):
        if self.main is None:
            return None
        st = self.pkg.by_type(self.main, '/styles')
        return self.pkg.xml(st[0]) if st else None

    def add(self, f):
        f.needs |= self.base_needs
        self.out.append(f)
        return f

    # ------------------------------------------------------------ the document

    def run(self):
        main = self.main
        ctype = self.pkg.content_type(main) if main else None
        if main is None:
            return
        if 'wordprocessingml' in (ctype or '') or 'ms-word' in (ctype or ''):
            self.word()
        elif 'spreadsheetml' in (ctype or ''):
            self.workbook(main)
        elif 'presentationml' in (ctype or ''):
            self.presentation(main)
        self.inventory()

    def word(self):
        main = self.main
        doc = self.pkg.xml(main)
        self.page_colour = None
        self.page_shown = False
        st = self.pkg.by_type(main, '/settings')
        if st and self.pkg.xml(st[0]).find(wq('displayBackgroundShape')) is not None:
            self.page_shown = on(self.pkg.xml(st[0]).find(wq('displayBackgroundShape')))
        bg = doc.find(wq('background'))
        if bg is not None and bg.get(wq('color')) and bg.get(wq('color')).lower() != 'auto':
            self.page_colour = bg.get(wq('color')).upper()
        self.load_faces()
        self.load_bindings()
        # sections: the header and footer parts they use, and each paragraph's page
        self.sections = []
        body = doc.find(wq('body'))
        used = {}
        for sp in doc.iter(wq('sectPr')):
            self.sections.append(sp)
            for ref in list(sp.findall(wq('headerReference'))) + list(sp.findall(wq('footerReference'))):
                t = self.pkg.target(main, ref.get('{%s}id' % R))
                if t:
                    used[t] = 'header' if ref.tag.endswith('headerReference') else 'footer'
        self.used = used
        refs = {'footnote': {}, 'endnote': {}, 'comment': set()}
        self.collect_references(main, doc, refs, 'main')
        for part in self.pkg.by_type(main, '/header') + self.pkg.by_type(main, '/footer'):
            x = self.pkg.xml(part)
            if x is not None:
                self.collect_references(part, x, refs, used.get(part))
        # a comment anchored in a note is anchored (phase 2)
        for part in self.pkg.by_type(main, '/footnotes') + self.pkg.by_type(main, '/endnotes'):
            x = self.pkg.xml(part)
            if x is not None:
                self.collect_references(part, x, refs, None)
        self.refs = refs
        self.authors = {}

        self.story(main, doc, needs=set(), story='main')
        for part in sorted(self.pkg.by_type(main, '/header') + self.pkg.by_type(main, '/footer')):
            x = self.pkg.xml(part)
            if part in used:
                f = self.add(Finding('header_footer', self.chain, part, excerpt=self.text(x),
                                     needs={'include:headers' if used[part] == 'header' else 'include:footers'}))
                self.story(part, x, needs=set(f.needs), story=used[part])
            else:
                self.add(Finding('story.unused', self.chain, part, excerpt=self.text(x), needs={'never'}))
                self.story(part, x, needs={'never'}, story='unused')
        for kind in ('footnote', 'endnote'):
            for part in self.pkg.by_type(main, '/%ss' % kind):
                x = self.pkg.xml(part)
                for note in x.findall(wq(kind)):
                    t = note.get(wq('type'))
                    if t not in (None, 'normal'):
                        continue
                    nid = note.get(wq('id'))
                    if nid not in refs[kind]:
                        self.add(Finding('story.unused', self.chain, part, path_of(note), excerpt=self.text(note), needs={'never'}))
                        self.story(part, note, needs={'never'}, story='unused')
                    else:
                        # read through its best reference: what reading that reference needs (phase 2)
                        needs = {'notes'} | refs[kind][nid][1]
                        self.add(Finding('note', self.chain, part, path_of(note), excerpt=self.text(note), needs=needs))
                        self.story(part, note, needs=needs, story='note')
        for part in self.pkg.by_type(main, '/comments'):
            x = self.pkg.xml(part)
            for c in x.findall(wq('comment')):
                if c.get(wq('id')) in refs['comment']:
                    self.add(Finding('comment', self.chain, part, path_of(c), excerpt=self.text(c), needs={'comments'},
                                     summary=c.get(wq('author')) or ''))
                    self.story(part, c, needs={'comments'}, story='comment')
                else:
                    self.add(Finding('story.unused', self.chain, part, path_of(c), excerpt=self.text(c), needs={'never'}))
                    self.story(part, c, needs={'never'}, story='unused')
        for part in self.pkg.by_type(main, '/glossaryDocument'):
            x = self.pkg.xml(part)
            for dp in x.iter(wq('docPart')):
                body = dp.find(wq('docPartBody'))
                self.add(Finding('glossary', self.chain, part, path_of(dp), excerpt=self.text(body), needs={'include:glossary'}))
                self.story(part, body, needs={'include:glossary'}, story='glossary')
        for author, (first, dates) in self.authors.items():
            f = first
            self.add(Finding('revision.author', self.chain, f[0], path_of(f[1]), excerpt=author, selector=author,
                             needs={'view:markup', 'markup_detail'},
                             summary=(min(dates) + ' to ' + max(dates)) if dates else ''))
        settings = self.pkg.by_type(main, '/settings')
        if settings:
            s = self.pkg.xml(settings[0])
            dv = s.find(wq('docVars'))
            if dv is not None and len(dv):
                vals = [d.get(wq('val')) or '' for d in dv.findall(wq('docVar'))]
                self.add(Finding('doc_vars', self.chain, settings[0], path_of(dv), excerpt='\n'.join(vals), needs={'never'}))
            uf = s.find(wq('updateFields'))
            if on(uf):
                self.add(Finding('settings.update_fields', self.chain, settings[0], path_of(uf), excerpt='', needs={'never'}))
            rs = s.find(wq('rsids'))
            if rs is not None:
                self.add(Finding('rsids', self.chain, settings[0], path_of(rs), excerpt='', needs={'never'},
                                 summary=str(len(rs))))
        for face, (part, first) in self.used_faces.items():
            f = self.add(Finding('font.embedded', self.chain, part, excerpt='', needs={'never'}, selector=face))
            f.anchor = first

    def collect_references(self, part, root, refs, story):
        """A story's note and comment references (phase 2). The text model reads a note through a reference it
        reads: in the main document, or in a header or footer a section uses (story None for one none does, whose
        references reach no note), where the reference itself is read. Each reference needs what reading it needs:
        its story's option, its revision's view, a text box, hidden text, a TOC, a stale control's surface; one in
        an unselected branch reaches nothing. refs[kind][id] keeps the best reference's (rank, needs): reach_rank"""
        if story:
            saved = (self.part, getattr(self, 'toc_runs', set()))
            self.part, self.toc_runs = part, self.toc_spans(part, root)
            base = {'header': {'include:headers'}, 'footer': {'include:footers'}}.get(story, set())
            for e in root.iter(wq('footnoteReference'), wq('endnoteReference')):
                kind = 'footnote' if e.tag.endswith('footnoteReference') else 'endnote'
                n = self.context_needs(e, root, base)
                if 'never' in n:
                    continue
                r = next((a for a in e.iterancestors() if a.tag == wq('r')), None)
                if r is not None and on(prop(self.layers(paragraph_of(r), r), 'vanish')):
                    n.add('hidden')
                nid = e.get(wq('id'))
                rank = reach_rank(n, story)
                was = refs[kind].get(nid)
                if was is None or rank < was[0]:
                    refs[kind][nid] = (rank, n)
            self.part, self.toc_runs = saved
        for e in root.iter(wq('commentReference'), wq('commentRangeStart')):
            refs['comment'].add(e.get(wq('id')))

    # ------------------------------------------------------------ text

    def run_text(self, r):
        """A run's stored text: w:t, w:delText, w:instrText, w:delInstrText, w:sym (one code point), m:t"""
        out = []
        for c in r:
            if not isinstance(c.tag, str):
                continue
            if c.tag in (wq('t'), wq('delText'), wq('instrText'), wq('delInstrText'), '{%s}t' % M):
                out.append(c.text or '')
            elif c.tag == wq('sym'):
                out.append(chr(int(c.get(wq('char')), 16)))
        return ''.join(out)

    def display_text(self, r):
        out = []
        for c in r:
            if not isinstance(c.tag, str):
                continue
            if c.tag in (wq('t'), wq('delText'), '{%s}t' % M):
                out.append(c.text or '')
            elif c.tag == wq('sym'):
                out.append(chr(int(c.get(wq('char')), 16)))
        return ''.join(out)

    def own_runs(self, p):
        """The runs whose nearest paragraph is p, in document order, skipping unselected branches relative to p"""
        out = []
        for r in p.iter(wq('r'), '{%s}r' % M):
            near = next((a for a in r.iterancestors() if a.tag in (wq('p'), '{%s}p' % A)), None)
            if near is not p:
                continue
            if self.unselected_between(r, p):
                continue
            out.append(r)
        return out

    def text(self, el):
        """An element's text: each paragraph's display text (selected branches), paragraphs joined by LF; an
        element inside a paragraph (a branch, a control's content) its runs' display text"""
        if el is None:
            return ''
        if el.tag == wq('p'):
            candidates = [el] + list(el.iter(wq('p')))[1:]
        else:
            candidates = list(el.iter(wq('p')))
        if not candidates:
            # inside a paragraph or a run: the text-bearing elements themselves
            out = []
            for t in el.iter(wq('t'), wq('delText'), '{%s}t' % M, wq('sym')):
                if self.unselected_between(t, el):
                    continue
                out.append(chr(int(t.get(wq('char')), 16)) if t.tag == wq('sym') else (t.text or ''))
            return ''.join(out)
        paras = []
        for p in candidates:
            if self.unselected_between(p, el):
                continue
            paras.append(''.join(self.display_text(r) for r in self.own_runs(p)))
        return '\n'.join(paras)

    def unselected_between(self, el, top):
        """Whether el lies in a branch its AlternateContent does not select, below top"""
        for a in el.iterancestors():
            if a is top:
                return False
            if a.tag in ('{%s}Choice' % MC, '{%s}Fallback' % MC) and not self.selected(a):
                return True
        return False

    def selected(self, branch):
        ac = branch.getparent()
        chosen = None
        for c in ac:
            if not isinstance(c.tag, str):
                continue
            if c.tag == '{%s}Choice' % MC and chosen is None:
                req = (c.get('Requires') or '').split()
                if all(c.nsmap.get(p) in UNDERSTOOD for p in req):
                    chosen = c
        if chosen is None:
            chosen = ac.find('{%s}Fallback' % MC)
        if chosen is None:
            chosen = ac.find('{%s}Choice' % MC)
        return chosen is branch

    # ------------------------------------------------------------ a story

    def story(self, part, root, needs, story):
        self.part = part
        self.story_kind = story
        self.toc_runs = self.toc_spans(part, root)
        self.walk_branches(part, root, needs)
        self.walk_paragraphs(part, root, needs)
        self.walk_fields(part, root, needs)
        self.walk_drawings(part, root, needs)
        self.walk_controls(part, root, needs)
        self.walk_revisions(part, root, needs)
        self.walk_collapsed(part, root, needs)

    def toc_spans(self, part, root):
        """The runs of every TOC field, begin to end, its result and nested fields included (REQ-520: toc: true)"""
        runs = list(root.iter(wq('r')))
        spans, stack = set(), []
        for i, r in enumerate(runs):
            for c in r:
                if c.tag == wq('fldChar'):
                    t = c.get(wq('fldCharType'))
                    if t == 'begin':
                        stack.append([i, ''])
                    elif t == 'end' and stack:
                        start, instr = stack.pop()
                        if instr.split()[:1] and instr.split()[0].upper() == 'TOC':
                            spans.update((part, path_of(x)) for x in runs[start:i + 1])
                elif c.tag in (wq('instrText'), wq('delInstrText')) and stack:
                    stack[-1][1] += c.text or ''
        return spans

    def context_needs(self, el, top, needs):
        """The needs of an element from the containers between it and the story root"""
        n = set(needs)
        inserted = deleted = False
        for a in [el] + list(el.iterancestors()):
            if a is top:
                break
            if a.tag == wq('r') and (self.part, path_of(a)) in getattr(self, 'toc_runs', ()):
                n.add('toc')
            if a.tag in ('{%s}Choice' % MC, '{%s}Fallback' % MC) and not self.selected(a):
                n.add('never')
            elif a.tag in (wq('ins'), wq('moveTo')):
                inserted = True
            elif a.tag in (wq('del'), wq('moveFrom')):
                deleted = True
            elif a.tag == wq('tr'):
                # a row's own mark: Accept All removes a deleted row, Reject All an inserted one (phase 1)
                deleted |= a.find('%s/%s' % (wq('trPr'), wq('del'))) is not None
                inserted |= a.find('%s/%s' % (wq('trPr'), wq('ins'))) is not None
            elif a.tag == wq('tc'):
                deleted |= a.find('%s/%s' % (wq('tcPr'), wq('cellDel'))) is not None
                inserted |= a.find('%s/%s' % (wq('tcPr'), wq('cellIns'))) is not None
            elif a.tag == wq('sdtContent'):
                if (self.part, path_of(a.getparent())) in self.stale:
                    n.add('bindings:surface')
                if on(a.getparent().find('%s/%s' % (wq('sdtPr'), wq('showingPlcHdr')))):
                    n.add('placeholders')   # placeholder content: placeholders: true (phase 1)
            elif a.tag == wq('txbxContent'):
                n.add('text_boxes')
            elif a.tag == '{%s}textpath' % V:
                n.add('text_boxes')
        if deleted:
            n.add('view:markup' if inserted else 'view:original')   # deleted inside an insertion: markup only
        elif inserted:
            n.add('not:original')   # inserted content: not the original view (phase 1)
        return n

    def walk_branches(self, part, root, needs):
        for ac in root.iter('{%s}AlternateContent' % MC):
            if self.unselected_between(ac, root):
                continue   # the outermost unselected branch is the finding
            for b in ac:
                if not isinstance(b.tag, str) or self.selected(b):
                    continue
                self.add(Finding('mce.branch', self.chain, part, path_of(b), excerpt=self.text(b),
                                 needs=self.context_needs(b, root, needs) | {'never'}))

    # paragraphs: hidden, formatting, characters, fonts
    def walk_paragraphs(self, part, root, needs):
        paras = [root] if root.tag == wq('p') else []
        paras += [p for p in root.iter(wq('p'))]
        seen = set()
        for p in paras:
            if id(p) in seen:
                continue
            seen.add(id(p))
            pneeds = self.context_needs(p, root, needs)
            runs = [r for r in self.own_runs_all(p)]
            disp = [(r, self.display_text(r)) for r in runs]
            for r, t in disp:
                if t and t != self.run_text(r):
                    raise ValueError('a run with both display and instruction text: ' + path_of(r))
            texts = [(r, t) for r, t in disp if t]
            mark_hidden = on(prop(mark_layers(self.styles, p), 'vanish'))
            hidden_runs = {id(r): bool(on(prop(self.layers(p, r), 'vanish'))) for r, t in texts}
            whole = mark_hidden and texts and all(hidden_runs[id(r)] for r, t in texts)
            if whole:
                self.add(Finding('paragraph.hidden', self.chain, part, path_of(p), excerpt=''.join(t for r, t in texts),
                                 needs=pneeds | {'hidden'}))
            # run kinds, merged across adjacent runs of equal attributes
            groups = {}
            for r, t in texts:
                rn = self.context_needs(r, root, needs)
                layers = self.layers(p, r)
                hid = hidden_runs[id(r)]
                inner = rn | ({'hidden'} if hid or whole else set())
                if hid and not whole:
                    self.emit_run(groups, 'text.hidden', r, t, 0, cps(t), rn | {'hidden'}, ())
                if on(prop(layers, 'webHidden')):
                    self.emit_run(groups, 'text.web_hidden', r, t, 0, cps(t), inner, ())
                if t.strip():
                    nb = self.near_background(p, r, layers)
                    if nb is not None:
                        decided, near, colour, background, page = nb
                        if not decided:
                            self.status['undecided'].append('background.indeterminate@' + self.loc(part, path_of(r)))
                        elif near:
                            self.emit_run(groups, 'text.near_background', r, t, 0, cps(t), inner, (colour, background),
                                          visible='indeterminate' if page and not self.page_shown else None)
                    tiny = self.tiny(r, t, layers)
                    if tiny:
                        self.emit_run(groups, 'text.tiny', r, t, 0, cps(t), inner, (tiny,))
                for kind, test in (('chars.zero_width', zero_width), ('chars.tag', tag), ('chars.bidi', bidi)):
                    idx = [i for i, c in enumerate(t) if test(c)]
                    if idx:
                        self.emit_run(groups, kind, r, t, idx[0], idx[-1] + 1, inner, ())
                pua = [i for i, c in enumerate(t) if private_use(c) and not self.symbol_char(p, r, c)]
                if pua:
                    self.emit_run(groups, 'chars.private_use', r, t, pua[0], pua[-1] + 1, inner, ())
                self.font_tests(groups, p, r, t, layers, inner)
            for g in groups.values():
                for f in g:
                    self.add(f)

    def own_runs_all(self, p):
        out = []
        for r in p.iter(wq('r'), '{%s}r' % M):
            near = next((a for a in r.iterancestors() if a.tag in (wq('p'), '{%s}p' % A)), None)
            if near is p:
                out.append(r)
        return out

    def layers(self, p, r):
        return rpr_layers(self.styles, p, r, self.table_style(p))

    def table_style(self, p):
        tc = next((a for a in p.iterancestors() if a.tag == wq('tc')), None)
        if tc is None:
            return None
        tbl = next((a for a in tc.iterancestors() if a.tag == wq('tbl')), None)
        ts = tbl.find('%s/%s' % (wq('tblPr'), wq('tblStyle'))) if tbl is not None else None
        return ts.get(wq('val')) if ts is not None else None

    def emit_run(self, groups, kind, r, t, start, end, needs, attrs, visible=None):
        """A run finding, merged with the previous one of its kind when the runs are adjacent in one paragraph
        and every attribute the finding reports is equal (INSPECTION.md, "Identity")"""
        lst = groups.setdefault(kind, [])
        f = lst[-1] if lst else None
        para = self.paragraph_of(r)
        index = self.run_index(r)
        if f is not None and f._attrs == (attrs, frozenset(needs), visible) and f._para is para and f._last_index == index - 1:
            f.end_path = path_of(r)
            f.end = end
            f._texts.append(t)
            f.excerpt = f._texts[0][f.start:] + ''.join(f._texts[1:-1]) + t[:end]
            f._last_index = index
            return
        f = Finding(kind, self.chain, self.part, path_of(r), start, '', end, excerpt=t[start:end], needs=needs, visible=visible)
        f._attrs = (attrs, frozenset(needs), visible)
        f._para = para
        f._last_index = index
        f._texts = [t]
        lst.append(f)

    def paragraph_of(self, r):
        return next((a for a in r.iterancestors() if a.tag in (wq('p'), '{%s}p' % A)), None)

    def run_index(self, r):
        p = self.paragraph_of(r)
        runs = [x for x in self.own_runs_all(p) if self.display_text(x)]
        for i, x in enumerate(runs):
            if x is r:
                return i
        return -10

    def near_background(self, p, r, layers):
        colour = prop(layers, 'color')
        if colour is None:
            return None
        val = colour.get(wq('val'))
        if not val or val.lower() == 'auto':
            return None
        colour = val.upper()
        background, decided, page = None, True, False
        hl = prop(layers, 'highlight')
        if hl is not None and hl.get(wq('val')) not in (None, 'none'):
            background = HIGHLIGHT.get(hl.get(wq('val')))
        if background is None:
            background, decided = shading(prop(layers, 'shd'))
        if background is None and decided:
            background, decided = shading(ppr_prop(self.styles, p, 'shd'))
        if background is None and decided:
            tc = next((a for a in p.iterancestors() if a.tag == wq('tc')), None)
            if tc is not None:
                tcpr = tc.find(wq('tcPr'))
                if tcpr is not None and tcpr.find(wq('shd')) is not None:
                    background, decided = shading(tcpr.find(wq('shd')))
                if background is None and decided and self.table_style(p):
                    for s in self.styles.chain(self.table_style(p)):
                        x = s.find(wq('tcPr'))
                        if x is not None and x.find(wq('shd')) is not None:
                            background, decided = shading(x.find(wq('shd')))
        if background is None and decided:
            box = self.box_fill(r)
            if box is not None:
                background, decided = box
        if background is None and decided:
            if self.page_colour:
                background, page = self.page_colour, True
            else:
                background = WHITE
        if not decided:
            return (False, False, colour, None, False)
        return (True, contrast(colour, background) < 1.5, colour, background, page)

    def box_fill(self, r):
        for a in r.iterancestors():
            if a.tag == '{%s}wsp' % WPS:
                sp = a.find('{%s}spPr' % WPS)
                if sp is None:
                    return None
                solid = sp.find('{%s}solidFill/{%s}srgbClr' % (A, A))
                if solid is not None:
                    return solid.get('val').upper(), True
                if sp.find('{%s}noFill' % A) is not None:
                    return None
                if sp.find('{%s}gradFill' % A) is not None or sp.find('{%s}blipFill' % A) is not None \
                        or sp.find('{%s}pattFill' % A) is not None:
                    return None, False
                return None
            if a.tag in ('{%s}shape' % V, '{%s}rect' % V):
                if (a.get('filled') or 't').lower() in ('f', 'false'):
                    return None
                fc = a.get('fillcolor')
                if fc and re.fullmatch(r'#?[0-9a-fA-F]{6}', fc):
                    return fc.lstrip('#').upper(), True
                return WHITE, True
        return None

    def tiny(self, r, t, layers):
        cs = on(prop(layers, 'cs')) or on(prop(layers, 'rtl'))
        sz = prop(layers, 'sz')
        szcs = prop(layers, 'szCs')
        size = int(sz.get(wq('val'))) if sz is not None else 20
        sizecs = int(szcs.get(wq('val'))) if szcs is not None else 20
        drawn = [sizecs if cs or complex_script(c) else size for c in t if not c.isspace()]
        small = [s for s in drawn if s < 4]
        return min(small) if small and len(small) == len(drawn) else None

    def font_of(self, p, r, c, layers):
        if r.tag == wq('r'):
            sym = r.find(wq('sym'))
            if sym is not None and chr(int(sym.get(wq('char')), 16)) == c:
                return sym.get(wq('font'))
        f = prop(layers, 'rFonts')
        if f is None:
            return None
        if complex_script(c) or on(prop(layers, 'cs')) or on(prop(layers, 'rtl')):
            return f.get(wq('cs'))
        if ord(c) < 0x80:
            return f.get(wq('ascii'))
        return f.get(wq('hAnsi'))

    def symbol_char(self, p, r, c):
        """D17 (c), Jason 19 October 2026: the documented list alone (Word 365 did not draw an embedded symbol face)"""
        return symbol_font(self.font_of(p, r, c, self.layers(p, r)))

    # ------------------------------------------------------------ embedded fonts (D17, D18)

    def load_faces(self):
        self.faces = {}
        ft = self.pkg.by_type(self.main, '/fontTable')
        if not ft:
            return
        root = self.pkg.xml(ft[0])
        for font in root.findall(wq('font')):
            name = font.get(wq('name'))
            for style in ('embedRegular', 'embedBold', 'embedItalic', 'embedBoldItalic'):
                e = font.find(wq(style))
                if e is None:
                    continue
                part = self.pkg.target(ft[0], e.get('{%s}id' % R))
                data = bytearray(self.pkg.read(part))
                key = e.get(wq('fontKey'))
                if key:
                    k = bytes.fromhex(key.strip('{}').replace('-', ''))[::-1]
                    for i in range(32):
                        data[i] ^= k[i % 16]
                tt = TTFont(io.BytesIO(bytes(data)))
                self.faces[(name.lower(), style)] = self.describe(tt, part)

    def describe(self, tt, part):
        cmap = {}
        for table in tt['cmap'].tables:
            if table.isUnicode() or (table.platformID, table.platEncID) == (3, 0):
                for code, glyph in table.cmap.items():
                    cmap.setdefault(code, glyph)
        glyf = tt['glyf']

        def outline(g):
            gl = glyf[g]
            return gl.numberOfContours != 0
        names = tt['post'].formatType == 2.0
        return {'tt': tt, 'part': part, 'cmap': cmap, 'outline': outline, 'names': names,
                'em': tt['head'].unitsPerEm, 'hmtx': tt['hmtx']}

    def face(self, name, layers):
        if not name:
            return None
        b, i = on(prop(layers, 'b')), on(prop(layers, 'i'))
        style = 'embedBoldItalic' if b and i else 'embedBold' if b else 'embedItalic' if i else 'embedRegular'
        return self.faces.get((name.lower(), style))

    def font_tests(self, groups, p, r, t, layers, needs):
        blank, mismatch = [], []
        for i, c in enumerate(t):
            name = self.font_of(p, r, c, layers)
            face = self.face(name, layers)
            if face is None:
                continue
            if name not in self.used_faces:
                self.used_faces[name] = (face['part'], path_of(r) if self.part == self.main else None)
            glyph = face['cmap'].get(ord(c))
            if glyph is None or glyph == '.notdef':
                continue
            cat = unicodedata.category(c)
            if not face['outline'](glyph):
                if not (c.isspace() or cat in ('Zs', 'Cf', 'Mn', 'Me', 'Mc')):
                    blank.append(i)
                continue
            if symbol_font(name):
                continue
            if self.mismatch(face, c, glyph, t, layers):
                mismatch.append(i)
        if blank:
            self.emit_run(groups, 'font.glyph_blank', r, t, blank[0], blank[-1] + 1, needs, ())
        if mismatch:
            self.emit_run(groups, 'font.glyph_mismatch', r, t, mismatch[0], mismatch[-1] + 1, needs, ())

    def mismatch(self, face, c, glyph, t, layers):
        """D18's four tests as calibrated on the layout-fidelity corpora's embedded faces (CR-050 phase 1): a
        compatibility character is not tested; "drawn as" another character is evidence only where the two are
        not the same letter (same_letter) and the other is a letter or digit and no compatibility form; a
        modifier letter's advance is not tested, nor a titlecase letter's width"""
        cat = unicodedata.category(c)
        if not (cat.startswith('L') or cat == 'Nd') or compatibility(c):
            return False
        for code, g in face['cmap'].items():
            if g == glyph and chr(code) != c and evidence(c, chr(code)):
                return True                                  # test 1: a shared glyph
        adv = face['hmtx'][glyph][0]
        if cat.startswith('L') and cat != 'Lm' and base_letter(c) and (adv == 0 or (adv > 1.5 * face['em'] and cat != 'Lt')):
            return True                                      # test 2: zero, or over 1.5 em
        if face['names']:
            u = agl.toUnicode(glyph)
            if len(u) == 1 and u != c and evidence(c, u):
                return True                                  # test 3: named for another character
        for out in self.substitutions(face, glyph, layers):
            for code, g in face['cmap'].items():
                if g == out and chr(code) != c and evidence(c, chr(code)):
                    return True                              # test 4: a substitution Word applies
        return False

    def substitutions(self, face, glyph, layers):
        tt = face['tt']
        if 'GSUB' not in tt:
            return []
        features = {'ccmp', 'locl', 'rlig'}   # ccmp measured in Word 365 (case 65); locl and rlig assumed with it
        rpr_lig = None
        for layer in layers:
            e = layer.find('{%s}ligatures' % W14)
            if e is not None:
                rpr_lig = e.get('{%s}val' % W14)
        if rpr_lig:
            v = rpr_lig.lower()
            if v == 'all' or 'standard' in v:
                features.add('liga')
            if v == 'all' or 'contextual' in v:
                features.add('clig')
            if v == 'all' or 'historical' in v:
                features.add('hlig')
            if v == 'all' or 'discretional' in v:
                features.add('dlig')
        for layer in layers:
            e = layer.find('{%s}cntxtAlts' % W14)
            if e is not None and on(e, '{%s}val' % W14):
                features.add('calt')
        table = tt['GSUB'].table
        indices = set()
        for sr in table.ScriptList.ScriptRecord:
            if sr.ScriptTag not in ('DFLT', 'latn'):
                continue
            ls = sr.Script.DefaultLangSys
            if ls is None:
                continue
            for fi in ls.FeatureIndex:
                fr = table.FeatureList.FeatureRecord[fi]
                if fr.FeatureTag in features:
                    indices.update(fr.Feature.LookupListIndex)
        outs = []
        for li in sorted(indices):
            lookup = table.LookupList.Lookup[li]
            for st in lookup.SubTable:
                ltype = lookup.LookupType
                if ltype == 7:
                    ltype, st = st.ExtSubTable.LookupType, st.ExtSubTable
                if ltype == 1 and glyph in st.mapping:
                    outs.append(st.mapping[glyph])
                elif ltype == 2 and glyph in st.mapping:
                    outs.extend(st.mapping[glyph])
        return outs

    # ------------------------------------------------------------ fields

    def walk_fields(self, part, root, needs):
        stack = []
        for e in root.iter(wq('fldChar'), wq('instrText'), wq('delInstrText'), wq('fldSimple')):
            if e.tag == wq('fldSimple'):
                fn = self.context_needs(e, root, needs) | {'field_codes'}
                instr = e.get(wq('instr')) or ''
                self.field_findings(part, path_of(e), '', '', '', instr, fn, e)
                continue
            if e.tag == wq('fldChar'):
                t = e.get(wq('fldCharType'))
                if t == 'begin':
                    stack.append({'runs': [], 'code': True, 'el': e})
                elif t == 'separate' and stack:
                    stack[-1]['code'] = False
                elif t == 'end' and stack:
                    f = stack.pop()
                    if f['runs']:
                        first, last = f['runs'][0], f['runs'][-1]
                        instr = ''.join(self.run_text(r) for r in f['runs'])
                        fn = self.context_needs(first, root, needs) | {'field_codes'}
                        self.field_findings(part, path_of(first), 0, path_of(last) if last is not first else '',
                                            cps(self.run_text(last)), instr, fn, first)
                continue
            r = e.getparent()
            for f in stack:
                if f['code'] and (not f['runs'] or f['runs'][-1] is not r):
                    f['runs'].append(r)

    def field_findings(self, part, path, start, end_path, end, instr, needs, el):
        self.add(Finding('field.code', self.chain, part, path, start, end_path, end, excerpt=instr, needs=needs))
        keyword, target, bookmark = parse_field(instr)
        k = keyword.upper()
        if k in ('INCLUDETEXT', 'INCLUDEPICTURE', 'LINK', 'IMPORT', 'DDE', 'DDEAUTO'):
            self.add(Finding('field.external', self.chain, part, path, start, end_path, end, excerpt=instr, needs=needs))
        elif k == 'HYPERLINK' and target:
            if web(target):
                self.add(Finding('hyperlink.web', self.chain, part, path, start, end_path, end, excerpt=target,
                                 needs=needs, selector=target))
            else:
                self.add(Finding('field.external', self.chain, part, path, start, end_path, end, excerpt=instr, needs=needs))

    # ------------------------------------------------------------ drawings: shapes and alt text

    def smartart(self, part, rel_ids, dn):
        """SmartArt data its drawing part does not show: where the data part names a drawing part
        (dsp:dataModelExt, its relId the story part's), each point the text model reads with a line no
        paragraph of the drawing part holds, each drawing line holding one (phase 1)"""
        data = self.pkg.target(part, rel_ids.get('{%s}dm' % R))
        if not data or not self.pkg.has(data):
            return
        model = self.pkg.xml(data)
        ext = next(model.iter('{%s}dataModelExt' % DSP), None)
        drawing = self.pkg.target(part, ext.get('relId')) if ext is not None else None
        if not drawing or not self.pkg.has(drawing):
            return   # no drawing part: Word lays the diagram out from the data
        shown = {}
        for body in self.pkg.xml(drawing).iter('{%s}txBody' % DSP):
            for ap in body.findall('{%s}p' % A):
                line = dml_line(ap)
                shown[line] = shown.get(line, 0) + 1
        for pt in smartart_points(model):
            missing = []
            t = pt.find('{%s}t' % DGM)
            for ap in (t.findall('{%s}p' % A) if t is not None else []):
                line = dml_line(ap)
                if not line:
                    continue
                if shown.get(line, 0) > 0:
                    shown[line] -= 1
                else:
                    missing.append(line)
            if missing:
                self.add(Finding('smartart.data', self.chain, data, path_of(pt), excerpt='\n'.join(missing),
                                 needs=dn | {'smartart'}))

    def walk_drawings(self, part, root, needs):
        for d in root.iter('{%s}inline' % WP, '{%s}anchor' % WP):
            dn = self.context_needs(d, root, needs)
            text = self.text_of_shape(d)
            tneeds = dn | ({'text_boxes'} if text else {'never'})
            ext = d.find('{%s}extent' % WP)
            cx, cy = int(ext.get('cx')), int(ext.get('cy'))
            docpr = d.find('{%s}docPr' % WP)
            if cx == 0 or cy == 0:
                self.add(Finding('shape.zero_size', self.chain, part, path_of(d), excerpt=text, needs=tneeds))
            if d.tag == '{%s}anchor' % WP and docpr is not None and on(docpr, 'hidden') and docpr.get('hidden') is not None:
                self.add(Finding('shape.hidden', self.chain, part, path_of(d), excerpt=text, needs=tneeds))
            if d.tag == '{%s}anchor' % WP:
                verdict = self.off_page_anchor(d, cx, cy)
                if verdict == 'off':
                    self.add(Finding('shape.off_page', self.chain, part, path_of(d), excerpt=text, needs=tneeds))
                elif verdict:
                    self.status['undecided'].append(verdict + '@' + self.loc(part, path_of(d)))
            if docpr is not None:
                self.alt_text(part, d, docpr, dn)
            chart = d.find('.//{http://schemas.openxmlformats.org/drawingml/2006/chart}chart')
            if chart is not None:
                target = self.pkg.target(part, chart.get('{%s}id' % R))
                if target and self.pkg.has(target):
                    cx_ = self.pkg.xml(target)
                    C = 'http://schemas.openxmlformats.org/drawingml/2006/chart'
                    strings = [e.text or '' for e in cx_.iter('{%s}v' % C, '{%s}t' % A)
                               if e.tag == '{%s}t' % A or any(a.tag == '{%s}strCache' % C for a in e.iterancestors())]
                    self.add(Finding('chart.cached', self.chain, target, excerpt='\n'.join(strings), needs=dn | {'charts'}))
            rel_ids = d.find('{%s}graphic/{%s}graphicData/{%s}relIds' % (A, A, DGM))
            if rel_ids is not None:
                self.smartart(part, rel_ids, dn)
        for s in root.iter('{%s}shape' % V, '{%s}rect' % V, '{%s}roundrect' % V, '{%s}oval' % V):
            dn = self.context_needs(s, root, needs)
            style = vml_style(s)
            text = self.text_of_shape(s)
            tneeds = dn | ({'text_boxes'} if text else {'never'})
            if (s.get('id') or '').startswith('PowerPlusWaterMarkObject'):
                tp = s.find('{%s}textpath' % V)
                wneeds = dn | {'text_boxes'} if self.story_kind in ('header', 'footer') else dn | {'never'}
                self.add(Finding('watermark', self.chain, part, path_of(s), excerpt=tp.get('string') if tp is not None else '',
                                 needs=wneeds))
            w, h = length_unit(style.get('width')), length_unit(style.get('height'))
            if (w is not None and w == 0) or (h is not None and h == 0):
                self.add(Finding('shape.zero_size', self.chain, part, path_of(s), excerpt=text, needs=tneeds))
            if style.get('visibility', '').lower() == 'hidden':
                self.add(Finding('shape.hidden', self.chain, part, path_of(s), excerpt=text, needs=tneeds))
            if style.get('position', '').lower() == 'absolute':
                verdict = self.off_page_vml(s, style)
                if verdict == 'off':
                    self.add(Finding('shape.off_page', self.chain, part, path_of(s), excerpt=text, needs=tneeds))
                elif verdict:
                    self.status['undecided'].append(verdict + '@' + self.loc(part, path_of(s)))
            if s.get('alt'):
                image = s.find('{%s}imagedata' % V) is not None
                self.add(Finding('alt_text.description', self.chain, part, path_of(s), excerpt=s.get('alt'),
                                 needs=dn | ({'alt_text'} if image else {'never'})))
            im = s.find('{%s}imagedata' % V)
            if im is not None and im.get('{%s}title' % O):
                self.add(Finding('alt_text.title', self.chain, part, path_of(im), excerpt=im.get('{%s}title' % O), needs=dn | {'never'}))

    def text_of_shape(self, d):
        boxes = [x for x in d.iter(wq('txbxContent'))]
        if boxes:
            return '\n'.join(self.text(b) for b in boxes)
        tp = d.find('{%s}textpath' % V)
        if tp is not None and tp.get('string'):
            return tp.get('string')
        return ''

    def alt_text(self, part, d, docpr, dn):
        decorative = any(x.get('val') in ('1', 'true') for x in docpr.iter('{%s}decorative' % DECORATIVE))
        picture = d.find('.//{%s}pic' % PIC) is not None
        descr, title, name = docpr.get('descr'), docpr.get('title'), docpr.get('name')
        if descr:
            self.add(Finding('alt_text.description', self.chain, part, path_of(docpr), excerpt=descr,
                             needs=dn | ({'alt_text'} if picture and not decorative else {'never'})))
        if title:
            self.add(Finding('alt_text.title', self.chain, part, path_of(docpr), excerpt=title, needs=dn | {'never'}))
        described = picture and not decorative and not descr
        if name and (described or not DEFAULT_NAME.fullmatch(name)):   # D19 (b)
            self.add(Finding('alt_text.name', self.chain, part, path_of(docpr), excerpt=name,
                             needs=dn | ({'alt_text'} if described else {'never'})))

    # ------------------------------------------------------------ geometry (D4)

    def page_of(self, el):
        """(width, height, top, right, bottom, left) in EMU of the section holding el, or None"""
        if self.story_kind in ('header', 'footer'):
            secs = [sp for sp in self.sections if any(self.pkg.target(self.main, ref.get('{%s}id' % R)) == self.part
                    for ref in list(sp.findall(wq('headerReference'))) + list(sp.findall(wq('footerReference'))))]
            sizes = {self.page_size(sp) for sp in secs}
            return sizes.pop() if len(sizes) == 1 else None
        if self.story_kind != 'main':
            return None
        body_child = el
        for a in el.iterancestors():
            if a.tag == wq('body'):
                break
            body_child = a
        for sib in [body_child] + list(body_child.itersiblings()):
            sp = sib.find('%s/%s' % (wq('pPr'), wq('sectPr'))) if sib.tag == wq('p') else (sib if sib.tag == wq('sectPr') else None)
            if sp is not None:
                return self.page_size(sp)
        return None

    def page_size(self, sp):
        pg, mar = sp.find(wq('pgSz')), sp.find(wq('pgMar'))
        tw = lambda e, a, d: int(e.get(wq(a))) * 635 if e is not None and e.get(wq(a)) else d
        cols = sp.find(wq('cols'))
        n = int(cols.get(wq('num'))) if cols is not None and cols.get(wq('num')) else 1
        return (tw(pg, 'w', 11906 * 635), tw(pg, 'h', 16838 * 635), tw(mar, 'top', 1440 * 635), tw(mar, 'right', 1440 * 635),
                tw(mar, 'bottom', 1440 * 635), tw(mar, 'left', 1440 * 635), n)

    def off_page_anchor(self, d, cx, cy):
        page = self.page_of(d)
        if page is None:
            return 'position.no_section'
        verdicts = []
        for axis, extent in (('H', cx), ('V', cy)):
            pos = d.find('{%s}position%s' % (WP, axis))
            if pos is None or pos.find('{%s}align' % WP) is not None:
                verdicts.append('on')
                continue
            off = int(pos.find('{%s}posOffset' % WP).text)
            verdicts.append(axis_verdict(page, axis, pos.get('relativeFrom'), off, extent))
        return combine(verdicts)

    def off_page_vml(self, s, style):
        page = self.page_of(s)
        if page is None:
            return 'position.no_section'
        verdicts = []
        for axis, key, ext in (('H', 'margin-left', 'width'), ('V', 'margin-top', 'height')):
            if style.get('mso-position-%s' % ('horizontal' if axis == 'H' else 'vertical')) not in (None, 'absolute'):
                verdicts.append('on')
                continue
            off = length_unit(style.get(key, '0')) or 0
            extent = length_unit(style.get(ext, '0')) or 0
            rel = style.get('mso-position-%s-relative' % ('horizontal' if axis == 'H' else 'vertical'), 'text')
            rel = {'text': 'column' if axis == 'H' else 'paragraph'}.get(rel, rel)
            verdicts.append(axis_verdict(page, axis, rel, off * EMU_PER_PT, extent * EMU_PER_PT))
        return combine(verdicts)

    def loc(self, part, path):
        return ('>'.join(self.chain) + '|' if self.chain else '|') + part + '|' + path

    # ------------------------------------------------------------ content controls

    def load_bindings(self):
        self.stores = {}
        for cx in self.pkg.by_type(self.main, '/customXml'):
            if self.pkg.content_type(cx) not in ('application/xml', None) and not cx.endswith('.xml'):
                continue
            if self.pkg.content_type(cx) == 'application/inkml+xml':
                continue
            for props in self.pkg.by_type(cx, '/customXmlProps'):
                x = self.pkg.xml(props)
                self.stores[x.get('{http://schemas.openxmlformats.org/officeDocument/2006/customXml}itemID')] = cx
        self.stale = set()
        doc = self.pkg.xml(self.main)
        self.part = self.main
        for sdt in doc.iter(wq('sdt')):
            db = sdt.find('%s/%s' % (wq('sdtPr'), wq('dataBinding')))
            if db is None:
                continue
            store = self.stores.get(db.get(wq('storeItemID')))
            if store is None:
                continue
            ns = dict(re.findall(r"xmlns:(\w+)='([^']*)'", db.get(wq('prefixMappings')) or ''))
            ns.update(dict(re.findall(r'xmlns:(\w+)="([^"]*)"', db.get(wq('prefixMappings')) or '')))
            try:
                hits = self.pkg.xml(store).getroottree().xpath(db.get(wq('xpath')), namespaces=ns)
            except etree.XPathError:
                continue
            if not hits:
                continue
            value = hits[0] if isinstance(hits[0], str) else ''.join(hits[0].itertext())
            surface = self.text(sdt.find(wq('sdtContent')))
            if surface != value:
                self.stale.add((self.main, path_of(sdt)))

    def walk_controls(self, part, root, needs):
        for sdt in root.iter(wq('sdt')):
            content = sdt.find(wq('sdtContent'))
            dn = self.context_needs(sdt, root, needs)
            if (part, path_of(sdt)) in self.stale:
                self.add(Finding('sdt.stored_surface', self.chain, part, path_of(content), excerpt=self.text(content),
                                 needs=dn | {'bindings:surface'}))
            elif on(sdt.find('%s/%s' % (wq('sdtPr'), wq('showingPlcHdr')))):
                self.add(Finding('sdt.placeholder', self.chain, part, path_of(content), excerpt=self.text(content),
                                 needs=dn | {'placeholders'}))

    # ------------------------------------------------------------ revisions

    def walk_revisions(self, part, root, needs):
        marks = (wq('ins'), wq('del'), wq('moveFrom'), wq('moveTo'), wq('rPrChange'), wq('pPrChange'),
                 wq('sectPrChange'), wq('tblPrChange'), wq('trPrChange'), wq('tcPrChange'), wq('numberingChange'),
                 wq('cellIns'), wq('cellDel'), wq('cellMerge'))
        for e in root.iter(*marks):
            author = e.get(wq('author'))
            if author is None:
                continue
            if author not in self.authors:
                self.authors[author] = ((part, e), [])
            if e.get(wq('date')):
                self.authors[author][1].append(e.get(wq('date')))
        groups = {}
        for e in root.iter(wq('del'), wq('moveFrom')):
            if e.getparent().tag in (wq('rPr'), wq('trPr')):
                continue
            kind = 'revision.deleted' if e.tag == wq('del') else 'revision.moved_from'
            for r in e.iter(wq('r')):
                t = self.display_text(r)
                if not t:
                    continue
                if next((a for a in r.iterancestors() if a.tag in (wq('del'), wq('moveFrom'))), None) is not e:
                    continue
                self.part = part
                self.emit_run(groups, kind, r, t, 0, cps(t), self.context_needs(r, root, needs),
                              (e.get(wq('author')), e.get(wq('date'))))
        for g in groups.values():
            for f in g:
                self.add(f)

    # ------------------------------------------------------------ collapsed headings

    def outline_level(self, p):
        lvl = ppr_prop(self.styles, p, 'outlineLvl')
        if lvl is None:
            return None
        v = int(lvl.get(wq('val')))
        return v if v < 9 else None

    def walk_collapsed(self, part, root, needs):
        body = root if root.tag in (wq('body'), wq('hdr'), wq('ftr')) else root.find(wq('body'))
        if body is None:
            return
        blocks = [b for b in body if isinstance(b.tag, str) and b.tag in (wq('p'), wq('tbl'), wq('sdt'))]
        i = 0
        while i < len(blocks):
            b = blocks[i]
            level = self.outline_level(b) if b.tag == wq('p') else None
            ppr = b.find(wq('pPr')) if b.tag == wq('p') else None
            direct = on(ppr.find('{%s}collapsed' % W15)) if ppr is not None else None
            collapsed = level is not None and (direct if direct is not None else self.style_collapsed(b))
            if not collapsed:
                i += 1
                continue
            j = i + 1
            while j < len(blocks):
                n = blocks[j]
                nl = self.outline_level(n) if n.tag == wq('p') else None
                if nl is not None and nl <= level:
                    break
                j += 1
            body_blocks = blocks[i + 1:j]
            if body_blocks:
                text = '\n'.join(self.text(x) for x in body_blocks)
                self.add(Finding('paragraph.collapsed', self.chain, part, path_of(body_blocks[0]), '',
                                 path_of(body_blocks[-1]) if len(body_blocks) > 1 else '', '', excerpt=text,
                                 needs=self.context_needs(b, root, needs), summary=str(len(body_blocks))))
            i = j

    def style_collapsed(self, p):
        ppr = p.find(wq('pPr'))
        ps = ppr.find(wq('pStyle')) if ppr is not None else None
        found = False
        for s in self.styles.chain(ps.get(wq('val')) if ps is not None else self.styles.default_para):
            x = s.find(wq('pPr'))
            if x is not None and x.find('{%s}collapsed' % W15) is not None:
                found = on(x.find('{%s}collapsed' % W15))
        return found

    # ------------------------------------------------------------ the inventory

    def inventory(self):
        pkg = self.pkg
        parts = sorted('/' + n for n in pkg.names if not n.endswith('.rels') and n != '[Content_Types].xml')
        objects = self.ole_objects()
        chunks = set()
        for part in parts:
            for i, t, target, external, e in pkg.rels(part):
                if t.endswith('/aFChunk') and not external:
                    chunks.add(target)
        for part in parts:
            ctype = pkg.content_type(part) or ''
            if ctype == 'application/vnd.ms-office.vbaProject':
                self.add(Finding('part.macro', self.chain, part, excerpt='', needs={'never'}))
            elif ctype == 'application/vnd.ms-office.activeX+xml':
                self.add(Finding('part.activex', self.chain, part, excerpt='', needs={'never'}))
            elif ctype == 'application/inkml+xml':
                self.add(Finding('ink', self.chain, part, excerpt='', needs={'never'}))
            elif ctype.endswith('wordprocessingml.people+xml'):
                x = pkg.xml(part)
                authors = [e.get('{%s}author' % W15) for e in x.iter('{%s}person' % W15)]
                self.add(Finding('people', self.chain, part, excerpt='\n'.join(a for a in authors if a), needs={'never'}))
            elif ctype in ('application/vnd.openxmlformats-package.core-properties+xml',
                           'application/vnd.openxmlformats-officedocument.extended-properties+xml',
                           'application/vnd.openxmlformats-officedocument.custom-properties+xml'):
                x = pkg.xml(part)
                values = [t.strip() for t in x.itertext() if t.strip()]
                if values:
                    needs = {'metadata'} if not self.chain else {'never'}
                    self.add(Finding('properties', self.chain, part, excerpt='\n'.join(values), needs=needs))
            elif ctype == 'application/xml' and self.is_custom_xml(part):
                x = pkg.xml(part)
                values = [t.strip() for t in x.itertext() if t.strip()]
                self.add(Finding('custom_xml', self.chain, part, excerpt='\n'.join(values), needs={'never'}))
            elif not any(ctype.startswith(k) for k in KNOWN_TYPES):
                self.add(Finding('part.unknown', self.chain, part, excerpt='', needs={'never'}, summary=ctype))
            if part in chunks:
                self.chunk(part, ctype)
            if part in objects:
                self.embedded(part, ctype, objects[part])
        for part in ['/'] + parts:
            for i, t, target, external, e in pkg.rels(part):
                rp = pkg.rels_part(part)
                if external:
                    if t.endswith('/hyperlink') and web(target):
                        self.add(Finding('hyperlink.web', self.chain, rp, path_of(e), excerpt=target, needs={'never'},
                                         selector=target))
                    else:
                        self.add(Finding('relationship.external', self.chain, rp, path_of(e), excerpt=target,
                                         needs={'never'}, selector=target))

    def is_custom_xml(self, part):
        for owner in [self.main] if self.main else []:
            if part in self.pkg.by_type(owner, '/customXml'):
                return True
        return False

    def ole_objects(self):
        """embedded part -> ProgID, from every o:OLEObject in the story parts"""
        out = {}
        if self.main is None:
            return out
        parts = [self.main] + self.pkg.by_type(self.main, '/header') + self.pkg.by_type(self.main, '/footer')
        for part in parts:
            x = self.pkg.xml(part)
            if x is None:
                continue
            for ole in x.iter('{%s}OLEObject' % O):
                if ole.get('Type') == 'Link':
                    continue
                target = self.pkg.target(part, ole.get('{%s}id' % R))
                if target:
                    out[target] = ole.get('ProgID')
        return out

    def chunk(self, part, ctype):
        self.add(Finding('altchunk', self.chain, part, excerpt='', needs={'never'}, summary=ctype))
        data = self.pkg.read(part)
        loc = ('>'.join(self.chain) + '|' if self.chain else '|') + part + '|'
        if self.depth >= 1:
            self.status['skipped'].append('depth@' + loc)
            return
        html = []
        if ctype in ('text/html', 'application/xhtml+xml'):
            html.append(decode_html(data))
        elif ctype == 'message/rfc822':
            msg = message_from_bytes(data)
            for sub in msg.walk():
                if sub.get_content_type() in ('text/html', 'application/xhtml+xml'):
                    payload, charset = sub.get_payload(decode=True), sub.get_content_charset()
                    try:
                        html.append(payload.decode('cp1252' if charset.lower() in WINDOWS_1252 else charset, 'replace')
                                    if charset else decode_html(payload))
                    except LookupError:
                        html.append(decode_html(payload))
        elif ctype in (OFFICE_CHUNK_DOCX,):
            inner = Package(data, self.chain + [part])
            Inspector(inner, self.out, self.status, self.depth + 1, self.base_needs | {'altchunks'}).run()
            return
        elif ctype == 'application/xml' and b'pkg:package' in data:
            inner = flat_opc(data, self.chain + [part])
            Inspector(inner, self.out, self.status, self.depth + 1, self.base_needs | {'altchunks'}).run()
            return
        elif ctype == 'text/plain':
            return
        else:
            self.status['skipped'].append('not_decoded@' + loc)
            return
        scripts, refs = [], []
        for h in html:
            scripts += re.findall(r'<script\b[^>]*>(.*?)</script>', h, re.S | re.I)
            scripts += re.findall(r'\son[a-z]+\s*=\s*"([^"]*)"', h, re.I)
            for m in re.finditer(r'\s(?:src|href|background)\s*=\s*"([^"]*)"', h, re.I):
                v = m.group(1).strip()
                if javascript_uri(m.group(1)):
                    scripts.append(v)   # a handler, as a script is (phase 1)
                elif v and not v.startswith('#') and not re.match(r'(data|cid|mailto|javascript):', v, re.I):
                    refs.append(v)
        if scripts:
            self.add(Finding('altchunk.script', self.chain, part, excerpt='', needs={'never'}))
        if refs:
            self.add(Finding('altchunk.external_ref', self.chain, part, excerpt='', needs={'never'},
                             summary=' '.join(refs)))
        # The oracle has no HTML reader (CR-053 phase 3, WD 0.27): it cannot tell what display:none
        # hides, so the chunk is skipped as not decoded, its scripts and references found all the same
        # (REQ-902's Chunks rule). A reader-present inspection is expected-inspection-html.tsv's.
        self.status['skipped'].append('not_decoded@' + loc)

    def embedded(self, part, ctype, prog):
        self.add(Finding('object.embedded', self.chain, part, excerpt='', needs={'never'}, summary=prog or ''))
        loc = ('>'.join(self.chain) + '|' if self.chain else '|') + part + '|'
        if self.depth >= 1:
            self.status['skipped'].append('depth@' + loc)
            return
        data = self.pkg.read(part)
        if data[:2] == b'PK':
            inner = Package(data, self.chain + [part])
            Inspector(inner, self.out, self.status, self.depth + 1, self.base_needs | {'embedded'}).run()

    # ------------------------------------------------------------ spreadsheets and presentations (inside a docx)

    def workbook(self, main):
        wb = self.pkg.xml(main)
        strings = []
        ss = self.pkg.by_type(main, '/sharedStrings')
        if ss:
            for si in self.pkg.xml(ss[0]).iter('{%s}si' % X):
                strings.append(''.join(t.text or '' for t in si.iter('{%s}t' % X)))
        for sheet in wb.iter('{%s}sheet' % X):
            state = sheet.get('state')
            target = self.pkg.target(main, sheet.get('{%s}id' % R))
            ws = self.pkg.xml(target)
            text = sheet_text(ws, strings)
            hidden = state in ('hidden', 'veryHidden')
            if hidden:
                kind = 'embedded.sheet_hidden' if state == 'hidden' else 'embedded.sheet_very_hidden'
                self.add(Finding(kind, self.chain, main, path_of(sheet), excerpt=text, needs={'hidden'}))
            for row in ws.iter('{%s}row' % X):
                if on(row, 'hidden') and row.get('hidden') is not None:
                    self.add(Finding('embedded.row_hidden', self.chain, target, path_of(row),
                                     excerpt='\t'.join(cell_text(c, strings) for c in row.iter('{%s}c' % X)),
                                     needs={'hidden'} if hidden else set()))
            for col in ws.iter('{%s}col' % X):
                if on(col, 'hidden') and col.get('hidden') is not None:
                    lo, hi = int(col.get('min')), int(col.get('max'))
                    cells = []
                    for row in ws.iter('{%s}row' % X):
                        for c in row.iter('{%s}c' % X):
                            if lo <= column_number(c.get('r')) <= hi:
                                cells.append(cell_text(c, strings))
                    self.add(Finding('embedded.column_hidden', self.chain, target, path_of(col), excerpt='\n'.join(cells),
                                     needs={'hidden'} if hidden else set()))

    def presentation(self, main):
        pres = self.pkg.xml(main)
        for sid in pres.iter('{%s}sldId' % P):
            target = self.pkg.target(main, sid.get('{%s}id' % R))
            sld = self.pkg.xml(target)
            if sld.get('show') in ('0', 'false'):
                self.add(Finding('embedded.slide_hidden', self.chain, target, path_of(sld), excerpt=drawing_text(sld),
                                 needs={'hidden'}))
            for notes in self.pkg.by_type(target, '/notesSlide'):
                n = self.pkg.xml(notes)
                text = notes_text(n)
                if text:
                    self.add(Finding('embedded.notes', self.chain, notes, path_of(n), excerpt=text, needs={'never'}))


OFFICE_CHUNK_DOCX = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml'


def script(c):
    name = unicodedata.name(c, '')
    return name.split(' ')[0] if name else ''


# Latin letters a face draws as another by design: dotless i and j (ccmp puts them under an accent), kra, eth
# and d with stroke (a capitals face draws them as K and D)
FOLD = {'\u0131': 'i', '\u0237': 'j', '\u0138': 'k', '\u00f0': 'd', '\u0111': 'd'}


def simple(x, f):
    """A one-character case mapping (Java's Character.toUpperCase), not Python's full one (ß to SS)"""
    y = f(x)
    return y if len(y) == 1 else x


def base_letter(c):
    """A character's base letter: its compatibility decomposition, marks, modifier letters and spaces dropped,
    case folded, FOLD applied"""
    out = []
    for x in unicodedata.normalize('NFKD', c):
        if unicodedata.category(x) in ('Mn', 'Mc', 'Me', 'Lm') or x.isspace():
            continue
        f = simple(simple(x, str.upper), str.lower)
        out.append(FOLD.get(f, f))
    return ''.join(out)


def same_letter(c, d):
    """One letter for D18: the same base, either with none (a mark-like letter), or different scripts"""
    bc, bd = base_letter(c), base_letter(d)
    return not bc or not bd or bc == bd or script(c) != script(d)


def compatibility(c):
    return unicodedata.normalize('NFKC', c) != c


def evidence(c, d):
    """c drawn as d is evidence: d a letter or digit, no compatibility form, another letter"""
    cat = unicodedata.category(d)
    return (cat.startswith('L') or cat == 'Nd') and not compatibility(d) and not same_letter(c, d)


def web(target):
    m = re.match(r'([A-Za-z][A-Za-z0-9+.-]*):', target or '')
    return bool(m) and m.group(1).lower() in ('http', 'https', 'mailto') and len(m.group(1)) > 1


def parse_field(instr):
    """(keyword, first argument, bookmark) of a field instruction: arguments quoted or bare, \\l takes one"""
    tokens = []
    i, s = 0, instr
    while i < len(s):
        c = s[i]
        if c.isspace():
            i += 1
            continue
        if c == '"':
            j, out = i + 1, []
            while j < len(s) and s[j] != '"':
                if s[j] == '\\' and j + 1 < len(s) and s[j + 1] in '\\"':
                    out.append(s[j + 1])
                    j += 2
                    continue
                out.append(s[j])
                j += 1
            tokens.append(('arg', ''.join(out)))
            i = j + 1
            continue
        j = i
        while j < len(s) and not s[j].isspace():
            j += 1
        word = s[i:j]
        tokens.append(('switch' if word.startswith('\\') and len(word) == 2 and word[1].isalpha() else 'arg', word))
        i = j
    if not tokens:
        return '', None, None
    keyword = tokens[0][1]
    target, bookmark = None, None
    k = 1
    while k < len(tokens):
        kind, value = tokens[k]
        if kind == 'switch':
            if value.lower() in ('\\l', '\\o', '\\t', '\\m') and k + 1 < len(tokens) and tokens[k + 1][0] == 'arg':
                if value.lower() == '\\l':
                    bookmark = tokens[k + 1][1]
                k += 2
                continue
        elif target is None:
            target = value
        k += 1
    return keyword, target, bookmark


def axis_verdict(page, axis, rel, off, extent):
    W_, H_, top, right, bottom, left, columns = page
    size, lo_margin, hi_margin = (W_, left, right) if axis == 'H' else (H_, top, bottom)
    if rel == 'page':
        lo = hi = off
    elif rel in ('margin', 'leftMargin', 'topMargin', 'insideMargin'):
        lo = hi = lo_margin + off
    elif rel in ('rightMargin', 'bottomMargin', 'outsideMargin'):
        lo = hi = size - hi_margin + off
    elif rel == 'column' and axis == 'H' and columns == 1:
        lo = hi = lo_margin + off    # one column: its left edge is the left margin
    else:   # paragraph, line, character, a column of several: anchored anywhere in the text area
        lo, hi = lo_margin + off, size - hi_margin + off
    if hi + extent <= 0 or lo >= size:
        return 'off'
    if lo + extent > 0 and hi < size:
        return 'on'
    return 'position.indeterminate'


def combine(verdicts):
    if 'off' in verdicts:
        return 'off'
    if 'position.indeterminate' in verdicts:
        return 'position.indeterminate'
    return None


def column_number(ref):
    letters = re.match(r'[A-Z]+', ref).group(0)
    n = 0
    for ch in letters:
        n = n * 26 + ord(ch) - 64
    return n


def cell_text(c, strings):
    t = c.get('t')
    if t == 'inlineStr':
        return ''.join(x.text or '' for x in c.iter('{%s}t' % X))
    v = c.find('{%s}v' % X)
    if v is None:
        return ''
    if t == 's':
        return strings[int(v.text)]
    return v.text or ''


def sheet_text(ws, strings):
    rows = []
    for row in ws.iter('{%s}row' % X):
        rows.append('\t'.join(cell_text(c, strings) for c in row.iter('{%s}c' % X)))
    return '\n'.join(rows)


def javascript_uri(v):
    """A javascript: URI as a browser reads one: tabs, line feeds and leading controls and spaces ignored"""
    v = re.sub(r'[\t\n\r]', '', v).lstrip(''.join(chr(c) for c in range(0x21)))
    return v[:11].lower() == 'javascript:'


# The labels WHATWG's Encoding standard reads as windows-1252 ([ENCODING]), ISO-8859-1 and US-ASCII among them
WINDOWS_1252 = {'ansi_x3.4-1968', 'ascii', 'cp1252', 'cp819', 'csisolatin1', 'ibm819', 'iso-8859-1', 'iso-ir-100',
                'iso8859-1', 'iso88591', 'iso_8859-1', 'iso_8859-1:1987', 'l1', 'latin1', 'us-ascii', 'windows-1252',
                'x-cp1252'}


def decode_html(data):
    """An HTML or XHTML chunk's characters (WD 0.28's Chunks rule): by its byte order mark; else the
    charset a meta element names in its first 1,024 bytes (its charset, or its http-equiv content
    type's), an [ENCODING] label; else UTF-8 where the bytes are valid UTF-8; else windows-1252. An XML
    declaration names nothing, and UTF-16 without a byte order mark is read as its bytes say (CR-053)"""
    for bom, enc in ((b'\xef\xbb\xbf', 'utf-8'), (b'\xff\xfe', 'utf-16-le'), (b'\xfe\xff', 'utf-16-be')):
        if data.startswith(bom):
            return data[len(bom):].decode(enc, 'replace')
    m = re.search(r'<meta\b[^>]*?charset\s*=\s*["\']?([A-Za-z0-9._:-]+)', data[:1024].decode('latin-1'), re.I)
    if m:
        label = m.group(1).strip().lower()
        try:
            name = 'cp1252' if label in WINDOWS_1252 else codecs.lookup(label).name
            if not name.startswith(('utf-16', 'utf-32')):
                return data.decode(name, 'replace')
        except LookupError:
            pass
    try:
        return data.decode('utf-8')
    except UnicodeDecodeError:
        return data.decode('cp1252', 'replace')


def dml_line(ap):
    """A DrawingML paragraph's line: its runs' and fields' a:t, an a:br a line feed"""
    out = []
    for c in ap:
        if c.tag in ('{%s}r' % A, '{%s}fld' % A):
            t = c.find('{%s}t' % A)
            out.append(t.text or '' if t is not None else '')
        elif c.tag == '{%s}br' % A:
            out.append('\n')
    return ''.join(out)


def smartart_points(model):
    """The points whose text the text model reads: from the doc point depth-first along parOf
    connections, each point's children by srcOrd, those of type node (the default) and asst"""
    points, doc = {}, None
    pt_lst = model.find('{%s}ptLst' % DGM)
    for pt in (pt_lst.findall('{%s}pt' % DGM) if pt_lst is not None else []):
        mid = pt.get('modelId')
        if mid is None:
            continue
        points[mid] = pt
        if doc is None and pt.get('type') == 'doc':
            doc = pt
    if doc is None:
        return []
    kids = {}
    cxn_lst = model.find('{%s}cxnLst' % DGM)
    for c in (cxn_lst.findall('{%s}cxn' % DGM) if cxn_lst is not None else []):
        if c.get('type', 'parOf') != 'parOf' or c.get('srcId') is None:
            continue
        kids.setdefault(c.get('srcId'), []).append(c)
    for l in kids.values():
        l.sort(key=lambda c: int(c.get('srcOrd') or 0))
    out, seen = [], set()

    def visit(pt):
        if pt.get('modelId') in seen:
            return   # a cycle
        seen.add(pt.get('modelId'))
        if pt.get('type', 'node') in ('node', 'asst'):
            out.append(pt)
        for c in kids.get(pt.get('modelId'), []):
            child = points.get(c.get('destId'))
            if child is not None:
                visit(child)
    visit(doc)
    return out


def drawing_text(root):
    return '\n'.join(''.join(t.text or '' for t in p.iter('{%s}t' % A)) for p in root.iter('{%s}p' % A))


def notes_text(root):
    parts = []
    for sp in root.iter('{%s}sp' % P):
        ph = sp.find('.//{%s}ph' % P)
        if ph is not None and ph.get('type') in ('sldImg', 'sldNum', 'hdr', 'ftr', 'dt'):
            continue
        t = drawing_text(sp)
        if t.strip():
            parts.append(t)
    return '\n'.join(parts)


def flat_opc(data, chain):
    root = etree.fromstring(data)
    PKG = 'http://schemas.microsoft.com/office/2006/xmlPackage'
    entries = {}
    types = []
    for part in root.iter('{%s}part' % PKG):
        name = part.get('{%s}name' % PKG)
        xd = part.find('{%s}xmlData' % PKG)
        content = etree.tostring(xd[0]) if xd is not None and len(xd) else b''
        entries[name.lstrip('/')] = content
        types.append('<Override PartName="%s" ContentType="%s"/>' % (name, part.get('{%s}contentType' % PKG)))
    buf = io.BytesIO()
    with zipfile.ZipFile(buf, 'w') as z:
        z.writestr('[Content_Types].xml', '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
                   + ''.join(types) + '</Types>')
        for n, c in entries.items():
            z.writestr(n, c)
    return Package(buf.getvalue(), chain)


# ---------------------------------------------------------------- the tables

def resolve(root, path):
    """The element at a canonical path, or None"""
    el = None
    for step in path.split('/')[1:]:
        m = re.fullmatch(r'([^\[]+)\[(\d+)\]', step)
        name, n = m.group(1), int(m.group(2))
        children = [root] if el is None else [c for c in el if isinstance(c.tag, str)]
        hits = [c for c in children if q(c) == name]
        if len(hits) < n:
            return None
        el = hits[n - 1]
    return el


CASE = re.compile(r'Case (\d+):')

# the case of an inventory finding no element of the document refers to, from the builder's source
PART_CASES = {
    ('extraction-probe-external.docm', '/word/settings.xml'): '50',
    ('extraction-probe-external.docm', '/word/vbaProject.bin'): '51',
    ('extraction-probe-inspect.docx', '/word/people.xml'): '63',
    ('extraction-probe-inspect.docx', '/word/probe63.xml'): '63',
    ('extraction-probe-inspect.docx', '/word/header1.xml'): '59',
}


def case_before(root, el):
    """The case of the nearest preceding body paragraph that reads "Case n: ..." """
    body = root.find(wq('body'))
    top = el
    for a in el.iterancestors():
        if a is body:
            break
        top = a
    for sib in [top] + list(top.itersiblings(preceding=True)):
        if sib.tag == wq('p'):
            m = CASE.match(''.join(t.text or '' for t in sib.iter(wq('t'))))
            if m:
                return m.group(1)
    return ''


def case_of(pkg, f, cases):
    if f.chain:
        for t in TOKEN.findall(' '.join([f.excerpt or '', f.summary or '', f.selector or ''])):
            if t in cases:
                return cases[t]
        return case_of_part(pkg, f.chain[0], cases)
    main = pkg.main()
    root = pkg.xml(main)
    if getattr(f, 'anchor', None):
        el = resolve(root, f.anchor)
        if el is not None:
            return case_before(root, el)
    if (pkg.fixture, f.part) in PART_CASES:
        return PART_CASES[(pkg.fixture, f.part)]
    if f.part == main and f.path:
        el = resolve(root, f.path)
        if el is not None:
            return case_before(root, el)
    for t in TOKEN.findall(' '.join([f.excerpt or '', f.summary or '', f.selector or ''])):
        if t in cases:
            return cases[t]
    if f.path and f.part.endswith('.rels'):
        el = resolve(pkg.xml(f.part), f.path)
        source = f.part.replace('/_rels/', '/')[:-len('.rels')]
        if el is not None and source == main:
            return case_of_id(root, el.get('Id'))
    return case_of_part(pkg, f.part, cases)


def case_of_part(pkg, part, cases):
    main = pkg.main()
    root = pkg.xml(main)
    for i, t, target, external, e in pkg.rels(main):
        if target == part:
            c = case_of_id(root, i)
            if c:
                return c
    # a part reached through another (an ActiveX control's binary, a chart's workbook): its referrer's case
    for owner in sorted('/' + n for n in pkg.names if n.endswith('.rels')):
        source = owner.replace('/_rels/', '/')[:-len('.rels')]
        if source in ('/', main) or not pkg.has(source):
            continue
        for i, t, target, external, e in pkg.rels(source):
            if target == part:
                c = case_of_part(pkg, source, cases)
                if c:
                    return c
    return ''


def case_of_id(root, rid):
    for el in root.iter():
        if not isinstance(el.tag, str):
            continue
        if any(v == rid for k, v in el.attrib.items() if k.startswith('{%s}' % R)):
            return case_before(root, el)
    return ''


CLASS = {'chars.zero_width': zero_width, 'chars.tag': tag, 'chars.bidi': bidi, 'chars.private_use': private_use}


def selector(pkg, f):
    """The (kind, selector) key of expected-findings.tsv: the marker the finding is about, its target, or its part"""
    if f.selector:
        return f.selector
    if f.start != '' and f.path and (f.kind in CLASS or f.kind.startswith('text.') or f.kind.startswith('font.')):
        part = f.part if not f.chain else None
        root = pkg.xml(f.part) if not f.chain else None
        r = resolve(root, f.path) if root is not None else None
        if r is not None:
            text = ''.join((c.text or '') if c.tag in (wq('t'), wq('delText'), wq('instrText'), wq('delInstrText'))
                           else chr(int(c.get(wq('char')), 16)) if c.tag == wq('sym') else '' for c in r)
            words = re.split(r'(\s+)', text)
            pos = 0
            for w in words:
                if pos <= f.start < pos + len(w) and not w.isspace():
                    test = CLASS.get(f.kind)
                    word = w.rstrip(''.join(ch for ch in w if test and test(ch))) if test else w
                    if TOKEN.match(word):
                        return word
                pos += len(w)
            m = TOKEN.search(f.excerpt or '')
            if m:
                return m.group(0)
            p = next((a for a in r.iterancestors() if a.tag == wq('p')), None)
            if p is not None:
                m = TOKEN.search(''.join(t.text or '' for t in p.iter(wq('t'))))
                if m:
                    return m.group(0)
    m = TOKEN.search(f.excerpt or '')
    if m:
        return m.group(0)
    if f.excerpt and len(f.excerpt) <= 80 and '\n' not in f.excerpt:
        return f.excerpt.strip()
    return ('>'.join(f.chain) + '>' if f.chain else '') + f.part


def inspect_fixture(path):
    data = open(path, 'rb').read()
    out, status = [], {'skipped': [], 'undecided': []}
    pkg = Package(data, [])
    pkg.fixture = posixpath.basename(path)
    Inspector(pkg, out, status).run()
    return pkg, out, status


def tsv_value(v):
    return '' if v is None else str(v)


def main(directory):
    for line in open(posixpath.join(posixpath.dirname(posixpath.abspath(__file__)), '..', '..', '..', '..', '..', '..',
                                    'docx4j-core/src/main/resources/org/docx4j/jaxb/understood-namespaces.txt')):
        line = line.strip()
        if line and not line.startswith('#'):
            UNDERSTOOD.add(line)
    cases = {}
    for line in open(posixpath.join(directory, 'expected-tokens.tsv'), encoding='utf-8').read().splitlines()[1:]:
        cols = line.split('\t')
        cases[cols[1].replace('‑', '').replace('­', '')] = cols[0]
    rows = ['fixture\tcase\tkind\tchain\tpart\tpath\tstart\tend_path\tend\taddress\tvisible\treveal\t'
            + '\t'.join(name for name, _, _ in OUTPUTS) + '\texcerpt_length\texcerpt_cut\tselector\tsummary']
    status_rows = ['fixture\tstatus\tskipped\tundecided']
    for fixture in FIXTURES:
        pkg, findings, status = inspect_fixture(posixpath.join(directory, fixture))
        for f in findings:
            sel = selector(pkg, f)
            case = case_of(pkg, f, cases) or '0'   # 0: the fixture's, as a golden's case is
            excerpt = f.excerpt or ''
            rows.append('\t'.join(tsv_value(x) for x in [
                fixture, case, f.kind, '>'.join(f.chain), f.part, f.path, f.start, f.end_path, f.end, '',
                f.visible, f.reveal()] + [f.group(o, fmt) for _, o, fmt in OUTPUTS] + [
                cps(excerpt), 'true' if cps(excerpt) > 200 else 'false', sel,
                f.summary.replace('\t', ' ').replace('\n', ' ')]))
        partial = bool(status['skipped'])
        status_rows.append('\t'.join([fixture, 'PARTIAL' if partial else 'COMPLETE', '; '.join(sorted(status['skipped'])),
                                      '; '.join(sorted(status['undecided']))]))
    open(posixpath.join(directory, 'expected-inspection.tsv'), 'w', encoding='utf-8', newline='\n').write('\n'.join(rows) + '\n')
    open(posixpath.join(directory, 'expected-inspection-status.tsv'), 'w', encoding='utf-8', newline='\n').write(
        '\n'.join(status_rows) + '\n')
    print('wrote %d findings' % (len(rows) - 1))


if __name__ == '__main__':
    main(sys.argv[1])
