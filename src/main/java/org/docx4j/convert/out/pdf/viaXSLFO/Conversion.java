package org.docx4j.convert.out.pdf.viaXSLFO;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.fop.apps.MimeConstants;
import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.Containerization;
import org.docx4j.convert.out.ConversionSectionWrappers;
import org.docx4j.convert.out.PageBreak;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.properties.paragraph.Indent;
import org.docx4j.model.properties.paragraph.PBorderBottom;
import org.docx4j.model.properties.paragraph.PBorderTop;
import org.docx4j.model.properties.paragraph.PShading;
import org.docx4j.model.properties.run.Font;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.FopUtils;
import org.docx4j.wml.CTPageNumber;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.NumberFormat;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.NumPr.Ilvl;
import org.docx4j.wml.RPr;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TrPr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.NodeIterator;

public class Conversion extends org.docx4j.convert.out.pdf.PdfConversion {
	public static Logger log = Logger.getLogger(Conversion.class);
	
	
	public static boolean isLoggingEnabled() {
		return log.isDebugEnabled();
	}
	
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
	
	private static File saveFO;
	public void setSaveFO(File save) {
		saveFO = save;
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
	 * @param settings
	 *            The configuration for the conversion 
	 * 
	 * */     
	@Override
	public void output(OutputStream os, PdfSettings settings) throws Docx4JException {
	PdfConversionContext conversionContext = null;
	Configuration localFopConfiguration = fopConfig;
	WordprocessingMLPackage localWmlPackage = (wordMLPackage != null ? 
			wordMLPackage : (WordprocessingMLPackage)settings.getWmlPackage());

		if (settings == null) {
			settings = new PdfSettings();
		}
		settings.setWmlPackage(localWmlPackage);
	
		
		if (localWmlPackage == null) {
			throw new Docx4JException("The WordprocessingMLPackage is missing.");
		}
		
		try {

			/*
			 * Based on the principle that we'll do all the smarts via extension
			 * functions which can take advantage of Java and docx4j's model of
			 * the package, all the XSLT needs is the main document part.
			 * 
			 * This means we can skip the step of generating a Flat OPC XML
			 * file.
			 */
			// Document domDoc = XmlPackage.getFlatDomDocument(wordMLPackage);
			// Document domDoc =
			// XmlUtils.marshaltoW3CDomDocument(wordMLPackage.getMainDocumentPart().getJaxbElement());

			// Don't change the user's Document object; create a tmp one
			org.docx4j.wml.Document tmpDoc = XmlUtils.deepCopy(localWmlPackage
					.getMainDocumentPart().getJaxbElement());
			
			
			// Preprocessing
			Containerization.groupAdjacentBorders(tmpDoc.getBody());
			PageBreak.movePageBreaks(tmpDoc.getBody());
			ConversionSectionWrappers conversionSectionWrappers = 
					ConversionSectionWrappers.build(tmpDoc, localWmlPackage);

			conversionContext = new PdfConversionContext(settings, conversionSectionWrappers);
			
			Document domDoc = XmlUtils.marshaltoW3CDomDocument(conversionSectionWrappers.createSections(),
					Context.jcSectionModel);
				
			if (log.isDebugEnabled()) {
				log.debug(XmlUtils.w3CDomNodeToString(domDoc));
			}

			if (localFopConfiguration == null) {
				localFopConfiguration = 
						FopUtils.createDefaultConfiguration(localWmlPackage.getFontMapper(), 
															localWmlPackage.getMainDocumentPart().fontsInUse());
			}
			if (saveFO != null || log.isDebugEnabled()) {

				ByteArrayOutputStream intermediate = new ByteArrayOutputStream();
				Result intermediateResult = new StreamResult(intermediate);

				XmlUtils.transform(domDoc, xslt, conversionContext.getXsltParameters(), intermediateResult);

				String fo = intermediate.toString("UTF-8");
				log.debug(fo);

				if (saveFO != null) {
					FileUtils.writeStringToFile(saveFO, fo, "UTF-8");
					log.info("Saved " + saveFO.getPath());
				}

				FopUtils.render(localFopConfiguration, MimeConstants.MIME_PDF, fo, null, os);
			} else {
				FopUtils.render(localFopConfiguration, MimeConstants.MIME_PDF, domDoc, xslt, conversionContext.getXsltParameters(), os);
			}

		} catch (Exception e) {
			throw new Docx4JException("FOP issues", e);
		} finally {
			// Clean-up
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	

    /* ---------------Xalan XSLT Extension Functions ---------------- */

	public static void logDebug(String message) {
		log.debug(message);
	}	
	public static void logInfo(String message) {
		log.info(message);
	}	
	public static void logWarn(String message) {
		log.warn(message);
	}

    public static DocumentFragment createBlockForSdt(PdfConversionContext context, 
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
    		PdfConversionContext context,
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
    		PdfConversionContext context,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults) {

    	return createBlock( 
        		context,
        		pPrNodeIt,
        		pStyleVal, childResults,
        		false);    	
    }
    
    public static DocumentFragment createBlock( 
    		PdfConversionContext context,
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
    	log.debug("style '" + pStyleVal );     		

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
    			log.debug("Here after all!!");        		
        		pPr = propertyResolver.getEffectivePPr(defaultParagraphStyleId);
        		rPr = propertyResolver.getEffectiveRPr(defaultParagraphStyleId);
        	} else {
        		Node n = pPrNodeIt.nextNode();
        		if (n==null) {
        			log.debug("pPrNodeIt.nextNode() was null.");
            		pPr = propertyResolver.getEffectivePPr(defaultParagraphStyleId);
            		rPr = propertyResolver.getEffectiveRPr(defaultParagraphStyleId);
            		// TODO - in this case, we should be able to compute once,
            		// and on subsequent calls, just return pre computed value
        		} else {
					log.debug( XmlUtils.w3CDomNodeToString(n) );
        			Unmarshaller u = Context.jc.createUnmarshaller();			
        			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
        			Object jaxb = u.unmarshal(n);
    				pPrDirect =  (PPr)jaxb;
    				pPr = propertyResolver.getEffectivePPr(pPrDirect);  
    				if (pPr==null) {
    					log.debug("pPr null; obtained from: " + XmlUtils.w3CDomNodeToString(n) );
    				}
    				log.debug("getting rPr for paragraph style");    				
    				rPr = propertyResolver.getEffectiveRPr(null, pPrDirect); 
    					// rPr in pPr direct formatting only applies to paragraph mark, 
    					// so pass null here       				
        		}
        	}        	

			if (log.isDebugEnabled() && pPr!=null) {				
				log.debug(XmlUtils.marshaltoString(pPr, true, true));					
			}
        	
            // Create a DOM builder and parse the fragment			
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();
			
			//log.info("Document: " + document.getClass().getName() );
			
			boolean inlist = false;
			
			Element foBlockElement;
			if (pPr!=null && pPr.getNumPr()!=null ) {
				
				inlist = true;
				
				// Its a list item.  At present we make a new list-block for
				// each list-item. This is not great; DocumentModel will ultimately
				// allow us to use fo:list-block properly.

				Element foListBlock = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
						"fo:list-block");
				document.appendChild(foListBlock);
								
				foListBlock.setAttribute("provisional-distance-between-starts", "0.5in");
				
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
	        		log.warn("computed number ResultTriple was null");
	        		if (log.isDebugEnabled() ) {
	        			foListItemLabelBody.setTextContent("nrt");
	        		} 
	        	} else {
	        		
	        		// Indent (in combination with provisional-distance-between-starts
	        		// above
	        		if (triple.getIndent()!=null) {
	        			Indent indent = new Indent(triple.getIndent());
	    				//foListBlock.setAttribute(Indent.FO_NAME, "2in");
	    				indent.setXslFO(foListBlock);
	        		}
	        		
	        		// Set the font
	        		if (triple.getNumFont()!=null) {
	        			String font = Font.getPhysicalFont(context.getWmlPackage(), triple.getNumFont() );
	        			if (font!=null) {
	        				foListItemLabelBody.setAttribute(Font.FO_NAME, font );
	        			}
	        		}
	        		
	        		if (triple.getBullet()!=null ) {
		        		foListItemLabelBody.setTextContent(triple.getBullet() );
		        	} else if (triple.getNumString()==null) {
			    		log.warn("computed NumString was null!");
		        		if (log.isDebugEnabled() ) {
		        			foListItemLabelBody.setTextContent("nns");
		        		} 
			    	} else {
						Text number = document.createTextNode( triple.getNumString() );
						foListItemLabelBody.appendChild(number);		    		
			    	}
	        	}
				
				Element foListItemBody = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
						"fo:list-item-body");
				foListItem.appendChild(foListItemBody);	

				foListItemBody.setAttribute(Indent.FO_NAME, "body-start()");
				
				foBlockElement = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
						"fo:block");
				foListItemBody.appendChild(foBlockElement);
				
			} else {

				foBlockElement = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block");
				document.appendChild(foBlockElement);
			}
			
							
			if (pPr!=null) {
				// Ignore paragraph borders once inside the container
				boolean ignoreBorders = !sdt;

				createFoAttributes(context.getWmlPackage(), pPr, ((Element)foBlockElement), inlist, ignoreBorders );
			}

			
			if (rPr!=null) {											
				createFoAttributes(context.getWmlPackage(), rPr, ((Element)foBlockElement) );
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
			e.printStackTrace();
			System.out.println(e.toString() );
			log.error(e);
		} 
    	
    	return null;
    	
    }

	public static void createFoAttributes(OpcPackage opcPackage, PPr pPr, Element foBlockElement, boolean inList, boolean ignoreBorders){
		
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
    public static DocumentFragment createBlockForRPr( 
    		PdfConversionContext context,
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
        		    	log.error("Couldn't cast " + jaxb.getClass().getName() + " to PPr!");
        			}        	        			
        		}
        	}
        	
			Object jaxbR = u.unmarshal(rPrNodeIt.nextNode());			
			RPr rPrDirect = null;
			try {
				rPrDirect =  (RPr)jaxbR;
			} catch (ClassCastException e) {
		    	log.error("Couldn't cast .." );
			}        	
        	RPr rPr = propertyResolver.getEffectiveRPr(rPrDirect, pPrDirect);
        	
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
			e.printStackTrace();
			System.out.println(e.toString() );
			log.error(e);
		} 
    	
    	return null;
    	
    }

	public static void createFoAttributes(OpcPackage opcPackage,
			RPr rPr, Element foInlineElement){

    	List<Property> properties = PropertyFactory.createProperties(opcPackage, rPr);
    	
    	for( Property p :  properties ) {
    		p.setXslFO(foInlineElement);
    	}
		
	}
	

    public static DocumentFragment createBlockForFldSimple( 
    		PdfConversionContext context,
    		NodeIterator fldSimpleNodeIt,
    		NodeIterator childResults ) {
    	
    	/* Support page numbering.
    	 * 
    	 * Word 2007 emits:
    	 * 
    	 *  <w:fldSimple w:instr=" PAGE   \* MERGEFORMAT ">
	          <w:r>
	            <w:rPr>
	              <w:noProof/>
	            </w:rPr>
	            <w:t>- 1 -</w:t>
	          </w:r>
	        </w:fldSimple>
	        
	        could also include:
	        
				{ PAGE \* Arabic }
				{ PAGE \* alphabetic }
				{ PAGE \* ALPHABETIC }
				{ PAGE \* roman }
				{ PAGE \* ROMAN }	        

		    <w:sectPr>
		      <w:pgNumType w:fmt="numberInDash"/>
		      
		    could also include start at value.
		    
		 *
		 * Word 2003 emits:
		 * 
		 *       <w:instrText xml:space="preserve">PAGE  </w:instrText>

    	 */
    	

    	CTSimpleField field = null;
    	
		try {
			field = (CTSimpleField)XmlUtils.unmarshal(
						fldSimpleNodeIt.nextNode(), 
						Context.jc, 
						CTSimpleField.class);
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}
			
		String instr = field.getInstr();			

		return handleField(context, instr, childResults);
        	
   	}
    	
   	private static DocumentFragment handleField(PdfConversionContext context, String instr, NodeIterator childResults) {
    		
    		try {
    			
            // Create a DOM builder and parse the fragment
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();
			
			//log.info("Document: " + document.getClass().getName() );

			
			if ( !instr.toLowerCase().contains( "page") ) {
				
				if (log.isDebugEnabled() ) {
					return context.getMessageWriter().message("no support for fields (except PAGE numbering)");
				} else {
					
					// Try this
					Node foInlineElement = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:inline");			
					document.appendChild(foInlineElement);
					
					Node n = childResults.nextNode();
					XmlUtils.treeCopy( n,  foInlineElement );
					
					DocumentFragment docfrag = document.createDocumentFragment();
					docfrag.appendChild(document.getDocumentElement());

					return docfrag;					
				}
			}

			// Its a PAGE numbering field
			
			/*
			 * For XSL FO page numbering, see generally
			 * http://www.dpawson.co.uk/xsl/sect3/N8703.html
			 * 
			 * In summary, 
			 * 
			 * <fo:page-sequence master-name="blagh" 
			 * 				format="i"
			 * 				initial-page-number="1"> ....
			 * 
			 */

			Node foPageNumber = document.createElementNS("http://www.w3.org/1999/XSL/Format", 
					"fo:page-number");			
			document.appendChild(foPageNumber);
						
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

    public static DocumentFragment createBlockForInstrText( 
    		PdfConversionContext context,
    		NodeIterator fldSimpleNodeIt,
    		NodeIterator childResults ) {
    	
    	/* Support page numbering.
    	 * 
		 * Word 2003 emits :
		 * 
		 * 		 <w:fldChar w:fldCharType="begin"/>
		 * 
		 *       <w:instrText xml:space="preserve">PAGE  </w:instrText>

				 <w:fldChar w:fldCharType="end"/>
    	 */
    	
    	org.docx4j.wml.Text field = null;
    	
		try {
			field = 
				(org.docx4j.wml.Text)XmlUtils.unmarshal(
						fldSimpleNodeIt.nextNode(), 
						Context.jc, 
						org.docx4j.wml.Text.class);
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}			
    	
		return handleField(context, field.getValue(), childResults);
    	
    }
    
    public static String getPageNumberFormat(PdfConversionContext context) {
    	
    	SectPr sectPr = context.getSections().getCurrentSection().getSectPr();
    	
    	if (sectPr==null) return "1";
    	
    	CTPageNumber pageNumber = sectPr.getPgNumType();
    	
    	if (pageNumber==null) return "1";
    	
    	NumberFormat format = pageNumber.getFmt();
    	
    	if (format==null) return "1";
    	
    	log.debug("w:pgNumType/@w:fmt=" + format.toString());
    	
//    	 *     &lt;enumeration value="decimal"/>
//    	 *     &lt;enumeration value="upperRoman"/>
//    	 *     &lt;enumeration value="lowerRoman"/>
//    	 *     &lt;enumeration value="upperLetter"/>
//    	 *     &lt;enumeration value="lowerLetter"/>    	
    	if (format==NumberFormat.DECIMAL)
    		return "1";
    	else if (format==NumberFormat.UPPER_ROMAN)
    		return "I";
    	else if (format==NumberFormat.LOWER_ROMAN)
    		return "i";
    	//else if (format.equals(NumberFormat.UPPER_LETTER))
    	else if (format==NumberFormat.UPPER_LETTER)
    		return "A";
    	else if (format==NumberFormat.LOWER_LETTER)
    		return "a";

        // TODO .. other formats
    		
    	return "1";
    }
	
    public static String getPageNumberInitial(PdfConversionContext context) {
    	
    	SectPr sectPr = context.getSections().getCurrentSection().getSectPr();

    	if (sectPr==null) return "1";
    	
    	CTPageNumber pageNumber = sectPr.getPgNumType();
    	
    	if (pageNumber==null) {
    		log.debug("No PgNumType");
    		return "1";
    	}
    	
    	BigInteger start = pageNumber.getStart();
    	
    	if (start==null) return "1";
    	
    	return start.toString();
    }
	
	
	public static void inFieldUpdateState(PdfConversionContext context, NodeIterator fldCharNodeIt) {
		context.inFieldUpdateState(fldCharNodeIt);
	}
	
	public static boolean inFieldGetState(PdfConversionContext context) {
		return context.inFieldGetState();
	}
	
}
    
