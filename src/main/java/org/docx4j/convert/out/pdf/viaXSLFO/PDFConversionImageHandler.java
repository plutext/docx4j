package org.docx4j.convert.out.pdf.viaXSLFO;

import java.io.File;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.model.images.FileConversionImageHandler;

/** This is a File-based ImageHandler, for generating images used in FO/PDF-documents
 */
public class PDFConversionImageHandler extends FileConversionImageHandler {
	
	private final static Logger log = LoggerFactory.getLogger(PDFConversionImageHandler.class);
	

	/** Create a PDFConversionImageHandler, 
	 * the images are generated in the java.io.tmpdir directory, runs are differentiated with an uuid
	 */
	public PDFConversionImageHandler() {
		super(null, true);
	}

	/** Create a PDFConversionImageHandler
	 * 
	 * @param imageDirPath Path where the images should be stored
	 * @param includeUUID, if a uuid should be used in the image name to differentiate the runs
	 */
	public PDFConversionImageHandler(String imageDirPath, boolean includeUUID) {
		super(imageDirPath, includeUUID);
	}

	@Override
	protected String setupImageUri(File imageFile) {
		// file:///"
		try {
			return imageFile.toURI().toURL().toString();
		} catch (MalformedURLException e) {
			log.error(e.getMessage(), e);
			return imageFile.getName();
		}
	}
}
