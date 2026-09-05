/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.fop.wordlayout;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.inline.LineLayoutManager.LineBreakPosition;

/**
 * Access to {@link LineBreakPosition}, whose constructor is package-private
 * and whose fields are private.  The rest of FOP (RestartAtLM,
 * LineLayoutPossibilities) recognises only that class, so
 * {@link WordLineLayoutManager} must create real instances rather than its
 * own.  fop-core is an automatic module, so reflection into it needs no
 * --add-opens.
 */
final class LBP {

	private static final Constructor<LineBreakPosition> CTOR;
	private static final Field PAR_INDEX, START_INDEX, AVAILABLE_SHRINK, AVAILABLE_STRETCH, DIFFERENCE,
			D_ADJUST, IPD_ADJUST, START_INDENT, END_INDENT, LINE_HEIGHT, LINE_WIDTH, SPACE_BEFORE, SPACE_AFTER, BASELINE;

	static {
		try {
			CTOR = LineBreakPosition.class.getDeclaredConstructor(LayoutManager.class, int.class, int.class, int.class,
					int.class, int.class, int.class, double.class, double.class, int.class, int.class, int.class,
					int.class, int.class, int.class, int.class);
			CTOR.setAccessible(true);
			PAR_INDEX = field("parIndex");
			START_INDEX = field("startIndex");
			AVAILABLE_SHRINK = field("availableShrink");
			AVAILABLE_STRETCH = field("availableStretch");
			DIFFERENCE = field("difference");
			D_ADJUST = field("dAdjust");
			IPD_ADJUST = field("ipdAdjust");
			START_INDENT = field("startIndent");
			END_INDENT = field("endIndent");
			LINE_HEIGHT = field("lineHeight");
			LINE_WIDTH = field("lineWidth");
			SPACE_BEFORE = field("spaceBefore");
			SPACE_AFTER = field("spaceAfter");
			BASELINE = field("baseline");
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("FOP's LineBreakPosition has changed; org.docx4j.fop.wordlayout needs updating", e);
		}
	}

	private static Field field(String name) throws NoSuchFieldException {
		Field f = LineBreakPosition.class.getDeclaredField(name);
		f.setAccessible(true);
		return f;
	}

	private LBP() {}

	private static final Constructor<org.apache.fop.layoutmgr.inline.AlignmentContext> AC_CTOR;
	static {
		try {
			AC_CTOR = org.apache.fop.layoutmgr.inline.AlignmentContext.class.getDeclaredConstructor(
					org.apache.fop.fonts.Font.class, int.class, org.apache.fop.traits.WritingMode.class);
			AC_CTOR.setAccessible(true);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("FOP's AlignmentContext has changed; org.docx4j.fop.wordlayout needs updating", e);
		}
	}

