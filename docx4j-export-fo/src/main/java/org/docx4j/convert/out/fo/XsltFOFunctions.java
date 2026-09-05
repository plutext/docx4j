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
package org.docx4j.convert.out.fo;

import java.util.List;

import jakarta.xml.bind.Unmarshaller;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.ConversionSectionWrapper;
import org.docx4j.convert.out.common.HiddenText;
import org.docx4j.convert.out.common.XsltCommonFunctions;
import org.docx4j.convert.out.common.preprocess.Containerization;
import org.docx4j.fonts.RunFontSelector;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.properties.paragraph.Bidi;
import org.docx4j.model.properties.paragraph.Indent;
import org.docx4j.model.properties.paragraph.Justification;
import org.docx4j.model.properties.paragraph.PBorderBottom;
import org.docx4j.model.properties.paragraph.PBorderTop;
import org.docx4j.model.properties.paragraph.PShading;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTPageNumber;
import org.docx4j.wml.CTTabStop;
import org.docx4j.wml.CTTwipsMeasure;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.PPrBase.NumPr.Ilvl;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STTabJc;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tabs;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TrPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.NodeIterator;


/** 
 * This class contains static functions that are specific to the FO xsl-transformation and 
 * are called from docx2fo.xslt. 
 *  
 */
public class XsltFOFunctions {
	
	private static Logger log = LoggerFactory.getLogger(XsltFOFunctions.class);
	

	public static DocumentFragment getLayoutMasterSetFragment(AbstractWmlConversionContext context) {
		return LayoutMasterSetBuilder.getLayoutMasterSetFragment(context);
	}
	
    public static DocumentFragment createBlockForSdt(FOConversionContext context,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults, String tag) {

    	DocumentFragment docfrag = createBlock(context,
        		 pPrNodeIt,
        		 pStyleVal,  childResults,
        		 true);

    	applySdtContainerMargins(docfrag, tag);
    	wrapInBidiBlockContainer(docfrag);

    	return docfrag;
    }

    /**
     * JAXB-typed form of createBlockForSdt, for the visitor pathway
     * (FOExporterVisitorGenerator).
     *
     * @param pPrDirect the pPr of the first paragraph inside the container
     * @param childResults the already converted contents of the sdt
     * @since 17.0.4
     */
    public static DocumentFragment createBlockForSdt(FOConversionContext context,
    		PPr pPrDirect, String pStyleVal, Node childResults, String tag) {

    	DocumentFragment docfrag = createBlock(context, pPrDirect, pStyleVal, childResults, true);

    	applySdtContainerMargins(docfrag, tag);
    	wrapInBidiBlockContainer(docfrag);

    	return docfrag;
    }

	/**
	 * Set margins, but only for a shading container, not a borders container
	 * (so there isn't a white strip between shaded paragraphs).
	 */
	private static void applySdtContainerMargins(DocumentFragment docfrag, String tag) {

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
	}

