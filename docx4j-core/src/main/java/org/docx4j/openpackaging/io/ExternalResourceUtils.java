package org.docx4j.openpackaging.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.ExternalTarget;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageBmpPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageGifPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageJpegPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImagePngPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageTiffPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileEmfPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileWmfPart;

public class ExternalResourceUtils {
	
	/* This was removed from Load and placed in a separate class,
	 * so that Load can load, even if the VFS jar is not present.
	 */

	private static Logger log = LoggerFactory.getLogger(ExternalResourceUtils.class);
	protected static final Map<String, String> CONTENT_TYPE_MAP = new TreeMap<String, String>();
	
	static {
		CONTENT_TYPE_MAP.put(ContentTypes.EXTENSION_BMP, ContentTypes.IMAGE_BMP);
		CONTENT_TYPE_MAP.put(ContentTypes.EXTENSION_EMF, ContentTypes.IMAGE_EMF);
		CONTENT_TYPE_MAP.put(ContentTypes.EXTENSION_GIF, ContentTypes.IMAGE_GIF);
		CONTENT_TYPE_MAP.put(ContentTypes.EXTENSION_JPG_1, ContentTypes.IMAGE_JPEG);
		CONTENT_TYPE_MAP.put(ContentTypes.EXTENSION_JPG_2, ContentTypes.IMAGE_JPEG);
		CONTENT_TYPE_MAP.put(ContentTypes.EXTENSION_PNG, ContentTypes.IMAGE_PNG);
		CONTENT_TYPE_MAP.put(ContentTypes.EXTENSION_TIFF, ContentTypes.IMAGE_TIFF);
		CONTENT_TYPE_MAP.put("tif", ContentTypes.IMAGE_TIFF);
		CONTENT_TYPE_MAP.put(ContentTypes.EXTENSION_WMF, ContentTypes.IMAGE_WMF);
	}
	
	public static BinaryPart getExternalResource(String absoluteTarget) throws Docx4JException {
	URI targetURI = null;
	URL targetURL = null;
	int p = absoluteTarget.lastIndexOf('.');
	String fileExtension = (p > -1 ? absoluteTarget.substring(p+1).toLowerCase() : null);
	String contentType = (fileExtension != null ? CONTENT_TYPE_MAP.get(fileExtension) : null);
	BinaryPart binaryPart = null;
	InputStream inStream = null;
		try {
			targetURI = new URI(absoluteTarget.replace('\\', '/'));
		}
		catch (URISyntaxException use) {
			throw new Docx4JException("Invalid absolute Target: '" + absoluteTarget + "'", use);
		}
		try {
			targetURL = targetURI.toURL();
		} catch (MalformedURLException mue) {
			throw new Docx4JException("Invalid absolute Target: '" + absoluteTarget + "'", mue);
		}
		try {
			inStream = targetURL.openStream();
			binaryPart = createBinaryPart(absoluteTarget, contentType);
			binaryPart.setBinaryData(inStream);
		} catch (IOException ioe) {
			throw new Docx4JException("Could not load external resource: '" + absoluteTarget + "'", ioe);
		}
		return binaryPart;
	}
	
	protected static BinaryPart createBinaryPart(String absoluteTarget, String contentType) {
	ExternalTarget externalTarget = new ExternalTarget(absoluteTarget);
	BinaryPart ret = null;
		if (ContentTypes.IMAGE_JPEG.equals(contentType))
			ret = new ImageJpegPart(externalTarget);
		else if (ContentTypes.IMAGE_PNG.equals(contentType))
		    ret = new ImagePngPart(externalTarget);
		else if (ContentTypes.IMAGE_GIF.equals(contentType))
			ret = new ImageGifPart(externalTarget);
		else if (ContentTypes.IMAGE_TIFF.equals(contentType))
			ret = new ImageTiffPart(externalTarget);
		else if (ContentTypes.IMAGE_BMP.equals(contentType))
			ret = new ImageBmpPart(externalTarget);
		else if (ContentTypes.IMAGE_EMF.equals(contentType))
			ret = new MetafileEmfPart(externalTarget);
		else if (ContentTypes.IMAGE_WMF.equals(contentType))
			ret = new MetafileWmfPart(externalTarget);
		else 
			ret = new BinaryPart(externalTarget);
		
		return ret;
	}

}
