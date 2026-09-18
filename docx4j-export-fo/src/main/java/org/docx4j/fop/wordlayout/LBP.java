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

	// ---- LineBreakPosition: package-private constructor, private fields.  On the docx4j FO
	// renderer (hook inline-access) both are public and read through handles; on Apache FOP
	// by reflection, as before.  CR-020 phase 1.

	private static final java.lang.invoke.MethodHandle H_LBP_CTOR = FopHooks.constructor(FopHooks.INLINE_ACCESS,
			LineBreakPosition.class, LayoutManager.class, int.class, int.class, int.class, int.class, int.class,
			int.class, double.class, double.class, int.class, int.class, int.class, int.class, int.class, int.class,
			int.class);
	private static final java.lang.invoke.MethodHandle H_PAR_INDEX = lbpGetter("getParIndex"), H_START_INDEX = lbpGetter("getStartIndex"),
			H_AVAILABLE_SHRINK = lbpGetter("getAvailableShrink"), H_AVAILABLE_STRETCH = lbpGetter("getAvailableStretch"),
			H_DIFFERENCE = lbpGetter("getDifference"), H_D_ADJUST = lbpGetter("getDAdjust"),
			H_IPD_ADJUST = lbpGetter("getIpdAdjust"), H_START_INDENT = lbpGetter("getStartIndent"),
			H_END_INDENT = lbpGetter("getEndIndent"), H_LINE_HEIGHT = lbpGetter("getLineHeight"),
			H_LINE_WIDTH = lbpGetter("getLineWidth"), H_SPACE_BEFORE = lbpGetter("getSpaceBefore"),
			H_SPACE_AFTER = lbpGetter("getSpaceAfter"), H_BASELINE = lbpGetter("getBaseline");

	private static java.lang.invoke.MethodHandle lbpGetter(String name) {
		return FopHooks.method(FopHooks.INLINE_ACCESS, LineBreakPosition.class, name);
	}

	private static Constructor<LineBreakPosition> CTOR;
	private static Field PAR_INDEX, START_INDEX, AVAILABLE_SHRINK, AVAILABLE_STRETCH, DIFFERENCE,
			D_ADJUST, IPD_ADJUST, START_INDENT, END_INDENT, LINE_HEIGHT, LINE_WIDTH, SPACE_BEFORE, SPACE_AFTER, BASELINE;

	static {
		if (H_LBP_CTOR == null) {
			try {
				CTOR = LineBreakPosition.class.getDeclaredConstructor(LayoutManager.class, int.class, int.class, int.class,
						int.class, int.class, int.class, double.class, double.class, int.class, int.class, int.class,
						int.class, int.class, int.class, int.class);
				CTOR.setAccessible(true);
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException("FOP's LineBreakPosition has changed; org.docx4j.fop.wordlayout needs updating", e);
			}
		}
		PAR_INDEX = field("parIndex", H_PAR_INDEX);
		START_INDEX = field("startIndex", H_START_INDEX);
		AVAILABLE_SHRINK = field("availableShrink", H_AVAILABLE_SHRINK);
		AVAILABLE_STRETCH = field("availableStretch", H_AVAILABLE_STRETCH);
		DIFFERENCE = field("difference", H_DIFFERENCE);
		D_ADJUST = field("dAdjust", H_D_ADJUST);
		IPD_ADJUST = field("ipdAdjust", H_IPD_ADJUST);
		START_INDENT = field("startIndent", H_START_INDENT);
		END_INDENT = field("endIndent", H_END_INDENT);
		LINE_HEIGHT = field("lineHeight", H_LINE_HEIGHT);
		LINE_WIDTH = field("lineWidth", H_LINE_WIDTH);
		SPACE_BEFORE = field("spaceBefore", H_SPACE_BEFORE);
		SPACE_AFTER = field("spaceAfter", H_SPACE_AFTER);
		BASELINE = field("baseline", H_BASELINE);
	}

	/** The field, or null where the hook's handle stands in for it. */
	private static Field field(String name, java.lang.invoke.MethodHandle hook) {
		return hook != null ? null : FopHooks.field(LineBreakPosition.class, name, false);
	}

	private LBP() {}

	private static final java.lang.invoke.MethodHandle H_AC_CTOR = FopHooks.constructor(FopHooks.INLINE_ACCESS,
			org.apache.fop.layoutmgr.inline.AlignmentContext.class,
			org.apache.fop.fonts.Font.class, int.class, org.apache.fop.traits.WritingMode.class);
	private static Constructor<org.apache.fop.layoutmgr.inline.AlignmentContext> AC_CTOR;
	static {
		if (H_AC_CTOR == null) {
			try {
				AC_CTOR = org.apache.fop.layoutmgr.inline.AlignmentContext.class.getDeclaredConstructor(
						org.apache.fop.fonts.Font.class, int.class, org.apache.fop.traits.WritingMode.class);
				AC_CTOR.setAccessible(true);
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException("FOP's AlignmentContext has changed; org.docx4j.fop.wordlayout needs updating", e);
			}
		}
	}

	/** AlignmentContext(Font, int lineHeight, WritingMode) is package-private in Apache FOP
	 *  (public on the docx4j FO renderer, hook inline-access). */
	static org.apache.fop.layoutmgr.inline.AlignmentContext newAlignmentContext(org.apache.fop.fonts.Font font, int lineHeight,
			org.apache.fop.traits.WritingMode writingMode) {
		if (H_AC_CTOR != null) {
			return (org.apache.fop.layoutmgr.inline.AlignmentContext) FopHooks.call(H_AC_CTOR, font, lineHeight, writingMode);
		}
		try {
			return AC_CTOR.newInstance(font, lineHeight, writingMode);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	static LineBreakPosition create(LayoutManager lm, int index, int startIndex, int breakIndex,
			int shrink, int stretch, int diff, double ipdA, double adjust, int si,
			int ei, int lh, int lw, int sb, int sa, int bl) {
		if (H_LBP_CTOR != null) {
			return (LineBreakPosition) FopHooks.call(H_LBP_CTOR, lm, index, startIndex, breakIndex, shrink, stretch, diff,
					ipdA, adjust, si, ei, lh, lw, sb, sa, bl);
		}
		try {
			return CTOR.newInstance(lm, index, startIndex, breakIndex, shrink, stretch, diff, ipdA, adjust, si, ei, lh, lw, sb, sa, bl);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private static int i(java.lang.invoke.MethodHandle h, Field f, LineBreakPosition p) {
		if (h != null) return (Integer) FopHooks.call(h, p);
		try {
			return f.getInt(p);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	private static double d(java.lang.invoke.MethodHandle h, Field f, LineBreakPosition p) {
		if (h != null) return (Double) FopHooks.call(h, p);
		try {
			return f.getDouble(p);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	// ---- AlignmentContext.lineHeight and InlineLayoutManager.font (both private in Apache
	// FOP; getLineHeight() and getFont() on the docx4j FO renderer, hook inline-access)

	private static final java.lang.invoke.MethodHandle H_AC_LINE_HEIGHT = FopHooks.method(FopHooks.INLINE_ACCESS,
			org.apache.fop.layoutmgr.inline.AlignmentContext.class, "getLineHeight");
	private static final java.lang.invoke.MethodHandle H_ILM_FONT = FopHooks.method(FopHooks.INLINE_ACCESS,
			org.apache.fop.layoutmgr.inline.InlineLayoutManager.class, "getFont");
	private static final Field AC_LINE_HEIGHT = FopHooks.field(
			org.apache.fop.layoutmgr.inline.AlignmentContext.class, "lineHeight", H_AC_LINE_HEIGHT != null);
	private static final Field ILM_FONT = FopHooks.field(
			org.apache.fop.layoutmgr.inline.InlineLayoutManager.class, "font", H_ILM_FONT != null);

	/** The line-height the inline's alignment context was made with (its fo:inline's
	 *  line-height property, inherited if not set), in millipoints. */
	static int lineHeight(org.apache.fop.layoutmgr.inline.AlignmentContext ac) {
		if (H_AC_LINE_HEIGHT != null) return (Integer) FopHooks.call(H_AC_LINE_HEIGHT, ac);
		try {
			return AC_LINE_HEIGHT.getInt(ac);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	/** The font of an fo:inline's layout manager, or null for other managers. */
	static org.apache.fop.fonts.Font inlineFont(LayoutManager lm) {
		if (!(lm instanceof org.apache.fop.layoutmgr.inline.InlineLayoutManager)) return null;
		if (H_ILM_FONT != null) return (org.apache.fop.fonts.Font) FopHooks.call(H_ILM_FONT, lm);
		try {
			return (org.apache.fop.fonts.Font) ILM_FONT.get(lm);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	// ---- TextLayoutManager.mappings / letterSpaceIPD / foText / spaceCharIPD (private in
	// Apache FOP; getters on the docx4j FO renderer, hook inline-access)

	private static final java.lang.invoke.MethodHandle H_TLM_MAPPINGS = tlmGetter("getMappings"), H_TLM_LETTER_SPACE = tlmGetter("getLetterSpaceIPD"),
			H_TLM_FOTEXT = tlmGetter("getFOText"), H_TLM_SPACE_CHAR_IPD = tlmGetter("getSpaceCharIPD");

	private static java.lang.invoke.MethodHandle tlmGetter(String name) {
		return FopHooks.method(FopHooks.INLINE_ACCESS, org.apache.fop.layoutmgr.inline.TextLayoutManager.class, name);
	}

	private static final Field TLM_MAPPINGS = tlmField("mappings", H_TLM_MAPPINGS),
			TLM_LETTER_SPACE = tlmField("letterSpaceIPD", H_TLM_LETTER_SPACE),
			TLM_FOTEXT = tlmField("foText", H_TLM_FOTEXT),
			TLM_SPACE_CHAR_IPD = tlmField("spaceCharIPD", H_TLM_SPACE_CHAR_IPD);

	private static Field tlmField(String name, java.lang.invoke.MethodHandle hook) {
		return FopHooks.field(org.apache.fop.layoutmgr.inline.TextLayoutManager.class, name, hook != null);
	}

	@SuppressWarnings("unchecked")
	static java.util.List<org.apache.fop.fonts.GlyphMapping> mappings(org.apache.fop.layoutmgr.inline.TextLayoutManager tlm) {
		if (H_TLM_MAPPINGS != null) return (java.util.List<org.apache.fop.fonts.GlyphMapping>) FopHooks.call(H_TLM_MAPPINGS, tlm);
		try {
			return (java.util.List<org.apache.fop.fonts.GlyphMapping>) TLM_MAPPINGS.get(tlm);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	static org.apache.fop.traits.MinOptMax letterSpaceIPD(org.apache.fop.layoutmgr.inline.TextLayoutManager tlm) {
		if (H_TLM_LETTER_SPACE != null) return (org.apache.fop.traits.MinOptMax) FopHooks.call(H_TLM_LETTER_SPACE, tlm);
		try {
			return (org.apache.fop.traits.MinOptMax) TLM_LETTER_SPACE.get(tlm);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	/** The advance of the space character in the manager's space font, in millipoints. */
	static int spaceCharIPD(org.apache.fop.layoutmgr.inline.TextLayoutManager tlm) {
		if (H_TLM_SPACE_CHAR_IPD != null) return (Integer) FopHooks.call(H_TLM_SPACE_CHAR_IPD, tlm);
		try {
			return TLM_SPACE_CHAR_IPD.getInt(tlm);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	static org.apache.fop.fo.FOText foText(org.apache.fop.layoutmgr.inline.TextLayoutManager tlm) {
		if (H_TLM_FOTEXT != null) return (org.apache.fop.fo.FOText) FopHooks.call(H_TLM_FOTEXT, tlm);
		try {
			return (org.apache.fop.fo.FOText) TLM_FOTEXT.get(tlm);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	// ---- LeafPosition.leafPos (private, and no setter in Apache FOP; setLeafPos on the
	// docx4j FO renderer, hook inline-access)

	private static final java.lang.invoke.MethodHandle H_SET_LEAF_POS = FopHooks.method(FopHooks.INLINE_ACCESS,
			org.apache.fop.layoutmgr.LeafPosition.class, "setLeafPos", int.class);
	private static final Field LEAF_POS = FopHooks.field(
			org.apache.fop.layoutmgr.LeafPosition.class, "leafPos", H_SET_LEAF_POS != null);

	/** Move a position on to a different glyph mapping: splitting one mapping into
	 *  several shifts every later index, and a position is what carries the index.
	 *  @since 17.1.0 */
	static void setLeafPos(org.apache.fop.layoutmgr.LeafPosition p, int value) {
		if (H_SET_LEAF_POS != null) {
			FopHooks.call(H_SET_LEAF_POS, p, value);
			return;
		}
		try {
			LEAF_POS.setInt(p, value);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	// ---- LeafNodeLayoutManager.areaInfo / curArea and AreaInfo.ipdArea (all protected in
	// Apache FOP; getCurrentArea and setAreaInfoIPD on the docx4j FO renderer, hook
	// leader-placement; setCurrentArea is Apache's own public method, used on both)

	private static final java.lang.invoke.MethodHandle H_GET_CURRENT_AREA = FopHooks.method(FopHooks.LEADER_PLACEMENT,
			org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager.class, "getCurrentArea");
	private static final java.lang.invoke.MethodHandle H_SET_AREA_INFO_IPD = FopHooks.method(FopHooks.LEADER_PLACEMENT,
			org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager.class, "setAreaInfoIPD", org.apache.fop.traits.MinOptMax.class);
	private static final Field LNLM_AREA_INFO = FopHooks.field(
			org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager.class, "areaInfo", H_SET_AREA_INFO_IPD != null);
	private static final Field LNLM_CUR_AREA = FopHooks.field(
			org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager.class, "curArea", H_GET_CURRENT_AREA != null);
	private static final Field AREA_INFO_IPD;
	static {
		Field f = null;
		if (H_SET_AREA_INFO_IPD == null) {
			try {
				f = Class.forName("org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager$AreaInfo").getDeclaredField("ipdArea");
				f.setAccessible(true);
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException("FOP's LeafNodeLayoutManager has changed; org.docx4j.fop.wordlayout needs updating", e);
			}
		}
		AREA_INFO_IPD = f;
	}

	/** {@code curArea} of a leaf manager: through the hook, or the field.  Declared to throw
	 *  what the field path throws, so that every caller's handling stays as it was. */
	private static Object curAreaRaw(org.apache.fop.layoutmgr.LayoutManager lm) throws IllegalAccessException {
		if (H_GET_CURRENT_AREA != null) {
			return (lm instanceof org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager) ? FopHooks.call(H_GET_CURRENT_AREA, lm) : null;
		}
		return LNLM_CUR_AREA.get(lm);
	}

	/** {@code curArea} set, through LeafNodeLayoutManager's own public setter (no hook needed). */
	private static void setCurArea(org.apache.fop.layoutmgr.LayoutManager lm, org.apache.fop.area.inline.InlineArea area)
			throws IllegalAccessException {
		((org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager) lm).setCurrentArea(area);
	}

	/**
	 * Give a leaf inline manager (here: an fo:leader standing in for a tab) the width
	 * the line manager worked out.  Its area's IPD comes from its own stored AreaInfo
	 * at addAreas time, not from the Knuth element, so both have to be set.
	 */
	static void setLeafIPD(org.apache.fop.layoutmgr.LayoutManager lm, int ipd) {
		if (H_SET_AREA_INFO_IPD != null) {
			if (lm instanceof org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager) {
				FopHooks.call(H_SET_AREA_INFO_IPD, lm, org.apache.fop.traits.MinOptMax.getInstance(ipd));
			}
			return;
		}
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
	 *  @since 17.1.0 */
	static org.apache.fop.area.inline.InlineArea leafArea(org.apache.fop.layoutmgr.LayoutManager lm) {
		try {
			Object area = curAreaRaw(lm);
			return (area instanceof org.apache.fop.area.inline.InlineArea)
					? (org.apache.fop.area.inline.InlineArea) area : null;
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	// ---- the leader of the stop a tab actually reached -------------------------

	/** {@link #setLeaderPattern} kinds, as {@code w:leader} gives them.  The three
	 *  character kinds are drawn as a run of that character, which is what Word's own
	 *  PDF carries for them; {@link #LEADER_RULE} is the drawn line {@code w:leader="heavy"}
	 *  asks for.  @since 17.1.1 for the underscore and hyphen kinds */
	static final int LEADER_NONE = 0, LEADER_DOTS = 1, LEADER_RULE = 2,
			LEADER_UNDERSCORE = 3, LEADER_HYPHEN = 4, LEADER_MIDDLE_DOT = 5;

	/** The character a leader kind repeats, or 0 for a kind which is not a character.
	 *  Read off the {@code tab-leader-kinds} golden, where Word draws every kind but
	 *  {@code none} as characters: {@code .} and {@code ·} on their own advances,
	 *  {@code -}, and {@code _} for both underscore and heavy.  @since 17.1.1 */
	static char leaderChar(int kind) {
		switch (kind) {
		case LEADER_DOTS: return '.';
		case LEADER_MIDDLE_DOT: return '·';
		case LEADER_UNDERSCORE: return '_';
		case LEADER_HYPHEN: return '-';
		default: return 0;
		}
	}

	/** Whether this kind is drawn as a run of a character rather than as a rule. */
	static boolean isCharacterLeader(int kind) {
		return leaderChar(kind) != 0;
	}

	/** One 1/300 inch, the grid Word's layout works in, in millipoints. */
	static final int GRID_MPT = 240;

	/** An advance rounded to Word's 1/300 inch grid, which is the step its leader
	 *  characters take.  @since 17.1.1 */
	static int roundToGrid(int mpt) {
		if (mpt <= 0) return mpt;
		int units = (mpt + GRID_MPT / 2) / GRID_MPT;
		return units > 0 ? units * GRID_MPT : GRID_MPT;
	}

	/**
	 * Put a leader run FOP built on Word's step: its characters repeat on the advance
	 * rounded to the 1/300 inch grid, and the difference is written as a character spacing.
	 *
	 * <p>Read out of the tab-leader-kinds golden's stream, all in Calibri 11.04 - the
	 * paragraph mark's font.  A full stop and a middle dot advance 2.782pt and are drawn
	 * {@code 0.0979 Tc} apart, a step of 2.8816 (twelve cells); a hyphen advances 3.380 and
	 * is drawn {@code -0.0182 Tc} apart, 3.3616 (fourteen - so the advance is rounded, and
	 * not rounded up); an underscore advances 5.500 and is drawn {@code 0.0221 Tc} apart,
	 * 5.5222 (twenty-three).  The spacing can be negative, which a spacer cannot be, so it
	 * goes on the character.</p>
	 *
	 * <p>A leader the FO gave a pattern width of its own keeps it: that width is the
	 * author's, not the font's.</p>
	 *
	 * @since 17.1.1
	 */
	private static void setGridStep(Object area) {
		if (!WordLayoutCustomizer.leaderGrid()) return;
		if (!(area instanceof org.apache.fop.area.inline.FilledArea)) return;
		org.apache.fop.area.inline.FilledArea run = (org.apache.fop.area.inline.FilledArea) area;
		java.util.List<org.apache.fop.area.inline.InlineArea> kids = run.getChildAreas();
		if (kids == null || kids.size() != 1) return;         // a spacer: the FO's own width
		if (!(kids.get(0) instanceof org.apache.fop.area.inline.TextArea)) return;
		org.apache.fop.area.inline.TextArea c = (org.apache.fop.area.inline.TextArea) kids.get(0);
		if (run.getUnitWidth() != c.getIPD()) return;         // likewise
		if (gridStep(c, c.getIPD()) > 0) run.setUnitWidth(c.getIPD());
	}

	/** Step this leader character by {@code advance} rounded to Word's grid, as a character
	 *  spacing on the character itself; the step, or 0 where it is the advance already. */
	private static int gridStep(org.apache.fop.area.inline.TextArea c, int advance) {
		int pitch = roundToGrid(advance);
		if (pitch <= 0 || pitch == advance) return 0;
		c.setTextLetterSpaceAdjust(pitch - advance);
		c.setIPD(pitch);
		return pitch;
	}

	/**
	 * The blank Word leaves before the first character of a leader run whose step is
	 * {@code period}, where the run begins {@code from} millipoints from the <b>page's</b>
	 * left edge; 0 where the run begins where it is.
	 *
	 * <p>Word puts the run on a whole multiple of its step measured from the page edge, and
	 * takes the tab's start down to the 1/300 inch grid first.  On the tab-leader-kinds
	 * golden every run does sit on such a multiple: its hyphen runs open at 22, 46 and 45
	 * steps of 3.3612, its underscore runs at 14, 23, 26, 27, 30, 32 and 59 steps of 5.522,
	 * its dot and middle-dot runs at 25, 45, 47, 57 and 60 steps of 2.881.  The floor is
	 * what puts the P09 run at 46 steps (154.62) rather than 47 (157.98) when the text
	 * before it ends at 154.776 - a fraction of a cell past the 46th step - so that Word's
	 * run there opens a touch <em>behind</em> the text and carries no space.</p>
	 *
	 * @since 17.1.1
	 */
	static int gridPhase(int from, int period) {
		if (period <= 0 || from <= 0) return 0;
		int step = (period + GRID_MPT / 2) / GRID_MPT;      // the step, in grid cells
		if (step <= 0) return 0;
		int cells = from / GRID_MPT;                         // the tab's start, floored
		int start = ((cells + step - 1) / step) * step * GRID_MPT;
		int phase = start - from;
		return phase > 0 ? phase : 0;
	}

	/** LeaderLayoutManager.font (private in Apache FOP; getFont on the docx4j FO renderer,
	 *  hook leader-placement): the font its dots are drawn in. */
	private static final java.lang.invoke.MethodHandle H_LLM_FONT = FopHooks.method(FopHooks.LEADER_PLACEMENT,
			org.apache.fop.layoutmgr.inline.LeaderLayoutManager.class, "getFont");
	private static final Field LLM_FONT = FopHooks.field(
			org.apache.fop.layoutmgr.inline.LeaderLayoutManager.class, "font", H_LLM_FONT != null);

	/** Through the hook, or the field; declared to throw what the field path throws. */
	private static org.apache.fop.fonts.Font leaderFontRaw(org.apache.fop.layoutmgr.LayoutManager lm)
			throws IllegalAccessException {
		if (H_LLM_FONT != null) return (org.apache.fop.fonts.Font) FopHooks.call(H_LLM_FONT, lm);
		return (org.apache.fop.fonts.Font) LLM_FONT.get(lm);
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
	 * FO asked for, so the characters of a replaced area sit on the leader's rule
	 * thickness rather than on their own height.
	 *
	 * <p>XSL FO offers {@code dots} and {@code rule} and no other repeating glyph, so an
	 * underscore or hyphen leader asks FOP for a rule and is replaced here by a run of
	 * the character itself - which is what Word draws, and the only form of it that
	 * reaches the PDF's text layer.  {@code w:leader="heavy"} stays a rule.
	 *
	 * @param kind one of {@link #LEADER_NONE}, {@link #LEADER_DOTS}, {@link #LEADER_RULE},
	 *             {@link #LEADER_UNDERSCORE}, {@link #LEADER_HYPHEN}
	 * @since 17.1.0
	 */
	static void setLeaderPattern(org.apache.fop.layoutmgr.LayoutManager lm, int kind) {
		setLeaderPattern(lm, kind, 0, false);
	}

	/**
	 * As above, and where {@code spaces} is set a tab which reaches a stop with no leader
	 * carries a <b>space character</b> of its whole advance, as Word's PDF does, rather
	 * than a jump of the same width (CR-001 batch 45).
	 *
	 * @param width the tab's advance, in millipoints
	 * @since 17.1.1
	 */
	static void setLeaderPattern(org.apache.fop.layoutmgr.LayoutManager lm, int kind,
			int width, boolean spaces) {
		unphase(lm);
		if (kind == LEADER_NONE) {
			blankLeaderArea(lm, width, spaces);
			return;
		}
		if (!(lm.getFObj() instanceof org.apache.fop.fo.flow.Leader)) return;
		org.apache.fop.fo.flow.Leader fobj = (org.apache.fop.fo.flow.Leader) lm.getFObj();
		int pattern = fobj.getLeaderPattern();
		boolean asFopBuiltIt = (kind == LEADER_DOTS && pattern == org.apache.fop.fo.Constants.EN_DOTS)
				|| (kind == LEADER_RULE && pattern == org.apache.fop.fo.Constants.EN_RULE);
		try {
			Object area = curAreaRaw(lm);
			if (asFopBuiltIt) {
				// FOP built the area this stop wants, and it is the one that hangs on the
				// alignment context the FO asked for; all it needs is Word's step
				if (isCharacterLeader(kind)) setGridStep(area);
				return;
			}
			if (!(area instanceof org.apache.fop.area.inline.InlineArea)) return;
			org.apache.fop.area.inline.InlineArea old = (org.apache.fop.area.inline.InlineArea) area;
			int thickness = fobj.getRuleThickness().getValue(lm);
			char c = leaderChar(kind);
			org.apache.fop.area.inline.InlineArea fresh = c == 0
					? ruleArea(fobj, thickness, old.getBidiLevel())
					: charArea(lm, fobj, thickness, old.getBidiLevel(), c);
			if (fresh == null) return;
			setCurArea(lm, fresh);
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

	/** The area a leader of repeated characters is drawn as, built the way
	 *  {@code LeaderLayoutManager.getLeaderInlineArea} builds its dots.
	 *  @param c the character the leader repeats ('.', '_' or '-') */
	private static org.apache.fop.area.inline.InlineArea charArea(org.apache.fop.layoutmgr.LayoutManager lm,
			org.apache.fop.fo.flow.Leader fobj, int thickness, int level, char c) {
		org.apache.fop.fonts.Font font;
		try {
			font = leaderFontRaw(lm);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
		if (font == null || !font.hasChar(c)) return null;
		int width = font.getCharWidth(c);
		if (width <= 0) return null;
		org.apache.fop.area.inline.TextArea dot = new org.apache.fop.area.inline.TextArea();
		int[] levels = (level < 0) ? null : new int[] { level };
		dot.addWord(String.valueOf(c), width, null, levels, null, 0);
		dot.setIPD(width);
		dot.setBPD(width);
		// FOP would put the character's baseline at its own height; this area hangs on an
		// alignment context built for the pattern the FO asked for, whose height is the
		// leader's rule thickness, so the baseline goes there instead
		dot.setBaselineOffset(thickness);
		org.apache.fop.layoutmgr.TraitSetter.addFontTraits(dot, font);
		dot.addTrait(org.apache.fop.area.Trait.COLOR, fobj.getColor());
		if (WordLayoutCustomizer.leaderGrid() && gridStep(dot, width) > 0) width = dot.getIPD();
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

	// ---- Word's fixed dot grid ------------------------------------------------

	/**
	 * The area a tab's dot leader is drawn as, wrapping the blank Word's grid puts before
	 * the first dot around FOP's own repeating area.  A {@code FilledArea} repeats its unit
	 * from its own left edge, and {@code leader-alignment="reference-area"} - which is what
	 * XSL FO offers for the grid - is inert in FOP 2.11's PDF output, so the phase is a
	 * space of its own (see {@code WordLineLayoutManager.dotLeaderPhase}).
	 *
	 * <p>The dots, not this wrapper, are what a {@code TabPageNumberWidth} widens when the
	 * page number after the tab resolves: the growth then reaches this area through
	 * {@code notifyIPDVariation} and on to the line, so nothing else has to know.
	 *
	 * @since 17.1.0
	 */
	static final class PhasedLeaderArea extends org.apache.fop.area.inline.InlineParent {

		private static final long serialVersionUID = 1L;

		private final org.apache.fop.area.inline.InlineArea leader;

		PhasedLeaderArea(int phase, org.apache.fop.area.inline.InlineArea leader) {
			this.leader = leader;
			org.apache.fop.area.inline.Space gap = new org.apache.fop.area.inline.Space();
			gap.setIPD(phase);
			gap.setBPD(leader.getBPD());
			if (leader.getBidiLevel() >= 0) gap.setBidiLevel(leader.getBidiLevel());
			addChildArea(gap);
			addChildArea(leader);
			setBPD(leader.getBPD());
		}

		/** The same, with the blank at each end of the run written as a space character:
		 *  either end may be null.  @since 17.1.1 */
		PhasedLeaderArea(org.apache.fop.area.inline.InlineArea lead,
				org.apache.fop.area.inline.InlineArea leader,
				org.apache.fop.area.inline.InlineArea tail) {
			this.leader = leader;
			if (lead != null) addChildArea(lead);
			addChildArea(leader);
			if (tail != null) addChildArea(tail);
			setBPD(leader.getBPD());
			if (leader.getBidiLevel() >= 0) setBidiLevel(leader.getBidiLevel());
		}

		org.apache.fop.area.inline.InlineArea getLeader() {
			return leader;
		}
	}

	/** Take a leader area back out of its {@link PhasedLeaderArea}, so that a line laid out
	 *  again is not wrapped twice. */
	private static void unphase(org.apache.fop.layoutmgr.LayoutManager lm) {
		try {
			Object area = curAreaRaw(lm);
			if (area instanceof PhasedLeaderArea) {
				setCurArea(lm, ((PhasedLeaderArea) area).getLeader());
			}
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	/** The width of the unit a dot leader repeats - Word's dot grid period - or 0 where
	 *  this tab's area does not repeat one. */
	static int leaderUnitWidth(org.apache.fop.layoutmgr.LayoutManager lm) {
		try {
			Object area = curAreaRaw(lm);
			if (area instanceof PhasedLeaderArea) area = ((PhasedLeaderArea) area).getLeader();
			return (area instanceof org.apache.fop.area.inline.FilledArea)
					? ((org.apache.fop.area.inline.FilledArea) area).getUnitWidth() : 0;
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Start this tab's dot leader a blank of {@code phase} in, so that its dots fall on
	 * Word's grid; {@code phase} 0 leaves the area as FOP built it.
	 *
	 * @param width the tab's whole width, of which the leader keeps what the blank leaves
	 * @since 17.1.0
	 */
	static void setLeaderPhase(org.apache.fop.layoutmgr.LayoutManager lm, int phase, int width) {
		setLeaderPhase(lm, phase, width, false);
	}

	/**
	 * As above, and with {@code spaces} the blank at <b>each</b> end of the run is written
	 * as a space character, which is what Word's own PDF carries there.
	 *
	 * <p>Word writes one there, in a text object of its own: a corpus table of contents
	 * whose stop has an underscore leader carries
	 * {@code 1 0 0 1 154.87 609.79 Tm [( )] TJ} between the entry's text and
	 * {@code 1 0 0 1 156.07 609.79 Tm [(____...)] TJ}, and another at 564.22 before the
	 * page number at 569.74.  The run itself opens on Word's grid, a fraction of a cell
	 * after the text ({@code WordLineLayoutManager.dotLeaderPhase}), and the space fills
	 * that partial cell.  (The glyph draws no ink, so {@code mutool draw -F trace} does not
	 * list it; the claim is read out of the operators.)</p>
	 *
	 * <p>A space is written only where there <em>is</em> a partial cell.</p>
	 *
	 * @since 17.1.1
	 */
	static void setLeaderPhase(org.apache.fop.layoutmgr.LayoutManager lm, int phase, int width,
			boolean spaces) {
		unphase(lm);
		if (!spaces && (phase <= 0 || phase >= width)) return;
		if (phase < 0 || phase >= width) phase = 0;
		try {
			Object area = curAreaRaw(lm);
			if (!(area instanceof org.apache.fop.area.inline.FilledArea)) return;
			org.apache.fop.area.inline.FilledArea run = (org.apache.fop.area.inline.FilledArea) area;
			if (!spaces) {
				run.setIPD(width - phase);
				setCurArea(lm, new PhasedLeaderArea(phase, run));
				return;
			}
			int period = run.getUnitWidth();
			if (period <= 0) return;
			int units = (width - phase) / period;
			if (units <= 0) return;            // no room for a run: the tab stays as it is
			int runWidth = units * period;
			int tail = width - phase - runWidth;
			org.apache.fop.fonts.Font font = leaderFontRaw(lm);
			java.awt.Color colour = (lm.getFObj() instanceof org.apache.fop.fo.flow.Leader)
					? ((org.apache.fop.fo.flow.Leader) lm.getFObj()).getColor() : null;
			int baseline = 0;
			for (Object child : run.getChildAreas()) {
				if (child instanceof org.apache.fop.area.inline.AbstractTextArea) {
					baseline = ((org.apache.fop.area.inline.AbstractTextArea) child).getBaselineOffset();
					break;
				}
			}
			// only where there is a partial cell to write: Word's space is that cell, and
			// where its grid is already met it writes nothing there
			org.apache.fop.area.inline.TextArea lead = phase <= 0 ? null
					: spaceArea(font, colour, phase, run.getBPD(), baseline, run.getBidiLevel());
			org.apache.fop.area.inline.TextArea end = tail <= 0 ? null
					: spaceArea(font, colour, tail, run.getBPD(), baseline, run.getBidiLevel());
			if (lead == null && end == null) {
				if (phase <= 0) return;
				run.setIPD(width - phase);
				setCurArea(lm, new PhasedLeaderArea(phase, run));
				return;
			}
			run.setIPD(runWidth);
			setCurArea(lm, new PhasedLeaderArea(lead, run, end));
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * One space character of exactly this advance, in the leader's own font: the blank
	 * Word writes where docx4j jumped the pen.  The glyph is blank, so the character
	 * spacing which gives it its advance (a PDF {@code Tc}) moves nothing; what it changes
	 * is what the text layer says.
	 *
	 * @since 17.1.1
	 */
	private static org.apache.fop.area.inline.TextArea spaceArea(org.apache.fop.fonts.Font font,
			java.awt.Color colour, int advance, int bpd, int baselineOffset, int level) {
		if (font == null || !font.hasChar(' ') || advance < 0) return null;
		org.apache.fop.area.inline.TextArea space = new org.apache.fop.area.inline.TextArea();
		int[] levels = (level < 0) ? null : new int[] { level };
		space.addWord(" ", advance, null, levels, null, 0);
		space.setIPD(advance);
		space.setBPD(bpd);
		space.setBaselineOffset(baselineOffset);
		if (level >= 0) space.setBidiLevel(level);
		space.setTextLetterSpaceAdjust(advance - font.getCharWidth(' '));
		org.apache.fop.layoutmgr.TraitSetter.addFontTraits(space, font);
		if (colour != null) space.addTrait(org.apache.fop.area.Trait.COLOR, colour);
		return space;
	}

	/** The area a page number's width transfer must reach (WordLineLayoutManager's
	 *  TabPageNumberWidth): the dots themselves, inside their phase wrapper. */
	static org.apache.fop.area.inline.InlineArea phasedLeader(org.apache.fop.area.inline.InlineArea area) {
		return (area instanceof PhasedLeaderArea) ? ((PhasedLeaderArea) area).getLeader() : area;
	}

	/**
	 * Put a leader area which is <b>already placed on a line</b> on Word's grid: its
	 * characters step on the advance rounded to 1/300 inch, and the run begins on a whole
	 * multiple of that step measured from the page's left edge.
	 *
	 * <p>The manager variant ({@link #setLeaderPhase}) reaches into the leader's own layout
	 * manager, which is where a <b>tab</b> is settled.  A table-of-contents entry's
	 * stretching leader is not laid out by the line manager at all - the justification
	 * gives it its width - and nothing positional reaches its layout manager either
	 * (FOP 2.11 hands a leaf manager a {@code LayoutContext} carrying {@code refIPD}, the
	 * reference area's width, and {@code ipdAdjust}, the stretch factor, and nothing that
	 * says where on the line it sits).  So it is done here, from the line, where the x is
	 * known.</p>
	 *
	 * <p><b>The line's width does not change.</b>  The blank takes exactly what the dots
	 * give up: the run is shrunk by the phase and the phase is written in front of it, so
	 * the wrapper measures what the leader measured.  That matters twice - the line is
	 * already justified when this runs, and this leader is the thing that absorbed the
	 * slack.</p>
	 *
	 * @param run  the leader's filled area, as FOP built and stretched it
	 * @param from where the run begins, in millipoints from the <b>page's</b> left edge
	 * @return the area to put in the leader's place, or null where nothing is to change
	 * @since 17.1.1
	 */
	static org.apache.fop.area.inline.InlineArea gridPlacedLeader(
			org.apache.fop.area.inline.FilledArea run, int from) {
		return gridPlacedLeader(run, from, null);
	}

	/**
	 * As above, and with {@code lead} the blank in front of the run is written as a
	 * <b>space character</b> of that advance rather than as a jump of the pen.
	 *
	 * <p>That is what Word's own PDF carries between a numbering label and its leader
	 * run.  On the {@code numbering-leader-kinds} golden, every one of its five items
	 * shows three text objects where ours showed two - the label, then a space in the
	 * <b>leader's</b> face at the label's size, then the run:</p>
	 *
	 * <pre>
	 * /F1 11.04 Tf 1 0 0 1  90.048 705.55 Tm [(1.)] TJ                 LiberationSerif
	 * /F3 11.04 Tf 1 0 0 1  98.208 705.55 Tm [( )] TJ                  ArialMT
	 * /F3 11.04 Tf 1 0 0 1  99.888 705.55 Tm 0.0509 Tc[(..............)] TJ
	 * </pre>
	 *
	 * <p>The space stands at the label's own end and the run opens on Word's grid, so the
	 * space <em>is</em> the phase; and the glyph draws no ink, so {@code mutool draw -F
	 * trace} does not list it and the claim is read out of the operators.  It is the same
	 * construct the paragraph tab's {@link #setLeaderPhase(org.apache.fop.layoutmgr.LayoutManager,
	 * int, int, boolean)} already writes, and it takes the same switch
	 * ({@code WordLayoutCustomizer.tabSpaces}).</p>
	 *
	 * <p><b>Nothing moves.</b>  {@code InlineParent.addChildArea} adds each child's
	 * allocated width to the parent's, so a space of the phase's own advance measures
	 * exactly what the blank measured, and the run keeps the position and the width it
	 * had.</p>
	 *
	 * @param lead builds a space area of the advance it is given, or returns null
	 * @since 17.1.1 (CR-001 batch 49 item 1)
	 */
	static org.apache.fop.area.inline.InlineArea gridPlacedLeader(
			org.apache.fop.area.inline.FilledArea run, int from,
			java.util.function.IntFunction<org.apache.fop.area.inline.InlineArea> lead) {
		if (!WordLayoutCustomizer.leaderGrid()) return null;
		int period = gridPlacedStep(run);
		int width = run.getIPD();
		if (period <= 0 || width <= period) return null;
		int phase = gridPhase(from, period);
		if (phase <= 0 || phase >= width) return null;
		run.setIPD(width - phase);
		org.apache.fop.area.inline.InlineArea space = (lead == null) ? null : lead.apply(phase);
		return (space == null) ? new PhasedLeaderArea(phase, run)
				: new PhasedLeaderArea(space, run, null);
	}

	/**
	 * Word's step for a leader run which is <b>already on a line</b>, set on it; the step,
	 * or 0 where there is nothing to change.
	 *
	 * <p>{@link #setGridStep} cannot be used on a placed run.  FOP's
	 * {@code FilledArea.getChildAreas()} does not return the repeating <em>unit</em> - it
	 * returns the unit repeated {@code getIPD() / unitWidth} times, computed on the call -
	 * so on a leader whose width is settled it hands back fifty-odd copies and the
	 * one-child test which identifies the unit fails.  The unit itself is the protected
	 * {@code InlineParent.inlines}, which is what this reads.</p>
	 *
	 * <p>Changing the unit width does not change the run's width: the run keeps the IPD it
	 * was stretched to and simply holds whole cells of the new step, with the remainder at
	 * the end - which is what Word draws too.</p>
	 *
	 * @since 17.1.1
	 */
	private static int gridPlacedStep(org.apache.fop.area.inline.FilledArea run) {
		java.util.List<org.apache.fop.area.inline.InlineArea> unit = filledUnit(run);
		if (unit == null || unit.size() != 1) return run.getUnitWidth();
		/* The unit is the character itself for a leader FOP built from its dot pattern,
		 * and the inline its content was laid out by for one of leader-pattern
		 * use-content - which is how a numbering tab's hyphen, underscore and middle-dot
		 * leaders are written (XsltFOFunctions.numberingLeaderGlyph).  Every wrapper's own
		 * width has to move with the character's, or the unit and the run's unit width
		 * disagree and FOP paints the characters on the old step.  (CR-001 batch 48 item 2) */
		java.util.List<org.apache.fop.area.inline.InlineParent> wrappers =
				new java.util.ArrayList<org.apache.fop.area.inline.InlineParent>();
		org.apache.fop.area.inline.InlineArea inner = unit.get(0);
		/* TextArea is itself an InlineParent - its own children are the WordAreas - so the
		 * descent stops at it and not at the word inside it. */
		while (inner instanceof org.apache.fop.area.inline.InlineParent
				&& !(inner instanceof org.apache.fop.area.inline.FilledArea)
				&& !(inner instanceof org.apache.fop.area.inline.TextArea)) {
			org.apache.fop.area.inline.InlineParent wrapper = (org.apache.fop.area.inline.InlineParent) inner;
			java.util.List<org.apache.fop.area.inline.InlineArea> kids = wrapper.getChildAreas();
			if (kids == null || kids.size() != 1) return run.getUnitWidth();
			wrappers.add(wrapper);
			inner = kids.get(0);
		}
		if (!(inner instanceof org.apache.fop.area.inline.TextArea)) return run.getUnitWidth();
		org.apache.fop.area.inline.TextArea c = (org.apache.fop.area.inline.TextArea) inner;
		if (run.getUnitWidth() != c.getIPD()) return run.getUnitWidth();   // the FO's own width
		int step = gridStep(c, c.getIPD());
		if (step > 0) {
			for (org.apache.fop.area.inline.InlineParent wrapper : wrappers) wrapper.setIPD(step);
			run.setUnitWidth(step);
		}
		return run.getUnitWidth();
	}

	/** {@code InlineParent.inlines} (protected): a FilledArea's repeating unit, which its
	 *  own {@code getChildAreas} expands; {@code getUnitAreas()} on the docx4j FO renderer
	 *  (hook leader-placement). */
	private static final java.lang.invoke.MethodHandle H_UNIT_AREAS = FopHooks.method(FopHooks.LEADER_PLACEMENT,
			org.apache.fop.area.inline.FilledArea.class, "getUnitAreas");
	private static final Field IP_INLINES = FopHooks.field(
			org.apache.fop.area.inline.InlineParent.class, "inlines", H_UNIT_AREAS != null);

	@SuppressWarnings("unchecked")
	static java.util.List<org.apache.fop.area.inline.InlineArea> filledUnit(
			org.apache.fop.area.inline.FilledArea run) {
		if (H_UNIT_AREAS != null) return (java.util.List<org.apache.fop.area.inline.InlineArea>) FopHooks.call(H_UNIT_AREAS, run);
		try {
			return (java.util.List<org.apache.fop.area.inline.InlineArea>) IP_INLINES.get(run);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	/** Replace a leader's area with a plain space of the same height: the tab reached
	 *  a stop with no leader, but the FO could not know which stop that would be. */
	static void blankLeaderArea(org.apache.fop.layoutmgr.LayoutManager lm) {
		blankLeaderArea(lm, 0, false);
	}

	/** As above; with {@code spaces} the blank is a space <b>character</b> of the tab's
	 *  whole advance, which is what Word's PDF writes for a tab.  @since 17.1.1 */
	static void blankLeaderArea(org.apache.fop.layoutmgr.LayoutManager lm, int width, boolean spaces) {
		try {
			Object area = curAreaRaw(lm);
			if (!(area instanceof org.apache.fop.area.inline.InlineArea)) return;
			org.apache.fop.area.inline.InlineArea old = (org.apache.fop.area.inline.InlineArea) area;
			if (spaces && width > 0) {
				org.apache.fop.fonts.Font font = leaderFontRaw(lm);
				java.awt.Color colour = (lm.getFObj() instanceof org.apache.fop.fo.flow.Leader)
						? ((org.apache.fop.fo.flow.Leader) lm.getFObj()).getColor() : null;
				int baseline = (old instanceof org.apache.fop.area.inline.AbstractTextArea)
						? ((org.apache.fop.area.inline.AbstractTextArea) old).getBaselineOffset() : old.getBPD();
				org.apache.fop.area.inline.TextArea space
						= spaceArea(font, colour, width, old.getBPD(), baseline, old.getBidiLevel());
				if (space != null) {
					setCurArea(lm, space);
					return;
				}
			}
			if (area instanceof org.apache.fop.area.inline.Space) return;
			org.apache.fop.area.inline.Space blank = new org.apache.fop.area.inline.Space();
			blank.setBPD(old.getBPD());
			blank.setBidiLevel(old.getBidiLevel());
			setCurArea(lm, blank);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	static int parIndex(LineBreakPosition p) { return i(H_PAR_INDEX, PAR_INDEX, p); }
	static int startIndex(LineBreakPosition p) { return i(H_START_INDEX, START_INDEX, p); }
	static int availableShrink(LineBreakPosition p) { return i(H_AVAILABLE_SHRINK, AVAILABLE_SHRINK, p); }
	static int availableStretch(LineBreakPosition p) { return i(H_AVAILABLE_STRETCH, AVAILABLE_STRETCH, p); }
	static int difference(LineBreakPosition p) { return i(H_DIFFERENCE, DIFFERENCE, p); }
	static double dAdjust(LineBreakPosition p) { return d(H_D_ADJUST, D_ADJUST, p); }
	static double ipdAdjust(LineBreakPosition p) { return d(H_IPD_ADJUST, IPD_ADJUST, p); }
	static int startIndent(LineBreakPosition p) { return i(H_START_INDENT, START_INDENT, p); }
	static int endIndent(LineBreakPosition p) { return i(H_END_INDENT, END_INDENT, p); }
	static int lineHeight(LineBreakPosition p) { return i(H_LINE_HEIGHT, LINE_HEIGHT, p); }
	static int lineWidth(LineBreakPosition p) { return i(H_LINE_WIDTH, LINE_WIDTH, p); }
	static int spaceBefore(LineBreakPosition p) { return i(H_SPACE_BEFORE, SPACE_BEFORE, p); }
	static int spaceAfter(LineBreakPosition p) { return i(H_SPACE_AFTER, SPACE_AFTER, p); }
	static int baseline(LineBreakPosition p) { return i(H_BASELINE, BASELINE, p); }
}
