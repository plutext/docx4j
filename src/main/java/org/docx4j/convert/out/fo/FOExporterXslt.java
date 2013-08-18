package org.docx4j.convert.out.fo;

import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.Exporter;
import org.docx4j.convert.out.common.WmlXsltExporterDelegate;

/**
 * Converts the document to fo with a xsl transformation
 * 
 * Uses Delegate: org.docx4j.convert.out.common. WmlXsltExporterDelegate
 * Uses Generator: none
 * 
 * @since 3.0
 */
public class FOExporterXslt extends AbstractFOExporter {
	protected static final String DEFAULT_TEMPLATES_RESOURCE = "org/docx4j/convert/out/fo/docx2fo.xslt";
	protected static final WmlXsltExporterDelegate<FOSettings, FOConversionContext> EXPORTER_DELEGATE = 
			new WmlXsltExporterDelegate<FOSettings, FOConversionContext>(DEFAULT_TEMPLATES_RESOURCE);

	protected static FOExporterXslt instance = null;
	
	protected FOExporterXslt() {
		super(EXPORTER_DELEGATE);
	}

	public static Exporter<FOSettings> getInstance() {
		if (instance == null) {
			synchronized(FOExporterXslt.class) {
				if (instance == null) {
					instance = new FOExporterXslt();
				}
			}
		}
		return instance;
	}
}
