package org.docx4j.samples;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.model.fields.ComplexFieldLocator;
import org.docx4j.model.fields.FieldInstructionException;
import org.docx4j.model.fields.FieldRef;
import org.docx4j.model.fields.FieldsPreprocessor;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;

/**
 * List all field instructions found in docx (main document part,
 * headers, footers), in Excel CSV format.
 * 
 * Produces records of the form:
 *   
 *    filename, partname, instruction, multi, data sample
 *    
 * TODO 
 * - extract info for FORMTEXT from start and result? 
 *
 */
public class FieldsReport {

	private static Logger log = Logger.getLogger(FieldsReport.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		// A docx or a dir containing docx files
		String inputpath = System.getProperty("user.dir") + "/corpus";
		
		// The sample data
		Map<DataFieldName, String> sampleData = new HashMap<DataFieldName, String>();
		sampleData.put( new DataFieldName("ACCOUNT1.SIGNER1.NAME"), "John Citizen");
		sampleData.put(new DataFieldName("CCN"), "123456");
		sampleData.put(new DataFieldName("LINE1"), "Addr Line 1");
		sampleData.put(new DataFieldName("LINE2"), "Addr Line 2");
		sampleData.put(new DataFieldName("LINE3"), "Addr Line 3");
		sampleData.put(new DataFieldName("CITY"), "Chicago");
		sampleData.put(new DataFieldName("STATE"), "Illinois");
		sampleData.put(new DataFieldName("ZIP"), "100");
		sampleData.put(new DataFieldName("COUNTRY"), "USA");
		sampleData.put(new DataFieldName("OWNERNAME"),"Johnny Walker");
		sampleData.put(new DataFieldName("USERLINE1"), "Addr Line 1");
		sampleData.put(new DataFieldName("USERLINE2"), "Addr Line 2");
		sampleData.put(new DataFieldName("USERLINE3"), "Addr Line 3");
		sampleData.put(new DataFieldName("USERCITY"), "Chicago");
		sampleData.put(new DataFieldName("USERSTATE"), "Illinois");
		sampleData.put(new DataFieldName("USERZIP"), "100");
		sampleData.put(new DataFieldName("CLIENTLINE1"), "LINE 1");
		sampleData.put(new DataFieldName("CLIENTLINE2"), "LINE 2");
		sampleData.put(new DataFieldName("CLIENTLINE3"), "LINE 3");
		sampleData.put(new DataFieldName("CLIENT.CITY.STATE.ZIP.COUNTRY"), "Chicago IL US");
		sampleData.put(new DataFieldName("ACCOUNTTITLE"), "ACCOUNT TITLE");		

		FieldsReport report = new FieldsReport(sampleData);
		report.processFiles(inputpath);
		
		
	}

	private FieldsReport(Map<DataFieldName, String> sampleData) {
		this.sampleData = sampleData;
	}
	
	private Map<DataFieldName, String> sampleData = null;
	private String filename;
	private String partName;
	private StringBuilder sb = new StringBuilder(); 
	
	
	private void processFiles(String inputpath) throws Docx4JException {
		
		sb.append("filename, partname, instruction, multi, sample");

		File dir = new File(inputpath);
		
		if (dir.isDirectory()) {
			
			String[] files = dir.list();
			
			for (int i = 0; i<files.length; i++  ) {
				
				if (files[i].endsWith("docx")) {
					WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputpath + "/" + files[i]));
					filename = files[i];
					pkgReport(wordMLPackage);
				}
			}
			
		} else if (inputpath.endsWith("docx")) {
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputpath ));		
			filename = inputpath;
			pkgReport(wordMLPackage);			
		}
		
		// Output the report to the console
		System.out.println(sb.toString());
		
	}
	
	
	private void pkgReport(WordprocessingMLPackage wordMLPackage) throws Docx4JException {

		System.out.println("\n\n" + filename + "\n");

		
		FieldsPreprocessor.complexifyFields(wordMLPackage.getMainDocumentPart() );

//		System.out.println("\n" + wordMLPackage.getMainDocumentPart().getPartName() + "\n");
		partName = wordMLPackage.getMainDocumentPart().getPartName().getName();
		listFieldsInPart(  wordMLPackage.getMainDocumentPart().getContent() );

		{ // Headers, footers

			RelationshipsPart rp = wordMLPackage.getMainDocumentPart().getRelationshipsPart();
			for ( Relationship r : rp.getJaxbElement().getRelationship()  ) {

				if (r.getType().equals(Namespaces.HEADER)
						|| r.getType().equals(Namespaces.FOOTER)) {

					JaxbXmlPart part = (JaxbXmlPart)rp.getPart(r);

					FieldsPreprocessor.complexifyFields(part );

					partName = part.getPartName().getName();
					listFieldsInPart(((ContentAccessor)part).getContent() );

				}
			}
		}
	}

	
	private void listFieldsInPart(List<Object> contentList) throws Docx4JException {

		// find fields
		ComplexFieldLocator fl = new ComplexFieldLocator();
		new TraversalUtil(contentList, fl);
		
		if (fl.getStarts().size()==0) return;

		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		for( P p : fl.getStarts() ) {
			FieldsPreprocessor.canonicalise(p, fieldRefs);
		}

		// Populate
		for (FieldRef fr : fieldRefs) {
			recurse(fr);
		}

	}
	
	private void recurse(FieldRef fr) {
		
		//System.out.println(XmlUtils.marshaltoString(fr.getBeginRun(), true, true));
		
		sb.append("\n" + toCSV(filename) + "," + toCSV(partName) + ",");
		
		
//		String instr = extractInstr(fr.getInstructions());  // ignore nested
		String instr = null;
		boolean multi = (fr.getInstructions().size()>1);
		try {
			instr = fr.getInstruction();
		} catch (FieldInstructionException e) {
			sb.append( e.getMessage() );									
			sb.append(", " + multi + ", null");
			return;
		}
		sb.append( toCSV( instr ) );									
				
		sb.append(", " + multi + ", ");
		
		// Populate sample data field
		if ( fr.getFldName()==null ) {
			sb.append("?"); // Shouldn't happen with 2.8.1.11
		} else if ( fr.getFldName().equals("MERGEFIELD") ) {
			
//			String instr = extractInstr(fr.getInstructions() );
			String datafieldName = getDatafieldNameFromInstr(instr);
			String val = sampleData.get( new DataFieldName(datafieldName));
			sb.append( toCSV(val));
			
//		} else if ( fr.getFldName().equals("FORMTEXT") ) {
//			R r = fr.getResultsSlot();
//			if (r==null) {
//				sb.append("null");
//			} else {
//				sb.append(extractResult(r.getContent()));
//			}
		} else {
			sb.append("null");
		}
	}
			
	private String getDatafieldNameFromInstr(String instr) {
		
		String tmp = instr.substring( instr.indexOf("MERGEFIELD") + 10);
		tmp = tmp.trim();
		String datafieldName  = tmp.indexOf(" ") >-1 ? tmp.substring(0, tmp.indexOf(" ")) : tmp ;
		return datafieldName;
	}
	
	
	private String toCSV(String simple) {
		// CSV rules: http://creativyst.com/Doc/Articles/CSV/CSV01.htm
		// There is an ASLv2 library at http://sourceforge.net/p/opencsv/code/112/tree/trunk/
		// but it is overkill for our purposes
		
		if (simple==null) return null;
		
		// Escape " as ""
		simple = simple.replace("\"", "\"\"");
		
		// For our purposes, always quote
		return "\"" + simple + "\""; 
	}
			

}
