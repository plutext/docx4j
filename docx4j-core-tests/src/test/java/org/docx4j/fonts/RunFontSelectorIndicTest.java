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
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Issues 622 and 666: characters in the Indic script ranges (U+0900 to U+0DFF)
 * should be formatted with the cs font (where one is available), and
 * consecutive characters should share a single span (FOP can't form conjuncts
 * or reorder vowel signs across span boundaries).
 *
 * See RunFontSelectorKhmerTest for the Khmer/Thai/Lao/Myanmar equivalents.
 */
public class RunFontSelectorIndicTest {

	protected static Logger log = LoggerFactory.getLogger(RunFontSelector.class); // same logger

	static String[] expectedFonts = {
			"Mangal",         // Hindi (Devanagari), from cs
			"Gautami",        // Telugu, from cs
			"Latha",          // Tamil, from cs
			"Vrinda",         // Bengali, from cs
			"Arial"           // Devanagari, no usable cs (docDefaults w:cs=""), so hAnsi
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

			/* Every stretch of Indic characters is one span in the cs font (FOP needs the
			 * conjuncts and vowel signs together); the spaces between the words are ASCII
			 * and take the ascii font, in a span of their own, as [MS-OI29500] 17.3.2.26
			 * says and Word does (CR-016 phase 2; until 17.1.1 a space joined the span
			 * before it, so the whole text was one span). */
			String text = ((Text)XmlUtils.unwrap(((R)p.getContent().get(0)).getContent().get(0))).getValue();
			int words = text.trim().split("\\s+").length;
			int indicSpans = 0;
			for (int k=0; k<vis.spanTexts.size(); k++) {
				String spanText = vis.spanTexts.get(k);
				boolean indic = false;
				for (int c=0; c<spanText.length(); c++) if (spanText.charAt(c) > 0x7F) indic = true;
				if (indic) {
					indicSpans++;
					assertEquals("paragraph " + i + " span " + k + " '" + spanText + "'", expectedFonts[i], vis.fontsUsed.get(k));
				}
			}
			assertEquals("paragraph " + i + ": one span per Indic word", words, indicSpans);
		}
	}

	String documentXML = "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
            + "<w:body>"

                        // Hindi (Devanagari): conjuncts and i-matra
                        + "<w:p>"
                            + "<w:pPr><w:pStyle w:val=\"Normal\"/></w:pPr>"
                            + "<w:r>"
                                + "<w:rPr><w:rFonts w:cs=\"Mangal\"/></w:rPr>"
                                + "<w:t>हिन्दी की स्थिति</w:t>"
                            +"</w:r>"
                        +"</w:p>"

                        // Telugu
                        + "<w:p>"
                            + "<w:pPr><w:pStyle w:val=\"Normal\"/></w:pPr>"
                            + "<w:r>"
                                + "<w:rPr><w:rFonts w:cs=\"Gautami\"/></w:rPr>"
                                + "<w:t>తెలుగు భాషలో వ్రాసిన</w:t>"
                            +"</w:r>"
                        +"</w:p>"

                        // Tamil
                        + "<w:p>"
                            + "<w:pPr><w:pStyle w:val=\"Normal\"/></w:pPr>"
                            + "<w:r>"
                                + "<w:rPr><w:rFonts w:cs=\"Latha\"/></w:rPr>"
                                + "<w:t>தமிழ் மொழி</w:t>"
                            +"</w:r>"
                        +"</w:p>"

                        // Bengali
                        + "<w:p>"
                            + "<w:pPr><w:pStyle w:val=\"Normal\"/></w:pPr>"
                            + "<w:r>"
                                + "<w:rPr><w:rFonts w:cs=\"Vrinda\"/></w:rPr>"
                                + "<w:t>বাংলা ভাষা</w:t>"
                            +"</w:r>"
                        +"</w:p>"

                        // Devanagari, no cs on the run, and docDefaults cs is empty: hAnsi
                        + "<w:p>"
                            + "<w:pPr><w:pStyle w:val=\"Normal\"/></w:pPr>"
                            + "<w:r>"
                                + "<w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\"/></w:rPr>"
                                + "<w:t>हिन्दी</w:t>"
                            +"</w:r>"
                        +"</w:p>"

        +"</w:body>"
    +"</w:document>";

	// docDefaults as written by LibreOffice (note w:cs="");
	// no theme part, so the theme attributes are ignored
	static String stylesXML = "<w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"

            + "<w:docDefaults>"
			   + "<w:rPrDefault>"
			            + "<w:rPr>"
			                + "<w:rFonts w:ascii=\"Aptos\" w:hAnsi=\"Aptos\" w:eastAsia=\"\" w:cs=\"\" w:asciiTheme=\"minorHAnsi\" w:cstheme=\"minorBidi\" w:eastAsiaTheme=\"minorEastAsia\" w:hAnsiTheme=\"minorHAnsi\"/>"
			                + "<w:sz w:val=\"24\"/>"
			                + "<w:szCs w:val=\"24\"/>"
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

		List<String> fontsUsed = new ArrayList<String>();   // one per span, in order
		List<String> spanTexts = new ArrayList<String>();   // one per span, in order
		int spansCreated = 0;
		private StringBuilder current;

		void reset() {
			fontsUsed.clear();
			spanTexts.clear();
			spansCreated = 0;
			current = null;
		}

		private org.w3c.dom.Document document;
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
		public void addCharacterToCurrent(char c) {
			if (current!=null) current.append(c);
		}

		@Override
		public void addCodePointToCurrent(int cp) {
			if (current!=null) current.appendCodePoint(cp);
		}

		@Override
		public void finishPrevious() {
			if (current!=null) {
				spanTexts.add(current.toString());
				current = null;
			}
		}

		@Override
		public void createNew() {
			spansCreated++;
			current = new StringBuilder();
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
		sdp.setJaxbElement((org.docx4j.wml.Styles)XmlUtils.unmarshalString(stylesXML));

		mdp.setJaxbElement(document);

		currentVisitor = new FontRecordingVisitor();
		return new RunFontSelector(wordMLPackage, currentVisitor, RunFontActionType.DISCOVERY);
	}
}
