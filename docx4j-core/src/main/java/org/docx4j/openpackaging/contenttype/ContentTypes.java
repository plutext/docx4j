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

/*
 * Portions Copyright (c) 2006, Wygwam
 * With respect to those portions:
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met: 
 * 
 * - Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation and/or 
 * other materials provided with the distribution.
 * - Neither the name of Wygwam nor the names of its contributors may be 
 * used to endorse or promote products derived from this software without 
 * specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.docx4j.openpackaging.contenttype;

/**
 * Content types.
 * 
 * @author CDubettier define some constants, Julien Chable
 * @version 0.1
 */
public class ContentTypes {
	
	/////////////////////////////////////////////////////////////////////////////
	// 				CONTENT TYPES
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Relationships part - CRITICAL!
	 */
	public static final String RELATIONSHIPS_PART = "application/vnd.openxmlformats-package.relationships+xml";
	
	
	//   <Default ContentType="application/xml" Extension="xml"/>
	public final static String APPLICATION_XML = "application/xml";	
	
	// PartName="/docProps/core.xml" 
	public final static String PACKAGE_COREPROPERTIES =
		"application/vnd.openxmlformats-package.core-properties+xml";	
	// PartName="/docProps/custom.xml"
	public final static String OFFICEDOCUMENT_CUSTOMPROPERTIES =
		"application/vnd.openxmlformats-officedocument.custom-properties+xml";	
	// PartName="/docProps/app.xml"
	public final static String OFFICEDOCUMENT_EXTENDEDPROPERTIES =
		"application/vnd.openxmlformats-officedocument.extended-properties+xml";
	
	// PartName="/customXml/itemProps1.xml"
	public final static String OFFICEDOCUMENT_CUSTOMXML_DATASTORAGEPROPERTIES = 
		"application/vnd.openxmlformats-officedocument.customXmlProperties+xml";
	public final static String OFFICEDOCUMENT_CUSTOMXML_DATASTORAGE = 
		"application/xml";
	
	public final static String OFFICEDOCUMENT_THEME =
		"application/vnd.openxmlformats-officedocument.theme+xml";

	public final static String OFFICEDOCUMENT_THEME_OVERRIDE ="application/vnd.openxmlformats-officedocument.themeOverride+xml";
	
	public final static String OFFICEDOCUMENT_FONT =
		"application/vnd.openxmlformats-officedocument.obfuscatedFont";

	public final static String TRUETYPE_FONT =
			"application/x-font-ttf";
	
	public final static String OFFICEDOCUMENT_VBA_PROJECT =
		"application/vnd.ms-office.vbaProject";

	public final static String OFFICEDOCUMENT_VBA_DATA =
		"application/vnd.ms-word.vbaData+xml";
	
	/**
	 * @since 3.2.0
	 */
	public final static String OFFICEDOCUMENT_VBA_PROJECT_SIGNATURE =
			"application/vnd.ms-office.vbaProjectSignature";
	
	public final static String OFFICEDOCUMENT_OLE_OBJECT =
		"application/vnd.openxmlformats-officedocument.oleObject";
	
	public final static String OFFICEDOCUMENT_ACTIVEX_OBJECT =
		"application/vnd.ms-office.activeX";
	
	public final static String OFFICEDOCUMENT_ACTIVEX_XML_OBJECT =
		"application/vnd.ms-office.activeX+xml";
	
	
	/**
	 * @since 3.3.2
	 */
	public final static String WEB_EXTENSION_TASKPANES = "application/vnd.ms-office.webextensiontaskpanes+xml";	
	public final static String WEB_EXTENSION_WEBEXTENSION = "application/vnd.ms-office.webextension+xml";
	
	
	// PartName="/word/document.xml"
	public final static String WORDPROCESSINGML_DOCUMENT = "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml";
	public final static String WORDPROCESSINGML_DOCUMENT_MACROENABLED = "application/vnd.ms-word.document.macroEnabled.main+xml";
	public final static String WORDPROCESSINGML_TEMPLATE = "application/vnd.openxmlformats-officedocument.wordprocessingml.template.main+xml";
	public final static String WORDPROCESSINGML_TEMPLATE_MACROENABLED = "application/vnd.ms-word.template.macroEnabledTemplate.main+xml";

	
	// PartName="/word/comments.xml"
	public final static String WORDPROCESSINGML_COMMENTS = "application/vnd.openxmlformats-officedocument.wordprocessingml.comments+xml";

	public final static String WORDPROCESSINGML_COMMENTS_EXTENDED = "application/vnd.openxmlformats-officedocument.wordprocessingml.commentsExtended+xml";

