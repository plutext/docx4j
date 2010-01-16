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


package org.docx4j.openpackaging.packages;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.LoadFromZipNG;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;
import org.docx4j.openpackaging.parts.DocPropsExtendedPart;
import org.docx4j.openpackaging.parts.ExternalTarget;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.Parts;
import org.docx4j.openpackaging.parts.relationships.Namespaces;


/**
 * Represent a Package as defined in the Open Packaging Specification.
 * 
 * @author Jason Harrop
 */
public class Package extends Base {

	private static Logger log = Logger.getLogger(Package.class);

	/**
	 * Package parts collection.  This is a collection of _all_
	 * parts in the package (_except_ relationship parts), 
	 * not just those referred to by the package-level relationships.
	 * It doesn't include external resources.
	 */
	protected Parts parts = new Parts();

	/**
	 * Retrieve the Parts object.
	 */
	public Parts getParts() {

		// Having a separate Parts object doesn't really buy
		// us much, but live with it...
		
		return parts;		
	}
	
	protected HashMap<ExternalTarget, Part> externalResources 
		= new HashMap<ExternalTarget, Part>();
	public HashMap<ExternalTarget, Part> getExternalResources() {
		return externalResources;		
	}	
	
	protected HashMap<String, CustomXmlDataStoragePart> customXmlDataStorageParts
		= new HashMap<String, CustomXmlDataStoragePart>();
	public HashMap<String, CustomXmlDataStoragePart> getCustomXmlDataStorageParts() {
		return customXmlDataStorageParts;
	}	
	
	protected ContentTypeManager contentTypeManager;

	public ContentTypeManager getContentTypeManager() {
		return contentTypeManager;
	}

	public void setContentTypeManager(ContentTypeManager contentTypeManager) {
		this.contentTypeManager = contentTypeManager;
	}
	
	
	/**
	 * Constructor.  Also creates a new content type manager
	 * 
	 */
	public Package() {
		try {
			partName = new PartName("/", false);
			
			contentTypeManager = new ContentTypeManager();
		} catch (Exception e) {
			log.error(e.getMessage());
			// TODO: handle exception
		}
	}

	/**
	 * Constructor.
	 *  
	 * @param contentTypeManager
	 *            The content type manager to use 
	 */
	public Package(ContentTypeManager contentTypeManager) {
		try {
			partName = new PartName("/", false);
			
			this.contentTypeManager = contentTypeManager;
		} catch (Exception e) {
			log.error(e.getMessage());
			// TODO: handle exception
		}
	}
	
	public Package getPackage() {
		return this;
	}
		
	
	protected DocPropsCorePart docPropsCorePart;

	protected DocPropsExtendedPart docPropsExtendedPart;
	
