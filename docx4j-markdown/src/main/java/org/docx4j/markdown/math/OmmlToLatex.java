package org.docx4j.markdown.math;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.math.CTAcc;
import org.docx4j.math.CTBar;
import org.docx4j.math.CTBorderBox;
import org.docx4j.math.CTD;
import org.docx4j.math.CTEqArr;
import org.docx4j.math.CTF;
import org.docx4j.math.CTNary;
import org.docx4j.math.CTOMath;
import org.docx4j.math.CTOMathArg;
import org.docx4j.math.CTOMathPara;
import org.docx4j.math.CTR;
import org.docx4j.math.CTRPR;
import org.docx4j.math.CTRad;
import org.docx4j.math.CTSSub;
import org.docx4j.math.CTSSubSup;
import org.docx4j.math.CTSSup;
import org.docx4j.math.CTText;
import org.docx4j.math.STStyle;
import org.docx4j.math.STTopBot;

/**
 * The reverse of {@link LatexToOmml}: OMML → LaTeX for the same subset.
 * Anything outside it throws {@link OmmlMathException} (the exporter then
 * flattens the equation to its text, with a warning).
 *
 * <p>The generated LaTeX is normalized: braced arguments ({@code U^{3}},
 * {@code \frac{1}{2}}), shortest symbol command on collisions ({@code \le}
 * not {@code \leq}), display math on one line.</p>
 */
public class OmmlToLatex {

	/** Reverse symbol map: Unicode → shortest LaTeX command. */
	private static final Map<String, String> REVERSE_SYMBOLS = new HashMap<>();
	private static final Map<String, String> REVERSE_NARY = new HashMap<>();
	private static final Map<String, String> REVERSE_ACCENTS = new HashMap<>();

	static {
		for (Map.Entry<String, String> e : LatexToOmml.SYMBOLS.entrySet()) {
			String existing = REVERSE_SYMBOLS.get(e.getValue());
			if (existing == null || shorter(e.getKey(), existing)) {
				REVERSE_SYMBOLS.put(e.getValue(), e.getKey());
			}
		}
		for (Map.Entry<String, String> e : LatexToOmml.NARY.entrySet()) {
			String existing = REVERSE_NARY.get(e.getValue());
			if (existing == null || shorter(e.getKey(), existing)) {
				REVERSE_NARY.put(e.getValue(), e.getKey());
			}
		}
		REVERSE_ACCENTS.put("\u0302", "hat");
		REVERSE_ACCENTS.put("\u0303", "tilde");
		REVERSE_ACCENTS.put("\u0305", "bar");
		REVERSE_ACCENTS.put("\u20D7", "vec");
		REVERSE_ACCENTS.put("\u0307", "dot");
		REVERSE_ACCENTS.put("\u0308", "ddot");
		REVERSE_ACCENTS.put("\u030C", "check");
		REVERSE_ACCENTS.put("\u0306", "breve");
		REVERSE_ACCENTS.put("\u0301", "acute");
		REVERSE_ACCENTS.put("\u0300", "grave");
	}

	private static boolean shorter(String a, String b) {
		return a.length() < b.length()
				|| (a.length() == b.length() && a.compareTo(b) < 0);
	}

	/** Accumulates LaTeX, spacing commands from following letters. */
	private static final class LatexBuilder {
		private final StringBuilder sb = new StringBuilder();
		private boolean commandOpen; // last append was a \letters command

		void command(String name) {
			sb.append('\\').append(name);
			commandOpen = Character.isLetter(name.charAt(name.length() - 1));
		}
		void raw(String s) {
			if (s.isEmpty()) {
				return;
			}
			if (commandOpen && Character.isLetter(s.charAt(0))) {
				sb.append(' ');
			}
			sb.append(s);
			commandOpen = false;
		}
		@Override
		public String toString() {
			return sb.toString();
		}
	}

	public String convertOMath(CTOMath oMath) throws OmmlMathException {
		LatexBuilder builder = new LatexBuilder();
		elements(oMath.getEGOMathElements(), builder, false);
		return builder.toString().trim();
	}

