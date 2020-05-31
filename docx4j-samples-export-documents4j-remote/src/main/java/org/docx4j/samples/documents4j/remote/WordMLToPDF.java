package org.docx4j.samples.documents4j.remote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.documents4j.remote.Documents4jRemoteExporter;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;


/**
 * This example uses documents4j (running remotely) to convert from docx 
 * to PDF.
 * 
 * Typically in docx4j you have a WordprocessingMLPackage, which is what we convert from
 * in this example.  
 *
 */
public class WordMLToPDF {

	
	public static void main(String[] args) throws IOException, Docx4JException {

		WordprocessingMLPackage pkg = Docx4J.load(new File(System.getProperty("user.dir")+"/Hello.docx"));
		
		File output = new File(System.getProperty("user.dir")+"/resultZ.pdf");
		FileOutputStream fos = new FileOutputStream(output); 
		
		Documents4jRemoteExporter exporter = new Documents4jRemoteExporter();
		exporter.export(pkg, fos);
		
		// or equivalently,
		// Docx4J.toPDF(pkg, fos);
		
		fos.close();
	}
	
}	
