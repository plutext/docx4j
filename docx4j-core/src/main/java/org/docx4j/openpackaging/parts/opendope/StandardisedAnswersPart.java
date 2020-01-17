package org.docx4j.openpackaging.parts.opendope;

import javax.xml.bind.JAXBContext;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

//	public static void main(String[] args) throws Docx4JException {
//
//		StandardisedAnswersPart sap = new StandardisedAnswersPart(new PartName("/testing"));
//		
//		Answers answers = new Answers();
//		Answer answer = new Answer();
//		answers.getAnswerOrRepeat().add(answer);
//		
//		answer.setId("id1");
//		answer.setValue("myval");
//		
//		sap.setContents(answers);
//		
//		System.out.println(sap.getXML());
//		
//		String prefixMappings = "xmlns:oda='http://opendope.org/answers'";
//		
//		System.out.println(
//				sap.xpathGetString("/oda:answers/oda:answer", prefixMappings));
//		
//		sap.setNodeValueAtXPath("/oda:answers/oda:answer", "foo", prefixMappings);
//		
//		System.out.println(
//				sap.xpathGetString("/oda:answers/oda:answer", prefixMappings));
//		
//	}
}
