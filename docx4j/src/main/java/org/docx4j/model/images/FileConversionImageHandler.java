package org.docx4j.model.images;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;

/** The DefaultConversionImageHandler is a pure File-based ImageHandler. 
 * 
 */
public class FileConversionImageHandler extends AbstractConversionImageHandler {
	
	/** Creates a DefaultConversionImageHandler.
	 * @param imageDirPath 
	 * @param targetUri
	 * @param includeUUID
	 */
	public FileConversionImageHandler(String imageDirPath, boolean includeUUID) {
		super(imageDirPath, includeUUID);
	}

	@Override
	protected String createStoredImage(BinaryPart binaryPart, byte[] bytes) throws Docx4JException {
	String uri = null;
		// To create directory:
		File folder = setupRootFolder(binaryPart);

		// Construct a file name from the part name
		String filename = setupImageName(binaryPart);
		log.debug("image file name: " + filename);

		uri = storeImage(binaryPart, bytes, folder, filename);
		return uri;
	}

	protected String storeImage(BinaryPart binaryPart, byte[] bytes, File folder, String filename) throws Docx4JException {
	String uri = null;
	File imageFile = new File(folder, filename);
	FileOutputStream out = null;
	
		if (imageFile.exists()) {
			log.warn("Overwriting (!) existing file!");
		}
		try {
			out = new FileOutputStream(imageFile);
			out.write(bytes);
			
			// return the uri
			uri = setupImageUri(imageFile);
			log.info("Wrote @src='" + uri);
		} catch (IOException ioe) {
			throw new Docx4JException("Exception storing '" + filename + "', " + ioe.toString(), ioe);
		} finally {
			try {
				out.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}					
		}
		return uri;
	}
	
	protected File setupRootFolder(BinaryPart binaryPart) throws Docx4JException {
	File folder = new File(imageDirPath);
		if ((folder.exists()) && (!folder.isDirectory())) {
			throw new Docx4JException("Invalid imageDirPath '" + imageDirPath + ", it isn't a directory");
		}
		if (!folder.exists()) {
			if (!folder.mkdirs()) {
				throw new Docx4JException("Invalid imageDirPath '" + imageDirPath + ", could not create the directory");
			}
		}
		return folder;
	}
	
	/** If there is a prefix use this prefix for the uri
	 */
    protected String setupImageUri(File imageFile) {
    	return imageFile.getName();
    }
}
