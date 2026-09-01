package org.docx4j.markdown.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.math.CTD;
import org.docx4j.math.CTF;
import org.docx4j.math.CTNary;
import org.docx4j.math.CTNaryPr;
import org.docx4j.math.CTOMath;
import org.docx4j.math.CTOMathArg;
import org.docx4j.math.CTOMathPara;
import org.docx4j.math.CTOnOff;
import org.docx4j.math.CTR;
import org.docx4j.math.CTRPR;
import org.docx4j.math.CTRad;
import org.docx4j.math.CTRadPr;
import org.docx4j.math.CTSSub;
import org.docx4j.math.CTSSubSup;
import org.docx4j.math.CTSSup;
import org.docx4j.math.CTText;
import org.docx4j.math.ObjectFactory;
import org.docx4j.math.STLimLoc;
import org.docx4j.math.STStyle;

/**
 * Translates a deliberately restricted LaTeX subset (see CR-markdown-math
 * §2 and the module README) directly into OMML ({@code org.docx4j.math}).
 * No MathML detour, no XSLT.
 *
 * <p>Anything outside the subset throws {@link LatexMathException} — the
 * translation is all-or-nothing per equation, never a silent partial.</p>
 */
public class LatexToOmml {

	private final ObjectFactory factory = new ObjectFactory();

	private String src;
	private int pos;

	/** Style context for runs; a copy is taken per group. */
	private static final class Ctx {
		STStyle sty;      // p / b / i, or null for default math italic
		boolean normalText; // \text{}: m:nor
		org.docx4j.math.STScript scr; // \mathcal etc: m:scr
		Ctx copy() {
			Ctx c = new Ctx();
			c.sty = sty;
			c.normalText = normalText;
			c.scr = scr;
			return c;
		}
	}

	/** {@code $...$} content → m:oMath. */
	public CTOMath convertInline(String latex) throws LatexMathException {
		this.src = latex;
		this.pos = 0;
		CTOMath oMath = factory.createCTOMath();
		List<Object> elements = parseSequence(new Ctx(), (char) 0);
		if (pos < src.length()) {
			throw err("unexpected '" + src.charAt(pos) + "'");
		}
		oMath.getEGOMathElements().addAll(elements);
		return oMath;
	}

	/** {@code $$...$$} content → m:oMathPara (Word centers display math). */
	public CTOMathPara convertDisplay(String latex) throws LatexMathException {
		CTOMathPara oMathPara = factory.createCTOMathPara();
		oMathPara.getOMath().add(convertInline(latex));
		return oMathPara;
	}

	// ---------------------------------------------------------------- parsing

	/**
	 * Parse until the given closing char ('}' for a group, or 0 for
	 * end-of-input), which is consumed.  Atoms are tracked so that a
	 * following script binds to the last atom only.
	 */
	private List<Object> parseSequence(Ctx outerCtx, char closer) throws LatexMathException {

		Ctx ctx = outerCtx.copy(); // \rm etc apply to the rest of the group
		List<List<Object>> atoms = new ArrayList<>();

		while (true) {
			skipWhitespace();
			if (pos >= src.length()) {
				if (closer != 0) {
					throw err("missing '" + closer + "'");
				}
				break;
			}
			char c = src.charAt(pos);
			if (c == closer) {
				pos++;
				break;
			}
			if (c == '}') {
				throw err("unexpected '}'");
			}
			if (c == '_' || c == '^') {
				List<Object> base = atoms.isEmpty() ? new ArrayList<>()
						: atoms.remove(atoms.size() - 1);
				atoms.add(List.of(parseScripts(argOf(base), ctx)));
				continue;
			}
			List<Object> atom = parseAtom(ctx);
			if (atom != null) {
				atoms.add(atom);
			}
		}

		List<Object> out = new ArrayList<>();
		for (List<Object> atom : atoms) {
			out.addAll(atom);
		}
		return mergeAdjacentRuns(out);
	}

	/** @return the atom's elements, or null for style switches (which mutate ctx) */
	private List<Object> parseAtom(Ctx ctx) throws LatexMathException {

		char c = src.charAt(pos);

		if (c == '{') {
			pos++;
			return parseSequence(ctx, '}');
		}
		if (c == '\\') {
			return parseCommand(ctx);
		}
		pos++;
		return List.of(run(charText(c), ctx));
	}

	private static String charText(char c) {
		if (c == '\'') {
			return "′"; // prime
		}
		return String.valueOf(c);
	}

