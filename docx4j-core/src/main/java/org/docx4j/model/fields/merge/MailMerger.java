package org.docx4j.model.fields.merge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.JAXBElement;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.Docx4jProperties;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.fields.AbstractMerger;
import org.docx4j.model.fields.ComplexFieldLocator;
import org.docx4j.model.fields.FieldRef;
import org.docx4j.model.fields.FieldsPreprocessor;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.model.structure.PageSizePaper;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.relationships.Relationship;
import org.docx4j.vml.CTTextbox;
import org.docx4j.wml.Body;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTFFData;
import org.docx4j.wml.CTFFName;
import org.docx4j.wml.CTFFTextInput;
import org.docx4j.wml.CTFFTextType;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.CTPageNumber;
import org.docx4j.wml.CTRel;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STFFTextType;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.jvnet.jaxb.lang.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Perform a mail merge.
 * 
 * Instance values are merged into a docx containing
 * MERGEFIELD to produce output docx made up of
 * a copy of the input docx for each collection of 
 * input values.
 * 
 * The output can be a single docx, or multiple docx.
 * 
 * If you choose single docx, there are two ways to
 * do this:
 * 
 * One is using MergeDocx, which will ensure each 
 * constituent "document" doesn't affect the neighbouring
 * ones (eg numbering will restart).
 * 
 * The other is the "poor man's" approach, which 
 * puts them together, and just hopes for the best.
 * Images and hyperlinks should be ok. But numbering 
 * will continue, as will footnotes/endnotes. 
 * 
 * From 3.0, there is some support for formatting switches
 * (date/time, numeric, and general), and basic 
 * support for MERGEFORMAT.
 *  
 * LIMITATIONS:
 * - no support for text before (\b) and text after (\f)
 *   switches
 * - no support for \m and \v switches
 * - no support for multiple MERGEFIELD in a single
 *   instruction (eg MERGEFIELD CoutesyTitle \f " " MERGEFIELD FirstName \f " " MERGEFIELD LastName ) 
 * 
 * @author jharrop
 *
 */
public class MailMerger extends AbstractMerger {

	static Logger log = LoggerFactory.getLogger(MailMerger.class);		

	public MailMerger(WordprocessingMLPackage input) {
		super(input);
	}
	
	
	/**
	 * A "poor man's" approach, which generates the mail merge  
	 * results as a single docx, and just hopes for the best.
	 * Images and hyperlinks should be ok. But numbering 
	 * will continue, as will footnotes/endnotes. 
	 * @param input
	 * @param data
	 * @return
	 * @throws Docx4JException
	 */
	@Deprecated
	public static WordprocessingMLPackage getConsolidatedResultCrude(WordprocessingMLPackage input, 
			List<Map<DataFieldName, String>> data) throws Docx4JException {
		
		// In 11.5.2, this static method is retained for backwards compatibility
		MailMerger mailMerger = new MailMerger(input); 
		return mailMerger.getConsolidatedResultCrude(data, false);
	}
	
	/**
	 * A "poor man's" approach, which generates the mail merge  
	 * results as a single docx, and just hopes for the best.
	 * Images and hyperlinks should be ok. But numbering 
	 * will continue, as will footnotes/endnotes. 
	 * [Advert:] If this isn't working for you, the commercial Enterprise Edition of docx4j
	 * (MergeDocx component) will solve your problems. 
	 * @param input
	 * @param data
	 * @param processHeadersAndFooters process headers and footers in FIRST section only.
	 * If you have multiple sections in your input docx, performMerge is a better approach
	 * @return
	 * @throws Docx4JException
	 * @ since 2.8.1
	 */
	@Deprecated
	public static WordprocessingMLPackage getConsolidatedResultCrude(WordprocessingMLPackage input, 
			List<Map<DataFieldName, String>> data, boolean processHeadersAndFooters) throws Docx4JException {

		// In 11.5.2, this static method is retained for backwards compatibility
		MailMerger mailMerger = new MailMerger(input); 
		return mailMerger.getConsolidatedResultCrude(data, processHeadersAndFooters);
		
	}
	
