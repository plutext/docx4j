package org.docx4j.openpackaging.parts.opendope;

import javax.xml.bind.JAXBContext;

import org.docx4j.model.datastorage.InputIntegrityException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.opendope.questions.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestionsPart extends JaxbCustomXmlDataStoragePart<org.opendope.questions.Questionnaire> {
	
	private static Logger log = LoggerFactory.getLogger(QuestionsPart.class);		
	
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
		
		throw new InputIntegrityException("No question with id " + id );		
	}

}
