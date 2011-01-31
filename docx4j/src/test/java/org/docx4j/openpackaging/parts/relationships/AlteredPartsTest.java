package org.docx4j.openpackaging.parts.relationships;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AlteredPartsTest {

	protected static Logger log = Logger.getLogger(AlteredPartsTest.class);
	
	//private static String resourceDir = System.getProperty("user.dir") + "/src/test/resources/AlteredParts/";
	private static String resourceDir = System.getProperty("user.dir") + "/sample-docs/";
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void initJaxbContext() throws Exception {
		
		// Do this first, so it is not included
		// in timing of first test.
		Context.getWmlObjectFactory(); 
	}
	
	@Test
	public void testSimpleDocx() throws Exception {
		
		log.warn("\n\n");
		
		String inputfilepath = resourceDir + "sample-docx.xml";
		
		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		List<Part> alteredParts = AlteredParts.start(thisPackage, otherPackage);
		
		assertTrue( alteredParts.size()==0 );
	}

	@Test
	public void testDifferent() throws Exception {

		log.warn("\n\n");
		
		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "sample-docx.xml"));
		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "Images.docx"));
		
		List<Part> alteredParts = AlteredParts.start(thisPackage, otherPackage);

		for (Part p : alteredParts) {
			System.out.println(p.partName.getName() );
		}
				
		assertTrue( true );
	}
	
	/* Test cases:
	 * 
	 * - identical docx
	 * - identical, except extra para
	 * - one has hyperlink (external part)
	 * - different images
	 * - header with different image
	 * - different header
	 * - extra comment
	 * 
	 */
	
}
