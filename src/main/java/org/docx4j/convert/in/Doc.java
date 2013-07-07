/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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

package org.docx4j.convert.in;


import java.io.FileInputStream;
import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.LineSpacingDescriptor;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.Spacing;
import org.docx4j.wml.STLineSpacingRule;

/**
 * @author jason
 *
 */
public class Doc {

	private static Logger log = LoggerFactory.getLogger(Doc.class);

	/**
	 * @param in doc file
	 * @return new WordprocessingMLPackage containing the results of 
	 * the conversion
	 * @throws Exception
	 */
	public static WordprocessingMLPackage convert(FileInputStream in)
			throws Exception {

		HWPFDocument doc = new HWPFDocument(in);

		WordprocessingMLPackage out = WordprocessingMLPackage.createPackage();

		convert(doc, out);
		
		return out;
	}

//	public static boolean convert(FileInputStream in, WordprocessingMLPackage out) throws Exception {
//		HWPFDocument doc = new HWPFDocument(in);
//		return convert(doc, out);
//	}

	
//	public static boolean convert(org.apache.commons.vfs.FileObject in,
//	WordprocessingMLPackage out) throws Exception {

	/**
	 * This method is private, since the fact that conversion is (currently)
	 * performed using POI's HWPF should be encapsulated.
	 * 
	 * @param doc
	 * @param wordMLPackage
	 * @return success or failure
	 */
	private static void convert(HWPFDocument doc,
			WordprocessingMLPackage wordMLPackage) throws Exception {
		
		// Convert styles
		org.apache.poi.hwpf.model.StyleSheet stylesheet = doc.getStyleSheet();
			// TODO - higher priority
			// At present, a default set of styles are defined in the output
			// document.
		
		// Convert lists
		org.apache.poi.hwpf.model.ListTables listTables = doc.getListTables();
			// TODO
		
		// Convert document properties
		org.apache.poi.hwpf.model.DocumentProperties docProps = doc.getDocProperties();
			// TODO
		
		// Convert main document part
		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();		

		Range r = doc.getRange();

		for (int x = 0; x < r.numSections(); x++) {
			Section s = r.getSection(x);
			
			// TODO - convert section
			
			for (int y = 0; y < s.numParagraphs(); y++) {
				Paragraph p = s.getParagraph(y);
												
                if (p.isInTable()) {
					Table t = s.getTable(p);
					int cl = numCol(t);

					log.info("Found " + t.numRows() + "x" + cl
							+ " table - TODO - convert");

					handleTable(t, stylesheet, documentPart, factory);
					
//					addTODO(factory, wmlP, "[TABLE " + + t.numRows() + "x" + cl 
//							+ " - can't convert tables yet]");

					y += t.numParagraphs() - 1;
				} else {
					org.docx4j.wml.P paraToAdd = handleP(p, stylesheet,
							documentPart, factory);
					
					documentPart.addObject(paraToAdd);
				}

			}
		}

	}

	private static org.docx4j.wml.P handleP(Paragraph p,
			org.apache.poi.hwpf.model.StyleSheet stylesheet,
			MainDocumentPart documentPart,
			org.docx4j.wml.ObjectFactory factory) {
		
		org.docx4j.wml.P wmlP =  null; 		
		
		

		if (p.getStyleIndex() > 0) {
			log.debug("Styled paragraph, with index: " + p.getStyleIndex());
			String styleName = stylesheet
					.getStyleDescription(p.getStyleIndex()).getName();
			log.debug(styleName);
			
			wmlP = documentPart.createStyledParagraphOfText( stripSpace(styleName), null);
			
		} else {
			wmlP = documentPart.createParagraphOfText(null);
		}
		
//		LineSpacingDescriptor lsd = p.getLineSpacing();
//		if (lsd==null || lsd.isEmpty()) {
//			// do nothing
//		} else {
//			PPr pPr = wmlP.getPPr();
//			if (pPr==null) {
//				pPr = Context.getWmlObjectFactory().createPPr();
//				wmlP.setPPr(pPr);
//			}
//			Spacing spacing = Context.getWmlObjectFactory().createPPrBaseSpacing();
//			spacing.setLine(lsd._dyaLine); // not visible
//			spacing.setLineRule(STLineSpacingRule.AUTO);
//			pPr.setSpacing(spacing);
//		}
		

		for (int z = 0; z < p.numCharacterRuns(); z++) {
			// character run
			CharacterRun run = p.getCharacterRun(z);

			// No character styles defined in there??

			org.docx4j.wml.RPr rPr = null;
			
			if (run.isBold()) {
				
				// TODO - HIGH PRIORITY- handle other run properties
				// esp underline, font size
				if (rPr == null) {
					rPr = factory.createRPr();
				}
				
				org.docx4j.wml.BooleanDefaultTrue boldOn = factory.createBooleanDefaultTrue();
				boldOn.setVal( Boolean.TRUE);
				
				rPr.setB(boldOn);
				
			}

			// character run text
			String text = run.text();
			
			// show us the text
			log.debug("Processing: " + text);
			
			String cleansed = stripNonValidXMLCharacters(text);
			// Necessary to avoid org.xml.sax.SAXParseException: An invalid XML character 
			// (Unicode: 0xb) was found in the element content of the document.
			// when trying to open the resulting docx.
			// ie JAXB happily writes (marshals) it, but doesn't want to
			// unmarshall.
			
			if (!text.equals(cleansed)) {
				log.warn("Cleansed..");
			}
			
			org.docx4j.wml.Text  t = factory.createText();
			t.setValue(cleansed);
	
			org.docx4j.wml.R  wmlRun = factory.createR();
			
			if (rPr!=null) {
				wmlRun.setRPr(rPr);
			}
			
			wmlRun.getRunContent().add(t);		
			
			wmlP.getParagraphContent().add(wmlRun);					
			
		}
		
		System.out.println(XmlUtils.marshaltoString(wmlP, true, true));
		
		return wmlP;

	}
	
