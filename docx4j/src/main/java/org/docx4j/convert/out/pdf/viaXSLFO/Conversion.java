package org.docx4j.convert.out.pdf.viaXSLFO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;

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
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
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
	      	  // Resulting SAX events (the generated FO) must be piped through to FOP
	      	  Result result = new SAXResult(fop.getDefaultHandler());
	    	  
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
    
    public static DocumentFragment createBlockForPPr( 
    		WordprocessingMLPackage wmlPackage,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults ) {
    	
    	// Note that this is invoked for every paragraph with a pPr node.
    	
    	// incoming objects are org.apache.xml.dtm.ref.DTMNodeIterator 
    	// which implements org.w3c.dom.traversal.NodeIterator

    	
    	if (pStyleVal!=null && !pStyleVal.equals("")) {
        	log.info("style '" + pStyleVal );     		
    	}
//    	log.info("pPrNode:" + pPrNodeIt.getClass().getName() ); // org.apache.xml.dtm.ref.DTMNodeIterator    	
//    	log.info("childResults:" + childResults.getClass().getName() ); 
    	
    	
        try {
        	
        	// Get the pPr node as a JAXB object,
        	// so we can read it using our standard
        	// methods.  Its a bit sad that we 
        	// can't just adorn our DOM tree with the
        	// original JAXB objects?
			Unmarshaller u = Context.jc.createUnmarshaller();			
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
			Object jaxb = u.unmarshal(pPrNodeIt.nextNode());
			
			PPr pPr = null;
			try {
				pPr =  (PPr)jaxb;
			} catch (ClassCastException e) {
		    	log.error("Couldn't cast " + jaxb.getClass().getName() + " to PPr!");
			}        	
        	
            // Create a DOM builder and parse the fragment			
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();
			
			//log.info("Document: " + document.getClass().getName() );

			Node foBlockElement = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block");			
			document.appendChild(foBlockElement);
			
			if (pPr==null) {
				Text err = document.createTextNode( "Couldn't cast " + jaxb.getClass().getName() + " to PPr!" );
				foBlockElement.appendChild(err);
				
			} else {
				
				if (log.isDebugEnabled()) {					
					log.debug(XmlUtils.marshaltoString(pPr, true, true));					
				}				
			       
				if ( pPr.getJc()!=null) {				
					((Element)foBlockElement).setAttribute("text-align", 
							pPr.getJc().getVal().value() );
				}
				// TODO - other pPr props, including rPr.
				
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
				treeCopy( (DTMNodeProxy)n,  foBlockElement );
			
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

    public static DocumentFragment createBlockForRPr( 
    		WordprocessingMLPackage wmlPackage,
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

			Node foBlockElement = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block");			
			document.appendChild(foBlockElement);
			
			if (rPr==null) {
				Text err = document.createTextNode( "Couldn't cast " + jaxb.getClass().getName() + " to PPr!" );
				foBlockElement.appendChild(err);
				
			} else {
				
				if (log.isDebugEnabled()) {					
					log.debug(XmlUtils.marshaltoString(rPr, true, true));					
				}
				
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
						((Element)foBlockElement).setAttribute("font-family", 
							 pf.getName() );
					} else {
						log.error("No mapping from " + font);
					}
				}
			    
				// bold				
				if ( rPr.getB()!=null ) {				
					((Element)foBlockElement).setAttribute("font-weight", 
							"bold" );
				}
				// italic
				if ( rPr.getI()!=null ) {				
					((Element)foBlockElement).setAttribute("font-style", 
							"italic" );
				}
				// TODO - other rPr props.
				
				// Our fo:block wraps whatever result tree fragment
				// our style sheet produced when it applied-templates
				// to the child nodes
				Node n = childResults.nextNode();
				treeCopy( (DTMNodeProxy)n,  foBlockElement );			
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
    
    
    private static void treeCopy( org.apache.xml.dtm.ref.DTMNodeProxy sourceNode, Node destParent ) {
    	
    	log.debug("node type" + sourceNode.getNodeType());
    	
            switch (sourceNode.getNodeType() ) {

            	case Node.DOCUMENT_NODE: // type 9
            
                    // recurse on each child
                    NodeList nodes = sourceNode.getChildNodes();
                    if (nodes != null) {
                        for (int i=0; i<nodes.getLength(); i++) {
                        	treeCopy((DTMNodeProxy)nodes.item(i), destParent);
                        }
                    }
                    break;
                case Node.ELEMENT_NODE:
                    
                    // Copy of the node itself
            		Node newChild = destParent.getOwnerDocument().createElementNS(
            				sourceNode.getNamespaceURI(), sourceNode.getLocalName() );                    
            		destParent.appendChild(newChild);
            		
            		// .. its attributes
                	NamedNodeMap atts = sourceNode.getAttributes();
                	for (int i = 0 ; i < atts.getLength() ; i++ ) {
                		
                		Attr attr = (Attr)atts.item(i);
                		
                		((Element)newChild).setAttributeNS(attr.getNamespaceURI(), 
                				attr.getLocalName(), attr.getValue() );
                		    		
                	}

                    // recurse on each child
                    NodeList children = sourceNode.getChildNodes();
                    if (children != null) {
                        for (int i=0; i<children.getLength(); i++) {
                        	treeCopy( (DTMNodeProxy)children.item(i), newChild);
                        }
                    }

                    break;

                case Node.TEXT_NODE:
                	Node textNode = destParent.getOwnerDocument().createTextNode(sourceNode.getNodeValue());       
                	destParent.appendChild(textNode);
                    break;

//                case Node.CDATA_SECTION_NODE:
//                    writer.write("<![CDATA[" +
//                                 node.getNodeValue() + "]]>");
//                    break;
//
//                case Node.COMMENT_NODE:
//                    writer.write(indentLevel + "<!-- " +
//                                 node.getNodeValue() + " -->");
//                    writer.write(lineSeparator);
//                    break;
//
//                case Node.PROCESSING_INSTRUCTION_NODE:
//                    writer.write("<?" + node.getNodeName() +
//                                 " " + node.getNodeValue() +
//                                 "?>");
//                    writer.write(lineSeparator);
//                    break;
//
//                case Node.ENTITY_REFERENCE_NODE:
//                    writer.write("&" + node.getNodeName() + ";");
//                    break;
//
//                case Node.DOCUMENT_TYPE_NODE:
//                    DocumentType docType = (DocumentType)node;
//                    writer.write("<!DOCTYPE " + docType.getName());
//                    if (docType.getPublicId() != null)  {
//                        System.out.print(" PUBLIC \"" +
//                            docType.getPublicId() + "\" ");
//                    } else {
//                        writer.write(" SYSTEM ");
//                    }
//                    writer.write("\"" + docType.getSystemId() + "\">");
//                    writer.write(lineSeparator);
//                    break;
            }
        }


    /* NB - the following are also used by DocX2Html.xslt,
     *      so they probably should be somewhere else ..
     */
    
	public static boolean hasFirstHeaderOrFooter(WordprocessingMLPackage wordmlPackage) {    		
		return (wordmlPackage.getHeaderFooterPolicy().getFirstHeader()==null && 
				wordmlPackage.getHeaderFooterPolicy().getFirstFooter() ==null? false : true);     		
	}
	public static boolean hasEvenOrOddHeaderOrFooter(WordprocessingMLPackage wordmlPackage) {    		
		return (wordmlPackage.getHeaderFooterPolicy().getOddHeader()==null && 
				wordmlPackage.getHeaderFooterPolicy().getOddFooter() ==null && 
				wordmlPackage.getHeaderFooterPolicy().getEvenHeader()==null && 
				wordmlPackage.getHeaderFooterPolicy().getEvenFooter() ==null? false : true);     		
	}
	public static boolean hasEvenHeaderOrFooter(WordprocessingMLPackage wordmlPackage) {    		
		return (wordmlPackage.getHeaderFooterPolicy().getEvenHeader()==null && 
				wordmlPackage.getHeaderFooterPolicy().getEvenFooter() ==null? false : true);     		
	}
	public static boolean hasOddHeaderOrFooter(WordprocessingMLPackage wordmlPackage) {    		
		return (wordmlPackage.getHeaderFooterPolicy().getOddHeader()==null && 
				wordmlPackage.getHeaderFooterPolicy().getOddFooter() ==null ? false : true);     		
	}
	public static boolean hasDefaultHeaderOrFooter(WordprocessingMLPackage wordmlPackage) {    		
		return (wordmlPackage.getHeaderFooterPolicy().getDefaultHeader()==null && 
				wordmlPackage.getHeaderFooterPolicy().getDefaultFooter() ==null ? false : true);     		
	}

	public static boolean hasFirstHeader(WordprocessingMLPackage wordmlPackage) {
		return (wordmlPackage.getHeaderFooterPolicy().getFirstHeader() == null ? false
				: true);
	}

	public static boolean hasOddHeader(WordprocessingMLPackage wordmlPackage) {
		return (wordmlPackage.getHeaderFooterPolicy().getOddHeader() == null ? false
				: true);
	}

	public static boolean hasEvenHeader(WordprocessingMLPackage wordmlPackage) {
		return (wordmlPackage.getHeaderFooterPolicy().getEvenHeader() == null ? false
				: true);
	}

	public static boolean hasDefaultHeader(WordprocessingMLPackage wordmlPackage) {
		return (wordmlPackage.getHeaderFooterPolicy().getDefaultHeader() == null ? false
				: true);
	}

	public static boolean hasFirstFooter(WordprocessingMLPackage wordmlPackage) {
		return (wordmlPackage.getHeaderFooterPolicy().getFirstFooter() == null ? false
				: true);
	}

	public static boolean hasOddFooter(WordprocessingMLPackage wordmlPackage) {
		return (wordmlPackage.getHeaderFooterPolicy().getOddFooter() == null ? false
				: true);
	}

	public static boolean hasEvenFooter(WordprocessingMLPackage wordmlPackage) {
		return (wordmlPackage.getHeaderFooterPolicy().getEvenFooter() == null ? false
				: true);
	}

	public static boolean hasDefaultFooter(WordprocessingMLPackage wordmlPackage) {
		return (wordmlPackage.getHeaderFooterPolicy().getDefaultFooter() == null ? false
				: true);
	}

	public static Node getFirstHeader(WordprocessingMLPackage wordmlPackage) {

		Hdr hdr = (Hdr) wordmlPackage.getHeaderFooterPolicy()
				.getFirstHeader().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(hdr);

	}

	public static Node getFirstFooter(WordprocessingMLPackage wordmlPackage) {

		Ftr ftr = (Ftr) wordmlPackage.getHeaderFooterPolicy()
				.getFirstFooter().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(ftr);

	}

	public static Node getOddHeader(WordprocessingMLPackage wordmlPackage) {

		Hdr hdr = (Hdr) wordmlPackage.getHeaderFooterPolicy()
				.getOddHeader().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(hdr);

	}

	public static Node getOddFooter(WordprocessingMLPackage wordmlPackage) {

		Ftr ftr = (Ftr) wordmlPackage.getHeaderFooterPolicy()
				.getOddFooter().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(ftr);

	}

	public static Node getEvenHeader(WordprocessingMLPackage wordmlPackage) {

		Hdr hdr = (Hdr) wordmlPackage.getHeaderFooterPolicy()
				.getEvenHeader().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(hdr);

	}

	public static Node getEvenFooter(WordprocessingMLPackage wordmlPackage) {

		Ftr ftr = (Ftr) wordmlPackage.getHeaderFooterPolicy()
				.getEvenFooter().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(ftr);

	}

	public static Node getDefaultHeader(WordprocessingMLPackage wordmlPackage) {

		Hdr hdr = (Hdr) wordmlPackage.getHeaderFooterPolicy()
				.getDefaultHeader().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(hdr);

	}

	public static Node getDefaultFooter(WordprocessingMLPackage wordmlPackage) {

		Ftr ftr = (Ftr) wordmlPackage.getHeaderFooterPolicy()
				.getDefaultFooter().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(ftr);

	}
}
    
