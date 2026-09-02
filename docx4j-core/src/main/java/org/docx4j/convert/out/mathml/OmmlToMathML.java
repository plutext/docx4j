/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.

   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.docx4j.convert.out.mathml;

import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.math.CTAcc;
import org.docx4j.math.CTBar;
import org.docx4j.math.CTBorderBox;
import org.docx4j.math.CTBox;
import org.docx4j.math.CTD;
import org.docx4j.math.CTEqArr;
import org.docx4j.math.CTF;
import org.docx4j.math.CTGroupChr;
import org.docx4j.math.CTLimLow;
import org.docx4j.math.CTLimUpp;
import org.docx4j.math.CTM;
import org.docx4j.math.CTMR;
import org.docx4j.math.CTNary;
import org.docx4j.math.CTOMath;
import org.docx4j.math.CTOMathArg;
import org.docx4j.math.CTOMathPara;
import org.docx4j.math.CTPhant;
import org.docx4j.math.CTR;
import org.docx4j.math.CTRPR;
import org.docx4j.math.CTRad;
import org.docx4j.math.CTSPre;
import org.docx4j.math.CTSSub;
import org.docx4j.math.CTSSubSup;
import org.docx4j.math.CTSSup;
import org.docx4j.math.CTText;
import org.docx4j.math.STFType;
import org.docx4j.math.STLimLoc;
import org.docx4j.math.STScript;
import org.docx4j.math.STStyle;
import org.docx4j.math.STTopBot;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Converts Word OMML ({@code org.docx4j.math}) to Presentation MathML, natively
 * in Java — no XSLT, so no dependency on Microsoft's non-redistributable
 * {@code OMML2MML.XSL}. Clean-room from ECMA-376 §22.1 and the W3C MathML spec;
 * see docs/developer/change-requests/CR-math-omml-mathml.md.
 *
 * <p>Output is a W3C DOM (or string) in the MathML namespace. Styles are emitted
 * as {@code mathvariant} attributes (Word itself uses the Mathematical
 * Alphanumeric Symbols block; the two render identically). Constructs outside the
 * supported subset raise {@link MathConversionException} so the caller can fall
 * back rather than fail the whole document.</p>
 *
 * <p>Not thread-safe: create one instance per conversion (or per thread).</p>
 *
 * @since 17.0.4
 */
public class OmmlToMathML {

	public static final String MML_NS = "http://www.w3.org/1998/Math/MathML";

	private Document doc;

	// ---------------------------------------------------------------- public

	public Document toMathMLDocument(CTOMath oMath) throws MathConversionException {
		newDoc();
		Element math = math(false);
		appendElements(math, oMath.getEGOMathElements());
		doc.appendChild(math);
		return doc;
	}

	public Document toMathMLDocument(CTOMathPara oMathPara) throws MathConversionException {
		newDoc();
		Element math = math(true);
		for (CTOMath oMath : oMathPara.getOMath()) {
			appendElements(math, oMath.getEGOMathElements());
		}
		doc.appendChild(math);
		return doc;
	}

	public String toMathMLString(CTOMath oMath) throws MathConversionException {
		return serialize(toMathMLDocument(oMath));
	}

	public String toMathMLString(CTOMathPara oMathPara) throws MathConversionException {
		return serialize(toMathMLDocument(oMathPara));
	}

	// ------------------------------------------------------------- dispatch

