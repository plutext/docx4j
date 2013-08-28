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

import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.preprocess.Containerization;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.properties.paragraph.Indent;
import org.docx4j.model.properties.paragraph.PBorderBottom;
import org.docx4j.model.properties.paragraph.PBorderTop;
import org.docx4j.model.properties.paragraph.PShading;
import org.docx4j.model.properties.run.Font;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTTabStop;
import org.docx4j.wml.CTTwipsMeasure;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tabs;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TrPr;
import org.docx4j.wml.PPrBase.NumPr.Ilvl;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.NodeIterator;


/** 
 * This class contains static functions that are specific to the FO xsl-transformation and 
 * are called from docx2fo.xslt. 
 *  
 */
public class XsltFOFunctions {

	public static DocumentFragment getLayoutMasterSetFragment(AbstractWmlConversionContext context) {
		return LayoutMasterSetBuilder.getLayoutMasterSetFragment(context);
	}
	
    public static DocumentFragment createBlockForSdt(FOConversionContext context, 
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults, String tag) {
    	
    	DocumentFragment docfrag = createBlock(context,
        		 pPrNodeIt,
        		 pStyleVal,  childResults,
        		 true);
    	
    	// Set margins, but only for a shading container,
    	// not a borders container
    	if (tag.equals(Containerization.TAG_SHADING) && docfrag!=null) {
    		// docfrag.getNodeName() is  #document-fragment
    	    Node foBlock = docfrag.getFirstChild();
    	    if (foBlock!=null) {
				((Element)foBlock).setAttribute("margin-top", "0in");    	    	
				((Element)foBlock).setAttribute("margin-bottom", "0in");    	    	

//				((Element)foBlock).setAttribute("padding-top", "0in");    	    	
//				((Element)foBlock).setAttribute("padding-bottom", "0in");    	    	
    	    }
    	}
    	    
    	return docfrag;
    }	

    public static DocumentFragment createInlineForSdt( 
    		FOConversionContext context,
    		NodeIterator rPrNodeIt,
    		NodeIterator childResults, String tag) {
    	
    	DocumentFragment docfrag = createBlockForRPr( 
        		context,
        		null,
        		rPrNodeIt,
        		childResults);
    	    
    	return docfrag;
    }	
    /**
     * This is invoked on every paragraph, whether it has a pPr or not.
     * 
     * @param wmlPackage
     * @param pPrNodeIt
     * @param pStyleVal
     * @param childResults - the already transformed contents of the paragraph.
     * @return
     */
    public static DocumentFragment createBlockForPPr( 
    		FOConversionContext context,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults) {
    	
    	return createBlock( 
        		context,
        		pPrNodeIt,
        		pStyleVal, childResults,
        		false);    	
    }
    
