package org.docx4j.events;

public enum WellKnownProcessSteps implements ProcessStep {
	
	NOT_SET,
	PKG_LOAD, // query whether we should bother having PKG_LOAD 
	PKG_SAVE,	
	BIND_INSERT_XML,
	BIND_BIND_XML,
	BIND_REMOVE_SDT,
	BIND_REMOVE_XML,
	PDF,
	HTML_OUT,
	XHTML_IMPORT;	

}
