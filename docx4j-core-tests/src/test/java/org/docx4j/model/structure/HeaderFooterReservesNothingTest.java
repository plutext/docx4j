package org.docx4j.model.structure;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.Test;

/**
 * Word reserves nothing on the page for a header or footer with nothing in it: measured
 * on a document with <code>w:pgMar/@w:top=510</code> (25.5pt) and
 * <code>w:header=709</code> (35.45pt) whose header is a single empty <code>w:p</code>,
 * Word's body top is 25.5 - neither the empty paragraph's line box nor the header
 * distance moves it.
 *
 * @since 17.1.0
 */
public class HeaderFooterReservesNothingTest {

	private static final ObjectFactory factory = Context.getWmlObjectFactory();

	private static HeaderPart header(Object... content) throws Exception {
		HeaderPart part = new HeaderPart(new PartName("/word/header9.xml"));
		part.setJaxbElement(factory.createHdr());
		for (Object o : content) part.getJaxbElement().getContent().add(o);
		return part;
	}

	private static P paragraph(Object... runContent) {
		P p = factory.createP();
		if (runContent.length > 0) {
			R r = factory.createR();
			for (Object o : runContent) r.getContent().add(o);
			p.getContent().add(r);
		}
		return p;
	}

	private static Text text(String value) {
		Text t = factory.createText();
		t.setValue(value);
		return t;
	}

	@Test
	public void anEmptyParagraphReservesNothing() throws Exception {
		assertTrue(HeaderFooterPolicy.reservesNothing(header(paragraph())));
	}

	/** But several empty paragraphs are blank lines the author put there, and Word
	 *  reserves them: measured on a two-empty-paragraph header with w:pgMar w:top=1417
	 *  (70.85pt) and w:header=708 (35.4pt), Word's body top is about 90.6, not 70.85. */
	@Test
	public void severalEmptyParagraphsDoReserve() throws Exception {
		assertFalse(HeaderFooterPolicy.reservesNothing(
				header(paragraph(), paragraph(), paragraph())));
	}

	@Test
	public void whitespaceOnlyTextReservesNothing() throws Exception {
		assertTrue(HeaderFooterPolicy.reservesNothing(header(paragraph(text("   ")))));
	}

	@Test
	public void textReservesSpace() throws Exception {
		assertFalse(HeaderFooterPolicy.reservesNothing(header(paragraph(text("Page")))));
	}

	@Test
	public void aPictureReservesSpace() throws Exception {
		assertFalse(HeaderFooterPolicy.reservesNothing(
				header(paragraph(factory.createDrawing()))));
	}

	@Test
	public void aTableReservesSpace() throws Exception {
		assertFalse(HeaderFooterPolicy.reservesNothing(header(factory.createTbl())));
	}

	/** The mirror at the foot: an empty footer part does not shorten the body. */
	@Test
	public void anEmptyFooterPartReservesNothing() throws Exception {
		FooterPart part = new FooterPart(new PartName("/word/footer9.xml"));
		part.setJaxbElement(factory.createFtr());
		part.getJaxbElement().getContent().add(paragraph());
		assertTrue(HeaderFooterPolicy.reservesNothing(part));

		part.getJaxbElement().getContent().add(paragraph(text("2")));
		assertFalse(HeaderFooterPolicy.reservesNothing(part));
	}

	/** A part which is not there at all reserves nothing either. */
	@Test
	public void noPartReservesNothing() {
		assertTrue(HeaderFooterPolicy.reservesNothing(null));
	}
}
