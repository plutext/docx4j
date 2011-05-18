package org.docx4j.openpackaging.parts.DrawingML;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.jaxb.Context;

public abstract class JaxbDmlPart<E>  extends JaxbXmlPart<E> {

	public JaxbDmlPart(PartName partName) throws InvalidFormatException {
		super(partName);
		setJAXBContext(Context.jc);						
	}

	public JaxbDmlPart() throws InvalidFormatException {
		super(new PartName("/xl/blagh.xml"));
		setJAXBContext(Context.jc);						
	}

	public static Part newPartForContentType(String contentType, String partName)
	throws InvalidFormatException, PartUnrecognisedException {
		
		if (contentType.equals(ContentTypes.DRAWINGML_DIAGRAM_COLORS)) {
			return new org.docx4j.openpackaging.parts.DrawingML.DiagramColorsPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.DRAWINGML_DIAGRAM_DATA)) {
			return new org.docx4j.openpackaging.parts.DrawingML.DiagramDataPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.DRAWINGML_DIAGRAM_LAYOUT)) {
			return new org.docx4j.openpackaging.parts.DrawingML.DiagramLayoutPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.DRAWINGML_DIAGRAM_STYLE)) {
			return new org.docx4j.openpackaging.parts.DrawingML.DiagramStylePart(new PartName(partName)); 
		} else if (contentType.equals(ContentTypes.DRAWINGML_DRAWING)) {
			return new Drawing(new PartName(partName));
		} else if (contentType.equals(ContentTypes.DRAWINGML_CHART)) {
			return new Chart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.DRAWINGML_DIAGRAM_LAYOUT_HEADER)) {
			return new org.docx4j.openpackaging.parts.DrawingML.DiagramLayoutHeaderPart(new PartName(partName));
		}
		else {
			throw new PartUnrecognisedException("No subclass found for "
					+ partName + " (content type '" + contentType + "')");
		}
	}	
	
//    public E unmarshal( java.io.InputStream is ) throws JAXBException {
//    	
//		try {
//			setJAXBContext(Context.jcPML);						
//		    		    
//			Unmarshaller u = jc.createUnmarshaller();
//			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
//
//			jaxbElement = (E)u.unmarshal( is );						
//			log.debug( this.getClass().getName() + " unmarshalled" );									
//
//		} catch (JAXBException e ) {
//			log.error(e);
//			throw e;
//		}
//		return jaxbElement;
//    }	
    
	
}
