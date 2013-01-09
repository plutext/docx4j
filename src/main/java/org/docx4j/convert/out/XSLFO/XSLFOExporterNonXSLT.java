/*
 *  Copyright 2012, Plutext Pty Ltd.
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
package org.docx4j.convert.out.XSLFO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.log4j.Logger;
import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.Converter;
import org.docx4j.convert.out.pdf.viaXSLFO.LayoutMasterSetBuilder;
import org.docx4j.convert.out.pdf.viaXSLFO.PDFConversionImageHandler;
import org.docx4j.convert.out.pdf.viaXSLFO.PdfSettings;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.fonts.fop.fonts.FontTriplet;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.images.ConversionImageHandler;
import org.docx4j.model.images.WordXmlPictureE10;
import org.docx4j.model.images.WordXmlPictureE20;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.properties.paragraph.Indent;
import org.docx4j.model.properties.paragraph.PBorderBottom;
import org.docx4j.model.properties.paragraph.PBorderTop;
import org.docx4j.model.properties.paragraph.PShading;
import org.docx4j.model.properties.run.Font;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.jaxb.ObjectFactory;
import org.docx4j.model.structure.jaxb.Sections;
import org.docx4j.model.structure.jaxb.Sections.Section;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.table.TableModel;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Br;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.NumPr.Ilvl;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TrPr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * 
 * Running Xalan extension functions on Android is problematic:
 * 
 *   http://stackoverflow.com/questions/10579339/is-it-possible-to-call-a-java-extension-function-from-xalan-on-android
 * 
 * and generating the XSL FO via Xalan + extension functions 
 * is a bit slow,   
 * so this uses TraversalUtils to generate XSL FO output
 * without any need for Xalan or XSLT.
 * 
 * The intent is that this mechanism will ultimately
 * completely replace the Xalan based approach, at which
 * point docx4j will no longer be dependent on Xalan
 * (the XSLT included in Oracle's java is sufficient for
 *  our mc pre processor)
 * 
 * We could use a simple JAXB model of XSL FO, 
 * or org.w3c.dom to construct the XSL FO document. 
 * 
 * This implementation uses org.w3c.dom, since
 * that is easiest based on existing code.
 * 
 * This class might be neater if it used CompoundTraversalUtilVisitorCallback,
 * but it would be less obvious what is going on.  
 * 
 * @author jharrop
 *
 */
public class XSLFOExporterNonXSLT {
	
	// TODO put the XSL FO to PDF code somewhere else;
	// implement the Conversion interface there

	private static Logger log = Logger.getLogger(XSLFOExporterNonXSLT.class);
		
	private static String XSL_FO = "http://www.w3.org/1999/XSL/Format";

	org.w3c.dom.Document document;
	
	Element foRoot;
	Element pageSequence;
	Element flow;
	Element flowPrevious;

	int sectPrCounter = 0;
	
	protected XSLFOConversionContextNonXSLT conversionContext = null;
	
	public XSLFOExporterNonXSLT(WordprocessingMLPackage wmlPackage, 
			ConversionImageHandler conversionImageHandler) {

		PdfSettings pdfSettings = new PdfSettings();
		pdfSettings.setWmlPackage(wmlPackage);
		pdfSettings.setImageHandler(conversionImageHandler);
		conversionContext = new XSLFOConversionContextNonXSLT(pdfSettings);
	}
	
	/**
	 * Generate XSL FO for the entire MainDocumentPart.
	 * @return
	 */
	public org.w3c.dom.Document export() {
				
    	document = XmlUtils.neww3cDomDocument();
    	
    	foRoot = document.createElementNS(XSL_FO, "root");
    	document.appendChild(foRoot);
    	
    	LayoutMasterSetBuilder.appendLayoutMasterSetFragment( conversionContext, foRoot);  	
    	
    	
		// <fo:page-sequence master-reference="{@name}" format="{$pageNumberFormat}" initial-page-number="{$pageNumberInitial}" >
    	createPageSequence();
    	
		List blockLevelContent = conversionContext.getWmlPackage().getMainDocumentPart().getContent();
    	
		XSLFOGenerator xslfoGenerator = new XSLFOGenerator(conversionContext, null);
		new TraversalUtil(blockLevelContent, xslfoGenerator);
		
		if (pageSequence.getAttribute("master-reference")==null
				|| pageSequence.getAttribute("master-reference").equals("") ) {
    	
			SectPr finalSectPr = conversionContext.getWmlPackage().getMainDocumentPart().getJaxbElement().getBody().getSectPr(); 
			if (finalSectPr==null) {
				if (sectPrCounter == 0) {
					// There was no sectPr in the docx
			    	pageSequence.setAttribute("master-reference", "s1");
			    	pageSequence.setAttribute("initial-page-number", "1");
				} else {
					// There are some sectPr in the docx, but no doc level one.
					// TODO: What to do?					
				}
			} else {
				handleSectPr(finalSectPr, false);
			}
		}

//		// Remove last page-sequence, if empty
//		if (flow.getChildNodes().getLength()==0) {
//			foRoot.removeChild(pageSequence);
//		}

		return document;
	}
	
