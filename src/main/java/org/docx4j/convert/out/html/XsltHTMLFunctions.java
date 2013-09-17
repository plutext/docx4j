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
package org.docx4j.convert.out.html;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.properties.AdHocProperty;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.properties.paragraph.Indent;
import org.docx4j.model.properties.table.BorderBottom;
import org.docx4j.model.properties.table.BorderLeft;
import org.docx4j.model.properties.table.BorderRight;
import org.docx4j.model.properties.table.BorderTop;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.Tree;
import org.docx4j.model.styles.StyleTree.AugmentedStyle;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.PPrBase.Ind;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.NodeIterator;


/** 
 * This class contains static functions that are specific to the HTML xsl-transformation and 
 * are called from docx2html-core.xslt. 
 *  
 */
public class XsltHTMLFunctions {
    private static final Pattern HEADING_PATTERN = Pattern.compile("heading\\s*(\\d)");
    private static final Pattern HEADING_TAG_PATTERN = Pattern.compile("h\\d");

    
    /**
	 * The method used by the XSLT extension function during HTML export.
	 * 
	 * If there is no number, it returns an empty span element.
	 * 
	 * @param em
	 * @param levelId
	 * @param numId
	 * @return
	 */
    public static String getNumberXmlNode(HTMLConversionContext context,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, String numId, String levelId) {
    	
    	
    	// Note that this is invoked for every paragraph with a pPr node.
    	
    	context.getLog().debug("numbering, using style '" + pStyleVal + "'; numId=" + numId + "; ilvl " + levelId);    	
    	
        try {
        	        	
        	ResultTriple triple = org.docx4j.model.listnumbering.Emulator.getNumber(
        			context.getWmlPackage(), pStyleVal, numId, levelId);   
        	

			if (triple==null) {
				context.getLog().debug("computed number ResultTriple was null");
        		return null;
        	}
			
			String styleVal = "";
			
    		if (triple.getBullet()!=null ) {
    			//return (triple.getBullet() + " " );  
    			// The old code did that:-
    			// https://github.com/plutext/docx4j/commit/7627863e47c5dc7b3c91290b8d993ae5a7cd9fab#docx4j/src/main/java/org/docx4j/convert/out/html/AbstractHtmlExporter.java
    			//What is wrong with that approach?
    			// 
    			return "\u2022  "; 
				// see notes in docx2xhtmlNG2.xslt as to why we don't use &bull;
    			
    		} else if (triple.getNumString()==null) {
	    		context.getLog().debug("computed NumString was null (which may be ok)");
	    		return " ";
	    		
	    	} else {
				return triple.getNumString() + " " ;
	    	}
			
		} catch (Exception e) {
			e.printStackTrace();
			// System.out.println(e.toString() );
			context.getLog().error(e.getMessage(), e);
		} 
    	
		return "?  ";
    	
    }
    
    
    public static String getCssForStyles(HTMLConversionContext context) {
    	StringBuilder result = new StringBuilder();
    	
    	StyleTree styleTree = null;
		try {
			styleTree = context.getWmlPackage().getMainDocumentPart().getStyleTree();
		} catch (Exception e) {
			context.getLog().error("Couldn't getStyleTree", e);
    		return result.toString();			
		}

		HtmlCssHelper.createCssForStyles(context.getWmlPackage(), styleTree, result);
    	
    	if (context.getLog().isDebugEnabled()) {
    		return result.toString();
    	} else {
    		String debug = result.toString();
    		return debug;
    	}
    }
    
