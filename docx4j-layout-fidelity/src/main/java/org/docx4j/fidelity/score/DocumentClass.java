/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 */
package org.docx4j.fidelity.score;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.docx4j.fonts.FontReport;
import org.docx4j.fonts.FontUsage;

/**
 * Which class of rule a scored document can be evidence for, and how much of its text
 * the class rests on - the two scoreboard columns {@code RULE-CLASSES.md} section 5 asks
 * for.
 *
 * <p>A golden measures one of two different things depending on the fonts the two
 * machines had (RULE-CLASSES.md section 2), and a scoreboard holds both kinds of
 * document at once, so a rule gated on the corpus mean is gated on both at once. The
 * class says which:</p>
 *
 * <ul>
 * <li><b>2</b> - Word had a face of its own for every family the document sets text in,
 *     and docx4j substituted none of them. The document is evidence for a class 2 rule:
 *     a residue here is a defect in docx4j.</li>
 * <li><b>2n</b> - Word had them all, and docx4j drew one or more in a near-metric clone
 *     or a measured stand-in. The vertical metrics are the document font's (the metrics
 *     table); the advances are the clone's.</li>
 * <li><b>3a</b> - docx4j drew a family the document uses in a face of the same class, or
 *     in nothing at all. The target is still what Word produced <em>with</em> the font,
 *     and the gap is what the metrics table and the clones are worth.</li>
 * <li><b>3b</b> - Word itself embedded another face for a family the document uses, so
 *     the golden measures Word's own substitution and not Word's layout of the
 *     document's fonts. A 3b reading is a note about Word; it is never gated on. It wins
 *     over 3a, because whatever docx4j did the reference is already not the target.</li>
 * </ul>
 *
 * <p>The <b>substituted share</b> is the fraction of the document's text runs set in a
 * substituted family, on whichever side is worse - Word's missing families or docx4j's
 * substitutions - so that a class 3b document with a share of 0.01 can be told from one
 * with a share of 1.00.</p>
 *
 * <p>Both sides are read at scoring time: the Word side from the faces the golden PDF
 * embeds (the corpus golden manifests carry no font list), the docx4j side from the
 * grades {@link org.docx4j.fonts.FontsAnalysis} reports for the render that was just
 * scored. The families are the ones the document's runs <em>resolve</em> to - the use
 * walk's answer, which is {@code RunFontSelector.documentFontFor} over the body, the
 * headers and footers, the notes and the comments, with the run's effective properties
 * resolved by {@code PropertyResolver} - not a reading of {@code w:rFonts}.</p>
 */
public final class DocumentClass {

	/** Word had every used family; docx4j substituted none. */
	public static final String CLASS_2 = "2";
	/** Word had every used family; docx4j drew one in a near-metric clone or stand-in. */
	public static final String CLASS_2N = "2n";
	/** docx4j drew a used family in a face of the same class, or in nothing. */
	public static final String CLASS_3A = "3a";
	/** Word embedded another face for a used family: the golden is Word's substitution. */
	public static final String CLASS_3B = "3b";

	/** The classes a summary reports, in this order; anything else is reported as {@code ?}. */
	public static final String[] CLASSES = { CLASS_2, CLASS_2N, CLASS_3A, CLASS_3B };

	/**
	 * Word appends the code page to a legacy family name in the {@code fontTable} and in
	 * the runs ({@code Times New Roman CYR}), and embeds the face under its own name, so
	 * the suffix is dropped before the two are compared.
	 */
	private static final Pattern LEGACY_CODE_PAGE =
			Pattern.compile("\\s+(cyr|ce|baltic|greek|tur|cyrillic)$", Pattern.CASE_INSENSITIVE);

	private DocumentClass() {}

	/** What one document's two columns say, and the evidence behind them. */
	public static final class Reading {

		private final String docClass;
		private final double share;
		private final long runs;
		private final Map<String, Long> wordMissing;
		private final Map<String, String> substituted;

