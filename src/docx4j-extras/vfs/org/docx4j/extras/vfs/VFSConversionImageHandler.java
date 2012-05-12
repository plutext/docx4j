package org.docx4j.extras.vfs;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.docx4j.model.images.AbstractConversionImageHandler;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;

public class VFSConversionImageHandler extends AbstractConversionImageHandler {
	
	/** Creates a DefaultConversionImageHandler with java.io.tmpdir as the target directory
	 */
	public VFSConversionImageHandler() {
		this(System.getProperty("java.io.tmpdir"), true);
	}
	
	/** Creates a DefaultConversionImageHandler with the supplied imageDirPath
	 */
	public VFSConversionImageHandler(String imageDirPath, boolean includeUUID) {
		super(imageDirPath, includeUUID);
	}

	@Override
	protected String createStoredImage(BinaryPart binaryPart, byte[] bytes) throws Docx4JException {
	String uri = null;
		try {
			// To create directory:
			FileObject folder = setupRootFolder(binaryPart);

			// Construct a file name from the part name
			String filename = setupImageName(binaryPart);
			log.debug("image file name: " + filename);

			uri = storeImage(binaryPart, bytes, folder, filename);
		}
		catch (FileSystemException fse) {
			throw new Docx4JException(fse.toString(), fse);
		}
		catch (IOException ioe) {
			throw new Docx4JException(ioe.toString(), ioe);
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
	
	protected FileObject setupRootFolder(BinaryPart binaryPart) throws FileSystemException {
	FileObject folder = VFSUtils.getFileSystemManager().resolveFile(imageDirPath);
		if (!folder.exists()) {
			folder.createFolder();
		}
		return folder;
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
}
