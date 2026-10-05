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
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A REF or PAGEREF field whose bookmark the document does not hold prints Word's error text,
 * in the field result's formatting with bold toggled, as Word's PDF export does
 * (word-layout-rules.md &#xa7;4.4, "A reference whose bookmark is gone"): measured over the four
 * corpora, every one of the ~1,000 such fields in 12 documents.  Bookmark names match ignoring
 * case, as Word matches them.
 *
 * @since 17.3.1
 */
public class FieldErrorTextTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	@After
	public void restore() {
		Docx4jProperties.getProperties().remove("docx4j.convert.out.fo.fieldErrors");
	}

	/** a complex field: code, then its cached result in a run of these properties */
	private static String field(String code, String rPr, String result) {
		return "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>"
				+ "<w:r><w:instrText xml:space=\"preserve\"> " + code + " </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>"
				+ "<w:r>" + rPr + "<w:t>" + result + "</w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";
	}

	private static final String BOLD = "<w:rPr><w:b/></w:rPr>";

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r>" + BOLD + "<w:t xml:space=\"preserve\">Entry </w:t></w:r>"
				+ field("PAGEREF _Toc1 \\h", BOLD, "5") + "</w:p>"
				+ "<w:p><w:r><w:t xml:space=\"preserve\">See </w:t></w:r>"
				+ field("REF _Ref2 \\h", "", "section 2") + "</w:p>"
				+ "<w:p><w:bookmarkStart w:id=\"0\" w:name=\"Target\"/><w:r><w:t>The target</w:t></w:r>"
				+ "<w:bookmarkEnd w:id=\"0\"/></w:p>"
				+ "<w:p><w:r><w:t xml:space=\"preserve\">And </w:t></w:r>"
				+ field("REF target \\h", "", "The target") + "</w:p>"
				// a form field's own bookmark, inside the field: Word resolves a REF to it (corpus 2823)
				+ "<w:p><w:r><w:t xml:space=\"preserve\">Form </w:t></w:r>" + field("REF  Text1", "", "XYZ") + "</w:p>"
				+ "<w:p><w:r><w:fldChar w:fldCharType=\"begin\"><w:ffData><w:name w:val=\"Text1\"/><w:enabled/>"
				+ "<w:calcOnExit w:val=\"0\"/><w:textInput/></w:ffData></w:fldChar></w:r>"
				+ "<w:bookmarkStart w:id=\"1\" w:name=\"Text1\"/>"
				+ "<w:r><w:instrText xml:space=\"preserve\"> FORMTEXT </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>XYZ</w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r><w:bookmarkEnd w:id=\"1\"/></w:p>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** the element holding this text */
	private static Element holderOf(org.w3c.dom.Document doc, String text) {
		NodeList inlines = doc.getElementsByTagNameNS(FO_NS, "inline");
		for (int i = 0; i < inlines.getLength(); i++) {
			for (Node n = inlines.item(i).getFirstChild(); n != null; n = n.getNextSibling()) {
				if (n.getNodeType() == Node.TEXT_NODE && n.getNodeValue().contains(text)) return (Element) inlines.item(i);
			}
		}
		return null;
	}

	private static String weight(Element e) {
		for (Node n = e; n instanceof Element; n = n.getParentNode()) {
			String w = ((Element) n).getAttribute("font-weight");
			if (w.length() > 0) return w;
		}
		return "normal";
	}

	private void check(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(flags);
		String text = doc.getDocumentElement().getTextContent();

		Element pageref = holderOf(doc, "Error! Bookmark not defined.");
		assertNotNull("the PAGEREF's error text", pageref);
		assertEquals("beside a bold result the error is regular", "normal", weight(pageref));

		Element ref = holderOf(doc, "Error! Reference source not found.");
		assertNotNull("the REF's error text", ref);
		assertEquals("beside a regular result the error is bold", "bold", weight(ref));

		assertFalse("the cached results of the missing ones are gone", text.contains("section 2"));
		assertTrue("a bookmark named in another case is found", text.contains("And The target"));
		assertTrue("a form field's bookmark, inside the field, is found", text.contains("Form XYZ"));
		assertEquals("one REF error only", text.indexOf("Error! Reference source not found."),
				text.lastIndexOf("Error! Reference source not found."));
		assertFalse("no hint is left behind", XmlUtils.w3CDomNodeToString(doc).contains("docx4j-field-error"));
	}

	@Test
	public void visitorPathway() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xsltPathway() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** docx4j.convert.out.fo.fieldErrors=cached: the cached results, as 17.1.0 to 17.3.0 */
	@Test
	public void cachedOnRequest() throws Exception {
		Docx4jProperties.setProperty("docx4j.convert.out.fo.fieldErrors", "cached");
		String text = fo(Docx4J.FLAG_NONE).getDocumentElement().getTextContent();
		assertFalse(text.contains("Error!"));
		assertTrue(text.contains("See section 2"));
	}
}
