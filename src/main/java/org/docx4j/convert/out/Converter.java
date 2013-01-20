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

package org.docx4j.convert.out;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

/** 
 * This class contains common static functions, that get called from the PDF and HTML xsl-transformations. 
 * Methods, that are specific to a certain conversion, get implemented in their corresponding Conversion classes.<br/>
 * The normal behaviour is to delegate this functions to the current context, that get's passed in.
 *  
 */
public class Converter {
	private final static Logger log = Logger.getLogger(Converter.class);

	private Converter() {
	}
	
	/** Conversion of Nodes via Models and Converters
	 * 
	 * @param context 
	 * @param node
	 * @param childResults the already transformed node (element) content
	 * @return
	 */
	public static Node toNode(AbstractWmlConversionContext context, Node node, NodeList childResults) {
		return context.getModelRegistry().toNode(context, node, childResults);
	}
	
	/** Next number of a footnote
	 * 
	 * @param context
	 * @return
	 */
    public static int getNextFootnoteNumber(AbstractWmlConversionContext context) {
    	return context.getNextFootnoteNumber();
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
    // Resolve href 
    //=====================================================
    /* 
    
		<w:hyperlink r:id="rId4" w:history="true">
			<w:r>
				<w:rPr>
				    <w:rStyle w:val="Hyperlink"/>
				</w:rPr>
				<w:t>hyperlink</w:t>
			</w:r>
		</w:hyperlink>
	
	  Micrososoft C# code replaces w:hyperlink with 
	  a new node 
	  
	      <w:hlink w:dest=".." [other attributes cloned] />
	      
	  before the XSLT is called.
	
	  But we use an extension function instead.
                    
                    */    
    public static String resolveHref(AbstractWmlConversionContext context, String id  )  {
    	
    	org.docx4j.relationships.Relationship rel = context.getWmlPackage().getMainDocumentPart().getRelationshipsPart().getRelationshipByID(id);
    	
    	if ( rel != null) {
    		
        	// TODO resolve ServerRelativePath, if its not a full URL 

    		return rel.getTarget();
    		
    	} else {
    		
    		log.error("Couldn't resolve hyperlink for rel " + id);    		
    		return "";    		
    	}
    }
	
	//=======================================================
	// Output of (debug) messages into the generated document
	//=======================================================
	public static DocumentFragment notImplemented(AbstractConversionContext context, NodeIterator nodes, String message) {
		return context.getMessageWriter().notImplemented(nodes, message);
	}
	
	public static DocumentFragment message(AbstractConversionContext context, String message) {
		return context.getMessageWriter().message(message);
	}
    
}
