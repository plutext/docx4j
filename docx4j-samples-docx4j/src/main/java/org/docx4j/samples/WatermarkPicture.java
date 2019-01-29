package org.docx4j.samples;

import java.io.File;
import java.io.IOException;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImagePngPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.SectPr;

/**
 * 
 * A watermark is set via the headers.
 * 
 *  This is different to w:document/w:background (see BackgroundImage sample for that,
 *  though this WatermarkPicture is probably what you want.)
 *  
 * @author jharrop
 */
public class WatermarkPicture
{


	static String DOCX_OUT; 
	
    public static void main(String[] args) throws Exception
    {
    	
		// The image to add
		imageFile = new File(System.getProperty("user.dir") + "/src/test/resources/images/greentick.png" );  
		
    	// Save it to
		DOCX_OUT = System.getProperty("user.dir") + "/OUT_WaterMarkPicture.docx";

    	WatermarkPicture sample = new WatermarkPicture();
        sample.addWaterMark();
    }

    static ObjectFactory factory = Context.getWmlObjectFactory();
	static File imageFile; 
    
    private byte[] image;    
    private WordprocessingMLPackage wordMLPackage;
    
    public void addWaterMark() throws Exception
    {
    	
    	image = this.getImage();

    	wordMLPackage = WordprocessingMLPackage
                .createPackage();
        
    	// A watermark is defined in the headers, which are in turn set in sectPr
    	wordMLPackage.getMainDocumentPart().getContents().getBody().setSectPr(
    			createSectPr() );

        File f = new File(DOCX_OUT);
        wordMLPackage.save(f);

    }
    
	private SectPr createSectPr() throws Exception {
		
		String openXML = "<w:sectPr xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				
				// Word adds the background image in each of 3 header parts
	            + "<w:headerReference r:id=\"" + createHeaderPart("even").getId() + "\" w:type=\"even\"/>"
	            + "<w:headerReference r:id=\"" + createHeaderPart("default").getId() + "\" w:type=\"default\"/>"
	            + "<w:headerReference r:id=\"" + createHeaderPart("first").getId() + "\" w:type=\"first\"/>"
	            
	            // Word adds empty footer parts when you create a watermark, but its not necessary
	            
//	            + "<w:footerReference r:id=\"rId9\" w:type=\"even\"/>"
//	            + "<w:footerReference r:id=\"rId10\" w:type=\"default\"/>"
//	            + "<w:footerReference r:id=\"rId12\" w:type=\"first\"/>"
	            
	            + "<w:pgSz w:h=\"15840\" w:w=\"12240\"/>"
	            + "<w:pgMar w:bottom=\"1440\" w:footer=\"708\" w:gutter=\"0\" w:header=\"708\" w:left=\"1440\" w:right=\"1440\" w:top=\"1440\"/>"
	            + "<w:cols w:space=\"708\"/>"
	            + "<w:docGrid w:linePitch=\"360\"/>"
	        +"</w:sectPr>";
	 
		return (SectPr)XmlUtils.unmarshalString(openXML);
	
	}

	private Relationship createHeaderPart(String nameSuffix) throws Exception {
		
		HeaderPart headerPart = new HeaderPart(new PartName("/word/header-" + nameSuffix + ".xml"));
		Relationship rel =  wordMLPackage.getMainDocumentPart().addTargetPart(headerPart);
		
		setHdr( headerPart);

		return rel;
	}
	
	private void setHdr(HeaderPart headerPart) throws Exception  {
		
		ImagePngPart imagePart = new ImagePngPart(new PartName("/media/background.png"));
		imagePart.setBinaryData(image);
		Relationship rel = headerPart.addTargetPart(imagePart, AddPartBehaviour.REUSE_EXISTING); // the one image is shared by the 3 header parts
		
		String openXML = "<w:hdr mc:Ignorable=\"w14 wp14\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
	            + "<w:p>"
	                + "<w:pPr>"
	                    + "<w:pStyle w:val=\"Header\"/>"
	                +"</w:pPr>"
	                + "<w:r>"
	                    + "<w:rPr>"
	                        + "<w:noProof/>"
	                    +"</w:rPr>"
	                    + "<w:pict>"
	                        + "<v:shapetype coordsize=\"21600,21600\" filled=\"f\" id=\"_x0000_t75\" o:preferrelative=\"t\" o:spt=\"75\" path=\"m@4@5l@4@11@9@11@9@5xe\" stroked=\"f\">"
	                            + "<v:stroke joinstyle=\"miter\"/>"
	                            + "<v:formulas>"
	                                + "<v:f eqn=\"if lineDrawn pixelLineWidth 0\"/>"
	                                + "<v:f eqn=\"sum @0 1 0\"/>"
	                                + "<v:f eqn=\"sum 0 0 @1\"/>"
	                                + "<v:f eqn=\"prod @2 1 2\"/>"
	                                + "<v:f eqn=\"prod @3 21600 pixelWidth\"/>"
	                                + "<v:f eqn=\"prod @3 21600 pixelHeight\"/>"
	                                + "<v:f eqn=\"sum @0 0 1\"/>"
	                                + "<v:f eqn=\"prod @6 1 2\"/>"
	                                + "<v:f eqn=\"prod @7 21600 pixelWidth\"/>"
	                                + "<v:f eqn=\"sum @8 21600 0\"/>"
	                                + "<v:f eqn=\"prod @7 21600 pixelHeight\"/>"
	                                + "<v:f eqn=\"sum @10 21600 0\"/>"
	                            +"</v:formulas>"
	                            + "<v:path gradientshapeok=\"t\" o:connecttype=\"rect\" o:extrusionok=\"f\"/>"
	                            + "<o:lock aspectratio=\"t\" v:ext=\"edit\"/>"
	                        +"</v:shapetype>"
	                        + "<v:shape id=\"WordPictureWatermark835936646\" o:allowincell=\"f\" o:spid=\"_x0000_s2050\" style=\"position:absolute;margin-left:0;margin-top:0;width:467.95pt;height:615.75pt;z-index:-251657216;mso-position-horizontal:center;mso-position-horizontal-relative:margin;mso-position-vertical:center;mso-position-vertical-relative:margin\" type=\"#_x0000_t75\">"
	                            + "<v:imagedata blacklevel=\"22938f\" gain=\"19661f\" o:title=\"docx4j-logo\" r:id=\"" + rel.getId() + "\"/>"
	                        +"</v:shape>"
	                    +"</w:pict>"
	                +"</w:r>"
	            +"</w:p>"
	        +"</w:hdr>";
		
			Hdr hdr = (Hdr)XmlUtils.unmarshalString(openXML);
			
			headerPart.setJaxbElement(hdr);

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
