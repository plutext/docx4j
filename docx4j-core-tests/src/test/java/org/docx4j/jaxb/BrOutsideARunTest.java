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
package org.docx4j.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.Body;
import org.docx4j.wml.Br;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Document;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
import org.junit.Test;

/**
 * A {@code w:br} outside a {@code w:r}, read as Word reads it (CR-073): directly in a
 * {@code w:p}, a {@code w:hyperlink} or {@code w:body}, or inside a {@code w:t} or a
 * {@code w:instrText}, it is a break at that point, and all the text is kept.  docx4j's
 * load survey found one in 206,351 of 3.7 million Common Crawl documents; docx4j dropped
 * the break, and inside a {@code w:t} the text before it too.
 *
 * <p>The probe files in {@code br-placement/} are compared with Word 365's saves of them:
 * the text, the breaks and their types, the field characters and paragraph ends, in
 * order, with the run properties of each.</p>
 */
public class BrOutsideARunTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\"";

	// ---------------------------------------------------------------- Word's saves

	private static void sameAsWord(String name) throws Exception {
		assertEquals(name, tokens(load(name + "-word365.docx")), tokens(load(name + ".docx")));
	}

	@Test public void paragraphOfOnlyABreak() throws Exception { sameAsWord("br-1-p-alone"); }
	@Test public void betweenRuns() throws Exception { sameAsWord("br-2-p-between-runs"); }
	@Test public void insideText() throws Exception { sameAsWord("br-3-t-mixed"); }
	@Test public void betweenNestedTexts() throws Exception { sameAsWord("br-4-t-nested"); }
	@Test public void inHyperlink() throws Exception { sameAsWord("br-5-hyperlink"); }
	@Test public void inTableCell() throws Exception { sameAsWord("br-6-cell"); }
	@Test public void inFooter() throws Exception { sameAsWord("br-7-footer"); }
	@Test public void pageBreakInParagraph() throws Exception { sameAsWord("br-8-p-page"); }

	/** The break's run properties (D1): the mark's while no run precedes it in its
	 *  paragraph, none after one; inside a run's w:t, that run's. */
	@Test public void runProperties() throws Exception { sameAsWord("br2-1-run-properties"); }

	/** A field code keeps its break, the w:instrText split round it (D2). */
	@Test public void inFieldCode() throws Exception { sameAsWord("br2-2-instrtext-field"); }

	/** Directly in w:body: the next paragraph starts with it, a page break too (D2). */
	@Test public void inBody() throws Exception { sameAsWord("br2-4-body-br"); }
	@Test public void pageBreakInBody() throws Exception { sameAsWord("br2-5-body-br-page"); }

	/** A w:instrText with no field round it: Word saves it as w:t and shows it (D6, out of
	 *  CR-073); docx4j keeps it a field code, its text and breaks now whole. */
	@Test public void bareFieldCodeInHeader() throws Exception {
		List<String> ours = tokens(load("br2-3-instrtext-bare-header.docx"));
		assertTrue(ours.toString(), ours.contains("instrText: I2 line one "));
		List<String> asText = new ArrayList<String>();
		for (String t : ours) {
			asText.add(t.startsWith("instrText") ? "t" + t.substring("instrText".length()) : t);
		}
		assertEquals(tokens(load("br2-3-instrtext-bare-header-word365.docx")), asText);
	}

	// ---------------------------------------------------------------- the rules' edges

	/** A math run holds a w:br legally, and keeps it while a stray one beside it is repaired;
	 *  a w:br in a w:r is left as it is. */
	@Test public void legalBreaksUntouched() throws Exception {
		Body body = body("<w:p><w:r><w:t>a</w:t><w:br/></w:r>"
				+ "<m:oMath><m:r><w:br/><m:t>x</m:t></m:r></m:oMath>"
				+ "<w:br/><w:r><w:t>b</w:t></w:r></w:p>");
		P p = (P) body.getContent().get(0);
		assertEquals("[t:a, br, br, t:b, ¶]", tokens(p).toString());
		org.docx4j.math.CTOMath math = null;
		for (Object o : p.getContent()) {
			if (XmlUtils.unwrap(o) instanceof org.docx4j.math.CTOMath) math = (org.docx4j.math.CTOMath) XmlUtils.unwrap(o);
		}
		assertNotNull(math);
		org.docx4j.math.CTR mr = (org.docx4j.math.CTR) XmlUtils.unwrap(math.getEGOMathElements().get(0));
		boolean br = false;
		for (Object o : mr.getContent()) br |= XmlUtils.unwrap(o) instanceof Br;
		assertTrue("the math run's w:br", br);
	}

	/** Whitespace between a text element's children is kept only where xml:space preserves it. */
	@Test public void whitespaceInTextWithoutPreserve() throws Exception {
		assertEquals("[br, ¶]", tokens(body("<w:p><w:r><w:t>  <w:br/></w:t></w:r></w:p>")).toString());
		assertEquals("[t:  , br, ¶]",
				tokens(body("<w:p><w:r><w:t xml:space=\"preserve\">  <w:br/></w:t></w:r></w:p>")).toString());
	}

	/** The mark's properties without what only a mark has (a tracked insertion of the mark). */
	@Test public void markPropertiesWithoutTheMarksOwn() throws Exception {
		Body body = body("<w:p><w:pPr><w:rPr><w:ins w:id=\"1\" w:author=\"a\"/><w:b/></w:rPr></w:pPr>"
				+ "<w:br/><w:r><w:t>x</w:t></w:r></w:p>");
		R r = (R) ((P) body.getContent().get(0)).getContent().get(0);
		assertTrue(XmlUtils.unwrap(r.getContent().get(0)) instanceof Br);
		assertNotNull(r.getRPr().getB());
	}

	/** Deleted text keeps its break, and a stray w:br in a w:del gets a run there. */
	@Test public void deletedText() throws Exception {
		assertEquals("[delText:x, br, delText:y, ¶]", tokens(body(
				"<w:p><w:del w:id=\"1\" w:author=\"a\"><w:r><w:delText>x<w:br/>y</w:delText></w:r></w:del></w:p>")).toString());
		assertEquals("[delText:x, br, delText:y, ¶]", tokens(body(
				"<w:p><w:del w:id=\"1\" w:author=\"a\"><w:r><w:delText>x</w:delText></w:r><w:br/>"
				+ "<w:r><w:delText>y</w:delText></w:r></w:del></w:p>")).toString());
	}

	/** In w:body before a paragraph with no w:pPr: still its first run; before a table (not
	 *  measured), a paragraph of its own. */
	@Test public void bodyBreakWithoutParagraphAfter() throws Exception {
		Body body = body("<w:p><w:r><w:t>a</w:t></w:r></w:p><w:br/><w:br/><w:p><w:r><w:t>b</w:t></w:r></w:p>"
				+ "<w:br w:type=\"page\"/><w:tbl><w:tblGrid><w:gridCol w:w=\"2000\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:p><w:r><w:t>c</w:t></w:r></w:p></w:tc></w:tr></w:tbl>");
		assertEquals("[t:a, ¶, br, br, t:b, ¶, br(page), ¶, t:c, ¶]", tokens(body).toString());
		assertTrue(body.getContent().get(3) instanceof Tbl || XmlUtils.unwrap(body.getContent().get(3)) instanceof Tbl);
	}

	// ---------------------------------------------------------------- helpers

	private static WordprocessingMLPackage load(String name) throws Exception {
		return WordprocessingMLPackage.load(ResourceUtils.getResource("br-placement/" + name));
	}

	private static Body body(String content) throws Exception {
		String xml = "<w:document " + W + "><w:body>" + content + "</w:body></w:document>";
		Object o = XmlUtils.unmarshal(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		return ((Document) XmlUtils.unwrap(o)).getBody();
	}

	/** The main part's tokens, then each header's and footer's. */
	private static List<String> tokens(WordprocessingMLPackage pkg) throws Exception {
		List<String> out = new ArrayList<String>();
		walk(pkg.getMainDocumentPart().getJaxbElement().getBody(), out);
		for (Part part : pkg.getParts().getParts().values()) {
			if (part instanceof HeaderPart) { out.add("header"); walk(((HeaderPart) part).getJaxbElement(), out); }
			if (part instanceof FooterPart) { out.add("footer"); walk(((FooterPart) part).getJaxbElement(), out); }
		}
		return out;
	}

	private static List<String> tokens(Object content) {
		List<String> out = new ArrayList<String>();
		walk(content, out);
		return out;
	}

	/** Text (merged where Word and docx4j split runs differently), breaks, field
	 *  characters and paragraph ends, each with its run's properties. */
	private static void walk(Object o, List<String> out) {
		if (o instanceof JAXBElement) {
			o = ((JAXBElement<?>) o).getValue();
		}
		if (o instanceof R) {
			R r = (R) o;
			String props = props(r.getRPr());
			for (Object c : r.getContent()) {
				String name = c instanceof JAXBElement ? ((JAXBElement<?>) c).getName().getLocalPart() : null;
				Object v = XmlUtils.unwrap(c);
				String text = v instanceof Text ? ((Text) v).getValue()
						: v instanceof org.docx4j.wml.DelText ? ((org.docx4j.wml.DelText) v).getValue() : null;
				if (text != null) {
					String head = (name == null ? "delText" : name) + props + ":";
					int last = out.size() - 1;
					if (last >= 0 && out.get(last).startsWith(head)) out.set(last, out.get(last) + text);
					else out.add(head + text);
				} else if (v instanceof Br) {
					Br br = (Br) v;
					out.add("br" + (br.getType() == null ? "" : "(" + br.getType().value() + ")") + props);
				} else if (v instanceof FldChar) {
					out.add("fld:" + ((FldChar) v).getFldCharType().value());
				}
			}
			return;
		}
		if (o instanceof org.docx4j.wml.RunDel) {
			for (Object c : ((org.docx4j.wml.RunDel) o).getCustomXmlOrSmartTagOrSdt()) walk(c, out);
		} else if (o instanceof ContentAccessor) {
			for (Object c : ((ContentAccessor) o).getContent()) walk(c, out);
			if (o instanceof P) out.add("¶");
		}
	}

	/** The properties the probes use: bold, italic, size. */
	private static String props(RPr rPr) {
		if (rPr == null) return "";
		StringBuilder sb = new StringBuilder();
		if (rPr.getB() != null && rPr.getB().isVal()) sb.append(" b");
		if (rPr.getI() != null && rPr.getI().isVal()) sb.append(" i");
		if (rPr.getSz() != null) sb.append(" sz").append(rPr.getSz().getVal());
		return sb.length() == 0 ? "" : "[" + sb.toString().trim() + "]";
	}
}