	private void appendElements(Element parent, List<Object> elements)
			throws MathConversionException {
		for (Object o : elements) {
			Object u = unwrap(o);
			if (u instanceof CTR) {
				appendRun(parent, (CTR) u);
			} else if (u instanceof CTF) {
				appendFraction(parent, (CTF) u);
			} else if (u instanceof CTRad) {
				appendRadical(parent, (CTRad) u);
			} else if (u instanceof CTSSub) {
				CTSSub s = (CTSSub) u;
				script(parent, "msub", s.getE(), s.getSub(), null);
			} else if (u instanceof CTSSup) {
				CTSSup s = (CTSSup) u;
				script(parent, "msup", s.getE(), null, s.getSup());
			} else if (u instanceof CTSSubSup) {
				CTSSubSup s = (CTSSubSup) u;
				script(parent, "msubsup", s.getE(), s.getSub(), s.getSup());
			} else if (u instanceof CTNary) {
				appendNary(parent, (CTNary) u);
			} else if (u instanceof CTD) {
				appendDelimiter(parent, (CTD) u);
			} else if (u instanceof CTM) {
				appendMatrix(parent, (CTM) u);
			} else if (u instanceof CTEqArr) {
				appendEqArr(parent, (CTEqArr) u);
			} else if (u instanceof CTSPre) {
				appendPreScript(parent, (CTSPre) u);
			} else if (u instanceof CTAcc) {
				appendAccent(parent, (CTAcc) u);
			} else if (u instanceof CTBar) {
				appendBar(parent, (CTBar) u);
			} else if (u instanceof CTLimLow) {
				CTLimLow l = (CTLimLow) u;
				script2(parent, "munder", l.getE(), l.getLim());
			} else if (u instanceof CTLimUpp) {
				CTLimUpp l = (CTLimUpp) u;
				script2(parent, "mover", l.getE(), l.getLim());
			} else if (u instanceof CTGroupChr) {
				appendGroupChr(parent, (CTGroupChr) u);
			} else if (u instanceof CTPhant) {
				Element mphantom = el("mphantom");
				appendFlat(mphantom, ((CTPhant) u).getE());
				parent.appendChild(mphantom);
			} else if (u instanceof CTBox) {
				// transparent grouping; render contents inline
				appendFlat(parent, ((CTBox) u).getE());
			} else if (u instanceof CTBorderBox) {
				Element menclose = el("menclose");
				menclose.setAttribute("notation", "box");
				appendFlat(menclose, ((CTBorderBox) u).getE());
				parent.appendChild(menclose);
			} else if (u instanceof org.docx4j.math.CTFunc) {
				appendFunc(parent, (org.docx4j.math.CTFunc) u);
			} else if (u instanceof CTOMath) {
				appendElements(parent, ((CTOMath) u).getEGOMathElements());
			} else {
				throw new MathConversionException(
						"unsupported OMML element " + u.getClass().getSimpleName());
			}
		}
	}

	// ------------------------------------------------------------------ run

	private void appendRun(Element parent, CTR r) {
		CTRPR rPr = null;
		StringBuilder text = new StringBuilder();
		for (Object o : r.getContent()) {
			Object u = unwrap(o);
			if (u instanceof CTRPR) {
				rPr = (CTRPR) u;
			} else if (u instanceof CTText) {
				text.append(((CTText) u).getValue());
			} else if (u instanceof org.docx4j.wml.Text) {
				text.append(((org.docx4j.wml.Text) u).getValue());
			}
			// other run content (breaks etc.) is dropped
		}
		String s = text.toString();
		if (s.isEmpty()) {
			return;
		}

		// normal (non-italic) text: a literal string, not tokenised maths
		if (rPr != null && rPr.getNor() != null) {
			Element mtext = el("mtext");
			mtext.appendChild(doc.createTextNode(s));
			parent.appendChild(mtext);
			return;
		}

		String variant = mathvariant(rPr);
		int i = 0;
		while (i < s.length()) {
			char c = s.charAt(i);
			Element token;
			if (isDigit(c)) {
				int j = i;
				while (j < s.length() && isDigit(s.charAt(j))) {
					j++;
				}
				token = el("mn");
				token.appendChild(doc.createTextNode(s.substring(i, j)));
				i = j;
			} else if (Character.isLetter(c)) {
				token = el("mi");
				token.appendChild(doc.createTextNode(String.valueOf(c)));
				i++;
			} else {
				token = el("mo");
				token.appendChild(doc.createTextNode(String.valueOf(c)));
				i++;
			}
			if (variant != null) {
				token.setAttribute("mathvariant", variant);
			}
			parent.appendChild(token);
		}
	}

