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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.LoadFromZipNG;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlPart;
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
public class OpcPackage extends Base {

	private static Logger log = Logger.getLogger(OpcPackage.class);

	/**
	 * This HashMap is intended to prevent loops during the loading 
	 * of this package. TODO This doesn't really tell us anything that
	 * the contents of Parts couldn't also tell us (except that
	 * that doesn't contain the rels parts), so consider removing.
	 * At least replace it with a method, so this implementation
	 * detail is hidden!
	 */
	public HashMap<String, String> handled = new HashMap<String, String>();
	
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
	
	protected HashMap<String, CustomXmlPart> customXmlDataStorageParts
		= new HashMap<String, CustomXmlPart>(); // NB key is lowercase
	public HashMap<String, CustomXmlPart> getCustomXmlDataStorageParts() {
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
	public OpcPackage() {
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
	public OpcPackage(ContentTypeManager contentTypeManager) {
		try {
			partName = new PartName("/", false);
			
			this.contentTypeManager = contentTypeManager;
		} catch (Exception e) {
			log.error(e.getMessage());
			// TODO: handle exception
		}
	}
	
	public OpcPackage getPackage() {
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
	public static OpcPackage load(final java.io.File docxFile) throws Docx4JException {
		return load(docxFile, null);
	}
	/**
	 * Convenience method to create a WordprocessingMLPackage
	 * or PresentationMLPackage
	 * from an existing File (.docx/.docxm, .ppxtx or Flat OPC .xml).
     *
	 * @param docxFile
	 *            The docx file
	 * @param password
	 *            The password, if the file is password protected (compound)
	 *            
	 * @Since 2.8.0           
	 */	
	public static OpcPackage load(final java.io.File docxFile, String password) throws Docx4JException {
		
		try {
			return OpcPackage.load(new FileInputStream(docxFile), password );
		} catch (final FileNotFoundException e) {
			OpcPackage.log.error(e);
			throw new Docx4JException("Couldn't load file from " + docxFile.getAbsolutePath(), e);
		}
	}

	/**
	 * Convenience method to create a WordprocessingMLPackage
	 * or PresentationMLPackage
	 * from an inputstream (.docx/.docxm, .ppxtx or Flat OPC .xml).
	 * It detects the convenient format inspecting two first bytes of stream (magic bytes). 
	 * For office 2007 'x' formats, these two bytes are 'PK' (same as zip file)  
     *
	 * @param inputStream
	 *            The docx file 
	 */	
	public static OpcPackage load(final InputStream inputStream) throws Docx4JException {
		return load(inputStream, "");
	}	
	/**
	 * Convenience method to create a WordprocessingMLPackage
	 * or PresentationMLPackage
	 * from an inputstream (.docx/.docxm, .ppxtx or Flat OPC .xml).
	 * It detects the convenient format inspecting two first bytes of stream (magic bytes). 
	 * For office 2007 'x' formats, these two bytes are 'PK' (same as zip file)  
     *
	 * @param inputStream
	 *            The docx file 
	 * @param password
	 *            The password, if the file is password protected (compound)
	 *            
	 * @Since 2.8.0           
	 */	
	public static OpcPackage load(final InputStream inputStream, String password) throws Docx4JException {
		//try to detect the type of file using a bufferedinputstream
		final BufferedInputStream bis = new BufferedInputStream(inputStream);
		bis.mark(0);
		final byte[] firstTwobytes=new byte[2];
		int read=0;
		try {
			read = bis.read(firstTwobytes);
			bis.reset();
		} catch (final IOException e) {
			throw new Docx4JException("Error reading from the stream", e);
		}
		if (read!=2){
			throw new Docx4JException("Error reading from the stream (no bytes available)");
		}
		if (firstTwobytes[0]=='P' && firstTwobytes[1]=='K') { // 50 4B
			return OpcPackage.load(bis, Filetype.ZippedPackage, null);
		} else if  (firstTwobytes[0]==(byte)0xD0 && firstTwobytes[1]==(byte)0xCF) {
			// password protected docx is a compound file, with signature D0 CF 11 E0 A1 B1 1A E1
			log.info("Detected compound file");
			return OpcPackage.load(bis, Filetype.Compound, password);
		} else {
			//Assume..
			log.info("Assuming Flat OPC XML");
			return OpcPackage.load(bis, Filetype.FlatOPC, null);
		}
	}
	
	
	/**
	 * convenience method to load a word2007 document 
	 * from an existing inputstream (.docx/.docxm, .ppxtx or Flat OPC .xml).
	 * Included for backwards compatibility
	 * 
	 * @param is
	 * @param docxFormat
	 * @return
	 * @throws Docx4JException
	 */
	@Deprecated
	public static OpcPackage load(final InputStream is, final boolean docxFormat) throws Docx4JException {
		return load(is);  // check again, in case its a password protected compound file
	}

	/**
	 * convenience method to load a word2007 document 
	 * from an existing inputstream (.docx/.docxm, .ppxtx or Flat OPC .xml).
	 * 
	 * @param is
	 * @param docxFormat
	 * @return
	 * @throws Docx4JException
	 * 
	 * @Since 2.8.0           
	 */
	public static OpcPackage load(final InputStream is, Filetype type) throws Docx4JException {
		return load(is, type, null);
	}	
	/**
	 * convenience method to load a word2007 document 
	 * from an existing inputstream (.docx/.docxm, .ppxtx or Flat OPC .xml).
	 * 
	 * @param is
	 * @param docxFormat
	 * @return
	 * @throws Docx4JException
	 * 
	 * @Since 2.8.0           
	 */
	public static OpcPackage load(final InputStream is, Filetype type, String password) throws Docx4JException {
		if (type.equals(Filetype.ZippedPackage)){
			final LoadFromZipNG loader = new LoadFromZipNG();
			return loader.get(is);
		} else if (type.equals(Filetype.Compound)){
	        try {
				POIFSFileSystem fs = new POIFSFileSystem(is);
				EncryptionInfo info = new EncryptionInfo(fs); 
		        Decryptor d = Decryptor.getInstance(info); 
		        d.verifyPassword(password); 
		        
				InputStream is2 = d.getDataStream(fs);
				final LoadFromZipNG loader = new LoadFromZipNG();
				return loader.get(is2);				
				
			} catch (java.security.InvalidKeyException e) {
		        /* Wrong password results in:
		         * 
			        Caused by: java.security.InvalidKeyException: No installed provider supports this key: (null)
			    	at javax.crypto.Cipher.a(DashoA13*..)
			    	at javax.crypto.Cipher.init(DashoA13*..)
			    	at javax.crypto.Cipher.init(DashoA13*..)
			    	at org.apache.poi.poifs.crypt.AgileDecryptor.getCipher(AgileDecryptor.java:216)
			    	at org.apache.poi.poifs.crypt.AgileDecryptor.access$200(AgileDecryptor.java:39)
			    	at org.apache.poi.poifs.crypt.AgileDecryptor$ChunkedCipherInputStream.<init>(AgileDecryptor.java:127)
			    	at org.apache.poi.poifs.crypt.AgileDecryptor.getDataStream(AgileDecryptor.java:103)
			    	at org.apache.poi.poifs.crypt.Decryptor.getDataStream(Decryptor.java:85)		        
		         */
				throw new Docx4JException("Problem reading compound file: wrong password?", e);
			} catch (Exception e) {
				throw new Docx4JException("Problem reading compound file", e);
			}  			
		}
		
		org.docx4j.convert.in.FlatOpcXmlImporter xmlPackage;
		try {
			final Unmarshaller u = Context.jcXmlPackage.createUnmarshaller();
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

//			final org.docx4j.xmlPackage.Package wmlPackageEl = (org.docx4j.xmlPackage.Package)((JAXBElement)u.unmarshal(
//					new javax.xml.transform.stream.StreamSource(is))).getValue(); 

			// JAXB RI unmarshalls to JAXBElement; MOXy gives Package directly
			final org.docx4j.xmlPackage.Package wmlPackageEl = (org.docx4j.xmlPackage.Package)XmlUtils.unwrap(u.unmarshal(
					new javax.xml.transform.stream.StreamSource(is))); 
			
			xmlPackage = new org.docx4j.convert.in.FlatOpcXmlImporter( wmlPackageEl);
		} catch (final Exception e) {
			OpcPackage.log.error(e);
			throw new Docx4JException("Couldn't load xml from stream ",e);
		} 
		return xmlPackage.get(); 
	}

	/**
	 * Convenience method to save a WordprocessingMLPackage
	 * or PresentationMLPackage to a File.
     *
	 * @param docxFile
	 *            The docx file 
	 */	
	public void save(java.io.File docxFile) throws Docx4JException {
		save(docxFile, null);
	}	
	/**
	 * Convenience method to save a WordprocessingMLPackage
	 * or PresentationMLPackage to a File.
     *
	 * @param docxFile
	 *            The docx file 
	 */	
	private void save(java.io.File docxFile, String password) throws Docx4JException {

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
		
		if (password==null) {
			SaveToZipFile saver = new SaveToZipFile(this); 
			saver.save(docxFile);
		} else {
			// Create the compound file
	        try {
	        	// Write the package to a stream
	        	
	        	// .. then encrypt
	        	
	        	// TODO.  See for example http://code.google.com/p/ooxmlcrypto/source/browse/trunk/OfficeCrypto/OfficeCrypto.cs
				
			} catch (Exception e) {
				throw new Docx4JException("Problem reading compound file", e);
			}  			
			
		}
	}
	
	
	

	@Override
	public boolean setPartShortcut(Part part, String relationshipType) {
		if (relationshipType.equals(Namespaces.PROPERTIES_CORE)) {
			docPropsCorePart = (DocPropsCorePart)part;
			return true;			
		} else if (relationshipType.equals(Namespaces.PROPERTIES_CUSTOM)) {
			docPropsCustomPart = (DocPropsCustomPart)part;
			return true;			
		} else if (relationshipType.equals(Namespaces.PROPERTIES_EXTENDED)) {
			docPropsExtendedPart = (DocPropsExtendedPart)part;
			return true;			
		} else {	
			return false;
		}
	}

	public DocPropsCorePart getDocPropsCorePart() {
//		if (docPropsCorePart==null) {
//			try {
//				docPropsCorePart = new org.docx4j.openpackaging.parts.DocPropsCorePart();
//				this.addTargetPart(docPropsCorePart);
//				
//				org.docx4j.docProps.core.ObjectFactory factory = 
//					new org.docx4j.docProps.core.ObjectFactory();				
//				org.docx4j.docProps.core.CoreProperties properties = factory.createCoreProperties();
//				((org.docx4j.openpackaging.parts.JaxbXmlPart)docPropsCorePart).setJaxbElement((Object)properties);
//				((org.docx4j.openpackaging.parts.JaxbXmlPart)docPropsCorePart).setJAXBContext(Context.jcDocPropsCore);						
//			} catch (InvalidFormatException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}			
//		}
		return docPropsCorePart;
	}

	public DocPropsExtendedPart getDocPropsExtendedPart() {
//		if (docPropsExtendedPart==null) {
//			try {
//				docPropsExtendedPart = new org.docx4j.openpackaging.parts.DocPropsExtendedPart();
//				this.addTargetPart(docPropsExtendedPart);
//				
//				org.docx4j.docProps.extended.ObjectFactory factory = 
//					new org.docx4j.docProps.extended.ObjectFactory();				
//				org.docx4j.docProps.extended.Properties properties = factory.createProperties();
//				((org.docx4j.openpackaging.parts.JaxbXmlPart)docPropsExtendedPart).setJaxbElement((Object)properties);
//				((org.docx4j.openpackaging.parts.JaxbXmlPart)docPropsExtendedPart).setJAXBContext(Context.jcDocPropsExtended);										
//			} catch (InvalidFormatException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}			
//		}
		return docPropsExtendedPart;
	}

	/**
	 * Get DocPropsCustomPart, if any.
	 * 
	 * @return
	 */
	public DocPropsCustomPart getDocPropsCustomPart() {
		
//		if (docPropsCustomPart==null) {
//			try {
//				docPropsCustomPart = new org.docx4j.openpackaging.parts.DocPropsCustomPart();
//				this.addTargetPart(docPropsCustomPart);
//				
//				org.docx4j.docProps.custom.ObjectFactory factory = 
//					new org.docx4j.docProps.custom.ObjectFactory();
//				
//				org.docx4j.docProps.custom.Properties properties = factory.createProperties();
//				((org.docx4j.openpackaging.parts.JaxbXmlPart)docPropsCustomPart).setJaxbElement((Object)properties);
//
//				((org.docx4j.openpackaging.parts.JaxbXmlPart)docPropsCustomPart).setJAXBContext(Context.jcDocPropsCustom);										
//				
//			} catch (InvalidFormatException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}			
//		}
		
		return docPropsCustomPart;
	}
	
	/**
	 * @since 3.0.0
	 */	
	public void setTitle(String title) {
		
		if (this.getDocPropsCorePart()==null) {
			DocPropsCorePart core;
			try {
				core = new DocPropsCorePart();
				org.docx4j.docProps.core.ObjectFactory coreFactory = new org.docx4j.docProps.core.ObjectFactory();
				core.setJaxbElement(coreFactory.createCoreProperties() );
				this.addTargetPart(core);			
			} catch (InvalidFormatException e) {
				log.error(e);
			}
		}
		
		org.docx4j.docProps.core.dc.elements.ObjectFactory of = new org.docx4j.docProps.core.dc.elements.ObjectFactory();
		SimpleLiteral literal = of.createSimpleLiteral();
		literal.getContent().add(title);
		this.getDocPropsCorePart().getJaxbElement().setTitle(of.createTitle(literal) );				
	}
	
	/**
	 * @since 3.0.0
	 */	
	public String getTitle() {
		
		if (this.getDocPropsCorePart()==null) {
			return null;
		}
		
		JAXBElement<SimpleLiteral> sl = this.getDocPropsCorePart().getJaxbElement().getTitle();
		if (sl == null) return null;
		
		StringWriter sw = new StringWriter(); 
		 try {
			TextUtils.extractText(sl, sw, Context.jcDocPropsCore);
		} catch (Exception e) {
			log.error(e);
		}
		return sw.toString();				
	}
	

	/** @since 2.7.2 */
	public OpcPackage clone() {
		
		OpcPackage result = null;
		
		// Zip it up
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SaveToZipFile saver = new SaveToZipFile(this);
		try {
			saver.save(baos);
			result = load(new ByteArrayInputStream(baos.toByteArray()));
		} catch (Docx4JException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
		
	}

	
}