    private static DocumentFragment createBlock( 
    		FOConversionContext context,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults,
    		boolean sdt) {

    	PropertyResolver propertyResolver = context.getPropertyResolver();
    	
    	// Note that this is invoked for every paragraph with a pPr node.
    	
    	// incoming objects are org.apache.xml.dtm.ref.DTMNodeIterator 
    	// which implements org.w3c.dom.traversal.NodeIterator
    	
		Style defaultParagraphStyle = 
				(context.getWmlPackage().getMainDocumentPart().getStyleDefinitionsPart() != null ?
				context.getWmlPackage().getMainDocumentPart().getStyleDefinitionsPart().getDefaultParagraphStyle() :
				null);
		
    	String defaultParagraphStyleId;
    	if (defaultParagraphStyle==null) // possible, for non MS source docx
    		defaultParagraphStyleId = "Normal";
    	else defaultParagraphStyleId = defaultParagraphStyle.getStyleId();
    	
		if ( pStyleVal ==null || pStyleVal.equals("") ) {
//			pStyleVal = "Normal";
			pStyleVal = defaultParagraphStyleId;
		}
		if (context.getLog().isDebugEnabled()) {
			context.getLog().debug("style '" + pStyleVal );
		}

    	//    	log.info("pPrNode:" + pPrNodeIt.getClass().getName() ); // org.apache.xml.dtm.ref.DTMNodeIterator    	
//    	log.info("childResults:" + childResults.getClass().getName() ); 
    	
    	
        try {
        	
        	PPr pPrDirect = null;
        	
        	// Get the pPr node as a JAXB object,
        	// so we can read it using our standard
        	// methods.  Its a bit sad that we 
        	// can't just adorn our DOM tree with the
        	// original JAXB objects?
        	PPr pPr = null;
        	RPr rPr = null;
        	if (pPrNodeIt==null) {  // Never happens?        		
    			if (context.getLog().isDebugEnabled()) {
    				context.getLog().debug("Here after all!!");
    			}
        		pPr = propertyResolver.getEffectivePPr(defaultParagraphStyleId);
        		rPr = propertyResolver.getEffectiveRPr(defaultParagraphStyleId);
        	} else {
        		Node n = pPrNodeIt.nextNode();
        		if (n==null) {
        			if (context.getLog().isDebugEnabled()) {
        				context.getLog().debug("pPrNodeIt.nextNode() was null (ie there is no pPr in this p)");
        			}
            		pPr = propertyResolver.getEffectivePPr(defaultParagraphStyleId);
            		rPr = propertyResolver.getEffectiveRPr(defaultParagraphStyleId);
            		// TODO - in this case, we should be able to compute once,
            		// and on subsequent calls, just return pre computed value
        		} else {
        			if (context.getLog().isDebugEnabled()) {
        				context.getLog().debug( "P actual pPr: "+ XmlUtils.w3CDomNodeToString(n) );
        			}
        			Unmarshaller u = Context.jc.createUnmarshaller();			
        			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
        			Object jaxb = u.unmarshal(n);
    				pPrDirect =  (PPr)jaxb;
    				pPr = propertyResolver.getEffectivePPr(pPrDirect);  
    				if ((pPr==null) && (context.getLog().isDebugEnabled())) {
    					context.getLog().debug("pPr null; obtained from: " + XmlUtils.w3CDomNodeToString(n) );
    				}
    				
    				// On the block representing the w:p, we want to put both
    			    // pPr and rPr attributes.
    				
    				if (context.getLog().isDebugEnabled()) {
    					context.getLog().debug("getting rPr for paragraph style");
    				}
    				rPr = propertyResolver.getEffectiveRPr(null, pPrDirect); 
    					// rPr in pPr direct formatting only applies to paragraph mark, 
    					// so pass null here       				
        		}
        	}        	

			if (context.getLog().isDebugEnabled() && pPr!=null) {				
				context.getLog().debug("P effective pPr: "+ XmlUtils.marshaltoString(pPr, true, true));					
			}
        	
            // Create a DOM builder and parse the fragment			
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();
			
			//log.info("Document: " + document.getClass().getName() );
			
			boolean indentHandledByNumbering = false;
			
			Element foBlockElement;
			Element foListBlock = null;
			if (pPr!=null 
					&& pPr.getNumPr()!=null 
					&& pPr.getNumPr().getNumId()!=null
					&& pPr.getNumPr().getNumId().getVal().longValue()!=0 //zero means no numbering
					) {
				
				
				// Its a list item.  At present we make a new list-block for
				// each list-item. This is not great; DocumentModel will ultimately
				// allow us to use fo:list-block properly.

				/* Create something like:
				 * 			
					<fo:list-block provisional-distance-between-starts="0.5in" start-indent="0.5in">
					  <fo:list-item>
					    <fo:list-item-label>
					      <fo:block font-family="Times New Roman">-</fo:block>
					    </fo:list-item-label>
					    <fo:list-item-body start-indent="body-start()">
					      <fo:block font-family="Times New Roman" font-size="9.0pt" line-height="100%" space-after="0.08in" space-before="0.08in" text-align="justify">
					        <inline xmlns="http://www.w3.org/1999/XSL/Format" id="clauseDPI5123341"/>Content goes here...
					      </fo:block>
					    </fo:list-item-body>
					  </fo:list-item>
					</fo:list-block>
				 */				

				foListBlock = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
						"fo:list-block");
				document.appendChild(foListBlock);
								
//				foListBlock.setAttribute("provisional-distance-between-starts", "0.5in");
				
				// Need to apply shading at fo:list-block level
				if (pPr.getShd()!=null) {
					PShading pShading = new PShading(pPr.getShd());
					pShading.setXslFO(foListBlock);
				}
				
				Element foListItem = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
						"fo:list-item");
				foListBlock.appendChild(foListItem);				

				
				Element foListItemLabel = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
						"fo:list-item-label");
				foListItem.appendChild(foListItemLabel);
				
