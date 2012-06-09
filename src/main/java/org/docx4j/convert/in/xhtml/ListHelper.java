/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.convert.in.xhtml;

import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.Numbering;

public class ListHelper {
	
	Numbering.Num orderedList;
	Numbering.Num unorderedList;
	
	protected Numbering.Num getOrderedList(NumberingDefinitionsPart ndp) throws JAXBException {
		
		if (orderedList==null) {
			orderedList = 
					ndp.addAbstractListNumberingDefinition(
							(Numbering.AbstractNum)XmlUtils.unmarshalString(olList));
		}
		return orderedList;
	}
	
	protected Numbering.Num getUnorderedList(NumberingDefinitionsPart ndp) throws JAXBException {
		
		if (unorderedList==null) {
			unorderedList = 
					ndp.addAbstractListNumberingDefinition(
							(Numbering.AbstractNum)XmlUtils.unmarshalString(ulList));
		}
		return unorderedList;
	}
	
	
	static final String ulList = 
			  "<w:abstractNum w:abstractNumId=\"0\"  xmlns:ve=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" xmlns:w10=\"urn:schemas-microsoft-com:office:word\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:wne=\"http://schemas.microsoft.com/office/word/2006/wordml\">"
			    +"<w:nsid w:val=\"16892FB7\"/>"
			    +"<w:multiLevelType w:val=\"hybridMultilevel\"/>"
			    +"<w:tmpl w:val=\"5A4EB96A\"/>"
			    +"<w:lvl w:ilvl=\"0\" w:tplc=\"0C090001\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"bullet\"/>"
			      +"<w:lvlText w:val=\"\"/>"
			      +"<w:lvlJc w:val=\"left\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"720\" w:hanging=\"360\"/>"
			      +"</w:pPr>"
			      +"<w:rPr>"
			        +"<w:rFonts w:ascii=\"Symbol\" w:hAnsi=\"Symbol\" w:hint=\"default\"/>"
			      +"</w:rPr>"
			    +"</w:lvl>"
			    +"<w:lvl w:ilvl=\"1\" w:tplc=\"0C090003\" w:tentative=\"1\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"bullet\"/>"
			      +"<w:lvlText w:val=\"o\"/>"
			      +"<w:lvlJc w:val=\"left\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"1440\" w:hanging=\"360\"/>"
			      +"</w:pPr>"
			      +"<w:rPr>"
			        +"<w:rFonts w:ascii=\"Courier New\" w:hAnsi=\"Courier New\" w:cs=\"Courier New\" w:hint=\"default\"/>"
			      +"</w:rPr>"
			    +"</w:lvl>"
			    +"<w:lvl w:ilvl=\"2\" w:tplc=\"0C090005\" w:tentative=\"1\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"bullet\"/>"
			      +"<w:lvlText w:val=\"\"/>"
			      +"<w:lvlJc w:val=\"left\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"2160\" w:hanging=\"360\"/>"
			      +"</w:pPr>"
			      +"<w:rPr>"
			        +"<w:rFonts w:ascii=\"Wingdings\" w:hAnsi=\"Wingdings\" w:hint=\"default\"/>"
			      +"</w:rPr>"
			    +"</w:lvl>"
			    +"<w:lvl w:ilvl=\"3\" w:tplc=\"0C090001\" w:tentative=\"1\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"bullet\"/>"
			      +"<w:lvlText w:val=\"\"/>"
			      +"<w:lvlJc w:val=\"left\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"2880\" w:hanging=\"360\"/>"
			      +"</w:pPr>"
			      +"<w:rPr>"
			        +"<w:rFonts w:ascii=\"Symbol\" w:hAnsi=\"Symbol\" w:hint=\"default\"/>"
			      +"</w:rPr>"
			    +"</w:lvl>"
			    +"<w:lvl w:ilvl=\"4\" w:tplc=\"0C090003\" w:tentative=\"1\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"bullet\"/>"
			      +"<w:lvlText w:val=\"o\"/>"
			      +"<w:lvlJc w:val=\"left\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"3600\" w:hanging=\"360\"/>"
			      +"</w:pPr>"
			      +"<w:rPr>"
			        +"<w:rFonts w:ascii=\"Courier New\" w:hAnsi=\"Courier New\" w:cs=\"Courier New\" w:hint=\"default\"/>"
			      +"</w:rPr>"
			    +"</w:lvl>"
			    +"<w:lvl w:ilvl=\"5\" w:tplc=\"0C090005\" w:tentative=\"1\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"bullet\"/>"
			      +"<w:lvlText w:val=\"\"/>"
			      +"<w:lvlJc w:val=\"left\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"4320\" w:hanging=\"360\"/>"
			      +"</w:pPr>"
			      +"<w:rPr>"
			        +"<w:rFonts w:ascii=\"Wingdings\" w:hAnsi=\"Wingdings\" w:hint=\"default\"/>"
			      +"</w:rPr>"
			    +"</w:lvl>"
			    +"<w:lvl w:ilvl=\"6\" w:tplc=\"0C090001\" w:tentative=\"1\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"bullet\"/>"
			      +"<w:lvlText w:val=\"\"/>"
			      +"<w:lvlJc w:val=\"left\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"5040\" w:hanging=\"360\"/>"
			      +"</w:pPr>"
			      +"<w:rPr>"
			        +"<w:rFonts w:ascii=\"Symbol\" w:hAnsi=\"Symbol\" w:hint=\"default\"/>"
			      +"</w:rPr>"
			    +"</w:lvl>"
			    +"<w:lvl w:ilvl=\"7\" w:tplc=\"0C090003\" w:tentative=\"1\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"bullet\"/>"
			      +"<w:lvlText w:val=\"o\"/>"
			      +"<w:lvlJc w:val=\"left\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"5760\" w:hanging=\"360\"/>"
			      +"</w:pPr>"
			      +"<w:rPr>"
			        +"<w:rFonts w:ascii=\"Courier New\" w:hAnsi=\"Courier New\" w:cs=\"Courier New\" w:hint=\"default\"/>"
			      +"</w:rPr>"
			    +"</w:lvl>"
			    +"<w:lvl w:ilvl=\"8\" w:tplc=\"0C090005\" w:tentative=\"1\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"bullet\"/>"
			      +"<w:lvlText w:val=\"\"/>"
			      +"<w:lvlJc w:val=\"left\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"6480\" w:hanging=\"360\"/>"
			      +"</w:pPr>"
			      +"<w:rPr>"
			        +"<w:rFonts w:ascii=\"Wingdings\" w:hAnsi=\"Wingdings\" w:hint=\"default\"/>"
			      +"</w:rPr>"
			    +"</w:lvl>"
			  +"</w:abstractNum>";
	
