package org.docx4j.convert.out.html;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.vfs.CacheStrategy;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.log4j.Logger;
import org.apache.xml.dtm.ref.DTMNodeProxy;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.Output;
import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.BestMatchingMapper;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.listnumbering.Emulator;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.docx4j.wml.UnderlineEnumeration;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;

import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

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
 * @author jason
 *
 */
public class HtmlExporterNG extends  AbstractHtmlExporter {
	
	
	protected static Logger log = Logger.getLogger(HtmlExporterNG.class);
	
	public static void log(String message ) {
		
		log.info(message);
	}
	
	static Templates xslt;			
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
    public void html(WordprocessingMLPackage wmlPackage, javax.xml.transform.Result result,
    		String imageDirPath) throws Exception {
    	
    	html(wmlPackage, result, true, imageDirPath);
    }

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
    	
    	PropertyResolver propertyResolver = 
    		wmlPackage.getMainDocumentPart().getPropertyResolver();
    	
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
    
    	PropertyResolver propertyResolver = 
    		wmlPackage.getMainDocumentPart().getPropertyResolver();
    	
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
    
    public static DocumentFragment createTable( 
    		WordprocessingMLPackage wmlPackage,
    		NodeIterator tblNodeIt ) {
    	    	
        try {
        	
			Unmarshaller u = Context.jc.createUnmarshaller();			
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
			Object jaxb = u.unmarshal(tblNodeIt.nextNode());
			
			Tbl tbl = null;
			try {
				tbl =  (Tbl)jaxb;
			} catch (ClassCastException e) {
		    	log.error("Couldn't cast " + jaxb.getClass().getName() + " to tbl!");
			}        	
        	
            // Create a DOM builder and parse the fragment			
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();
			
			//log.info("Document: " + document.getClass().getName() );

			Node xhtmlTable = document.createElement("table");			
			document.appendChild(xhtmlTable);
			
			if (log.isDebugEnabled()) {					
				log.debug(XmlUtils.marshaltoString(tbl, true, true));					
			}
			
			// Just simple stuff for now
			List<Object> rows = tbl.getEGContentRowContent();
			for( Object o : rows) {
				
				Tr tr = (Tr)o;
				
				Node xhtmlTr = document.createElement("tr");			
				xhtmlTable.appendChild(xhtmlTr);
				
				List<Object> cells = tr.getEGContentCellContent();
				for( Object o2 : cells) {
					
					Node xhtmlTd = document.createElement("td");
					xhtmlTr.appendChild(xhtmlTd);
					
					// Now, in the td, just put the original
					// *wml* content, which our XSLT can transform
					Tc tc = (Tc)((JAXBElement)o2).getValue();
					org.w3c.dom.Document tdDoc = XmlUtils.marshaltoW3CDomDocument(tc);
					
					
					/* xhtmlTc.appendChild(
							document.importNode(tcDoc, true) );
							
							com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl.importNode
							org.w3c.dom.DOMException: NOT_SUPPORTED_ERR: The implementation does not support the requested type of object or operation
							*/
					
                    NodeList nodes = tdDoc.getFirstChild().getChildNodes();
                    if (nodes != null) {
                        for (int i=0; i<nodes.getLength(); i++) {
                        	XmlUtils.treeCopy((Node)nodes.item(i), xhtmlTd);
                        }
                    }
				}
				
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
    
    private static void createCss(PPr pPr, StringBuffer result) {
    	
		if (pPr==null) {
			return;
		}
    	
		// Here is where we do the real work.  
		// There are a lot of paragraph properties
		// The below list is taken directly from PPrBase.
		
		//PPrBase.PStyle pStyle;
		
			// Ignore
		
		//BooleanDefaultTrue keepNext;
		if (pPr.getKeepNext()!=null) {
			if (pPr.getKeepNext().isVal() ) {
				result.append( "page-break-after:avoid;" );
			} else {
				result.append( "page-break-after:auto;" );				
			}
		}
		
	
		//BooleanDefaultTrue keepLines;
		if (pPr.getKeepLines()!=null) {
		}
	
		//BooleanDefaultTrue pageBreakBefore;
		if (pPr.getPageBreakBefore()!=null) {
			if (pPr.getPageBreakBefore().isVal() ) {
				result.append( "page-break-before:always;" );
			} else {
				result.append( "page-break-before:auto;" );				
			}
		}
	
		//CTFramePr framePr;
		//BooleanDefaultTrue widowControl;
		if (pPr.getWidowControl()!=null) {
		}
	
		//PPrBase.NumPr numPr;
		
			// High priority
	
		//BooleanDefaultTrue suppressLineNumbers;
		if (pPr.getSuppressLineNumbers()!=null) {
		}
		//PPrBase.PBdr pBdr;
		
			// Medium priority
	
		//CTShd shd;
		
			// Medium priority
	
		//Tabs tabs;
		
			// ???
	
		//BooleanDefaultTrue suppressAutoHyphens;
		//BooleanDefaultTrue kinsoku;
		//BooleanDefaultTrue wordWrap;
		//BooleanDefaultTrue overflowPunct;
		//BooleanDefaultTrue topLinePunct;
		//BooleanDefaultTrue autoSpaceDE;
		//BooleanDefaultTrue autoSpaceDN;
		//BooleanDefaultTrue bidi;
		//BooleanDefaultTrue adjustRightInd;
		//BooleanDefaultTrue snapToGrid;
		//PPrBase.Spacing spacing;
		
			// High priority
	
		//PPrBase.Ind ind;
		if (pPr.getInd()!=null ) {
			
			// Just handle left for the moment
			BigInteger left = pPr.getInd().getLeft();
			if (left!=null) {
				// 720 twip = 1 inch;
				// Try to guess whether inches or cm
				// looks nicer
				int leftL = left.intValue();
				float inch4f = 4*leftL/720;
				float inch4fabit = inch4f + 0.49f;
				int inch4 = Math.round(inch4f);
				int inch4next = Math.round( inch4fabit);
				float inches = leftL/720;
				if (inch4==inch4next) {
					// inches work 
					result.append( "position: relative; left: " + inches + "in;" );
				} else {
					float mm = inches/0.0394f;
					result.append( "position: relative; left: " + Math.round(mm) + "mm;" );
				} 
				
			}
			
		}
	
		//BooleanDefaultTrue contextualSpacing;
		//BooleanDefaultTrue mirrorIndents;
		//BooleanDefaultTrue suppressOverlap;
		//Jc jc;
		if ( pPr.getJc()!=null) {
			String val = pPr.getJc().getVal().value();
			if (val.equals("left") || val.equals("center") || val.equals("right")) {			
				result.append( "text-align: " + val + ";" );
			} else if (val.equals("both")) {
				result.append( "text-align:justify;");
				// IE only: text-justify:inter-ideograph;" );
			} // ignore the other possibilities for now
		}
	
		//TextDirection textDirection;
		//PPrBase.TextAlignment textAlignment;
		if ( pPr.getTextAlignment()!=null) {	
			String val = pPr.getTextAlignment().getVal();
			if (val.equals("top") || val.equals("bottom") || val.equals("baseline") ) {						
				result.append( "vertical-align: " + val + ";" );
			} else if (val.equals("center")) {
				result.append( "vertical-align: middle;" );
			} else if (val.equals("auto")) {
				result.append( "vertical-align: baseline;" );
			}
		}
	
		//CTTextboxTightWrap textboxTightWrap;
		//PPrBase.OutlineLvl outlineLvl;
		
			// Medium priority
	
		//PPrBase.DivId divId;
		//CTCnf cnfStyle;
    
    }
    
    private static void createCss(WordprocessingMLPackage wmlPackage, RPr rPr, StringBuffer result) {

		// Here is where we do the real work.  
		// There are a lot of run properties
		// The below list is taken directly from RPr, and so
		// is comprehensive.
		
		//RStyle rStyle;
		//RFonts rFonts;
		
		RFonts rFonts = rPr.getRFonts();
		if (rFonts !=null ) {
			
			String font = rFonts.getAscii();
			
			if (font==null) {
				// TODO - actually what Word does in this case
				// is inherit the default document font eg Calibri
				// (which is what it shows in its user interface)
				font = rFonts.getCs();
			}
			
			if (font==null) {
				log.error("Font was null in: " + XmlUtils.marshaltoString(rPr, true, true));
				font=Mapper.FONT_FALLBACK;
			}
			
			log.info("Font: " + font);
			
			PhysicalFont pf = wmlPackage.getFontMapper().getFontMappings().get(font);
			if (pf!=null) {					
				result.append( "font-family: " + pf.getName() + ";" );
			} else {
				log.error("No mapping from " + font);
			}
		}
	    

		//BooleanDefaultTrue b;
		if (rPr.getB()!=null) {
			if (rPr.getB().isVal() ) {
				result.append( "font-weight: bold;" );
			} else {
				result.append( "font-weight: normal;" );				
			}
		}

		//BooleanDefaultTrue bCs;
		//BooleanDefaultTrue i;
		if (rPr.getI()!=null) {
			if (rPr.getI().isVal() ) {
				result.append( "font-style: italic;" );
			} else {
				result.append( "font-style: normal;" );				
			}
		}

		//BooleanDefaultTrue iCs;
		//BooleanDefaultTrue caps;
		if (rPr.getCaps()!=null) {
		}

		//BooleanDefaultTrue smallCaps;
		if (rPr.getSmallCaps()!=null) {
		}

		//BooleanDefaultTrue strike;
		if (rPr.getStrike()!=null) {
			if (rPr.getStrike().isVal() ) {
				result.append( "text-decoration:line-through;" );
			} else {
				result.append( "text-decoration:none;" );				
			}
		}
		//BooleanDefaultTrue dstrike;
		//BooleanDefaultTrue outline;
		//BooleanDefaultTrue shadow;
		//BooleanDefaultTrue emboss;
		//BooleanDefaultTrue imprint;
		//BooleanDefaultTrue noProof;
		//BooleanDefaultTrue snapToGrid;
		//BooleanDefaultTrue vanish;
		//BooleanDefaultTrue webHidden;
		//Color color;
		if (rPr.getColor()!=null) {
			if (rPr.getColor().getVal()!=null) {
				result.append( "color: #" + rPr.getColor().getVal() + ";" );
			} // ignore theme stuff
		}

		//CTSignedTwipsMeasure spacing;
		//CTTextScale w;
		//HpsMeasure kern;
		//CTSignedHpsMeasure position;
		//HpsMeasure sz;
		if (rPr.getSz()!=null) {			
			float pts = rPr.getSz().getVal().floatValue()/2;
			result.append( "font-size:" + pts + "pt;" );
		}

		//HpsMeasure szCs;
		//Highlight highlight;
		//U u;
		if (rPr.getU()!=null) {
			if (rPr.getU().getVal()==null ) {
				// This does happen
				result.append( "text-decoration:underline;" );
			} else if (!rPr.getU().getVal().equals( UnderlineEnumeration.NONE ) ) {
				result.append( "text-decoration:underline;" );
			} 
			// How to handle <w:u w:color="FF0000"> ie coloured underline?
		}

		//CTTextEffect effect;
		//CTBorder bdr;
		//CTShd shd;
		//CTFitText fitText;
		//CTVerticalAlignRun vertAlign;
		//BooleanDefaultTrue rtl;
		//BooleanDefaultTrue cs;
		//CTEm em;
		//CTLanguage lang;
		//CTEastAsianLayout eastAsianLayout;
		//BooleanDefaultTrue specVanish;
		//BooleanDefaultTrue oMath;
		//CTRPrChange rPrChange;
    	
    }

}