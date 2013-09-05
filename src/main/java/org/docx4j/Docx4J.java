/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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
package org.docx4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;

import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.common.Exporter;
import org.docx4j.convert.out.common.preprocess.PartialDeepCopy;
import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.convert.out.fo.FOExporterVisitor;
import org.docx4j.convert.out.fo.FOExporterXslt;
import org.docx4j.convert.out.html.HTMLExporterVisitor;
import org.docx4j.convert.out.html.HTMLExporterXslt;
import org.docx4j.customXmlProperties.DatastoreItem;
import org.docx4j.customXmlProperties.SchemaRefs.SchemaRef;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.model.datastorage.BindingHandler;
import org.docx4j.model.datastorage.CustomXmlDataStorage;
import org.docx4j.model.datastorage.OpenDoPEHandler;
import org.docx4j.model.datastorage.RemovalHandler;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.opendope.xpaths.Xpaths.Xpath;
import org.w3c.dom.Document;

/** This is a facade for some typical uses of Docx4J:
 * <ul>
 * <li>Loading a document</li> 
 * <li>Saving a document</li> 
 * <li>Binding xml to content controls in a document</li> 
 * <li>Exporting the document (to HTML, or PDF and other formats supported by the FO renderer) </li> 
 * </ul>
 * 
 */
public class Docx4J {
	public static final String MIME_PDF = FOSettings.MIME_PDF;
	public static final String MIME_FO = FOSettings.INTERNAL_FO_MIME;
	
	/** No flags passed, do the default
	 */
	public static final int FLAG_NONE = 0;
	
	/** If avaiable export the document using a xsl transformation
	 */
	public static final int FLAG_EXPORT_PREFER_XSL = 1;
	
	/** If avaiable export the document using a visitor
	 */
	public static final int FLAG_EXPORT_PREFER_NONXSL = 2;

	/** Save the document in a zip container (default docx)
	 */
	public static final int FLAG_SAVE_ZIP_FILE = 1;
	
	/** Save the document as a flat xml document
	 */
	public static final int FLAG_SAVE_FLAT_XML = 2;

	/** inject the passed xml into the document
	 *  if you don't do this step, then the xml in 
	 *  the document will be used.
	 */
	public static final int FLAG_BIND_INSERT_XML = 1;
	
	/** Insert the data of the xml in the content controls 
	 *  Not needed, if the document will only be opened in word 
	 *  and not converted to other formats.
	 */
	public static final int FLAG_BIND_BIND_XML = 2;
	
	/** Remove the content controls of the document
	 */
	public static final int FLAG_BIND_REMOVE_SDT = 4;
	
	/** Remove any xml parts from the document that 
	 *  are used with the content controls.
	 */
	public static final int FLAG_BIND_REMOVE_XML = 8;
	
	protected static class FindContentControlsVisitor extends TraversalUtilVisitor<SdtElement> {
		public static class BreakException extends RuntimeException {
		}
		
		protected Set<String> definedStoreItemIds = null;
		protected String storeItemId = null;
		public FindContentControlsVisitor(Set<String> definedStoreItemIds) {
			this.definedStoreItemIds = definedStoreItemIds;
		}
		
		@Override
		public void apply(SdtElement element) {
		SdtPr sdtPr = element.getSdtPr();
			
			if ((sdtPr.getDataBinding() != null) &&
				(sdtPr.getDataBinding().getStoreItemID() != null)) {
				String tmp = sdtPr.getDataBinding().getStoreItemID().toLowerCase();
				if (definedStoreItemIds.contains(tmp)) {
					storeItemId = tmp;
					throw new BreakException();
				}
			}
		}
		
		public String getdefinedStoreItemId() {
			return storeItemId;
		}
	}
	
	protected static final String NS_CONDITIONS = "http://opendope.org/conditions";
	protected static final String NS_XPATHS = "http://opendope.org/xpaths";
	protected static final String NS_QUESTIONS = "http://opendope.org/questions";
	protected static final String NS_COMPONENTS = "http://opendope.org/components";
	protected static final Set<String> PART_TO_REMOVE_SCHEMA_TYPES = new TreeSet<String>();
	
	static {
		PART_TO_REMOVE_SCHEMA_TYPES.add(NS_CONDITIONS);
		PART_TO_REMOVE_SCHEMA_TYPES.add(NS_XPATHS);
		PART_TO_REMOVE_SCHEMA_TYPES.add(NS_QUESTIONS);
		PART_TO_REMOVE_SCHEMA_TYPES.add(NS_COMPONENTS);
	}

	/**
	 *  Load a Docx Document from a File
	 */	
	public static WordprocessingMLPackage load(File inFile) throws Docx4JException {
		return WordprocessingMLPackage.load(inFile);
	}
	
