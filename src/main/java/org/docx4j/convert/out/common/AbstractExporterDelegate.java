package org.docx4j.convert.out.common;

import java.io.OutputStream;

import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;

public abstract class AbstractExporterDelegate<CS extends AbstractConversionSettings, CC extends AbstractWmlConversionContext> {
	public abstract void process(CS conversionSettings, CC conversionContext, OutputStream outputStream) throws Docx4JException;
}
