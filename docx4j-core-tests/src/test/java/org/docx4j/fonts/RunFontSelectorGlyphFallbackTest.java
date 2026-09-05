package org.docx4j.fonts;

import static org.junit.Assert.assertNotEquals;
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
 * The last-resort fallback is glyph-aware.
 *
 * <p>A font the box hasn't got used to fall back to whatever the document's default
 * font mapped to, chosen without reference to what the run contains: a document
 * setting Georgian in Sylfaen rendered as a row of notdef boxes even on a box with
 * Noto Sans Georgian and DejaVu Sans installed (CR-001 cause C3).  The font is now
 * chosen per script, from those which can render it.</p>
 *
 * @since 17.0.5
 */
public class RunFontSelectorGlyphFallbackTest {

	/** Georgian, in a font this box hasn't got */
	private static final String DOC_FONT = "Sylfaen";
	private static final String GEORGIAN = "გამარჯობა";
	private static final String LATIN_AND_GEORGIAN = "Hello გამარჯობა";

	@Test
	public void georgianGetsAFontWithGeorgianGlyphs() throws Exception {

		WordprocessingMLPackage pkg = pkg(GEORGIAN);
		Assume.assumeTrue(DOC_FONT + " is unexpectedly installed", PhysicalFonts.get(DOC_FONT)==null);
		assumeSomethingHasGeorgian();

		RunFontSelector rfs = createRunFontSelector(pkg);
		String fontFamily = fontFamilyOf(convert(rfs, pkg));

		PhysicalFont chosen = PhysicalFonts.get(fontFamily);
		assertNotNull("the chosen font " + fontFamily + " is not a physical font", chosen);
		assertTrue(fontFamily + " has no Georgian glyphs",
				FontFallback.covers(chosen, GEORGIAN.codePoints().toArray()));
		assertNotEquals("still the document default's font", rfs.fallbackFont, fontFamily);
	}

	/** Latin and Georgian in one run: each stretch in a font which can render it. */
	@Test
	public void mixedScriptRunKeepsBothReadable() throws Exception {

		WordprocessingMLPackage pkg = pkg(LATIN_AND_GEORGIAN);
		Assume.assumeTrue(DOC_FONT + " is unexpectedly installed", PhysicalFonts.get(DOC_FONT)==null);
		assumeSomethingHasGeorgian();

		RunFontSelector rfs = createRunFontSelector(pkg);
		DocumentFragment df = convert(rfs, pkg);

		// every stretch of text must be in a font which has its glyphs
		assertAllRenderable(df, null);
	}

	/**
	 * The document font's own class is preferred, so the substitute is not just the
	 * first font on the system with the glyphs.  Sylfaen is a serif.
	 */
	@Test
	public void theSubstituteIsOfTheDocumentFontsClass() throws Exception {

		WordprocessingMLPackage pkg = pkg(GEORGIAN);
		Assume.assumeTrue(DOC_FONT + " is unexpectedly installed", PhysicalFonts.get(DOC_FONT)==null);
		Assume.assumeTrue("no serif face with Georgian installed", serifWithGeorgian()!=null);

		String fontFamily = fontFamilyOf(convert(createRunFontSelector(pkg), pkg));
		assertNotEquals("expected a serif, given Sylfaen is one",
				FontFallback.FontClass.SANS, FontFallback.classOf(fontFamily));
	}

	// ---- helpers

	private PhysicalFont serifWithGeorgian() {
		int[] cps = GEORGIAN.codePoints().toArray();
		for (PhysicalFont pf : PhysicalFonts.getPhysicalFonts().values()) {
			if (FontFallback.classOf(pf.getName())==FontFallback.FontClass.SERIF
					&& FontFallback.covers(pf, cps)) return pf;
		}
		return null;
	}

	private void assumeSomethingHasGeorgian() {
		Assume.assumeTrue("no font on this box has Georgian glyphs",
				FontFallback.selectCovering(DOC_FONT, GEORGIAN.codePoints().toArray())!=null);
	}

	/** every text node must be in a font which has glyphs for it */
	private void assertAllRenderable(Node node, String inheritedFont) throws Exception {

		String font = inheritedFont;
		if (node instanceof Element && ((Element)node).getAttribute("font-family").length()>0) {
			font = ((Element)node).getAttribute("font-family");
		}
		if (node instanceof org.w3c.dom.Text) {
			String text = node.getNodeValue();
			if (text!=null && text.trim().length()>0) {
				assertNotNull("no font for '" + text + "'", font);
				PhysicalFont pf = PhysicalFonts.get(font);
				assertNotNull(font + " is not a physical font", pf);
				assertTrue(font + " can't render '" + text + "'",
						FontFallback.covers(pf, text.codePoints().toArray()));
			}
		}
		for (Node child = node.getFirstChild(); child!=null; child = child.getNextSibling()) {
			assertAllRenderable(child, font);
		}
	}

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
		// the physical fonts are discovered in the Mapper's static initialiser
		pkg.getFontMapper();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>"
				+ "<w:p><w:r><w:rPr>"
				+ "<w:rFonts w:ascii=\"" + DOC_FONT + "\" w:hAnsi=\"" + DOC_FONT + "\"/>"
				+ "<w:sz w:val=\"22\"/>"
				+ "</w:rPr><w:t>" + text + "</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));
		return pkg;
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
