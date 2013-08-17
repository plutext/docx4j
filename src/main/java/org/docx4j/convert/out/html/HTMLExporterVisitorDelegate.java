package org.docx4j.convert.out.html;

import java.util.List;

import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.common.AbstractVisitorExporterDelegate;
import org.docx4j.convert.out.common.ConversionSectionWrapper;
import org.docx4j.convert.out.common.XsltCommonFunctions;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTFtnEdn;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HTMLExporterVisitorDelegate extends AbstractVisitorExporterDelegate<HTMLSettings, HTMLConversionContext> {

	protected HTMLExporterVisitorDelegate() {
		super(HTMLExporterVisitorGenerator.GENERATOR_FACTORY);
	}

	@Override
	protected Element createDocumentRoot(HTMLConversionContext conversionContext, Document document) throws Docx4JException {
		return document.createElement("html");
	}

	@Override
	protected void appendDocumentHeader(HTMLConversionContext conversionContext, Document document, Element documentRoot) throws Docx4JException {
    Element	headEl = document.createElement("head");
	Element meta = document.createElement("meta");
	Element element = null;
	StringBuilder buffer = new StringBuilder(10240);
	String userCSS = conversionContext.getUserCSS();
	String userScript = conversionContext.getUserScript();
	
	boolean hasDefaultHeader = false;
	boolean hasDefaultFooter = false;
    	documentRoot.appendChild(headEl);    	
    	meta.setAttribute("http-equiv", "Content-Type");
    	meta.setAttribute("content", "text/html; charset=utf-8");
    	headEl.appendChild(meta);
    	
    	//TODO: This doesn't quite work as the defaultHeader and defaultFooter are per section,
    	//but this definition is on the document level. 
    	//To access the first section, we have to call first a next() and later return to start()
    	try {
    		conversionContext.getSections().next();
        	hasDefaultHeader = XsltCommonFunctions.hasDefaultHeader(conversionContext);
        	hasDefaultFooter = XsltCommonFunctions.hasDefaultHeader(conversionContext);
    	}
    	finally {
    		conversionContext.getSections().start();
    	}
    	HtmlCssHelper.createDefaultCss(hasDefaultHeader, hasDefaultFooter, buffer);
		HtmlCssHelper.createCssForStyles(conversionContext.getWmlPackage(), 
										 conversionContext.getStyleTree(), 
										 buffer);
		if ((userCSS != null) && (userCSS.length() > 0)) {
			buffer.append(userCSS);
		}
		element = conversionContext.createStyleElement(document, buffer.toString());
		if (element != null) {
			headEl.appendChild(element);
		}
		buffer.setLength(0);
		HtmlCssHelper.createDefaultScript(buffer);
		if ((userScript != null) && (userScript.length() > 0)) {
			buffer.append(userScript);
		}
		element = conversionContext.createScriptElement(document, buffer.toString());
		if (element != null) {
			headEl.appendChild(element);
		}
	}

	@Override
	protected Element createDocumentBody(HTMLConversionContext conversionContext, Document document, Element documentRoot) {
		return document.createElement("body");
	}

	@Override
	protected Element createSectionRoot(
			HTMLConversionContext conversionContext, Document document,
			ConversionSectionWrapper sectionWrapper, Element currentParent)
			throws Docx4JException {
		// A div for each section might make sense, but docx2xhtml doesn't use it,
		// for this reason we don't do it here.
		return null;
	}

	@Override
	protected void appendSectionHeader(HTMLConversionContext conversionContext,
			Document document, ConversionSectionWrapper sectionWrapper,
			Element currentParent) throws Docx4JException {
		if (XsltCommonFunctions.hasDefaultHeader(conversionContext)) {
			appendPartContent(
				conversionContext, document,
				sectionWrapper, currentParent, 
				"header",
				sectionWrapper.getHeaderFooterPolicy().getDefaultHeader(),
				sectionWrapper.getHeaderFooterPolicy().getDefaultHeader().getJaxbElement().getContent());
		}
	}

	@Override
	protected Element createSectionBody(
			HTMLConversionContext conversionContext, Document document,
			ConversionSectionWrapper sectionWrapper, Element currentParent)
			throws Docx4JException {
    Element	div = document.createElement("div");
    	div.setAttribute("class", "document");
    	return div;
	}

	@Override
	protected void appendSectionFooter(HTMLConversionContext conversionContext,
			Document document, ConversionSectionWrapper sectionWrapper,
			Element currentParent) throws Docx4JException {
		if (XsltCommonFunctions.hasDefaultFooter(conversionContext)) {
			appendPartContent(
				conversionContext, document,
				sectionWrapper, currentParent, 
				"footer",
				sectionWrapper.getHeaderFooterPolicy().getDefaultFooter(),
				sectionWrapper.getHeaderFooterPolicy().getDefaultFooter().getJaxbElement().getContent());
		}
	}

	protected void appendPartContent(HTMLConversionContext conversionContext, 
								  	 Document document, 
								     ConversionSectionWrapper sectionWrapper, 
								     Element currentParent,
								     String className, Part part, List<Object> content) throws Docx4JException {
    Element	div = document.createElement("div");    	
    	currentParent.appendChild(div); 
    	div.setAttribute("class", className);
    	appendPartContent(conversionContext, document, part, content, div);
	}	

	@Override
	protected void appendDocumentFooter(
			HTMLConversionContext conversionContext, Document document,
			Element documentRoot) throws Docx4JException {
	MainDocumentPart mainDocumentPart = conversionContext.getWmlPackage().getMainDocumentPart();
		if (mainDocumentPart.hasFootnotesPart()) {
			appendFootnotesEndnotes(
				conversionContext, document,
				documentRoot, 
				"footnotes",
				mainDocumentPart.getFootnotesPart(),
				mainDocumentPart.getFootnotesPart().getJaxbElement().getFootnote());
		}
		if (mainDocumentPart.hasEndnotesPart()) {
			appendFootnotesEndnotes(
				conversionContext, document,
				documentRoot, 
				"endnotes",
				mainDocumentPart.getEndNotesPart(),
				mainDocumentPart.getEndNotesPart().getJaxbElement().getEndnote());
		}
	}

	protected void appendFootnotesEndnotes(HTMLConversionContext conversionContext,
			Document document, Element documentRoot, String string,
			Part part, List<CTFtnEdn> ftnEdnList) {
		//TODO:...
	}

	

}
