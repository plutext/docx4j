package org.docx4j.openpackaging.parts.WordprocessingML;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.bibliography.CTSourceType;
import org.docx4j.bibliography.CTSources;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.opendope.JaxbCustomXmlDataStoragePart;

/**
 * @since 2.7
 */
public class BibliographyPart extends JaxbCustomXmlDataStoragePart<JAXBElement<org.docx4j.bibliography.CTSources>> {
	
	private static Logger log = LoggerFactory.getLogger(BibliographyPart.class);		

	public BibliographyPart() throws InvalidFormatException {
		super(new PartName("/customXml/item1.xml"));
		init();
	}
	
	
	public BibliographyPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public BibliographyPart(PartName partName, JAXBContext jc) throws InvalidFormatException {
		super(partName, jc);
		init();
	}
	
	public void importSources(BibliographyPart otherPart) {
		
		org.docx4j.bibliography.CTSources ourSources = (CTSources)XmlUtils.unwrap(this.getJaxbElement());
		
		org.docx4j.bibliography.CTSources otherSourcesTmp = (CTSources)XmlUtils.unwrap(otherPart.getJaxbElement());		
		org.docx4j.bibliography.CTSources otherSourcesCloned = XmlUtils.deepCopy(otherSourcesTmp);
		
		for (CTSourceType sourceType : otherSourcesCloned.getSource()) {
		
			// TODO duplicate detection.
			
			ourSources.getSource().add(sourceType);
		}
	}

}
