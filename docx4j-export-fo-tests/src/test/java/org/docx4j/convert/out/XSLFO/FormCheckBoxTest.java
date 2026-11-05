/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
 *
 * docx4j is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A legacy check box in the FO, on both pathways: a stroked square with what follows set past it
 * (probe formcheckbox-metrics, Word's goldens), and no character of its own.
 *
 * @since 17.3.6
 */
public class FormCheckBoxTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final String SVG = "http://www.w3.org/2000/svg";

	private static String field(String rPr, String checkBox) {
		String r = "<w:r>" + rPr;
		return r + "<w:fldChar w:fldCharType=\"begin\"><w:ffData><w:name w:val=\"Check\"/><w:enabled/>"
				+ "<w:checkBox>" + checkBox + "</w:checkBox></w:ffData></w:fldChar></w:r>"
				+ r + "<w:instrText xml:space=\"preserve\"> FORMCHECKBOX </w:instrText></w:r>"
				+ r + "<w:fldChar w:fldCharType=\"separate\"/></w:r>"
				+ r + "<w:fldChar w:fldCharType=\"end\"/></w:r>";
	}

	private static org.w3c.dom.Document fo(String paragraphContent, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p>" + paragraphContent + "</w:p></w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private static final String TEN_POINT = "<w:rPr><w:sz w:val=\"20\"/></w:rPr>";
	private static final String AUTO = "<w:sizeAuto/><w:default w:val=\"0\"/>";

	private void check(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(field(TEN_POINT, AUTO) + "<w:r><w:t>label</w:t></w:r>", flags);
		NodeList objects = doc.getElementsByTagNameNS(FO, "instream-foreign-object");
		assertEquals(1, objects.getLength());
		Element object = (Element) objects.item(0);
		assertEquals("the side 9.05 and the gap 2.4", "11.450pt", object.getAttribute("content-width"));
		Element square = (Element) doc.getElementsByTagNameNS(SVG, "rect").item(0);
		assertEquals("9.050", square.getAttribute("width"));
		assertEquals("0.980", square.getAttribute("x"));
		assertEquals("#000000", square.getAttribute("stroke"));
		assertEquals("not checked", 0, doc.getElementsByTagNameNS(SVG, "path").getLength());
		String text = doc.getDocumentElement().getTextContent();
		assertFalse("nothing in the text layer: " + text, text.contains("☐"));
		assertTrue(text.contains("label"));

		org.w3c.dom.Document checked = fo(field("<w:rPr><w:sz w:val=\"20\"/><w:color w:val=\"000080\"/></w:rPr>",
				"<w:sizeAuto/><w:default w:val=\"0\"/><w:checked/>"), flags);
		assertEquals("both diagonals", 1, checked.getElementsByTagNameNS(SVG, "path").getLength());
		assertEquals("the run's colour", "#000080", ((Element) checked.getElementsByTagNameNS(SVG, "rect").item(0)).getAttribute("stroke"));
	}

	@Test
	public void visitorPathway() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xsltPathway() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
