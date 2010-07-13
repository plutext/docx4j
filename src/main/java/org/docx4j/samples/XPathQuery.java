package org.docx4j.samples;

import java.util.List;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

public class XPathQuery {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		//String inputfilepath = System.getProperty("user.dir") + "/sample-docs/Images.docx";
		String inputfilepath = System.getProperty("user.dir") + "//tmp//modelo.docx";
//    	String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/fo-200912.xml";
				
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
				
		String xpath = "//w:p";
		
		List<Object> list = documentPart.getJAXBNodesViaXPath(xpath, false);
		
		System.out.println("got " + list.size() + " matching " + xpath );
						
	}
	
		
}
