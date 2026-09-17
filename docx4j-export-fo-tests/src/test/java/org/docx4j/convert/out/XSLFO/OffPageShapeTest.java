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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.FooterReference;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.SectPr;
import org.junit.Test;

/**
 * A positioned shape whose whole box lies off the paper is not painted at all, which is
 * what Word does (CR-001 batch 47 item 2).
 *
 * <p>Measured on the {@code footer-offpage-shape} golden: a footer holding three bare
 * {@code w:pict}/{@code v:rect} shapes with {@code mso-position-vertical-relative:text} at
 * {@code margin-top} 900pt (past the A4 page's whole 841.92pt height), 719.35pt (past its
 * foot) and -300pt (on the page).  Word's PDF carries not one glyph of the first two, on any
 * page, and draws the third on every page at y = 493.96..505.12 with the footer's own line at
 * 755.28..766.52.  docx4j painted all three - the two off-page shapes 657.1pt and 824.3pt
 * below the page bottom, six text lines no page of Word's has - and the probe went from
 * 137 extracted lines to Word's 131 when they stopped being painted.</p>
 *
 * <p>The offset is the paragraph's, and the paragraph is in the footer, so the shape's top is
 * at least the body's bottom edge plus that offset: a footer shape is off the page once its
 * offset exceeds the bottom margin.  A shape only <em>partly</em> off is painted whole, as
 * Word clips it and so does every PDF viewer.</p>
 */
public class OffPageShapeTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";
	private static final ObjectFactory factory = Context.getWmlObjectFactory();

	/** A4, 72pt margins: a shape in the footer is off the page once margin-top exceeds 72pt. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\""
			+ " w:header=\"709\" w:footer=\"709\"/></w:sectPr>";

	/** The probe's own shape: a bare w:pict/v:rect with one line of text in it. */
	private static String rect(int id, String marginTop, String text) {
		return "<w:p><w:r>"
				+ "<w:pict xmlns:v=\"urn:schemas-microsoft-com:vml\""
				+ " xmlns:o=\"urn:schemas-microsoft-com:office:office\">"
				+ "<v:rect id=\"Rectangle " + id + "\" o:spid=\"_x0000_s" + (1025 + id) + "\""
				+ " style=\"position:absolute;margin-left:0;margin-top:" + marginTop + ";"
				+ "width:300pt;height:40pt;z-index:" + id + ";"
				+ "mso-position-vertical-relative:text\" filled=\"f\" stroked=\"t\">"
				+ "<v:textbox inset=\"0,0,0,0\"><w:txbxContent>"
				+ "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>"
				+ "</w:txbxContent></v:textbox></v:rect></w:pict>"
				+ "</w:r></w:p>";
	}

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>first line of the body</w:t></w:r></w:p>"
				+ SECT_PR + "</w:body></w:document>"));

		FooterPart fp = new FooterPart(new PartName("/word/footer1.xml"));
		fp.setPackage(pkg);
		fp.setJaxbElement((Ftr) XmlUtils.unmarshalString("<w:ftr " + W + ">"
				+ "<w:p><w:r><w:t>FOOTERLINE</w:t></w:r></w:p>"
				+ rect(1, "900pt", "PASTTHEPAPER")
				+ rect(2, "719.35pt", "PASTTHEFOOT")
				+ rect(3, "-300pt", "ONTHEPAGE")
				+ "</w:ftr>", Context.jc, Ftr.class));
		Relationship fr = pkg.getMainDocumentPart().addTargetPart(fp);

		SectPr sectPr = pkg.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
		FooterReference footerReference = factory.createFooterReference();
		footerReference.setId(fr.getId());
		footerReference.setType(HdrFtrRef.DEFAULT);
		sectPr.getEGHdrFtrReferences().add(footerReference);
		return pkg;
	}

	private static String fo(int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return new String(baos.toByteArray(), "UTF-8");
	}

	private void offPageShapesAreNotPainted(int flags) throws Exception {
		String fo = fo(flags);
		assertTrue("the footer's own line is there", fo.contains("FOOTERLINE"));
		assertTrue("and so is the shape which is on the page", fo.contains("ONTHEPAGE"));
		assertFalse("a shape 900pt below its footer paragraph is off the paper",
				fo.contains("PASTTHEPAPER"));
		assertFalse("and so is one 719.35pt below it, the corpus's own value",
				fo.contains("PASTTHEFOOT"));
	}

	@Test
	public void offPageShapesAreNotPaintedVisitor() throws Exception {
		offPageShapesAreNotPainted(Docx4J.FLAG_NONE);
	}

	@Test
	public void offPageShapesAreNotPaintedXslt() throws Exception {
		offPageShapesAreNotPainted(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
