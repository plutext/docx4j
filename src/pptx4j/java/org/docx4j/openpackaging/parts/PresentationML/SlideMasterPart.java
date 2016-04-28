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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTColorMapping;
import org.docx4j.jaxb.McIgnorableNamespaceDeclarator;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
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
import org.pptx4j.pml.SldMaster;
import org.pptx4j.pml.SlideLayoutIdList;
import org.pptx4j.pml.SlideLayoutIdList.SldLayoutId;



public final class SlideMasterPart extends JaxbPmlPart<SldMaster> {
	
	public SlideMasterPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public SlideMasterPart() throws InvalidFormatException {
		super(new PartName("/ppt/slideMasters/slideMaster1.xml"));
		init();
	}
	
	public void init() {		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.PRESENTATIONML_SLIDE_MASTER));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.PRESENTATIONML_SLIDE_MASTER);
		
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
	
	private ResolvedLayout resolvedLayout;
	public ResolvedLayout getResolvedLayout() {
		if (resolvedLayout!=null) {
			return resolvedLayout;		
		}
		resolvedLayout = new ResolvedLayout();
		resolvedLayout.setBg( getJaxbElement().getCSld().getBg() );
		
		// ShapeTree
		resolvedLayout.setShapeTree( 
					getJaxbElement().getCSld().getSpTree() );
		
		return resolvedLayout;
	}
	
	public static SldMaster createSldMaster() throws JAXBException {

		ObjectFactory factory = Context.getpmlObjectFactory(); 
		SldMaster sldMaster = factory.createSldMaster();
		sldMaster.setCSld( 
				(CommonSlideData)XmlUtils.unmarshalString(COMMON_SLIDE_DATA, Context.jcPML,
						CommonSlideData.class) );
		sldMaster.setClrMap(
				(CTColorMapping)XmlUtils.unmarshalString(COLOR_MAPPING, Context.jcPML, CTColorMapping.class ) );
		
		SlideLayoutIdList slideLayoutIdList = factory.createSlideLayoutIdList();
		sldMaster.setSldLayoutIdLst(slideLayoutIdList);
		
		return sldMaster;		
	}
		
	public SldLayoutId addSlideLayoutIdListEntry(SlideLayoutPart slideLayoutPart) 
		throws InvalidFormatException {
		
		Relationship rel = this.addTargetPart(slideLayoutPart);
		
		SldLayoutId entry = Context.getpmlObjectFactory().createSlideLayoutIdListSldLayoutId();
		
		entry.setId( new Long(this.getSlideLayoutOrMasterId()) );
		entry.setRid(rel.getId());
		
		this.getJaxbElement().getSldLayoutIdLst().getSldLayoutId().add(entry);
		
		return entry;
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
		
    	List<Object> possiblyShapes = getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame();
    	    	
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
    				log.debug("Indexed: " + sw.getPhType() + " in " + sw.getOwner().getPartName().toString() );
    			}
    		}
    	}
	    return indexedPlaceHolders;
	}
	
	private static final String VML_DECL = "xmlns:v=\"urn:schemas-microsoft-com:vml\"";
	
    /**
	 * Marshal the content tree rooted at <tt>jaxbElement</tt> into an output
	 * stream
	 * 
	 * @param os
	 *            XML will be added to this stream.
	 * @param namespacePrefixMapper
	 *            namespacePrefixMapper
	 * 
	 * @throws JAXBException
	 *             If any unexpected problem occurs during the marshalling.
	 */
	@Override
    public void marshal(java.io.OutputStream os, Object namespacePrefixMapper) throws JAXBException {

		// Add xmlns:v="urn:schemas-microsoft-com:vml" eg in
        // <mc:AlternateContent xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006">
        // <mc:Choice xmlns:v="urn:schemas-microsoft-com:vml" Requires="v">		
		// How?  Could marshall to a DOM doc, but there is no way to force the xmlns to be included
		// where it is not required.  Well, JAXB namespace prefix mapping stuff promises a way, but it is buggy.
		// So do string manipulation
    	
		// @since 3.2.1
		
		String xmlString = XmlUtils.marshaltoString( getJaxbElement(), false, true, jc ); 
			// include the XML declaration; it'll be UTF-8
		int pos = xmlString.indexOf(":sldMaster "); //11
		int closeTagPos = xmlString.indexOf(">", pos);
		if (xmlString.substring(pos, closeTagPos).contains(VML_DECL)) {
			// nothing to do; vml namespace is already declared
		} else {
			xmlString = xmlString.substring(0, pos + 11 ) +  VML_DECL + " " + xmlString.substring(pos + 11 );
		}
		
		try {
			IOUtils.write(xmlString, os, "UTF-8"); // be sure to write UTF-8 irrespective of default encoding
			/* FIX confirmed by running a presentation containing eg mög
			 * through RoundTripTest, 
			 * with run configuration setting -Dfile.encoding=ISO-8859-1,
			 * verified Powerpoint (2010) can open it.
			 */
		} catch (IOException e) {
			throw new JAXBException(e.getMessage(), e);
		}
			
	}	
}
