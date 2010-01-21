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

import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.dml.CTColorMapping;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTTextBody;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.pptx4j.model.ResolvedLayout;
import org.pptx4j.model.ShapeWrapper;
import org.pptx4j.pml.CTPlaceholder;
import org.pptx4j.pml.CommonSlideData;
import org.pptx4j.pml.GroupShape;
import org.pptx4j.pml.ObjectFactory;
import org.pptx4j.pml.Shape;
import org.pptx4j.pml.Sld;
import org.pptx4j.pml.Shape.NvSpPr;



public final class SlidePart extends JaxbPmlPart<Sld> {
	
	public SlidePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public SlidePart() throws InvalidFormatException {
		super(new PartName("/ppt/slides/slide1.xml"));
		init();
	}
	
	public void init() {		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.PRESENTATIONML_SLIDE));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.PRESENTATIONML_SLIDE);
		
	}
	
	
	public static Sld createSld() throws JAXBException {

		ObjectFactory factory = Context.getpmlObjectFactory(); 
		Sld sld = factory.createSld();
		sld.setCSld( 
				(CommonSlideData)XmlUtils.unmarshalString(COMMON_SLIDE_DATA, Context.jcPML, CommonSlideData.class) );
		// sld.setClrMapOvr(value)
		
		return sld;		
	}
	
	private ResolvedLayout resolvedLayout;
	public ResolvedLayout getResolvedLayout() {
		if (resolvedLayout!=null) {
			return resolvedLayout;		
		}
		
		resolvedLayout = ResolvedLayout.resolveSlideLayout(this);
		return resolvedLayout;
	}	

}
