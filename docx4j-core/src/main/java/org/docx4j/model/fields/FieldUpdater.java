package org.docx4j.model.fields;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.fields.docproperty.DocPropertyResolver;
import org.docx4j.model.fields.docvariable.DocVariableResolver;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.List;

/**
 * Refreshes the values of certain fields in the
 * docx (currently DOCPROPERTY and DOCVARIABLE only).
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
		new TraversalUtil(contentList, fl);
		
		report.append("\n\nSimple Fields in " + part.getPartName() + "\n");
		report.append("============= \n");
		report.append("Found " + fl.simpleFields.size() + " simple fields \n ");
		
		for( CTSimpleField simpleField : fl.simpleFields ) {
			
			//System.out.println(XmlUtils.marshaltoString(simpleField, true, true));
//			System.out.println(simpleField.getInstr());
			String fldSimpleName = FormattingSwitchHelper.getFldSimpleName(simpleField.getInstr());
			if (DOCPROPERTY.equals(fldSimpleName)
					|| DOCVARIABLE.equals(fldSimpleName) ) {
				//only parse those fields that get processed
				try {
					fsm.build(simpleField.getInstr());
				} catch (TransformerException e) {
					e.printStackTrace();
				}
				
				String key = fsm.getFldParameters().get(0);
				
				String val = null;
				try {
					if (DOCPROPERTY.equals(fldSimpleName) ) {
						val = docPropertyResolver.getValue(key);
					} else if (DOCVARIABLE.equals(fldSimpleName) ) {
						val = docVariableResolver.getValue(key);
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
					r.getContent().add(t);
					
	//				System.out.println(XmlUtils.marshaltoString(simpleField, true, true));
				}
								
			} else {
				
				report.append("Ignoring " + simpleField.getInstr() + "\n");
				
			}
		}
		
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
		new TraversalUtil(contentList, fl);
		
		report.append("\n Complex Fields in "+ part.getPartName() + "\n");
		report.append("============== \n");
		
		report.append("Found " + fl.getStarts().size() + " fields \n");
		
		
		// canonicalise and setup fieldRefs 
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		for( P p : fl.getStarts() ) {
			int index;
			if (p.getParent() instanceof ContentAccessor) {
				index = ((ContentAccessor)p.getParent()).getContent().indexOf(p);
				P newP = FieldsPreprocessor.canonicalise(p, fieldRefs);
//				log.debug("NewP length: " + newP.getContent().size() );
				((ContentAccessor)p.getParent()).getContent().set(index, newP);
			} else if (p.getParent() instanceof java.util.List) {
				// This does happen!
				index = ((java.util.List)p.getParent()).indexOf(p);
				P newP = FieldsPreprocessor.canonicalise(p, fieldRefs);
//				log.debug("NewP length: " + newP.getContent().size() );
				((java.util.List)p.getParent()).set(index, newP);				
			} else {
				throw new Docx4JException ("Unexpected parent: " + p.getParent().getClass().getName() );
			}
		}
		
		// Populate
		for (FieldRef fr : fieldRefs) {
			
//			if ("DOCPROPERTY".equals(fr.getFldName())) {
			String fldName = fr.getFldName();
			if (DOCPROPERTY.equals(fldName)
					|| DOCVARIABLE.equals(fldName) ) {
				
				String instr = extractInstr(fr.getInstructions());
				try {
					fsm.build(instr);
				} catch (TransformerException e) {
					e.printStackTrace();
				}

				String val = null;
				String key = null;
				try {
					// Safe checking when fldParameters is 0 (and null just for good measure)
					if (fsm.getFldParameters() != null && fsm.getFldParameters().size() > 0) {
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
	
	private String extractInstr(List<Object> instructions) {
		// For DOCPROPERTY, expect the list to contain a simple string
		
		if (instructions.size()!=1) {
			log.error("TODO DOCPROPERTY field contained complex instruction");
			return null;
		}
		
		Object o = XmlUtils.unwrap(instructions.get(0));
		if (o instanceof Text) {
			return ((Text)o).getValue();
		} else {
            if(log.isErrorEnabled()) {
                log.error("TODO: extract field name from " + o.getClass().getName());
                log.error(XmlUtils.marshaltoString(instructions.get(0), true, true));
            }
			return null;
		}
	}
	
}
