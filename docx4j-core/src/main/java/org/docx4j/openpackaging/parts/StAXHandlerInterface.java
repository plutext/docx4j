package org.docx4j.openpackaging.parts;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.docx4j.openpackaging.exceptions.LocationAwareXMLStreamException;

public interface StAXHandlerInterface {
	
	public void handle(XMLStreamReader xmlr, XMLStreamWriter xmlWriter) throws LocationAwareXMLStreamException, XMLStreamException ;

}
