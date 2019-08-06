package org.docx4j.convert.out.fo;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;

import org.docx4j.org.apache.xpath.XPathAPI;
import org.docx4j.XmlUtils;
import org.docx4j.fonts.RunFontSelector;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RPr;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;


@Ignore  // TODO work in progress
public class CreateBlockTest {

	@Test
	public void test() throws SAXException, IOException, TransformerException, InvalidFormatException {
		
		/* Here we want to test white space handling in 
		 * 	protected static DocumentFragment createBlock(WordprocessingMLPackage wmlPackage, RunFontSelector runFontSelector, NodeIterator childResults,
			boolean sdt, PPr pPrDirect, PPr pPr, RPr rPr, RPr rPrParagraphMark) 
			
			
		 */
		
		// We need NodeIterator, simulating the results from XSLT
		File f = new File("src/test/java/org/docx4j/convert/out/fo/nodes.fo");
		DocumentBuilder db = XmlUtils.getNewDocumentBuilder();
		Document doc = db.parse(f);
		NodeIterator childResults = XPathAPI.selectNodeIterator(doc, "/*");

		//System.out.println(childResults.nextNode().getNodeName());
		
		// Other params don't matter
		FOConversionContext context = null; 
		String pStyleVal = "styleX";
		boolean sdt = false;
		PPr pPrDirect = new PPr();
		PPr pPr = new PPr(); 
		RPr rPr = new RPr();
		RPr rPrParagraphMark  = new RPr();
		
		WordprocessingMLPackage wmlPackage = WordprocessingMLPackage.createPackage();
		RunFontSelector runFontSelector = FOConversionContext.createRunFontSelector(wmlPackage);
		
		DocumentFragment df = XsltFOFunctions.createBlock( wmlPackage, runFontSelector,  pStyleVal,  childResults,
				 sdt,  pPrDirect,  pPr,  rPr,  rPrParagraphMark);
		
		System.out.println(XmlUtils.w3CDomNodeToString(df));
		
	}

}
