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
	
	protected static Logger log = LoggerFactory.getLogger(XMLStreamWriterWrapperIndenting.class);
		
    

	@Override
    public void indent() throws XMLStreamException {
    	underlying.writeCharacters("\n");		
        char[] indent = new char[depth * 2];
        Arrays.fill(indent, ' ');
        underlying.writeCharacters(indent, 0, indent.length);
    }    
    
    
	public XMLStreamWriterWrapperIndenting(JaxbXmlPart part, XMLStreamWriter underlying) {
		super(part, underlying);
	}
	

	@Override
	public void writeComment(String data) throws XMLStreamException {
    	underlying.writeCharacters("\n");
    	indent();
		super.writeComment(data);
		
	}
    
	
}
