package org.docx4j.samples;

import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

public class XPathQuery {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docx.xml";
		//String inputfilepath = System.getProperty("user.dir") + "//tmp//modelo.docx";
//    	String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/fo-200912.xml";
				
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
				
		//String xpath = "//w:t";
		String xpath = "//w:r[w:t[contains(text(),'scaled')]]";
		
		List<Object> list = documentPart.getJAXBNodesViaXPath(xpath, false);
		
		System.out.println("got " + list.size() + " matching " + xpath );
		
		for (Object o : list) {
			
			System.out.println(o.getClass().getName() );
			
			Object o2 = XmlUtils.unwrap(o);
			// this is ok, provided the results of the Callback
			// won't be marshalled
			
			
			if (o2 instanceof org.docx4j.wml.Text) {
				
				org.docx4j.wml.Text txt = (org.docx4j.wml.Text)o2;
				
				System.out.println( txt.getValue() );
				
				Object parent = txt.getParent();
			
				System.out.println( "parent: " +  XmlUtils.unwrap(parent).getClass().getName() );
			} else {
				System.out.println( XmlUtils.marshaltoString(o, true, true));
			}

			
			
		}
						
	}
	
		
}