	public final static String WORDPROCESSINGML_COMMENTS_IDS = "application/vnd.openxmlformats-officedocument.wordprocessingml.commentsIds+xml";
	
	// PartName="/word/endnotes.xml"
	public final static String WORDPROCESSINGML_ENDNOTES = "application/vnd.openxmlformats-officedocument.wordprocessingml.endnotes+xml";
	// PartName="/word/fontTable.xml"
	public final static String WORDPROCESSINGML_FONTTABLE =
		"application/vnd.openxmlformats-officedocument.wordprocessingml.fontTable+xml";

	// PartName="/word/footer[N].xml"
	public final static String WORDPROCESSINGML_FOOTER ="application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml";

	// PartName="/word/footnotes.xml"
	public final static String WORDPROCESSINGML_FOOTNOTES ="application/vnd.openxmlformats-officedocument.wordprocessingml.footnotes+xml"; 
	  
	// PartName="/word/glossary/document.xml"
	public final static String WORDPROCESSINGML_GLOSSARYDOCUMENT ="application/vnd.openxmlformats-officedocument.wordprocessingml.document.glossary+xml"; 

	// PartName="/word/header[N].xml"
	public final static String WORDPROCESSINGML_HEADER ="application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml";

	public final static String MS_WORD_KEYMAP = "application/vnd.ms-word.keyMapCustomizations+xml";
	
	// PartName="/word/numbering.xml"
	public final static String WORDPROCESSINGML_NUMBERING ="application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"; 
	
	public final static String WORDPROCESSINGML_PEOPLE ="application/vnd.openxmlformats-officedocument.wordprocessingml.people+xml";
	
	public final static String WORDPROCESSINGML_SETTINGS =
		"application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml";
	
	public final static String WORDPROCESSINGML_STYLES = 
		"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml";
	
	public final static String WORDPROCESSINGML_STYLESWITHEFFECTS = 
			"application/vnd.ms-word.stylesWithEffects+xml";
	
	public final static String WORDPROCESSINGML_WEBSETTINGS =
		"application/vnd.openxmlformats-officedocument.wordprocessingml.webSettings+xml";

	// "/word/diagrams/data1.xml"
	public final static String DRAWINGML_DIAGRAM_DATA =
		"application/vnd.openxmlformats-officedocument.drawingml.diagramData+xml";

	// "/word/diagrams/layout1.xml"
	public final static String DRAWINGML_DIAGRAM_LAYOUT =
		"application/vnd.openxmlformats-officedocument.drawingml.diagramLayout+xml";
	
	// "/word/diagrams/colors1.xml"
	public final static String DRAWINGML_DIAGRAM_COLORS =
		"application/vnd.openxmlformats-officedocument.drawingml.diagramColors+xml";
	
	// "/word/diagrams/quickStyle1.xml"
	public final static String DRAWINGML_DIAGRAM_STYLE =
		"application/vnd.openxmlformats-officedocument.drawingml.diagramStyle+xml";

	// "/word/diagrams/layout1.xml"
	public final static String DRAWINGML_DIAGRAM_LAYOUT_HEADER =
		"application/vnd.openxmlformats-officedocument.drawingml.diagramLayoutHeader+xml";

	// "/word/diagrams/drawing1.xml"
	public final static String DRAWINGML_DIAGRAM_DRAWING =
		"application/vnd.ms-office.drawingml.diagramDrawing+xml";
	
	// embedded presentation
	public final static String PRESENTATION = 
		"application/vnd.openxmlformats-officedocument.presentationml.presentation";

	public final static String PRESENTATIONML_COMMENTS = 
			"application/vnd.openxmlformats-officedocument.presentationml.comments+xml";

	public final static String PRESENTATIONML_FONT_DATA = 
			"application/x-fontdata";
	
	public final static String PRESENTATIONML_COMMENT_AUTHORS = 
			"application/vnd.openxmlformats-officedocument.presentationml.commentAuthors+xml";
	
    // /ppt/presentation.xml
	public final static String PRESENTATIONML_MAIN = 
		"application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml";
	
	// .pptm
	public final static String PRESENTATIONML_MACROENABLED = 
			"application/vnd.ms-powerpoint.presentation.macroEnabled.main+xml";
	// .potm |
	public final static String PRESENTATIONML_TEMPLATE_MACROENABLED = 
			"application/vnd.ms-powerpoint.template.macroEnabled.main+xml";
	