	private List<Object> parseCommand(Ctx ctx) throws LatexMathException {

		String command = readCommandName();

		// "\ " — including a backslash before a line break — is an explicit space
		if (command.length() == 1 && Character.isWhitespace(command.charAt(0))) {
			return List.of(run(" ", ctx));
		}

		switch (command) {
		case "frac": {
			CTF f = factory.createCTF();
			f.setNum(parseArg(ctx));
			f.setDen(parseArg(ctx));
			return List.of(factory.createCTOMathArgF(f));
		}
		case "sqrt": {
			CTRad rad = factory.createCTRad();
			CTOMathArg degree = parseOptionalBracketArg(ctx);
			if (degree == null) {
				CTRadPr radPr = factory.createCTRadPr();
				CTOnOff on = factory.createCTOnOff();
				radPr.setDegHide(on);
				rad.setRadPr(radPr);
				rad.setDeg(factory.createCTOMathArg());
			} else {
				rad.setDeg(degree);
			}
			rad.setE(parseArg(ctx));
			return List.of(factory.createCTOMathArgRad(rad));
		}
		case "left":
			return List.of(parseDelimited(ctx));
		case "right":
			throw err("\\right without \\left");
		case "begin": {
			String environment = readRawGroup();
			switch (environment) {
			case "aligned":
			case "align":
			case "align*":
				return List.of(parseEqArr(ctx, environment));
			case "cases":
				return List.of(parseCases(ctx));
			default:
				throw err("unsupported environment \\begin{" + environment + "}");
			}
		}
		case "end":
			throw err("\\end without \\begin");
		case "boxed": {
			org.docx4j.math.CTBorderBox borderBox = factory.createCTBorderBox();
			borderBox.setE(parseArg(ctx));
			return List.of(factory.createCTOMathArgBorderBox(borderBox));
		}
		case "hat":
			return List.of(accent("\u0302", ctx));
		case "tilde":
			return List.of(accent("\u0303", ctx));
		case "bar":
			return List.of(accent("\u0305", ctx));
		case "vec":
			return List.of(accent("\u20D7", ctx));
		case "dot":
			return List.of(accent("\u0307", ctx));
		case "ddot":
			return List.of(accent("\u0308", ctx));
		case "check":
			return List.of(accent("\u030C", ctx));
		case "breve":
			return List.of(accent("\u0306", ctx));
		case "acute":
			return List.of(accent("\u0301", ctx));
		case "grave":
			return List.of(accent("\u0300", ctx));
		case "widehat":
			return List.of(accent("\u0302", ctx));
		case "widetilde":
			return List.of(accent("\u0303", ctx));
		case "overline":
			return List.of(bar(org.docx4j.math.STTopBot.TOP, ctx));
		case "underline":
			return List.of(bar(org.docx4j.math.STTopBot.BOT, ctx));
		case "xrightarrow":
			return List.of(limUpp(argOf(List.of(run("⟶", ctx))), parseArg(ctx)));
		case "xleftarrow":
			return List.of(limUpp(argOf(List.of(run("⟵", ctx))), parseArg(ctx)));
		case "overset":
		case "stackrel": {
			CTOMathArg over = parseArg(ctx);
			CTOMathArg base = parseArg(ctx);
			return List.of(limUpp(base, over));
		}
		case "not": {
			// negate the following symbol with a combining long solidus
			List<Object> next = parseAtom(ctx);
			org.docx4j.math.CTText t = (next != null && next.size() == 1)
					? singleRunText(next.get(0)) : null;
			if (t == null) {
				throw err("\\not must be followed by a single symbol");
			}
			t.setValue(t.getValue() + "\u0338"); // combining long solidus overlay
			return next;
		}
		case "big": case "Big": case "bigg": case "Bigg":
		case "bigl": case "Bigl": case "biggl": case "Biggl":
		case "bigr": case "Bigr": case "biggr": case "Biggr":
		case "bigm": case "Bigm": case "biggm": case "Biggm": {
			// sizing prefixes: keep the delimiter, drop the sizing
			String delimiter = readDelimiterChar();
			return delimiter.isEmpty() ? null : List.of(run(delimiter, ctx));
		}
		case "middle":
			throw err("\\middle outside \\left ... \\right");
		case "text":
			return List.of(textRun(readRawGroup(), true, ctx));
		case "mathrm":
		case "operatorname":
			return List.of(textRun(readRawGroup(), false, ctx));
		case "mathbf": {
			Ctx bold = ctx.copy();
			bold.sty = STStyle.B;
			return parseArgAsSequence(bold);
		}
		case "mathit": {
			Ctx italic = ctx.copy();
			italic.sty = STStyle.I;
			return parseArgAsSequence(italic);
		}
		case "mathcal":
			return parseArgAsScript(ctx, org.docx4j.math.STScript.SCRIPT);
		case "mathbb":
			return parseArgAsScript(ctx, org.docx4j.math.STScript.DOUBLE_STRUCK);
		case "mathfrak":
			return parseArgAsScript(ctx, org.docx4j.math.STScript.FRAKTUR);
		case "textbf":
			return List.of(boldOrItalicTextRun(readRawGroup(), true, false));
		case "textit":
			return List.of(boldOrItalicTextRun(readRawGroup(), false, true));
		case "rm":
			ctx.sty = STStyle.P;
			return null;
		case "bf":
			ctx.sty = STStyle.B;
			return null;
		case "it":
			ctx.sty = STStyle.I;
			return null;
		case "quad":
			return List.of(run(" ", ctx));
		case "qquad":
			return List.of(run("  ", ctx));
		case ",":
			return List.of(run(" ", ctx)); // thin space
		case ";":
			return List.of(run(" ", ctx));
		case ":":
			return List.of(run(" ", ctx));
		case "!":
			return null; // negative thin space: no OMML equivalent, dropped
		case " ":
			return List.of(run(" ", ctx));
		case "{":
		case "}":
		case "$":
		case "%":
		case "&":
		case "#":
		case "_":
			return List.of(run(command, ctx));
		default:
			// n-ary operators take their limits with them
			String naryChar = NARY.get(command);
			if (naryChar != null) {
				return List.of(nary(naryChar, command, ctx));
			}
			String symbol = SYMBOLS.get(command);
			if (symbol != null) {
				return List.of(run(symbol, ctx));
			}
			if (FUNCTION_NAMES.contains(command)) {
				return List.of(textRun(command, false, ctx));
			}
			throw err("unsupported command \\" + command);
		}
	}

