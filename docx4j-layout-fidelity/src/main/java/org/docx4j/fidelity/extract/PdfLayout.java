package org.docx4j.fidelity.extract;

import java.util.ArrayList;
import java.util.List;

/**
 * A PDF reduced to what layout comparison needs: per page, the text lines
 * (baseline, start/end x, text, font, size) and the line-art / image boxes.
 * All coordinates are in points with the origin at the top-left of the page.
 */
public final class PdfLayout {

	public static final class Line {
		public int page;          // 0-based
		public double y;          // baseline
		public double x0, x1;
		public double size;       // font size in pt of the first glyph
		public String font;
		public String text;       // whitespace-collapsed

		/**
		 * The text a line is paired on.  Runs of leader characters are collapsed to a
		 * single token, with the whitespace around them, so that a dot leader whose dots
		 * differ by one - which is what a phase difference of less than one dot's advance
		 * comes to - still pairs.  Word writes {@code "1. Scope ...... 5"} where the dots
		 * start on the reference area's grid; docx4j's start on the text, so the two
		 * extract as {@code "duty ...... 5"} and {@code "duty......5"} although the
		 * geometry agrees to 0.2pt.  Both sides go through this, so nothing is hidden
		 * that is not equally hidden on Word's side.
		 *
		 * <p>Measured over the corpora: worth up to +0.085 of line parity on a
		 * table-of-contents-heavy document.  {@code -Dfidelity.leaderNormalise=false}
		 * restores the raw text.</p>
		 *
		 * <p>A date or a time is collapsed to a placeholder for the same reason.  A
		 * {@code DATE}, {@code CREATEDATE}, {@code PRINTDATE}, {@code SAVEDATE} or
		 * {@code TIME} field prints the day the PDF was made, so a golden cut on one
		 * day never pairs with a render made on the next: the line drifts out of the
		 * LCS and takes its neighbours with it, and the document's score falls by a
		 * line a day for reasons that have nothing to do with layout.  Both sides go
		 * through this, so nothing is hidden that is not equally hidden on Word's
		 * side - and the geometry of a paired line is still compared in full, so a
		 * date we lay out in the wrong place is still counted against us.
		 * {@code -Dfidelity.dateNormalise=false} restores the raw text.</p>
		 */
		public String key() {
			// cached: the LCS asks every reference line for its key against every
			// candidate line, so a 43,000-line document would run the regex 1.8 billion
			// times (measured: the biggest corpus document stopped finishing at all)
			String k = key;
			if (k == null) {
				k = NORMALISE_LEADERS ? LEADER_RUN.matcher(text).replaceAll("\u2026") : text;
				if (NORMALISE_DATES) k = normaliseDates(k);
				key = k;
			}
			return k;
		}

		private String key;

		/** Three or more leader glyphs (dot, middle dot, underscore), however spaced,
		 *  together with the whitespace on either side of the run. */
		private static final java.util.regex.Pattern LEADER_RUN = java.util.regex.Pattern
				.compile("\\s*[.\u00b7\u2027_]\\s*(?:[.\u00b7\u2027_]\\s*){2,}");

		private static final boolean NORMALISE_LEADERS =
				!"false".equalsIgnoreCase(System.getProperty("fidelity.leaderNormalise", "true"));

		private static final boolean NORMALISE_DATES =
				!"false".equalsIgnoreCase(System.getProperty("fidelity.dateNormalise", "true"));

