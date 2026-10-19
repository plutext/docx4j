#!/usr/bin/env python3
# Copyright 2026, Plutext Pty Ltd.
#
# This file is part of docx4j.
#
# docx4j is licensed under the Apache License, Version 2.0; see
# http://www.apache.org/licenses/LICENSE-2.0
"""The altered fonts of CR-050's inspection fixture (case 65, extraction-probe-inspect.docx).

Each is Liberation Sans 2.1.5 Regular (SIL OFL 1.1, Reserved Font Names Liberation, Arimo,
Tinos and Cousine), cut down to the characters its run holds, renamed (the licence forbids a
modified version the reserved names), and altered as its entry below says. Run with fontTools
4.66.1 from the repository root:

    python3 docx4j-core-tests/src/test/resources/text-extraction/support/inspect-fonts/make_fonts.py \
        /usr/share/fonts/liberation/LiberationSans-Regular.ttf

The output is deterministic (no timestamps, no FFTM table); README.md beside this script lists
each file's SHA-256 at generation.
"""
import copy
import os
import sys

from fontTools import subset
from fontTools.ttLib import TTFont, newTable
from fontTools.ttLib.tables import otTables
from fontTools.ttLib.tables._g_l_y_f import Glyph, GlyphComponent
from fontTools.ttLib.tables._c_m_a_p import cmap_format_4

HERE = os.path.dirname(os.path.abspath(__file__))
EPOCH = 0x7C259DC0  # 1970-01-01 in the head table's 1904 epoch: a fixed date, not the build's


def cut(source, text):
    """Liberation Sans reduced to the characters of text (and the space), its layout tables dropped."""
    font = TTFont(source)
    options = subset.Options()
    options.glyph_names = True
    options.hinting = False
    options.layout_features = []
    options.drop_tables += ['FFTM', 'GPOS', 'GSUB', 'GDEF', 'kern']
    options.name_IDs = ['*']
    options.notdef_outline = True
    subsetter = subset.Subsetter(options)
    subsetter.populate(text=text + ' ')
    subsetter.subset(font)
    return font


def rename(font, family, ps, description):
    name = font['name']
    keep = {0, 13, 14}
    name.names = [n for n in name.names if n.nameID in keep]
    for nid, value in ((1, family), (2, 'Regular'), (3, ps + ' 1.0 docx4j CR-050'), (4, family),
                       (5, 'Version 1.0; altered from Liberation Sans 2.1.5'), (6, ps), (10, description)):
        name.setName(value, nid, 3, 1, 0x409)
        name.setName(value, nid, 1, 0, 0)


def cmap_of(font):
    for table in font['cmap'].tables:
        if table.isUnicode():
            yield table


def remap(font, mapping):
    """mapping: character -> glyph name, applied to every Unicode cmap subtable."""
    for table in cmap_of(font):
        for ch, glyph in mapping.items():
            table.cmap[ord(ch)] = glyph


def add_glyph(font, name, glyph, advance):
    order = font.getGlyphOrder() + [name]
    font.setGlyphOrder(order)
    font['glyf'].glyphs[name] = glyph
    font['glyf'].glyphOrder = order
    font['hmtx'].metrics[name] = (advance, 0)


def empty_glyph():
    g = Glyph()
    g.numberOfContours = 0
    return g


def strip_names(font):
    font['post'].formatType = 3.0
    if hasattr(font['post'], 'extraNames'):
        font['post'].extraNames = []
    if hasattr(font['post'], 'mapping'):
        font['post'].mapping = {}


def finish(font, file):
    font['head'].created = EPOCH
    font['head'].modified = EPOCH
    font['OS/2'].fsType = 0
    font.recalcTimestamp = False
    font.save(os.path.join(HERE, file), reorderTables=True)
    print('wrote', file)


