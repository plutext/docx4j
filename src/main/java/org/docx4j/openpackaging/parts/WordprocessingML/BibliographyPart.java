package org.docx4j.openpackaging.parts.WordprocessingML;

import javax.xml.bind.JAXBContext;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.opendope.JaxbCustomXmlDataStoragePart;

public class BibliographyPart extends JaxbCustomXmlDataStoragePart<org.docx4j.bibliography.CTSources> {
	
	private static Logger log = Logger.getLogger(BibliographyPart.class);		
	
	public BibliographyPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public BibliographyPart(PartName partName, JAXBContext jc) throws InvalidFormatException {
		super(partName, jc);
		init();
	}
	

}
