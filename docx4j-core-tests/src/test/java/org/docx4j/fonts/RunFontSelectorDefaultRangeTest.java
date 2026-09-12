package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;

import org.docx4j.XmlUtils;
import org.docx4j.fonts.RunFontSelector.RunFontActionType;
import org.docx4j.fonts.RunFontSelector.RunFontCharacterVisitor;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;
import org.junit.Test;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * A script [MS-OI29500] 17.3.2.26 does not list - Georgian, Armenian, Ethiopic,
 * Tibetan, Greek Extended - takes the range dispatch's final else, which uses hAnsi
 * for every one of its characters.  Until 17.0.5 that branch reset the current range
 * to an empty one, so each character started a new fo:inline: a Georgian word came
 * out as one span per letter, which also stopped FOP kerning and letter-spacing
 * across the letters.  Consecutive characters of such a script share a span.
 *
 * <p>Since 17.1.1 (CR-016 phase 2) a span is cut where the chosen font changes or
 * where the script changes between two non-shared characters, not where a character's
 * range does: the Cyrillic beside a Georgian word is its own span though both are
 * hAnsi's, a run whose ascii and hAnsi fonts differ is cut between its Latin and its
 * Georgian, and Latin with its accents and punctuation is one span.</p>
 *
 * @since 17.0.5
 */
public class RunFontSelectorDefaultRangeTest {

	private static final String GEORGIAN = "გამარჯობა";
	private static final String CYRILLIC = "да";
	private static final String DOC_FONT = "Times New Roman";
	private static final String OTHER_FONT = "Arial";

	@Test
	public void testGeorgianWordIsOneInline() throws Exception {
		Element[] spans = spans(GEORGIAN);
		assertEquals("a Georgian word should be a single fo:inline", 1, spans.length);
		assertEquals(GEORGIAN, spans[0].getTextContent());
	}

	@Test
	public void testTheSpaceBetweenGeorgianWordsKeepsTheDocumentFont() throws Exception {
		// the dispatch gives the phrase one span (one font, one script; the space is
		// shared); where the document font lacks Georgian, the coverage pass then puts
		// each word in a covering face and leaves the space in the document font's own,
		// whose width Word uses (Sylfaen's space is 0.250em, measured on the corpus's
		// Georgian golden; the substitute's is 0.286) - so three spans, or one where the
		// document font has Georgian itself
		Element[] spans = spans(GEORGIAN + " " + GEORGIAN);
		if (spans.length == 1) {
			assertEquals(GEORGIAN + " " + GEORGIAN, spans[0].getTextContent());
		} else {
			assertEquals(3, spans.length);
			assertEquals(GEORGIAN, spans[0].getTextContent());
			assertEquals(" ", spans[1].getTextContent());
			assertEquals(GEORGIAN, spans[2].getTextContent());
			assertEquals(spans[0].getAttribute("font-family"), spans[2].getAttribute("font-family"));
		}
	}

	@Test
	public void testAScriptChangeStillCuts() throws Exception {
		// Georgian and Cyrillic share the hAnsi font but not a script: two spans, so that
		// the coverage pass substitutes each as one top-level span
		Element[] spans = spans(GEORGIAN + CYRILLIC);
		assertEquals(2, spans.length);
		assertEquals(GEORGIAN, spans[0].getTextContent());
		assertEquals(CYRILLIC, spans[1].getTextContent());
	}

	@Test
	public void testDifferentFontsStillCut() throws Exception {
		// ascii and hAnsi name different fonts: Latin (ascii) and Georgian (hAnsi) are two spans
		Element[] spans = spans("abc " + GEORGIAN, DOC_FONT, OTHER_FONT);
		assertEquals(2, spans.length);
		assertEquals("abc ", spans[0].getTextContent());
		assertEquals(GEORGIAN, spans[1].getTextContent());
	}

	private Element[] spans(String text) throws Exception {
		return spans(text, DOC_FONT, DOC_FONT);
	}

	private Element[] spans(String text, String ascii, String hAnsi) throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		Document document = (Document)XmlUtils.unmarshalString(documentXML(text, ascii, hAnsi));
		wordMLPackage.getMainDocumentPart().setJaxbElement(document);

		RunFontSelector rfs = createRunFontSelector(wordMLPackage);
		P p = (P)document.getContent().get(0);
		PPr pPr = p.getPPr();
		RPr rPr = ((R)p.getContent().get(0)).getRPr();
		Text wmlText = (Text)XmlUtils.unwrap(((R)p.getContent().get(0)).getContent().get(0));

		DocumentFragment df = (DocumentFragment)rfs.fontSelector(pPr, rPr, wmlText);
		java.util.List<Element> result = new java.util.ArrayList<Element>();
		for (org.w3c.dom.Node n = df.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element) result.add((Element)n);
		}
		return result.toArray(new Element[0]);
	}

	private String documentXML(String text, String ascii, String hAnsi) {
		return "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>"
				+ "<w:p><w:r><w:rPr>"
				+ "<w:rFonts w:ascii=\"" + ascii + "\" w:hAnsi=\"" + hAnsi + "\"/>"
				+ "</w:rPr><w:t xml:space=\"preserve\">" + text + "</w:t></w:r></w:p>"
				+ "</w:body></w:document>";
	}

	// copied from FOConversionContext, as in the other RunFontSelector tests
	private static RunFontSelector createRunFontSelector(WordprocessingMLPackage wmlPackage) {

		return new RunFontSelector(wmlPackage,

			new RunFontCharacterVisitor() {

	    		DocumentFragment df;
				StringBuilder sb = new StringBuilder(1024);
				Element span;

				String lastFont;
				String fallbackFontName;

				private org.w3c.dom.Document document;
				@Override
				public void setDocument(org.w3c.dom.Document document) {
					this.document = document;
					 df = document.createDocumentFragment();
				}

				private boolean spanReusable = true;
				public boolean isReusable() {
					return spanReusable;
				}

				public void addCharacterToCurrent(char c) {
			    	sb.append(c);
				}

				@Override
				public void addCodePointToCurrent(int cp) {
					sb.append(new String(Character.toChars(cp)));
				}

				public void finishPrevious() {

			    	if (sb.length()>0) {
			    		if (span==null) { // init
			    			span = runFontSelector.createElement(document);
			    			if (lastFont!=null) {
								runFontSelector.setAttribute(span, lastFont);
			    			}
			    		}
				    	df.appendChild(span);
				    	span.setTextContent(sb.toString());
				    	sb.setLength(0);
			    	}
				}

				public void createNew() {
					span = runFontSelector.createElement(document);
				}

				public void setMustCreateNewFlag(boolean val) {
					spanReusable = !val;
				}

				public void fontAction(String fontname) {

					if (fontname==null) {
						runFontSelector.setAttribute(span, fallbackFontName);
					} else {
						runFontSelector.setAttribute(span, fontname);
						lastFont = fontname;
					}
				}

				@Override
				public Object getResult() {
					span=null; // ready for next time
					return df;
				}

				private RunFontSelector runFontSelector;
				@Override
				public void setRunFontSelector(RunFontSelector runFontSelector) {
					this.runFontSelector = runFontSelector;
				}

				@Override
				public void setFallbackFont(String fontname) {
					fallbackFontName = fontname;
				}

			}, RunFontActionType.XSL_FO);
	}
}
