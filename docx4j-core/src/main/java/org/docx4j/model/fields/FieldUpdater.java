package org.docx4j.model.fields;

import org.docx4j.TraversalUtil;
import org.docx4j.jaxb.McMode;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.McSplice;
import org.docx4j.jaxb.Context;
import org.docx4j.model.fields.docproperty.DocPropertyResolver;
import org.docx4j.model.fields.docvariable.DocVariableResolver;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.vml.CTTextbox;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Refreshes the values of certain fields in the
 * docx (currently DOCPROPERTY and DOCVARIABLE, and since 17.3.5 the document information fields AUTHOR,
 * COMMENTS, KEYWORDS, LASTSAVEDBY, SUBJECT and TITLE, #277).
 * (For MERGEFIELD, see FieldsMailMerge) 
 * 
 * Do this whether they are simple or complex.
 * 
 * This updates the docx.  If you don't want to do
 * that, apply it to a clone instead.
 * 
 * @author jharrop
 *
 */
public class FieldUpdater {
	
	private static Logger log = LoggerFactory.getLogger(FieldUpdater.class);			
	
	WordprocessingMLPackage wordMLPackage;
	DocPropertyResolver docPropertyResolver;
	DocVariableResolver docVariableResolver;
	
	StringBuilder report = null;
	
	private static final String DOCPROPERTY = "DOCPROPERTY";
	private static final String DOCVARIABLE = "DOCVARIABLE";

	/**
	 * The document information fields Word inserts for a document property (Insert, Field, Document Information),
	 * and the property each shows, as DOCPROPERTY names it (#277, since 17.3.5).
	 */
	private static final Map<String, String> DOC_INFO = new HashMap<String, String>();
	static {
		DOC_INFO.put("AUTHOR", "Author");
		DOC_INFO.put("COMMENTS", "Comments");
		DOC_INFO.put("KEYWORDS", "Keywords");
		DOC_INFO.put("LASTSAVEDBY", "LastSavedBy");
		DOC_INFO.put("SUBJECT", "Subject");
		DOC_INFO.put("TITLE", "Title");
	}

	/**
	 * Whether a document information field has an argument (AUTHOR "Jo"), with which Word sets the property; such a
	 * field is left as it is.
	 */
	private static boolean hasArgument(FldSimpleModel fsm) {
		List<String> parameters = fsm.getFldParameters();
		return parameters != null && !parameters.isEmpty() && !parameters.get(0).startsWith("\\");
	}
	
	public FieldUpdater(WordprocessingMLPackage wordMLPackage) {
		this.wordMLPackage = wordMLPackage;
//		docPropsCustomPart = wordMLPackage.getDocPropsCustomPart();
		docPropertyResolver = new DocPropertyResolver(wordMLPackage);
		docVariableResolver = new DocVariableResolver(wordMLPackage);
	}

	public void update(boolean processHeadersAndFooters) throws Docx4JException {

		report = new StringBuilder();
		
		updatePart(wordMLPackage.getMainDocumentPart() );

		if (processHeadersAndFooters) {
			
			RelationshipsPart rp = wordMLPackage.getMainDocumentPart().getRelationshipsPart();
			for ( Relationship r : rp.getJaxbElement().getRelationship()  ) {
				
				if (r.getType().equals(Namespaces.HEADER)
						|| r.getType().equals(Namespaces.FOOTER)) {
					
					JaxbXmlPart part = (JaxbXmlPart)rp.getPart(r);
					
					report.append("\n" + part.getPartName() + "\n");
										
					log.debug("\n" + part.getPartName() + "\n");
					updatePart(part );
//						performOnInstance(
//								((ContentAccessor)part).getContent() );
					
				}			
			}	
		}	
		
		log.info(report.toString());
	}
	
	public void updatePart(JaxbXmlPart part) throws Docx4JException {

		updateSimple(part);
		updateComplex(part);
	}
	
	public void updateSimple(JaxbXmlPart part) throws Docx4JException {
		
		FldSimpleModel fsm = new FldSimpleModel(); //gets reused
		List contentList = ((ContentAccessor)part).getContent();
		WordprocessingMLPackage wmlPackage = (WordprocessingMLPackage)part.getPackage();
		
		// find fields
		SimpleFieldLocator fl = new SimpleFieldLocator();
		// CR-021: ALL - field results are written in every branch
		new TraversalUtil(contentList, fl, McMode.ALL);
		
		report.append("\n\nSimple Fields in " + part.getPartName() + "\n");
		report.append("============= \n");
		report.append("Found " + fl.simpleFields.size() + " simple fields \n ");
		
		for( CTSimpleField simpleField : fl.simpleFields ) {
			
			if (simpleField.isFldLock()) {
				// a locked field's result is not recalculated (w:fldLock; since 17.3.5)
				report.append("Locked, not updated: " + simpleField.getInstr() + "\n");
				continue;
			}
			
			//System.out.println(XmlUtils.marshaltoString(simpleField, true, true));
//			System.out.println(simpleField.getInstr());
			String fldSimpleName = FormattingSwitchHelper.getFldSimpleName(simpleField.getInstr());
			if (DOCPROPERTY.equals(fldSimpleName)
					|| DOCVARIABLE.equals(fldSimpleName)
					|| DOC_INFO.containsKey(fldSimpleName) ) {
				//only parse those fields that get processed
				try {
					fsm.build(simpleField.getInstr());
				} catch (TransformerException e) {
					e.printStackTrace();
				}
				
				String key;
				if (DOC_INFO.containsKey(fldSimpleName)) {
					if (hasArgument(fsm)) {
						report.append("Not updated (Word sets the property from its argument): " + simpleField.getInstr() + "\n");
						continue;
					}
					key = DOC_INFO.get(fldSimpleName);
				} else if (fsm.getFldParameters().isEmpty()) {
					report.append("No property named: " + simpleField.getInstr() + "\n");
					continue;
				} else {
					key = fsm.getFldParameters().get(0).replaceAll("\"", ""); // as for a complex field (since 17.3.5)
				}
				
				String val = null;
				try {
					if (DOCVARIABLE.equals(fldSimpleName) ) {
						val = docVariableResolver.getValue(key);
					} else {
						val = docPropertyResolver.getValue(key);
					}
				} catch (FieldValueException e) {
					report.append( simpleField.getInstr() + "\n");
					report.append( key + " -> NOT FOUND! \n");	
					continue;
				}
				
				if (val==null) {
					
					report.append( simpleField.getInstr() + "\n");
					report.append( key + " -> NOT FOUND! \n");	
					
				} else {
							//docPropsCustomPart.getProperty(key);
	//				System.out.println(val);
					val = FormattingSwitchHelper.applyFormattingSwitch(wmlPackage, fsm, val);
	//				System.out.println("--> " + val);
					report.append( simpleField.getInstr() + "\n");
					report.append( "--> " + val + "\n");
					
					R r=null;
					if (simpleField.getInstr().toUpperCase().contains("MERGEFORMAT")) {					
						// find the first run and use the formatting of that
						r = getFirstRun(simpleField.getContent());					
					} 
					if (r==null) {
						r = Context.getWmlObjectFactory().createR();
					} else {
						r.getContent().clear();
					}
					simpleField.getContent().clear();	
					simpleField.getContent().add(r);
					Text t = Context.getWmlObjectFactory().createText();
					t.setValue(val);
					// t.setSpace(value) //TODO
					setSimpleFieldConent(r, val);
					
	//				System.out.println(XmlUtils.marshaltoString(simpleField, true, true));
				}
								
			} else {
				
				report.append("Ignoring " + simpleField.getInstr() + "\n");
				
			}
		}
		
	}
	
	protected void setSimpleFieldConent(R r, String val) {
		Text t = Context.getWmlObjectFactory().createText();
		t.setValue(val);
		// t.setSpace(value) //TODO
		r.getContent().add(t);
	}

	private R getFirstRun(List<Object> content) {
		
		for (Object o : content) {
			if (o instanceof R) return (R)o;
		}
		return null;
	}

	public void updateComplex(JaxbXmlPart part) throws Docx4JException {
		
		FldSimpleModel fsm = new FldSimpleModel(); //gets reused
		List contentList = ((ContentAccessor)part).getContent();
		WordprocessingMLPackage wmlPackage = (WordprocessingMLPackage)part.getPackage();
		
		ComplexFieldLocator fl = new ComplexFieldLocator();
		// CR-021: ALL - field results are written in every branch
		new TraversalUtil(contentList, fl, McMode.ALL);
		
		report.append("\n Complex Fields in "+ part.getPartName() + "\n");
		report.append("============== \n");
		
		report.append("Found " + fl.getStarts().size() + " fields \n");
		
		
		// canonicalise and setup fieldRefs 
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		for( P p : fl.getStarts() ) {
			int index;
			// the parent's live list, a Choice's or Fallback's included (CR-051 section 10.3, item 1)
			List<Object> siblings = McSplice.contentOf(p.getParent());
			if (siblings == null) {
				throw new Docx4JException ("Unexpected parent: " + (p.getParent() == null ? "null" : p.getParent().getClass().getName()) );
			}
			index = siblings.indexOf(p);
			P newP = FieldsPreprocessor.canonicalise(p, fieldRefs);
//			log.debug("NewP length: " + newP.getContent().size() );
			siblings.set(index, newP);
		}
		
		// Populate
		for (FieldRef fr : fieldRefs) {
			if (fr.getInstructions().isEmpty()) {				
				// field w/o instructions (empty) - see https://github.com/plutext/docx4j/issues/643
				continue;
			}
			if (fr.isLock()) {
				// a locked field keeps its result, which FieldsPreprocessor kept too (w:fldLock; since 17.3.5)
				report.append("Locked, not updated: " + fr.getFldName() + "\n");
				continue;
			}
			
//			if ("DOCPROPERTY".equals(fr.getFldName())) {
			String fldName = fr.getFldName();
			if (DOCPROPERTY.equals(fldName)
					|| DOCVARIABLE.equals(fldName)
					|| DOC_INFO.containsKey(fldName) ) {
				
				String instr = fr.getInstr();
				try {
					fsm.build(instr);
				} catch (TransformerException e) {
					e.printStackTrace();
				}

				String val = null;
				String key = null;
				try {
					if (DOC_INFO.containsKey(fldName)) {
						if (hasArgument(fsm)) {
							report.append("Not updated (Word sets the property from its argument): " + instr + "\n");
							continue;
						}
						key = DOC_INFO.get(fldName);
						val = docPropertyResolver.getValue(key);
					// Safe checking when fldParameters is 0 (and null just for good measure)
					} else if (fsm.getFldParameters() != null && fsm.getFldParameters().size() > 0) {
						key = fsm.getFldParameters().get(0);
						
						// Remove any " char that may appear in the key name
						if (log.isDebugEnabled() ) {
							log.debug("Key: " + key);
							if (key.contains("\"") ) log.debug("(quote char will be disregarded)");
						}
						key = key.replaceAll("\"", "");
						if (DOCPROPERTY.equals(fldName) ) {
							val = docPropertyResolver.getValue(key);
						} else if (DOCVARIABLE.equals(fldName) ) {
							val = docVariableResolver.getValue(key);
						}
					} else {
						log.warn("FldParameters null or empty");
					}
				} catch (FieldValueException e) {
					report.append( instr + "\n");
					report.append( key + " -> NOT FOUND! \n");	
					continue;
				}
				
				if (val==null) {
					
					report.append( instr + "\n");
					report.append( key + " -> NOT FOUND! \n");
					
				} else {
				
	//				System.out.println(val);
					val = FormattingSwitchHelper.applyFormattingSwitch(wmlPackage, fsm, val);
	//				System.out.println("--> " + val);
					report.append( instr + "\n");
					report.append( "--> " + val + "\n");
	
					if (DOCPROPERTY.equals(fldName)) fr.formatNewResult(); // its old result is kept now (FieldsPreprocessor)
					fr.setResult(val);
					
	//				// If doing an actual mail merge, the begin-separate run is removed, as is the end run
	//				fr.getParent().getContent().remove(fr.getBeginRun());
	//				fr.getParent().getContent().remove(fr.getEndRun());
					
//					System.out.println(XmlUtils.marshaltoString(
//							fr.getParent(), true, true));
				}				
			} else {
				report.append("Ignoring " + fr.getFldName() + "\n");				
			}
		}	
	}
	
}
