package org.docx4j.convert.out.html;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

import javax.xml.bind.JAXBElement;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtContentBlock;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListsToContentControlsTest {

	protected static Logger log = LoggerFactory.getLogger(ListsToContentControls.class); // same logger	

	private static org.docx4j.wml.ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();
	
	
	@Test
	public  void singleList() throws Exception {	
		
		WordprocessingMLPackage wordMLPackage = createPkg();
		
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

		mdp.getContent().add(createUnnumberedP());
		
		mdp.getContent().add(createNumberedP(1,0));
		mdp.getContent().add(createNumberedP(1,0));

		mdp.getContent().add(createUnnumberedP());
		
		//wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_simpleTest.docx"));
		
		ListsToContentControls.process(wordMLPackage);
		
		System.out.println(mdp.getXML());
		
	}
	
	@Test
	public  void singleListTwice() throws Exception {	
		
		WordprocessingMLPackage wordMLPackage = createPkg();
		
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

		mdp.getContent().add(createUnnumberedP());
		
		mdp.getContent().add(createNumberedP(1,0));
		mdp.getContent().add(createNumberedP(1,0));

		mdp.getContent().add(createUnnumberedP());

		mdp.getContent().add(createNumberedP(1,0));
		mdp.getContent().add(createNumberedP(1,0));

		mdp.getContent().add(createUnnumberedP());
		
		//wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_simpleTest.docx"));
		
		ListsToContentControls.process(wordMLPackage);
		
		System.out.println(mdp.getXML());
		
	}	

	@Test
	public  void singleList2Levels() throws Exception {	
		
		WordprocessingMLPackage wordMLPackage = createPkg();
		
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

		mdp.getContent().add(createUnnumberedP());
		
		mdp.getContent().add(createNumberedP(1,0));
		mdp.getContent().add(createNumberedP(1,1)); // nested

		mdp.getContent().add(createUnnumberedP());
		
		//wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_simpleTest.docx"));
		
		ListsToContentControls.process(wordMLPackage);
		
		System.out.println(mdp.getXML());
		
	}	

	@Test
	public  void singleList2LevelsPop() throws Exception {	
		
		WordprocessingMLPackage wordMLPackage = createPkg();
		
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

		mdp.getContent().add(createUnnumberedP());
		
		mdp.getContent().add(createNumberedP(1,0));
		mdp.getContent().add(createNumberedP(1,1)); // nested
		mdp.getContent().add(createNumberedP(1,0)); // then back 

		mdp.getContent().add(createUnnumberedP());
		
		//wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_simpleTest.docx"));
		
		ListsToContentControls.process(wordMLPackage);
		
		System.out.println(mdp.getXML());
		
	}
	
	@Test
	public  void singleListStartLvl2() throws Exception {	
		
		WordprocessingMLPackage wordMLPackage = createPkg();
		
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

		mdp.getContent().add(createUnnumberedP());
		
		mdp.getContent().add(createNumberedP(1,1));
		mdp.getContent().add(createNumberedP(1,0));
		mdp.getContent().add(createNumberedP(1,2));

		mdp.getContent().add(createUnnumberedP());
		
		//wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_simpleTest.docx"));
		
		ListsToContentControls.process(wordMLPackage);
		
		System.out.println(mdp.getXML());
		
	}
	
	@Test
	public  void twoLists() throws Exception {	
		
		WordprocessingMLPackage wordMLPackage = createPkg();
		
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

		mdp.getContent().add(createUnnumberedP());
		
		mdp.getContent().add(createNumberedP(1,0));
		mdp.getContent().add(createNumberedP(2,0));

		mdp.getContent().add(createUnnumberedP());
		
		//wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_simpleTest.docx"));
		
		ListsToContentControls.process(wordMLPackage);
		
		System.out.println(mdp.getXML());
		
	}	
		
	@Test
	public  void existingControl() throws Exception {	
		
		WordprocessingMLPackage wordMLPackage = createPkg();
		
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		SdtBlock sdtBlock = new SdtBlock();
		SdtContentBlock sdtContentBlock = new SdtContentBlock();
		
		sdtBlock.setSdtContent(sdtContentBlock);
		
		mdp.getContent().add(sdtBlock);
		
		sdtBlock.getSdtContent().getContent().add(createUnnumberedP());
		
		sdtBlock.getSdtContent().getContent().add(createNumberedP(1,0));
		sdtBlock.getSdtContent().getContent().add(createNumberedP(1,0));

		sdtBlock.getSdtContent().getContent().add(createUnnumberedP());

		ListsToContentControls.process(wordMLPackage);
		
		System.out.println(mdp.getXML());
		
	}

	@Test
	public  void tableCell() throws Exception {	
		
		WordprocessingMLPackage wordMLPackage = createPkg();
		
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		org.docx4j.wml.ObjectFactory wmlObjectFactory = new org.docx4j.wml.ObjectFactory();

		Tbl tbl = wmlObjectFactory.createTbl(); 
		JAXBElement<org.docx4j.wml.Tbl> tblWrapped = wmlObjectFactory.createCTFtnEdnTbl(tbl); 
		    // Create object for tr
		    Tr tr = wmlObjectFactory.createTr(); 
		    tbl.getContent().add( tr); 
		        // Create object for tc (wrapped in JAXBElement) 
		        Tc tc = wmlObjectFactory.createTc(); 
		        JAXBElement<org.docx4j.wml.Tc> tcWrapped = wmlObjectFactory.createTrTc(tc); 
		        tr.getContent().add( tcWrapped); 		
		
		mdp.getContent().add(tbl);
		
		tc.getContent().add(createUnnumberedP());
		
		tc.getContent().add(createNumberedP(1,0));
		tc.getContent().add(createNumberedP(1,0));

		tc.getContent().add(createUnnumberedP());

		ListsToContentControls.process(wordMLPackage);
		
		System.out.println(mdp.getXML());
		
	}
	

	@Test
	public  void EndToEnd() throws Exception {	
		
		WordprocessingMLPackage wordMLPackage = createPkg();
		
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

		mdp.getContent().add(createUnnumberedP());
		
		mdp.getContent().add(createNumberedP(1,0));
		mdp.getContent().add(createNumberedP(2,0));

		mdp.getContent().add(createUnnumberedP());
		
		//wordMLPackage.save(new File(System.getProperty("user.dir") + "/OUT_simpleTest.docx"));
		
//		ListsToContentControls.process(wordMLPackage);
		
		System.out.println(mdp.getXML());
		
		toHTML( wordMLPackage);
		
	}	
	
	private void toHTML(WordprocessingMLPackage wordMLPackage) throws Docx4JException {

		// Back to XHTML

		HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
		htmlSettings.setOpcPackage(wordMLPackage);

		// Sample sdt tag handler (tag handlers insert specific
		// html depending on the contents of an sdt's tag).
		// This will only have an effect if the sdt tag contains
		// the string class=
		SdtWriter.registerTagHandler("HTML_ELEMENT", new SdtToListSdtTagHandler());	
	
		// output to an OutputStream.
		OutputStream os = new ByteArrayOutputStream();

		// If you want XHTML output
		Docx4jProperties.setProperty("docx4j.Convert.Out.HTML.OutputMethodXML",
				true);
		Docx4J.toHTML(htmlSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);  // TODO NON_XSL implementation

		System.out.println(((ByteArrayOutputStream) os).toString());		
	}
	

	
	protected P createNumberedP(int numId, int ilvl) {
		
		P p = wmlObjectFactory.createP(); 
		    // Create object for pPr
		    PPr ppr = wmlObjectFactory.createPPr(); 
		    p.setPPr(ppr); 
		        // Create object for numPr
		        PPrBase.NumPr pprbasenumpr = wmlObjectFactory.createPPrBaseNumPr(); 
		        ppr.setNumPr(pprbasenumpr); 
		            // Create object for ilvl
		            PPrBase.NumPr.Ilvl pprbasenumprilvl = wmlObjectFactory.createPPrBaseNumPrIlvl(); 
		            pprbasenumpr.setIlvl(pprbasenumprilvl); 
		                pprbasenumprilvl.setVal( BigInteger.valueOf( ilvl) ); 
		            // Create object for numId
		            PPrBase.NumPr.NumId pprbasenumprnumid = wmlObjectFactory.createPPrBaseNumPrNumId(); 
		            pprbasenumpr.setNumId(pprbasenumprnumid); 
		                pprbasenumprnumid.setVal( BigInteger.valueOf( numId) ); 
		        // Create object for pStyle
//		        PPrBase.PStyle pprbasepstyle = wmlObjectFactory.createPPrBasePStyle(); 
//		        ppr.setPStyle(pprbasepstyle); 
//		            pprbasepstyle.setVal( "ListParagraph"); 
		    // Create object for r
		    R r = wmlObjectFactory.createR(); 
		    p.getContent().add( r); 
		        // Create object for t (wrapped in JAXBElement) 
		        Text text = wmlObjectFactory.createText(); 
		        JAXBElement<org.docx4j.wml.Text> textWrapped = wmlObjectFactory.createRT(text); 
		        r.getContent().add( textWrapped); 
		            text.setValue( "list " + numId + ", ilvl " + ilvl); 

		return p;
		}
	
	protected P createUnnumberedP() {
		
		P p = wmlObjectFactory.createP(); 
		    R r = wmlObjectFactory.createR(); 
		    p.getContent().add( r); 
		        // Create object for t (wrapped in JAXBElement) 
		        Text text = wmlObjectFactory.createText(); 
		        JAXBElement<org.docx4j.wml.Text> textWrapped = wmlObjectFactory.createRT(text); 
		        r.getContent().add( textWrapped); 
		            text.setValue( "UnnumberedP"); 

		return p;
		}	
	
	protected WordprocessingMLPackage createPkg() throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		ndp.setContents((Numbering)XmlUtils.unmarshalString(numbering));
		
		wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
		
		return wordMLPackage;
	}
	
	private static final String numbering = "<w:numbering mc:Ignorable=\"w14 wp14\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\">"
			// Ordered list
            + "<w:abstractNum w:abstractNumId=\"0\">"
                + "<w:nsid w:val=\"069653C8\"/>"
                + "<w:multiLevelType w:val=\"multilevel\"/>"
                + "<w:tmpl w:val=\"0C09001D\"/>"
                + "<w:lvl w:ilvl=\"0\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"decimal\"/>"
                    + "<w:lvlText w:val=\"%1)\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"360\"/>"
                    + "</w:pPr>"
                + "</w:lvl>"
                + "<w:lvl w:ilvl=\"1\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"lowerLetter\"/>"
                    + "<w:lvlText w:val=\"%2)\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"720\"/>"
                    + "</w:pPr>"
                + "</w:lvl>"
                + "<w:lvl w:ilvl=\"2\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"lowerRoman\"/>"
                    + "<w:lvlText w:val=\"%3)\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"1080\"/>"
                    + "</w:pPr>"
                + "</w:lvl>"
                + "<w:lvl w:ilvl=\"3\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"decimal\"/>"
                    + "<w:lvlText w:val=\"(%4)\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"1440\"/>"
                    + "</w:pPr>"
                + "</w:lvl>"
                + "<w:lvl w:ilvl=\"4\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"lowerLetter\"/>"
                    + "<w:lvlText w:val=\"(%5)\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"1800\"/>"
                    + "</w:pPr>"
                + "</w:lvl>"
                + "<w:lvl w:ilvl=\"5\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"lowerRoman\"/>"
                    + "<w:lvlText w:val=\"(%6)\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"2160\"/>"
                    + "</w:pPr>"
                + "</w:lvl>"
                + "<w:lvl w:ilvl=\"6\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"decimal\"/>"
                    + "<w:lvlText w:val=\"%7.\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"2520\"/>"
                    + "</w:pPr>"
                + "</w:lvl>"
                + "<w:lvl w:ilvl=\"7\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"lowerLetter\"/>"
                    + "<w:lvlText w:val=\"%8.\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"2880\"/>"
                    + "</w:pPr>"
                + "</w:lvl>"
                + "<w:lvl w:ilvl=\"8\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"lowerRoman\"/>"
                    + "<w:lvlText w:val=\"%9.\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"3240\"/>"
                    + "</w:pPr>"
                + "</w:lvl>"
            + "</w:abstractNum>"
            // Unordered (bulleted) list
            + "<w:abstractNum w:abstractNumId=\"1\">"
                + "<w:nsid w:val=\"38572E21\"/>"
                + "<w:multiLevelType w:val=\"multilevel\"/>"
                + "<w:tmpl w:val=\"0C090021\"/>"
                + "<w:lvl w:ilvl=\"0\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"bullet\"/>"
                    + "<w:lvlText w:val=\"\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"360\"/>"
                    + "</w:pPr>"
                    + "<w:rPr>"
                        + "<w:rFonts w:ascii=\"Wingdings\" w:hAnsi=\"Wingdings\" w:hint=\"default\"/>"
                    + "</w:rPr>"
                + "</w:lvl>"
                + "<w:lvl w:ilvl=\"1\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"bullet\"/>"
                    + "<w:lvlText w:val=\"\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"720\"/>"
                    + "</w:pPr>"
                    + "<w:rPr>"
                        + "<w:rFonts w:ascii=\"Wingdings\" w:hAnsi=\"Wingdings\" w:hint=\"default\"/>"
                    + "</w:rPr>"
                + "</w:lvl>"
                + "<w:lvl w:ilvl=\"2\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"bullet\"/>"
                    + "<w:lvlText w:val=\"\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"1080\"/>"
                    + "</w:pPr>"
                    + "<w:rPr>"
                        + "<w:rFonts w:ascii=\"Wingdings\" w:hAnsi=\"Wingdings\" w:hint=\"default\"/>"
                    + "</w:rPr>"
                + "</w:lvl>"
                + "<w:lvl w:ilvl=\"3\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"bullet\"/>"
                    + "<w:lvlText w:val=\"\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"1440\"/>"
                    + "</w:pPr>"
                    + "<w:rPr>"
                        + "<w:rFonts w:ascii=\"Symbol\" w:hAnsi=\"Symbol\" w:hint=\"default\"/>"
                    + "</w:rPr>"
                + "</w:lvl>"
                + "<w:lvl w:ilvl=\"4\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"bullet\"/>"
                    + "<w:lvlText w:val=\"\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"1800\"/>"
                    + "</w:pPr>"
                    + "<w:rPr>"
                        + "<w:rFonts w:ascii=\"Symbol\" w:hAnsi=\"Symbol\" w:hint=\"default\"/>"
                    + "</w:rPr>"
                + "</w:lvl>"
                + "<w:lvl w:ilvl=\"5\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"bullet\"/>"
                    + "<w:lvlText w:val=\"\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"2160\"/>"
                    + "</w:pPr>"
                    + "<w:rPr>"
                        + "<w:rFonts w:ascii=\"Wingdings\" w:hAnsi=\"Wingdings\" w:hint=\"default\"/>"
                    + "</w:rPr>"
                + "</w:lvl>"
                + "<w:lvl w:ilvl=\"6\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"bullet\"/>"
                    + "<w:lvlText w:val=\"\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"2520\"/>"
                    + "</w:pPr>"
                    + "<w:rPr>"
                        + "<w:rFonts w:ascii=\"Wingdings\" w:hAnsi=\"Wingdings\" w:hint=\"default\"/>"
                    + "</w:rPr>"
                + "</w:lvl>"
                + "<w:lvl w:ilvl=\"7\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"bullet\"/>"
                    + "<w:lvlText w:val=\"\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"2880\"/>"
                    + "</w:pPr>"
                    + "<w:rPr>"
                        + "<w:rFonts w:ascii=\"Symbol\" w:hAnsi=\"Symbol\" w:hint=\"default\"/>"
                    + "</w:rPr>"
                + "</w:lvl>"
                + "<w:lvl w:ilvl=\"8\">"
                    + "<w:start w:val=\"1\"/>"
                    + "<w:numFmt w:val=\"bullet\"/>"
                    + "<w:lvlText w:val=\"\"/>"
                    + "<w:lvlJc w:val=\"left\"/>"
                    + "<w:pPr>"
                        + "<w:ind w:hanging=\"360\" w:left=\"3240\"/>"
                    + "</w:pPr>"
                    + "<w:rPr>"
                        + "<w:rFonts w:ascii=\"Symbol\" w:hAnsi=\"Symbol\" w:hint=\"default\"/>"
                    + "</w:rPr>"
                + "</w:lvl>"
            + "</w:abstractNum>"
            + "<w:num w:numId=\"1\">"
                + "<w:abstractNumId w:val=\"0\"/>"
            + "</w:num>"
            + "<w:num w:numId=\"2\">"
                + "<w:abstractNumId w:val=\"1\"/>"
            + "</w:num>"
        + "</w:numbering>";

}
