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

import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

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
		String variant = compressedVariant(picture, relationship, part);
		if (variant != null) {
			String variantKey = key + "#" + variant;
			if (handledImagesMap.containsKey(variantKey)) return handledImagesMap.get(variantKey);
			uri = handleCompressed(picture, relationship, part, variant);
			if (uri != null) {
				handledImagesMap.put(variantKey, uri);
				return uri;
			}
			// nothing to change: the picture as stored, shared with every other use of it
		}
		if (handledImagesMap.containsKey(key)) {
			uri = handledImagesMap.get(key);
		}
		else {
			uri = doHandleImage(picture, relationship, part);
			handledImagesMap.put(key, uri);
		}
		if (variant != null) handledImagesMap.put(key + "#" + variant, uri);
		return uri;
	}

	/** This handler's resolution (a value of {@link PictureCompression#RESOLUTION_PROPERTY}),
	 *  or null for the property.  @since 17.3.0 */
	protected String pictureResolution;

	/** Whether this handler applies crops, or null for the property.  @since 17.3.0 */
	protected Boolean pictureCrop;

	/** Set by the FO conversion from {@code FOSettings.setImageResolution}.  @since 17.3.0 */
	public void setPictureResolution(String resolution) {
		this.pictureResolution = resolution;
	}

	/** Set by the FO conversion from {@code FOSettings.setImageCrop}.  @since 17.3.0 */
	public void setPictureCrop(Boolean crop) {
		this.pictureCrop = crop;
	}

	private boolean cropping() {
		return pictureCrop != null ? pictureCrop.booleanValue() : PictureCompression.cropEnabled();
	}

	/** Whether this handler applies pictures' crops in XSL-FO output (CR-029).  @since 17.3.0 */
	public boolean cropsPictures() {
		return cropping();
	}

	/**
	 * For a picture in XSL-FO (PDF) output, the picture as Word's Compress Pictures would
	 * leave it (CR-029): the crop applied, its colour adjustments applied (CR-084), and
	 * resampled to the document's resolution for the size it is shown at
	 * ({@link PictureCompression}); in HTML output, its colour adjustments alone (CR-084
	 * phase 4). Identifies the variant, or null where there is nothing to consider (an
	 * external picture; no crop, no adjustment and no resolution).
	 *
	 * @since 17.3.0
	 */
	protected String compressedVariant(AbstractWordXmlPicture picture, Relationship relationship, BinaryPart part)
			throws Docx4JException {
		if (picture == null || part == null || !isInternalImage(picture, relationship, part)) {
			return null;
		}
		if (!picture.isForXslFo()) {
			// HTML: the colour adjustments alone (CR-084 phase 4, D6); the crop and the resolution are the PDF's
			return picture.getRecolour().isNone() ? null : "html+" + picture.getRecolour().signature();
		}
		PictureCrop crop = cropping() ? picture.getCrop() : PictureCrop.NONE;
		PictureRecolour recolour = picture.getRecolour();
		PictureCompression.Resolution resolution = PictureCompression.resolution(picture.getWmlPackage(), pictureResolution);
		if (crop.isNone() && recolour.isNone() && resolution.isHighFidelity()) return null;
		// a picture without adjustments keeps the key (and so the derived part's name) it had before CR-084
		return crop.signature() + (recolour.isNone() ? "" : "+" + recolour.signature()) + "@" + resolution + ":"
				+ Math.round(picture.getWidthInPoints() * 100) + "x" + Math.round(picture.getHeightInPoints() * 100);
	}

	/** The compressed picture stored through this handler as a part of its own (so a
	 *  second crop of the same bitmap does not overwrite the first), or null where
	 *  nothing changes.  @since 17.3.0 */
	private String handleCompressed(AbstractWordXmlPicture picture, Relationship relationship,
			BinaryPart part, String variant) throws Docx4JException {
		boolean fo = picture.isForXslFo();
		PictureCrop crop = fo && cropping() ? picture.getCrop() : PictureCrop.NONE;
		PictureCompression.Result r = PictureCompression.compress(part.getBytes(), crop, picture.getRecolour(),
				picture.getWidthInPoints(), picture.getHeightInPoints(),
				fo ? PictureCompression.resolution(picture.getWmlPackage(), pictureResolution)
						: PictureCompression.Resolution.HIGH_FIDELITY);
		if (r == null) return null;
		try {
			String name = derivedName(part.getPartName() == null ? null : part.getPartName().getName(), variant, r.extension);
			BinaryPart derived = new BinaryPart(new org.docx4j.openpackaging.parts.PartName(name));
			derived.setContentType(new org.docx4j.openpackaging.contenttype.ContentType(r.contentType));
			derived.setBinaryData(r.bytes);
			if (log.isDebugEnabled()) {
				log.debug(relationship.getTarget() + ": " + (crop.isNone() ? "" : crop + ", ")
						+ (picture.getRecolour().isNone() ? "" : picture.getRecolour() + ", ")
						+ r.width + "x" + r.height + " " + r.extension + ", " + r.bytes.length + " bytes (was "
						+ part.getBytes().length + ")");
			}
			return doHandleImage(picture, relationship, derived);
		} catch (org.docx4j.openpackaging.exceptions.InvalidFormatException e) {
			log.warn("Couldn't store the compressed picture: " + e.getMessage());
			return null;
		}
	}

	/**
	 * The name of a picture's compressed variant: its part's name, the variant's hash, the new extension. The part's own
	 * extension is kept in it (image1-png-..., image1-gif-...): until 17.3.7 it was dropped, so two parts differing only
	 * by it, given the same crop or adjustment, wrote one file, and every use of either drew the last one written
	 * (measured: a PNG and a GIF made transparent, the PNG drawn with the GIF's exact clrChange).
	 *
	 * @param partName the part's name; null for none
	 * @since 17.3.7
	 */
	static String derivedName(String partName, String variant, String extension) {
		String name = partName == null ? "/word/media/picture" : partName;
		int dot = name.lastIndexOf('.');
		if (dot > name.lastIndexOf('/')) name = name.substring(0, dot) + "-" + name.substring(dot + 1);
		return name + "-" + Integer.toHexString(variant.hashCode()) + "." + extension;
	}

	protected String createKey(Relationship relationship, BinaryPart part) {
		return relationship.getTarget();
	}

	/** True where the images are embedded in the output rather than stored: that is
	 *  what an empty imageDirPath means to {@link #handleInternalImage}.
	 *  @since 17.1.0 */
	@Override
	public boolean isInline() {
		return imageDirPath != null && imageDirPath.equals("");
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
			uri = createEncodedImage(binaryPart, bytes);

		} else {
			uri = createStoredImage(binaryPart, bytes);

		}
		return uri;
	}

	protected abstract String createStoredImage(BinaryPart binaryPart, byte[] bytes) throws Docx4JException;

	/** Create a data URI (RFC 2397) containing the image.
	 *
	 *  The base64 payload is unchunked (no line breaks): XML attribute-value
	 *  normalization turns line breaks into spaces, which would break the
	 *  data URI where the output is parsed as XML (eg XHTML).
	 */
	protected String createEncodedImage(BinaryPart binaryPart, byte[] bytes) throws Docx4JException {
		return "data:" + binaryPart.getContentType()
				+ ";base64,"
				+ Base64.getEncoder().encodeToString(bytes);
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
