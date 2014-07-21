package org.docx4j.utils.sax;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Stack;

import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.jvnet.jaxb2_commons.xpath_tracker.Histgram;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class SAXIdentityHandler extends DefaultHandler { 

	protected static Logger log = Logger.getLogger(SAXIdentityHandler.class);
	
	protected String TAG_OPEN="<";
	protected String TAG_CLOSE=">";
	protected String QUOTE="\"";

	protected final Stack<Histgram> histgrams = new Stack<Histgram>();
	private Writer out;

	public SAXIdentityHandler(Writer out) {
		this.out = out;
	}

	// @Override
	public void endDocument() throws SAXException {
	}

	// @Override
	public void endPrefixMapping(String prefix) throws SAXException {
	}

	// @Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
	}

	// @Override
	public void processingInstruction(String target, String data)
			throws SAXException {
	}

	// @Override
	public void setDocumentLocator(Locator locator) {
	}

	// @Override
	public void skippedEntity(String name) throws SAXException {
	}

	// @Override
	public void startDocument() throws SAXException {
        histgrams.clear();
        histgrams.push(new Histgram());		
	}

	// @Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
	}

	// @Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		
		if (needToClose) {
			write(TAG_CLOSE+"\n", false);
			needToClose = false;
		}

		histgrams.peek().update(uri, localName, qName);
		histgrams.push(new Histgram());

		write(TAG_OPEN + qName, true); // + getXPath() + "\n");

		int length = atts.getLength();
		for (int i = 0; i < length; i++) {
			
			if (atts.getLocalName(i).equals("rsidRDefault")
					|| atts.getLocalName(i).equals("rsidP")
					|| atts.getLocalName(i).equals("rsidR")
					|| atts.getLocalName(i).equals("rsidRPr")
					|| atts.getLocalName(i).equals("rsidSect")
					) {
				// Ignore these
			} else {
				write(" " + atts.getQName(i)+"=" + QUOTE +atts.getValue(i)+ QUOTE, false);
			}
		}
		
		needToClose = true;

	}
	
	/**
	 * So we can create empty elements <likethis/>
	 */
	boolean needToClose;
	/**
	 * SO we can avoid indentation after text content
	 */
	boolean encounteredText;

	// @Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		if (needToClose) {
			write("/"+TAG_CLOSE+"\n", false);
			needToClose = false;
		} else if (encounteredText) {
			write(TAG_OPEN+"/" + qName + TAG_CLOSE+"\n", false);
		} else {
			write(TAG_OPEN+"/" + qName + TAG_CLOSE+"\n", true);
		}
		
        histgrams.pop();		
		encounteredText = false;
	}

	// @Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		
		if (needToClose) {
			write(TAG_CLOSE, false);
			needToClose = false;
		}
		
		write(new String(ch, start, length), false);
		encounteredText = true;
		
	}


	boolean charactersIncludeNewline = false;
	
	protected void write(String text, boolean indent) throws SAXException {
		
		charactersIncludeNewline = text.contains("\n");
		
		try {			
			if (indent) out.append(getIndentation());
			out.append(text);
		} catch (IOException e) {
		    throw new SAXException(e);   
		}

	}
	
	private String getIndentation() {
		
        StringBuilder buf = new StringBuilder();
        for (Histgram h : histgrams) {
            buf.append("    ");
        }
        return buf.toString();
		
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
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		
		String inputfilepath = System.getProperty("user.dir") + "/test.docx";
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		
		Marshaller marshaller=Context.jc.createMarshaller();
		NamespacePrefixMapperUtils.setProperty(marshaller, 
				NamespacePrefixMapperUtils.getPrefixMapper());
		
		Writer out = new OutputStreamWriter(System.out);		
		marshaller.marshal(documentPart.getJaxbElement(), new SAXIdentityHandler(out));
		
		out.close();
	}

}
