package org.docx4j.openpackaging.parts.opendope;

import javax.xml.bind.JAXBContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;

public class StandardisedAnswersPart extends JaxbCustomXmlDataStoragePart<org.opendope.answers.Answers> {
	
	private static Logger log = LoggerFactory.getLogger(StandardisedAnswersPart.class);		
	
	public StandardisedAnswersPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public StandardisedAnswersPart(PartName partName, JAXBContext jc) throws InvalidFormatException {
		super(partName, jc);
		init();
	}
	
}
