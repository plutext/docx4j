package org.docx4j.openpackaging.parts.WordprocessingML;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

public class ImageTypeBmpTest {
	
	@Test
	public void testExtensions() throws Exception {

		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		File file = new File(System.getProperty("user.dir") + "/src/test/resources/images/VENUS.BMP" );
		
		// Our utility method wants that as a byte array
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
        
        String filenameHint = null;
        String altText = null;
        int id1 = 0;
        int id2 = 1;
        		
        org.docx4j.wml.P p = newImage( wordMLPackage, bytes, 
        		filenameHint, altText, 
    			id1, id2 );
        
		// Now add our p to the document
		wordMLPackage.getMainDocumentPart().addObject(p);

				
	}

	public static org.docx4j.wml.P newImage( WordprocessingMLPackage wordMLPackage,
			byte[] bytes,
			String filenameHint, String altText, 
			int id1, int id2) throws Exception {
		
        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, bytes);
		        
//        System.out.println(imagePart.getContentType());
//        System.out.println(imagePart.getClass().getName());
        assertTrue(imagePart instanceof ImageBmpPart);
        
        Inline inline = imagePart.createImageInline( filenameHint, altText, 
    			id1, id2);
        
        // Now add the inline in w:p/w:r/w:drawing
		org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
		org.docx4j.wml.P  p = factory.createP();
		org.docx4j.wml.R  run = factory.createR();		
		p.getParagraphContent().add(run);        
		org.docx4j.wml.Drawing drawing = factory.createDrawing();		
		run.getRunContent().add(drawing);		
		drawing.getAnchorOrInline().add(inline);
		
		return p;
		
	}	
	
}
