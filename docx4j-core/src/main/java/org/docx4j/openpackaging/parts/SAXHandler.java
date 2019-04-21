package org.docx4j.openpackaging.parts;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * Following http://stackoverflow.com/a/2240367/1031689
 * courtesy of http://stackoverflow.com/users/270610/chris-pacejo
 *
 */
public class SAXHandler implements ContentHandler {
	
	protected static Logger log = LoggerFactory.getLogger(SAXHandler.class);	
	
    static final private TransformerFactory tf = TransformerFactory.newInstance();
    private ContentHandler ch;

    private ByteArrayOutputStream os;
    
	public ByteArrayOutputStream getOutputStream() {
		return os;
	}

	public SAXHandler() throws SAXException {
        try {
            final Transformer t = tf.newTransformer();
            
            os = new ByteArrayOutputStream(); 
            
            log.info("Using transformer: " + t.getClass().getName());
            	// org.apache.xalan.transformer.TransformerIdentityImpl works

            t.transform(new SAXSource(                
                new XMLReader() {     
                    public ContentHandler getContentHandler() { return ch; }
                    public DTDHandler getDTDHandler() { return null; }      
                    public EntityResolver getEntityResolver() { return null; }
                    public ErrorHandler getErrorHandler() { return null; }    
                    public boolean getFeature(String name) { return false; }
                    public Object getProperty(String name) { return null; } 
                    public void parse(InputSource input) { }               
                    public void parse(String systemId) { }  
                    
                    public void setContentHandler(ContentHandler handler) { ch = handler; }  
                    
                    public void setDTDHandler(DTDHandler handler) { }
                    public void setEntityResolver(EntityResolver resolver) { }
                    public void setErrorHandler(ErrorHandler handler) { }
                    public void setFeature(String name, boolean value) { }
                    public void setProperty(String name, Object value) { }
                }, new InputSource()),                                    
                new StreamResult(os));
        }
        catch (TransformerException e) {
            throw new SAXException(e);  
        }

        if (ch == null)
            throw new SAXException("Transformer didn't set ContentHandler");
    }

    public void setDocumentLocator(Locator locator) {
        ch.setDocumentLocator(locator);
    }

    public void startDocument() throws SAXException {
        ch.startDocument();
    }

    public void endDocument() throws SAXException {
        ch.endDocument();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        ch.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        ch.endPrefixMapping(prefix);
    }

    public void startElement(String uri, String localName, String qName, Attributes atts)
        throws SAXException {
        ch.startElement(uri, localName, qName, atts);
    }

    public void endElement(String uri, String localName, String qName)
        throws SAXException {
        ch.endElement(uri, localName, qName);
    }

    /* You'll probably want to override this.
     * 
     * (non-Javadoc)
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {
        this.ch.characters(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException {
        this.ch.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data)
        throws SAXException {
        ch.processingInstruction(target, data);
    }

    public void skippedEntity(String name) throws SAXException {
        ch.skippedEntity(name);
    }

	public ContentHandler getContentHandler() {
		return ch;
	}

}