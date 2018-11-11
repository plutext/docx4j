package org.docx4j.utils;

import java.util.Arrays;
import java.util.Stack;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jharrop
 * @since 6.1.0
 */
public class XMLStreamWriterWrapperIndenting extends XMLStreamWriterWrapper {
	
	// TODO: write newline before element with self-closing tag. 
	
	protected static Logger log = LoggerFactory.getLogger(XMLStreamWriterWrapperIndenting.class);
		
    private int depth = 0;
    
    // Kohsuke's
    private final static Object SEEN_NOTHING = new Object();
    private final static Object SEEN_ELEMENT = new Object();
    private final static Object SEEN_DATA = new Object();

    private Object state = SEEN_NOTHING;
    private Stack<Object> stateStack = new Stack<Object>();
    
    
    public void indent() throws XMLStreamException {
        char[] indent = new char[depth * 2];
        Arrays.fill(indent, ' ');
        underlying.writeCharacters(indent, 0, indent.length);
    }    
    
    private void onStartElement() throws XMLStreamException {
    	stateStack.push(SEEN_ELEMENT);
        state = SEEN_NOTHING;
//        if (depth > 0) {
        	underlying.writeCharacters("\n");
//        }
        indent();
        depth++;
    }    
    private void onEmptyElement() throws XMLStreamException {
        state = SEEN_ELEMENT;    	
        if (depth > 0) {
        	underlying.writeCharacters("\n");
        }
        indent();
        // don't increment depth
    }    

    private void onEndElement() throws XMLStreamException {
        depth--;
        if (state == SEEN_ELEMENT) {
            super.writeCharacters("\n");
            indent();
        }
        state = stateStack.pop();
    }    
    
	public XMLStreamWriterWrapperIndenting(JaxbXmlPart part, XMLStreamWriter underlying) {
		super(part, underlying);
	}
	
	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
		onStartElement();
		super.writeStartElement(localName);
		
	}

	@Override
	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		onStartElement();
		super.writeStartElement(namespaceURI, localName);
		
	}

	@Override
	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		onStartElement();
		super.writeStartElement(prefix, localName, namespaceURI);
		
	}

	@Override
	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		onEmptyElement();
		super.writeEmptyElement(namespaceURI, localName);
		
	}

	@Override
	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		onEmptyElement();
		super.writeEmptyElement(prefix, localName, namespaceURI);
		
	}

	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
		onEmptyElement();
		super.writeEmptyElement(localName);
		
	}

	@Override
	public void writeEndElement() throws XMLStreamException {
		onEndElement();
		super.writeEndElement();
		
	}

	@Override
	public void writeComment(String data) throws XMLStreamException {
    	underlying.writeCharacters("\n");
    	indent();
		super.writeComment(data);
		
	}
	
}