	private String readCommandName() throws LatexMathException {
		pos++; // the backslash
		if (pos >= src.length()) {
			throw err("dangling backslash");
		}
		char c = src.charAt(pos);
		if (!Character.isLetter(c)) {
			pos++;
			return String.valueOf(c);
		}
		int start = pos;
		while (pos < src.length() && Character.isLetter(src.charAt(pos))) {
			pos++;
		}
		return src.substring(start, pos);
	}

	/** A command/script argument: a {...} group, a command, or a single char. */
	private CTOMathArg parseArg(Ctx ctx) throws LatexMathException {
		skipWhitespace();
		if (pos >= src.length()) {
			throw err("missing argument");
		}
		char c = src.charAt(pos);
		if (c == '{') {
			pos++;
			return argOf(parseSequence(ctx, '}'));
		}
		if (c == '\\') {
			List<Object> atom = parseCommand(ctx);
			return argOf(atom == null ? List.of() : atom);
		}
		if (c == '_' || c == '^' || c == '}') {
			throw err("missing argument before '" + c + "'");
		}
		pos++;
		return argOf(List.of(run(charText(c), ctx)));
	}

	private List<Object> parseArgAsSequence(Ctx ctx) throws LatexMathException {
		skipWhitespace();
		if (pos < src.length() && src.charAt(pos) == '{') {
			pos++;
			return parseSequence(ctx, '}');
		}
		return List.of(argContent(parseArg(ctx)));
	}

	private Object argContent(CTOMathArg arg) throws LatexMathException {
		if (arg.getEGOMathElements().size() != 1) {
			throw err("expected a single element");
		}
		return arg.getEGOMathElements().get(0);
	}

	/** {@code [n]} after \sqrt, if present. */
	private CTOMathArg parseOptionalBracketArg(Ctx ctx) throws LatexMathException {
		skipWhitespace();
		if (pos >= src.length() || src.charAt(pos) != '[') {
			return null;
		}
		pos++;
		List<List<Object>> content = new ArrayList<>();
		while (true) {
			skipWhitespace();
			if (pos >= src.length()) {
				throw err("missing ']'");
			}
			if (src.charAt(pos) == ']') {
				pos++;
				break;
			}
			List<Object> atom = parseAtom(ctx);
			if (atom != null) {
				content.add(atom);
			}
		}
		List<Object> flat = new ArrayList<>();
		for (List<Object> atom : content) {
			flat.addAll(atom);
		}
		return argOf(mergeAdjacentRuns(flat));
	}

	/** Raw content of a {...} group (nested braces allowed), spaces kept. */
	private String readRawGroup() throws LatexMathException {
		skipWhitespace();
		if (pos >= src.length() || src.charAt(pos) != '{') {
			throw err("expected '{'");
		}
		pos++;
		int depth = 1;
		StringBuilder sb = new StringBuilder();
		while (pos < src.length()) {
			char c = src.charAt(pos++);
			if (c == '{') {
				depth++;
			} else if (c == '}') {
				if (--depth == 0) {
					return sb.toString().replace('\n', ' ');
				}
			}
			if (depth > 0) {
				sb.append(c == '\n' ? ' ' : c);
			}
		}
		throw err("missing '}'");
	}