				Element foListItemLabelBody = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
						"fo:block");
				foListItemLabel.appendChild(foListItemLabelBody);
				
	        	ResultTriple triple;
	        	if (pPrDirect!=null && pPrDirect.getNumPr()!=null) {
	        		triple = org.docx4j.model.listnumbering.Emulator.getNumber(
	        			context.getWmlPackage(), pStyleVal, 
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
		        				context.getWmlPackage(), pStyleVal, 
			        			pPr.getNumPr().getNumId().getVal().toString(), 
			        			ilvlString ); 		        	
	        		}
	        	}
				
				if (triple==null) {
					context.getLog().warn("computed number ResultTriple was null");
	        		if (context.getLog().isDebugEnabled() ) {
	        			foListItemLabelBody.setAttribute("color", "red");
	        			foListItemLabelBody.setTextContent("null#");
	        		} 
	        	} else {
        			int numChars=1;
	        		if (triple.getBullet()!=null ) {
		        		foListItemLabelBody.setTextContent(triple.getBullet() );
		        	} else if (triple.getNumString()==null) {
		        		context.getLog().debug("computed NumString was null!");
		        		if (context.getLog().isDebugEnabled() ) {
		        			foListItemLabelBody.setAttribute("color", "red");
		        			foListItemLabelBody.setTextContent("null#");
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
	        			
	        		    int pdbs = getDistanceToNextTabStop(context, indent.getNumberPosition(), numWidth,
	        		    		pPrDirect.getTabs(), context.getWmlPackage().getMainDocumentPart().getDocumentSettingsPart());
	    				indent.setXslFOListBlock(foListBlock, pdbs);	        				        			
	        		}
    				indentHandledByNumbering = true; 
	        		
	        		// Set the font
	        		if (triple.getNumFont()!=null) {
	        			String font = Font.getPhysicalFont(context.getWmlPackage(), triple.getNumFont() );
	        			if (font!=null) {
	        				foListItemLabelBody.setAttribute(Font.FO_NAME, font );
	        			}
	        		}
	        		
	        	}
				
				Element foListItemBody = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
						"fo:list-item-body");
				foListItem.appendChild(foListItemBody);	

				foListItemBody.setAttribute(Indent.FO_NAME, "body-start()");
				
				foBlockElement = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
						"fo:block");
				foListItemBody.appendChild(foBlockElement);
				
				if (context.getLog().isDebugEnabled()) {
					context.getLog().debug("bare list result: " + XmlUtils.w3CDomNodeToString(foListBlock) );
				}
				
				
			} else {

				foBlockElement = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block");
				document.appendChild(foBlockElement);
			}
			
							
			if (pPr!=null) {
				// Ignore paragraph borders once inside the container
				boolean ignoreBorders = !sdt;

				createFoAttributes(context.getWmlPackage(), pPr, ((Element)foBlockElement), indentHandledByNumbering, ignoreBorders );
			}

			
			if (rPr!=null) {
				
				if (foListBlock==null) {
					createFoAttributes(context.getWmlPackage(), rPr, ((Element)foBlockElement) );
				} else {
					createFoAttributes(context.getWmlPackage(), rPr, ((Element)foListBlock) );					
				}
	        }

			if (context.getLog().isDebugEnabled()) {
				context.getLog().debug("after createFoAttributes: " + XmlUtils.w3CDomNodeToString(foBlockElement) );
			}
			
			// Our fo:block wraps whatever result tree fragment
			// our style sheet produced when it applied-templates
			// to the child nodes
			Node n = childResults.nextNode();
			
			// Handle empty case - want the block to be preserved!
			if (n.getChildNodes().getLength()==0) {
				
				((Element)foBlockElement).setAttribute( "white-space-treatment", "preserve");
				foBlockElement.setTextContent(" ");
				
			} else {
			
	//				log.info("Node we are importing: " + n.getClass().getName() );
	//				foBlockElement.appendChild(
	//						document.importNode(n, true) );
				/*
				 * Node we'd like to import is of type org.apache.xml.dtm.ref.DTMNodeProxy
				 * which causes
				 * org.w3c.dom.DOMException: NOT_SUPPORTED_ERR: The implementation does not support the requested type of object or operation.
				 * 
				 * See http://osdir.com/ml/text.xml.xerces-j.devel/2004-04/msg00066.html
				 * 
				 * So instead of importNode, use 
				 */
				XmlUtils.treeCopy( n,  foBlockElement );
				
			}
			
			DocumentFragment docfrag = document.createDocumentFragment();
			docfrag.appendChild(document.getDocumentElement());

			return docfrag;
						
		} catch (Exception e) {
			context.getLog().error(e.getLocalizedMessage(), e);
		} 
    	
    	return null;
    	
    }
    
    
    private static int getDistanceToNextTabStop(FOConversionContext context, int pos, int numWidth, Tabs pprTabs, DocumentSettingsPart settings) {
    	    	
		int pdbs = 0; 
		int defaultTab = 360;
		if (pprTabs!=null
				&& pprTabs.getTab()!=null
				&& pprTabs.getTab().size()>0) {
			
			for ( CTTabStop tabStop : pprTabs.getTab() ) {
					if (tabStop.getPos().intValue()> (pos+ numWidth) ) {
						context.getLog().debug("tab stop: using specified");
						return (tabStop.getPos().intValue() - pos);
					}
			}
			
		} 
		
		// The default tabs continue to apply after the specified ones
		if (settings!=null
				&& settings.getJaxbElement().getDefaultTabStop()!=null ) {
			CTTwipsMeasure twips = settings.getJaxbElement().getDefaultTabStop();
			defaultTab = twips.getVal().intValue();
			
			if (defaultTab>0) {
				context.getLog().debug("tab stop: using default from docx");
				int tabNUmber = (int)Math.floor((pos+numWidth)/defaultTab);
				int nextTabPos = defaultTab*(tabNUmber+1);
				return nextTabPos - pos;
			}
		}

		context.getLog().debug("tab stop: assuming default tab 360");
		int tabNUmber = (int)Math.floor((pos+numWidth)/defaultTab);
		int nextTabPos = defaultTab*(tabNUmber+1);
		return nextTabPos - pos;
    }

	private static void createFoAttributes(OpcPackage opcPackage, PPr pPr, Element foBlockElement, boolean inList, boolean ignoreBorders){
		
    	List<Property> properties = PropertyFactory.createProperties(opcPackage, pPr);
    	
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
	public static void applyFoAttributes(List<Property> properties, Element foElement) {
		if ((properties != null) && (!properties.isEmpty())) {
			for (int i=0; i<properties.size(); i++) {
				properties.get(i).setXslFO(foElement);
			}
		}
	}
	
    private static void createFoAttributes(TrPr trPr, Element foBlockElement){
    	if (trPr == null) {
    		return;
    	}
    	applyFoAttributes(PropertyFactory.createProperties(trPr), foBlockElement);
    }
	
    private static void createFoAttributes(TcPr tcPr, Element foBlockElement){
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
    public static DocumentFragment createBlockForRPr( 
    		FOConversionContext context,
    		NodeIterator pPrNodeIt,
    		NodeIterator rPrNodeIt,
    		NodeIterator childResults ) {

    	PropertyResolver propertyResolver = context.getPropertyResolver();
    	
    	// Note that this is invoked for every paragraph with a pPr node.
    	
    	// incoming objects are org.apache.xml.dtm.ref.DTMNodeIterator 
    	// which implements org.w3c.dom.traversal.NodeIterator

    	
//    	log.info("rPrNode:" + rPrNodeIt.getClass().getName() ); // org.apache.xml.dtm.ref.DTMNodeIterator    	
//    	log.info("childResults:" + childResults.getClass().getName() ); 
    	
    	
        try {
        	
			Unmarshaller u = Context.jc.createUnmarshaller();			
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			// If there is w:pPr/w:pStyle,			
			// we need to honour any rPr in the pStyle
			PPr pPrDirect = null;
        	if (pPrNodeIt!=null) {
        		Node n = pPrNodeIt.nextNode();
        		if (n!=null) {
        			Object jaxb = u.unmarshal(n);
        			try {
        				pPrDirect =  (PPr)jaxb;
        			} catch (ClassCastException e) {
        		    	context.getLog().error("Couldn't cast " + jaxb.getClass().getName() + " to PPr!");
        			}        	        			
        		}
        	}
        	
			Object jaxbR = u.unmarshal(rPrNodeIt.nextNode());			
			RPr rPrDirect = null;
			try {
				rPrDirect =  (RPr)jaxbR;
			} catch (ClassCastException e) {
				context.getLog().error("Couldn't cast .." );
			}        	
        	RPr rPr = propertyResolver.getEffectiveRPr(rPrDirect, pPrDirect);
        	
            // Create a DOM builder and parse the fragment
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();
			
			//log.info("Document: " + document.getClass().getName() );

			Node foInlineElement = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:inline");			
			document.appendChild(foInlineElement);
			
				
//			if (log.isDebugEnabled() && rPr!=null) {					
//				log.debug(XmlUtils.marshaltoString(rPr, true, true));					
//			}
			
			//if (rPr!=null) {				
				createFoAttributes(context.getWmlPackage(), rPr, ((Element)foInlineElement) );
			//}
			
			// Our fo:block wraps whatever result tree fragment
			// our style sheet produced when it applied-templates
			// to the child nodes
			Node n = childResults.nextNode();
			XmlUtils.treeCopy( n,  foInlineElement );			
			
			DocumentFragment docfrag = document.createDocumentFragment();
			docfrag.appendChild(document.getDocumentElement());

			return docfrag;
						
		} catch (Exception e) {
			context.getLog().error(e.getMessage(), e);
		} 
    	
    	return null;
    	
    }

	private static void createFoAttributes(OpcPackage opcPackage,
			RPr rPr, Element foInlineElement){

    	List<Property> properties = PropertyFactory.createProperties(opcPackage, rPr);
    	
    	for( Property p :  properties ) {
    		p.setXslFO(foInlineElement);
    	}
		
	}
	
    public static String getPageNumberFormat(FOConversionContext context) {
    	String pageFormat = 
    			context.getSections().getCurrentSection().getPageNumberInformation().getPageFormat();
    	//may return empty string if no page number format supplied
    	pageFormat = FormattingSwitchHelper.getFoPageNumberFormat(pageFormat);
    	return (pageFormat == null ? "" : pageFormat);
    }
	
    public static String getPageNumberInitial(FOConversionContext context) {
    	int ret = 
    			context.getSections().getCurrentSection().getPageNumberInformation().getPageStart();
    	//may return empty string if no start page number supplied
    	return (ret == -1 ? "" : Integer.toString(ret));
    }

    public static boolean hasPgNumTypeStart(FOConversionContext context) {
    	return (context.getSections().getCurrentSection().getPageNumberInformation().getPageStart() > -1);
    }
}
