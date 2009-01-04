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


package org.docx4j.utils;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.io.File;

import org.apache.log4j.Logger;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.DefaultImageContext;
import org.apache.xmlgraphics.image.loader.impl.DefaultImageSessionContext;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypeManagerImpl;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.SectPr.PgMar;
import org.docx4j.wml.SectPr.PgSz;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.OleObjectBinaryPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Image utilities 
 * 
 * @author Jason Harrop
 * @version 1.0
 */
public class ImageUtils {

	protected static Logger log = Logger.getLogger(ImageUtils.class);
	
	static {
		
		imageManager = new ImageManager(new DefaultImageContext());
		
	}

	static ImageManager imageManager;
	
	public static BinaryPart createImagePart(WordprocessingMLPackage wordMLPackage,
			byte[] bytes,
			String partName) throws Exception {
		
		// TODO: consider whether an abstract ImagePart
		// is desirable		
		
		log.debug("entering, for " + partName );		
		
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
			
			int dpi = 300;
			
			convertToPNG(bais, fos, dpi);
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
		BinaryPart imagePart = (BinaryPart)ctm.newPartForContentType(info.getMimeType(), partName);
		log.debug("created part " + imagePart.getClass().getName() );		
		
		
		FileInputStream fis = new FileInputStream(tmpImageFile); //reuse		
		imagePart.setBinaryData( fis );
				
		Relationship rel = wordMLPackage.getMainDocumentPart().addTargetPart(imagePart);
		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();
		
		// Since the object will be added at the end of the document,
		// it is safe to look for the page dimensions in the last sectPr 
		SectPr sectPr = wmlDocumentEl.getBody().getSectPr();
		
		double writableWidthTwips;
		if (sectPr==null) {
			
			log.debug("PgSz and PgMar not defined in this doc's SectPr element");
			// Have to make a sensible default
			// A4
			writableWidthTwips = 12240 - (1440 + 1440); 
				
		} else {
			
			PgSz pgSz = sectPr.getPgSz();
			PgMar pgMar = sectPr.getPgMar();

			writableWidthTwips = pgSz.getW().doubleValue() - (pgMar.getLeft().doubleValue() + pgMar.getRight().doubleValue() );
		}
				
		log.debug("writableWidthTwips: " + writableWidthTwips);
		
		  ImageSize size = info.getSize();
		  
		  Dimension2D dPt = size.getDimensionPt();
		double imageWidthTwips = dPt.getWidth() * 20;
		log.debug("imageWidthTwips: " + imageWidthTwips);
		
		double cx;
		double cy;
		if (imageWidthTwips>writableWidthTwips) {
			
			log.debug("Scaling image to fit page width");
			
			cx = twipToEMU(writableWidthTwips);
			cy = twipToEMU(dPt.getHeight() * 20 * writableWidthTwips/imageWidthTwips);
			
		} else {

			log.debug("Scaling image - not necessary");
			
			cx = twipToEMU(imageWidthTwips);
			cy = twipToEMU(dPt.getHeight() * 20);			
			
		}
		
		
		
		// Contains ${docPrId}, ${docPrName}, ${docPrDesc}, ${picName}, ${rEmbedId}
        String ml ="<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\"><w:r>" +
        "<w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\">" +
        //"<wp:extent cx=\"${cx}\" cy=\"${cy}\"/>" +
        "<wp:effectExtent l=\"19050\" t=\"0\" r=\"0\" b=\"0\"/>" +
        "<wp:docPr id=\"${docPrId}\" name=\"${docPrName}\" descr=\"${docPrDesc}\"/><wp:cNvGraphicFramePr><a:graphicFrameLocks xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" noChangeAspect=\"1\"/></wp:cNvGraphicFramePr><a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\"><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:nvPicPr><pic:cNvPr id=\"0\" name=\"${picName}\"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed=\"${rEmbedId}\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"3238500\" cy=\"2362200\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>";
        java.util.HashMap<String, String>mappings = new java.util.HashMap<String, String>();
        
        mappings.put("cx", Double.toString(cx));
        mappings.put("cy", Double.toString(cy));
        mappings.put("docPrId", "1");
        mappings.put("docPrName", "Picture 1");
        mappings.put("docPrDesc", "some.jpeg");
        mappings.put("picName", "some.jpeg");
        mappings.put("rEmbedId", rel.getId()  );

        wordMLPackage.getMainDocumentPart().addObject(
              org.docx4j.XmlUtils.unmarshallFromTemplate(ml, mappings ) );
		
		
		  // Delete the tmp file
		//tmpImageFile.delete();
		  
		return imagePart;
		
	}
	
	
	public static double twipToEMU(double twips) {
		
		return 635 * twips;
				
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
		  
//		  System.out.println("dpi x: " + size.getDpiHorizontal() );
//		  System.out.println("points: " + d2d.getWidth() );
//		  System.out.println("so inches: " +  d2d.getWidth()/72 );	
		  
//		  double xPixels =  size.getDpiHorizontal() * dPt.getWidth()/72 ;
		  
		  
//		  System.out.println("\n dpi y: " + size.getDpiVertical() );
//		  System.out.println("points: " + d2d.getHeight() );
//		  System.out.println("so inches: " +  d2d.getHeight()/72 );	
		  
//		  double yPixels =  size.getDpiVertical() * dPt.getHeight()/72 ;

		  System.out.println(info.getOriginalURI() + " " + info.getMimeType() 
				  + " " + dPx.getWidth() +"x" + dPx.getHeight());
		  
//		  System.out.println( "should be the same as  " + xPixels +"x" + yPixels);
		  
		  
		  System.out.println("Resolution:" + size.getDpiHorizontal() + "x" + size.getDpiVertical() );
		  System.out.println("Print size: " + dPt.getWidth()/72 + "x" + dPt.getHeight()/72 ); 
		
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
	private static void convertToPNG(InputStream is, OutputStream os, int density) throws IOException, InterruptedException{
		
	/*
	 * See http://www.eichberger.de/2006/05/imagemagick-in-servlets.html
	 * 
	 * "Calling 'convert - png:-' as an external command and feeding it the 
	 * source image as standard input and reading the converted image 
	 * (in this case png) as standard output"
	 * 
	 */
		
	 System.out.println("Start ImageMagick...");
	 Process p = Runtime.getRuntime().exec("convert -density " + density + " -units PixelsPerInch - png:-");  // jpeg
	 //initialize Gobblers
	 StreamGobbler inGobbler = new StreamGobbler(p.getInputStream(), os);
	 StreamGobbler errGobbler = new StreamGobbler(p.getErrorStream(), System.err);
	 //start them
	 inGobbler.start();
	 errGobbler.start();
	 
	 // p.getOutputStream() is the _output stream_ of the subprocess, so
	 // this copies is into the standard input stream of the process 
	 copy2(is, new BufferedOutputStream(p.getOutputStream()));
	 
	 p.getOutputStream().close();
	 System.out.println("Image copied...");
	 //copy2(p.getErrorStream(), System.err);
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
	    	  ImageUtils.copy2(is, os);
	      } catch (IOException ioe)
	          {
	          ioe.printStackTrace();
	          }
	  }
	  
	}
	
	
