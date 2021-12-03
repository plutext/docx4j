package org.docx4j.samples.documents4j.local;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.docx4j.documents4j.local.Documents4jLocalServices;
import org.docx4j.openpackaging.exceptions.Docx4JException;

import com.documents4j.api.DocumentType;


/**
 * This example uses documents4j (running locally) to convert an Office file
 * to PDF.  This example expects docx|pptx|xlsx but docm etc should also work.
 * 
 * Note that you'd typically have a WordprocessingMLPackage or whatever, in which case
 * see that example instead.
 *
 */
public class OfficeFileToPDF {

	
	public static void main(String[] args) throws IOException, Docx4JException {
	
		String inputPath = System.getProperty("user.dir")+"/Hello.docx";
		
		File output = new File(System.getProperty("user.dir")+"/result.pdf");
		FileOutputStream fos = new FileOutputStream(output); 
		
		Documents4jLocalServices exporter = new Documents4jLocalServices();
		
		if (inputPath.toLowerCase().endsWith(".docx")) {
			exporter.export(new File(inputPath) , fos, DocumentType.MS_WORD);
		} else if (inputPath.toLowerCase().endsWith(".pptx")) {
			exporter.export(new File(inputPath) , fos, DocumentType.MS_POWERPOINT);
		} else if (inputPath.toLowerCase().endsWith(".xlsx")) {
			exporter.export(new File(inputPath) , fos, DocumentType.MS_EXCEL);
		} else  {
			System.out.println("File type not supported by this example, but may still work.");
		}
		
		fos.close();
	}
	
}	
