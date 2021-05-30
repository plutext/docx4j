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

public class Namespaces {

//	public static final String MARKUP_COMPATIBILITY = "http://schemas.openxmlformats.org/markup-compatibility/2006";	

	public static final String CONTENT_TYPES = 
		"http://schemas.openxmlformats.org/package/2006/content-types";
	
//	public static final String DIGITAL_SIGNATURE = "http://schemas.openxmlformats.org/package/2006/digital-signature";

	public static final String RELATIONSHIPS = 
		"http://schemas.openxmlformats.org/package/2006/relationships";

	public static final String RELATIONSHIPS_OFFICEDOC = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships";
	
	public final static String DIGITAL_SIGNATURE = "http://schemas.openxmlformats.org/package/2006/relationships/digital-signature/signature";
	public final static String DIGITAL_SIGNATURE_CERTIFICATE = "http://schemas.openxmlformats.org/package/2006/relationships/digital-signature/certificate";
	public final static String DIGITAL_SIGNATURE_ORIGIN = "http://schemas.openxmlformats.org/package/2006/relationships/digital-signature/origin";
	
	
	// Ones found in Package relationship
	
	// docProps/core.xml
	public final static String PROPERTIES_CORE = 
		"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties";
	
	// docProps/app.xml
	public final static String PROPERTIES_EXTENDED = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties";	

	// docProps/custom.xml
	public final static String PROPERTIES_CUSTOM = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/custom-properties";

	public final static String PROPERTIES_COVERPAGE = 
			"http://schemas.microsoft.com/‌office/‌2006/‌coverPageProps";
	
	public final static String IMAGE = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image";

	public final static String THUMBNAIL = 
		"http://schemas.openxmlformats.org/package/2006/relationships/metadata/thumbnail";
	
	// Office document eg word/document.xml
	public final static String DOCUMENT = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument";
	
	public final static String CUSTOM_XML_DATA_STORAGE = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/customXml";
	
	public final static String CUSTOM_XML_DATA_STORAGE_PROPERTIES = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/customXmlProps";
	

	
	// Ones typically found in Part level relationships
	
	// word/webSettings.xml
	public final static String WEB_SETTINGS = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/webSettings";
	
	// word/settings.xml
	public final static String SETTINGS = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings";
	
	// word/styles.xml
	public final static String STYLES = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles";
	
	// word/theme/theme1.xml
	public final static String THEME  = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme";

	// ppt/theme/themeOverride1.xml
	public final static String THEME_OVERRIDE  = 
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/themeOverride";
	
	// word/fontTable.xml
	public final static String FONT_TABLE = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/fontTable";

	public final static String FONT = 
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/font";
	
	public final static String HEADER = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/header";
		
	public final static String FOOTER = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer";

	public final static String GLOSSARY_DOCUMENT = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/glossaryDocument";

	public final static String NUMBERING = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering";

	public final static String FOOTNOTES = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/footnotes";

	public final static String ENDNOTES = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/endnotes";

	public final static String COMMENTS = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments";

	public final static String COMMENTS_EXTENDED = 
			"http://schemas.microsoft.com/office/2011/relationships/commentsExtended";

	public final static String COMMENTS_IDS = 
			"http://schemas.microsoft.com/office/2016/09/relationships/commentsIds";
	
	public final static String OFFICE_2011_PEOPLE = 
			"http://schemas.microsoft.com/office/2011/relationships/people";
	
	public final static String AF = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/aFChunk";
	
	/* The standard states that the relationship type is 
	 * 
	 *     http://schemas.openxmlformats.org/officeDocument/2006/relationships/afChunk
	 *   
	 *  but Word uses 
	 *  
	 *     http://schemas.openxmlformats.org/officeDocument/2006/relationships/aFChunk
	 *     
	 *  (see [MS-OE376]; we do the same)
	 */
		

	public final static String SUBDOCUMENT  = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/subDocument";
	
	public final static String ATTACHED_TEMPLATE  = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/attachedTemplate";
	
