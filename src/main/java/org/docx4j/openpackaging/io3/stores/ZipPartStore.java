package org.docx4j.openpackaging.io3.stores;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io3.Load3;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.w3c.dom.Document;

/**
 * With this PartStore, the current content of
 * a part is as follows:
 * 
 * - for a JaxbXmlPart, the ByteArray, unless
 *   it has been unmarshalled (in which case
 *   it is that part's JaxbXmlElement)
 *   
 * - for any other part (including BinaryPart,
 *   CustomXmlDataStoragePart, XmlPart), 
 *   as contained in that part.
 * 
 * @author jharrop
 *
 */
public class ZipPartStore implements PartStore {
	
	private static Logger log = Logger.getLogger(Load3.class);
	
	
	HashMap<String, ByteArray> partByteArrays;
	
	public ZipPartStore(File f) throws Docx4JException {
		log.info("Filepath = " + f.getPath() );
		
		ZipFile zf = null;
		try {
			if (!f.exists()) {
				log.info( "Couldn't find " + f.getPath() );
			}
			zf = new ZipFile(f);
		} catch (IOException ioe) {
			ioe.printStackTrace() ;
			throw new Docx4JException("Couldn't get ZipFile", ioe);
		}
				
		partByteArrays = new HashMap<String, ByteArray>();
		Enumeration entries = zf.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			//log.info( "\n\n" + entry.getName() + "\n" );
			InputStream in = null;
			try {			
				byte[] bytes =  getBytesFromInputStream( zf.getInputStream(entry) );
				partByteArrays.put(entry.getName(), new ByteArray(bytes) );
			} catch (Exception e) {
				e.printStackTrace() ;
			}	
		}
		 // At this point, we've finished with the zip file
		 try {
			 zf.close();
		 } catch (IOException exc) {
			 exc.printStackTrace();
		 }
	}

	public ZipPartStore(InputStream is) throws Docx4JException {

		partByteArrays = new HashMap<String, ByteArray>();	
       try {
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry entry = null;
            while ((entry = zis.getNextEntry()) != null) {
				byte[] bytes =  getBytesFromInputStream( zis );
				//log.debug("Extracting " + entry.getName());
				partByteArrays.put(entry.getName(), new ByteArray(bytes) );
            }
            zis.close();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new Docx4JException("Error processing zip file (is it a zip file?)", e);
        }	
	            
	}
	
	/////// Load methods
	
	
	public boolean partExists(String partName) {
		return (partByteArrays.get(partName) !=null );
	}
	
	public static byte[] getBytesFromInputStream(InputStream is)
			throws Exception {
			
			BufferedInputStream bufIn = new BufferedInputStream(is);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedOutputStream bos = new BufferedOutputStream(baos);
			int c = bufIn.read();
			while (c != -1) {
				bos.write(c);
				c = bufIn.read();
			}
			bos.flush();
			baos.flush();		
			//bufIn.close(); //don't do that, since it closes the ZipInputStream after we've read an entry!
			bos.close();
			return baos.toByteArray();
		} 			
	
//	private static InputStream getInputStreamFromZippedPart(HashMap<String, ByteArray> partByteArrays,
//			String partName) throws IOException {
//		
//        ByteArray bytes = partByteArrays.get(partName);
//        if (bytes == null) throw new IOException("part '" + partName + "' not found");
//		return bytes.getInputStream();
//	}