	/**
	 * Perform merge on a single instance.
	 * 
	 * This is the best approach, if your input has headers/footers in
	 * multiple sections.
	 * 
	 * If you are using MergeDocx, you can use that to join the 
	 * instances into a single docx.
	 * 
	 * WARNING: The input docx will be modified, so input a copy if that is a problem.
	 * This is left to the user, since that can potentially be more efficient, than
	 * doing it here.
	 * 
	 * @param input
	 * @param data
	 * @param processHeadersAndFooters
	 * @return
	 * @throws Docx4JException
	 */
	@Deprecated
	public static void performMerge(WordprocessingMLPackage input, 
			Map<DataFieldName, String> data, boolean processHeadersAndFooters ) throws Docx4JException {

		// In 11.5.2, this static method is retained for backwards compatibility
		MailMerger mailMerger = new MailMerger(input); 
		mailMerger.performMerge(data, processHeadersAndFooters);
	}
	
	protected List<Object> performOnInstance( 
			List<Object> contentList, 
			Map<DataFieldName, String> datamap,
			FormTextFieldNames formTextFieldNames) throws Docx4JException {
		
		// We need our fieldRefs point to the correct objects;
		// the easiest way to do this is to create them after cloning!
		
		// to facilitate cloning, wrap the list in a body
		Body shell = Context.getWmlObjectFactory().createBody();
		shell.getContent().addAll(contentList);
		Body shellClone = (Body)XmlUtils.deepCopy(shell);
		
		// find fields
		ComplexFieldLocator fl = new ComplexFieldLocator();
		new TraversalUtil(shellClone, fl);
		log.info("Found " + fl.getStarts().size() + " fields ");
		
		
		// canonicalise and setup fieldRefs 
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		canonicaliseStarts(fl, fieldRefs);
		
		// Populate
		for (FieldRef fr : fieldRefs) {
			
			if ( "MERGEFIELD".equals(fr.getFldName()) ) {
				
				String instr = extractInstr(fr.getInstructions() );
				if (instr==null) {
					log.warn("No instructions found in this field");
					// TODO for various cases
					continue;
				}
				String lang = extractLang(fr.getResultsSlot());
				String datafieldName = getDatafieldNameFromInstr(instr);
				String val = datamap.get( new DataFieldName(datafieldName));
				String gFormat = null; // required only for FORMTEXT conversion
				
				if (StringUtils.isBlank(val)) {
					log.warn("Couldn't find value for key: '" + datafieldName + "'");
                    if (fieldFate.equals(OutputField.REMOVED)) {
                        // Remove the mergefield from the document
                        removeSimpleField(fr);

                        // Concatenate all content still present in the parent
                        String text = getTextInsideContent(fr.getParent());

                        // If the parent still contains data, don't delete it
                        if (StringUtils.isBlank(text)) {
                            recursiveRemove(shellClone, fr.getParent());
                        }
                    }				
				} else {
					
					// Now format the result
					FldSimpleModel fsm = new FldSimpleModel();
					try {
						fsm.build(instr);
						val = FormattingSwitchHelper.applyFormattingSwitch(input, fsm, val, lang);
						
						gFormat = FormattingSwitchHelper.findFirstSwitchValue("\\*", fsm.getFldParameters(), true);
						// Solely for potential use in OutputField.AS_FORMTEXT_REGULAR
						// We are in fact applying all formatting switches above.
						
					} catch (TransformerException e) {
						log.warn("Can't format the field", e);
					}
					
					fr.setResult(val);
				}
				
				if (fieldFate.equals(OutputField.AS_FORMTEXT_REGULAR)) {
					
					log.debug(gFormat);
					// TODO if we're going to use gFormat, setup FSM irrespective of whether we can find key 
					
					
					// TODO: other format instructions
//					if (gFormat!=null) {
//						if (gFormat.equals("Upper")) {
//							gFormat = "UPPERCASE";
//						} else if (gFormat.equals("Lower")) {
//							gFormat = "LOWERCASE";
//						} 
//					}
					
					// replace instrText
					// eg MERGEFIELD  CLIENT.ORGANIZATIONSTATE \* Upper  \* MERGEFORMAT
					// to FORMTEXT
					// Do this first, so we can abort without affecting output
					List<Object> instructions = fr.getInstructions();
					if (instructions.size()!=1) {
						log.error("TODO MERGEFIELD field contained complex instruction");
						continue;
					}
					Object o = XmlUtils.unwrap(instructions.get(0));
					if (o instanceof Text) {
						((Text)o).setValue("FORMTEXT");
					} else {
                        if(log.isErrorEnabled()) {
                            log.error("TODO: set FORMTEXT in" + o.getClass().getName());
                            log.error(XmlUtils.marshaltoString(instructions.get(0), true, true));
                        }
						continue;
					}
					
					String fieldName = formTextFieldNames.generateName(datafieldName);
					log.debug("Field name normalisation: " + datafieldName + " -> " + fieldName);
					setFormFieldProperties(fr, fieldName, null);
					
					// remove <w:highlight w:val="lightGray"/>, if present
					// (corresponds in Word to clicking Legacy Forms > Form Field Shading)
					// so that the result is not printed in grey
					R resultR = fr.getResultsSlot();
					if (resultR.getRPr()!=null
							&& resultR.getRPr().getHighlight()!=null) {
						resultR.getRPr().setHighlight(null);
					}
					
				} else if (!fieldFate.equals(OutputField.KEEP_MERGEFIELD)) {
					// If doing an actual mail merge, the begin-separate run is removed, as is the end run				
					fr.getParent().getContent().remove(fr.getBeginRun());
					fr.getParent().getContent().remove(fr.getEndRun());
				}
				
//				System.out.println("AFTER " +XmlUtils.marshaltoString(
//						fr.getParent(), true, true));
				
			}
		}
		
		return shellClone.getContent();

	}

