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
	
	public final static String OFFICEDOCUMENT_FONT =
		"application/vnd.openxmlformats-officedocument.obfuscatedFont";
	
	public final static String OFFICEDOCUMENT_VBA_PROJECT =
		"application/vnd.ms-office.vbaProject";

	public final static String OFFICEDOCUMENT_VBA_DATA =
		"application/vnd.ms-word.vbaData+xml";
	
	public final static String OFFICEDOCUMENT_OLE_OBJECT =
		"application/vnd.openxmlformats-officedocument.oleObject";
	
	public final static String OFFICEDOCUMENT_ACTIVEX_XML_OBJECT =
		"application/vnd.ms-office.activeX+xml";
	
//	public final static String OFFICEDOCUMENT_ACTIVEX_BINARY_OBJECT =
//		"???";
	
	// PartName="/word/document.xml"
	public final static String WORDPROCESSINGML_DOCUMENT = "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml";
	public final static String WORDPROCESSINGML_DOCUMENT_MACROENABLED = "application/vnd.ms-word.document.macroEnabled.main+xml";

	
	// PartName="/word/comments.xml"
	public final static String WORDPROCESSINGML_COMMENTS = "application/vnd.openxmlformats-officedocument.wordprocessingml.comments+xml";

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
	
	// PartName="/word/numbering.xml"
	public final static String WORDPROCESSINGML_NUMBERING ="application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"; 
	
	public final static String WORDPROCESSINGML_SETTINGS =
		"application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml";
	public final static String WORDPROCESSINGML_STYLES = 
		"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml";
	public final static String WORDPROCESSINGML_WEBSETTINGS =
		"application/vnd.openxmlformats-officedocument.wordprocessingml.webSettings+xml";


    // /ppt/presentation.xml
	public final static String PRESENTATIONML_MAIN = 
		"application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml";

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
//	/**
//	 * Digital Signature Origin part.
//	 */
//	public static final String DIGITAL_SIGNATURE_ORIGIN_PART = "application/vnd.openxmlformats-package.digital-signature-origin";
//
//	/**
//	 * Digital Signature XML Signature part.
//	 */
//	public static final String DIGITAL_SIGNATURE_XML_SIGNATURE_PART = "application/vnd.openxmlformats-package.digital-signature-xmlsignature+xml";
//

	// Images format supported by WordprocessingML. See tc45-2006-334.pdf p.146

	public static final String IMAGE_EMF = "image/x-emf";
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


	public static final String XML = "text/xml";
	
	
}