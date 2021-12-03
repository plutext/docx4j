package org.docx4j.samples.documents4j.remote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.docx4j.documents4j.remote.Documents4jRemoteServices;
import org.docx4j.openpackaging.exceptions.Docx4JException;

import com.documents4j.api.DocumentType;


/**
 * This example uses documents4j (running remotely) to convert an XLSX file
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
 * java -jar documents4j-server-standalone-1.1.7-shaded.jar http://localhost:9998
 * 
 * (don't use http://localhost:9998 if you are trying to access it from a remote
 *  server!)
 * 
 * or with logging:
 *   
 *   java -jar documents4j-server-standalone-1.1.7-shaded.jar http://192.168.0.33:9998 --level debug *  
 * Note that you'd typically have a SpreadsheetMLPackage, in which case
 * see that example instead.
 *
 */
public class XlsxFileToPDF {

	
	public static void main(String[] args) throws IOException, Docx4JException {
	
		File output = new File(System.getProperty("user.dir")+"/result-xlsx.pdf");
		FileOutputStream fos = new FileOutputStream(output); 
		
		Documents4jRemoteServices exporter = new Documents4jRemoteServices();
		exporter.export(new File(System.getProperty("user.dir")+"/../docx4j-samples-xlsx4j/sample-docs/comments.xlsx") , 
				fos, DocumentType.MS_EXCEL); 
		
		fos.close();
	}
	
}	
