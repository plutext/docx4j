package org.docx4j.openpackaging.parts.relationships;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import junit.framework.Assert;

import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.AlteredParts.Alterations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatcherTest {

	protected static Logger log = LoggerFactory.getLogger(PatcherTest.class);
	
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
	public void testSimpleDocx() throws Exception {
		// Should return no differences
		
		log.warn("\ntestSimpleDocx\n");
		
		String inputfilepath = resourceDir + "paragraph-single.docx";
		
		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		Alterations alterations = AlteredParts.start(thisPackage, otherPackage);
		
		Patcher.apply(otherPackage, alterations);
		
//		if (save) {		
//			SaveToZipFile saver = new SaveToZipFile(otherPackage);
//			saver.save(DIR_OUT+"patch-producing-header-section2.docx");
//		}
			
		Assert.assertTrue(java.util.Arrays.equals(
				marshallFlatOPC(thisPackage).toByteArray(), 
				marshallFlatOPC(otherPackage).toByteArray() ));
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

		Patcher.apply(otherPackage, alterations);
		
//		if (save) {		
//			SaveToZipFile saver = new SaveToZipFile(otherPackage);
//			saver.save(DIR_OUT+"patch-producing-header-section2.docx");
//		}
		
		Assert.assertTrue(java.util.Arrays.equals(
				marshallFlatOPC(thisPackage).toByteArray(), 
				marshallFlatOPC(otherPackage).toByteArray() ));
		
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

		Patcher.apply(otherPackage, alterations);
		
//		if (save) {		
//			SaveToZipFile saver = new SaveToZipFile(otherPackage);
//			saver.save(DIR_OUT+"patch-producing-header-section2.docx");
//		}
		
		
		byte[] array1 = marshallFlatOPC(thisPackage).toByteArray();
		byte[] array2 = marshallFlatOPC(otherPackage).toByteArray();
				
		Assert.assertTrue(java.util.Arrays.equals(
				array1, 
				array2));
		
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

		Patcher.apply(otherPackage, alterations);
		
//		if (save) {		
//			SaveToZipFile saver = new SaveToZipFile(otherPackage);
//			saver.save(DIR_OUT+"patch-producing-header-section2.docx");
//		}
		
		Assert.assertTrue(java.util.Arrays.equals(
				marshallFlatOPC(thisPackage).toByteArray(), 
				marshallFlatOPC(otherPackage).toByteArray() ));
		
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

		Patcher.apply(otherPackage, alterations);
		
//		if (save) {		
//			SaveToZipFile saver = new SaveToZipFile(otherPackage);
//			saver.save(DIR_OUT+"patch-producing-header-section2.docx");
//		}
		
		Assert.assertTrue(java.util.Arrays.equals(
				marshallFlatOPC(thisPackage).toByteArray(), 
				marshallFlatOPC(otherPackage).toByteArray() ));
		
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

		Patcher.apply(otherPackage, alterations);
		
//		if (save) {		
//			SaveToZipFile saver = new SaveToZipFile(otherPackage);
//			saver.save(DIR_OUT+"patch-producing-header-section2.docx");
//		}
		
		Assert.assertTrue(java.util.Arrays.equals(
				marshallFlatOPC(thisPackage).toByteArray(), 
				marshallFlatOPC(otherPackage).toByteArray() ));
		
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

		Patcher.apply(otherPackage, alterations);
		
//		if (save) {		
//			SaveToZipFile saver = new SaveToZipFile(otherPackage);
//			saver.save(DIR_OUT+"patch-producing-header-section2.docx");
//		}
		
		Assert.assertTrue(java.util.Arrays.equals(
				marshallFlatOPC(thisPackage).toByteArray(), 
				marshallFlatOPC(otherPackage).toByteArray() ));
		
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

		Patcher.apply(otherPackage, alterations);
		
//		if (save) {		
//			SaveToZipFile saver = new SaveToZipFile(otherPackage);
//			saver.save(DIR_OUT+"patch-producing-header-section2.docx");
//		}
		
		Assert.assertTrue(java.util.Arrays.equals(
				marshallFlatOPC(thisPackage).toByteArray(), 
				marshallFlatOPC(otherPackage).toByteArray() ));
		
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

		Patcher.apply(otherPackage, alterations);
		
//		if (save) {		
//			SaveToZipFile saver = new SaveToZipFile(otherPackage);
//			saver.save(DIR_OUT+"patch-producing-header-section2.docx");
//		}
		
		Assert.assertTrue(java.util.Arrays.equals(
				marshallFlatOPC(thisPackage).toByteArray(), 
				marshallFlatOPC(otherPackage).toByteArray() ));
		
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

		Patcher.apply(otherPackage, alterations);
		
//		if (save) {		
//			SaveToZipFile saver = new SaveToZipFile(otherPackage);
//			saver.save(DIR_OUT+"patch-producing-header-section2.docx");
//		}
		
		Assert.assertTrue(java.util.Arrays.equals(
				marshallFlatOPC(thisPackage).toByteArray(), 
				marshallFlatOPC(otherPackage).toByteArray() ));
		
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

		Patcher.apply(otherPackage, alterations);
		
//		if (save) {		
//			SaveToZipFile saver = new SaveToZipFile(otherPackage);
//			saver.save(DIR_OUT+"patch-producing-header-section2.docx");
//		}
		
		Assert.assertTrue(java.util.Arrays.equals(
				marshallFlatOPC(thisPackage).toByteArray(), 
				marshallFlatOPC(otherPackage).toByteArray() ));
		
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

		Patcher.apply(otherPackage, alterations);
		
//		if (save) {		
//			SaveToZipFile saver = new SaveToZipFile(otherPackage);
//			saver.save(DIR_OUT+"patch-producing-header-section2.docx");
//		}
		
		Assert.assertTrue(java.util.Arrays.equals(
				marshallFlatOPC(thisPackage).toByteArray(), 
				marshallFlatOPC(otherPackage).toByteArray() ));
		
	}
		
	private ByteArrayOutputStream marshallFlatOPC(WordprocessingMLPackage wmlPkg) throws JAXBException, Docx4JException {

    	ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		
	   	// Create a org.docx4j.wml.Package object
		FlatOpcXmlCreator worker = new FlatOpcXmlCreator(wmlPkg);
		org.docx4j.xmlPackage.Package pkg = worker.get();
    	
    	// Now marshall it
		JAXBContext jc = Context.jcXmlPackage;
		Marshaller marshaller=jc.createMarshaller();
		
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		NamespacePrefixMapperUtils.setProperty(marshaller, 
				NamespacePrefixMapperUtils.getPrefixMapper());	
		
		marshaller.marshal(pkg, baos);
//		marshaller.marshal(pkg, System.out);
		
		return baos;
		
	}
	
}
