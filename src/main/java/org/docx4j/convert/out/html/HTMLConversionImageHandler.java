package org.docx4j.convert.out.html;

import java.io.File;

import org.docx4j.model.images.FileConversionImageHandler;

/** This is a File-based ImageHandler, for generating images used in HTML-documents
 */
public class HTMLConversionImageHandler extends FileConversionImageHandler {
	protected String targetUri = null;

	/** Creates as HTMLConversionImageHandler
	 * 
	 * @param imageDirPath, the target path where images are stored 
	 * @param targetUri, the uri prefix that will be used in the generated HTML
	 * @param includeUUID, if a uuid should be included in the image name to differentiate the images of different runs
	 */
	public HTMLConversionImageHandler(String imageDirPath, String targetUri, boolean includeUUID) {
		super(imageDirPath, includeUUID);
		this.targetUri = targetUri;
	}
	
	/** If there is a prefix use this prefix for the uri
	 */
	@Override
    protected String setupImageUri(File imageFile) {
    String uri = null;
    	if ((targetUri == null) || (targetUri.length() == 0)) {
    		uri = imageFile.getName();
    	}
    	else {
    		uri = ((targetUri.charAt(targetUri.length() - 1) != '/') ?
    				targetUri + '/' + imageFile.getName() :
    				targetUri + imageFile.getName());
    	}
    	return uri;
    }
	
}