def word_drawn_as_another(source, family, ps, file):
    """CAT is drawn DOG: C's glyph draws D, O and G (an advance over 2 em); A and T draw nothing."""
    font = cut(source, 'CATDOG')
    glyf, hmtx = font['glyf'], font['hmtx']
    composite = Glyph()
    composite.numberOfContours = -1
    composite.components = []
    x = 0
    for letter in 'DOG':
        c = GlyphComponent()
        c.glyphName, c.x, c.y, c.flags = letter, x, 0, 0x0003  # ARGS_ARE_WORDS | ARGS_ARE_XY_VALUES
        composite.components.append(c)
        x += hmtx[letter][0]
    add_glyph(font, 'C.word', composite, x)
    add_glyph(font, 'blank0', empty_glyph(), 0)
    remap(font, {'C': 'C.word', 'A': 'blank0', 'T': 'blank0'})
    rename(font, family, ps, 'Altered for the docx4j inspection probe (CR-050): CAT is drawn DOG.')
    font['glyf'].glyphs['C.word'].recalcBounds(glyf)
    finish(font, file)


def twin_plain(source):
    """Inspect Probe Twin as made: the font of that name a viewer has installed, for the precedence check."""
    font = cut(source, 'CATDOG')
    rename(font, 'Inspect Probe Twin', 'InspectProbeTwin',
           'Liberation Sans glyphs, renamed, unaltered: install it to see which face a viewer prefers (CR-050).')
    finish(font, 'inspect-probe-twin-installed.ttf')


def blank(source):
    """Every letter drawn as nothing, at an ordinary advance."""
    text = 'INVISBLEK'
    font = cut(source, text)
    add_glyph(font, 'blank', empty_glyph(), 1139)
    remap(font, {ch: 'blank' for ch in text})
    rename(font, 'Inspect Probe Blank', 'InspectProbeBlank',
           'Altered for the docx4j inspection probe (CR-050): every letter is drawn as nothing.')
    finish(font, 'inspect-probe-blank.ttf')


def zero(source):
    """X keeps its outline at an advance of zero, so it is drawn over the next letter."""
    font = cut(source, 'MIXED')
    glyph = copy.deepcopy(font['glyf']['X'])
    add_glyph(font, 'X.zero', glyph, 0)
    remap(font, {'X': 'X.zero'})
    rename(font, 'Inspect Probe Zero', 'InspectProbeZero',
           'Altered for the docx4j inspection probe (CR-050): X has no advance and overprints the next letter.')
    finish(font, 'inspect-probe-zero.ttf')


def named(source):
    """A and M swapped in the cmap, the glyph names kept: A's glyph is named M, M's A."""
    font = cut(source, 'MAT')
    remap(font, {'A': 'M', 'M': 'A'})
    rename(font, 'Inspect Probe Named', 'InspectProbeNamed',
           'Altered for the docx4j inspection probe (CR-050): A and M swapped, glyph names kept.')
    finish(font, 'inspect-probe-named.ttf')


def shared(source):
    """E drawn with F's glyph: two letters share one glyph; no glyph names."""
    font = cut(source, 'FE')
    remap(font, {'E': 'F'})
    strip_names(font)
    rename(font, 'Inspect Probe Shared', 'InspectProbeShared',
           'Altered for the docx4j inspection probe (CR-050): E is drawn with F\'s glyph.')
    finish(font, 'inspect-probe-shared.ttf')


def careful(source):
    """S and Z swapped: every glyph unique, of ordinary width, no glyph names, no substitution."""
    font = cut(source, 'SIZE')
    remap(font, {'S': 'Z', 'Z': 'S'})
    strip_names(font)
    rename(font, 'Inspect Probe Careful', 'InspectProbeCareful',
           'Altered for the docx4j inspection probe (CR-050): S and Z swapped with care (unique glyphs, no names).')
    finish(font, 'inspect-probe-careful.ttf')


def plain(source):
    """Renamed only (the reserved name may not be kept), the glyphs as the foundry made them."""
    font = cut(source, 'PLAIN')
    rename(font, 'Inspect Probe Plain', 'InspectProbePlain',
           'Liberation Sans glyphs, renamed, unaltered (CR-050 inspection probe).')
    finish(font, 'inspect-probe-plain.ttf')


