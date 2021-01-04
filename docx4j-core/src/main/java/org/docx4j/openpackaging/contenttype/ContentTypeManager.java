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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.packages.DefaultPackage;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLTemplatePackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.docx4j.openpackaging.parts.DefaultXmlPart;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;
import org.docx4j.openpackaging.parts.DocPropsExtendedPart;
import org.docx4j.openpackaging.parts.InkmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.TrueTypeFontPart;
import org.docx4j.openpackaging.parts.VMLPart;
import org.docx4j.openpackaging.parts.DrawingML.JaxbDmlPart;
import org.docx4j.openpackaging.parts.PresentationML.FontDataPart;
import org.docx4j.openpackaging.parts.PresentationML.JaxbPmlPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.JaxbSmlPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsExtendedPart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsIdsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.GlossaryDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.KeyMapCustomizationsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileEmfPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileWmfPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart;
import org.docx4j.openpackaging.parts.WordprocessingML.OleObjectBinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.PeoplePart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.VbaDataPart;
import org.docx4j.openpackaging.parts.WordprocessingML.VbaProjectBinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.VbaProjectSignatureBin;
import org.docx4j.openpackaging.parts.WordprocessingML.WebSettingsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.webextensions.TaskpanesPart;
import org.docx4j.openpackaging.parts.webextensions.WebExtensionPart;
import org.docx4j.relationships.Relationship;
import org.glox4j.openpackaging.packages.GloxPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manage package content types ([Content_Types].xml ) .
 * 
 * @author Julien Chable
 * @version 1.0
 */
public class ContentTypeManager  {
	
	protected static Logger log = LoggerFactory.getLogger(ContentTypeManager.class);
	
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
	private TreeMap<String, CTDefault> defaultContentType;

	/**
	 * @return the defaultContentType
	 * @since 2.8.1
	 */
	public TreeMap<String, CTDefault> getDefaultContentType() {
		return defaultContentType;
	}

	/**
	 * Override content type tree.
	 */
	private TreeMap<URI, CTOverride> overrideContentType;
	
	/**
	 * @return the overrideContentType
	 * @since 2.8.1
	 */
	public TreeMap<URI, CTOverride> getOverrideContentType() {
		return overrideContentType;
	}

	private static ObjectFactory ctFactory = new ObjectFactory();

	public ContentTypeManager()  {
		init();
	}
	
	private void init() {
		defaultContentType = new TreeMap<String, CTDefault>();
		overrideContentType = new TreeMap<URI, CTOverride>();
	}

//	/**
//	 * Build association extention-> content type (will be stored in
//	 * [Content_Types].xml) for example ContentType="image/png" Extension="png"
//	 * 
//	 * @param partUri
//	 *            the uri that will be stored
//	 * @return <b>false</b> if an error occured.
//	 */
//	public void addContentType(PartName partName, String contentType) {
//		boolean defaultCTExists = false;
//		String extension = partName.getExtension();
//		if ((extension.length() == 0)
//				|| (this.defaultContentType.containsKey(extension) 
//						&& !(defaultCTExists = this.defaultContentType.containsValue(contentType)))) {
//			this.addOverrideContentType(partName.getURI(), contentType);
//		} else if (!defaultCTExists) {
//			this.addDefaultContentType(extension, contentType);
//		}
//	}

	/**
	 * Add an override content type for a specific part.
	 * 
	 * @param partUri
	 *            Uri of the part.
	 * @param contentType
	 *            Content type of the part.
	 */
	public void addOverrideContentType(URI partUri, CTOverride contentType) {
		log.debug("Registered " + partUri.toString() + " of type " + contentType.getContentType() );
		overrideContentType.put(partUri, contentType);
	}
	