	private void createPageSequence() {
		
    	pageSequence = document.createElementNS(XSL_FO, "page-sequence");
    	foRoot.appendChild(pageSequence);
//    	pageSequence.setAttribute("master-reference", "s1");
//    	pageSequence.setAttribute("initial-page-number", "1");
    	
		// <fo:flow flow-name="xsl-region-body">
    	flow = document.createElementNS(XSL_FO, "flow");
    	pageSequence.appendChild(flow);
    	flow.setAttribute("flow-name", "xsl-region-body");
	}
	
	
//	/**
//	 * Generate XSL FO for the specified content.
//	 * 
//	 * @param blockLevelContent
//	 * @return
//	 */
//	public org.w3c.dom.Document export(Object blockLevelContent) {
	
	
//	/**
//	 * If you define headers/footers, and then create a continuous
//	 * section break, Word will put the headerReferences on the first 
//	 * sectPr.  
//	 */
//	private void shuffleSectPr(org.docx4j.wml.Document doc) {
//		
//		//org.docx4j.wml.Document doc = (org.docx4j.wml.Document)wordMLPackage.getMainDocumentPart().getJaxbElement();
//		
//		int i = 2;
//		for (Object o : doc.getBody().getEGBlockLevelElts() ) {
//			
//			if (o instanceof org.docx4j.wml.P) {
//				if (((org.docx4j.wml.P)o).getPPr() != null ) {
//					
//					org.docx4j.wml.PPr ppr = ((org.docx4j.wml.P)o).getPPr();
//					if (ppr.getSectPr()!=null) {
//
//						// According to the ECMA-376 2ed, if type is not specified, read it as next page
//						// However Word 2007 sometimes treats it as continuous, and sometimes doesn't??						
//						
//						if ( ppr.getSectPr().getType()!=null
//								     && ppr.getSectPr().getType().getVal().equals("continuous")) {
//							// If its continuous, don't add a section
//						} else {
//							section = factory.createSectionsSection();
//							section.setName("s" +i); // name must match fo master
//							sections.getSection().add(section);	
//							i++;
//							containersCreated++;
//						}
//					}
//				}				
//			} 
//		}
//	}
	
	
// ========================================================================
	
//    public DocumentFragment createBlockForSdt(     		
//    		PPr pPrDirect,
//    		String pStyleVal, NodeIterator childResults, String tag) {
//    	
//    	DocumentFragment docfrag = createBlock( 
//        		 pPrDirect,
//        		 pStyleVal,  childResults,
//        		 true);
//    	
//    	// Set margins, but only for a shading container,
//    	// not a borders container
//    	if (tag.equals(Containerization.TAG_SHADING) && docfrag!=null) {
//    		// docfrag.getNodeName() is  #document-fragment
//    	    Node foBlock = docfrag.getFirstChild();
//    	    if (foBlock!=null) {
//				((Element)foBlock).setAttribute("margin-top", "0in");    	    	
//				((Element)foBlock).setAttribute("margin-bottom", "0in");    	    	
//
////				((Element)foBlock).setAttribute("padding-top", "0in");    	    	
////				((Element)foBlock).setAttribute("padding-bottom", "0in");    	    	
//    	    }
//    	}
//    	    
//    	return docfrag;
//    }	
//
//	public DocumentFragment createInlineForSdt(RPr rPrDirect,
//			NodeIterator childResults, String tag) {
//
//		DocumentFragment docfrag = createBlockForRPr(null, rPrDirect,
//				childResults);
//
//		return docfrag;
//	}
//
//	/**
//	 * On the block representing the w:p, we want to put both pPr and rPr
//	 * attributes.
//	 * 
//	 * @param wmlPackage
//	 * @param pPrNodeIt
//	 * @param pStyleVal
//	 * @param childResults
//	 * @return
//	 */
//	public DocumentFragment createBlockForPPr(PPr pPrDirect, String pStyleVal,
//			NodeIterator childResults) {
//
//		return createBlock(pPrDirect, pStyleVal, childResults, false);
//	}
    
