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
package org.docx4j.model;

import java.util.EnumMap;
import java.util.Map;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTCompat;
import org.docx4j.wml.CTSettings;

/**
 * A document's <code>w:settings/w:compat</code> layout switches, resolved as Word
 * resolves them: <b>the flag's own value where the document states one, else the value
 * this document's compatibility mode implies, else Word 365's default.</b>
 *
 * <p>Every <code>w:compat</code> child is a <code>CT_OnOff</code>, and by the schema an
 * absent element is <i>off</i>.  That is not how Word behaves.  Word records which layout
 * engine lays a document out separately, in
 * <code>w:compat/w:compatSetting[&#64;w:name="compatibilityMode"]</code> (11 = Word 2003,
 * 12 = Word 2007, 14 = Word 2010, 15 = Word 2013 and later), and that engine can apply its
 * own era's rules whether or not the corresponding flag is written.  So a rule keys on the
 * flag, and the mode supplies the flag's default where the document does not state it.</p>
 *
 * <p><b>What the mode actually supplies is narrower than it looks.</b>
 * <code>docx4j-export-fo/docs/word-layout-settings.md</code> &#xa7;2 tabulates which flags
 * Word <em>writes</em> at each mode, and it does not follow that the engine applies a flag
 * the document leaves out - measured, it does not (see {@link Flag}).  So every legacy flag
 * here defaults <b>off</b> and is honoured only where the document states it; the two
 * exceptions, {@link Flag#SPLIT_PG_BREAK_AND_PARA_MARK} and
 * {@link Flag#SUPPRESS_SP_BF_AFTER_PG_BRK}, have defaults that are themselves measurements
 * of Word 365's behaviour.  Each {@link Flag} says in its own javadoc where its default
 * comes from.</p>
 *
 * <p>Three-state care: {@link BooleanDefaultTrue#isVal()} returns a primitive, so through it
 * "absent" and "explicitly false" are indistinguishable - the trap
 * {@code org.docx4j.wml.AutospacingAccess} was written for.  Here the element itself is the
 * third state: a null child means "not stated" and takes the mode default, while
 * <code>&lt;w:growAutofit w:val="0"/&gt;</code> is stated, is false, and beats the mode
 * default.</p>
 *
 * <p>Immutable, and built once per conversion: it is {@link HyphenationSettings}'s twin, and
 * rides the same route (built beside it in the FO exporter, put on the conversion context,
 * passed into the FO fixups pass).</p>
 *
 * @since 17.0.6
 */
public final class CompatibilityOptions {

	/** Word's compatibility mode where the document states none: Word opens such a
	 *  document in compatibility mode, and 12 is what a docx4j-created package gets. */
	public static final int DEFAULT_MODE = 12;

	/** A mode threshold no real mode reaches, i.e. "in every mode". */
	private static final int ALWAYS = Integer.MAX_VALUE;
	/** A mode threshold no real mode falls below, i.e. "in no mode". */
	private static final int NEVER = Integer.MIN_VALUE;

	/**
	 * The <code>w:compat</code> flags the layout rules read (ECMA-376-1 &#xa7;17.15.1, one
	 * numbered subclause per element; the numbering shifts between editions, so each flag
	 * is cited by name and by the spec's own title for it).
	 *
	 * <p>Each carries the mode range in which Word applies it when the document does not
	 * state it: <code>onBelowMode</code> (the legacy era - Word's older engines apply the
	 * flag) and <code>onFromMode</code> (the rare flag whose behaviour <i>arrived</i> with a
	 * newer engine).  The default is
	 * <code>mode &lt; onBelowMode || mode &gt;= onFromMode</code>.</p>
	 *
	 * <p><b>The era flags default off in every mode</b>, which is measured rather than
	 * assumed and is not what word-layout-settings.md &#xa7;2's bundle table would suggest.
	 * That table says what Word <em>writes</em> at each mode; it does not follow that the
	 * engine applies a flag the document leaves out - measured, it does not, and measured,
	 * Word 365 does not apply one the document <em>does</em> write either (three mode-11
	 * corpus documents, word-layout-settings.md &#xa7;4(e)).  So a legacy flag is honoured
	 * only where the document states it, three of the rules that read one were measured and
	 * unkeyed again, and the two flags below whose defaults are <em>not</em> off are the two
	 * whose defaults are themselves measurements of Word's behaviour.</p>
	 */
	public enum Flag {

