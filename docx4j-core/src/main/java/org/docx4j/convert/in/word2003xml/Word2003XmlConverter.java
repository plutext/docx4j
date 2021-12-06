/**
 * 
 */
package org.docx4j.convert.in.word2003xml;

import java.io.File;
import java.io.IOException;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.util.JAXBResult;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.Numbering.AbstractNum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple proof of concept of
 * converting Word 2003 XML to ECMA 376 docx.
 * 
 * @author jharrop
 * @since 3.0.0
 */
public class Word2003XmlConverter {
	
	private static Logger log = LoggerFactory.getLogger(Word2003XmlConverter.class);
	
	static Templates xslt;	
	
	private Transition03To06 transitionContainer;
		
	static {
		try {
			// XmlUtils.getTransformerFactory().setURIResolver(new OutHtmlURIResolver());
			// TODO FIXME - not thread safe, which would be an issue
			
			Source xsltSource = new StreamSource(org.docx4j.utils.ResourceUtils.getResource(
								"org/docx4j/convert/in/word2003xml/2003-import.xslt"));
			xslt = XmlUtils.getTransformerTemplate(xsltSource);
			
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Couldn't setup 2003-import.xslt", e);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			log.error("Couldn't setup 2003-import.xslt", e);
		}
	}	

	public Word2003XmlConverter(Source source) throws JAXBException, Docx4JException {
		
		// Use 2003-import.xsl to convert to a Transition03To06 object
		java.lang.ClassLoader classLoader = Word2003XmlConverter.class.getClassLoader();		
		
		JAXBResult result = new JAXBResult(
		         JAXBContext.newInstance("org.docx4j.convert.in.word2003xml:org.docx4j.w15", classLoader) );
		XmlUtils.transform(source, xslt, null, result);
		
		// set the unmarshalled content tree
		transitionContainer = (Transition03To06)result.getResult();
	}

	/**
	 * Get the new docx.  Will be made public if/when this code is mature enough. 
	 * @return
	 */
	public WordprocessingMLPackage getWordprocessingMLPackage() {
		
		return getWordprocessingMLPackage(false);
	}
	
	private WordprocessingMLPackage getWordprocessingMLPackage(boolean mainDocOnly) {
		
		WordprocessingMLPackage wordMLPackage=null;
		try {
			wordMLPackage = WordprocessingMLPackage.createPackage();
		} catch (InvalidFormatException e) {}
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		// Main Document Part
		mdp.getJaxbElement().setBody(transitionContainer.getBody());
		
		// DEBUGGING: if Word can't open the resulting docx,
		// a process for working out why is to
		// make sure it works with just the main document part,
		// then each of the following 3 parts, one by one.
		// What you need to do is to compare the XSLT output for the part
		// (XmlUtils.marshaltoString for the relevant part is usually 
		//  enough) to what ECMA 376 requires.
		if (!mainDocOnly) {
		
			// Styles
			mdp.getStyleDefinitionsPart(true).setJaxbElement(transitionContainer.getStyles());
			
			// Numbering
			try {
				NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
				ndp.setJaxbElement(transitionContainer.getNumbering());
				mdp.addTargetPart(ndp);
				
				// fix attributes
				// <w:multiLevelType w:val="Multilevel"/> should start with lower case
				for (AbstractNum anum : ndp.getJaxbElement().getAbstractNum()) {
					if (anum.getMultiLevelType()==null) continue;
					String multiLevelType = anum.getMultiLevelType().getVal();
					multiLevelType = multiLevelType.substring(0, 1).toLowerCase() + multiLevelType.substring(1);
					anum.getMultiLevelType().setVal(multiLevelType);
				}
				
			} catch (InvalidFormatException e) {}
			
			// Fonts
			try {
				FontTablePart fontsPart = new FontTablePart();
				fontsPart.setJaxbElement(transitionContainer.getFonts());
								
				mdp.addTargetPart(fontsPart);
			} catch (InvalidFormatException e) {}
		}
		
		return wordMLPackage;
		
	}



}
