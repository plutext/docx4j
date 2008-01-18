/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
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
import org.docx4j.openpackaging.packages.Package;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.*;
import org.docx4j.openpackaging.parts.WordprocessingML.*;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;


/**
 * Manage package content types ([Content_Types].xml ) .
 * 
 * @author Julien Chable
 * @version 1.0
 */
public class ContentTypeManagerImpl implements ContentTypeManager {

	protected static Logger log = Logger.getLogger(ContentTypeManagerImpl.class);

	/**
	 * Content type part name.
	 */
	public static final String CONTENT_TYPES_PART_NAME = "[Content_Types].xml";

	/**
	 * Content type namespace
	 */
	public static final String TYPES_NAMESPACE_URI = "http://schemas.openxmlformats.org/package/2006/content-types";

	
	
	/* Xml elements in content type part */

	private static final String TYPES_TAG_NAME = "Types";

	private static final String DEFAULT_TAG_NAME = "Default";

	private static final String EXTENSION_ATTRIBUTE_NAME = "Extension";

	private static final String CONTENT_TYPE_ATTRIBUTE_NAME = "ContentType";

	private static final String OVERRIDE_TAG_NAME = "Override";

	private static final String PART_NAME_ATTRIBUTE_NAME = "PartName";

	/**
	 * Default content type tree. <Extension, ContentType>
	 */
	private TreeMap<String, String> defaultContentType;

	/**
	 * Override content type tree.
	 */
	private TreeMap<URI, String> overrideContentType;

	/**
	 * Constructor. Parses the content of the specified input stream.
	 * 
	 * @param archive
	 *            If different of <i>null</i> then the content types part is
	 *            retrieve and parse.
	 * @throws InvalidFormatException
	 *             If the content types part content is not valid.
	 */
	public ContentTypeManagerImpl(Document contentTypes) throws InvalidFormatException {
		init();
		if (contentTypes != null) {
			try {
				parseContentTypesFile(contentTypes);
			} catch (InvalidFormatException e) {
				throw new InvalidFormatException(
						"Can't read content types part !");
			}
		} else {
			log.warn("Passed null content types ?!");
		}
	}

	public ContentTypeManagerImpl()  {
		init();
	}
	
	private void init() {
		defaultContentType = new TreeMap<String, String>();
		overrideContentType = new TreeMap<URI, String>();
	}

	/**
	 * Build association extention-> content type (will be stored in
	 * [Content_Types].xml) for example ContentType="image/png" Extension="png"
	 * 
	 * @param partUri
	 *            the uri that will be stored
	 * @return <b>false</b> if an error occured.
	 */
	public void addContentType(PartName partName, String contentType) {
		boolean defaultCTExists = false;
		String extension = partName.getExtension();
		if ((extension.length() == 0)
				|| (this.defaultContentType.containsKey(extension) && !(defaultCTExists = this.defaultContentType
						.containsValue(contentType))))
			this.addOverrideContentType(partName.getURI(), contentType);
		else if (!defaultCTExists)
			this.addDefaultContentType(extension, contentType);
	}

	/**
	 * Add an override content type for a specific part.
	 * 
	 * @param partUri
	 *            Uri of the part.
	 * @param contentType
	 *            Content type of the part.
	 */
	public void addOverrideContentType(URI partUri, String contentType) {
		log.info("Registered " + partUri.toString() );
		overrideContentType.put(partUri, contentType);
	}

//	public String getOverrideContentType(URI partUri) {
//		return overrideContentType.get(partUri);
//	}