	private static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	/** OMML run style/script → MathML mathvariant, or null for the default. */
	private static String mathvariant(CTRPR rPr) {
		STScript scr = (rPr != null && rPr.getScr() != null) ? rPr.getScr().getVal() : null;
		STStyle sty = (rPr != null && rPr.getSty() != null) ? rPr.getSty().getVal() : null;
		boolean bold = sty == STStyle.B || sty == STStyle.BI;
		boolean italic = sty == STStyle.I || sty == STStyle.BI;
		if (scr != null) {
			switch (scr) {
			case SCRIPT:        return bold ? "bold-script" : "script";
			case FRAKTUR:       return bold ? "bold-fraktur" : "fraktur";
			case DOUBLE_STRUCK: return "double-struck";
			case MONOSPACE:     return "monospace";
			case SANS_SERIF:
				if (bold && italic) return "sans-serif-bold-italic";
				if (bold)           return "bold-sans-serif";
				if (italic)         return "sans-serif-italic";
				return "sans-serif";
			case ROMAN:
			default:
				break; // fall through to sty handling
			}
		}
		if (sty == STStyle.P)  return "normal";
		if (sty == STStyle.B)  return "bold";
		if (sty == STStyle.BI) return "bold-italic";
		// STStyle.I (and unset) is the default for an identifier — no attribute
		return null;
	}

	// ----------------------------------------------------------- structures

	private void appendFraction(Element parent, CTF f) throws MathConversionException {
		STFType type = (f.getFPr() != null && f.getFPr().getType() != null)
				? f.getFPr().getType().getVal() : null;
		if (type == STFType.LIN) {
			// linear fraction a/b
			Element mrow = el("mrow");
			appendArg(mrow, f.getNum());
			Element slash = el("mo");
			slash.appendChild(doc.createTextNode("/"));
			mrow.appendChild(slash);
			appendArg(mrow, f.getDen());
			parent.appendChild(mrow);
			return;
		}
		Element mfrac = el("mfrac");
		if (type == STFType.SKW) {
			mfrac.setAttribute("bevelled", "true");
		} else if (type == STFType.NO_BAR) {
			mfrac.setAttribute("linethickness", "0");
		}
		appendArg(mfrac, f.getNum());
		appendArg(mfrac, f.getDen());
		parent.appendChild(mfrac);
	}

	private void appendRadical(Element parent, CTRad rad) throws MathConversionException {
		boolean degHidden = rad.getRadPr() != null && rad.getRadPr().getDegHide() != null;
		if (!degHidden && hasContent(rad.getDeg())) {
			Element mroot = el("mroot");
			appendArg(mroot, rad.getE());     // base
			appendArg(mroot, rad.getDeg());   // index
			parent.appendChild(mroot);
		} else {
			Element msqrt = el("msqrt");
			appendFlat(msqrt, rad.getE());
			parent.appendChild(msqrt);
		}
	}

	private void script(Element parent, String tag, CTOMathArg base, CTOMathArg sub, CTOMathArg sup)
			throws MathConversionException {
		Element e = el(tag);
		appendArg(e, base);
		if (sub != null) {
			appendArg(e, sub);
		}
		if (sup != null) {
			appendArg(e, sup);
		}
		parent.appendChild(e);
	}

	private void script2(Element parent, String tag, CTOMathArg base, CTOMathArg script)
			throws MathConversionException {
		Element e = el(tag);
		appendArg(e, base);
		appendArg(e, script);
		parent.appendChild(e);
	}

