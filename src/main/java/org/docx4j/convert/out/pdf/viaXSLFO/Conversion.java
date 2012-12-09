package org.docx4j.convert.out.pdf.viaXSLFO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.Containerization;
import org.docx4j.convert.out.Converter;
import org.docx4j.convert.out.PageBreak;
import org.docx4j.convert.out.html.HtmlExporterNG2.EndnoteState;
import org.docx4j.convert.out.html.HtmlExporterNG2.FootnoteState;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.fonts.fop.fonts.FontTriplet;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.SymbolModel.SymbolModelTransformState;
import org.docx4j.model.TransformState;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.properties.paragraph.Indent;
import org.docx4j.model.properties.paragraph.PBorderBottom;
import org.docx4j.model.properties.paragraph.PBorderTop;
import org.docx4j.model.properties.paragraph.PShading;
import org.docx4j.model.properties.run.Font;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.model.structure.jaxb.ObjectFactory;
import org.docx4j.model.structure.jaxb.Sections;
import org.docx4j.model.structure.jaxb.Sections.Section;
import org.docx4j.model.table.TableModel.TableModelTransformState;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTPageNumber;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.NumberFormat;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.NumPr.Ilvl;
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
import org.xml.sax.InputSource;


public class Conversion extends org.docx4j.convert.out.pdf.PdfConversion {
	
	public static Logger log = Logger.getLogger(Conversion.class);	
	
	public static boolean isLoggingEnabled() {
		return log.isDebugEnabled();
	}
	
	
	
