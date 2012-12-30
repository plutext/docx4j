package org.docx4j.openpackaging.parts.WordprocessingML;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * Test that we can create an image part 
 * where the image source is:
 * 
 * - byte[]
 * - File
 * - file url
 * - http url
 * 
 * @author jharrop
 *
 */
public class CreateImageTest {
	
	private final static int LENGTH = 1965;

	@Test
	public void testFile() throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		File file = new File(System.getProperty("user.dir") + "/src/test/resources/images/greentick.png" );
        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, file);
        assertTrue(imagePart instanceof ImagePngPart);
        
        assertTrue( ((ImagePngPart)imagePart).getBuffer().capacity()==1965 );
        
	}

	@Test
	public void testByteArray() throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		File file = new File(System.getProperty("user.dir") + "/src/test/resources/images/greentick.png" );
		
		// Convert file to byte array
		java.io.InputStream is = new java.io.FileInputStream(file );
        long length = file.length();    
        // You cannot create an array using a long type.
        // It needs to be an int type.
        if (length > Integer.MAX_VALUE) {
        	System.out.println("File too large!!");
        }
        byte[] bytes = new byte[(int)length];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            System.out.println("Could not completely read file "+file.getName());
        }
        is.close();		
		
        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, bytes);
        assertTrue(imagePart instanceof ImagePngPart);
        assertTrue( ((ImagePngPart)imagePart).getBuffer().capacity()==1965 );
        
	}
	
	@Test
	public void testFileUrl() throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		// Construct file url
		File file = new File(System.getProperty("user.dir") + "/src/test/resources/images/greentick.png" );		
		URL url = file.toURI().toURL();
		System.out.println(url);
		
        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createLinkedImagePart(wordMLPackage, url);
        assertTrue(imagePart instanceof ImagePngPart);
        
//        System.out.println(
//        		XmlUtils.marshaltoString(imagePart.getSourceRelationship(), true, Context.jcRelationships) );
        
	}
	
	@Test
	public void testHttpUrl() throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		// Construct file url
		URL url = new URL("http://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Green_tick.svg/75px-Green_tick.svg.png");
		
        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createLinkedImagePart(wordMLPackage, url);
        assertTrue(imagePart instanceof ImagePngPart);
        
//        System.out.println(
//        		XmlUtils.marshaltoString(imagePart.getSourceRelationship(), true, Context.jcRelationships) );
        
	}
	
}
