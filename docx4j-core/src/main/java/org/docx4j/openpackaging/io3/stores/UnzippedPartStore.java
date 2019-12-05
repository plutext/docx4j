/*
 *  Copyright 2012, Plutext Pty Ltd.
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
package org.docx4j.openpackaging.io3.stores;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.Docx4JRuntimeException;
import org.docx4j.openpackaging.io3.stores.ZipPartStore.ByteArray;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Load an unzipped package from the file system;
 * save it to some output stream.
 *
 * TODO convert path sep in part name to suit
 * underlying file system.
 *
 * @author jharrop
 * @since 3.0
 */
public class UnzippedPartStore implements PartStore {

	private static Logger log = LoggerFactory.getLogger(UnzippedPartStore.class);


	private File dir;

	public UnzippedPartStore(File dir) throws Docx4JException {

		this.dir = dir;

	}

	private PartStore sourcePartStore;

	/**
	 * Set this if its different to the target part store
	 * (ie this object)
	 */
	public void setSourcePartStore(PartStore partStore) {
		this.sourcePartStore = partStore;
	}

	/////// Load methods

//	public boolean partExists(String partName) {
//
//		String filePath = dir.getPath() + dir.separator + partName;
//
//		return (new File(filePath)).exists();
//
//	}

//	public static byte[] getBytesFromInputStream(InputStream is)
//			throws Exception {
//
//			BufferedInputStream bufIn = new BufferedInputStream(is);
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			BufferedOutputStream bos = new BufferedOutputStream(baos);
//			int c = bufIn.read();
//			while (c != -1) {
//				bos.write(c);
//				c = bufIn.read();
//			}
//			bos.flush();
//			baos.flush();
//			//bufIn.close(); //don't do that, since it closes the ZipInputStream after we've read an entry!
//			bos.close();
//			return baos.toByteArray();
//		}

	public InputStream loadPart(String partName) throws  Docx4JException {

		String filePath = dir.getPath() + dir.separator + partName;

		System.out.println("Using " + filePath);

		InputStream is;
		try {
			is = new FileInputStream(new File(filePath)); 
		} catch (FileNotFoundException e) {
			return null;
//			throw new Docx4JException(e.getMessage(), e);
		}
//        if (is == null) throw new Docx4JException("part '" + partName + "' not found at " + filePath );
		return is;
	}
	
	@Override
	public long getPartSize(String partName) throws Docx4JException {
		
		String filePath = dir.getPath() + dir.separator + partName;
		
		File f = new File(filePath);
		if (f.exists()) {
			return f.length();
		} else {
			return -1;
		}
	}
	
	/**
	 * @param oldName
	 * @param newName
	 * @since 8.1.4
	 */
	public void rename(PartName oldName, PartName newName) {
		
		log.info("Renaming part " + oldName.getName() + " to " + newName.getName() );

		String filePath = dir.getPath() + dir.separator + oldName.getName();
		File f = new File(filePath); 
		f.renameTo(new File(dir.getPath() + dir.separator + newName.getName()) );
		
	}
	
	///// Save methods

	/**
	 * Does nothing
	 */
	public void setOutputStream(OutputStream os) {
		// Nothing to do
	}

	public void saveContentTypes(ContentTypeManager ctm) throws Docx4JException {

		try {

			String filePath = dir.getPath() + dir.separator + "[Content_Types].xml";
			FileOutputStream fos = new FileOutputStream(new File(filePath));
	        ctm.marshal(fos);
	        fos.close();

		} catch (Exception e) {
			throw new Docx4JException("Error marshalling Content_Types ", e);
		}

	}

