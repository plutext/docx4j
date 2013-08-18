package org.docx4j.convert.out.html;

import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.common.Exporter;

/**
 * Converts the document to HTML with a visitor
 * 
 * Uses Delegate: org.docx4j.convert.out.html.HTMLExporterVisitorDelegate
 * Uses Generator: org.docx4j.convert.out.html.HTMLExporterVisitorGenerator
 * 
 * @since 3.0
 */
public class HTMLExporterVisitor extends AbstractHTMLExporter3 {
	protected static final HTMLExporterVisitorDelegate EXPORTER_DELEGATE_INSTANCE = new 
			HTMLExporterVisitorDelegate();

	protected static HTMLExporterVisitor instance = null;
	
	protected HTMLExporterVisitor() {
		super(EXPORTER_DELEGATE_INSTANCE);
	}

	
	public static Exporter<HTMLSettings> getInstance() {
		if (instance == null) {
			synchronized(HTMLExporterVisitor.class) {
				if (instance == null) {
					instance = new HTMLExporterVisitor();
				}
			}
		}
		return instance;
	}
}
