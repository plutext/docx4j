/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */

package org.docx4j.openpackaging.parts.relationships;

import org.dom4j.Namespace;

public class Namespaces {

//	public static final String MARKUP_COMPATIBILITY = "http://schemas.openxmlformats.org/markup-compatibility/2006";	

	public static final String CONTENT_TYPES = "http://schemas.openxmlformats.org/package/2006/content-types";
	
//	public static final String DIGITAL_SIGNATURE = "http://schemas.openxmlformats.org/package/2006/digital-signature";

	public static final String RELATIONSHIPS = "http://schemas.openxmlformats.org/package/2006/relationships";

//	public final static String DIGITAL_SIGNATURE = "http://schemas.openxmlformats.org/package/2006/relationships/digital-signature/signature";
//	public final static String DIGITAL_SIGNATURE_CERTIFICATE = "http://schemas.openxmlformats.org/package/2006/relationships/digital-signature/certificate";
//	public final static String DIGITAL_SIGNATURE_ORIGIN = "http://schemas.openxmlformats.org/package/2006/relationships/digital-signature/origin";
	
	
	// Ones found in Package relationship
	
	// docProps/core.xml
	public final static String PROPERTIES_CORE = "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties";

	
	
	
	
	
	
	// docProps/app.xml
	public final static String PROPERTIES_EXTENDED = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties";	

	// docProps/custom.xml
	public final static String PROPERTIES_CUSTOM = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/custom-properties";	
	
	public final static String IMAGE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image";

	public final static String THUMBNAIL = "http://schemas.openxmlformats.org/package/2006/relationships/metadata/thumbnail";
	
	// Office document eg word/document.xml
	public final static String DOCUMENT = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument";
	
	

	
	// Ones typically found in Part level relationships
	
	// word/webSettings.xml
	public final static String WEB_SETTINGS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/webSettings";
	
	// word/settings.xml
	public final static String SETTINGS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings";
	
	// word/styles.xml
	public final static String STYLES = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles";
	
	// word/theme/theme1.xml
	public final static String THEME  = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme";
	
	// word/fontTable.xml
	public final static String FONT_TABLE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/fontTable";
	
	public final static String HEADER = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/header";
		
	public final static String FOOTER = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer";

	public final static String GLOSSARY_DOCUMENT = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/glossaryDocument";

	public final static String NUMBERING = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering";

	public final static String FOOTNOTES = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/footnotes";

	public final static String ENDNOTES = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/endnotes";

	public final static String COMMENTS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments";
	
	public final static String AF = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/aFChunk";
	
	
	public final static String VBA_PROJECT = "http://schemas.microsoft.com/office/2006/relationships/vbaProject";
	public final static String VBA_DATA_WORD = "http://schemas.microsoft.com/office/2006/relationships/wordVbaData";

	
//	public final static String NS_OFFICE = "urn:schemas-microsoft-com:office:office";

//	public final static String NS_OFFICE_12 = "http://schemas.microsoft.com/office/2004/7/core";

//	public final static String NS_OMML = "http://schemas.microsoft.com/office/omml/2004/12/core";

//	public final static String NS_VML = "urn:schemas-microsoft-com:vml";

//	public final static String NS_DRAWINGML = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing";

//	public final static String NS_WORD10 = "urn:schemas-microsoft-com:office:word";

	//open xml namespace for word (displayed as w: in document.xml)
	public final static String NS_WORD12 = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

	// JBH added
	public final static Namespace namespaceWord = new Namespace("w", NS_WORD12);
	
	
	
	
}
