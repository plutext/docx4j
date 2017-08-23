package org.docx4j.openpackaging.parts;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;

import org.docx4j.openpackaging.exceptions.LocationAwareXMLStreamException;

public abstract class StAXHandlerAbstract implements StAXHandlerInterface {
	
	
	/**
	 * Implement this method; for an example, see VariableReplaceStAX sample
	 * 
	 * @param xmlr
	 * @param writer
	 */
	public abstract void handleCharacters(XMLStreamReader xmlr, XMLStreamWriter writer)  throws XMLStreamException;
		
	
	@Override
	public void handle(XMLStreamReader xmlr,
			XMLStreamWriter xmlWriter) throws LocationAwareXMLStreamException, XMLStreamException {
		
        while (xmlr.hasNext())          
        {   
        	try {
        		write(xmlr, xmlWriter);
        	} catch (XMLStreamException xse) {
//        		System.out.println("line: " + xmlr.getLocation().getLineNumber());
//        		System.out.println("col: " + xmlr.getLocation().getColumnNumber());
        		throw new LocationAwareXMLStreamException(xse.getMessage(), xse, xmlr.getLocation());
        	}
            xmlr.next();
        }
		
		
	}
	
	
	/**
	 * This is designed to cater for the common case of replacing character content.
	 * Everything else is passed through unchanged.
	 * 
	 * If you wanted to find say a w:tbl then handle it with JAXB, you'll need to
	 * override this method and handle START_ELEMENT and END_ELEMENT differently
	 * (keeping track of where you are, so they match).
	 * 
	 * @param xmlr
	 * @param writer
	 * @throws XMLStreamException
	 */
	public void write(XMLStreamReader xmlr, XMLStreamWriter writer)
			throws XMLStreamException {
		
		switch (xmlr.getEventType()) {
		
			case XMLEvent.START_ELEMENT:
				final String localName = xmlr.getLocalName();
	//			System.out.println(localName + " " + xmlr.getNamespaceURI() );
				if (xmlr.getNamespaceURI()==null) {
					writer.writeStartElement(localName);												
				} else {
					writer.writeStartElement(xmlr.getPrefix(), localName, xmlr.getNamespaceURI());
				}
				
	            int namespaceCount = xmlr.getNamespaceCount();
	            for (int i = namespaceCount - 1; i >= 0; i--) {
	            	writer.writeNamespace(xmlr.getNamespacePrefix(i),
	            			xmlr.getNamespaceURI(i));
	            }
	            int attributeCount = xmlr.getAttributeCount();
	            for (int i = 0; i < attributeCount; i++) {
	            	
					if (xmlr.getAttributeNamespace(i)==null) {

		            	writer.writeAttribute(
		            			xmlr.getAttributeLocalName(i),
		            			xmlr.getAttributeValue(i));
						
					} else {
		            	writer.writeAttribute(xmlr.getAttributePrefix(i),
		            			xmlr.getAttributeNamespace(i),
		            			xmlr.getAttributeLocalName(i),
		            			xmlr.getAttributeValue(i));
					}
	            }
	
	            break;
	            
			case XMLEvent.END_ELEMENT:
				writer.writeEndElement();
				break;
			case XMLEvent.SPACE:
				break;
				
			case XMLEvent.CHARACTERS:
				
				handleCharacters( xmlr, writer);	// TODO: include identity implementation		
				break;
				
			case XMLEvent.PROCESSING_INSTRUCTION:
				writer.writeProcessingInstruction(xmlr.getPITarget(),
						xmlr.getPIData());
				break;
			case XMLEvent.CDATA:
				writer.writeCData(xmlr.getText());
				break;
			case XMLEvent.COMMENT:
				writer.writeComment(xmlr.getText());
				break;
			case XMLEvent.ENTITY_REFERENCE:
				writer.writeEntityRef(xmlr.getLocalName());
				break;
			case XMLEvent.START_DOCUMENT:
				String encoding = "UTF-8"; //xmlr.getCharacterEncodingScheme();
						
				String version = xmlr.getVersion();
				if (encoding != null && version != null)
					writer.writeStartDocument(encoding, version);
				else if (version != null)
					writer.writeStartDocument(xmlr.getVersion());
				break;
			case XMLEvent.END_DOCUMENT:
				writer.writeEndDocument();
				break;
		
		}
	}
	


}
