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
package org.docx4j.convert.out;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.docx4j.model.images.ConversionImageHandler;
import org.docx4j.openpackaging.packages.OpcPackage;

/**
 * The Settings classes pass everything that’s needed to the conversion process. 
 * 
 * @since 3.0
 */
public abstract class AbstractConversionSettings { //implements ConversionFeatures {
	
	public static final String IMAGE_INCLUDE_UUID = "imageIncludeUUID";
	public static final String IMAGE_DIR_PATH = "imageDirPath";
	public static final String IMAGE_HANDLER = "imageHandler";
	public static final String HYPERLINK_HANDLER = "hyperlinkHandler";
	public static final String OPC_PACKAGE = "opcPackage";
	public static final String CUSTOM_XSLT_TEMPLATES = "customXsltTemplates";

	protected Map<String, Object> settings = new TreeMap<String, Object>();
	protected Set<String> features = new TreeSet<String>();
	
	public Map<String, Object> getSettings() {
		return settings;
	}
	
	public Set<String> getFeatures() {
		return features;
	}
	
	public void addFeatures(String[] featuresArray) {
		if ((featuresArray != null) && (featuresArray.length > 0)) {
			for (int i=0; i<featuresArray.length; i++) {
				getFeatures().add(featuresArray[i]);
			}
		}
	}
	
	// If this is set to something, images in
	// internal binary parts will be saved to this directory;
	// otherwise they won't
	public void setImageDirPath(String imageDirPath) {
		settings.put(IMAGE_DIR_PATH, imageDirPath);
	}
	public String getImageDirPath() {
		return (String)settings.get(IMAGE_DIR_PATH);
	}

	/** Should the image names be prefixed with an UUID to differentiate runs? Default: true
	 */
	public void setImageIncludeUUID(boolean imageIncludeUUID) {
		settings.put(IMAGE_INCLUDE_UUID, Boolean.valueOf(imageIncludeUUID));
	}
	
	public boolean isImageIncludeUUID() {
		return (settings.containsKey(IMAGE_INCLUDE_UUID) ? 
				(Boolean)settings.get(IMAGE_INCLUDE_UUID) : 
				true);
	}

	public void setImageHandler(ConversionImageHandler imageHandler) {
		settings.put(IMAGE_HANDLER, imageHandler);
	}
	public ConversionImageHandler getImageHandler() {
		return (ConversionImageHandler)settings.get(IMAGE_HANDLER);
	}

	public void setHyperlinkHandler(ConversionHyperlinkHandler hyperlinkHandler) {
		settings.put(HYPERLINK_HANDLER, hyperlinkHandler);
	}
	public ConversionHyperlinkHandler getHyperlinkHandler() {
		return (ConversionHyperlinkHandler)settings.get(HYPERLINK_HANDLER);
	}
	
	@Deprecated
	public void setWmlPackage(OpcPackage wmlPackage) {
		settings.put(OPC_PACKAGE, wmlPackage);
	}
	public void setOpcPackage(OpcPackage wmlPackage) {
		settings.put(OPC_PACKAGE, wmlPackage);
	}
	public OpcPackage getOpcPackage() {
		return (OpcPackage)settings.get(OPC_PACKAGE);
	}
	
	/** Pass a custom xslt template to do the transformation.<br> 
	 * This template will only be used if you have selected an export that uses 
	 * templates. The signature is defined as Object templates, to not introduce a 
	 * dependency on javax.xml.transform - but don't expect it to work if you pass
	 * something different than a javax.xml.transform.Templates. 
	 */
	public void setCustomXsltTemplates(Object templates) {
		settings.put(CUSTOM_XSLT_TEMPLATES, templates);
	}
	public Object getCustomXsltTemplates() {
		return settings.get(CUSTOM_XSLT_TEMPLATES);
	}
	
}
