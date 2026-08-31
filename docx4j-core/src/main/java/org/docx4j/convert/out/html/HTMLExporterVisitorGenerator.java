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
package org.docx4j.convert.out.html;

import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.AbstractVisitorExporterDelegate;
import org.docx4j.convert.out.common.AbstractVisitorExporterDelegate.AbstractVisitorExporterGeneratorFactory;
import org.docx4j.convert.out.common.AbstractVisitorExporterGenerator;
import org.docx4j.convert.out.common.writer.AbstractBrWriter;
import org.docx4j.model.images.WordXmlPictureE10;
import org.docx4j.model.images.WordXmlPictureE20;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTMoveBookmark;
import org.docx4j.wml.CTMoveFromRangeEnd;
import org.docx4j.wml.CTMoveToRangeEnd;
import org.docx4j.wml.DelText;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.RunIns;
import org.docx4j.wml.RunTrackChange;
import org.docx4j.wml.SdtElement;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class HTMLExporterVisitorGenerator extends AbstractVisitorExporterGenerator<HTMLConversionContext>{

	public static final AbstractVisitorExporterDelegate.AbstractVisitorExporterGeneratorFactory<HTMLConversionContext> GENERATOR_FACTORY = 
			new AbstractVisitorExporterGeneratorFactory<HTMLConversionContext>() {
				@Override
				public AbstractVisitorExporterGenerator<HTMLConversionContext> createInstance(
						HTMLConversionContext conversionContext,
						Document document, Node parentNode) {
					return new HTMLExporterVisitorGenerator(conversionContext, document, parentNode);
				}
			};
			
	
	private HTMLExporterVisitorGenerator(HTMLConversionContext conversionContext, Document document, Node parentNode) {
		super(conversionContext, document, parentNode);
	}

	@Override
	protected AbstractVisitorExporterGeneratorFactory<HTMLConversionContext> getFactory() {
		return GENERATOR_FACTORY;
	}

	@Override
	public List<Object> apply(Object o) {

		// HTML-specific handling of elements the shared visitor has no case for;
		// the output matches the corresponding docx2xhtml-core.xslt templates.

		if (o instanceof P) {

			// Since 17.0.4, the paragraph element is built by the same code as the
			// XSLT pathway (children first, then wrap), rather than by handlePPr
			handleP((P)o);
			return null;

		} else if (o instanceof DelText) {

			if (!conversionContext.isInComplexFieldDefinition()) {
				Element span = createNode(document, NODE_INLINE);
				span.setAttribute("class", "del");
				span.setTextContent(((DelText)o).getValue());
				getCurrentParent().appendChild(span);
			}
			return null;

		} else if (o instanceof RunIns || o instanceof RunDel
				|| o instanceof RunTrackChange) {

			// w:ins/w:moveTo/w:moveFrom get their span wrapper in walkJAXBElements
			// (their runs are appended while walking); w:del content is just
			// traversed, with the visible marking on the w:delText above
			return null;

		} else if (o instanceof R.SoftHyphen) {

			if (!conversionContext.isInComplexFieldDefinition()) {
				getCurrentParent().appendChild(document.createTextNode("\u00AD"));
			}
			return null;

		} else if (o instanceof R.NoBreakHyphen) {

			// U+2011 NON-BREAKING HYPHEN (browsers can be trusted to have a glyph,
			// unlike the FO pathway's PDF fonts)
			if (!conversionContext.isInComplexFieldDefinition()) {
				getCurrentParent().appendChild(document.createTextNode("\u2011"));
			}
			return null;

		} else if (o instanceof R.Cr) {

			if (!conversionContext.isInComplexFieldDefinition()) {
				Element br = document.createElement("br");
				br.setAttribute("clear", "all");
				getCurrentParent().appendChild(br);
			}
			return null;

		} else if (o instanceof CTMoveBookmark
				|| o instanceof CTMoveFromRangeEnd
				|| o instanceof CTMoveToRangeEnd) {

			// move range markers are skipped, as in the XSLT (NB CTMoveBookmark
			// extends CTBookmark, so without this it would reach the bookmark
			// writer and emit an anchor)
			return null;

		}

		return super.apply(o);
	}

	@Override
	public boolean shouldTraverse(Object o) {

		if (o instanceof P) {
			// its contents were already converted in apply (handleP)
			return false;
		}
		return super.shouldTraverse(o);
	}

	/**
	 * Convert the paragraph's children into a fragment first (cf the XSLT's
	 * childResults), then wrap them in the paragraph's element via the shared
	 * XsltHTMLFunctions.createBlockForPPr — the same code the XSLT pathway uses,
	 * so the class attribute (incl. for default-styled paragraphs), numbering
	 * (incl. style-based) and its indentation, empty-paragraph nbsp, span merging
	 * and the bookmarkStart mapTo=id contract all behave identically.  A w:p
	 * inside an HTML_ELEMENT list sdt becomes an li, per the w:p template.
	 *
	 * @since 17.0.4
	 */
	private void handleP(P p) {

		DocumentFragment childResults = document.createDocumentFragment();

		PPr pPrDirect = p.getPPr();
		String pStyleVal = (pPrDirect!=null && pPrDirect.getPStyle()!=null
				? pPrDirect.getPStyle().getVal() : "");
		boolean htmlElement = isInHtmlElementSdt(p);

		// the number text comes first (cf the w:p template); not in the li case
		// (the li numbers itself), nor for a completely empty paragraph in a table
		// (see the template's comment about microscopic row heights)
		boolean emptyInTable = (tc.peek()!=null && pPrDirect==null && p.getContent().isEmpty());
		if (!htmlElement && !emptyInTable) {
			String numId = "";
			String levelId = "";
			if (pPrDirect!=null && pPrDirect.getNumPr()!=null) {
				if (pPrDirect.getNumPr().getNumId()!=null
						&& pPrDirect.getNumPr().getNumId().getVal()!=null) {
					numId = pPrDirect.getNumPr().getNumId().getVal().toString();
				}
				if (pPrDirect.getNumPr().getIlvl()!=null
						&& pPrDirect.getNumPr().getIlvl().getVal()!=null) {
					levelId = pPrDirect.getNumPr().getIlvl().getVal().toString();
				}
			}
			String numberText = XsltHTMLFunctions.getNumberXmlNode(
					conversionContext, null, pStyleVal, numId, levelId);
			if (numberText!=null) {
				childResults.appendChild(document.createTextNode(numberText));
			}
		}

		HTMLExporterVisitorGenerator generator = (HTMLExporterVisitorGenerator)
				getFactory().createInstance(conversionContext, document, childResults);
		generator.pPr = pPrDirect;
		new TraversalUtil(p.getContent(), generator);

		// createBlock merges the numbering indentation into the pPr's ind, so give
		// it a copy (the XSLT pathway works on a freshly unmarshalled copy anyway)
		PPr pPrCopy = (pPrDirect==null ? null : XmlUtils.deepCopy(pPrDirect));
		DocumentFragment block = (htmlElement
				? XsltHTMLFunctions.createListItemBlockForPPr(conversionContext, pPrCopy, pStyleVal, childResults)
				: XsltHTMLFunctions.createBlockForPPr(conversionContext, pPrCopy, pStyleVal, childResults));
		if (block!=null) {
			(tc.peek()!=null ? tc.peek() : parentNode)
					.appendChild(document.importNode(block, true));
		}
		currentP = null;
		currentSpan = null;
	}

	/** is this paragraph directly inside an sdt whose tag contains HTML_ELEMENT
	 *  (the ListsToContentControls markup)?  cf the w:p template's
	 *  contains(../../w:sdtPr/w:tag/@w:val, 'HTML_ELEMENT') */
	private boolean isInHtmlElementSdt(P p) {

		Object parent = p.getParent();
		if (!(parent instanceof org.jvnet.jaxb.lang.Child)) return false;
		Object grandparent = ((org.jvnet.jaxb.lang.Child)parent).getParent();
		if (!(grandparent instanceof SdtElement)) return false;
		SdtElement sdt = (SdtElement)grandparent;
		return sdt.getSdtPr()!=null
				&& sdt.getSdtPr().getTag()!=null
				&& sdt.getSdtPr().getTag().getVal()!=null
				&& sdt.getSdtPr().getTag().getVal().contains("HTML_ELEMENT");
	}

	/**
	 * The composition createBlockForRPr performs in the XSLT pathway, applied to
	 * the streamed run span after its children have been walked: no span at all
	 * for a run without rPr, otherwise class/style incl. merging with the w:t
	 * font-selection span.
	 */
	private void postProcessRunSpan(R r, Element span) {

		if (span==null || span.getParentNode()==null) return;

		if (r.getRPr()==null) {
			// the XSLT emits no span for a run without rPr; unwrap
			Node parent = span.getParentNode();
			while (span.getFirstChild()!=null) {
				parent.insertBefore(span.getFirstChild(), span);
			}
			parent.removeChild(span);
		} else {
			XsltHTMLFunctions.composeRunSpan(conversionContext, r.getRPr(), span);
		}
		currentSpan = null;
	}

	/** the span class for a tracked-changes wrapper element, or null if o isn't one */
	private String trackChangeClass(Object o) {

		if (o instanceof RunIns) return "ins";
		if (o instanceof RunTrackChange) {
			// w:moveTo and w:moveFrom share this class; the JAXBElement wrapper
			// in the parent's content list distinguishes them
			String name = jaxbElementName((RunTrackChange)o);
			if ("moveTo".equals(name)) return "ins";
			if ("moveFrom".equals(name)) return "del";
		}
		return null;
	}

	@Override
	public void walkJAXBElements(Object o) {

		String trackClass = trackChangeClass(o);
		if (trackClass!=null
				&& !conversionContext.isInComplexFieldDefinition()) {

			Element wrapper = createNode(document, NODE_INLINE);
			wrapper.setAttribute("class", trackClass);
			(currentP != null ? currentP : parentNode).appendChild(wrapper);

			// the w:ins/w:moveTo/w:moveFrom contains runs; have them append their
			// spans to the wrapper
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

		if (o instanceof R
				&& !conversionContext.isInComplexFieldDefinition()) {

			Element runSpan = currentSpan; // just created by apply(R)
			super.walkJAXBElements(o);
			postProcessRunSpan((R)o, runSpan);
			return;
		}

		super.walkJAXBElements(o);
	}

	@Override
	protected Element createNode(Document doc, int nodeType) {
		switch (nodeType) {
			case NODE_BLOCK:
				return document.createElement("p");
			case NODE_INLINE:
				return document.createElement("span");
		}
		return null;
	}
	
	@Override
	protected DocumentFragment createImage(int imgType, HTMLConversionContext conversionContext, Object anchorOrInline) {
			switch (imgType) {
			case IMAGE_E10:
				return WordXmlPictureE10.createHtmlImgE10(conversionContext, anchorOrInline);
			case IMAGE_E20:
				return WordXmlPictureE20.createHtmlImgE20(conversionContext, anchorOrInline);
			}
		return null;
	}
	
	/**
	 * Not used by this generator since 17.0.4: the paragraph element is built via
	 * XsltHTMLFunctions.createBlockForPPr (see handleP), the same code as the XSLT
	 * pathway.
	 */
	@Override
	protected Element handlePPr(HTMLConversionContext conversionContext, PPr pPrDirect, boolean sdt, Element currentParent) {

		return currentParent;
	}

	/**
	 * Not used by this generator since 17.0.4: the run span's attributes are
	 * applied after its children are walked, via XsltHTMLFunctions.composeRunSpan
	 * (see postProcessRunSpan), the same composition as the XSLT pathway.
	 */
    @Override
	protected void handleRPr(
    		HTMLConversionContext conversionContext,
    		PPr pPrDirect,
    		RPr rPrDirect, Element currentParent ) {
	}

	@Override
	protected void handleBr(Br o) {
		
		// Just the usual case (unlike XSL FO, no attempt is made here to manage vertical space) 
		convertToNode(conversionContext, 
				  o, AbstractBrWriter.WRITER_ID,
				  document, (currentP != null ? currentP : parentNode));
		
		currentSpan=null;		
			
	}    
}
