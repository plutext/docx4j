package org.docx4j.convert.out.html;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.xml.dtm.ref.DTMNodeProxy;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.Converter;
import org.docx4j.convert.out.html.SymbolWriter;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.TransformState;
import org.docx4j.model.SymbolModel.SymbolModelTransformState;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.table.TableModel.TableModelTransformState;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.CTTblStylePr;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TrPr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.NodeIterator;

/**
 * Export a docx package to HTML.
 * 
 * This class aims for 2 improvements over the existing
 * HtmlExporter:
 * 
 * 1. No need to convert the docx4j WordprocessingMLPackage
 *    to Flat OPC format xml before applying the XSLT
 *    (we can do this by using XSLT extension functions 
 *     to get other parts eg a header from docx4j as necessary).
 *     
 * 2. Use docx4j do eg resolve styles, rather than trying
 *    to do this via XSLT (the old stylesheet is around
 *    7000 lines, and is largely undocumented and difficult
 *    to maintain)
 * 
 * But it doesn't handle table styles, though it could
 * easily enough.  It is superseded by HtmlExporterNG2. 
 * 
 * @author jason
 *
 */
public class HtmlExporterNG extends  AbstractHtmlExporter {
	
	
	protected static Logger log = Logger.getLogger(HtmlExporterNG.class);
	
	public static void log(String message ) {
		
		log.info(message);
	}
	
	static Templates xslt;		

	/**
	 * org/docx4j/convert/out/html/docx4j2xhtml.xslt will be used by default
	 * to transform the docx to html.
	 * This method allows you to use your own xslt instead.
	 * @param xslt
	 */	
	public static void setXslt(Templates xslt) {
		HtmlExporterNG.xslt = xslt;
	}
	
