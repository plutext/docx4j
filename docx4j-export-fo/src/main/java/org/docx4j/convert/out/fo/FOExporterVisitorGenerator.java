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
import org.docx4j.convert.out.common.writer.AbstractBrWriter;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.images.WordXmlPictureE10;
import org.docx4j.model.images.WordXmlPictureE20;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.properties.paragraph.Indent;
import org.docx4j.model.properties.paragraph.Justification;
import org.docx4j.model.properties.paragraph.PBorderBottom;
import org.docx4j.model.properties.paragraph.PBorderTop;
import org.docx4j.model.properties.paragraph.PShading;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTTabStop;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.NumPr.Ilvl;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.STTabJc;
import org.docx4j.wml.STTabTlc;
import org.docx4j.wml.Style;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TrPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class FOExporterVisitorGenerator extends AbstractVisitorExporterGenerator<FOConversionContext>{
	
	private static Logger log = LoggerFactory.getLogger(FOExporterVisitorGenerator.class);
	
	
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
	protected void handleBr(Br br) {
		
		
		/* Is there a w:br immediately before this one?
		
	      If this is the first child of this w:r, and the w:r is preceded by another w:r, look at its last child
	
		  If this is not the first child of this w:r, look at the preceding sibling
*/
		
		boolean firstBr = true; // until proven otherwise
		
		R r = (R)br.getParent();
		int pos = getPos(r.getContent(), br);
		if (pos<0) {
			log.error("Couldn't locate w:br in w:r");
		}
		else if (pos==0) {
			// Need to look in preceding run
			Object rParent = r.getParent();
			// Handle just the case where this is w:p for now
			if(rParent instanceof P) {
				P parentP = (P)rParent;
				pos = getPos(parentP.getContent(), r);
				if (pos<0) {
					log.error("Couldn't locate w:r in w:p");
				} else if (pos>0) {
					Object beforeR = parentP.getContent().get(pos-1);
					if (beforeR instanceof R) {
						List list = ((R)beforeR).getContent();
						Object previous = list.get(list.size()-1);
						if (previous instanceof Br) {
							firstBr=false;
						}
					} else {
//						System.out.println(beforeR.getClass().getName());
						
					}
				}
			} else {
				log.info("TODO: handle run parent " + rParent.getClass().getName());
			}
		} else {
			Object previous = r.getContent().get(pos-1);
			if (previous instanceof Br) {
				firstBr=false;
			} else {
//				System.out.println("previous: " + previous.getClass().getName());
			}
		}
		
		if ((!firstBr) && 
				(br.getType()==null
				  || br.getType().equals(STBrType.TEXT_WRAPPING))) {
			
			// ie  a soft-return following another
			// 
			Element ret = createNode(document, NODE_BLOCK);
			// see http://stackoverflow.com/a/3664468/1031689 answer
			// at http://stackoverflow.com/questions/3661483/inserting-a-line-break-in-a-pdf-generated-from-xsl-fo-using-xslvalue-of
			ret.setAttribute("linefeed-treatment", "preserve");
			ret.setAttribute("white-space-treatment", "preserve");
			ret.setTextContent("\n");
			
			getCurrentParent().appendChild(ret); // should be spanEl
			
		} else {
			// Usual case
			convertToNode(conversionContext, 
					  br, AbstractBrWriter.WRITER_ID,
					  document, getCurrentParent() );
			
		}
		
		if ((br.getType()!=null
				  && br.getType().equals(STBrType.PAGE))) {
			currentSpan=null;			
		}
	}
	
	
	@Override
	protected void convertTabToNode(FOConversionContext conversionContext, Document document) throws DOMException {

		if (!conversionContext.isInComplexFieldDefinition()) {
						
	    	if (pPr!=null && pPr.getTabs()!=null) {
	    		
	    		// xsl:when test="count($p/w:pPr/w:tabs/w:tab[1][@w:leader='dot' and @w:val='right'])=1"
	    		CTTabStop tabStop = pPr.getTabs().getTab().get(0);
	    			    		
	    		if (tabStop!=null
	    				&& tabStop.getVal()!=null     // unlikely
	    				&& tabStop.getVal().equals(STTabJc.RIGHT)
	    				&& tabStop.getLeader()!=null  // more likely
	    				&& tabStop.getLeader().equals(STTabTlc.DOT) ) {
	    			
					// <fo:leader leader-length.minimum="12pt" leader-length.optimum="40pt"
					//		    leader-length.maximum="100%" leader-pattern="dots">
	    			Element foLeader = document.createElementNS(XSL_FO, "leader");	    			
	    			foLeader.setAttribute("leader-length.minimum",  "12pt");
	    			foLeader.setAttribute("leader-length.maximum",  "100%");
	    			foLeader.setAttribute("leader-length.optimum",  "40pt");
	    			foLeader.setAttribute("leader-pattern",  "dots");
	    			
	    			getCurrentParent().appendChild(foLeader);	    			
	    			
	    		} else {
	    			getCurrentParent().appendChild(document.createTextNode(TAB_DUMMY));	    			
	    		}
	    	}
	    	else {
    			getCurrentParent().appendChild(document.createTextNode(TAB_DUMMY));	    			
    		}	    	
			
		}
	}
	
	
    @Override
	protected Element handlePPr(FOConversionContext conversionContext, PPr pPrDirect, boolean sdt, 
			Element currentParent) {
    	Element ret = currentParent;

    	PropertyResolver propertyResolver = conversionContext.getPropertyResolver();
    	

    	String defaultParagraphStyleId = "Normal";
    	if (conversionContext.getWmlPackage().getMainDocumentPart().getStyleDefinitionsPart(false) != null) {
        	Style defaultParagraphStyle = 
        			conversionContext.getWmlPackage().getMainDocumentPart().getStyleDefinitionsPart(false).getDefaultParagraphStyle();
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
        	
//        	PPr pPr = null;
//        	RPr rPr = null;
			pPr = propertyResolver.getEffectivePPr(pPrDirect);  

			getLog().debug("getting rPr for paragraph style");    				
			rPr = propertyResolver.getEffectiveRPr(null, pPrDirect); 
			// rPr in pPr direct formatting only applies to paragraph mark, 
			// and by virtue of that, to list item label,
			// so pass null here
			
			// Now, work out the value for list item label
			RPr rPrParagraphMark = XmlUtils.deepCopy(rPr);
	//		System.out.println("p rpr-->" + XmlUtils.marshaltoString(pPrDirect.getRPr()));
			if (pPrDirect!=null) {
				StyleUtil.apply(pPrDirect.getRPr(), rPrParagraphMark);				
			}
			
			if (getLog().isDebugEnabled() && pPr!=null) {				
				getLog().debug(XmlUtils.marshaltoString(pPr, true, true));					
			}
        							
			boolean inlist = false;
			boolean indentHandledByNumbering = false;
			
			
			Element foBlockElement = null;
			Element foListBlock = null;
			
//			if (pPr!=null && pPr.getNumPr()!=null ) {
			if (pPr!=null 
					&& pPr.getNumPr()!=null 
					&& pPr.getNumPr().getNumId()!=null
					&& pPr.getNumPr().getNumId().getVal().longValue()!=0 //zero means no numbering
					) {
				
				inlist = true;
				
				// Its a list item.  At present we make a new list-block for
				// each list-item. This is not great; DocumentModel will ultimately
				// allow us to use fo:list-block properly.

				foListBlock = document.createElementNS(XSL_FO, 
						"list-block");
				currentParent.appendChild(foListBlock);
					// That's different to XSL.
					// Here we end up with block/list-block
					// cf XSL, where the logic avoids creating 2 elements.
					// May be easy to fix, if instead of passing in
					// currentParent, we return block or list-block
					// from here, and appendChild in the calling code.
								
//				foListBlock.setAttribute("provisional-distance-between-starts", "0.5in");
				
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
				
				Element foListItemBody = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
						"fo:list-item-body");
				foListItem.appendChild(foListItemBody);	
				foListItemBody.setAttribute(Indent.FO_NAME, "body-start()");				
				
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
	        		
	        		/* Format the list item label
	        		 * 
	        		 * Since it turns out (in FOP at least) that the label and the body 
	        		 * don't have the same vertical alignment 
	        		 * unless font size is applied at the same level
	        		 * (ie to both -label and -body, or to the block inside each), 
	        		 * we have to format the list-item-body as well.
	        		 * This issue only manifests itself if the font size on
	        		 * the outer list-block is larger than the font sizes
	        		 * set inside it.
	        		 */

	        		// OK just to override specific values
        			// Values come from numbering rPr, unless overridden in p-level rpr
	        		if(triple.getRPr()==null) {
	        			
	        			if (pPr.getRPr()==null) {
	        				// do nothing, since we're already inheriting the formatting in the style
	        				// (as opposed to the paragraph mark formatting)
	        				// EXCEPT for font
	        				XsltFOFunctions.setFont( conversionContext.getRunFontSelector(),  foListItemLabelBody, pPr, rPr,  triple.getNumString());        				
	        			} else {

	        				createFoAttributes(conversionContext.getWmlPackage(), rPrParagraphMark, foListItemLabel );	        				
	        				createFoAttributes(conversionContext.getWmlPackage(), rPrParagraphMark, foListItemBody );
	        				
							XsltFOFunctions.setFont( conversionContext.getRunFontSelector(),  foListItemLabelBody, pPr, rPrParagraphMark,  triple.getNumString());	        				
	        			}
	        			
	        		} else {
	        			RPr actual = XmlUtils.deepCopy(triple.getRPr()); // clone, so the ilvl rpr is not altered
//	        			System.out.println(XmlUtils.marshaltoString(rPrParagraphMark));
	        			
	        			// pMark overrides numbering, except for font
	        			// (which makes sense, since that would change the bullet)
	        			// so set the font
	        			XsltFOFunctions.setFont( conversionContext.getRunFontSelector(),  foListItemLabelBody,  pPr,  actual,  triple.getNumString());
        				// .. before taking rPrParagraphMark into account
	            		StyleUtil.apply(rPrParagraphMark, actual); 
//	        			System.out.println(XmlUtils.marshaltoString(actual));
	            		
						createFoAttributes(conversionContext.getWmlPackage(), actual, foListItemLabel );
						createFoAttributes(conversionContext.getWmlPackage(), actual, foListItemBody );	        			
	        		}	        		

/*	        		
	        		// Indent (in combination with provisional-distance-between-starts
	        		// above
	        		if (triple.getIndent()!=null) {
	        			Indent indent = new Indent(triple.getIndent());
	    				//foListBlock.setAttribute(Indent.FO_NAME, "2in");
	    				indent.setXslFO(foListBlock);
	        		}
	        		
	*/      
	        	}

    			int numChars=1;	        		
        		if (triple.getBullet()!=null ) {
	        		foListItemLabelBody.setTextContent(triple.getBullet() );
	        	} else if (triple.getNumString()==null) {
	        		getLog().warn("computed NumString was null!");
	        		if (getLog().isDebugEnabled() ) {
	        			foListItemLabelBody.setTextContent("nns");
	        		} 
	        		numChars=0;		        		
		    	} else {
					Text number = document.createTextNode( triple.getNumString() );
					foListItemLabelBody.appendChild(number);
					numChars = triple.getNumString().length();						
		    	}
				
        		// Indent (setting provisional-distance-between-starts)
        		// Indent on direct pPr trumps indent in pPr in numbering, which trumps indent
    			// specified in a style.  Well, not exactly, components which aren't set in
        		// the direct formatting will be contributed by the numbering's indent settings
        		Indent indent = new Indent(pPrDirect.getInd(), triple.getIndent());
        		if (indent.isHanging() ) {
    				indent.setXslFOListBlock(foListBlock, -1);	        			
        		} else {
        			
        			int numWidth = 90 * numChars; // crude .. TODO take font size into account
        			
        		    int pdbs = XsltFOFunctions.getDistanceToNextTabStop(indent.getNumberPosition(), numWidth,
        		    		pPrDirect.getTabs(), conversionContext.getWmlPackage().getMainDocumentPart().getDocumentSettingsPart());
    				indent.setXslFOListBlock(foListBlock, pdbs);	        				        			
        		}
				indentHandledByNumbering = true; 				
								
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
//				createFoAttributes(conversionContext.getWmlPackage(), rPr, currentParent );
				
				if (foListBlock==null) {
					createFoAttributes(conversionContext.getWmlPackage(), rPr, currentParent );
				} else {
					createFoAttributes(conversionContext.getWmlPackage(), rPr, ((Element)foListBlock) );					
				}
				
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
    	
    	if (pPr==null) return;
		
    	// Special case, since bidi is translated to align right
    	// Handle interaction between w:pPr/w:bidi and w:pPr/w:jc/@w:val='right'
    	if (pPr.getBidi()!=null && pPr.getBidi().isVal()) {
    		
    		if (pPr.getJc()!=null) {
    			if (pPr.getJc().getVal().equals(JcEnumeration.RIGHT)) {
    				// set it to left!
    				foBlockElement.setAttribute(Justification.FO_NAME,  "left");
    			} else if (pPr.getJc().getVal().equals(JcEnumeration.LEFT)) {
    				// set it to right!
    				foBlockElement.setAttribute(Justification.FO_NAME,  "right");
    			}
    		}
    	}   
    	
    	// Table of contents dot leader needs text-align-last="justify"
    	// Are we in a TOC?
    	if (pPr.getTabs()!=null
    			
    			// PStyle is not included in our effective pPr!
//    			&& pPr.getPStyle()!=null 
//    			&& pPr.getPStyle().getVal()!=null
//    			&& pPr.getPStyle().getVal().startsWith("TOC")  
    			) {
    		
    		CTTabStop tabStop = pPr.getTabs().getTab().get(0);
    		if (tabStop!=null
    				//&& tabStop.getLeader().equals(STTabTlc.DOT)
    				&& tabStop.getVal().equals(STTabJc.RIGHT) ) {
    			
    			foBlockElement.setAttribute("text-align-last",  "justify");
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

	@Override
	protected void rtlAwareAppendChildToCurrentP(Element spanEl) {
		
    	// Arabic (and presumably Hebrew) fix
    	// If we have inline direction="rtl" (created by TextDirection class)
    	// wrap the inline with:
    	//    <bidi-override direction="rtl" unicode-bidi="embed">
		/* See further:
			From: Glenn Adams <glenn@skynav.com>
			Date: Fri, Mar 21, 2014 at 8:41 AM
			Subject: Re: right align arabic in table-cell
			To: FOP Users <fop-users@xmlgraphics.apache.org>
		 */						

		if (rPr!=null 
				&& rPr.getRtl()!=null
			&& rPr.getRtl().isVal())  {
			
			spanEl.removeAttribute("direction");
    		
    		Element bidiOverride = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
					"fo:bidi-override");
        	bidiOverride.setAttribute("unicode-bidi", "embed" );
        	bidiOverride.setAttribute("direction", "rtl" );    		
    		
        	bidiOverride.appendChild(spanEl);

			currentP.appendChild( bidiOverride  );
        	
		} else {
			// Usual case
			currentP.appendChild( spanEl  );
		}
		
	}
}