	public static final String PART_TRACKER = "partTracker";
	public static final String FIELD_TRACKER = "fieldTracker";  // are we in a field or not?
	
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
//		    	pairs = (Map.Entry)fontMappingsIterator.next();
		    	continue;
		    }
		    
		    String fontName = (String)pairs.getKey();		    
		    
		    PhysicalFont pf = wordMLPackage.getFontMapper().getFontMappings().get(fontName);
		    
		    if (pf==null) {
		    	log.error("Document font " + fontName + " is not mapped to a physical font!");
		    	continue;
		    }
		    
		    String subFontAtt = "";
		    if (pf.getEmbedFontInfo().getSubFontName()!=null)
		    	subFontAtt= " sub-font=\"" + pf.getEmbedFontInfo().getSubFontName() + "\"";
		    
		    result.append("<font embed-url=\"" +pf.getEmbeddedFile() + "\""+ subFontAtt +">" );
		    	// now add the first font triplet
			    FontTriplet fontTriplet = (FontTriplet)pf.getEmbedFontInfo().getFontTriplets().get(0);
			    addFontTriplet(result, fontTriplet);
		    result.append("</font>" );
		    
		    // bold, italic etc
		    PhysicalFont pfVariation = PhysicalFonts.getBoldForm(pf);
		    if (pfVariation!=null) {
			    result.append("<font embed-url=\"" +pfVariation.getEmbeddedFile() + "\""+ subFontAtt +">" );
		    	addFontTriplet(result, pf.getName(), "normal", "bold");
			    result.append("</font>" );
		    }
		    pfVariation = PhysicalFonts.getBoldItalicForm(pf);
		    if (pfVariation!=null) {
			    result.append("<font embed-url=\"" +pfVariation.getEmbeddedFile() + "\""+ subFontAtt +">" );
		    	addFontTriplet(result, pf.getName(), "italic", "bold");
			    result.append("</font>" );
		    }
		    pfVariation = PhysicalFonts.getItalicForm(pf);
		    if (pfVariation!=null) {
			    result.append("<font embed-url=\"" +pfVariation.getEmbeddedFile() + "\""+ subFontAtt +">" );
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
	 * @param settings
	 *            The configuration for the conversion 
	 * 
	 * */     
	@Override
	public void output(OutputStream os, PdfSettings settings) throws Docx4JException {

		// Refresh the document model, in case
		// the user has added headers or footers
		wordMLPackage.getDocumentModel().refresh();
		
		// See http://xmlgraphics.apache.org/fop/0.95/embedding.html
		// (reuse if you plan to render multiple documents!)
		FopFactory fopFactory = FopFactory.newInstance();

//		FopFactory fopFactory = null;
//		// in FOP r1356646 (after FOP 1.1),
//		// FopFactory.newInstance() was replaced with FopFactory.newInstance(URI) 
//		Method[] methods = FopFactory.class.getDeclaredMethods();		
//		Method method;
//		try {
//			method = FopFactory.class.getDeclaredMethod("newInstance", new Class[0] );
//			if (method==null) {
//				Class[] params = new Class[1];
//				params[0] = URI.class;
//				method = FopFactory.class.getDeclaredMethod("newInstance", params );
//				fopFactory = (FopFactory)method.invoke(null, new URI("http://") );
//				// Also requires xmlgraphics-commons to be built from nightly 
//				// for org/apache/xmlgraphics/image/loader/impl/AbstractImageSessionContext$FallbackResolver
//				// which was introduced in r1391005 
//			} else {
//				fopFactory = (FopFactory)method.invoke(null);
//			}
//		} catch (Exception e1) {
//			log.error(e1);
//		} 

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

				log.debug("\nUsing config:\n " + myConfig + "\n");

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
						.getBytes("UTF-8")));
			}

			fopFactory.setUserConfig(fopConfig);

			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, os);

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

			// Containerization of borders/shading
			MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

			// Don't change the user's Document object; create a tmp one
			org.docx4j.wml.Document tmpDoc = XmlUtils.deepCopy(wordMLPackage
					.getMainDocumentPart().getJaxbElement());
			Containerization.groupAdjacentBorders(tmpDoc.getBody());
			PageBreak.movePageBreaks(tmpDoc.getBody());

			// log.info(XmlUtils.marshaltoString(mdp.getJaxbElement(), false));

			Sections sections = createSectionContainers(tmpDoc);
			Document domDoc = XmlUtils.marshaltoW3CDomDocument(sections,
					Context.jcSectionModel);

			log.debug(XmlUtils.marshaltoString(sections, false, Context.jcSectionModel));
			
			if (settings == null) {
				settings = new PdfSettings();
			}
			settings.setWmlPackage(wordMLPackage);
			boolean privateImageHandler = false;
			if (settings.getImageHandler() == null) {
				settings.setImageHandler(settings.getImageDirPath() != null ? 
						new PDFConversionImageHandler(settings.getImageDirPath(), true) : 
						new PDFConversionImageHandler());
				privateImageHandler = true;
			}
			

			// Resulting SAX events (the generated FO) must be piped through to
			// FOP
			Result result = new SAXResult(fop.getDefaultHandler());

			// Allow arbitrary objects to be passed to the converters.
			// The objects are assumed to be specific to a particular converter
			// (eg table),
			// so assume there will be one object implementing TransformState
			// per converter.
			HashMap<String, TransformState> modelStates = new HashMap<String, TransformState>();
			settings.getSettings().put("modelStates", modelStates);

			// Converter c = new Converter();
			Converter.getInstance().registerModelConverter("w:tbl",
					new TableWriter());
			Converter.getInstance().registerModelConverter("w:sym",
					new SymbolWriter());

			// By convention, the transform state object is stored by reference
			// to the
			// type of element to which its model applies
			modelStates.put("w:tbl", new TableModelTransformState());
			modelStates.put("w:sym", new SymbolModelTransformState());

			modelStates.put("footnoteNumber", new FootnoteState());
			modelStates.put("endnoteNumber", new EndnoteState());
			modelStates.put(PART_TRACKER, new PartTracker());
			modelStates.put(FIELD_TRACKER, new InField());

			Converter.getInstance().start(wordMLPackage);

			if (saveFO != null || log.isDebugEnabled()) {

				ByteArrayOutputStream intermediate = new ByteArrayOutputStream();
				Result intermediateResult = new StreamResult(intermediate);

				XmlUtils.transform(domDoc, xslt, settings.getSettings(), intermediateResult);

				String fo = intermediate.toString("UTF-8");
				log.debug(fo);

				if (saveFO != null) {
					FileUtils.writeStringToFile(saveFO, fo, "UTF-8");
					log.info("Saved " + saveFO.getPath());
				}

				Source src = new StreamSource(new StringReader(fo));

				Transformer transformer = XmlUtils.getTransformerFactory().newTransformer();
				transformer.transform(src, result);
			} else {

				XmlUtils.transform(domDoc, xslt, settings.getSettings(), result);
			}
			
			if (privateImageHandler) {
				//remove a locally created imageHandler in case the HtmlSettings get reused
				settings.getSettings().remove(PdfSettings.IMAGE_HANDLER);
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
    
	private Sections createSectionContainers(org.docx4j.wml.Document doc) {
				
		ObjectFactory factory = new ObjectFactory();
		
		Sections sections = factory.createSections();
		Section section = factory.createSectionsSection();
		section.setName("s1"); // name must match fo master
		
		sections.getSection().add(section);
						
		//org.docx4j.wml.Document doc = (org.docx4j.wml.Document)wordMLPackage.getMainDocumentPart().getJaxbElement();
		
		int i = 2;
		for (Object o : doc.getBody().getEGBlockLevelElts() ) {
			
			if (o instanceof org.docx4j.wml.P) {
				if (((org.docx4j.wml.P)o).getPPr() != null ) {
					org.docx4j.wml.PPr ppr = ((org.docx4j.wml.P)o).getPPr();
					if (ppr.getSectPr()!=null) {

						// According to the ECMA-376 2ed, if type is not specified, read it as next page
						// However Word 2007 sometimes treats it as continuous, and sometimes doesn't??						
						
						if ( ppr.getSectPr().getType()!=null
								     && ppr.getSectPr().getType().getVal().equals("continuous")) {
							// If its continuous, don't add a section
						} else {
							section = factory.createSectionsSection();
							section.setName("s" +i); // name must match fo master
							sections.getSection().add(section);	
							i++;
						}
					}
				}				
			} 
			section.getAny().add( marshall(o) );
				// TODO: since the section model knows nothing about WML,
				// we have to marshall each object separately.
				// To fix this, next time wml is generated, include the section model there!
		}
		return sections;				
	}
    
	private Element marshall(Object o) {
		
		try {
			org.w3c.dom.Document w3cDoc = 
				XmlUtils.marshaltoW3CDomDocument(o);
			
			
				/* Force the RelationshipsPart to be marshalled using
				 * the normal non-rels part NamespacePrefixMapper,
				 * since otherwise (because we'd be using 2 namespace
				 * prefix mappers?) we end up with errant xmlns="",
				 * which is wrong and stops Word 2007 from loading the
				 * document.
				 * 
				 * Note that xmlPackage.xsd defines:
				 * 	<xsd:complexType name="CT_XmlData">
						<xsd:sequence>
							<xsd:any processContents="skip" />
						</xsd:sequence>
				 *
				 * Note also that marshaltoString uses 
				 * just the normal non-rels part NamespacePrefixMapper,
				 * so if/when this is marshalled again, that could
				 * have been causing problems as well?? 
				 */
	        return w3cDoc.getDocumentElement();		        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		        
		return null;
		
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
	
	public static DocumentFragment notImplemented(NodeIterator nodes, String message) {

		Node n = nodes.nextNode();
		log.warn("NOT IMPLEMENTED: support for "+ n.getNodeName() + "\n" + message);
		
		if (log.isDebugEnabled() ) {
			
			if (message==null) message="";
			
			log.debug( XmlUtils.w3CDomNodeToString(n)  );

			// Return something which will show up in the PDF
			return message("NOT IMPLEMENTED: support for " + n.getNodeName() + " - " + message);
		} else {
			
			// Put it in a comment node instead?
			
			return null;
		}
	}
	
	public static DocumentFragment message(String message) {
		
		if (!log.isDebugEnabled()) return null;

		String fo = "<fo:block xmlns:fo=\"http://www.w3.org/1999/XSL/Format\"  " 
			+ "font-size=\"12pt\" "
        	+ "color=\"red\" "
        	+ "font-family=\"sans-serif\" "
        	+ "line-height=\"15pt\" "
        	+ "space-after.optimum=\"3pt\" "
        	+ "text-align=\"justify\"> "
			+ message
			+ "</fo:block>";  

		javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory
				.newInstance();
		dbf.setNamespaceAware(true);
		StringReader reader = new StringReader(fo);
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
    		WordprocessingMLPackage wmlPackage,
    		NodeIterator rPrNodeIt,
    		NodeIterator childResults, String tag) {
    	
    	DocumentFragment docfrag = createBlockForRPr( 
        		wmlPackage,
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
    		WordprocessingMLPackage wmlPackage,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults) {

    	return createBlock( 
        		wmlPackage,
        		pPrNodeIt,
        		pStyleVal, childResults,
        		false);    	
    }
    
    public static DocumentFragment createBlock( 
    		WordprocessingMLPackage wmlPackage,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults,
    		boolean sdt) {

    	PropertyResolver propertyResolver = 
    		wmlPackage.getMainDocumentPart().getPropertyResolver();
    	
    	// Note that this is invoked for every paragraph with a pPr node.
    	
    	// incoming objects are org.apache.xml.dtm.ref.DTMNodeIterator 
    	// which implements org.w3c.dom.traversal.NodeIterator
    	
		Style defaultParagraphStyle = wmlPackage.getMainDocumentPart().getStyleDefinitionsPart().getDefaultParagraphStyle();
				// TODO: handle the case where there is no SDP!
		
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
	        			String font = Font.getPhysicalFont(wmlPackage, triple.getNumFont() );
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

				createFoAttributes(wmlPackage, pPr, ((Element)foBlockElement), inlist, ignoreBorders );
			}

			
			if (rPr!=null) {											
				createFoAttributes(wmlPackage, rPr, ((Element)foBlockElement) );
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

	public static void createFoAttributes(WordprocessingMLPackage wmlPackage, PPr pPr, Element foBlockElement, boolean inList, boolean ignoreBorders){
		
    	List<Property> properties = PropertyFactory.createProperties(wmlPackage, pPr);
    	
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
    		WordprocessingMLPackage wmlPackage,
    		NodeIterator pPrNodeIt,
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
				createFoAttributes(wmlPackage, rPr, ((Element)foInlineElement) );
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

	public static void createFoAttributes(WordprocessingMLPackage wmlPackage,
			RPr rPr, Element foInlineElement){

    	List<Property> properties = PropertyFactory.createProperties(wmlPackage, rPr);
    	
    	for( Property p :  properties ) {
    		p.setXslFO(foInlineElement);
    	}
		
	}
	

    public static DocumentFragment createBlockForFldSimple( 
    		WordprocessingMLPackage wmlPackage,
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

		return handleField(instr, childResults);
        	
   	}
    	
   	private static DocumentFragment handleField(String instr, NodeIterator childResults) {
    		
    		try {
    			
            // Create a DOM builder and parse the fragment
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();
			
			//log.info("Document: " + document.getClass().getName() );

			
			if ( !instr.toLowerCase().contains( "page") ) {
				
				if (log.isDebugEnabled() ) {
					return message("no support for fields (except PAGE numbering)");
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
    		WordprocessingMLPackage wmlPackage,
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
    	
		return handleField( field.getValue(), childResults);
    	
    }
    
    public static String getPageNumberFormat(WordprocessingMLPackage wordmlPackage, int sectionNumber) {
    	
    	SectionWrapper sw = wordmlPackage.getDocumentModel().getSections().get(sectionNumber-1);
    	
    	if (sw.getSectPr()==null) return "1";
    	
    	CTPageNumber pageNumber = sw.getSectPr().getPgNumType();
    	
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
	
    public static String getPageNumberInitial(WordprocessingMLPackage wordmlPackage, int sectionNumber) {

    	SectionWrapper sw = wordmlPackage.getDocumentModel().getSections().get(sectionNumber-1);

    	if (sw.getSectPr()==null) return "1";
    	
    	CTPageNumber pageNumber = sw.getSectPr().getPgNumType();
    	
    	if (pageNumber==null) {
    		log.debug("No PgNumType");
    		return "1";
    	}
    	
    	BigInteger start = pageNumber.getStart();
    	
    	if (start==null) return "1";
    	
    	return start.toString();
    }
	
}
    
