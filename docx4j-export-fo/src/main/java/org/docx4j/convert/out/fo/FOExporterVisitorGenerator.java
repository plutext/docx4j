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

import jakarta.xml.bind.JAXBElement;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.AbstractVisitorExporterDelegate;
import org.docx4j.convert.out.common.AbstractVisitorExporterDelegate.AbstractVisitorExporterGeneratorFactory;
import org.docx4j.convert.out.common.AbstractVisitorExporterGenerator;
import org.docx4j.convert.out.common.XsltCommonFunctions;
import org.docx4j.convert.out.common.writer.AbstractBrWriter;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.images.WordXmlPictureE10;
import org.docx4j.model.images.WordXmlPictureE20;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTFtnEdn;
import org.docx4j.wml.CTFtnEdnRef;
import org.docx4j.wml.CTTabStop;
import org.docx4j.wml.DelText;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.RunIns;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.STPTabAlignment;
import org.docx4j.wml.STTabJc;
import org.docx4j.wml.STTabTlc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class FOExporterVisitorGenerator extends AbstractVisitorExporterGenerator<FOConversionContext>{
	
	private static Logger log = LoggerFactory.getLogger(FOExporterVisitorGenerator.class);
	
	
	private static String XSL_FO = "http://www.w3.org/1999/XSL/Format";

	public static final AbstractVisitorExporterDelegate.AbstractVisitorExporterGeneratorFactory<FOConversionContext> GENERATOR_FACTORY = 
			new AbstractVisitorExporterGeneratorFactory<FOConversionContext>() {
				@Override
				public AbstractVisitorExporterGenerator<FOConversionContext> createInstance(
						FOConversionContext conversionContext,
						Document document, Node parentNode) {
					return new FOExporterVisitorGenerator(conversionContext, document, parentNode);
				}
			};
			
	
	private FOExporterVisitorGenerator(FOConversionContext conversionContext, Document document, Node parentNode) {
		super(conversionContext, document, parentNode);
	}

	@Override
	protected AbstractVisitorExporterGeneratorFactory<FOConversionContext> getFactory() {
		return GENERATOR_FACTORY;
	}

	@Override
	public List<Object> apply(Object o) {

		// FO-specific handling of elements the shared visitor has no case for;
		// the output matches the corresponding docx2fo.xslt templates.

		if (o instanceof P) {

			// Since 17.0.4, the paragraph block is built by the same code as the
			// XSLT pathway (children first, then wrap), rather than by handlePPr
			handleP((P)o);
			return null;

		} else if (o instanceof DelText) {

			if (!conversionContext.isInComplexFieldDefinition()) {
				Element inline = createNode(document, NODE_INLINE);
				inline.setAttribute("color", "red");
				inline.setAttribute("text-decoration", "line-through");
				inline.setTextContent(((DelText)o).getValue());
				getCurrentParent().appendChild(inline);
			}
			return null;

		} else if (o instanceof RunIns || o instanceof RunDel) {

			// RunIns gets its styling wrapper in walkJAXBElements (its runs are
			// appended while walking); RunDel content is just traversed, with the
			// visible marking on the w:delText above.
			return null;

		} else if (o instanceof R.SoftHyphen) {

			if (!conversionContext.isInComplexFieldDefinition()) {
				getCurrentParent().appendChild(document.createTextNode("\u00AD"));
			}
			return null;

		} else if (o instanceof R.NoBreakHyphen) {

			// There is no glyph for NON-BREAKING HYPHEN U+2011 in many fonts, so use
			// an ordinary hyphen with a zero-width no-break space
			if (!conversionContext.isInComplexFieldDefinition()) {
				getCurrentParent().appendChild(document.createTextNode("-\uFEFF"));
			}
			return null;

		} else if (o instanceof R.Cr) {

			if (!conversionContext.isInComplexFieldDefinition()) {
				// an empty block suffices for the line break (matching the XSLT,
				// whose template's literal space is stripped as stylesheet whitespace)
				Element block = createNode(document, NODE_BLOCK);
				block.setAttribute("white-space-treatment", "preserve");
				getCurrentParent().appendChild(block);
			}
			return null;

		} else if (o instanceof CTFtnEdnRef) {

			// w:footnoteReference and w:endnoteReference share this class; look at
			// the JAXBElement wrapper in the containing run to tell them apart
			if (!conversionContext.isInComplexFieldDefinition()) {
				if ("endnoteReference".equals(refKind((CTFtnEdnRef)o))) {
					handleEndnoteReference();
				} else {
					handleFootnoteReference((CTFtnEdnRef)o);
				}
			}
			return null;

		} else if (o instanceof R.EndnoteRef) {

			// the number in the endnote itself; endnoteNumber is set by
			// FOExporterVisitorDelegate.appendSectionFooter when rendering endnotes
			if (endnoteNumber>=0 && !conversionContext.isInComplexFieldDefinition()) {
				appendNoteNumber(endnoteNumber);
			}
			return null;

		} else if (o instanceof R.FootnoteRef) {

			// the number in the footnote itself, styled by its own run (Word's
			// FootnoteReference style makes it a superscript); footnoteNumber is
			// set by handleFootnoteReference on the generator for the note's content
			if (footnoteNumber>=0 && !conversionContext.isInComplexFieldDefinition()) {
				DocumentFragment styled = XsltCommonFunctions.fontSelectorForGeneratedText(
						conversionContext, pPr, rPr, Integer.toString(footnoteNumber));
				XmlUtils.treeCopy(styled, getCurrentParent());
			}
			return null;

		} else if (o instanceof R.Separator
				|| o instanceof R.ContinuationSeparator) {

			// separators are generated (xsl-footnote-separator), not copied
			return null;

		} else if (o instanceof SdtElement) {

			// A borders/shading container inserted by the Containerization preprocess
			// (tag XSLT_PBdr/XSLT_Shd/XSLT_RPr) is rendered as a block/inline carrying
			// the borders/shading; any other sdt is traversed transparently, as in the
			// XSLT's w:sdt template
			SdtElement sdt = (SdtElement)o;
			if (isSpanAllContainer(sdt)) {
				// a part of a merged page-sequence with fewer columns than the
				// sequence (ConversionSectionWrapperFactory): a block spanning them all
				Element block = document.createElementNS(XSL_FO, "block");
				block.setAttribute("span", "all");
				getCurrentParent().appendChild(block);
				FOExporterVisitorGenerator generator = childGenerator(block);
				if (sdt.getSdtContent()!=null) {
					new TraversalUtil(sdt.getSdtContent().getContent(), generator);
				}
				return null;
			}
			String tag = containerTag(sdt);
			if (tag!=null) {
				handleXsltContainer(sdt, tag);
			}
			return null;

		} else if (o instanceof R.Ptab) {

			if (conversionContext.isInComplexFieldDefinition()) {
				return null;
			}
			if (STPTabAlignment.RIGHT.equals(((R.Ptab)o).getAlignment())) {
				Element leader = document.createElementNS(XSL_FO, "leader");
				leader.setAttribute("leader-length.minimum", "12pt");
				leader.setAttribute("leader-length.maximum", "100%");
				// optimum is the length FOP uses when it measures the line, so 100% (the
				// whole reference area) made every such line over-full and broke it at the
				// nearest opportunity - in the middle of the text either side of the tab.
				// The leader stretches to the maximum at justification time, which is what
				// text-align-last="justify" (set by createBlockForPPr's leader check) asks
				// for.  @since 17.0.5
				leader.setAttribute("leader-length.optimum", "12pt");
				leader.setAttribute("leader-pattern", "space");
				leader.setAttribute("leader-alignment", "reference-area");
				getCurrentParent().appendChild(leader);
				// text-align-last=justify (which the leader needs in FOP) is set on
				// the paragraph's block by createBlockForPPr's leader check
				return null;
			}
			return super.apply(o); // other alignments: warn, as the XSLT does

		} else if (o instanceof org.docx4j.math.CTOMathPara
				|| o instanceof org.docx4j.math.CTOMath) {

			// equation -> MathML in fo:instream-foreign-object (rendered by the
			// jeuclid-fop plugin), or the equation's text if no renderer is present
			org.w3c.dom.DocumentFragment frag = XsltFOFunctions.mathToFO(conversionContext, o);
			if (frag != null) {
				getCurrentParent().appendChild(document.importNode(frag, true));
			}
			return null;

		}

		return super.apply(o);
	}

	/** the number of the endnote whose content is being rendered (for w:endnoteRef);
	 *  set by FOExporterVisitorDelegate when appending the Endnotes block */
	protected int endnoteNumber = -1;

	/** the number of the footnote whose content is being rendered (for w:footnoteRef);
	 *  set by handleFootnoteReference.  @since 17.0.5 */
	protected int footnoteNumber = -1;

	/** the local name of the JAXBElement wrapping this reference in its run */
	private String refKind(CTFtnEdnRef ref) {
		return jaxbElementName(ref);
	}

	/**
	 * fo:footnote at the reference position: the number, styled by the
	 * reference's run (Word's FootnoteReference style makes it a superscript),
	 * and the note's paragraphs as the footnote body, as Word lays them out:
	 * no hanging indent, the number (w:footnoteRef) inline in the first
	 * paragraph.  Mirrors the XSLT's w:footnoteReference template.
	 */
	private void handleFootnoteReference(CTFtnEdnRef ref) {

		int fn = conversionContext.getNextFootnoteNumber();
		DocumentFragment fnStyled = XsltCommonFunctions.fontSelectorForGeneratedText(
				conversionContext, pPr, rPr, Integer.toString(fn));

		Element footnote = document.createElementNS(XSL_FO, "footnote");

		Element marker = document.createElementNS(XSL_FO, "inline");
		XmlUtils.treeCopy(fnStyled, marker);
		footnote.appendChild(marker);

		Element body = document.createElementNS(XSL_FO, "footnote-body");
		footnote.appendChild(body);

		// the footnote's content, found by w:id as the XSLT's getFootnote does
		try {
			CTFtnEdn ftn = XsltCommonFunctions.findNote(
					conversionContext.getWmlPackage().getMainDocumentPart()
					.getFootnotesPart().getJaxbElement().getFootnote(),
					ref.getId().toString());
			FOExporterVisitorGenerator generator = childGenerator(body);
			generator.footnoteNumber = fn;
			new TraversalUtil(ftn.getContent(), generator);
		} catch (Exception e) {
			log.error("Couldn't get footnote " + (ref.getId()==null ? "(no id)" : ref.getId())
					+ ": " + e.getMessage(), e);
		}

		getCurrentParent().appendChild(footnote);
	}

	/** superscript endnote number at the reference position */
	private void handleEndnoteReference() {

		appendNoteNumber(conversionContext.getNextEndnoteNumber());
	}

	private void appendNoteNumber(int number) {

		// styled by the run alone: Word raises it only if the run (usually via the
		// EndnoteReference style) is a superscript
		DocumentFragment styled = XsltCommonFunctions.fontSelectorForGeneratedText(
				conversionContext, pPr, rPr, Integer.toString(number));
		XmlUtils.treeCopy(styled, getCurrentParent());
	}

	private static boolean isSpanAllContainer(SdtElement sdt) {
		return sdt.getSdtPr()!=null && sdt.getSdtPr().getTag()!=null && sdt.getSdtPr().getTag().getVal()!=null
				&& sdt.getSdtPr().getTag().getVal().startsWith(
						org.docx4j.convert.out.common.wrappers.ConversionSectionWrapperFactory.TAG_SPAN_ALL);
	}

	@Override
	public boolean shouldTraverse(Object o) {

		if (o instanceof P) {
			// its contents were already converted in apply (handleP)
			return false;
		}
		if (o instanceof SdtElement && (isSpanAllContainer((SdtElement)o) || containerTag((SdtElement)o)!=null)) {
			// its contents were already converted in apply (handleXsltContainer)
			return false;
		}
		if (o instanceof org.docx4j.math.CTOMath
				|| o instanceof org.docx4j.math.CTOMathPara) {
			// handled whole in apply(); don't descend into the OMML children
			return false;
		}
		return super.shouldTraverse(o);
	}

	/**
	 * Convert the paragraph's children into a fragment first (cf the XSLT's
	 * childResults), then wrap them in the paragraph's fo:block (or fo:list-block)
	 * via the shared XsltFOFunctions.createBlockForPPr — the same code the XSLT
	 * pathway uses, so list structure, empty-paragraph preservation, the
	 * paragraph-mark sz/lang line-height contribution, hyphenation, leader handling
	 * and the bidi block-container all behave identically.
	 *
	 * @since 17.0.4
	 */
	/** A generator for nested content, carrying the note numbers w:footnoteRef and
	 *  w:endnoteRef render (a note's paragraphs are converted by child generators). */
	private FOExporterVisitorGenerator childGenerator(Node parent) {
		FOExporterVisitorGenerator generator = (FOExporterVisitorGenerator)
				getFactory().createInstance(conversionContext, document, parent);
		generator.footnoteNumber = footnoteNumber;
		generator.endnoteNumber = endnoteNumber;
		return generator;
	}

	private void handleP(P p) {

		DocumentFragment childResults = document.createDocumentFragment();
		FOExporterVisitorGenerator generator = childGenerator(childResults);
		try {
			// the effective pPr, for font selection within the paragraph (as before)
			generator.pPr = conversionContext.getPropertyResolver().getEffectivePPr(p.getPPr());
			// the resolved pPr has no w:pStyle; RunFontSelector needs it to apply the
			// paragraph style's run properties (fonts, size, w:kern) to the runs, as
			// the XSLT pathway does with the raw pPr (found by KernedRunsTest, 17.0.5)
			if (generator.pPr != null && generator.pPr.getPStyle() == null
					&& p.getPPr() != null && p.getPPr().getPStyle() != null) {
				generator.pPr = XmlUtils.deepCopy(generator.pPr);
				generator.pPr.setPStyle(p.getPPr().getPStyle());
			}
		} catch (Docx4JException e) {
			log.error(e.getMessage(), e);
		}
		new TraversalUtil(p.getContent(), generator);

		String pStyleVal = (p.getPPr()!=null && p.getPPr().getPStyle()!=null
				? p.getPPr().getPStyle().getVal() : null);
		DocumentFragment block = XsltFOFunctions.createBlockForPPr(
				conversionContext, p.getPPr(), pStyleVal, childResults);
		if (block!=null) {
			(tc.peek()!=null ? tc.peek() : parentNode)
					.appendChild(document.importNode(block, true));
		}
		currentP = null;
		currentSpan = null;
	}

	/**
	 * The Containerization tag if this sdt is a borders/shading container in a shape
	 * we can render (cf the cases of the XSLT's w:sdt template); null otherwise.
	 */
	private String containerTag(SdtElement sdt) {

		if (sdt.getSdtPr()==null
				|| sdt.getSdtPr().getTag()==null
				|| sdt.getSdtPr().getTag().getVal()==null) return null;
		String tag = sdt.getSdtPr().getTag().getVal();
		if (!tag.contains("XSLT_")) return null;
		if (firstPPPr(sdt)!=null || sdtPrRPr(sdt)!=null) return tag;
		return null;
	}

	/** the pPr of the container's first paragraph (looking through a nested
	 *  container, as a borders container may directly hold a shading one) */
	private PPr firstPPPr(SdtElement sdt) {

		if (sdt.getSdtContent()==null) return null;
		PPr pPr = firstPPPr(sdt.getSdtContent().getContent());
		if (pPr!=null) return pPr;
		for (Object o : sdt.getSdtContent().getContent()) {
			o = XmlUtils.unwrap(o);
			if (o instanceof SdtElement) {
				SdtElement inner = (SdtElement)o;
				return (inner.getSdtContent()==null ? null
						: firstPPPr(inner.getSdtContent().getContent()));
			}
		}
		return null;
	}

	private PPr firstPPPr(List<Object> content) {

		for (Object o : content) {
			o = XmlUtils.unwrap(o);
			if (o instanceof P) return ((P)o).getPPr();
		}
		return null;
	}

	private RPr sdtPrRPr(SdtElement sdt) {

		if (sdt.getSdtPr()==null) return null;
		for (Object o : sdt.getSdtPr().getRPrOrAliasOrLock()) {
			o = XmlUtils.unwrap(o);
			if (o instanceof RPr) return (RPr)o;
		}
		return null;
	}

	private void handleXsltContainer(SdtElement sdt, String tag) {

		// convert the contents first (cf the XSLT's childResults)
		DocumentFragment childResults = document.createDocumentFragment();
		if (sdt.getSdtContent()!=null) {
			FOExporterVisitorGenerator generator = childGenerator(childResults);
			generator.pPr = pPr; // a run-level container keeps its paragraph context
			new TraversalUtil(sdt.getSdtContent().getContent(), generator);
		}

		PPr containerPPr = firstPPPr(sdt);
		if (containerPPr!=null) {
			// pStyleVal: the XSLT evaluates w:pPr/w:pStyle relative to the w:sdt,
			// which selects nothing, so pass null (default paragraph style)
			DocumentFragment result = XsltFOFunctions.createBlockForSdt(
					conversionContext, containerPPr, null, childResults, tag);
			if (result!=null) {
				(tc.peek()!=null ? tc.peek() : parentNode)
						.appendChild(document.importNode(result, true));
			}
		} else {
			DocumentFragment result = XsltFOFunctions.createInlineForSdt(
					conversionContext, sdtPrRPr(sdt), childResults);
			if (result!=null) {
				(currentP!=null ? currentP : parentNode)
						.appendChild(document.importNode(result, true));
			}
		}
	}

	@Override
	public void walkJAXBElements(Object o) {

		if (o instanceof RunIns
				&& !conversionContext.isInComplexFieldDefinition()) {

			Element wrapper = createNode(document, NODE_INLINE);
			wrapper.setAttribute("color", "blue");
			wrapper.setAttribute("text-decoration", "underline");
			(currentP != null ? currentP : parentNode).appendChild(wrapper);

			// the w:ins contains runs; have them append their spans to the wrapper
			Element savedP = currentP;
			Element savedSpan = currentSpan;
			currentP = wrapper;
			try {
				super.walkJAXBElements(o);
			} finally {
				currentP = savedP;
				currentSpan = savedSpan;
			}
			return;
		}

		super.walkJAXBElements(o);
	}

	@Override
	protected DocumentFragment createImage(int imgType, FOConversionContext conversionContext, Object anchorOrInline) {
			switch (imgType) {
			case IMAGE_E10:
				return WordXmlPictureE10.createXslFoImgE10(conversionContext, anchorOrInline);
			case IMAGE_E20:
				return WordXmlPictureE20.createXslFoImgE20(conversionContext, anchorOrInline);
			}
		return null;
	}
    
	@Override
	protected Element createNode(Document doc, int nodeType) {
		switch (nodeType) {
			case NODE_BLOCK:
				return document.createElementNS(XSL_FO, "block");
			case NODE_INLINE:
				return document.createElementNS(XSL_FO, "inline");
		}
		return null;
	}
	
	@Override
	protected void handleBr(Br br) {
		
		
		/* Is there a w:br immediately before this one?
		
	      If this is the first child of this w:r, and the w:r is preceded by another w:r, look at its last child
	
		  If this is not the first child of this w:r, look at the preceding sibling
*/
		
		boolean firstBr = true; // until proven otherwise
		
		R r = (R)br.getParent();
		int pos = getPos(r.getContent(), br);
		if (pos<0) {
			log.error("Couldn't locate w:br in w:r");
		}
		else if (pos==0) {
			// Need to look in preceding run
			Object rParent = r.getParent();
			// Handle just the case where this is w:p for now
			if(rParent instanceof P) {
				P parentP = (P)rParent;
				pos = getPos(parentP.getContent(), r);
				if (pos<0) {
					log.error("Couldn't locate w:r in w:p");
				} else if (pos>0) {
					Object beforeR = parentP.getContent().get(pos-1);
					if (beforeR instanceof R) {
						List list = ((R)beforeR).getContent();
						Object previous = list.get(list.size()-1);
						if (previous instanceof Br) {
							firstBr=false;
						}
					} else {
//						System.out.println(beforeR.getClass().getName());
						
					}
				}
			} else {
				log.info("TODO: handle run parent " + rParent.getClass().getName());
			}
		} else {
			Object previous = r.getContent().get(pos-1);
			if (previous instanceof Br) {
				firstBr=false;
			} else {
//				System.out.println("previous: " + previous.getClass().getName());
			}
		}
		
		if ((!firstBr) && 
				(br.getType()==null
				  || br.getType().equals(STBrType.TEXT_WRAPPING))) {
			
			// ie  a soft-return following another
			// 
			Element ret = createNode(document, NODE_BLOCK);
			// see http://stackoverflow.com/a/3664468/1031689 answer
			// at http://stackoverflow.com/questions/3661483/inserting-a-line-break-in-a-pdf-generated-from-xsl-fo-using-xslvalue-of
			ret.setAttribute("linefeed-treatment", "preserve");
			ret.setAttribute("white-space-treatment", "preserve");
			ret.setTextContent("\n");
			
			getCurrentParent().appendChild(ret); // should be spanEl
			
		} else {
			// Usual case
			convertToNode(conversionContext, 
					  br, AbstractBrWriter.WRITER_ID,
					  document, getCurrentParent() );
			
		}

		if ((br.getType()==null || br.getType().equals(STBrType.TEXT_WRAPPING))
				&& XsltCommonFunctions.isTrailingBreak(br)) {
			// Word gives a break at the end of a paragraph an empty line of its own
			// (measured, CR-001 §6.10); a no-break space in the run's font makes one
			DocumentFragment line = XsltCommonFunctions.fontSelectorForGeneratedText(conversionContext, pPr, rPr, "\u00A0");
			if (line != null) getCurrentParent().appendChild(document.importNode(line, true));
		}
		
		if ((br.getType()!=null
				  && br.getType().equals(STBrType.PAGE))) {
			currentSpan=null;			
		}
	}
	
	
	@Override
	protected void convertTabToNode(FOConversionContext conversionContext, Document document, org.docx4j.wml.R.Tab tab) throws DOMException {
		leadingTabOrdinal = XsltCommonFunctions.leadingTabOrdinal(tab);
		try {
			convertTabToNode(conversionContext, document);
		} finally {
			leadingTabOrdinal = -1;
		}
	}

	/** the tab being converted: how many tabs precede it at its paragraph's start, or -1 */
	private int leadingTabOrdinal = -1;

	@Override
	protected void convertTabToNode(FOConversionContext conversionContext, Document document) throws DOMException {

		if (!conversionContext.isInComplexFieldDefinition()) {

			// The leader dots, and the spaces we use where there is no leader, are
			// characters we generate; there is no w:t to hang a font off, so unless we
			// set one, they'd be rendered/measured in the renderer's default font.
			String fontFamily = XsltFOFunctions.getFontFamily(conversionContext, pPr, rPr);

	    	if (pPr!=null && pPr.getTabs()!=null) {

	    		// xsl:when test="count($p/w:pPr/w:tabs/w:tab[1][@w:leader='dot' and @w:val='right'])=1"
	    		CTTabStop tabStop = pPr.getTabs().getTab().get(0);

	    		if (tabStop!=null
	    				&& tabStop.getVal()!=null     // unlikely
	    				&& tabStop.getVal().equals(STTabJc.RIGHT)
	    				&& tabStop.getLeader()!=null  // more likely
	    				&& tabStop.getLeader().equals(STTabTlc.DOT) ) {

					// <fo:leader leader-length.minimum="12pt" leader-length.optimum="40pt"
					//		    leader-length.maximum="100%" leader-pattern="dots">
	    			Element foLeader = document.createElementNS(XSL_FO, "leader");
	    			foLeader.setAttribute("leader-length.minimum",  "12pt");
	    			foLeader.setAttribute("leader-length.maximum",  "100%");
	    			foLeader.setAttribute("leader-length.optimum",  "40pt");
	    			foLeader.setAttribute("leader-pattern",  "dots");
	    			if (fontFamily.length()>0) {
	    				foLeader.setAttribute("font-family", fontFamily);
	    			}

	    			getCurrentParent().appendChild(foLeader);

	    		} else if (!appendLeadingTab(conversionContext, fontFamily)) {
	    			appendTabDummy(fontFamily);
	    		}
	    	}
	    	else if (!appendLeadingTab(conversionContext, fontFamily)) {
	    		appendTabDummy(fontFamily);
    		}

		}
	}

	/** A tab before any text in the paragraph: a leader to Word's next tab stop
	 *  (XsltFOFunctions.leadingTabLeaderLength).  @return whether one was added */
	private boolean appendLeadingTab(FOConversionContext conversionContext, String fontFamily) {
		if (leadingTabOrdinal < 0) return false;
		String length = XsltFOFunctions.leadingTabLeaderLength(conversionContext, pPr, leadingTabOrdinal, 0);
		if (length.length() == 0) return false;
		Element leader = document.createElementNS(XSL_FO, "leader");
		leader.setAttribute("leader-pattern", "space");
		leader.setAttribute("leader-length", length);
		if (fontFamily.length() > 0) leader.setAttribute("font-family", fontFamily);
		getCurrentParent().appendChild(leader);
		return true;
	}


	/** the spaces standing in for a tab, in an inline carrying the font (as the
	 *  XSLT's w:tab template does) */
	private void appendTabDummy(String fontFamily) {

		Element inline = createNode(document, NODE_INLINE);
		if (fontFamily.length()>0) {
			inline.setAttribute("font-family", fontFamily);
		}
		inline.setTextContent(TAB_DUMMY);
		getCurrentParent().appendChild(inline);
	}
	
	
    /**
     * Not used by this generator since 17.0.4: the paragraph block is built via
     * XsltFOFunctions.createBlockForPPr (see handleP), the same code as the XSLT
     * pathway, and sdt containers via createBlockForSdt (see handleXsltContainer).
     */
    @Override
	protected Element handlePPr(FOConversionContext conversionContext, PPr pPrDirect, boolean sdt,
			Element currentParent) throws Docx4JException {

        return currentParent;
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
     * @throws Docx4JException 
     */
    @Override
	protected void handleRPr(
    		FOConversionContext conversionContext,
    		PPr pPrDirect,
    		RPr rPrDirect, Element currentParent ) throws Docx4JException {

    	PropertyResolver propertyResolver = conversionContext.getPropertyResolver();
    	
    	
        try {
        	RPr rPr = propertyResolver.getEffectiveRPr(rPrDirect, pPrDirect);
        	
				
			if (getLog().isDebugEnabled() && rPr!=null) {					
				getLog().debug(XmlUtils.marshaltoString(rPr, true, true));					
			}
			
			//if (rPr!=null) {
				XsltFOFunctions.createFoAttributes(conversionContext.getWmlPackage(), rPr, ((Element)currentParent) );
			//}
			
						
		} catch (Exception e) {
			getLog().error(e.getMessage(), e);
		} 
    	
    }

	// Note: prior to 17.0.1, rtlAwareAppendChildToCurrentP was overridden here to
	// wrap a w:rtl run's inline in <fo:bidi-override direction="rtl"
	// unicode-bidi="embed">.  That actively broke FOP's own bidi processing
	// (unshaped Arabic, and wrong run order in mixed RTL/LTR paragraphs); see
	// issue 660 and the TextDirection class.  The base implementation now does
	// what the interim override did.
}
