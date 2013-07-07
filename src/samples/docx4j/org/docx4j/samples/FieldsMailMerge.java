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

		// To get dates right, make sure you have docx4j property docx4j.Fields.Dates.DateFormatInferencer.USA
		// set to true or false as appropriate.  It defaults to non-US.
		map.put(new DataFieldName("yourdate"), "15/4/2013");  
		map.put(new DataFieldName("yournumber"), "2456800");
		
		data.add(map);
				
		map = new HashMap<DataFieldName, String>();
		map.put( new DataFieldName("Kundenname"), "Jason");
		map.put(new DataFieldName("Kundenstrasse"), "Collins Street");

		map.put(new DataFieldName("yourdate"), "12/10/2013");
		map.put(new DataFieldName("yournumber"), "1234800");
		
		data.add(map);		
		
		
		System.out.println(XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true));

		org.docx4j.model.fields.merge.MailMerger.keepMERGEFIELD(true);
		WordprocessingMLPackage output = org.docx4j.model.fields.merge.MailMerger.getConsolidatedResultCrude(wordMLPackage, data, true);
		
		
		System.out.println(XmlUtils.marshaltoString(output.getMainDocumentPart().getJaxbElement(), true, true));
		
		output.save(new java.io.File(
				System.getProperty("user.dir") + "/OUT_FieldsMailMerge.docx") );
		
	}

}
