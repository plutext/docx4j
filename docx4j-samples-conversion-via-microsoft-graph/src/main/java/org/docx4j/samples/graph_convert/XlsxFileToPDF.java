package org.docx4j.samples.graph_convert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.docx4j.convert.out.microsoft_graph.XlsxToPdfExporter;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.plutext.msgraph.convert.XlsxToPdfConverter;
import org.plutext.msgraph.convert.graphsdk.XlsxToPdfConverterLarge;


/**
 * This example uses Microsoft Graph to convert an xlsx file to PDF.
 * 
 * There are 4 implementations to choose from; choose 1 in your pom.
 * 
 * See further https://github.com/plutext/java-docx-to-pdf-using-Microsoft-Graph
 * 
 * In order for this to work, you'll need to set up a Microsoft Graph 
 * environment, and provide its details in an AuthConfig.  See the above link for more details.
 * 
 * Note that you'd typically have a SpreadsheetMLPackage, in which case
 * see that example instead.
 *
 */
public class XlsxFileToPDF {

	
	public static void main(String[] args) throws IOException, Docx4JException {
	
		File output = new File(System.getProperty("user.dir")+"/xlsx.pdf");
		FileOutputStream fos = new FileOutputStream(output); 
		
		// Choose your converter implementation here, corresponding to the module you've uncommented in your pom
		XlsxToPdfConverter converter = new XlsxToPdfConverterLarge(new AuthConfigImpl());

		XlsxToPdfExporter exporter = new XlsxToPdfExporter(converter);
		exporter.export(
				new File(System.getProperty("user.dir")+ "/../docx4j-samples-xlsx4j/sample-docs/comments.xlsx") , 
				fos); 
		
		fos.close();
	}
	
}	
