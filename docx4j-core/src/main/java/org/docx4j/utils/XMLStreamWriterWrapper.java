package org.docx4j.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This wrapper drops xmlns="" since OpenXML specifies
 * namespace prefix, and that empty declaration confuses some consumers.
 * 
 * It also writes empty-element tags using Luca Basso Ricci's 
 * code from https://stackoverflow.com/a/27208399/1031689
 */
public class XMLStreamWriterWrapper implements XMLStreamWriter {
	
	protected static Logger log = LoggerFactory.getLogger(XMLStreamWriterWrapper.class);
	

	XMLStreamWriter underlying;
	JaxbXmlPart part;
	
	public XMLStreamWriterWrapper(JaxbXmlPart part, XMLStreamWriter underlying) {
		this.part = part;
		this.underlying = underlying;
	}
	
    class Event {
        Method m;
        Object[] args;
    }
    enum EventEnum {
        writeStartElement,
        writeAttribute,
        writeNamespace,
        writeEndElement,
        setPrefix,
        setDefaultNamespace,    
    }
    private List<Event> queue = new ArrayList<Event>();	
    
    /** Namespace declarations to write on the next start element (CR-052), prefix to URI; null where none. */
    private java.util.Map<String, String> pendingDeclarations;
    /** The prefixes declared on the element being started, with their URIs, so a later declaration of one is not duplicated. */
    private final java.util.Map<String, String> declaredOnCurrent = new java.util.HashMap<String, String>();
    /** Of those, the ones declareOnNextStartElement put there, which the marshaller's own declaration of the prefix displaces. */
    private final java.util.Set<String> oursOnCurrent = new java.util.HashSet<String>();

    /**
     * Has the next start element declare these namespaces, each written ahead of the element's
     * attributes: how a marshal declares a prefix on the {@code mc:Choice} that names it
     * ({@link org.docx4j.jaxb.McDeclarations}; CR-052).  The marshaller's listener calls it
     * before the element is marshalled.
     *
     * @since 17.3.4
     */
    public void declareOnNextStartElement(java.util.Map<String, String> declarations) {
    	if (declarations == null || declarations.isEmpty()) return;
    	if (pendingDeclarations == null) {
    		pendingDeclarations = new java.util.LinkedHashMap<String, String>(declarations);
    	} else {
    		pendingDeclarations.putAll(declarations);
    	}
    }

    private void started() throws XMLStreamException {
    	declaredOnCurrent.clear();
    	oursOnCurrent.clear();
    	if (pendingDeclarations == null) return;
    	java.util.Map<String, String> decls = pendingDeclarations;
    	pendingDeclarations = null;
    	for (java.util.Map.Entry<String, String> e : decls.entrySet()) {
    		writeNamespace(e.getKey(), e.getValue());
    		oursOnCurrent.add(e.getKey());
    	}
    }

    /** Drops a queued declaration of the prefix on the element being started. */
    private void unqueueNamespace(String prefix) {
    	for (java.util.Iterator<Event> it = queue.iterator(); it.hasNext();) {
    		Event e = it.next();
    		if ("writeNamespace".equals(e.m.getName()) && prefix.equals(e.args[0])) it.remove();
    	}
    }

