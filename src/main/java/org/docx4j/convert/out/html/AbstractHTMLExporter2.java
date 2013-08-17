package org.docx4j.convert.out.html;

import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.common.AbstractExporterDelegate;
import org.docx4j.convert.out.common.AbstractWmlExporter;
import org.docx4j.convert.out.common.ConversionSectionWrappers;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

public abstract class AbstractHTMLExporter2 extends AbstractWmlExporter<HTMLSettings, HTMLConversionContext>{
	
	protected AbstractHTMLExporter2(AbstractExporterDelegate<HTMLSettings, HTMLConversionContext> exporterDelegate) {
		super(exporterDelegate);
	}


	@Override
	protected HTMLConversionContext createContext(
			HTMLSettings conversionSettings, 
			WordprocessingMLPackage preprocessedPackage,
			ConversionSectionWrappers sectionWrappers) {
		return new HTMLConversionContext(conversionSettings, preprocessedPackage, sectionWrappers);
	}
	
}
