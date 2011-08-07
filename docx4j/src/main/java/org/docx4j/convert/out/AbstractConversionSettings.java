package org.docx4j.convert.out;

import java.util.Map;
import java.util.TreeMap;

import org.docx4j.model.images.ConversionImageHandler;
import org.docx4j.openpackaging.packages.OpcPackage;

public abstract class AbstractConversionSettings {
	
	public static final String IMAGE_INCLUDE_UUID = "imageIncludeUUID";
	public static final String IMAGE_DIR_PATH = "imageDirPath";
	public static final String IMAGE_HANDLER = "imageHandler";
	public static final String WML_PACKAGE = "wmlPackage";

	protected Map<String, Object> settings = new TreeMap<String, Object>();
	
	public Map<String, Object> getSettings() {
		return settings;
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
	
	public void setWmlPackage(OpcPackage wmlPackage) {
		settings.put("wmlPackage", wmlPackage);
	}
	public OpcPackage getWmlPackage() {
		return (OpcPackage)settings.get("wmlPackage");
	}
}
