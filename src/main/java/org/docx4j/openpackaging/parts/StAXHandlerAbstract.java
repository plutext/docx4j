package org.docx4j.openpackaging.parts;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;

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
			XMLStreamWriter xmlWriter) throws XMLStreamException {
		
        while (xmlr.hasNext())          
        {               
            write(xmlr, xmlWriter);             
            xmlr.next();
        }
		
		
	}
	
	
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
	            	writer.writeAttribute(xmlr.getAttributePrefix(i),
	            			xmlr.getAttributeNamespace(i),
	            			xmlr.getAttributeLocalName(i),
	            			xmlr.getAttributeValue(i));
	            }
	
	            break;
	            
			case XMLEvent.END_ELEMENT:
				writer.writeEndElement();
				break;
			case XMLEvent.SPACE:
				break;
				
			case XMLEvent.CHARACTERS:
				
				handleCharacters( xmlr, writer);			
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
				String encoding = xmlr.getCharacterEncodingScheme();
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
