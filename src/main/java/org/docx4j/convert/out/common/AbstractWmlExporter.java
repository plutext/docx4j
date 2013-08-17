package org.docx4j.convert.out.common;

import java.io.OutputStream;

import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/** Superclass for the export of WordprocessingMLPackage(s)
 * 
 * @param <CS>
 */
public abstract class AbstractWmlExporter<CS extends AbstractConversionSettings, CC extends AbstractWmlConversionContext> extends AbstractExporter<CS, CC, WordprocessingMLPackage> {
	protected AbstractExporterDelegate<CS, CC> exporterDelegate = null;

	protected AbstractWmlExporter(AbstractExporterDelegate<CS, CC> exporterDelegate) {
		this.exporterDelegate = exporterDelegate;
	}

	@Override
	protected WordprocessingMLPackage preprocess(CS conversionSettings) throws Docx4JException {
	WordprocessingMLPackage wmlPackage = null;
		try {
			wmlPackage = (WordprocessingMLPackage)conversionSettings.getWmlPackage();
		}
		catch (ClassCastException cce) {
			throw new Docx4JException("Invalid document package in the settings, it isn't a WordprocessingMLPackage");
		}
		if (wmlPackage == null) {
			throw new Docx4JException("Missing WordprocessingMLPackage in the conversion settings");
		}
		return Preprocess.process(wmlPackage, conversionSettings.getFeatures());
	}

	@Override
	protected ConversionSectionWrappers createWrappers(CS conversionSettings, WordprocessingMLPackage preprocessedPackage) throws Docx4JException {
	ConversionSectionWrappers ret = null;
		ret = Preprocess.createWrappers(preprocessedPackage, conversionSettings.getFeatures());
		return ret;
	}

	@Override
	protected void process(CS conversionSettings, CC conversionContext, OutputStream outputStream) throws Docx4JException {
		exporterDelegate.process(conversionSettings, conversionContext, outputStream);
	}
}
