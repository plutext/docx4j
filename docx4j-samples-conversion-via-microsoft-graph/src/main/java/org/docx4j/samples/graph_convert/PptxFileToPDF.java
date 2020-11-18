package org.docx4j.samples.graph_convert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.docx4j.convert.out.microsoft_graph.DocxToPdfExporter;
import org.docx4j.convert.out.microsoft_graph.PptxToPdfExporter;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.plutext.msgraph.convert.DocxToPdfConverter;
import org.plutext.msgraph.convert.PptxToPdfConverter;
import org.plutext.msgraph.convert.graphsdk.DocxToPdfConverterLarge;
import org.plutext.msgraph.convert.graphsdk.PptxToPdfConverterLarge;


/**
 * This example uses Microsoft Graph to convert a pptx file to PDF.
 * 
 * There are 4 implementations to choose from; choose 1 in your pom.
 * 
 * See further https://github.com/plutext/java-docx-to-pdf-using-Microsoft-Graph
 * 
 * In order for this to work, you'll need to set up a Microsoft Graph 
 * environment, and provide its details in an AuthConfig.  See the above link for more details.
 * 
 * Note that you'd typically have a WordprocessingMLPackage, in which case
 * see that example instead.
 *
 */
public class PptxFileToPDF {

	
	public static void main(String[] args) throws IOException, Docx4JException {
	
		File output = new File(System.getProperty("user.dir")+"/pptx.pdf");
		FileOutputStream fos = new FileOutputStream(output); 
		
		// Choose your converter implementation here, corresponding to the module you've uncommented in your pom
		PptxToPdfConverter converter = new PptxToPdfConverterLarge(new AuthConfigImpl());

		PptxToPdfExporter exporter = new PptxToPdfExporter(converter);
		exporter.export(
				new File(System.getProperty("user.dir")+ "/../docx4j-samples-pptx4j/sample-docs/table.pptx") , 
				fos); 
		
		fos.close();
	}
	
}	
