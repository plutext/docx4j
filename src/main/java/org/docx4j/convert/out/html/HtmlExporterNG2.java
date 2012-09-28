/*
 *  Copyright 2007-2010, Plutext Pty Ltd.
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
package org.docx4j.convert.out.html;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.Containerization;
import org.docx4j.convert.out.Converter;
import org.docx4j.convert.out.PageBreak;
import org.docx4j.jaxb.Context;
import org.docx4j.model.SymbolModel.SymbolModelTransformState;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.TransformState;
import org.docx4j.model.properties.paragraph.SpaceBefore;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.StyleTree.AugmentedStyle;
import org.docx4j.model.styles.Tree;
import org.docx4j.model.table.TableModel.TableModelTransformState;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RPr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

/**
 * HtmlExporterNG (now removed - see 
 * http://dev.plutext.org/trac/docx4j/browser/trunk/docx4j/src/main/java/org/docx4j/convert/out/html/HtmlExporterNG.java?rev=1238
 * ) had 2 salient features:
 * 
 * 1.  CSS .xx_pPr and .xx_rPr are created / split out for 
 *     each docx paragraph style
 *     
 * 2.  Each docx style is resolved into its effective
 *     style, by the PropertyResolver
 *     
 * When roundtripping HTML content, this had adverse
 * consequences (neither too bad, but adding to our work):
 * 
 * (a) It is harder to tell what has been added to the
 *     style definition in an editor (if/when that is 
 *     possible)
 *     
 * (b) Have to convert .xx_pPr and .xx_rPr back to xx
 * 
 * Also, we're not really taking advantage of the 
 * ability of CSS to cascade.
 * 
 * So this HtmlExporterNG2 takes a different approach.
 * It creates a css definition for each style in use
 * (either directly or indirectly), and in the
 * document, on the element's class attribute, puts the
 * set of style names in the hierarchy into . 
 *  
 * The CSS rules are written in a depth or breadth
 * first traversal of the style graph, so that  
 * conflicts are handled correctly.
 * 
 * TODO: handle document defaults.
 * 
 * 
 * @author jason
 *
 */
public class HtmlExporterNG2 extends  AbstractHtmlExporter {
	
	
	protected static Logger log = Logger.getLogger(HtmlExporterNG2.class);
	
	public static void log(String message ) {
		
		log.info(message);
	}

	static Templates xslt;		
	
	/**
	 * org/docx4j/convert/out/html/docx2xhtmlNG2.xslt will be used by default
	 * to transform the docx to html.
	 * This method allows you to use your own xslt instead.
	 * @param xslt
	 */		
	public static void setXslt(Templates xslt) {
		
		HtmlExporterNG2.xslt = xslt;
	}	
	
