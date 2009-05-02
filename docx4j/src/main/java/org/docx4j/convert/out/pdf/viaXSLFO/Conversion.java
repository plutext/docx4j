package org.docx4j.convert.out.pdf.viaXSLFO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.Converter;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.docx4j.wml.UnderlineEnumeration;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.NodeIterator;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fonts.FontTriplet;
import org.apache.log4j.Logger;
import org.apache.xml.dtm.ref.DTMNodeProxy;


public class Conversion extends org.docx4j.convert.out.pdf.PdfConversion {
	
	protected static Logger log = Logger.getLogger(Conversion.class);	
	
	public Conversion(WordprocessingMLPackage wordMLPackage) {
		super(wordMLPackage);
	}
	
	static Templates xslt;	
	static {
		try {
			Source xsltSource  = new StreamSource(
					org.docx4j.utils.ResourceUtils.getResource(
							"org/docx4j/convert/out/pdf/viaXSLFO/docx2fo.xslt"));
			xslt = XmlUtils.getTransformerTemplate(xsltSource);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Create a FOP font configuration for each font used in the
	 * document.
	 * 
	 * @return
	 */
	private String declareFonts() {
		
		StringBuffer result = new StringBuffer();
		Map fontsInUse = wordMLPackage.getMainDocumentPart().fontsInUse();
		Iterator fontMappingsIterator = fontsInUse.entrySet().iterator();
		while (fontMappingsIterator.hasNext()) {
		    Map.Entry pairs = (Map.Entry)fontMappingsIterator.next();
		    if(pairs.getKey()==null) {
		    	log.info("Skipped null key");
		    	pairs = (Map.Entry)fontMappingsIterator.next();
		    }
		    
		    String fontName = (String)pairs.getKey();		    
		    
		    PhysicalFont pf = wordMLPackage.getFontMapper().getFontMappings().get(fontName);
		    
		    if (pf==null) {
		    	log.error("Document font " + fontName + " is not mapped to a physical font!");
		    	continue;
		    }
		    
		    result.append("<font embed-url=\"" +pf.getEmbeddedFile() + "\">" );
		    	// now add the first font triplet
			    FontTriplet fontTriplet = (FontTriplet)pf.getEmbedFontInfo().getFontTriplets().get(0);
			    addFontTriplet(result, fontTriplet);
		    result.append("</font>" );
		    
		    // bold, italic etc
		    PhysicalFont pfVariation = PhysicalFonts.getBoldForm(pf);
		    if (pfVariation!=null) {
			    result.append("<font embed-url=\"" +pfVariation.getEmbeddedFile() + "\">" );
		    	addFontTriplet(result, pf.getName(), "normal", "bold");
			    result.append("</font>" );
		    }
		    pfVariation = PhysicalFonts.getBoldItalicForm(pf);
		    if (pfVariation!=null) {
			    result.append("<font embed-url=\"" +pfVariation.getEmbeddedFile() + "\">" );
		    	addFontTriplet(result, pf.getName(), "italic", "bold");
			    result.append("</font>" );
		    }
		    pfVariation = PhysicalFonts.getItalicForm(pf);
		    if (pfVariation!=null) {
			    result.append("<font embed-url=\"" +pfVariation.getEmbeddedFile() + "\">" );
		    	addFontTriplet(result, pf.getName(), "italic", "normal");
			    result.append("</font>" );
		    }
			    
		}
		
		return result.toString();
		
	}
		
	private void addFontTriplet(StringBuffer result, FontTriplet fontTriplet) {
	    result.append("<font-triplet name=\"" + fontTriplet.getName() + "\""
				+ " style=\"" + fontTriplet.getStyle() + "\""
				+ " weight=\"" + weightToCSS2FontWeight(fontTriplet.getWeight()) + "\""
						+ "/>" );		
	}
	private void addFontTriplet(StringBuffer result, String familyName, String style, String weight) {
	    result.append("<font-triplet name=\"" + familyName + "\""
				+ " style=\"" + style + "\""
				+ " weight=\"" + weight + "\""
						+ "/>" );		
	}
	
	private String weightToCSS2FontWeight(int i) {
		
		if (i>=700) {
			return "bold";
		} else {
			return "normal";
		}
		
	}
	
	Configuration fopConfig;
	/**
	 * User can set their own fop configuration if they
	 * wish, in which can it is their responsibility
	 * to include the fonts the font mapper is using.
	 * (If this method is not used, an appropriate
	 *  configuration will be generated automatically)
	 * @param fopConfig
	 */
	public void setFopConfig(Configuration fopConfig) {
		this.fopConfig = fopConfig;
	}
	
	
	/** Create a pdf version of the document, using XSL FO. 
	 * 
	 * @param os
	 *            The OutputStream to write the pdf to 
	 * 
	 * */     
    public void output(OutputStream os) throws Docx4JException {
    	
    	// See http://xmlgraphics.apache.org/fop/0.95/embedding.html
    	// (reuse if you plan to render multiple documents!)
    	FopFactory fopFactory = FopFactory.newInstance();
    	
    	try {
                
    		if (fopConfig == null) {
    			
				DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
				String myConfig = "<fop version=\"1.0\"><strict-configuration>true</strict-configuration>"
						+ "<renderers><renderer mime=\"application/pdf\">"
						+ "<fonts>" + declareFonts() +
						// <directory>/home/dev/fonts</directory>" +
						// "<directory>/usr/share/fonts/truetype/ttf-lucida</directory>"
						// +
						// "<directory>/var/lib/defoma/fontconfig.d/D</directory>"
						// +
						// "<directory>/var/lib/defoma/fontconfig.d/L</directory>"
						// +
						// "<auto-detect/>" +
						"</fonts></renderer></renderers></fop>";

				log.info("\nUsing config:\n " + myConfig + "\n");

				// See FOP's PrintRendererConfigurator
				// String myConfig = "<fop
				// version=\"1.0\"><strict-configuration>true</strict-configuration>"
				// +
				// "<renderers><renderer mime=\"application/pdf\">" +
				// "<fonts><directory
				// recursive=\"true\">C:\\WINDOWS\\Fonts</directory>" +
				// "<auto-detect/>" +
				// "</fonts></renderer></renderers></fop>";

				fopConfig = cfgBuilder.build(new ByteArrayInputStream(myConfig
						.getBytes()));
			}
    		
    	  fopFactory.setUserConfig(fopConfig);
    	  
    	  Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, os);

    	  /*
    	   * Based on the principle that we'll do all the smarts via
    	   * extension functions which can take advantage of Java
    	   * and docx4j's model of the package, all the XSLT
    	   * needs is the main document part.
    	   * 
    	   * This means we can skip the step of generating a
    	   * Flat OPC XML file.
    	   */
    	  //Document domDoc = XmlPackage.getFlatDomDocument(wordMLPackage);
    	  Document domDoc = XmlUtils.marshaltoW3CDomDocument(wordMLPackage.getMainDocumentPart().getJaxbElement());
    	  
    	  java.util.HashMap<String, Object> settings = new java.util.HashMap<String, Object>();
			settings.put("wmlPackage", wordMLPackage);
	        String imageDirPath = System.getProperty("java.io.tmpdir");
			settings.put("imageDirPath", imageDirPath);
			
	      	  // Resulting SAX events (the generated FO) must be piped through to FOP
	      	  Result result = new SAXResult(fop.getDefaultHandler());
	      	  
	  		Converter c = new Converter();
			c.getInstance().registerModelConverter("w:tbl", new TableWriter() );
			c.start(wordMLPackage);
	      	  
	    	  
	  		if (log.isDebugEnabled()) {

	  			ByteArrayOutputStream intermediate = new ByteArrayOutputStream();
	  			Result intermediateResult =  new StreamResult( intermediate );
	  			
	  			XmlUtils.transform(domDoc, xslt, settings, intermediateResult);
	  			
	  			String fo = intermediate.toString("UTF-8");
	  			log.debug(fo);
	  			
	  			Source src = new StreamSource(new StringReader(fo));
		    	
	  			Transformer transformer = XmlUtils.tfactory.newTransformer();
	  			transformer.transform(src, result);
	  		} else {
	      	   
	    	  XmlUtils.transform(domDoc, xslt, settings, result);
	  			
	  		}
    	  
    	} catch (Exception e) {
    		throw new Docx4JException("FOP issues", e);
    	} finally {
    	  //Clean-up
    	  try {
			os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}		
		
	}

    /* ---------------Xalan XSLT Extension Functions ---------------- */
    
    /**
     * On the block representing the w:p, we want to put both
     * pPr and rPr attributes.
     * 
     * @param wmlPackage
     * @param pPrNodeIt
     * @param pStyleVal
     * @param childResults
     * @return
     */
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
        	RPr rPr = null;
        	if (pPrNodeIt==null) {  // Never happens?        		
    			log.debug("Here after all!!");        		
        		pPr = propertyResolver.getEffectivePPr("Normal");
        		rPr = propertyResolver.getEffectiveRPr("Normal");
        	} else {
        		Node n = pPrNodeIt.nextNode();
        		if (n==null) {
        			log.warn("pPrNodeIt.nextNode() was null.");
            		pPr = propertyResolver.getEffectivePPr("Normal");
            		rPr = propertyResolver.getEffectiveRPr("Normal");
            		// TODO - in this case, we should be able to compute once,
            		// and on subsequent calls, just return pre computed value
        		} else {
        			Unmarshaller u = Context.jc.createUnmarshaller();			
        			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
        			Object jaxb = u.unmarshal(n);
    				PPr pPrDirect =  (PPr)jaxb;
    				pPr = propertyResolver.getEffectivePPr(pPrDirect);   
    				log.warn("getting rPr for paragraph style");    				
    				rPr = propertyResolver.getEffectiveRPr(null, pPrDirect); 
    					// rPr in pPr direct formatting only applies to paragraph mark, 
    					// so pass null here       				
        		}
        	}        	
        	
            // Create a DOM builder and parse the fragment			
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();
			
			//log.info("Document: " + document.getClass().getName() );

			Node foBlockElement = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block");			
			document.appendChild(foBlockElement);
							
			if (log.isDebugEnabled() && pPr!=null) {				
				log.debug(XmlUtils.marshaltoString(pPr, true, true));					
			}				
		       
			createFoAttributes(pPr, ((Element)foBlockElement) );
			createFoAttributes(wmlPackage, rPr, ((Element)foBlockElement) );
			
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
			XmlUtils.treeCopy( (DTMNodeProxy)n,  foBlockElement );
			
			
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

	public static void createFoAttributes(PPr pPr, Element foBlockElement){
		
		// TODO - other pPr props, including rPr.
		// Here is where we do the real work.  
		// There are a lot of paragraph properties
		// The below list is taken directly from PPrBase.
		
		//PPrBase.PStyle pStyle;
		
			// Ignore
		
		//BooleanDefaultTrue keepNext;
		if (pPr.getKeepNext()!=null) {
			if (pPr.getKeepNext().isVal() ) {
				((Element)foBlockElement).setAttribute("keep-with-next", "always");
			} 
		}
		
	
		//BooleanDefaultTrue keepLines;
		if (pPr.getKeepLines()!=null) {
		}
	
		//BooleanDefaultTrue pageBreakBefore;
		if (pPr.getPageBreakBefore()!=null) {
			if (pPr.getPageBreakBefore().isVal() ) {
				((Element)foBlockElement).setAttribute("break-before", "page");
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
			// TODO hanging, something like start-indent="1in" text-indent="-1in"
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
					((Element)foBlockElement).setAttribute("start-indent", inches + "in" );
				} else {
					float mm = inches/0.0394f;
					((Element)foBlockElement).setAttribute("start-indent", Math.round(mm) + "mm" );
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
				((Element)foBlockElement).setAttribute("text-align", 
					val );
			} // ignore the other possibilities for now
		}
	
		//TextDirection textDirection;
		//PPrBase.TextAlignment textAlignment;
		if ( pPr.getTextAlignment()!=null) {	
			String val = pPr.getTextAlignment().getVal();
			if (val.equals("top") || val.equals("bottom") || val.equals("baseline") ) {						
				((Element)foBlockElement).setAttribute( "vertical-align", val );
			} else if (val.equals("center")) {
				((Element)foBlockElement).setAttribute("vertical-align","middle" );
			} else if (val.equals("auto")) {
				((Element)foBlockElement).setAttribute("vertical-align", "baseline" );
			}
		}
	
		//CTTextboxTightWrap textboxTightWrap;
		//PPrBase.OutlineLvl outlineLvl;
		
			// Medium priority
	
		//PPrBase.DivId divId;
		//CTCnf cnfStyle;
		
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
    		WordprocessingMLPackage wmlPackage,
    		//NodeIterator pPrNodeIt,
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

//			// We might not have a pPr node
//			PPr pPrDirect = null;
//        	if (pPrNodeIt!=null) {
//        		Node n = pPrNodeIt.nextNode();
//        		if (n!=null) {
//        			Object jaxb = u.unmarshal(n);
//        			try {
//        				pPrDirect =  (PPr)jaxb;
//        			} catch (ClassCastException e) {
//        		    	log.error("Couldn't cast " + jaxb.getClass().getName() + " to PPr!");
//        			}        	        			
//        		}
//        	}
        	
			Object jaxbR = u.unmarshal(rPrNodeIt.nextNode());			
			RPr rPrDirect = null;
			try {
				rPrDirect =  (RPr)jaxbR;
			} catch (ClassCastException e) {
		    	log.error("Couldn't cast .." );
			}        	
        	RPr rPr = propertyResolver.getEffectiveRPr(rPrDirect, null);
        	
            // Create a DOM builder and parse the fragment
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();
			
			//log.info("Document: " + document.getClass().getName() );

			Node foInlineElement = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:inline");			
			document.appendChild(foInlineElement);
			
				
			if (log.isDebugEnabled() && rPr!=null) {					
				log.debug(XmlUtils.marshaltoString(rPr, true, true));					
			}
			
			//if (rPr!=null) {				
				createFoAttributes(wmlPackage, rPr, ((Element)foInlineElement) );
			//}
			
			// Our fo:block wraps whatever result tree fragment
			// our style sheet produced when it applied-templates
			// to the child nodes
			Node n = childResults.nextNode();
			XmlUtils.treeCopy( (DTMNodeProxy)n,  foInlineElement );			
			
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

	public static void createFoAttributes(WordprocessingMLPackage wmlPackage,
			RPr rPr, Element foInlineElement){

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
				((Element)foInlineElement).setAttribute("font-family", 
					 pf.getName() );
			} else {
				log.error("No mapping from " + font);
			}
		}
	    

		//BooleanDefaultTrue b;
		// bold				
		if ( rPr.getB()!=null ) {				
			((Element)foInlineElement).setAttribute("font-weight", 
					"bold" );
		}

		//BooleanDefaultTrue bCs;
		//BooleanDefaultTrue i;
		// italic
		if ( rPr.getI()!=null ) {				
			((Element)foInlineElement).setAttribute("font-style", 
					"italic" );
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
				((Element)foInlineElement).setAttribute("text-decoration", "line-through" );
			} else {
				((Element)foInlineElement).setAttribute("text-decoration", "none" );
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
				((Element)foInlineElement).setAttribute("color", "#" + rPr.getColor().getVal() );
			} // ignore theme stuff
		}

		//CTSignedTwipsMeasure spacing;
		//CTTextScale w;
		//HpsMeasure kern;
		//CTSignedHpsMeasure position;
		//HpsMeasure sz;
		if (rPr.getSz()!=null) {			
			float pts = rPr.getSz().getVal().floatValue()/2;
			((Element)foInlineElement).setAttribute("font-size", pts + "pt" );
		}

		//HpsMeasure szCs;
		//Highlight highlight;
		//U u;
		if (rPr.getU()!=null) {
			if (rPr.getU().getVal()==null ) {
				// This does happen
				((Element)foInlineElement).setAttribute("text-decoration", "underline" );
			} else if (!rPr.getU().getVal().equals( UnderlineEnumeration.NONE ) ) {
				((Element)foInlineElement).setAttribute("text-decoration", "underline" );
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
			
			// Not implemented in FOP
//			Node foTableAndCaption = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
//					"fo:table-and-caption");			
//			document.appendChild(foTableAndCaption);

			Node foTable = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
					"fo:table");			
			document.appendChild(foTable);

			Node foTableBody = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
			"fo:table-body");			
			foTable.appendChild(foTableBody);
			
			if (log.isDebugEnabled()) {					
				log.debug(XmlUtils.marshaltoString(tbl, true, true));					
			}
			
			// Just simple stuff for now
			List<Object> rows = tbl.getEGContentRowContent();
			for( Object o : rows) {
				
				Tr tr = (Tr)o;
				
				Node foTr = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
						"fo:table-row");			
				foTableBody.appendChild(foTr);
				
				List<Object> cells = tr.getEGContentCellContent();
				for( Object o2 : cells) {
					
					Node foTc = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
							"fo:table-cell");
					foTr.appendChild(foTc);
					
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
                        	XmlUtils.treeCopy((Node)nodes.item(i), foTc);
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
    
    
}
    