	// ---------------------------------------------------------------- scripts

	private Object parseScripts(CTOMathArg base, Ctx ctx) throws LatexMathException {

		CTOMathArg sub = null;
		CTOMathArg sup = null;
		while (pos < src.length()) {
			char c = src.charAt(pos);
			if (c == '_' && sub == null) {
				pos++;
				sub = parseArg(ctx);
			} else if (c == '^' && sup == null) {
				pos++;
				sup = parseArg(ctx);
			} else {
				break;
			}
		}
		return scripted(base, sub, sup);
	}

	private Object scripted(CTOMathArg base, CTOMathArg sub, CTOMathArg sup) {
		if (sub != null && sup != null) {
			CTSSubSup s = factory.createCTSSubSup();
			s.setE(base);
			s.setSub(sub);
			s.setSup(sup);
			return factory.createCTOMathArgSSubSup(s);
		}
		if (sub != null) {
			CTSSub s = factory.createCTSSub();
			s.setE(base);
			s.setSub(sub);
			return factory.createCTOMathArgSSub(s);
		}
		CTSSup s = factory.createCTSSup();
		s.setE(base);
		s.setSup(sup);
		return factory.createCTOMathArgSSup(s);
	}

	// ---------------------------------------------------------------- nary

	private Object nary(String chr, String command, Ctx ctx)
			throws LatexMathException {

		CTNary nary = factory.createCTNary();
		CTNaryPr naryPr = factory.createCTNaryPr();
		org.docx4j.math.CTChar chrEl = factory.createCTChar();
		chrEl.setVal(chr);
		naryPr.setChr(chrEl);
		org.docx4j.math.CTLimLoc limLoc = factory.createCTLimLoc();
		limLoc.setVal(command.contains("int") || "oint".equals(command)
				? STLimLoc.SUB_SUP : STLimLoc.UND_OVR);
		naryPr.setLimLoc(limLoc);
		nary.setNaryPr(naryPr);

		CTOMathArg sub = null;
		CTOMathArg sup = null;
		skipWhitespace();
		while (pos < src.length()) {
			char c = src.charAt(pos);
			if (c == '_' && sub == null) {
				pos++;
				sub = parseArg(ctx);
			} else if (c == '^' && sup == null) {
				pos++;
				sup = parseArg(ctx);
			} else {
				break;
			}
			skipWhitespace();
		}
		if (sub == null) {
			naryPr.setSubHide(factory.createCTOnOff());
			sub = factory.createCTOMathArg();
		}
		if (sup == null) {
			naryPr.setSupHide(factory.createCTOnOff());
			sup = factory.createCTOMathArg();
		}
		nary.setSub(sub);
		nary.setSup(sup);
		// like texmath, the operand is not grouped in LaTeX, so the base is
		// empty and following content simply flows after the operator
		nary.setE(factory.createCTOMathArg());
		return factory.createCTOMathArgNary(nary);
	}

	// ---------------------------------------------------------------- structures

	/** {@code \begin{cases}} → an m:eqArr behind a lone "{" delimiter. */
	private Object parseCases(Ctx ctx) throws LatexMathException {
		Object eqArr = parseEqArr(ctx, "cases");
		CTD d = factory.createCTD();
		org.docx4j.math.CTDPr dPr = factory.createCTDPr();
		org.docx4j.math.CTChar begChr = factory.createCTChar();
		begChr.setVal("{");
		dPr.setBegChr(begChr);
		org.docx4j.math.CTChar endChr = factory.createCTChar();
		endChr.setVal("");
		dPr.setEndChr(endChr);
		d.setDPr(dPr);
		d.getE().add(argOf(List.of(eqArr)));
		return factory.createCTOMathArgD(d);
	}

	/** {@code \xrightarrow{f}}, {@code \overset{a}{b}}: content above a base. */
	private Object limUpp(CTOMathArg base, CTOMathArg lim) {
		org.docx4j.math.CTLimUpp limUpp = factory.createCTLimUpp();
		limUpp.setE(base);
		limUpp.setLim(lim);
		return factory.createCTOMathArgLimUpp(limUpp);
	}

	private List<Object> parseArgAsScript(Ctx outer, org.docx4j.math.STScript script)
			throws LatexMathException {
		Ctx ctx = outer.copy();
		ctx.scr = script;
		return parseArgAsSequence(ctx);
	}

