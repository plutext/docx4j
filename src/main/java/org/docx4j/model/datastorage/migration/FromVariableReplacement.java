package org.docx4j.model.datastorage.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.customXmlProperties.DatastoreItem;
import org.docx4j.customXmlProperties.SchemaRefs;
import org.docx4j.customXmlProperties.SchemaRefs.SchemaRef;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;
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
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.SdtPr.Alias;
import org.docx4j.wml.SdtRun;
import org.docx4j.wml.Tag;
import org.docx4j.wml.Text;
import org.opendope.answers.Answer;
import org.opendope.answers.Answers;
import org.opendope.conditions.Conditions;
import org.opendope.questions.Question;
import org.opendope.questions.Questionnaire;
import org.opendope.questions.Response;
import org.opendope.xpaths.Xpaths;
import org.opendope.xpaths.Xpaths.Xpath.DataBinding;

/**
 * This class will help you to migrate
 * from a string replacement approach,
 * to use of content control data bindings.
 * 
 * After migrating, you'll be able to
 * use the OpenDoPE authoring tool to
 * add repeats, conditionals, and other
 * OpenDoPE features, if you need them.
 * 
 * It assumes your magic strings take the
 * form ${--}.  If they don't, you'll
 * need to modify this class, or use Word
 * to search/replace to change them so they
 * take that form.
 * 
 * We'd be happy to accept a patch which
 * specifies a regex the magic string
 * must match.
 * 
 * @author jharrop
 * @since 3.0.0
 */
public class FromVariableReplacement {
	
	private static Logger log = Logger.getLogger(FromVariableReplacement.class);
	
	private Map<String, String> keys = new HashMap<String, String>();
	
	private XPathsPart xPathsPart;
	private QuestionsPart questionsPart;
	private StandardisedAnswersPart standardisedAnswersPart;
	
	private String storeItemID; // of the answers part
	
	public WordprocessingMLPackage migrate(WordprocessingMLPackage pkgIn) throws Exception {
		
		// TODO - test that OpenDoPE parts aren't already present
		// or if they are, that this docx is using our answer format
		// (since only that format is supported here)
		
		// Clone it
		WordprocessingMLPackage pkgOut = (WordprocessingMLPackage)pkgIn.clone();
		
		// Run the cleaner first
		VariablePrepare.prepare(pkgOut);		
		
		// Add OpenDoPE parts to target
		// .. conditions - not that we use this here
		ConditionsPart conditionsPart = new ConditionsPart(new PartName("/customXml/item1.xml")); // name doesn't matter
		pkgOut.getMainDocumentPart().addTargetPart(conditionsPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS); // Word will silently drop the CXPs if they aren't added to the MDP!
		addPropertiesPart(conditionsPart, "http://opendope.org/conditions");
		conditionsPart.setJaxbElement(new Conditions());
		// .. XPaths
		xPathsPart = new XPathsPart(new PartName("/customXml/item1.xml")); 
		pkgOut.getMainDocumentPart().addTargetPart(xPathsPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS);
		addPropertiesPart(xPathsPart, "http://opendope.org/xpaths");
		xPathsPart.setJaxbElement(new Xpaths());
		// .. Questions
		questionsPart = new QuestionsPart(new PartName("/customXml/item1.xml")); 
		pkgOut.getMainDocumentPart().addTargetPart(questionsPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS);
		addPropertiesPart(questionsPart,"http://opendope.org/questions");
		questionsPart.setJaxbElement(new Questionnaire());
		Questionnaire.Questions questions = new Questionnaire.Questions();
		questionsPart.getJaxbElement().setQuestions(questions);
		
		// .. Standardised Answer format
		standardisedAnswersPart = new StandardisedAnswersPart(new PartName("/customXml/item1.xml")); 
		pkgOut.getMainDocumentPart().addTargetPart(standardisedAnswersPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS);
		storeItemID = addPropertiesPart(standardisedAnswersPart, "http://opendope.org/answers");	
		standardisedAnswersPart.setJaxbElement(new Answers());
		
		// Operate at the p level
		PFinder pFinder = new PFinder();
        new TraversalUtil(pkgOut.getMainDocumentPart().getContent(), pFinder);

        for ( P p : pFinder.pList) { 
        	
        	List<Object> replacementContent = new ArrayList<Object>(); 
        	
        	for (Object o : p.getContent() ) {
        		
        		o = XmlUtils.unwrap(o);
        		
        		if ( o instanceof R) {
        			
        			for (Object o2 : ((R)o).getContent() ) {
                		o2 = XmlUtils.unwrap(o2);
        				
        				if ( o2 instanceof Text ) {
        					handle((R)o, ((Text)o2).getValue(), 0, replacementContent);
        				} else {
        					// Just add this bit
        					R rnew = new R();
        					rnew.setRPr( ((R)o).getRPr() ); // point at old rPr, if any
        					rnew.getContent().add(o2);
        					replacementContent.add(rnew);
        				}        				
        			}
        		} else {
        			replacementContent.add(o);
        		}
        	}
        	
        	p.getContent().clear();
        	p.getContent().addAll(replacementContent);
        	
        }
				
		return pkgOut;

	}
	

