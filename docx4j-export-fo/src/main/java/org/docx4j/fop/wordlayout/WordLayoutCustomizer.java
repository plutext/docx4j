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

import org.apache.fop.apps.FopFactoryBuilder;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.fo.renderers.FopFactoryCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers Word-style layout (line breaking and line placement) with every
 * FopFactory docx4j builds.  Part of docx4j-export-fo, found through
 * ServiceLoader (META-INF/services), and on by default; nothing has to be
 * added to the classpath.  Turn it off with the docx4j property
 * docx4j.convert.out.fo.wordLayout=false, which restores plain FOP layout.
 */
public class WordLayoutCustomizer implements FopFactoryCustomizer {

	private static final Logger log = LoggerFactory.getLogger(WordLayoutCustomizer.class);

	public static final String PROPERTY = "docx4j.convert.out.fo.wordLayout";

	/** How far the spaces of a justified line may be compressed to fit one more word,
	 *  as a fraction of their natural width; docx4j property or system property
	 *  docx4j.convert.out.fo.wordLayout.maxSpaceShrink.
	 *
	 *  <p>0.245, which is where the two justification-crossover probes put Word's own
	 *  floor.  Each of their cases is a two-line justified paragraph whose next word is
	 *  built, with TextMeasurer on Carlito at 11pt, to need every space at exactly one
	 *  stated fraction of nominal; Word brought the word up at 0.8010, 0.7803, 0.7797,
	 *  0.7612, 0.7607 and <b>0.7586</b>, and left it at <b>0.7500</b>, 0.7401, 0.7400,
	 *  0.7398, 0.7214, 0.7201 and below - so its floor is in (0.7500, 0.7586], and 0.245
	 *  is the middle of that band.  The same crossover for 5, 12 and 20 spaces on the
	 *  line, so the limit is per space and carries no line-length term; and Word took a
	 *  5.00 x stretch rather than compress past it, so it is a floor and not a judgement
	 *  weighing the alternative.
	 *
	 *  <p>0.24 - the previous value, measured against the justified probe, at which it
	 *  broke 98% of its lines as Word does (0.20 gives 78%, 0.30 gives 74%) - is a floor
	 *  of 0.760, which refuses the 0.7586 Word accepts.  The change is 0.005, well inside
	 *  that measurement's resolution and now pinned to under 1%.
	 *
	 *  <p>This is the limit for a document Word lays out with its 2013 engine
	 *  (w:compatSetting compatibilityMode 15).  Older documents get 0: docx4j writes
	 *  docx4j:space-shrink="0" on fo:root for them and the line manager caps this
	 *  value with it. */
	public static final String MAX_SPACE_SHRINK = "docx4j.convert.out.fo.wordLayout.maxSpaceShrink";

	public static final double DEFAULT_MAX_SPACE_SHRINK = 0.245;

	public static double maxSpaceShrink() {
		Double v = configuredMaxSpaceShrink();
		return v == null ? DEFAULT_MAX_SPACE_SHRINK : v.doubleValue();
	}

	/** How far the spaces of a justified line may be compressed to take a longer
	 *  <em>hyphenation fragment</em> - the part of a hyphenated word which stays on the
	 *  line.  Measured against Word 365 (the hyphenation probes' goldens): Word accepted
	 *  a fragment costing 1.2% and 6.0% of the line's spaces and rejected 13.5%, 14.5%,
	 *  22.0% and 25.6%, where it will compress by up to 20.5% to pull a whole word on
	 *  (see {@link #MAX_SPACE_SHRINK}).  docx4j property or system property
	 *  docx4j.convert.out.fo.wordLayout.maxHyphenSpaceShrink; default 0.10.
	 *  @since 17.1.0 */
	public static final String MAX_HYPHEN_SPACE_SHRINK
			= "docx4j.convert.out.fo.wordLayout.maxHyphenSpaceShrink";

	public static final double DEFAULT_MAX_HYPHEN_SPACE_SHRINK = 0.10;