		/**
		 * <code>w:growAutofit</code>, "Allow Tables to AutoFit Into Page Margins".
		 * Word 2003 writes it; Word 2007 and later do not.  Off in every mode: see the
		 * note on the era flags above.  <b>No rule reads it</b>: keying
		 * {@code AbstractTableWriter.fitToAvailableWidth} on it was measured and rejected
		 * (word-layout-settings.md &#xa7;4(e)).
		 */
		GROW_AUTOFIT(NEVER, ALWAYS),

		/**
		 * <code>w:useWord2002TableStyleRules</code>, "Emulate Word 2002 Table Style Rules".
		 * Word 2003 writes it.  Off in every mode, <b>measured</b>, and <b>no rule reads
		 * it</b>: three mode-11 corpus documents carry a Word-2003 compat block, one
		 * without the flag and two with it, and Word applies the modern table style rules
		 * to all three (word-layout-settings.md &#xa7;4(e)).
		 */
		USE_WORD2002_TABLE_STYLE_RULES(NEVER, ALWAYS),

		/**
		 * <code>w:forgetLastTabAlignment</code>, "Ignore Width of Last Tab Stop When
		 * Aligning Paragraph If It Is Not Left Aligned".  Word 2003 writes it.  Off in
		 * every mode, and <b>no rule reads it</b>: keying word-layout-rules.md &#xa7;4.4jc
		 * on it was measured and rejected (word-layout-settings.md &#xa7;4(e)).
		 */
		FORGET_LAST_TAB_ALIGNMENT(NEVER, ALWAYS),

		/**
		 * <code>w:layoutRawTableWidth</code>, "Ignore Space Before Table When Deciding If
		 * Table Should Wrap Floating Object".  Word 2003 writes it.  Off in every mode.
		 */
		LAYOUT_RAW_TABLE_WIDTH(NEVER, ALWAYS),

		/**
		 * <code>w:doNotBreakWrappedTables</code>, "Do Not Allow Floating Tables To Break
		 * Across Pages".  Word 2003 writes it.  Off in every mode.
		 */
		DO_NOT_BREAK_WRAPPED_TABLES(NEVER, ALWAYS),

		/**
		 * <code>w:doNotExpandShiftReturn</code>, "Don't Justify Lines Ending in Soft Line
		 * Break".  One of the seven flags the Word 2007 default block writes, so a mode-11
		 * or mode-12 document which Word wrote states it explicitly (corroborated: all 32
		 * mode-12 documents of the reference corpus carry it).  Off in every mode where it
		 * is not stated: see the note on the era flags above.
		 */
		DO_NOT_EXPAND_SHIFT_RETURN(NEVER, ALWAYS),

		/**
		 * <code>w:doNotAutofitConstrainedTables</code>, "Do Not AutoFit Tables To Fit Next
		 * To Wrapped Objects".  No Word version writes it by default; off in every mode.
		 */
		DO_NOT_AUTOFIT_CONSTRAINED_TABLES(NEVER, ALWAYS),

		/**
		 * <code>w:autofitToFirstFixedWidthCell</code>, "Allow Table Columns To Exceed
		 * Preferred Widths of Constituent Cells".  Off in every mode.
		 */
		AUTOFIT_TO_FIRST_FIXED_WIDTH_CELL(NEVER, ALWAYS),

		/**
		 * <code>w:doNotUseIndentAsNumberingTabStop</code>, "Ignore Hanging Indent When
		 * Creating Tab Stop After Numbering".  Off in every mode, and <b>no rule reads
		 * it</b>: keying the list label column on it was measured and rejected
		 * (word-layout-settings.md &#xa7;4(d)).
		 */
		DO_NOT_USE_INDENT_AS_NUMBERING_TAB_STOP(NEVER, ALWAYS),

		/**
		 * <code>w:noTabHangInd</code>, "Do Not Create Custom Tab Stop for Hanging Indent".
		 * Off in every mode.
		 */
		NO_TAB_HANG_IND(NEVER, ALWAYS),

