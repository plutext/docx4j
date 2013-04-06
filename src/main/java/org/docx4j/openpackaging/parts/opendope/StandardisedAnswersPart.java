package org.docx4j.openpackaging.parts.opendope;

import javax.xml.bind.JAXBContext;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbCustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.PartName;

public class StandardisedAnswersPart extends JaxbCustomXmlDataStoragePart<org.opendope.answers.Answers> {
	
	private static Logger log = Logger.getLogger(StandardisedAnswersPart.class);		
	
	public StandardisedAnswersPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public StandardisedAnswersPart(PartName partName, JAXBContext jc) throws InvalidFormatException {
		super(partName, jc);
		init();
	}
	
}
