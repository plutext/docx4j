package org.docx4j.openpackaging.parts.opendope;

import javax.xml.bind.JAXBContext;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.opendope.xpaths.Xpaths.Xpath;

public class XPathsPart extends JaxbCustomXmlDataStoragePart<org.opendope.xpaths.Xpaths> {
	
	private static Logger log = Logger.getLogger(XPathsPart.class);		
	
	public XPathsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public XPathsPart(PartName partName, JAXBContext jc) throws InvalidFormatException {
		super(partName, jc);
		init();
	}
	
	public Xpath getXPathById(String id) {
		
		for (Xpath c : this.jaxbElement.getXpath() ) {
			
			if (c.getId().equals(id))
				return c;
		}
		
		log.warn("XPath " + id + " is missing");
		return null;
	}


}
