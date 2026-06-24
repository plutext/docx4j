package org.docx4j;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.jaxb.Context;
import org.docx4j.list.ArrayListDocx4j;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTAltChunk;
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

}
