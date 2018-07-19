package org.docx4j.utils;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLStreamReaderWrapper implements XMLStreamReader {
	
	protected static Logger log = LoggerFactory.getLogger(XMLStreamReaderWrapper.class);
	

	XMLStreamReader underlying;
	JaxbXmlPart part;
	
	public XMLStreamReaderWrapper(JaxbXmlPart part, XMLStreamReader underlying) {
		this.part = part;
		this.underlying = underlying;
	}
	

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {

		return underlying.getProperty(name);
	}

	@Override
	public int next() throws XMLStreamException {

		return underlying.next();
	}

	@Override
	public void require(int type, String namespaceURI, String localName) throws XMLStreamException {

		underlying.require(type, namespaceURI, localName);

	}

	@Override
	public String getElementText() throws XMLStreamException {

		return underlying.getElementText();
	}

	@Override
	public int nextTag() throws XMLStreamException {

		return underlying.nextTag();
	}

	@Override
	public boolean hasNext() throws XMLStreamException {

		return underlying.hasNext();
	}

	@Override
	public void close() throws XMLStreamException {

		underlying.close();
	}

	@Override
	public String getNamespaceURI(String prefix) {

		return underlying.getNamespaceURI(prefix);
	}

	@Override
	public boolean isStartElement() {

		return underlying.isStartElement();
	}

	@Override
	public boolean isEndElement() {

		return underlying.isEndElement();
	}

	@Override
	public boolean isCharacters() {

		return underlying.isCharacters();
	}

	@Override
	public boolean isWhiteSpace() {

		return underlying.isWhiteSpace();
	}

	@Override
	public String getAttributeValue(String namespaceURI, String localName) {

		return underlying.getAttributeValue(namespaceURI, localName);
	}

	@Override
	public int getAttributeCount() {

		return underlying.getAttributeCount();
	}

	@Override
	public QName getAttributeName(int index) {

		return underlying.getAttributeName(index);
	}

	@Override
	public String getAttributeNamespace(int index) {

		return underlying.getAttributeNamespace(index);
	}

	@Override
	public String getAttributeLocalName(int index) {

		return underlying.getAttributeLocalName(index);
	}

	@Override
	public String getAttributePrefix(int index) {

		return underlying.getAttributePrefix(index);
	}

	@Override
	public String getAttributeType(int index) {

		return underlying.getLocalName();
	}

	@Override
	public String getAttributeValue(int index) {

		return underlying.getAttributeValue(index);
	}

	@Override
	public boolean isAttributeSpecified(int index) {

		return underlying.isAttributeSpecified(index);
	}

	@Override
	public int getNamespaceCount() {

		return underlying.getNamespaceCount();
	}

	@Override
	public String getNamespacePrefix(int index) {

		return underlying.getNamespacePrefix(index);
	}

	@Override
	public String getNamespaceURI(int index) {

		return underlying.getNamespaceURI(index);
	}

	@Override
	public NamespaceContext getNamespaceContext() {

		return underlying.getNamespaceContext();
	}

	@Override
	public int getEventType() {

		return underlying.getEventType();
	}

	@Override
	public String getText() {

		return underlying.getText();
	}

	@Override
	public char[] getTextCharacters() {

		return underlying.getTextCharacters();
	}

	@Override
	public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length)
			throws XMLStreamException {

		return underlying.getTextCharacters(sourceStart, target, targetStart, length);
	}

	@Override
	public int getTextStart() {

		return underlying.getTextStart();
	}

	@Override
	public int getTextLength() {

		return underlying.getTextLength();
	}

	@Override
	public String getEncoding() {

		return underlying.getEncoding();
	}

	@Override
	public boolean hasText() {

		return underlying.hasText();
	}

	@Override
	public Location getLocation() {

		return underlying.getLocation();
	}

	@Override
	public QName getName() {

		return underlying.getName();
	}

	@Override
	public String getLocalName() {

		return underlying.getLocalName();
	}

	@Override
	public boolean hasName() {

		return underlying.hasName();
	}

	@Override
	public String getNamespaceURI() {
		
		return underlying.getNamespaceURI();
	}

	@Override
	public String getPrefix() {

		return underlying.getPrefix();
	}

	@Override
	public String getVersion() {

		return underlying.getVersion();
	}

	@Override
	public boolean isStandalone() {

		return underlying.isStandalone();
	}

	@Override
	public boolean standaloneSet() {

		return underlying.standaloneSet();
	}

	@Override
	public String getCharacterEncodingScheme() {

		return underlying.getCharacterEncodingScheme();
	}

	@Override
	public String getPITarget() {

		return underlying.getPITarget();
	}

	@Override
	public String getPIData() {

		return underlying.getPIData();
	}

}
