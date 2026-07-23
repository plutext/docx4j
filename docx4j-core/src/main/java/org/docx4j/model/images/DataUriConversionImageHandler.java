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

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;

/** An ImageHandler which embeds images in the output document as
 *  RFC 2397 data URIs (eg src="data:image/png;base64,...."),
 *  instead of storing them on the file system.
 *
 *  For HTML/XHTML output, use it via
 *  htmlSettings.setImageHandler(new DataUriConversionImageHandler()).
 *
 *  Notes:
 *  - external (linked) images are left as their original URL;
 *  - image formats a browser can't render (eg WMF, EMF, TIFF) are
 *    embedded as-is, so won't display, just as they wouldn't if
 *    stored in an image dir;
 *  - base64 encoding makes the image bytes around 33% larger, and the
 *    data URI is repeated at each use of the image in the output;
 *  - don't use this for XSL FO (PDF output), which needs image files.
 *
 *  @since 17.0.1
 */
public class DataUriConversionImageHandler extends AbstractConversionImageHandler {

	public DataUriConversionImageHandler() {
		// empty (not null) imageDirPath routes handleInternalImage
		// to createEncodedImage
		super("", false);
	}

	@Override
	protected String createStoredImage(BinaryPart binaryPart, byte[] bytes) throws Docx4JException {
		// never store to disk
		return createEncodedImage(binaryPart, bytes);
	}
}