	protected DocPropsCustomPart docPropsCustomPart;
	
	
	/**
	 * Convenience method to create a WordprocessingMLPackage
	 * or PresentationMLPackage
	 * from an existing File (.docx/.docxm, .ppxtx or Flat OPC .xml).
     *
	 * @param docxFile
	 *            The docx file 
	 */	
	public static Package load(java.io.File docxFile) throws Docx4JException {
		
		if (docxFile.getName().endsWith(".xml")) {
			
			org.docx4j.convert.in.FlatOpcXmlImporter xmlPackage;
			try {
				Unmarshaller u = Context.jcXmlPackage.createUnmarshaller();
				u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

				org.docx4j.xmlPackage.Package wmlPackageEl = (org.docx4j.xmlPackage.Package)((JAXBElement)u.unmarshal(
						new javax.xml.transform.stream.StreamSource(new FileInputStream(docxFile.getAbsolutePath())))).getValue(); 

				xmlPackage = new org.docx4j.convert.in.FlatOpcXmlImporter( wmlPackageEl);
			} catch (Exception e) {
				log.error(e);
				throw new Docx4JException("Couldn't load xml from " + docxFile.getAbsolutePath(), e);
			} 
			return xmlPackage.get(); 
		}
		
//		LoadFromZipFile loader = new LoadFromZipFile();
		LoadFromZipNG loader = new LoadFromZipNG();
//		return (WordprocessingMLPackage)loader.get(docxFile);		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(docxFile);
		} catch (FileNotFoundException e) {
			log.error(e);
			throw new Docx4JException("Couldn't load file from " + docxFile.getAbsolutePath(), e);
		}
		return loader.get(fis);
	}

	/**
	 * Convenience method to save a WordprocessingMLPackage
	 * or PresentationMLPackage to a File.
     *
	 * @param docxFile
	 *            The docx file 
	 */	
	public void save(java.io.File docxFile) throws Docx4JException {

		if (docxFile.getName().endsWith(".xml")) {
			
		   	// Create a org.docx4j.wml.Package object
			FlatOpcXmlCreator worker = new FlatOpcXmlCreator(this);
			org.docx4j.xmlPackage.Package pkg = worker.get();
	    	
	    	// Now marshall it
			JAXBContext jc = Context.jcXmlPackage;
			try {
				Marshaller marshaller=jc.createMarshaller();
				
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				NamespacePrefixMapperUtils.setProperty(marshaller, 
						NamespacePrefixMapperUtils.getPrefixMapper());			
				
				marshaller.marshal(pkg, new FileOutputStream(docxFile));
			} catch (Exception e) {
				throw new Docx4JException("Error saving Flat OPC XML", e);
			}	
			return;
		}
			
		SaveToZipFile saver = new SaveToZipFile(this); 
		saver.save(docxFile);
	}
	
	
	

	@Override
	public boolean setPartShortcut(Part part, String relationshipType) {
		if (relationshipType.equals(Namespaces.PROPERTIES_CORE)) {
			docPropsCorePart = (DocPropsCorePart)part;
			return true;			
		} else if (relationshipType.equals(Namespaces.PROPERTIES_EXTENDED)) {
			docPropsExtendedPart = (DocPropsExtendedPart)part;
			return true;			
		} else {	
			return false;
		}
	}

	public DocPropsCorePart getDocPropsCorePart() {
		if (docPropsCorePart==null) {
			try {
				docPropsCorePart = new org.docx4j.openpackaging.parts.DocPropsCorePart();
				this.addTargetPart(docPropsCorePart);
				
				org.docx4j.docProps.core.ObjectFactory factory = 
					new org.docx4j.docProps.core.ObjectFactory();				
				org.docx4j.docProps.core.CoreProperties properties = factory.createCoreProperties();
				((org.docx4j.openpackaging.parts.JaxbXmlPart)docPropsCorePart).setJaxbElement((Object)properties);
				((org.docx4j.openpackaging.parts.JaxbXmlPart)docPropsCorePart).setJAXBContext(Context.jcDocPropsCore);						
			} catch (InvalidFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return docPropsCorePart;
	}

	public DocPropsExtendedPart getDocPropsExtendedPart() {
		if (docPropsExtendedPart==null) {
			try {
				docPropsExtendedPart = new org.docx4j.openpackaging.parts.DocPropsExtendedPart();
				this.addTargetPart(docPropsExtendedPart);
				
				org.docx4j.docProps.extended.ObjectFactory factory = 
					new org.docx4j.docProps.extended.ObjectFactory();				
				org.docx4j.docProps.extended.Properties properties = factory.createProperties();
				((org.docx4j.openpackaging.parts.JaxbXmlPart)docPropsExtendedPart).setJaxbElement((Object)properties);
				((org.docx4j.openpackaging.parts.JaxbXmlPart)docPropsExtendedPart).setJAXBContext(Context.jcDocPropsExtended);										
			} catch (InvalidFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return docPropsExtendedPart;
	}

	/**
	 * Get DocPropsCustomPart, creating it if necessary.
	 * 
	 * @return
	 */
	public DocPropsCustomPart getDocPropsCustomPart() {
		
		if (docPropsCustomPart==null) {
			try {
				docPropsCustomPart = new org.docx4j.openpackaging.parts.DocPropsCustomPart();
				this.addTargetPart(docPropsCustomPart);
				
				org.docx4j.docProps.custom.ObjectFactory factory = 
					new org.docx4j.docProps.custom.ObjectFactory();
				
				org.docx4j.docProps.custom.Properties properties = factory.createProperties();
				((org.docx4j.openpackaging.parts.JaxbXmlPart)docPropsCustomPart).setJaxbElement((Object)properties);

				((org.docx4j.openpackaging.parts.JaxbXmlPart)docPropsCustomPart).setJAXBContext(Context.jcDocPropsCustom);										
				
			} catch (InvalidFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
		return docPropsCustomPart;
	}




}
