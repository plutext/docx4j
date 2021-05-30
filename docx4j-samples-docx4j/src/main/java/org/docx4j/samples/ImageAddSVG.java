package org.docx4j.samples;

import java.io.File;

import javax.xml.bind.JAXBElement;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DefaultXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Drawing;

public class ImageAddSVG {

	public static void main(String[] args) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		// The image to add
		File file = new File(System.getProperty("user.dir") 
				+ "/image1.svg" );
		
		java.io.InputStream is = new java.io.FileInputStream(file );

		DefaultXmlPart imagePart = new DefaultXmlPart(new PartName("/word/media/image1.svg"));
		imagePart.setRelationshipType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/image");
		// <Default ContentType="image/svg+xml" Extension="svg"/>
		imagePart.setContentType(new ContentType("image/svg+xml"));
		imagePart.setDocument(is); 
		
		
		Relationship rel = wordMLPackage.getMainDocumentPart().addTargetPart(imagePart);

        org.docx4j.wml.P p = newImage( rel ); 
		wordMLPackage.getMainDocumentPart().addObject(p);

		
		
		// Now save it 
		wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/OUT_ImageAddSVG.docx") );
		
	}

	/**
	 * Create image, without specifying width
	 */
	public static org.docx4j.wml.P newImage(Relationship rel) throws Exception {		
		
		Drawing drawing = createImageInline( rel);
        
        // Now add the inline in w:p/w:r/w:drawing
		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
		org.docx4j.wml.P  p = factory.createP();
		org.docx4j.wml.R  run = factory.createR();		
		p.getContent().add(run);        

        JAXBElement<org.docx4j.wml.Drawing> drawingWrapped = factory.createRDrawing(drawing); 
        run.getContent().add( drawingWrapped);         
				
		return p;
		
	}	
	
	public static Drawing createImageInline(Relationship rel) throws Exception {
		
		
        String ml =
                "<w:drawing " + namespaces + ">"
                        + "<wp:inline distB=\"0\" distL=\"0\" distR=\"0\" distT=\"0\">"
                        + "<wp:extent cx=\"1019175\" cy=\"1581150\"/>"

                        + "<wp:effectExtent b=\"0\" l=\"0\" r=\"9525\" t=\"0\"/>"

                        + "<wp:docPr id=\"2\" name=\"Picture 2\"/>"

                        + "<wp:cNvGraphicFramePr>"
                                + "<a:graphicFrameLocks noChangeAspect=\"true\"/>"

                        + "</wp:cNvGraphicFramePr>"

                        + "<a:graphic>"
                                + "<a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
                                        + "<pic:pic>"
                                                + "<pic:nvPicPr>"
                                                        + "<pic:cNvPr id=\"1\" name=\"\"/>"

                                                        + "<pic:cNvPicPr/>"

                                                + "</pic:nvPicPr>"

                                                + "<pic:blipFill>"
                                                        + "<a:blip>"
	                                                        + "<a:extLst>"
		                                                        + "<a:ext uri=\"{96DAC541-7B7A-43D3-8B79-37D633B846F1}\">"
		                                                        	+ "<asvg:svgBlip xmlns:asvg=\"http://schemas.microsoft.com/office/drawing/2016/SVG/main\" r:embed=\"" + rel.getId() + "\"/>"
													        	+ "</a:ext>"
												        	+ "</a:extLst>"
	                                                     + "</a:blip>"
                                                

                                                        + "<a:stretch>"
                                                                + "<a:fillRect/>"

                                                        + "</a:stretch>"

                                                + "</pic:blipFill>"

                                                + "<pic:spPr>"
                                                        + "<a:xfrm>"
                                                                + "<a:off x=\"0\" y=\"0\"/>"

                                                                + "<a:ext cx=\"1019175\" cy=\"1581150\"/>"

                                                        + "</a:xfrm>"

                                                        + "<a:prstGeom prst=\"rect\">"
                                                                + "<a:avLst/>"

                                                        + "</a:prstGeom>"

                                                + "</pic:spPr>"

                                        + "</pic:pic>"

                                + "</a:graphicData>"

                        + "</a:graphic>"

                + "</wp:inline>"

        + "</w:drawing>";
        
        Drawing drawing =  (Drawing)org.docx4j.XmlUtils.unmarshalString(ml);
        
		return drawing;		
	}

    final static String namespaces = " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
    		+ "xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" "
    		+ "xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" "
    		+ "xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\" "
            + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" "
            ;
    
	
}
