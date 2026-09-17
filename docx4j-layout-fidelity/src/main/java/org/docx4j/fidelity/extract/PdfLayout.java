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
		 * Right edge of a leading run of one to three digit glyphs followed by a space
		 * or the end of the line, and the x of the first ink glyph after it (NaN where
		 * the digits are the whole line).  Set by the extractor for the line-number
		 * rule, {@code PdfLayoutExtractor.dropLineNumbers}; NaN where the line does not
		 * start with such a run.
		 */
		double leadNumberEnd = Double.NaN, restX0 = Double.NaN;

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
		 *
		 * <p>A <b>leading symbol bullet</b> is collapsed to one token for the third such
		 * reason: the two renders name the same glyph differently, and neither name means
		 * anything.  Word's PDF gives a {@code w:sym} bullet the symbol font's own code
		 * point, in the private-use area; docx4j gives the Unicode character the glyph
		 * stands for.  Measured over one corpus: {@code U+F02A} against {@code U+2217}
		 * ASTERISK OPERATOR on three documents (62, 30 and 14 lines), {@code U+F077}
		 * against {@code U+2B25} on a fourth (61), and {@code U+F072}, {@code U+F0A8},
		 * {@code U+F06F} and {@code U+F0EA} against their own substitutes elsewhere.
		 * Ours is the better text layer - Word's copies as noise - and it is the one thing
		 * that cannot pair with Word's, so the metric stops asking.
		 * {@code -Dfidelity.bulletNormalise=false} restores the raw character.</p>
		 *
		 * <p>It is a function of the <b>text</b> and of nothing else, so it fires on both
		 * sides alike.  An earlier version asked instead whether the label was drawn in a
		 * font of its own, which is how a symbol bullet reaches the page - and it cost one
		 * corpus 314 matched lines, because that is not symmetric: on a document of
		 * {@code U+25AA} bullets, Word's PDF draws them in a symbol font (so they
		 * collapsed) and ours in the paragraph's own (so they did not), and 20 lines of a
		 * document which had been at 1.0000 stopped pairing.</p>
		 */
		public String key() {
			// cached: the LCS asks every reference line for its key against every
			// candidate line, so a 43,000-line document would run the regex 1.8 billion
			// times (measured: the biggest corpus document stopped finishing at all)
			String k = key;
			if (k == null) {
				k = NORMALISE_BULLETS ? normaliseBullet(text) : text;
				if (NORMALISE_LEADERS) k = LEADER_RUN.matcher(k).replaceAll("\u2026");
				if (NORMALISE_DATES) k = normaliseDates(k);
				key = k;
			}
			return k;
		}

		private String key;

		/** What a symbol bullet is collapsed to: U+2043 HYPHEN BULLET, which no corpus
		 *  document uses as text of its own. */
		static final String SYMBOL_LABEL = "\u2043";

		/**
		 * What one piece contributes to a run the merge pass joins: its <b>text</b> where
		 * the bullet rule is on, its key otherwise.
		 *
		 * <p>The rule looks at the head of a whole line, so a piece must not be normalised
		 * before the pieces are joined.  Measured: a corpus table row which Word reads as
		 * one line, {@code • Ecz... • Thy...}, is two lines on our side, each with its own
		 * bullet.  Joining the pieces' keys gave {@code U+2043 Ecz... U+2043 Thy...} - each
		 * piece's own leading bullet already a token - against the single line's
		 * {@code U+2043 Ecz... • Thy...}, where only the first is; the merge stopped
		 * pairing and the document lost the line.  Joining the texts and normalising the
		 * result gives the same key on both sides.</p>
		 */
		public static String mergePiece(Line l) {
			return NORMALISE_BULLETS ? l.text : l.key();
		}

		/** The key of a run the merge pass joined out of {@link #mergePiece} parts. */
		public static String joinedKey(String joined) {
			if (!NORMALISE_BULLETS) return joined;      // the pieces were already keys
			String k = normaliseBullet(joined);
			if (NORMALISE_LEADERS) k = LEADER_RUN.matcher(k).replaceAll("\u2026");
			if (NORMALISE_DATES) k = normaliseDates(k);
			return k;
		}

		/**
		 * A line which opens with one symbol character - a list label - and then with a
		 * word, with that character replaced by {@link #SYMBOL_LABEL} and the space after
		 * it dropped.  It is <b>replaced</b> and not dropped, so a line which carries no
		 * bullet where the other has one still fails: that is a real difference and the
		 * metric must keep counting it.
		 */
		static String normaliseBullet(String s) {
			if (s.isEmpty()) return s;
			int n = Character.charCount(s.codePointAt(0));
			if (!isBulletChar(s.codePointAt(0))) return s;
			int at = n;
			while (at < s.length() && s.charAt(at) == ' ') at++;
			// something must follow, and it must be a word rather than another symbol
			if (at >= s.length() || !Character.isLetterOrDigit(s.codePointAt(at))) return s;
			return SYMBOL_LABEL + s.substring(at);
		}

		/**
		 * Whether a code point is one a list label is drawn with rather than one a
		 * sentence is written with: a private-use code point (which is how Word names a
		 * Symbol or Wingdings glyph), a symbol from U+2000 up (which is what docx4j's
		 * substitution produces for one - and the floor keeps ASCII arithmetic and the
		 * currency signs out), or one of the punctuation marks used as bullets.
		 *
		 * <p>The <b>specials</b> block U+FFF0..U+FFFF is excluded although two of its
		 * characters are symbols: U+FFFC is what {@link #normaliseDates} leaves in a key
		 * for a date, and taking it for a bullet broke the merge pass on a corpus
		 * document whose table rows are two dates - the joined run read {@code U+2043 d}
		 * where the single line read {@code U+FFFC d} - costing it 11 matched lines.
		 * U+FFFD, a decoding failure, is not a bullet either.</p>
		 */
		private static boolean isBulletChar(int cp) {
			if (cp >= 0xE000 && cp <= 0xF8FF) return true;               // private use
			if (cp >= 0xF0000) return true;                              // private use, planes 15-16
			switch (cp) {
			case 0x00A7:   // section sign, a Wingdings square
			case 0x00B7:   // middle dot
			case 0x2022:   // bullet
			case 0x2023:   // triangular bullet
			case 0x2043:   // hyphen bullet
				return true;
			default:
				break;
			}
			if (cp < 0x2000) return false;
			if (cp >= 0xFFF0 && cp <= 0xFFFF) return false;              // specials
			int type = Character.getType(cp);
			return type == Character.OTHER_SYMBOL || type == Character.MATH_SYMBOL;
		}

		private static final boolean NORMALISE_BULLETS =
				!"false".equalsIgnoreCase(System.getProperty("fidelity.bulletNormalise", "true"));

		/** Three or more leader glyphs (dot, middle dot, underscore), however spaced,
		 *  together with the whitespace on either side of the run. */
		private static final java.util.regex.Pattern LEADER_RUN = java.util.regex.Pattern
				.compile("\\s*[.\u00b7\u2027_]\\s*(?:[.\u00b7\u2027_]\\s*){2,}");

		private static final boolean NORMALISE_LEADERS =
				!"false".equalsIgnoreCase(System.getProperty("fidelity.leaderNormalise", "true"));

		private static final boolean NORMALISE_DATES =
				!"false".equalsIgnoreCase(System.getProperty("fidelity.dateNormalise", "true"));

		/**
		 * The three no-break spaces, folded to an ordinary space before a line's
		 * whitespace is collapsed.  Java's {@code \\s} is exactly the set
		 * {@link Character#isWhitespace} accepts, which excludes U+00A0, U+2007 and
		 * U+202F by definition, so a {@code "1\u00a0000"} on our side and a
		 * {@code "1 000"} on Word's read identically and never paired: docx4j writes
		 * the document's own U+00A0 glyph where Word's PDF writes a plain space.
		 * Measured over the 95 corpus documents holding ten or more of them, mean
		 * line parity 0.8351 to 0.8478 and 145 more lines matched, ten documents up
		 * and none down (one from 0.105 to 0.947).  Both sides go through it;
		 * {@code -Dfidelity.nbspNormalise=false} keeps the glyphs distinct.
		 */
		static String foldSpaces(String s) {
			if (!NORMALISE_NBSP) return s;
			StringBuilder sb = null;
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if (c == '\u00a0' || c == '\u2007' || c == '\u202f') {
					if (sb == null) sb = new StringBuilder(s);
					sb.setCharAt(i, ' ');
				}
			}
			return sb == null ? s : sb.toString();
		}

		private static final boolean NORMALISE_NBSP =
				!"false".equalsIgnoreCase(System.getProperty("fidelity.nbspNormalise", "true"));

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
		/** White on white: the box is in the PDF but the reader sees nothing there. */
		public boolean invisible;

		@Override
		public String toString() {
			return String.format("p%d %s x=%.2f y=%.2f w=%.2f h=%.2f%s",
					page + 1, kind, x, y, w, h, invisible ? " invisible" : "");
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
