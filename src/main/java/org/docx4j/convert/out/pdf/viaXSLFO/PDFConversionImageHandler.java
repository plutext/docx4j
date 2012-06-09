/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.convert.out.pdf.viaXSLFO;

import java.io.File;

import org.docx4j.model.images.FileConversionImageHandler;

/** This is a File-based ImageHandler, for generating images used in FO/PDF-documents
 */
public class PDFConversionImageHandler extends FileConversionImageHandler {

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
		return imageFile.getAbsolutePath();
	}
}