    public static String getCssForTableCells(HTMLConversionContext context, 
    		NodeIterator tables) {
    	
    	// The only way we seem to be able to make rules which
    	// apply to all the cells in a particular table
    	
    	Tbl tbl;
    	StringBuffer result = new StringBuffer();		
    	
		//DTMNodeProxy n = (DTMNodeProxy)tables.nextNode();
    	Element n = (Element)tables.nextNode();
		if (n==null) {
			// No tables in this document
			return "";
		}    	
    	int idx = 0;
		do {
			if (n.getNodeName().equals("w:tbl" )) {
				// n.getLocalName() -> tbl
				// n.getNodeName() -> w:tbl

    			Object jaxb;
				try {
					Unmarshaller u = Context.jc.createUnmarshaller();			
					u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
					jaxb = u.unmarshal(n);
    				tbl =  (Tbl)jaxb;
    				
    				result.append(getCssForTableCells(context, tbl,  idx) );
    				
				} catch (JAXBException e1) {
					context.getLog().error("JAXB error", e1);
    			} catch (ClassCastException e) {
    				context.getLog().error("Couldn't cast to Tbl!");
    			}        	        			
				
			} else {
				context.getLog().warn("Expected table but encountered: " + n.getNodeName() );
			}
			// next 
			idx++;
			n = (Element)tables.nextNode();
			
		} while ( n !=null ); 
    	
		return result.toString();
    }
    
    
    private static String getCssForTableCells(HTMLConversionContext context, 
    		Tbl tbl, int idx) {
    	
    	StringBuffer result = new StringBuffer();		
		PropertyResolver pr = context.getPropertyResolver();
		Style s = pr.getEffectiveTableStyle(tbl.getTblPr() );
		
		result.append("#" + TableWriter.getId(idx) + " td { ");
    	List<Property> properties =  new ArrayList<Property>();
    	if (s.getTblPr()!=null
    			&& s.getTblPr().getTblBorders()!=null ) {
    		TblBorders tblBorders = s.getTblPr().getTblBorders();
    		if (tblBorders.getInsideH()!=null) {
    			if (tblBorders.getInsideH().getVal()==STBorder.NONE
    					|| tblBorders.getInsideH().getVal()==STBorder.NIL
    					|| tblBorders.getInsideH().getSz()==BigInteger.ZERO ) {
    				properties.add( new AdHocProperty("border-top-style", "none", null, null));
    				properties.add( new AdHocProperty("border-top-width", "0mm", null, null));
    				properties.add( new AdHocProperty("border-bottom-style", "none", null, null));
    				properties.add( new AdHocProperty("border-bottom-width", "0mm", null, null));
    			} else {
    				properties.add( new BorderTop(tblBorders.getTop() ));
    				properties.add( new BorderBottom(tblBorders.getBottom() ));
    			}
    		}
    		if (tblBorders.getInsideV()!=null) { 
    			if (tblBorders.getInsideV().getVal()==STBorder.NONE
    					|| tblBorders.getInsideV().getVal()==STBorder.NIL
    					|| tblBorders.getInsideV().getSz()==BigInteger.ZERO ) {
    				properties.add( new AdHocProperty("border-left-style", "none", null, null));
    				properties.add( new AdHocProperty("border-left-width", "0mm", null, null));
    				properties.add( new AdHocProperty("border-right-style", "none", null, null));
    				properties.add( new AdHocProperty("border-right-width", "0mm", null, null));
    			} else {
    				properties.add( new BorderRight(tblBorders.getRight() ));
    				properties.add( new BorderLeft(tblBorders.getLeft() ));
    			}
    		}
    	}
    	if (s.getTcPr()!=null ) {
    		PropertyFactory.createProperties(properties, s.getTcPr() );
    	}
		// Ensure empty cells have a sensible height
    	// TODO - this is no good with IE8, which doesn't treat this 
    	// as a minimum; it won't resize if there is more :-(
    	properties.add(new AdHocProperty("height", "5mm", null, null));
    	
    	
		for( Property p :  properties ) {
			if (p!=null) {
				result.append(p.getCssProperty());
			}
		}
		result.append("}\n");
		return result.toString();
    	
    }
    
	public static Node toSdtNode(HTMLConversionContext context,
    		NodeIterator sdtPrNodeIt,
			NodeIterator childResults) throws TransformerException {
		return SdtWriter.toNode(context, sdtPrNodeIt, childResults);
	}
    
    private static DocumentFragment createBlockForSdt( 
    		HTMLConversionContext context,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults, String tag) {
    	
    	DocumentFragment docfrag = createBlock( context,
        		 pPrNodeIt,
        		 pStyleVal,  childResults,
        		 "div");
    	    	    
    	return docfrag;
    }	    

    public static DocumentFragment createBlockForPPr( 
    		HTMLConversionContext context,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults ) {

    	return createBlock( 
        		 context,
        		 pPrNodeIt,
        		 pStyleVal,  childResults,
        		  "p" );
    	
    }
    
