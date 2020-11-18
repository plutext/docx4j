package org.docx4j.samples;

import org.docx4j.XmlUtils;
import org.docx4j.model.fields.FieldUpdater;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Example showing how to refresh DOCPROPERTY and DOCVARIABLE
 * fields in the docx.
 * (For MERGEFIELD, use FieldsMailMerge instead) 
 * 
 * This updates the docx.  If you don't want to do
 * that, apply it to a clone instead.
 */
public class FieldUpdaterExample {

	public static void main(String[] args) throws Docx4JException {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(
				new java.io.File(
						System.getProperty("user.dir") + "/DocProp.docx")); 
		
		FieldUpdater fu = new FieldUpdater(wordMLPackage);
		fu.update(true);
		
		System.out.println(XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true));
		
	}
	
}
