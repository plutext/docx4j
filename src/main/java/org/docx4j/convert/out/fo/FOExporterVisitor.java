package org.docx4j.convert.out.fo;

import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.Exporter;

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
