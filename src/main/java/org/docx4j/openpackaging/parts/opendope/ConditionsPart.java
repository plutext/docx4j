package org.docx4j.openpackaging.parts.opendope;

import javax.xml.bind.JAXBContext;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.PartName;
import org.opendope.conditions.Condition;

public class ConditionsPart extends JaxbCustomXmlDataStoragePart<org.opendope.conditions.Conditions> {
	
	private static Logger log = Logger.getLogger(ConditionsPart.class);		
	
	public ConditionsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public ConditionsPart(PartName partName, JAXBContext jc) throws InvalidFormatException {
		super(partName, jc);
		init();
	}
	
	public static Condition getConditionById(
			org.opendope.conditions.Conditions conditions,
			String id) {
		
		for (Condition c : conditions.getCondition() ) {
			
			if (c.getId().equals(id))
				return c;
		}
		
		log.warn("Condition " + id + " is missing");
		return null;
	}

}
