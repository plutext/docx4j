package org.docx4j.samples.documents4j.remote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.docx4j.documents4j.remote.Documents4jRemoteServices;
import org.docx4j.openpackaging.exceptions.Docx4JException;

import com.documents4j.api.DocumentType;


/**
 * This example uses documents4j (running remotely) to convert a docx file
 * to PDF.
 * 
 * Note that you'd typically have a WordprocessingMLPackage, in which case
 * see that example instead.
 *
 */
public class DocxFileToPDF {

	
	public static void main(String[] args) throws IOException, Docx4JException {
	
		File output = new File(System.getProperty("user.dir")+"/result.pdf");
		FileOutputStream fos = new FileOutputStream(output); 
		
		Documents4jRemoteServices exporter = new Documents4jRemoteServices();
		exporter.export(new File(System.getProperty("user.dir")+"/Hello.docx") , fos, DocumentType.MS_WORD); 
		
		fos.close();
	}
	
}	