	/** {@code \textbf}/{@code \textit}: normal text carrying w:rPr bold/italic. */
	private Object boldOrItalicTextRun(String text, boolean bold, boolean italic) {
		CTR r = factory.createCTR();
		CTRPR mathRPr = factory.createCTRPR();
		mathRPr.setNor(factory.createCTOnOff());
		r.getContent().add(factory.createCTRRPrMath(mathRPr));
		org.docx4j.wml.RPr wmlRPr = new org.docx4j.wml.ObjectFactory().createRPr();
		if (bold) {
			wmlRPr.setB(new org.docx4j.wml.ObjectFactory().createBooleanDefaultTrue());
		}
		if (italic) {
			wmlRPr.setI(new org.docx4j.wml.ObjectFactory().createBooleanDefaultTrue());
		}
		r.getContent().add(factory.createCTRRPr(wmlRPr));
		CTText t = factory.createCTText();
		t.setValue(text);
		t.setSpace("preserve");
		r.getContent().add(factory.createCTRTMath(t));
		return factory.createCTOMathArgR(r);
	}

	/** The single m:t of a one-run element list, else null. */
	private static CTText singleRunText(Object element) {
		CTR run = asRun(element);
		return (run == null) ? null : textOf(run);
	}

	private Object accent(String combiningChar, Ctx ctx) throws LatexMathException {
		org.docx4j.math.CTAcc acc = factory.createCTAcc();
		org.docx4j.math.CTAccPr accPr = factory.createCTAccPr();
		org.docx4j.math.CTChar chr = factory.createCTChar();
		chr.setVal(combiningChar);
		accPr.setChr(chr);
		acc.setAccPr(accPr);
		acc.setE(parseArg(ctx));
		return factory.createCTOMathArgAcc(acc);
	}

	private Object bar(org.docx4j.math.STTopBot position, Ctx ctx) throws LatexMathException {
		org.docx4j.math.CTBar bar = factory.createCTBar();
		org.docx4j.math.CTBarPr barPr = factory.createCTBarPr();
		org.docx4j.math.CTTopBot pos = factory.createCTTopBot();
		pos.setVal(position);
		barPr.setPos(pos);
		bar.setBarPr(barPr);
		bar.setE(parseArg(ctx));
		return factory.createCTOMathArgBar(bar);
	}

	/**
	 * {@code \begin{aligned} rows \\ separated \end{aligned}} → m:eqArr.
	 * '&' alignment marks are kept as literal characters in the row text —
	 * which is how Word's linear format stores alignment in an equation
	 * array.
	 */
	private Object parseEqArr(Ctx outerCtx, String environment) throws LatexMathException {

		Ctx ctx = outerCtx.copy();
		org.docx4j.math.CTEqArr eqArr = factory.createCTEqArr();
		List<List<Object>> atoms = new ArrayList<>();

		while (true) {
			skipWhitespace();
			if (pos >= src.length()) {
				throw err("missing \\end{" + environment + "}");
			}
			char c = src.charAt(pos);
			if (c == '\\') {
				int saved = pos;
				String command = readCommandName();
				if ("\\".equals(command)) {
					eqArr.getE().add(argOf(mergeAdjacentRuns(flatten(atoms))));
					atoms = new ArrayList<>();
					continue;
				}
				if ("end".equals(command)) {
					String closed = readRawGroup();
					if (!environment.equals(closed)) {
						throw err("\\begin{" + environment + "} closed by \\end{" + closed + "}");
					}
					break;
				}
				pos = saved; // an ordinary command: let parseAtom re-read it
				List<Object> atom = parseAtom(ctx);
				if (atom != null) {
					atoms.add(atom);
				}
				continue;
			}
			if (c == '_' || c == '^') {
				List<Object> base = atoms.isEmpty() ? new ArrayList<>()
						: atoms.remove(atoms.size() - 1);
				atoms.add(List.of(parseScripts(argOf(base), ctx)));
				continue;
			}
			List<Object> atom = parseAtom(ctx);
			if (atom != null) {
				atoms.add(atom);
			}
		}

		if (!atoms.isEmpty() || eqArr.getE().isEmpty()) {
			eqArr.getE().add(argOf(mergeAdjacentRuns(flatten(atoms))));
		}
		return factory.createCTOMathArgEqArr(eqArr);
	}

	private static List<Object> flatten(List<List<Object>> atoms) {
		List<Object> out = new ArrayList<>();
		for (List<Object> atom : atoms) {
			out.addAll(atom);
		}
		return out;
	}

	// ---------------------------------------------------------------- \left..\right

