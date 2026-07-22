package org.docx4j.model.fields.seq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.fields.ComplexFieldLocator;
import org.docx4j.model.fields.FieldRef;
import org.docx4j.model.fields.FieldsPreprocessor;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.docx4j.model.fields.SimpleFieldLocator;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.vml.CTTextbox;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Renumbers SEQ fields, following the approach 
 * in FieldUpdater (for DOCPROPERTY and DOCVARIABLE fields).
 * 
 * This implementation updates all SEQ fields,
 * whatever their identifier.  For example, all
 * "Figure", "Table".
 * 
 * Do this whether they are simple or complex.
 * 
 * This updates the docx.  If you don't want to do
 * that, apply it to a clone instead.
 * 
 * @author jharrop
 *
 */
public class FieldUpdaterSEQ {
	
	private static Logger log = LoggerFactory.getLogger(FieldUpdaterSEQ.class);			
	
	WordprocessingMLPackage wordMLPackage;
	
	/**
	 * A counter for each identifier;
	 * identifier is the name assigned to the series of items that are to be numbered. 
     * [Example: identifier might be Equation, Figure, Table, or Thing, as the user deems appropriate for a caption. end example] 
	 */
	Map<String, Integer> counters = new HashMap<String, Integer>();
	
	private int getValue(String identifier) {
		
		Integer i = counters.get(identifier);
		if (i==null) {
			counters.put(identifier, Integer.valueOf(1));
			return 1;
		} else {
			counters.put(identifier, Integer.valueOf(i+1));
			return i+1;
		}
	}
	
	StringBuilder report = null;	
	
	private static final String SEQ = "SEQ";	

	public FieldUpdaterSEQ(WordprocessingMLPackage wordMLPackage) {
		this.wordMLPackage = wordMLPackage;
	}

	public void update() throws Docx4JException {
		
		/*
			Per http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/SEQ.html
			Switches: Zero or one of the numeric-formatting-switches, or zero or more of the following field-specific-switches. If no numeric-formatting-switch is present, \* Arabic is used.
			
			\c Repeats the closest preceding sequence number. [Note: This is useful for inserting chapter numbers in headers or footers. end note]
			
			\h Hides the field result unless a general-formatting-switch is also present.[Note: This switch can be used to refer to a SEQ field in a cross-reference without printing the number. end note]
			
			\n Inserts the next sequence number for the specified item. This is the default.
			
			\r field-argument  Resets the sequence number to the integer number specified by text in this switch's field-argument.
			
			\s field-argument  Resets the sequence number to the built-in (integer) heading level specified by text in this switch's field-argument.
			
		*/	
		// At present, this implementation only supports the general formatting switch \* 
		// (see further http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/file_10.html )
		// and then only for values ARABIC and ROMAN 
		
		report = new StringBuilder();
		
		updateSimple(wordMLPackage.getMainDocumentPart());
		updateComplex(wordMLPackage.getMainDocumentPart());
		
		log.info(report.toString());
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
			
			String fldSimpleName = FormattingSwitchHelper.getFldSimpleName(simpleField.getInstr());
			if (SEQ.equals(fldSimpleName)) {
				//only parse those fields that get processed
				try {
					fsm.build(simpleField.getInstr());
				} catch (TransformerException e) {
					e.printStackTrace();
				}
				
				String key = fsm.getFldParameters().get(0);
				String val = getValue(key) +"";
				
				// Now format it
				val = FormattingSwitchHelper.applyFormattingSwitch(wmlPackage, fsm, val);
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
			} else if (p.getParent() instanceof CTTextbox) {
				index = ((CTTextbox) p.getParent()).getTxbxContent().getContent().indexOf(p);
				P newP = FieldsPreprocessor.canonicalise(p, fieldRefs);
				((CTTextbox) p.getParent()).getTxbxContent().getContent().set(index, newP);
			} else {
				throw new Docx4JException ("Unexpected parent: " + p.getParent().getClass().getName() );
			}
		}
		
		// Populate
		for (FieldRef fr : fieldRefs) {
			
			String fldName = fr.getFldName();
			if (SEQ.equals(fldName) ) {
				
				String instr = extractInstr(fr.getInstructions());
				try {
					fsm.build(instr);
				} catch (TransformerException e) {
					e.printStackTrace();
				}

				String val = null;
				String key = null;
				
				// Safe checking when fldParameters is 0 (and null just for good measure)
				if (fsm.getFldParameters() != null && fsm.getFldParameters().size() > 0) {
					key = fsm.getFldParameters().get(0);
					
					// Remove any " char that may appear in the key name
					if (log.isDebugEnabled() ) {
						log.debug("Key: " + key);
						if (key.contains("\"") ) log.debug("(quote char will be disregarded)");
					}
					key = key.replaceAll("\"", "");
					
					val = getValue(key) +"";
					
				} else {
					log.warn("FldParameters null or empty");
				}
				
				if (val==null) {
					
					report.append( instr + "\n");
					report.append( key + " -> NOT FOUND! \n");
					
				} 
				else {
				
					val = FormattingSwitchHelper.applyFormattingSwitch(wmlPackage, fsm, val);

					report.append( instr + "\n");
					report.append( "--> " + val + "\n");
	
					fr.setResult(val);
					
				}	
				
			} else {
				report.append("Ignoring " + fr.getFldName() + "\n");				
			}
		}	
	}
	
	private String extractInstr(List<Object> instructions) {
		return FieldsPreprocessor.extractInstr(instructions);
	}
		
	
}
