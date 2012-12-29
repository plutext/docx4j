package org.docx4j.openpackaging.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.Docx4JException;

public class ZipPartStore implements PartStore {
	
	private static Logger log = Logger.getLogger(LoadNG2.class);
	
	
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
	public InputStream getInputStreamForPart(String partName) throws IOException {
		
        ByteArray bytes = partByteArrays.get(partName);
        if (bytes == null) throw new IOException("part '" + partName + "' not found");
		return bytes.getInputStream();
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