    public static DocumentFragment createInlineForSdt(
    		FOConversionContext context,
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
     * JAXB-typed form of createInlineForSdt, for the visitor pathway: an fo:inline
     * carrying the effective run properties of the sdtPr's rPr (a run borders/shading
     * container created by the Containerization preprocess).
     *
     * @since 17.0.4
     */
    public static DocumentFragment createInlineForSdt(
    		FOConversionContext context,
    		RPr rPrDirect, Node childResults) {

        try {
			RPr rPr = context.getPropertyResolver().getEffectiveRPr(rPrDirect, null);

			Document document = XmlUtils.getNewDocumentBuilder().newDocument();
			Element foInlineElement = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:inline");
			document.appendChild(foInlineElement);

			createFoAttributes(context.getWmlPackage(), rPr, foInlineElement);

			if (childResults!=null) {
				XmlUtils.treeCopy(childResults, foInlineElement);
			}

			DocumentFragment docfrag = document.createDocumentFragment();
			docfrag.appendChild(document.getDocumentElement());
			return docfrag;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
    }

    /**
     * Convert an m:oMath / m:oMathPara node to FO for PDF. When a MathML renderer
     * (jeuclid-fop) is on the classpath, emits fo:instream-foreign-object wrapping
     * the MathML from OmmlToMathML (FOP + the plugin render it); otherwise falls
     * back to the equation's text so PDF export never fails on math. Shared by the
     * XSLT pathway (docx2fo.xslt, via this NodeIterator form) and the visitor
     * pathway (FOExporterVisitorGenerator, via the Object form). See CR-008-math-pdf-fo.
     *
     * @since 17.0.4
     */
    public static DocumentFragment mathToFO(FOConversionContext context, NodeIterator ommlNodeIt) {
    	Node n = (ommlNodeIt == null) ? null : ommlNodeIt.nextNode();
    	if (n == null) {
    		return null;
    	}
    	try {
    		Unmarshaller u = Context.jc.createUnmarshaller();
    		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
    		return mathToFO(context, XmlUtils.unwrap(u.unmarshal(n)));
    	} catch (Exception e) {
    		log.warn("MathML->FO failed; omitting equation: " + e.getMessage());
    		return null;
    	}
    }

    public static DocumentFragment mathToFO(FOConversionContext context, Object omml) {
    	// Xalan resolves the stylesheet's call to this overload and passes its node
    	// iterator (a DTMNodeIterator) as the Object, not to the NodeIterator form
    	// above; the equation then went unconverted in the XSLT pathway (17.0.4).
    	if (omml instanceof NodeIterator) {
    		return mathToFO(context, (NodeIterator) omml);
    	}
    	if (omml instanceof Node) {
    		try {
    			Unmarshaller u = Context.jc.createUnmarshaller();
    			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
    			omml = XmlUtils.unwrap(u.unmarshal((Node) omml));
    		} catch (Exception e) {
    			log.warn("MathML->FO failed; omitting equation: " + e.getMessage());
    			return null;
    		}
    	}
    	Document document = XmlUtils.getNewDocumentBuilder().newDocument();
    	DocumentFragment frag = document.createDocumentFragment();

    	if (org.docx4j.convert.out.fo.renderers.FORendererApacheFOP.isMathMLRendererAvailable()) {
    		try {
    			org.docx4j.convert.out.mathml.OmmlToMathML converter =
    					new org.docx4j.convert.out.mathml.OmmlToMathML();
    			Document mathDoc;
    			if (omml instanceof org.docx4j.math.CTOMathPara) {
    				mathDoc = converter.toMathMLDocument((org.docx4j.math.CTOMathPara) omml);
    			} else {
    				mathDoc = converter.toMathMLDocument((org.docx4j.math.CTOMath) omml);
    			}
    			Element ifo = document.createElementNS(
    					"http://www.w3.org/1999/XSL/Format", "fo:instream-foreign-object");
    			ifo.appendChild(document.importNode(mathDoc.getDocumentElement(), true));
    			frag.appendChild(ifo);
    			return frag;
    		} catch (org.docx4j.convert.out.mathml.MathConversionException e) {
    			log.warn("OMML->MathML failed; emitting equation text: " + e.getMessage());
    			// fall through to text
    		}
    	}

    	String text = mathText(omml);
    	if (!text.isEmpty()) {
    		frag.appendChild(document.createTextNode(text));
    	}
    	return frag;
    }

    /** Plain text of an OMath/OMathPara, for the no-renderer / failure fallback. */
    private static String mathText(Object omml) {
    	try {
    		org.docx4j.math.ObjectFactory of = new org.docx4j.math.ObjectFactory();
    		Object toMarshal = (omml instanceof org.docx4j.math.CTOMathPara)
    				? of.createOMathPara((org.docx4j.math.CTOMathPara) omml)
    				: of.createOMath((org.docx4j.math.CTOMath) omml);
    		Document d = XmlUtils.marshaltoW3CDomDocument(toMarshal);
    		org.w3c.dom.NodeList ts = d.getElementsByTagNameNS(
    				"http://schemas.openxmlformats.org/officeDocument/2006/math", "t");
    		StringBuilder sb = new StringBuilder();
    		for (int i = 0; i < ts.getLength(); i++) {
    			sb.append(ts.item(i).getTextContent());
    		}
    		return sb.toString();
    	} catch (Exception e) {
    		return "";
    	}
    }

    /**
     * This is invoked on every paragraph, whether it has a pPr or not.
     * 
     * @param wmlPackage
     * @param pPrNodeIt
     * @param pStyleVal
     * @param childResults - the already transformed contents of the paragraph.
     * @return
     */
    public static DocumentFragment createBlockForPPr(
    		FOConversionContext context,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults) {

    	DocumentFragment df = createBlock(
        		context,
        		pPrNodeIt,
        		pStyleVal, childResults,
        		false);

    	return postProcessBlockForPPr(df);
    }

    /**
     * JAXB-typed form of createBlockForPPr, for the visitor pathway
     * (FOExporterVisitorGenerator).
     *
     * @param pPrDirect the paragraph's own pPr (may be null)
     * @param childResults the already converted contents of the paragraph
     * @since 17.0.4
     */
    public static DocumentFragment createBlockForPPr(
    		FOConversionContext context,
    		PPr pPrDirect,
    		String pStyleVal, Node childResults) {

    	DocumentFragment df = createBlock(context, pPrDirect, pStyleVal, childResults, false);

    	return postProcessBlockForPPr(df);
    }

    private static DocumentFragment postProcessBlockForPPr(DocumentFragment df) {

    	// Note: prior to 17.0.1, an inline with direction="rtl" (as then created by
    	// the TextDirection class for a w:rtl run) was wrapped here in
    	//    <fo:bidi-override direction="rtl" unicode-bidi="embed">
    	// That actively broke FOP's own bidi processing (unshaped Arabic, and wrong
    	// run order in mixed RTL/LTR paragraphs); see issue 660.  Instead, a w:bidi
    	// paragraph now gets an RTL paragraph embedding level; see wrapInBidiBlockContainer.

    	if (df==null || !(df.getFirstChild() instanceof Element)) return df;

    	Element block = (Element)df.getFirstChild();

    	if (foContainsStretchingLeader(block)) {
			// ptab to leader implementation:
			// for leader to work as expected in fop, we need text-align-last; see http://xmlgraphics.apache.org/fop/faq.html#leader-expansion
			// this code adds that.
    		// Note that it doesn't seem to be necessary for leader in TOC, but it doesn't hurt
    		// (a leader of fixed length, standing in for a leading tab, must not justify the last line)
			block.setAttribute("text-align-last", "justify");
    	}

    	// Hyphenation defaults to off
    	if (Docx4jProperties.getProperty("docx4j.convert.out.fo.hyphenate", false)) {
    		block.setAttribute("hyphenate", "true");
    	}

    	wrapInBidiBlockContainer(df);

    	return df;
    }

    /**
     * If this paragraph has w:bidi set, the Bidi class will have put a
     * writing-mode attribute on its fo:block (where the property does not
     * apply, and FOP ignores it).  Move it to an fo:block-container wrapped
     * around the fragment's content, which is where it takes effect: it
     * gives FOP's Unicode bidi algorithm implementation an RTL paragraph
     * embedding level, so that RTL and LTR runs are ordered correctly.
     * See issue 660.
     *
     * @since 17.0.1
     */
    protected static void wrapInBidiBlockContainer(DocumentFragment df) {

    	if (df==null || !(df.getFirstChild() instanceof Element)) return;
    	Element first = (Element)df.getFirstChild();

    	// The attribute is on the block carrying the pPr properties: the first
    	// child itself, or, in the list item case, a block inside the list-block.
    	Element marked = findElementWithWritingMode(first);
    	if (marked==null) return;

    	marked.removeAttribute(Bidi.FO_WRITING_MODE_NAME);

    	Element container = df.getOwnerDocument().createElementNS("http://www.w3.org/1999/XSL/Format",
    			"fo:block-container");
    	container.setAttribute(Bidi.FO_WRITING_MODE_NAME, Bidi.FO_WRITING_MODE_RTL);

    	df.replaceChild(container, first);
    	container.appendChild(first);
    }

    private static Element findElementWithWritingMode(Element el) {

    	if (el.hasAttribute(Bidi.FO_WRITING_MODE_NAME)) return el;

    	// Only descend the list item structure (the marked block sits inside
    	// fo:list-item-body there).  In particular, don't descend into an
    	// fo:block: anything below it is the paragraph's content, which may
    	// contain already-wrapped nested paragraphs of its own.
    	String localName = el.getLocalName();
    	if ("list-block".equals(localName)
    			|| "list-item".equals(localName)
    			|| "list-item-body".equals(localName)) {

	    	NodeList children = el.getChildNodes();
	    	for (int i=0; i<children.getLength(); i++) {
	    		if (children.item(i) instanceof Element) {
	    			Element found = findElementWithWritingMode((Element)children.item(i));
	    			if (found!=null) return found;
	    		}
	    	}
    	}
    	return null;
    }
    
    
    /**
     * Recurse sourceNode looking to see whether it contains element with local name elementName
     * @param sourceNode
     * @param elementName
     * @return
     */
	/** a leader with a range of lengths (dot leaders, ptabs): FOP expands it only on a justified last line */
	private static boolean foContainsStretchingLeader(Element block) {
		NodeList nl = block.getElementsByTagNameNS(XSL_FO, "leader");
		for (int i = 0; i < nl.getLength(); i++) {
			Element l = (Element) nl.item(i);
			if (l.hasAttribute("leader-length.maximum") || l.hasAttribute("leader-length.optimum")) return true;
		}
		return false;
	}

    private static boolean foContainsElement(Node sourceNode, String elementName) {

        switch (sourceNode.getNodeType() ) {

	    	case Node.DOCUMENT_NODE: // type 9
        	case Node.DOCUMENT_FRAGMENT_NODE: // type 11
        
                // recurse on each child
                NodeList nodes = sourceNode.getChildNodes();
                if (nodes != null) {
                    for (int i=0; i<nodes.getLength(); i++) {
                    	if (foContainsElement(nodes.item(i), elementName)) {
                    		return true;
                    	}
                    }
                }
                return false;
                
            case Node.ELEMENT_NODE:
                
                // Do it...
            	Element el = (Element)sourceNode;
            	if (el.getLocalName().equals(elementName)) {
//            		log.debug("Got " + elementName);
            		return true;
            	}

                // recurse on each child
                NodeList children = sourceNode.getChildNodes();
                if (children != null) {
                    for (int i=0; i<children.getLength(); i++) {
                    	if (foContainsElement(children.item(i), elementName)) {
                    		return true;
                    	}
                    }
                }

                return false;

            case Node.TEXT_NODE:
            	
                return false;
                
            default:
            	
                return false;
        }
    }
    
    private static DocumentFragment createBlock(
    		FOConversionContext context,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults,
    		boolean sdt) {

    	// Note that this is invoked for every paragraph with a pPr node.

    	// incoming objects are org.apache.xml.dtm.ref.DTMNodeIterator
    	// which implements org.w3c.dom.traversal.NodeIterator

    	// Get the pPr node as a JAXB object,
    	// so we can read it using our standard
    	// methods.  Its a bit sad that we
    	// can't just adorn our DOM tree with the
    	// original JAXB objects?
    	PPr pPrDirect = null;
    	if (pPrNodeIt!=null) {
    		Node n = pPrNodeIt.nextNode();
    		if (n==null) {
    			if (log.isDebugEnabled()) {
    				log.debug("pPrNodeIt.nextNode() was null (ie there is no pPr in this p)");
    			}
    		} else {
    			if (log.isDebugEnabled()) {
    				log.debug( "P actual pPr: "+ XmlUtils.w3CDomNodeToString(n) );
    			}
    			try {
	    			Unmarshaller u = Context.jc.createUnmarshaller();
	    			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
					pPrDirect =  (PPr)u.unmarshal(n);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
			    	return null;
				}
    		}
    	}
    	return createBlock(context, pPrDirect, pStyleVal, childResults.nextNode(), sdt);
    }

    /**
     * JAXB-typed form: determine the effective paragraph and run properties for
     * pPrDirect, and format the block.  Used by both the XSLT pathway (above, after
     * unmarshalling) and the visitor pathway.
     *
     * @since 17.0.4
     */
    protected static DocumentFragment createBlock(
    		FOConversionContext context,
    		PPr pPrDirect,
    		String pStyleVal, Node childResults,
    		boolean sdt) {

    	PropertyResolver propertyResolver;
		try {
			propertyResolver = context.getPropertyResolver();
		} catch (Docx4JException e) {
			log.error(e.getMessage(), e);
	    	return null;
		}

		Style defaultParagraphStyle =
				(context.getWmlPackage().getMainDocumentPart().getStyleDefinitionsPart(false) != null ?
				context.getWmlPackage().getMainDocumentPart().getStyleDefinitionsPart(false).getDefaultParagraphStyle() :
				null);

    	String defaultParagraphStyleId;
    	if (defaultParagraphStyle==null) // possible, for non MS source docx
    		defaultParagraphStyleId = "Normal";
    	else defaultParagraphStyleId = defaultParagraphStyle.getStyleId();

		if ( pStyleVal ==null || pStyleVal.equals("") ) {
//			pStyleVal = "Normal";
			pStyleVal = defaultParagraphStyleId;
		}
		if (log.isDebugEnabled()) {
			log.debug("style '" + pStyleVal );
		}

		/* First, determine effective paragraph and run properties (pPr, rPr) */
    	PPr pPr = null;
    	RPr rPr = null;
    	RPr rPrParagraphMark = null;  // required for list item label
        try {

        	if (pPrDirect==null) {
            	pPr = propertyResolver.getEffectivePPr(defaultParagraphStyleId);
            	rPr = propertyResolver.getEffectiveRPr(defaultParagraphStyleId);
            	rPrParagraphMark = rPr;
        		// TODO - in this case, we should be able to compute once,
        		// and on subsequent calls, just return pre computed value
        	} else {
				pPr = propertyResolver.getEffectivePPr(pPrDirect);
				if ((pPr==null) && (log.isDebugEnabled())) {
					log.debug("pPr null; obtained from: " + XmlUtils.marshaltoString(pPrDirect, true, true) );
				}

				// On the block representing the w:p, we want to put both
			    // pPr and rPr attributes.

				if (log.isDebugEnabled()) {
					log.debug("getting rPr for paragraph style");
				}

				// rPr in pPr direct formatting only applies to paragraph mark,
				// and by virtue of that, to list item label,
				// so pass null here.
				// 2018 05 .. 17.0.4: the paragraph mark's sz was applied to the block too,
        		// on the theory that a 12pt mark on 11pt runs gives more line spacing.
        		// Measured against Word 365 (CR-001 harness, line-mixed probe): it does
        		// not; Word ignores the mark's size for a paragraph that has text, and
        		// putting it on the block made FOP use it as a floor for every line
        		// (a 36pt mark gave 36pt lines).  Since 17.0.5 line heights come from
        		// the runs (see WordLineMetrics / applyBlockLineHeight below); the mark
        		// only sizes an empty paragraph, via rPrParagraphMark.  Lang is kept.
        		RPr fontSzOnlyRPr = new RPr();
        		if (pPrDirect.getRPr()!=null) {
        			// Assume no need to clone
        			fontSzOnlyRPr.setLang(pPrDirect.getRPr().getLang());
        		}

        		rPr = propertyResolver.getEffectiveRPr(fontSzOnlyRPr, pPrDirect);

				// Now, work out the value for list item label
        		rPrParagraphMark = XmlUtils.deepCopy(rPr);

//    			System.out.println("p rpr-->" + XmlUtils.marshaltoString(pPrDirect.getRPr()));

        		StyleUtil.apply(pPrDirect.getRPr(), rPrParagraphMark);
        	}

			if (log.isDebugEnabled() && pPr!=null) {
				log.debug("P effective pPr: "+ XmlUtils.marshaltoString(pPr, true, true));
			}
		} catch (Exception e) {
			//log.error(e.getLocalizedMessage(), e);
			log.error(e.getMessage(), e);
	    	return null;
		}

		// A paragraph whose mark is hidden text and whose content came to nothing (its
		// runs were all hidden, or it had none) leaves no line at all in Word, where an
		// empty block here would push everything below it down.  @since 17.0.5
		if (HiddenText.isHiddenParagraph(rPrParagraphMark) && isEmptyContent(childResults)) {
			return null;
		}

		/* Now that we have pPr, we can format the block. */
		return createBlock(context.getWmlPackage(), context.getRunFontSelector(), pStyleVal, childResults, sdt, pPrDirect, pPr, rPr, rPrParagraphMark);

    }

	/** Whether the paragraph's converted content is nothing at all: no elements, and no
	 *  text beyond white space the stylesheet may have contributed.  @since 17.0.5 */
	private static boolean isEmptyContent(Node childResults) {
		if (childResults==null) return true;
		for (Node n = childResults.getFirstChild(); n!=null; n = n.getNextSibling()) {
			if (n.getNodeType()==Node.ELEMENT_NODE) return false;
			if (n.getNodeType()==Node.TEXT_NODE || n.getNodeType()==Node.CDATA_SECTION_NODE) {
				String v = n.getNodeValue();
				if (v!=null && v.trim().length()>0) return false;
			}
		}
		return true;
	}

	/** The font-family which applies to text we generate ourselves - a tab leader, or
	 *  the space we put in an otherwise empty block - as opposed to the contents of a
	 *  w:t.  Empty string where it can't be determined.
	 *
	 *  For use from the stylesheet; see also XsltCommonFunctions.fontSelectorForGeneratedText,
	 *  which is what you want if you can emit the text itself, rather than needing to set
	 *  a font on an element of your own.
	 *
	 * @since 17.0.3
	 */
	public static String getFontFamily(FOConversionContext context,
			NodeIterator pPrNodeIt, NodeIterator rPrNodeIt) {

		return fontFamilyOf(
				XsltCommonFunctions.fontSelectorForGeneratedText(context, pPrNodeIt, rPrNodeIt, "."));
	}

	/** JAXB-typed form of the above, for the visitor pathway.
	 *
	 * @since 17.0.4
	 */
	public static String getFontFamily(FOConversionContext context, PPr pPr, RPr rPr) {

		return fontFamilyOf(
				XsltCommonFunctions.fontSelectorForGeneratedText(context, pPr, rPr, "."));
	}

	private static String resolveFontFamily(WordprocessingMLPackage wmlPackage,
			RunFontSelector runFontSelector, PPr pPr, RPr rPr, String sampleText) {

		if (runFontSelector==null) return "";
		try {
			org.docx4j.wml.Text sample = Context.getWmlObjectFactory().createText();
			sample.setValue(sampleText);
			Object result = runFontSelector.fontSelector(pPr, rPr, sample);
			if (!(result instanceof DocumentFragment)) return "";
			// the font may not have these characters; see XsltCommonFunctions.fontCanRender
			Node styled = ((DocumentFragment)result).getFirstChild();
			if (styled instanceof Element
					&& !XsltCommonFunctions.fontCanRender(
							(wmlPackage==null ? null : wmlPackage.getFontMapper()),
							(Element)styled, sampleText)) return "";
			return fontFamilyOf((DocumentFragment)result);
		} catch (Exception e) {
			// Not fatal; the renderer's default font is used, as it was before
			log.warn("Couldn't determine font: " + e.getMessage(), e);
			return "";
		}
	}

	private static String fontFamilyOf(DocumentFragment df) {

		if (df==null) return "";
		Node n = df.getFirstChild();
		if (n instanceof Element) {
			String fontFamily = ((Element)n).getAttribute("font-family");
			if (fontFamily!=null) return fontFamily;
		}
		return "";
	}

	protected static DocumentFragment createBlock(WordprocessingMLPackage wmlPackage, RunFontSelector runFontSelector,
			String pStyleVal, NodeIterator childResults,
			boolean sdt, PPr pPrDirect, PPr pPr, RPr rPr, RPr rPrParagraphMark) {

		return createBlock(wmlPackage, runFontSelector, pStyleVal, childResults.nextNode(),
				sdt, pPrDirect, pPr, rPr, rPrParagraphMark);
	}

	protected static DocumentFragment createBlock(WordprocessingMLPackage wmlPackage, RunFontSelector runFontSelector,
			String pStyleVal, Node childResults,
			boolean sdt, PPr pPrDirect, PPr pPr, RPr rPr, RPr rPrParagraphMark) {

        try {
            // Create a DOM builder and parse the fragment			
			Document document = XmlUtils.getNewDocumentBuilder().newDocument();
			//log.info("Document: " + document.getClass().getName() );
			
			Element foBlockElement = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block");
			Element foListBlock = null;
			boolean indentHandledByNumbering = false;
			// Is it a list item?
			if (sdt) { 
				// Don't convert an SDT into an extra fo:list-block!
				document.appendChild(foBlockElement);
			} else if (numIdVal(pPr)!=null
					&& pPr.getNumPr().getNumId().getVal().longValue()!=0 //zero means no numbering
					) {
				
				foListBlock = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:list-block");
				document.appendChild(foListBlock);
				
				// Its a list item.  At present we make a new list-block for
				// each list-item. This is not great; DocumentModel will ultimately
				// allow us to use fo:list-block properly.

				indentHandledByNumbering = createListBlock(wmlPackage, runFontSelector, pStyleVal, pPrDirect, pPr, rPr,
						rPrParagraphMark, document, foBlockElement, foListBlock);
				
				if (log.isDebugEnabled()) {
					log.debug("bare list result: " + XmlUtils.w3CDomNodeToString(foListBlock) );
				}
				
				
			} else /* its not a list item */ {
				document.appendChild(foBlockElement);
			}
			
						/* Now apply pPr (whether its a list or not) */				
			if (pPr!=null) {
				// Ignore paragraph borders once inside the container
				boolean ignoreBorders = !sdt;
				createFoAttributes(wmlPackage, pPr, ((Element)foBlockElement), indentHandledByNumbering, ignoreBorders );				
			}
						// Hints for WordLayoutFixups (removed there, so FOP never sees them): the
			// paragraph style, and whether w:contextualSpacing applies, which needs the
			// neighbours.  Plain attribute names: a namespaced attribute loses its
			// declaration when Xalan copies the fragment in the XSLT pathway.  @since 17.0.5
			// (not on a borders/shading container: its paragraphs carry their own, and
			// a hint on the wrapper would make it look like a paragraph of the default
			// style next to its neighbours)
			if (WordLayoutFixups.isEnabled() && !sdt) {
				foBlockElement.setAttribute(WordLayoutFixups.HINT_PSTYLE, pStyleVal==null ? "" : pStyleVal);
				if (pPr!=null && pPr.getContextualSpacing()!=null && pPr.getContextualSpacing().isVal()) {
					foBlockElement.setAttribute(WordLayoutFixups.HINT_CONTEXTUAL, "1");
				}
				if (pPr!=null && pPr.getSpacing()!=null
						&& (pPr.getSpacing().isBeforeAutospacing() || pPr.getSpacing().isAfterAutospacing())) {
					foBlockElement.setAttribute(WordLayoutFixups.HINT_AUTOSPACING,
							(pPr.getSpacing().isBeforeAutospacing() ? "b" : "") + (pPr.getSpacing().isAfterAutospacing() ? "a" : ""));
				}
				if (foListBlock!=null) {
					foBlockElement.setAttribute(WordLayoutFixups.HINT_LIST, "1");
				}
			}
			
			/* Now apply rPr */				
			if (rPr!=null) {
				
				if (foListBlock==null) {
					createFoAttributes(wmlPackage, rPr, ((Element)foBlockElement) );
				} else {
					createFoAttributes(wmlPackage, rPr, ((Element)foListBlock) );					
				}
	        }

			if (log.isDebugEnabled()) {
				log.debug("after createFoAttributes: " + XmlUtils.w3CDomNodeToString(foBlockElement) );
			}
			
			// Our fo:block wraps whatever result tree fragment
			// our style sheet produced when it applied-templates
			// to the child nodes
			Node n = childResults;

			// Handle empty case - want the block to be preserved!
			if (n.getChildNodes().getLength()==0) {
				
				((Element)foBlockElement).setAttribute( "white-space-treatment", "preserve");
				
				/* The space we're about to put in the block is the block's own content,
				 * and what it represents is the paragraph mark, so it is one of the few
				 * places where the paragraph mark's rPr is the right thing to format
				 * with.  Without a font, it would be measured in the renderer's default
				 * font, and the height of an empty paragraph would be wrong.
				 * @since 17.0.3 */
				String fontFamily = resolveFontFamily(wmlPackage, runFontSelector, pPr,
						(rPrParagraphMark!=null ? rPrParagraphMark : rPr), " ");
				if (fontFamily.length()>0) {
					((Element)foBlockElement).setAttribute("font-family", fontFamily);
				}
				
				foBlockElement.setTextContent(" ");
				applyEmptyParagraphLineHeight(foBlockElement, fontFamily, pPr,
						(rPrParagraphMark!=null ? rPrParagraphMark : rPr),
						runFontSelector==null ? null : runFontSelector.asciiFontName(rPrParagraphMark!=null ? rPrParagraphMark : rPr));
			} else {
			
				/* don't do:
				 * 
	            ((Element)foBlockElement).setAttribute( "white-space-treatment", "preserve");
	            ((Element)foBlockElement).setAttribute( "white-space-collapse", "false");

	            Suggested at https://www.docx4java.org/forums/docx-java-f6/export-fo-preserve-whitespaces-t2762.html 
	            because it causes unwanted formatting issues.  See https://stackoverflow.com/questions/57475550/unwanted-indent-after-line-wrap/57488818
	            and https://github.com/plutext/docx4j/issues/369
	            
	            In any subsequent review of this, also consider https://www.docx4java.org/forums/pdf-output-f27/converting-docx-to-pdf-not-preserving-whitespace-t2752.html
	            
				 */
	            
				
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
	            if (!applyBlockLineHeight(foBlockElement, pPr)) {
	            	// no run text to size the lines from (a paragraph holding only a
	            	// picture): the paragraph's own font, as for an empty paragraph, so
	            	// the line manager still applies Word's rules (the picture's line
	            	// then has the picture's height and no descent)
					String fontFamily = resolveFontFamily(wmlPackage, runFontSelector, pPr,
							(rPrParagraphMark!=null ? rPrParagraphMark : rPr), " ");
					if (fontFamily.length()>0) {
						((Element)foBlockElement).setAttribute("font-family", fontFamily);
					}
					applyEmptyParagraphLineHeight(foBlockElement, fontFamily, pPr,
							(rPrParagraphMark!=null ? rPrParagraphMark : rPr),
							runFontSelector==null ? null : runFontSelector.asciiFontName(rPrParagraphMark!=null ? rPrParagraphMark : rPr));
	            }
			}
			// FOP doesn't support "ignore-if-surrounding-linefeed", and "pre" is no good, since wrapping does not happen
			// (so paragraph continues right over edge of page)
			//((Element)foBlockElement).setAttribute( "white-space", "ignore");
			
			DocumentFragment docfrag = document.createDocumentFragment();
			docfrag.appendChild(document.getDocumentElement());

			return docfrag;
						
		} catch (Exception e) {
			//log.error(e.getLocalizedMessage(), e);
			log.error(e.getMessage(), e);
		}
        return null;
	}

		/**
	 * Give the block the font and line-height of the run that owns most of the
	 * paragraph's text.
	 *
	 * Why the block: measured with FOP 2.11 (CR-001 harness), a line's height is
	 * the union of the block's nominal line rectangle (the block's font plus its
	 * line-height) and the bare glyph boxes of the inlines; the line-height on an
	 * fo:inline is ignored for line stacking.  So a block whose font differs from
	 * its runs' adds a per-line error (0.7pt for 11pt default vs 12pt Liberation
	 * Serif) however good the inline values are, while a block in the run's font
	 * with Word's line-height reproduces Word's pitch exactly (13.80pt at 12pt).
	 *
	 * RunFontSelector puts font-family, font-size and the Word line-height (from
	 * the physical font's metrics, the run size and the paragraph's w:spacing) on
	 * every span it creates; this picks the combination carrying the most
	 * characters, which is the whole paragraph in the common uniform case.  A
	 * larger run inside such a paragraph still enlarges its own line through its
	 * glyph box (as in Word, though by its ascent+descent rather than Word's full
	 * line height for that size); a smaller run does not shrink anything.
	 *
	 * @since 17.0.5
	 */
	/** @return whether a run font was found to size the block's lines from */
	protected static boolean applyBlockLineHeight(Element foBlockElement, PPr pPr) {
		java.util.Map<String, long[]> weights = new java.util.HashMap<>(); // key -> {chars}
		java.util.Map<String, String[]> attrs = new java.util.HashMap<>();
		collectRunFonts(foBlockElement, weights, attrs);
		String best = null;
		long bestWeight = -1;
		for (java.util.Map.Entry<String, long[]> e : weights.entrySet()) {
			if (e.getValue()[0] > bestWeight) {
				bestWeight = e.getValue()[0];
				best = e.getKey();
			}
		}
		if (best==null) return false;
		String[] a = attrs.get(best);
		if (a[0]!=null && a[0].length()>0) foBlockElement.setAttribute("font-family", a[0]);
		if (a[1]!=null && a[1].length()>0) foBlockElement.setAttribute("font-size", a[1]);
		foBlockElement.setAttribute("line-height", a[2]);
		applyLineBoxHints(foBlockElement, a[0], a[1], a[2], pPr, a[3]);
		return true;
	}

	/**
	 * Word's text box and baseline for the block's lines, as hints for
	 * WordLayoutFixups (which passes them to org.docx4j.fop.wordlayout's line manager
	 * when Word layout is on, or drops them): for "auto" spacing the box is the font's
	 * single-spacing pitch and the extra leading goes below it, where Word drops it
	 * at a page bottom; the baseline sits at ascent + external leading.  Exact and
	 * atLeast spacing keep the whole line as the box, with Word's baseline in it.
	 * Measured against Word 365 (CR-001 §6.3, §6.9).
	 *
	 * @since 17.0.5
	 */
	static void applyLineBoxHints(Element foBlockElement, String physicalFontFamily, String fontSize,
			String lineHeight, PPr pPr) {
		applyLineBoxHints(foBlockElement, physicalFontFamily, fontSize, lineHeight, pPr, null);
	}

	/** @param documentFont the font the docx asks for (its Word metrics, when known, size the box) */
	static void applyLineBoxHints(Element foBlockElement, String physicalFontFamily, String fontSize,
			String lineHeight, PPr pPr, String documentFont) {
		if (!WordLayoutFixups.isEnabled()) return;
		if (physicalFontFamily==null || physicalFontFamily.length()==0 || fontSize==null || !fontSize.endsWith("pt")
				|| lineHeight==null || !lineHeight.endsWith("pt")) return;
		org.docx4j.fonts.PhysicalFont pf = org.docx4j.fonts.PhysicalFonts.get(physicalFontFamily);
		org.docx4j.fonts.WordLineMetrics.Metrics m = org.docx4j.fonts.WordLineMetrics.get(documentFont, pf);
		if (m==null || m.fallback) return;
		double sizePt, lhPt;
		try {
			sizePt = Double.parseDouble(fontSize.substring(0, fontSize.length()-2));
			lhPt = Double.parseDouble(lineHeight.substring(0, lineHeight.length()-2));
		} catch (NumberFormatException e) {
			return;
		}
		org.docx4j.wml.PPrBase.Spacing spacing = pPr==null ? null : pPr.getSpacing();
		double single = m.lineHeightFactor() * sizePt;
		double natural = (m.winAscent + m.externalLeading) * sizePt;
		// (m already carries the document font's metrics when the table knows it)
		org.docx4j.wml.STLineSpacingRule rule = (spacing==null || spacing.getLine()==null) ? org.docx4j.wml.STLineSpacingRule.AUTO
				: (spacing.getLineRule()==null ? org.docx4j.wml.STLineSpacingRule.AUTO : spacing.getLineRule());
		double box, baseline;
		String ruleName;
		switch (rule) {
		case EXACT:
			// the line is the given height and the text is placed in it in the font's
			// ascent:descent ratio (measured 0.80 of the line for Liberation Serif;
			// usWinAscent/(usWinAscent+usWinDescent) gives 0.81)
			box = lhPt;
			baseline = lhPt * m.winAscent / (m.winAscent + m.winDescent);
			ruleName = "exact";
			break;
		case AT_LEAST:
			// the natural line; the line manager puts any shortfall against the
			// block's line-height above the text
			box = single;
			baseline = natural;
			ruleName = "atLeast";
			break;
		default:
			// the natural line; the block's line-height / box is the multiple, whose
			// extra goes below the text as droppable leading.  Multiples below 1
			// shrink the box, off the top.
			box = Math.min(single, lhPt);
			baseline = lhPt < single ? Math.max(0, natural - (single - lhPt)) : natural;
			ruleName = "auto";
		}
		foBlockElement.setAttribute(WordLayoutFixups.HINT_LINE_BOX, org.docx4j.fonts.WordLineMetrics.format(box));
		foBlockElement.setAttribute(WordLayoutFixups.HINT_BASELINE, org.docx4j.fonts.WordLineMetrics.format(baseline));
		foBlockElement.setAttribute(WordLayoutFixups.HINT_LINE_RULE, ruleName);
	}

	/** Accumulate, per (font-family, font-size, line-height) of the spans carrying a
	 *  line-height, the number of characters they hold; not descending into nested blocks. */
	private static void collectRunFonts(Node node, java.util.Map<String, long[]> weights, java.util.Map<String, String[]> attrs) {
		NodeList children = node.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			Node c = children.item(i);
			if (!(c instanceof Element)) continue;
			Element el = (Element)c;
			String localName = el.getLocalName();
			// nested blocks, tables, footnotes: their lines are their own
			if ("block".equals(localName) || "table".equals(localName)
					|| "block-container".equals(localName) || "list-block".equals(localName)
					|| "footnote".equals(localName)) continue;
			String lh = el.getAttribute("line-height");
			if (lh!=null && lh.endsWith("pt")) {
				String family = el.getAttribute("font-family");
				String size = inheritedAttribute(el, "font-size");
				String docFont = el.getAttribute(org.docx4j.fonts.RunFontSelector.HINT_FONT);
				String key = family + "|" + size + "|" + lh + "|" + docFont;
				long chars = el.getTextContent()==null ? 0 : el.getTextContent().length();
				weights.computeIfAbsent(key, k -> new long[1])[0] += Math.max(1, chars);
				attrs.putIfAbsent(key, new String[] { family, size, lh, docFont.length()==0 ? null : docFont });
			}
			collectRunFonts(el, weights, attrs);
		}
	}

