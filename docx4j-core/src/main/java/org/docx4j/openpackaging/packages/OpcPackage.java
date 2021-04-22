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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.docx4j.Docx4J;
import org.docx4j.TextUtils;
import org.docx4j.convert.in.FlatOpcXmlImporter;
import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.docProps.custom.Properties;
import org.docx4j.events.EventFinished;
import org.docx4j.events.PackageIdentifier;
import org.docx4j.events.PackageIdentifierTransient;
import org.docx4j.events.StartEvent;
import org.docx4j.events.WellKnownProcessSteps;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.PackageRelsUtil;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.io3.Load3;
import org.docx4j.openpackaging.io3.Save;
import org.docx4j.openpackaging.io3.stores.PartStore;
import org.docx4j.openpackaging.io3.stores.ZipPartStore;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;
import org.docx4j.openpackaging.parts.DocPropsExtendedPart;
import org.docx4j.openpackaging.parts.ExternalTarget;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.Parts;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.org.apache.poi.poifs.crypt.Decryptor;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfo;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionMode;
import org.docx4j.org.apache.poi.poifs.crypt.Encryptor;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Represent a Package as defined in the Open Packaging Specification.
 * 
 * @author Jason Harrop
 */
public abstract class OpcPackage extends Base implements PackageIdentifier {

	private static Logger log = LoggerFactory.getLogger(OpcPackage.class);

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
	
	// Currently only external images are stored here
	protected HashMap<ExternalTarget, Part> externalResources 
		= new HashMap<ExternalTarget, Part>();
	public HashMap<ExternalTarget, Part> getExternalResources() {
		return externalResources;		
	}	
	
	protected HashMap<String, CustomXmlPart> customXmlDataStorageParts
		= new HashMap<String, CustomXmlPart>(); // NB key is lowercase
	/**
	 * keyed by item id (in lower case)
	 * @return
	 */
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
	
	private PartStore sourcePartStore;	
	
	/**
	 * @return the partStore
	 * @since 3.0.
	 */
	public PartStore getSourcePartStore() {
		return sourcePartStore;
	}

	/**
	 * @param partStore the partStore to set
	 * @since 3.0.
	 */
	public void setSourcePartStore(PartStore partStore) {
		this.sourcePartStore = partStore;
	}

	private PartStore targetPartStore;	
	
	/**
	 * @return the partStore
	 * @since 3.0.
	 */
	public PartStore getTargetPartStore() {
		return targetPartStore;
	}

	/**
	 * @param partStore the partStore to set
	 * @since 3.0.
	 */
	public void setTargetPartStore(PartStore partStore) {
		this.targetPartStore = partStore;
	}
	
	/**
	 * Constructor.  Also creates a new content type manager
	 * 
	 */
	public OpcPackage() {
		try {
			this.setPartName(new PartName("/", false));
			
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
			this.setPartName(new PartName("/", false));
			
			this.contentTypeManager = contentTypeManager;
		} catch (Exception e) {
			log.error(e.getMessage());
			// TODO: handle exception
		}
	}
	
	@Override
	public OpcPackage getPackage() {
		return this;
	}
		
	
	protected DocPropsCorePart docPropsCorePart;

	protected DocPropsExtendedPart docPropsExtendedPart;
	
	protected DocPropsCustomPart docPropsCustomPart;
	
	
	boolean isNew = true;	
	/**
	 * Was this pkg created from scratch in docx4j, or loaded?
	 * @return
	 */
	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	/**
	 * Convenience method to create a WordprocessingMLPackage
	 * or PresentationMLPackage
	 * from an existing File (.docx/.docxm, .ppxtx or Flat OPC .xml).
     *
	 * @param docxFile
	 *            The docx file 
	 * @since 3.1.0
	 */	
	public static OpcPackage load(PackageIdentifier pkgIdentifier, final java.io.File docxFile) throws Docx4JException {
		return load(pkgIdentifier, docxFile, null);
	}
	
