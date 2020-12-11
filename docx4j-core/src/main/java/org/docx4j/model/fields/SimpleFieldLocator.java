package org.docx4j.model.fields;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.wml.CTSimpleField;

	
public class SimpleFieldLocator extends CallbackImpl {
	
	public List<CTSimpleField> simpleFields = new ArrayList<CTSimpleField>();	

	@Override
	public List<Object> apply(Object o) {
					
		if (o instanceof CTSimpleField) {
			simpleFields.add((CTSimpleField)o);
		}
		
		return null;
	}
}
