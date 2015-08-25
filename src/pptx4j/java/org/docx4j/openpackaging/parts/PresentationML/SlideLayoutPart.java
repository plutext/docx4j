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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.McIgnorableNamespaceDeclarator;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.pptx4j.jaxb.Context;
import org.pptx4j.model.ResolvedLayout;
import org.pptx4j.model.ShapeWrapper;
import org.pptx4j.pml.CTPlaceholder;
import org.pptx4j.pml.CommonSlideData;
import org.pptx4j.pml.ObjectFactory;
import org.pptx4j.pml.Shape;
import org.pptx4j.pml.SldLayout;



public final class SlideLayoutPart extends JaxbPmlPart<SldLayout> {
	
	public SlideLayoutPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public SlideLayoutPart() throws InvalidFormatException {
		super(new PartName("/ppt/slideLayouts/slideLayout1.xml"));
		init();
	}
	
	public void init() {		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.PRESENTATIONML_SLIDE_LAYOUT));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.PRESENTATIONML_SLIDE_LAYOUT);
		
	}
	
	/* (non-Javadoc)
	 * @see org.docx4j.openpackaging.parts.JaxbXmlPart#setMceIgnorable(org.docx4j.jaxb.McIgnorableNamespaceDeclarator)
	 * 
	 * @since 3.3.0
	 */
	@Override
    protected void setMceIgnorable(McIgnorableNamespaceDeclarator namespacePrefixMapper) {
		
		/* Ensure we declare the namespace.  Otherwise Powerpoint 2010 treats the pptx as corrupt.
		 * 2013 and Mac version might repair it, at leat..
		 * 
            <mc:AlternateContent xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006">
              <mc:Choice xmlns:v="urn:schemas-microsoft-com:vml" Requires="v">
		 */
		
		
		// Unlike MainDocumentPart, there is no this.getJaxbElement().getIgnorable()
		
		namespacePrefixMapper.setMcIgnorable("v");
	}	
	
	SlideMasterPart master = null;
	public SlideMasterPart getSlideMasterPart() {
		
		if (master!=null) {
			return master;
		}
		
		Relationship masterRel = getRelationshipsPart().getRelationshipByType(
				Namespaces.PRESENTATIONML_SLIDE_MASTER);
		if (masterRel==null) {
			log.warn(this.getPartName().getName() + " has no master!");
		} else {
			master = (SlideMasterPart)getRelationshipsPart().getPart(masterRel);
		}
		return master;
	}
	
	public boolean setPartShortcut(Part part) {
		
		if (part == null ){
			return false;
		} else {
			return setPartShortcut(part, part.getRelationshipType() );
		}
	}	
		
	public boolean setPartShortcut(Part part, String relationshipType) {
		
		if (relationshipType==null) {
			log.warn("trying to set part shortcut against a null relationship type.");
			return false;
		}
		
		if (relationshipType.equals(Namespaces.PRESENTATIONML_SLIDE_MASTER)) {
			master = (SlideMasterPart)part;
			return true;			
						
		} else {	
			return false;
		}
	}
	
	
	private ResolvedLayout resolvedLayout;
	public ResolvedLayout getResolvedLayout() {
		if (resolvedLayout!=null) {
			return resolvedLayout;		
		}
		resolvedLayout = ResolvedLayout.resolveSlideLayout(this);
		return resolvedLayout;
	}	
	
	public static SldLayout createSldLayout() throws JAXBException {

		ObjectFactory factory = Context.getpmlObjectFactory(); 
		SldLayout sldLayout = factory.createSldLayout();
		
		CommonSlideData cSld = (CommonSlideData)XmlUtils.unmarshalString(COMMON_SLIDE_DATA, 
				Context.jcPML,
				CommonSlideData.class);
		cSld.setName("Title Slide");
		
		sldLayout.setCSld( cSld );
		return sldLayout;		
	}
	
	Map<String, ShapeWrapper> indexedPlaceHolders;
	public Map<String, ShapeWrapper> getIndexedPlaceHolders() {
		if (indexedPlaceHolders==null) {
			indexPlaceHolders();
		}
		return indexedPlaceHolders;
	}
	
	private Map<String, ShapeWrapper> indexPlaceHolders() {
		
		// All this for the 16 possible things defined in STPlaceholderType!
		
		indexedPlaceHolders = new HashMap<String, ShapeWrapper>();
		
//    	List<Object> possiblyShapes = getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame();
		
		
		// The placeholders are resolved against the master.
		List<Object> possiblyShapes = getResolvedLayout().getShapeTree().getSpOrGrpSpOrGraphicFrame();
		
    	
    	for (Object o : possiblyShapes) {
    		
    		if (o instanceof Shape) {
    			Shape sp = (Shape)o;
    			if (sp.getNvSpPr()!=null
    					&& sp.getNvSpPr().getNvPr()!=null
    						&& sp.getNvSpPr().getNvPr().getPh() != null) {
    				CTPlaceholder placeholder = sp.getNvSpPr().getNvPr().getPh();
    				ShapeWrapper sw = new ShapeWrapper(sp, placeholder.getType().toString(),
    						this);
    				indexedPlaceHolders.put(sw.getPhType(), sw);
    				
    				String name = "";
    				if (sp.getNvSpPr().getCNvPr()!=null) {
    					name = sp.getNvSpPr().getCNvPr().getName();
    				}
    				
    				log.debug("Indexed: " + sw.getPhType() + "(" + name + ") in " + sw.getOwner().getPartName().toString() );
    			}
    		}
    	}
	    return indexedPlaceHolders;
	}
	

}
