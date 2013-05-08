package org.docx4j.openpackaging.parts.WordprocessingML;

import org.docx4j.openpackaging.contenttype.ContentTypes;

public enum AltChunkType {
	// Same as http://msdn.microsoft.com/en-us/library/documentformat.openxml.packaging.alternativeformatimportparttype.aspx
	
	Xhtml("xhtml", "application/xhtml+xml"),
				// Alternatively, you can serve your XHTML (any version) as application/xml, or even as text/xml
				// but which is right for Word?
    Mht("mht", "message/rfc822"), 
    Xml("xml", "application/xml"),	 // or text/xml?
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
