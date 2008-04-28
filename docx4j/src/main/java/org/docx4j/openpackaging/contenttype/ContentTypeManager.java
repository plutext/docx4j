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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.packages.Package;
import org.docx4j.openpackaging.parts.*;
import org.docx4j.openpackaging.parts.WordprocessingML.*;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;


public interface ContentTypeManager {

//	protected static Logger log = Logger.getLogger(ContentTypeManager.class);
	
	/* 
	 * 
	 * [Content_Types].xml example:
	 * 
		<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
		
		<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
			<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
			<Default Extension="xml"  ContentType="application/xml"/>
			<Override PartName="/word/document.xml"     ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
			<Override PartName="/word/styles.xml"       ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
			<Override PartName="/docProps/app.xml"      ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
			<Override PartName="/word/settings.xml"     ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml"/>
			<Override PartName="/word/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml"/>
			<Override PartName="/word/fontTable.xml"    ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.fontTable+xml"/>
			<Override PartName="/word/webSettings.xml"  ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.webSettings+xml"/>
			<Override PartName="/docProps/core.xml"     ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
		</Types>

	 * 
	 * 
	 * Note - there is nothing in the spec which ensures that any PartName in an Override 
	 * listed here is actually present in the Package?
	 * */

	/**
	 * Content type part name.
	 */
	public static final String CONTENT_TYPES_PART_NAME = "[Content_Types].xml";

	/**
	 * Content type namespace
	 */
	public static final String TYPES_NAMESPACE_URI = "http://schemas.openxmlformats.org/package/2006/content-types";

	
	
	/* Xml elements in content type part */

	public static final String TYPES_TAG_NAME = "Types";

	public static final String DEFAULT_TAG_NAME = "Default";

	public static final String EXTENSION_ATTRIBUTE_NAME = "Extension";

	public static final String CONTENT_TYPE_ATTRIBUTE_NAME = "ContentType";

	public static final String OVERRIDE_TAG_NAME = "Override";

	public static final String PART_NAME_ATTRIBUTE_NAME = "PartName";

	/**
	 * Build association extention-> content type (will be stored in
	 * [Content_Types].xml) for example ContentType="image/png" Extension="png"
	 * 
	 * @param partUri
	 *            the uri that will be stored
	 * @return <b>false</b> if an error occured.
	 */
	public void addContentType(PartName partName, String contentType);
	

	/**
	 * Add an override content type for a specific part.
	 * 
	 * @param partUri
	 *            Uri of the part.
	 * @param contentType
	 *            Content type of the part.
	 */
	public void addOverrideContentType(URI partUri, String contentType);
	
//	public String getOverrideContentType(URI partUri);

	/* Given a content type, return the Part Name URI is it
	 * overridden by.
	 */ 
	public URI getPartNameOverridenByContentType(String contentType);

	
	/* Return a part of the appropriate sub class */
	public  Part getPart(String partName) throws URISyntaxException, PartUnrecognisedException, InvalidFormatException;

	/* Return a part appropriate to the content type */
	public Part newPartForContentType(String contentType, String partName)
		throws InvalidFormatException, PartUnrecognisedException;

	public Part CreateDefaultPartObject(String partName)
	throws InvalidFormatException;	
	
	public Part CreateCustomXmlPropertiesPartObject(String partName)
	throws InvalidFormatException;
	
	public Part CreateDocPropsCorePartObject(String partName)
	throws InvalidFormatException;

	public Part CreateDocPropsCustomPartObject(String partName)
	throws InvalidFormatException;

	public Part CreateDocPropsExtendedPartObject(String partName)
	throws InvalidFormatException;

	public Part CreateFontTablePartObject(String partName)
	throws InvalidFormatException;

	public Part CreateCommentsPartObject(String partName)
	throws InvalidFormatException;

	public Part CreateEndnotesPartObject(String partName)
	throws InvalidFormatException;	

	public Part CreateFooterPartObject(String partName)
	throws InvalidFormatException;
	
	public Part CreateFootnotesPartObject(String partName)
	throws InvalidFormatException;
	
	public Part CreateGlossaryDocumentPartObject(String partName)
	throws InvalidFormatException;
	
	public Part CreateHeaderPartObject(String partName)
	throws InvalidFormatException;
	
	public Part CreateNumberingPartObject(String partName)
	throws InvalidFormatException;	
	
	public Part CreateMainDocumentPartObject(String partName)
	throws InvalidFormatException;

	public Part CreateDocumentSettingsPartObject(String partName)
	throws InvalidFormatException;

	public Part CreateStyleDefinitionsPartObject(String partName)
	throws InvalidFormatException;

	public Part CreateThemePartObject(String partName)
	throws InvalidFormatException;

	public Part CreateWebSettingsPartObject(String partName)
	throws InvalidFormatException;

	public Part CreateObfuscatedFontPartObject(String partName)
	throws InvalidFormatException;

	
	
	/**
	 * Add a content type associated with the specified extension.
	 * 
	 * @param extension
	 *            The part name extension to bind to a content type.
	 * @param contentType
	 *            The content type associated with the specified extension.
	 */
	public void addDefaultContentType(String extension, String contentType);

	/**
	 * Delete a content type based on the specified part name. If the specified
	 * part name is register with an override content type, then this content
	 * type is remove, else the content type is remove in the default content
	 * type list if it exists.
	 * 
	 * @param partUri
	 *            The part URI associated with the override content type to
	 *            delete.
	 */
	public void removeContentType(PartName partName);
	
	/**
	 * Check if the specified content type is already register.
	 * 
	 * @param contentType
	 *            The content type to check.
	 * @return <code>true</code> if the specified content type is already
	 *         register, then <code>false</code>.
	 */
	public boolean isContentTypeRegistered(String contentType); 
	
	/**
	 * Get the content type for the specified part, if any.
	 * 
	 * @param partUri
	 *            The URI part to check.
	 * @return The content type associated with the URI (in case of an override
	 *         content type) or the extension (in case of default content type),
	 *         else <code>null</code>.
	 */
	public String getContentType(PartName partName); 
	
	/**
	 * Clear all content types.
	 */
	public void clearAll(); 
	

	/**
	 * Clear all override content types.
	 * 
	 */
	public void clearOverrideContentTypes(); 
	

	/**
	 * Parse the content types part.
	 * 
	 * @throws InvalidFormatException
	 *             Throws if the content type doesn't exist or the XML format is
	 *             invalid.
	 */
	public void parseContentTypesFile(Document xmlContentTypetDoc)
			throws InvalidFormatException; 
	
	/**
	 * Generates the XML for the contents type part.
	 * 
	 * @param outStream
	 *            The output stream use to save the XML content of the content
	 *            types part.
	 * @return <b>true</b> if the operation success, else <b>false</b>.
	 */
	public Document getDocument(); 
	
	/* Return a package of the appropriate type */
	public Package createPackage() throws InvalidFormatException;

}