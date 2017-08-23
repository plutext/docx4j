/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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
		log.debug("image folder: " + folder.getAbsolutePath());

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