    private static DocumentFragment createBlock( 
    		HTMLConversionContext context,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults,
    		String htmlElementName ) {
    	

		StyleTree styleTree = context.getWmlPackage().getMainDocumentPart().getStyleTree();
    	
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
    	context.getLog().debug("style '" + pStyleVal );     		
    	
//    	log.info("pPrNode:" + pPrNodeIt.getClass().getName() ); // org.apache.xml.dtm.ref.DTMNodeIterator    	
//    	log.info("childResults:" + childResults.getClass().getName() ); 

    	
        try {
        	
        	// Get the pPr node as a JAXB object,
        	// so we can read it using our standard
        	// methods.  Its a bit sad that we 
        	// can't just adorn our DOM tree with the
        	// original JAXB objects?
        	PPr pPr = null;
        	if (pPrNodeIt!=null) { //It is never null
        		Node n = pPrNodeIt.nextNode();
        		if (n!=null) {
        			Unmarshaller u = Context.jc.createUnmarshaller();			
        			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
        			Object jaxb = u.unmarshal(n);
        			try {
        				pPr =  (PPr)jaxb;
        			} catch (ClassCastException e) {
        				context.getLog().error("Couldn't cast " + jaxb.getClass().getName() + " to PPr!");
        			}        	        			
        		}
        	}
        	
            // Create a DOM document to take the results			
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();			
				//log.info("Document: " + document.getClass().getName() );
			
            context.getLog().debug(pStyleVal);
            Tree<AugmentedStyle> pTree = styleTree.getParagraphStylesTree();        
            org.docx4j.model.styles.Node<AugmentedStyle> asn = pTree.get(pStyleVal);			
							
			if (context.getLog().isDebugEnabled() && pPr!=null) {					
				context.getLog().debug(XmlUtils.marshaltoString(pPr, true, true));					
			}				

			// Check if heading
			Matcher matcher = HEADING_PATTERN.matcher(asn.getData().getStyle().getName().getVal());
            if(matcher.find()) {
                htmlElementName="h"+Math.min(Integer.parseInt(matcher.group(1)),6);
            }
            Element xhtmlBlock = document.createElement(htmlElementName);           
            document.appendChild(xhtmlBlock);
            
            // Set @class
            xhtmlBlock.setAttribute("class", 
                    StyleTree.getHtmlClassAttributeValue(pTree, asn)            
            );
            
			// Does our pPr contain anything else?
			boolean ignoreBorders = (htmlElementName.equals("p") || 
                    HEADING_TAG_PATTERN.matcher(htmlElementName).matches());
			if (pPr!=null) {
				
				// Is there numbering indentation to honour?
				if (pPr.getNumPr()!=null 
						&& pPr.getNumPr().getNumId()!=null
						&& pPr.getNumPr().getNumId().getVal().longValue()!=0 //zero means no numbering
						) {
					Ind numInd = org.docx4j.model.listnumbering.Emulator.getInd(
	        			context.getWmlPackage(), pStyleVal, 
	        			pPr.getNumPr().getNumId().getVal().toString(), 
	        			pPr.getNumPr().getIlvl().getVal().toString() ); 
					if (numInd!=null) {
		        		Indent indent = new Indent(pPr.getInd(), numInd);
		        		pPr.setInd((Ind)indent.getObject());						
					}
				}
				
				StringBuilder inlineStyle =  new StringBuilder();
				HtmlCssHelper.createCss(context.getWmlPackage(), pPr, inlineStyle, ignoreBorders);				
				if (!inlineStyle.toString().equals("") ) {
					xhtmlBlock.setAttribute("style", inlineStyle.toString() );
				}
			}
			
						
			// Our fo:block wraps whatever result tree fragment
			// our style sheet produced when it applied-templates
			// to the child nodes
			// init
			Node n = childResults.nextNode();
			do {	
				
				// getNumberXmlNode creates a span node, which is empty
				// if there is no numbering.
				// Let's get rid of any such <span/>.
				
				// What we actually get is a document node
				if (n.getNodeType()==Node.DOCUMENT_NODE) {
					context.getLog().debug("handling DOCUMENT_NODE");
					// Do just enough of the handling here
	                NodeList nodes = n.getChildNodes();
	                if (nodes != null) {
	                    for (int i=0; i<nodes.getLength(); i++) {
	                    	
	        				if (((Node)nodes.item(i)).getLocalName().equals("span")
	        						&& ! ((Node)nodes.item(i)).hasChildNodes() ) {
	        					// ignore
	        					context.getLog().debug(".. ignoring <span/> ");
	        				} else {
	        					XmlUtils.treeCopy( (Node)nodes.item(i),  xhtmlBlock );	        					
	        				}
	                    }
	                }					
				} else {
					
	//					log.info("Node we are importing: " + n.getClass().getName() );
	//					foBlockElement.appendChild(
	//							document.importNode(n, true) );
					/*
					 * Node we'd like to import is of type org.apache.xml.dtm.ref.DTMNodeProxy
					 * which causes
					 * org.w3c.dom.DOMException: NOT_SUPPORTED_ERR: The implementation does not support the requested type of object or operation.
					 * 
					 * See http://osdir.com/ml/text.xml.xerces-j.devel/2004-04/msg00066.html
					 * 
					 * So instead of importNode, use 
					 */
					XmlUtils.treeCopy( n,  xhtmlBlock );
				}
				// next 
				n = childResults.nextNode();
				
			} while ( n !=null ); 
			
			if (xhtmlBlock.getNodeName().equals("p")
					&& !xhtmlBlock.hasChildNodes() ) {
				// browsers don't display an empty p, so add a space to it
				
				Text t = document.createTextNode("\u00A0"); //= &nbsp; = &#160;
					// see notes in docx2xhtmlNG2.xslt as to why it is done this way!
				xhtmlBlock.appendChild(t);
			}
			
//			System.out.println(XmlUtils.w3CDomNodeToString(document));
			
			DocumentFragment docfrag = document.createDocumentFragment();
			docfrag.appendChild(document.getDocumentElement());

			return docfrag;
						
		} catch (Exception e) {
			context.getLog().error(e.getMessage(), e);
		} 
    	
    	return null;
    	
    }