	public void addOverrideContentType(URI partUri, String contentType) {

		CTOverride overrideCT = ctFactory.createCTOverride();
		overrideCT.setPartName( partUri.toASCIIString() );
		overrideCT.setContentType(contentType );
		
		overrideContentType.put(partUri, overrideCT);
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
				if (log.isDebugEnabled()) {
					log.debug("Inspecting " + e.getValue());
				}
				if ( ((CTOverride)e.getValue()).getContentType().equals(contentType) ) {
//					log.debug("Matched!");
					return (URI)e.getKey(); 
				}
			} 
		} 		
		return null;
		
	}
	
	/* Return a part of the appropriate sub class */
	public  Part getPart(String partName, Relationship rel) throws URISyntaxException, PartUnrecognisedException,
	 InvalidFormatException {
		
		Part p = null;

		// look for an override
		CTOverride overrideCT = (CTOverride) overrideContentType.get(new URI(partName));
		if (overrideCT!=null ) {
			String contentType = new String(overrideCT.getContentType()); 
			if (log.isDebugEnabled()) {
				log.debug("Found content type '" + contentType + "' for " + partName);
			}
			try {
				p = newPartForContentType(contentType, partName, rel);
			} catch (NoClassDefFoundError noClass) {
				log.error(noClass.getMessage(), noClass);
				throw noClass;
			}
			p.setContentType(new ContentType(contentType));
			return p;
		}		
		
		// if there is no override, get use the file extension
		String ext = new String(partName.substring(partName.indexOf(".") + 1).toLowerCase());
		log.debug("Looking at extension '" + ext);
		CTDefault defaultCT = (CTDefault)defaultContentType.get(ext);
		if (defaultCT!=null ) {
			String contentType = defaultCT.getContentType();
			if (log.isDebugEnabled()) {			
				log.debug("Found content type '" + contentType + "' for "
								+ partName);
			}
			try {
				p = newPartForContentType(contentType, partName, rel);
			} catch (NoClassDefFoundError noClass) {
				log.error(noClass.getMessage(), noClass);
				throw noClass;
			}
			p.setContentType(new ContentType(contentType));
			return p;
		}
		
		// otherwise
		log.error("No content type found for " + partName);
		return null;		
		
	}
	
	public Part newPartForContentType(String contentType, String partName, Relationship rel)
		throws InvalidFormatException, PartUnrecognisedException {
						
		// TODO - a number of WordML parts aren't listed here!
		if (rel!=null && rel.getType().equals(Namespaces.AF) ) {
			// Could have just passed String relType
			// Null where used from BPAI, and a FlatOpcXmlImporter case.
			// Cases where rel is not available can prepare a suitable dummy
			
			AlternativeFormatInputPart afip = 
				new AlternativeFormatInputPart(new PartName(partName) );
			afip.setContentType(new ContentType(contentType));
			return afip;
			
		} else if (rel!=null && rel.getType().equals(Namespaces.EMBEDDED_PKG) ) {
			
			EmbeddedPackagePart epp = new EmbeddedPackagePart(new PartName(partName) );
			epp.setContentType(new ContentType(contentType));
			return epp;

		} else if (rel!=null && rel.getType().equals(Namespaces.OLE_OBJECT) ) {
			
			OleObjectBinaryPart olePart = new OleObjectBinaryPart(new PartName(partName));
			olePart.setContentType(new ContentType(contentType));
			return olePart;
			
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_DOCUMENT)) { 
			return CreateMainDocumentPartObject(partName);
			// how is the main document distinguished from the glossary document?
			// Answer:- Main Document is a Package level relationship target,
			// whereas the Glossary Document is a Part-level target (from the
			// Main Document part)						
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_DOCUMENT_MACROENABLED)) {
			return CreateMainDocumentPartObject(partName);
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_TEMPLATE)) {
			return CreateMainDocumentPartObject(partName);
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_TEMPLATE_MACROENABLED)) {
			return CreateMainDocumentPartObject(partName);
		} else if (contentType.equals(ContentTypes.PACKAGE_COREPROPERTIES)) {
			return CreateDocPropsCorePartObject(partName ); 
		} else if (contentType.equals(ContentTypes.OFFICEDOCUMENT_CUSTOMPROPERTIES)) {
			return CreateDocPropsCustomPartObject(partName );
		} else if (contentType.equals(ContentTypes.OFFICEDOCUMENT_EXTENDEDPROPERTIES)) {
			return CreateDocPropsExtendedPartObject(partName );
			
		} else if (contentType.equals(ContentTypes.OFFICEDOCUMENT_CUSTOMXML_DATASTORAGE)) {
			
			// NB, since this is just "application/xml", it 
			return new org.docx4j.openpackaging.parts.CustomXmlDataStoragePart(new PartName(partName));
			
		} else if (contentType.equals(ContentTypes.OFFICEDOCUMENT_CUSTOMXML_DATASTORAGEPROPERTIES)) {
			return CreateCustomXmlDataStoragePropertiesPartObject(partName );			
		} else if (contentType.equals(ContentTypes.OFFICEDOCUMENT_FONT)) {
			return CreateObfuscatedFontPartObject(partName );
		} else if (contentType.equals(ContentTypes.TRUETYPE_FONT)) {
			return  new TrueTypeFontPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.OFFICEDOCUMENT_OLE_OBJECT)
				|| contentType.equals(ContentTypes.OFFICEDOCUMENT_ACTIVEX_OBJECT)) {
			return new org.docx4j.openpackaging.parts.WordprocessingML.OleObjectBinaryPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.OFFICEDOCUMENT_ACTIVEX_XML_OBJECT)) {
			return new org.docx4j.openpackaging.parts.ActiveXControlXmlPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.OFFICEDOCUMENT_THEME)) {
			return CreateThemePartObject(partName );
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_COMMENTS)) {
			return CreateCommentsPartObject(partName );
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_COMMENTS_EXTENDED)) {
			return new CommentsExtendedPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_COMMENTS_IDS)) {
			return new CommentsIdsPart(new PartName(partName));
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
		} else if (contentType.equals(ContentTypes.OFFICEDOCUMENT_VBA_DATA)) {
			return new VbaDataPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.OFFICEDOCUMENT_VBA_PROJECT)) {
			return new VbaProjectBinaryPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.OFFICEDOCUMENT_VBA_PROJECT_SIGNATURE)) {
			return new VbaProjectSignatureBin(new PartName(partName));
		} else if (contentType.equals(ContentTypes.IMAGE_JPEG)) {
			return new org.docx4j.openpackaging.parts.WordprocessingML.ImageJpegPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.IMAGE_PNG)) {
			return new org.docx4j.openpackaging.parts.WordprocessingML.ImagePngPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.IMAGE_GIF)) {
			return new org.docx4j.openpackaging.parts.WordprocessingML.ImageGifPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.IMAGE_TIFF)) {
			return new org.docx4j.openpackaging.parts.WordprocessingML.ImageTiffPart(new PartName(partName));
//		} else if (contentType.equals(ContentTypes.IMAGE_EPS)) {
//			return new org.docx4j.openpackaging.parts.WordprocessingML.ImageEpsPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.IMAGE_BMP)) {
			return new org.docx4j.openpackaging.parts.WordprocessingML.ImageBmpPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.IMAGE_EMF) || contentType.equals(ContentTypes.IMAGE_EMF2)) {
			return new MetafileEmfPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.IMAGE_WMF)) {
			return new MetafileWmfPart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.WORDPROCESSINGML_PEOPLE)) {
			return new PeoplePart(new PartName(partName));
		} else if (contentType.equals(ContentTypes.VML_DRAWING)) {
			return new VMLPart(new PartName(partName));
//			return new VMLBinaryPart(new PartName(partName));				
		} else if (contentType.equals(ContentTypes.DRAWINGML_DIAGRAM_DRAWING)) {
			return new org.docx4j.openpackaging.parts.DrawingML.DiagramDrawingPart(new PartName(partName));
		} else if (contentType.startsWith("application/vnd.openxmlformats-officedocument.drawing")) {
			try {
				return JaxbDmlPart.newPartForContentType(contentType, partName);
			} catch (Exception e) {
				return new BinaryPart( new PartName(partName));				
			}
		} else if (contentType.startsWith("application/vnd.openxmlformats-officedocument.presentation")
				|| contentType.equals(ContentTypes.PRESENTATIONML_MACROENABLED)
				|| contentType.equals(ContentTypes.PRESENTATIONML_TEMPLATE)
				|| contentType.equals(ContentTypes.PRESENTATIONML_TEMPLATE_MACROENABLED)
						) {
			try {
				return JaxbPmlPart.newPartForContentType(contentType, partName);
			} catch (Exception e) {
				return new BinaryPart( new PartName(partName));				
			}
		} else if (contentType.equals(ContentTypes.SPREADSHEETML_WORKBOOK)
				|| contentType.equals(ContentTypes.SPREADSHEETML_WORKBOOK_MACROENABLED)
				|| contentType.equals(ContentTypes.SPREADSHEETML_TEMPLATE)
				|| contentType.equals(ContentTypes.SPREADSHEETML_TEMPLATE_MACROENABLED)) { 
			try {
				return new WorkbookPart(new PartName(partName));
			} catch (Exception e) {
				return new BinaryPart( new PartName(partName));				
			}
			
		} else if (contentType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml")) {
			try {
				return JaxbSmlPart.newPartForContentType(contentType, partName);
			} catch (Exception e) {
				return new BinaryPart( new PartName(partName));				
			}
		} else if (contentType.equals(ContentTypes.OFFICEDOCUMENT_THEME_OVERRIDE)) {
			return new org.docx4j.openpackaging.parts.DrawingML.ThemeOverridePart(new PartName(partName));	
			
		} else if (contentType.equals(ContentTypes.DIGITAL_SIGNATURE_XML_SIGNATURE_PART)) {
//			return new org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart(new PartName(partName));
			
			// Use reflection, since the dig sig functionality is in Enterprise only
			try {
				Class<?> xmlSignaturePart = Class.forName("org.docx4j.openpackaging.parts.digitalsignature.XmlSignaturePart");
				Constructor<?> cons = xmlSignaturePart.getConstructor(PartName.class);
				PartName pn = new PartName(partName);
				return (Part) cons.newInstance(pn);
			} catch (Exception e) {
				return CreateDefaultXmlPartObject(partName );				
			}			
			
		} else if (contentType.equals(ContentTypes.DIGITAL_SIGNATURE_ORIGIN_PART)) {
//			return new org.docx4j.openpackaging.parts.digitalsignature.SignatureOriginPart(new PartName(partName));
			
			// Use reflection, since the dig sig functionality is in Enterprise only
			try {
				Class<?> originPart = Class.forName("org.docx4j.openpackaging.parts.digitalsignature.SignatureOriginPart");
				Constructor<?> cons = originPart.getConstructor(PartName.class);
				PartName pn = new PartName(partName);
				return (Part) cons.newInstance(pn);
			} catch (Exception e) {
				return new BinaryPart( new PartName(partName));				
			}			
			
		} else if (contentType.equals(ContentTypes.WEB_EXTENSION_TASKPANES)) {
			return new TaskpanesPart(new PartName(partName));

		} else if (contentType.equals(ContentTypes.WEB_EXTENSION_WEBEXTENSION)) {
			return new WebExtensionPart(new PartName(partName));
			
		} else if (contentType.equals(ContentTypes.INK_ML)) {
			return new InkmlPart(new PartName(partName));

		} else if (contentType.equals(ContentTypes.MS_WORD_KEYMAP)) {
			return new KeyMapCustomizationsPart(new PartName(partName));
			
		} else if (contentType.equals(ContentTypes.APPLICATION_XML)
				|| contentType.equals(ContentTypes.IMAGE_SVG)				
				|| partName.endsWith(".xml")) {
			
			// Rarely (but sometimes) used, owing to OFFICEDOCUMENT_CUSTOMXML_DATASTORAGE above.
			
			// Simple minded detection of XML content.
			// If it turns out not to be XML, the zip loader
			// will catch the error and load it as a binary part instead.
			
			// For SVG, we could unmarshall with our jaxb-svg, but probably little reason to do that.
			
			if (contentType.equals(ContentTypes.WORDPROCESSINGML_STYLESWITHEFFECTS)) {
				log.debug("DefaultPart used for part '" + partName + "' of content type '" + contentType + "'");
				
			} else {
				log.warn("DefaultPart used for part '" + partName 
						+ "' of content type '" + contentType + "'");				
			}
			
			return CreateDefaultXmlPartObject(partName );
			
		} else if (contentType.equals(ContentTypes.PRESENTATIONML_FONT_DATA)) {
			
			return new FontDataPart(new PartName(partName));
			
		} else {
			
			log.error("No subclass found for " + partName + "; defaulting to binary");
			//throw new PartUnrecognisedException("No subclass found for " + partName + " (content type '" + contentType + "')");		
			return new BinaryPart( new PartName(partName));
		}

	}

	public Part CreateDefaultXmlPartObject(String partName)
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

	public Part CreateCustomXmlDataStoragePropertiesPartObject(String partName)
			throws InvalidFormatException {
		return new CustomXmlDataStoragePropertiesPart(new PartName(partName));
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
		
	public Part CreateObfuscatedFontPartObject(String partName)
			throws InvalidFormatException {
		return new ObfuscatedFontPart(new PartName(partName));
	}
	
	/**
	 * Add a content type associated with the specified extension.
	 * 
	 * @param extension
	 *            The part name extension to bind to a content type.
	 * @param contentType
	 *            The content type associated with the specified extension.
	 */
	public void addDefaultContentType(String extension, CTDefault contentType) {
		log.debug("Registered " + extension );
		defaultContentType.put(extension.toLowerCase(), contentType);
	}
	
	public void addDefaultContentType(String extension, String contentType) {
		
		CTDefault defaultCT = ctFactory.createCTDefault();
		defaultCT.setExtension(extension.toLowerCase());
		defaultCT.setContentType(contentType);
		
		log.debug("Registered " + extension );
		defaultContentType.put(extension.toLowerCase(), defaultCT);
	}
	
	/**
	 * @param partName
	 * @param contentType
	 * @since 6.1.0
	 */
	public void addOverrideContentType(PartName partName, String contentType) {
		
		CTOverride defaultCT = ctFactory.createCTOverride();
		defaultCT.setPartName(partName.getName());
		defaultCT.setContentType(contentType);
		
		overrideContentType.put(partName.getURI(), defaultCT);
	}

	/**
	 * Delete a content type based on the specified part name. If the specified
	 * part name is registered with an override content type, then this content
	 * type is removed, else the content type is removed in the default content
	 * type list if it exists.  Deprecated since you'd typically NOT want to
	 * accidentally remove *.xml or *.rels from the default Content type list 
	 * 
	 * @param partUri
	 *            The part URI associated with the override content type to
	 *            delete.
	 */
	@Deprecated
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
		this.defaultContentType.remove(partName.getExtension().toLowerCase());
	}

	/**
	 * Delete an override content type based on the specified part name. 
	 * 
	 * @param partUri
	 *            The part URI associated with the override content type to
	 *            delete.
	 * @since 6.1.0
	 */
	public void removeOverrideContentType(PartName partName) {
		if (partName == null)
			throw new IllegalArgumentException("partName");
		
		// Override content type
		if (this.overrideContentType != null
				&& (this.overrideContentType.get(partName.getURI()) != null)) {
			this.overrideContentType.remove(partName.getURI());
			return;
		}
	}

	/**
	 * Delete a default content type based on the specified part name. Note 
	 * that you'd typically NOT want to remove *.xml or *.rels from the default
	 * Content type list 
	 * 
	 * @param partUri
	 *            The part URI associated with the override content type to
	 *            delete.
	 * @since 6.1.0
	 */
	public void removeDefaultContentType(String ext) {

		// Default content type
		this.defaultContentType.remove(ext.toLowerCase());
	}
	
	/**
	 * Check if the specified content type is already registered
	 * as a default content type.  We don't currently have a method
	 * to check whether its registered as an override content type;
	 * getContentType(PartName partName) may suffice for that purpose.
	 * 
	 * @param contentType
	 *            The content type to check.
	 * @return <code>true</code> if the specified content type is already
	 *         registered, then <code>false</code>.
	 */
	public boolean isContentTypeRegistered(String contentType) {
		if (contentType == null)
			throw new IllegalArgumentException("contentType");

		return this.defaultContentType.values().contains(contentType); 
//				|| (this.overrideContentType != null 
//						&& this.overrideContentType.values().contains(contentType)));
	}

	/**
	 * Check if the specified content type is already registered
	 * as a default content type.  We don't currently have a method
	 * to check whether its registered as an override content type;
	 * getContentType(PartName partName) may suffice for that purpose.
	 * 
	 * @param contentType
	 *            The content type to check.
	 * @return <code>true</code> if the specified content type is already
	 *         registered, then <code>false</code>.
	 * @since 6.1.0
	 */
	public boolean isOverrideContentTypeRegistered(String contentType) {
		if (contentType == null)
			throw new IllegalArgumentException("contentType");

		return (this.overrideContentType != null 
				&& this.overrideContentType.values().contains(contentType));
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
			return this.overrideContentType.get(partName.getURI()).getContentType();

		String extension = partName.getExtension().toLowerCase();
		if (this.defaultContentType.containsKey(extension))
			return this.defaultContentType.get(extension).getContentType();

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

	
	public void parseContentTypesFile(InputStream contentTypes) 
		throws InvalidFormatException {
		
		CTTypes types;
		
		try {
	        XMLInputFactory xif = XMLInputFactory.newInstance();
	        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
	        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false); // a DTD is merely ignored, its presence doesn't cause an exception
	        XMLStreamReader xsr = xif.createXMLStreamReader(contentTypes);			
	        
			Unmarshaller u = Context.jcContentTypes.createUnmarshaller();
			
			//u.setSchema(org.docx4j.jaxb.WmlSchema.schema);
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			log.debug("unmarshalling " + this.getClass().getName() );		
			
			Object res = XmlUtils.unwrap(u.unmarshal( xsr ));
			//types = (CTTypes)((JAXBElement)res).getValue();				
			types = (CTTypes)res;
			//log.debug( types.getClass().getName() + " unmarshalled" );
			
			if (log.isDebugEnabled()) {
				XmlUtils.marshaltoString(res, true, true, Context.jcContentTypes );
			}

			CTDefault defaultCT;
			CTOverride overrideCT;
			for(Object o : types.getDefaultOrOverride() ) {
				
				if (o instanceof CTDefault) {
					defaultCT = (CTDefault)o;
					addDefaultContentType( defaultCT.getExtension(), defaultCT  );
				}
				
				if (o instanceof CTOverride) {
					overrideCT = (CTOverride)o;
					URI uri = new URI(overrideCT.getPartName() );
					addOverrideContentType(uri, overrideCT );
				}
			}
			
		} catch (Exception e ) {
			throw new InvalidFormatException("Bad [Content_Types].xml", e);
		}
		
		
	}

	private CTTypes buildTypes() {
		
		// Build the JAXB object
		ObjectFactory factory = new ObjectFactory();
		CTTypes types = factory.createCTTypes();

		for (Entry<String, CTDefault> entry : defaultContentType.entrySet()) {
			types.getDefaultOrOverride().add(entry.getValue());
		}

		if (overrideContentType != null) {
			for (Entry<URI, CTOverride> entry : overrideContentType.entrySet()) {
				types.getDefaultOrOverride().add(entry.getValue());
			}
		}	
		return types;
	}
	
