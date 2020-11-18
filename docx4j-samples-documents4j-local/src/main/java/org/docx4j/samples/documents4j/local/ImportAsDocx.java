package org.docx4j.samples.documents4j.local;

import java.io.File;
import java.io.IOException;

import org.docx4j.documents4j.local.Documents4jLocalServices;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;


/**
 * This example uses documents4j (running locally) to import HTML, RTF or binary .doc as docx.
 * 
 */
public class ImportAsDocx {

	
	public static void main(String[] args) throws IOException, Docx4JException {
	
		Documents4jLocalServices importer = new Documents4jLocalServices();
		WordprocessingMLPackage  pkg = importer.importAsDocx(new File(System.getProperty("user.dir")+"/in.htm"));
		
		System.out.println(pkg.getMainDocumentPart().getXML());
	}
	
}	