def symbol(source):
    """Symbol-encoded: the letters at U+F020 to U+F07E, in a (3,0) subtable, as a symbol font keeps them."""
    text = 'Aak'
    font = cut(source, text)
    best = font.getBestCmap()
    table = cmap_format_4(4)
    table.platformID, table.platEncID, table.language = 3, 0, 0
    table.cmap = {0xF000 + code: glyph for code, glyph in best.items() if 0x20 <= code <= 0x7E}
    font['cmap'].tables = [table]
    os2 = font['OS/2']
    os2.ulCodePageRange1 = 1 << 31   # symbol character set
    os2.ulCodePageRange2 = 0
    os2.usFirstCharIndex, os2.usLastCharIndex = 0xF020, 0xF07E
    rename(font, 'Inspect Probe Symbol', 'InspectProbeSymbol',
           'Liberation Sans glyphs, symbol-encoded at U+F020-U+F07E (CR-050 inspection probe).')
    finish(font, 'inspect-probe-symbol.ttf')


def single_substitution(mapping):
    lookup = otTables.Lookup()
    lookup.LookupType = 1
    lookup.LookupFlag = 0
    st = otTables.SingleSubst()
    st.mapping = dict(mapping)
    lookup.SubTable = [st]
    lookup.SubTableCount = 1
    return lookup


def substitution(source):
    """GSUB: ccmp swaps B for P's glyph, liga swaps D for O's glyph (single substitutions)."""
    font = cut(source, 'BOPDT')
    gsub = newTable('GSUB')
    table = otTables.GSUB()
    table.Version = 0x00010000
    table.LookupList = otTables.LookupList()
    table.LookupList.Lookup = [single_substitution({'B': 'P'}), single_substitution({'D': 'O'})]
    table.LookupList.LookupCount = 2
    table.FeatureList = otTables.FeatureList()
    records = []
    for tag, index in (('ccmp', 0), ('liga', 1)):
        record = otTables.FeatureRecord()
        record.FeatureTag = tag
        record.Feature = otTables.Feature()
        record.Feature.LookupListIndex = [index]
        record.Feature.LookupCount = 1
        records.append(record)
    table.FeatureList.FeatureRecord = records
    table.FeatureList.FeatureCount = 2
    table.ScriptList = otTables.ScriptList()
    scripts = []
    for tag in ('DFLT', 'latn'):
        sr = otTables.ScriptRecord()
        sr.ScriptTag = tag
        sr.Script = otTables.Script()
        sr.Script.DefaultLangSys = otTables.DefaultLangSys()
        sr.Script.DefaultLangSys.ReqFeatureIndex = 0xFFFF
        sr.Script.DefaultLangSys.FeatureIndex = [0, 1]
        sr.Script.DefaultLangSys.FeatureCount = 2
        sr.Script.LangSysRecord = []
        sr.Script.LangSysCount = 0
        scripts.append(sr)
    table.ScriptList.ScriptRecord = scripts
    table.ScriptList.ScriptCount = 2
    gsub.table = table
    font['GSUB'] = gsub
    rename(font, 'Inspect Probe Substitution', 'InspectProbeSubstitution',
           'Altered for the docx4j inspection probe (CR-050): ccmp draws B as P, liga draws D as O.')
    finish(font, 'inspect-probe-substitution.ttf')


def main(source):
    word_drawn_as_another(source, 'Inspect Probe Word', 'InspectProbeWord', 'inspect-probe-word.ttf')
    word_drawn_as_another(source, 'Inspect Probe Twin', 'InspectProbeTwin', 'inspect-probe-twin.ttf')
    twin_plain(source)
    blank(source)
    zero(source)
    named(source)
    shared(source)
    careful(source)
    plain(source)
    symbol(source)
    substitution(source)


if __name__ == '__main__':
    main(sys.argv[1] if len(sys.argv) > 1 else '/usr/share/fonts/liberation/LiberationSans-Regular.ttf')
