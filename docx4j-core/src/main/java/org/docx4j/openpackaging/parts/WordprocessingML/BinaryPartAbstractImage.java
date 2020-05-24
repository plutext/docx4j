/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
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
package org.docx4j.openpackaging.parts.WordprocessingML;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.DefaultImageContext;
import org.apache.xmlgraphics.image.loader.impl.DefaultImageSessionContext;
import org.docx4j.Docx4jProperties;
import org.docx4j.UnitsOfMeasurement;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.ExternalTarget;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.pptx4j.pml.Presentation.SldSz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BinaryPartAbstractImage extends BinaryPart {
	
	protected static Logger log = LoggerFactory.getLogger(BinaryPartAbstractImage.class);
	final static String IMAGE_DIR_PREFIX = "/word/media/";
	final static String IMAGE_NAME_PREFIX = "image";
	


	public BinaryPartAbstractImage(PartName partName) throws InvalidFormatException {
		super(partName);
		
		// Can't setContentType or setRelationshipType, since 
		// these will differ depending on the nature of the data.
		// Common binary parts should extend this class to 
		// provide that information.
	
		this.getOwningRelationshipPart();
		
	}
	
	public BinaryPartAbstractImage(ExternalTarget externalTarget) {
		super(externalTarget);
	}
	ImageInfo imageInfo;

	@Deprecated // don't want to be tied to this; instead need an API which provides mime type, height & width
	public ImageInfo getImageInfo() {
		
        if (imageInfo == null) {
			// TODO - create it
			// Save byte buffer as a tmp file
			// Generate ImageInfo
			// Delete tmp file
        	log.info("ImageInfo is created by some of the createImageInline methods; that hasn't been done in this case");
		}
		
		return imageInfo;
	}

	@Deprecated
	public void setImageInfo(ImageInfo imageInfo) {
		this.imageInfo = imageInfo;
	}
	
	// TODO, instead of Part.getOwningRelationshipPart(),
	// it would be better to have getOwningRelationship(),
	// and if required, to get OwningRelationshipPart from that
	// This is a temp workaround	
	private List<Relationship> rels = new ArrayList<Relationship>();
	public List<Relationship> getRels() {
		return rels;
	}
	public Relationship getRelLast() {
		int size = rels.size();
		if (size<1) {
			return null;
		} else {
			return rels.get(size-1);
		}
	}
	static int density = 150;	

	/**
	 * Set the resolution at which a PDF or EPS is converted
	 * to PNG.  For best quality, you should set this to match
	 * the target output device.  Higher densities (eg 600) 
	 * give better quality, at the expense of conversion time
	 * (and file size). 
	 * @param density
	 */
	public static void setDensity(int density) {
		BinaryPartAbstractImage.density = density;
	}
	
	private static ImageManager imageManagerInstance;
	private static Object imageManagerMutex = new Object();

	protected static ImageManager getImageManager() {
		if (imageManagerInstance == null) {
			synchronized(imageManagerMutex) {
				if (imageManagerInstance == null) {
					imageManagerInstance = new ImageManager(new DefaultImageContext());
				}
			}
		}
		return imageManagerInstance;
	}

	/**
	 * Create an image part from the provided byte array, attach it to the 
	 * docx main document part, and return it.
	 * 
	 * @param wordMLPackage
	 * @param sourcePart
	 * @param bytes
	 * @return
	 * @throws Exception
	 */
	public static BinaryPartAbstractImage createImagePart(WordprocessingMLPackage wordMLPackage,
			byte[] bytes) throws Exception {
		
		return createImagePart(wordMLPackage,
				wordMLPackage.getMainDocumentPart(), bytes);

	}
	
	
    /**
     * Possibility to put directly an image filePath instead of giving an image byte array
     * @param wordMLPackage
     * @param imageFile
     * 
     */
    public static BinaryPartAbstractImage createImagePart(WordprocessingMLPackage wordMLPackage,
    		File imageFile) throws Exception {

        return createImagePart(wordMLPackage,
                wordMLPackage.getMainDocumentPart(), imageFile);

    }

	/**
	 * This method assumes your package is a docx (not a pptx or xlsx).
	 * 
	 * @param sourcePart
	 * @param proposedRelId
	 * @param ext
	 * @return
	 */
	@Deprecated
    public static String createImageName(Base sourcePart, String proposedRelId, String ext) {
		
		return PartName.generateUniqueName(sourcePart, proposedRelId, 
				IMAGE_DIR_PREFIX, IMAGE_NAME_PREFIX, ext);
	}

    /**
     * @param opcPackage
     * @param sourcePart
     * @param proposedRelId
     * @param ext extension eg png
     * @return
     */
    public static String createImageName(OpcPackage opcPackage, Base sourcePart, String proposedRelId, String ext) {
		
		if (opcPackage instanceof WordprocessingMLPackage) {		
			return PartName.generateUniqueName(sourcePart, proposedRelId, 
					IMAGE_DIR_PREFIX, IMAGE_NAME_PREFIX, ext);
		} else if (opcPackage instanceof PresentationMLPackage) {		
			return PartName.generateUniqueName(sourcePart, proposedRelId, 
					"/ppt/media/", IMAGE_NAME_PREFIX, ext);
		} else if (opcPackage instanceof SpreadsheetMLPackage) {		
			return PartName.generateUniqueName(sourcePart, proposedRelId, 
					"/xl/media/", IMAGE_NAME_PREFIX, ext);
		} else {
			// Shouldn't happen
			return PartName.generateUniqueName(sourcePart, proposedRelId, 
					IMAGE_DIR_PREFIX, IMAGE_NAME_PREFIX, ext);			
		}
	}
	
	/**
	 * Create an image part from the provided byte array, attach it to the source part
	 * (eg the main document part, a header part etc), and return it.
	 * 
	 * Works for both docx and pptx.
	 * 
	 * Note: this method creates a temp file (and attempts to delete it).
	 * That's because it uses org.apache.xmlgraphics 
	 * 
	 * @param opcPackage
	 * @param sourcePart
	 * @param bytes
	 * @return
	 * @throws Exception
	 */
	public static BinaryPartAbstractImage createImagePart(
			OpcPackage opcPackage,
			Part sourcePart, byte[] bytes) throws Exception {
		
		if (bytes==null) {
			throw new Docx4JException("Can't create image from null byte array");
		} else if  (bytes.length==0) {
			throw new Docx4JException("Can't create image from empty byte array");
		}
				
		// Whatever image type this is, we're going to need 
		// to know its dimensions.
		// For that we use ImageInfo, which can only
		// load an image from a URI.
		
		// So first, write the bytes to a temp file		
		File tmpImageFile = File.createTempFile("img", ".img");
		
		FileOutputStream fos = new FileOutputStream(tmpImageFile);
		fos.write(bytes);
		fos.close();
        log.debug("created tmp file: " + tmpImageFile.getAbsolutePath());
				
		ImageInfo info = ensureFormatIsSupported(tmpImageFile, bytes, true);
		
		// In the absence of an exception, tmpImageFile now contains an image 
		// Word will accept
		
		ContentTypeManager ctm = opcPackage.getContentTypeManager();
		
		// Ensure the relationships part exists
        if (sourcePart.getRelationshipsPart() == null) {
			RelationshipsPart.createRelationshipsPartForPart(sourcePart);
        }

		String proposedRelId = sourcePart.getRelationshipsPart().getNextId();
				
        String ext = info.getMimeType().substring(info.getMimeType().indexOf("/") + 1);
		
//		System.out.println(ext);
		
		BinaryPartAbstractImage imagePart = 
                (BinaryPartAbstractImage) ctm.newPartForContentType(
				info.getMimeType(), 
                createImageName(opcPackage, sourcePart, proposedRelId, ext), null);
				
        log.debug("created part " + imagePart.getClass().getName()
                + " with name " + imagePart.getPartName().toString());
		
		FileInputStream fis = new FileInputStream(tmpImageFile); 		
        imagePart.setBinaryData(fis);
				
        imagePart.rels.add(sourcePart.addTargetPart(imagePart, proposedRelId));
		
		imagePart.setImageInfo(info);

		// Delete the tmp file
		// As per http://stackoverflow.com/questions/991489/i-cant-delete-a-file-in-java
		// the following 3 lines are necessary, at least on Win 7 x64
		// Also reported on Win XP, but in my testing, the files were deleting OK anyway.
		fos = null;
		fis = null;
		if (Docx4jProperties.getProperty("docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.TempFiles.ForceGC", true)) {
			System.gc();
		}
        if (tmpImageFile.delete()) {
            log.debug(".. deleted " + tmpImageFile.getAbsolutePath());
		} else {
			log.warn("Couldn't delete tmp file " + tmpImageFile.getAbsolutePath());
			tmpImageFile.deleteOnExit();
			// If that doesn't work, see "Clean Up Your Mess: Managing Temp Files in Java Apps"
			// at devx.com
		}
		
		return imagePart;
		
	}
	
	/**
	 * Create an image part from the provided byte array, attach it to the source part
	 * (eg the main document part, a header part etc), and return it.
	 * 
	 * Works for both docx and pptx.
	 * 
	 * Knowing the MIME type allows you to avoid ImageInfo, but you'll probably also need to
	 * know the image dimensions
	 * 
	 * @param opcPackage
	 * @param sourcePart
	 * @param bytes
	 * @param mime MIME type eg image/png
     * @param ext filename extension eg png
	 * @return
	 * @throws Exception
	 * 
	 * @Since 3.0.0
	 */
	@Deprecated
	public static BinaryPartAbstractImage createImagePart(
			OpcPackage opcPackage,
			Part sourcePart, byte[] bytes, String mime, String ext) throws Exception {
		
		if (bytes==null) {
			throw new Docx4JException("Can't create image from null byte array");
		} else if  (bytes.length==0) {
			throw new Docx4JException("Can't create image from empty byte array");
		}
		
		ContentTypeManager ctm = opcPackage.getContentTypeManager();
		
		// Ensure the relationships part exists
        if (sourcePart.getRelationshipsPart() == null) {
			RelationshipsPart.createRelationshipsPartForPart(sourcePart);
        }

		String proposedRelId = sourcePart.getRelationshipsPart().getNextId();
						
		BinaryPartAbstractImage imagePart = 
                (BinaryPartAbstractImage) ctm.newPartForContentType(
				mime, 
                createImageName(opcPackage, sourcePart, proposedRelId, ext), null);
				
        log.debug("created part " + imagePart.getClass().getName()
                + " with name " + imagePart.getPartName().toString());

        imagePart.setBinaryData(bytes);
        imagePart.rels.add(sourcePart.addTargetPart(imagePart, proposedRelId));
		
		return imagePart;
        
	}

	/**
	 * Create an image part from the provided byte array, attach it to the source part
	 * (eg the main document part, a header part etc), and return it.
	 * 
	 * Works for both docx and pptx.
	 * 
	 * Knowing the MIME type allows you to avoid ImageInfo, but you'll probably also need to
	 * know the image dimensions
	 * 
	 * @param opcPackage
	 * @param sourcePart
	 * @param bytes
	 * @param mime MIME type eg image/png
	 * @return
	 * @throws Exception
	 * 
	 * @Since 3.0.1
	 */
	public static BinaryPartAbstractImage createImagePart(
			OpcPackage opcPackage,
			Part sourcePart, byte[] bytes, String mime) throws Exception {

		if (bytes==null) {
			throw new Docx4JException("Can't create image from null byte array");
		} else if  (bytes.length==0) {
			throw new Docx4JException("Can't create image from empty byte array");
		}
		
		String ext = mimeToExt(mime);
		if (mime==null || ext==null) {
			log.warn("Null or unknown mime type; image introspection required!");
			return createImagePart(
					 opcPackage,
					 sourcePart,  bytes);
		}
		
		ContentTypeManager ctm = opcPackage.getContentTypeManager();
		
		// Ensure the relationships part exists
        if (sourcePart.getRelationshipsPart() == null) {
			RelationshipsPart.createRelationshipsPartForPart(sourcePart);
        }

		String proposedRelId = sourcePart.getRelationshipsPart().getNextId();
						
		BinaryPartAbstractImage imagePart = 
                (BinaryPartAbstractImage) ctm.newPartForContentType(
				mime, 
                createImageName(opcPackage, sourcePart, proposedRelId, ext), null);
				
        log.debug("created part " + imagePart.getClass().getName()
                + " with name " + imagePart.getPartName().toString());

        imagePart.setBinaryData(bytes);
        imagePart.rels.add(sourcePart.addTargetPart(imagePart, proposedRelId));
		
		return imagePart;
        
	}
	
	private static String mimeToExt(String mime) {
		
		if (mime==null) return null;
		if (mime.equals(ContentTypes.IMAGE_BMP)) return ContentTypes.EXTENSION_BMP; 

		if (mime.equals(ContentTypes.IMAGE_EMF)
				|| mime.equals(ContentTypes.IMAGE_EMF2)) return ContentTypes.EXTENSION_EMF; 
		
		if (mime.equals(ContentTypes.IMAGE_WMF)) return ContentTypes.EXTENSION_WMF; 
		
		if (mime.equals(ContentTypes.IMAGE_GIF)) return ContentTypes.EXTENSION_GIF; 
		
		if (mime.equals(ContentTypes.IMAGE_JPEG)) return ContentTypes.EXTENSION_JPG_2; // prefer .jpeg

		if (mime.equals(ContentTypes.IMAGE_PICT)) return ContentTypes.EXTENSION_PICT; // ??

		if (mime.equals(ContentTypes.IMAGE_PNG)) return ContentTypes.EXTENSION_PNG; 
		
		if (mime.equals(ContentTypes.IMAGE_TIFF)) return ContentTypes.EXTENSION_TIFF; 

		if (mime.equals(ContentTypes.IMAGE_EPS)) return ContentTypes.EXTENSION_EPS; 

		if (mime.equals(ContentTypes.IMAGE_BMP)) return ContentTypes.EXTENSION_BMP; 
		
		return null;
	}

	/**
     * Create an image part from the provided filePath image, attach it to the source part
     * (eg the main document part, a header part etc), and return it.
     * 
     * Works for both docx and pptx.
     * 
     * @param opcPackage
     * @param sourcePart
     * @param filePath
     * @return
     * @throws Exception
     */
    public static BinaryPartAbstractImage createImagePart(
            OpcPackage opcPackage,
            Part sourcePart, File imageFile) throws Exception {

        final byte[] locByte = new byte[1];

        //We are in the case that image is not load (no byte Array) so isLoad is false
        ImageInfo info = ensureFormatIsSupported(imageFile, locByte, false);

        ContentTypeManager ctm = opcPackage.getContentTypeManager();

        // Ensure the relationships part exists
        if (sourcePart.getRelationshipsPart() == null) {
            RelationshipsPart.createRelationshipsPartForPart(sourcePart);
        }

        String proposedRelId = sourcePart.getRelationshipsPart().getNextId();

        String ext = info.getMimeType().substring(info.getMimeType().indexOf("/") + 1);

        BinaryPartAbstractImage imagePart =
                (BinaryPartAbstractImage) ctm.newPartForContentType(
                info.getMimeType(),
                createImageName(opcPackage, sourcePart, proposedRelId, ext), null);

        log.debug("created part " + imagePart.getClass().getName()
                + " with name " + imagePart.getPartName().toString());

        FileInputStream fis = new FileInputStream(imageFile);
        imagePart.setBinaryData(fis);

        imagePart.rels.add(sourcePart.addTargetPart(imagePart, proposedRelId));

        imagePart.setImageInfo(info);

        return imagePart;

    }

	private static ImageInfo ensureFormatIsSupported(File imageFile, byte[] bytes, boolean isLoad) throws Docx4JException, MalformedURLException {
		return ensureFormatIsSupported(imageFile.toURI().toURL(),  imageFile,  bytes, isLoad);
	}
	
    /**
	 * @param bytes
	 * @param imageFile
	 * @return
	 * @throws Exception
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static ImageInfo ensureFormatIsSupported(URL url, File imageFile, byte[] bytes, boolean isLoad) throws Docx4JException {
		
		FileOutputStream fos;
		// ImageInfo can also tell us what sort of image it is	
		
		ImageInfo info = null;
		boolean imagePreloaderFound = true;
		try {
			try {
				info = getImageInfo(url);

				// Debug ... note that these figures 
				// aren't necessarily accurate for EPS
				displayImageInfo(info);
			} catch (Exception e) {
				
				// Assume: The file format is not supported. No ImagePreloader found for /tmp/img55623.img
				// There is no preloader for eg PDFs.
				// (To use an image natively, we do need a preloader)
				imagePreloaderFound = false;
                log.warn(e.getMessage());
                log.info(e.getMessage(),e);
			}
			
            if (imagePreloaderFound
                    && (info.getMimeType().equals(ContentTypes.IMAGE_TIFF)
					|| info.getMimeType().equals(ContentTypes.IMAGE_EMF2) // ImageInfo 
					|| info.getMimeType().equals(ContentTypes.IMAGE_WMF) 
					|| info.getMimeType().equals(ContentTypes.IMAGE_PNG) 
					|| info.getMimeType().equals(ContentTypes.IMAGE_JPEG) 
					|| info.getMimeType().equals(ContentTypes.IMAGE_GIF) 
//					 || info.getMimeType().equals(ContentTypes.IMAGE_EPS)
                    || info.getMimeType().equals(ContentTypes.IMAGE_BMP))) {
					// TODO: add other supported formats
				
				// If its a format Word supports natively, 
				// do nothing here
				log.debug(".. supported natively by Word");					
				
            } else if (imageFile != null && bytes != null) {
				
				// otherwise (eg if its an EPS or PDF), try to convert it
				// Although the Word UI suggests you can embed an EPS
				// directly, Word actually converts it to an EMF;
				// Word is unable to read a plain EPS image part.
				
				
				// (TODO: detect failure)

				log.debug(".. attempting to convert to PNG");		
				
                //If image haven't been load (using function createImagePartFromFilePath), we load it
                if (isLoad == false) {
    				log.debug("isLoad == false");		

                    // So first, we create tmpFile	
                    File tmpImageFile = File.createTempFile("img", ".img");
                    fos = new FileOutputStream(tmpImageFile);

                    //Now we get the inputStream, which is represented by imageFile in this case
                    FileInputStream bais = new FileInputStream(imageFile);

                    //We convert
                    convertToPNG(bais, fos, density);
                    
                    bais.close();

                    //We don't forget to change locFile because the new image file is the converted image file!!
                    imageFile = tmpImageFile;
                    
                } //Else image has been load in an array (using function createImagePart)
                else {
					ByteArrayInputStream bais = new ByteArrayInputStream(bytes);			
					fos = new FileOutputStream(imageFile); 
					convertToPNG(bais, fos, density);
					bais.close();
                }

				fos.close();
				fos = null;
				
				// We need to refresh image info 
				getImageManager().getCache().clearCache();
				try {
					// not file:// for xmlgraphics-commons-2.1, at least on Windows
					// eg file://C:/Users/jharrop/AppData/Local/Temp/img5440591995488867319.img
					// Is that a bug?  As of March 2017, not at https://issues.apache.org/jira/issues/?jql=project%20%3D%20XGC 
					info = getImageInfo(new URL("file:/" + imageFile.getAbsolutePath())); 
				} catch (java.io.FileNotFoundException fnfe) {
					// fallback to trying old behaviour
					log.info("trying file://");
					info = getImageInfo(new URL("file://" + imageFile.getAbsolutePath())); 					
				}
				
				// Debug ...
				displayImageInfo(info);
			} else {
				throw new Docx4JException("Unsupported linked image type.");
			}
		} catch (Exception e) {
			throw new Docx4JException("Error checking image format", e);
		} 
		return info;
	}
	
	/**
	 * Create a linked image part, and attach it as a rel of the docx main document part
	 * @param wordMLPackage
	 * @param fileurl
	 * @return
	 * @throws Exception
	 */
	public static BinaryPartAbstractImage createLinkedImagePart(WordprocessingMLPackage wordMLPackage, 
			URL fileurl) throws Exception {
		
		return createLinkedImagePart(wordMLPackage,
				wordMLPackage.getMainDocumentPart(), fileurl);
	}
	
	/**
	 * Create a linked image part, and attach it as a rel of the specified source part
	 * (eg a header part).
	 * 
	 * The current behaviour is that the part is added to the package, but 
	 * since the target mode of the rel is external, the part is redundant. 
	 * 
	 * @param wordMLPackage
	 * @param sourcePart
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static BinaryPartAbstractImage createLinkedImagePart(OpcPackage opcPackage, 
			Part sourcePart, URL url) throws Exception {
		
		log.debug("Incoming url for linked image: " + url.toString() );
		
        ImageInfo info = ensureFormatIsSupported(url, null, null, false); // final param doesn't matter in this case

		ContentTypeManager ctm = opcPackage.getContentTypeManager();
		String proposedRelId = sourcePart.getRelationshipsPart().getNextId();
		// In order to ensure unique part name,
		// idea is to use the relId, which ought to be unique
        String ext = info.getMimeType().substring(info.getMimeType().indexOf("/") + 1);
		
		BinaryPartAbstractImage imagePart = 
                (BinaryPartAbstractImage) ctm.newPartForContentType(
				info.getMimeType(), 
                createImageName(opcPackage, sourcePart, proposedRelId, ext), null);
		
		// NB: contents never populated
				
		log.debug("created part " + imagePart.getClass().getName()
				+ " with name " + imagePart.getPartName().toString());

		imagePart.rels.add(sourcePart.addTargetPart(imagePart)); // want to create rel with suitable name; side effect is to add part
		imagePart.getRelLast().setTargetMode("External");

		opcPackage.getExternalResources().put(imagePart.getExternalTarget(), 
				imagePart);			
		
//		if (!url.getProtocol().equals("file") && new File(url.toString() ).isFile()) {
//			imagePart.rel.setTarget("file:///" + url);
//		} else {
//			imagePart.rel.setTarget(url.toString());
//		}
		imagePart.getRelLast().setTarget(url.toString());

		imagePart.setImageInfo(info);
		
		return imagePart;
	}	

	/**
	 * Create a <wp:inline> element suitable for this image,
	 * which can be _embedded_ in w:p/w:r/w:drawing.
	 * If the image is wider than the page, it will be scaled
	 * automatically. To avoid the deprecated warning, use the
	 * same method, but with an additional argument of false appended.  
	 * @param filenameHint Any text, for example the original filename
	 * @param altText  Like HTML's alt text
	 * @param id1   An id unique in the document
	 * @param id2   Another id unique in the document
	 * None of these things seem to be exposed in Word 2007's
	 * user interface, but Word won't open the document if 
	 * any of the attributes these go in (except @ desc) aren't present!
	 * @throws Exception
	 */
	@Deprecated
	public Inline createImageInline(String filenameHint, String altText, 
			int id1, int id2) throws Exception {
		
        return createImageInline(filenameHint, altText,
                id1, id2, false);

	}
	
	/**
	 * Create a <wp:inline> element suitable for this image,
	 * which can be linked or embedded in w:p/w:r/w:drawing.
	 * If the image is wider than the page, it will be scaled
	 * automatically.
	 * @param filenameHint Any text, for example the original filename
	 * @param altText  Like HTML's alt text
	 * @param id1   An id unique in the document
	 * @param id2   Another id unique in the document
	 * @param link  true if this is to be linked not embedded
	 * None of these things seem to be exposed in Word 2007's
	 * user interface, but Word won't open the document if 
	 * any of the attributes these go in (except @ desc) aren't present!
	 * @throws Exception
	 */
	public Inline createImageInline(String filenameHint, String altText, 
			int id1, int id2, boolean link) throws Exception {
				
        WordprocessingMLPackage wmlPackage = ((WordprocessingMLPackage) this.getPackage());
		
		List<SectionWrapper> sections = wmlPackage.getDocumentModel().getSections();
        PageDimensions page = sections.get(sections.size() - 1).getPageDimensions();
		
		CxCy cxcy = CxCy.scale(imageInfo, page);
 
        return createImageInline(filenameHint, altText,
                id1, id2, cxcy.getCx(), cxcy.getCy(), link);
	}

	/**
	 * @param filenameHint
	 * @param altText
	 * @param id1
	 * @param id2
	 * @param link
	 * @param maxWidth
	 * @return
	 * @throws Exception
	 * @since 3.3.0
	 */
	public Inline createImageInline(String filenameHint, String altText, 
			int id1, int id2, boolean link, int maxWidth) throws Exception {
		
		// This signature can scale the image to specified maxWidth
				
        WordprocessingMLPackage wmlPackage = ((WordprocessingMLPackage) this.getPackage());
		
		List<SectionWrapper> sections = wmlPackage.getDocumentModel().getSections();
        PageDimensions page = sections.get(sections.size() - 1).getPageDimensions();
		
		CxCy cxcy = CxCy.scale(imageInfo, page, maxWidth);
 
        return createImageInline(filenameHint, altText,
                id1, id2, cxcy.getCx(), cxcy.getCy(), link);
	}

	
	/**
	 * Create a <wp:inline> element suitable for this image,
	 * which can be _embedded_ in w:p/w:r/w:drawing. To avoid the deprecated warning, use the
	 * same method, but with an additional argument of false appended.
	 * @param filenameHint Any text, for example the original filename
	 * @param altText  Like HTML's alt text
	 * @param id1   An id unique in the document
	 * @param id2   Another id unique in the document
	 * @param cx    Image width in twip
	 * None of these things seem to be exposed in Word 2007's
	 * user interface, but Word won't open the document if 
	 * any of the attributes these go in (except @ desc) aren't present!
	 * @throws Exception
	 */
	@Deprecated
	public Inline createImageInline(String filenameHint, String altText, 
			int id1, int id2, long cx) throws Exception {
		
        return createImageInline(filenameHint, altText,
                id1, id2, cx, false);

	}
	
	/**
	 * Create a <wp:inline> element suitable for this image,
	 * which can be _embedded_ in w:p/w:r/w:drawing.
	 * @param filenameHint Any text, for example the original filename
	 * @param altText  Like HTML's alt text
	 * @param id1   An id unique in the document
	 * @param id2   Another id unique in the document
	 * @param cx    Image width in twip
	 * @param link  true if this is to be linked not embedded
	 * None of these things seem to be exposed in Word 2007's
	 * user interface, but Word won't open the document if 
	 * any of the attributes these go in (except @ desc) aren't present!
	 * @throws Exception
	 */
	public Inline createImageInline(String filenameHint, String altText, 
			int id1, int id2, long cx, boolean link) throws Exception {
		
		ImageSize size = imageInfo.getSize();

		Dimension2D dPt = size.getDimensionPt();
		double imageWidthTwips = dPt.getWidth() * 20;
		log.debug("imageWidthTwips: " + imageWidthTwips);

		long cy;

		log.debug("Scaling image height to retain aspect ratio");
		cy = UnitsOfMeasurement.twipToEMU(dPt.getHeight() * 20 * cx / imageWidthTwips);

		// Now convert cx to EMU
		cx = UnitsOfMeasurement.twipToEMU(cx);
		

		log.debug("cx=" + cx + "; cy=" + cy);

		return createImageInline(filenameHint, altText, id1, id2, cx, cy, link);		
	}

	/**
	 * Create a <wp:inline> element suitable for this image, which can be
	 * linked or embedded in w:p/w:r/w:drawing, specifying height and width.  Note
	 * that you'd ordinarily use one of the methods which don't require
	 * you to specify height (cy). 
	 * 
	 * @param filenameHint
	 *            Any text, for example the original filename
	 * @param altText
	 *            Like HTML's alt text
	 * @param id1
	 *            An id unique in the document
	 * @param id2
	 *            Another id unique in the document None of these things seem to
	 *            be exposed in Word 2007's user interface, but Word won't open
	 * the document if any of the attributes these go in (except @ desc) aren't
	 *            present!
	 * @param cx    Image width in EMU
	 * @param cy    Image height in EMU
	 * @param link  true if this is to be linked not embedded
	 * @throws Exception
	 */
	public Inline createImageInline(String filenameHint, String altText, 
			int id1, int id2, long cx, long cy, boolean link) throws Exception {
		
        if (filenameHint == null) {
			filenameHint = "";
		}
        if (altText == null) {
			altText = "";
		}
		
		String type;
		if (link) {
			type = "r:link";
		} else {
			type = "r:embed";
		}
		
        String ml =
//        	"<w:p ><w:r>" +
//        "<w:drawing>" +
                "<wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\"" + namespaces + ">"
                + "<wp:extent cx=\"${cx}\" cy=\"${cy}\"/>"
                + "<wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>" + //l=\"19050\"
                "<wp:docPr id=\"${id1}\" name=\"${filenameHint}\" descr=\"${altText}\"/><wp:cNvGraphicFramePr><a:graphicFrameLocks xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" noChangeAspect=\"1\"/></wp:cNvGraphicFramePr><a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
                + "<a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
                + "<pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:nvPicPr><pic:cNvPr id=\"${id2}\" name=\"${filenameHint}\"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill>"
                + "<a:blip " + type + "=\"${rEmbedId}\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
                + "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"${cx}\" cy=\"${cy}\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic>"
                + "</wp:inline>"; // +
//        "</w:drawing>" +
//        "</w:r></w:p>";
        java.util.HashMap<String, String> mappings = new java.util.HashMap<String, String>();
        
        mappings.put("cx", Long.toString(cx));
        mappings.put("cy", Long.toString(cy));
        mappings.put("filenameHint", filenameHint);
        mappings.put("altText", altText);
        mappings.put("rEmbedId", this.getRelLast().getId());
        mappings.put("id1", Integer.toString(id1));
        mappings.put("id2", Integer.toString(id2));

        Object o = org.docx4j.XmlUtils.unmarshallFromTemplate(ml, mappings);
        Inline inline = (Inline) ((JAXBElement) o).getValue();
        
		return inline;		
	}
    final static String namespaces = " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
            + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" "
            + "xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\"";
	
	public static ImageInfo getImageInfo(URL url) throws Exception {
		
		// XmlGraphics images caches images by their URI;
		// therefore it can only load images from a URI, rather
		// than say a byte array, byte buffer, or input stream.

		ImageSessionContext sessionContext = new DefaultImageSessionContext(
				getImageManager().getImageContext(), null);

		ImageInfo info = getImageManager().getImageInfo(url.toString(), sessionContext);
		//getImageManager().closeImage(url.toString(), sessionContext); // until we can use xmlgraphics-commons 2.0.1
		
		// Note that these figures do not appear to be reliable for EPS
		// eg ImageMagick 6.2.4 10/02/07 Q16
		// identify fig1.eps
		// reports:
		// fig1.eps PS 516x429 516x429+0+0 DirectClass 869kb
		// whereas ImageInfo reports 1147x953
		
		/* Note2: odd results for PNG? 
		 * 
			If for an image, ImageMagick (v.6.2.4 and 6.3.7) identify says:
			
			  Resolution: 320x320 (or whatever)
			  Units: Undefined  <---------------------

			then ImageInfo will report a default value, using Toolkit.getDefaultToolkit().getScreenResolution(), 
			which may be say 160.
			
			To prevent the "Undefined", be sure to use -units when you call convert.
			
		 * When PreloaderImageIO.preloadImage does:

			        ImageIOUtil.extractResolution(iiometa, size);
			
			it is finding the Dimension child, but not "HorizontalPixelSize" or VerticalPixelSize (these are null).
		 * */
		
		return info;
		
	}
	
	public static void main(String[] args) throws Exception {
		
		//String uri = System.getProperty("user.dir") + "/sample-docs/metafile-samples/gradient.emf";
		String uri = System.getProperty("user.dir") + "/sample-docs/metafile-samples/freehand_picture_saveas.wmf";
		System.out.println(uri);
		
		//String uri = "/tmp/img4448.img";
		
		ImageInfo ii = getImageInfo(new URL(uri));
		
		displayImageInfo(ii);
	}
	
    public static void displayImageInfo(ImageInfo info) {
	
		  ImageSize size = info.getSize();
		  
		  Dimension2D dPt = size.getDimensionPt();
		  Dimension dPx = size.getDimensionPx();

		  log.debug(info.getOriginalURI() + " " + info.getMimeType() 
                + " " + Math.round(dPx.getWidth()) + "x" + Math.round(dPx.getHeight()));
		  		  
        log.debug("Resolution:" + Math.round(size.getDpiHorizontal()) + "x" + Math.round(size.getDpiVertical()));
        log.debug("Print size: " + Math.round(dPt.getWidth() / 72) + "\" x" + Math.round(dPt.getHeight() / 72) + "\"");
		
	}
	
	/**
	 * Convenience method, given a Graphic in a document,
	 * to get the byte[] representing
	 * the associated image 
	 * 
	 * The method assumes your image is in the main document part (as opposed to a header/footer etc)
	 * 
	 * @param wmlPkg
	 * @param graphic
	 * @return
	 */
	public static byte[] getImage(WordprocessingMLPackage wmlPkg,
			org.docx4j.dml.Graphic graphic) {
		
		if (wmlPkg == null 
			|| wmlPkg.getMainDocumentPart() == null
			|| wmlPkg.getMainDocumentPart().getRelationshipsPart() == null) {
			return null;
		}
		
		Pic pic = graphic.getGraphicData().getPic();
		String rId = pic.getBlipFill().getBlip().getEmbed();
		if (rId.equals("")) {
			rId = pic.getBlipFill().getBlip().getLink();
		}
		log.debug("Image rel id: " + rId);
		org.docx4j.relationships.Relationship rel = 
			wmlPkg.getMainDocumentPart().getRelationshipsPart().getRelationshipByID(rId);
		if (rel != null) {
			org.docx4j.openpackaging.parts.Part part = 
				wmlPkg.getMainDocumentPart().getRelationshipsPart().getPart(rel);
			if (part == null) {
				log.error("Couldn't get Part!");
			} else if (part instanceof BinaryPart) {
				log.debug("getting bytes...");
				return ((BinaryPart)part).getBytes();
			} else {				
                log.error("Part was a " + part.getClass().getName());
			}
		} else {
			log.error("Couldn't find rel " + rId);
		}
		
		return null;
	}

//	/**
//	 * Create a <wp:inline> element suitable for this image,
//	 * which can be linked or embedded in w:p/w:r/w:drawing.
//	 * If the image is wider than the page, it will be scaled
//	 * automatically.
//	 * @param filenameHint Any text, for example the original filename
//	 * @param altText  Like HTML's alt text
//	 * @param id1   An id unique in the document
//	 * @param id2   Another id unique in the document
//	 * @param link  true if this is to be linked not embedded
//	 * None of these things seem to be exposed in Word 2007's
//	 * user interface, but Word won't open the document if 
//	 * any of the attributes these go in (except @ desc) aren't present!
//	 * @throws Exception
//	 */
//	public Anchor createImageAnchor(String filenameHint, String altText, 
//			int id1, int id2, boolean link, long posHOffset, long posVOffset) throws Exception {
//				
//        WordprocessingMLPackage wmlPackage = ((WordprocessingMLPackage) this.getPackage());
//		
//		List<SectionWrapper> sections = wmlPackage.getDocumentModel().getSections();
//        PageDimensions page = sections.get(sections.size() - 1).getPageDimensions();
//		
//		CxCy cxcy = CxCy.scale(imageInfo, page);
// 
//        return createImageAnchor(filenameHint, altText,
//                id1, id2, cxcy.getCx(), cxcy.getCy(), link, posHOffset, posVOffset);
//	}
//	
//	
//	/**
//	 * Create a <wp:inline> element suitable for this image,
//	 * which can be _embedded_ in w:p/w:r/w:drawing.
//	 * @param filenameHint Any text, for example the original filename
//	 * @param altText  Like HTML's alt text
//	 * @param id1   An id unique in the document
//	 * @param id2   Another id unique in the document
//	 * @param cx    Image width in twip
//	 * @param link  true if this is to be linked not embedded
//	 * None of these things seem to be exposed in Word 2007's
//	 * user interface, but Word won't open the document if 
//	 * any of the attributes these go in (except @ desc) aren't present!
//	 * @throws Exception
//	 */
//	public Anchor createImageAnchor(String filenameHint, String altText, 
//			int id1, int id2, long cx, boolean link, long posHOffset, long posVOffset) throws Exception {
//		
//		ImageSize size = imageInfo.getSize();
//
//		Dimension2D dPt = size.getDimensionPt();
//		double imageWidthTwips = dPt.getWidth() * 20;
//		log.debug("imageWidthTwips: " + imageWidthTwips);
//
//		long cy;
//
//		log.debug("Scaling image height to retain aspect ratio");
//		cy = UnitsOfMeasurement.twipToEMU(dPt.getHeight() * 20 * cx / imageWidthTwips);
//
//		// Now convert cx to EMU
//		cx = UnitsOfMeasurement.twipToEMU(cx);
//		
//
//		log.debug("cx=" + cx + "; cy=" + cy);
//
//		return createImageAnchor(filenameHint, altText, id1, id2, cx, cy, link, posHOffset, posVOffset);		
//	}
//
//	/**
//	 * Create a <wp:anchor> element suitable for this image, which can be
//	 * linked or embedded in w:p/w:r/w:drawing, specifying height and width.  Note
//	 * that you'd ordinarily use one of the methods which don't require
//	 * you to specify height (cy). 
//	 * 
//	 * @param filenameHint
//	 *            Any text, for example the original filename
//	 * @param altText
//	 *            Like HTML's alt text
//	 * @param id1
//	 *            An id unique in the document
//	 * @param id2
//	 *            Another id unique in the document None of these things seem to
//	 *            be exposed in Word 2007's user interface, but Word won't open
//	 * the document if any of the attributes these go in (except @ desc) aren't
//	 *            present!
//	 * @param cx    Image width in EMU
//	 * @param cy    Image height in EMU
//	 * @param link  true if this is to be linked not embedded
//	 * @throws Exception
//	 */
//	public Anchor createImageAnchor(String filenameHint, String altText, 
//			int id1, int id2, long cx, long cy, boolean link,
//			long posHOffset, long posVOffset) throws Exception {
//		
//		// Floating - The drawing object is anchored within the text, but can be absolutely positioned in the
//		// document relative to the page.
//		
//        if (filenameHint == null) {
//			filenameHint = "";
//		}
//        if (altText == null) {
//			altText = "";
//		}
//		
//		String type;
//		if (link) {
//			type = "r:link";
//		} else {
//			type = "r:embed";
//		}
//		        
//      String ml =
//		    "<wp:anchor distT=\"0\" distB=\"0\" distL=\"114300\" distR=\"114300\" simplePos=\"0\" relativeHeight=\"251658240\" behindDoc=\"0\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\"" + namespaces + ">" //  wp14:anchorId=\"28C768E3\" wp14:editId=\"050FA02A\">"
//    		  + "<wp:simplePos x=\"0\" y=\"0\"/>"
//		      + "<wp:positionH relativeFrom=\"column\"><wp:posOffset>${posHOffset}</wp:posOffset></wp:positionH>"
//		      + "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>${posVOffset}</wp:posOffset></wp:positionV>"
//              + "<wp:extent cx=\"${cx}\" cy=\"${cy}\"/>"
//		      + "<wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>"  // b=4445
//		      + "<wp:wrapNone/>"
//		      + "<wp:docPr id=\"${id1}\" name=\"${filenameHint}\" descr=\"${altText}\"/><wp:cNvGraphicFramePr><a:graphicFrameLocks xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" noChangeAspect=\"1\"/></wp:cNvGraphicFramePr>"
//		      + "<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
//		       + " <a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
//		         + " <pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:nvPicPr><pic:cNvPr id=\"${id2}\" name=\"${filenameHint}\"/><pic:cNvPicPr/></pic:nvPicPr>"
//		          + "<pic:blipFill><a:blip " + type + "=\"${rEmbedId}\"/>"
////		           + " <a:extLst><a:ext uri=\"{28A0092B-C50C-407E-A947-70E740481C1C}\"><a14:useLocalDpi xmlns:a14=\"http://schemas.microsoft.com/office/drawing/2010/main\" val=\"0\"/></a:ext></a:extLst>"
//		          + "</a:blip><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
//		            + "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"${cx}\" cy=\"${cy}\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData>"
//		      + "</a:graphic>"
////		      + "<wp14:sizeRelH relativeFrom=\"page\"><wp14:pctWidth>0</wp14:pctWidth></wp14:sizeRelH><wp14:sizeRelV relativeFrom=\"page\"><wp14:pctHeight>0</wp14:pctHeight></wp14:sizeRelV>"
//		    + "</wp:anchor>";
//        
//        
//        
//        java.util.HashMap<String, String> mappings = new java.util.HashMap<String, String>();
//        
//        mappings.put("cx", Long.toString(cx));
//        mappings.put("cy", Long.toString(cy));
//        mappings.put("filenameHint", filenameHint);
//        mappings.put("altText", altText);
//        mappings.put("rEmbedId", rel.getId());
//        mappings.put("id1", Integer.toString(id1));
//        mappings.put("id2", Integer.toString(id2));
//
//        Object o = org.docx4j.XmlUtils.unmarshallFromTemplate(ml, mappings);
//        Anchor anchor = (Anchor) ((JAXBElement) o).getValue();
//        
//		return anchor;		
//	}
	
	
	
	public static class CxCy {

		private static final int EMU_RATIO = 914400;
		
		long cx;

		/**
		 * @return the resulting cx
		 */
		public long getCx() {
			return cx;
		}
		long cy;

		/**
		 * @return the resulting cy
		 */
		public long getCy() {
			return cy;
		}
		boolean scaled;

		/**
		 * @return whether it was necessary to scale
		 * the image to fit the page width
		 */
		public boolean isScaled() {
			return scaled;
		}
		
		CxCy(long cx, long cy, boolean scaled) {
			
			this.cx = cx;
			this.cy = cy;
			this.scaled = scaled;
			
		}

		public static CxCy scale(ImageInfo imageInfo, PageDimensions page) {
			return scale( imageInfo,  page, -1);
		}
		
		/**
		 * Return scaling values constrained by specified maxWidth.
		 * Useful if the image is to be inserted into a table cell of known width.
		 * 
		 * @param imageInfo
		 * @param page
		 * @param maxWidth
		 * @return
		 * @since 3.3.0
		 */
		public static CxCy scale(ImageInfo imageInfo, PageDimensions page, int maxWidth) {
			
			double writableWidthTwips = page.getWritableWidthTwips(); 				
			log.debug("writableWidthTwips: " + writableWidthTwips);
			
			if (maxWidth>0 && maxWidth<writableWidthTwips) {
				writableWidthTwips = maxWidth;
				log.debug("reduced to: " + writableWidthTwips);
			}

			ImageSize size = imageInfo.getSize();

			// get image size in pixels
			Dimension2D dpx = size.getDimensionPx();

			// convert pixels to twips (uses configured DPI setting - NOT the
			// image DPI)
			double imageWidthTwips = UnitsOfMeasurement.pxToTwipDouble(dpx.getWidth());
			double imageHeightTwips = UnitsOfMeasurement.pxToTwipDouble(dpx.getHeight());

			log.debug("imageWidthTwips: " + imageWidthTwips);
			log.debug("imageHeightTwips: " + imageHeightTwips);

			long cx;
			long cy;
			boolean scaled = false;
            if (imageWidthTwips > writableWidthTwips) {
				
				log.debug("Scaling image to fit page width");
				scaled = true;
				
				cx = UnitsOfMeasurement.twipToEMU(writableWidthTwips);
//                cy = UnitsOfMeasurement.twipToEMU(dPt.getHeight() * 20 * writableWidthTwips / imageWidthTwips);
				cy = UnitsOfMeasurement.twipToEMU(imageHeightTwips * writableWidthTwips / imageWidthTwips);
				
			} else {

				log.debug("Scaling image - not necessary");
				
				cx = UnitsOfMeasurement.twipToEMU(imageWidthTwips);
//				cy = UnitsOfMeasurement.twipToEMU(dPt.getHeight() * 20);			
				cy = UnitsOfMeasurement.twipToEMU(imageHeightTwips);
			}
			
			log.debug("cx=" + cx + "; cy=" + cy);
			
			return new CxCy(cx, cy, scaled);
			
			
		}
		
		public static CxCy scale(ImageInfo imageInfo, SldSz sldSz) {
			return scale(imageInfo, sldSz.getCx(), sldSz.getCy());
		}
		
		private static double toEmu(double widthPx, double dpi) {
			return widthPx * EMU_RATIO / dpi;
		}

        public static CxCy scale(ImageInfo imageInfo, double xEmu, double yEmu) {
            ImageSize size = imageInfo.getSize();
            double iwEmu = toEmu(size.getWidthPx(), size.getDpiHorizontal());
            double ihEmu = toEmu(size.getHeightPx(), size.getDpiVertical());
        
            return scaleToFit(iwEmu, ihEmu, xEmu, yEmu);
        }

		
		/**
		 * 
		 * @param iwEmu image width in EMU
		 * @param ihEmu image height in EMU
		 * @param swEmu slide width in EMU
		 * @param shEmu slide height in EMU
		 * @return
		 */
		public static CxCy scaleToFit(double iwEmu, double ihEmu, double swEmu, double shEmu) {
			double ir = iwEmu / ihEmu;
			double sr = swEmu / shEmu;
			double xr = Math.max( iwEmu/swEmu, ihEmu/shEmu);

			double cx;
			double cy;
			boolean scaled;

			if(xr <= 1) {
				log.debug("Scaling image - not necessary");

				scaled = false;
				cx = iwEmu;
				cy = ihEmu;
			} else if (sr > ir) {
				log.debug("Scaling image to fit width");

				scaled = true;
				cx = iwEmu * shEmu / ihEmu;
				cy = shEmu;
			} else {
				log.debug("Scaling image to fit height");

				scaled = true;
				cx = swEmu;
				cy = ihEmu * swEmu / iwEmu;
			}

			log.trace(String.format("iw: %4.1f; ih: %4.1f; sw: %4.1f; sh: %4.1f; ir: %4.1f; sr: %4.1f; xr: %4.1f; cx: %4.1f; cy: %4.1f", iwEmu, ihEmu, swEmu, shEmu, ir, sr, xr, cx, cy));

			return new CxCy(Math.round(cx), Math.round(cy), scaled);

		}
	}

	/**
	 * Convert image formats which are not supported by Word (eg EPS, PDF),
	 * into ones which are.  This requires ImageMagick to be on your
	 * system's path (renamed to imconvert); for EPS and PDF images, Ghostscript is also required.
	 * 
	 * @param is
	 * @param os
	 * @param density  PixelsPerInch 
	 * @throws IOException
	 * @throws InterruptedException
	 */
    public static void convertToPNG(InputStream is, OutputStream os, int density) throws IOException, InterruptedException {
		
		/*
		 * See http://www.eichberger.de/2006/05/imagemagick-in-servlets.html
		 * 
		 * "Calling 'convert - png:-' as an external command and feeding it the 
		 * source image as standard input and reading the converted image 
		 * (in this case png) as standard output"
		 * 
		 */
			
		 String executableName = Docx4jProperties.getProperty("org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.ImageMagickExecutable", "imconvert");		 
		 log.info("Start ImageMagick..." + executableName);
		 Process p = Runtime.getRuntime().exec(executableName + " -density " + density + " -units PixelsPerInch - png:-");  
		 
		 // GraphicsMagick is a little quicker than ImageMagick,
		 // but v1.3.3 (of Dec 2008) still has the now fixed in GM bug
		 // whereby the right most ~10% of the resulting image is chopped off
		 //Process p = Runtime.getRuntime().exec("gm convert -density " + density + " -units PixelsPerInch - png:-");  
		 
		 /* On Windows, if this results in "Invalid Parameter",
		  * then either ImageMagick is not installed,
		  * or exec is finding the wrong convert
		  * program.  See http://studio.imagemagick.org/pipermail/magick-users/2005-October/016464.html
		  * and http://www.imagemagick.org/discourse-server/viewtopic.php?f=1&t=8324&start=0
		  * 
		  * Rather than use full path, rename convert to imconvert (which Alfresco and others do)
		  * 
		  */
		 
		 //initialize Gobblers
		 StreamGobbler inGobbler = new StreamGobbler(p.getInputStream(), os);
		 StreamGobbler errGobbler = new StreamGobbler(p.getErrorStream(), System.err);
		 //start them
		 inGobbler.start();
		 errGobbler.start();
		 
		 // p.getOutputStream() is the _output stream_ of the subprocess, so
		 // this copies is into the standard input stream of the process 
		 try {
			 copy2(is, new BufferedOutputStream(p.getOutputStream()));
			 p.getOutputStream().close();
			 log.debug("Image copied...");
		 } catch (IOException ioe) {
			 
			 ioe.printStackTrace();
			 // debug
			 copy2(p.getErrorStream(), System.err);
		 }
		 
		if (p.waitFor() != 0) {
			log.error("Error");
		}
		log.debug("End Process...");
	}

	public static void copy2(InputStream is, OutputStream os) throws IOException {
	    byte[] buffer = new byte[512];
	    while (true) {
	     int bytesRead = is.read(buffer);
            if (bytesRead == -1) {
                break;
            }
	     os.write(buffer, 0, bytesRead);
	    }
	    os.flush();
	   }//method
	}//class

class StreamGobbler extends Thread {
		// The term "StreamGobbler" was taken from an article called "When Runtime.exec() won't", 
		// see http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html.
		
	  InputStream is;
	  OutputStream os;

    StreamGobbler(InputStream is, OutputStream redirect) {
        this.is = new BufferedInputStream(is);
	      this.os = redirect;
	  }

    public void run() {
        try {
	    	  BinaryPartAbstractImage.copy2(is, os);
        } catch (IOException ioe) {
	          ioe.printStackTrace();
	          }
	  }
	}