	public final static String VBA_PROJECT = 
		"http://schemas.microsoft.com/office/2006/relationships/vbaProject";
	public final static String VBA_DATA_WORD = 
		"http://schemas.microsoft.com/office/2006/relationships/wordVbaData";

	public final static String KEYMAP = 
			"http://schemas.microsoft.com/office/2006/relationships/keyMapCustomizations";
	
	/**
	 * @since 3.2.0
	 */
	public final static String VBA_PROJECT_SIGNATURE = 
			"http://schemas.microsoft.com/office/2006/relationships/vbaProjectSignature";

	public final static String OLE_OBJECT = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/oleObject"; 
	
	public final static String ACTIVEX_XML_OBJECT = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/control"; 
	
	// "/word/diagrams/data1.xml"
	public final static String DRAWINGML_DIAGRAM_DATA =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/diagramData";

	// "/word/diagrams/layout1.xml"
	public final static String DRAWINGML_DIAGRAM_LAYOUT =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/diagramLayout";
	
	// "/word/diagrams/colors1.xml"
	public final static String DRAWINGML_DIAGRAM_COLORS =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/diagramColors";
	
	// "/word/diagrams/quickStyle1.xml"
	public final static String DRAWINGML_DIAGRAM_STYLE =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/diagramQuickStyle";
	
	// "/diagrams/layoutheader1.xml"
	public final static String DRAWINGML_DIAGRAM_LAYOUT_HEADER =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/diagramLayoutHeader";

	// "/word/diagrams/drawing1.xml"
	public final static String DRAWINGML_DIAGRAM_DRAWING =
		"http://schemas.microsoft.com/office/2007/relationships/diagramDrawing";
	
	public final static String PRESENTATIONML_COMMENTS = 
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments";

	public final static String PRESENTATIONML_COMMENT_AUTHORS = 
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/commentAuthors";

	public final static String PRESENTATIONML_FONT_DATA = 	
	"http://schemas.openxmlformats.org/officeDocument/2006/relationships/font"; 
	
	// Target="ppt/presentation.xml"
	public final static String PRESENTATIONML_MAIN = 	
	"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"; 
		
	// Target="slides/slide1.xml"
	public final static String PRESENTATIONML_SLIDE = 
	"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide";
		
	// Target="slideMasters/slideMaster1.xml"
	public final static String PRESENTATIONML_SLIDE_MASTER = 
	"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster";
		
	// Target="../slideLayouts/slideLayout1.xml"
	public final static String PRESENTATIONML_SLIDE_LAYOUT = 	
	"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout";
		
	// Target="tableStyles.xml"
	public final static String PRESENTATIONML_TABLE_STYLES = 
	"http://schemas.openxmlformats.org/officeDocument/2006/relationships/tableStyles";
		
	// Target="presProps.xml"
	public final static String PRESENTATIONML_PRES_PROPS = 
	"http://schemas.openxmlformats.org/officeDocument/2006/relationships/presProps";
		
	// Target="viewProps.xml"
	public final static String PRESENTATIONML_VIEW_PROPS = 
	"http://schemas.openxmlformats.org/officeDocument/2006/relationships/viewProps";
		
	// Target="../tags/tag66.xml"
	public final static String PRESENTATIONML_TAGS = 
	"http://schemas.openxmlformats.org/officeDocument/2006/relationships/tags";
	
	// Target="docProps/thumbnail.jpeg"
	public final static String METADATA_THUMBNAIL = 
	"http://schemas.openxmlformats.org/package/2006/relationships/metadata/thumbnail"; 
	
	// "../notesSlides/notesSlide1.xml"/>
	public final static String PRESENTATIONML_NOTES_SLIDE =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/notesSlide"; 
    		
    // "notesMasters/notesMaster1.xml"/>
	public final static String PRESENTATIONML_NOTES_MASTER =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/notesMaster"; 
    
    // handoutMasters/handoutMaster1.xml"/>
	public final static String PRESENTATIONML_HANDOUT_MASTER =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/handoutMaster"; 
	