	/** AlignmentContext(Font, int lineHeight, WritingMode) is package-private. */
	static org.apache.fop.layoutmgr.inline.AlignmentContext newAlignmentContext(org.apache.fop.fonts.Font font, int lineHeight,
			org.apache.fop.traits.WritingMode writingMode) {
		try {
			return AC_CTOR.newInstance(font, lineHeight, writingMode);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	static LineBreakPosition create(LayoutManager lm, int index, int startIndex, int breakIndex,
			int shrink, int stretch, int diff, double ipdA, double adjust, int si,
			int ei, int lh, int lw, int sb, int sa, int bl) {
		try {
			return CTOR.newInstance(lm, index, startIndex, breakIndex, shrink, stretch, diff, ipdA, adjust, si, ei, lh, lw, sb, sa, bl);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private static int i(Field f, LineBreakPosition p) {
		try {
			return f.getInt(p);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	private static double d(Field f, LineBreakPosition p) {
		try {
			return f.getDouble(p);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	// ---- AlignmentContext.lineHeight and InlineLayoutManager.font (both private)

	private static final Field AC_LINE_HEIGHT, ILM_FONT;
	static {
		try {
			AC_LINE_HEIGHT = org.apache.fop.layoutmgr.inline.AlignmentContext.class.getDeclaredField("lineHeight");
			AC_LINE_HEIGHT.setAccessible(true);
			ILM_FONT = org.apache.fop.layoutmgr.inline.InlineLayoutManager.class.getDeclaredField("font");
			ILM_FONT.setAccessible(true);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("FOP's AlignmentContext/InlineLayoutManager have changed; org.docx4j.fop.wordlayout needs updating", e);
		}
	}

	/** The line-height the inline's alignment context was made with (its fo:inline's
	 *  line-height property, inherited if not set), in millipoints. */
	static int lineHeight(org.apache.fop.layoutmgr.inline.AlignmentContext ac) {
		try {
			return AC_LINE_HEIGHT.getInt(ac);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	/** The font of an fo:inline's layout manager, or null for other managers. */
	static org.apache.fop.fonts.Font inlineFont(LayoutManager lm) {
		if (!(lm instanceof org.apache.fop.layoutmgr.inline.InlineLayoutManager)) return null;
		try {
			return (org.apache.fop.fonts.Font) ILM_FONT.get(lm);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	// ---- TextLayoutManager.mappings / letterSpaceIPD / foText (private)

	private static final Field TLM_MAPPINGS, TLM_LETTER_SPACE, TLM_FOTEXT;
	static {
		try {
			TLM_MAPPINGS = org.apache.fop.layoutmgr.inline.TextLayoutManager.class.getDeclaredField("mappings");
			TLM_MAPPINGS.setAccessible(true);
			TLM_LETTER_SPACE = org.apache.fop.layoutmgr.inline.TextLayoutManager.class.getDeclaredField("letterSpaceIPD");
			TLM_LETTER_SPACE.setAccessible(true);
			TLM_FOTEXT = org.apache.fop.layoutmgr.inline.TextLayoutManager.class.getDeclaredField("foText");
			TLM_FOTEXT.setAccessible(true);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("FOP's TextLayoutManager has changed; org.docx4j.fop.wordlayout needs updating", e);
		}
	}

	@SuppressWarnings("unchecked")
	static java.util.List<org.apache.fop.fonts.GlyphMapping> mappings(org.apache.fop.layoutmgr.inline.TextLayoutManager tlm) {
		try {
			return (java.util.List<org.apache.fop.fonts.GlyphMapping>) TLM_MAPPINGS.get(tlm);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	static org.apache.fop.traits.MinOptMax letterSpaceIPD(org.apache.fop.layoutmgr.inline.TextLayoutManager tlm) {
		try {
			return (org.apache.fop.traits.MinOptMax) TLM_LETTER_SPACE.get(tlm);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	static org.apache.fop.fo.FOText foText(org.apache.fop.layoutmgr.inline.TextLayoutManager tlm) {
		try {
			return (org.apache.fop.fo.FOText) TLM_FOTEXT.get(tlm);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	// ---- LeafNodeLayoutManager.areaInfo / curArea and AreaInfo.ipdArea (all protected)

	private static final Field LNLM_AREA_INFO, LNLM_CUR_AREA, AREA_INFO_IPD;
	static {
		try {
			LNLM_AREA_INFO = org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager.class.getDeclaredField("areaInfo");
			LNLM_AREA_INFO.setAccessible(true);
			LNLM_CUR_AREA = org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager.class.getDeclaredField("curArea");
			LNLM_CUR_AREA.setAccessible(true);
			AREA_INFO_IPD = Class.forName("org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager$AreaInfo")
					.getDeclaredField("ipdArea");
			AREA_INFO_IPD.setAccessible(true);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("FOP's LeafNodeLayoutManager has changed; org.docx4j.fop.wordlayout needs updating", e);
		}
	}

	/**
	 * Give a leaf inline manager (here: an fo:leader standing in for a tab) the width
	 * the line manager worked out.  Its area's IPD comes from its own stored AreaInfo
	 * at addAreas time, not from the Knuth element, so both have to be set.
	 */
	static void setLeafIPD(org.apache.fop.layoutmgr.LayoutManager lm, int ipd) {
		try {
			Object areaInfo = LNLM_AREA_INFO.get(lm);
			if (areaInfo == null) return;
			AREA_INFO_IPD.set(areaInfo, org.apache.fop.traits.MinOptMax.getInstance(ipd));
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	/** The area a leaf inline manager (an fo:leader standing in for a tab) will add to
	 *  the line, so that it can be found again once the line's areas exist.
	 *  @since 17.0.6 */
	static org.apache.fop.area.inline.InlineArea leafArea(org.apache.fop.layoutmgr.LayoutManager lm) {
		try {
			Object area = LNLM_CUR_AREA.get(lm);
			return (area instanceof org.apache.fop.area.inline.InlineArea)
					? (org.apache.fop.area.inline.InlineArea) area : null;
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	// ---- the leader of the stop a tab actually reached -------------------------

	/** {@link #setLeaderPattern} kinds, as {@code w:leader} gives them. */
	static final int LEADER_NONE = 0, LEADER_DOTS = 1, LEADER_RULE = 2;

	/** LeaderLayoutManager.font (private): the font its dots are drawn in. */
	private static final Field LLM_FONT;
	static {
		try {
			LLM_FONT = org.apache.fop.layoutmgr.inline.LeaderLayoutManager.class.getDeclaredField("font");
			LLM_FONT.setAccessible(true);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("FOP's LeaderLayoutManager has changed; org.docx4j.fop.wordlayout needs updating", e);
		}
	}

	/**
	 * Give a tab's leader the leader of the stop it turned out to reach.
	 *
	 * <p>The FO cannot know which stop that will be, so it asks FOP for the paragraph's
	 * own leader (XsltFOFunctions.tabLeaderPattern) and the line manager settles it
	 * here: a stop with no leader blanks the area, and one whose leader is the other
	 * kind - a paragraph mixing dot and rule stops - gets an area built the way
	 * {@code LeaderLayoutManager.getLeaderInlineArea} builds it.  The replacement is
	 * hung on the leader's own alignment context, which FOP made for the pattern the
	 * FO asked for, so the dots of a replaced area sit on the leader's rule thickness
	 * rather than on their own height.
	 *
	 * @param kind one of {@link #LEADER_NONE}, {@link #LEADER_DOTS}, {@link #LEADER_RULE}
	 * @since 17.0.6
	 */
	static void setLeaderPattern(org.apache.fop.layoutmgr.LayoutManager lm, int kind) {
		if (kind == LEADER_NONE) {
			blankLeaderArea(lm);
			return;
		}
		if (!(lm.getFObj() instanceof org.apache.fop.fo.flow.Leader)) return;
		org.apache.fop.fo.flow.Leader fobj = (org.apache.fop.fo.flow.Leader) lm.getFObj();
		int pattern = fobj.getLeaderPattern();
		if ((kind == LEADER_DOTS && pattern == org.apache.fop.fo.Constants.EN_DOTS)
				|| (kind == LEADER_RULE && pattern == org.apache.fop.fo.Constants.EN_RULE)) {
			return;   // FOP already built the area this stop wants
		}
		try {
			Object area = LNLM_CUR_AREA.get(lm);
			if (!(area instanceof org.apache.fop.area.inline.InlineArea)) return;
			org.apache.fop.area.inline.InlineArea old = (org.apache.fop.area.inline.InlineArea) area;
			int thickness = fobj.getRuleThickness().getValue(lm);
			org.apache.fop.area.inline.InlineArea fresh = kind == LEADER_RULE
					? ruleArea(fobj, thickness, old.getBidiLevel())
					: dotsArea(lm, fobj, thickness, old.getBidiLevel());
			if (fresh == null) return;
			LNLM_CUR_AREA.set(lm, fresh);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	private static org.apache.fop.area.inline.InlineArea ruleArea(org.apache.fop.fo.flow.Leader fobj,
			int thickness, int level) {
		if (fobj.getRuleStyle() == org.apache.fop.fo.Constants.EN_NONE) return null;
		org.apache.fop.area.inline.Leader rule = new org.apache.fop.area.inline.Leader();
		rule.setRuleStyle(fobj.getRuleStyle());
		rule.setRuleThickness(thickness);
		rule.setBPD(thickness);
		rule.addTrait(org.apache.fop.area.Trait.COLOR, fobj.getColor());
		if (level >= 0) rule.setBidiLevel(level);
		return rule;
	}

	private static org.apache.fop.area.inline.InlineArea dotsArea(org.apache.fop.layoutmgr.LayoutManager lm,
			org.apache.fop.fo.flow.Leader fobj, int thickness, int level) {
		org.apache.fop.fonts.Font font;
		try {
			font = (org.apache.fop.fonts.Font) LLM_FONT.get(lm);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
		if (font == null) return null;
		int width = font.getCharWidth('.');
		if (width <= 0) return null;
		org.apache.fop.area.inline.TextArea dot = new org.apache.fop.area.inline.TextArea();
		int[] levels = (level < 0) ? null : new int[] { level };
		dot.addWord(".", width, null, levels, null, 0);
		dot.setIPD(width);
		dot.setBPD(width);
		// FOP would put the dot's baseline at its own height; this area hangs on an
		// alignment context built for the pattern the FO asked for, whose height is the
		// leader's rule thickness, so the baseline goes there instead
		dot.setBaselineOffset(thickness);
		org.apache.fop.layoutmgr.TraitSetter.addFontTraits(dot, font);
		dot.addTrait(org.apache.fop.area.Trait.COLOR, fobj.getColor());
		org.apache.fop.area.inline.Space spacer = null;
		int patternWidth = fobj.getLeaderPatternWidth().getValue(lm);
		if (patternWidth > width) {
			spacer = new org.apache.fop.area.inline.Space();
			spacer.setIPD(patternWidth - width);
			if (level >= 0) spacer.setBidiLevel(level);
			width = patternWidth;
		}
		org.apache.fop.area.inline.FilledArea filled = new org.apache.fop.area.inline.FilledArea();
		filled.setUnitWidth(width);
		filled.addChildArea(dot);
		if (spacer != null) filled.addChildArea(spacer);
		filled.setBPD(dot.getBPD());
		if (level >= 0) filled.setBidiLevel(level);
		return filled;
	}

	/** Replace a leader's area with a plain space of the same height: the tab reached
	 *  a stop with no leader, but the FO could not know which stop that would be. */
	static void blankLeaderArea(org.apache.fop.layoutmgr.LayoutManager lm) {
		try {
			Object area = LNLM_CUR_AREA.get(lm);
			if (!(area instanceof org.apache.fop.area.inline.InlineArea)
					|| area instanceof org.apache.fop.area.inline.Space) return;
			org.apache.fop.area.inline.InlineArea old = (org.apache.fop.area.inline.InlineArea) area;
			org.apache.fop.area.inline.Space blank = new org.apache.fop.area.inline.Space();
			blank.setBPD(old.getBPD());
			blank.setBidiLevel(old.getBidiLevel());
			LNLM_CUR_AREA.set(lm, blank);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	static int parIndex(LineBreakPosition p) { return i(PAR_INDEX, p); }
	static int startIndex(LineBreakPosition p) { return i(START_INDEX, p); }
	static int availableShrink(LineBreakPosition p) { return i(AVAILABLE_SHRINK, p); }
	static int availableStretch(LineBreakPosition p) { return i(AVAILABLE_STRETCH, p); }
	static int difference(LineBreakPosition p) { return i(DIFFERENCE, p); }
	static double dAdjust(LineBreakPosition p) { return d(D_ADJUST, p); }
	static double ipdAdjust(LineBreakPosition p) { return d(IPD_ADJUST, p); }
	static int startIndent(LineBreakPosition p) { return i(START_INDENT, p); }
	static int endIndent(LineBreakPosition p) { return i(END_INDENT, p); }
	static int lineHeight(LineBreakPosition p) { return i(LINE_HEIGHT, p); }
	static int lineWidth(LineBreakPosition p) { return i(LINE_WIDTH, p); }
	static int spaceBefore(LineBreakPosition p) { return i(SPACE_BEFORE, p); }
	static int spaceAfter(LineBreakPosition p) { return i(SPACE_AFTER, p); }
	static int baseline(LineBreakPosition p) { return i(BASELINE, p); }
}