	static {
		try {
			Source xsltSource = new StreamSource(
						org.docx4j.utils.ResourceUtils.getResource(
								"org/docx4j/convert/out/html/docx4j2xhtml.xslt"));
			xslt = XmlUtils.getTransformerTemplate(xsltSource);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
	}

	
	// Implement the interface.  Everything in this class was
	// static, until now.
	
	WordprocessingMLPackage wmlPackage;
	public void setWmlPackage(WordprocessingMLPackage wmlPackage) {
		this.wmlPackage = wmlPackage;
	}

	HtmlSettings htmlSettings;
	public void setHtmlSettings(HtmlSettings htmlSettings) {
		this.htmlSettings = htmlSettings;
	}
	
	
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
	 *  on the way to generating PDF output. The Microsoft Conditional
	 *  Comments (supportMisalignedColumns, supportAnnotations,
	 *  and mso) which are defined in the XSLT are not inserted.
	 * 
	 * @param result
	 *            The javax.xml.transform.Result object to transform into 
	 * 
	 * */ 
	@Deprecated	
    public void html(WordprocessingMLPackage wmlPackage, javax.xml.transform.Result result,
    		String imageDirPath) throws Exception {
    	
    	html(wmlPackage, result, true, imageDirPath);
    }

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
	@Deprecated	
    public void html(WordprocessingMLPackage wmlPackage, javax.xml.transform.Result result, 
    		HtmlSettings htmlSettings) throws Exception {
    			
		org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(
				wmlPackage.getMainDocumentPart().getJaxbElement() ); 	
		
		//log.debug( XmlUtils.w3CDomNodeToString(doc));
			
		// Prep parameters
		if (htmlSettings==null) {
			htmlSettings = new HtmlSettings();
			// ..Ensure that the font names in the XHTML have been mapped to these matches
			//     possibly via an extension function in the XSLT
		}

		

		if (htmlSettings.getFontMapper()==null) {
			
			if (wmlPackage.getFontMapper()==null) {
				log.debug("Creating new Substituter.");
//				wmlPackage.setFontSubstituter(new SubstituterImplPanose());
				wmlPackage.setFontMapper(new IdentityPlusMapper());
			} else {
				log.debug("Using existing Substituter.");
			}
			htmlSettings.setFontMapper(wmlPackage.getFontMapper());
			log.debug("FontMapper set.. ");
		}
		
		htmlSettings.setWmlPackage(wmlPackage);
		
		// Allow arbitrary objects to be passed to the converters.
		// The objects are assumed to be specific to a particular converter (eg table),
		// so assume there will be one object implementing TransformState per converter.   
		HashMap<String, TransformState> modelStates  = new HashMap<String, TransformState>();
		htmlSettings.getSettings().put("modelStates", modelStates );
		
		//Converter c = new Converter();
		Converter.getInstance().registerModelConverter("w:tbl", new TableWriter() );
      	Converter.getInstance().registerModelConverter("w:sym", new SymbolWriter() );
		
		// By convention, the transform state object is stored by reference to the 
		// type of element to which its model applies
		modelStates.put("w:tbl", new TableModelTransformState() );
		modelStates.put("w:sym", new SymbolModelTransformState() );
		
		Converter.getInstance().start(wmlPackage);
		
		// Now do the transformation
		log.debug("About to transform...");
		org.docx4j.XmlUtils.transform(doc, xslt, htmlSettings.getSettings(), result);
		
		log.info("wordDocument transformed to xhtml ..");
    	
    }
    
    /* ---------------Xalan XSLT Extension Functions ---------------- */
    
    public static DocumentFragment createBlockForPPr( 
    		WordprocessingMLPackage wmlPackage,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults ) {
    	    	
    	// Note that this is invoked for every paragraph with a pPr node.
    	
    	// incoming objects are org.apache.xml.dtm.ref.DTMNodeIterator 
    	// which implements org.w3c.dom.traversal.NodeIterator

		if ( pStyleVal ==null || pStyleVal.equals("") ) {
			pStyleVal = "Normal";
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
        	
            // Create a DOM builder and parse the fragment			
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();
			
			//log.info("Document: " + document.getClass().getName() );

			Node xhtmlP = document.createElement("p");			
			document.appendChild(xhtmlP);
							
			if (log.isDebugEnabled() && pPr!=null) {					
				log.debug(XmlUtils.marshaltoString(pPr, true, true));					
			}				
		    
			// Set @class
			((Element)xhtmlP).setAttribute("class", 
					pStyleVal + PPR_COMPONENT + " " 
				  + pStyleVal + RPR_COMPONENT);
			
			
			// Does our pPr contain anything else?
			if (pPr!=null) {
				StringBuffer inlineStyle =  new StringBuffer();
				createCss(pPr, inlineStyle);				
				if (!inlineStyle.toString().equals("") ) {
					((Element)xhtmlP).setAttribute("style", inlineStyle.toString() );
				}
			}
			// Our fo:block wraps whatever result tree fragment
			// our style sheet produced when it applied-templates
			// to the child nodes
			Node n = childResults.nextNode();
			
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
			XmlUtils.treeCopy( (DTMNodeProxy)n,  xhtmlP );
			
			
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
					pStyleVal = "Normal";
				}

				// Set @class				
				if ( rPr.getRStyle()!=null) {
					String rStyleVal = rPr.getRStyle().getVal();
					if (!rStyleVal.toString().equals("") ) {
						((Element)span).setAttribute("class", rStyleVal + " " + pStyleVal + RPR_COMPONENT );
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
				XmlUtils.treeCopy( (DTMNodeProxy)n,  span );			
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
    
    public static final String PPR_COMPONENT = "_pPr";
    public static final String RPR_COMPONENT = "_rPr";
    
    public static String getCssForStyles(WordprocessingMLPackage wmlPackage) {
    	
    	StringBuffer result = new StringBuffer();
    	
    	Map stylesInUse = wmlPackage.getMainDocumentPart().getStylesInUse();
    	if (stylesInUse.get("Normal")==null) {
    		stylesInUse.put("Normal", "Normal");
    	}
    	

    	PropertyResolver propertyResolver = 
    		wmlPackage.getMainDocumentPart().getPropertyResolver();
    	
		Iterator it = stylesInUse.entrySet().iterator();
		// First iteration - paragraph level pPr *and rPr*
		result.append("\n /* PARAGRAPH STYLES */ \n");
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        String styleId = (String)pairs.getKey();
	        
	        Style s = propertyResolver.getStyle(styleId);
	        
	        if (s == null ) {
	        	log.error("Couldn't find style: " + styleId);
	        } else if (s.getType().equals("paragraph") ) {
	        	// the pPr component
	        	PPr pPr = propertyResolver.getEffectivePPr(styleId);
	        	if (pPr==null) {
	        		log.debug("null pPr for style " + styleId);
	        	} else {
		        	result.append( "."+styleId + PPR_COMPONENT + " {display:block;" );
		        	createCss( pPr, result);
		        	result.append( "}\n" );
	        	}
	        	// the rPr component
	        	RPr rPr = propertyResolver.getEffectiveRPr(styleId);
	        	if (rPr==null) {
	        		log.debug("null rPr for style " + styleId);
	        	} else {
		        	result.append( "."+styleId + RPR_COMPONENT + " {" );
		        	createCss(wmlPackage, rPr, result);
		        	result.append( "}\n" );
	        	}
	        } // ignore character, and table and numbering styles
	        
	    }
	    // Second iteration, character styles
		result.append("\n /* CHARACTER STYLES */ ");
		result.append("\n /* These come last, so they have more weight than the paragraph _rPr component styles */ \n ");
		it = stylesInUse.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        String styleId = (String)pairs.getKey();
	        
	        Style s = propertyResolver.getStyle(styleId);
	        
	        if (s == null ) {
	        	log.error("Couldn't find style: " + styleId);
	        } else if (s.getType().equals("character") ) {
	        	RPr rPr = propertyResolver.getEffectiveRPr(styleId);
	        	if (rPr==null) {
	        		log.debug("null rPr for style " + styleId);
	        	} else {
		        	result.append( "."+styleId + " {display:inline;" );
		        	createCss( wmlPackage, rPr, result);	        	
		        	result.append( "}\n" );
	        	}
	        } // ignore table and numbering styles
	        
	    }
	    return result.toString();
    }

    protected static void createCss(CTTblPrBase  tblPr, StringBuffer result) {
    	
		if (tblPr==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(tblPr);    	
    	for( Property p :  properties ) {
    		result.append(p.getCssProperty());
    	}    
    }
    protected static void createCss(List<CTTblStylePr> tblStylePrList, StringBuffer result) {
    	// STTblStyleOverrideType
    	
		if (tblStylePrList==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(tblStylePrList);    	
    	for( Property p :  properties ) {
    		result.append(p.getCssProperty());
    	}    
    }
    protected static void createCss(TrPr trPr, StringBuffer result) {
    	// includes jc, trHeight, wAfter, tblCellSpacing
    	
		if (trPr==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(trPr);    	
    	for( Property p :  properties ) {
    		result.append(p.getCssProperty());
    	}    
    }
    protected static void createCss(TcPr tcPr, StringBuffer result) {
    	// includes TcPrInner.TcBorders, CTShd, TcMar, CTVerticalJc
    	
		if (tcPr==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(tcPr);    	
    	for( Property p :  properties ) {
    		result.append(p.getCssProperty());
    	}    
    }
    
    
    protected static void createCss(PPr pPr, StringBuffer result) {
    	
		if (pPr==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(pPr);    	
    	for( Property p :  properties ) {
    		result.append(p.getCssProperty());
    	}    
    }
    
    
    protected static void createCss(WordprocessingMLPackage wmlPackage, RPr rPr, StringBuffer result) {

    	List<Property> properties = PropertyFactory.createProperties(wmlPackage, rPr);
    	
    	for( Property p :  properties ) {
    		result.append(p.getCssProperty());
    	}
    }

}