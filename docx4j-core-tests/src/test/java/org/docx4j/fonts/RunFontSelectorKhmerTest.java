package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.fonts.RunFontSelector.RunFontActionType;
import org.docx4j.fonts.RunFontSelector.RunFontCharacterVisitor;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Text;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Issue 666: characters in complex script ranges (Khmer, Thai, Lao, Myanmar)
 * should be formatted with the cs font (where one is available), and
 * consecutive characters in such a range should share a single span
 * (FOP can't shape across span boundaries).
 *
 * The rFonts markup here is as written by LibreOffice: the cs font is
 * specified via w:cs alone (no w:cs toggle property in the rPr),
 * and docDefaults contain w:cs="".
 */
public class RunFontSelectorKhmerTest {

	protected static Logger log = LoggerFactory.getLogger(RunFontSelector.class); // same logger

	static String[] expectedFonts = {
			"Khmer OS Muol",  // Khmer, from cs
			"Angsana New",    // Thai, from cs
			"DokChampa",      // Lao, from cs
			"Myanmar Text",   // Myanmar, from cs
			"Arial"           // Khmer, no usable cs (docDefaults w:cs=""), so hAnsi
	};

	@Test
	public  void testCsFontUsed() throws Exception {

		Document document = (Document)XmlUtils.unmarshalString(documentXML);
		RunFontSelector rfs = createRunFontSelector(document);

		for (int i=0; i<expectedFonts.length; i++) {

			P p = (P)document.getContent().get(i);

			FontRecordingVisitor vis = currentVisitor;
			vis.reset();

			rfs.fontSelector(p.getPPr(),
					((R)p.getContent().get(0)).getRPr(),
					(Text)XmlUtils.unwrap(((R)p.getContent().get(0)).getContent().get(0)));

			assertEquals("paragraph " + i, 1, vis.spansCreated);
			for (String font : vis.fontsUsed) {
				assertEquals("paragraph " + i, expectedFonts[i], font);
			}
		}
	}

	@Test
	public  void testMixedScriptRun() throws Exception {

		Document document = (Document)XmlUtils.unmarshalString(documentXML);
		RunFontSelector rfs = createRunFontSelector(document);

		// Last paragraph: "Hello លិខិត" in a single run; ascii/hAnsi Times New Roman, cs Khmer OS Content
		P p = (P)document.getContent().get(5);

		FontRecordingVisitor vis = currentVisitor;
		vis.reset();

		rfs.fontSelector(p.getPPr(),
				((R)p.getContent().get(0)).getRPr(),
				(Text)XmlUtils.unwrap(((R)p.getContent().get(0)).getContent().get(0)));

		assertEquals(2, vis.spansCreated);
		assertEquals("Times New Roman", vis.fontsUsed.get(0));
		assertEquals("Khmer OS Content", vis.fontsUsed.get(vis.fontsUsed.size()-1));
	}

	String documentXML = "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
            + "<w:body>"

                        // Khmer, cs font only (as LibreOffice writes it)
                        + "<w:p>"
                            + "<w:pPr><w:pStyle w:val=\"Normal\"/></w:pPr>"
                            + "<w:r>"
                                + "<w:rPr><w:rFonts w:cs=\"Khmer OS Muol\"/></w:rPr>"
                                + "<w:t>លិខិតទទួលយកដោយមានលក្ខខណ្ឌ</w:t>"
                            +"</w:r>"
                        +"</w:p>"

                        // Thai
                        + "<w:p>"
                            + "<w:pPr><w:pStyle w:val=\"Normal\"/></w:pPr>"
                            + "<w:r>"
                                + "<w:rPr><w:rFonts w:cs=\"Angsana New\"/></w:rPr>"
                                + "<w:t>สวัสดีชาวโลก</w:t>"
                            +"</w:r>"
                        +"</w:p>"

                        // Lao
                        + "<w:p>"
                            + "<w:pPr><w:pStyle w:val=\"Normal\"/></w:pPr>"
                            + "<w:r>"
                                + "<w:rPr><w:rFonts w:cs=\"DokChampa\"/></w:rPr>"
                                + "<w:t>ສະບາຍດີຊາວໂລກ</w:t>"
                            +"</w:r>"
                        +"</w:p>"

                        // Myanmar
                        + "<w:p>"
                            + "<w:pPr><w:pStyle w:val=\"Normal\"/></w:pPr>"
                            + "<w:r>"
                                + "<w:rPr><w:rFonts w:cs=\"Myanmar Text\"/></w:rPr>"
                                + "<w:t>မင်္ဂလာပါကမ္ဘာလောက</w:t>"
                            +"</w:r>"
                        +"</w:p>"

                        // Khmer, no cs on the run, and docDefaults cs is empty: hAnsi
                        + "<w:p>"
                            + "<w:pPr><w:pStyle w:val=\"Normal\"/></w:pPr>"
                            + "<w:r>"
                                + "<w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\"/></w:rPr>"
                                + "<w:t>លិខិតទទួលយកដោយមានលក្ខខណ្ឌ</w:t>"
                            +"</w:r>"
                        +"</w:p>"

                        // Latin + Khmer in one run
                        + "<w:p>"
                            + "<w:pPr><w:pStyle w:val=\"Normal\"/></w:pPr>"
                            + "<w:r>"
                                + "<w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\" w:cs=\"Khmer OS Content\"/></w:rPr>"
                                + "<w:t>Hello លិខិត</w:t>"
                            +"</w:r>"
                        +"</w:p>"

        +"</w:body>"
    +"</w:document>";

	// docDefaults as written by LibreOffice for the issue 666 document
	// (note w:cs=""); no theme part, so the theme attributes are ignored
	static String stylesXML = "<w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"

            + "<w:docDefaults>"
			   + "<w:rPrDefault>"
			            + "<w:rPr>"
			                + "<w:rFonts w:ascii=\"Aptos\" w:hAnsi=\"Aptos\" w:eastAsia=\"\" w:cs=\"\" w:asciiTheme=\"minorHAnsi\" w:cstheme=\"minorBidi\" w:eastAsiaTheme=\"minorEastAsia\" w:hAnsiTheme=\"minorHAnsi\"/>"
			                + "<w:sz w:val=\"24\"/>"
			                + "<w:szCs w:val=\"24\"/>"
			                + "<w:lang w:bidi=\"ar-SA\" w:eastAsia=\"ja-JP\" w:val=\"en-US\"/>"
			            +"</w:rPr>"
			        +"</w:rPrDefault>"
			        + "<w:pPrDefault/>"
			    +"</w:docDefaults>"

              + "<w:style w:default=\"1\" w:styleId=\"Normal\" w:type=\"paragraph\">"
                  + "<w:name w:val=\"Normal\"/>"
                  + "<w:qFormat/>"
            +"</w:style>"

            + "<w:style w:default=\"1\" w:styleId=\"DefaultParagraphFont\" w:type=\"character\">"
                  + "<w:name w:val=\"Default Paragraph Font\"/>"
                  + "<w:semiHidden/>"
                  + "<w:unhideWhenUsed/>"
            +"</w:style>"

      +"</w:styles>";


	static class FontRecordingVisitor implements RunFontCharacterVisitor {

		List<String> fontsUsed = new ArrayList<String>();
		int spansCreated = 0;

		void reset() {
			fontsUsed.clear();
			spansCreated = 0;
		}

		private org.w3c.dom.Document document;
		private org.w3c.dom.Element span;
		private RunFontSelector runFontSelector;

		@Override
		public void setDocument(org.w3c.dom.Document document) {
			this.document = document;
		}

		@Override
		public boolean isReusable() {
			return true;
		}

		@Override
		public void addCharacterToCurrent(char c) {}

		@Override
		public void addCodePointToCurrent(int cp) {}

		@Override
		public void finishPrevious() {}

		@Override
		public void createNew() {
			spansCreated++;
			span = runFontSelector.createElement(document);
		}

		@Override
		public void setMustCreateNewFlag(boolean val) {}

		@Override
		public void fontAction(String fontname) {
			fontsUsed.add(fontname);
		}

		@Override
		public Object getResult() {
			return null;
		}

		@Override
		public void setRunFontSelector(RunFontSelector runFontSelector) {
			this.runFontSelector = runFontSelector;
		}

		@Override
		public void setFallbackFont(String fontname) {}
	}

	private FontRecordingVisitor currentVisitor;

	private RunFontSelector createRunFontSelector(Document document) throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

		StyleDefinitionsPart sdp = mdp.getStyleDefinitionsPart();
		Styles styles = (Styles)XmlUtils.unmarshalString(stylesXML);
		sdp.setJaxbElement(styles);

		mdp.setJaxbElement(document);

		currentVisitor = new FontRecordingVisitor();
		return new RunFontSelector(wordMLPackage, currentVisitor, RunFontActionType.DISCOVERY);
	}
}
