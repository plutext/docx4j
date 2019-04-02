package org.docx4j.samples;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBElement;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.vml.CTFill;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBackground;
import org.docx4j.wml.ObjectFactory;

/*
 * 
 * Add w:document/w:background;  this should be visible in Word 2016, 2013, 2010; haven't tested 2007.
 * In Word Online (as of April 2019), you only see it in "reading view" (which is not editable).
 * 
 * Without the <w:displayBackgroundShape/> setting, this would only be visible in Windows Explorer file preview, or
 * Word's "Web layout" and "Full screen reading" document views (ie not Print Layout, Outline,
 * or Draft).  Checking Word options > Display > Printing options > Print background colors and images
 * makes no difference to what you see in Print Layout, but it does change what you see in Print Preview.
 * 
 * This creates a "tiled" background from your image.  It is different to a watermark, which is set via the headers (see WatermarkPicture sample for that),
 * which is probably what you want.
 * 
 * From [MS-OI29500]:
 * 
 * a. The standard states that any element from the VML namespace or the drawingML namespace 
 *    is allowed as a child of the background element.
 *    
 *    Word renders any drawingML specified as a background at the beginning of the document and not as a background.
 */
public class BackgroundImage {


	static String DOCX_OUT; 
	
    public static void main(String[] args) throws Exception
    {
    	
		// The image to add
		imageFile = new File(System.getProperty("user.dir") + "/src/test/resources/images/greentick.png" );  
		
    	// Save it to
		DOCX_OUT = System.getProperty("user.dir") + "/OUT_BackgroundImage.docx";

		BackgroundImage sample = new BackgroundImage();
        sample.addBackground();
    }

    static ObjectFactory factory = Context.getWmlObjectFactory();
	static File imageFile; 
    
    private byte[] image;    
    private WordprocessingMLPackage wordMLPackage;
    
    public void addBackground() throws Exception
    {
    	
    	image = this.getImage();

    	wordMLPackage = WordprocessingMLPackage.createPackage();
        
    	BinaryPartAbstractImage imagePartBG = BinaryPartAbstractImage.createImagePart(wordMLPackage, image);

    	wordMLPackage.getMainDocumentPart().getJaxbElement().setBackground(
    			createBackground(
    					imagePartBG.getRelLast().getId())); 

    	wordMLPackage.getMainDocumentPart().addParagraphOfText("You should be able to see your background; if not, go to 'Web layout' or 'Full screen reading' document view!");
    	
    	// <w:displayBackgroundShape/>
    	DocumentSettingsPart dsp = wordMLPackage.getMainDocumentPart().getDocumentSettingsPart(true);
    	dsp.getContents().setDisplayBackgroundShape(new BooleanDefaultTrue());
    	
        File f = new File(DOCX_OUT);
        wordMLPackage.save(f);

    }
    
	private static CTBackground createBackground(String rId) {

		org.docx4j.wml.ObjectFactory wmlObjectFactory = new org.docx4j.wml.ObjectFactory();

		CTBackground background = wmlObjectFactory.createCTBackground();
		background.setColor("FFFFFF");
		org.docx4j.vml.ObjectFactory vmlObjectFactory = new org.docx4j.vml.ObjectFactory();
		// Create object for background (wrapped in JAXBElement)
		org.docx4j.vml.CTBackground background2 = vmlObjectFactory
				.createCTBackground();
		JAXBElement<org.docx4j.vml.CTBackground> backgroundWrapped = vmlObjectFactory
				.createBackground(background2);
		background.getAnyAndAny().add(backgroundWrapped);
		background2.setTargetscreensize("1024,768");
		background2.setVmlId("_x0000_s1025");
		background2.setBwmode(org.docx4j.vml.officedrawing.STBWMode.WHITE);
		// Create object for fill
		CTFill fill = vmlObjectFactory.createCTFill();
		background2.setFill(fill);
		fill.setTitle("Alien 1");
		fill.setId(rId);
		fill.setType(org.docx4j.vml.STFillType.FRAME);
		fill.setRecolor(org.docx4j.vml.STTrueFalse.T);

		return background;
	}

	private byte[] getImage() throws IOException {
	
		// Our utility method wants that as a byte array
		java.io.InputStream is = new java.io.FileInputStream(imageFile );
	    long length = imageFile.length();    
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
	        System.out.println("Could not completely read file "+imageFile.getName());
	    }
	    is.close();
		
	    return bytes;
	}


}

