package org.jvnet.jaxb2_commons.xpath_tracker;

import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

import java.util.Stack;

/**
 * {@link XMLFilter} that monitors the infoset that flows through it,
 * and provide a capability to {@link #getXPath() compute the XPath to the current element}.
 *
 * <p>
 * In context of JAXB, this can be used to report error location by using XPath.
 * 
 * @author Kohsuke Kawaguchi
 */
public class XPathTracker extends XMLFilterImpl {

    private final Stack<Histgram> histgrams = new Stack<Histgram>();

    public XPathTracker() {
        super();
    }

    public XPathTracker(XMLReader parent) {
        super(parent);
    }

    public XPathTracker(ContentHandler contentHandler) {
        setContentHandler(contentHandler);
    }

    public void startDocument() throws SAXException {
        super.startDocument();

        histgrams.clear();
        histgrams.push(new Histgram());
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        super.startElement(uri, localName, qName, atts);

        histgrams.peek().update(uri,localName,qName);
        histgrams.push(new Histgram());
    }


    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        histgrams.pop();
    }

    /**
     * Gets the XPath to the current element.
     */
    public String getXPath() {
        StringBuilder buf = new StringBuilder();
        for (Histgram h : histgrams) {
            h.appendPath(buf);
        }
        return buf.toString();
    }
}