	public static double maxHyphenSpaceShrink() {
		Double v = doubleProperty(MAX_HYPHEN_SPACE_SHRINK);
		return v == null ? DEFAULT_MAX_HYPHEN_SPACE_SHRINK : v.doubleValue();
	}

	/** How stretched the alternative line has to be before Word compresses instead.
	 *  Word does <em>not</em> compress a justified line to pull one more word on merely
	 *  because the compression is inside {@link #MAX_SPACE_SHRINK}; it does so only when
	 *  the line it would otherwise leave is very loose.  Measured (CR-001 §4.2, corpus
	 *  batch 1): on three corpus lines Word refused compressions of 22.5%, 15.1% and
	 *  13.3% and took a line stretched by 38.5%, 16.1% and 12.8% instead, while in the
	 *  <code>break-justified</code> golden it compressed lines whose alternative was far
	 *  looser again.  Swept on that probe: 0 and 0.1 break 98% of its lines as Word
	 *  does, <b>0.2 and 0.3 break 100%</b>, 0.5 gives 83% and 0.7 gives 57%; on the
	 *  batch-1 documents carrying these lines 0.3 is also the maximum (77.5% -> 80.2% of
	 *  lines matched).  docx4j property or system property
	 *  docx4j.convert.out.fo.wordLayout.minStretchToCompress; 0 restores 17.0.5's
	 *  behaviour of compressing whenever the shrink is within the cap.
	 *  @since 17.1.0 */
	public static final String MIN_STRETCH_TO_COMPRESS
			= "docx4j.convert.out.fo.wordLayout.minStretchToCompress";

	public static final double DEFAULT_MIN_STRETCH_TO_COMPRESS = 0.30;

	public static double minStretchToCompress() {
		Double v = doubleProperty(MIN_STRETCH_TO_COMPRESS);
		return v == null ? DEFAULT_MIN_STRETCH_TO_COMPRESS : v.doubleValue();
	}

	/** Whether <code>w:hyphenationZone</code> is enforced as the largest gap Word will
	 *  leave at a line end before hyphenating.  Measured on the two hyphenation probes,
	 *  whose zones are 18pt and 36pt: Word's line breaks are the same in both, and it
	 *  hyphenated lines whose gap without the hyphen was 16.71pt to 34.09pt - inside the
	 *  36pt zone - so the zone never fired.  Off by default; set the docx4j property or
	 *  system property docx4j.convert.out.fo.wordLayout.hyphenationZone to true to
	 *  restore the behaviour docx4j had in 17.0.5.
	 *  @since 17.1.0 */
	public static final String ENFORCE_HYPHENATION_ZONE
			= "docx4j.convert.out.fo.wordLayout.hyphenationZone";

	public static boolean enforceHyphenationZone() {
		String v = System.getProperty(ENFORCE_HYPHENATION_ZONE);
		if (v == null) {
			return Docx4jProperties.getProperty(ENFORCE_HYPHENATION_ZONE, false);
		}
		return Boolean.parseBoolean(v.trim());
	}

	/**
	 * Whether a list label's excess ascent is measured against the paragraph's own
	 * ascent (the {@code docx4j:baseline} the FO exporter wrote from the document
	 * font's metrics) rather than against whatever ascent the line's runs report at
	 * layout time.
	 *
	 * <p>The two differ when a substitute renders the run and its document font is
	 * not the one the span names: the run's ascent is then the substitute's own share
	 * of the pitch (Arimo 1.043/1.432 = 0.728) where the exporter measured the label
	 * against the document font's (Arial 0.938/1.150 = 0.815), so a label in the very
	 * font of the text looked 1pt taller than the line and every numbered paragraph
	 * grew by that much.  Measured on a corpus contract set in Arial 10pt with 6pt
	 * before and after: Word's gap from the last line of one item to the first of the
	 * next is 17.52pt (11.52 + 6.00) and ours was 18.51pt, 89 such boundaries in the
	 * document; with the label's excess measured against the block's baseline the
	 * first line is 11.499pt again, not 12.505pt.
	 *
	 * <p>On by default; docx4j property or system property
	 * docx4j.convert.out.fo.wordLayout.labelAscentAgainstBaseline=false restores
	 * 17.1.0's behaviour.
	 *
	 * @since 17.1.1
	 */
	public static final String LABEL_ASCENT_AGAINST_BASELINE
			= "docx4j.convert.out.fo.wordLayout.labelAscentAgainstBaseline";