	private Object parseDelimited(Ctx ctx) throws LatexMathException {

		String beg = readDelimiterChar();
		List<CTOMathArg> args = new ArrayList<>();
		List<List<Object>> atoms = new ArrayList<>();
		String sep = null;
		String end = null;
		while (true) {
			skipWhitespace();
			if (pos >= src.length()) {
				throw err("missing \\right");
			}
			char c = src.charAt(pos);
			if (c == '\\' && src.startsWith("\\right", pos)
					&& !continuesAsLetter(pos + "\\right".length())) {
				pos += "\\right".length();
				end = readDelimiterChar();
				break;
			}
			if (c == '\\' && src.startsWith("\\middle", pos)
					&& !continuesAsLetter(pos + "\\middle".length())) {
				// \middle| splits the content: OMML's sepChr between m:e args
				pos += "\\middle".length();
				String delimiter = readDelimiterChar();
				if (sep == null) {
					sep = delimiter;
				}
				args.add(argOf(mergeAdjacentRuns(flatten(atoms))));
				atoms = new ArrayList<>();
				continue;
			}
			if (c == '_' || c == '^') {
				List<Object> base = atoms.isEmpty() ? new ArrayList<>()
						: atoms.remove(atoms.size() - 1);
				atoms.add(List.of(parseScripts(argOf(base), ctx)));
				continue;
			}
			List<Object> atom = parseAtom(ctx);
			if (atom != null) {
				atoms.add(atom);
			}
		}
		args.add(argOf(mergeAdjacentRuns(flatten(atoms))));

		CTD d = factory.createCTD();
		org.docx4j.math.CTDPr dPr = factory.createCTDPr();
		org.docx4j.math.CTChar begChr = factory.createCTChar();
		begChr.setVal(beg);
		dPr.setBegChr(begChr);
		org.docx4j.math.CTChar endChr = factory.createCTChar();
		endChr.setVal(end);
		dPr.setEndChr(endChr);
		if (sep != null) {
			org.docx4j.math.CTChar sepChr = factory.createCTChar();
			sepChr.setVal(sep);
			dPr.setSepChr(sepChr);
		}
		d.setDPr(dPr);
		d.getE().addAll(args);
		return factory.createCTOMathArgD(d);
	}

	private boolean continuesAsLetter(int index) {
		return index < src.length() && Character.isLetter(src.charAt(index));
	}

	private String readDelimiterChar() throws LatexMathException {
		skipWhitespace();
		if (pos >= src.length()) {
			throw err("missing delimiter");
		}
		char c = src.charAt(pos);
		if (c == '\\') {
			String name = readCommandName();
			switch (name) {
			case "{": return "{";
			case "}": return "}";
			case "|": return "‖";
			case "langle": return "⟨";
			case "rangle": return "⟩";
			case "lfloor": return "⌊";
			case "rfloor": return "⌋";
			case "lceil": return "⌈";
			case "rceil": return "⌉";
			default: throw err("unsupported delimiter \\" + name);
			}
		}
		pos++;
		switch (c) {
		case '(': case ')': case '[': case ']': case '|': case '<': case '>':
			return String.valueOf(c);
		case '.':
			return ""; // invisible delimiter
		default:
			throw err("unsupported delimiter '" + c + "'");
		}
	}

	// ---------------------------------------------------------------- runs

	private Object run(String text, Ctx ctx) {
		return runElement(text, ctx.normalText, ctx.sty, ctx.scr, false);
	}

	private Object textRun(String text, boolean normalText, Ctx ctx) {
		// \text -> m:nor (prose); \mathrm/function names -> upright math (m:sty p)
		return runElement(text, normalText || ctx.normalText,
				normalText ? null : STStyle.P, null, true);
	}

	private Object runElement(String text, boolean normalText, STStyle sty,
			org.docx4j.math.STScript scr, boolean preserveSpace) {
		CTR r = factory.createCTR();
		if (normalText || sty != null || scr != null) {
			CTRPR rPr = factory.createCTRPR();
			if (normalText) {
				rPr.setNor(factory.createCTOnOff());
			} else if (sty != null) {
				org.docx4j.math.CTStyle style = factory.createCTStyle();
				style.setVal(sty);
				rPr.setSty(style);
			}
			if (scr != null && !normalText) {
				org.docx4j.math.CTScript script = factory.createCTScript();
				script.setVal(scr);
				rPr.setScr(script);
			}
			r.getContent().add(factory.createCTRRPrMath(rPr));
		}
		CTText t = factory.createCTText();
		t.setValue(text);
		if (preserveSpace || text.startsWith(" ") || text.endsWith(" ")) {
			t.setSpace("preserve");
		}
		r.getContent().add(factory.createCTRTMath(t));
		return factory.createCTOMathArgR(r);
	}

