package org.docx4j.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.docx4j.jaxb.NamespacePrefixMappings;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * This wrapper drops xmlns="" since OpenXML specifies
 * namespace prefix, and that empty declaration confuses some consumers.
 * 
 * @author jharrop
 *
 */
public class XMLStreamWriterWrapper implements XMLStreamWriter {
	
	protected static Logger log = LoggerFactory.getLogger(XMLStreamWriterWrapper.class);
	
	public XMLStreamWriterWrapper(JaxbXmlPart part, XMLStreamWriter underlying) {
		this.part = part;
		this.underlying = underlying;
		
//		log.debug(
//				underlying.getProperty(name)
	}

	public void setIgnorableNamespaces(String mcIgnorable) {
		this.mcIgnorable = mcIgnorable;
	}
	
	XMLStreamWriter underlying;
	JaxbXmlPart part;
	
	String mcIgnorable;	
	private HashSet<String> topLevelPrefixesWritten = new HashSet<String>(); 
    
    // Kohsuke's
    private final static Object SEEN_NOTHING = new Object();
    private final static Object SEEN_ELEMENT = new Object();
    private final static Object SEEN_DATA = new Object();

    private Object state = SEEN_NOTHING;
    private Stack<Object> stateStack = new Stack<Object>();
    
    
    public void indent() throws XMLStreamException {
    	// overridden in subclass
    }
    
    protected int depth = 0;
    
    protected void onStartElement() throws XMLStreamException {
    	stateStack.push(SEEN_ELEMENT);
        state = SEEN_NOTHING;
        indent();
        depth++;
    }    
    
    protected void onEmptyElement() throws XMLStreamException {
        state = SEEN_ELEMENT;    	
        indent();
        // don't increment depth
    }    

    protected void onEndElement() throws XMLStreamException {
        depth--;
        if (state == SEEN_ELEMENT) {
            indent();
        }
        state = stateStack.pop();
    }    
	
	
	
	@Override
	public void writeStartElement(String localName) throws XMLStreamException {		
		
		onStartElement();
		underlying.writeStartElement(localName);
		
		if (depth==1) { 
			declareIgnorableNamespaces();
		}
	}

	@Override
	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {

		onStartElement();
		underlying.writeStartElement(namespaceURI, localName);
		
		if (depth==1) { 
			declareIgnorableNamespaces();
		}
	}

	@Override
	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {

		onStartElement();
		underlying.writeStartElement(prefix, localName, namespaceURI);

		if (depth==1) { 
			declareIgnorableNamespaces();
		}
	}

	@Override
	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		onEmptyElement();
		underlying.writeEmptyElement(namespaceURI, localName);
		
	}

	@Override
	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		onEmptyElement();
		underlying.writeEmptyElement(prefix, localName, namespaceURI);
		
	}

	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
		onEmptyElement();
		underlying.writeEmptyElement(localName);
		
	}

	@Override
	public void writeEndElement() throws XMLStreamException {
		onEndElement();
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

	/**
	 * Write namespaces Word needs at top level to open documents
	 * which use mc.  Added especially for MOXy case.
	 * 
	 * @throws XMLStreamException
	 * @since 6.1.0
	 */
	private void declareIgnorableNamespaces() throws XMLStreamException {
		
		if (mcIgnorable==null) return;
		
		StringTokenizer st = new StringTokenizer(mcIgnorable, " ");
		while (st.hasMoreTokens()) {
			String prefix = (String) st.nextToken();
			
			String uri = NamespacePrefixMappings.getNamespaceURIStatic(prefix);
			
			if (uri==null) {
				log.warn("No mapping for prefix '" + prefix + "'");
			} else {
				writeNamespace(prefix, uri);
				topLevelPrefixesWritten.add(prefix);
			}
		}
		
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
			
			if (// depth==1 &&
					topLevelPrefixesWritten.contains(prefix)) {
				// already wrote this.
			} else {
				// usual case
				underlying.writeNamespace(prefix, namespaceURI);
			}
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
