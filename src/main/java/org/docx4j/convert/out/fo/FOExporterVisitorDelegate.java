package org.docx4j.convert.out.fo;

import java.util.List;

import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.AbstractVisitorExporterDelegate;
import org.docx4j.convert.out.common.ConversionSectionWrapper;
import org.docx4j.convert.out.common.XsltCommonFunctions;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.Part;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FOExporterVisitorDelegate extends AbstractVisitorExporterDelegate<FOSettings, FOConversionContext> {
	
	protected FOExporterVisitorDelegate() {
		super(FOExporterVisitorGenerator.GENERATOR_FACTORY);
	}

	private static String XSL_FO = "http://www.w3.org/1999/XSL/Format";

	@Override
	protected Element createDocumentRoot(FOConversionContext conversionContext, Document document) throws Docx4JException {
    	return document.createElementNS(XSL_FO, "root");
	}
	
	@Override
	protected void appendDocumentHeader(FOConversionContext conversionContext, 
			Document document, Element documentRoot) throws Docx4JException {
    	LayoutMasterSetBuilder.appendLayoutMasterSetFragment( conversionContext, documentRoot);  	
	}

	@Override
	protected Element createSectionRoot(FOConversionContext conversionContext, 
			Document document, 
			ConversionSectionWrapper sectionWrapper, Element currentParent) throws Docx4JException {
	Element pageSequence = document.createElementNS(XSL_FO, "page-sequence");
	int pageNumberInitial = sectionWrapper.getPageNumberInformation().getPageStart();
	String pageFormat = sectionWrapper.getPageNumberInformation().getPageFormat();
	
    	pageSequence.setAttribute("master-reference", sectionWrapper.getId());
    	pageSequence.setAttribute("id", "section_" + sectionWrapper.getId());
    	pageFormat = FormattingSwitchHelper.getFoPageNumberFormat(pageFormat);
    	if (pageNumberInitial > -1) {
        	pageSequence.setAttribute("initial-page-number", Integer.toString(pageNumberInitial));
    	}
    	if (pageFormat != null) {
        	pageSequence.setAttribute("format", pageFormat);
    	}
    	
    	return pageSequence;
	}

	@Override
	protected void appendSectionHeader(FOConversionContext conversionContext, Document document,
			ConversionSectionWrapper sectionWrapper,
			Element currentParent) throws Docx4JException {
	    Element staticContent = null;
    	
    	// <!--  First Page Header -->
		// <fo:static-content flow-name="xsl-region-before-firstpage">
    	if (XsltCommonFunctions.hasFirstHeader(conversionContext)) {
    		appendPartContent(
				conversionContext, document,
				currentParent, 
    			"xsl-region-before-firstpage",
    			sectionWrapper.getHeaderFooterPolicy().getFirstHeader(),
    			sectionWrapper.getHeaderFooterPolicy().getFirstHeader().getJaxbElement().getContent());
    	}
    	
    	// <!--  First Page Footer -->
    	// <fo:static-content flow-name="xsl-region-after-firstpage">
    	if (XsltCommonFunctions.hasFirstFooter(conversionContext)) {
    		appendPartContent(
				conversionContext, document,
				currentParent, 
    			"xsl-region-after-firstpage",
    			sectionWrapper.getHeaderFooterPolicy().getFirstFooter(),
    			sectionWrapper.getHeaderFooterPolicy().getFirstFooter().getJaxbElement().getContent());
    	}
    	
		// <!--  Even Page Header -->
		// <fo:static-content flow-name="xsl-region-before-evenpage">
		if (XsltCommonFunctions.hasEvenHeader(conversionContext)) {
			appendPartContent(
				conversionContext, document,
				currentParent, 
				"xsl-region-before-evenpage",
				sectionWrapper.getHeaderFooterPolicy().getEvenHeader(),
				sectionWrapper.getHeaderFooterPolicy().getEvenHeader().getJaxbElement().getContent());
		}
		
		// <!--  Even Page Footer -->
		// <fo:static-content flow-name="xsl-region-after-evenpage">
		if (XsltCommonFunctions.hasEvenFooter(conversionContext)) {
			appendPartContent(
				conversionContext, document,
				currentParent, 
				"xsl-region-after-evenpage",
				sectionWrapper.getHeaderFooterPolicy().getEvenFooter(),
				sectionWrapper.getHeaderFooterPolicy().getEvenFooter().getJaxbElement().getContent());
		}
    	
    	
		// <!--  Default/Odd Page Header -->
		// <fo:static-content flow-name="xsl-region-before-firstpage">
		if (XsltCommonFunctions.hasDefaultHeader(conversionContext)) {
			appendPartContent(
				conversionContext, document,
				currentParent, 
				"xsl-region-before-default",
				sectionWrapper.getHeaderFooterPolicy().getDefaultHeader(),
				sectionWrapper.getHeaderFooterPolicy().getDefaultHeader().getJaxbElement().getContent());
		}
		
		// <!--  Default/Odd Page Footer -->
		// <fo:static-content flow-name="xsl-region-after-firstpage">
		if (XsltCommonFunctions.hasDefaultFooter(conversionContext)) {
			appendPartContent(
				conversionContext, document,
				currentParent, 
				"xsl-region-after-default",
				sectionWrapper.getHeaderFooterPolicy().getDefaultFooter(),
				sectionWrapper.getHeaderFooterPolicy().getDefaultFooter().getJaxbElement().getContent());
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

	protected void appendPartContent(FOConversionContext conversionContext, 
									  Document document, 
									  Element currentParent,
									  String name, Part part, List<Object> content) throws Docx4JException {
    Element	flow = document.createElementNS(XSL_FO, "static-content");    	
    	currentParent.appendChild(flow); 
    	flow.setAttribute("flow-name", name);
    	appendPartContent(conversionContext, document, part, content, flow);
	}	

	@Override
	protected Element createSectionBody(FOConversionContext conversionContext, Document document, 
			ConversionSectionWrapper sectionWrapper,
			Element currentParent) throws Docx4JException {
    Element ret = document.createElementNS(XSL_FO, "flow");
    	ret.setAttribute("flow-name", "xsl-region-body");
    	return ret;
	}
}
