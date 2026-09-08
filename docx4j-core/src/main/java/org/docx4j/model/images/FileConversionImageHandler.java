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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.docx4j.Docx4jProperties;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;

/** The DefaultConversionImageHandler is a pure File-based ImageHandler.
 *
 */
public class FileConversionImageHandler extends AbstractConversionImageHandler {

	/**
	 * docx4j.convert.out.images.deleteTemporary (default true): whether docx4j
	 * deletes the image files it wrote into a directory it chose itself.
	 *
	 * <p>Where the caller supplies no imageDirPath, the images a conversion needs
	 * are written to {@code java.io.tmpdir}, one per picture per run (the names are
	 * prefixed with a UUID).  Nothing used to delete them, so a process doing
	 * docx&rarr;PDF or docx&rarr;HTML conversions filled its temp directory.
	 * Set this false to keep them (docx4j to 17.1.0 behaviour).</p>
	 *
	 * <p>Files written to a directory the caller named are the caller's, and are
	 * never deleted whatever this says.</p>
	 *
	 * @since 17.1.1
	 */
	public static final String DELETE_TEMPORARY_PROPERTY = "docx4j.convert.out.images.deleteTemporary";

	/** True where docx4j chose the directory (java.io.tmpdir), so the files in it
	 *  are docx4j's to delete.  @since 17.1.1 */
	protected final boolean temporaryImageDir;

	/** The files written, where they are ours to delete.  @since 17.1.1 */
	private final List<File> temporaryImages = Collections.synchronizedList(new ArrayList<File>());

	/** Creates a DefaultConversionImageHandler.
	 * @param imageDirPath
	 * @param targetUri
	 * @param includeUUID
	 */
	public FileConversionImageHandler(String imageDirPath, boolean includeUUID) {
		super(imageDirPath, includeUUID);
		// AbstractConversionImageHandler defaults a null imageDirPath to java.io.tmpdir;
		// only in that case did docx4j choose where the files went.
		this.temporaryImageDir = (imageDirPath == null);
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

			trackTemporaryImage(imageFile);

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

	/** Remember a file we wrote, so that {@link #cleanupTemporaryImages(boolean)}
	 *  can delete it - but only where docx4j chose the directory it went into.
	 *
	 * @since 17.1.1
	 */
	protected void trackTemporaryImage(File imageFile) {
		if (temporaryImageDir) {
			temporaryImages.add(imageFile);
		}
	}

	/** {@inheritDoc}
	 *
	 * @since 17.1.1
	 */
	@Override
	public void cleanupTemporaryImages(boolean deferToJvmExit) {

		if (!temporaryImageDir) return; // the caller's directory; the files are theirs
		if (!Docx4jProperties.getProperty(DELETE_TEMPORARY_PROPERTY, true)) return;

		synchronized (temporaryImages) {
			for (File imageFile : temporaryImages) {
				try {
					if (deferToJvmExit) {
						// the output still points at this file
						imageFile.deleteOnExit();
					} else if (!imageFile.delete() && imageFile.exists()) {
						log.debug("Couldn't delete {}; deferring to JVM exit", imageFile.getPath());
						imageFile.deleteOnExit();
					}
				} catch (Exception e) {
					// eg a SecurityManager: never let cleanup fail a conversion
					log.warn("Couldn't clean up " + imageFile.getPath() + ": " + e.getMessage());
				}
			}
			temporaryImages.clear();
		}
		if (!deferToJvmExit) {
			// the uris in there point at files which no longer exist, so were this
			// handler reused, it would hand them out again
			handledImagesMap.clear();
		}
	}
}
