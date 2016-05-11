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

package org.docx4j.convert.out.common;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.CTFtnEdn;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.PPr;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

/** 
 * This class contains common static functions, that get called from the PDF and HTML xsl-transformations. 
 * Methods, that are specific to a certain conversion, get implemented in their corresponding XsltxxxFunction classes.<br/>
 * The normal behaviour is to delegate this functions to the current context, that gets passed in.
 */
public class XsltCommonFunctions {
	
	private static Logger log = LoggerFactory.getLogger(XsltCommonFunctions.class);
	
	
	private XsltCommonFunctions() {
	}
	
    public static DocumentFragment fontSelector(AbstractWmlConversionContext conversionContext, 
    		NodeIterator pPrNodeIt,
    		NodeIterator rPrNodeIt,
    		NodeIterator textNodeIt) {

		PPr pPr = null;
		RPr rPr = null;
		Text text = null;
    	
//    	if (rPrNodeIt!=null) 
		{ 
    		Node n = pPrNodeIt.nextNode(); //It is never null
    		if (n!=null) {
    			try {
        			Unmarshaller u = Context.jc.createUnmarshaller();			
        			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
        			Object jaxb = u.unmarshal(n);
    				pPr =  (PPr)jaxb;
    			} catch (ClassCastException e) {
    				log.error("Couldn't cast  to RPr!");
    			} catch (JAXBException e) {
    				log.error(e.getMessage(), e);
				}        	        			
    		}
    	}
    	
//    	if (rPrNodeIt!=null) 
		{ 
    		Node n = rPrNodeIt.nextNode();
    		if (n!=null) {
    			try {
        			Unmarshaller u = Context.jc.createUnmarshaller();			
        			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
        			Object jaxb = u.unmarshal(n);
    				//rPr =  (RPr)jaxb;
    				
    				if (jaxb instanceof RPr) {
    					//rPrDirect =  (RPr)jaxbR;
    					rPr = (RPr)jaxb;
    				} else if (jaxb instanceof ParaRPr) {
//    					if (log.isDebugEnabled()) {
//    						Throwable t = new Throwable();
//    						log.debug("passed ParaRPr", t);
//    					}
    					rPr = conversionContext.getPropertyResolver().getEffectiveRPr(null, pPr); 
//    	    			System.out.println("p rpr-->" + XmlUtils.marshaltoString(pPrDirect.getRPr()));
    	        		
    	        		StyleUtil.apply((ParaRPr)jaxb, rPr); 				
    					
    				}    				
    				
    				
    			} catch (ClassCastException e) {
    				log.error("Couldn't cast  to RPr!");
    			} catch (JAXBException e) {
    				log.error(e.getMessage(), e);
				}        	        			
    		}
    	}
		
		{ 
    		Node n = textNodeIt.nextNode();
    		if (n!=null) {
    			try {
        			Unmarshaller u = Context.jc.createUnmarshaller();			
        			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
        			Object jaxb = u.unmarshal(n);
    				text =  (Text)jaxb;
    			} catch (ClassCastException e) {
    				log.error("Couldn't cast  to Text!");
    			} catch (JAXBException e) {
    				log.error(e.getMessage(), e);
				}        	        			
    		}
    	}
		
    	
    	return (DocumentFragment) conversionContext.getRunFontSelector().fontSelector(pPr, rPr, text);

    }
	
	
	/** Conversion of Nodes via Models and Converters
	 * 
	 * @param context 
	 * @param node
	 * @param childResults the already transformed node (element) content
	 * @return
	 */
	public static Node toNode(AbstractWmlConversionContext context, Node node, NodeList childResults) {
		return context.getWriterRegistry().toNode(context, node, childResults);
	}
	
	/** Next number of a footnote
	 * 
	 * @param context
	 * @return
	 */
    public static int getNextFootnoteNumber(AbstractWmlConversionContext context) {
    	return context.getNextFootnoteNumber();
    }
    
