/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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
package org.docx4j.convert.out.fo;

import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.AbstractVisitorExporterDelegate;
import org.docx4j.convert.out.common.AbstractVisitorExporterDelegate.AbstractVisitorExporterGeneratorFactory;
import org.docx4j.convert.out.common.AbstractVisitorExporterGenerator;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.images.WordXmlPictureE10;
import org.docx4j.model.images.WordXmlPictureE20;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.properties.paragraph.Indent;
import org.docx4j.model.properties.paragraph.PBorderBottom;
import org.docx4j.model.properties.paragraph.PBorderTop;
import org.docx4j.model.properties.paragraph.PShading;
import org.docx4j.model.properties.run.Font;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.NumPr.Ilvl;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TrPr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class FOExporterVisitorGenerator extends AbstractVisitorExporterGenerator<FOConversionContext>{
	private static String XSL_FO = "http://www.w3.org/1999/XSL/Format";

	public static final AbstractVisitorExporterDelegate.AbstractVisitorExporterGeneratorFactory<FOConversionContext> GENERATOR_FACTORY = 
			new AbstractVisitorExporterGeneratorFactory<FOConversionContext>() {
				@Override
				public AbstractVisitorExporterGenerator<FOConversionContext> createInstance(
						FOConversionContext conversionContext,
						Document document, Node parentNode) {
					return new FOExporterVisitorGenerator(conversionContext, document, parentNode);
				}
			};
			
	
	private FOExporterVisitorGenerator(FOConversionContext conversionContext, Document document, Node parentNode) {
		super(conversionContext, document, parentNode);
	}

	@Override
	protected AbstractVisitorExporterGeneratorFactory<FOConversionContext> getFactory() {
		return GENERATOR_FACTORY;
	}

	@Override
	protected DocumentFragment createImage(int imgType, FOConversionContext conversionContext, Object anchorOrInline) {
			switch (imgType) {
			case IMAGE_E10:
				return WordXmlPictureE10.createXslFoImgE10(conversionContext, anchorOrInline);
			case IMAGE_E20:
				return WordXmlPictureE20.createXslFoImgE20(conversionContext, anchorOrInline);
			}
		return null;
	}
    
	@Override
	protected Element createNode(Document doc, int nodeType) {
		switch (nodeType) {
			case NODE_BLOCK:
				return document.createElementNS(XSL_FO, "block");
			case NODE_INLINE:
				return document.createElementNS(XSL_FO, "inline");
		}
		return null;
	}
	
    @Override
	protected Element handlePPr(FOConversionContext conversionContext, PPr pPrDirect, boolean sdt, Element currentParent) {
    	Element ret = currentParent;

    	PropertyResolver propertyResolver = conversionContext.getPropertyResolver();
    	

    	String defaultParagraphStyleId = "Normal";
    	if (conversionContext.getWmlPackage().getMainDocumentPart().getStyleDefinitionsPart() != null) {
        	Style defaultParagraphStyle = 
        			conversionContext.getWmlPackage().getMainDocumentPart().getStyleDefinitionsPart().getDefaultParagraphStyle();
        	if (defaultParagraphStyle != null) {
        		defaultParagraphStyleId = defaultParagraphStyle.getStyleId();
        	}
    	}
    	
    	String pStyleVal = null;
    	if (pPrDirect!=null && pPrDirect.getPStyle()!=null) {
    		pStyleVal = pPrDirect.getPStyle().getVal();
    	} else {
			pStyleVal = defaultParagraphStyleId;
		}
    	getLog().debug("style '" + pStyleVal );     		

        try {
        	
        	PPr pPr = null;
        	RPr rPr = null;
			pPr = propertyResolver.getEffectivePPr(pPrDirect);  

			getLog().debug("getting rPr for paragraph style");    				
			rPr = propertyResolver.getEffectiveRPr(null, pPrDirect); 
				// rPr in pPr direct formatting only applies to paragraph mark, 
				// so pass null here       				

			if (getLog().isDebugEnabled() && pPr!=null) {				
				getLog().debug(XmlUtils.marshaltoString(pPr, true, true));					
			}
        							
			boolean inlist = false;
			
			Element foBlockElement;
			if (pPr!=null && pPr.getNumPr()!=null ) {
				
				inlist = true;
				
				// Its a list item.  At present we make a new list-block for
				// each list-item. This is not great; DocumentModel will ultimately
				// allow us to use fo:list-block properly.

				Element foListBlock = document.createElementNS(XSL_FO, 
						"list-block");
				currentParent.appendChild(foListBlock);
								
				foListBlock.setAttribute("provisional-distance-between-starts", "0.5in");
				
				// Need to apply shading at fo:list-block level
				if (pPr.getShd()!=null) {
					PShading pShading = new PShading(pPr.getShd());
					pShading.setXslFO(foListBlock);
				}
				
				Element foListItem = document.createElementNS(XSL_FO, 
						"list-item");
				foListBlock.appendChild(foListItem);				

				
				Element foListItemLabel = document.createElementNS(XSL_FO, 
						"list-item-label");
				foListItem.appendChild(foListItemLabel);
				
				Element foListItemLabelBody = document.createElementNS(XSL_FO, 
						"block");
				foListItemLabel.appendChild(foListItemLabelBody);
				
	        	ResultTriple triple;
	        	if (pPrDirect!=null && pPrDirect.getNumPr()!=null) {
	        		triple = org.docx4j.model.listnumbering.Emulator.getNumber(
	        			conversionContext.getWmlPackage(), pStyleVal, 
	        			pPrDirect.getNumPr().getNumId().getVal().toString(), 
	        			pPrDirect.getNumPr().getIlvl().getVal().toString() ); 
	        	} else {
	        		// Get the effective values; since we already know this,
	        		// save the effort of doing this again in Emulator
	        		Ilvl ilvl = pPr.getNumPr().getIlvl();
	        		String ilvlString = ilvl == null ? "0" : ilvl.getVal().toString();
	        		triple = null; 
	        		if (pPr.getNumPr().getNumId()!=null) {
		        		triple = org.docx4j.model.listnumbering.Emulator.getNumber(
		        				conversionContext.getWmlPackage(), pStyleVal, 
			        			pPr.getNumPr().getNumId().getVal().toString(), 
			        			ilvlString ); 		        	
	        		}
	        	}
				
				if (triple==null) {
					getLog().warn("computed number ResultTriple was null");
	        		if (getLog().isDebugEnabled() ) {
	        			foListItemLabelBody.setTextContent("nrt");
	        		} 
	        	} else {
	        		
	        		// Indent (in combination with provisional-distance-between-starts
	        		// above
	        		if (triple.getIndent()!=null) {
	        			Indent indent = new Indent(triple.getIndent());
	    				//foListBlock.setAttribute(Indent.FO_NAME, "2in");
	    				indent.setXslFO(foListBlock);
	        		}
	        		
	        		// Set the font
	        		if (triple.getNumFont()!=null) {
	        			String font = Font.getPhysicalFont(conversionContext.getWmlPackage(), triple.getNumFont() );
	        			if (font!=null) {
	        				foListItemLabelBody.setAttribute(Font.FO_NAME, font );
	        			}
	        		}
	        		
	        		if (triple.getBullet()!=null ) {
		        		foListItemLabelBody.setTextContent(triple.getBullet() );
		        	} else if (triple.getNumString()==null) {
		        		getLog().warn("computed NumString was null!");
		        		if (getLog().isDebugEnabled() ) {
		        			foListItemLabelBody.setTextContent("nns");
		        		} 
			    	} else {
						Text number = document.createTextNode( triple.getNumString() );
						foListItemLabelBody.appendChild(number);		    		
			    	}
	        	}
				
				Element foListItemBody = document.createElementNS(XSL_FO, 
						"list-item-body");
				foListItem.appendChild(foListItemBody);	

				foListItemBody.setAttribute(Indent.FO_NAME, "body-start()");
				
				foBlockElement = document.createElementNS(XSL_FO, 
						"block");
				foListItemBody.appendChild(foBlockElement);
				//If we have list items the parent for spans changes (currentP)
				ret = foBlockElement;
				
			} else {

				// Do nothing 
			}
			
							
			if (pPr!=null) {
				// Ignore paragraph borders once inside the container
				boolean ignoreBorders = !sdt;

				createFoAttributes(conversionContext, pPr, currentParent, inlist, ignoreBorders );
			}
			
			if (rPr!=null) {											
				createFoAttributes(conversionContext.getWmlPackage(), rPr, currentParent );
	        }
        
						
		} catch (Exception e) {
			getLog().error(e.getMessage(), e);
		} 
        
        return ret;
    }

	protected void createFoAttributes(FOConversionContext conversionContext, PPr pPr, Element foBlockElement, boolean inList, boolean ignoreBorders){
		
    	List<Property> properties = PropertyFactory.createProperties(conversionContext.getWmlPackage(), pPr);
    	
    	for( Property p :  properties ) {
			if (p!=null) {
				
				if (ignoreBorders &&
						((p instanceof PBorderTop)
								|| (p instanceof PBorderBottom))) {
					continue;
				}
								
				if (inList && !(p instanceof Indent) ) { 
					// Don't set start-indent in 
					// fo:list-item-body/fo:block.
					// This has to be handled above using something like 
					//  <fo:list-block provisional-distance-between-starts="0.5in" start-indent="2in">
					p.setXslFO(foBlockElement);
				} else if (!inList) {
					p.setXslFO(foBlockElement);
				}
			}
    	}
		
    	
    	
	}
	
	/*
	 *  @since 3.0.0
	 */
	protected static void applyFoAttributes(List<Property> properties, Element foElement) {
		if ((properties != null) && (!properties.isEmpty())) {
			for (int i=0; i<properties.size(); i++) {
				properties.get(i).setXslFO(foElement);
			}
		}
	}
	
    protected static void createFoAttributes(TrPr trPr, Element foBlockElement){
    	if (trPr == null) {
    		return;
    	}
    	applyFoAttributes(PropertyFactory.createProperties(trPr), foBlockElement);
    }
	
    protected static void createFoAttributes(TcPr tcPr, Element foBlockElement){
    	// includes TcPrInner.TcBorders, CTShd, TcMar, CTVerticalJc
    	
		if (tcPr==null) {
			return;
		}
    	applyFoAttributes(PropertyFactory.createProperties(tcPr), foBlockElement);
    }
	

    
    /**
     * On a block representing a run, we just put run properties
     * from this rPr node. The paragraph style rPr's have been
     * taken care of on the fo block which represents the paragraph.
     * 
     * @param wmlPackage
     * @param rPrNodeIt
     * @param childResults
     * @return
     */
    @Override
	protected void handleRPr(
    		FOConversionContext conversionContext,
    		PPr pPrDirect,
    		RPr rPrDirect, Element currentParent ) {

    	PropertyResolver propertyResolver = conversionContext.getPropertyResolver();
    	
    	
        try {
        	RPr rPr = propertyResolver.getEffectiveRPr(rPrDirect, pPrDirect);
        	
				
			if (getLog().isDebugEnabled() && rPr!=null) {					
				getLog().debug(XmlUtils.marshaltoString(rPr, true, true));					
			}
			
			//if (rPr!=null) {				
				createFoAttributes(conversionContext.getWmlPackage(), rPr, ((Element)currentParent) );
			//}
			
						
		} catch (Exception e) {
			getLog().error(e.getMessage(), e);
		} 
    	
    }

	protected static void createFoAttributes(WordprocessingMLPackage wmlPackage,
			RPr rPr, Element foInlineElement){

    	List<Property> properties = PropertyFactory.createProperties(wmlPackage, rPr);
    	
    	for( Property p :  properties ) {
    		p.setXslFO(foInlineElement);
    	}
		
	}
}
