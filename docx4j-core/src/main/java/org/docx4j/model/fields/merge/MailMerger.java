package org.docx4j.model.fields.merge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.Docx4jProperties;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
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
import org.jvnet.jaxb2_commons.ppp.Child;
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
public class MailMerger {

	private static Logger log = LoggerFactory.getLogger(MailMerger.class);		

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
	public static WordprocessingMLPackage getConsolidatedResultCrude(WordprocessingMLPackage input, 
			List<Map<DataFieldName, String>> data) throws Docx4JException {
		return getConsolidatedResultCrude(input, data, false);
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
	public static WordprocessingMLPackage getConsolidatedResultCrude(WordprocessingMLPackage input, 
			List<Map<DataFieldName, String>> data, boolean processHeadersAndFooters) throws Docx4JException {
		
		FormTextFieldNames formTextFieldNames = new FormTextFieldNames(); 		
		
		// create contents destined for the main document part
		FieldsPreprocessor.complexifyFields(input.getMainDocumentPart() );
        if(log.isDebugEnabled()) {
            log.debug("complexified: " + XmlUtils.marshaltoString(input.getMainDocumentPart().getJaxbElement(), true));
        }
		List<List<Object>> mdpResults = performOverList(input, input.getMainDocumentPart().getContent(), data, formTextFieldNames );

		// headers/footers
		Map<CTRel, JaxbXmlPart> hfTemplates = null;
		BooleanDefaultTrue titlePage = null;
		if (processHeadersAndFooters) {
			// then we need a clone/template of the headers/footers
			// in the first section
			
			hfTemplates = new HashMap<CTRel, JaxbXmlPart>();
			
			SectionWrapper sw = input.getDocumentModel().getSections().get(0);
			SectPr sectPr = sw.getSectPr();
			
			List<CTRel> hdrFtrRefs = sectPr.getEGHdrFtrReferences();
			titlePage = sectPr.getTitlePg();
			
			for (CTRel rel : hdrFtrRefs) {
				String relId = rel.getId();
				log.debug("for h|f relId: " + relId);
				
				JaxbXmlPart part = (JaxbXmlPart)input.getMainDocumentPart().getRelationshipsPart().getPart(relId);
				FieldsPreprocessor.complexifyFields(part );

                if(log.isDebugEnabled()) {
                    log.debug("complexified: " + XmlUtils.marshaltoString(part.getJaxbElement(), true));
                }
				
				hfTemplates.put(rel, part);
			}
		}
		
		// Create WordprocessingMLPackage target, by cloning
		OpcPackage result = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SaveToZipFile saver = new SaveToZipFile(input);
		saver.save(baos);
		byte[] template = baos.toByteArray();
		WordprocessingMLPackage target = WordprocessingMLPackage.load(
				new ByteArrayInputStream(template));
		
		
		// populate main document part
		SectPr documentSeparator = getDocumentSeparator(target);
		if (processHeadersAndFooters) {
			if (titlePage!=null
					&& titlePage.isVal()) {
				documentSeparator.setTitlePg(titlePage);
			}
			documentSeparator.getEGHdrFtrReferences().clear();
		}
		target.getMainDocumentPart().getContent().clear();
		
		/*
		 * What we're doing, effectively, is doing the 
		 * main content in a single hit (ie for all
		 * instances), and then, for each instance,
		 * doing the headers/footers.
		 * 
		 * It is this way because that is how the code
		 * has evolved.
		 * 
		 * Since we have to do the headers/footers 
		 * instance by instance, it would probably
		 * be neater to do the main content at 
		 * the same time (ie instead of using 
		 * performOverList at the start of this method).
		 */
		
		int i = 0;
		for (List<Object> content : mdpResults) {
			
						
			// now inject the content
			target.getMainDocumentPart().getContent().addAll(content);

            if(log.isDebugEnabled()) {
                log.debug(XmlUtils.marshaltoString(target.getMainDocumentPart().getJaxbElement(), true, true));
            }
			
			// add sectPr to final paragraph
			Object last = content.get( content.size()-1);
			P lastP = null;
			if (last instanceof P) {
				lastP = (P)last;
			} else {
				lastP = Context.getWmlObjectFactory().createP();
				target.getMainDocumentPart().getContent().add(lastP);	
			}
			if (lastP.getPPr()==null) {
				lastP.setPPr(Context.getWmlObjectFactory().createPPr());				
			}
			SectPr thisSection = XmlUtils.deepCopy(documentSeparator);
			lastP.getPPr().setSectPr(thisSection);
			
			if (processHeadersAndFooters) {
				for( CTRel ctRel : hfTemplates.keySet()) {
					
					// Create a suitable part
					JaxbXmlPart part = hfTemplates.get(ctRel);
					JaxbXmlPart clonedPart = null;
					if (part instanceof HeaderPart) {
						clonedPart = new HeaderPart();
						clonedPart.setJaxbElement(Context.getWmlObjectFactory().createHdr());
					} else if (part instanceof FooterPart) {
						clonedPart = new FooterPart();
						clonedPart.setJaxbElement(Context.getWmlObjectFactory().createFtr());
					}
					
					// Populate it
					List<Object> newContent = performOnInstance(input,
							((ContentAccessor)part).getContent(), 
							data.get(i), formTextFieldNames );	
					((ContentAccessor)clonedPart).getContent().addAll(newContent);
					
					// Add it
					Relationship rel = target.getMainDocumentPart().addTargetPart(clonedPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS);
					
					// Now add CTRel!
					CTRel newHfRef = XmlUtils.deepCopy(ctRel);
					newHfRef.setId(rel.getId());
					
					thisSection.getEGHdrFtrReferences().add(newHfRef);
					
				}
			}
			i++;
		}
		
		
		return target;
	}

	   /**
	    * Word uses the existing sectPr element, but adds 
	    * a page numbering restart to it.  TODO: investigate
	    * what it does with headers/footers.
	 * @param template
	 * @return
	 */
	private static SectPr getDocumentSeparator(WordprocessingMLPackage template) {
	    	
		   SectPr sectPr = template.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
		   
		   if (sectPr==null) {
			   // Maybe the last P already contains one?
			   // Presumably Word would reuse this.
			   List all = template.getMainDocumentPart().getContent();
		    	Object last = all.get( all.size()-1 );
		    	if (last instanceof P) {
		    		if (((P) last).getPPr()!=null 
		    				&& ((P) last).getPPr().getSectPr() !=null) {
		    			sectPr = ((P) last).getPPr().getSectPr();
		    		}
		    	}			   
		   }
		   
		   if (sectPr==null) {
			   
				// Create a basic sectPr using our Page model
				String papersize= Docx4jProperties.getProperties().getProperty("docx4j.PageSize", "A4");
				log.info("Using paper size: " + papersize);
				
				String landscapeString = Docx4jProperties.getProperties().getProperty("docx4j.PageOrientationLandscape", "false");
				boolean landscape= Boolean.parseBoolean(landscapeString);
				log.info("Landscape orientation: " + landscape);
				
				PageDimensions page = new PageDimensions();
				page.setPgSize(PageSizePaper.valueOf(papersize), landscape);
				
				sectPr = Context.getWmlObjectFactory().createSectPr();
				sectPr.setPgSz(  page.getPgSz() );
				sectPr.setPgMar( page.getPgMar() );				
		   }
		   
		   // <w:pgNumType w:start="1"/>
		   CTPageNumber pageNumber = sectPr.getPgNumType();
		   if (pageNumber==null) {
			   pageNumber = Context.getWmlObjectFactory().createCTPageNumber();
			   sectPr.setPgNumType(pageNumber);
		   }
		   pageNumber.setStart(BigInteger.ONE);
		   
		   return sectPr;
	   }	
	
//	public static List<WordprocessingMLPackage> getResults(WordprocessingMLPackage input, 
//			List<Map<DataFieldName, String>> data) throws Docx4JException {
//		
//		List<WordprocessingMLPackage> pkgs = new ArrayList<WordprocessingMLPackage>();
//		
//		// Just MDP for now
//		FieldsPreprocessor.complexifyFields(input.getMainDocumentPart() );
//		List<List<Object>> results = performOverList(input.getMainDocumentPart().getContent(), data );
//		
//		// Prepare for cloning
//		OpcPackage result = null;
//		
//		// Zip it up
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		SaveToZipFile saver = new SaveToZipFile(input);
//		saver.save(baos);
//		byte[] template = baos.toByteArray();
//		
//		for (List<Object> content : results) {
//			
//			WordprocessingMLPackage target = WordprocessingMLPackage.load(
//					new ByteArrayInputStream(template));
//			pkgs.add(target);			
//			
//			// now inject the content
//			target.getMainDocumentPart().getContent().clear();
//			target.getMainDocumentPart().getContent().addAll(content);
//		}
//		
//		return pkgs;
//	}

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
	public static void performMerge(WordprocessingMLPackage input, 
			Map<DataFieldName, String> data, boolean processHeadersAndFooters ) throws Docx4JException {
		
		// HOWTO Clone the input docx
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		SaveToZipFile saver = new SaveToZipFile(input);
//		saver.save(baos);
//		byte[] template = baos.toByteArray();
//		WordprocessingMLPackage target = WordprocessingMLPackage.load(
//				new ByteArrayInputStream(template));
		
		// Required where converting MERGEFIELD to FORMTEXT
		FormTextFieldNames formTextFieldNames = new FormTextFieldNames(); 
		
		// MDP
		FieldsPreprocessor.complexifyFields(input.getMainDocumentPart() );
//		log.debug("\n\n COMPLEXIFIED " + input.getMainDocumentPart().getPartName().getName() + "\n\n"
//				+ input.getMainDocumentPart().getXML() + "\n");
		List<Object> mdpResults = performOnInstance(input, input.getMainDocumentPart().getContent(), data, formTextFieldNames );
		input.getMainDocumentPart().getContent().clear();
		input.getMainDocumentPart().getContent().addAll(mdpResults);
		
		if (processHeadersAndFooters) {
			
			RelationshipsPart rp = input.getMainDocumentPart().getRelationshipsPart();
			for ( Relationship r : rp.getJaxbElement().getRelationship()  ) {
				
				if (r.getType().equals(Namespaces.HEADER)
						|| r.getType().equals(Namespaces.FOOTER)) {
					
					JaxbXmlPart part = (JaxbXmlPart)rp.getPart(r);

                    if(log.isDebugEnabled()) {
                        log.debug("\n\n BEFORE " + part.getPartName().getName() + "\n\n"
                                + XmlUtils.marshaltoString(part.getJaxbElement(), true, true) + "\n");
                    }
					
					FieldsPreprocessor.complexifyFields(part );

                    if(log.isDebugEnabled()) {
                        log.debug("\n\n COMPLEXIFIED " + part.getPartName().getName() + "\n\n"
                                + XmlUtils.marshaltoString(part.getJaxbElement(), true, true) + "\n");
                    }
					
					List<Object> results = performOnInstance(input,
							((ContentAccessor)part).getContent(), data, formTextFieldNames );
					((ContentAccessor)part).getContent().clear();
					((ContentAccessor)part).getContent().addAll(results);

                    if(log.isDebugEnabled()) {
                        log.debug("\n\n AFTER " + part.getPartName().getName() + "\n\n"
                                + XmlUtils.marshaltoString(part.getJaxbElement(), true, true) + "\n");
                    }
					
				}			
			}		
		}
			
			
	}
	
    
	
	/**
	 * The idea is to be able to perform a mail merge
	 * on content from main document part, or a header/footer etc.
	 * 
	 * We return a list of content lists, so that the consumer
	 * can choose whether to produce a single docx (via
	 * MergeDocx or otherwise), or a docx for each item in the list.
	 * 
	 * @param wordMLPackage
	 * @param data
	 * @return
	 * @throws Docx4JException 
	 */
	private static List<List<Object>> performOverList(WordprocessingMLPackage input, 
			List<Object> contentList, 
			List<Map<DataFieldName, String>> data,
			FormTextFieldNames formTextFieldNames) throws Docx4JException {
		
		List<List<Object>> results = new ArrayList<List<Object>>();
		for (Map<DataFieldName, String> datamap : data) {
			
			results.add(
					performOnInstance(input, contentList, datamap, formTextFieldNames));
		}
		
		return results;
	}
	
	
	
	private static List<Object> performOnInstance(WordprocessingMLPackage input, 
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
			
			if ( fr.getFldName().equals("MERGEFIELD") ) {
				
				String instr = extractInstr(fr.getInstructions() );
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
	 * @param fl
	 * @param fieldRefs
	 * @throws Docx4JException
	 */
	protected static void canonicaliseStarts(ComplexFieldLocator fl,
			List<FieldRef> fieldRefs) throws Docx4JException {
		for( P p : fl.getStarts() ) {
			int index;
			if (p.getParent() instanceof ContentAccessor) {
				// 2.8.1
				index = ((ContentAccessor)p.getParent()).getContent().indexOf(p);
				P newP = FieldsPreprocessor.canonicalise(p, fieldRefs);
				newP.setParent(p.getParent());
                if(log.isDebugEnabled()) {
                    log.debug("Canonicalised: " + XmlUtils.marshaltoString(newP, true, true));
                }
				((ContentAccessor)p.getParent()).getContent().set(index, newP);
			} else if (p.getParent() instanceof java.util.List) {
				// 3.0
				index = ((java.util.List)p.getParent()).indexOf(p);
				P newP = FieldsPreprocessor.canonicalise(p, fieldRefs);
				newP.setParent(p.getParent());
				log.debug("NewP length: " + newP.getContent().size() );
				((java.util.List) p.getParent()).set(index, newP);
			} else if (p.getParent() instanceof CTTextbox) {
				// 3.0.1
				index = ((CTTextbox) p.getParent()).getTxbxContent().getContent().indexOf(p);
				P newP = FieldsPreprocessor.canonicalise(p, fieldRefs);
				newP.setParent(p.getParent());
                if(log.isDebugEnabled()) {
                    log.debug("Canonicalised: " + XmlUtils.marshaltoString(newP, true, true));
                }
				((CTTextbox) p.getParent()).getTxbxContent().getContent().set(index, newP);
			} else {
				throw new Docx4JException("Unexpected parent: "+ p.getParent().getClass().getName());
			}
		}
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
			log.error("TODO MERGEFIELD field contained complex instruction");
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
	
    /**
     * Remove the field but preserve the paragraph and content around it
     * 
     * @param fr
     */
    protected static void removeSimpleField(FieldRef fr) {
        int end = fr.getParent().getContent().indexOf(fr.getEndRun());
        int begin = fr.getParent().getContent().indexOf(fr.getBeginRun());
        for (int i = end; i >= begin; i--) {
            fr.getParent().getContent().remove(i);
        }
    }

    /**
     * Parse through all content inside the paragraph to concatenate all values inside a text
     * 
     * @param paragraph The paragraph which contains (or not) data
     * @return All text inside the paragraph
     */
    protected static String getTextInsideContent(ContentAccessor paragraph) {
        StringBuilder result = new StringBuilder();
        for (Object content : paragraph.getContent()) {
            if (content instanceof org.docx4j.wml.R) {
                org.docx4j.wml.R run = (org.docx4j.wml.R) content;
                List<Object> runContent = run.getContent();
                for (Object o2 : runContent) {
                    if (o2 instanceof javax.xml.bind.JAXBElement) {
                        if (((JAXBElement<?>) o2).getDeclaredType().getName().equals("org.docx4j.wml.Text")) {
                            org.docx4j.wml.Text t = (org.docx4j.wml.Text) ((JAXBElement) o2).getValue();
                            result.append(t.getValue());
                        }
                    } else if(o2 instanceof org.docx4j.wml.Text){
                        org.docx4j.wml.Text t = (org.docx4j.wml.Text) o2;
                        result.append(t.getValue());
                    } 
                }
            }
        }
        return result.toString();
    }

    /**
     * To remove an object from the docx template
     * 
     * @param content Body (or other part) of the template
     * @param needToBeRemoved The object that will be removed from the content
     */
    protected static void recursiveRemove(ContentAccessor content, Object needToBeRemoved) {

//    	if (log.isDebugEnabled() ) {
//    		log.debug("removing " + needToBeRemoved.getClass().getName());
//    	}
    	
    	// we've already removed the simple field
    	// here we'll be removing a paragraph say.
    	// But we can't do that in a tc if its the only p
    	if (needToBeRemoved instanceof P ) {
    		Object parent = ((Child)needToBeRemoved).getParent();
    		if (parent==null) {
    			log.debug("Unknown parent");
    		} else if (parent instanceof Tc) {
    			if ( ((Tc)parent).getContent().size()==1 ) {
    				// a tc must contain a p; it can't be empty
        			log.debug("preserving p in tc");
        			return;
    			}
    		}
    		log.debug("parent: " + ((Child)needToBeRemoved).getParent().getClass().getName());
    		
    	}
    	
        if (content.getContent().contains(needToBeRemoved)) {
            content.getContent().remove(needToBeRemoved);
            log.debug(".. removed");
            return;
        }

        // TODO REVISIT!  I guess this is just trying to match JAXBElement case
        for (Object object : content.getContent()) {
            if (object instanceof ContentAccessor) {
                recursiveRemove((ContentAccessor) object, needToBeRemoved);
            } else if (object instanceof JAXBElement<?>) {
                JAXBElement<?> element = (JAXBElement<?>) object;
                if (element.getValue() instanceof ContentAccessor) {
                    recursiveRemove((ContentAccessor) element.getValue(), needToBeRemoved);
                }
            }
        }
    }	
	
	protected static OutputField fieldFate = OutputField.REMOVED;
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
    public static void setMERGEFIELDInOutput(OutputField fieldFate) {
    	MailMerger.fieldFate = fieldFate;
    }

	
	  public enum OutputField {

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
	
	/**
	 * If we're converting MERGEFIELD to FORMTEXT, it is
	 * desirable to make the w:fldChar/w:ffData/w:name
	 * unique within the docx (though Word 2010 can still open the docx if they
	 * aren't), and to remove spaces 
	 * 
	 * @author jharrop
	 * @since 3.0.0
	 */
	protected static class FormTextFieldNames {
		
		// MS-OE376 says Word only allows strings of length at most 40 for the name attribute.
		
		// Also, by experiment (in Word 2010), only alphanumberic characters and "_" are allowed 
		// (no spaces, other symbols/punctuation etc),
		// and it can't start with a numeral.  Comparison is case insensitive.
		// Some UTF-8 characters are allowed eg ϣ
		Pattern pattern = java.util.regex.Pattern.compile("[^a-zA-Z0-9]");
		
		// http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/bookmarkStart.html
		// says:
		//    If multiple bookmarks in a document share the same name, then the first bookmark 
		//    (defined by the location of the bookmarkStart element in document order) shall 
		//    be maintained, and all subsequent bookmarks should be ignored.
		
		// By experiment (inserting a cross ref) it looks like Word 2010 actually ignores the first!
		// Not that that matters here, since we make the field names unique
		
		private FormTextFieldNameSet names = new FormTextFieldNameSet(); 
		
		public String generateName(String input) {
			
			// Strip characters
			String unpunctuated = pattern.matcher(input).replaceAll("_");			
			
			// Ensure it starts with a letter
			char c = unpunctuated.charAt(0);
			if('0'<=c && c<='9') {
				unpunctuated = "z" + unpunctuated;
			} else if(c=='_') {
				unpunctuated = "z" + unpunctuated;
			}
			
			if (names.contains(unpunctuated)) {
				// Then make unique
				int i = 2;
				String newName = null;
		    	do {
		    		newName = unpunctuated + i;
		    		i++;
		    		
		    	} while (names.contains(newName));
		    	unpunctuated = newName;
			}
			
			// Add to FormTextFieldNameSet
	    	names.add(unpunctuated);
			
	    	return unpunctuated;
		}
		
		/**
		 * Case insensitive key
		 * (matching http://www.w3.org/TR/css3-fonts/#font-family-casing
		 */
		private static class FormTextFieldNameSet extends HashSet<String> {

		    @Override
		    public boolean add(String key) {
		       log.debug("Added '" + key.toLowerCase() + "'");	
		       return super.add(key.toLowerCase());
		    }

		    // not @Override because that would require the key parameter to be of type Object
		    public boolean contains(String key) {
		       return super.contains(key.toLowerCase());
		    }
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
