/*
 *  Copyright 2025, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.

    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.toc.switches;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.NotImplementedException;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.finders.InstrTextFinder;
import org.docx4j.model.fields.FieldRef;
import org.docx4j.model.fields.FieldsPreprocessor;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.docx4j.model.fields.SimpleFieldLocator;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.toc.TocEntry;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.P;
import org.docx4j.wml.Style;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * \c field-argument
 * 
 * Includes figures, tables, charts, and other items that are numbered 
 * by a SEQ field (§2.16.5.63). 
 * 
 * The sequence identifier designated by text in this switch's field-argument, 
 * which corresponds to the caption label, shall match the identifier 
 * in the corresponding SEQ field.
 * 
 * @since 11.5.4
 */
public class CSwitch extends SelectorSwitch {
	
	// eg  TOC \h \z \c "Figure"
	
	private static Logger log = LoggerFactory.getLogger(CSwitch.class);					
	

    public static final String ID = "\\c";
    
    private static final int PRIORITY = 7;
	@Override
	public int getPriority() {
		return PRIORITY;
	}
    
    /**
     * identifier is the name assigned to the series of items that are to be numbered. 
     * [Example: identifier might be Equation, Figure, Table, or Thing, as the user deems appropriate for a caption. end example] 
     * 
     * identifier shall start with a Latin letter and shall consist of no more than 40 Latin letters, Arabic digits, and underscores. 
     * 
     * (See the TOC field (§2.16.5.75) switches \c and \s for uses of identifier.)
     * 
     * Since a C switch can only include one series (ie 1 identifier), there is
     * a 1:1 relationship in this class.
     */
    private String itemIdentifier;
    
    public String getItemIdentifier() {
		return itemIdentifier;
	}

    int counter = 1;

	@Override
    public String parseFieldArgument(String fieldArgument){
        this.fieldArgument = fieldArgument;
        itemIdentifier = prepareArgument(fieldArgument);
        return EMPTY;
    }
    
    
    @Override
    public boolean hasFieldArgument() {
        return true;
    }

    @Override
    public void process(Style s, SwitchProcessorInterface sp) {

    	throw new NotImplementedException("For ToC C swtich, don't use this method.");
    	
    }

