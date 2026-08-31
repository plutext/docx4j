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

import org.docx4j.convert.out.common.AbstractVisitorExporterDelegate;
import org.docx4j.convert.out.common.AbstractVisitorExporterDelegate.AbstractVisitorExporterGeneratorFactory;
import org.docx4j.convert.out.common.AbstractVisitorExporterGenerator;
import org.docx4j.convert.out.common.writer.AbstractBrWriter;
import org.docx4j.model.images.WordXmlPictureE10;
import org.docx4j.model.images.WordXmlPictureE20;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.StyleTree.AugmentedStyle;
import org.docx4j.model.styles.Tree;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTMoveBookmark;
import org.docx4j.wml.CTMoveFromRangeEnd;
import org.docx4j.wml.CTMoveToRangeEnd;
import org.docx4j.wml.DelText;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.RunIns;
import org.docx4j.wml.RunTrackChange;
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

		if (o instanceof DelText) {

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
	
	@Override
	protected Element handlePPr(HTMLConversionContext conversionContext, PPr pPrDirect, boolean sdt, Element currentParent) {
		Element ret = currentParent;

		if ( pPrDirect!=null ) {
			
			String pStyleVal=null;
			
			// Set @class
			if (pPrDirect.getPStyle()!=null
					&& pPrDirect.getPStyle().getVal()!=null) {

				pStyleVal = pPrDirect.getPStyle().getVal();						
				Tree<AugmentedStyle> pTree = conversionContext.getStyleTree().getParagraphStylesTree();		
				org.docx4j.model.styles.Node<AugmentedStyle> asn = pTree.get(pStyleVal);
				currentParent.setAttribute("class", 
						StyleTree.getHtmlClassAttributeValue(pTree, asn)			
				);
			}
						
			// Does our pPr contain anything else?
			boolean ignoreBorders = true;
			StringBuilder inlineStyle =  new StringBuilder();
			HtmlCssHelper.createCss(conversionContext.getWmlPackage(), pPrDirect, inlineStyle, ignoreBorders, false);				
			if (!inlineStyle.toString().equals("") ) {
				currentParent.setAttribute("style", inlineStyle.toString() );
			}
			
			// Numbering
			String numberText=null;
			String numId=null;
			String levelId=null;
			if (pPrDirect.getNumPr()!=null) {
				numId = pPrDirect.getNumPr().getNumId()==null ? null : pPrDirect.getNumPr().getNumId().getVal().toString(); 
				levelId = pPrDirect.getNumPr().getIlvl()==null ? null : pPrDirect.getNumPr().getIlvl().getVal().toString(); 
			}
			
        	ResultTriple triple = org.docx4j.model.listnumbering.Emulator.getNumber(
        			conversionContext.getWmlPackage(), pStyleVal, numId, levelId);   
        	

			if (triple==null) {
				getLog().debug("computed number ResultTriple was null");
        	} else {
				if (triple.getBullet() != null) {
					//numberText = (triple.getBullet() + " ");
					numberText = "\u2022  "; 
				} else if (triple.getNumString() == null) {
					getLog().error("computed NumString was null!");
					numberText = ("?");
				} else {
					numberText = (triple.getNumString() + " ");
				}
        	}
			if (numberText!=null) {
				currentParent.appendChild(document.createTextNode(
						numberText + " "));				
			}
		}
		
		return ret;
	}

    @Override
	protected void handleRPr(
    		HTMLConversionContext conversionContext,
    		PPr pPrDirect,
    		RPr rPrDirect, Element currentParent ) {

		// Set @class	
		if ( rPrDirect.getRStyle()!=null) {
			String rStyleVal = rPrDirect.getRStyle().getVal();
			Tree<AugmentedStyle> cTree = conversionContext.getStyleTree().getCharacterStylesTree();		
			org.docx4j.model.styles.Node<AugmentedStyle> asn = cTree.get(rStyleVal);
			if (asn==null) {
				getLog().warn("No style node for: " + rStyleVal);
			} else {
				currentParent.setAttribute("class", 
						StyleTree.getHtmlClassAttributeValue(cTree, asn)			
				);		
			}
		}
		
		// Does our rPr contain anything else?
		StringBuilder inlineStyle =  new StringBuilder();
		HtmlCssHelper.createCss(conversionContext.getWmlPackage(), rPrDirect, inlineStyle);				
		if (!inlineStyle.toString().equals("") ) {
			currentParent.setAttribute("style", inlineStyle.toString() );
		}
			
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
