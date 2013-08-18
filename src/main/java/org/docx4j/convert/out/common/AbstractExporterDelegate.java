package org.docx4j.convert.out.common;

import java.io.OutputStream;

import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;

/**
 * The …ExporterDelegate generates the html/fo document from the WordprocessingMLPackage.
 * Docx4j supports convert.out via both xslt and non-xslt based approaches.
 * So some …ExporterDelegate use a Xslt transformation;
 * the others use a visitor (…ExporterGenerator)
 * 
 * @since 3.0
 */
public abstract class AbstractExporterDelegate<CS extends AbstractConversionSettings, CC extends AbstractWmlConversionContext> {
	public abstract void process(CS conversionSettings, CC conversionContext, OutputStream outputStream) throws Docx4JException;
}
