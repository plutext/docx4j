package org.docx4j.samples;

import java.io.File;

import ae.javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Body;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.Text;

public class AddTOC {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			
		        ObjectFactory factory = Context.getWmlObjectFactory();
		        
		        P p = factory.createP();		       
		        R r = factory.createR();

		        FldChar fldchar = factory.createFldChar();
		        fldchar.setFldCharType(STFldCharType.BEGIN);
		        fldchar.setDirty(true);
		        r.getRunContent().add(getWrappedFldChar(fldchar));
		        p.getParagraphContent().add(r);

		        R r1 = factory.createR();
		        Text txt = new Text();
		        txt.setSpace("preserve");
		        txt.setValue("TOC \\o \"1-3\" \\h \\z \\u \\h");
		        r.getRunContent().add(factory.createRInstrText(txt) );
		        p.getParagraphContent().add(r1);

		        FldChar fldcharend = factory.createFldChar();
		        fldcharend.setFldCharType(STFldCharType.END);
		        R r2 = factory.createR();
		        r2.getRunContent().add(getWrappedFldChar(fldcharend));
		        p.getParagraphContent().add(r2);
		        
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
			MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
			
			org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();
			Body body =  wmlDocumentEl.getBody();
			body.getEGBlockLevelElts().add(p);
			
			documentPart.addStyledParagraphOfText("Heading1", "Hello 1");
			documentPart.addStyledParagraphOfText("Heading2", "Hello 2");
			documentPart.addStyledParagraphOfText("Heading3", "Hello 3");
			documentPart.addStyledParagraphOfText("Heading1", "Hello 1");
			
			wordMLPackage.save(new File("c:\\tmpxml\\AddTOC.docx"));
			
			
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static JAXBElement getWrappedFldChar(FldChar fldchar) {
		
		return new JAXBElement( new QName(Namespaces.NS_WORD12, "fldChar"), 
				FldChar.class, fldchar);
		
	}
	
	
}
