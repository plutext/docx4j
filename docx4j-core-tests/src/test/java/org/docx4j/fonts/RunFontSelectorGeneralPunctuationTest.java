package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
import org.junit.Assume;
import org.junit.Test;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * General Punctuation (U+2000-U+206F) - the curly quotes, the dashes, the ellipsis - is
 * ordinary text: it must stay in the run's own font, even where that font can't be
 * resolved or lacks the glyph.  Until 17.0.4 it was handled with the symbol blocks
 * (everything from U+2000 to U+2EFF took the path worked out for the U+2751 dingbat
 * checkbox), so a quotation mark the font lacked was set in a symbol font.
 *
 * The symbol blocks themselves (U+2190-U+2BFF) must keep that substitution behavior;
 * the dingbat and arrow cases here pin it.
 *
 * @since 17.0.4
 */
public class RunFontSelectorGeneralPunctuationTest {

	/** Not installed and not mapped, so every glyph check on it says "no glyph" -
	 *  the case which used to send punctuation hunting for a symbol font. */
	private static final String DOC_FONT = "Some Font Not On This System";

	private static final String FONT_WORD_2016_USES = "Segoe UI Symbol";

	private static final String PUNCTUATION = "“—…”"; // curly quotes, em dash, ellipsis
	private static final String DINGBAT = "❑";  // the checkbox Word substitutes for
	private static final String ARROW = "→";

	@Test
	public void testFonts() throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();

		// NB the physical fonts are discovered in the Mapper's static initialiser,
		// so ask for the mapper before looking in PhysicalFonts
		Mapper fontMapper = wordMLPackage.getFontMapper();

		PhysicalFont dejaVu = PhysicalFonts.get("DejaVu Sans");
		Assume.assumeTrue("DejaVu Sans isn't installed", dejaVu != null);
		Assume.assumeTrue("unexpectedly installed", PhysicalFonts.get(DOC_FONT) == null);
		for (char c : (PUNCTUATION + DINGBAT + ARROW).toCharArray()) {
			Assume.assumeTrue("DejaVu Sans has no 0x" + Integer.toHexString(c),
					GlyphCheck.hasChar(dejaVu, c));
		}

		/* Only the symbol substitute is mapped; DOC_FONT resolves to nothing,
		 * exactly as an embedded font (or Calibri on a bare Linux box) does. */
		fontMapper.getFontMappings().clear();
		fontMapper.put(FONT_WORD_2016_USES, dejaVu);

		String[] texts = { PUNCTUATION, DINGBAT, ARROW };
		Document document = (Document)XmlUtils.unmarshalString(documentXML(texts));
		wordMLPackage.getMainDocumentPart().setJaxbElement(document);

		for (int i=0; i<texts.length; i++) {

			// a fresh selector (and so a fresh visitor) per paragraph
			RunFontSelector rfs = createRunFontSelector(wordMLPackage);

			P p = (P)document.getContent().get(i);
			PPr pPr = p.getPPr();
			RPr rPr = ((R)p.getContent().get(0)).getRPr();
			Text wmlText = (Text)XmlUtils.unwrap(((R)p.getContent().get(0)).getContent().get(0));

			DocumentFragment df = (DocumentFragment)rfs.fontSelector(pPr, rPr, wmlText);
			Element foInline = (Element)df.getFirstChild();
			String fontFamily = foInline.getAttribute("font-family");

			if (texts[i].equals(PUNCTUATION)) {
				// no symbol substitution: since DOC_FONT resolves to nothing, we
				// get the fallback font, but never the symbol font
				assertNotEquals("punctuation was set in the symbol font",
						dejaVu.getName(), plain(fontFamily));
				assertEquals(rfs.fallbackFont, plain(fontFamily));
			} else {
				// the dingbat and the arrow still substitute, as Word does
				assertEquals(texts[i] + " should have used the symbol substitute",
						dejaVu.getName(), plain(fontFamily));
			}
		}
	}

	private String documentXML(String[] texts) {
		StringBuilder sb = new StringBuilder();
		sb.append("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>");
		for (String text : texts) {
			sb.append("<w:p><w:r><w:rPr>"
					+ "<w:rFonts w:ascii=\"" + DOC_FONT + "\" w:hAnsi=\"" + DOC_FONT + "\"/>"
					+ "</w:rPr><w:t>" + text + "</w:t></w:r></w:p>");
		}
		sb.append("</w:body></w:document>");
		return sb.toString();
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
					sb.append(
							new String(Character.toChars(cp)));
				}

				public void finishPrevious() {

			    	if (sb.length()>0) {
			    		if (span==null) { // init
			    			span = runFontSelector.createElement(document);
			    			// so that spaces have correct font set
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

	/** the font-family without the variant suffix (the kerned or no-ligature twin
	 *  FopConfigUtil declares); it is the same physical font */
	private static String plain(String fontFamily) {
		if (fontFamily==null) return null;
		if (fontFamily.endsWith(org.docx4j.fonts.RunFontSelector.NOLIGA_SUFFIX)) {
			return fontFamily.substring(0, fontFamily.length()
					- org.docx4j.fonts.RunFontSelector.NOLIGA_SUFFIX.length());
		}
		if (fontFamily.endsWith(org.docx4j.fonts.RunFontSelector.KERNED_SUFFIX)) {
			return fontFamily.substring(0, fontFamily.length()
					- org.docx4j.fonts.RunFontSelector.KERNED_SUFFIX.length());
		}
		return fontFamily;
	}
}
