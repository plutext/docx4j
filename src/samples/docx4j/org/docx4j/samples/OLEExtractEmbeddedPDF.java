package org.docx4j.samples;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.OleObjectBinaryPart;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentInputStream;

public class OLEExtractEmbeddedPDF {

	public static void main(String[] args) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File(System.getProperty("user.dir")
				+ "/yourdocx.docx"));

		for (Map.Entry<PartName,Part> entry : wordMLPackage.getParts().getParts().entrySet()) {
			
			Part p = entry.getValue();
			if (p instanceof OleObjectBinaryPart) {

				OleObjectBinaryPart olePart = (OleObjectBinaryPart)p;
				DocumentInputStream inputStream = olePart.getFs().createDocumentInputStream("CONTENTS");
				byte[] pdfBytes = IOUtils.toByteArray(inputStream);
				
				// Do something with it here...
				System.out.println("Found " + p.getPartName().getName() );
			}			
			
		}
	}
	
	
}