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

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.TransformerException;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.XsltCommonFunctions;
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
import org.docx4j.model.styles.StyleTree.AugmentedStyle;
import org.docx4j.model.styles.Tree;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.Ind;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static Logger log = LoggerFactory.getLogger(XsltHTMLFunctions.class);
    
	
	/*
		<head><meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
			<style>
				<xsl:comment>

					/#paged media #/ div.header {display: none }
					div.footer {display: none } /#@media print { #/
					<xsl:if
						test="java:org.docx4j.convert.out.common.XsltCommonFunctions.hasDefaultHeader($conversionContext)">
						div.header {display: block; position: running(header) }
					</xsl:if>
					<xsl:if
						test="java:org.docx4j.convert.out.common.XsltCommonFunctions.hasDefaultFooter($conversionContext)">
						div.footer {display: block; position: running(footer) }
					</xsl:if>

					@page { size: A4; margin: 10%; @top-center {
					content: element(header) } @bottom-center {
					content: element(footer) } }


					/#font definitions#/

					/#element styles#/ .del
					{text-decoration:line-through;color:red;}
					<xsl:choose>
						<xsl:when test="/w:document/w:settings/w:trackRevisions">
  					          .ins {text-decoration:none;background:#c0ffc0;padding:1px;}
						</xsl:when>
						<xsl:otherwise>
						  .ins {text-decoration:none;background:#c0ffc0;padding:1px;}
						</xsl:otherwise>
					</xsl:choose>


					/# Word style definitions #/
					<xsl:copy-of
						select="java:org.docx4j.convert.out.html.XsltHTMLFunctions.getCssForStyles( 
		  											$conversionContext)" />

					/# TABLE CELL STYLES #/
					<xsl:variable name="tables" select="./w:body//w:tbl" />
					<xsl:copy-of
						select="java:org.docx4j.convert.out.html.XsltHTMLFunctions.getCssForTableCells( 
		  											$conversionContext, $tables)" />

					
				</xsl:comment>
			</style>

			<xsl:value-of select="$userCSS" disable-output-escaping="yes" />

			<script type="text/javascript">
				<!-- For collapsible content controls -->
				function toggleDiv(divid){
					if(document.getElementById(divid).style.display == 'none'){
						document.getElementById(divid).style.display = 'block';
					}else{
						document.getElementById(divid).style.display = 'none';
					}
				}
								
			</script>
			
			<!-- User script -->
			<xsl:value-of select="$userScript" disable-output-escaping="yes" />
			
		</head>
	 */
	
	public static DocumentFragment appendHeadElement(HTMLConversionContext conversionContext) {
		// There is a similar method for the non XSLT case, in HTMLExporterVisitorDelegate
		
        // Create a DOM document to take the results			
		Document document = XmlUtils.getNewDocumentBuilder().newDocument();
		
	    Element	headEl = document.createElement("head");
		Element meta = document.createElement("meta");
		Element element = null;
		StringBuilder buffer = new StringBuilder(10240);
		
    	document.appendChild(headEl);   
    	
    	// <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    	meta.setAttribute("http-equiv", "Content-Type");
    	meta.setAttribute("content", "text/html; charset=utf-8");
    	headEl.appendChild(meta);
    	
    	// <style..
    	element = createStyleElement(conversionContext, document, buffer);
		if (element != null) {
			headEl.appendChild(element);
		}

		
		// <script
		buffer.setLength(0);
		element = createScriptElement(conversionContext, document, buffer);
		if (element != null) {
			headEl.appendChild(element);
		}
		
		DocumentFragment docfrag = document.createDocumentFragment();
		docfrag.appendChild(document.getDocumentElement());

		return docfrag;
		

	}

	/**
	 * A customised XSLT might just want the <style> element, because it customises
	 * the rest of the <head> element.
	 * 
	 * @param conversionContext
	 * @return
	 */
	public static DocumentFragment appendStyleElement(HTMLConversionContext conversionContext) {
		
        // Create a DOM document to take the results			
		Document document = XmlUtils.getNewDocumentBuilder().newDocument();

		StringBuilder buffer = new StringBuilder(10240);
		
    	// <style..
    	Element element = createStyleElement(conversionContext, document, buffer);
		if (element == null) {
			return null;
		}
		document.appendChild(element);
	
		DocumentFragment docfrag = document.createDocumentFragment();
		docfrag.appendChild(document.getDocumentElement());

		return docfrag;
	}

	/**
	 * A customised XSLT might just want the &lt;script> element, because it customises
	 * the rest of the <head> element.
	 * 
	 * @param conversionContext
	 * @return
	 */
	public static DocumentFragment appendScriptElement(HTMLConversionContext conversionContext) {
		// This method is necessary, since it is how the customised XSLT
		// actually invokes ConversionHTMLScriptElementHandler
		
        // Create a DOM document to take the results			
		Document document = XmlUtils.getNewDocumentBuilder().newDocument();

		StringBuilder buffer = new StringBuilder(10240);
		
    	// <script..
    	Element element = createScriptElement(conversionContext, document, buffer);
		if (element == null) {
			return null;
		}
		document.appendChild(element);
	
		DocumentFragment docfrag = document.createDocumentFragment();
		docfrag.appendChild(document.getDocumentElement());

		return docfrag;

	}
	
	private static Element createStyleElement(HTMLConversionContext conversionContext, Document document, StringBuilder buffer) {

		String userCSS = conversionContext.getUserCSS();
		
		boolean hasDefaultHeader = false;
		boolean hasDefaultFooter = false;

		//TODO: This doesn't quite work as the defaultHeader and defaultFooter are per section,
    	//but this definition is on the document level. 
    	//To access the first section, we have to call first a next() and later return to start()
    	try {
//    		conversionContext.getSections().next(); // causes exception
        	hasDefaultHeader = XsltCommonFunctions.hasDefaultHeader(conversionContext);
        	hasDefaultFooter = XsltCommonFunctions.hasDefaultHeader(conversionContext);
    	}
    	finally {
//    		conversionContext.getSections().start();
    	}
    	HtmlCssHelper.createDefaultCss(hasDefaultHeader, hasDefaultFooter, buffer);
		HtmlCssHelper.createCssForStyles(conversionContext.getWmlPackage(), 
										 conversionContext.getStyleTree(), 
										 buffer);
		if ((userCSS != null) && (userCSS.length() > 0)) {
			buffer.append(userCSS);
		}
		return conversionContext.createStyleElement(document, buffer.toString());
	}

	private static Element createScriptElement(HTMLConversionContext conversionContext, Document document, StringBuilder buffer) {
		
		String userScript = conversionContext.getUserScript();
		HtmlScriptHelper.createDefaultScript(buffer);
		if ((userScript != null) && (userScript.length() > 0)) {
			buffer.append(userScript);
		}
		return conversionContext.createScriptElement(document, buffer.toString());
	}
	
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

    public static DocumentFragment createListItemBlockForPPr( 
    		HTMLConversionContext context,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults ) {

    	return createBlock( 
        		 context,
        		 pPrNodeIt,
        		 pStyleVal,  childResults,
        		  "li" );
    	
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
			Document document = XmlUtils.getNewDocumentBuilder().newDocument();			
				//log.info("Document: " + document.getClass().getName() );
			Element xhtmlBlock = document.createElement(htmlElementName);			
			document.appendChild(xhtmlBlock);
							
			if (context.getLog().isDebugEnabled() && pPr!=null) {					
				context.getLog().debug(XmlUtils.marshaltoString(pPr, true, true));					
			}				
		    
			// Set @class
			context.getLog().debug(pStyleVal);
			Tree<AugmentedStyle> pTree = styleTree.getParagraphStylesTree();		
			org.docx4j.model.styles.Node<AugmentedStyle> asn = pTree.get(pStyleVal);
			String classVal = StyleTree.getHtmlClassAttributeValue(pTree, asn);			
			xhtmlBlock.setAttribute("class", classVal);
		
			
			// Does our pPr contain anything else?
			boolean ignoreBorders = (htmlElementName.equals("p"));
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
				HtmlCssHelper.createCss(context.getWmlPackage(), pPr, inlineStyle, ignoreBorders,
						xhtmlBlock.getNodeName().equals("li"));	
				if (!inlineStyle.toString().equals("") ) {
					xhtmlBlock.setAttribute("style", inlineStyle.toString() );
				}
			}

			// Our element (eg <p>) wraps whatever result tree fragment
			// our style sheet produced when it applied-templates
			// to the child nodes
			// init
			Node n = childResults.nextNode();
			
			if (xhtmlBlock.getNodeName().equals("p") 
					&& context.getBookmarkStart()!=null ) {
				
				xhtmlBlock.setAttribute("id", context.getBookmarkStart().getName() );
				context.setBookmarkStart(null);
			}
			
			
			if (xhtmlBlock.getNodeName().equals("p")
					&& n.hasChildNodes()
					&& n.getChildNodes().item(0).getLocalName().equals("span")) {
					// old XSLT won't produce a span for w:r unless there is w:rPr
				
				mergeSpans(n.getChildNodes(), document, xhtmlBlock);
				
			} else {
			
				do {	
					
	//				System.out.println("\n\n" + XmlUtils.w3CDomNodeToString(n) + "\n\n");
					
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
			}
			
			
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
    
    /**
     * Merge adjacent spans if @class and @style are the same
     * @param nodes
     * @param result
     */
    private static void mergeSpans(NodeList nodes, Document document, Element xhtmlBlock) {
    	
    	if (nodes==null || nodes.getLength()==0) return;
    	
    	// init .. skip children until we find a span
    	int startIndex = 0;
    	Element currentEl; 
    	while (true) {
    		currentEl = ((Element)nodes.item(startIndex));
    		    		
        	if (currentEl.getLocalName().equals("span")) {
            	break;        		
        	} else {
            	XmlUtils.treeCopy( currentEl,  xhtmlBlock );
        	}
        	
        	startIndex++;
        	if (startIndex == nodes.getLength() ) return;
    	}
    	
    	Element currentSpan = currentEl;
    	String currentClass = currentSpan.getAttribute("class");
    	String currentStyle = currentSpan.getAttribute("style");
    		// should work for anything with @class, @style
    	
    	// Can't reuse existing span, since we'll get org.apache.xml.dtm.DTMDOMException
		Element newSpan = document.createElement("span");	
		if (currentClass!=null) {
			newSpan.setAttribute("class", currentClass);
		}
		if (currentStyle!=null) {
			newSpan.setAttribute("style", currentStyle);
		}

    	XmlUtils.treeCopy( currentSpan.getChildNodes(),  newSpan );
    	xhtmlBlock.appendChild(newSpan);
    	
    	//System.out.println(XmlUtils.w3CDomNodeToString(xhtmlBlock));
    	
//    	if (nodes.getLength()==1) return;
    	
    	for (int i=(startIndex+1); i<nodes.getLength(); i++) {
    		
        	Element thisSpan = ((Element)nodes.item(i));
        	
        	// Handle elements other than span eg img
        	if (!thisSpan.getLocalName().equals("span")) {
            	XmlUtils.treeCopy( thisSpan,  xhtmlBlock );
        		
            	// Get read for next span
        		newSpan = document.createElement("span");	
            	xhtmlBlock.appendChild(newSpan); // might end up with an empty span
        		if (currentClass!=null) {
        			newSpan.setAttribute("class", currentClass);
        		}
        		if (currentStyle!=null) {
        			newSpan.setAttribute("style", currentStyle);
        		}
        		continue;
        	}
        	
        	// Handle span
        	if (!thisSpan.hasChildNodes()) continue;
        	
        	String thisClass = thisSpan.getAttribute("class");
        	String thisStyle = thisSpan.getAttribute("style");
    		
        	boolean classSame = (currentClass==null && thisClass==null)
        			|| (currentClass!=null && currentClass.equals(thisClass));
        	boolean styleSame = (currentStyle==null && thisStyle==null)
        			|| (currentStyle!=null && currentStyle.equals(thisStyle));
        		// TODO handle case where only difference is "white-space:pre-wrap;"
        	
        	if (classSame && styleSame) {
        		// add to existing
				XmlUtils.treeCopy( thisSpan.getChildNodes(),  newSpan );
        		
        	} else {
        		newSpan = document.createElement("span");	
        		if (thisClass!=null) {
        			newSpan.setAttribute("class", thisClass);
        		}
        		if (thisStyle!=null) {
        			newSpan.setAttribute("style", thisStyle);
        		}
            	
            	XmlUtils.treeCopy( thisSpan.getChildNodes(),  newSpan );
            	xhtmlBlock.appendChild(newSpan);
            	currentClass = thisClass;
            	currentStyle = thisStyle;
        	}
    	}
    }
    

    public static DocumentFragment createBlockForRPr( 
    		HTMLConversionContext context,
    		String pStyleVal,
    		NodeIterator rPrNodeIt,
    		NodeIterator childResults ) {

		Style defaultRunStyle = 
				(context.getWmlPackage().getMainDocumentPart().getStyleDefinitionsPart(false) != null ?
				context.getWmlPackage().getMainDocumentPart().getStyleDefinitionsPart(false).getDefaultCharacterStyle() :
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

			// Our span wraps whatever result tree fragment
			// our style sheet produced when it applied-templates
			// to the child nodes
			Node n = childResults.nextNode();
			
//	    	System.out.println(XmlUtils.w3CDomNodeToString(n));
			

            // Create a DOM builder and parse the fragment
			Document document = XmlUtils.getNewDocumentBuilder().newDocument();
				
			Element span = document.createElement("span");			
			document.appendChild(span);
			
    		// Avoid unnecessary nested span for common case of single child
        	if (n.hasChildNodes() && n.getChildNodes().getLength()==1
        			&& n.getChildNodes().item(0).getLocalName().equals("span")) {
        		        		
        		String existingStyle = ((Element)n.getChildNodes().item(0)).getAttribute("style");
				span.setAttribute("style", existingStyle );				
				setSpanAttr(context, defaultCharacterStyleId, styleTree, rPr, span);
				XmlUtils.treeCopy( n.getChildNodes().item(0).getChildNodes(),  span );			
        		
//        	} else if (!n.getChildNodes().item(0).getLocalName().equals("span")) {
//        		
//        		// EXP - for backwards compat with old code where w:t didn't create span
//				XmlUtils.treeCopy( n,  span );

        	} else {
        		
				setSpanAttr(context, defaultCharacterStyleId, styleTree, rPr, span);
				//XmlUtils.treeCopy( n,  span );
				mergeSpans(n.getChildNodes(), document, span);
				
        	}
			DocumentFragment docfrag = document.createDocumentFragment();
			docfrag.appendChild(document.getDocumentElement());

			return docfrag;
        	
						
		} catch (Exception e) {
			context.getLog().error(e.getMessage(), e);
		} 
    	
    	return null;
    	
    }

	/**
	 * @param context
	 * @param defaultCharacterStyleId
	 * @param styleTree
	 * @param rPr
	 * @param span
	 */
	private static void setSpanAttr(HTMLConversionContext context,
			String defaultCharacterStyleId, StyleTree styleTree, RPr rPr,
			Element span) {
		
		// Set @class	
		String rStyleVal = defaultCharacterStyleId;
		if ( rPr!=null && rPr.getRStyle()!=null) {
			rStyleVal = rPr.getRStyle().getVal();
		}
		Tree<AugmentedStyle> cTree = styleTree.getCharacterStylesTree();		
		org.docx4j.model.styles.Node<AugmentedStyle> asn = cTree.get(rStyleVal);
		if (asn==null) {
			context.getLog().warn("Can't set @class; No style node for: " + rStyleVal);
		} else {
			String classVal = StyleTree.getHtmlClassAttributeValue(cTree, asn);
			if (classVal!=null && !classVal.equals("")) {
				((Element)span).setAttribute("class", classVal);
			}
		}
		
		if (rPr!=null) {
			
			if (context.getLog().isDebugEnabled()) {					
				context.getLog().debug(XmlUtils.marshaltoString(rPr, true, true));					
			}
			
			// Does our rPr contain anything else?
			StringBuilder inlineStyle =  new StringBuilder();
			HtmlCssHelper.createCss(context.getWmlPackage(), rPr, inlineStyle);				
			if (inlineStyle.toString().equals("") ) {
				// Do nothing here - just keep existing style
				// (which if present, is a font definition obtained at w:t level)
			} else {
				String existingStyle = span.getAttribute("style");
				if (existingStyle==null
						|| existingStyle.trim().equals("")) {
					span.setAttribute("style", inlineStyle.toString() );
				} else {
					span.setAttribute("style", inlineStyle.toString() + ";" + existingStyle );					
				}
			}
		}
	}
}