	public static boolean labelAscentAgainstBaseline() {
		String v = System.getProperty(LABEL_ASCENT_AGAINST_BASELINE);
		if (v == null) {
			return Docx4jProperties.getProperty(LABEL_ASCENT_AGAINST_BASELINE, true);
		}
		return Boolean.parseBoolean(v.trim());
	}

	/**
	 * Whether a line which comes out the box {@code docx4j:line-box} declares takes the
	 * baseline {@code docx4j:baseline} declares with it, rather than the ascent its runs
	 * report.
	 *
	 * <p>The two are a pair the FO exporter measured together from the document font.  A
	 * run's ascent is a share of the pitch read from the <em>run's</em> metrics, and where
	 * the paragraph's box was grown - an East Asian face takes 1.3 times its usWin box
	 * (CR-001 batch 43) - that share is taken from a winAscent which was not grown with
	 * it, so the line keeps the declared height and loses the declared baseline.
	 *
	 * <p>Measured on a generated reproduction of a corpus document's shape, a bulleted
	 * item whose Symbol label this machine substitutes with DejaVu Serif and whose 9pt
	 * body run names Meiryo, which it lacks: the body block declares
	 * {@code docx4j:line-box="17.55pt" docx4j:baseline="9.541pt"} and its line came out
	 * {@code ascent=13689 descent=3861} - the box exactly, the baseline 4.148pt low -
	 * where the label block beside it came out {@code ascent=9540}, its own declared
	 * baseline.  The bullet therefore sat 4.15pt above its text, which is the offset that
	 * corpus document shows over 854 of its labels.
	 *
	 * <p>On by default; docx4j property or system property
	 * docx4j.convert.out.fo.wordLayout.lineBaselineFromBlock=false restores the run's
	 * ascent.
	 *
	 * @since 17.1.1
	 */
	public static final String LINE_BASELINE_FROM_BLOCK
			= "docx4j.convert.out.fo.wordLayout.lineBaselineFromBlock";

	public static boolean lineBaselineFromBlock() {
		String v = System.getProperty(LINE_BASELINE_FROM_BLOCK);
		if (v == null) {
			return Docx4jProperties.getProperty(LINE_BASELINE_FROM_BLOCK, true);
		}
		return Boolean.parseBoolean(v.trim());
	}

	/**
	 * Whether the {@code w:suff} separator between a list item's number and its text is
	 * written into the PDF's text layer as a space glyph, as Word writes it.
	 *
	 * <p>Word paints the label, then the separator - a tab, or a space - and then the
	 * text, and its PDF carries one <b>space character</b> for that separator whose quad
	 * is the separator's advance.  Measured on one corpus report's first heading, Word's
	 * label runs 127.49..134.45 and a space quad 134.45..138.53 precedes the text at
	 * 138.53; on another's, the label ends at 93.02 and a space quad 93.02..97.01
	 * precedes the text at 97.01.  docx4j lays the label out as an
	 * {@code fo:list-item-label} beside an {@code fo:list-item-body}, so the gap between
	 * them is geometry and no character is written at all, and the label fuses with the
	 * word after it.  The {@code styles-numpr-ilvl-only} golden probe carries Word's own
	 * answer: its item extracted as {@code 1.1.(b) L, direct w:numPr of w:ilvl 1 only}
	 * where Word's golden reads {@code 1.1. (b) L, ...}, and it goes from 50% to 100%
	 * line parity.  847 lines over the three real-document corpora differ from Word's by
	 * that one space (CR-001 batch 45, ledger5 section 1f).
	 *
	 * <p>The space is appended to the label's line area with the gap's own width, so no
	 * glyph moves: the body block is positioned from {@code body-start()}, not from the
	 * label's width.
	 *
	 * <p>On by default; docx4j property or system property
	 * docx4j.convert.out.fo.wordLayout.labelSuffixSpace=false leaves the gap empty.
	 *
	 * @since 17.1.1
	 */
	public static final String LABEL_SUFFIX_SPACE
			= "docx4j.convert.out.fo.wordLayout.labelSuffixSpace";