	/**
	 * Extract language information from run parameters to be able to 
	 * format month, day, week, etc. in its abbreviated form according to the 
	 * language specified by the lang element on the run containing the field instructions.
	 * Also it will be used to use language specific <code>DecimalFormatSymbols</code> for number formating
	 * @param R Run
	 * @returns string language like "fr-CA" abbreviation or null
	 */
	private static String extractLang(R resultsSlot) {
		RPr rPr = resultsSlot.getRPr();
		if(rPr != null){
			CTLanguage lang = rPr.getLang();
			if(lang != null){
				return lang.getVal();
			}
		}
		return null;
	}

	
	
	/**
	 * Get the datafield name from, for example
	 * <w:instrText xml:space="preserve"> MERGEFIELD  Kundenstrasse \* MERGEFORMAT </w:instrText>
	 * or <w:instrText xml:space="preserve"> MERGEFIELD  Kundenstrasse</w:instrText>
	 */
	protected static String getDatafieldNameFromInstr(String instr) {
		

//		System.out.println("BEFORE " +XmlUtils.marshaltoString(
//			fr.getParent(), true, true));
		
//		log.debug(instr);
		String tmp = instr.substring( instr.indexOf("MERGEFIELD") + 10);
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
	
	protected static String extractInstr(List<Object> instructions) {
		// For MERGEFIELD, expect the list to contain a simple string
		
		if (instructions.size()!=1) {
			log.warn("MERGEFIELD field contained complex instruction; attempting to process");
			/* eg
			 * 
			 *    <w:r>
			        <w:instrText xml:space="preserve"> MERGEFIELD  lasauv</w:instrText>
			      </w:r>
			      <w:r>
			        <w:instrText xml:space="preserve">egarde  \* MERGEFORMAT </w:instrText>
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
     * What to do with the MERGEFIELD in the output docx.
     * 
     * Default is REMOVED.
     * 
     * KEEP_MERGEFIELD will allow you to perform
	 * another merge on the output document.
	 * 
	 * The AS_FORMTEXT options convert the MERGEFIELD to a FORMTEXT field.
	 * This is convenient if you want users to
	 * be able to edit the field, where editing is restricted
	 * to forms. 
	 * 
     * @param fieldFate
     */
	@Deprecated	
    public static void setMERGEFIELDInOutput(OutputField fieldFate) {
    	MailMerger.fieldFate = fieldFate;
    }

	public enum OutputField {
		  // This class left here for backwards compatibility

		    DEFAULT,
		    REMOVED,
		    KEEP_MERGEFIELD,
		    AS_FORMTEXT_REGULAR;
//		    AS_FORMTEXT_TYPED,
//		    AS_FORMTEXT_TYPED_FORMATTED;
	  } 	
	
	protected static void setFormFieldProperties(FieldRef fr, String ffName, String ffTextInputFormat) {
		
		ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();
		
	        // Create object for ffData
	        CTFFData ffdata = wmlObjectFactory.createCTFFData(); 
	        fr.setFormFieldProperties(ffdata);
	        
	            // Create object for name (wrapped in JAXBElement) 
	            CTFFName ffname = wmlObjectFactory.createCTFFName(); 
	            JAXBElement<org.docx4j.wml.CTFFName> ffnameWrapped = wmlObjectFactory.createCTFFDataName(ffname); 
	            ffdata.getNameOrEnabledOrCalcOnExit().add( ffnameWrapped); 
	                ffname.setVal(ffName); 
	                
	            // Create object for enabled (wrapped in JAXBElement) 
	            BooleanDefaultTrue booleandefaulttrue = wmlObjectFactory.createBooleanDefaultTrue(); 
	            JAXBElement<org.docx4j.wml.BooleanDefaultTrue> booleandefaulttrueWrapped = wmlObjectFactory.createCTFFDataEnabled(booleandefaulttrue); 
	            ffdata.getNameOrEnabledOrCalcOnExit().add( booleandefaulttrueWrapped); 
	            
	            // Create object for calcOnExit (wrapped in JAXBElement) 
	            BooleanDefaultTrue booleandefaulttrue2 = wmlObjectFactory.createBooleanDefaultTrue(); 
	            JAXBElement<org.docx4j.wml.BooleanDefaultTrue> booleandefaulttrueWrapped2 = wmlObjectFactory.createCTFFDataCalcOnExit(booleandefaulttrue2); 
	            ffdata.getNameOrEnabledOrCalcOnExit().add( booleandefaulttrueWrapped2); 
	            
	            // Create object for textInput (wrapped in JAXBElement) 
	            CTFFTextInput fftextinput = wmlObjectFactory.createCTFFTextInput(); 
	            JAXBElement<org.docx4j.wml.CTFFTextInput> fftextinputWrapped = wmlObjectFactory.createCTFFDataTextInput(fftextinput); 
	            ffdata.getNameOrEnabledOrCalcOnExit().add( fftextinputWrapped); 
	            
	            //Set type to regular
	            CTFFTextType ffTextType = wmlObjectFactory.createCTFFTextType();
	            ffTextType.setVal(STFFTextType.REGULAR);
	            fftextinput.setType(ffTextType);
	            
	            if (ffTextInputFormat!=null) {
	            
	                // Create object for format
	                CTFFTextInput.Format fftextinputformat = wmlObjectFactory.createCTFFTextInputFormat(); 
	                fftextinput.setFormat(fftextinputformat); 
	                    fftextinputformat.setVal( ffTextInputFormat);  // eg "UPPERCASE"
	            }
	}
	
	
//	public static boolean isMergeField(String type) {
//	
//		if (type.contains("MERGEFIELD")) {
//			return true;
//		} else {
//			return false;
//		}
//	}
	

}
