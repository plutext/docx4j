# Copyright 2026, Plutext Pty Ltd.
# This file is part of docx4j.
"""Purpose-built probes for the questions Fable's review of CR-079 phase 1 left for Word (CR-079 section 6; WD REQ-317 to REQ-320).
Jason: "The small Word round for the review's open questions? yes". Each asks what Word does with a shape no round has had; docx4j
reads each by REQ-320 for now, with no text and part.unexpected_root. The package is build_mr.py's. Each file's words say where they stand.
usage: build_rq.py <staging dir> <set name>"""
import os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import build_mr
from build_mr import run, p, doc, root, SECT, XW, MC, W14

TR = f'<w:tr><w:tc><w:tcPr><w:tcW w:w="4000" w:type="dxa"/></w:tcPr>{p("ROW WORDS")}</w:tc></w:tr>'
SDT_PR = '<w:sdtPr><w:id w:val="12345"/></w:sdtPr>'

def branch(held):
    """mc:AlternateContent at the root, a w14 Choice holding what's asked about, a Fallback with a w:document of its own."""
    return (f'<mc:AlternateContent {MC} {XW} {W14}><mc:Choice Requires="w14">{held}</mc:Choice>'
            f'<mc:Fallback>{doc("FALLBACK WORDS")}</mc:Fallback></mc:AlternateContent>')

FILES = [
    ('rq-00-control', root('w:document', f'<w:body>{p("CONTROL WORDS")}{SECT}</w:body>')),
    # An unknown w: root holding what REQ-319's list doesn't name
    ('rq-10-madeup-ac', root('w:madeUp', f'<mc:AlternateContent><mc:Choice Requires="w14">{p("CHOICE WORDS")}</mc:Choice>'
                                           f'<mc:Fallback>{p("FALLBACK WORDS")}</mc:Fallback></mc:AlternateContent>', f'{MC} {XW} {W14}')),
    ('rq-11-madeup-sdt-runs', root('w:madeUp', f'<w:sdt>{SDT_PR}<w:sdtContent>{run("RUN CONTROL WORDS")}</w:sdtContent></w:sdt>')),
    # A w:sdtContent root that JAXB takes as a row control's
    ('rq-20-sdtcontent-empty', f'<w:sdtContent {XW}/>'),
    ('rq-21-sdtcontent-rows', root('w:sdtContent', f'<w:tbl><w:tblGrid><w:gridCol w:w="4000"/></w:tblGrid>{TR}</w:tbl>'.replace(
        '<w:tbl><w:tblGrid><w:gridCol w:w="4000"/></w:tblGrid>', '').replace('</w:tbl>', ''))),
    ('rq-22-sdtcontent-row-sdt', root('w:sdtContent', f'<w:sdt>{SDT_PR}<w:sdtContent>{TR}</w:sdtContent></w:sdt>')),
    # In the branch taken at the root
    ('rq-30-branch-sdt', branch(f'<w:sdt>{SDT_PR}<w:sdtContent>{p("BRANCH CONTROL WORDS")}</w:sdtContent></w:sdt>')),
    ('rq-31-branch-txbxcontent', branch(f'<w:txbxContent>{p("TEXTBOX WORDS")}</w:txbxContent>')),
    ('rq-32-branch-document-then-hdr', branch(doc('DOCUMENT WORDS') + f'<w:hdr>{p("HEADER WORDS")}</w:hdr>')),
    ('rq-33-branch-hdr-then-document', branch(f'<w:hdr>{p("HEADER WORDS")}</w:hdr>' + doc('DOCUMENT WORDS'))),
]

if __name__ == '__main__':
    build_mr.FILES = FILES
    build_mr.main(sys.argv[1], sys.argv[2])