		/**
		 * Month names, full and abbreviated, of the locales the corpora are written in
		 * (en, de, fr, es, it, nl, pt, ru, hu, tr, sk).  Russian is in the genitive,
		 * which is the form a date takes.  Matched case-insensitively.
		 */
		private static final String MONTHS =
				"jan(?:uary|uar|vier|eiro)?|feb(?:ruary|ruar)?|f[e\u00e9]v(?:rier|ereiro)?|"
				+ "mar(?:ch|ch|s|zo|\u00e7o|z|ec)?|apr(?:il|ile)?|avr(?:il)?|abr(?:il)?|"
				+ "ma[yiej]|mei|mai|mag(?:gio)?|maj|"
				+ "jun[ie]?|jun(?:e|io|ho)?|juin|giu(?:gno)?|"
				+ "jul[iy]?|jul(?:y|io|ho)?|juil(?:let)?|lug(?:lio)?|"
				+ "aug(?:ust|usti)?|ao\u00fbt|ago(?:sto)?|"
				+ "sep(?:t|tember|tembre|tiembre|tembro)?|set(?:tembre|embro)?|"
				+ "o[ck]t(?:ober|obre|ubre|ubro|obar)?|"
				+ "nov(?:ember|embre|iembre|embro)?|"
				+ "de[czs](?:ember|embre|iembre|embro|zember)?|"
				+ "\u044f\u043d\u0432\u0430\u0440\u044f|\u0444\u0435\u0432\u0440\u0430\u043b\u044f|\u043c\u0430\u0440\u0442\u0430|\u0430\u043f\u0440\u0435\u043b\u044f|\u043c\u0430\u044f|\u0438\u044e\u043d\u044f|"
				+ "\u0438\u044e\u043b\u044f|\u0430\u0432\u0433\u0443\u0441\u0442\u0430|\u0441\u0435\u043d\u0442\u044f\u0431\u0440\u044f|\u043e\u043a\u0442\u044f\u0431\u0440\u044f|\u043d\u043e\u044f\u0431\u0440\u044f|\u0434\u0435\u043a\u0430\u0431\u0440\u044f";

		/** {@code 05.09.2026}, {@code 2026-09-05}, {@code 9/5/2026}.  A four-digit year
		 *  is required, so a section number ("1.2.34") is not a date. */
		private static final java.util.regex.Pattern NUMERIC_DATE = java.util.regex.Pattern
				.compile("\\b(?:\\d{4}[./-]\\d{1,2}[./-]\\d{1,2}|\\d{1,2}[./-]\\d{1,2}[./-]\\d{4})\\b");

		/** {@code 5 September 2026}, {@code 5. September 2026}, {@code 5 sept. 2026}. */
		private static final java.util.regex.Pattern DMY_DATE = java.util.regex.Pattern
				.compile("\\b\\d{1,2}\\.?\\s+(?:de\\s+)?(?:" + MONTHS + ")\\.?\\s+(?:de\\s+)?\\d{4}\\b",
						java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE);

		/** {@code September 5, 2026}, {@code September 5 2026}. */
		private static final java.util.regex.Pattern MDY_DATE = java.util.regex.Pattern
				.compile("(?<!\\p{L})(?:" + MONTHS + ")\\.?\\s+\\d{1,2},?\\s+\\d{4}\\b",
						java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE);

		/** {@code 14:05}, {@code 14:05:32}, {@code 2:05 PM}. */
		private static final java.util.regex.Pattern CLOCK_TIME = java.util.regex.Pattern
				.compile("\\b\\d{1,2}:\\d{2}(?::\\d{2})?(?:\\s*[AaPp]\\.?[Mm]\\.?)?");

		static final String DATE_TOKEN = "\uFFFCd";
		static final String TIME_TOKEN = "\uFFFCt";

		/** Package-visible so a test can assert what is and is not a date. */
		static String normaliseDates(String s) {
			// cheap pre-filter: a line with no digit can hold no date or time
			boolean digit = false;
			for (int i = 0; i < s.length(); i++) {
				if (s.charAt(i) >= '0' && s.charAt(i) <= '9') { digit = true; break; }
			}
			if (!digit) return s;
			String out = NUMERIC_DATE.matcher(s).replaceAll(DATE_TOKEN);
			out = DMY_DATE.matcher(out).replaceAll(DATE_TOKEN);
			out = MDY_DATE.matcher(out).replaceAll(DATE_TOKEN);
			out = CLOCK_TIME.matcher(out).replaceAll(TIME_TOKEN);
			return out;
		}

		@Override
		public String toString() {
			return String.format("p%d y=%.2f x=%.2f..%.2f %s %.1fpt \"%s\"", page + 1, y, x0, x1, font, size, text);
		}
	}

	public static final class Box {
		public int page;
		public double x, y, w, h;
		public String kind;       // stroke | fill | image

		@Override
		public String toString() {
			return String.format("p%d %s x=%.2f y=%.2f w=%.2f h=%.2f", page + 1, kind, x, y, w, h);
		}
	}

	public final List<Double> pageWidths = new ArrayList<>();
	public final List<Double> pageHeights = new ArrayList<>();
	public final List<Line> lines = new ArrayList<>();
	public final List<Box> boxes = new ArrayList<>();

	public int pageCount() {
		return pageWidths.size();
	}

	public List<Line> linesOnPage(int page) {
		List<Line> out = new ArrayList<>();
		for (Line l : lines) {
			if (l.page == page) out.add(l);
		}
		return out;
	}
}
