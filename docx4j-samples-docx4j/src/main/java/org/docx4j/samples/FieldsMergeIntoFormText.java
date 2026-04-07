package org.docx4j.samples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.model.fields.merge.MailMerger.OutputField;
import org.docx4j.XmlUtils;
import org.docx4j.model.fields.formtext.FORMTEXTMerger;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Microsoft intended FORMTEXT fields for 
 * a user interactively filling forms via the Word UI
 * (compared to MERGEFIELD which are intended for the
 *  automated case).
 *  
 * Even so, organisations sometimes wish to 
 * automate documents containing FORMTEXT fields. 
 * 
 * This class facilitates that.
 * 
 * Datafield name is expected to be in textInput/default. 
 * 
 * Recommended best practice is to migrate to content 
 * control data binding.  org.docx4j.model.datastorage.migration.FromFormText
 * facilitates that.
 * 
 * @author jharrop
 * @since 11.5.2
 */
public class FieldsMergeIntoFormText {
	
	/*
    <w:ffData>
        <w:name w:val=""/>
        <w:enabled/>
        <w:calcOnExit w:val="false"/>
        <w:textInput>
            <w:default w:val="sellerFax"/> <-------- data field name
        </w:textInput>
    </w:ffData>
 */
	
	public static void main(String[] args) throws Exception {
		
		// Whether to create a single output docx, or a docx per Map of input data.
		// Note: If you only have 1 instance of input data, then you can just invoke performMerge
		boolean mergedOutput = true;
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(
				new java.io.File(
						System.getProperty("user.dir")+ "/T.docx"));
//						System.getProperty("user.dir") + "/template.docx"));
		
		List<Map<DataFieldName, String>> data = new ArrayList<Map<DataFieldName, String>>();

		System.out.println(wordMLPackage.getMainDocumentPart().getXML());
		
		// Instance 1
		Map<DataFieldName, String> map = new HashMap<DataFieldName, String>();
		map.put( new DataFieldName("customerContactName"), "Jason"); // ignored
		map.put( new DataFieldName("customerPhone"), "0402 123 123");
		map.put(new DataFieldName("customerEmail"), "jason@plutext.org");
		data.add(map);
				
		// Instance 2
		map = new HashMap<DataFieldName, String>();
		map.put( new DataFieldName("customerContactName"), "Jane"); // ignored
		map.put( new DataFieldName("customerPhone"), "0419 321 321");
		map.put(new DataFieldName("customerEmail"), "jane@gmail.com");
		data.add(map);		
		
		FORMTEXTMerger formtextMerger = new FORMTEXTMerger(wordMLPackage);
		
		if (mergedOutput) {
			/*
			 * This is a "poor man's" merge, which generates the mail merge  
			 * results as a single docx, and just hopes for the best.
			 * Images and hyperlinks should be ok. But numbering 
			 * will continue, as will footnotes/endnotes.
			 *  
			 * If your resulting documents aren't opening in Word, then
			 * you probably need MergeDocx to perform the merge.
			 */

			// How to treat the field in the output?
			FORMTEXTMerger.setOutputField(OutputField.AS_FORMTEXT_REGULAR);
//			FORMTEXTMerger.setOutputField(OutputField.REMOVED); 
			
//			System.out.println(wordMLPackage.getMainDocumentPart().getXML());
			
			WordprocessingMLPackage output = formtextMerger.getConsolidatedResultCrude( data, true);
			
//			System.out.println(wordMLPackage.getMainDocumentPart().getXML());
			
			output.save(new java.io.File(
					System.getProperty("user.dir") + "/OUT_FieldsMailMerge.docx") );
			
		} else {
			// Need to keep the FORMTEXT. If you don't, you'd have to clone the docx, and perform the
			// merge on the clone.  For how to clone, see the MailMerger code, method getConsolidatedResultCrude
			FORMTEXTMerger.setOutputField(OutputField.AS_FORMTEXT_REGULAR);
			
			int i = 1;
			for (Map<DataFieldName, String> thismap : data) {
				formtextMerger.performMerge( thismap, true);
				System.out.println(wordMLPackage.getMainDocumentPart().getXML());
				wordMLPackage.save(new java.io.File(
						System.getProperty("user.dir") + "/OUT_FieldsMailMerge_" + i + ".docx") );
				i++;
			}			
		}
		
	}
	
	

}
