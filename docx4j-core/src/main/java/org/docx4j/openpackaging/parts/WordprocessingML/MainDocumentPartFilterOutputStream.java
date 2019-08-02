package org.docx4j.openpackaging.parts.WordprocessingML;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.docx4j.Docx4jProperties;
import org.docx4j.Version;

/**
 * Write comment to document.xml with docx4j / JAXB / Java / OS info.
 * @author jharrop
 * @since 6.1.0
 */
public class MainDocumentPartFilterOutputStream extends FilterOutputStream {
	
	boolean isNewPkg = true;	

	public MainDocumentPartFilterOutputStream(OutputStream out, boolean isNewPkg) {
		super(out);
		this.isNewPkg = isNewPkg;
	}
	
	boolean commentWritten = false;
		 
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		
		if (commentWritten) {
			super.write(b, off, len);
			return;
		}
		
    	if (Docx4jProperties.getProperty("docx4j.jaxb.marshal.suppressVersionComment", false)==true) {
    		commentWritten = true; // fiction
			super.write(b, off, len);
			return;
    	}
		
		String text = new String(b, off, len);
		int pos = text.indexOf("<w:body>");  // code assumes we don't receive a fragment
		if (pos<0) {
			super.write(b, off, len);
			return;
		}

		int bodyEnd = pos + 8;
		String bodyString = text.substring(0, bodyEnd);
		out.write(bodyString.getBytes("UTF-8"));
		
		String comment = "<!-- " + Version.getPoweredBy(isNewPkg) + " -->"; 
		out.write(comment.getBytes("UTF-8"));

		out.write(text.substring(bodyEnd).getBytes("UTF-8"));

		commentWritten = true;
		
	}
	
	
	
}