	public static boolean labelSuffixSpace() {
		String v = System.getProperty(LABEL_SUFFIX_SPACE);
		if (v == null) {
			return Docx4jProperties.getProperty(LABEL_SUFFIX_SPACE, true);
		}
		return Boolean.parseBoolean(v.trim());
	}

	/**
	 * Whether a word too long for a line of its own is broken inside it, at the last
	 * character that fits - Word's last resort, and the only way such a word does not
	 * run off the page.  UAX #14, which FOP follows, offers no break inside a word like
	 * {@code KONS_ADATOK_SZERZODO_ADATAI_TERM_SZEMELY_LAKCIM_VAROS}, so FOP paints the
	 * whole of it: measured over the three corpora, 1959 lines are painted outside their
	 * page in 73 documents against Word's 207 in 20, and Word's are deliberate overhangs.
	 *
	 * <p>Measured on a corpus golden (an insurance template of long placeholder tokens in
	 * a 279pt cell): Word moves such a word to a line of its own first - a preceding line
	 * with 85pt of slack is left short rather than filled with the word's head - and then
	 * breaks it wherever the measure falls, mid-token and with no hyphen
	 * ({@code ...THEN(m} / {@code egegyezik}).
	 *
	 * <p>On by default; docx4j property or system property
	 * docx4j.convert.out.fo.wordLayout.emergencyBreak=false turns it off.
	 *
	 * @since 17.1.0
	 */
	public static final String EMERGENCY_BREAK
			= "docx4j.convert.out.fo.wordLayout.emergencyBreak";

	public static boolean emergencyBreak() {
		String v = System.getProperty(EMERGENCY_BREAK);
		if (v == null) {
			return Docx4jProperties.getProperty(EMERGENCY_BREAK, true);
		}
		return Boolean.parseBoolean(v.trim());
	}

	/**
	 * How far past the measure, in points, a word may run before it is broken rather
	 * than left to overflow.
	 *
	 * <p>The rule has to be conservative, because a word which does not fit is often a
	 * measure <em>we</em> got wrong rather than a word Word breaks, and breaking it then
	 * hides the real defect and costs a line.  See
	 * {@code WordLineLayoutManager.overrunTolerance} for what was measured at each value.
	 *
	 * <p>docx4j property or system property
	 * docx4j.convert.out.fo.wordLayout.emergencyBreakTolerance sets it.
	 *
	 * @since 17.1.1
	 */
	public static final String EMERGENCY_BREAK_TOLERANCE
			= "docx4j.convert.out.fo.wordLayout.emergencyBreakTolerance";

	public static double emergencyBreakTolerance(double dflt) {
		Double v = doubleProperty(EMERGENCY_BREAK_TOLERANCE);
		return v == null ? dflt : v.doubleValue();
	}

	/**
	 * The same inside a table cell, in points: how far past the cell's measure a word may
	 * run before it is broken.  Word breaks a word as soon as it exceeds the cell it is in
	 * - measured, a 21.55pt column breaks {@code Categorizador/Período} down 15 lines of
	 * one or two characters - so the default is one twip (0.05pt), Word's own unit of
	 * layout, rather than the inch body text gets.  The general tolerance caps it: a value
	 * above {@link #EMERGENCY_BREAK_TOLERANCE} gives cells no special treatment.
	 *
	 * <p>A workaround for FOP, which has no intra-word break at all (docx4j's own line
	 * manager splits the word, see {@code WordLineLayoutManager.emergencyBreaks}); it
	 * goes when FOP grows one.  72 breaks in cells as sparingly as in body text, which is
	 * 17.1.0's behaviour.
	 *
	 * <p>docx4j property or system property
	 * docx4j.convert.out.fo.wordLayout.cellEmergencyBreakTolerance sets it.
	 *
	 * @since 17.1.1
	 */
	public static final String CELL_EMERGENCY_BREAK_TOLERANCE
			= "docx4j.convert.out.fo.wordLayout.cellEmergencyBreakTolerance";

