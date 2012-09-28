package org.docx4j.samples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

public class FieldsMailMerge {
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(
				new java.io.File(
						System.getProperty("user.dir") + "/src/test/resources/MERGEFIELD.docx"));
		
		List<Map<DataFieldName, String>> data = new ArrayList<Map<DataFieldName, String>>();

		Map<DataFieldName, String> map = new HashMap<DataFieldName, String>();
		map.put( new DataFieldName("KundenNAme"), "Daffy duck");
		map.put( new DataFieldName("Kundenname"), "Plutext");
		map.put(new DataFieldName("Kundenstrasse"), "Bourke Street");
		
		data.add(map);
				
		map = new HashMap<DataFieldName, String>();
		map.put( new DataFieldName("Kundenname"), "Jason");
		map.put(new DataFieldName("Kundenstrasse"), "Collins Street");
		
		data.add(map);		
		
		
		System.out.println(XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true));

		WordprocessingMLPackage output = org.docx4j.model.fields.merge.MailMerger.getConsolidatedResultCrude(wordMLPackage, data, true);
		
		
		System.out.println(XmlUtils.marshaltoString(output.getMainDocumentPart().getJaxbElement(), true, true));
		
		output.save(new java.io.File(
				System.getProperty("user.dir") + "/OUT_FieldsMailMerge.docx") );
		
	}

}
