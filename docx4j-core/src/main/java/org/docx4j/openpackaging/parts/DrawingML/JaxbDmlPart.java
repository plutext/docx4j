/*
 *  Copyright 2010, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
package org.docx4j.openpackaging.parts.DrawingML;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.parts.JaxbXmlPartXPathAware;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;

public abstract class JaxbDmlPart<E>  extends JaxbXmlPartXPathAware<E> {

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
//		} else if (contentType.equals(ContentTypes.DRAWINGML_DIAGRAM_DRAWING)) {
//			return new org.docx4j.openpackaging.parts.DrawingML.DiagramDrawingPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.DRAWINGML_DRAWING)) {
			return new Drawing(new PartName(partName));
		} else if (contentType.equals(ContentTypes.DRAWINGML_CHART)) {
			return new Chart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.DRAWINGML_DIAGRAM_LAYOUT_HEADER)) {
			return new org.docx4j.openpackaging.parts.DrawingML.DiagramLayoutHeaderPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.DRAWINGML_CHART_SHAPES)) {
			return new ChartShapePart(new PartName(partName));
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
//			log.error(e.getMessage(), e);
//			throw e;
//		}
//		return jaxbElement;
//    }	
    
	
}