	private void appendNary(Element parent, CTNary nary) throws MathConversionException {
		String chr = (nary.getNaryPr() != null && nary.getNaryPr().getChr() != null)
				? nary.getNaryPr().getChr().getVal() : "∫"; // OMML default: integral
		boolean subHidden = nary.getNaryPr() != null && nary.getNaryPr().getSubHide() != null;
		boolean supHidden = nary.getNaryPr() != null && nary.getNaryPr().getSupHide() != null;
		boolean hasSub = !subHidden && hasContent(nary.getSub());
		boolean hasSup = !supHidden && hasContent(nary.getSup());

		STLimLoc limLoc = (nary.getNaryPr() != null && nary.getNaryPr().getLimLoc() != null)
				? nary.getNaryPr().getLimLoc().getVal() : null;
		boolean underOver = limLoc == STLimLoc.UND_OVR
				|| (limLoc == null && !isIntegral(chr));

		Element op = el("mo");
		op.appendChild(doc.createTextNode(chr));

		Element wrapper;
		if (hasSub && hasSup) {
			wrapper = el(underOver ? "munderover" : "msubsup");
			wrapper.appendChild(op);
			appendArg(wrapper, nary.getSub());
			appendArg(wrapper, nary.getSup());
		} else if (hasSup) {
			wrapper = el(underOver ? "mover" : "msup");
			wrapper.appendChild(op);
			appendArg(wrapper, nary.getSup());
		} else if (hasSub) {
			wrapper = el(underOver ? "munder" : "msub");
			wrapper.appendChild(op);
			appendArg(wrapper, nary.getSub());
		} else {
			wrapper = op;
		}

		Element mrow = el("mrow");
		mrow.appendChild(wrapper);
		if (nary.getE() != null) {
			appendFlat(mrow, nary.getE());
		}
		parent.appendChild(mrow);
	}

	private static boolean isIntegral(String chr) {
		return chr != null && chr.length() == 1
				&& chr.charAt(0) >= '∫' && chr.charAt(0) <= '∳';
	}

	private void appendDelimiter(Element parent, CTD d) throws MathConversionException {
		String beg = (d.getDPr() != null && d.getDPr().getBegChr() != null)
				? d.getDPr().getBegChr().getVal() : "(";
		String end = (d.getDPr() != null && d.getDPr().getEndChr() != null)
				? d.getDPr().getEndChr().getVal() : ")";
		String sep = (d.getDPr() != null && d.getDPr().getSepChr() != null)
				? d.getDPr().getSepChr().getVal() : null;

		Element mrow = el("mrow");
		if (beg != null && !beg.isEmpty()) {
			mrow.appendChild(fence(beg));
		}
		boolean first = true;
		for (CTOMathArg arg : d.getE()) {
			if (!first && sep != null && !sep.isEmpty()) {
				mrow.appendChild(fence(sep));
			}
			first = false;
			appendArg(mrow, arg);
		}
		if (end != null && !end.isEmpty()) {
			mrow.appendChild(fence(end));
		}
		parent.appendChild(mrow);
	}

	private Element fence(String chr) {
		Element mo = el("mo");
		mo.appendChild(doc.createTextNode(chr));
		return mo;
	}

	private void appendMatrix(Element parent, CTM m) throws MathConversionException {
		Element mtable = el("mtable");
		for (CTMR row : m.getMr()) {
			Element mtr = el("mtr");
			for (CTOMathArg cell : row.getE()) {
				Element mtd = el("mtd");
				appendFlat(mtd, cell);
				mtr.appendChild(mtd);
			}
			mtable.appendChild(mtr);
		}
		parent.appendChild(mtable);
	}

	private void appendEqArr(Element parent, CTEqArr eqArr) throws MathConversionException {
		Element mtable = el("mtable");
		for (CTOMathArg row : eqArr.getE()) {
			Element mtr = el("mtr");
			Element mtd = el("mtd");
			appendFlat(mtd, row);
			mtr.appendChild(mtd);
			mtable.appendChild(mtr);
		}
		parent.appendChild(mtable);
	}

	private void appendPreScript(Element parent, CTSPre spre) throws MathConversionException {
		Element mm = el("mmultiscripts");
		appendArg(mm, spre.getE());          // base
		mm.appendChild(el("mprescripts"));
		scriptOrNone(mm, spre.getSub());     // pre-subscript
		scriptOrNone(mm, spre.getSup());     // pre-superscript
		parent.appendChild(mm);
	}

	private void scriptOrNone(Element parent, CTOMathArg arg) throws MathConversionException {
		if (hasContent(arg)) {
			appendArg(parent, arg);
		} else {
			parent.appendChild(el("none"));
		}
	}

