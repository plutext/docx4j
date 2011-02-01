package org.docx4j.openpackaging.parts.relationships;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.relationships.AlteredParts.Alterations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PatcherTest {

	protected static Logger log = Logger.getLogger(PatcherTest.class);
	
	private static String resourceDir = System.getProperty("user.dir") + "/src/test/resources/AlteredParts/";
	
	static boolean save = true; // so we can check that Word can actually open the result
	static String DIR_OUT = System.getProperty("user.dir") + "/src/test/";
	
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
	public void testPatcherModsAndDeletions() throws Exception {
		// document.xml and rels should be different,
		// and headers added: doc now contains 2 x headers, 0 x footers, endnotes, and footnotes
		// Interesting that the number of header parts is reduced (and content of header1.xml changed)

		log.warn("\ntestPatcherDeletions\n");
		
		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "header-section2.docx"));
		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "header-simple.docx"));
		
		Alterations alterations = AlteredParts.start(thisPackage, otherPackage);

		alterations.debug();
				
		assertTrue( alterations.getPartsAdded().size()==0 ); // one of the existing header parts is re-purposed
		assertTrue( alterations.getPartsModified().size()==4 );
		assertTrue( alterations.getPartsDeleted().size()==4 );
		
		Patcher.apply(otherPackage, alterations);
		
		// How best to test?  For now, by inspection ..
		if (save) {		
			SaveToZipFile saver = new SaveToZipFile(otherPackage);
			saver.save(DIR_OUT+"patch-producing-header-section2.docx");
		}
		
	}

	@Test
	public void testPatcherAdditions() throws Exception {
		// the affected header should be different,
		// plus image + header rels

		log.warn("\ntestPatcherAdditions\n");
		
		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "header-simple-plus-image.docx"));
		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "header-simple.docx"));
		
		Alterations alterations = AlteredParts.start(thisPackage, otherPackage);

		alterations.debug();
				
		assertTrue( alterations.getPartsAdded().size()==2 );
		assertTrue( alterations.getPartsModified().size()==1 );
		assertTrue( alterations.getPartsDeleted().size()==0 );
		
		Patcher.apply(otherPackage, alterations);
		
		// How best to test?  For now, by inspection ..
		// Documents should be identical though (except for [Content_Types.xml - consider that
		if (save) {		
			SaveToZipFile saver = new SaveToZipFile(otherPackage);
			saver.save(DIR_OUT+"patch-producing-header-simple-plus-image.docx");
		}
		
	}
	
}