	/** The attribute on this element or its nearest ancestor within the fragment (font-size
	 *  sits on the run's fo:inline, the font-family/line-height on the span inside it). */
	private static String inheritedAttribute(Element el, String name) {
		Node n = el;
		while (n instanceof Element) {
			String v = ((Element)n).getAttribute(name);
			if (v!=null && v.length()>0) return v;
			n = n.getParentNode();
		}
		return "";
	}

	/**
	 * An empty paragraph is as tall as its paragraph mark: the mark's font and
	 * size under the paragraph's w:spacing.
	 *
	 * @since 17.0.5
	 */
	/** Word's footnote separator is a rule 2in long. */
	public static final double FOOTNOTE_SEPARATOR_LENGTH_PT = 144;
	/** ... 0.6pt thick (measured from Word 365 PDF output) ... */
	public static final double FOOTNOTE_SEPARATOR_THICKNESS_PT = 0.6;

	/**
	 * The content of the xsl-footnote-separator static-content, as Word draws
	 * it: the separator note is a paragraph in the document's default font whose
	 * line holds a 2in rule, vertically centred in that line (measured: rule
	 * top at 6.5pt of a 13.44pt Carlito 11pt line).  A block-container as tall
	 * as that line, holding a 2in-wide block-container whose bottom border is
	 * the rule.
	 *
	 * @since 17.0.5
	 */
	public static DocumentFragment footnoteSeparator(AbstractWmlConversionContext context) {
		double lineHeightPt = 0;
		try {
			org.docx4j.wml.CTFootnotes footnotes = context.getWmlPackage().getMainDocumentPart()
					.getFootnotesPart().getJaxbElement();
			PPr pPr = null;
			for (org.docx4j.wml.CTFtnEdn note : footnotes.getFootnote()) {
				if (note.getType()==org.docx4j.wml.STFtnEdn.SEPARATOR) {
					for (Object o : note.getContent()) {
						o = XmlUtils.unwrap(o);
						if (o instanceof org.docx4j.wml.P) {
							pPr = ((org.docx4j.wml.P)o).getPPr();
							break;
						}
					}
					break;
				}
			}
			RPr rPr = context.getPropertyResolver().getEffectiveRPr(null, pPr);
			DocumentFragment styled = XsltCommonFunctions.fontSelectorForGeneratedText(context, pPr, rPr, " ");
			String lh = findAttribute(styled, "line-height");
			if (lh!=null && lh.endsWith("pt")) {
				lineHeightPt = Double.parseDouble(lh.substring(0, lh.length()-2));
			} else if (rPr!=null && rPr.getSz()!=null) {
				lineHeightPt = rPr.getSz().getVal().doubleValue()/2 * org.docx4j.fonts.WordLineMetrics.FALLBACK_FACTOR;
			}
		} catch (Exception e) {
			log.warn("Footnote separator line height: " + e.getMessage(), e);
		}
		if (lineHeightPt<=0) lineHeightPt = 13.8;

		Document d = XmlUtils.getNewDocumentBuilder().newDocument();
		DocumentFragment frag = d.createDocumentFragment();
		Element line = d.createElementNS(XSL_FO, "block-container");
		line.setAttribute("height", org.docx4j.fonts.WordLineMetrics.format(lineHeightPt));
		Element rule = d.createElementNS(XSL_FO, "block-container");
		rule.setAttribute("width", org.docx4j.fonts.WordLineMetrics.format(FOOTNOTE_SEPARATOR_LENGTH_PT));
		rule.setAttribute("height", org.docx4j.fonts.WordLineMetrics.format(
				(lineHeightPt - FOOTNOTE_SEPARATOR_THICKNESS_PT)/2));
		rule.setAttribute("border-bottom", org.docx4j.fonts.WordLineMetrics.format(FOOTNOTE_SEPARATOR_THICKNESS_PT)
				+ " solid black");
		rule.appendChild(d.createElementNS(XSL_FO, "block"));
		line.appendChild(rule);
		frag.appendChild(line);
		return frag;
	}

