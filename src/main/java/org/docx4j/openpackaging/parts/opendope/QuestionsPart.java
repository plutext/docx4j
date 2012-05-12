package org.docx4j.openpackaging.parts.opendope;

import javax.xml.bind.JAXBContext;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.PartName;
import org.opendope.conditions.Condition;
import org.opendope.questions.Question;

public class QuestionsPart extends JaxbCustomXmlDataStoragePart<org.opendope.questions.Questionnaire> {
	
	private static Logger log = Logger.getLogger(QuestionsPart.class);		
	
	public QuestionsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public QuestionsPart(PartName partName, JAXBContext jc) throws InvalidFormatException {
		super(partName, jc);
		init();
	}
	
	public static Question getQuestionById(
			org.opendope.questions.Questionnaire questionnaire,
			String id) {
		
		for (Question qu : questionnaire.getQuestions().getQuestion() ) {
			
			if (qu.getId().equals(id))
				return qu;
		}
		
		log.warn("Question " + id + " is missing");
		return null;
	}

}