    public  void handlePPr(PPr pPrDirect, boolean sdt, Element currentEl) {

    	PropertyResolver propertyResolver = conversionContext.getPropertyResolver();
    	

    	String defaultParagraphStyleId = "Normal";
    	if (conversionContext.getWmlPackage().getMainDocumentPart().getStyleDefinitionsPart() != null) {
        	Style defaultParagraphStyle = 
        			conversionContext.getWmlPackage().getMainDocumentPart().getStyleDefinitionsPart().getDefaultParagraphStyle();
        	if (defaultParagraphStyle != null) {
        		defaultParagraphStyleId = defaultParagraphStyle.getStyleId();
        	}
    	}
    	
    	String pStyleVal = null;
    	if (pPrDirect!=null && pPrDirect.getPStyle()!=null) {
    		pStyleVal = pPrDirect.getPStyle().getVal();
    	} else {
			pStyleVal = defaultParagraphStyleId;
		}
    	log.debug("style '" + pStyleVal );     		

        try {
        	
        	PPr pPr = null;
        	RPr rPr = null;
			pPr = propertyResolver.getEffectivePPr(pPrDirect);  

			log.debug("getting rPr for paragraph style");    				
			rPr = propertyResolver.getEffectiveRPr(null, pPrDirect); 
				// rPr in pPr direct formatting only applies to paragraph mark, 
				// so pass null here       				

			if (log.isDebugEnabled() && pPr!=null) {				
				log.debug(XmlUtils.marshaltoString(pPr, true, true));					
			}
        							
			boolean inlist = false;
			
			Element foBlockElement;
			if (pPr!=null && pPr.getNumPr()!=null ) {
				
				inlist = true;
				
				// Its a list item.  At present we make a new list-block for
				// each list-item. This is not great; DocumentModel will ultimately
				// allow us to use fo:list-block properly.

				Element foListBlock = document.createElementNS(XSL_FO, 
						"list-block");
				currentEl.appendChild(foListBlock);
								
				foListBlock.setAttribute("provisional-distance-between-starts", "0.5in");
				
				// Need to apply shading at fo:list-block level
				if (pPr.getShd()!=null) {
					PShading pShading = new PShading(pPr.getShd());
					pShading.setXslFO(foListBlock);
				}
				
				Element foListItem = document.createElementNS(XSL_FO, 
						"list-item");
				foListBlock.appendChild(foListItem);				

				
				Element foListItemLabel = document.createElementNS(XSL_FO, 
						"list-item-label");
				foListItem.appendChild(foListItemLabel);
				
				Element foListItemLabelBody = document.createElementNS(XSL_FO, 
						"block");
				foListItemLabel.appendChild(foListItemLabelBody);
				
	        	ResultTriple triple;
	        	if (pPrDirect!=null && pPrDirect.getNumPr()!=null) {
	        		triple = org.docx4j.model.listnumbering.Emulator.getNumber(
	        			conversionContext.getWmlPackage(), pStyleVal, 
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
		        				conversionContext.getWmlPackage(), pStyleVal, 
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
	        			String font = Font.getPhysicalFont(conversionContext.getWmlPackage(), triple.getNumFont() );
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
				
				Element foListItemBody = document.createElementNS(XSL_FO, 
						"list-item-body");
				foListItem.appendChild(foListItemBody);	

				foListItemBody.setAttribute(Indent.FO_NAME, "body-start()");
				
				foBlockElement = document.createElementNS(XSL_FO, 
						"block");
				foListItemBody.appendChild(foBlockElement);
				
			} else {

				// Do nothing 
			}
			
							
			if (pPr!=null) {
				// Ignore paragraph borders once inside the container
				boolean ignoreBorders = !sdt;

				createFoAttributes( pPr, currentEl, inlist, ignoreBorders );
			}

			if (pPrDirect !=null 
					&& pPrDirect.getSectPr()!=null) {  
				
				handleSectPr(pPrDirect.getSectPr(), true);
			
			} 				
			
			if (rPr!=null) {											
				createFoAttributes(conversionContext.getWmlPackage(), rPr, currentEl );
	        }
        
						
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString() );
			log.error(e);
		} 
    }

    private void handleSectPr(SectPr sectPr, boolean makeNextPageSequence) {
    	
		// Create a page-sequence for each section
    	// and use the correct master-reference.
    	// The general idea is to create an initial page sequence.  
		// Then, during the traverse, each time we hit a sectPr, write the relevant 
    	// values back to the page sequence element, then create the next page-sequence.

    	sectPrCounter++;
		if ( sectPr.getType()!=null
			     && sectPr.getType().getVal().equals("continuous")) {
			
			// If its continuous, add this content to the previous paged section
			// and delete current page sequence
			
            NodeList children = flow.getChildNodes();
            if (children != null) {
                for (int i=0; i<children.getLength(); i++) {
        			flowPrevious.appendChild(children.item(i).cloneNode(true));  // note cloneNode!
                }
            }
            log.debug("Removing " + XmlUtils.w3CDomNodeToString(pageSequence));
	    	foRoot.removeChild(pageSequence);

		} else {
			flowPrevious= flow;
		}
		
		addPageSequenceAttributes(sectPrCounter);
		handleStaticContent(sectPrCounter);
		
		// Set up next 
		if (makeNextPageSequence) {
			createPageSequence();
		}
		
		// TODO: can a sectPr occur in a table cell?
		
    	
    }

	public  void createFoAttributes( PPr pPr, Element foBlockElement, boolean inList, boolean ignoreBorders){
		
    	List<Property> properties = PropertyFactory.createProperties(conversionContext.getWmlPackage(), pPr);
    	
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
	protected static void applyFoAttributes(List<Property> properties, Element foElement) {
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
    public void handleRPr( 
    		PPr pPrDirect,
    		RPr rPrDirect, Element foInlineElement ) {

    	PropertyResolver propertyResolver = conversionContext.getPropertyResolver();
    	
    	
        try {
        	RPr rPr = propertyResolver.getEffectiveRPr(rPrDirect, pPrDirect);
        	
				
			if (log.isDebugEnabled() && rPr!=null) {					
				log.debug(XmlUtils.marshaltoString(rPr, true, true));					
			}
			
			//if (rPr!=null) {				
				createFoAttributes(conversionContext.getWmlPackage(), rPr, ((Element)foInlineElement) );
			//}
			
						
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString() );
			log.error(e);
		} 
    	
    }

	protected static void createFoAttributes(WordprocessingMLPackage wmlPackage,
			RPr rPr, Element foInlineElement){

    	List<Property> properties = PropertyFactory.createProperties(wmlPackage, rPr);
    	
    	for( Property p :  properties ) {
    		p.setXslFO(foInlineElement);
    	}
		
	}
	

//    public static DocumentFragment createBlockForFldSimple( 
//    		WordprocessingMLPackage wmlPackage,
//    		NodeIterator fldSimpleNodeIt,
//    		NodeIterator childResults ) {
//    	
//    	/* Support page numbering.
//    	 * 
//    	 * Word 2007 emits:
//    	 * 
//    	 *  <w:fldSimple w:instr=" PAGE   \* MERGEFORMAT ">
//	          <w:r>
//	            <w:rPr>
//	              <w:noProof/>
//	            </w:rPr>
//	            <w:t>- 1 -</w:t>
//	          </w:r>
//	        </w:fldSimple>
//	        
//	        could also include:
//	        
//				{ PAGE \* Arabic }
//				{ PAGE \* alphabetic }
//				{ PAGE \* ALPHABETIC }
//				{ PAGE \* roman }
//				{ PAGE \* ROMAN }	        
//
//		    <w:sectPr>
//		      <w:pgNumType w:fmt="numberInDash"/>
//		      
//		    could also include start at value.
//		    
//		 *
//		 * Word 2003 emits:
//		 * 
//		 *       <w:instrText xml:space="preserve">PAGE  </w:instrText>
//
//    	 */
//    	
//
//    	CTSimpleField field = null;
//    	
//		try {
//			field = (CTSimpleField)XmlUtils.unmarshal(
//						fldSimpleNodeIt.nextNode(), 
//						Context.jc, 
//						CTSimpleField.class);
//		} catch (JAXBException e1) {
//			e1.printStackTrace();
//		}
//			
//		String instr = field.getInstr();			
//
//		return handleField(instr, childResults);
//        	
//   	}
//    	
//   	private static DocumentFragment handleField(String instr, NodeIterator childResults) {
//    		
//    		try {
//    			
//            // Create a DOM builder and parse the fragment
//        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
//			Document document = factory.newDocumentBuilder().newDocument();
//			
//			//log.info("Document: " + document.getClass().getName() );
//
//			
//			if ( !instr.toLowerCase().contains( "page") ) {
//				
//				if (log.isDebugEnabled() ) {
//					log.warn("no support for fields (except PAGE numbering)");
//				} else {
//					
//					// Try this
//					Node foInlineElement = document.createElementNS(XSL_FO, "inline");			
//					document.appendChild(foInlineElement);
//					
//					Node n = childResults.nextNode();
//					XmlUtils.treeCopy( n,  foInlineElement );
//					
//					DocumentFragment docfrag = document.createDocumentFragment();
//					docfrag.appendChild(document.getDocumentElement());
//
//					return docfrag;					
//				}
//			}
//
//			// Its a PAGE numbering field
//			
//			/*
//			 * For XSL FO page numbering, see generally
//			 * http://www.dpawson.co.uk/xsl/sect3/N8703.html
//			 * 
//			 * In summary, 
//			 * 
//			 * <fo:page-sequence master-name="blagh" 
//			 * 				format="i"
//			 * 				initial-page-number="1"> ....
//			 * 
//			 */
//
//			Node foPageNumber = document.createElementNS(XSL_FO, 
//					"page-number");			
//			document.appendChild(foPageNumber);
//						
//			DocumentFragment docfrag = document.createDocumentFragment();
//			docfrag.appendChild(document.getDocumentElement());
//
//			return docfrag;
//						
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println(e.toString() );
//			log.error(e);
//		} 
//    	
//    	return null;
//    	
//    }
//
//    public static DocumentFragment createBlockForInstrText( 
//    		WordprocessingMLPackage wmlPackage,
//    		NodeIterator fldSimpleNodeIt,
//    		NodeIterator childResults ) {
//    	
//    	/* Support page numbering.
//    	 * 
//		 * Word 2003 emits :
//		 * 
//		 * 		 <w:fldChar w:fldCharType="begin"/>
//		 * 
//		 *       <w:instrText xml:space="preserve">PAGE  </w:instrText>
//
//				 <w:fldChar w:fldCharType="end"/>
//    	 */
//    	
//    	org.docx4j.wml.Text field = null;
//    	
//		try {
//			field = 
//				(org.docx4j.wml.Text)XmlUtils.unmarshal(
//						fldSimpleNodeIt.nextNode(), 
//						Context.jc, 
//						org.docx4j.wml.Text.class);
//		} catch (JAXBException e1) {
//			e1.printStackTrace();
//		}			
//    	
//		return handleField( field.getValue(), childResults);
//    	
//    }
	
	
// ========================================================================	
	

    class XSLFOGenerator extends CallbackImpl {
    	XSLFOConversionContextNonXSLT conversionContext = null;
    	
    	Node tableContext; 
    	
		Element currentP; 
    	Element currentSpan;
    	
		Element tr;		
		Element tc; // will only get set in an XSLFOGenerator object created by this one
    	
    	
    	PPr pPr;
    	
    	// E20 image
    	Object anchorOrInline;
    	
    	XSLFOGenerator(XSLFOConversionContextNonXSLT conversionContext, Node tableContext) {
			super();
			this.conversionContext = conversionContext;
			this.tableContext = tableContext;
		}
    	
    	@Override
		public List<Object> apply(Object o) {
			
			if (o instanceof P) {
				
				currentP = document.createElementNS(XSL_FO, "block");
				currentSpan = null;
				if (tc!=null) {
					tc.appendChild( currentP  );
				} else {
					flow.appendChild( currentP  );					
				}
				
				pPr = ((P)o).getPPr();
				handlePPr(pPr, false, currentP);
				
			} else if (o instanceof org.docx4j.wml.R) {

				// Convert run to span
				Element spanEl = document.createElementNS(XSL_FO, "inline");
				currentP.appendChild( spanEl  );
				currentSpan = spanEl;
				
				RPr rPr = ((R)o).getRPr();
				if ( rPr!=null ) {
					handleRPr(pPr, rPr, currentSpan);
				}
								
			} else if (o instanceof org.docx4j.wml.Text) {
				
				System.out.println("Processing '" + ((org.docx4j.wml.Text)o).getValue());					
				
				if (currentSpan!=null) {
					currentSpan.appendChild(document.createTextNode(
							((org.docx4j.wml.Text)o).getValue()));
					
					System.out.println(XmlUtils.w3CDomNodeToString(currentP));
					
				} else {
					currentP.appendChild(document.createTextNode(
							((org.docx4j.wml.Text)o).getValue()));					
				}

			} else if (o instanceof org.docx4j.wml.Tbl) {

				Tbl tbl = (org.docx4j.wml.Tbl)o;
				
				// To use our existing model, first we need childResults.
				// We get these using a new XSLFOGenerator object.
				
		    	DocumentFragment tableFragment = document.createDocumentFragment();
				XSLFOGenerator tableRowTraversor = new XSLFOGenerator(conversionContext, tableFragment);
				new TraversalUtil(tbl.getContent(), tableRowTraversor);
				
				Node foTable = 
					 conversionContext.getModelRegistry().toNode(
							 conversionContext, 
							 tbl, 
							 TableModel.MODEL_ID, 
							 tableFragment, 
							 document);
				
				if (foTable != null) {
					if (currentP != null) { // ??
						currentP.appendChild(foTable);
					}
					else {
						//in case there isn't a paragraph 
						tableContext.appendChild(foTable);
					}
				}
				
				currentP=null;
				currentSpan=null;
				
			} else if (o instanceof org.docx4j.wml.Tr) {
				
				tr = document.createElementNS(Namespaces.NS_WORD12, "tr");
				tableContext.appendChild(tr);
				
			} else if (o instanceof org.docx4j.wml.Tc) {
				
				tc = document.createElementNS(Namespaces.NS_WORD12, "tc");
				tr.appendChild(tc);
				// now the html p content will go temporarily go in w:tc,
				// which is what we need for our existing table model.
				
			} else if (o instanceof org.docx4j.dml.wordprocessingDrawing.Inline
					|| o instanceof org.docx4j.dml.wordprocessingDrawing.Anchor) {
				
				anchorOrInline = o;  // keep this until we handle CTBlip
				
			} else if (o instanceof org.docx4j.dml.CTBlip) {
	            /*<w:drawing>
	                <wp:inline distT="0" distB="0" distL="0" distR="0">
	                  <a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
	                    <a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
	                      <pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
	                        <pic:blipFill>
	                          <a:blip r:embed="rId10" cstate="print"/> */
				
				DocumentFragment foreignFragment = 
						WordXmlPictureE20.createXslFoImgE20(
								conversionContext,
								anchorOrInline, 
								conversionContext.getWmlPackage().getMainDocumentPart());
				anchorOrInline = null;
				
				currentP.appendChild( document.importNode(foreignFragment, true) );
				
			} else if (o instanceof org.docx4j.wml.Pict) {
		      /*<w:pict>
		          <v:shape id="_x0000_i1025" type="#_x0000_t75" style="width:428.25pt;height:321pt">
		            <v:imagedata r:id="rId4" o:title=""/>
		          </v:shape> */

				DocumentFragment foreignFragment = WordXmlPictureE10.createXslFoImgE10(
						conversionContext, 
						o, 
						conversionContext.getWmlPackage().getMainDocumentPart());
				
				currentP.appendChild( document.importNode(foreignFragment, true) );
				
			} else if (o instanceof org.docx4j.wml.Br) {
				
				currentP = document.createElementNS(XSL_FO, "block");
				currentSpan = null;
				flow.appendChild( currentP  );
				
				Br br = (Br)o;
				if (br.getType()!=null 
						&& br.getType().equals(STBrType.PAGE)) {
				
					currentP.setAttribute("break-before", "page");
				
				} else {
				
					currentP.setAttribute("white-space-treatment", "preserve");
				}
				
			} else {
				log.warn("Need to handle " + o.getClass().getName() );				
			}
			
			return null;
		}
    	
    	@Override
		public boolean shouldTraverse(Object o) {
    		if (o instanceof org.docx4j.wml.Tbl) {
    			// Don't traverse into the table,
    			// since this is handled separately    			
    			return false;
    		} else {
    			return true;
    		}
		}
    	
    	
	}
    
    private void addPageSequenceAttributes(int pos) {
    	
    	// master-reference="{@name}"
		this.pageSequence.setAttribute("master-reference", "s"+pos);

		// format="{$pageNumberFormat}" initial-page-number="{$pageNumberInitial}"
    	
		String pageNumberFormat = PageNumberHelper.getPageNumberFormat(conversionContext.getWmlPackage(), pos);
		this.pageSequence.setAttribute("format", pageNumberFormat);
	
		String pageNumberInitial = PageNumberHelper.getPageNumberInitial(conversionContext.getWmlPackage(), pos);
		this.pageSequence.setAttribute("initial-page-number", pageNumberInitial);
    	
    }
    

    /**
     * Handle headers/footers, footnotes/endnotes
     * @param pos
     */
    private void handleStaticContent(int pos) {
    	
    	Node firstChild = pageSequence.getFirstChild();
    	    	
    	// <!--  First Page Header -->
		// <fo:static-content flow-name="xsl-region-before-firstpage">
    	if (Converter.hasFirstHeader(conversionContext, pos)) {
    		
        	flow = document.createElementNS(XSL_FO, "static-content");
        	pageSequence.insertBefore(flow, firstChild); 
        	flow.setAttribute("flow-name", "xsl-region-before-firstpage");

    		Hdr hdr = (Hdr) conversionContext.getWmlPackage().getDocumentModel().getSections().get(pos-1)
    				.getHeaderFooterPolicy()
    						.getFirstHeader().getJaxbElement();
        	
    		XSLFOGenerator xslfoGenerator = new XSLFOGenerator(conversionContext, null);
    		new TraversalUtil(hdr.getContent(), xslfoGenerator);
    	}
    	

    	// <!--  First Page Footer -->
    	// <fo:static-content flow-name="xsl-region-after-firstpage">
    	if (Converter.hasFirstFooter(conversionContext, pos)) {
    		
        	flow = document.createElementNS(XSL_FO, "static-content");
//        	pageSequence.appendChild(flow); // TODO insertBefore
        	pageSequence.insertBefore(flow, firstChild); 
        	flow.setAttribute("flow-name", "xsl-region-after-firstpage");

    		Ftr ftr = (Ftr) conversionContext.getWmlPackage().getDocumentModel().getSections().get(pos-1)
    				.getHeaderFooterPolicy()
    						.getFirstFooter().getJaxbElement();
        	
    		XSLFOGenerator xslfoGenerator = new XSLFOGenerator(conversionContext, null);
    		new TraversalUtil(ftr.getContent(), xslfoGenerator);
    	}
    	
    	boolean treatDefaultAsOdd = false;
    	
    	if (Converter.hasEvenHeader(conversionContext, pos)
    			|| Converter.hasEvenFooter(conversionContext, pos)) {
    		/*
    		 *       <w:headerReference w:type="even" r:id="rId12"/>
    		 *       <w:headerReference w:type="default" r:id="rId13"/>
    		 *       
    		 *       the default one is treated as odd.
    		 */
    		treatDefaultAsOdd = true;
    		
    		// <fo:static-content flow-name="xsl-region-before-evenpage">
    		if (Converter.hasEvenHeader(conversionContext, pos)) {
    			
    			flow = document.createElementNS(XSL_FO, "static-content");
            	pageSequence.insertBefore(flow, firstChild); 
            	flow.setAttribute("flow-name", "xsl-region-before-evenpage");

        		Hdr hdr = (Hdr) conversionContext.getWmlPackage().getDocumentModel().getSections().get(pos-1)
        				.getHeaderFooterPolicy()
        						.getEvenHeader().getJaxbElement();
            	
        		XSLFOGenerator xslfoGenerator = new XSLFOGenerator(conversionContext, null);
        		new TraversalUtil(hdr.getContent(), xslfoGenerator);
    			
    		}

    		// <fo:static-content flow-name="xsl-region-after-evenpage">
    		if (Converter.hasEvenFooter(conversionContext, pos)) {
    			
    			flow = document.createElementNS(XSL_FO, "static-content");
            	pageSequence.insertBefore(flow, firstChild); 
            	flow.setAttribute("flow-name", "xsl-region-after-evenpage");

        		Ftr ftr = (Ftr) conversionContext.getWmlPackage().getDocumentModel().getSections().get(pos-1)
        				.getHeaderFooterPolicy()
        						.getEvenFooter().getJaxbElement();
            	
        		XSLFOGenerator xslfoGenerator = new XSLFOGenerator(conversionContext, null);
        		new TraversalUtil(ftr.getContent(), xslfoGenerator);
    		}
    		
    	}
    	    	
    	if (Converter.hasDefaultHeaderOrFooter(conversionContext, pos) ) {
    		
    		// <fo:static-content flow-name="xsl-region-before-default">
    		if (Converter.hasDefaultHeader(conversionContext, pos) ) {

    			flow = document.createElementNS(XSL_FO, "static-content");
            	pageSequence.insertBefore(flow, firstChild); 
            	if (treatDefaultAsOdd) {
            		flow.setAttribute("flow-name", "xsl-region-before-oddpage");            		
            	} else {
            		flow.setAttribute("flow-name", "xsl-region-before-default");
            	}
            	
        		Hdr hdr = (Hdr) conversionContext.getWmlPackage().getDocumentModel().getSections().get(pos-1)
        				.getHeaderFooterPolicy()
        						.getDefaultHeader().getJaxbElement();
            	
        		XSLFOGenerator xslfoGenerator = new XSLFOGenerator(conversionContext, null);
        		new TraversalUtil(hdr.getContent(), xslfoGenerator);
    		}

    		// <fo:static-content flow-name="xsl-region-after-default">
    		if (Converter.hasDefaultFooter(conversionContext, pos) ) {

    			flow = document.createElementNS(XSL_FO, "static-content");
            	pageSequence.insertBefore(flow, firstChild); 
            	if (treatDefaultAsOdd) {
            		flow.setAttribute("flow-name", "xsl-region-after-oddpage");            		
            	} else {
            		flow.setAttribute("flow-name", "xsl-region-after-default");
            	}

        		Ftr ftr = (Ftr) conversionContext.getWmlPackage().getDocumentModel().getSections().get(pos-1)
        				.getHeaderFooterPolicy()
        						.getDefaultFooter().getJaxbElement();
            	
        		XSLFOGenerator xslfoGenerator = new XSLFOGenerator(conversionContext, null);
        		new TraversalUtil(ftr.getContent(), xslfoGenerator);
    		}
    		
    		if (conversionContext.getWmlPackage().getMainDocumentPart().getFootnotesPart()!=null) {
    			// TODO
//						<fo:static-content flow-name="xsl-footnote-separator">
//						    <fo:block>
//						      <fo:leader leader-pattern="rule"
//						                 leader-length="100%"
//						                 rule-style="solid"
//						                 rule-thickness="0.5pt"/>
//						    </fo:block>
//						  </fo:static-content>				
    		}
    		
    	} 
    	
    	// TODO
//		<xsl:if
//		test="java:org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart.hasEndnotesPart($wmlPackage)">
//		
//        <fo:block space-before="44pt" font-weight="bold" font-size="14pt">
//          <xsl:text>Endnotes</xsl:text>
//        </fo:block>
//		
//		<xsl:apply-templates
//				select="java:org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart.getEndnotes($wmlPackage)" />
//	</xsl:if>
    	

    }
    


	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath;	
		String outputfilepath;		

		inputfilepath = System.getProperty("user.dir")
//				+ "/OpenXML_1ed_Part4_500_sections_none.docx";
//		+ "/OpenXML_1ed_Part4.docx";
//		+ "/sample-docs/word/sample-docx.docx";
//		+ "/sample-docs/word/2003/word2003-vml.docx";
//				+ "/table-nested.docx";
		+ "/sample-docs/word/headers.docx";
		
		WordprocessingMLPackage wmlPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));
		