	static {
		try {
			Source xsltSource = new StreamSource(
						org.docx4j.utils.ResourceUtils.getResource(
								"org/docx4j/convert/out/html/docx2xhtmlNG2.xslt"));
			xslt = XmlUtils.getTransformerTemplate(xsltSource);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	
	// Implement the interface.  	
	
	public void output(javax.xml.transform.Result result) throws Docx4JException {
		
		if (wmlPackage==null) {
			throw new Docx4JException("Must setWmlPackage");
		}
		
		if (htmlSettings==null) {
			log.debug("Using empty HtmlSettings");
			htmlSettings = new HtmlSettings();			
		}		
		
		try {
			html(wmlPackage, result, htmlSettings);
		} catch (Exception e) {
			throw new Docx4JException("Failed to create HTML output", e);
		}		
		
	}
	
	// End interface
	
	/** Create an html version of the document, using CSS font family
	 *  stacks.  This is appropriate if the HTML is intended for
	 *  viewing in a web browser, rather than an intermediate step
	 *  on the way to generating PDF output (not that docx4j
	 *  supports that approach anymore). 
	 * 
	 * @param result
	 *            The javax.xml.transform.Result object to transform into 
	 * 
	 * */ 
	@Override
	@Deprecated	
    public void html(WordprocessingMLPackage wmlPackage, javax.xml.transform.Result result,
    		String imageDirPath) throws Exception {
    	
    	html(wmlPackage, result, true, imageDirPath);
    }

	@Override
	@Deprecated
    public void html(WordprocessingMLPackage wmlPackage, javax.xml.transform.Result result, boolean fontFamilyStack,
    		String imageDirPath) throws Exception {

		// Prep parameters
    	HtmlSettings htmlSettings = new HtmlSettings();
    	htmlSettings.setFontFamilyStack(fontFamilyStack);
    	
    	if (imageDirPath==null) {
    		imageDirPath = "";
    	}
    	htmlSettings.setImageDirPath(imageDirPath);    	
    	
		html(wmlPackage, result, htmlSettings);
    }
    
	/** Create an html version of the document. 
	 * 
	 * @param result
	 *            The javax.xml.transform.Result object to transform into 
	 * 
	 * */ 
	@Override
	public void html(WordprocessingMLPackage wmlPackage,
			javax.xml.transform.Result result, HtmlSettings htmlSettings)
			throws Exception {

		// Containerization of borders/shading
		MainDocumentPart mdp = wmlPackage.getMainDocumentPart();
		// Don't change the user's Document object; create a tmp one
		org.docx4j.wml.Document tmpDoc = XmlUtils.deepCopy(wmlPackage
				.getMainDocumentPart().getJaxbElement());
		Containerization.groupAdjacentBorders(tmpDoc.getBody());
		PageBreak.movePageBreaks(tmpDoc.getBody());

		org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(tmpDoc);

		// log.debug( XmlUtils.w3CDomNodeToString(doc));

		// Prep parameters
		if (htmlSettings == null) {
			htmlSettings = new HtmlSettings();
			// ..Ensure that the font names in the XHTML have been mapped to
			// these matches
			// possibly via an extension function in the XSLT
		}

		// Ensure that the imageHandler is set up
		boolean privateImageHandler = false;
		if (htmlSettings.getImageHandler() == null) {
			htmlSettings.setImageHandler(
				new HTMLConversionImageHandler(htmlSettings.getImageDirPath(), 
											   htmlSettings.getImageTargetUri(), 
											   htmlSettings.isImageIncludeUUID()));
			privateImageHandler = true;
		}
		
		if (htmlSettings.getFontMapper() == null) {
			htmlSettings.setFontMapper(wmlPackage.getFontMapper());
			log.debug("FontMapper set.. ");
		}

		htmlSettings.setWmlPackage(wmlPackage);

		// Allow arbitrary objects to be passed to the converters.
		// The objects are assumed to be specific to a particular converter (eg
		// table),
		// so assume there will be one object implementing TransformState per
		// converter.
		HashMap<String, TransformState> modelStates = new HashMap<String, TransformState>();
		htmlSettings.getSettings().put("modelStates", modelStates);

		// Converter c = new Converter();
		Converter.getInstance().registerModelConverter("w:tbl",
				new TableWriter());
		Converter.getInstance().registerModelConverter("w:sym",
				new SymbolWriter());

		// By convention, the transform state object is stored by reference to
		// the
		// type of element to which its model applies
		modelStates.put("w:tbl", new TableModelTransformState());
		modelStates.put("w:sym", new SymbolModelTransformState());

		// .. although that convention can be bent ..
		modelStates.put("footnoteNumber", new FootnoteState());
		modelStates.put("endnoteNumber", new EndnoteState());

		Converter.getInstance().start(wmlPackage);

		// Now do the transformation
		log.debug("About to transform...");
		org.docx4j.XmlUtils.transform(doc, xslt, htmlSettings.getSettings(),
				result);

		if (privateImageHandler) {
			//remove a locally created imageHandler in case the HtmlSettings get reused
			htmlSettings.getSettings().remove(HtmlSettings.IMAGE_HANDLER);
		}
		log.info("wordDocument transformed to xhtml ..");

	}
        
    /* ---------------Xalan XSLT Extension Functions ---------------- */

	
	public static DocumentFragment notImplemented(NodeIterator nodes, String message) {

		Node n = nodes.nextNode();
		log.warn("NOT IMPLEMENTED: support for "+ n.getNodeName() + "; " + message);
		
		if (log.isDebugEnabled() ) {
			
			if (message==null) message="";
			
			log.debug( XmlUtils.w3CDomNodeToString(n)  );

			// Return something which will show up in the HTML
			return message("NOT IMPLEMENTED: support for " + n.getNodeName() + " - " + message);
		} else {
			
			// Put it in a comment node instead?
			
			return null;
		}
	}
	
	public static DocumentFragment message(String message) {
		
		if (!log.isDebugEnabled()) return null;

		String html = "<div style=\"color:red\" >"
			+ message
			+ "</div>";  

		javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory
				.newInstance();
		dbf.setNamespaceAware(true);
		StringReader reader = new StringReader(html);
		InputSource inputSource = new InputSource(reader);
		Document doc = null;
		try {
			doc = dbf.newDocumentBuilder().parse(inputSource);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reader.close();

		DocumentFragment docfrag = doc.createDocumentFragment();
		docfrag.appendChild(doc.getDocumentElement());
		return docfrag;		
	}
	
	
	
    
    
    public static DocumentFragment createBlockForSdt( 
    		WordprocessingMLPackage wmlPackage,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults, String tag) {
    	
    	DocumentFragment docfrag = createBlock( wmlPackage,
        		 pPrNodeIt,
        		 pStyleVal,  childResults,
        		 "div");
    	    	    
    	return docfrag;
    }	    

    public static DocumentFragment createBlockForPPr( 
    		WordprocessingMLPackage wmlPackage,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults ) {

    	return createBlock( 
        		 wmlPackage,
        		 pPrNodeIt,
        		 pStyleVal,  childResults,
        		  "p" );
    	
    }
    
    private static DocumentFragment createBlock( 
    		WordprocessingMLPackage wmlPackage,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults,
    		String htmlElementName ) {
    	

		StyleTree styleTree = wmlPackage.getMainDocumentPart().getStyleTree();
    	
    	// Note that this is invoked for every paragraph with a pPr node.
    	
    	// incoming objects are org.apache.xml.dtm.ref.DTMNodeIterator 
    	// which implements org.w3c.dom.traversal.NodeIterator

		if ( pStyleVal ==null || pStyleVal.equals("") ) {
//			pStyleVal = "Normal";
			pStyleVal = wmlPackage.getMainDocumentPart().getStyleDefinitionsPart().getDefaultParagraphStyle().getStyleId();
		}
    	log.debug("style '" + pStyleVal );     		
    	
//    	log.info("pPrNode:" + pPrNodeIt.getClass().getName() ); // org.apache.xml.dtm.ref.DTMNodeIterator    	
//    	log.info("childResults:" + childResults.getClass().getName() ); 
    	    	
    	
        try {
        	
        	// Get the pPr node as a JAXB object,
        	// so we can read it using our standard
        	// methods.  Its a bit sad that we 
        	// can't just adorn our DOM tree with the
        	// original JAXB objects?
        	PPr pPr = null;
        	if (pPrNodeIt!=null) {
        		Node n = pPrNodeIt.nextNode();
        		if (n!=null) {
        			Unmarshaller u = Context.jc.createUnmarshaller();			
        			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
        			Object jaxb = u.unmarshal(n);
        			try {
        				pPr =  (PPr)jaxb;
        			} catch (ClassCastException e) {
        		    	log.error("Couldn't cast " + jaxb.getClass().getName() + " to PPr!");
        			}        	        			
        		}
        	}
        	
            // Create a DOM document to take the results			
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();			
				//log.info("Document: " + document.getClass().getName() );
			Element xhtmlBlock = document.createElement(htmlElementName);			
			document.appendChild(xhtmlBlock);
							
			if (log.isDebugEnabled() && pPr!=null) {					
				log.debug(XmlUtils.marshaltoString(pPr, true, true));					
			}				
		    
			// Set @class
			log.debug(pStyleVal);
			Tree<AugmentedStyle> pTree = styleTree.getParagraphStylesTree();		
			org.docx4j.model.styles.Node<AugmentedStyle> asn = pTree.get(pStyleVal);
			xhtmlBlock.setAttribute("class", 
					StyleTree.getHtmlClassAttributeValue(pTree, asn)			
			);
		
			
			// Does our pPr contain anything else?
			boolean ignoreBorders = (htmlElementName.equals("p"));
			if (pPr!=null) {
				StringBuffer inlineStyle =  new StringBuffer();
				createCss(wmlPackage, pPr, inlineStyle, ignoreBorders);				
				if (!inlineStyle.toString().equals("") ) {
					xhtmlBlock.setAttribute("style", inlineStyle.toString() );
				}
				if (!inlineStyle.toString().contains(SpaceBefore.CSS_NAME) ) {
			    	// If there is no w:spacing/@w:before, it should be 0.
			    	// unless it is set to anything in the effective style 
			    	PropertyResolver propertyResolver = 
			        		wmlPackage.getMainDocumentPart().getPropertyResolver();
					PPr stylePPr = propertyResolver.getEffectivePPr(pStyleVal);
					if (stylePPr.getSpacing()!=null 
							&& stylePPr.getSpacing().getBefore()!=null) {
						// it is specified in the style
					} else {
						/* For a doc where there is no w:spacing/@w:before specified
						 * in the styles, Word's HTML output defaults to 
 
							 p.MsoNormal, li.MsoNormal, div.MsoNormal
								{
								margin-top:0cm;
								margin-right:0cm;
								margin-bottom:10.0pt;
								margin-left:0cm;
								line-height:115%;  <------- from w:spacing/@w:line?
								mso-pagination:widow-orphan;
								font-size:11.0pt;
								font-family:"Calibri","sans-serif";
								
							Here, for now, we are just dealing with 
							w:spacing/@w:before.
						 */
						xhtmlBlock.setAttribute("style", inlineStyle.toString() + "; margin-top:0cm; " );
						
					}
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
					log.debug("handling DOCUMENT_NODE");
					// Do just enough of the handling here
	                NodeList nodes = n.getChildNodes();
	                if (nodes != null) {
	                    for (int i=0; i<nodes.getLength(); i++) {
	                    	
	        				if (((Node)nodes.item(i)).getLocalName().equals("span")
	        						&& ! ((Node)nodes.item(i)).hasChildNodes() ) {
	        					// ignore
	        					log.debug(".. ignoring <span/> ");
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
			e.printStackTrace();
			System.out.println(e.toString() );
			log.error(e);
		} 
    	
    	return null;
    	
    }

    public static DocumentFragment createBlockForRPr( 
    		WordprocessingMLPackage wmlPackage,
    		String pStyleVal,
    		NodeIterator rPrNodeIt,
    		NodeIterator childResults ) {
    
    	StyleTree styleTree = wmlPackage.getMainDocumentPart().getStyleTree();
    	    	
    	// Note that this is invoked for every paragraph with a pPr node.
    	
    	// incoming objects are org.apache.xml.dtm.ref.DTMNodeIterator 
    	// which implements org.w3c.dom.traversal.NodeIterator

    	
//    	log.info("rPrNode:" + rPrNodeIt.getClass().getName() ); // org.apache.xml.dtm.ref.DTMNodeIterator    	
//    	log.info("childResults:" + childResults.getClass().getName() ); 
    	
    	
        try {
        	
        	// Get the pPr node as a JAXB object,
        	// so we can read it using our standard
        	// methods.  Its a bit sad that we 
        	// can't just adorn our DOM tree with the
        	// original JAXB objects?
			Unmarshaller u = Context.jc.createUnmarshaller();			
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
			Object jaxb = u.unmarshal(rPrNodeIt.nextNode());
			
			RPr rPr = null;
			try {
				rPr =  (RPr)jaxb;
			} catch (ClassCastException e) {
		    	log.error("Couldn't cast " + jaxb.getClass().getName() + " to RPr!");
			}
        	
            // Create a DOM builder and parse the fragment
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();
			
			//log.info("Document: " + document.getClass().getName() );

			Node span = document.createElement("span");			
			document.appendChild(span);
			
			if (rPr==null) {
				Text err = document.createTextNode( "Couldn't cast " + jaxb.getClass().getName() + " to PPr!" );
				span.appendChild(err);
				
			} else {
				
				if (log.isDebugEnabled()) {					
					log.debug(XmlUtils.marshaltoString(rPr, true, true));					
				}
				
				if (pStyleVal==null || pStyleVal.equals("")) {
//					pStyleVal = "Normal";
					pStyleVal = wmlPackage.getMainDocumentPart().getStyleDefinitionsPart().getDefaultParagraphStyle().getStyleId();
				}

				// Set @class	
				if ( rPr.getRStyle()!=null) {
					String rStyleVal = rPr.getRStyle().getVal();
					Tree<AugmentedStyle> cTree = styleTree.getCharacterStylesTree();		
					org.docx4j.model.styles.Node<AugmentedStyle> asn = cTree.get(rStyleVal);
					if (asn==null) {
						log.warn("No style node for: " + rStyleVal);
					} else {
						((Element)span).setAttribute("class", 
								StyleTree.getHtmlClassAttributeValue(cTree, asn)			
						);		
					}
				}
				
				// Does our rPr contain anything else?
				StringBuffer inlineStyle =  new StringBuffer();
				createCss(wmlPackage, rPr, inlineStyle);				
				if (!inlineStyle.toString().equals("") ) {
					((Element)span).setAttribute("style", inlineStyle.toString() );
				}
				
				
				// Our fo:block wraps whatever result tree fragment
				// our style sheet produced when it applied-templates
				// to the child nodes
				Node n = childResults.nextNode();
				XmlUtils.treeCopy( n,  span );			
			}
			
			DocumentFragment docfrag = document.createDocumentFragment();
			docfrag.appendChild(document.getDocumentElement());

			return docfrag;
						
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString() );
			log.error(e);
		} 
    	
    	return null;
    	
    }
    
    public static int getNextFootnoteNumber(HashMap<String, TransformState> modelStates) {
    	
    	FootnoteState fs = (FootnoteState)modelStates.get("footnoteNumber");
    	return fs.getNextFootnoteNumber();
    }
    
    public static class FootnoteState implements TransformState {
    
	    int footnoteNumber=0;
	    public int getNextFootnoteNumber() {
	    	footnoteNumber++;
	    	return footnoteNumber;
	    	
	    }
    }

    public static int getNextEndnoteNumber(HashMap<String, TransformState> modelStates) {
    	
    	EndnoteState fs = (EndnoteState)modelStates.get("endnoteNumber");
    	return fs.getNextEndnoteNumber();
    }
    
    public static class EndnoteState implements TransformState {
    
	    int endnoteNumber=0;
	    public int getNextEndnoteNumber() {
	    	endnoteNumber++;
	    	return endnoteNumber;
	    	
	    }
    }
    
   
}