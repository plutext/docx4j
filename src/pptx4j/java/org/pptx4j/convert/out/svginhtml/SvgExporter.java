package org.pptx4j.convert.out.svginhtml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.xml.dtm.ref.DTMNodeProxy;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.html.HtmlExporterNG;
import org.docx4j.convert.out.html.AbstractHtmlExporter.HtmlSettings;
import org.docx4j.dml.CTTextListStyle;
import org.docx4j.dml.CTTextParagraphProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.Tree;
import org.docx4j.model.styles.StyleTree.AugmentedStyle;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.PPr;
import org.docx4j.wml.Style;
import org.pptx4j.model.ResolvedLayout;
import org.pptx4j.model.TextStyles;
import org.pptx4j.pml.GroupShape;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

public class SvgExporter {
	
	protected static Logger log = Logger.getLogger(SvgExporter.class);	
	
	static Templates xslt;			
	static {
		try {
			Source xsltSource = new StreamSource(
						org.docx4j.utils.ResourceUtils.getResource(
								"org/pptx4j/convert/out/svginhtml/pptx2svginhtml.xslt"));
			xslt = XmlUtils.getTransformerTemplate(xsltSource);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public static void svg(PresentationMLPackage presentationMLPackage,
			ResolvedLayout layout) throws Exception {
	
		ByteArrayOutputStream intermediate = new ByteArrayOutputStream();
		Result intermediateResult =  new StreamResult( intermediate );
			
		svg(presentationMLPackage, layout, intermediateResult);
			
		String svg = intermediate.toString("UTF-8");
		log.debug(svg);
	}
	
	
	public static void svg(PresentationMLPackage presentationMLPackage,
			ResolvedLayout layout, javax.xml.transform.Result result
			) throws Exception {
    			
		org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(
				layout.getShapeTree(),
				Context.jcPML,
				"http://schemas.openxmlformats.org/presentationml/2006/main", "spTree", GroupShape.class); 	

    	HtmlSettings htmlSettings = new HtmlSettings();
		htmlSettings.setWmlPackage(presentationMLPackage);
		htmlSettings.getSettings().put("resolvedLayout", layout);
		
		org.docx4j.XmlUtils.transform(doc, xslt, htmlSettings.getSettings(), result);
	}

    public static DocumentFragment createBlockForP( 
    		PresentationMLPackage pmlPackage,
    		ResolvedLayout rl,
    		String lvl,
    		String cNvPrName,
    		String phType, NodeIterator childResults, NodeIterator lvlNpPr ) {
    	    	
    	
		StyleTree styleTree = null;
		try {
			styleTree = pmlPackage.getStyleTree();
		} catch (InvalidFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
		log.debug("lvl:" +lvl);
		int level;
		if (lvl.equals("NaN")) {
			level = 1;
		} else {
			level = Integer.parseInt(lvl);
		}
		String pStyleVal;
    	
		if (cNvPrName.toLowerCase().indexOf("subtitle")>-1
				|| phType.toLowerCase().indexOf("subtitle")>-1) {
			// Subtitle on first page in default layout is styled as a Body.
				pStyleVal = "Lvl" + level + "Master" + rl.getMasterNumber() + "Body";
		} else if (cNvPrName.toLowerCase().indexOf("title")>-1
			|| phType.toLowerCase().indexOf("title")>-1) {
			pStyleVal = "Lvl" + level + "Master" + rl.getMasterNumber() + "Title";
		} else {
			pStyleVal = "Lvl" + level + "Master" + rl.getMasterNumber() + "Body";			
		}
		
        try {
        	
        	
            // Create a DOM builder and parse the fragment			
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();
			
			//log.info("Document: " + document.getClass().getName() );

			Node xhtmlP = document.createElement("p");			
			document.appendChild(xhtmlP);
									    
			// Set @class
			log.debug(pStyleVal);
			Tree<AugmentedStyle> pTree = styleTree.getParagraphStylesTree();		
			org.docx4j.model.styles.Node<AugmentedStyle> asn = pTree.get(pStyleVal);
			((Element)xhtmlP).setAttribute("class", 
					StyleTree.getHtmlClassAttributeValue(pTree, asn)			
			);
			

			// Do we have CTTextParagraphProperties
			// <a:lvl?pPr>
			// Convert it to a WordML pPr
			CTTextParagraphProperties lvlPPr = unmarshalFormatting(lvlNpPr);
			if (lvlPPr!=null) {
			
				log.debug("We have lvlPPr");
				log.debug(
						XmlUtils.marshaltoString(lvlPPr, true, true, 
								Context.jcPML, "FIXME", "lvl1pPr",
								CTTextParagraphProperties.class)
						);
				PPr pPr = TextStyles.getWmlPPr(lvlPPr);
				if (pPr!=null) {
					StringBuffer inlineStyle =  new StringBuffer();
					HtmlExporterNG.createCss(pPr, inlineStyle);				
					if (!inlineStyle.toString().equals("") ) {
						((Element)xhtmlP).setAttribute("style", inlineStyle.toString() );
					}
				}
				// TODO RPR
			}
			
			// Our fo:block wraps whatever result tree fragment
			// our style sheet produced when it applied-templates
			// to the child nodes
			// init
			DTMNodeProxy n = (DTMNodeProxy)childResults.nextNode();
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
	        					XmlUtils.treeCopy( (Node)nodes.item(i),  xhtmlP );	        					
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
					XmlUtils.treeCopy( n,  xhtmlP );
				}
				// next 
				n = (DTMNodeProxy)childResults.nextNode();
				
			} while ( n !=null ); 
			
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


  private static CTTextParagraphProperties unmarshalFormatting(NodeIterator lvlNpPr ) {
	
	// Get the pPr node as a JAXB object,
	// so we can read it using our standard
	// methods.  Its a bit sad that we 
	// can't just adorn our DOM tree with the
	// original JAXB objects?
	try {
		//CTTextListStyle lstStyle = null;
		CTTextParagraphProperties pPr = null;
		
		if (lvlNpPr!=null) {
			Node n = lvlNpPr.nextNode();
			
			log.debug(n.getClass().getName());
			
			String str = XmlUtils.w3CDomNodeToString(n);
			//log.debug("'" + str + "'");
			
			// Convert to String first ... 
			// unmarshalling the node directly doesn't work as expected
			// (see comment in XmlUtils) 
			
//			if (n!=null) {
//				return  (CTTextParagraphProperties)XmlUtils.unmarshal(n, Context.jcPML, 
//						CTTextParagraphProperties.class);
//			}
			
			if (!str.equals("")) {
				return  (CTTextParagraphProperties)XmlUtils.unmarshalString(str, Context.jcPML, 
						CTTextParagraphProperties.class);
			}
		}
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	return null;
	
}
    
//    public static CTTextListStyle unmarshalFormatting(NodeIterator lstStyleNodeIt ) {
//		
//    	// Get the pPr node as a JAXB object,
//    	// so we can read it using our standard
//    	// methods.  Its a bit sad that we 
//    	// can't just adorn our DOM tree with the
//    	// original JAXB objects?
//    	try {
//			CTTextListStyle lstStyle = null;
//			if (lstStyleNodeIt!=null) {
//				Node n = lstStyleNodeIt.nextNode();
//				if (n!=null) {
//					return  (CTTextListStyle)XmlUtils.unmarshal(n, Context.jcPML, CTTextListStyle.class);
//				}
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
//		return null;
//    	
//    }
	
    public static String getCssForStyles(PresentationMLPackage pmlPackage) {
    	
    	StringBuffer result = new StringBuffer();
    	
		StyleTree styleTree=null;
		try {
			styleTree = pmlPackage.getStyleTree();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Second iteration - paragraph level pPr *and rPr*
		result.append("\n /* PARAGRAPH STYLES */ \n");    	
		Tree<AugmentedStyle> pTree = styleTree.getParagraphStylesTree();		
    	for (org.docx4j.model.styles.Node<AugmentedStyle> n : pTree.toList() ) {
    		Style s = n.getData().getStyle();

    		result.append( "."+ s.getStyleId()  + " {display:block;" );
        	if (s.getPPr()==null) {
        		log.debug("null pPr for style " + s.getStyleId());
        	} else {
        		HtmlExporterNG.createCss( s.getPPr(), result );
        	}
        	if (s.getRPr()==null) {
        		log.debug("null rPr for style " + s.getStyleId());
        	} else {
        		HtmlExporterNG.createCss(pmlPackage, s.getRPr(), result);
        	}
        	result.append( "}\n" );        	
    	}
		    	    	
    	if (log.isDebugEnabled()) {
    		return result.toString();
    	} else {
    		String debug = result.toString();
    		return debug;
    	}
    }
    

}