	// xl/workbook.xml
	public final static String SPREADSHEETML_WORKBOOK = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument";

	// worksheets/sheet1.xml
	public final static String SPREADSHEETML_WORKSHEET = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet";

	// chartsheets/sheet1.xml
	public final static String SPREADSHEETML_CHARTSHEET = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/chartsheet";

	// ../printerSettings/printerSettings1.bin
	public final static String SPREADSHEETML_PRINTER_SETTINGS = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/printerSettings";

	// calcChain.xml
	public final static String SPREADSHEETML_CALC_CHAIN = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/calcChain";

	// sharedStrings.xml
	public final static String SPREADSHEETML_SHARED_STRINGS = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings";

	// styles.xml
	public final static String SPREADSHEETML_STYLES = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles";

	///xl/drawings/drawing1.xml
	public final static String SPREADSHEETML_DRAWING =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing";
	
	///xl/charts/chart1.xml
	// Should have just called this CHART or RELS_CHART,
	// since it is also used in PresentationML
	public final static String SPREADSHEETML_CHART =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart";
	
	public final static String CHART_USER_SHAPES =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/chartUserShapes";	

	// /xl/comments1.xml 
	public final static String SPREADSHEETML_COMMENTS =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments";
		
	///xl/pivotTables/pivotTable1.xml
	public final static String SPREADSHEETML_PIVOT_TABLE =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/pivotTable";

	// /xl/queryTables/queryTable1.xml
	public final static String SPREADSHEETML_QUERY_TABLE =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/queryTable";
	
	// /xl/tables/table1.xml
	public final static String SPREADSHEETML_TABLE =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/table";
	
	// /xl/connections.xml
	public final static String SPREADSHEETML_CONNECTIONS =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/connections";	
	
	///xl/pivotCache/pivotCacheDefinition1.xml
	public final static String SPREADSHEETML_PIVOT_CACHE_DEFINITION =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/pivotCacheDefinition";
	
	///xl/pivotCache/pivotCacheRecords1.xml
	public final static String SPREADSHEETML_PIVOT_CACHE_RECORDS =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/pivotCacheRecords";
	
	///xl/externalLinks/externalLink1.xml
	public final static String SPREADSHEETML_EXTERNAL_LINK =
			"http://schemas.openxmlformats.org/officeDocument/2006/relationships/externalLink";
            
//	public final static String NS_OFFICE = "urn:schemas-microsoft-com:office:office";

//	public final static String NS_OFFICE_12 = "http://schemas.microsoft.com/office/2004/7/core";

//	public final static String NS_OMML = "http://schemas.microsoft.com/office/omml/2004/12/core";

//	public final static String NS_VML = "urn:schemas-microsoft-com:vml";

//	public final static String NS_DRAWINGML = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing";

//	public final static String NS_WORD10 = "urn:schemas-microsoft-com:office:word";

	//open xml namespace for word (displayed as w: in document.xml)
	public final static String NS_WORD12 = 
		"http://schemas.openxmlformats.org/wordprocessingml/2006/main";

	public final static String W_NAMESPACE_DECLARATION = "xmlns:w=\"" + NS_WORD12 + "\""; 
		
	public final static String HYPERLINK  = 
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink";
	
	public final static String PKG_XML = "http://schemas.microsoft.com/office/2006/xmlPackage";
	
	public final static String EMBEDDED_PKG = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/package";
	
	public final static String VML =  "http://schemas.openxmlformats.org/officeDocument/2006/relationships/vmlDrawing";
	
	public final static String XML_EVENTS = "http://www.w3.org/2001/xml-events";

	public final static String XFORMS = "http://www.w3.org/2002/xforms";

	public final static String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	
	public final static String WEB_EXTENSION_TASKPANES = "http://schemas.microsoft.com/office/webextensions/taskpanes/2010/11";
	public final static String WEB_EXTENSION_WEBEXTENSION = "http://schemas.microsoft.com/office/webextensions/webextension/2010/11";
	
}
