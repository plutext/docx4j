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