	private static String stripSpace(String in) {
		
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < in.length(); i++) {
			if (in.charAt(i) != ' ') {
				sb.append(in.charAt(i));
			}
		}
		
		return sb.toString();
	}
	
	private static void addTODO(org.docx4j.wml.ObjectFactory factory,
			org.docx4j.wml.P wmlP, String message) {
		
		org.docx4j.wml.Text  t = factory.createText();
		t.setValue(message);

		org.docx4j.wml.R  wmlRun = factory.createR();
		wmlRun.getRunContent().add(t);		
		
		wmlP.getParagraphContent().add(wmlRun);							
		
	}
	
	
	 /**
     * This method ensures that the output String has only
     * valid XML unicode characters as specified by the
     * XML 1.0 standard. For reference, please see
     * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
     * standard</a>. This method will return an empty
     * String if the input is null or empty.
     * 
     * See http://cse-mjmcl.cse.bris.ac.uk/blog/2007/02/14/1171465494443.html
     *
     * @param in The String whose non-valid characters we want to remove.
     * @return The in String, stripped of non-valid characters.
     */
    public static String stripNonValidXMLCharacters(String in) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xD) ||
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF))) {
                out.append(current);
            } else {
            	out.append("[#?]");
            }
        }
        return out.toString();
    }    	
	
	  private static int numCol(Table t) {
		int col = 0;
		for (int i = 0; i < t.numRows(); i++) {
			if (t.getRow(i).numCells() > col)
				col = t.getRow(i).numCells();
		}

		return col;
	}

	private static void handleTable(Table t,
			org.apache.poi.hwpf.model.StyleSheet stylesheet,			
			MainDocumentPart documentPart,
			org.docx4j.wml.ObjectFactory factory) {
		
		org.docx4j.wml.Tbl tbl = factory.createTbl();
		documentPart.addObject(tbl);
		
		org.docx4j.wml.TblPr tblPr = factory.createTblPr();
		tbl.setTblPr(tblPr);
		// TODO - set tblPr values
		
		org.docx4j.wml.TblGrid tblGrid = factory.createTblGrid();
		tbl.setTblGrid(tblGrid);
		// TODO - set tblGrid values
				
		for (int i = 0; i < t.numRows(); i++) {
			TableRow tr = t.getRow(i);
			
			org.docx4j.wml.Tr trOut = factory.createTr();
			tbl.getEGContentRowContent().add(trOut);

			for (int j = 0; j < tr.numCells(); j++) {
				TableCell tc = tr.getCell(j);
				
				org.docx4j.wml.Tc tcOut = factory.createTc();
				trOut.getEGContentCellContent().add(tcOut);
								
				//System.out.println("CELL[" + i + "][" + j + "]=" + tc.text());
				
				for (int y = 0; y < tc.numParagraphs(); y++) {
					Paragraph p = tc.getParagraph(y);

					// Nested tables?
	                // if (p.isInTable()) ???
				
					org.docx4j.wml.P paraToAdd = handleP(p, stylesheet,
						documentPart, factory);
					
					tcOut.getEGBlockLevelElts().add(paraToAdd);
					
					log.debug("Added p to tc");
				}
				
			}
		}
	}	

	public static void main(String[] args) throws Exception {
				
		String localPath = System.getProperty("user.dir") + "/LineSpacing.doc";
				
		WordprocessingMLPackage out = convert(new FileInputStream(localPath));
		
//		String outputfilepath = "/home/dev/tmp/test-out.docx";		
//		
//		SaveToZipFile saver = new SaveToZipFile(out);
//		saver.save(outputfilepath);
//		log.info("Done - saved docx as " + outputfilepath);
		
		
	}	
}
