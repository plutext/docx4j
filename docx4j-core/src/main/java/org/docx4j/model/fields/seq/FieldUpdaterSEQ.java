package org.docx4j.model.fields.seq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.docx4j.TraversalUtil;
import org.docx4j.jaxb.McMode;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.McSplice;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.McPurpose;
import org.docx4j.jaxb.McSelection;
import org.docx4j.jaxb.McSelection.BranchStep;
import org.docx4j.mce.AlternateContent;
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
	
	/*
	 * mc:AlternateContent (CR-051 section 10.5): its branches render the same content (a text box
	 * written as a wps Choice and a VML Fallback), so Word counts a caption there once. Each branch
	 * is numbered from the counters as they were at the element; after it, the counters are those
	 * the branch Word reads left.
	 */
	private final List<Frame> frames = new ArrayList<Frame>();

	private static final class Frame {
		final AlternateContent ac;
		final Object read;
		final Map<String, Integer> atStart;
		Object branch;
		Map<String, Integer> afterRead;

		Frame(AlternateContent ac, Object branch, Map<String, Integer> counters) {
			this.ac = ac;
			this.branch = branch;
			this.read = McSelection.selectedBranch(McPurpose.TEXT, ac);
			this.atStart = new HashMap<String, Integer>(counters);
		}
	}

	/** Brings the counters to a field in these branches, outermost first. */
	private void enter(List<BranchStep> path) {
		int m = 0;
		while (m < frames.size() && m < path.size() && frames.get(m).ac == path.get(m).getElement()
				&& frames.get(m).branch == path.get(m).getBranch()) m++;
		boolean sameElement = m < frames.size() && m < path.size() && frames.get(m).ac == path.get(m).getElement();
		while (frames.size() > (sameElement ? m + 1 : m)) leave(frames.remove(frames.size() - 1));
		if (sameElement) {
			// the element's next branch
			Frame f = frames.get(m);
			if (f.branch == f.read) f.afterRead = new HashMap<String, Integer>(counters);
			counters = new HashMap<String, Integer>(f.atStart);
			f.branch = path.get(m).getBranch();
			m++;
		}
		for (int i = m; i < path.size(); i++) {
			frames.add(new Frame(path.get(i).getElement(), path.get(i).getBranch(), counters));
		}
	}

	private void leave(Frame f) {
		if (f.branch == f.read) f.afterRead = new HashMap<String, Integer>(counters);
		counters = f.afterRead != null ? f.afterRead : f.atStart;
	}

	private void leaveAll() {
		while (!frames.isEmpty()) leave(frames.remove(frames.size() - 1));
	}

	private static final List<BranchStep> NO_BRANCHES = new ArrayList<BranchStep>();

	private static List<BranchStep> pathOf(Map<Object, List<BranchStep>> paths, Object o) {
		List<BranchStep> path = paths.get(o);
		return path == null ? NO_BRANCHES : path;
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
		// CR-021: ALL - field results are written in every branch
		new TraversalUtil(contentList, fl, McMode.ALL);
		Map<Object, List<BranchStep>> paths = McSelection.branchPaths(contentList, CTSimpleField.class);
		
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
				enter(pathOf(paths, simpleField));
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
		leaveAll();
		
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
		Map<Object, List<BranchStep>> paths = McSelection.branchPaths(contentList, P.class);
		Map<Object, List<BranchStep>> refPaths = new IdentityHashMap<Object, List<BranchStep>>();
		for( P p : fl.getStarts() ) {
			int before = fieldRefs.size();
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
			for (int k = before; k < fieldRefs.size(); k++) refPaths.put(fieldRefs.get(k), pathOf(paths, p));
		}
		
		// Populate
		for (FieldRef fr : fieldRefs) {
			
			String fldName = fr.getFldName();
			if (SEQ.equals(fldName) ) {
				
				String instr = fr.getInstr();
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
					
					enter(pathOf(refPaths, fr));
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
		leaveAll();
	}
	
		
	
}
