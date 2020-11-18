package org.docx4j.samples.graph_convert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.microsoft_graph.DocxToPdfExporter;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.plutext.msgraph.convert.DocxToPdfConverter;
import org.plutext.msgraph.convert.graphsdk.DocxToPdfConverterLarge;



/**
 * This example uses Microsoft Graph to convert from docx 
 * to PDF.
 * 
 * There are 4 implementations to choose from; choose 1 in your pom.
 * 
 * See further https://github.com/plutext/java-docx-to-pdf-using-Microsoft-Graph
 * 
 * In order for this to work, you'll need to set up a Microsoft Graph 
 * environment, and provide its details in an AuthConfig.  See the above link for more details.
 * 
 * Typically in docx4j you have a WordprocessingMLPackage, which is what we convert from
 * in this example.  
 *
 */
public class WordMLToPDF {

	
	public static void main(String[] args) throws IOException, Docx4JException {

		WordprocessingMLPackage pkg = (WordprocessingMLPackage)Docx4J.load(
				new File(System.getProperty("user.dir")
						+ "/../docx4j-samples-docx4j/sample-docs/sample-docx.docx"));
		
		File output = new File(System.getProperty("user.dir")+"/resultZZ.pdf");
		FileOutputStream fos = new FileOutputStream(output); 
		
		// Choose your converter implementation here, corresponding to the module you've uncommented in your pom
		DocxToPdfConverter converter = new DocxToPdfConverterLarge(new AuthConfigImpl());

		DocxToPdfExporter exporter = new DocxToPdfExporter(converter);
		exporter.export(pkg, fos);
		
		// or equivalently,
		// Docx4J.toPDF(pkg, fos);
		
		fos.close();
		
		// If you choose to use the using-graph-sdk implementation, 
		// it uses OKHttp; you'll see a bunch of threads continue running.
	}
	
}	
