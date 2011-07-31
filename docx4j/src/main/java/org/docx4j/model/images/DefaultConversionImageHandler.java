package org.docx4j.model.images;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.log4j.Logger;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.utils.VFSUtils;

public class DefaultConversionImageHandler implements ConversionImageHandler {
	
	protected static Logger log = Logger.getLogger(DefaultConversionImageHandler.class);
	protected String uuid = UUID.randomUUID().toString();
	protected String imageDirPath = null;
	protected Map<String, String> handledImagesMap = new TreeMap<String, String>();
	
	/** Creates a DefaultConversionImageHandler with java.io.tmpdir as the target directory
	 */
	public DefaultConversionImageHandler() {
		this(System.getProperty("java.io.tmpdir"));
	}
	
	/** Creates a DefaultConversionImageHandler with the supplied imageDirPath
	 */
	public DefaultConversionImageHandler(String imageDirPath) {
		this.imageDirPath = imageDirPath;
	}

	@Override
	public String handleImage(AbstractWordXmlPicture picture, Relationship relationship, BinaryPart part) {
	String key = createKey(relationship, part);
	String uri = null;
		if (handledImagesMap.containsKey(key)) {
			uri = handledImagesMap.get(key);
		}
		else {
			uri = doHandleImage(picture, relationship, part);
			handledImagesMap.put(key, uri);
		}
		return uri;
	}

	protected String createKey(Relationship relationship, BinaryPart part) {
		return relationship.getTarget();
	}

	protected String doHandleImage(AbstractWordXmlPicture picture, Relationship relationship, BinaryPart part) {
	String uri = null;
		if ((relationship.getTargetMode() == null) || 
			(relationship.getTargetMode().equals("Internal"))) {
			uri = handleInternalImage(picture, relationship, part);
		} else { // External
			uri = handleExternalImage(picture, relationship, part);
		}
		return uri;
	}

