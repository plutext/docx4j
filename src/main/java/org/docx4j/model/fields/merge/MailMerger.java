package org.docx4j.model.fields.merge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.docx4j.Docx4jProperties;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.fields.FieldLocator;
import org.docx4j.model.fields.FieldRef;
import org.docx4j.model.fields.FieldsPreprocessor;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.model.structure.PageSizePaper;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTPageNumber;
import org.docx4j.wml.CTRel;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.SectPr;


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
 * LIMITATIONS:
 * - no support for text before (\b) and text after (\f)
 *   switches
 * - no support for \m and \v switches
 * - no support formatting (date/time, numeric, or general), 
 *   including MERGEFORMAT (which means preserve
 *   the formatting of any existing field result)
 * - no support for multiple MERGEFIELD in a single
 *   instruction (eg MERGEFIELD CoutesyTitle \f " " MERGEFIELD FirstName \f " " MERGEFIELD LastName ) 
 * 
 * @author jharrop
 *
 */
public class MailMerger {

	private static Logger log = Logger.getLogger(MailMerger.class);		

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
		
		FieldsPreprocessor.complexifyFields(input.getMainDocumentPart() );
		System.out.println("complexified: " + XmlUtils.marshaltoString(input.getMainDocumentPart().getJaxbElement(), true));
		List<List<Object>> results = performOverList(input.getMainDocumentPart().getContent(), data );

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
				
				System.out.println("complexified: " + XmlUtils.marshaltoString(part.getJaxbElement(), true));
				
				hfTemplates.put(rel, part);
			}
		}
		
		// Prepare for cloning
		OpcPackage result = null;
		
		// Zip it up
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SaveToZipFile saver = new SaveToZipFile(input);
		saver.save(baos);
		byte[] template = baos.toByteArray();

		
		WordprocessingMLPackage target = WordprocessingMLPackage.load(
				new ByteArrayInputStream(template));
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
		for (List<Object> content : results) {
						
			// now inject the content
			target.getMainDocumentPart().getContent().addAll(content);
			
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
					List<Object> newContent = performOnInstance(
							((ContentAccessor)part).getContent(), 
							data.get(i) );	
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
		
		// MDP
		FieldsPreprocessor.complexifyFields(input.getMainDocumentPart() );
		List<Object> mdpResults = performOnInstance(input.getMainDocumentPart().getContent(), data );
		input.getMainDocumentPart().getContent().clear();
		input.getMainDocumentPart().getContent().addAll(mdpResults);
		
		if (processHeadersAndFooters) {
			
			RelationshipsPart rp = input.getMainDocumentPart().getRelationshipsPart();
			for ( Relationship r : rp.getJaxbElement().getRelationship()  ) {
				
				if (r.getType().equals(Namespaces.HEADER)
						|| r.getType().equals(Namespaces.FOOTER)) {
					
					JaxbXmlPart part = (JaxbXmlPart)rp.getPart(r);
					
					FieldsPreprocessor.complexifyFields(part );
					List<Object> results = performOnInstance(
							((ContentAccessor)part).getContent(), data );
					((ContentAccessor)part).getContent().clear();
					((ContentAccessor)part).getContent().addAll(results);
					
					System.out.println(XmlUtils.marshaltoString(part.getJaxbElement(), true));
					
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
	private static List<List<Object>> performOverList(List<Object> contentList, 
			List<Map<DataFieldName, String>> data ) throws Docx4JException {
		
				
		List<List<Object>> results = new ArrayList<List<Object>>();
		for (Map<DataFieldName, String> datamap : data) {
			
			results.add(
					performOnInstance(contentList, datamap));
		}
		
		return results;
	}
	
	private static List<Object> performOnInstance(List<Object> contentList, 
			Map<DataFieldName, String> datamap ) throws Docx4JException {
		
		// We need our fieldRefs point to the correct objects;
		// the easiest way to do this is to create them after cloning!
		
		// to facilitate cloning, wrap the list in a body
		Body shell = Context.getWmlObjectFactory().createBody();
		shell.getContent().addAll(contentList);
		Body shellClone = (Body)XmlUtils.deepCopy(shell);
		
		// find fields
		FieldLocator fl = new FieldLocator();
		new TraversalUtil(shellClone, fl);
		log.info("Found " + fl.getStarts().size() + " fields ");
		
		
		// canonicalise and setup fieldRefs 
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		for( P p : fl.getStarts() ) {
			int index = ((ContentAccessor)p.getParent()).getContent().indexOf(p);
			P newP = FieldsPreprocessor.canonicalise(p, fieldRefs);
			System.out.println("NewP length: " + newP.getContent().size() );
			((ContentAccessor)p.getParent()).getContent().set(index, newP);
		}
		
		// Populate
		for (FieldRef fr : fieldRefs) {
			
			String instr = fr.getInstr();
			if ( isMergeField(instr) ) {

				// eg <w:instrText xml:space="preserve"> MERGEFIELD  Kundenstrasse \* MERGEFORMAT </w:instrText>
				// or <w:instrText xml:space="preserve"> MERGEFIELD  Kundenstrasse</w:instrText>
				
				String tmp = instr.substring( instr.indexOf("MERGEFIELD") + 10);
				tmp = tmp.trim();
				String key  = tmp.indexOf(" ") >-1 ? tmp.substring(0, tmp.indexOf(" ")) : tmp ;
				log.info("Key: '" + key + "'");
				
				String val = datamap.get( new DataFieldName(key));
				
				if (val==null) {
					log.warn("Couldn't find value for key: '" + key + "'");
				} else {
					fr.setResult(val);
				}
				
				// If doing an actual mail merge, the begin-separate run is removed, as is the end run
				fr.getParent().getContent().remove(fr.getBeginRun());
				fr.getParent().getContent().remove(fr.getEndRun());
				
//				System.out.println(XmlUtils.marshaltoString(
//						fr.getParent(), true, true));
				
			}
		}
		
		return shellClone.getContent();

	}
	

	public static boolean isMergeField(String type) {
	
		if (type.contains("MERGEFIELD")) {
			return true;
		} else {
			return false;
		}
	}
	

}
