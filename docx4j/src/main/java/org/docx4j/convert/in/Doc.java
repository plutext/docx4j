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

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileTypeSelector;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.provider.local.LocalFile;
import org.apache.log4j.Logger;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.*;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.utils.VFSUtils;

/**
 * @author jason
 *
 */
public class Doc {

	private static Logger log = Logger.getLogger(Doc.class);

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
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public static WordprocessingMLPackage convert(
			org.apache.commons.vfs.FileObject in) throws Exception {

		LocalFile localCopy = null;
		if (!(in instanceof LocalFile)) {

			StringBuffer sb = new StringBuffer("file:///");
			sb.append(System.getProperty("user.home"));
			sb.append("/.docx4j/tmp/");
			sb.append(in.getName().getBaseName());
			String tmpPath = sb.toString().replace('\\', '/');

			try {
				localCopy = (LocalFile) VFS.getManager().resolveFile(tmpPath);
				localCopy.copyFrom(in, new FileTypeSelector(FileType.FILE));
				localCopy.close();
			} catch (FileSystemException exc) {
				exc.printStackTrace();
				throw new Docx4JException(
						"Could not create a temporary local copy", exc);
			} finally {
				if (localCopy != null) {
					try {
						localCopy.delete();
					} catch (FileSystemException exc) {
						exc.printStackTrace();
						log.warn("Couldn't delete temporary file " + tmpPath);
					}
				}
			}
		} else {
			localCopy = (LocalFile) in;
		}

		String localPath = VFSUtils.getLocalFilePath(in);
		if (localPath == null) {
			throw new Docx4JException("Couldn't get local path");
		}

		HWPFDocument doc = new HWPFDocument(new FileInputStream(localPath));

		WordprocessingMLPackage out = WordprocessingMLPackage.createPackage();
		
		convert(doc, out);
		
		return out;
	}

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
				
				org.docx4j.wml.P wmlP =  documentPart.addParagraphOfText(null);				

				if (p.getStyleIndex() > 0) {
					log.debug("Styled paragraph, with index: " + p.getStyleIndex());
					log.debug(stylesheet
							.getStyleDescription(p.getStyleIndex()).getName());
					
					// TODO - HIGH PRIORITY - attach paragraph styles 
				}
								
                if (p.isInTable()) {
					Table t = s.getTable(p);
					int cl = numCol(t);

					log.info("Found " + t.numRows() + "x" + cl
							+ " table - TODO - convert");

					dumpTable(t);
					
					addTODO(factory, wmlP, "[TABLE " + + t.numRows() + "x" + cl 
							+ " - can't convert tables yet]");

					y += t.numParagraphs() - 1;
				}				

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
			}
		}

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

	private static void dumpTable(Table t) {
		for (int i = 0; i < t.numRows(); i++) {
			TableRow tr = t.getRow(i);

			for (int j = 0; j < tr.numCells(); j++) {
				TableCell tc = tr.getCell(j);
				System.out.println("CELL[" + i + "][" + j + "]=" + tc.text());
			}
		}
	}	

	public static void main(String[] args) throws Exception {
				
		String localPath = "/home/dev/TargetFeatureSet.doc";
				
		WordprocessingMLPackage out = convert(new FileInputStream(localPath));
		
		String outputfilepath = "/home/dev/tmp/test-out.docx";		
		
		SaveToZipFile saver = new SaveToZipFile(out);
		saver.save(outputfilepath);
		log.info("Done - saved docx as " + outputfilepath);
		
		
	}	
}