	/**
	 * @param picture
	 * @param relationship
	 * @param binaryPart
	 * @return uri for the image we've saved, or null
	 */
	protected String handleInternalImage(AbstractWordXmlPicture picture, Relationship relationship, BinaryPart binaryPart) {
	byte[] bytes = getImageData(binaryPart);
	String uri = null;
		try {

			if (imageDirPath.equals("")) {
				
				// TODO: this isn't going to work for XSL FO!
				// So for XSL FO, you always need an imageDirPath! 

				// <img
				// src="data:image/gif;base64,R0lGODlhEAAOALMAAOazToeHh0tLS/7LZv/0jvb29t/f3//Ub/
				//
				// which is nice, except it doesn't work in IE7,
				// and is limited to 32KB in IE8!
				uri = createEncodedImage(binaryPart, bytes);

			} else {
				// To create directory:
				FileObject folder = setupRootFolder(binaryPart);

				// Construct a file name from the part name
				String filename = setupImageName(binaryPart);
				log.debug("image file name: " + filename);

				uri = storeImage(binaryPart, bytes, folder, filename);

			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
		return uri;
	}

	protected String storeImage(BinaryPart binaryPart, byte[] bytes, FileObject folder, String filename) throws FileSystemException, IOException {
	String uri = null;
	FileObject imageFileObject = folder.resolveFile(filename);
	
		if (imageFileObject.exists()) {
			log.warn("Overwriting (!) existing file!");
		} else {
			imageFileObject.createFile();
		}
		
		// System.out.println("URL: " +
		// fo.getURL().toExternalForm() );
		// System.out.println("String: " + fo.toString() );

		// Save the file
		OutputStream out = imageFileObject.getContent().getOutputStream();
		// instance of org.apache.commons.vfs.provider.DefaultFileContent$FileContentOutputStream
		// which extends MonitorOutputStream
		// which in turn extends BufferedOutputStream
		// which in turn extends FilterOutputStream.
		
		try {
			out.write(bytes);
			
			// return the uri
			uri = fixImgSrcURL(imageFileObject);
			log.info("Wrote @src='" + uri);
		} finally {
			try {
				imageFileObject.close();
				// That Closes this file, and its content.
				// Closing the content in turn
				// closes any open stream.
				// out.flush() is unnecessary, since 
				// FilterOutputStream's close() does do flush() first.
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}					
		}
		return uri;
	}

	protected String createEncodedImage(BinaryPart binaryPart, byte[] bytes) throws UnsupportedEncodingException {
	String uri = null;
	byte[] encoded = Base64.encodeBase64(bytes, true);
		uri = "data:" + binaryPart.getContentType()
		+ ";base64,"
		+ (new String(encoded, "UTF-8"));
		return uri;
	}
	
	protected FileObject setupRootFolder(BinaryPart binaryPart) throws FileSystemException {
	FileObject folder = VFSUtils.getFileSystemManager().resolveFile(imageDirPath);
		if (!folder.exists()) {
			folder.createFolder();
		}
		return folder;
	}
	
	protected String setupImageName(BinaryPart binaryPart) {
	String filename = getImageName(binaryPart);
		
		// Don't want multiple threads using the same file
		filename = uuid + filename;
		return filename;
	}
    
	/** Get the image base name
	 * 
	 * @param binaryPart
	 * @return
	 */
	protected String getImageName(BinaryPart binaryPart) {
	String partname = binaryPart.getPartName().toString();
		return partname.substring(partname.lastIndexOf("/") + 1);
	}
    
	/** Get the image data of the buffer
	 * 
	 * @param binaryPart
	 * @return
	 */
	protected byte[] getImageData(BinaryPart binaryPart) {
	byte[] bytes = null;
	ByteBuffer bb = binaryPart.getBuffer();
		bb.clear();
		bytes = new byte[bb.capacity()];
		bb.get(bytes, 0, bytes.length);
		return bytes;
	}
	
	/**
	 * imageDirPath is anything VFSJFileChooser can resolve into a FileObject. 
	 * That's enough for saving the image. In order for a web browser to
	 * display it, the URI Scheme has to be something a web browser can
	 * understand. So at that point, webdav:// will have to become http://, 
	 * and smb:// become file:// ...
	 */
    protected String fixImgSrcURL(FileObject fo) {   	
    	String itemUrl = null;
		try {
			itemUrl = fo.getURL().toExternalForm();
			log.debug(itemUrl);

			String itemUrlLower = itemUrl.toLowerCase();			
	        if (itemUrlLower.startsWith("http://") 
	        		 || itemUrlLower.startsWith("https://")) {
				return itemUrl;
			} else if (itemUrlLower.startsWith("file://")) {
				// we'll convert file protocol to relative reference
				// if this is html output
				
				if (fo.getParent() == null) {
					return itemUrl;					
				} else if (fo.getParent().getURL().toExternalForm().equalsIgnoreCase(
						VFSUtils.getFileSystemManager().resolveFile(System.getProperty("java.io.tmpdir")).getURL().toExternalForm() )) {
					
					// The image is being stored in the system temp directory,
					// so assume this is a pdf export, and preserve the absolute
					// file path

					// org.apache.commons.vfs.provider.local.LocalFile has a
					// method doIsSameFile, but the point of using FileObject is
					// that it won't necessarily be a local file. 
					
					return itemUrl;						
				} else {
		             // Otherwise, assume it is an html export and return a relative path
					return  fo.getParent().getName().getBaseName() 
								+ "/" + fo.getName().getBaseName();
				}
				
			} else if (itemUrlLower.startsWith("webdav://")) {
				// TODO - convert to http:, dropping username / password
				return itemUrl;
			} 			
	        log.warn("How to handle scheme: " + itemUrl );        
		} catch (FileSystemException e) {
			log.error("Problem fixing Img Src URL", e);
		}		    	
    	return itemUrl;        
    }


	/**
	 * @param picture
	 * @param relationship
	 * @param part (is always null)
	 * @return uri for the image we've saved, or null
	 */
	protected String handleExternalImage(AbstractWordXmlPicture picture, Relationship relationship, BinaryPart part) {
		return relationship.getTarget();
	}
	

	/** If the instance is reused, it should be cleared first
	 */
	public void clear() {
		uuid = UUID.randomUUID().toString();
		handledImagesMap.clear();		
	}
}