	private CTOMathArg argOf(List<Object> elements) {
		CTOMathArg arg = factory.createCTOMathArg();
		arg.getEGOMathElements().addAll(elements);
		return arg;
	}

	/** Adjacent plain-char runs with identical formatting become one m:r. */
	@SuppressWarnings("unchecked")
	private List<Object> mergeAdjacentRuns(List<Object> elements) {
		List<Object> out = new ArrayList<>();
		for (Object o : elements) {
			CTR run = asRun(o);
			CTR previous = out.isEmpty() ? null : asRun(out.get(out.size() - 1));
			if (run != null && previous != null
					&& sameFormatting(previous, run)) {
				CTText prevText = textOf(previous);
				CTText text = textOf(run);
				if (prevText != null && text != null) {
					prevText.setValue(prevText.getValue() + text.getValue());
					if ("preserve".equals(text.getSpace())) {
						prevText.setSpace("preserve");
					}
					continue;
				}
			}
			out.add(o);
		}
		return out;
	}

	private static CTR asRun(Object o) {
		if (o instanceof jakarta.xml.bind.JAXBElement
				&& ((jakarta.xml.bind.JAXBElement<?>) o).getValue() instanceof CTR) {
			return (CTR) ((jakarta.xml.bind.JAXBElement<?>) o).getValue();
		}
		return null;
	}

	private static CTText textOf(CTR run) {
		CTText found = null;
		for (Object o : run.getContent()) {
			if (o instanceof jakarta.xml.bind.JAXBElement) {
				Object v = ((jakarta.xml.bind.JAXBElement<?>) o).getValue();
				if (v instanceof CTText) {
					if (found != null) {
						return null; // more than one m:t: don't merge
					}
					found = (CTText) v;
				}
			}
		}
		return found;
	}

	private static boolean sameFormatting(CTR a, CTR b) {
		CTRPR pa = rPrOf(a);
		CTRPR pb = rPrOf(b);
		boolean norA = pa != null && pa.getNor() != null;
		boolean norB = pb != null && pb.getNor() != null;
		STStyle styA = (pa != null && pa.getSty() != null) ? pa.getSty().getVal() : null;
		STStyle styB = (pb != null && pb.getSty() != null) ? pb.getSty().getVal() : null;
		org.docx4j.math.STScript scrA = (pa != null && pa.getScr() != null) ? pa.getScr().getVal() : null;
		org.docx4j.math.STScript scrB = (pb != null && pb.getScr() != null) ? pb.getScr().getVal() : null;
		return norA == norB && styA == styB && scrA == scrB
				&& wmlRPrOf(a) == null && wmlRPrOf(b) == null; // \textbf runs never merge
	}

	private static org.docx4j.wml.RPr wmlRPrOf(CTR r) {
		for (Object o : r.getContent()) {
			if (o instanceof jakarta.xml.bind.JAXBElement
					&& ((jakarta.xml.bind.JAXBElement<?>) o).getValue() instanceof org.docx4j.wml.RPr) {
				return (org.docx4j.wml.RPr) ((jakarta.xml.bind.JAXBElement<?>) o).getValue();
			}
		}
		return null;
	}

	private static CTRPR rPrOf(CTR r) {
		for (Object o : r.getContent()) {
			if (o instanceof jakarta.xml.bind.JAXBElement
					&& ((jakarta.xml.bind.JAXBElement<?>) o).getValue() instanceof CTRPR) {
				return (CTRPR) ((jakarta.xml.bind.JAXBElement<?>) o).getValue();
			}
		}
		return null;
	}

	// ---------------------------------------------------------------- lexing

