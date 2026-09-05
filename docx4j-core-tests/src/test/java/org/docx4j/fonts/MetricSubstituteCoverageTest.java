package org.docx4j.fonts;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.fonts.RunFontSelector.RunFontActionType;
import org.docx4j.fonts.RunFontSelector.RunFontCharacterVisitor;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.Assume;
import org.junit.Test;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A metric-compatible substitute which cannot draw the run (CR-001 &#xa7;5.1).
 *
 * <p>The clone is chosen for its advance widths, and several of them carry the Latin
 * alphabet alone: Caladea, which stands in for Cambria, has neither Greek nor Cyrillic
 * (fc-query: {@code 20-7e a0-161 164-17f 192 1fa-1ff 218-21b 237 2c6-2c7 ...}).  Until
 * 17.0.6 the glyph-aware per-script pass skipped Greek and Cyrillic altogether, on the
 * assumption that any conventional stand-in covers them, so step 1's substitute was never
 * questioned.  Measured on a 68-page Greek document set in Cambria: 48% of the glyphs
 * docx4j painted were notdef, and its line parity was 0.0719, the worst in a
 * 103-document corpus - while Carlito, already loaded for the same document's Calibri,
 * covers both scripts.</p>
 *
 * @since 17.0.6
 */
public class MetricSubstituteCoverageTest {

	private static final String DOC_FONT = "Cambria";
	private static final String GREEK = "Ανοικτό";      // Anoikto
	private static final String CYRILLIC = "Сведения"; // Svedeniya

	@Test
	public void greekSetInCambriaGetsAFontWhichHasIt() throws Exception {
		aScriptTheCloneLacksIsRendered(GREEK);
	}

	@Test
	public void cyrillicSetInCambriaGetsAFontWhichHasIt() throws Exception {
		aScriptTheCloneLacksIsRendered(CYRILLIC);
	}

	private void aScriptTheCloneLacksIsRendered(String text) throws Exception {

		WordprocessingMLPackage pkg = pkg(text);
		Assume.assumeTrue(DOC_FONT + " is unexpectedly installed", PhysicalFonts.get(DOC_FONT)==null);

		int[] cps = text.codePoints().toArray();
		PhysicalFont mapped = pkg.getFontMapper().get(DOC_FONT);
		Assume.assumeNotNull(mapped);
		Assume.assumeTrue("the substitute for " + DOC_FONT + " (" + mapped.getName()
				+ ") covers this script here, so there is nothing to fall back from",
				!FontFallback.covers(mapped, cps));
		Assume.assumeTrue("no installed font can render it either",
				FontFallback.selectCovering(DOC_FONT, cps)!=null);

		String family = fontFamilyOf(convert(createRunFontSelector(pkg), pkg));
		PhysicalFont chosen = PhysicalFonts.get(family);
		assertNotNull("the chosen font " + family + " is not a physical font", chosen);
		assertTrue(family + " cannot draw '" + text + "'", FontFallback.covers(chosen, cps));
	}

	/** Latin set in Cambria still goes to the metric clone: the widths are why it is
	 *  chosen, and the coverage check must not disturb that. */
	@Test
	public void latinSetInCambriaKeepsTheMetricClone() throws Exception {

		WordprocessingMLPackage pkg = pkg("Hello");
		Assume.assumeTrue(DOC_FONT + " is unexpectedly installed", PhysicalFonts.get(DOC_FONT)==null);
		PhysicalFont mapped = pkg.getFontMapper().get(DOC_FONT);
		Assume.assumeNotNull(mapped);

		String family = fontFamilyOf(convert(createRunFontSelector(pkg), pkg));
		assertTrue("expected " + mapped.getName() + ", got " + family,
				family.startsWith(mapped.getName()));
	}

	// ---- helpers (as in RunFontSelectorGlyphFallbackTest)

	private String fontFamilyOf(DocumentFragment df) {
		Element el = (Element)df.getFirstChild();
		String family = el.getAttribute("font-family");
		for (Node child = el.getFirstChild(); child!=null && family.length()==0; child = child.getNextSibling()) {
			if (child instanceof Element) family = ((Element)child).getAttribute("font-family");
		}
		return family;
	}

	private DocumentFragment convert(RunFontSelector rfs, WordprocessingMLPackage pkg) throws Exception {
		Document document = (Document)pkg.getMainDocumentPart().getJaxbElement();
		P p = (P)document.getBody().getContent().get(0);
		R r = (R)p.getContent().get(0);
		return (DocumentFragment)rfs.fontSelector(p.getPPr(), r.getRPr(),
				(Text)XmlUtils.unwrap(r.getContent().get(0)));
	}

	private WordprocessingMLPackage pkg(String text) throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getFontMapper(); // discovery, and the metrically-compatible substitutes
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>"
				+ "<w:p><w:r><w:rPr>"
				+ "<w:rFonts w:ascii=\"" + DOC_FONT + "\" w:hAnsi=\"" + DOC_FONT + "\"/>"
				+ "<w:sz w:val=\"22\"/>"
				+ "</w:rPr><w:t>" + text + "</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));
		return pkg;
	}

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