		XSLFOExporterNonXSLT withoutXSLT = new XSLFOExporterNonXSLT(wmlPackage, 
													new PDFConversionImageHandler());		
//		new PDFConversionImageHandler(settings.getImageDirPath(), true) : 
//				new HTMLConversionImageHandler("c:\\temp", "/bar", true) );
		

		
		long startTime = System.currentTimeMillis();				
		Document xslfo = withoutXSLT.export();
		long endTime = System.currentTimeMillis();
		log.info("done.  elapsed time: " + Math.round((endTime-startTime)/1000) );

		log.info(XmlUtils.w3CDomNodeToString(xslfo));
		
		outputfilepath = inputfilepath + "K.pdf";
		OutputStream os = new java.io.FileOutputStream(outputfilepath);
		
		// OK, do it...
		withoutXSLT.output(xslfo, os );
		System.out.println("Saved " + outputfilepath);	
		
	}

// ========================================================================	
// TODO: what follows is duplicated code. Refactor/remove.
	
	
	public void output(Document xslfo, OutputStream os) throws Docx4JException {

		// Refresh the document model, in case
		// the user has added headers or footers
		conversionContext.getWmlPackage().getDocumentModel().refresh();
		
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

			// Resulting SAX events (the generated FO) must be piped through to
			// FOP
			Result result = new SAXResult(fop.getDefaultHandler());


			Source src = new DOMSource (xslfo);

			Transformer transformer = XmlUtils.getTransformerFactory().newTransformer();
			long startTime = System.currentTimeMillis();				
			transformer.transform(src, result);
			long endTime = System.currentTimeMillis();
			log.info("FOP converted FO to PDF in elapsed time: " + Math.round((endTime-startTime)/1000) );
			//settings.getSettings().remove(PdfSettings.IMAGE_HANDLER);

				
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
	
	/**
	 * Create a FOP font configuration for each font used in the
	 * document.
	 * 
	 * @return
	 */
	private String declareFonts() {
		
		StringBuffer result = new StringBuffer();
		Map fontsInUse = conversionContext.getWmlPackage().getMainDocumentPart().fontsInUse();
		Iterator fontMappingsIterator = fontsInUse.entrySet().iterator();
		while (fontMappingsIterator.hasNext()) {
		    Map.Entry pairs = (Map.Entry)fontMappingsIterator.next();
		    if(pairs.getKey()==null) {
		    	log.info("Skipped null key");
//		    	pairs = (Map.Entry)fontMappingsIterator.next();
		    	continue;
		    }
		    
		    String fontName = (String)pairs.getKey();		    
		    
		    PhysicalFont pf = conversionContext.getWmlPackage().getFontMapper().getFontMappings().get(fontName);
		    
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
	
	
	
}