		/**
		 * <code>w:allowSpaceOfSameStyleInTable</code>, "Allow Contextual Spacing of
		 * Paragraphs in Tables".  Off in every mode: Word cancels a contextual paragraph's
		 * spacing at a cell's edges unless this is set.
		 */
		ALLOW_SPACE_OF_SAME_STYLE_IN_TABLE(NEVER, ALWAYS),

		/**
		 * <code>w:noLeading</code>, "Do Not Add Leading Between Lines of Text".  Off in
		 * every mode: Word's single-spacing pitch includes the font's external leading
		 * (word-layout-rules.md &#xa7;2.1).
		 */
		NO_LEADING(NEVER, ALWAYS),

		/**
		 * <code>w:splitPgBreakAndParaMark</code>, "Always Move Paragraph Mark to Page after
		 * a Page Break".  <b>Assumed on in every mode</b>: it is what Word 365 was measured
		 * doing for both a mode-12 and a mode-15 golden (word-layout-rules.md &#xa7;3.3 -
		 * the paragraph is split at the break, what precedes it staying on the page it is
		 * on), and no document of the reference corpus states the flag either way.  A
		 * document which states <code>w:val="0"</code> takes the whole paragraph to the
		 * next page, which is what docx4j did to 17.0.5.
		 */
		SPLIT_PG_BREAK_AND_PARA_MARK(ALWAYS, ALWAYS),

		/**
		 * <code>w:suppressSpBfAfterPgBrk</code>, "Do Not Use Space Before On First Line
		 * After a Page Break".  <b>Assumed on from mode 15</b> and off below it, which is
		 * the polarity measured against Word's goldens (word-layout-rules.md &#xa7;3.3: the
		 * space-before after a paragraph holding only a page break is dropped from mode 15
		 * and kept below it).  No document of the reference corpus states the flag, so
		 * which way Word reads it when it is stated is not established.
		 */
		SUPPRESS_SP_BF_AFTER_PG_BRK(NEVER, 15);

		private final int onBelowMode;
		private final int onFromMode;

		Flag(int onBelowMode, int onFromMode) {
			this.onBelowMode = onBelowMode;
			this.onFromMode = onFromMode;
		}

		/** Whether Word's engine for this mode applies the flag where the document does
		 *  not state it (word-layout-settings.md &#xa7;2). */
		public boolean defaultAt(int mode) {
			return mode < onBelowMode || mode >= onFromMode;
		}
	}

	private final int mode;
	private final int defaultMode;
	private final Map<Flag, Boolean> stated;
	private final CTSettings settings;

	private CompatibilityOptions(int mode, int defaultMode, Map<Flag, Boolean> stated,
			CTSettings settings) {
		this.mode = mode;
		this.defaultMode = defaultMode;
		this.stated = stated;
		this.settings = settings;
	}

	/**
	 * The options of this package.
	 *
	 * <p>A package with <b>no settings part at all</b>, or none which can be read, states
	 * nothing: its flags take Word 365's own defaults, i.e. mode 15's.  Its {@link #mode()}
	 * is still {@link #DEFAULT_MODE}, which is what
	 * {@link DocumentSettingsPart#getCompatibilityMode(WordprocessingMLPackage)} answers and
	 * what the mode-keyed rules are measured against - a document Word opens without a
	 * <code>compatibilityMode</code> setting is laid out by the older engine.</p>
	 */
	public static CompatibilityOptions of(WordprocessingMLPackage pkg) {
		CTSettings settings = settings(pkg);
		if (settings == null) {
			return new CompatibilityOptions(DEFAULT_MODE, 15,
					new EnumMap<Flag, Boolean>(Flag.class), null);
		}
		int mode = DocumentSettingsPart.getCompatibilityMode(pkg);
		return new CompatibilityOptions(mode, mode, stated(settings.getCompat()), settings);
	}

	/** The options a document of this compatibility mode which states no flag would have.
	 *  For tests and for a caller which has a mode but no package. */
	public static CompatibilityOptions ofMode(int mode) {
		return new CompatibilityOptions(mode, mode, new EnumMap<Flag, Boolean>(Flag.class), null);
	}