	/**
	 * Convenience method to create a WordprocessingMLPackage
	 * or PresentationMLPackage
	 * from an existing File (.docx/.docxm, .ppxtx or Flat OPC .xml).
     *
	 * @param docxFile
	 *            The docx file 
	 */	
	public static OpcPackage load(final java.io.File docxFile) throws Docx4JException {
		return load(docxFile, "");
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
		
		PackageIdentifier name = new PackageIdentifierTransient(docxFile.getName());
		
		try {
			 final FileInputStream fileInputStream = new FileInputStream(docxFile);
			    try {
				return OpcPackage.load(name, fileInputStream, password);
			    } finally {
				try {
				    fileInputStream.close();
				} catch (final IOException e) {
				    log.warn("Could not close fileInputStream of file {}: {}", docxFile.toString(), e.getMessage());
				}
			    }
		} catch (final FileNotFoundException e) {
			throw new Docx4JException("Couldn't load file from " + docxFile.getAbsolutePath(), e);
		}
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
	 * @Since 3.1.0         
	 */	
	public static OpcPackage load(PackageIdentifier pkgIdentifier, final java.io.File docxFile, String password) throws Docx4JException {
		
		try {
			final FileInputStream fileInputStream = new FileInputStream(docxFile);
			try {
				return OpcPackage.load(pkgIdentifier, fileInputStream, password);
			} finally {
				try {
					fileInputStream.close();
				} catch (final IOException e) {
					log.warn("Could not close fileInputStream of file {}: {}", docxFile.toString(), e.getMessage());
				}
			}
		} catch (final FileNotFoundException e) {
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
	 *            
	 * @since 3.1.0            
	 */	
	public static OpcPackage load(PackageIdentifier pkgIdentifier, final InputStream inputStream) throws Docx4JException {
		return load(pkgIdentifier, inputStream, "");
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

		return load(null, inputStream,  password);
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
	 * @Since 3.1.0     
	 */	
	private static OpcPackage load(PackageIdentifier pkgIdentifier, final InputStream inputStream, String password) throws Docx4JException {
		
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
			return OpcPackage.load(pkgIdentifier, bis, Filetype.ZippedPackage, null);
		} else if  (firstTwobytes[0]==(byte)0xD0 && firstTwobytes[1]==(byte)0xCF) {
			// password protected docx is a compound file, with signature D0 CF 11 E0 A1 B1 1A E1
			log.info("Detected compound file");
			return OpcPackage.load(pkgIdentifier, bis, Filetype.Compound, password);
		} else {
			//Assume..
			log.info("Assuming Flat OPC XML");
			return OpcPackage.load(pkgIdentifier, bis, Filetype.FlatOPC, null);
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

		return load(null, is, type, password);
	}

	/**
	 * convenience method to load from a file, where you know the Filetype
	 * (if it is Filetype.ZippedPackage, ZipFile will be used instead of ZipArchiveInputStream)
	 * 
	 * @param is
	 * @param docxFormat
	 * @return
	 * @throws Docx4JException
	 * 
	 * @Since 6.0.0           
	 */
	public static OpcPackage load(final File file, Filetype type) throws Docx4JException {
		return load(file, type, null);
	}

	/**
	 * convenience method to load from a file, where you know the Filetype
	 * (if it is Filetype.ZippedPackage, ZipFile will be used instead of ZipArchiveInputStream)
	 * 
	 * @param is
	 * @param docxFormat
	 * @return
	 * @throws Docx4JException
	 * 
	 * @Since 6.0.0           
	 */
	public static OpcPackage load(final File file, Filetype type, String password) throws Docx4JException {
		PackageIdentifier pkgIdentifier = new PackageIdentifierTransient(file.getName());
		
		if (type.equals(Filetype.ZippedPackage)){

			StartEvent startEvent = new StartEvent( pkgIdentifier,  WellKnownProcessSteps.PKG_LOAD );
			startEvent.publish();			
			
			// We can/should use Common Compress' ZipFile in preference to ZipArchiveInputStream
			final ZipPartStore partLoader = new ZipPartStore(file);
			final Load3 loader = new Load3(partLoader);
			OpcPackage opcPackage = loader.get();
			
			opcPackage.setNew(false);
			
			if (pkgIdentifier!=null) {
				opcPackage.setName(pkgIdentifier.name());
			}
			
			new EventFinished(startEvent).publish();						
			return opcPackage;
			
		} else {
			try {

				final FileInputStream fileInputStream = new FileInputStream(file);
				try {
					return load(pkgIdentifier, fileInputStream, type, password);
				} finally {
					try {
						fileInputStream.close();
					} catch (final IOException e) {
						log.warn("Could not close fileInputStream of file {}: {}", file.toString(), e.getMessage());
					}
				}
			} catch (final FileNotFoundException e) {
				throw new Docx4JException("Couldn't load file from " + file.getAbsolutePath(), e);
			}
			
		}
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
	 * @Since 3.3.0      
	 */
	private static OpcPackage load(PackageIdentifier pkgIdentifier, final InputStream is, Filetype type, String password) throws Docx4JException {

		if (pkgIdentifier==null) {
			pkgIdentifier = new PackageIdentifierTransient("pkg_" + System.currentTimeMillis());
		}
		
		StartEvent startEvent = new StartEvent( pkgIdentifier,  WellKnownProcessSteps.PKG_LOAD );
		startEvent.publish();			
		
		if (type.equals(Filetype.ZippedPackage)){
			
			final ZipPartStore partLoader = new ZipPartStore(is);
			final Load3 loader = new Load3(partLoader);
			OpcPackage opcPackage = loader.get();

			opcPackage.setNew(false);
			
			if (pkgIdentifier!=null) {
				opcPackage.setName(pkgIdentifier.name());
			}
			
			new EventFinished(startEvent).publish();						
			return opcPackage;
			
//			final LoadFromZipNG loader = new LoadFromZipNG();
//			return loader.get(is);			
			
		} else if (type.equals(Filetype.Compound)){
			
	        try {
				POIFSFileSystem fs = new POIFSFileSystem(is);
				InputStream is2 = null;
				try {
					EncryptionInfo info = new EncryptionInfo(fs); 
			        Decryptor d = Decryptor.getInstance(info); 
			        log.debug("Decrypting with " + d.getClass().getName());
			        if (d.verifyPassword(password))   {
		                 log.debug("Password works");
		             } else {
		 				throw new Docx4JException("Problem reading encrypted document: wrong password?");
		             }
			        
					 is2 = d.getDataStream(fs);
					/* Note, this uses getCipher again:
					 * 
							at org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions.getCipher(CryptoFunctions.java:208)
							at org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor.initCipherForBlock(AgileDecryptor.java:305)
							at org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor$AgileCipherInputStream.initCipherForBlock(AgileDecryptor.java:351)
							at org.docx4j.org.apache.poi.poifs.crypt.ChunkedCipherInputStream.<init>(ChunkedCipherInputStream.java:56)
							at org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor$AgileCipherInputStream.<init>(AgileDecryptor.java:343)
							at org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor.getDataStream(AgileDecryptor.java:287)
							at org.docx4j.org.apache.poi.poifs.crypt.Decryptor.getDataStream(Decryptor.java:95)
						
						but at this point you'll have a null key if verifyPassword failed! 
							
						 */
				} catch (FileNotFoundException fnf) {
					
					/*
						java.io.FileNotFoundException: no such entry: "EncryptionInfo", had: [Data, CompObj, ObjectPool, 1Table, DocumentSummaryInformation, SummaryInformation, WordDocument, Macros, MsoDataStore]
							at org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode.getEntry(DirectoryNode.java:406)
							at org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode.createDocumentInputStream(DirectoryNode.java:194)
							at org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfo.<init>(EncryptionInfo.java:103)					 
							*/
					throw new Docx4JException("This file seems to be a binary doc/ppt/xls, not an encrypted OLE2 file containing a doc/pptx/xlsx");
					
				}
				final ZipPartStore partLoader = new ZipPartStore(is2);
				final Load3 loader = new Load3(partLoader);
				OpcPackage opcPackage = loader.get();

				opcPackage.setNew(false);
				
				if (pkgIdentifier!=null) {
					opcPackage.setName(pkgIdentifier.name());
				}
				
//				opcPackage.getProtectionSettings().setWasEncrypted(true);
				
				return opcPackage;
			} catch (Docx4JException e) {
				throw e;
			} catch (Exception e) {
				throw new Docx4JException("Problem reading encrypted document", e);
			} finally {
				new EventFinished(startEvent).publish();				
			}
		}
		
		try {
			FlatOpcXmlImporter xmlPackage = new FlatOpcXmlImporter(is); 
			return xmlPackage.get(); 
		} catch (final Exception e) {
			throw new Docx4JException("Couldn't load xml from stream ",e);
		} finally {
			new EventFinished(startEvent).publish();									
		}
	}

	/**
	 * Convenience method to save a WordprocessingMLPackage
	 * or PresentationMLPackage to a File.  If the file ends with .xml,
	 * use Flat OPC XML format; otherwise zip it up.
	 */	
	public void save(java.io.File file) throws Docx4JException {
		if (file.getName().endsWith(".xml")) {
			save(file, Docx4J.FLAG_SAVE_FLAT_XML);			
		} else {
			save(file, Docx4J.FLAG_SAVE_ZIP_FILE);						
		}
	}	

	/**
	 *  Save this pkg to a File. The flag is typically Docx4J.FLAG_SAVE_ZIP_FILE
	 *  or Docx4J.FLAG_SAVE_FLAT_XML
	 *  
	 *  @since 3.1.0
	 */	
	public void save(File outFile, int flags) throws Docx4JException {
		save( outFile,  flags,  null);
	}
	
	/**
	 *  Save this pkg to a file. The flag is typically Docx4J.FLAG_SAVE_ZIP_FILE
	 *  or Docx4J.FLAG_SAVE_FLAT_XML or one of the Docx4J.FLAG_SAVE_ENCRYPTED_ variants
	 *  (recommend FLAG_SAVE_ENCRYPTED_AGILE)
	 *   
	 *  For the FLAG_SAVE_ENCRYPTED_ variants, you need to provide a password.
	 *  	 *  
	 *  @since 3.3.0
	 */	
	public void save(File outFile, int flags, String password) throws Docx4JException {
		
		OutputStream outStream = null;
		try {
			outStream = new FileOutputStream(outFile);
			save(outStream, flags, password);
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
	 *  Save this pkg to an OutputStream in the usual zipped up format
	 *  (Docx4J.FLAG_SAVE_ZIP_FILE)
	 *  
	 *  @since 3.1.0
	 */	
	public void save(OutputStream outStream) throws Docx4JException {
		save(outStream, Docx4J.FLAG_SAVE_ZIP_FILE);						
	}


	/**
	 *  Save this pkg to an OutputStream. The flag is typically Docx4J.FLAG_SAVE_ZIP_FILE
	 *  or Docx4J.FLAG_SAVE_FLAT_XML
	 *  
	 *  @since 3.1.0
	 */	
	public void save(OutputStream outStream, int flags) throws Docx4JException {
		
		save( outStream,  flags, null);
	}
	
	/**
	 *  Save this pkg to an OutputStream. The flag is typically Docx4J.FLAG_SAVE_ZIP_FILE
	 *  or Docx4J.FLAG_SAVE_FLAT_XML or one of the Docx4J.FLAG_SAVE_ENCRYPTED_ variants
	 *  (recommend FLAG_SAVE_ENCRYPTED_AGILE) 
	 *  
	 *  For the FLAG_SAVE_ENCRYPTED_ variants, you need to provide a password.
	 *  
	 *  @since 3.3.0
	 */	
	public void save(OutputStream outStream, int flags, String password) throws Docx4JException {
		
		StartEvent startEvent = new StartEvent( this,  WellKnownProcessSteps.PKG_SAVE );
		startEvent.publish();
		
		if (flags == Docx4J.FLAG_SAVE_FLAT_XML) {
			JAXBContext jc = Context.jcXmlPackage;
			FlatOpcXmlCreator opcXmlCreator = new FlatOpcXmlCreator(this);
			opcXmlCreator.populate();
			opcXmlCreator.marshal(outStream);
			
		} else if (
				flags == Docx4J.FLAG_SAVE_ENCRYPTED_BINARYRC4
				|| flags == Docx4J.FLAG_SAVE_ENCRYPTED_STANDARD 
				|| flags == Docx4J.FLAG_SAVE_ENCRYPTED_AGILE 							
				) {
			
			if (password==null || password.trim().length()==0) {
				// If in Word you hit enter when asked to set the password, the docx will be saved unencrypted
				throw new Docx4JException("Encryption requested, but a new password not provided.");
			}
			
			// We could set DocSecurity=1, but it seems completely irrelevant,
			// so why bother, until proven that some Microsoft software somewhere uses it?
			// If/when we do so, use ProtectionSettings.setDocSecurity

			EncryptionInfo info = null;			
			if (flags == Docx4J.FLAG_SAVE_ENCRYPTED_BINARYRC4) {
				info = new EncryptionInfo(EncryptionMode.binaryRC4);
				
			} else if (flags == Docx4J.FLAG_SAVE_ENCRYPTED_STANDARD ) {
				info = new EncryptionInfo(EncryptionMode.standard);	
				
			} else if (flags == Docx4J.FLAG_SAVE_ENCRYPTED_AGILE ) {
				info = new EncryptionInfo(EncryptionMode.agile);
				// EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile, CipherAlgorithm.aes192, HashAlgorithm.sha384, -1, -1, null);

			}  			

			Encryptor enc = info.getEncryptor();
			enc.confirmPassword(password); 

			try {
				POIFSFileSystem fs = new POIFSFileSystem();
				OutputStream os = enc.getDataStream(fs);	
				
				Save saver = new Save(this);
				saver.save(os);
				
				fs.writeFilesystem(outStream);
				
			} catch (Exception e) {
				throw new  Docx4JException("Error encrypting as OLE compound file", e);
			}
			
		} else {
//			SaveToZipFile saver = new SaveToZipFile(wmlPackage);
			Save saver = new Save(this);
			saver.save(outStream);
		}
		new EventFinished(startEvent).publish();
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

	/**
	 * Get the DocPropsCorePart, if any.
	 * 
	 * @return
	 */
	public DocPropsCorePart getDocPropsCorePart() {
		return docPropsCorePart;
	}

	public void addDocPropsCorePart() {
		if (docPropsCorePart==null) {
			try {
				docPropsCorePart = new org.docx4j.openpackaging.parts.DocPropsCorePart();
				this.addTargetPart(docPropsCorePart);
				
				docPropsCorePart.setJaxbElement(new CoreProperties());
			} catch (InvalidFormatException e) {
				//Won't happen, so don't throw
				log.error(e.getMessage(), e);
			}			
		}
	}
	
	/**
	 * Get the DocPropsExtendedPart, if any.
	 * 
	 * @return
	 */
	public DocPropsExtendedPart getDocPropsExtendedPart() {
		return docPropsExtendedPart;
	}

	public void addDocPropsExtendedPart() {
		if (docPropsExtendedPart==null) {
			try {
				docPropsExtendedPart = new org.docx4j.openpackaging.parts.DocPropsExtendedPart();
				this.addTargetPart(docPropsExtendedPart);
				
				docPropsExtendedPart.setJaxbElement(new org.docx4j.docProps.extended.Properties());
			} catch (InvalidFormatException e) {
				//Won't happen, so don't throw
				log.error(e.getMessage(), e);
			}			
		}
	}
	
	/**
	 * Get DocPropsCustomPart, if any.
	 * 
	 * @return
	 */
	public DocPropsCustomPart getDocPropsCustomPart() {
		
		return docPropsCustomPart;
	}
	
	public void addDocPropsCustomPart() {
		
		if (docPropsCustomPart==null) {
			try {
				docPropsCustomPart = new org.docx4j.openpackaging.parts.DocPropsCustomPart();
				docPropsCustomPart.setJaxbElement(new Properties());

				this.addTargetPart(docPropsCustomPart);
				
			} catch (InvalidFormatException e) {
				//Won't happen, so don't throw
				log.error(e.getMessage(), e);
			}			
		}
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
				log.error(e.getMessage(), e);
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
			log.error(e.getMessage(), e);
		}
		return sw.toString();				
	}
	

	/** @since 2.7.2 */
	@Override
	public OpcPackage clone() {
		
		OpcPackage result = null;
		
		// Zip it up
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Save saver = new Save(this);
		
		try {
			saver.save(baos);
			result = load(new ByteArrayInputStream(baos.toByteArray()));
		} catch (Docx4JException e) {
			// Shouldn't happen
			log.error(e.getMessage(), e);
		}

		return result;
		
	}
	
	/**
	 * @return
	 * @throws Docx4JException 
	 * @since 6.1.0
	 */
	public OpcPackage cloneAs(String targetContentType) throws Docx4JException {
		
		// This approach to cloning supports having different objects
		// for WordprocessingMLPackage and WordprocessingMLTemplatePackage,
		// should that distinction prove useful
		
		if (targetContentType.equals(this.getContentType())) {
			log.debug("No change to content type " + this.getContentType());
			return clone();
		}		
		
		// first temporarily change this content type to target
		// No need to change it in this pkg object, so we don't!
		//ContentType existing = this.contentType;
		String mainPartNameString = PackageRelsUtil.getNameOfMainPart(this.getRelationshipsPart());
		PartName mainPartName = new PartName("/" + mainPartNameString);
//		String pkgContentType = this.contentTypeManager.getContentType(new PartName("/" + mainPartName));
		this.contentTypeManager.removeOverrideContentType(mainPartName);
		this.contentTypeManager.addOverrideContentType(mainPartName.getURI(), targetContentType);
		
		// then clone it
		OpcPackage clone = this.clone();
		
		// now change content type back
		this.contentTypeManager.removeOverrideContentType(mainPartName);
		this.contentTypeManager.addOverrideContentType(mainPartName.getURI(), this.getContentType());
		
		return clone;
	}
	

	@Override
	public String name() {
		return name;
	}
	private String name;
	/**
	 * Allocate a name to this package, for the purposes of Docx4jEvent,
	 * and logging.
	 */
	public void setName(String name) {
		this.name = name;
	}
		
	/**
	 * Reinit fields so this pkg object can be re-used.
	 * @since 3.3.7
	 */
	@Override
	public void reset() {
		super.reset();
		handled = new HashMap<String, String>();
		parts = new Parts();
		externalResources = new HashMap<ExternalTarget, Part>();
		customXmlDataStorageParts= new HashMap<String, CustomXmlPart>();
		sourcePartStore=null;
		targetPartStore=null;
		docPropsCorePart=null;
		docPropsExtendedPart=null;
		docPropsCustomPart=null;
		name=null;
		log.info("reset complete");
	}
	
}
