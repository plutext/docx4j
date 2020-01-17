package org.docx4j.samples;

import java.io.File;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * There are at least 3 approaches for replacing variables in 
 * a docx.
 * 
 * 1. as shown in this example
 * 2. using Merge Fields (see org.docx4j.model.fields.merge.MailMerger)
 * 3. binding content controls to an XML Part (via XPath)
 * 
 * Approach 3 is the recommended one when using docx4j. See the 
 * ContentControl* examples, Getting Started, and the subforum.
 * 
 * Approach 1, works in simple cases only. 
 * 
 * It won't work if your KEY is split across separate
 * runs in your docx (which often happens), or if you want 
 * to insert images, or multiple rows in a table.
 * 
 * This class tidies up your document, so your keys should
 * not be split across separate runs.
 */
public class VariablePrepareRuns extends AbstractSample {

	
	static boolean save = true;
	
	public static void main(String[] args) throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
	    	inputfilepath = System.getProperty("user.dir") + "/input.docx";
		}
		System.out.println(inputfilepath);	    			
		
		// Load the docx
		WordprocessingMLPackage wmlPackage = Docx4J.load(new java.io.File(inputfilepath));
		
		// Before .. note attributes w:rsidRDefault="00D15781" w:rsidR="00D15781"
		System.out.println(XmlUtils.marshaltoString(wmlPackage.getMainDocumentPart().getJaxbElement(), true, true));
		
		org.docx4j.model.datastorage.migration.VariablePrepare.prepare(wmlPackage);
		
		System.out.println(XmlUtils.marshaltoString(wmlPackage.getMainDocumentPart().getJaxbElement(), true, true));

		// Save it
		if (save) {
			String outputfilepath = System.getProperty("user.dir") + "/OUT_VariablePrepare.docx";
			Docx4J.save(wmlPackage, new File(outputfilepath), Docx4J.FLAG_NONE); //(FLAG_NONE == default == zipped docx)
			
			System.out.println("Saved: " + outputfilepath);
		} 
	}	
}