    public static DocumentFragment createBlockForRPr( 
    		HTMLConversionContext context,
    		String pStyleVal,
    		NodeIterator rPrNodeIt,
    		NodeIterator childResults ) {

		Style defaultRunStyle = 
				(context.getWmlPackage().getMainDocumentPart().getStyleDefinitionsPart() != null ?
				context.getWmlPackage().getMainDocumentPart().getStyleDefinitionsPart().getDefaultCharacterStyle() :
				null);
		
    	String defaultCharacterStyleId;
    	if (defaultRunStyle.getStyleId()==null) // possible, for non MS source docx
    		defaultCharacterStyleId = "DefaultParagraphFont";
    	else defaultCharacterStyleId = defaultRunStyle.getStyleId();
    	
    	
    	StyleTree styleTree = context.getWmlPackage().getMainDocumentPart().getStyleTree();
    	    	
    	// Note that this is invoked for every paragraph with a pPr node.
    	
    	// incoming objects are org.apache.xml.dtm.ref.DTMNodeIterator 
    	// which implements org.w3c.dom.traversal.NodeIterator

    	
//    	log.info("rPrNode:" + rPrNodeIt.getClass().getName() ); // org.apache.xml.dtm.ref.DTMNodeIterator    	
//    	log.info("childResults:" + childResults.getClass().getName() ); 
    	
    	
        try {

        	// Get the rPr node as a JAXB object,
        	// so we can read it using our standard
        	// methods.  Its a bit sad that we 
        	// can't just adorn our DOM tree with the
        	// original JAXB objects?
			RPr rPr = null;
        	if (rPrNodeIt!=null) { //It is never null
        		Node n = rPrNodeIt.nextNode();
        		if (n!=null) {
        			Unmarshaller u = Context.jc.createUnmarshaller();			
        			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
        			Object jaxb = u.unmarshal(n);
        			try {
        				rPr =  (RPr)jaxb;
        			} catch (ClassCastException e) {
        				context.getLog().error("Couldn't cast " + jaxb.getClass().getName() + " to RPr!");
        			}        	        			
        		}
        	}
        	        	
            // Create a DOM builder and parse the fragment
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();
			
			//log.info("Document: " + document.getClass().getName() );

			Node span = document.createElement("span");			
			document.appendChild(span);
			
			// Set @class	
			String rStyleVal = defaultCharacterStyleId;
			if ( rPr!=null && rPr.getRStyle()!=null) {
				rStyleVal = rPr.getRStyle().getVal();
			}
			Tree<AugmentedStyle> cTree = styleTree.getCharacterStylesTree();		
			org.docx4j.model.styles.Node<AugmentedStyle> asn = cTree.get(rStyleVal);
			if (asn==null) {
				context.getLog().warn("No style node for: " + rStyleVal);
			} else {
				((Element)span).setAttribute("class", 
						StyleTree.getHtmlClassAttributeValue(cTree, asn)			
				);		
			}
			
			if (rPr!=null) {
				
				if (context.getLog().isDebugEnabled()) {					
					context.getLog().debug(XmlUtils.marshaltoString(rPr, true, true));					
				}
				
				// Does our rPr contain anything else?
				StringBuilder inlineStyle =  new StringBuilder();
				HtmlCssHelper.createCss(context.getWmlPackage(), rPr, inlineStyle);				
				if (!inlineStyle.toString().equals("") ) {
					((Element)span).setAttribute("style", inlineStyle.toString() );
				}
			}
			
			// Our fo:block wraps whatever result tree fragment
			// our style sheet produced when it applied-templates
			// to the child nodes
			Node n = childResults.nextNode();
			XmlUtils.treeCopy( n,  span );			
			
			DocumentFragment docfrag = document.createDocumentFragment();
			docfrag.appendChild(document.getDocumentElement());

			return docfrag;
						
		} catch (Exception e) {
			context.getLog().error(e.getMessage(), e);
		} 
    	
    	return null;
    	
    }
}