	private static final String XSL_FO = "http://www.w3.org/1999/XSL/Format";

	private static String findAttribute(Node n, String name) {
		if (n instanceof Element && ((Element)n).hasAttribute(name)) return ((Element)n).getAttribute(name);
		NodeList children = n.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			String v = findAttribute(children.item(i), name);
			if (v!=null) return v;
		}
		return null;
	}

	protected static void applyEmptyParagraphLineHeight(Element foBlockElement, String physicalFontFamily,
			PPr pPr, RPr markRPr) {
		applyEmptyParagraphLineHeight(foBlockElement, physicalFontFamily, pPr, markRPr, null);
	}

	/** @param documentFont the mark's document font (RunFontSelector.asciiFontName), for Word's metrics when a substitute renders it */
	protected static void applyEmptyParagraphLineHeight(Element foBlockElement, String physicalFontFamily,
			PPr pPr, RPr markRPr, String documentFont) {
		if (markRPr==null || markRPr.getSz()==null || markRPr.getSz().getVal()==null) return;
		double sizePt = markRPr.getSz().getVal().doubleValue()/2;
		org.docx4j.fonts.PhysicalFont pf = (physicalFontFamily==null || physicalFontFamily.length()==0)
				? null : org.docx4j.fonts.PhysicalFonts.get(physicalFontFamily);
		foBlockElement.setAttribute("font-size", org.docx4j.fonts.WordLineMetrics.format(sizePt));
		foBlockElement.setAttribute("line-height", org.docx4j.fonts.WordLineMetrics.lineHeightPtString(
				documentFont, pf, sizePt, pPr==null ? null : pPr.getSpacing()));
		applyLineBoxHints(foBlockElement, physicalFontFamily, foBlockElement.getAttribute("font-size"),
				foBlockElement.getAttribute("line-height"), pPr, documentFont);
	}

	/**
	 * The numbering level of a w:numPr as a string.  Both w:ilvl and its w:val are
	 * optional; Word treats their absence as level 0.
	 *
	 * @since 17.0.5
	 */
	static String ilvlVal(PPrBase.NumPr numPr) {
		if (numPr == null || numPr.getIlvl() == null || numPr.getIlvl().getVal() == null) return "0";
		return numPr.getIlvl().getVal().toString();
	}

	/**
	 * The w:numId of a pPr's numbering as a string, or null when there isn't one.
	 *
	 * @since 17.0.5
	 */
	static String numIdVal(PPrBase pPr) {
		if (pPr == null || pPr.getNumPr() == null
				|| pPr.getNumPr().getNumId() == null
				|| pPr.getNumPr().getNumId().getVal() == null) return null;
		return pPr.getNumPr().getNumId().getVal().toString();
	}

	protected static boolean createListBlock(WordprocessingMLPackage wmlPackage, RunFontSelector runFontSelector,
			String pStyleVal, PPr pPrDirect, PPr pPr, RPr rPr, RPr rPrParagraphMark, Document document,
			Element foBlockElement, Element foListBlock) {
		
		/* Create something like:
		 * 			
			<fo:list-block provisional-distance-between-starts="0.5in" start-indent="0.5in">
			  <fo:list-item>
			    <fo:list-item-label>
			      <fo:block font-family="Times New Roman">-</fo:block>
			    </fo:list-item-label>
			    <fo:list-item-body start-indent="body-start()">
			      <fo:block font-family="Times New Roman" font-size="9.0pt" line-height="100%" space-after="0.08in" space-before="0.08in" text-align="justify">
			        <inline xmlns="http://www.w3.org/1999/XSL/Format" id="clauseDPI5123341"/>Content goes here...
			      </fo:block>
			    </fo:list-item-body>
			  </fo:list-item>
			</fo:list-block>
		 */				
						
//				foListBlock.setAttribute("provisional-distance-between-starts", "0.5in");
		
		boolean indentHandledByNumbering = false;
		
		// Need to apply shading at fo:list-block level
		if (pPr.getShd()!=null) {
			PShading pShading = new PShading(pPr.getShd());
			pShading.setXslFO(foListBlock);
		}
		
		Element foListItem = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:list-item");
		foListBlock.appendChild(foListItem);				

		
		Element foListItemLabel = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:list-item-label");
		foListItem.appendChild(foListItemLabel);
		
		Element foListItemLabelBody = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block");
		foListItemLabel.appendChild(foListItemLabelBody);
		// the following applies to the label the same ppr props as are applied to the body
		// but it needs testing
		// createFoAttributes(wmlPackage, pPr, foListItemLabelBody, true, true );				
		
		
		Element foListItemBody = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:list-item-body");
		foListItem.appendChild(foListItemBody);	
		foListItemBody.setAttribute(Indent.FO_NAME, "body-start()");
		
		ResultTriple triple;
		// w:ilvl is optional (and so is its w:val); a paragraph numbered with just a
		// w:numId is at level 0.  Likewise w:numId/@w:val: without it there is no
		// numbering to apply.  @since 17.0.5
		String directNumId = numIdVal(pPrDirect);
		if (directNumId != null) {
			triple = org.docx4j.model.listnumbering.Emulator.getNumber(
					wmlPackage, pStyleVal,
				directNumId,
				ilvlVal(pPrDirect.getNumPr()) );
		} else {
			// Get the effective values; since we already know this,
			// save the effort of doing this again in Emulator
			String ilvlString = ilvlVal(pPr.getNumPr());
			triple = null;
			String numIdString = numIdVal(pPr);
			if (numIdString != null) {
				triple = org.docx4j.model.listnumbering.Emulator.getNumber(
						wmlPackage, pStyleVal,
		    			numIdString,
		    			ilvlString );
			}
		}
		
		if (triple==null) {
			log.warn("computed number ResultTriple was null");
			if (log.isDebugEnabled() ) {
				foListItemLabelBody.setAttribute("color", "red");
				foListItemLabelBody.setTextContent("null#");
			} 
		} else {

			/* Format the list item label
			 * 
			 * Since it turns out (in FOP at least) that the label and the body 
			 * don't have the same vertical alignment 
			 * unless font size is applied at the same level
			 * (ie to both -label and -body, or to the block inside each), 
			 * we have to format the list-item-body as well.
			 * This issue only manifests itself if the font size on
			 * the outer list-block is larger than the font sizes
			 * set inside it.
			 */
			
			// OK just to override specific values
			// Values come from numbering rPr, unless overridden in p-level rpr
			DocumentFragment rfsFrag = null;
			if(triple.getRPr()==null) {
				
				if (pPr.getRPr()==null) {
					// do nothing, since we're already inheriting the formatting in the style
					// (as opposed to the paragraph mark formatting)
					// EXCEPT for font
					rfsFrag = (DocumentFragment)runFontSelector.fontSelector(pPr, rPr, triple.getNumString());
					applyRunFontSelection(rfsFrag, foListItemLabelBody);
				} else {
					
					createFoAttributes(wmlPackage, rPrParagraphMark, foListItemLabel );	        				
					createFoAttributes(wmlPackage, rPrParagraphMark, foListItemBody );	
					
					rfsFrag = (DocumentFragment)runFontSelector.fontSelector(pPr, rPrParagraphMark, triple.getNumString());
					applyRunFontSelection(rfsFrag, foListItemLabelBody);
				}
				
			} else {
				RPr actual = XmlUtils.deepCopy(triple.getRPr()); // clone, so the ilvl rpr is not altered
//	        			System.out.println(XmlUtils.marshaltoString(rPrParagraphMark));
				
				// pMark overrides numbering, except for font
				// (which makes sense, since that would change the bullet)
				// so set the font
				rfsFrag = (DocumentFragment)runFontSelector.fontSelector(pPr, actual, triple.getNumString());
				applyRunFontSelection(rfsFrag, foListItemLabelBody);
				
				// .. before taking rPrParagraphMark into account
				StyleUtil.apply(rPrParagraphMark, actual); 
//	        			System.out.println(XmlUtils.marshaltoString(actual));
				
				createFoAttributes(wmlPackage, actual, foListItemLabel );
				createFoAttributes(wmlPackage, actual, foListItemBody );
				
			}
				        		
			
			int numChars=1;
			if (triple.getBullet()!=null ) {
//				foListItemLabelBody.setTextContent(triple.getBullet() );  
		    	foListItemLabelBody.setTextContent(rfsFrag.getTextContent());  // give effect to any character mapping performed by RFS
				
			} else if (triple.getNumString()==null) {
				log.debug("computed NumString was null!");
				if (log.isDebugEnabled() ) {
					foListItemLabelBody.setAttribute("color", "red");
					foListItemLabelBody.setTextContent("null#");
				} 
				numChars=0;
			} else {
				Text number = document.createTextNode( triple.getNumString() );
				foListItemLabelBody.appendChild(number);
				numChars = triple.getNumString().length();
			}
			
			// Indent (setting provisional-distance-between-starts)
			// Indent on direct pPr (trumps indent specified in
			// direct numbering??) which trumps indent in pPr in style, 
			// which trumps indent specified in a style's numbering.  
			// Well, not exactly, components which aren't set in
			// the direct formatting will be contributed by the numbering's indent settings
			Indent indent = new Indent(pPrDirect.getInd(), triple.getIndent());
			if (indent.isHanging() ) {
				indent.setXslFOListBlock(foListBlock, -1);	        			
			} else {
				
				int numWidth = 90 * numChars; // crude .. TODO take font size into account
				
			    int pdbs = getDistanceToNextTabStop(indent.getNumberPosition(), numWidth,
			    		pPrDirect.getTabs(), wmlPackage.getMainDocumentPart().getDocumentSettingsPart());
				indent.setXslFOListBlock(foListBlock, pdbs);	        				        			
			}
			indentHandledByNumbering = true; 
			
//	        		// Set the font
//	        		if (triple.getNumFont()!=null) {
//	        			String font = PhysicalFonts.getPhysicalFont(context.getWmlPackage(), triple.getNumFont() );
//	        			if (font!=null) {
//	        				foListItemLabelBody.setAttribute("font-family", font );
//	        			}
//	        		}
			
		}
		foListItemBody.appendChild(foBlockElement);
		return indentHandledByNumbering;
	}
    
    
    /**
     * Use RunFontSelector result to set the correct font for the list item label.
     */
    protected static void applyRunFontSelection(DocumentFragment frag, Element foListItemLabelBody) {
    	
    	if (log.isDebugEnabled()) {
    		log.debug(XmlUtils.w3CDomNodeToString(frag));
    	}
    	// eg <fo:inline xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Times New Roman">1)</fo:inline>
    	
    	// Now get the attribute value
    	if (frag!=null && frag.getFirstChild()!=null) {
    		Attr attr = ((Element)frag.getFirstChild()).getAttributeNode("font-family");
    		if (attr!=null) {
    			foListItemLabelBody.setAttribute("font-family", attr.getValue());
    		}
    		// and its line pitch and document font (WordLayoutFixups.listLabelLines
    		// sizes the item's first line from the label's metrics too, as Word does)
    		for (String name : new String[] { "line-height", org.docx4j.fonts.RunFontSelector.HINT_FONT }) {
    			Attr a = ((Element)frag.getFirstChild()).getAttributeNode(name);
    			if (a!=null) foListItemLabelBody.setAttribute(name, a.getValue());
    		}
    	}
			
    }
    
	/**
	 * The length of the leader standing in for a tab at the start of a paragraph
	 * (nothing but tabs before it), as Word lays it out: from the first line's
	 * start to the next tab stop.  Stops are the paragraph's w:tabs (cleared
	 * ones excepted), the implicit stop a hanging indent makes at the left
	 * indent, and the document's default interval (w:defaultTabStop, 720 twips
	 * if absent), all measured from the left margin.  Measured on the Getting
	 * Started guide (CR-001 §6.10): its code blocks begin with tabs, and Word's
	 * text starts at 0.5in per tab.  A tab after text is not handled here
	 * (its start is not known before layout).
	 *
	 * @param precedingTabs how many tabs already began the paragraph
	 * @param precedingText how many text runs precede it (any: not a leading tab)
	 * @return the leader length such as "36pt", or "" when it does not apply
	 * @since 17.0.5
	 */
	public static String leadingTabLeaderLength(FOConversionContext context, PPr effectivePPr,
			int precedingTabs, int precedingText) {
		if (precedingText > 0 || effectivePPr == null) return "";
		int pos = firstLineStartTwips(effectivePPr);
		DocumentSettingsPart settings = null;
		try {
			settings = context.getWmlPackage().getMainDocumentPart().getDocumentSettingsPart();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
		int from = pos;
		int stop = pos;
		for (int i = 0; i <= precedingTabs; i++) {
			from = stop;
			stop = nextTabStop(stop, effectivePPr, settings);
		}
		if (stop <= from) return "";
		return org.docx4j.fonts.WordLineMetrics.format((stop - from) / 20.0);
	}

	/** XSLT form of the above (the direct pPr, resolved here). @since 17.0.5 */
	public static String leadingTabLeaderLength(FOConversionContext context, NodeIterator pPrNodeIt,
			int precedingTabs, int precedingText) {
		if (precedingText > 0) return "";
		PPr pPr = null;
		try {
			Node n = pPrNodeIt == null ? null : pPrNodeIt.nextNode();
			if (n != null) pPr = (PPr) XmlUtils.unwrap(XmlUtils.unmarshal(n));
			pPr = context.getPropertyResolver().getEffectivePPr(pPr);
		} catch (Exception e) {
			log.warn("Couldn't resolve pPr for a leading tab: " + e.getMessage());
			return "";
		}
		return leadingTabLeaderLength(context, pPr, precedingTabs, precedingText);
	}

	/** where the first line's text starts, in twips from the left margin */
	static int firstLineStartTwips(PPr pPr) {
		PPrBase.Ind ind = pPr == null ? null : pPr.getInd();
		if (ind == null) return 0;
		int left = ind.getLeft() != null ? ind.getLeft().intValue()
				: ind.getStart() != null ? ind.getStart().intValue() : 0;
		if (ind.getHanging() != null) return left - ind.getHanging().intValue();
		if (ind.getFirstLine() != null) return left + ind.getFirstLine().intValue();
		return left;
	}

	/** the first tab stop after pos (twips), as Word finds it: a custom stop (or a
	 *  hanging indent's) clears the default stops before it; the default interval
	 *  resumes beyond the last of them */
	static int nextTabStop(int pos, PPr pPr, DocumentSettingsPart settings) {
		int best = Integer.MAX_VALUE;
		if (pPr != null && pPr.getTabs() != null) {
			for (CTTabStop t : pPr.getTabs().getTab()) {
				if (t.getPos() == null || STTabJc.CLEAR.equals(t.getVal())) continue;
				int p = t.getPos().intValue();
				if (p > pos && p < best) best = p;
			}
		}
		PPrBase.Ind ind = pPr == null ? null : pPr.getInd();
		if (ind != null && ind.getHanging() != null) {
			int left = ind.getLeft() != null ? ind.getLeft().intValue()
					: ind.getStart() != null ? ind.getStart().intValue() : 0;
			if (left > pos && left < best) best = left;
		}
		if (best < Integer.MAX_VALUE) return best;
		int defaultTab = 720;
		try {
			if (settings != null && settings.getJaxbElement() != null
					&& settings.getJaxbElement().getDefaultTabStop() != null
					&& settings.getJaxbElement().getDefaultTabStop().getVal() != null) {
				int v = settings.getJaxbElement().getDefaultTabStop().getVal().intValue();
				if (v > 0) defaultTab = v;
			}
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
		return pos < 0 ? 0 : (pos / defaultTab + 1) * defaultTab;
	}

    protected static int getDistanceToNextTabStop( int pos, int numWidth, Tabs pprTabs, DocumentSettingsPart settings) {

		int pdbs = 0; 
		int defaultTab = 360;
		if (pprTabs!=null
				&& pprTabs.getTab()!=null
				&& pprTabs.getTab().size()>0) {
			
			for ( CTTabStop tabStop : pprTabs.getTab() ) {
					if (tabStop.getPos().intValue()> (pos+ numWidth) ) {
						log.debug("tab stop: using specified");
						return (tabStop.getPos().intValue() - pos);
					}
			}
			
		} 
		
		// The default tabs continue to apply after the specified ones
		if (settings!=null
				&& settings.getJaxbElement().getDefaultTabStop()!=null ) {
			CTTwipsMeasure twips = settings.getJaxbElement().getDefaultTabStop();
			defaultTab = twips.getVal().intValue();
			
			if (defaultTab>0) {
				log.debug("tab stop: using default from docx");
				int tabNUmber = (int)Math.floor((pos+numWidth)/defaultTab);
				int nextTabPos = defaultTab*(tabNUmber+1);
				return nextTabPos - pos;
			}
		}

		log.debug("tab stop: assuming default tab 360");
		int tabNUmber = (int)Math.floor((pos+numWidth)/defaultTab);
		int nextTabPos = defaultTab*(tabNUmber+1);
		return nextTabPos - pos;
    }

	private static void createFoAttributes(OpcPackage opcPackage, PPr pPr, Element foBlockElement, boolean inList, boolean ignoreBorders){
		
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
    	
    	if (pPr==null) return;
		
    	// Special case, since bidi is translated to align right
    	// Handle interaction between w:pPr/w:bidi and w:pPr/w:jc/@w:val='right'
    	if (pPr.getBidi()!=null && pPr.getBidi().isVal()) {
    		
    		if (pPr.getJc()!=null) {
    			if (pPr.getJc().getVal().equals(JcEnumeration.RIGHT)) {
    				// set it to left!
    				foBlockElement.setAttribute(Justification.FO_NAME,  "left");
    			} else if (pPr.getJc().getVal().equals(JcEnumeration.LEFT)) {
    				// set it to right!
    				foBlockElement.setAttribute(Justification.FO_NAME,  "right");
    			}
    		}
    	}
    	
    	// Table of contents dot leader needs text-align-last="justify"
    	// Are we in a TOC?
    	if (pPr.getTabs()!=null
    			
    			// PStyle is not included in our effective pPr!
//    			&& pPr.getPStyle()!=null 
//    			&& pPr.getPStyle().getVal()!=null
//    			&& pPr.getPStyle().getVal().startsWith("TOC")  
    			) {
    		
    		CTTabStop tabStop = pPr.getTabs().getTab().get(0);
    		if (tabStop!=null
    				//&& tabStop.getLeader().equals(STTabTlc.DOT)
    				&& tabStop.getVal().equals(STTabJc.RIGHT) ) {
    			
    			foBlockElement.setAttribute("text-align-last",  "justify");
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
	
    private static void createFoAttributes(TrPr trPr, Element foBlockElement){
    	if (trPr == null) {
    		return;
    	}
    	applyFoAttributes(PropertyFactory.createProperties(trPr), foBlockElement);
    }
	
    private static void createFoAttributes(TcPr tcPr, Element foBlockElement){
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
    		FOConversionContext context,
    		NodeIterator pPrNodeIt,
    		NodeIterator rPrNodeIt,
    		NodeIterator childResults ) {

        try {
        	PropertyResolver propertyResolver = context.getPropertyResolver();
    	
    	// Note that this is invoked for every paragraph with a pPr node.
    	
    	// incoming objects are org.apache.xml.dtm.ref.DTMNodeIterator 
    	// which implements org.w3c.dom.traversal.NodeIterator

    	
//    	log.info("rPrNode:" + rPrNodeIt.getClass().getName() ); // org.apache.xml.dtm.ref.DTMNodeIterator    	
//    	log.info("childResults:" + childResults.getClass().getName() ); 
    	
    	
        	
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
        		    	log.error("Couldn't cast to PPr " + jaxb.getClass().getName() + " to PPr!");
        			}        	        			
        		}
        	}
        	
			Object jaxbR = u.unmarshal(rPrNodeIt.nextNode());			
			//RPr rPrDirect = null;
			RPr rPr = null;
			if (jaxbR instanceof RPr) {
				//rPrDirect =  (RPr)jaxbR;
				rPr = propertyResolver.getEffectiveRPr((RPr)jaxbR, pPrDirect);
			} else if (jaxbR instanceof ParaRPr) {
//				if (log.isDebugEnabled()) {
//					Throwable t = new Throwable();
//					log.debug("passed ParaRPr", t);
//				}
				
				rPr = propertyResolver.getEffectiveRPr(null, pPrDirect); 
//    			System.out.println("p rpr-->" + XmlUtils.marshaltoString(pPrDirect.getRPr()));
        		
        		StyleUtil.apply((ParaRPr)jaxbR, rPr); 				
				
			} else {
				log.error("TODO handle  .." + jaxbR.getClass().getName());
			}

			// Word prints nothing for hidden text, and leaves no space for it.  The
			// visitor pathway does this in AbstractVisitorExporterGenerator.  @since 17.0.5
			if (!HiddenText.isPrinted() && HiddenText.isHidden(rPr)) {
				return null;
			}

            // Create a DOM builder and parse the fragment
			Document document = XmlUtils.getNewDocumentBuilder().newDocument();
			
			//log.info("Document: " + document.getClass().getName() );

			Node foInlineElement = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:inline");			
			document.appendChild(foInlineElement);
			
				
//			if (log.isDebugEnabled() && rPr!=null) {					
//				log.debug(XmlUtils.marshaltoString(rPr, true, true));					
//			}
			
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
			log.error(e.getMessage(), e);
		} 
    	
    	return null;
    	
    }

	/** Apply the FO attributes for these run properties.  Public since 17.0.4, so
	 *  the visitor pathway shares it (previously FOExporterVisitorGenerator had its
	 *  own copy). */
	public static void createFoAttributes(OpcPackage opcPackage,
			RPr rPr, Element foInlineElement){

    	List<Property> properties = PropertyFactory.createProperties(opcPackage, rPr);

    	for( Property p :  properties ) {
    		p.setXslFO(foInlineElement);
    	}

	}
	
    public static String getPageNumberFormat(FOConversionContext context) {
    	String pageFormat = 
    			context.getSections().getCurrentSection().getPageNumberInformation().getPageFormat();
    	//may return empty string if no page number format supplied
    	pageFormat = FormattingSwitchHelper.getFoPageNumberFormat(pageFormat);
    	return (pageFormat == null ? "" : pageFormat);
    }
	
    public static String getPageNumberInitial(FOConversionContext context) {
    	int ret = 
    			context.getSections().getCurrentSection().getPageNumberInformation().getPageStart();
    	//may return empty string if no start page number supplied
    	return (ret == -1 ? "" : Integer.toString(ret));
    }
    
    /**
     * FOP inserts a blank page if necessary so that a section with page numbering
     * from 1 would be face up when printed double sided. Word doesn't do that
     * (unless you have an odd section type), so this function mimics Word's 
     * behaviour. 
     * 
     * @param context
     * @return
     * @since 3.2.2
     */
    public static String getForcePageCount(FOConversionContext context) {
    	
    	// see http://www.w3.org/TR/xsl/#force-page-count
    	
    	ConversionSectionWrapper wrapper = context.getSections().peekNextSection();
    	
    	if (wrapper==null) {
    		// final section
    		return "no-force";
    	} else {
    		SectPr.Type secType = wrapper.getSectPr().getType();
    		
    		CTPageNumber pgNumType = wrapper.getSectPr().getPgNumType();
    		Boolean isExplicitOdd = null; // null means numbering will continue from the highest page number in the previous section
    		if (pgNumType!=null && pgNumType.getStart()!=null) {
    			int start = pgNumType.getStart().intValue();
    			if ( start % 2 == 0) {
    				isExplicitOdd = Boolean.FALSE;    				
    			} else {
    				isExplicitOdd = Boolean.TRUE;    				    				
    			}
    		}
    		
    		if (secType==null || secType.getVal().equals("nextPage") ) {
        		return "no-force";  
    		} else if (isExplicitOdd==null  // LIMITATION: We don't get this right after the user has set the page number explicitly in a previous section
    						|| isExplicitOdd) {
    			// The normal case
    			if ( secType.getVal().equals("evenPage") ) {
	    			// Even page section breaks, which begin the new section on the next even-numbered page.
	    			// (What happens if that section has w:pgNumType/@w:start="1"?)
	    			return "end-on-odd";
	    		} else if ( secType.getVal().equals("oddPage") ) {
	    			// Odd page section breaks, which begin the new section on the next odd-numbered page
	    			return "end-on-even";
	    		} else {
	    			// continuous (!)
	        		return "no-force";    			    			
	    		}
    		} else {
    			// section starts with p2 or p4
    			if ( secType.getVal().equals("evenPage") ) {
	    			// Even page section breaks, which begin the new section on the next even-numbered page.
	    			// (What happens if that section has w:pgNumType/@w:start="1"?)
	    			return "end-on-even";
	    		} else if ( secType.getVal().equals("oddPage") ) {
	    			// Odd page section breaks, which begin the new section on the next odd-numbered page
	    			return "end-on-odd";
	    		} else {
	    			// continuous (!)
	        		return "no-force";    			    			
	    		}
    			
    		}
    	}

    }
    
    private static boolean isOdd(SectPr sectPr) {
    	
    	CTPageNumber pgNumType = sectPr.getPgNumType();
    	
    	return true;
    }

    public static boolean hasPgNumTypeStart(FOConversionContext context) {
    	return (context.getSections().getCurrentSection().getPageNumberInformation().getPageStart() > -1);
    }
}