	/**
	 *  Load a Docx Document from an InputStream
	 */	
	public static WordprocessingMLPackage load(InputStream inStream) throws Docx4JException {
		return WordprocessingMLPackage.load(inStream);
	}
	
	/**
	 *  Save a Docx Document to a File
	 */	
	public static void save(WordprocessingMLPackage wmlPackage, File outFile, int flags) throws Docx4JException {
	OutputStream outStream = null;
		try {
			outStream = new FileOutputStream(outFile);
			save(wmlPackage, outStream, flags);
		} catch (FileNotFoundException e) {
			throw new Docx4JException("Exception creating output stream: " + e.getMessage(), e);
		}
		finally {
			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException e) {}
				outStream = null;
			}
		}
	}
	
	/**
	 *  Save a Docx Document to an OutputStream
	 */	
	public static void save(WordprocessingMLPackage wmlPackage, OutputStream outStream, int flags) throws Docx4JException {
		if (flags == FLAG_SAVE_FLAT_XML) {
			JAXBContext jc = Context.jcXmlPackage;
			FlatOpcXmlCreator opcXmlCreator = new FlatOpcXmlCreator(wmlPackage);
			org.docx4j.xmlPackage.Package pkg = opcXmlCreator.get();
			Marshaller marshaller;
			try {
				marshaller = jc.createMarshaller();
				NamespacePrefixMapperUtils.setProperty(marshaller, 
						NamespacePrefixMapperUtils.getPrefixMapper());			
				marshaller.marshal(pkg, outStream);				
			} catch (JAXBException e) {
				throw new Docx4JException("Exception marshalling document for output: " + e.getMessage(), e);
			}
		}
		else {
			SaveToZipFile saver = new SaveToZipFile(wmlPackage);
			saver.save(outStream);
		}
	}
	
	/**
	 *  Bind the content controls of the passed document to the xml.
	 */	
	public static void bind(WordprocessingMLPackage wmlPackage, String xmlDocument, int flags) throws Docx4JException {
	ByteArrayInputStream xmlStream = null;
		try {
			xmlStream = new ByteArrayInputStream(xmlDocument.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			xmlStream = new ByteArrayInputStream(xmlDocument.getBytes());
		}
        bind(wmlPackage, xmlStream, flags);
	}
	
	/**
	 *  Bind the content controls of the passed document to the xml.
	 */	
	public static void bind(WordprocessingMLPackage wmlPackage, InputStream xmlDocument, int flags) throws Docx4JException {
    DocumentBuilder documentBuilder = null; 
    Document xmlDoc = null;
		try {
            documentBuilder = XmlUtils.getDocumentBuilderFactory().newDocumentBuilder(); 
            xmlDoc = documentBuilder.parse(xmlDocument);
		} catch (Exception e) {
			throw new Docx4JException("Problems creating a org.w3c.dom.Document for the passed input stream.", e);
		} 
        bind(wmlPackage, xmlDoc, flags);
	}
	
	/**
	 *  Bind the content controls of the passed document to the xml.
	 */	
	public static void bind(WordprocessingMLPackage wmlPackage, Document xmlDocument, int flags) throws Docx4JException {
	OpenDoPEHandler	openDoPEHandler = null;
	CustomXmlDataStoragePart customXmlDataStoragePart = null;
	RemovalHandler removalHandler = null;
	String xpathStorageItemId = null;

		if (flags == FLAG_NONE) {
			//do everything
			flags = (FLAG_BIND_INSERT_XML |
					 FLAG_BIND_BIND_XML |
					 FLAG_BIND_REMOVE_SDT |
					 FLAG_BIND_REMOVE_XML);
		}
		
		xpathStorageItemId = findXPathStorageItemIdInXPathsPart(wmlPackage);
		if ((xpathStorageItemId == null) && (flags == FLAG_BIND_INSERT_XML)) {
			//If no XPathsPart found and the user only wants to inject the XML 
			//then search for a storageItemId via the content controls.
			//If the user wants to do more, then it won't work as the BindingHandler
			//relies on the XPathsPart
			xpathStorageItemId = findXPathStorageItemIdInContentControls(wmlPackage);
		}
		if (xpathStorageItemId == null) {
			throw new Docx4JException("No xpathStorageItemId found, does the document contain content controls that are bound?");
		}
		
		if ((flags & FLAG_BIND_INSERT_XML) == FLAG_BIND_INSERT_XML) {
			insertXMLData(wmlPackage, xpathStorageItemId, xmlDocument);
		}
		if ((flags & FLAG_BIND_BIND_XML) == FLAG_BIND_BIND_XML) {
			openDoPEHandler = new OpenDoPEHandler(wmlPackage);
			openDoPEHandler.preprocess();
			BindingHandler.applyBindings(wmlPackage);
		}
		if ((flags & FLAG_BIND_REMOVE_SDT) == FLAG_BIND_REMOVE_SDT) {
			removeSDTs(wmlPackage);
		}
		if ((flags & FLAG_BIND_REMOVE_XML) == FLAG_BIND_REMOVE_XML) {
			removeDefinedCustomXmlParts(wmlPackage, xpathStorageItemId);
		}
	}

	protected static void insertXMLData(WordprocessingMLPackage wmlPackage, 
										String storageId, Document xmlDocument) throws Docx4JException {
	CustomXmlDataStorage customXmlDataStorage = null;
	CustomXmlDataStoragePart customXmlDataStoragePart = null;
		customXmlDataStoragePart = (CustomXmlDataStoragePart)wmlPackage.getCustomXmlDataStorageParts().get(storageId.toLowerCase());
		customXmlDataStoragePart.getData().setDocument(xmlDocument);
	}

	protected static String findXPathStorageItemIdInXPathsPart(WordprocessingMLPackage wmlPackage) {
	String ret = null;
	List<Xpath> xpathList = null;
	Xpath xpath = null;
		try {
			if ((wmlPackage.getMainDocumentPart().getXPathsPart() != null) &&
				(wmlPackage.getMainDocumentPart().getXPathsPart().getJaxbElement() != null) &&
				(wmlPackage.getMainDocumentPart().getXPathsPart().getJaxbElement().getXpath() != null)) {
				xpathList = wmlPackage.getMainDocumentPart().getXPathsPart().getJaxbElement().getXpath();
			}
		}
		catch (NullPointerException npe) {
			//noop
		}
		if ((xpathList != null) && (xpathList.size() > 0)) {
			for (int i=0; (ret == null) && (i<xpathList.size()); i++) {
				xpath = xpathList.get(i);
				if (xpath.getDataBinding() != null) {
					ret = xpath.getDataBinding().getStoreItemID();
				}
			}
		}
		return ret;
	}

	protected static String findXPathStorageItemIdInContentControls(WordprocessingMLPackage wmlPackage) {
	FindContentControlsVisitor visitor = null;
		if ((wmlPackage.getCustomXmlDataStorageParts() != null) && 
			(!wmlPackage.getCustomXmlDataStorageParts().isEmpty())) {
			try {
				visitor = new FindContentControlsVisitor(wmlPackage.getCustomXmlDataStorageParts().keySet());
				TraversalUtil.visit(wmlPackage, false, visitor);
			}
			catch (FindContentControlsVisitor.BreakException be) {//noop
			}
		}
		return (visitor != null ? visitor.getdefinedStoreItemId() : null);
	}

	protected static void removeSDTs(WordprocessingMLPackage wmlPackage)throws Docx4JException {
	RemovalHandler removalHandler;
	removalHandler = new RemovalHandler();
	removalHandler.removeSDTs(wmlPackage.getMainDocumentPart(), RemovalHandler.Quantifier.ALL, (String[])null);
		for (Part part:wmlPackage.getParts().getParts().values()) {
			if (part instanceof HeaderPart) {
				removalHandler.removeSDTs((HeaderPart)part, RemovalHandler.Quantifier.ALL, (String[])null);
			}
			else if (part instanceof FooterPart) {
				removalHandler.removeSDTs((FooterPart)part, RemovalHandler.Quantifier.ALL, (String[])null);
			}
		}
	}

	protected static void removeDefinedCustomXmlParts(WordprocessingMLPackage wmlPackage, String xpathStorageItemId) {
	List<PartName> partsToRemove = new ArrayList<PartName>();
	RelationshipsPart relationshipsPart = wmlPackage.getMainDocumentPart().getRelationshipsPart();
	List<Relationship> relationshipsList = ((relationshipsPart != null) && 
										    (relationshipsPart.getRelationships() != null) ?
										    relationshipsPart.getRelationships().getRelationship() : null);
	Part part = null;
	CustomXmlDataStoragePart dataPart = null;
		if (relationshipsList != null) {
			for (Relationship relationship : relationshipsList) {
				if (Namespaces.CUSTOM_XML_DATA_STORAGE.equals(relationship.getType())) {
					part = relationshipsPart.getPart(relationship);
					if (IsPartToRemove(part, xpathStorageItemId)) {
						partsToRemove.add(part.getPartName());
					}
				}
			}
		}
		if (!partsToRemove.isEmpty()) {
			for (int i=0; i<partsToRemove.size(); i++) {
				relationshipsPart.removePart(partsToRemove.get(i));
			}
		}
	}

	protected static boolean IsPartToRemove(Part part, String xpathStorageItemId) {
	boolean ret = false;
	RelationshipsPart relationshipsPart = part.getRelationshipsPart();
	List<Relationship> relationshipsList = ((relationshipsPart != null) && 
										    (relationshipsPart.getRelationships() != null) ?
										    relationshipsPart.getRelationships().getRelationship() : null);
	
	CustomXmlDataStoragePropertiesPart propertiesPart = null;
	DatastoreItem datastoreItem = null;
		if ((relationshipsList != null) && (!relationshipsList.isEmpty())) {
			for (Relationship relationship : relationshipsList) {
				if (Namespaces.CUSTOM_XML_DATA_STORAGE_PROPERTIES.equals(relationship.getType())) {
					propertiesPart = (CustomXmlDataStoragePropertiesPart)relationshipsPart.getPart(relationship);
					break;
				}
			}
		}
		if (propertiesPart != null)  {
			datastoreItem = propertiesPart.getJaxbElement();
		}
		if (datastoreItem != null) {
			if ((datastoreItem.getItemID() != null) && (datastoreItem.getItemID().length() > 0)) {
				ret = datastoreItem.getItemID().equals(xpathStorageItemId);
			}
			if ((!ret) && 
				(datastoreItem.getSchemaRefs() != null) && 
				(datastoreItem.getSchemaRefs().getSchemaRef() != null) &&
				(!datastoreItem.getSchemaRefs().getSchemaRef().isEmpty())) {
				for (SchemaRef ref : datastoreItem.getSchemaRefs().getSchemaRef()) {
					if (PART_TO_REMOVE_SCHEMA_TYPES.contains(ref.getUri())) {
						ret = true;
						break;
					}
				}
			}
		}
		return ret;
	}
	
	/**
	 *  Duplicate the document
	 */	
	public static WordprocessingMLPackage clone(WordprocessingMLPackage wmlPackage) throws Docx4JException {
		//Using the PartialDeepCopy is probably faster than serializing and deserializing the complete document.
		return (WordprocessingMLPackage)PartialDeepCopy.process(wmlPackage, null);
	}
	
	/**
	 *  Create the configuration object for conversions that are done via xsl-fo
	 */	
	public static FOSettings createFOSettings() {
		return new FOSettings();
	}
	
	/**
	 *  Convert the document via xsl-fo
	 */	
	public static void toFO(FOSettings settings, OutputStream outputStream, int flags) throws Docx4JException {
	Exporter<FOSettings> exporter = getFOExporter(flags);
		exporter.export(settings, outputStream);
	}
	
	/**
	 *  Convenience method to convert the document to PDF
	 */	
	public static void toPDF(WordprocessingMLPackage wmlPackage, OutputStream outputStream) throws Docx4JException {
	FOSettings settings = createFOSettings();
		settings.setWmlPackage(wmlPackage);
		settings.setApacheFopMime("application/pdf");
		toFO(settings, outputStream, FLAG_NONE);
	}
	
	protected static Exporter<FOSettings> getFOExporter(int flags) {
		switch (flags) {
			case FLAG_EXPORT_PREFER_NONXSL:
				return FOExporterVisitor.getInstance();
			case FLAG_EXPORT_PREFER_XSL:
			default:
				return FOExporterXslt.getInstance();
		}
	}

	/**
	 *  Create the configuration object for conversions to html
	 */	
	public static HTMLSettings createHTMLSettings() {
		return new HTMLSettings();
	}
	
	/**
	 *  Convert the document to HTML
	 */	
	public static void toHTML(HTMLSettings settings, OutputStream outputStream, int flags) throws Docx4JException {
	Exporter<HTMLSettings> exporter = getHTMLExporter(flags);
		exporter.export(settings, outputStream);
	}
	
	/**
	 *  Convenience the document to HTML
	 */	
	public static void toHTML(WordprocessingMLPackage wmlPackage, String imageDirPath, String imageTargetUri, OutputStream outputStream) throws Docx4JException {
	HTMLSettings settings = createHTMLSettings();
		settings.setWmlPackage(wmlPackage);
		if (imageDirPath != null) {
			settings.setImageDirPath(imageDirPath);
		}
		if (imageTargetUri != null) {
			settings.setImageTargetUri(imageTargetUri);
		}
		toHTML(settings, outputStream, FLAG_NONE);
	}
	
	protected static Exporter<HTMLSettings> getHTMLExporter(int flags) {
		switch (flags) {
			case FLAG_EXPORT_PREFER_NONXSL:
				return HTMLExporterVisitor.getInstance();
			case FLAG_EXPORT_PREFER_XSL:
			default:
				return HTMLExporterXslt.getInstance();
		}
	}
}