    /** Writes out whatever is queued (a start element awaiting its attributes), without closing. @since 17.3.4 */
    public void flushQueue() throws XMLStreamException {
    	fq();
    }
    	
	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
        d(e(m("writeStartElement",String.class)), localName);
        started();
	}

	@Override
	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        d(e(m("writeStartElement",String.class,String.class)), namespaceURI, localName);
        started();
	}

	@Override
	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        d(e(m("writeStartElement",String.class,String.class,String.class)), prefix, localName, namespaceURI);
        started();
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
        d(e(m("writeEndElement")));		
		
	}

	@Override
	public void writeEndDocument() throws XMLStreamException {
		fq();
		underlying.writeEndDocument();
		
	}

	@Override
	public void close() throws XMLStreamException {
		fq();
		underlying.close();
		
	}

	@Override
	public void flush() throws XMLStreamException {
		if(queue.isEmpty()) {
			underlying.flush();
		}
	}

	
	@Override
	public void writeAttribute(String localName, String value) throws XMLStreamException {
        d(e(m("writeAttribute",String.class, String.class)), localName, value);
		
	}

	@Override
	public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
			throws XMLStreamException {
		
//		at org.eclipse.persistence.oxm.record.XMLStreamWriterRecord.attributeWithoutQName(XMLStreamWriterRecord.java:105)
//		at org.eclipse.persistence.internal.oxm.record.AbstractMarshalRecordImpl.writeXsiTypeAttribute(AbstractMarshalRecordImpl.java:487)
//		at org.eclipse.persistence.internal.oxm.record.AbstractMarshalRecordImpl.addXsiTypeAndClassIndicatorIfRequired(AbstractMarshalRecordImpl.java:183)

		// MOXy wants to write XsiType, but doesn't provide prefix
    	if (prefix==null && namespaceURI.equals("http://www.w3.org/2001/XMLSchema-instance")) {
			log.info("added prefix for xsi");
    		prefix ="xsi";
    	}
		
		if (prefix==null || prefix.length()==0) {
			
			// suppress xsi type; shouldn't happen given the above code?
			if (ExceptionUtils.getStackFrames(new Throwable())[3].contains("writeXsiTypeAttribute") ) {
				log.info("suppressed xsi:type");
				return;
			} // or we could write it .. xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
			
			log.error("prefix null for " + namespaceURI + " localname " + localName,  new Throwable());
		}
		
        d(e(m("writeAttribute",String.class, String.class, String.class, String.class)), 
        		prefix, namespaceURI, localName, value);
		
	}

	@Override
	public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        d(e(m("writeAttribute",String.class, String.class, String.class)), namespaceURI, localName, value);
	}

	@Override
	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
		if (namespaceURI==null) {
			log.debug("Dropping null for prefix '" + prefix + "'");			
		} else if (namespaceURI.trim().isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("Dropping empty for prefix '" + prefix + "'");
			}
		} else if (!queue.isEmpty() && prefix != null && declaredOnCurrent.containsKey(prefix)) {
			// already declared on this element
			String have = declaredOnCurrent.get(prefix);
			if (namespaceURI.equals(have)) {
				if (log.isDebugEnabled()) log.debug("Not declaring {} again on this element", prefix);
			} else if (oursOnCurrent.remove(prefix)) {
				// the marshaller binds the prefix otherwise for its content: its binding stands (CR-052, D3)
				log.warn("Prefix '" + prefix + "' declared as " + have + " on this mc:Choice by its source, but the content binds it to "
						+ namespaceURI + "; the content's binding stands");
				unqueueNamespace(prefix);
				declaredOnCurrent.put(prefix, namespaceURI);
		        d(e(m("writeNamespace",String.class, String.class)), prefix, namespaceURI);
			} else {
				log.warn("Prefix '" + prefix + "' declared twice on one element, as " + have + " and " + namespaceURI + "; keeping the first");
			}
		} else {
			if (!queue.isEmpty() && prefix != null) declaredOnCurrent.put(prefix, namespaceURI);
			if (log.isDebugEnabled()) {
				log.debug("Writing {}={}", prefix, namespaceURI);
			}
	        d(e(m("writeNamespace",String.class, String.class)), prefix, namespaceURI);
			
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
        fq();
		underlying.writeComment(data);
		
	}

	@Override
	public void writeProcessingInstruction(String target) throws XMLStreamException {
        fq();
		underlying.writeProcessingInstruction(target);
		
	}

	@Override
	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        fq();
		underlying.writeProcessingInstruction(target, data);
		
	}

	@Override
	public void writeCData(String data) throws XMLStreamException {
	    fq();
		underlying.writeCData(data);
		
	}

	@Override
	public void writeDTD(String dtd) throws XMLStreamException {
        fq();
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
        fq();
		underlying.writeCharacters(text);
		
	}

	@Override
	public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        fq();
		underlying.writeCharacters(text, start, len);
		
	}


	@Override
	public String getPrefix(String uri) throws XMLStreamException {
		return underlying.getPrefix(uri);
	}

	@Override
	public void setPrefix(String prefix, String uri) throws XMLStreamException {
	    d(e(m("setPrefix", String.class, String.class)), prefix, uri);
		
	}

	@Override
	public void setDefaultNamespace(String uri) throws XMLStreamException {
	       d(e(m("setDefaultNamespace", String.class)), uri);		
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

	// ------------------------------------------------------------------
	// from https://stackoverflow.com/a/27208399/1031689
	
    void d(Event e,Object...args) throws XMLStreamException {
        e.args = args;
        switch(EventEnum.valueOf(e.m.getName()))
        {
            case writeStartElement:
                fq();
                queue.add(e);
                break;
            case writeAttribute:
            case writeNamespace:
            case setPrefix:
            case setDefaultNamespace:
                if(!queue.isEmpty())
                    queue.add(e);
                else
                    ex(e, args);
                break;
            case writeEndElement:
                if(!queue.isEmpty())
                {
                    final Event e1 = queue.get(0);
                    e1.m = m("writeEmptyElement", e1.m.getParameterTypes());
                    fq();
                }
                else
                {
                    ex(e, args);
                }
                break;
        }
    }
    Event e(Method m,Object...params)
    {
        final Event e = new Event();
        e.m = m;
        e.args = params;
        return e;
    }
    Method m(String methodName,Class<?>...args) throws XMLStreamException {
        try {
            return XMLStreamWriter.class.getMethod(methodName, args);
        } catch (Exception e) {
            throw new XMLStreamException(e);
        }
    }
    void fq() throws XMLStreamException
    {
        for(int i = 0;i < queue.size();i++)
        {
            Event e = queue.get(i);
            ex(e, e.args);
        }
        queue.clear();
    }
    void ex(Event e,Object...args) throws XMLStreamException
    {
        try
        {
            e.m.invoke(underlying, e.args);
        }
        catch(Exception ex)
        {
            throw new XMLStreamException(ex);
        }
    }	
}
