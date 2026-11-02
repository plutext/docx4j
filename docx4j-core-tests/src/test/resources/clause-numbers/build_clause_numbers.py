# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""The clause-numbers probe (CR-074 phase 0): what Word shows for a numbered paragraph as a cross-reference, in
full context (REF \\w), no context (\\n) and relative context (\\r), with and without \\t, on nested, restarted,
style-linked and other lists; and what Word's own Insert Cross-reference writes (the bookmark, the switches).

cn-1-labels.docx: every numbered paragraph is bookmarked (cn_<id> round its id, L6 for instance), and REF fields over
the bookmarks, their results a stale "?", sit in a table of field lines after the lists (F-<id>: \\w, \\n, \\r, \\w \\t,
\\n \\t, \\r \\t) and, for \\r from inside a list, in chosen numbered paragraphs ("r:" then one field a target).
clause-numbers-probe.ps1 has Word update them and save; Word's results are the goldens.
cn-2-insert.docx: the same lists, no bookmarks or fields, and X paragraphs where the script has Word insert its own
cross-references.

Run from this directory: python3 build_clause_numbers.py .  (a rebuild is byte-identical; README.md has the cases)."""
import sys, os, zipfile

NS = ('xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" '
      'xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"')
XML = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
CT = (XML + '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
      '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
      '<Default Extension="xml" ContentType="application/xml"/>'
      '<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>'
      '<Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>'
      '<Override PartName="/word/numbering.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"/>'
      '</Types>')
RELS = (XML + '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
        '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>'
        '</Relationships>')
DOC_RELS = (XML + '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
            '<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>'
            '<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering" Target="numbering.xml"/>'
            '</Relationships>')
DATE = '2026-11-03T00:00:00Z'

# ---- numbering.xml: each level (format, text, options), and the lists ----

def lvl(ilvl, fmt, text, style=None, legal=False, symbol=False):
    return ('<w:lvl w:ilvl="%d"><w:start w:val="1"/><w:numFmt w:val="%s"/>%s%s<w:lvlText w:val="%s"/><w:lvlJc w:val="left"/>'
            '<w:pPr><w:ind w:left="%d" w:hanging="720"/></w:pPr>%s</w:lvl>'
            % (ilvl, fmt, '<w:pStyle w:val="%s"/>' % style if style else '', '<w:isLgl/>' if legal else '', text,
               720 * (ilvl + 1), '<w:rPr><w:rFonts w:ascii="Symbol" w:hAnsi="Symbol" w:hint="default"/></w:rPr>' if symbol else ''))

def abstract(aid, levels, link=None, numStyleLink=None):
    head = '<w:abstractNum w:abstractNumId="%d"><w:multiLevelType w:val="multilevel"/>' % aid
    if link: head += '<w:styleLink w:val="%s"/>' % link
    if numStyleLink: head += '<w:numStyleLink w:val="%s"/>' % numStyleLink
    return head + ''.join(levels) + '</w:abstractNum>'

LEGAL = [lvl(0, 'decimal', '%1.'), lvl(1, 'decimalZero', '%1.%2'), lvl(2, 'lowerLetter', '(%3)'), lvl(3, 'lowerRoman', '(%4)')]
ABSTRACTS = [
    abstract(1, LEGAL),                                                                                  # L
    abstract(2, [lvl(0, 'decimal', '%1.'), lvl(1, 'lowerLetter', '%2.'), lvl(2, 'lowerRoman', '%3.')]),  # O
    abstract(3, [lvl(0, 'decimal', '%1)'), lvl(1, 'lowerLetter', '%2)'), lvl(2, 'lowerRoman', '%3)')]),  # P
    abstract(4, [lvl(0, 'decimal', '%1.'), lvl(1, 'decimal', '%1.%2.'), lvl(2, 'decimal', '%1.%2.%3.')]),  # H
    abstract(5, [lvl(0, 'upperRoman', 'Article %1', 'ArticleHeading'),                                   # A
                 lvl(1, 'decimalZero', 'Section %1.%2', 'SectionHeading', legal=True),
                 lvl(2, 'lowerLetter', '(%3)', 'ClauseA')]),
    abstract(6, [lvl(0, 'upperRoman', '%1.'), lvl(1, 'decimal', '%1.%2', legal=True), lvl(2, 'lowerLetter', '(%3)')]),  # G
    abstract(7, LEGAL),                                                                                  # S
    abstract(8, [lvl(0, 'decimal', '%1.'), lvl(1, 'lowerLetter', '(%2)')]),                              # R
    abstract(9, [lvl(0, 'decimal', '%1.'), lvl(1, 'decimal', '%1.%2')], link='LegalList'),               # Y, the list style's
    abstract(10, [], numStyleLink='LegalList'),                                                          # Y, the paragraphs'
    abstract(12, [lvl(0, 'upperRoman', 'Article %1'), lvl(1, 'decimal', '%2.'), lvl(2, 'lowerLetter', '(%3)')]),  # M
    abstract(13, [lvl(0, 'bullet', '', symbol=True)]),                                             # N1
    abstract(14, [lvl(0, 'decimal', 'Step %1:'), lvl(1, 'lowerLetter', '%2.')]),                         # N2, N3
    abstract(15, [lvl(0, 'none', 'NOTE')]),                                                              # N4
    abstract(16, [lvl(0, 'none', '')]),                                                                  # N5
    abstract(17, [lvl(0, 'decimal', '%1'), lvl(1, 'decimal', '%1-%2'), lvl(2, 'upperLetter', '%3')]),   # N6 to N8
    abstract(18, [lvl(0, 'decimal', '%1.'), lvl(1, 'lowerLetter', '(%2)')]),                             # T
]
NUMS = [(n, n) for n in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18)]
RESTART = '<w:num w:numId="11"><w:abstractNumId w:val="8"/><w:lvlOverride w:ilvl="0"><w:startOverride w:val="1"/></w:lvlOverride></w:num>'
NUMBERING = (XML + '<w:numbering %s>%s%s%s</w:numbering>'
             % (NS, ''.join(ABSTRACTS), ''.join('<w:num w:numId="%d"><w:abstractNumId w:val="%d"/></w:num>' % n for n in NUMS), RESTART))

# ---- styles.xml: Normal, three headings linked to list A's levels, the list style of Y ----

def pstyle(sid, name, ppr, rpr=''):
    return ('<w:style w:type="paragraph" w:styleId="%s"><w:name w:val="%s"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/>'
            '<w:qFormat/><w:pPr>%s</w:pPr>%s</w:style>' % (sid, name, ppr, rpr))

STYLES = (XML + '<w:styles ' + NS + '><w:docDefaults><w:rPrDefault><w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:cs="Calibri"/>'
          '<w:sz w:val="22"/><w:lang w:val="en-AU"/></w:rPr></w:rPrDefault><w:pPrDefault><w:pPr><w:spacing w:after="120"/></w:pPr>'
          '</w:pPrDefault></w:docDefaults>'
          '<w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/><w:qFormat/></w:style>'
          '<w:style w:type="character" w:default="1" w:styleId="DefaultParagraphFont"><w:name w:val="Default Paragraph Font"/></w:style>'
          '<w:style w:type="numbering" w:default="1" w:styleId="NoList"><w:name w:val="No List"/></w:style>'
          + pstyle('CaseHeading', 'Case Heading', '<w:keepNext/><w:spacing w:before="240"/>', '<w:rPr><w:b/><w:color w:val="1F4E79"/></w:rPr>')
          + pstyle('ArticleHeading', 'Article Heading', '<w:keepNext/><w:numPr><w:numId w:val="5"/></w:numPr><w:outlineLvl w:val="0"/>', '<w:rPr><w:b/></w:rPr>')
          + pstyle('SectionHeading', 'Section Heading', '<w:keepNext/><w:numPr><w:ilvl w:val="1"/><w:numId w:val="5"/></w:numPr><w:outlineLvl w:val="1"/>', '<w:rPr><w:b/></w:rPr>')
          + pstyle('ClauseA', 'Clause A', '<w:numPr><w:ilvl w:val="2"/><w:numId w:val="5"/></w:numPr>')
          + '<w:style w:type="numbering" w:customStyle="1" w:styleId="LegalList"><w:name w:val="Legal List"/>'
            '<w:pPr><w:numPr><w:numId w:val="9"/></w:numPr></w:pPr></w:style>'
          '</w:styles>')

# ---- document.xml ----

class Doc:
    def __init__(self, probe):
        self.probe = probe   # True for cn-1 (bookmarks and fields), False for cn-2 (plain lists)
        self.ids = 0
        self.parts = []
        self.order = []      # numbered paragraph ids, in document order

    def nid(self):
        self.ids += 1
        return self.ids

    def add(self, xml):
        self.parts.append(xml)

    def run(self, text):
        return '<w:r><w:t xml:space="preserve">%s</w:t></w:r>' % text.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')

    def field(self, instr):
        return ('<w:r><w:fldChar w:fldCharType="begin"/></w:r><w:r><w:instrText xml:space="preserve"> %s </w:instrText></w:r>'
                '<w:r><w:fldChar w:fldCharType="separate"/></w:r><w:r><w:t>?</w:t></w:r><w:r><w:fldChar w:fldCharType="end"/></w:r>'
                % instr.replace('"', '&quot;'))

    def bookmark(self, name, inner):
        if not self.probe: return inner
        i = self.nid()
        return '<w:bookmarkStart w:id="%d" w:name="%s"/>%s<w:bookmarkEnd w:id="%d"/>' % (i, name, inner, i)

    def para(self, body, ppr=''):
        return '<w:p>%s%s</w:p>' % ('<w:pPr>%s</w:pPr>' % ppr if ppr else '', body)

    def plain(self, text, style=None):
        self.add(self.para(self.run(text), '<w:pStyle w:val="%s"/>' % style if style else ''))

    def case(self, title):
        self.plain(title, 'CaseHeading')

    def item(self, pid, text, num=None, ilvl=0, style=None, extra='', before='', track=None):
        """A numbered paragraph: by w:numPr (num, ilvl) or by its style; its id bookmarked as cn_<id>."""
        ppr = '<w:pStyle w:val="%s"/>' % style if style else ''
        if num is not None:
            ppr += '<w:numPr><w:ilvl w:val="%d"/><w:numId w:val="%d"/></w:numPr>' % (ilvl, num)
        content = self.run(pid) + self.run(' ' + text)
        if track == 'del':
            ppr += '<w:rPr><w:del w:id="%d" w:author="cn" w:date="%s"/></w:rPr>' % (self.nid(), DATE)
            content = ('<w:del w:id="%d" w:author="cn" w:date="%s"><w:r><w:delText xml:space="preserve">%s %s</w:delText></w:r></w:del>'
                       % (self.nid(), DATE, pid, text))
            body = self.bookmark('cn_' + pid, content)
        elif track == 'ins':
            ppr += '<w:rPr><w:ins w:id="%d" w:author="cn" w:date="%s"/></w:rPr>' % (self.nid(), DATE)
            body = '<w:ins w:id="%d" w:author="cn" w:date="%s">%s</w:ins>' % (
                self.nid(), DATE, self.bookmark('cn_' + pid, self.run(pid)) + self.run(' ' + text))
        else:
            body = self.bookmark('cn_' + pid, self.run(pid)) + self.run(' ' + text)
        self.order.append(pid)
        return self.para(before + body + (extra if self.probe else ''), ppr)

    def items(self, rows):
        for row in rows:
            self.add(self.item(*row[:2], **row[2]) if len(row) > 2 else self.item(*row))

    def relative(self, targets, switches=r'\r'):
        """The "r:" fields of a paragraph that cites the targets in relative context (or other switches)."""
        key = 'r' if switches == r'\r' else switches.replace('\\', '').replace(' ', '')
        return self.run(' %s:' % key) + ''.join(self.run(' %s=' % t) + self.field(r'REF cn_%s %s' % (t, switches)) for t in targets)

def lists(d):
    """The lists, the same in both documents; in cn-1 the paragraphs named "from" cite others by \\r."""
    L = ['L%d' % i for i in range(1, 14)]
    d.case('Case L: legal, 1. / 1.01 / (a) / (i), each level its own text, the levels above by %1.%2 at level 1 only')
    rows = [('L1', 'level 0', dict(num=1)), ('L2', 'level 1', dict(num=1, ilvl=1)), ('L3', 'level 1', dict(num=1, ilvl=1)),
            ('L4', 'level 0', dict(num=1)), ('L5', 'level 1', dict(num=1, ilvl=1)),
            ('L6', 'level 2', dict(num=1, ilvl=2, extra=d.relative(['L3', 'L5', 'L7', 'L9', 'L11']))),
            ('L7', 'level 3', dict(num=1, ilvl=3, extra=d.relative(['L6', 'L8', 'L9', 'L11', 'L4']))),
            ('L8', 'level 3', dict(num=1, ilvl=3, extra=d.relative(L[:12]))),
            ('L9', 'level 2', dict(num=1, ilvl=2, extra=d.relative(L[:12]) + d.relative(['L6', 'L7', 'L11'], r'\w'))),
            ('L10', 'level 1', dict(num=1, ilvl=1, before=d.bookmark('cn_E2', ''))),
            ('L11', 'level 2', dict(num=1, ilvl=2, extra=d.relative(L[:12]))),
            ('L12', 'level 0', dict(num=1, extra=d.relative(L[:12])))]
    d.items(rows)
    d.add('<w:tbl><w:tblPr><w:tblW w:w="0" w:type="auto"/><w:tblBorders><w:top w:val="single" w:sz="4" w:space="0" w:color="auto"/>'
          '<w:left w:val="single" w:sz="4" w:space="0" w:color="auto"/><w:bottom w:val="single" w:sz="4" w:space="0" w:color="auto"/>'
          '<w:right w:val="single" w:sz="4" w:space="0" w:color="auto"/></w:tblBorders></w:tblPr><w:tblGrid><w:gridCol w:w="8000"/></w:tblGrid>'
          '<w:tr><w:tc><w:tcPr><w:tcW w:w="8000" w:type="dxa"/></w:tcPr>%s</w:tc></w:tr></w:tbl>'
          % d.item('L13', 'level 1, in a table cell', num=1, ilvl=1, extra=d.relative(['L12', 'L6'])))
    d.plain('After case L.')

    d.case('Case O: Word\'s outline, 1. / a. / i., each level its own counter and a period')
    d.items([('O1', 'level 0', dict(num=2)), ('O2', 'level 1', dict(num=2, ilvl=1, extra=d.relative(['L6', 'L5', 'L4']))),
             ('O3', 'level 2', dict(num=2, ilvl=2)), ('O4', 'level 2', dict(num=2, ilvl=2, extra=d.relative(['O1', 'O2', 'O3', 'O5', 'O6', 'O7']))),
             ('O5', 'level 1', dict(num=2, ilvl=1)), ('O6', 'level 0', dict(num=2)),
             ('O7', 'level 1', dict(num=2, ilvl=1, extra=d.relative(['O1', 'O2', 'O3', 'O4', 'O5', 'O6'])))])

    d.case('Case P: 1) / a) / i), each level its own counter and a parenthesis')
    d.items([('P1', 'level 0', dict(num=3)), ('P2', 'level 1', dict(num=3, ilvl=1)), ('P3', 'level 2', dict(num=3, ilvl=2)),
             ('P4', 'level 2', dict(num=3, ilvl=2, extra=d.relative(['P1', 'P2', 'P3', 'P5', 'P6', 'P7']))),
             ('P5', 'level 1', dict(num=3, ilvl=1)), ('P6', 'level 0', dict(num=3)),
             ('P7', 'level 1', dict(num=3, ilvl=1, extra=d.relative(['P1', 'P2', 'P3', 'P4', 'P5', 'P6'])))])

    d.case('Case H: 1. / 1.1. / 1.1.1., each level showing the levels above')
    d.items([('H1', 'level 0', dict(num=4)), ('H2', 'level 1', dict(num=4, ilvl=1)), ('H3', 'level 2', dict(num=4, ilvl=2)),
             ('H4', 'level 2', dict(num=4, ilvl=2, extra=d.relative(['H1', 'H2', 'H3', 'H5', 'H6', 'H7']))),
             ('H5', 'level 1', dict(num=4, ilvl=1)), ('H6', 'level 0', dict(num=4)),
             ('H7', 'level 1', dict(num=4, ilvl=1, extra=d.relative(['H1', 'H2', 'H3', 'H4', 'H5', 'H6'])))])

    d.case('Case A: headings linked to the list by their styles, Article I / Section 1.01 (legal) / (a)')
    d.items([('A1', 'Article heading', dict(style='ArticleHeading')), ('A2', 'Section heading', dict(style='SectionHeading')),
             ('A3', 'clause', dict(style='ClauseA')), ('A4', 'clause', dict(style='ClauseA')),
             ('A5', 'Article heading', dict(style='ArticleHeading')), ('A6', 'Section heading', dict(style='SectionHeading')),
             ('A7', 'clause', dict(style='ClauseA', extra=d.relative(['A1', 'A2', 'A3', 'A4', 'A5', 'A6', 'A8']))),
             ('A8', 'Section heading', dict(style='SectionHeading', extra=d.relative(['A1', 'A2', 'A3', 'A5', 'A6', 'A7'])))])

    d.case('Case Y: a list style (w:numStyleLink), 1. / 1.1')
    d.items([('Y1', 'level 0', dict(num=10)), ('Y2', 'level 1', dict(num=10, ilvl=1)), ('Y3', 'level 0', dict(num=10)),
             ('Y4', 'level 1', dict(num=10, ilvl=1, extra=d.relative(['Y1', 'Y2', 'Y3'])))])

    d.case('Case G: legal numbering (w:isLgl) at level 1 under upper roman, I. / 1.1 / (a)')
    d.items([('G1', 'level 0', dict(num=6)), ('G2', 'level 1', dict(num=6, ilvl=1)), ('G3', 'level 2', dict(num=6, ilvl=2)),
             ('G4', 'level 0', dict(num=6)), ('G5', 'level 2, level 1 skipped', dict(num=6, ilvl=2, extra=d.relative(['G1', 'G2', 'G3', 'G4'])))])

    d.case('Case S: levels skipped, case L\'s definitions in a list of their own')
    d.items([('S1', 'level 0', dict(num=7)), ('S2', 'level 2, level 1 skipped', dict(num=7, ilvl=2)),
             ('S3', 'level 3', dict(num=7, ilvl=3)), ('S4', 'level 1', dict(num=7, ilvl=1)),
             ('S5', 'level 3, level 2 skipped', dict(num=7, ilvl=3, extra=d.relative(['S1', 'S2', 'S3', 'S4'])))])

    d.case('Case R: a list restarted (a second w:num, start override 1), so two paragraphs show 1.')
    d.items([('R1', 'level 0', dict(num=8)), ('R2', 'level 1', dict(num=8, ilvl=1)), ('R3', 'level 0', dict(num=8))])
    d.plain('Between the two lists of case R.')
    d.items([('R4', 'level 0, restarted', dict(num=11)), ('R5', 'level 1', dict(num=11, ilvl=1, extra=d.relative(['R1', 'R2', 'R3', 'R4'])))])

    d.case('Case M: words in the level text, Article I / 1. (no %1) / (a)')
    d.items([('M1', 'level 0', dict(num=12)), ('M2', 'level 1', dict(num=12, ilvl=1)), ('M3', 'level 2', dict(num=12, ilvl=2)),
             ('M4', 'level 1', dict(num=12, ilvl=1)), ('M5', 'level 0', dict(num=12, extra=d.relative(['M1', 'M2', 'M3', 'M4'])))])

    d.case('Case N: not numbers: a bullet, Step 1: / a., NOTE (format none), an empty level text, 1 / 1-1 / A')
    d.items([('N1', 'a bullet', dict(num=13)), ('N2', 'level 0', dict(num=14)), ('N3', 'level 1', dict(num=14, ilvl=1)),
             ('N4', 'format none, text NOTE', dict(num=15)), ('N5', 'format none, empty text', dict(num=16)),
             ('N6', 'level 0', dict(num=17)), ('N7', 'level 1', dict(num=17, ilvl=1)),
             ('N8', 'level 2', dict(num=17, ilvl=2, extra=d.relative(['N6', 'N7', 'N2', 'N3'])))])

    d.case('Case T: tracked changes, T2 deleted, T3 inserted (all markup shown)')
    d.items([('T1', 'level 0', dict(num=18)), ('T2', 'level 0, deleted', dict(num=18, track='del')),
             ('T3', 'level 1, inserted', dict(num=18, ilvl=1, track='ins')), ('T4', 'level 0', dict(num=18)),
             ('T5', 'level 1', dict(num=18, ilvl=1, extra=d.relative(['T1', 'T2', 'T3', 'T4'])))])

SWITCHES = [('w', r'\w'), ('n', r'\n'), ('r', r'\r'), ('wt', r'\w \t'), ('nt', r'\n \t'), ('rt', r'\r \t')]

def labels_doc():
    d = Doc(True)
    d.plain('CR-074 clause-numbers probe, cn-1. Each numbered paragraph\'s id is bookmarked cn_<id>; the fields read ? until Word updates them.')
    d.add(d.para(d.run('F0 before the lists: r:') + ''.join(d.run(' L%d=' % i) + d.field(r'REF cn_L%d \r' % i) for i in (1, 4, 5, 6, 7))))
    lists(d)
    d.case('Edge cases: E1 a bookmark on a paragraph not numbered; E2 an empty bookmark before L10\'s id; E3 a bookmark from L6 into L7')
    d.add(d.para(d.bookmark('cn_E1', d.run('E1')) + d.run(' not numbered')))
    # E3: starts in L6's paragraph text, ends in L7's; written as text markers here, placed by the patch below
    d.case('Field lines: one a numbered paragraph, then the edge cases; w = \\w, n = \\n, r = \\r, t = \\t; L also \\p, \\r \\p, \\w \\d "-"')
    for pid in d.order + ['E1', 'E2', 'E3']:
        body = d.run('F-%s' % pid)
        for key, sw in SWITCHES:
            body += d.run(' %s=' % key) + d.field(r'REF cn_%s %s' % (pid, sw))
        if pid in ('L6', 'L9', 'A3', 'A7'):
            for key, sw in (('p', r'\p'), ('rp', r'\r \p'), ('wd', r'\w \d "-"'), ('h', r'\w \h')):
                body += d.run(' %s=' % key) + d.field(r'REF cn_%s %s' % (pid, sw))
        d.add(d.para(body))
    return d

def insert_doc():
    d = Doc(False)
    d.plain('CR-074 clause-numbers probe, cn-2. The lists of cn-1 without bookmarks or fields; the script has Word insert cross-references at X1 to X6 and in L9 and L11.')
    lists(d)
    d.case('Where Word inserts its cross-references (clause-numbers-probe.ps1, part 2)')
    for x, what in (('X1', 'full, no and relative context, as hyperlinks, to L6, L9, L11, A3, A6, A7, R4, M3, N2, G3, S2, Y2:'),
                    ('X2', 'L6 in full context, not a hyperlink:'), ('X3', 'L6 relative, with above/below:'),
                    ('X4', 'L6 in full context, numbers separated by -:'), ('X5', 'L6 as paragraph text:'),
                    ('X6', 'L6 again in full context (a second reference to one paragraph):')):
        d.plain('%s %s' % (x, what))
    return d

def document(d):
    body = ''.join(d.parts)
    if d.probe:
        # E3: a bookmark from the end of L6's text into L7's
        i = d.nid()
        body = body.replace(d.run(' level 2') + d.run(' r:'), d.run(' level 2') + '<w:bookmarkStart w:id="%d" w:name="cn_E3"/>' % i + d.run(' r:'), 1)
        body = body.replace(d.run(' level 3') + d.run(' r:'), '<w:bookmarkEnd w:id="%d"/>' % i + d.run(' level 3') + d.run(' r:'), 1)
        assert 'cn_E3' in body and body.count('w:id="%d"' % i) == 2
    return (XML + '<w:document %s><w:body>%s<w:sectPr><w:pgSz w:w="11906" w:h="16838"/>'
            '<w:pgMar w:top="1134" w:right="1134" w:bottom="1134" w:left="1134" w:header="567" w:footer="567" w:gutter="0"/>'
            '</w:sectPr></w:body></w:document>' % (NS, body))

def put(z, name, text):
    # a fixed date, so that a rebuild is byte-identical
    info = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
    info.compress_type = zipfile.ZIP_DEFLATED
    z.writestr(info, text.encode('utf-8'))

def build(out):
    os.makedirs(out, exist_ok=True)
    for name, d in (('cn-1-labels', labels_doc()), ('cn-2-insert', insert_doc())):
        with zipfile.ZipFile(os.path.join(out, name + '.docx'), 'w', zipfile.ZIP_DEFLATED) as z:
            put(z, '[Content_Types].xml', CT)
            put(z, '_rels/.rels', RELS)
            put(z, 'word/_rels/document.xml.rels', DOC_RELS)
            put(z, 'word/document.xml', document(d))
            put(z, 'word/styles.xml', STYLES)
            put(z, 'word/numbering.xml', NUMBERING)
        fields = document(d).count('fldCharType="begin"') if d.probe else 0
        print('%s.docx: %d numbered paragraphs, %d fields' % (name, len(d.order), fields))

if __name__ == '__main__':
    build(sys.argv[1] if len(sys.argv) > 1 else '.')
