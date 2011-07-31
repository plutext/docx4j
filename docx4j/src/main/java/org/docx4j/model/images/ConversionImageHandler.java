package org.docx4j.model.images;

import org.docx4j.model.images.AbstractWordXmlPicture;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.relationships.Relationship;

/** The ImageHandler stores (if necessary) any images in an conversion. 
 *  It returns the uri for the image saved, or null
 */
public interface ConversionImageHandler {

	/**
	 * @param picture 
	 * @param relationship of the image 
	 * @param part of the image, if it is an internal image, otherwise null
	 * @return uri for the image we've saved, or null
	 */
	public String handleImage(AbstractWordXmlPicture picture, Relationship relationship, BinaryPart part);
}