	private void handle(R r, String s, int offset,
			List<Object> replacementContent) {

		int startKey = s.indexOf("${", offset);
		if (startKey == -1) {
			addTextRun(r, s.substring(offset), replacementContent);
			return;
		} else {
			addTextRun(r, s.substring(offset, startKey), replacementContent);
			int keyEnd = s.indexOf('}', startKey);
			String key = s.substring(startKey + 2, keyEnd);

			// Has it been encountered already?
			if (!keys.containsKey(key) ) {
				// add the part entries
				addPartEntries(key);
				keys.put(key,  key);
			} 
			
			// create the content control
			SdtRun sdtRun = Context.getWmlObjectFactory().createSdtRun();
			replacementContent.add(sdtRun);

			SdtPr sdtPr = Context.getWmlObjectFactory().createSdtPr();
			sdtRun.setSdtPr(sdtPr);
		    /* <w:sdtPr>
		        <w:alias w:val="Title of document"/>
		        <w:tag w:val="od:xpath=cktpD"/>
		        <w:id w:val="289095091"/>
		        <w:dataBinding w:prefixMappings="xmlns:oda='http://opendope.org/answers'" 
		        w:xpath="/oda:answers/oda:answer[@id='Title_of_document_nM']" 
		        w:storeItemID="{183E9AF4-65AB-46DF-8044-944891825721}"/>
		        <w:text w:multiLine="1"/>
		      </w:sdtPr>
		      */
			
			Alias alias = Context.getWmlObjectFactory().createSdtPrAlias();
			alias.setVal(key);
			Tag tag = Context.getWmlObjectFactory().createTag();
			tag.setVal("od:xpath=" + key);
			sdtPr.setTag(tag);
			sdtPr.setId();
			CTDataBinding ctDataBinding = Context.getWmlObjectFactory().createCTDataBinding();
			JAXBElement<CTDataBinding> jaxbDB = 
					Context.getWmlObjectFactory().createSdtPrDataBinding(ctDataBinding);
			sdtPr.setDataBinding(ctDataBinding);
			ctDataBinding.setXpath("/oda:answers/oda:answer[@id='" + key +"']");
			ctDataBinding.setPrefixMappings("xmlns:oda='http://opendope.org/answers'");
			ctDataBinding.setStoreItemID(storeItemID);
						
			CTSdtContentRun sdtContent = Context.getWmlObjectFactory().createCTSdtContentRun();			
			sdtRun.setSdtContent(sdtContent);

			R rnew = new R();
			rnew.setRPr( r.getRPr() ); // point at old rPr, if any
			Text text = Context.getWmlObjectFactory().createText();
			text.setValue(key);
			rnew.getContent().add(text);
			
			sdtContent.getContent().add(rnew);
			
			handle(r, s, keyEnd + 1, replacementContent);
		}
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
		questionsPart.getJaxbElement().getQuestions().getQuestion().add(q);
	}
	
	 private void addTextRun(R r, String val, List<Object> replacementContent) {
		 
			R rnew = new R();
			rnew.setRPr( r.getRPr() ); // point at old rPr, if any
			
			Text text = Context.getWmlObjectFactory().createText();
			text.setValue(val);
			if (val.startsWith(" ")
					|| val.endsWith(" ") ) {
				text.setSpace("preserve");
			}
			
			rnew.getContent().add(text);
			replacementContent.add(rnew);
		 
	 }
	 

	public String addPropertiesPart(JaxbCustomXmlDataStoragePart<?> customXmlDataStoragePart, String ns)
			throws InvalidFormatException {

		CustomXmlDataStoragePropertiesPart part = new CustomXmlDataStoragePropertiesPart();

		org.docx4j.customXmlProperties.ObjectFactory of = new org.docx4j.customXmlProperties.ObjectFactory();

		DatastoreItem dsi = of.createDatastoreItem();
		String newItemId = "{" + UUID.randomUUID().toString().toUpperCase() + "}";
		dsi.setItemID(newItemId);
		
		SchemaRefs srefs = of.createSchemaRefs();
		dsi.setSchemaRefs(srefs);
		
		SchemaRef sref = of.createSchemaRefsSchemaRef();
		sref.setUri(ns);
		
		srefs.getSchemaRef().add(sref);
		
		part.setJaxbElement(dsi);

		customXmlDataStoragePart.addTargetPart(part, AddPartBehaviour.RENAME_IF_NAME_EXISTS);
		
		return newItemId;
	}
	
	  static class PFinder extends CallbackImpl {

          List<P> pList = new ArrayList<P>();  

          @Override
          public List<Object> apply(Object o) {

              if (o instanceof P ) {
            	  pList.add((P)o);
              }                      
              return null;
          }
	  }

	

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/unmarshallFromTemplateExample.docx";

		String outputfilepath = System.getProperty("user.dir")
				+ "/OUT_VariableReplace.docx";

		WordprocessingMLPackage pkgIn = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		FromVariableReplacement migrator = new FromVariableReplacement();
		WordprocessingMLPackage pkgOut = migrator.migrate(pkgIn);
		
		SaveToZipFile saver = new SaveToZipFile(pkgOut);
		saver.save(outputfilepath);
		
	}

}
