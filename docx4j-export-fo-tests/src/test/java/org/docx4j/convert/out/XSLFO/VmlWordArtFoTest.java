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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Document;
import org.docx4j.wml.Hdr;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * CR-083 phase 4: VML WordArt drawn in the FO and the PDF, by both routes: Word's text watermark in a header, drawn
 * with no text (D17); a VML WordArt in the body, its text invisible (D2); a shapetype with only its o:spt, nothing.
 */
public class VmlWordArtFoTest extends AbstractXSLFOTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
			+ " xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\""
			+ " xmlns:w10=\"urn:schemas-microsoft-com:office:word\"";
	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";
	private static final String SVG_NS = "http://www.w3.org/2000/svg";
	private static final int[] BOTH = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	/** Shapetype 136 as Word writes it with a watermark */
	private static final String T136 = "<v:shapetype id=\"_x0000_t136\" coordsize=\"21600,21600\" o:spt=\"136\" adj=\"10800\" "
			+ "path=\"m@7,l@8,m@5,21600l@6,21600e\"><v:formulas><v:f eqn=\"sum #0 0 10800\"/><v:f eqn=\"prod #0 2 1\"/>"
			+ "<v:f eqn=\"sum 21600 0 @1\"/><v:f eqn=\"sum 0 0 @2\"/><v:f eqn=\"sum 21600 0 @3\"/><v:f eqn=\"if @0 @3 0\"/>"
			+ "<v:f eqn=\"if @0 21600 @1\"/><v:f eqn=\"if @0 0 @2\"/><v:f eqn=\"if @0 @4 21600\"/><v:f eqn=\"mid @5 @6\"/>"
			+ "<v:f eqn=\"mid @8 @5\"/><v:f eqn=\"mid @7 @8\"/><v:f eqn=\"mid @6 @7\"/><v:f eqn=\"sum @6 0 @5\"/></v:formulas>"
			+ "<v:path textpathok=\"t\"/><v:textpath on=\"t\" fitshape=\"t\"/></v:shapetype>";

	private static final String MARK = "<w:p><w:r><w:pict>" + T136 + "<v:shape id=\"PowerPlusWaterMarkObject1\" type=\"#_x0000_t136\" "
			+ "style=\"position:absolute;margin-left:0;margin-top:0;width:468pt;height:117pt;rotation:315;z-index:-251659776;"
			+ "mso-position-horizontal:center;mso-position-horizontal-relative:margin;mso-position-vertical:center;"
			+ "mso-position-vertical-relative:margin\" o:allowincell=\"f\" fillcolor=\"silver\" stroked=\"f\"><v:fill opacity=\".5\"/>"
			+ "<v:textpath style=\"font-family:&quot;Arial&quot;;font-size:1pt\" string=\"CONFIDENTIAL\"/>"
			+ "<w10:wrap anchorx=\"margin\" anchory=\"margin\"/></v:shape></w:pict></w:r></w:p>";

	private static String body(String type) {
		return "<w:p><w:r><w:pict>" + type + "<v:shape id=\"WordArt1\" type=\"#_x0000_t136\" style=\"position:absolute;"
				+ "margin-left:144pt;margin-top:252pt;width:288pt;height:144pt;z-index:1;mso-position-horizontal-relative:page;"
				+ "mso-position-vertical-relative:page\" fillcolor=\"black\" stroked=\"f\"><v:textpath style=\"font-family:&quot;Arial&quot;;"
				+ "font-size:36pt\" trim=\"t\" fitpath=\"t\" string=\"WordArt\"/></v:shape></w:pict></w:r></w:p>";
	}

	private static WordprocessingMLPackage pkg(String body, String header) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String ref = "";
		if (header != null) {
			HeaderPart hp = new HeaderPart();
			hp.setJaxbElement((Hdr) XmlUtils.unmarshalString("<w:hdr " + NS + ">" + header + "</w:hdr>"));
			Relationship rel = pkg.getMainDocumentPart().addTargetPart(hp);
			ref = "<w:headerReference w:type=\"default\" r:id=\"" + rel.getId() + "\"/>";
		}
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString("<w:document " + NS + "><w:body>" + body
				+ "<w:p><w:r><w:t>body text</w:t></w:r></w:p><w:sectPr>" + ref + "<w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"708\" w:footer=\"708\"/>"
				+ "</w:sectPr></w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private static String pdfText(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		try (PDDocument d = Loader.loadPDF(baos.toByteArray())) {
			return new PDFTextStripper().getText(d);
		}
	}

	/** The SVG paths in the FO's static content (headers) or its flow */
	private static int paths(org.w3c.dom.Document fo, String where) {
		NodeList nl = fo.getElementsByTagNameNS(FO_NS, where);
		int n = 0;
		for (int i = 0; i < nl.getLength(); i++) n += ((Element) nl.item(i)).getElementsByTagNameNS(SVG_NS, "path").getLength();
		return n;
	}

	@Test
	public void aWatermarkIsDrawnWithNoText() throws Exception {
		for (int flags : BOTH) {
			WordprocessingMLPackage pkg = pkg("", MARK);
			org.w3c.dom.Document fo = fo(pkg, flags);
			assertTrue("flags " + flags + ": the watermark drawn in the header", paths(fo, "static-content") > 0);
			assertFalse("flags " + flags + ": no text in its drawing",
					fo.getDocumentElement().getTextContent().contains("CONFIDENTIAL"));
			String text = pdfText(pkg("", MARK), flags);
			assertTrue(text.contains("body text"));
			assertFalse("flags " + flags + ": " + text, text.contains("CONFIDENTIAL"));
		}
	}

	@Test
	public void inTheBodyItsTextIsInvisibleAndFound() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document fo = fo(pkg(body(T136), null), flags);
			assertTrue("flags " + flags, paths(fo, "flow") > 0);
			NodeList texts = fo.getElementsByTagNameNS(SVG_NS, "text");
			assertEquals("flags " + flags, 1, texts.getLength());
			assertEquals("none", ((Element) texts.item(0)).getAttribute("fill"));
			assertTrue("flags " + flags, pdfText(pkg(body(T136), null), flags).contains("WordArt"));
		}
	}

	/** real3's form, in the body: a w:object holding a v:group, its WordArt at its box through the group's coordinates (D18) */
	@Test
	public void aGroupsWordArtIsDrawn() throws Exception {
		String group = "<w:p><w:r><w:object><v:group id=\"g\" style=\"position:absolute;margin-left:72pt;margin-top:72pt;"
				+ "width:200pt;height:100pt;z-index:1;mso-position-horizontal-relative:page;mso-position-vertical-relative:page\" "
				+ "coordorigin=\"1000,1000\" coordsize=\"2000,1000\">" + T136
				+ "<v:shape id=\"s\" type=\"#_x0000_t136\" style=\"position:absolute;left:1200;top:1100;width:1000;height:500\" "
				+ "adj=\",10800\" fillcolor=\"blue\" strokecolor=\"blue\"><v:textpath style=\"font-family:&quot;Arial&quot;;font-size:8pt\" "
				+ "trim=\"t\" fitpath=\"t\" string=\"Grouped\"/></v:shape></v:group></w:object></w:r></w:p>";
		for (int flags : BOTH) {
			org.w3c.dom.Document fo = fo(pkg(group, null), flags);
			assertTrue("flags " + flags, paths(fo, "flow") > 0);
			// the member at (1200 - 1000, 1100 - 1000) of 2000 by 1000 units in 200 by 100 pt: 20, 10, 100 by 50 pt
			NodeList bc = fo.getElementsByTagNameNS(FO_NS, "block-container");
			boolean found = false;
			for (int i = 0; i < bc.getLength(); i++) {
				Element e = (Element) bc.item(i);
				if ("20pt".equals(e.getAttribute("left")) && "10pt".equals(e.getAttribute("top"))
						&& "100pt".equals(e.getAttribute("width")) && "50pt".equals(e.getAttribute("height"))) found = true;
			}
			assertTrue("flags " + flags + ": the member's box", found);
			assertTrue("flags " + flags, pdfText(pkg(group, null), flags).contains("Grouped"));
		}
	}

	@Test
	public void aShapetypeWithOnlyItsSptDrawsNothing() throws Exception {
		String sptOnly = "<v:shapetype id=\"_x0000_t136\" coordsize=\"21600,21600\" o:spt=\"136\"><v:path textpathok=\"t\"/>"
				+ "<v:textpath on=\"t\" fitshape=\"t\"/></v:shapetype>";
		for (int flags : BOTH) {
			org.w3c.dom.Document fo = fo(pkg(body(sptOnly), null), flags);
			assertEquals("flags " + flags, 0, paths(fo, "flow"));
			assertFalse("flags " + flags, pdfText(pkg(body(sptOnly), null), flags).contains("WordArt"));
		}
	}
}
