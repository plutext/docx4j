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

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.XmlUtils;
import org.docx4j.fonts.GlyphCheck;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.jaxb.Context;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.openpackaging.exceptions.CyclicStylesException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
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
import org.w3c.dom.Element;
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

		PPr pPr = toPPr(pPrNodeIt);
		RPr rPr = toRPr(conversionContext, rPrNodeIt, pPr);
		Text text = null;

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

    /** As above, but for text we generate ourselves (a footnote or endnote number),
     *  as opposed to the contents of a w:t.
     *
     *  Without this, the number would be rendered in the renderer's default font,
     *  rather than the font of the run it belongs to.
     *
     * @param conversionContext
     * @param pPrNodeIt the w:pPr of the containing w:p (may be empty)
     * @param rPrNodeIt the w:rPr of the containing w:r (may be empty)
     * @param text the text to be rendered
     * @since 17.0.3
     */
    public static DocumentFragment fontSelectorForGeneratedText(AbstractWmlConversionContext conversionContext,
    		NodeIterator pPrNodeIt,
    		NodeIterator rPrNodeIt,
    		String text) {

		PPr pPr = toPPr(pPrNodeIt);
		RPr rPr = toRPr(conversionContext, rPrNodeIt, pPr);

		return fontSelectorForGeneratedText(conversionContext, pPr, rPr, text);
    }

    /** JAXB-typed form of the above, for the visitor pathway.
     *
     * @since 17.0.4
     */
    public static DocumentFragment fontSelectorForGeneratedText(AbstractWmlConversionContext conversionContext,
    		PPr pPr,
    		RPr rPr,
    		String text) {

    	/* Pass the text as a w:t with no xml:space, rather than as a String: the
    	 * String overload doesn't reset RunFontSelector's spacePreserve flag, so
    	 * generated text would otherwise inherit xml:space="preserve" from whatever
    	 * w:t that instance (which lives for the conversion) was last used for. */
    	Text wmlText = Context.getWmlObjectFactory().createText();
    	wmlText.setValue(text);

    	DocumentFragment df =
    			(DocumentFragment) conversionContext.getRunFontSelector().fontSelector(pPr, rPr, wmlText);

    	// the font we resolved may not actually have these characters; see fontCanRender
    	if (df!=null && df.getFirstChild() instanceof Element) {
    		Element styled = (Element)df.getFirstChild();
    		if (!fontCanRender(conversionContext.getWmlPackage().getFontMapper(), styled, text)) {
    			removeFont(styled);
    		}
    	}
    	return df;
    }

    private static final Pattern FONT_FAMILY_IN_CSS = Pattern.compile("font-family:\\s*([^;]*);?");

    /** Whether the font RunFontSelector chose for 'styled' can actually render this text.
     *
     *  Only worth asking for text we generate ourselves.  For the contents of a w:t the
     *  question doesn't arise: where the document embeds a subsetted font (w:subsetted="1"),
     *  the subset covers the text it was subsetted from.  But a page number, a footnote
     *  number or a tab leader is produced at render time, so there is no guarantee the
     *  author's subset contains it.  Apache FOP doesn't fall back to another font; it
     *  warns 'Glyph "1" (0x31, one) not available in font ...' and renders .notdef.
     *
     *  Where this returns false, the caller should leave the font unset, so that the text
     *  is rendered in whatever font it inherits - legible, if not what was asked for.
     *
     * @since 17.0.3
     */
    public static boolean fontCanRender(Mapper fontMapper, Element styled, String text) {

    	if ((styled==null) || (text==null) || (text.length()==0)) return true;

    	String fontName = physicalFontNameOf(styled);
    	if ((fontName==null) || (fontName.length()==0)) return true;  // no font was set anyway

    	PhysicalFont pf = physicalFontFor(fontMapper, fontName);
    	if (pf==null) {
    		// We can't tell, so use the font: dropping it would be worse.
    		log.debug("Couldn't resolve " + fontName + "; assuming it can render " + text);
    		return true;
    	}

    	try {
    		for (int i=0; i<text.length(); ) {
    			int cp = text.codePointAt(i);
    			if (!GlyphCheck.hasCodepoint(pf, cp)) {
    				log.debug(fontName + " has no glyph for '" + new String(Character.toChars(cp))
    						+ "'; leaving the font unset");
    				return false;
    			}
    			i += Character.charCount(cp);
    		}
    	} catch (Exception e) {
    		// not fatal; better to set the font than to fail the conversion
    		log.warn("Couldn't glyph check " + fontName + ": " + e.getMessage(), e);
    	}
    	return true;
    }

    /** The PhysicalFont this name refers to.
     *
     *  A font embedded in the document is deliberately NOT added to PhysicalFonts (those are
     *  available to all documents; see ObfuscatedFontPart.extract), so it has to be found via
     *  this document's Mapper.  The Mapper is keyed by the name the document uses, whereas what
     *  we have is the name of the physical font it was mapped to, so we look at the values.
     */
    private static PhysicalFont physicalFontFor(Mapper fontMapper, String physicalFontName) {

    	if (fontMapper!=null) {
    		for (PhysicalFont pf : fontMapper.getFontMappings().values()) {
    			if ((pf!=null) && physicalFontName.equals(pf.getName())) return pf;
    		}
    	}
    	return PhysicalFonts.get(physicalFontName);
    }

    /** The physical font name RunFontSelector put on this element: @font-family for fo,
     *  or the font-family declaration in @style for html. */
    private static String physicalFontNameOf(Element styled) {

    	String fontFamily = styled.getAttribute("font-family");  // fo
    	if ((fontFamily!=null) && (fontFamily.length()>0)) return fontFamily;

    	String style = styled.getAttribute("style");  // html, eg font-family:'Courier New';
    	if (style==null) return null;
    	Matcher m = FONT_FAMILY_IN_CSS.matcher(style);
    	return (m.find() ? m.group(1).trim().replace("'", "") : null);
    }

    /** Undo the font RunFontSelector set, leaving the text in whatever font it inherits.
     *
     * @since 17.0.3
     */
    public static void removeFont(Element styled) {

    	styled.removeAttribute("font-family");
    	String style = styled.getAttribute("style");
    	if ((style!=null) && (style.length()>0)) {
    		String stripped = FONT_FAMILY_IN_CSS.matcher(style).replaceAll("");
    		if (stripped.length()==0) {
    			styled.removeAttribute("style");
    		} else {
    			styled.setAttribute("style", stripped);
    		}
    	}
    }

    /** Unmarshal the w:pPr, if there is one. */
    private static PPr toPPr(NodeIterator pPrNodeIt) {

    	PPr pPr = null;
		if (pPrNodeIt!=null) 
		{ 
    		Node n = pPrNodeIt.nextNode(); 
    		if (n!=null) {
    			try {
        			Unmarshaller u = Context.jc.createUnmarshaller();			
        			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
        			Object jaxb = u.unmarshal(n);
    				pPr =  (PPr)jaxb;
    			} catch (ClassCastException e) {
    				log.error("Couldn't cast  to PPr!");
    			} catch (JAXBException e) {
    				log.error(e.getMessage(), e);
				}        	        			
    		}
    	}
		return pPr;
    }

    /** Unmarshal the w:rPr, if there is one.  A w:paraRPr (ie the properties of the
     *  paragraph mark) is applied to the effective rPr of the paragraph. */
    private static RPr toRPr(AbstractWmlConversionContext conversionContext, NodeIterator rPrNodeIt, PPr pPr) {

    	RPr rPr = null;
		if (rPrNodeIt!=null) 
		{ 
    		Node n = rPrNodeIt.nextNode();
    		if (n!=null) {
    			try {
        			Unmarshaller u = Context.jc.createUnmarshaller();			
        			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
        			Object jaxb = u.unmarshal(n);
    				
    				if (jaxb instanceof RPr) {
    					rPr = (RPr)jaxb;
    				} else if (jaxb instanceof ParaRPr) {
    					rPr = conversionContext.getPropertyResolver().getEffectiveRPr(null, pPr); 
    	        		StyleUtil.apply((ParaRPr)jaxb, rPr); 				
    				}    				
    				
    			} catch (ClassCastException e) {
    				log.error("Couldn't cast  to RPr!");
    			} catch (JAXBException e) {
    				log.error(e.getMessage(), e);
				} catch (Docx4JException e) {
    				log.error(e.getMessage(), e);
				}        	        			
    		}
    	}
		return rPr;
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

	/** As above, but also making the pPr of the containing w:p available to the writer.
	 *
	 *  A writer which generates content of its own (a field) has no w:t to hang a font
	 *  off, and can't reach the containing paragraph itself (it is given the node
	 *  unmarshalled on its own), so it needs this in order to resolve the font the
	 *  same way an ordinary run's text is resolved.
	 *
	 * @param context
	 * @param node
	 * @param childResults the already transformed node (element) content
	 * @param pPrNodeIt the w:pPr of the containing w:p (may be empty)
	 * @since 17.0.3
	 */
	public static Node toNode(AbstractWmlConversionContext context, Node node, NodeList childResults,
			NodeIterator pPrNodeIt) {

		context.setCurrentPPr(toPPr(pPrNodeIt));
		try {
			return context.getWriterRegistry().toNode(context, node, childResults);
		} finally {
			context.setCurrentPPr(null);
		}
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

	public static void logDebug(AbstractConversionContext context, NodeIterator ni, String message) {
		
		context.getLog().debug(message);
		Node n;
        while ((n = ni.nextNode()) != null) {
    		context.getLog().debug( XmlUtils.w3CDomNodeToString(n));
        }		
	}	
	
	public static void logInfo(AbstractConversionContext context, String message) {
		context.getLog().info(message);
	}	
	
	public static void logWarn(AbstractConversionContext context, String message) {
		
//		if (message.startsWith("XSLT_")) {
//			(new Throwable()).printStackTrace();
//		}
		
		context.getLog().warn(message);
	}
	
	
	
}
