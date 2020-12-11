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

package org.pptx4j.model;

import java.util.List;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextListStyle;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideMasterPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.pptx4j.jaxb.Context;
import org.pptx4j.pml.CTBackground;
import org.pptx4j.pml.CTPlaceholder;
import org.pptx4j.pml.GroupShape;
import org.pptx4j.pml.Shape;
import org.pptx4j.pml.Shape.NvSpPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The idea of this class is that it provides 
 * an effective slide layout.  
 * 
 * It can either represent:
 * 
 *  - a slide layout (resolved so that no further reference to
 *    the slide master is required), or
 *    
 *  - a slide (resolved so that no further reference to a
 *    slide layout is required)
 * 
 * It doesn't say anything about text fonts/styles, since
 * these are handled in TextStyles.
 * 
 * @author jharrop
 *
 */
public class ResolvedLayout implements Cloneable {
	
	protected static Logger log = LoggerFactory.getLogger(ResolvedLayout.class);
	
	private CTBackground bg;
	/**
	 * @return the bg
	 */
	public CTBackground getBg() {
		return bg;
	}
	/**
	 * @param bg the bg to set
	 */
	public void setBg(CTBackground bg) {
		this.bg = bg;
	}

	private GroupShape shapeTree;
	/**
	 * @return the shapeTree
	 */
	public GroupShape getShapeTree() {
		return shapeTree;
	}
	/**
	 * @param shapeTree the shapeTree to set
	 */
	public void setShapeTree(GroupShape shapeTree) {
		this.shapeTree = shapeTree;
	}
	
	// Record which master this layout uses
	private int masterNumber;
	public int getMasterNumber() {
		return masterNumber;
	}
		
	/**
	 * Create a ResolvedLayout for a SlideLayoutPart.
	 * @param slideLayoutPart
	 * @return
	 */
	public static ResolvedLayout resolveSlideLayout(SlideLayoutPart slideLayoutPart)  {

		ResolvedLayout resolvedLayout = new ResolvedLayout();
		
		// Get the master layout
		SlideMasterPart master = slideLayoutPart.getSlideMasterPart();
		ResolvedLayout masterLayout = master.getResolvedLayout();		
		Map<String, ShapeWrapper> masterPlaceholders = master.getIndexedPlaceHolders();
		
		// Assume the slide layout doesn't itself have a bg
		if (masterLayout.getBg() != null) {
		    resolvedLayout.bg = 
		        XmlUtils.deepCopy(
					masterLayout.getBg(),
					Context.jcPML);
		}
		
		// ShapeTree
		resolvedLayout.shapeTree = 
			createEffectiveShapeTree( slideLayoutPart.getJaxbElement().getCSld().getSpTree(),  
					 masterPlaceholders );
		
		resolvedLayout.masterNumber = 1; // TODO FIXME
		
		return resolvedLayout;
	}

	public RelationshipsPart relationships;
	
	/**
	 * Create a ResolvedLayout for a SlidePart.
	 * @param slideLayoutPart
	 * @return
	 */
	public static ResolvedLayout resolveSlideLayout(SlidePart slidePart) {

		ResolvedLayout resolvedLayout = new ResolvedLayout();
		
		// Does this slide reference a layout explicitly?
		// .. sometimes it doesn't.
		SlideLayoutPart layoutPart = null; 
		
		// Need these for images etc
		resolvedLayout.relationships = slidePart.getRelationshipsPart();		
		
		Relationship rel = slidePart.getRelationshipsPart().getRelationshipByType(
				Namespaces.PRESENTATIONML_SLIDE_LAYOUT);
		if (rel==null) {
			log.warn(slidePart.getPartName().getName() + " has no explicit layout!");
			// This happens at least some of the time.
			// eg for title slide 
			// That case is ok, since CTR_TITLE and SUB_TITLE only occur
			// once across layouts. But others aren't unique;
			// they can be partially disambiguated using p:cNvPr/@name
			
			Map<String, ShapeWrapper> globalPlaceHolders = ((PresentationMLPackage)slidePart.getPackage())
				.getPlaceHoldersFromAcrossLayouts();
			// These are resolved placeholders :-)
			
			// ShapeTree
			resolvedLayout.shapeTree = 
				createEffectiveShapeTree( slidePart.getJaxbElement().getCSld().getSpTree(),  
						globalPlaceHolders );			
			
		} else {
			// Usual case 
			layoutPart = (SlideLayoutPart)slidePart.getRelationshipsPart().getPart(rel);
			
			// ShapeTree
			resolvedLayout.shapeTree = 
				createEffectiveShapeTree( slidePart.getJaxbElement().getCSld().getSpTree(),  
						layoutPart.getIndexedPlaceHolders() );			
		}

		resolvedLayout.masterNumber = 1; // TODO FIXME
		
		return resolvedLayout;
	}
	
	
	
