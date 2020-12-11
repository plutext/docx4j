package org.docx4j.model.datastorage;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

public class JAXBElementInRepeatBugFixTest {

	/**
	 * w:smartTag is represented using a JAXBElement.
	 * It doesn't have an @XmlRootElement annotation.
	 * So if it is unwrapped, then it can't be marshalled.
	 * This test will throw an exception unless that 
	 * issue is addressed.
	 * @throws Exception
	 */
	@Test
	public void testSmartTag() throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") 
			+ "/src/test/resources/OpenDoPE/repeat-containing-JAXBElement.docx";
				
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));				

		System.out.println(
		XmlUtils.marshaltoString(
				wordMLPackage.getMainDocumentPart().getJaxbElement(), true) );
		
		// Process conditionals and repeats
		OpenDoPEHandler odh = new OpenDoPEHandler(wordMLPackage);
		wordMLPackage = odh.preprocess();

		XmlUtils.marshaltoString(
				wordMLPackage.getMainDocumentPart().getJaxbElement(), true) ;
		
	}
	
}