	// .potx | 
	public final static String PRESENTATIONML_TEMPLATE = 
		"application/vnd.openxmlformats-officedocument.presentationml.template.main+xml";
	// .ppsx | 
	public final static String PRESENTATIONML_SLIDESHOW = 
		"application/vnd.openxmlformats-officedocument.presentationml.slideshow.main+xml";
	
	// /ppt/slides/slide1.xml"
	public final static String PRESENTATIONML_SLIDE = 
		"application/vnd.openxmlformats-officedocument.presentationml.slide+xml";

	// /ppt/slideMasters/slideMaster1.xml
	public final static String PRESENTATIONML_SLIDE_MASTER = 
		"application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml";

	// /ppt/slideLayouts/slideLayout11.xml
	public final static String PRESENTATIONML_SLIDE_LAYOUT = 
		"application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml";

	// /ppt/tableStyles.xml
	public final static String PRESENTATIONML_TABLE_STYLES = 
		"application/vnd.openxmlformats-officedocument.presentationml.tableStyles+xml";

	// /ppt/presProps.xml
	public final static String PRESENTATIONML_PRES_PROPS = 
		"application/vnd.openxmlformats-officedocument.presentationml.presProps+xml";

	// /ppt/viewProps.xml
	public final static String PRESENTATIONML_VIEW_PROPS = 
		"application/vnd.openxmlformats-officedocument.presentationml.viewProps+xml";

	// /ppt/tags/tag19.xml
	public final static String PRESENTATIONML_TAGS = 
		"application/vnd.openxmlformats-officedocument.presentationml.tags+xml";
	  
	// "/ppt/notesSlides/notesSlide1.xml"
	// p:notes
	public final static String PRESENTATIONML_NOTES_SLIDE =
		"application/vnd.openxmlformats-officedocument.presentationml.notesSlide+xml"; 
    		
    // "/ppt/notesMasters/notesMaster1.xml"
	// p:notesMaster
	public final static String PRESENTATIONML_NOTES_MASTER =
		"application/vnd.openxmlformats-officedocument.presentationml.notesMaster+xml"; 
    
    // /ppt/handoutMasters/handoutMaster1.xml"
	// p:handoutMaster
	public final static String PRESENTATIONML_HANDOUT_MASTER =
		"application/vnd.openxmlformats-officedocument.presentationml.handoutMaster+xml"; 
	  
	public final static String SPREADSHEETML_WORKBOOK_MACROENABLED = 
		"application/vnd.ms-excel.sheet.macroEnabled.main+xml";

	public final static String SPREADSHEETML_TEMPLATE_MACROENABLED = 
		"application/vnd.ms-excel.template.macroEnabled.main+xml";

	public final static String SPREADSHEETML_TEMPLATE = 
		"application/vnd.openxmlformats-officedocument.spreadsheetml.template.main+xml";;
	
	// PartName="/xl/workbook.xml"
	public final static String SPREADSHEETML_WORKBOOK = 
		"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml";

	public final static String SPREADSHEETML_PRINTER_SETTINGS = 
		"application/vnd.openxmlformats-officedocument.spreadsheetml.printerSettings";

	// PartName="/xl/styles.xml"
	public final static String SPREADSHEETML_STYLES = 
		"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml";
	
	// PartName="/xl/worksheets/sheet1.xml"
	public final static String SPREADSHEETML_WORKSHEET = 
		"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml";

	// PartName="/xl/chartsheets/sheet1.xml"
	public final static String SPREADSHEETML_CHARTSHEET = 
		"application/vnd.openxmlformats-officedocument.spreadsheetml.chartsheet+xml";
	
	// PartName="/xl/calcChain.xml"
	public final static String SPREADSHEETML_CALC_CHAIN = 
		"application/vnd.openxmlformats-officedocument.spreadsheetml.calcChain+xml";
	
	// PartName="/xl/sharedStrings.xml"
	public final static String SPREADSHEETML_SHARED_STRINGS = 
		"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml";

	// /xl/pivotCache/pivotCacheDefinition1.xml
	public final static String SPREADSHEETML_PIVOT_CACHE_DEFINITION =
		"application/vnd.openxmlformats-officedocument.spreadsheetml.pivotCacheDefinition+xml";

	// /xl/pivotCache/pivotCacheRecords1.xml
	public final static String SPREADSHEETML_PIVOT_CACHE_RECORDS =
		"application/vnd.openxmlformats-officedocument.spreadsheetml.pivotCacheRecords+xml";

	// /xl/pivotTables/pivotTable1.xml
	public final static String SPREADSHEETML_PIVOT_TABLE =
		"application/vnd.openxmlformats-officedocument.spreadsheetml.pivotTable+xml";