//	public void listTypes() {
//		
//
//		for (Entry<String, CTDefault> entry : defaultContentType.entrySet()) {
//			
//			System.out.println("// " + entry.getValue().getExtension());
//			System.out.println("public final static String XX =" );
//			System.out.println(entry.getValue().getContentType());
//		}
//
//		if (overrideContentType != null) {
//			for (Entry<URI, CTOverride> entry : overrideContentType.entrySet()) {
//				System.out.println("// " + entry.getValue().getPartName());
//				System.out.println("public final static String XX =" );
//				System.out.println("\"" + entry.getValue().getContentType() + "\";");
//			}
//		}	
//	}
	
    public void marshal(org.w3c.dom.Node node) throws JAXBException {
		
		try {
			Marshaller marshaller = Context.jcContentTypes.createMarshaller();
			
			NamespacePrefixMapperUtils.setProperty(marshaller, 
					NamespacePrefixMapperUtils.getPrefixMapper() );
			
			log.debug("marshalling " + this.getClass().getName() + " ..." );									
			
			marshaller.marshal(buildTypes(), node);
			
			log.debug("content types marshalled \n\n" );									

		} catch (JAXBException e) {
			//e.printStackTrace();
			log.error(e.getMessage(), e);
			throw e;
		}
    }
    
    public void marshal(java.io.OutputStream os) throws JAXBException {
		
		try {
			Marshaller marshaller = Context.jcContentTypes.createMarshaller();
			
			NamespacePrefixMapperUtils.setProperty(marshaller, 
					NamespacePrefixMapperUtils.getPrefixMapper() );
			
			log.debug("marshalling " + this.getClass().getName() + " ..." );									
			marshaller.marshal(buildTypes(), os);

		} catch (JAXBException e) {
			//e.printStackTrace();
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	

	/** Return a package of the appropriate type.  Used when loading an existing
	 *  Package, with an already populated [Content_Types].xml.  When 
	 *  creating a new Package, start with the new WordprocessingMLPackage constructor. */
	public OpcPackage createPackage(String pkgContentType) throws InvalidFormatException {
		
		OpcPackage p;
		
		// Check overrides first
		if (pkgContentType.equals(ContentTypes.WORDPROCESSINGML_DOCUMENT) 
				|| pkgContentType.equals(ContentTypes.WORDPROCESSINGML_DOCUMENT_MACROENABLED) ) {
			
			log.info("Detected WordProcessingML package ");
			p = new WordprocessingMLPackage(this);
			p.setContentType(new ContentType(pkgContentType));			
			return p;
			
		} else if (pkgContentType.equals(ContentTypes.WORDPROCESSINGML_TEMPLATE ) 
				|| pkgContentType.equals(ContentTypes.WORDPROCESSINGML_TEMPLATE_MACROENABLED)  ) {
			
			log.info("Detected WordprocessingMLTemplatePackage package ");
			p = new WordprocessingMLTemplatePackage(this);
			p.setContentType(new ContentType(pkgContentType));			
			return p;
			
		} else if (pkgContentType.equals(ContentTypes.PRESENTATIONML_MAIN) 
				|| pkgContentType.equals(ContentTypes.PRESENTATIONML_TEMPLATE) 
				|| pkgContentType.equals(ContentTypes.PRESENTATIONML_TEMPLATE_MACROENABLED) 
				|| pkgContentType.equals(ContentTypes.PRESENTATIONML_MACROENABLED) 
				|| pkgContentType.equals(ContentTypes.PRESENTATIONML_SLIDESHOW) ) {
			log.info("Detected PresentationMLPackage package ");
			p = new PresentationMLPackage(this);
			p.setContentType(new ContentType(pkgContentType));			
			return p;
		} else if (pkgContentType.equals(ContentTypes.SPREADSHEETML_WORKBOOK) 
				|| pkgContentType.equals(ContentTypes.SPREADSHEETML_WORKBOOK_MACROENABLED) 
				|| pkgContentType.equals(ContentTypes.SPREADSHEETML_TEMPLATE) 
				|| pkgContentType.equals(ContentTypes.SPREADSHEETML_TEMPLATE_MACROENABLED) ) {
			//  "xlam", "xlsb" ?
			log.info("Detected SpreadhseetMLPackage package ");
			p = new SpreadsheetMLPackage(this);
			p.setContentType(new ContentType(pkgContentType));			
			return p;			
		} else if (pkgContentType.equals(ContentTypes.DRAWINGML_DIAGRAM_LAYOUT) ) {
			log.info("Detected Glox file ");
			p = new GloxPackage(this);
			p.setContentType(new ContentType(pkgContentType));			
			return p;						
		} 
				
		// Nothing in overrides or defaults
		//throw new InvalidFormatException("Couldn't identify package from " + pkgContentType);
		log.error("Couldn't identify package from " + pkgContentType);
		return new DefaultPackage(this);
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
	
    public boolean isContentEqual(ContentTypeManager other) throws Docx4JException {
    	
    	ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
    	ByteArrayOutputStream baos2 = new ByteArrayOutputStream(); 
    	try {
        	marshal(baos);
			other.marshal(baos2);
		} catch (JAXBException e) {
			throw new Docx4JException("Error marshalling parts", e);
		}
    	
    	return java.util.Arrays.equals(baos.toByteArray(), baos2.toByteArray());

    }
	
    public String toString() {
    	
    	CTTypes types = buildTypes();
    	return XmlUtils.marshaltoString(types, true, true, Context.jcContentTypes);
    	
    }
	
}
