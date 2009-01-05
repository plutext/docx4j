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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.DefaultImageContext;
import org.apache.xmlgraphics.image.loader.impl.DefaultImageSessionContext;
import org.docx4j.dml.Inline;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.SectPr.PgMar;
import org.docx4j.wml.SectPr.PgSz;


public abstract class BinaryPartAbstractImage extends BinaryPart {
	
	protected static Logger log = Logger.getLogger(BinaryPartAbstractImage.class);
	
	final static String IMAGE_PREFIX = "/word/media/image";
	
	public BinaryPartAbstractImage(PartName partName) throws InvalidFormatException {
		super(partName);
		
		// Can't setContentType or setRelationshipType, since 
		// these will differ depending on the nature of the data.
		// Common binary parts should extend this class to 
		// provide that information.
		
	}
	
	public static String generateName() {
		counter++;
		return IMAGE_PREFIX + counter;
	}
	static int counter = 0;
	
	
	ImageInfo imageInfo;

	public ImageInfo getImageInfo() {
		
		if (imageInfo==null) {
			
			// TODO - create it
			
			// Save byte buffer as a tmp file
			
			// Generate ImageInfo
			
			// Delete tmp file
			
		}
		
		return imageInfo;
	}

	public void setImageInfo(ImageInfo imageInfo) {
		this.imageInfo = imageInfo;
	}

	
	// TODO, instead of Part.getOwningRelationshipPart(),
	// it would be better to have getOwningRelationship(),
	// and if required, to get OwningRelationshipPart from that
	
	// This is a temp workaround
	
	Relationship rel;
		
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

	static {
		
		imageManager = new ImageManager(new DefaultImageContext());
		
	}

	static ImageManager imageManager;
	
	public static BinaryPartAbstractImage createImagePart(WordprocessingMLPackage wordMLPackage,
			byte[] bytes) throws Exception {
				
		// Whatever image type this is, we're going to need 
		// to know its dimensions.
		// For that we use ImageInfo, which can only
		// load an image from a URI.
		
		// So first, write the bytes to a temp file		
		File tmpImageFile = File.createTempFile("img", ".img");
		
		FileOutputStream fos = new FileOutputStream(tmpImageFile);
		fos.write(bytes);
		fos.close();
		log.debug("created tmp file: " +  tmpImageFile.getAbsolutePath() );
				
		// ImageInfo can also tell us what sort of image it is	
		
		ImageInfo info = null;
		boolean imagePreloaderFound = true;
		try {
			info = getImageInfo(tmpImageFile.getAbsolutePath() );
			
			// Debug ... note that these figures 
			// aren't necessarily accurate for EPS
			displayImageInfo(info);
		} catch (org.apache.xmlgraphics.image.loader.ImageException e) {
			
			// Assume: The file format is not supported. No ImagePreloader found for /tmp/img55623.img
			// There is no preloader for eg PDFs.
			// (To use an image natively, we do need a preloader)
			imagePreloaderFound = false;
			log.warn(e.getMessage() );
		}
		
		if ( imagePreloaderFound &&
				(info.getMimeType().equals(ContentTypes.IMAGE_TIFF)
				|| info.getMimeType().equals(ContentTypes.IMAGE_EMF) 
				|| info.getMimeType().equals(ContentTypes.IMAGE_WMF) 
				|| info.getMimeType().equals(ContentTypes.IMAGE_PNG) 
				|| info.getMimeType().equals(ContentTypes.IMAGE_JPEG) 
				|| info.getMimeType().equals(ContentTypes.IMAGE_GIF) )  ) {
				// TODO: add other supported formats
			
			// If its a format Word supports natively, 
			// do nothing here
			log.debug(".. supported natively by Word");		
			
			
		} else {
			
			// otherwise (eg if its an EPS or PDF), try to convert it
			// (TODO: detect failure)

			log.debug(".. attempting to convert to PNG");		
			
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);			
			fos = new FileOutputStream(tmpImageFile); //reuse
						
			convertToPNG(bais, fos, density);
			fos.close();
			
			// We need to refresh image info 
			imageManager.getCache().clearCache();
			info = getImageInfo(tmpImageFile.getAbsolutePath() );
			
			// Debug ...
			displayImageInfo(info);
		}
		
		// In either case, tmpImageFile now contains an image 
		// Word will accept
		
		ContentTypeManager ctm = wordMLPackage.getContentTypeManager();
		BinaryPartAbstractImage imagePart = (BinaryPartAbstractImage)ctm.newPartForContentType(info.getMimeType(), 
				generateName() );
		log.debug("created part " + imagePart.getClass().getName() +
				" with name " + imagePart.getPartName().toString() );		
		
		
		FileInputStream fis = new FileInputStream(tmpImageFile); //reuse		
		imagePart.setBinaryData( fis );
				