		Reading(String docClass, double share, long runs, Map<String, Long> wordMissing,
				Map<String, String> substituted) {
			this.docClass = docClass;
			this.share = share;
			this.runs = runs;
			this.wordMissing = Collections.unmodifiableMap(wordMissing);
			this.substituted = Collections.unmodifiableMap(substituted);
		}

		/** {@code 2}, {@code 2n}, {@code 3a} or {@code 3b}. */
		public String getDocClass() { return docClass; }

		/** The fraction of the document's text runs in substituted families, worse side. */
		public double getShare() { return share; }

		/** Text runs walked. */
		public long getRuns() { return runs; }

		/** The families Word embedded no face of its own for, with their run counts. */
		public Map<String, Long> getWordMissing() { return wordMissing; }

		/** The families docx4j drew in something else, each with its grade. */
		public Map<String, String> getSubstituted() { return substituted; }

		/** The one line the run's {@code classes.txt} carries. */
		public String line(String id) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format(Locale.ROOT, "%-44s %-3s share %.3f runs %d", id, docClass, share, runs));
			if (!wordMissing.isEmpty()) {
				sb.append("  wordMissing=");
				boolean first = true;
				for (Map.Entry<String, Long> e : wordMissing.entrySet()) {
					if (!first) sb.append(',');
					first = false;
					sb.append(e.getKey()).append('(').append(e.getValue()).append(')');
				}
			}
			if (!substituted.isEmpty()) {
				sb.append("  docx4j=");
				boolean first = true;
				for (Map.Entry<String, String> e : substituted.entrySet()) {
					if (!first) sb.append(',');
					first = false;
					sb.append(e.getKey()).append(':').append(e.getValue());
				}
			}
			return sb.toString();
		}
	}

	/** One family the document sets text in: how much, and what docx4j drew it in. */
	public static final class Used {

		private final String family;
		private final long runs;
		private final FontReport.Grade grade;

		public Used(String family, long runs, FontReport.Grade grade) {
			this.family = family;
			this.runs = runs;
			this.grade = grade;
		}

		public String getFamily() { return family; }

		public long getRuns() { return runs; }

		public FontReport.Grade getGrade() { return grade; }
	}

	/**
	 * The class and the share of one document.
	 *
	 * @param report      {@link org.docx4j.fonts.FontsAnalysis#analyse} of the package the
	 *                    render used, so the grades are the render's own
	 * @param goldenFaces the base font names the golden PDF embeds, subset tags stripped
	 */
	public static Reading of(FontReport report, Collection<String> goldenFaces) {
		List<Used> used = new ArrayList<Used>();
		for (FontReport.Entry entry : report.getEntries()) {
			FontUsage.Use use = entry.getUse();
			if (use == null || use.getRuns() == 0) continue; // no text is set in it
			used.add(new Used(entry.getDocumentFont(), use.getRuns(), entry.getGrade()));
		}
		long runs = report.getUsage() == null ? 0 : report.getUsage().getRuns();
		return of(used, runs, goldenFaces);
	}

	/**
	 * The classification itself, over the families and the counts, so that it can be
	 * tested without a package, a mapper and a PDF.
	 *
	 * @param used        every family the document sets text in, with its run count and
	 *                    the grade docx4j's mapping earned
	 * @param totalRuns   text runs walked; a run which sets two families counts once here
	 *                    and once in each family, so the share is clamped at 1
	 * @param goldenFaces the base font names the golden PDF embeds
	 */
	public static Reading of(List<Used> used, long totalRuns, Collection<String> goldenFaces) {

		Map<String, Long> wordMissing = new LinkedHashMap<String, Long>();
		Map<String, String> substituted = new LinkedHashMap<String, String>();
		long wordMissingRuns = 0, substitutedRuns = 0;
		boolean hard = false, near = false;

		List<String> faces = normaliseFaces(goldenFaces);
		for (Used u : used) {
			if (u.getRuns() == 0) continue;
			if (!wordEmbedded(u.getFamily(), faces)) {
				wordMissing.put(u.getFamily(), Long.valueOf(u.getRuns()));
				wordMissingRuns += u.getRuns();
			}
			FontReport.Grade grade = u.getGrade();
			if (grade != null && grade != FontReport.Grade.EXACT) {
				substituted.put(u.getFamily(), grade.name());
				substitutedRuns += u.getRuns();
				if (grade == FontReport.Grade.NEAR) near = true;
				else hard = true; // CLASS or NONE
			}
		}

		double share = totalRuns == 0 ? 0
				: Math.min(1.0, Math.max(wordMissingRuns, substitutedRuns) / (double) totalRuns);

		String docClass = CLASS_2;
		if (!wordMissing.isEmpty()) docClass = CLASS_3B;
		else if (hard) docClass = CLASS_3A;
		else if (near) docClass = CLASS_2N;
		return new Reading(docClass, share, totalRuns, wordMissing, substituted);
	}

	// ------------------------------------------------------------------ name matching

	/**
	 * Whether the golden embeds a face of this family's own.
	 *
	 * <p>A PDF base font name is the <em>face</em>'s PostScript name, which is the family
	 * name with the style welded on and the spaces taken out in a way no rule can undo
	 * ({@code TimesNewRomanPS-BoldMT} for Times New Roman Bold, {@code Calibri-Italic},
	 * {@code Arial}), so the comparison is on the normalised names with either allowed to
	 * be a prefix of the other. That is deliberately the same rule the 2026-09-18 census
	 * used, so the two readings are comparable; it is generous in one direction (a family
	 * {@code Arial} is satisfied by an embedded {@code ArialNarrow}) and the class 2
	 * counts are therefore an upper bound on that account.</p>
	 */
	static boolean wordEmbedded(String family, Collection<String> normalisedFaces) {
		String f = norm(LEGACY_CODE_PAGE.matcher(family == null ? "" : family).replaceAll(""));
		if (f.isEmpty()) return true; // nothing to look for
		for (String face : normalisedFaces) {
			if (face.isEmpty()) continue;
			if (face.startsWith(f) || f.startsWith(face)) return true;
		}
		return false;
	}

	/** The PDF's base font names as they are compared: comma cut, normalised. */
	static List<String> normaliseFaces(Collection<String> faces) {
		List<String> out = new ArrayList<String>();
		if (faces == null) return out;
		for (String face : faces) {
			if (face == null) continue;
			int comma = face.indexOf(',');
			out.add(norm(comma < 0 ? face : face.substring(0, comma)));
		}
		return out;
	}

	/**
	 * Lower case, ASCII letters and digits only: the only footing the two naming schemes
	 * share, and the rule the 2026-09-18 census used, so that the two readings are
	 * comparable.
	 *
	 * <p><b>Its bound.</b> A family named wholly outside ASCII would normalise to nothing
	 * and {@link #wordEmbedded} would answer "there is nothing to look for", so such a
	 * family could never make a document class 3b. Measured over all 905 docx of the
	 * three corpora on both bases, no {@code w:font w:name} normalises to empty, so no
	 * document's class turns on this; the one partly non-ASCII family in the corpora - the
	 * legacy GB2312 KaiTi, whose name is two Han characters followed by {@code _GB2312} -
	 * keeps its {@code gb2312} and is classed on that. It does cost the match of that name
	 * against the cloud cache's {@code KaiTi} folder, which has to be made by hand. On that
	 * measurement it is kept ASCII-only: comparability with the census is worth more here
	 * than a case no document exercises.</p>
	 */
	static String norm(String name) {
		if (name == null) return "";
		StringBuilder sb = new StringBuilder(name.length());
		String lower = name.toLowerCase(Locale.ROOT);
		for (int i = 0; i < lower.length(); i++) {
			char c = lower.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) sb.append(c);
		}
		return sb.toString();
	}
}
