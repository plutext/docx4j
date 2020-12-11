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

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConversionImageHandler implements ConversionImageHandler {
	
	protected static Logger log = LoggerFactory.getLogger(AbstractConversionImageHandler.class);
	protected String uuid = UUID.randomUUID().toString();
	protected Map<String, String> handledImagesMap = new TreeMap<String, String>();
	protected String imageDirPath = null;  // TODO FIXME should not be here; move to FileConversionImageHandler
	protected boolean includeUUID = true;
	
	/** Creates an AbstractConversionImageHandler
	 * @param imageDirPath, the path, where the images will be stored
	 * @param includeUUID, should the image names be prefixed with an UUID to differentiate different runs 
	 */
	protected AbstractConversionImageHandler(String imageDirPath, boolean includeUUID) {
		this.imageDirPath = (imageDirPath == null ? 
					System.getProperty("java.io.tmpdir") : 
					imageDirPath);
		this.includeUUID = includeUUID;
	}

	@Override
	public String handleImage(AbstractWordXmlPicture picture, Relationship relationship, BinaryPart part) throws Docx4JException {
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

	protected String doHandleImage(AbstractWordXmlPicture picture, Relationship relationship, BinaryPart part) throws Docx4JException {
	String uri = null;
		if (isInternalImage(picture, relationship, part)) {
			uri = handleInternalImage(picture, relationship, part);
		} else { // External
			uri = handleExternalImage(picture, relationship, part);
		}
		return uri;
	}

	protected boolean isInternalImage(AbstractWordXmlPicture picture, Relationship relationship, BinaryPart part) throws Docx4JException {
		//treat external images, that are loaded, as internal images
		return (part != null) &&
			   ((part.getExternalTarget() == null) || (part.getBuffer() != null)); 	
	}

	/**
	 * @param picture
	 * @param relationship
	 * @param binaryPart
	 * @return uri for the image we've saved, or null
	 */
	protected String handleInternalImage(AbstractWordXmlPicture picture, Relationship relationship, BinaryPart binaryPart) throws Docx4JException {
	byte[] bytes = getImageData(binaryPart);
	String uri = null;
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
			uri = createStoredImage(binaryPart, bytes);

		}
		return uri;
	}

	protected abstract String createStoredImage(BinaryPart binaryPart, byte[] bytes) throws Docx4JException;
	
	protected String createEncodedImage(BinaryPart binaryPart, byte[] bytes) throws Docx4JException {
	String uri = null;
	byte[] encoded = Base64.encodeBase64(bytes, true);
		try {
			uri = "data:" + binaryPart.getContentType()
			+ ";base64,"
			+ (new String(encoded, "UTF-8"));
		}
		catch (UnsupportedEncodingException uue) {
			uri = "data:" + binaryPart.getContentType()
			+ ";base64,"
			+ (new String(encoded));
		}
		return uri;
	}
	
	protected String setupImageName(BinaryPart binaryPart) {
	String filename = getImageName(binaryPart);
		
		// Don't want multiple threads using the same file
		filename = (includeUUID ? uuid + filename : filename);
		return filename;
	}
    
	/** Get the image base name
	 * 
	 * @param binaryPart
	 * @return
	 */
	protected String getImageName(BinaryPart binaryPart) {
	String partname = null;
	int p = -1;
		if (binaryPart.getExternalTarget() != null) {
			partname = binaryPart.getExternalTarget().getValue();
			p = partname.lastIndexOf('\\'); 
			if (p == -1) {
				p = partname.lastIndexOf('/');
			}
		}
		else {
			partname = binaryPart.getPartName().toString();
			p = partname.lastIndexOf('/');
		}
		return (p > -1 ? partname.substring(p + 1) : partname);
	}
    
	/** Get the image data of the buffer
	 * 
	 * @param binaryPart
	 * @return
	 */
	protected byte[] getImageData(BinaryPart binaryPart) {
		return binaryPart.getBytes();
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
