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

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.ConversionSectionWrapper;
import org.docx4j.convert.out.common.preprocess.Containerization;
import org.docx4j.fonts.RunFontSelector;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.properties.paragraph.Indent;
import org.docx4j.model.properties.paragraph.Justification;
import org.docx4j.model.properties.paragraph.PBorderBottom;
import org.docx4j.model.properties.paragraph.PBorderTop;
import org.docx4j.model.properties.paragraph.PShading;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTPageNumber;
import org.docx4j.wml.CTTabStop;
import org.docx4j.wml.CTTwipsMeasure;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.NumPr.Ilvl;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STTabJc;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tabs;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TrPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.NodeIterator;


/** 
 * This class contains static functions that are specific to the FO xsl-transformation and 
 * are called from docx2fo.xslt. 
 *  
 */
public class XsltFOFunctions {
	
	private static Logger log = LoggerFactory.getLogger(XsltFOFunctions.class);
	

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
    	
    	DocumentFragment df = createBlock( 
        		context,
        		pPrNodeIt,
        		pStyleVal, childResults,
        		false);  
    	
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
    	
    	Element block = (Element)df.getFirstChild();
		NodeList blockChildren = block.getChildNodes();
    	for (int i = 0 ; i <blockChildren.getLength(); i++ ) {
    	
    		if (blockChildren.item(i) instanceof Element) {
	    		Element inline = (Element)blockChildren.item(i);
	    	
		    	if (inline !=null && inline.getAttribute("direction")!=null
		    			&& inline.getAttribute("direction").equals("rtl")) {
	
		        	inline.removeAttribute("direction");
		    		
		    		Element bidiOverride = df.getOwnerDocument().createElementNS("http://www.w3.org/1999/XSL/Format", 
							"fo:bidi-override");
		        	bidiOverride.setAttribute("unicode-bidi", "embed" );
		        	bidiOverride.setAttribute("direction", "rtl" );    		
		    		
		        	block.replaceChild(bidiOverride, inline);
		        	bidiOverride.appendChild(inline);
		    		
		    	}
    		}
    	} 
    	
    	if (foContainsElement(block, "leader")) {
			// ptab to leader implementation:
			// for leader to work as expected in fop, we need text-align-last; see http://xmlgraphics.apache.org/fop/faq.html#leader-expansion
			// this code adds that.
    		// Note that it doesn't seem to be necessary for leader in TOC, but it doesn't hurt
			block.setAttribute("text-align-last", "justify");
    	}
    	