	static final String olList = 
			  "<w:abstractNum w:abstractNumId=\"1\"   xmlns:ve=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" xmlns:w10=\"urn:schemas-microsoft-com:office:word\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:wne=\"http://schemas.microsoft.com/office/word/2006/wordml\">"
			    +"<w:nsid w:val=\"7E706046\"/>"
			    +"<w:multiLevelType w:val=\"hybridMultilevel\"/>"
			    +"<w:tmpl w:val=\"336E8F2C\"/>"
			    +"<w:lvl w:ilvl=\"0\" w:tplc=\"0C09000F\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"decimal\"/>"
			      +"<w:lvlText w:val=\"%1.\"/>"
			      +"<w:lvlJc w:val=\"left\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"720\" w:hanging=\"360\"/>"
			      +"</w:pPr>"
			    +"</w:lvl>"
			    +"<w:lvl w:ilvl=\"1\" w:tplc=\"0C090019\" w:tentative=\"1\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"lowerLetter\"/>"
			      +"<w:lvlText w:val=\"%2.\"/>"
			      +"<w:lvlJc w:val=\"left\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"1440\" w:hanging=\"360\"/>"
			      +"</w:pPr>"
			    +"</w:lvl>"
			    +"<w:lvl w:ilvl=\"2\" w:tplc=\"0C09001B\" w:tentative=\"1\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"lowerRoman\"/>"
			      +"<w:lvlText w:val=\"%3.\"/>"
			      +"<w:lvlJc w:val=\"right\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"2160\" w:hanging=\"180\"/>"
			      +"</w:pPr>"
			    +"</w:lvl>"
			    +"<w:lvl w:ilvl=\"3\" w:tplc=\"0C09000F\" w:tentative=\"1\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"decimal\"/>"
			      +"<w:lvlText w:val=\"%4.\"/>"
			      +"<w:lvlJc w:val=\"left\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"2880\" w:hanging=\"360\"/>"
			      +"</w:pPr>"
			    +"</w:lvl>"
			    +"<w:lvl w:ilvl=\"4\" w:tplc=\"0C090019\" w:tentative=\"1\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"lowerLetter\"/>"
			      +"<w:lvlText w:val=\"%5.\"/>"
			      +"<w:lvlJc w:val=\"left\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"3600\" w:hanging=\"360\"/>"
			      +"</w:pPr>"
			    +"</w:lvl>"
			    +"<w:lvl w:ilvl=\"5\" w:tplc=\"0C09001B\" w:tentative=\"1\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"lowerRoman\"/>"
			      +"<w:lvlText w:val=\"%6.\"/>"
			      +"<w:lvlJc w:val=\"right\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"4320\" w:hanging=\"180\"/>"
			      +"</w:pPr>"
			    +"</w:lvl>"
			    +"<w:lvl w:ilvl=\"6\" w:tplc=\"0C09000F\" w:tentative=\"1\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"decimal\"/>"
			      +"<w:lvlText w:val=\"%7.\"/>"
			      +"<w:lvlJc w:val=\"left\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"5040\" w:hanging=\"360\"/>"
			      +"</w:pPr>"
			    +"</w:lvl>"
			    +"<w:lvl w:ilvl=\"7\" w:tplc=\"0C090019\" w:tentative=\"1\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"lowerLetter\"/>"
			      +"<w:lvlText w:val=\"%8.\"/>"
			      +"<w:lvlJc w:val=\"left\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"5760\" w:hanging=\"360\"/>"
			      +"</w:pPr>"
			    +"</w:lvl>"
			    +"<w:lvl w:ilvl=\"8\" w:tplc=\"0C09001B\" w:tentative=\"1\">"
			      +"<w:start w:val=\"1\"/>"
			      +"<w:numFmt w:val=\"lowerRoman\"/>"
			      +"<w:lvlText w:val=\"%9.\"/>"
			      +"<w:lvlJc w:val=\"right\"/>"
			      +"<w:pPr>"
			        +"<w:ind w:left=\"6480\" w:hanging=\"180\"/>"
			      +"</w:pPr>"
			    +"</w:lvl>"
			  +"</w:abstractNum>";
	
//			  +"<w:num w:numId=\"1\">"
//			    +"<w:abstractNumId w:val=\"1\"/>"
//			  +"</w:num>"
//			  +"<w:num w:numId=\"2\">"
//			    +"<w:abstractNumId w:val=\"0\"/>"
//			  +"</w:num>"
//			+"</w:numbering>";
	

}
