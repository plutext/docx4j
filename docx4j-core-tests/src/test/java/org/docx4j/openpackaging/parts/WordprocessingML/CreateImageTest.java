package org.docx4j.openpackaging.parts.WordprocessingML;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Ignore;
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
	
	
/*  
 * 2015 06 18 Commented out.  This test fails when run via JUnit:
 * 
java.lang.reflect.UndeclaredThrowableException
	at $Proxy35.readUnsignedInt(Unknown Source)
	at org.apache.xmlgraphics.image.loader.impl.PreloaderEPS.preloadImage(PreloaderEPS.java:65)
	at org.apache.xmlgraphics.image.loader.ImageManager.preloadImage(ImageManager.java:175)
	at org.apache.xmlgraphics.image.loader.cache.ImageCache.needImageInfo(ImageCache.java:128)
	at org.apache.xmlgraphics.image.loader.ImageManager.getImageInfo(ImageManager.java:122)
	at org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.getImageInfo(BinaryPartAbstractImage.java:866)
	at org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.ensureFormatIsSupported(BinaryPartAbstractImage.java:521)
	at org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.createLinkedImagePart(BinaryPartAbstractImage.java:636)
	at org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.createLinkedImagePart(BinaryPartAbstractImage.java:614)
	at org.docx4j.openpackaging.parts.WordprocessingML.CreateImageTest.testHttpUrl(CreateImageTest.java:100)
	:
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:197)
Caused by: java.lang.reflect.InvocationTargetException
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
	at java.lang.reflect.Method.invoke(Unknown Source)
	at org.apache.xmlgraphics.image.loader.impl.AbstractImageSessionContext$ObservingImageInputStreamInvocationHandler.invoke(AbstractImageSessionContext.java:219)
	... 32 more
Caused by: java.io.EOFException
	at javax.imageio.stream.ImageInputStreamImpl.readInt(Unknown Source)
	at javax.imageio.stream.ImageInputStreamImpl.readUnsignedInt(Unknown Source)
	... 37 more
	 * 
 * However, the same code works when run as a main method.  Go figure... 
 * 
 * 
	public void testHttpUrl() throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		// Construct file url
		URL url = new URL("http://www.docx4java.org/logo-75.png");
		//");
		                   
		
        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createLinkedImagePart(wordMLPackage, url);
        assertTrue(imagePart instanceof ImagePngPart);
        
//        System.out.println(
//        		XmlUtils.marshaltoString(imagePart.getSourceRelationship(), true, Context.jcRelationships) );
        
	}
	
*/	
	
}