//	protected InputStream getInputStreamFromZippedPart(String partName) throws IOException {
	public InputStream loadPart(String partName) throws IOException {
		
        ByteArray bytes = partByteArrays.get(partName);
        if (bytes == null) throw new IOException("part '" + partName + "' not found");
		return bytes.getInputStream();
	}

	///// Save methods
	
	private ZipOutputStream zos;
	
	/**
	 * @param zipOutputStream the zipOutputStream to set
	 */
	public void setOutputStream(OutputStream os) {
		this.zos = new ZipOutputStream(os);
	}

	public void saveContentTypes(ContentTypeManager ctm) throws Docx4JException {
	
		try {
			
	        zos.putNextEntry(new ZipEntry("[Content_Types].xml"));
	        ctm.marshal(zos);
	        zos.closeEntry();
	        
		} catch (Exception e) {
			throw new Docx4JException("Error marshalling Content_Types ", e);
		}
	
	}
	
	public void saveJaxbXmlPart(JaxbXmlPart part, String targetName) throws Docx4JException {
		
		try {
	        // Add ZIP entry to output stream.
	        zos.putNextEntry(new ZipEntry(targetName));		        
	
	        if (part.isUnmarshalled() ) {
	        	log.debug("marshalling " + part.getPartName() );
	        	part.marshal( zos );
	        } else {
	        	// Just use the ByteArray
	        	log.debug(part.getPartName() + " is clean" );
	            ByteArray bytes = partByteArrays.get(
	            		part.getPartName().getName().substring(1) );
	            if (bytes == null) throw new IOException("part '" + part.getPartName() + "' not found");
		        zos.write( bytes.getBytes() );
	        	
	        }
	        
	        // Complete the entry
	        zos.closeEntry();
	        
		} catch (Exception e) {
			throw new Docx4JException("Error marshalling JaxbXmlPart " + part.getPartName(), e);
		}
	}
	
	public void saveCustomXmlDataStoragePart(CustomXmlDataStoragePart part, String targetName) throws Docx4JException {
		
		try {
			
	        // Add ZIP entry to output stream.
	        zos.putNextEntry(new ZipEntry(targetName));		        
	
	        part.getData().writeDocument( zos );
	        
	        // Complete the entry
	        zos.closeEntry();
	        
		} catch (Exception e) {
			throw new Docx4JException("Error marshalling CustomXmlDataStoragePart " + part.getPartName(), e);
		}
		
	}
	
	public void saveXmlPart(XmlPart part, String targetName) throws Docx4JException {
	
		try {
			
		    // Add ZIP entry to output stream.
		    zos.putNextEntry(new ZipEntry(targetName));		        
		
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
					 new StreamResult(zos) );
		   
		    
		    // Complete the entry
		    zos.closeEntry();
		    
		} catch (Exception e) {
			throw new Docx4JException("Error marshalling XmlPart " + part.getPartName(), e);
		}
	}
	
	public void saveBinaryPart(Part part) throws Docx4JException {

		// Drop the leading '/'
		String resolvedPartUri = part.getPartName().getName().substring(1);

		try {
	        // Add ZIP entry to output stream.
	        zos.putNextEntry(new ZipEntry(resolvedPartUri));
	        	        
            java.nio.ByteBuffer bb = ((BinaryPart)part).getBuffer();
            byte[] bytes = null;
            bytes = new byte[bb.limit()];
            bb.get(bytes);	        
	        
	        zos.write( bytes );

			// Complete the entry
	        zos.closeEntry();
			
		} catch (Exception e ) {
			throw new Docx4JException("Failed to put binary part", e);			
		}
		
		log.info( "success writing part: " + resolvedPartUri);		
		
	}
	
	public void finishSave() throws Docx4JException {

		try {
			// Complete the ZIP file
			// Don't forget to do this or everything will appear
			// to work, but when you open the zip file you'll get an error
			// "End-of-central-directory signature not found."
	        zos.close();
		} catch (Exception e ) {
			throw new Docx4JException("Failed to put binary part", e);			
		}
		
	}
	
	
	public static class ByteArray implements Serializable {
		
		private static final long serialVersionUID = -784146312250361899L;
		// 4469266984448028582L; 
		
		private byte[] bytes;
		public byte[] getBytes() {
			return bytes;
		}

		private String mimetype;
		public String getMimetype() {
			return mimetype;			
		}
		
		public ByteArray(byte[] bytes) {
			this.bytes = bytes;
			//log.info("Added " + bytes.length  );
		}
		
		
		public ByteArray(ByteBuffer bb, String mimetype ) {
			
			bb.clear();
			bytes = new byte[bb.capacity()];
			bb.get(bytes, 0, bytes.length);
			
			this.mimetype = mimetype;
		}
		
		
		public InputStream getInputStream() {
			return new ByteArrayInputStream(bytes);
		}

		public int getLength() {
			return bytes.length;			
		}
		
	}
	
}