	private void appendAccent(Element parent, CTAcc acc) throws MathConversionException {
		String chr = (acc.getAccPr() != null && acc.getAccPr().getChr() != null)
				? acc.getAccPr().getChr().getVal() : "̂"; // default: combining circumflex
		Element mover = el("mover");
		mover.setAttribute("accent", "true");
		appendArg(mover, acc.getE());
		Element mo = el("mo");
		mo.appendChild(doc.createTextNode(chr));
		mover.appendChild(mo);
		parent.appendChild(mover);
	}

	private void appendBar(Element parent, CTBar bar) throws MathConversionException {
		boolean top = bar.getBarPr() != null && bar.getBarPr().getPos() != null
				&& bar.getBarPr().getPos().getVal() == STTopBot.TOP;
		Element e = el(top ? "mover" : "munder");
		e.setAttribute("accent", "true");
		appendArg(e, bar.getE());
		Element mo = el("mo");
		mo.appendChild(doc.createTextNode(top ? "‾" : "_")); // overline / low line
		mo.setAttribute("stretchy", "true");
		e.appendChild(mo);
		parent.appendChild(e);
	}

	/** Function application: name, U+2061 APPLY FUNCTION, argument. */
	private void appendFunc(Element parent, org.docx4j.math.CTFunc fn)
			throws MathConversionException {
		Element mrow = el("mrow");
		appendFlat(mrow, fn.getFName());
		Element apply = el("mo");
		apply.appendChild(doc.createTextNode("⁡"));
		mrow.appendChild(apply);
		appendArg(mrow, fn.getE());
		parent.appendChild(mrow);
	}

	private void appendGroupChr(Element parent, CTGroupChr gc) throws MathConversionException {
		String chr = (gc.getGroupChrPr() != null && gc.getGroupChrPr().getChr() != null)
				? gc.getGroupChrPr().getChr().getVal() : "⏟"; // default: bottom brace
		boolean top = gc.getGroupChrPr() != null && gc.getGroupChrPr().getPos() != null
				&& gc.getGroupChrPr().getPos().getVal() == STTopBot.TOP;
		Element e = el(top ? "mover" : "munder");
		appendArg(e, gc.getE());
		Element mo = el("mo");
		mo.appendChild(doc.createTextNode(chr));
		mo.setAttribute("stretchy", "true");
		e.appendChild(mo);
		parent.appendChild(e);
	}

	// ------------------------------------------------------------------ args

	/** Render an argument as exactly one child of {@code parent} (wrapping in
	 *  {@code <mrow>} unless it produces exactly one element). */
	private void appendArg(Element parent, CTOMathArg arg) throws MathConversionException {
		Element mrow = el("mrow");
		if (arg != null) {
			appendElements(mrow, arg.getEGOMathElements());
		}
		if (mrow.getChildNodes().getLength() == 1) {
			parent.appendChild(mrow.getFirstChild()); // moves the single child
		} else {
			parent.appendChild(mrow); // 0 or >1 children stay wrapped
		}
	}

	/** Render an argument's elements directly into {@code parent} (an implicit
	 *  mrow context such as msqrt, mtd, or an n-ary operand). */
	private void appendFlat(Element parent, CTOMathArg arg) throws MathConversionException {
		if (arg != null) {
			appendElements(parent, arg.getEGOMathElements());
		}
	}

	private static boolean hasContent(CTOMathArg arg) {
		return arg != null && !arg.getEGOMathElements().isEmpty();
	}

	// ------------------------------------------------------------------ dom

	private void newDoc() throws MathConversionException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			doc = dbf.newDocumentBuilder().newDocument();
		} catch (Exception e) {
			throw new MathConversionException("could not create DOM document", e);
		}
	}

	private Element math(boolean display) {
		Element math = el("math");
		if (display) {
			math.setAttribute("display", "block");
		}
		return math;
	}

	private Element el(String name) {
		return doc.createElementNS(MML_NS, name);
	}

	private static Object unwrap(Object o) {
		return (o instanceof JAXBElement) ? ((JAXBElement<?>) o).getValue() : o;
	}

	private static String serialize(Document doc) throws MathConversionException {
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter sw = new StringWriter();
			t.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		} catch (Exception e) {
			throw new MathConversionException("could not serialise MathML", e);
		}
	}
}
