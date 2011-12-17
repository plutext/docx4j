package org.docx4j.model.fields.merge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.fields.FieldLocator;
import org.docx4j.model.fields.FieldRef;
import org.docx4j.model.fields.FieldsPreprocessor;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;


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
			List<Map<String, String>> data) throws Docx4JException {
		
		// Just MDP for now
		FieldsPreprocessor.complexifyFields(input.getMainDocumentPart() );
		List<List<Object>> results = perform(input.getMainDocumentPart(), data );

		// Prepare for cloning
		OpcPackage result = null;
		
		// Zip it up
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SaveToZipFile saver = new SaveToZipFile(input);
		saver.save(baos);
		byte[] template = baos.toByteArray();

		WordprocessingMLPackage target = WordprocessingMLPackage.load(
				new ByteArrayInputStream(template));
		target.getMainDocumentPart().getContent().clear();
		
		for (List<Object> content : results) {
						
			// now inject the content
			target.getMainDocumentPart().getContent().addAll(content);
			
			// TODO - sectPr
		}
		
		return target;
	}

	public static List<WordprocessingMLPackage> getResults(WordprocessingMLPackage input, 
			List<Map<String, String>> data) throws Docx4JException {
		
		List<WordprocessingMLPackage> pkgs = new ArrayList<WordprocessingMLPackage>();
		
		// Just MDP for now
		FieldsPreprocessor.complexifyFields(input.getMainDocumentPart() );
		List<List<Object>> results = perform(input.getMainDocumentPart(), data );
		
		// Prepare for cloning
		OpcPackage result = null;
		
		// Zip it up
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SaveToZipFile saver = new SaveToZipFile(input);
		saver.save(baos);
		byte[] template = baos.toByteArray();
		
		for (List<Object> content : results) {
			
			WordprocessingMLPackage target = WordprocessingMLPackage.load(
					new ByteArrayInputStream(template));
			pkgs.add(target);			
			
			// now inject the content
			target.getMainDocumentPart().getContent().clear();
			target.getMainDocumentPart().getContent().addAll(content);
		}
		
		return pkgs;
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
	private static List<List<Object>> perform(ContentAccessor contentList, 
			List<Map<String, String>> data ) throws Docx4JException {
		
				
		List<List<Object>> results = new ArrayList<List<Object>>();
		for (Map<String, String> datamap : data) {
			
			// We need our fieldRefs point to the correct objects;
			// the easiest way to do this is to create them after cloning!
			
			// to facilitate cloning, wrap the list in a body
			Body shell = Context.getWmlObjectFactory().createBody();
			shell.getContent().addAll(contentList.getContent());
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
					
					String tmp = instr.substring( instr.indexOf("MERGEFIELD") + 10);
					tmp = tmp.trim();
					String key  = tmp.substring(0, tmp.indexOf(" ")) ;
					log.info("Key: '" + key + "'");
					
					String val = datamap.get(key);
					
					if (val==null) {
						log.warn("Couldn't find value for key: '" + key + "'");
					} else {
						fr.setResult(val);
					}
					
					// If doing an actual mail merge, the begin-separate run is removed, as is the end run
					fr.getParent().getContent().remove(fr.getBeginRun());
					fr.getParent().getContent().remove(fr.getEndRun());
					
//					System.out.println(XmlUtils.marshaltoString(
//							fr.getParent(), true, true));
					
				}
			}

			results.add(shellClone.getContent());
		}
		
		return results;
	}

	public static boolean isMergeField(String type) {
	
		if (type.contains("MERGEFIELD")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(
				new java.io.File(
						System.getProperty("user.dir") + "/mergefield1.docx"));
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("Kundenname", "Plutext");
		map.put("Kundenstrasse", "Bourke Street");
		
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		data.add(map);
		
		
//		System.out.println(XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true));

		WordprocessingMLPackage output = getConsolidatedResultCrude(wordMLPackage, data);
		
		
		System.out.println(XmlUtils.marshaltoString(output.getMainDocumentPart().getJaxbElement(), true, true));
		
//		wordMLPackage.save(new java.io.File(
//				System.getProperty("user.dir") + "/mergefield1-OUT.docx") );
		
	}

}
