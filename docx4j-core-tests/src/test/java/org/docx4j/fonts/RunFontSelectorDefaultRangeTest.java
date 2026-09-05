package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
 * across the letters.  Consecutive characters of such a script now share a span,
 * while a character of a listed range still starts a new one.
 *
 * @since 17.0.5
 */
public class RunFontSelectorDefaultRangeTest {

	private static final String GEORGIAN = "გამარჯობა";
	private static final String CYRILLIC = "да";
	private static final String DOC_FONT = "Times New Roman";

	@Test
	public void testGeorgianWordIsOneInline() throws Exception {
		Element[] spans = spans(GEORGIAN);
		assertEquals("a Georgian word should be a single fo:inline", 1, spans.length);
		assertEquals(GEORGIAN, spans[0].getTextContent());
	}

	@Test
	public void testGeorgianWordsShareASpanAcrossASpace() throws Exception {
		assertEquals(1, spans(GEORGIAN + " " + GEORGIAN).length);
	}

	@Test
	public void testListedRangeStillStartsANewInline() throws Exception {
		Element[] spans = spans(GEORGIAN + CYRILLIC);
		assertEquals("Cyrillic is a listed range and must not join the Georgian span",
				2, spans.length);
		assertEquals(GEORGIAN, spans[0].getTextContent());
		assertEquals(CYRILLIC, spans[1].getTextContent());
	}

	/** The gap must never overlap a range the dispatch lists. */
	@Test
	public void testDefaultRangeStaysInsideItsGap() {
		for (int i = 0; i <= 0xFFFF; i++) {
			char c = (char) i;
			char[] r = RunFontSelector.defaultRange(c);
			assertTrue("0x" + Integer.toHexString(i), r[0] <= c && c <= r[1]);
		}
		char[] georgian = RunFontSelector.defaultRange('ა');
		assertEquals('Ⴀ', georgian[0]);
		assertEquals('ჿ', georgian[1]);
		// a listed character gets only itself, so it can never widen a gap
		char[] listed = RunFontSelector.defaultRange('a');
		assertEquals('a', listed[0]);
		assertEquals('a', listed[1]);
	}

	private Element[] spans(String text) throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		Document document = (Document)XmlUtils.unmarshalString(documentXML(text));
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

	private String documentXML(String text) {
		return "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>"
				+ "<w:p><w:r><w:rPr>"
				+ "<w:rFonts w:ascii=\"" + DOC_FONT + "\" w:hAnsi=\"" + DOC_FONT + "\"/>"
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
