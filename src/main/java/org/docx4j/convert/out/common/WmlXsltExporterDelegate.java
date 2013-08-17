package org.docx4j.convert.out.common;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.w3c.dom.Document;

public class WmlXsltExporterDelegate<CS extends AbstractConversionSettings, CC extends AbstractWmlConversionContext> extends AbstractXsltExporterDelegate<CS, CC> {

	public WmlXsltExporterDelegate(String defaultTemplatesResource) {
		super(defaultTemplatesResource);
	}

	@Override
	protected Document getSourceDocument(
			AbstractConversionSettings conversionSettings,
			AbstractWmlConversionContext conversionContext)
			throws Docx4JException {
		ConversionSectionWrappers conversionSectionWrappers = conversionContext.getSections();
		Document ret = XmlUtils.marshaltoW3CDomDocument(conversionSectionWrappers.createSections(),
				Context.jcSectionModel);
		return ret;
	}

}
