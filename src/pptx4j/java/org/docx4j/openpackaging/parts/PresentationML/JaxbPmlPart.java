/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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

package org.docx4j.openpackaging.parts.PresentationML;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Comments;



public abstract class JaxbPmlPart<E> extends JaxbXmlPart<E> {
	
	public JaxbPmlPart(PartName partName) throws InvalidFormatException {
		super(partName);
		setJAXBContext(Context.jcPML);						
	}

	public JaxbPmlPart() throws InvalidFormatException {
		super(new PartName("/ppt/presentation.xml"));
		setJAXBContext(Context.jcPML);						
	}

	public static Part newPartForContentType(String contentType, String partName)
	throws InvalidFormatException, PartUnrecognisedException {
		
		if (contentType.equals(ContentTypes.PRESENTATIONML_MAIN)) {
			return new MainPresentationPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_SLIDE)) {
			return new SlidePart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_SLIDE_MASTER)) {
			return new SlideMasterPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_SLIDE_LAYOUT)) {
			return new SlideLayoutPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_TABLE_STYLES)) {
			return new TableStylesPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_PRES_PROPS)) {
			return new PresentationPropertiesPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_VIEW_PROPS)) {
			return new ViewPropertiesPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_TAGS)) {
			return new TagsPart(new PartName(partName));
		} else {
			throw new PartUnrecognisedException("No subclass found for " 
					+ partName + " (content type '" + contentType + "')");					
		}
	}	
	
    public E unmarshal( java.io.InputStream is ) throws JAXBException {
    	
		try {
			setJAXBContext(Context.jcPML);						
		    		    
			Unmarshaller u = jc.createUnmarshaller();
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			jaxbElement = (E)u.unmarshal( is );						
			log.debug( this.getClass().getName() + " unmarshalled" );									

		} catch (JAXBException e ) {
			log.error(e);
			throw e;
		}
		return jaxbElement;
    }	
    
    
	
}
