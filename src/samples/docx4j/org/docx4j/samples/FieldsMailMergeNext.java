package org.docx4j.samples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.model.fields.merge.MailMerger.OutputField;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Example of how to process mailing labels with MERGEFIELD / NEXT
 * 
 * See http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/MERGEFIELD.html
 *
 */
public class FieldsMailMergeNext {

	public static void main(String[] args) throws Exception {
		
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(
				new java.io.File(
						System.getProperty("user.dir") + "/template.docx"));
		
		List<Map<DataFieldName, String>> data = new ArrayList<Map<DataFieldName, String>>();
		

		// Instance 1
		Map<DataFieldName, String> map = new HashMap<DataFieldName, String>();
		map.put( new DataFieldName("Anrede"), "Daffy duck");
		map.put( new DataFieldName("Vorname"), "Plutext");
		data.add(map);
				
		// Instance 2
		map = new HashMap<DataFieldName, String>();
		map.put( new DataFieldName("Anrede"), "Jason");
		map.put(new DataFieldName("Vorname"), "Collins Street");
		data.add(map);		
		

		// How to treat the MERGEFIELD, in the output?
		org.docx4j.model.fields.merge.MailMerger.setMERGEFIELDInOutput(OutputField.REMOVED);
		
		org.docx4j.model.fields.merge.MailMergerWithNext.performLabelMerge(wordMLPackage, data);
		
		wordMLPackage.save(new java.io.File(
				System.getProperty("user.dir") + "/OUT_FieldsMailMerge.docx") );
			
		
	}

}
