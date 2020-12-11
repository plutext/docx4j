package org.docx4j.model.datastorage.migration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBElement;

import org.docx4j.customXmlProperties.DatastoreItem;
import org.docx4j.customXmlProperties.SchemaRefs;
import org.docx4j.customXmlProperties.SchemaRefs.SchemaRef;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.opendope.ConditionsPart;
import org.docx4j.openpackaging.parts.opendope.JaxbCustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.opendope.QuestionsPart;
import org.docx4j.openpackaging.parts.opendope.StandardisedAnswersPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.wml.CTDataBinding;
import org.docx4j.wml.CTSdtContentRun;
import org.docx4j.wml.CTSdtText;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.SdtRun;
import org.docx4j.wml.Tag;
import org.docx4j.wml.Text;
import org.docx4j.wml.SdtPr.Alias;
import org.opendope.answers.Answer;
import org.opendope.answers.Answers;
import org.opendope.conditions.Conditions;
import org.opendope.questions.Question;
import org.opendope.questions.Questionnaire;
import org.opendope.questions.Response;
import org.opendope.xpaths.Xpaths;
import org.opendope.xpaths.Xpaths.Xpath.DataBinding;

public class AbstractMigratorUsingAnswersFormat extends AbstractMigrator {
	
	protected QuestionsPart questionsPart;
	protected StandardisedAnswersPart standardisedAnswersPart;
	
	protected Map<String, String> keys = new HashMap<String, String>();
	
	protected void createParts(WordprocessingMLPackage pkgOut) throws InvalidFormatException {
		
		super.createParts(pkgOut);
	
		// Add OpenDoPE parts to target

		// .. Questions
		questionsPart = new QuestionsPart(new PartName("/customXml/item1.xml")); 
		pkgOut.getMainDocumentPart().addTargetPart(questionsPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS);
		addPropertiesPart(pkgOut, questionsPart,"http://opendope.org/questions");
		questionsPart.setJaxbElement(new Questionnaire());
		Questionnaire.Questions questions = new Questionnaire.Questions();
		questionsPart.getJaxbElement().setQuestions(questions);
		
		// .. Standardised Answer format
		standardisedAnswersPart = new StandardisedAnswersPart(new PartName("/customXml/item1.xml")); 
		pkgOut.getMainDocumentPart().addTargetPart(standardisedAnswersPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS);
		storeItemID = addPropertiesPart(pkgOut, standardisedAnswersPart, "http://opendope.org/answers");	
		standardisedAnswersPart.setJaxbElement(new Answers());
		
	}
	
	/**
	 * @param r
	 * @param replacementContent
	 * @param key
	 */
	protected void createContentControl(RPr rPr, List<Object> replacementContent,
			String key) {
		// Has it been encountered already?
		if (!keys.containsKey(key) ) {
			// add the part entries
			addPartEntries(key);
			keys.put(key,  key);
		} 
		
		super.createContentControl(rPr, replacementContent, key, 
				"/oda:answers/oda:answer[@id='" + key +"']", 
				"xmlns:oda='http://opendope.org/answers'");
		
	}
	
	private void addPartEntries(String key) {
		
		// answer
		Answer a = new Answer();
		a.setId(key);
		a.setValue("${" + key + "}");
		standardisedAnswersPart.getJaxbElement().getAnswerOrRepeat().add(a);
		
		// XPath
		Xpaths.Xpath xp = new org.opendope.xpaths.ObjectFactory().createXpathsXpath();
		xp.setId(key);
		xp.setQuestionID(key);
		xp.setPrepopulate(false);
		xp.setRequired(false);
		xp.setType("string");
		DataBinding db = new org.opendope.xpaths.ObjectFactory().createXpathsXpathDataBinding();
		db.setXpath("/oda:answers/oda:answer[@id='" + key +"']");
		db.setPrefixMappings("xmlns:oda='http://opendope.org/answers'");
		db.setStoreItemID(storeItemID);
		xp.setDataBinding(db);
		xPathsPart.getJaxbElement().getXpath().add(xp);
		
		// question
		Question q = new Question();
		q.setId(key);
		q.setText(key + "?");
		Response r = new Response();
		r.setFree( new Response.Free() );
		q.setResponse(r);
		questionsPart.getJaxbElement().getQuestions().getQuestion().add(q);
	}
	
}