	// /xl/queryTables/queryTable1.xml
	public final static String SPREADSHEETML_QUERY_TABLE =
		"application/vnd.openxmlformats-officedocument.spreadsheetml.queryTable+xml";
	
	// /xl/tables/table1.xml
	public final static String SPREADSHEETML_TABLE =
		"application/vnd.openxmlformats-officedocument.spreadsheetml.table+xml";
	
	// /xl/comments1.xml
	public final static String SPREADSHEETML_COMMENTS =
		"application/vnd.openxmlformats-officedocument.spreadsheetml.comments+xml";
	
	// /xl/connections.xml
	public final static String SPREADSHEETML_CONNECTIONS =
		"application/vnd.openxmlformats-officedocument.spreadsheetml.connections+xml";	
	
	///xl/externalLinks/externalLink1.xml
	public final static String SPREADSHEETML_EXTERNAL_LINK =
			"application/vnd.openxmlformats-officedocument.spreadsheetml.externalLink+xml";
	
	// /xl/charts/chart1.xml
	public final static String DRAWINGML_CHART =
		"application/vnd.openxmlformats-officedocument.drawingml.chart+xml";

	// /xl/drawings/drawing1.xml
	public final static String DRAWINGML_DRAWING =
		"application/vnd.openxmlformats-officedocument.drawing+xml";

	public final static String DRAWINGML_CHART_SHAPES =
			"application/vnd.openxmlformats-officedocument.drawingml.chartshapes+xml";	

	public final static String VML_DRAWING =
		"application/vnd.openxmlformats-officedocument.vmlDrawing";
	
	
	/*
	 * Open Packaging Convention (Annex F : Standard Namespaces and Content
	 * Types)
	 * 
	 * TODO - make the names consistent with above!
	 */

//
//	/**
//	 * Digital Signature Certificate part.
//	 */
//	public static final String DIGITAL_SIGNATURE_CERTIFICATE_PART = "application/vnd.openxmlformats-package.digital-signature-certificate";
//
	/**
	 * Digital Signature Origin part.
	 */
	public static final String DIGITAL_SIGNATURE_ORIGIN_PART = "application/vnd.openxmlformats-package.digital-signature-origin";

	/**
	 * Digital Signature XML Signature part.
	 */
	public static final String DIGITAL_SIGNATURE_XML_SIGNATURE_PART = "application/vnd.openxmlformats-package.digital-signature-xmlsignature+xml";


	// Images format supported by WordprocessingML. See tc45-2006-334.pdf p.146

	public static final String IMAGE_EMF = "image/x-emf";
	public static final String IMAGE_EMF2 = "image/emf";  // as per org.apache.xmlgraphics.image.loader.ImageInfo
	public static final String EXTENSION_EMF = "emf";
	
	public static final String IMAGE_WMF = "image/x-wmf";
	public static final String EXTENSION_WMF = "wmf";
	
	
	// image/gif http://www.w3.org/Graphics/GIF/spec-gif89a.txt
	public static final String IMAGE_GIF = "image/gif";
	public static final String EXTENSION_GIF = "gif";
	
	public static final String IMAGE_JPEG = "image/jpeg";
	public static final String EXTENSION_JPG_1 = "jpg";
	public static final String EXTENSION_JPG_2 = "jpeg";

	/**
	 * Pict image format.
	 * 
	 * @see http://developer.apple.com/documentation/mac/QuickDraw/QuickDraw-2.html
	 */
	public static final String IMAGE_PICT = "image/pict";	
	public static final String EXTENSION_PICT = "tiff"; // ?????????	
	
	// image/png ISO/IEC 15948:2003 http://www.libpng.org/pub/png/spec/
	public static final String IMAGE_PNG = "image/png";
	public static final String EXTENSION_PNG = "png";


	/**
	 * TIFF image format.
	 * 
	 * @see http://partners.adobe.com/public/developer/tiff/index.html#spec
	 */
	public static final String IMAGE_TIFF = "image/tiff";
	public static final String EXTENSION_TIFF = "tiff";

	public static final String IMAGE_EPS = "application/postscript";  // as reported by XmlGraphics 
	public static final String EXTENSION_EPS = "eps";  

	public static final String IMAGE_BMP = "image/bmp";
	public static final String EXTENSION_BMP = "bmp";
	
	// <Default Extension="svg" ContentType="image/svg+xml"/>
	// Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image"
	public static final String IMAGE_SVG = "image/svg+xml";

	
	public static final String XML = "text/xml";
	
	/**
	 * @since 8.1.0
	 */
	public final static String INK_ML = "application/inkml+xml";
	
}