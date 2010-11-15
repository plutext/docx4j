package org.docx4j.openpackaging.parts.opendope;

import javax.xml.bind.JAXBContext;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.opendope.components.Components.Component;

public class ComponentsPart extends JaxbCustomXmlDataStoragePart<org.opendope.components.Components> {
	
	private static Logger log = Logger.getLogger(ComponentsPart.class);		
	
	public ComponentsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public ComponentsPart(PartName partName, JAXBContext jc) throws InvalidFormatException {
		super(partName, jc);
		init();
	}
	
	public static Component getComponentById(
			org.opendope.components.Components components,
			String id) {
		
		for (Component c : components.getComponent() ) {
			
			if (c.getId().equals(id))
				return c;
		}
		
		log.warn("Component " + id + " is missing");
		return null;
	}

}
