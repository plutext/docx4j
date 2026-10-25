package org.docx4j;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.jaxb.Context;
import org.docx4j.list.ArrayListDocx4j;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ArrayListDocx4jTest {

	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Our list semantics are that contains, indexOf, remove
	 * etc should work for an object, irrespective
	 * of whether it is wrapped in a JAXBElement or not.
	 */
	@Test
	public void testContainsJAXBElement() {
		
		Body b = new Body();
		
		ArrayListDocx4j list = new ArrayListDocx4j(b);
		
		CTAltChunk altChunk = new CTAltChunk();
		
		JAXBElement<CTAltChunk> altChunkWrapper = Context.getWmlObjectFactory().createBodyAltChunk(altChunk);
		
		list.add(altChunkWrapper);
		
		Assert.assertTrue(list.contains(altChunk));
	}

	/**
	 * A hyperlink's content list refuses what its paragraph's does: a table,
	 * a paragraph, bare Text (each is a mistake Word would repair); a run is accepted.
	 */
	@Test
	public void hyperlinkRefusesWhatAParagraphRefuses() {

		P.Hyperlink h = new P.Hyperlink();

		assertRefused(h, new Tbl(), "table");
		assertRefused(h, new P(), "paragraph");
		assertRefused(h, new Text(), "Text");

		R r = new R();
		h.getContent().add(r);
		Assert.assertSame(h, r.getParent());
		Assert.assertEquals(1, h.getContent().size());
	}

	private static void assertRefused(P.Hyperlink h, Object o, String what) {
		try {
			h.getContent().add(o);
			Assert.fail("a " + what + " was added to a hyperlink");
		} catch (RuntimeException expected) {
			Assert.assertTrue(expected.getMessage(), expected.getMessage().contains("hyperlink"));
		}
		Assert.assertTrue(h.getContent().isEmpty());
	}

}