	public static double cellEmergencyBreakTolerance(double dflt) {
		Double v = doubleProperty(CELL_EMERGENCY_BREAK_TOLERANCE);
		return v == null ? dflt : v.doubleValue();
	}

	/**
	 * The cell tolerance as a fraction of the cell's measure, the larger of it and
	 * {@link #CELL_EMERGENCY_BREAK_TOLERANCE} applying.  Written for the hypothesis that
	 * a word within a couple of per cent of its column is inside our own measurement
	 * error (substitute fonts, the column sizer against FOP); measured at 0.02 over the
	 * three corpora it recovers no page count the twip loses, because the words those
	 * documents break are 5 to 60 points over their columns, not a few per cent
	 * (word-layout-rules.md &#xa7;6.11).  Kept at 0 so the measurement can be repeated.
	 *
	 * <p>docx4j property or system property
	 * docx4j.convert.out.fo.wordLayout.cellEmergencyBreakToleranceRatio sets it.
	 *
	 * @since 17.1.1
	 */
	public static final String CELL_EMERGENCY_BREAK_TOLERANCE_RATIO
			= "docx4j.convert.out.fo.wordLayout.cellEmergencyBreakToleranceRatio";

	public static double cellEmergencyBreakToleranceRatio(double dflt) {
		Double v = doubleProperty(CELL_EMERGENCY_BREAK_TOLERANCE_RATIO);
		return v == null ? dflt : v.doubleValue();
	}

	/**
	 * Whether the line manager adds the break opportunity FOP lacks at the seam of two
	 * runs - {@code foo-} in one {@code fo:inline} and {@code bar} in the next - where
	 * UAX #14 and Word's rules allow one between those two characters
	 * ({@code WordBreakOpportunities.breakAtSeam}; word-layout-rules.md &#xa7;10).
	 *
	 * <p>On by default; docx4j property or system property
	 * docx4j.convert.out.fo.wordLayout.seamBreak=false turns it off.
	 *
	 * @since 17.1.1
	 */
	public static final String SEAM_BREAK = "docx4j.convert.out.fo.wordLayout.seamBreak";

	public static boolean seamBreak() {
		String v = System.getProperty(SEAM_BREAK);
		if (v == null) {
			return Docx4jProperties.getProperty(SEAM_BREAK, true);
		}
		return Boolean.parseBoolean(v.trim());
	}

	private static Double doubleProperty(String name) {
		String v = System.getProperty(name);
		if (v == null) v = Docx4jProperties.getProperty(name);
		if (v == null) return null;
		try {
			return Double.valueOf(v.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** The value the caller set, or null: an explicit setting applies to every document,
	 *  where the default is capped by the document's own docx4j:space-shrink. */
	public static Double configuredMaxSpaceShrink() {
		return doubleProperty(MAX_SPACE_SHRINK);
	}

	/** The namespace of the line-box attributes WordLineLayoutManager reads, when
	 *  Word layout is on (otherwise docx4j leaves them out). */
	@Override
	public String extensionNamespace() {
		return Docx4jProperties.getProperty(PROPERTY, true) ? WordLayoutElementMapping.URI : null;
	}

	@Override
	public void customize(FopFactoryBuilder builder, FOSettings settings) {
		if (!Docx4jProperties.getProperty(PROPERTY, true)) {
			log.debug("Word layout disabled by " + PROPERTY);
			return;
		}
		builder.setLayoutManagerMakerOverride(new WordLayoutManagerMaker());
		log.debug("Word layout enabled");
	}
}
