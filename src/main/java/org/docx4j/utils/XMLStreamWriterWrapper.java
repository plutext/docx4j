package org.docx4j.utils;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This wrapper drops xmlns="" since OpenXML specifies
 * namespace prefix, and that empty declaration confuses some consumers.
 * 
 * @author jharrop
 *
 */
public class XMLStreamWriterWrapper implements XMLStreamWriter {
	
	protected static Logger log = LoggerFactory.getLogger(XMLStreamWriterWrapper.class);
	

	XMLStreamWriter underlying;
	JaxbXmlPart part;
	
	public XMLStreamWriterWrapper(JaxbXmlPart part, XMLStreamWriter underlying) {
		this.part = part;
		this.underlying = underlying;
	}
	
	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
		underlying.writeStartElement(localName);
		
	}

	@Override
	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		underlying.writeStartElement(namespaceURI, localName);
		
	}

	@Override
	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		underlying.writeStartElement(prefix, localName, namespaceURI);
		
	}

	@Override
	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		underlying.writeEmptyElement(namespaceURI, localName);
		
	}

	@Override
	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		underlying.writeEmptyElement(prefix, localName, namespaceURI);
		
	}

	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
		underlying.writeEmptyElement(localName);
		
	}

	@Override
	public void writeEndElement() throws XMLStreamException {
		underlying.writeEndElement();
		
	}

	@Override
	public void writeEndDocument() throws XMLStreamException {
		underlying.writeEndDocument();
		
	}

	@Override
	public void close() throws XMLStreamException {
		underlying.close();
		
	}

	@Override
	public void flush() throws XMLStreamException {
		underlying.flush();
		
	}

	@Override
	public void writeAttribute(String localName, String value) throws XMLStreamException {
		underlying.writeAttribute(localName, value);
		
	}

	@Override
	public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
			throws XMLStreamException {
		underlying.writeAttribute(prefix, namespaceURI, localName, value);
		
	}

	@Override
	public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
		underlying.writeAttribute(namespaceURI, localName, value);
		
	}

	@Override
	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
		if (namespaceURI==null) {
			log.debug("Dropping null for prefix '" + prefix + "'");			
		} else if (namespaceURI.trim().isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("Dropping empty for prefix '" + prefix + "'");
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Writing {}={}", prefix, namespaceURI);
			}
			underlying.writeNamespace(prefix, namespaceURI);
		}
	}

	@Override
	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
		
		// This isn't used?
		
		if (namespaceURI==null) {
			log.debug("Dropping null ");						
		} else if (namespaceURI.trim().isEmpty()) {
			log.debug("Dropping empty " );
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Writing {}", namespaceURI);
			}
			underlying.writeDefaultNamespace(namespaceURI);
		}
		
	}

	@Override
	public void writeComment(String data) throws XMLStreamException {
		underlying.writeComment(data);
		
	}

	@Override
	public void writeProcessingInstruction(String target) throws XMLStreamException {
		underlying.writeProcessingInstruction(target);
		
	}

	@Override
	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
		underlying.writeProcessingInstruction(target, data);
		
	}

	@Override
	public void writeCData(String data) throws XMLStreamException {
		underlying.writeCData(data);
		
	}

	@Override
	public void writeDTD(String dtd) throws XMLStreamException {
		underlying.writeDTD(dtd);
		
	}

	@Override
	public void writeEntityRef(String name) throws XMLStreamException {
		underlying.writeEntityRef(name);
		
	}
	
	@Override
	public void writeStartDocument() throws XMLStreamException {
		// Sun/Oracle JAXB always invokes this
		log.debug("writeStartDocument ");
		writeStartDocument("UTF-8", "1.0");
		
	}

	@Override
	public void writeStartDocument(String version) throws XMLStreamException {
		// MOXy always invokes this
		log.debug("writeStartDocument " + version);
		writeStartDocument("UTF-8", version);
		
	}

	@Override
	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		log.debug("writeStartDocument " + encoding);
		underlying.writeStartDocument(encoding, version);
		
	}

	@Override
	public void writeCharacters(String text) throws XMLStreamException {
		underlying.writeCharacters(text);
		
	}

	@Override
	public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
		underlying.writeCharacters(text, start, len);
		
	}

	@Override
	public String getPrefix(String uri) throws XMLStreamException {
		return underlying.getPrefix(uri);
	}

	@Override
	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		underlying.setPrefix(prefix, uri);
		
	}

	@Override
	public void setDefaultNamespace(String uri) throws XMLStreamException {
		underlying.setDefaultNamespace(uri);
		
	}

	@Override
	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
		underlying.setNamespaceContext(context);
		
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return underlying.getNamespaceContext();
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		return underlying.getProperty(name);
	}

}
