package org.docx4j.samples;

import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.utils.XPathFactoryUtil;
import org.jvnet.jaxb2_commons.ppp.Child;

public class XPathQuery {

	
	/**
	 * Example of how to find an object in document.xml
	 * via XPath.
	 * 
	 * As explained in Getting Started, there are limitations 
	 * in the JAXB reference implementation which make this
	 * approach buggy:
	 * 
	 * 1. the xpath expressions are evaluated against the XML 
	 * document as it was when first opened in docx4j.  You 
	 * can update the associated XML document once only, by
	 * passing true into getJAXBNodesViaXPath. Updating it again 
	 * (with current JAXB 2.1.x or 2.2.x) will cause an error.
	 * 
	 * 2. For some documents, JAXB can’t set up the XPath at all!
	 * 
	 * If these problems affect you, you could try using
	 * MOXy as your JAXB implementation (which docx4j supports
	 * as from docx4j 3.0).  See 
	 * http://www.docx4java.org/forums/docx-java-f6/moxy-t1242.html
	 * 
	 * In you want to delete the object you've found, bear in mind
	 * it may be wrapped in a JAXBElement. 
	 * Do it as explained at https://stackoverflow.com/a/52134975/1031689
	 * 
	 * Alternatively, the tried and tested approach is
	 * to use TraversalUtil;
	 * see the OpenMainDocumentAndTraverse sample.
	 * 
	 */
	public static void main(String[] args) throws Exception {
		
		// Without Saxon, you are restricted to XPath 1.0
		boolean USE_SAXON = false; // set this to true; add Saxon to your classpath, and uncomment below 
		
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/sample-docx.xml";
				
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		
//		if (USE_SAXON) XPathFactoryUtil.setxPathFactory(
//		        new net.sf.saxon.xpath.XPathFactoryImpl());
		
		String xpath = "//w:t[contains(text(),'scaled')]";
		//String xpath = "//w:r[w:t[contains(text(),'scaled')]]";
		
		List<Object> list = documentPart.getJAXBNodesViaXPath(xpath, false);
		
		System.out.println("got " + list.size() + " matching " + xpath );
		
		int i=1;
		for (Object o : list) {
			
			System.out.println("\n " + i + ":" + o.getClass().getName() );
			
			Object o2 = XmlUtils.unwrap(o);
			System.out.println("with parent:" + ((Child)o2).getParent().getClass().getName() );
			
			if (o2 instanceof org.docx4j.wml.Text) {
				
				org.docx4j.wml.Text txt = (org.docx4j.wml.Text)o2;
				
				System.out.println( txt.getValue() );
				
			} else {
				System.out.println( XmlUtils.marshaltoString(o, true, true));
			}
			
			i++;
		}
	}
	
		
}
