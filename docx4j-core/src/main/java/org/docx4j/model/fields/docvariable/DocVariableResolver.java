package org.docx4j.model.fields.docvariable;

import java.util.HashMap;

import org.docx4j.model.fields.FieldFormattingException;
import org.docx4j.model.fields.FieldValueException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTDocVar;
import org.docx4j.wml.CTDocVars;
import org.docx4j.wml.CTSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a docVar is stored in the docVars element
 * in the DocumentSettings part.
 * 
 * @author jharrop
 * @since 8.2.2
 */
public class DocVariableResolver {
	
	private static Logger log = LoggerFactory.getLogger(DocVariableResolver.class);		
	
	private HashMap<String, String> docVarMap = new HashMap<String, String>(); 
	
	public DocVariableResolver(WordprocessingMLPackage wordMLPackage) {
		
		CTSettings settings = wordMLPackage.getMainDocumentPart().getDocumentSettingsPart() == null 
				? null : wordMLPackage.getMainDocumentPart().getDocumentSettingsPart().getJaxbElement(); 
		
		if (settings!=null && settings.getDocVars()!=null) {
			CTDocVars docvars = settings.getDocVars();
			for(CTDocVar docvar: docvars.getDocVar()) {
				docVarMap.put(docvar.getName(), docvar.getVal() ); 
			}
		}
		
	}
	
	public String getValue(String key) throws FieldFormattingException, FieldValueException {
		
		String value = docVarMap.get(key);
		if (value==null) {
			throw new FieldValueException("No value found for DOCVARIABLE " + key);
		} else {
			return value;
		}
	}
	

}
