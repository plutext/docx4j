package org.docx4j.convert.out.fo;

import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.Exporter;

/**
 * Converts the document to fo with a visitor
 * 
 * Uses Delegate: org.docx4j.convert.out.fo.FOExporterVisitorDelegate
 * Uses Generator: org.docx4j.convert.out.fo.FOExporterVisitorGenerator 
 * 
 * @since 3.0
 */
public class FOExporterVisitor extends AbstractFOExporter {
	protected static final FOExporterVisitorDelegate EXPORTER_DELEGATE_INSTANCE = new 
			FOExporterVisitorDelegate();
	
	protected static FOExporterVisitor instance = null;
	
	protected FOExporterVisitor() {
		super(EXPORTER_DELEGATE_INSTANCE);
	}
	
	public static Exporter<FOSettings> getInstance() {
		if (instance == null) {
			synchronized(FOExporterVisitor.class) {
				if (instance == null) {
					instance = new FOExporterVisitor();
				}
			}
		}
		return instance;
	}

}