	/** All the para's equations, joined (display math re-exports on one line). */
	public String convertOMathPara(CTOMathPara oMathPara) throws OmmlMathException {
		StringBuilder sb = new StringBuilder();
		for (CTOMath oMath : oMathPara.getOMath()) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(convertOMath(oMath));
		}
		return sb.toString();
	}

	// ---------------------------------------------------------------- walk

	private void elements(List<Object> elements, LatexBuilder builder, boolean inEqArr)
			throws OmmlMathException {

		for (Object o : elements) {
			Object u = (o instanceof JAXBElement) ? ((JAXBElement<?>) o).getValue() : o;

			if (u instanceof CTR) {
				run((CTR) u, builder, inEqArr);
			} else if (u instanceof CTF) {
				CTF f = (CTF) u;
				builder.command("frac");
				bracedArg(f.getNum(), builder);
				bracedArg(f.getDen(), builder);
			} else if (u instanceof CTSSub) {
				CTSSub s = (CTSSub) u;
				base(s.getE(), builder);
				builder.raw("_");
				bracedArg(s.getSub(), builder);
			} else if (u instanceof CTSSup) {
				CTSSup s = (CTSSup) u;
				base(s.getE(), builder);
				builder.raw("^");
				bracedArg(s.getSup(), builder);
			} else if (u instanceof CTSSubSup) {
				CTSSubSup s = (CTSSubSup) u;
				base(s.getE(), builder);
				builder.raw("_");
				bracedArg(s.getSub(), builder);
				builder.raw("^");
				bracedArg(s.getSup(), builder);
			} else if (u instanceof CTRad) {
				rad((CTRad) u, builder);
			} else if (u instanceof CTNary) {
				nary((CTNary) u, builder);
			} else if (u instanceof CTD) {
				delimited((CTD) u, builder);
			} else if (u instanceof CTEqArr) {
				eqArr((CTEqArr) u, builder);
			} else if (u instanceof CTBorderBox) {
				builder.command("boxed");
				bracedArg(((CTBorderBox) u).getE(), builder);
			} else if (u instanceof CTAcc) {
				acc((CTAcc) u, builder);
			} else if (u instanceof CTBar) {
				CTBar bar = (CTBar) u;
				boolean top = bar.getBarPr() != null && bar.getBarPr().getPos() != null
						&& bar.getBarPr().getPos().getVal() == STTopBot.TOP;
				builder.command(top ? "overline" : "underline");
				bracedArg(bar.getE(), builder);
			} else if (u instanceof CTOMath) {
				elements(((CTOMath) u).getEGOMathElements(), builder, inEqArr);
			} else {
				throw new OmmlMathException(
						"unsupported OMML element " + u.getClass().getSimpleName());
			}
		}
	}

	private void run(CTR r, LatexBuilder builder, boolean inEqArr) throws OmmlMathException {

		CTRPR rPr = null;
		StringBuilder text = new StringBuilder();
		for (Object o : r.getContent()) {
			Object u = (o instanceof JAXBElement) ? ((JAXBElement<?>) o).getValue() : o;
			if (u instanceof CTRPR) {
				rPr = (CTRPR) u;
			} else if (u instanceof CTText) {
				text.append(((CTText) u).getValue());
			} else if (u instanceof org.docx4j.wml.Text) {
				text.append(((org.docx4j.wml.Text) u).getValue());
			}
			// other run content (breaks etc) is dropped
		}
		String s = text.toString();
		if (s.isEmpty()) {
			return;
		}

		if (rPr != null && rPr.getNor() != null) {
			builder.command("text");
			builder.raw("{" + escapeText(s) + "}");
			return;
		}
		STStyle sty = (rPr != null && rPr.getSty() != null) ? rPr.getSty().getVal() : null;
		if (sty == STStyle.P) {
			if (LatexToOmml.FUNCTION_NAMES.contains(s)) {
				builder.command(s);
			} else {
				builder.command("mathrm");
				builder.raw("{" + escapeText(s) + "}");
			}
			return;
		}
		if (sty == STStyle.B) {
			builder.command("mathbf");
			builder.raw("{" + escapeText(s) + "}");
			return;
		}
		if (sty == STStyle.I) {
			builder.command("mathit");
			builder.raw("{" + escapeText(s) + "}");
			return;
		}

		mathChars(s, builder, inEqArr);
	}

	private void mathChars(String s, LatexBuilder builder, boolean inEqArr) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			String symbol = REVERSE_SYMBOLS.get(String.valueOf(c));
			if (symbol != null) {
				builder.command(symbol);
				continue;
			}
			switch (c) {
			case '\u2032': builder.raw("'"); break;
			case '\u2009': builder.command(","); break;
			case '\u2005': builder.command(";"); break;
			case '\u2003': builder.command("quad"); break;
			case '{': case '}': case '_': case '^': case '%': case '$': case '#':
				builder.raw("\\" + c);
				break;
			case '&':
				// inside an equation array '&' is the alignment mark
				builder.raw(inEqArr ? "&" : "\\&");
				break;
			case '\\':
				builder.command("backslash"); // not round-trippable; rare
				break;
			default:
				builder.raw(String.valueOf(c));
			}
		}
	}

	private static String escapeText(String s) {
		return s.replace("\\", "\\backslash ")
				.replace("{", "\\{").replace("}", "\\}")
				.replace("&", "\\&").replace("%", "\\%")
				.replace("$", "\\$").replace("#", "\\#")
				.replace("_", "\\_").replace("^", "\\^");
	}

	private void rad(CTRad rad, LatexBuilder builder) throws OmmlMathException {
		builder.command("sqrt");
		boolean degHidden = rad.getRadPr() != null && rad.getRadPr().getDegHide() != null;
		if (!degHidden && rad.getDeg() != null
				&& !rad.getDeg().getEGOMathElements().isEmpty()) {
			builder.raw("[");
			elements(rad.getDeg().getEGOMathElements(), builder, false);
			builder.raw("]");
		}
		bracedArg(rad.getE(), builder);
	}

	private void nary(CTNary nary, LatexBuilder builder) throws OmmlMathException {
		String chr = (nary.getNaryPr() != null && nary.getNaryPr().getChr() != null)
				? nary.getNaryPr().getChr().getVal() : "∫"; // OMML default
		String command = REVERSE_NARY.get(chr);
		if (command == null) {
			throw new OmmlMathException("unsupported n-ary operator " + chr);
		}
		builder.command(command);
		boolean subHidden = nary.getNaryPr() != null && nary.getNaryPr().getSubHide() != null;
		boolean supHidden = nary.getNaryPr() != null && nary.getNaryPr().getSupHide() != null;
		if (!subHidden && nary.getSub() != null) {
			builder.raw("_");
			bracedArg(nary.getSub(), builder);
		}
		if (!supHidden && nary.getSup() != null) {
			builder.raw("^");
			bracedArg(nary.getSup(), builder);
		}
		if (nary.getE() != null) {
			elements(nary.getE().getEGOMathElements(), builder, false);
		}
	}

	private void delimited(CTD d, LatexBuilder builder) throws OmmlMathException {
		String beg = (d.getDPr() != null && d.getDPr().getBegChr() != null)
				? d.getDPr().getBegChr().getVal() : "(";
		String end = (d.getDPr() != null && d.getDPr().getEndChr() != null)
				? d.getDPr().getEndChr().getVal() : ")";
		builder.command("left");
		builder.raw(delimiterFor(beg));
		for (CTOMathArg e : d.getE()) {
			elements(e.getEGOMathElements(), builder, false);
		}
		builder.command("right");
		builder.raw(delimiterFor(end));
	}

	private static String delimiterFor(String chr) {
		if (chr == null || chr.isEmpty()) {
			return ".";
		}
		switch (chr) {
		case "{": return "\\{";
		case "}": return "\\}";
		case "‖": return "\\|";
		case "⟨": return "\\langle ";
		case "⟩": return "\\rangle ";
		case "⌊": return "\\lfloor ";
		case "⌋": return "\\rfloor ";
		case "⌈": return "\\lceil ";
		case "⌉": return "\\rceil ";
		default: return chr;
		}
	}

	private void eqArr(CTEqArr eqArr, LatexBuilder builder) throws OmmlMathException {
		builder.raw("\\begin{aligned}");
		boolean first = true;
		for (CTOMathArg row : eqArr.getE()) {
			if (!first) {
				builder.raw(" \\\\ ");
			}
			first = false;
			elements(row.getEGOMathElements(), builder, true);
		}
		builder.raw("\\end{aligned}");
	}

	private void acc(CTAcc acc, LatexBuilder builder) throws OmmlMathException {
		String chr = (acc.getAccPr() != null && acc.getAccPr().getChr() != null)
				? acc.getAccPr().getChr().getVal() : "\u0302"; // OMML default is hat
		String command = REVERSE_ACCENTS.get(chr);
		if (command == null) {
			throw new OmmlMathException("unsupported accent " + chr);
		}
		builder.command(command);
		bracedArg(acc.getE(), builder);
	}

	// ---------------------------------------------------------------- args

	private void bracedArg(CTOMathArg arg, LatexBuilder builder) throws OmmlMathException {
		builder.raw("{");
		if (arg != null) {
			elements(arg.getEGOMathElements(), builder, false);
		}
		builder.raw("}");
	}

	/**
	 * A script base: a single-character plain run stays bare ({@code U^{3}});
	 * anything else is braced.
	 */
	private void base(CTOMathArg arg, LatexBuilder builder) throws OmmlMathException {
		if (arg != null && arg.getEGOMathElements().size() == 1) {
			Object o = arg.getEGOMathElements().get(0);
			Object u = (o instanceof JAXBElement) ? ((JAXBElement<?>) o).getValue() : o;
			if (u instanceof CTR) {
				CTR r = (CTR) u;
				String text = null;
				boolean plain = true;
				for (Object rc : r.getContent()) {
					Object ru = (rc instanceof JAXBElement) ? ((JAXBElement<?>) rc).getValue() : rc;
					if (ru instanceof CTRPR) {
						plain = false;
					} else if (ru instanceof CTText) {
						text = ((CTText) ru).getValue();
					}
				}
				if (plain && text != null && text.length() == 1
						&& !REVERSE_SYMBOLS.containsKey(text)
						&& "{}_^%$#&\\".indexOf(text.charAt(0)) < 0) {
					builder.raw(text);
					return;
				}
			}
		}
		bracedArg(arg, builder);
	}

}
