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
			throw new IllegalStateException("FOP's LineBreakPosition has changed; docx4j-fop-word-layout needs updating", e);
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
			throw new IllegalStateException("FOP's AlignmentContext has changed; docx4j-fop-word-layout needs updating", e);
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
			throw new IllegalStateException("FOP's AlignmentContext/InlineLayoutManager have changed; docx4j-fop-word-layout needs updating", e);
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
			throw new IllegalStateException("FOP's TextLayoutManager has changed; docx4j-fop-word-layout needs updating", e);
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
