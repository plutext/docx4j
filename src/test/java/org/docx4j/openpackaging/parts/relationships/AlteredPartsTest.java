package org.docx4j.openpackaging.parts.relationships;

import static org.junit.Assert.assertTrue;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.AlteredParts.Alterations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlteredPartsTest {

	protected static Logger log = LoggerFactory.getLogger(AlteredPartsTest.class);
	
	private static String resourceDir = System.getProperty("user.dir") + "/src/test/resources/AlteredParts/";
	/* All of the docx in resourceDir have had their docprops parts stripped
	 * (using StripParts).  */
		
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
		// Should return no differences
		
		log.warn("\ntestSimpleDocx\n");
		
		String inputfilepath = resourceDir + "paragraph-single.docx";
		
		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		Alterations alterations = AlteredParts.start(thisPackage, otherPackage);
		
		assertTrue( alterations.getPartsAdded().size()==0 );
//		assertTrue( alterations.getPartsModified().size()==0 ); // FIXME
		assertTrue( alterations.getPartsDeleted().size()==0 );
	}

	@Test
	public void testExtraParagraph() throws Exception {
		// Only document.xml should be different

		log.warn("\ntestExtraParagraph\n");
		
		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "paragraph-single.docx"));
		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "paragraph-two.docx"));
		
		Alterations alterations = AlteredParts.start(thisPackage, otherPackage);

		alterations.debug();
				
		assertTrue( alterations.getPartsAdded().size()==0 );
		assertTrue( alterations.getPartsModified().size()==1 );
		assertTrue( alterations.getPartsDeleted().size()==0 );
		
	}

	@Test
	public void testHyperlink() throws Exception {
		// document.xml and rels should be different,
		// and styles, since hyperlink style is now in use

		log.warn("\ntestHyperlink\n");
		
		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "hyperlink.docx"));
		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "paragraph-single.docx"));
		
		Alterations alterations = AlteredParts.start(thisPackage, otherPackage);

		alterations.debug();
				
		assertTrue( alterations.getPartsAdded().size()==0 );
		assertTrue( alterations.getPartsModified().size()==3 );
		assertTrue( alterations.getPartsDeleted().size()==0 );
		
	}

	@Test
	public void testImageScaled() throws Exception {
		// document.xml should be different,

		log.warn("\ntestImageScaled\n");
		
		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "image-png1.docx"));
		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "image-png1-scaled.docx"));
		
		Alterations alterations = AlteredParts.start(thisPackage, otherPackage);

		alterations.debug();
				
		assertTrue( alterations.getPartsAdded().size()==0 );
		assertTrue( alterations.getPartsModified().size()==1 );
		assertTrue( alterations.getPartsDeleted().size()==0 );
		
	}
	
	@Test
	public void testImagesDifferentPng() throws Exception {
		// document.xml and image part should be different,
		// but rels unchanged

		log.warn("\testImagesDifferentPng\n");
		
		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "image-png1.docx"));
		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "image-png2.docx"));
		
		Alterations alterations = AlteredParts.start(thisPackage, otherPackage);

		alterations.debug();
				
		assertTrue( alterations.getPartsAdded().size()==0 );
		assertTrue( alterations.getPartsModified().size()==2 );
		assertTrue( alterations.getPartsDeleted().size()==0 );
		
	}

	@Test
	public void testImagesRelId() throws Exception {
		// a jpg inserted before, so the png's relId is now different
		// document.xml and rels should be different,
		// and extra image reported
		// Because we currently match images on name only, and
		// png is now called image2.png, that has changed as well.

		log.warn("\ntestImagesDifferentPng\n");
		
		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "images-jpg then png2.docx"));
		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "image-png2.docx"));
		
		Alterations alterations = AlteredParts.start(thisPackage, otherPackage);

		alterations.debug();
				
		assertTrue( alterations.getPartsAdded().size()==2 );
		assertTrue( alterations.getPartsModified().size()==2 );
		assertTrue( alterations.getPartsDeleted().size()==1 );
		
	}

	@Test
	public void testImagesOrderSwapped() throws Exception {
		// document.xml and rels should be different;
		// Because we currently match images on name only, 
		// both image names have changed as well.
		

		log.warn("\ntestImagesOrderSwapped\n");
		
		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "images-jpg then png2.docx"));
		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "images-png2 then jpg.docx"));
		
		Alterations alterations = AlteredParts.start(thisPackage, otherPackage);

		alterations.debug();
				
		assertTrue( alterations.getPartsAdded().size()==2 );
		assertTrue( alterations.getPartsModified().size()==2 );
		assertTrue( alterations.getPartsDeleted().size()==2 );
		
	}

	@Test
	public void testHeaderAdded() throws Exception {
		// document.xml and rels should be different,
		// and headers added: Word adds 3 x headers, 3 x footers, endnotes, and footnotes
		// plus header style

		log.warn("\ntestHeaderAdded\n");
		
		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "header-simple.docx"));
		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "paragraph-single.docx"));
		
		Alterations alterations = AlteredParts.start(thisPackage, otherPackage);

		alterations.debug();
				
		assertTrue( alterations.getPartsAdded().size()==8 );
		assertTrue( alterations.getPartsModified().size()==3 );
		assertTrue( alterations.getPartsDeleted().size()==0 );
		
	}

	@Test
	public void testHeaderAddedSection2() throws Exception {
		// document.xml and rels should be different,
		// and headers added: doc now contains 2 x headers, 0 x footers, endnotes, and footnotes
		// Interesting that the number of header parts is reduced (and content of header1.xml changed)

		log.warn("\ntestHeaderAddedSection2\n");
		
		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "header-section2.docx"));
		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "header-simple.docx"));
		
		Alterations alterations = AlteredParts.start(thisPackage, otherPackage);

		alterations.debug();
				
		assertTrue( alterations.getPartsAdded().size()==0 ); // one of the existing header parts is re-purposed
		assertTrue( alterations.getPartsModified().size()==4 );
		assertTrue( alterations.getPartsDeleted().size()==4 );
		
	}

	@Test
	public void testHeaderImageAdded() throws Exception {
		// the affected header should be different,
		// plus image + header rels

		log.warn("\ntestHeaderImageAdded\n");
		
		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "header-simple-plus-image.docx"));
		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "header-simple.docx"));
		
		Alterations alterations = AlteredParts.start(thisPackage, otherPackage);

		alterations.debug();
				
		assertTrue( alterations.getPartsAdded().size()==1 );
//		assertTrue( alterations.getPartsModified().size()==2 ); // FIXME
		assertTrue( alterations.getPartsDeleted().size()==0 );
		
	}
	
	@Test
	public void testComment() throws Exception {
		// document.xml + rels + added comments part should be different,
		// also styles

		log.warn("\ntestComment\n");
		
		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "comments-one.docx"));
		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "paragraph-single.docx"));
		
		Alterations alterations = AlteredParts.start(thisPackage, otherPackage);

		alterations.debug();
				
		assertTrue( alterations.getPartsAdded().size()==1 );
		assertTrue( alterations.getPartsModified().size()==3 );
		assertTrue( alterations.getPartsDeleted().size()==0 );
		
	}

	@Test
	public void testComment2() throws Exception {
		// document.xml + comments part should be different

		log.warn("\ntestComment2\n");
		
		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "comments-two.docx"));
		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(
				new java.io.File(resourceDir + "comments-one.docx"));
		
		Alterations alterations = AlteredParts.start(thisPackage, otherPackage);

		alterations.debug();
				
		assertTrue( alterations.getPartsAdded().size()==0 );
		assertTrue( alterations.getPartsModified().size()==2 );
		assertTrue( alterations.getPartsDeleted().size()==0 );
		
	}

	
}
