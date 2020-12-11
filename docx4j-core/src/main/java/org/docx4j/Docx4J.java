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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.docx4j.convert.out.Documents4jConversionSettings;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.MicrosoftGraphConversionSettings;
import org.docx4j.convert.out.common.Exporter;
import org.docx4j.convert.out.common.preprocess.PartialDeepCopy;
import org.docx4j.convert.out.html.HTMLExporterVisitor;
import org.docx4j.convert.out.html.HTMLExporterXslt;
import org.docx4j.events.Docx4jEvent;
import org.docx4j.events.EventFinished;
import org.docx4j.events.PackageIdentifier;
import org.docx4j.events.StartEvent;
import org.docx4j.events.WellKnownJobTypes;
import org.docx4j.events.WellKnownProcessSteps;
import org.docx4j.model.datastorage.BindingHandler;
import org.docx4j.model.datastorage.CustomXmlDataStoragePartSelector;
import org.docx4j.model.datastorage.DocxFetcher;
import org.docx4j.model.datastorage.DomToXPathMap;
import org.docx4j.model.datastorage.OpenDoPEHandler;
import org.docx4j.model.datastorage.OpenDoPEHandlerComponents;
import org.docx4j.model.datastorage.OpenDoPEIntegrity;
import org.docx4j.model.datastorage.OpenDoPEIntegrityAfterBinding;
import org.docx4j.model.datastorage.RemovalHandler;
import org.docx4j.model.datastorage.XsltFinisher;
import org.docx4j.model.datastorage.XsltProvider;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io3.Load3;
import org.docx4j.openpackaging.io3.stores.ZipPartStore;
import org.docx4j.openpackaging.packages.Filetype;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.opendope.ConditionsPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.services.client.Converter;
import org.docx4j.services.client.ConverterHttp;
import org.docx4j.services.client.Format;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.opendope.answers.Answers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import net.engio.mbassy.bus.MBassador;


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
	
	private static Logger log = LoggerFactory.getLogger(Docx4J.class);		
	
	public static final String MIME_PDF = FOSettings.MIME_PDF;
	public static final String MIME_FO = FOSettings.INTERNAL_FO_MIME;
	
	/** No flags passed, do the default
	 */
	public static final int FLAG_NONE = 0;
	
	/** If available export the document using a xsl transformation
	 */
	public static final int FLAG_EXPORT_PREFER_XSL = 1;
	
	/** If available export the document using a visitor
	 */
	public static final int FLAG_EXPORT_PREFER_NONXSL = 2;
	
	/** Save the document in a zip container (default docx)
	 */
	public static final int FLAG_SAVE_ZIP_FILE = 1;
	
	/** Save the document as a flat xml document
	 */
	public static final int FLAG_SAVE_FLAT_XML = 2;
	

	/**
	 * RC4 is weak, so don't use it unless you have to for backwards compatibility purposes
	 * (ie the applications to be used for reading your docx don't support anything better). 
	 * See further http://blogs.msdn.com/b/david_leblanc/archive/2010/04/16/don-t-use-office-rc4-encryption-really-just-don-t-do-it.aspx
	 */
	public static final int FLAG_SAVE_ENCRYPTED_BINARYRC4 = 3;
			
	/**
	 * Standard encryption: This approach uses a binary EncryptionInfo structure. 
	 * It uses Advanced Encryption Standard (AES) as an encryption algorithm and SHA-1 as a hashing algorithm.
	 */
	public static final int FLAG_SAVE_ENCRYPTED_STANDARD = 4;
	
	/**
	 * Agile encryption: This is used by Word 2010, it uses an XML EncryptionInfo structure.  
	 * 
	 * The encryption and hashing algorithms are specified in the structure and can be for any encryption supported on the host computer.
	 */
	public static final int FLAG_SAVE_ENCRYPTED_AGILE = 5;
	
	/** inject the passed xml into the document
	 *  if you don't do this step, then the xml in 
	 *  the document will be used.
	 */
	public static final int FLAG_BIND_INSERT_XML = 1;
	
	/** Insert the data of the xml in the content controls 
	 *  Not needed, if the document will only be opened in Word 
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
	
	private static MBassador<Docx4jEvent> bus;
	public static void setEventNotifier(MBassador<Docx4jEvent> eventbus) {
		bus = eventbus;
	}
	
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
	 *  Load a docx/pptx/xlsx from a File, where you know in advance what type (zip, compound, flat opc)
	 *  it is.  Typically you'll cast the result to WordprocessingMLPackage or pptx/xlsx equivalent.
	 *  @since 6.0.0
	 */	
	public static OpcPackage load(final File inFile, Filetype type)  throws Docx4JException {
		
		return OpcPackage.load(inFile, type);
	}
	
	/**
	 *  Load a Docx Document from a File
	 */	
	public static WordprocessingMLPackage load(File inFile) throws Docx4JException {
		// it'd be more flexible if these load methods returned an OpcPackage
		// but it is too late to change this now, since it would be client code
		// (they'd need to add a cast).
		// And besides, for docx files anyway, this is easier.
		return WordprocessingMLPackage.load(inFile);
	}

	/**
	 *  Load a Docx Document from a File, assigning it an identifier for eventing
	 *  
	 *  @since 3.1.0
	 */	
	public static WordprocessingMLPackage load(PackageIdentifier pkgIdentifier, File inFile) throws Docx4JException {
		
		return (WordprocessingMLPackage)OpcPackage.load(pkgIdentifier, inFile);
	}
	
	/**
	 *  Load a Docx Document from an InputStream
	 */	
	public static WordprocessingMLPackage load(InputStream inStream) throws Docx4JException {
		return WordprocessingMLPackage.load(inStream);
	}

	/**
	 *  Load a Docx Document from an InputStream, assigning it an identifier for eventing
	 *  
	 *  @since 3.1.0
	 */	
	public static WordprocessingMLPackage load(PackageIdentifier pkgIdentifier, InputStream inStream) throws Docx4JException {
		return (WordprocessingMLPackage)OpcPackage.load(pkgIdentifier, inStream);
	}
	
	/**
	 *  Save a Ddocx/pptx/xlsx to a File. 
	 *  
	 *  @since 3.3.0
	 */	
	public static void save(OpcPackage pkg, File outFile) throws Docx4JException {
		
		pkg.save(outFile, Docx4J.FLAG_SAVE_ZIP_FILE);
	}
	
	/**
	 *  Save a docx/pptx/xlsx to a File. The flag is typically Docx4J.FLAG_SAVE_ZIP_FILE or Docx4J.FLAG_SAVE_FLAT_XML
	 */	
	public static void save(OpcPackage pkg, File outFile, int flags) throws Docx4JException {
		
		pkg.save(outFile, flags);
	}

	/**
	 *  Save a docx/pptx/xlsx to an OutputStream using flag Docx4J.FLAG_SAVE_ZIP_FILE 
	 *
	 *  @since 3.3.0
	 */	
	public static void save(OpcPackage pkg, OutputStream outStream) throws Docx4JException {
		
		pkg.save(outStream, Docx4J.FLAG_SAVE_ZIP_FILE);
	}
	
	/**
	 *  Save a docx/pptx/xlsx to an OutputStream. The flag is typically Docx4J.FLAG_SAVE_ZIP_FILE or Docx4J.FLAG_SAVE_FLAT_XML
	 */	
	public static void save(OpcPackage pkg, OutputStream outStream, int flags) throws Docx4JException {
		
		pkg.save(outStream, flags);
		
	}

	/**
	 *  Save a docx/pptx/xlsx to a File. The flag is typically Docx4J.FLAG_SAVE_ZIP_FILE
	 *  or Docx4J.FLAG_SAVE_FLAT_XML or one of the Docx4J.FLAG_SAVE_ENCRYPTED_ variants
	 *  (recommend FLAG_SAVE_ENCRYPTED_AGILE) 
	 *  
	 *  For the FLAG_SAVE_ENCRYPTED_ variants, you need to provide a password.

	 */	
	public static void save(OpcPackage pkg, File outFile, int flags, String password) throws Docx4JException {
		
		pkg.save(outFile, flags, password);
	}
	
	/**
	 *  Save this pkg to an OutputStream. The flag is typically Docx4J.FLAG_SAVE_ZIP_FILE
	 *  or Docx4J.FLAG_SAVE_FLAT_XML or one of the Docx4J.FLAG_SAVE_ENCRYPTED_ variants
	 *  (recommend FLAG_SAVE_ENCRYPTED_AGILE) 
	 *  
	 *  For the FLAG_SAVE_ENCRYPTED_ variants, you need to provide a password.
	 */	
	public static void save(OpcPackage pkg, OutputStream outStream, int flags, String password) throws Docx4JException {
		
		pkg.save(outStream, flags, password);
		
	}
	
	/**
	 *  Bind the content controls of the passed document to the xml.
	 */	
	public static void bind(WordprocessingMLPackage wmlPackage, String xmlDocument, int flags) throws Docx4JException {
		
		ByteArrayInputStream xmlStream = null;
		if (flags == FLAG_NONE) {
			//do everything
			flags = (FLAG_BIND_INSERT_XML |
					 FLAG_BIND_BIND_XML |
					 FLAG_BIND_REMOVE_SDT |
					 FLAG_BIND_REMOVE_XML);
		}
		if ((flags & FLAG_BIND_INSERT_XML) == FLAG_BIND_INSERT_XML) {
			try {
				xmlStream = new ByteArrayInputStream(xmlDocument.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				xmlStream = new ByteArrayInputStream(xmlDocument.getBytes());
			}
		}
        bind(wmlPackage, xmlStream, flags);
	}
	
	/**
	 *  Bind the content controls of the passed document to the xml.
	 */	
	public static void bind(WordprocessingMLPackage wmlPackage, InputStream xmlDocument, int flags) throws Docx4JException {
		
		
		if (flags == FLAG_NONE) {
			//do everything
			flags = (FLAG_BIND_INSERT_XML |
					 FLAG_BIND_BIND_XML |
					 FLAG_BIND_REMOVE_SDT |
					 FLAG_BIND_REMOVE_XML);
		}
	    Document xmlDoc = null;
		if ((flags & FLAG_BIND_INSERT_XML) == FLAG_BIND_INSERT_XML) {
				try {
		            xmlDoc = XmlUtils.getNewDocumentBuilder().parse(xmlDocument);
				} catch (Exception e) {
					throw new Docx4JException("Problems creating a org.w3c.dom.Document for the passed input stream.", e);
				}
		}
        bind(wmlPackage, xmlDoc, flags);
	}

	/**
	 *  Bind the content controls of the passed document to the xml (here using optional standardised Answers format).
	 */	
	public static void bind(WordprocessingMLPackage wmlPackage, Answers answers, int flags) throws Docx4JException {
		
		
		if (flags == FLAG_NONE) {
			//do everything
			flags = (FLAG_BIND_INSERT_XML |
					 FLAG_BIND_BIND_XML |
					 FLAG_BIND_REMOVE_SDT |
					 FLAG_BIND_REMOVE_XML);
		}
	    Document xmlDoc = null;
		if ((flags & FLAG_BIND_INSERT_XML) == FLAG_BIND_INSERT_XML) {
				try {
					xmlDoc = XmlUtils.marshaltoW3CDomDocument(answers);
				} catch (Exception e) {
					throw new Docx4JException("Problems creating a org.w3c.dom.Document from Answers", e);
				}
		}
        bind(wmlPackage, xmlDoc, flags);
	}
	
	/**
	 *  Bind the content controls of the passed document to the xml.
	 */	
	public static void bind(WordprocessingMLPackage wmlPackage, Document xmlDocument, int flags) throws Docx4JException {
		bind( wmlPackage,  xmlDocument,  flags, null);
	}

	/**
	 *  Bind the content controls of the passed document to the xml.
	 */	
	public static void bind(WordprocessingMLPackage wmlPackage, Document xmlDocument, int flags, DocxFetcher docxFetcher) throws Docx4JException {
		bind( wmlPackage,  xmlDocument,  flags,  docxFetcher, null, null, null); // no final formatting step
	}
	
	/**
	 *  Bind the content controls of the passed document to the xml, applying some formatting finishing touches 
	 *  to the final docx. 
	 *  @since 6.1.0
	 */	
	public static void bind(WordprocessingMLPackage wmlPackage, Document xmlDocument, int flags, DocxFetcher docxFetcher,
			XsltProvider xsltProvider, String xsltFinisherfilename, Map<String, Map<String, Object>> finisherParams) throws Docx4JException {

		StartEvent bindJobStartEvent = new StartEvent( WellKnownJobTypes.BIND, wmlPackage );
		bindJobStartEvent.publish();
		
		OpenDoPEHandler	openDoPEHandler = null;
		CustomXmlPart customXmlPart = null;
		RemovalHandler removalHandler = null;
		//String xpathStorageItemId = null;
		
		AtomicInteger bookmarkId = null;

		if (flags == FLAG_NONE) {
			//do everything
			flags = (FLAG_BIND_INSERT_XML |
					 FLAG_BIND_BIND_XML |
					 FLAG_BIND_REMOVE_SDT |
					 FLAG_BIND_REMOVE_XML);
		}
		
		customXmlPart 
			= CustomXmlDataStoragePartSelector.getCustomXmlDataStoragePart(wmlPackage);
		if (customXmlPart==null
				&& flags != FLAG_BIND_REMOVE_SDT /* OK if that's all we're doing */) {
			
			throw new Docx4JException("Couldn't find CustomXmlDataStoragePart! exiting..");
		}
	
		if ((flags & FLAG_BIND_INSERT_XML) == FLAG_BIND_INSERT_XML) {
			
			log.debug("insertXMLData");
			
			StartEvent startEvent = new StartEvent( WellKnownJobTypes.BIND, wmlPackage, WellKnownProcessSteps.BIND_INSERT_XML );
			startEvent.publish();
			
			insertXMLData(customXmlPart, xmlDocument);
			
			new EventFinished(startEvent).publish();
		}
		BindingHandler bh = null;
		if ((flags & FLAG_BIND_BIND_XML) == FLAG_BIND_BIND_XML) {

			log.debug("openDoPEHandler");
			
			StartEvent startEvent = new StartEvent( WellKnownJobTypes.BIND, wmlPackage, WellKnownProcessSteps.BIND_BIND_XML_OpenDoPEHandler );
			startEvent.publish();

				WordprocessingMLPackage tmpMergeResult = wmlPackage;
				
				// component processing is OFF by default
				if (Docx4jProperties.getProperty("docx4j.model.datastorage.OpenDoPEHandlerComponents.enabled", 
						false)) {
					
					// since 6.1, component processing happens earlier than before,
					// and as a discrete step
					// This step could potentially be done before insertXMLData, 
					// since it doesn't need CustomXmlDataStoragePart to be present
					// (ie it would work in the case where we can't figure that out
					//  eg there are no XPaths in the docx)
					OpenDoPEHandlerComponents componentsHandler =
							new OpenDoPEHandlerComponents(wmlPackage);
					if (docxFetcher!=null) {
						componentsHandler.setDocxFetcher(docxFetcher);
					}
					tmpMergeResult = componentsHandler.fetchComponents();
				}
			
				// since 3.2.2, OpenDoPEHandler also handles w15 repeatingSection,
				// and does that whether or not we have an XPaths part
				openDoPEHandler = new OpenDoPEHandler(tmpMergeResult);
				tmpMergeResult = openDoPEHandler.preprocess();
				
				DomToXPathMap domToXPathMap = openDoPEHandler.getDomToXPathMap();
				
				// TODO: now null out openDoPEHandler

				// Since Docx4J.bind modifies the original document,
				// as opposed to returning a new one,
				// we must copy tmpMergeResult contents into the original one.
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				Docx4J.save(tmpMergeResult, outStream);

				final ZipPartStore partLoader = new ZipPartStore( new ByteArrayInputStream(outStream.toByteArray() ));
				final Load3 loader = new Load3(partLoader);
				loader.reuseExistingOpcPackage(wmlPackage); // reuse existing object
				/* wmlPackage = (WordprocessingMLPackage) */ loader.get();
				
			new EventFinished(startEvent).publish();
			
			startEvent = new StartEvent( WellKnownJobTypes.BIND, wmlPackage, WellKnownProcessSteps.BIND_BIND_XML_OpenDoPEIntegrity );
			startEvent.publish();
			
				// since 3.3.2
				log.debug("OpenDoPEIntegrity");
				OpenDoPEIntegrity odi = new OpenDoPEIntegrity();
				odi.process(wmlPackage);

			new EventFinished(startEvent).publish();
			
//			if (log.isDebugEnabled()) {
//				Docx4J.save(wmlPackage, 
//						new File(System.getProperty("user.dir") + "/_preprocessed.docx"));
//				System.out.println("saved .. use NextBookmarkId "
//						+ openDoPEHandler.getNextBookmarkId().get());
//			}
			
//			System.out.println(
//					XmlUtils.marshaltoString(wmlPackage.getMainDocumentPart().getJaxbElement(), true, true)
//					);		
			
			startEvent = new StartEvent( WellKnownJobTypes.BIND, wmlPackage, WellKnownProcessSteps.BIND_BIND_XML_BindingHandler );
			startEvent.publish();
			
				bh = new BindingHandler(wmlPackage);
				bh.setStartingIdForNewBookmarks(openDoPEHandler.getNextBookmarkId());
				bh.setDomToXPathMap(domToXPathMap); // since 3.3.6
				bh.applyBindings();
			
			new EventFinished(startEvent).publish();
			
			startEvent = new StartEvent( WellKnownJobTypes.BIND, wmlPackage, WellKnownProcessSteps.BIND_BIND_XML_OpenDoPEIntegrityAfterBinding );
			startEvent.publish();
			
				// since 6.0.3
				log.debug("OpenDoPEIntegrityAfterBinding");
				OpenDoPEIntegrityAfterBinding odiab = new OpenDoPEIntegrityAfterBinding();
				odiab.process(wmlPackage);

			new EventFinished(startEvent).publish();
			
		}
		
		// User can provide an XSLT to perform some formatting
		if (xsltProvider!=null) {

			StartEvent startEvent = new StartEvent( WellKnownJobTypes.BIND, wmlPackage, WellKnownProcessSteps.BIND_BIND_XML_XsltFinisher );
			startEvent.publish();
			
			XsltFinisher finisher = new XsltFinisher(wmlPackage);
			XsltFinisher.setXsltProvider(xsltProvider );
						
			finisher.apply(wmlPackage.getMainDocumentPart(), bh.getXpathsMap(), xsltFinisherfilename, finisherParams);
			
			new EventFinished(startEvent).publish();			
		}
		
//		System.out.println(
//				XmlUtils.marshaltoString(wmlPackage.getMainDocumentPart().getJaxbElement(), true, true)
//				);		
		
		if ((flags & FLAG_BIND_REMOVE_SDT) == FLAG_BIND_REMOVE_SDT) {
			
			StartEvent startEvent = new StartEvent( WellKnownJobTypes.BIND, wmlPackage, WellKnownProcessSteps.BIND_REMOVE_SDT );
			startEvent.publish();

			log.debug("removeSDTs");
			removeSDTs(wmlPackage);
			
//			System.out.println(
//			XmlUtils.marshaltoString(wmlPackage.getMainDocumentPart().getJaxbElement(), true, true)
//			);		
			
			
			new EventFinished(startEvent).publish();
		}
		if ((flags & FLAG_BIND_REMOVE_XML) == FLAG_BIND_REMOVE_XML) {
			
			StartEvent startEvent = new StartEvent( WellKnownJobTypes.BIND, wmlPackage, WellKnownProcessSteps.BIND_REMOVE_XML );
			startEvent.publish();
			
			log.debug("removeDefinedCustomXmlParts");
			removeDefinedCustomXmlParts(wmlPackage, customXmlPart.getItemId());
			
			new EventFinished(startEvent).publish();
		}

		new EventFinished(bindJobStartEvent).publish();
	}

	protected static void insertXMLData(CustomXmlPart customXmlDataStoragePart, Document xmlDocument) throws Docx4JException {
		
		customXmlDataStoragePart.setXML(xmlDocument);
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

	protected static void removeSDTs(WordprocessingMLPackage wmlPackage) throws Docx4JException {
		
		RemovalHandler removalHandler = new RemovalHandler();
		removalHandler.removeSDTs(wmlPackage);
	}

	protected static void removeDefinedCustomXmlParts(WordprocessingMLPackage wmlPackage, 
			String itemId) {
	List<PartName> partsToRemove = new ArrayList<PartName>();
	RelationshipsPart relationshipsPart = wmlPackage.getMainDocumentPart().getRelationshipsPart();
	List<Relationship> relationshipsList = ((relationshipsPart != null) && 
										    (relationshipsPart.getRelationships() != null) ?
										    relationshipsPart.getRelationships().getRelationship() : null);
	Part part = null;
		if (relationshipsList != null) {
			for (Relationship relationship : relationshipsList) {
				if (Namespaces.CUSTOM_XML_DATA_STORAGE.equals(relationship.getType())) {
					part = relationshipsPart.getPart(relationship);
					if ( itemId!=null && itemId.equals(((CustomXmlPart)part).getItemId() )) {
						partsToRemove.add(part.getPartName());
					} else if (part instanceof XPathsPart) {
						partsToRemove.add(part.getPartName());
					} else if (part instanceof ConditionsPart) {
						partsToRemove.add(part.getPartName());
					} else {
						log.warn("Keeping " + part.getPartName() + " of type " + part.getClass().getName());
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

//	protected static boolean IsPartToRemove(Part part, String xpathStorageItemId) {
//	boolean ret = false;
//	RelationshipsPart relationshipsPart = part.getRelationshipsPart();
//	List<Relationship> relationshipsList = ((relationshipsPart != null) && 
//										    (relationshipsPart.getRelationships() != null) ?
//										    relationshipsPart.getRelationships().getRelationship() : null);
//	
//	CustomXmlDataStoragePropertiesPart propertiesPart = null;
//	DatastoreItem datastoreItem = null;
//		if ((relationshipsList != null) && (!relationshipsList.isEmpty())) {
//			for (Relationship relationship : relationshipsList) {
//				if (Namespaces.CUSTOM_XML_DATA_STORAGE_PROPERTIES.equals(relationship.getType())) {
//					propertiesPart = (CustomXmlDataStoragePropertiesPart)relationshipsPart.getPart(relationship);
//					break;
//				}
//			}
//		}
//		if (propertiesPart != null)  {
//			datastoreItem = propertiesPart.getJaxbElement();
//		}
//		if (datastoreItem != null) {
//			if ((datastoreItem.getItemID() != null) && (datastoreItem.getItemID().length() > 0)) {
//				ret = datastoreItem.getItemID().equals(xpathStorageItemId);
//			}
//			if ((!ret) && 
//				(datastoreItem.getSchemaRefs() != null) && 
//				(datastoreItem.getSchemaRefs().getSchemaRef() != null) &&
//				(!datastoreItem.getSchemaRefs().getSchemaRef().isEmpty())) {
//				for (SchemaRef ref : datastoreItem.getSchemaRefs().getSchemaRef()) {
//					if (PART_TO_REMOVE_SCHEMA_TYPES.contains(ref.getUri())) {
//						ret = true;
//						break;
//					}
//				}
//			}
//		}
//		return ret;
//	}
	
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
				
		StartEvent startEvent = new StartEvent( wmlPackage, WellKnownProcessSteps.PDF );
		startEvent.publish();
		
		if (pdfViaDocuments4jRemote()) {
			
			Exporter<Documents4jConversionSettings> exporter = documents4jRemoteExporterGetInstance();
			Documents4jConversionSettings settings = new Documents4jConversionSettings();
			settings.setOpcPackage(wmlPackage);
			exporter.export(settings, outputStream); 
			
		} else if (pdfViaDocuments4jLocal()) {

			Exporter<Documents4jConversionSettings> exporter = documents4jLocalExporterGetInstance();
			Documents4jConversionSettings settings = new Documents4jConversionSettings();
			settings.setOpcPackage(wmlPackage);
			exporter.export(settings, outputStream); 
			
		} else if (pdfViaFO()) {
			FOSettings settings = createFOSettings();
			settings.setOpcPackage(wmlPackage);
			settings.setApacheFopMime("application/pdf");
			toFO(settings, outputStream, FLAG_NONE);
			new EventFinished(startEvent).publish();
			
		} else if (pdfViaMicrosoftGraph()) {
			
			log.error("Microsoft Graph can't be invoked via this interface.  Use it directly; see https://github.com/plutext/java-docx-to-pdf-using-Microsoft-Graph   ");
			
		} else {
			
			log.info("No documents4j or FO found); falling back to legacy Converter.");
			
			// Configure this property to point to your own Converter instance.
			// Since this converter is no longer available, this will only suit existing users
			String URL = Docx4jProperties.getProperty("com.plutext.converter.URL", "http://localhost:9016/v1/00000000-0000-0000-0000-000000000000/convert");
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			save(wmlPackage, baos);
			
			Converter converter = new ConverterHttp(URL); 
			try {
				converter.convert(baos.toByteArray(), Format.DOCX, Format.PDF, outputStream);
				baos.close();
			} catch (Exception e) {
				throw new Docx4JException(e.getMessage(), e);
			} finally {
				new EventFinished(startEvent).publish();
			}
			
		}
		
	}

	public static Boolean EXPORT_FO_DETECTED = null;
	public static Boolean EXPORT_DOCUMENTS4J_REMOTE_DETECTED = null;
	public static Boolean EXPORT_DOCUMENTS4J_LOCAL_DETECTED = null;
	public static Boolean EXPORT_MICROSOFT_GRAPH_DETECTED = null;
	
	/**
	 * If the docx4j-export-fo project is present, 
	 * we'll use FO for PDF export.
	 * 
	 * Otherwise, we'll try to use Plutext's now-retired PDF Converter.
	 * 
	 * @return
	 * @since 3.3.0
	 */
	public static boolean pdfViaFO() {
		
		if (EXPORT_FO_DETECTED==null) {
			
			try {
				Object o = FOExporterVisitorGetInstance();
				EXPORT_FO_DETECTED = Boolean.TRUE;
			} catch (Docx4JException e) {
				EXPORT_FO_DETECTED = Boolean.FALSE;
			}
		}
		
		return EXPORT_FO_DETECTED;
	}
	
	/**
	 * @since 8.2.0
	 */
	public static boolean pdfViaDocuments4jRemote() {
		
		if (EXPORT_DOCUMENTS4J_REMOTE_DETECTED==null) {
			
			try {
				Object o = documents4jRemoteExporterGetInstance();
				EXPORT_DOCUMENTS4J_REMOTE_DETECTED = Boolean.TRUE;
			} catch (Docx4JException e) {
				EXPORT_DOCUMENTS4J_REMOTE_DETECTED = Boolean.FALSE;
			}
		}
		
		return EXPORT_DOCUMENTS4J_REMOTE_DETECTED;
	}
	/**
	 * @since 8.2.0
	 */
	public static boolean pdfViaDocuments4jLocal() {
		
		if (EXPORT_DOCUMENTS4J_LOCAL_DETECTED==null) {
			
			try {
				Object o = documents4jLocalExporterGetInstance();
				EXPORT_DOCUMENTS4J_LOCAL_DETECTED = Boolean.TRUE;
			} catch (Docx4JException e) {
				EXPORT_DOCUMENTS4J_LOCAL_DETECTED = Boolean.FALSE;
			}
		}
		
		return EXPORT_DOCUMENTS4J_LOCAL_DETECTED;
	}
	
	/**
	 * @since 8.2.7
	 */
	public static boolean pdfViaMicrosoftGraph() {
		
		if (EXPORT_MICROSOFT_GRAPH_DETECTED==null) {
			
			try {
				Object o = microsoftGraphExporterGetInstance();
				EXPORT_MICROSOFT_GRAPH_DETECTED = Boolean.TRUE;
			} catch (Docx4JException e) {
				EXPORT_MICROSOFT_GRAPH_DETECTED = Boolean.FALSE;
			}
		}
		
		return EXPORT_MICROSOFT_GRAPH_DETECTED;
	}

	private static Exporter<MicrosoftGraphConversionSettings> microsoftGraphExporterGetInstance()  throws Docx4JException {
		
		// Use reflection to return Exporter
		// so docx4j-core doesn't depend on docx4j-conversion-via-microsoft-graph
		
		try {
			Class<?> clazz = Class.forName("org.docx4j.convert.out.microsoft_graph.DocxToPdfExporter");			
			Method method = clazz.getMethod("getInstance", null);
			// TODO: must pass a DocxToPdfConverter arg for this to be useful
			return (Exporter<MicrosoftGraphConversionSettings>)method.invoke(null, null, null);
			
		} catch (Exception e) {
			throw new Docx4JException("org.docx4j.convert.out.microsoft_graph.DocxToPdfExporter not found; "
					+ "if you want it, add an implementation to your path.  " + "/n" + e.getMessage(), e);
		}			
		
	}
	
	
	protected static Exporter<FOSettings> getFOExporter(int flags)  throws Docx4JException {
		switch (flags) {
			case FLAG_EXPORT_PREFER_NONXSL:
				return FOExporterVisitorGetInstance();
			case FLAG_EXPORT_PREFER_XSL:
			default:
				return FOExporterXsltGetInstance();
		}
	}
		
	private static Exporter<FOSettings> FOExporterVisitorGetInstance() throws Docx4JException {
		
		// Use reflection to return FOExporterVisitor.getInstance();
		// so docx4j-core doesn't depend on docx4j-export-FO
		
		try {
			Class<?> clazz = Class.forName("org.docx4j.convert.out.fo.FOExporterVisitor");
			Method method = clazz.getMethod("getInstance", null);
			return (Exporter<FOSettings>)method.invoke(null, null);
			
		} catch (Exception e) {
			throw new Docx4JException("org.docx4j.convert.out.fo.FOExporterVisitor not found; if you want it, add docx4j-export-FO to your path.  Doing so will disable Plutext's PDF Converter." + "/n" + e.getMessage(), e);
		}			
	}

	private static Exporter<FOSettings> FOExporterXsltGetInstance()  throws Docx4JException {
		
		// Use reflection to return FOExporterXslt.getInstance();
		// so docx4j-core doesn't depend on docx4j-export-FO
		
		try {
			Class<?> clazz = Class.forName("org.docx4j.convert.out.fo.FOExporterXslt");			
			Method method = clazz.getMethod("getInstance", null);
			return (Exporter<FOSettings>)method.invoke(null, null);
			
		} catch (Exception e) {
			throw new Docx4JException("org.docx4j.convert.out.fo.FOExporterXslt not found; if you want it, add docx4j-export-FO to your path.  " + "/n" + e.getMessage(), e);
		}			
		
	}

	private static Exporter<Documents4jConversionSettings> documents4jRemoteExporterGetInstance()  throws Docx4JException {
		
		// Use reflection to return Exporter
		// so docx4j-core doesn't depend on docx4j-export-documents4j-remote
		
		try {
			Class<?> clazz = Class.forName("org.docx4j.convert.out.documents4j.remote.Documents4jRemoteExporter");			
			Method method = clazz.getMethod("getInstance", null);
			return (Exporter<Documents4jConversionSettings>)method.invoke(null, null);
			
		} catch (Exception e) {
			throw new Docx4JException("org.docx4j.convert.out.documents4j.remote.Documents4jRemoteExporter not found; "
					+ "if you want it, add docx4j-export-documents4j-remote to your path.  " + "/n" + e.getMessage(), e);
		}			
		
	}
	private static Exporter<Documents4jConversionSettings> documents4jLocalExporterGetInstance()  throws Docx4JException {
		
		// Use reflection to return Exporter
		// so docx4j-core doesn't depend on docx4j-export-documents4j-local
		
		try {
			Class<?> clazz = Class.forName("org.docx4j.convert.out.documents4j.local.Documents4jLocalExporter");			
			Method method = clazz.getMethod("getInstance", null);
			return (Exporter<Documents4jConversionSettings>)method.invoke(null, null);
			
		} catch (Exception e) {
			throw new Docx4JException("org.docx4j.convert.out.documents4j.local.Documents4jLocalExporter not found; "
					+ "if you want it, add docx4j-export-documents4j-local to your path.  " + "/n" + e.getMessage(), e);
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

		StartEvent startEvent = new StartEvent( settings.getOpcPackage(), WellKnownProcessSteps.HTML_OUT );
		startEvent.publish();
		
		Exporter<HTMLSettings> exporter = getHTMLExporter(flags);
		exporter.export(settings, outputStream);
		
		new EventFinished(startEvent).publish();
	}
	
	/**
	 *  Convert the document to HTML
	 */	
	public static void toHTML(WordprocessingMLPackage wmlPackage, String imageDirPath, String imageTargetUri, OutputStream outputStream) throws Docx4JException {

		StartEvent startEvent = new StartEvent( wmlPackage, WellKnownProcessSteps.HTML_OUT );
		startEvent.publish();
		
		HTMLSettings settings = createHTMLSettings();
		settings.setOpcPackage(wmlPackage);
		if (imageDirPath != null) {
			settings.setImageDirPath(imageDirPath);
		}
		if (imageTargetUri != null) {
			settings.setImageTargetUri(imageTargetUri);
		}
		toHTML(settings, outputStream, FLAG_NONE);
		
		new EventFinished(startEvent).publish();
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
