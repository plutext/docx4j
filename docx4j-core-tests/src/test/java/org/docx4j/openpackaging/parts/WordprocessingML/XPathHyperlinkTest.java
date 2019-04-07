package org.docx4j.openpackaging.parts.WordprocessingML;

import static org.junit.Assert.*;

import java.util.List;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.XPathBinderAssociationIsPartialException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.wml.Document;
import org.junit.Test;

public class XPathHyperlinkTest {

	@Test
	public void test() throws JAXBException, Docx4JException {
		
		MainDocumentPart mdp = new MainDocumentPart();
		Document document = (Document)XmlUtils.unmarshalString(openXML);
		mdp.setJaxbElement(document);
		
		List<Object> results = mdp.getJAXBNodesViaXPath("//w:hyperlink", false);
		
		Assert.assertTrue(results.size()==1);
		
		
	}

	static String openXML = "<w:document mc:Ignorable=\"w14 wp14\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\">"
            + "<w:body>"
                + "<w:p>"
                    + "<w:pPr>"
                        + "<w:rPr>"
                            + "<w:lang w:val=\"en-AU\"/>"
                        + "</w:rPr>"
                    + "</w:pPr>"
                    + "<w:r>"
                        + "<w:rPr>"
                            + "<w:lang w:val=\"en-AU\"/>"
                        + "</w:rPr>"
                        + "<w:t>Some content</w:t>"
                    + "</w:r>"
                + "</w:p>"
                + "<w:p>"
                    + "<w:pPr>"
                        + "<w:rPr>"
                            + "<w:lang w:val=\"en-AU\"/>"
                        + "</w:rPr>"
                    + "</w:pPr>"
                    + "<w:r>"
                        + "<w:rPr>"
                            + "<w:lang w:val=\"en-AU\"/>"
                        + "</w:rPr>"
                        + "<w:t>A table</w:t>"
                    + "</w:r>"
                + "</w:p>"
                + "<w:tbl>"
                    + "<w:tblPr>"
                        + "<w:tblStyle w:val=\"TableGrid\"/>"
                        + "<w:tblW w:type=\"auto\" w:w=\"0\"/>"
                        + "<w:tblLook w:firstColumn=\"1\" w:firstRow=\"1\" w:lastColumn=\"0\" w:lastRow=\"0\" w:noHBand=\"0\" w:noVBand=\"1\" w:val=\"04A0\"/>"
                    + "</w:tblPr>"
                    + "<w:tblGrid>"
                        + "<w:gridCol w:w=\"4788\"/>"
                        + "<w:gridCol w:w=\"4788\"/>"
                    + "</w:tblGrid>"
                    + "<w:tr>"
                        + "<w:tc>"
                            + "<w:tcPr>"
                                + "<w:tcW w:type=\"dxa\" w:w=\"4788\"/>"
                            + "</w:tcPr>"
                            + "<w:p>"
                                + "<w:pPr>"
                                    + "<w:rPr>"
                                        + "<w:lang w:val=\"en-AU\"/>"
                                    + "</w:rPr>"
                                + "</w:pPr>"
                            + "</w:p>"
                        + "</w:tc>"
                        + "<w:tc>"
                            + "<w:tcPr>"
                                + "<w:tcW w:type=\"dxa\" w:w=\"4788\"/>"
                            + "</w:tcPr>"
                            + "<w:p>"
                                + "<w:pPr>"
                                    + "<w:rPr>"
                                        + "<w:lang w:val=\"en-AU\"/>"
                                    + "</w:rPr>"
                                + "</w:pPr>"
                                + "<w:r>"
                                    + "<w:rPr>"
                                        + "<w:lang w:val=\"en-AU\"/>"
                                    + "</w:rPr>"
                                    + "<w:t xml:space=\"preserve\">Now, here is the </w:t>"
                                + "</w:r>"
                                + "<w:hyperlink r:id=\"rId5\" w:history=\"1\">"
                                    + "<w:r>"
                                        + "<w:rPr>"
                                            + "<w:rStyle w:val=\"Hyperlink\"/>"
                                            + "<w:lang w:val=\"en-AU\"/>"
                                        + "</w:rPr>"
                                        + "<w:t>StackOverflow question</w:t>"
                                    + "</w:r>"
                                + "</w:hyperlink>"
                                + "<w:r>"
                                    + "<w:rPr>"
                                        + "<w:lang w:val=\"en-AU\"/>"
                                    + "</w:rPr>"
                                    + "<w:t>.</w:t>"
                                + "</w:r>"
                            + "</w:p>"
                        + "</w:tc>"
                    + "</w:tr>"
                    + "<w:tr>"
                        + "<w:tc>"
                            + "<w:tcPr>"
                                + "<w:tcW w:type=\"dxa\" w:w=\"4788\"/>"
                            + "</w:tcPr>"
                            + "<w:p>"
                                + "<w:pPr>"
                                    + "<w:rPr>"
                                        + "<w:lang w:val=\"en-AU\"/>"
                                    + "</w:rPr>"
                                + "</w:pPr>"
                            + "</w:p>"
                        + "</w:tc>"
                        + "<w:tc>"
                            + "<w:tcPr>"
                                + "<w:tcW w:type=\"dxa\" w:w=\"4788\"/>"
                            + "</w:tcPr>"
                            + "<w:p>"
                                + "<w:pPr>"
                                    + "<w:rPr>"
                                        + "<w:lang w:val=\"en-AU\"/>"
                                    + "</w:rPr>"
                                + "</w:pPr>"
                            + "</w:p>"
                        + "</w:tc>"
                    + "</w:tr>"
                + "</w:tbl>"
                + "<w:p>"
                    + "<w:pPr>"
                        + "<w:rPr>"
                            + "<w:lang w:val=\"en-AU\"/>"
                        + "</w:rPr>"
                    + "</w:pPr>"
                + "</w:p>"
                + "<w:sectPr>"
                    + "<w:pgSz w:h=\"15840\" w:w=\"12240\"/>"
                    + "<w:pgMar w:bottom=\"1440\" w:footer=\"708\" w:gutter=\"0\" w:header=\"708\" w:left=\"1440\" w:right=\"1440\" w:top=\"1440\"/>"
                    + "<w:cols w:space=\"708\"/>"
                    + "<w:docGrid w:linePitch=\"360\"/>"
                + "</w:sectPr>"
            + "</w:body>"
        + "</w:document>";
	
}
