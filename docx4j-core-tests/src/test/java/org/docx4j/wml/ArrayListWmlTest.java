package org.docx4j.wml;

import static org.junit.Assert.*;

import javax.xml.bind.JAXBElement;

import org.docx4j.jaxb.Context;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ArrayListWmlTest {

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
		
		ArrayListWml list = new ArrayListWml(b);
		
		CTAltChunk altChunk = new CTAltChunk();
		
		JAXBElement<CTAltChunk> altChunkWrapper = Context.getWmlObjectFactory().createBodyAltChunk(altChunk);
		
		list.add(altChunkWrapper);
		
		Assert.assertTrue(list.contains(altChunk));
	}

}