	public void saveJaxbXmlPart(JaxbXmlPart part) throws Docx4JException {

		String targetName;
		if (part.getPartName().getName().equals("_rels/.rels")) {
			targetName = part.getPartName().getName();
		} else {
			targetName = part.getPartName().getName().substring(1);
		}

		String filePath = dir.getPath() + dir.separator + targetName;
		System.out.println("Saving " + filePath);
		try {

			File file = new File(filePath);
			file.getParentFile().mkdirs();

			if (part.isUnmarshalled() ) {

				FileOutputStream fos = new FileOutputStream(file);
	        	log.debug("marshalling " + part.getPartName() );
	        	part.marshal( fos );
		        fos.close();

	        } else {

	        	if (!file.exists()
	        			&& this.sourcePartStore==null) {
	        		throw new Docx4JException("part store has changed, and sourcePartStore not set");
	        	} else {
	        		InputStream is = sourcePartStore.loadPart(part.getPartName().getName().substring(1));
					FileOutputStream fos = new FileOutputStream(file);

	        		int read = 0;
	        		byte[] bytes = new byte[1024];

	        		while ((read = is.read(bytes)) != -1) {
	        			fos.write(bytes, 0, read);
	        		}

	        		is.close();
	        		fos.flush();
	        		fos.close();

	        	}

	        }

		} catch (Exception e) {
			throw new Docx4JException("Error marshalling JaxbXmlPart " + part.getPartName(), e);
		}
	}

	public void saveCustomXmlDataStoragePart(CustomXmlDataStoragePart part) throws Docx4JException {

		String targetName = part.getPartName().getName().substring(1);

		String filePath = dir.getPath() + dir.separator + targetName;

		File file = new File(filePath);
		file.getParentFile().mkdirs();

		try {

			FileOutputStream fos = new FileOutputStream(file);
	        part.getData().writeDocument( fos );
	        fos.close();

		} catch (Exception e) {
			throw new Docx4JException("Error marshalling CustomXmlDataStoragePart " + part.getPartName(), e);
		}

	}

	public void saveXmlPart(XmlPart part) throws Docx4JException {

		String targetName = part.getPartName().getName().substring(1);

		String filePath = dir.getPath() + dir.separator + targetName;
		File file = new File(filePath);
		file.getParentFile().mkdirs();

		try {

			FileOutputStream fos = new FileOutputStream(file);

		   Document doc =  part.getDocument();

			/*
			 * With Crimson, this gives:
			 *
				Exception in thread "main" java.lang.AbstractMethodError: org.apache.crimson.tree.XmlDocument.getXmlStandalone()Z
					at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.setDocumentInfo(DOM2TO.java:373)
					at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.parse(DOM2TO.java:127)
					at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.parse(DOM2TO.java:94)
					at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transformIdentity(TransformerImpl.java:662)
					at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transform(TransformerImpl.java:708)
					at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transform(TransformerImpl.java:313)
					at org.docx4j.model.datastorage.CustomXmlDataStorageImpl.writeDocument(CustomXmlDataStorageImpl.java:174)
			 *
			 */
			DOMSource source = new DOMSource(doc);
			 XmlUtils.getTransformerFactory().newTransformer().transform(source,
					 new StreamResult(fos) );

		    fos.close();

		} catch (Exception e) {
			throw new Docx4JException("Error marshalling XmlPart " + part.getPartName(), e);
		}
	}

	public void saveBinaryPart(Part part) throws Docx4JException {


		// Drop the leading '/'
		String resolvedPartUri = part.getPartName().getName().substring(1);
		String filePath = dir.getPath() + dir.separator + resolvedPartUri;
		System.out.println("saveBinaryPart " + filePath);

		File file = new File(filePath);
		file.getParentFile().mkdirs();


		try {
			FileOutputStream fos;

	        if (((BinaryPart)part).isLoaded() ) {

	        	fos = new FileOutputStream(file);
		        fos.write( ((BinaryPart)part).getBytes() );
			    fos.close();

	        } else {

	        	if (file.exists() ) {

		        	// No need to save .. 
	        		// either source = target,
	        		// or incrementally saved to target already
	        	
	        	} else if (this.sourcePartStore==null) {

	        		throw new Docx4JException("part store has changed, and sourcePartStore not set");

	        	} else {

	        		InputStream is = sourcePartStore.loadPart(part.getPartName().getName().substring(1));
	        		int read = 0;
	        		byte[] bytes = new byte[1024];

		        	fos = new FileOutputStream(file);

	        		while ((read = is.read(bytes)) != -1) {
	        			fos.write(bytes, 0, read);
	        		}
	        		is.close();
	        	}
	        }


		} catch (Exception e ) {
			throw new Docx4JException("Failed to put binary part", e);
		}

		log.info( "success writing part: " + resolvedPartUri);

	}

	@Override
	public void finishSave() throws Docx4JException {
		// nothing to do
	}

	@Override
	public void dispose() {
		// nothing to do
	}






}