	/**
	 * Resolve references to Slide Layouts, Master etc, 
	 * in order to get the actual shapes we'll render.
	 * @return
	 */
	public static GroupShape createEffectiveShapeTree(GroupShape shapeTree, Map<String, ShapeWrapper> placeholders ) {
		
		// Start with a clone
		GroupShape effectiveShapeTree
			=  XmlUtils.deepCopy( shapeTree,
					Context.jcPML );
		
		// Now handle the placeholders
    	List<Object> possiblyShapes = effectiveShapeTree.getSpOrGrpSpOrGraphicFrame();    	
    	for (Object o : possiblyShapes) {
    		
    		if (o instanceof Shape) {
    			Shape sp = (Shape)o;
    			if (sp.getNvSpPr()!=null
    					&& sp.getNvSpPr().getNvPr()!=null
    						&& sp.getNvSpPr().getNvPr().getPh() != null) {
    				CTPlaceholder placeholder = sp.getNvSpPr().getNvPr().getPh();
					String placeholderType = placeholder.getType().toString();
    				log.info("Handling placeholder: " + placeholderType );
    				handle(placeholders, placeholderType, sp);
    			}
    		}
    	}
		
		return effectiveShapeTree;
	}
	
	private static void handle(Map<String, ShapeWrapper> placeholders, String placeholderType, Shape sp) {
		Shape layoutShape = null;
		if (placeholders.get(placeholderType)!=null) {
			log.debug("Got it..");
			layoutShape = placeholders.get(placeholderType).getSp();
		} else {
			log.debug("Missing..");
			return;
		}
		
		handleNvSpPr( sp.getNvSpPr(), layoutShape.getNvSpPr());
		handleSpPr( sp.getSpPr(), layoutShape.getSpPr());
		handleTxBody( sp.getTxBody(), layoutShape.getTxBody() ); 
		
		// TODO: other fields
	}
	
	private static void handleNvSpPr(NvSpPr sp, NvSpPr layoutShape) {
		
		// Get rid of p:nvPr/p:ph
		// No, don't do that; we need to know for rendering text properly whether its a
		// title or body elememt
		// sp.getNvPr().setPh(null);		
	}

	private static void handleSpPr(CTShapeProperties sp, CTShapeProperties layoutShape) {
		
		// a:xrm
		if (sp.getXfrm()==null && layoutShape.getXfrm()!=null) {
			sp.setXfrm( XmlUtils.deepCopy(layoutShape.getXfrm(), Context.jcPML ));
		}
	}
	
	private static void handleTxBody(CTTextBody sp, CTTextBody layoutShape) {
		
		// a slide layout p:sp/p:txBody can override, with a <a:lstStyle> element
		// so copy a:lstStyle over
		if (sp.getLstStyle()!=null) {
			log.warn("Slide shape contains lstStyle! (Not expected at that level)");
			log.debug(XmlUtils.marshaltoString(sp.getLstStyle(), false, true, Context.jcPML,
					"FIXME", "lstStyle", CTTextListStyle.class));
		}		
		if (layoutShape.getLstStyle()!=null) {
			sp.setLstStyle( XmlUtils.deepCopy(layoutShape.getLstStyle(), Context.jcPML ));			
		}
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ResolvedLayout clone() throws CloneNotSupportedException {
		ResolvedLayout clone = (ResolvedLayout)super.clone();
		if (this.bg!=null) {
			clone.bg = 
				XmlUtils.deepCopy(
						this.bg,
						Context.jcPML);
		}
		if (this.shapeTree!=null) {
			clone.shapeTree = 
				XmlUtils.deepCopy(
						this.shapeTree,
						Context.jcPML);
		}
		return clone;
	}

	

}