	/** The <code>w:compatSetting</code> compatibilityMode: 11, 12, 14 or 15. */
	public int mode() {
		return mode;
	}

	/**
	 * A <code>w:compat</code> flag: the document's own value where it states one, else the
	 * value this document's compatibility mode implies.
	 */
	public boolean is(Flag flag) {
		Boolean own = stated.get(flag);
		return own != null ? own.booleanValue() : flag.defaultAt(defaultMode);
	}

	/** Whether the document states this flag itself (as opposed to taking the mode's
	 *  default for it). */
	public boolean isStated(Flag flag) {
		return stated.containsKey(flag);
	}

	/**
	 * A <code>w:compatSetting</code> in the Word namespace, by name.
	 *
	 * @param modeDefault what to answer where the document names no such setting
	 */
	public boolean setting(String name, boolean modeDefault) {
		if (settings == null || settings.getCompat() == null) return modeDefault;
		for (org.docx4j.wml.CTCompatSetting cs : settings.getCompat().getCompatSetting()) {
			if (!"http://schemas.microsoft.com/office/word".equals(cs.getUri())) continue;
			if (!name.equals(cs.getName())) continue;
			String v = cs.getVal();
			if (v == null) return modeDefault;
			v = v.trim().toLowerCase();
			return "1".equals(v) || "true".equals(v) || "yes".equals(v) || "on".equals(v);
		}
		return modeDefault;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("compatibilityMode=").append(mode);
		for (Flag f : Flag.values()) {
			if (isStated(f)) sb.append(' ').append(f).append('=').append(is(f)).append("(stated)");
		}
		return sb.toString();
	}

	// ------------------------------------------------------------------ reading

	private static CTSettings settings(WordprocessingMLPackage pkg) {
		try {
			if (pkg == null || pkg.getMainDocumentPart() == null) return null;
			DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart();
			return dsp == null ? null : dsp.getContents();
		} catch (Exception e) {
			return null;
		}
	}

	/** The flags the document states, true or false; the ones it does not state are absent
	 *  from the map and take their mode default. */
	private static Map<Flag, Boolean> stated(CTCompat compat) {
		Map<Flag, Boolean> m = new EnumMap<Flag, Boolean>(Flag.class);
		if (compat == null) return m;
		for (Flag f : Flag.values()) {
			BooleanDefaultTrue b = element(compat, f);
			if (b != null) m.put(f, Boolean.valueOf(b.isVal()));
		}
		return m;
	}

	/** The flag's own element, or null where the document does not state it. */
	private static BooleanDefaultTrue element(CTCompat c, Flag f) {
		switch (f) {
		case GROW_AUTOFIT:                        return c.getGrowAutofit();
		case USE_WORD2002_TABLE_STYLE_RULES:      return c.getUseWord2002TableStyleRules();
		case FORGET_LAST_TAB_ALIGNMENT:           return c.getForgetLastTabAlignment();
		case LAYOUT_RAW_TABLE_WIDTH:              return c.getLayoutRawTableWidth();
		case DO_NOT_BREAK_WRAPPED_TABLES:         return c.getDoNotBreakWrappedTables();
		case DO_NOT_EXPAND_SHIFT_RETURN:          return c.getDoNotExpandShiftReturn();
		case DO_NOT_AUTOFIT_CONSTRAINED_TABLES:   return c.getDoNotAutofitConstrainedTables();
		case AUTOFIT_TO_FIRST_FIXED_WIDTH_CELL:   return c.getAutofitToFirstFixedWidthCell();
		case DO_NOT_USE_INDENT_AS_NUMBERING_TAB_STOP: return c.getDoNotUseIndentAsNumberingTabStop();
		case NO_TAB_HANG_IND:                     return c.getNoTabHangInd();
		case ALLOW_SPACE_OF_SAME_STYLE_IN_TABLE:  return c.getAllowSpaceOfSameStyleInTable();
		case NO_LEADING:                          return c.getNoLeading();
		case SPLIT_PG_BREAK_AND_PARA_MARK:        return c.getSplitPgBreakAndParaMark();
		case SUPPRESS_SP_BF_AFTER_PG_BRK:         return c.getSuppressSpBfAfterPgBrk();
		default:                                  return null;
		}
	}
}