	private void skipWhitespace() {
		while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
			pos++;
		}
	}

	private LatexMathException err(String message) {
		return new LatexMathException(message + " (at index " + pos + ")");
	}

	// ---------------------------------------------------------------- tables

	static final Map<String, String> NARY = new HashMap<>();
	static final Map<String, String> SYMBOLS = new HashMap<>();
	static final java.util.Set<String> FUNCTION_NAMES = java.util.Set.of(
			"sin", "cos", "tan", "cot", "sec", "csc",
			"arcsin", "arccos", "arctan", "sinh", "cosh", "tanh",
			"log", "ln", "lg", "exp", "max", "min", "sup", "inf",
			"lim", "arg", "det", "dim", "gcd", "mod");

	static {
		NARY.put("sum", "∑");
		NARY.put("prod", "∏");
		NARY.put("coprod", "∐");
		NARY.put("int", "∫");
		NARY.put("iint", "∬");
		NARY.put("iiint", "∭");
		NARY.put("oint", "∮");
		NARY.put("bigcup", "⋃");
		NARY.put("bigcap", "⋂");

		// greek
		String[][] greek = {
			{"alpha", "α"}, {"beta", "β"}, {"gamma", "γ"}, {"delta", "δ"},
			{"epsilon", "ϵ"}, {"varepsilon", "ε"}, {"zeta", "ζ"}, {"eta", "η"},
			{"theta", "θ"}, {"vartheta", "ϑ"}, {"iota", "ι"}, {"kappa", "κ"},
			{"lambda", "λ"}, {"mu", "μ"}, {"nu", "ν"}, {"xi", "ξ"},
			{"pi", "π"}, {"varpi", "ϖ"}, {"rho", "ρ"}, {"varrho", "ϱ"},
			{"sigma", "σ"}, {"varsigma", "ς"}, {"tau", "τ"}, {"upsilon", "υ"},
			{"phi", "ϕ"}, {"varphi", "φ"}, {"chi", "χ"}, {"psi", "ψ"},
			{"omega", "ω"},
			{"Gamma", "Γ"}, {"Delta", "Δ"}, {"Theta", "Θ"}, {"Lambda", "Λ"},
			{"Xi", "Ξ"}, {"Pi", "Π"}, {"Sigma", "Σ"}, {"Upsilon", "Υ"},
			{"Phi", "Φ"}, {"Psi", "Ψ"}, {"Omega", "Ω"},
		};
		for (String[] g : greek) {
			SYMBOLS.put(g[0], g[1]);
		}

		// operators, relations, arrows, misc
		String[][] symbols = {
			{"pm", "±"}, {"mp", "∓"}, {"times", "×"}, {"cdot", "⋅"}, {"div", "÷"},
			{"ast", "∗"}, {"star", "⋆"}, {"circ", "∘"}, {"bullet", "•"},
			{"oplus", "⊕"}, {"ominus", "⊖"}, {"otimes", "⊗"}, {"oslash", "⊘"},
			{"le", "≤"}, {"leq", "≤"}, {"ge", "≥"}, {"geq", "≥"},
			{"ne", "≠"}, {"neq", "≠"}, {"approx", "≈"}, {"sim", "∼"},
			{"simeq", "≃"}, {"cong", "≅"}, {"equiv", "≡"}, {"propto", "∝"},
			{"ll", "≪"}, {"gg", "≫"}, {"prec", "≺"}, {"succ", "≻"},
			{"to", "→"}, {"rightarrow", "→"}, {"leftarrow", "←"},
			{"Rightarrow", "⇒"}, {"Leftarrow", "⇐"},
			{"leftrightarrow", "↔"}, {"Leftrightarrow", "⇔"},
			{"mapsto", "↦"}, {"uparrow", "↑"}, {"downarrow", "↓"},
			{"longrightarrow", "⟶"}, {"longleftarrow", "⟵"},
			{"longleftrightarrow", "⟷"}, {"Longrightarrow", "⟹"},
			{"Longleftarrow", "⟸"}, {"Longleftrightarrow", "⟺"},
			{"implies", "⟹"}, {"iff", "⟺"}, {"mid", "∣"},
			{"infty", "∞"}, {"partial", "∂"}, {"nabla", "∇"},
			{"hbar", "ℏ"}, {"ell", "ℓ"}, {"Re", "ℜ"}, {"Im", "ℑ"},
			{"aleph", "ℵ"}, {"wp", "℘"}, {"prime", "′"}, {"degree", "°"},
			{"cdots", "⋯"}, {"ldots", "…"}, {"dots", "…"}, {"vdots", "⋮"},
			{"ddots", "⋱"},
			{"in", "∈"}, {"notin", "∉"}, {"ni", "∋"},
			{"subset", "⊂"}, {"subseteq", "⊆"}, {"supset", "⊃"}, {"supseteq", "⊇"},
			{"cup", "∪"}, {"cap", "∩"}, {"setminus", "∖"},
			{"emptyset", "∅"}, {"varnothing", "∅"},
			{"forall", "∀"}, {"exists", "∃"}, {"nexists", "∄"}, {"neg", "¬"},
			{"wedge", "∧"}, {"land", "∧"}, {"vee", "∨"}, {"lor", "∨"},
			{"angle", "∠"}, {"perp", "⊥"}, {"parallel", "∥"}, {"mid", "∣"},
			{"langle", "⟨"}, {"rangle", "⟩"},
		};
		for (String[] s : symbols) {
			SYMBOLS.put(s[0], s[1]);
		}
	}

}