	/* Given a content type, return the Part Name URI is it
	 * overridden by.
	 */ 
	public URI getPartNameOverridenByContentType(String contentType) {
		
		// hmm, can there only be one instance of a given
		// content type?
		
		Iterator i = overrideContentType.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry)i.next();
			if (e != null) {
				log.debug("Inspecting " + e.getValue());
				if ( ((String)e.getValue()).equals(contentType) ) {
					log.debug("Matched!");
					return (URI)e.getKey(); 
				}
			} 
		} 		
		return null;
		
	}
	
	/* Return a part of the appropriate sub class */
	public  Part getPart(String partName) throws URISyntaxException, InvalidFormatException  {
		
		Part p;

		// look for an override
		String contentType = (String)overrideContentType.get(new URI(partName));
		if (contentType!=null ) {
			log.debug("Found content type '" + contentType + "' for " + partName);
			 p = newPartForContentType(contentType, partName);
			 p.setContentType( new ContentType(contentType) );
			 return p;
		}		
		
		// if there is no override, get use the file extension
		String ext = partName.substring(partName.indexOf(".") + 1);
		log.info("Looking at extension '" + ext);
		contentType = (String)defaultContentType.get(ext);
		if (contentType!=null ) {
			log.info("Found content type '" + contentType + "' for "
							+ partName);
			p = newPartForContentType(contentType, partName);
			p.setContentType(new ContentType(contentType));
			return p;
		}
		
		// otherwise
		log.error("No content type found for " + partName);
		return null;		
		
	}
	
	public Part newPartForContentType(String contentType, String partName)
		throws InvalidFormatException {
		
		// TODO - a number of WordML parts aren't listed here!
		if (contentType.equals(ContentTypes.WORDPROCESSINGML_DOCUMENT)) { 
			return CreateMainDocumentPartObject(partName);
			// how is the main document distinguished from the glossary document?
			// Answer:- Main Document is a Package level relationship target,
			// whereas the Glossary Document is a Part-level target (from the
			// Main Document part)
		} else if (contentType.equals(ContentTypes.PACKAGE_COREPROPERTIES)) {
			return CreateDocPropsCorePartObject(partName ); 
		} else if (contentType.equals(ContentTypes.OFFICEDOCUMENT_CUSTOMPROPERTIES)) {
			return CreateDocPropsCustomPartObject(partName );
		} else if (contentType.equals(ContentTypes.OFFICEDOCUMENT_CUSTOMXMLPROPERTIES)) {
			return CreateCustomXmlPropertiesPartObject(partName );
		} else if (contentType.equals(ContentTypes.OFFICEDOCUMENT_EXTENDEDPROPERTIES)) {
			return CreateDocPropsExtendedPartObject(partName );
		} else if (contentType.equals(ContentTypes.OFFICEDOCUMENT_THEME)) {
			return CreateThemePartObject(partName );
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_COMMENTS)) {
			return CreateCommentsPartObject(partName );
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_ENDNOTES)) {
			return CreateEndnotesPartObject(partName );
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_FONTTABLE)) {
			return CreateFontTablePartObject(partName );
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_FOOTER)) {
			return CreateFooterPartObject(partName );
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_FOOTNOTES)) {
			return CreateFootnotesPartObject(partName );
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_GLOSSARYDOCUMENT)) {
			return CreateGlossaryDocumentPartObject(partName );
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_HEADER)) {
			return CreateHeaderPartObject(partName );
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_NUMBERING)) {
			return CreateNumberingPartObject(partName );
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_SETTINGS)) {
			return CreateDocumentSettingsPartObject(partName );
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_STYLES)) { 
			return CreateStyleDefinitionsPartObject( partName);
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_WEBSETTINGS)) {
			return CreateWebSettingsPartObject(partName );
		} else if (contentType.equals(ContentTypes.APPLICATION_XML)) {
			log.warn("DefaultPart used for part '" + partName 
					+ "' of content type '" + contentType + "'");
			return CreateDefaultPartObject(partName );
		} else {
			log.error("No subclass found for " + partName);
			throw new InvalidFormatException("No subclass found for " + partName);		

		}

	}

	public Part CreateDefaultPartObject(String partName)
			throws InvalidFormatException {
		return new DefaultXmlPart(new PartName(partName));
	}
	
	public Part CreateMainDocumentPartObject(String partName)
			throws InvalidFormatException {
		return new MainDocumentPart(new PartName(partName));
	}

	public Part CreateStyleDefinitionsPartObject(String partName) throws InvalidFormatException {
		return new StyleDefinitionsPart(new PartName(partName));
	}

	public Part CreateDocumentSettingsPartObject(String partName)
			throws InvalidFormatException {
		return new DocumentSettingsPart(new PartName(partName));
	}

	public Part CreateWebSettingsPartObject(String partName)
			throws InvalidFormatException {
		return new WebSettingsPart(new PartName(partName));
	}

	public Part CreateFontTablePartObject(String partName)
			throws InvalidFormatException {
		return new FontTablePart(new PartName(partName));
	}

	public Part CreateThemePartObject(String partName)
			throws InvalidFormatException {
		return new ThemePart(new PartName(partName));
	}

	public Part CreateDocPropsCorePartObject(String partName)
			throws InvalidFormatException {
		return new DocPropsCorePart(new PartName(partName));
	}

	public Part CreateDocPropsExtendedPartObject(String partName)
			throws InvalidFormatException {
		return new DocPropsExtendedPart(new PartName(partName));
	}

	public Part CreateDocPropsCustomPartObject(String partName)
			throws InvalidFormatException {
		log.info("Using DocPropsCustomPart ...");		
		return new DocPropsCustomPart(new PartName(partName));
	}
	
	public Part CreateCommentsPartObject(String partName)
			throws InvalidFormatException {
		return new CommentsPart(new PartName(partName));
	}

	public Part CreateCustomXmlPropertiesPartObject(String partName)
			throws InvalidFormatException {
		return new CustomXmlPropertiesPart(new PartName(partName));
	}

	public Part CreateEndnotesPartObject(String partName)
			throws InvalidFormatException {
		return new EndnotesPart(new PartName(partName));
	}

	public Part CreateFooterPartObject(String partName)
			throws InvalidFormatException {
		return new FooterPart(new PartName(partName));
	}

	public Part CreateFootnotesPartObject(String partName)
			throws InvalidFormatException {
		return new FootnotesPart(new PartName(partName));
	}

	public Part CreateGlossaryDocumentPartObject(String partName)
			throws InvalidFormatException {
		return new GlossaryDocumentPart(new PartName(partName));
	}

	public Part CreateHeaderPartObject(String partName)
			throws InvalidFormatException {
		return new HeaderPart(new PartName(partName));
	}

	public Part CreateNumberingPartObject(String partName)
			throws InvalidFormatException {
		return new NumberingDefinitionsPart(new PartName(partName));
	}
	
	
	
	/**
	 * Add a content type associated with the specified extension.
	 * 
	 * @param extension
	 *            The part name extension to bind to a content type.
	 * @param contentType
	 *            The content type associated with the specified extension.
	 */
	public void addDefaultContentType(String extension, String contentType) {
		log.debug("Registered " + extension );
		defaultContentType.put(extension, contentType);
	}

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
	public void removeContentType(PartName partName) {
		if (partName == null)
			throw new IllegalArgumentException("partName");
		
		// Override content type
		if (this.overrideContentType != null
				&& (this.overrideContentType.get(partName.getURI()) != null)) {
			this.overrideContentType.remove(partName.getURI());
			return;
		}
		// Default content type
		this.defaultContentType.remove(partName.getExtension());
	}

	/**
	 * Check if the specified content type is already register.
	 * 
	 * @param contentType
	 *            The content type to check.
	 * @return <code>true</code> if the specified content type is already
	 *         register, then <code>false</code>.
	 */
	public boolean isContentTypeRegistered(String contentType) {
		if (contentType == null)
			throw new IllegalArgumentException("contentType");

		return (this.defaultContentType.values().contains(contentType) || (this.overrideContentType != null && this.overrideContentType
				.values().contains(contentType)));
	}

	/**
	 * Get the content type for the specified part, if any.
	 * 
	 * @param partUri
	 *            The URI part to check.
	 * @return The content type associated with the URI (in case of an override
	 *         content type) or the extension (in case of default content type),
	 *         else <code>null</code>.
	 */
	public String getContentType(PartName partName) {
		if (partName == null)
			throw new IllegalArgumentException("partName");

		if ((this.overrideContentType != null)
				&& this.overrideContentType.containsKey(partName.getURI()))
			return this.overrideContentType.get(partName.getURI());

		String extension = partName.getExtension();
		if (this.defaultContentType.containsKey(extension))
			return this.defaultContentType.get(extension);

		return null;
	}

	/**
	 * Clear all content types.
	 */
	public void clearAll() {
		this.defaultContentType.clear();
		if (this.overrideContentType != null)
			this.overrideContentType.clear();
	}

	/**
	 * Clear all override content types.
	 * 
	 */
	public void clearOverrideContentTypes() {
		if (this.overrideContentType != null)
			this.overrideContentType.clear();
	}

	/**
	 * Parse the content types part.
	 * 
	 * @throws InvalidFormatException
	 *             Throws if the content type doesn't exist or the XML format is
	 *             invalid.
	 */
	public void parseContentTypesFile(Document xmlContentTypeDoc)
			throws InvalidFormatException {
		//log.info("parseContentTypesFile");
		try {

			// Default content types
			List defaultTypes = xmlContentTypeDoc.getRootElement().elements(
					DEFAULT_TAG_NAME);
			Iterator elementIteratorDefault = defaultTypes.iterator();
			while (elementIteratorDefault.hasNext()) {
				Element element = (Element) elementIteratorDefault.next();
				String extension = element.attribute(EXTENSION_ATTRIBUTE_NAME)
						.getValue();
				//log.info("found " + DEFAULT_TAG_NAME + extension);
				String contentType = element.attribute(
						CONTENT_TYPE_ATTRIBUTE_NAME).getValue();
				addDefaultContentType(extension, contentType);
			}

			// Overriden content types
			List overrideTypes = xmlContentTypeDoc.getRootElement().elements(
					OVERRIDE_TAG_NAME);
			Iterator elementIteratorOverride = overrideTypes.iterator();
			while (elementIteratorOverride.hasNext()) {
				Element element = (Element) elementIteratorOverride.next();
				URI uri = new URI(element.attribute(PART_NAME_ATTRIBUTE_NAME)
						.getValue());
				String contentType = element.attribute(
						CONTENT_TYPE_ATTRIBUTE_NAME).getValue();
				addOverrideContentType(uri, contentType);
			}
		} catch (URISyntaxException urie) {
			throw new InvalidFormatException(urie.getMessage());
		}
	}

	/**
	 * Generates the XML for the contents type part.
	 * 
	 * @param outStream
	 *            The output stream use to save the XML content of the content
	 *            types part.
	 * @return <b>true</b> if the operation success, else <b>false</b>.
	 */
	public Document getDocument() {
		Document xmlOutDoc = DocumentHelper.createDocument();

		// Building namespace
		Namespace dfNs = Namespace.get("", TYPES_NAMESPACE_URI);
		Element typesElem = xmlOutDoc
				.addElement(new QName(TYPES_TAG_NAME, dfNs));

		// Adding default types
		for (Entry<String, String> entry : defaultContentType.entrySet()) {
			appendDefaultType(typesElem, entry);
		}

		// Adding specific types if any exist
		if (overrideContentType != null) {
			for (Entry<URI, String> entry : overrideContentType.entrySet()) {
				appendSpecificTypes(typesElem, entry);
			}
		}
		xmlOutDoc.normalize();

		return xmlOutDoc;
	}

	/**
	 * Use to append specific type XML elements, use by the save() method.
	 * 
	 * @param root
	 *            XML parent element use to append this override type element.
	 * @param entry
	 *            The values to append.
	 * @see #save(ZipOutputStream)
	 */
	private void appendSpecificTypes(Element root, Entry<URI, String> entry) {
		root.addElement(OVERRIDE_TAG_NAME).addAttribute(
				PART_NAME_ATTRIBUTE_NAME, ((URI) entry.getKey()).getPath())
				.addAttribute(CONTENT_TYPE_ATTRIBUTE_NAME,
						(String) entry.getValue());
	}

	/**
	 * Use to append default types XML elements, use by the save() metid.
	 * 
	 * @param root
	 *            XML parent element use to append this default type element.
	 * @param entry
	 *            The values to append.
	 * @see #save(ZipOutputStream)
	 */
	private void appendDefaultType(Element root, Entry<String, String> entry) {
		root.addElement(DEFAULT_TAG_NAME).addAttribute(
				EXTENSION_ATTRIBUTE_NAME, (String) entry.getKey())
				.addAttribute(CONTENT_TYPE_ATTRIBUTE_NAME,
						(String) entry.getValue());

	}

	/* Return a package of the appropriate type.  Used when loading an existing
	 * Package, with an already populated [Content_Types].xml.  When 
	 * creating a new Package, start with the new WordprocessingMLPackage constructor. */
	public Package createPackage() throws InvalidFormatException {
		
		/* How do we know what type of Package this is?
		 * 
		 * In principle, either:
		 * 
		 * 1. We were told its file extension or mime type in the constructor/method parameters, or
		 * 
		 * 2. Because [Content_Types].xml contains an override for PartName /document.xml 
		 *    of content type application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml
		 *    
		 * The latter approach is more reliable, so ..
		 * 
		 */
//		debugPrint(ctmDocument);
		Package p;
		
		java.net.URI partURI = null;
		partURI = getPartNameOverridenByContentType(ContentTypes.WORDPROCESSINGML_DOCUMENT);
		if ( partURI == null ) {
			log.warn("No part in [Content_Types].xml for content type" + ContentTypes.WORDPROCESSINGML_DOCUMENT);
			// TODO - what content type in this case?
			return new Package(this);
		} else {
			log.info("Detected WordProcessingML package, at " + partURI.toString() );			
			p = new WordprocessingMLPackage(this);
			return p;
		}
	}

	/*
	 * Gets the content type from an extension.
	 
	public static String getContentTypeFromExtension(String extension) {
		if ((extension.equals(ContentTypes.EXTENSION_JPG_1))
				|| (extension.equals(ContentTypes.EXTENSION_JPG_2))) {
			return ContentTypes.IMAGE_JPEG;
		}
		if (extension.equals(ContentTypes.EXTENSION_PNG)) {
			return ContentTypes.IMAGE_PNG;
		}
		if (extension.equals(ContentTypes.EXTENSION_GIF)) {
			return ContentTypes.IMAGE_GIF;
		}
		if (extension.equals(ContentTypes.EXTENSION_TIFF)) {
			return ContentTypes.IMAGE_TIFF;
		}
		if (extension.equals(ContentTypes.EXTENSION_PICT)) {
			return ContentTypes.IMAGE_PICT;
		}
		return null;
	}
*/
	
}