    public void process(P p, SwitchProcessorInterface sp) {
    	
		// Does the paragraph contain  <w:fldSimple w:instr=" SEQ Figure \* ARABIC ">
		// or equivalent complex field:  <w:instrText xml:space="preserve"> SEQ Figure \* ARABIC </w:instrText>
    	// JAXBElement<Text>(_RInstrText_QNAME, Text.class, R.class, value)
    	
    	// Simple fields
		SimpleFieldLocator fl = new SimpleFieldLocator();
		new TraversalUtil(p, fl);
		
		for( CTSimpleField simpleField : fl.simpleFields ) {
			
			//System.out.println(XmlUtils.marshaltoString(simpleField, true, true));
//			System.out.println(simpleField.getInstr());
			String fldSimpleName = FormattingSwitchHelper.getFldSimpleName(simpleField.getInstr());
			if ("SEQ".equals(fldSimpleName)) {
				FldSimpleModel fsm = new FldSimpleModel(); //gets reused
				try {
					fsm.build(simpleField.getInstr());
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String key = fsm.getFldParameters().get(0);  
				if (log.isDebugEnabled()) {
					log.debug(simpleField.getInstr() + " ----> " + key);
				}
    			
    			// if key matches \c item identifier
    			if (key.equals(this.getItemIdentifier())) {
    				
    		        TocEntry te = sp.getEntry(); // creates it       	
    		        te.setEntryLevel(1); // ??
    		        detected = true;
    		    	sp.setSelected(true);  // important 
    		    	return;
    			}
			}
		
		}
    	
    	// Complex fields
		InstrTextFinder complexFinder = new InstrTextFinder(); 
		new TraversalUtil(p, complexFinder);
		for (Object o : complexFinder.results) {
			Text t = (Text)XmlUtils.unwrap(o);
			String key = t.getValue();
			// if key matches \c item identifier
			if (key.contains("SEQ") && key.contains(this.getItemIdentifier())) {
				
		        TocEntry te = sp.getEntry(); // creates it       	
		        te.setEntryLevel(1); // ??
		        detected = true;
		    	sp.setSelected(true);  // important 
		    	return;
			}			
		}
		log.debug("P doesn't contain SEQ");
    	
    }
    
    boolean detected = false;
    
	public boolean isDetected() {
		return detected;
	}

    /**
     * We detected a SEQ in the P, so before we put the P in the TOC,
     * lets resolve the SEQ to a number.
     * @param p
     */
    public P postprocess(P p, WordprocessingMLPackage wordMLPackage) {
    	
    	// Step 1: create a clone of the P
    	P clonedP = (P)XmlUtils.deepCopy(p);
    	
    	// Have to do this for both simple and complex fields
    	List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
    	clonedP = FieldsPreprocessor.canonicalise(clonedP, fieldRefs);
    	
    	/* canonicalised result:
    	 * 
			<w:p>
			    <w:pPr>
			        <w:pStyle w:val="Caption"/>
			    </w:pPr>
			    <w:bookmarkStart w:name="_Toc198891146" w:id="4"/>
			    <w:r>
			        <w:t xml:space="preserve">Figure </w:t>
			    </w:r>
			    <w:r>
			        <w:fldChar w:fldCharType="begin"/>
			        <w:instrText xml:space="preserve"> SEQ Figure \* ARABIC </w:instrText>
			        <w:fldChar w:fldCharType="separate"/>
			    </w:r>
			    <w:r>
			        <w:rPr>
			            <w:noProof/>
			        </w:rPr>
			        <w:t>1</w:t>
			    </w:r>
			    <w:r>
			        <w:fldChar w:fldCharType="end"/>
			    </w:r>
			    <w:r>
			        <w:t xml:space="preserve"> Turbines</w:t>
			    </w:r>
			    <w:bookmarkEnd w:id="4"/>
			</w:p>
    	 */
    	
//    	System.out.println(XmlUtils.marshaltoString(clonedP));
    	
    	for (FieldRef fr : fieldRefs) {

    		if (log.isDebugEnabled()) {
    			log.debug("'" + fr.getFldName() + "'");
    		}
			if ( "SEQ".equals(fr.getFldName()) ) {
				
				String instr = extractInstr(fr.getInstructions() );
				if (instr==null) {
					log.warn("No instructions found in this field");
					// TODO for various cases
					continue;
				}  
				System.out.println(instr);
				// SEQ Figure \* ARABIC 
				
				String identifier = getIdentifierFromInstr(instr);
				if (!this.getItemIdentifier().equals(identifier)) {
					
	    			log.debug("'" + identifier + "' does not match sought '" + this.getItemIdentifier() + "'");
										
				} else {
					
					// Now format the result
					FldSimpleModel fsm = new FldSimpleModel();
					String result = counter + "";
					
					try {
						fsm.build(instr);
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
						// and then only for values ARABIC, Roman and roman 
												
						try {
							result = FormattingSwitchHelper.applyFormattingSwitch(wordMLPackage, fsm, result);
						} catch (Docx4JException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						
					} catch (TransformerException e) {
						log.warn("Can't format the field", e);
					}
					
					fr.setResult(result);
					
					fr.getParent().getContent().remove(fr.getBeginRun());
					fr.getParent().getContent().remove(fr.getEndRun());
					
				} 
			} 
    	}
			
    	
    	// System.out.println(XmlUtils.marshaltoString(clonedP));
    	/* result:
			<w:p>
			    <w:pPr>
			        <w:pStyle w:val="Caption"/>
			    </w:pPr>
			    <w:bookmarkStart w:name="_Toc198891146" w:id="4"/>
			    <w:r>
			        <w:t xml:space="preserve">Figure </w:t>
			    </w:r>
			    <w:r>
			        <w:rPr>
			            <w:noProof/>
			        </w:rPr>
			        <w:t>4</w:t>
			    </w:r>
			    <w:r>
			        <w:t xml:space="preserve"> Turbines</w:t>
			    </w:r>
			    <w:bookmarkEnd w:id="4"/>
			</w:p>
    	 */
    	
    	counter++;
    	// reset
    	detected = false;
    	return clonedP;
    }
	

	
	protected static String extractInstr(List<Object> instructions) {
		// For SEQ, expect the list to contain a simple string
		
		if (instructions.size()!=1) {
			log.warn("SEQ field contained complex instruction; attempting to process");
			/* eg
			 * 
			 *    <w:r>
			        <w:instrText xml:space="preserve"> SEQ  Fig</w:instrText>
			      </w:r>
			      <w:r>
			        <w:instrText xml:space="preserve">ure  \* ARABIC </w:instrText>
			      </w:r>
			      
				for (Object i : instructions) {
					i = XmlUtils.unwrap(i);
					if (i instanceof Text) {
						log.error( ((Text)i).getValue());
					} else {
						log.error(XmlUtils.marshaltoString(i, true, true) );
					}
				}
			 */
			StringBuffer sb = new StringBuffer(); 
			for (Object i : instructions) {
				i = XmlUtils.unwrap(i);
				if (i instanceof Text) {
					String t = ((Text)i).getValue();
					log.debug( t);
					sb.append(t);
				} else {
					log.warn("Failed: non Text object encountered.");
					log.debug(XmlUtils.marshaltoString(i, true, true) );
					return null;					
				}
			}
			return sb.toString();
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
	
	/**
	 * Get the identifier from, for example
	 * SEQ Figure \* ARABIC
	 */
	protected static String getIdentifierFromInstr(String instr) {
		// Copied from MailMerger.getDatafieldNameFromInstr

//		System.out.println("BEFORE " +XmlUtils.marshaltoString(
//			fr.getParent(), true, true));
		
//		log.debug(instr);
		String tmp = instr.substring( instr.indexOf("SEQ") + 3);
		tmp = tmp.trim();
		String datafieldName  = null;
		// A data field name will be quoted if it contains spaces
		if (tmp.startsWith("\"")) {
			if (tmp.indexOf("\"",1)>-1) {
				datafieldName = tmp.substring(1, tmp.indexOf("\"",1));				
			} else {
				log.warn("Quote mismatch in " + instr);
				// hope for the best
				datafieldName = tmp.indexOf(" ") >-1 ? tmp.substring(1, tmp.indexOf(" ")) : tmp.substring(1) ;				
			}
		} else {
			datafieldName = tmp.indexOf(" ") >-1 ? tmp.substring(0, tmp.indexOf(" ")) : tmp ;
		}
		log.info("Key: '" + datafieldName + "'");

		return datafieldName;
		
	}	

}