		imagePart.rel =  wordMLPackage.getMainDocumentPart().addTargetPart(imagePart);
		
		imagePart.setImageInfo(info);

		  // Delete the tmp file
		tmpImageFile.delete();
		
		return imagePart;
		
			}
	
	// Defaults - if values aren't defined in sectPr 
	private static int DEFAULT_PAGE_WIDTH_TWIPS = 12240;  // Letter; A4 would be 11907  
	private static int DEFAULT_LEFT_MARGIN_TWIPS = 1440;  // 1 inch
	private static int DEFAULT_RIGHT_MARGIN_TWIPS = 1440;
		
	/**
	 * Create a <wp:inline> element suitable for this image,
	 * which can be embedded in w:p/w:r/w:drawing
	 * @param filenameHint Any text, for example the original filename
	 * @param altText  Like HTML's alt text
	 * @param id1   An id unique in the document
	 * @param id2   Another id unique in the document
	 * None of these things seem to be exposed in Word 2007's
	 * user interface, but Word won't open the document if 
	 * any of the attributes these go in (except @ desc) aren't present!
	 * @throws Exception
	 */
	public Inline createImageInline(String filenameHint, String altText, 
			int id1, int id2) throws Exception {
		
		if (filenameHint==null) {
			filenameHint = "";
		}
		if (altText==null) {
			altText = "";
		}
		
		WordprocessingMLPackage wordMLPackage = ((WordprocessingMLPackage)this.getPackage()); 
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();
		
		// Since the object will be added at the end of the document,
		// it is safe to look for the page dimensions in the last sectPr 
		SectPr sectPr = wmlDocumentEl.getBody().getSectPr();
		
		double writableWidthTwips;
		if (sectPr==null) {
			
			log.debug("PgSz and PgMar not defined in this doc's SectPr element");
			writableWidthTwips = DEFAULT_PAGE_WIDTH_TWIPS - (DEFAULT_LEFT_MARGIN_TWIPS + DEFAULT_RIGHT_MARGIN_TWIPS); 
				
		} else {
			
			PgSz pgSz = sectPr.getPgSz();
			PgMar pgMar = sectPr.getPgMar();
			
			double pageWidth;
			double leftMargin;
			double rightMargin;
			
			if ( pgSz == null ) {
				pageWidth = DEFAULT_PAGE_WIDTH_TWIPS;
			} else {
				pageWidth = pgSz.getW().doubleValue();
			}
			if ( pgMar == null 
					|| pgMar.getLeft()==null) {
				leftMargin = DEFAULT_LEFT_MARGIN_TWIPS;
			} else {
				leftMargin = pgMar.getLeft().doubleValue();
			}
			if ( pgMar == null 
					|| pgMar.getRight()==null) {
				rightMargin = DEFAULT_RIGHT_MARGIN_TWIPS;
			} else {
				rightMargin = pgMar.getRight().doubleValue();
			}

			writableWidthTwips = pageWidth - (leftMargin + rightMargin );
		}
				
		log.debug("writableWidthTwips: " + writableWidthTwips);
		
		  ImageSize size = imageInfo.getSize();
		  
		  Dimension2D dPt = size.getDimensionPt();
		double imageWidthTwips = dPt.getWidth() * 20;
		log.debug("imageWidthTwips: " + imageWidthTwips);
		
		long cx;
		long cy;
		if (imageWidthTwips>writableWidthTwips) {
			
			log.debug("Scaling image to fit page width");
			
			cx = twipToEMU(writableWidthTwips);
			cy = twipToEMU(dPt.getHeight() * 20 * writableWidthTwips/imageWidthTwips);
			
		} else {

			log.debug("Scaling image - not necessary");
			
			cx = twipToEMU(imageWidthTwips);
			cy = twipToEMU(dPt.getHeight() * 20);			
			
		}
		
		log.debug("cx=" + cx + "; cy=" + cy);
		
        String ml =
//        	"<w:p ><w:r>" +
//        "<w:drawing>" +
        "<wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\"" + namespaces + ">" +
        "<wp:extent cx=\"${cx}\" cy=\"${cy}\"/>" +
        "<wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>" +  //l=\"19050\"
        "<wp:docPr id=\"${id1}\" name=\"${filenameHint}\" descr=\"${altText}\"/><wp:cNvGraphicFramePr><a:graphicFrameLocks xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" noChangeAspect=\"1\"/></wp:cNvGraphicFramePr><a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">" +
        "<a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">" +
        "<pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:nvPicPr><pic:cNvPr id=\"${id2}\" name=\"${filenameHint}\"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed=\"${rEmbedId}\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>" +
        "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"${cx}\" cy=\"${cy}\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic>" +
        "</wp:inline>"; // +
//        "</w:drawing>" +
//        "</w:r></w:p>";
        java.util.HashMap<String, String>mappings = new java.util.HashMap<String, String>();
        
        mappings.put("cx", Long.toString(cx));
        mappings.put("cy", Long.toString(cy));
        mappings.put("filenameHint", filenameHint);
        mappings.put("altText", altText);
        mappings.put("rEmbedId", rel.getId()  );
        mappings.put("id1", Integer.toString(id1));
        mappings.put("id2", Integer.toString(id2));

        Object o = org.docx4j.XmlUtils.unmarshallFromTemplate(ml, mappings ) ;        
        Inline inline = (Inline)((JAXBElement)o).getValue();
        
		return inline;		
	}
	
	final static String namespaces = " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" " +
			"xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" " +
			"xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\"";
	
	public static long twipToEMU(double twips) {
		
		return Math.round(635 * twips);
				
	}
	
	public static ImageInfo getImageInfo(String uri) throws Exception {
		
		// XmlGraphics images caches images by their URI;
		// therefore it can only load images from a URI, rather
		// than say a byte array, byte buffer, or input stream.

		ImageSessionContext sessionContext = new DefaultImageSessionContext(
				imageManager.getImageContext(), null);

		ImageInfo info = imageManager.getImageInfo(uri, sessionContext);
		
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
		
		String uri = "/tmp/img4448.img";
		
		ImageInfo ii = getImageInfo(uri);
		
		displayImageInfo(ii);
	}
	
	
	public static void displayImageInfo(ImageInfo info ) {
		
		  ImageSize size = info.getSize();
		  
		  Dimension2D dPt = size.getDimensionPt();
		  Dimension dPx = size.getDimensionPx();

		  System.out.println(info.getOriginalURI() + " " + info.getMimeType() 
				  + " " + Math.round(dPx.getWidth()) +"x" + Math.round(dPx.getHeight()));
		  		  
		  System.out.println("Resolution:" + Math.round(size.getDpiHorizontal()) + "x" + Math.round(size.getDpiVertical()) );
		  System.out.println("Print size: " + Math.round(dPt.getWidth()/72) + "\" x" + Math.round(dPt.getHeight()/72)+"\"" ); 
		
	}

	/**
	 * Convert image formats which are not supported by Word (eg EPS, PDF),
	 * into ones which are.  This requires ImageMagick to be on your
	 * system's path; for EPS and PDF images, Ghostscript is also required.
	 * 
	 * @param is
	 * @param os
	 * @param density  PixelsPerInch 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void convertToPNG(InputStream is, OutputStream os, int density) throws IOException, InterruptedException{
		
	/*
	 * See http://www.eichberger.de/2006/05/imagemagick-in-servlets.html
	 * 
	 * "Calling 'convert - png:-' as an external command and feeding it the 
	 * source image as standard input and reading the converted image 
	 * (in this case png) as standard output"
	 * 
	 */
		
	 System.out.println("Start ImageMagick...");
	 Process p = Runtime.getRuntime().exec("imconvert -density " + density + " -units PixelsPerInch - png:-");  
	 
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
		 System.out.println("Image copied...");
	 } catch (IOException ioe) {
		 
		 ioe.printStackTrace();
		 // debug
		 copy2(p.getErrorStream(), System.err);
	 }
	 
	 if (p.waitFor()!=0) {
	  System.err.println("Error");
	 }
	 System.out.println("End Process...");
	}

	public static void copy2(InputStream is, OutputStream os) throws IOException {
	    byte[] buffer = new byte[512];
	    while (true) {
	     int bytesRead = is.read(buffer);
	     if ( bytesRead == -1 ) break;
	     os.write(buffer, 0, bytesRead);
	    }
	    os.flush();
	   }//method
	}//class


	class StreamGobbler extends Thread
	{
		// The term "StreamGobbler" was taken from an article called "When Runtime.exec() won't", 
		// see http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html.
		
	  InputStream is;
	  OutputStream os;


	  StreamGobbler(InputStream is, OutputStream redirect)
	  {
	      this.is =  new BufferedInputStream(is);
	      this.os = redirect;
	  }

	  public void run()
	  {
	      try
	      {  
	    	  BinaryPartAbstractImage.copy2(is, os);
	      } catch (IOException ioe)
	          {
	          ioe.printStackTrace();
	          }
	  }
	  
	}
		
