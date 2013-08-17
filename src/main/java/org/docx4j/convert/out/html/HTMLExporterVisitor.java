package org.docx4j.convert.out.html;

import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.common.Exporter;

public class HTMLExporterVisitor extends AbstractHTMLExporter2 {
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
