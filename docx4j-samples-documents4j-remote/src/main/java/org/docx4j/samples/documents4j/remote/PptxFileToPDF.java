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
 * First you'll need Word and documents4j running on the remote server.
 * 
 * The easiest way to set up documents4j on a remote server is to build it
 * from https://github.com/documents4j/documents4j using maven, then in
 * documents4j-server-standalone, create a shaded jar using profile shaded-jar.
 * 
 * For example: mvn install -Pshaded-jar
 *  
 * Then start it:
 * 
 * java -jar documents4j-server-standalone-1.1.7-shaded.jar http://192.168.0.33:9998 --enable com.documents4j.conversion.msoffice.MicrosoftPowerpointBridge
 * 
 * (don't use http://localhost:9998 if you are trying to access it from a remote
 *  server!)
 *
 * Note that com.documents4j.conversion.msoffice.MicrosoftPowerpointBridge has to be 
 * specifically enabled (unlike the Word and Excel converters) 
 */
public class PptxFileToPDF {

	
	public static void main(String[] args) throws IOException, Docx4JException {
	
		File output = new File(System.getProperty("user.dir")+"/result_pptx.pdf");
		FileOutputStream fos = new FileOutputStream(output); 
		
		Documents4jRemoteServices exporter = new Documents4jRemoteServices();
		exporter.export(new File(System.getProperty("user.dir")+"/../docx4j-samples-pptx4j/sample-docs/pptx-chart.pptx") , 
				fos, DocumentType.MS_POWERPOINT); 
		
		fos.close();
	}
	
}	
