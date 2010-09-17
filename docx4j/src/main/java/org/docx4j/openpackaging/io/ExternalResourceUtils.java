package org.docx4j.openpackaging.io;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.ExternalTarget;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageGifPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageJpegPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImagePngPart;

public class ExternalResourceUtils {
	
	/* This was removed from Load and placed in a separate class,
	 * so that Load can load, even if the VFS jar is not present.
	 */

	private static Logger log = Logger.getLogger(ExternalResourceUtils.class);
	
	public static BinaryPart getExternalResource(String absoluteTarget) throws Docx4JException {

		try {
			FileObject fo = VFS.getManager().resolveFile(absoluteTarget);
			
			ExternalTarget externalTarget = new ExternalTarget(absoluteTarget);
			
			// Assume it is a binary part, though there is no reason in principle
			// that it couldn't be an XML part..			
			BinaryPart bp;
			if (absoluteTarget.toLowerCase().endsWith(".gif" )) {
				
				bp = new ImageGifPart(externalTarget); 
				
			} else if  (absoluteTarget.toLowerCase().endsWith(".jpeg" )
					|| absoluteTarget.toLowerCase().endsWith(".jpg" )) {
				
				bp = new ImageJpegPart(externalTarget); 
				
			} else if (absoluteTarget.toLowerCase().endsWith(".png" )) {
				
				bp = new ImagePngPart(externalTarget); 
				
			} else {
				log.warn("Using simple BinaryPart for " + absoluteTarget);
				bp = new BinaryPart(externalTarget);
			}
			
			FileContent fc = fo.getContent();
			bp.setBinaryData(fc.getInputStream());			
			
			return bp;
			
		} catch (FileSystemException exc) {
			exc.printStackTrace();
			throw new Docx4JException("Couldn't get FileObject", exc);			
		}
		
	}
	

}
