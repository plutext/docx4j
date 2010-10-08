package org.docx4j.openpackaging.parts.opendope;

import javax.xml.bind.JAXBContext;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

public abstract class JaxbCustomXmlDataStoragePart<E> extends JaxbXmlPart<E> {
	
	public JaxbCustomXmlDataStoragePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public JaxbCustomXmlDataStoragePart(PartName partName, JAXBContext jc) throws InvalidFormatException {
		super(partName, jc);
		init();
	}
	
	public void init() {		
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_CUSTOMXML_DATASTORAGE));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.CUSTOM_XML_DATA_STORAGE);
		
	}
	

}
