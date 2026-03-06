package org.docx4j.samples;

import java.io.File;
import java.util.Map;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.OleObjectBinaryPart;

public class OLEPartMimeType {

	public static void main(String[] args) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File(System.getProperty("user.dir")
				+ "/yourdocx.docx"));
		

		for (Map.Entry<PartName,Part> entry : wordMLPackage.getParts().getParts().entrySet()) {
			
			Part p = entry.getValue();
			if (p instanceof OleObjectBinaryPart) {

				System.out.println("\n\nFound " + p.getPartName().getName() );
				
				OleObjectBinaryPart olePart = (OleObjectBinaryPart)p;
				
				olePart.viewFile(true);
				
				String result = olePart.detectMimeType();
				System.out.println("Result: " + result );
				
			}			
			
		}
	}
	
	
}