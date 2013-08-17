package org.docx4j.convert.out.common;

import java.io.OutputStream;

import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;

/** The AbstractExporter is the interface to singletons that do the conversion 
 *  from a OpcPackage to a target format.
 * 
 * @param <T>
 */
public interface Exporter<T extends AbstractConversionSettings> {
	public void export(T conversionSettings, OutputStream outputStream) throws Docx4JException;
}
