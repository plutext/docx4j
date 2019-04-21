package org.docx4j.openpackaging.parts.WordprocessingML;

import org.docx4j.openpackaging.contenttype.ContentTypes;

public enum AltChunkType {
	// Same as http://msdn.microsoft.com/en-us/library/documentformat.openxml.packaging.alternativeformatimportparttype.aspx
	// except that has 'htm', not 'html'
	
	Xhtml("xhtml", "application/xhtml+xml"),
				// Alternatively, you can serve your XHTML (any version) as application/xml, or even as text/xml
				// but which is right for Word?
    Mht("mht", "message/rfc822"), 
    
    // Office 2003 Word XML format (schemas) and the Office 2007 flat OPC format.
    Xml("xml", "application/xml"),	 // see http://blogs.msdn.com/b/ericwhite/archive/2010/03/11/formats-supported-for-altchunk.aspx
    
    TextPlain("txt", "text/plain"),
    WordprocessingML("docx", ContentTypes.WORDPROCESSINGML_DOCUMENT),
    OfficeWordMacroEnabled("docm", ContentTypes.WORDPROCESSINGML_DOCUMENT_MACROENABLED),
    OfficeWordTemplate("dotx", ContentTypes.WORDPROCESSINGML_TEMPLATE),
    OfficeWordMacroEnabledTemplate("dotm",ContentTypes.WORDPROCESSINGML_TEMPLATE_MACROENABLED),
    Rtf("rtf", "text/rtf"),
    Html("html", "text/html");
	
	// file name extension
	private String extension;
	// content-type
	private String contentType;
	
	private AltChunkType(String extension, String contentType) {
		this.extension = extension;
		this.contentType = contentType;
	}
	
	public String getExtension() {
		return extension;
	}
	
	public String getContentType() {
		return contentType;
	}

}
