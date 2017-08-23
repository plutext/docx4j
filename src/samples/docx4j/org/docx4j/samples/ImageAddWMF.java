package org.docx4j.samples;

import java.io.File;

import javax.xml.bind.JAXBElement;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileWmfPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Pict;

public class ImageAddWMF {

	public static void main(String[] args) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		// The image to add
		File file = new File(System.getProperty("user.dir") 
				+ "/image1.wmf" );
		
		java.io.InputStream is = new java.io.FileInputStream(file );

		MetafileWmfPart imagePart = new MetafileWmfPart(new PartName("/word/media/image1.wmf"));
		imagePart.setBinaryData(is);
		Relationship rel = wordMLPackage.getMainDocumentPart().addTargetPart(imagePart);

        org.docx4j.wml.P p = newImage( rel, "width:33pt;height:15pt", "image1" ); // for best results, supply the size
		wordMLPackage.getMainDocumentPart().addObject(p);

		
		
		// Now save it 
		wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/OUT_ImageAddWMF.docx") );
		
	}

	/**
	 * Create image, without specifying width
	 */
	public static org.docx4j.wml.P newImage(Relationship rel, String style, String title) throws Exception {		
		
		Pict pict = createImageInline( rel, style, title);
        
        // Now add the inline in w:p/w:r/w:drawing
		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
		org.docx4j.wml.P  p = factory.createP();
		org.docx4j.wml.R  run = factory.createR();		
		p.getContent().add(run);        
		run.getContent().add(pict);		
		
		return p;
		
	}	
	
	public static org.docx4j.wml.Pict createImageInline(Relationship rel, String style, String title) throws Exception {
		
		
        String ml =
                "<w:pict " + namespaces + ">"
        +"<v:shape id=\"_x0000_i1025\" type=\"#_x0000_t75\" style=\"" + style + "\">"
        +"  <v:imagedata r:id=\""+rel.getId()+"\" o:title=\""+title+"\"/>"
        +"</v:shape>"
      +"</w:pict>";        		
        		
        Pict pict = (Pict) org.docx4j.XmlUtils.unmarshalString(ml);
        
		return pict;		
	}

    final static String namespaces = " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
    		+ "xmlns:v=\"urn:schemas-microsoft-com:vml\" "
    		+ "xmlns:o=\"urn:schemas-microsoft-com:office:office\" "
            + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" "
            + "xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
            ;
	
}