	public static Node getFootnote(AbstractWmlConversionContext context, String id) {	
		WordprocessingMLPackage wmlPackage = context.getWmlPackage();
		CTFootnotes footnotes = wmlPackage.getMainDocumentPart().getFootnotesPart().getJaxbElement();
		int pos = Integer.parseInt(id);
		
		// No @XmlRootElement on CTFtnEdn, so .. 
		CTFtnEdn ftn = (CTFtnEdn)footnotes.getFootnote().get(pos);
		Document d = XmlUtils.marshaltoW3CDomDocument( ftn,
				Context.jc, Namespaces.NS_WORD12, "footnote",  CTFtnEdn.class );
		if (log.isDebugEnabled()) {
			log.debug("Footnote " + id + ": " + XmlUtils.w3CDomNodeToString(d));
		}
		return d;
	}

    /** Next number of a endnote
     * 
     * @param context
     * @return
     */
    public static int getNextEndnoteNumber(AbstractWmlConversionContext context) {
    	return context.getNextEndnoteNumber();
    }

    //=====================================================
    // Handling of the PartTracker
    //=====================================================
	public static void setCurrentPart(AbstractWmlConversionContext context, Part currentPart) {
		context.setCurrentPart(currentPart);
	}

	public static Part getCurrentPart(AbstractWmlConversionContext context) {
		return context.getCurrentPart();
	}
	
	public static void setCurrentPartMainDocument(AbstractWmlConversionContext context) {
		context.setCurrentPartMainDocument();
	}

	public static void setCurrentPartDefaultHeader(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		context.setCurrentPart(currentSection.getHeaderFooterPolicy().getDefaultHeader());
	}
	
	public static void setCurrentPartDefaultFooter(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		context.setCurrentPart(currentSection.getHeaderFooterPolicy().getDefaultFooter());
	}
	
    //=====================================================
    // Keeping track of headers and footers 
    //=====================================================
	
	// Yuck! Getting rid of as many of these as possible ....
	
	public static void moveNextSection(AbstractWmlConversionContext context) {
		context.getSections().next();
	}
	
	public static boolean hasDefaultHeaderOrFooter(AbstractWmlConversionContext context) {
	ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		return (currentSection.getHeaderFooterPolicy().getDefaultHeader()!=null) || 
			   (currentSection.getHeaderFooterPolicy().getDefaultFooter()!=null);     		
	}

	public static boolean hasFirstHeader(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		return (currentSection.getHeaderFooterPolicy().getFirstHeader() != null);
	}

	public static boolean hasEvenHeader(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		return (currentSection.getHeaderFooterPolicy().getEvenHeader() != null);
	}

	public static boolean hasDefaultHeader(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		return (currentSection.getHeaderFooterPolicy().getDefaultHeader() != null);
	}

	public static boolean hasFirstFooter(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		return (currentSection.getHeaderFooterPolicy().getFirstFooter() != null);
	}

	public static boolean hasEvenFooter(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		return (currentSection.getHeaderFooterPolicy().getEvenFooter() != null);
	}

	public static boolean hasDefaultFooter(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		return (currentSection.getHeaderFooterPolicy().getDefaultFooter() != null);
	}

	public static Node getFirstHeader(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		Hdr hdr = (Hdr)currentSection.getHeaderFooterPolicy().getFirstHeader().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(hdr);
	}

	public static Node getFirstFooter(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		Ftr ftr = (Ftr)currentSection.getHeaderFooterPolicy().getFirstFooter().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(ftr);
	}

	public static Node getEvenHeader(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		Hdr hdr = (Hdr)currentSection.getHeaderFooterPolicy().getEvenHeader().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(hdr);
	}

	public static Node getEvenFooter(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		Ftr ftr = (Ftr)currentSection.getHeaderFooterPolicy().getEvenFooter().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(ftr);

	}

	public static Node getDefaultHeader(AbstractWmlConversionContext context) {		
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		Hdr hdr = currentSection.getHeaderFooterPolicy().getDefaultHeader().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(hdr);

	}