    	return df;
    }
    
    
    /**
     * Recurse sourceNode looking to see whether it contains element with local name elementName
     * @param sourceNode
     * @param elementName
     * @return
     */
    private static boolean foContainsElement(Node sourceNode, String elementName) {

        switch (sourceNode.getNodeType() ) {

	    	case Node.DOCUMENT_NODE: // type 9
        	case Node.DOCUMENT_FRAGMENT_NODE: // type 11
        
                // recurse on each child
                NodeList nodes = sourceNode.getChildNodes();
                if (nodes != null) {
                    for (int i=0; i<nodes.getLength(); i++) {
                    	if (foContainsElement(nodes.item(i), elementName)) {
                    		return true;
                    	}
                    }
                }
                return false;
                
            case Node.ELEMENT_NODE:
                
                // Do it...
            	Element el = (Element)sourceNode;
            	if (el.getLocalName().equals(elementName)) {
//            		log.debug("Got " + elementName);
            		return true;
            	}

                // recurse on each child
                NodeList children = sourceNode.getChildNodes();
                if (children != null) {
                    for (int i=0; i<children.getLength(); i++) {
                    	if (foContainsElement(children.item(i), elementName)) {
                    		return true;
                    	}
                    }
                }

                return false;

            case Node.TEXT_NODE:
            	
                return false;
                
            default:
            	
                return false;
        }
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
				(context.getWmlPackage().getMainDocumentPart().getStyleDefinitionsPart(false) != null ?
				context.getWmlPackage().getMainDocumentPart().getStyleDefinitionsPart(false).getDefaultParagraphStyle() :
				null);
		
    	String defaultParagraphStyleId;
    	if (defaultParagraphStyle==null) // possible, for non MS source docx
    		defaultParagraphStyleId = "Normal";
    	else defaultParagraphStyleId = defaultParagraphStyle.getStyleId();
    	
		if ( pStyleVal ==null || pStyleVal.equals("") ) {
//			pStyleVal = "Normal";
			pStyleVal = defaultParagraphStyleId;
		}
		if (log.isDebugEnabled()) {
			log.debug("style '" + pStyleVal );
		}

    	//    	log.info("pPrNode:" + pPrNodeIt.getClass().getName() ); // org.apache.xml.dtm.ref.DTMNodeIterator    	
//    	log.info("childResults:" + childResults.getClass().getName() ); 
    	
    	
		/* First, determine effective paragraph and run properties (pPr, rPr) */
    	PPr pPrDirect = null;
    	
    	// Get the pPr node as a JAXB object,
    	// so we can read it using our standard
    	// methods.  Its a bit sad that we 
    	// can't just adorn our DOM tree with the
    	// original JAXB objects?
    	PPr pPr = null;
    	RPr rPr = null;
    	RPr rPrParagraphMark = null;  // required for list item label
        try {
        	
        	if (pPrNodeIt==null) {  // Never happens?        		
    			if (log.isDebugEnabled()) {
    				log.debug("Here after all!!");
    			}
        		pPr = propertyResolver.getEffectivePPr(defaultParagraphStyleId);
        		rPr = propertyResolver.getEffectiveRPr(defaultParagraphStyleId);
        		rPrParagraphMark = rPr;
        	} else {
        		Node n = pPrNodeIt.nextNode();
        		if (n==null) {
        			if (log.isDebugEnabled()) {
        				log.debug("pPrNodeIt.nextNode() was null (ie there is no pPr in this p)");
        			}
            		pPr = propertyResolver.getEffectivePPr(defaultParagraphStyleId);
            		rPr = propertyResolver.getEffectiveRPr(defaultParagraphStyleId);
            		// TODO - in this case, we should be able to compute once,
            		// and on subsequent calls, just return pre computed value
        		} else {
        			if (log.isDebugEnabled()) {
        				log.debug( "P actual pPr: "+ XmlUtils.w3CDomNodeToString(n) );
        			}
        			Unmarshaller u = Context.jc.createUnmarshaller();			
        			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
        			Object jaxb = u.unmarshal(n);
    				pPrDirect =  (PPr)jaxb;
    				pPr = propertyResolver.getEffectivePPr(pPrDirect);  
    				if ((pPr==null) && (log.isDebugEnabled())) {
    					log.debug("pPr null; obtained from: " + XmlUtils.w3CDomNodeToString(n) );
    				}
    				
    				// On the block representing the w:p, we want to put both
    			    // pPr and rPr attributes.
    				
    				if (log.isDebugEnabled()) {
    					log.debug("getting rPr for paragraph style");
    				}

					// rPr in pPr direct formatting only applies to paragraph mark, 
					// and by virtue of that, to list item label,
					// so pass null here.				
					// 2018 05, actually, sz also affects line spacing (eg 12 pt on block
            		// with 11 pt inside would have more space), so its important
            		RPr fontSzOnlyRPr = new RPr();
            		if (pPrDirect.getRPr()!=null && pPrDirect.getRPr().getSz()!=null) {
            			HpsMeasure hm = new HpsMeasure(); 
            			hm.setVal( pPrDirect.getRPr().getSz().getVal() ); // does this suffice as a copy?
                		fontSzOnlyRPr.setSz(hm);
            		}
            		
            		rPr = propertyResolver.getEffectiveRPr(fontSzOnlyRPr, pPrDirect);
            		
    				// Now, work out the value for list item label
            		rPrParagraphMark = XmlUtils.deepCopy(rPr);
    				
//        			System.out.println("p rpr-->" + XmlUtils.marshaltoString(pPrDirect.getRPr()));
            		
            		StyleUtil.apply(pPrDirect.getRPr(), rPrParagraphMark); 
    				
        		}
        	}        	

			if (log.isDebugEnabled() && pPr!=null) {				
				log.debug("P effective pPr: "+ XmlUtils.marshaltoString(pPr, true, true));					
			}
		} catch (Exception e) {
			//log.error(e.getLocalizedMessage(), e);
			log.error(e.getMessage(), e);
	    	return null;
		} 
			
		/* Now that we have pPr, we can format the block. */
		return createBlock(context.getWmlPackage(), context.getRunFontSelector(), pStyleVal, childResults, sdt, pPrDirect, pPr, rPr, rPrParagraphMark); 
    	    	
    }

	protected static DocumentFragment createBlock(WordprocessingMLPackage wmlPackage, RunFontSelector runFontSelector, 
			String pStyleVal, NodeIterator childResults,
			boolean sdt, PPr pPrDirect, PPr pPr, RPr rPr, RPr rPrParagraphMark) {

        try {
            // Create a DOM builder and parse the fragment			
			Document document = XmlUtils.getNewDocumentBuilder().newDocument();
			//log.info("Document: " + document.getClass().getName() );
			
			Element foBlockElement = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block");
			Element foListBlock = null;
			boolean indentHandledByNumbering = false;
			// Is it a list item?
			if (pPr!=null 
					&& pPr.getNumPr()!=null 
					&& pPr.getNumPr().getNumId()!=null
					&& pPr.getNumPr().getNumId().getVal().longValue()!=0 //zero means no numbering
					) {
				
				foListBlock = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:list-block");
				document.appendChild(foListBlock);
				
				// Its a list item.  At present we make a new list-block for
				// each list-item. This is not great; DocumentModel will ultimately
				// allow us to use fo:list-block properly.

				indentHandledByNumbering = createListBlock(wmlPackage, runFontSelector, pStyleVal, pPrDirect, pPr, rPr,
						rPrParagraphMark, document, foBlockElement, foListBlock);
				
				if (log.isDebugEnabled()) {
					log.debug("bare list result: " + XmlUtils.w3CDomNodeToString(foListBlock) );
				}
				
				
			} else /* its not a list item */ {
				document.appendChild(foBlockElement);
			}
			
			/* Now apply pPr (whether its a list or not) */				
			if (pPr!=null) {
				// Ignore paragraph borders once inside the container
				boolean ignoreBorders = !sdt;
				createFoAttributes(wmlPackage, pPr, ((Element)foBlockElement), indentHandledByNumbering, ignoreBorders );				
			}
			
			/* Now apply rPr */				
			if (rPr!=null) {
				
				if (foListBlock==null) {
					createFoAttributes(wmlPackage, rPr, ((Element)foBlockElement) );
				} else {
					createFoAttributes(wmlPackage, rPr, ((Element)foListBlock) );					
				}
	        }

			if (log.isDebugEnabled()) {
				log.debug("after createFoAttributes: " + XmlUtils.w3CDomNodeToString(foBlockElement) );
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
			
				/* don't do:
				 * 
	            ((Element)foBlockElement).setAttribute( "white-space-treatment", "preserve");
	            ((Element)foBlockElement).setAttribute( "white-space-collapse", "false");

	            Suggested at https://www.docx4java.org/forums/docx-java-f6/export-fo-preserve-whitespaces-t2762.html 
	            because it causes unwanted formatting issues.  See https://stackoverflow.com/questions/57475550/unwanted-indent-after-line-wrap/57488818
	            and https://github.com/plutext/docx4j/issues/369
	            
	            In any subsequent review of this, also consider https://www.docx4java.org/forums/pdf-output-f27/converting-docx-to-pdf-not-preserving-whitespace-t2752.html
	            
				 */
	            
				
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
			
			// FOP doesn't support "ignore-if-surrounding-linefeed", and "pre" is no good, since wrapping does not happen
			// (so paragraph continues right over edge of page)
			//((Element)foBlockElement).setAttribute( "white-space", "ignore");
			
			DocumentFragment docfrag = document.createDocumentFragment();
			docfrag.appendChild(document.getDocumentElement());

			return docfrag;
						
		} catch (Exception e) {
			//log.error(e.getLocalizedMessage(), e);
			log.error(e.getMessage(), e);
		}
        return null;
	}

	protected static boolean createListBlock(WordprocessingMLPackage wmlPackage, RunFontSelector runFontSelector,
			String pStyleVal, PPr pPrDirect, PPr pPr, RPr rPr, RPr rPrParagraphMark, Document document,
			Element foBlockElement, Element foListBlock) {
		
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
						
//				foListBlock.setAttribute("provisional-distance-between-starts", "0.5in");
		
		boolean indentHandledByNumbering = false;
		
		// Need to apply shading at fo:list-block level
		if (pPr.getShd()!=null) {
			PShading pShading = new PShading(pPr.getShd());
			pShading.setXslFO(foListBlock);
		}
		
		Element foListItem = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:list-item");
		foListBlock.appendChild(foListItem);				

		
		Element foListItemLabel = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:list-item-label");
		foListItem.appendChild(foListItemLabel);
		
		Element foListItemLabelBody = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block");
		foListItemLabel.appendChild(foListItemLabelBody);
		
		Element foListItemBody = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:list-item-body");
		foListItem.appendChild(foListItemBody);	
		foListItemBody.setAttribute(Indent.FO_NAME, "body-start()");
		
		ResultTriple triple;
		if (pPrDirect!=null && pPrDirect.getNumPr()!=null) {
			triple = org.docx4j.model.listnumbering.Emulator.getNumber(
					wmlPackage, pStyleVal, 
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
						wmlPackage, pStyleVal, 
		    			pPr.getNumPr().getNumId().getVal().toString(), 
		    			ilvlString ); 		        	
			}
		}
		
		if (triple==null) {
			log.warn("computed number ResultTriple was null");
			if (log.isDebugEnabled() ) {
				foListItemLabelBody.setAttribute("color", "red");
				foListItemLabelBody.setTextContent("null#");
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
//	        				setFont( context,  foListItemLabelBody, rPr.getRFonts()); 
					setFont( runFontSelector,  foListItemLabelBody,  pPr,  rPr,  triple.getNumString());
				} else {
					
					createFoAttributes(wmlPackage, rPrParagraphMark, foListItemLabel );	        				
					createFoAttributes(wmlPackage, rPrParagraphMark, foListItemBody );	
					
//	        				setFont( context,  foListItemLabelBody, rPrParagraphMark.getRFonts()); 	        				
					setFont( runFontSelector,  foListItemLabelBody,  pPr,  rPrParagraphMark,  triple.getNumString());
				}
				
			} else {
				RPr actual = XmlUtils.deepCopy(triple.getRPr()); // clone, so the ilvl rpr is not altered
//	        			System.out.println(XmlUtils.marshaltoString(rPrParagraphMark));
				
				// pMark overrides numbering, except for font
				// (which makes sense, since that would change the bullet)
				// so set the font
				setFont( runFontSelector,  foListItemLabelBody,  pPr,  actual,  triple.getNumString());
				// .. before taking rPrParagraphMark into account
				StyleUtil.apply(rPrParagraphMark, actual); 
//	        			System.out.println(XmlUtils.marshaltoString(actual));
				
				createFoAttributes(wmlPackage, actual, foListItemLabel );
				createFoAttributes(wmlPackage, actual, foListItemBody );
				
			}
				        		
			
			int numChars=1;
			if (triple.getBullet()!=null ) {
				foListItemLabelBody.setTextContent(triple.getBullet() );
			} else if (triple.getNumString()==null) {
				log.debug("computed NumString was null!");
				if (log.isDebugEnabled() ) {
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
				
			    int pdbs = getDistanceToNextTabStop(indent.getNumberPosition(), numWidth,
			    		pPrDirect.getTabs(), wmlPackage.getMainDocumentPart().getDocumentSettingsPart());
				indent.setXslFOListBlock(foListBlock, pdbs);	        				        			
			}
			indentHandledByNumbering = true; 
			
//	        		// Set the font
//	        		if (triple.getNumFont()!=null) {
//	        			String font = PhysicalFonts.getPhysicalFont(context.getWmlPackage(), triple.getNumFont() );
//	        			if (font!=null) {
//	        				foListItemLabelBody.setAttribute("font-family", font );
//	        			}
//	        		}
			
		}
		foListItemBody.appendChild(foBlockElement);
		return indentHandledByNumbering;
	}
    
    /**
     * Use RunFontSelector to determine the correct font for the list item label.
     * 
     * @param context
     * @param foListItemLabelBody
     * @param pPr
     * @param rPr
     * @param text
     */
    protected static void setFont(RunFontSelector runFontSelector, Element foListItemLabelBody, PPr pPr, RPr rPr, String text) {
    	
    	DocumentFragment result = (DocumentFragment)runFontSelector.fontSelector(pPr, rPr, text);
    	log.debug(XmlUtils.w3CDomNodeToString(result));
    	// eg <fo:inline xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Times New Roman">1)</fo:inline>
    	
    	// Now get the attribute value
    	if (result!=null && result.getFirstChild()!=null) {
    		Attr attr = ((Element)result.getFirstChild()).getAttributeNode("font-family");
    		if (attr!=null) {
    			foListItemLabelBody.setAttribute("font-family", attr.getValue());
    		}
    	}
			
    }
    
    
    protected static int getDistanceToNextTabStop( int pos, int numWidth, Tabs pprTabs, DocumentSettingsPart settings) {
    	// Also used by FOExporterVisitorGenerator, so should be moved
    	    	
		int pdbs = 0; 
		int defaultTab = 360;
		if (pprTabs!=null
				&& pprTabs.getTab()!=null
				&& pprTabs.getTab().size()>0) {
			
			for ( CTTabStop tabStop : pprTabs.getTab() ) {
					if (tabStop.getPos().intValue()> (pos+ numWidth) ) {
						log.debug("tab stop: using specified");
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
				log.debug("tab stop: using default from docx");
				int tabNUmber = (int)Math.floor((pos+numWidth)/defaultTab);
				int nextTabPos = defaultTab*(tabNUmber+1);
				return nextTabPos - pos;
			}
		}

		log.debug("tab stop: assuming default tab 360");
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
        		    	log.error("Couldn't cast to PPr " + jaxb.getClass().getName() + " to PPr!");
        			}        	        			
        		}
        	}
        	
			Object jaxbR = u.unmarshal(rPrNodeIt.nextNode());			
			//RPr rPrDirect = null;
			RPr rPr = null;
			if (jaxbR instanceof RPr) {
				//rPrDirect =  (RPr)jaxbR;
				rPr = propertyResolver.getEffectiveRPr((RPr)jaxbR, pPrDirect);
			} else if (jaxbR instanceof ParaRPr) {
//				if (log.isDebugEnabled()) {
//					Throwable t = new Throwable();
//					log.debug("passed ParaRPr", t);
//				}
				
				rPr = propertyResolver.getEffectiveRPr(null, pPrDirect); 
//    			System.out.println("p rpr-->" + XmlUtils.marshaltoString(pPrDirect.getRPr()));
        		
        		StyleUtil.apply((ParaRPr)jaxbR, rPr); 				
				
			} else {
				log.error("TODO handle  .." + jaxbR.getClass().getName());
			}        	
        	
			
            // Create a DOM builder and parse the fragment
			Document document = XmlUtils.getNewDocumentBuilder().newDocument();
			
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
			log.error(e.getMessage(), e);
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
    
    /**
     * FOP inserts a blank page if necessary so that a section with page numbering
     * from 1 would be face up when printed double sided. Word doesn't do that
     * (unless you have an odd section type), so this function mimics Word's 
     * behaviour. 
     * 
     * @param context
     * @return
     * @since 3.2.2
     */
    public static String getForcePageCount(FOConversionContext context) {
    	
    	// see http://www.w3.org/TR/xsl/#force-page-count
    	
    	ConversionSectionWrapper wrapper = context.getSections().peekNextSection();
    	
    	if (wrapper==null) {
    		// final section
    		return "no-force";
    	} else {
    		SectPr.Type secType = wrapper.getSectPr().getType();
    		
    		CTPageNumber pgNumType = wrapper.getSectPr().getPgNumType();
    		Boolean isExplicitOdd = null; // null means numbering will continue from the highest page number in the previous section
    		if (pgNumType!=null && pgNumType.getStart()!=null) {
    			int start = pgNumType.getStart().intValue();
    			if ( start % 2 == 0) {
    				isExplicitOdd = Boolean.FALSE;    				
    			} else {
    				isExplicitOdd = Boolean.TRUE;    				    				
    			}
    		}
    		
    		if (secType==null || secType.getVal().equals("nextPage") ) {
        		return "no-force";  
    		} else if (isExplicitOdd==null  // LIMITATION: We don't get this right after the user has set the page number explicitly in a previous section
    						|| isExplicitOdd) {
    			// The normal case
    			if ( secType.getVal().equals("evenPage") ) {
	    			// Even page section breaks, which begin the new section on the next even-numbered page.
	    			// (What happens if that section has w:pgNumType/@w:start="1"?)
	    			return "end-on-odd";
	    		} else if ( secType.getVal().equals("oddPage") ) {
	    			// Odd page section breaks, which begin the new section on the next odd-numbered page
	    			return "end-on-even";
	    		} else {
	    			// continuous (!)
	        		return "no-force";    			    			
	    		}
    		} else {
    			// section starts with p2 or p4
    			if ( secType.getVal().equals("evenPage") ) {
	    			// Even page section breaks, which begin the new section on the next even-numbered page.
	    			// (What happens if that section has w:pgNumType/@w:start="1"?)
	    			return "end-on-even";
	    		} else if ( secType.getVal().equals("oddPage") ) {
	    			// Odd page section breaks, which begin the new section on the next odd-numbered page
	    			return "end-on-odd";
	    		} else {
	    			// continuous (!)
	        		return "no-force";    			    			
	    		}
    			
    		}
    	}

    }
    
    private static boolean isOdd(SectPr sectPr) {
    	
    	CTPageNumber pgNumType = sectPr.getPgNumType();
    	
    	return true;
    }

    public static boolean hasPgNumTypeStart(FOConversionContext context) {
    	return (context.getSections().getCurrentSection().getPageNumberInformation().getPageStart() > -1);
    }
}