	public static Node getDefaultFooter(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		Ftr ftr = (Ftr)currentSection.getHeaderFooterPolicy().getDefaultFooter().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(ftr);

	}

	public static void inFirstHeader(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		setCurrentPart(context, currentSection.getHeaderFooterPolicy().getFirstHeader());
	}

	public static void inEvenHeader(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		setCurrentPart(context, currentSection.getHeaderFooterPolicy().getEvenHeader());
	}

	public static void inDefaultHeader(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		setCurrentPart(context, currentSection.getHeaderFooterPolicy().getDefaultHeader());
	}

	public static void inFirstFooter(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		setCurrentPart(context, currentSection.getHeaderFooterPolicy().getFirstFooter());
	}

	public static void inEvenFooter(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		setCurrentPart(context, currentSection.getHeaderFooterPolicy().getEvenFooter());
	}

	public static void inDefaultFooter(AbstractWmlConversionContext context) {
		ConversionSectionWrapper currentSection = context.getSections().getCurrentSection();
		setCurrentPart(context, currentSection.getHeaderFooterPolicy().getDefaultFooter());
	}

    //=====================================================
    // Keeping track of footnotes and endnotes 
    //=====================================================
	public static boolean hasEndnotesPart(AbstractWmlConversionContext context) {
		return context.getWmlPackage().getMainDocumentPart().hasEndnotesPart();
	}
	
	public static Node getEndnotes(AbstractWmlConversionContext context) {
		return XmlUtils.marshaltoW3CDomDocument(
				context.getWmlPackage().getMainDocumentPart().getEndNotesPart().getJaxbElement());		
	}
	
	public static boolean hasFootnotesPart(AbstractWmlConversionContext context) {
		return context.getWmlPackage().getMainDocumentPart().hasFootnotesPart();
	}

	public static Node getFootnotes(AbstractWmlConversionContext context) {
		return XmlUtils.marshaltoW3CDomDocument(
				context.getWmlPackage().getMainDocumentPart().getFootnotesPart().getJaxbElement());		
	}

    //=====================================================
    // Keeping track of complex field definitions 
    //=====================================================
	public static void updateComplexFieldDefinition(AbstractWmlConversionContext context, NodeIterator fldCharNodeIt) {
	org.docx4j.wml.FldChar field = null;
	Node node = fldCharNodeIt.nextNode();
    	
		try {
			field = (org.docx4j.wml.FldChar)XmlUtils.unmarshal(
						node, 
						Context.jc, 
						org.docx4j.wml.FldChar.class);
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}			
		
		STFldCharType fieldCharType = field.getFldCharType();
		
		if (fieldCharType==null) {
			if (log.isDebugEnabled()) {
				log.debug("Ignoring unrecognised: " + XmlUtils.w3CDomNodeToString(node));
			}
			
		} else {
			context.updateComplexFieldDefinition(fieldCharType);
		}
		
	}

	public static boolean isInComplexFieldDefinition(AbstractWmlConversionContext context) {
		return context.isInComplexFieldDefinition();
	}
	
	//=======================================================
	// Output of (debug) messages into the generated document
	//=======================================================
	public static DocumentFragment notImplemented(AbstractConversionContext context, NodeIterator nodes, String message) {
		return context.getMessageWriter().notImplemented(context, nodes, message);
	}
	
	public static DocumentFragment message(AbstractConversionContext context, String message) {
		return context.getMessageWriter().message(context, message);
	}
    
	//=======================================================
	// Logging support
	//=======================================================
	public static boolean isLoggingEnabled(AbstractConversionContext context) {
		return context.getLog().isDebugEnabled();
	}

	public static void logDebug(AbstractConversionContext context, String message) {
		context.getLog().debug(message);
	}	
	
	public static void logInfo(AbstractConversionContext context, String message) {
		context.getLog().info(message);
	}	
	
	public static void logWarn(AbstractConversionContext context, String message) {
		context.getLog().warn(message);
	}
	
}
