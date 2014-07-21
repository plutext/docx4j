package org.docx4j.utils.sax;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.jvnet.jaxb2_commons.xpath_tracker.Histgram;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * Convert OpenXML to hyperlinked HTML
 * 
 * The hyperlinks are XPaths to each element
 *
 * Could have used XSLT to produce these XPaths.
 * See for example http://stackoverflow.com/questions/953197/how-do-you-output-the-current-element-path-in-xslt
 * 
 * But that would've been inefficient.
 * 
 * I could have written the XPath as an extra attribute,
 * then used XSLT to transform the XML to HTML, but it
 * seems simpler to create the HTML here as well.
 *
 */
public class SAXHandlerToCodeString extends SAXIdentityHandler { 

	protected static Logger log = Logger.getLogger(SAXHandlerToCodeString.class);
	
	final static boolean IS_PRETTY_PRINTED = true;
	
	boolean firstElement=true;
	boolean initialised=false;
	
	Set<String> requiredNamespaceDecs = new HashSet<String>();
	
	private void registerNamespaceDecs(String uri, String qName) {
		
		int index = qName.indexOf(":");
		if (index>0) {
			String prefix = qName.substring(0, index);
			String dec = "xmlns:"+prefix+"=\""+ uri+ "\"";
			requiredNamespaceDecs.add(dec);
		} else if (uri.equals("http://schemas.openxmlformats.org/spreadsheetml/2006/main")) {
			String dec = "xmlns:s=\""+ uri+ "\"";				
		} else {
			log.warn("TODO: handle default namespace " + uri);
		}
	}
	
	public String getNamespaceDecs() {

		StringBuilder sb = new StringBuilder();

		for (String s : requiredNamespaceDecs) {
			sb.append( " " + s.replace("\"", "\\\"") );
		}
		return sb.toString();
	}
	
	
	private void init() {
        histgrams.clear();
        histgrams.push(new Histgram());
        initialised=true;
	}
	
	  public SAXHandlerToCodeString(Writer out) {
		    super(out); 
		    TAG_OPEN="<";
			TAG_CLOSE=">";		 
			QUOTE="\\\"";
	  }


		// @Override
		public void startDocument() throws SAXException {
			init();
		}
		
		// @Override
		public void endDocument() throws SAXException {
			write("\"", false);
		}
		
	  
	// @Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
	}

	// @Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		
		if (!initialised) init();
		
		if (needToClose) {
			write(TAG_CLOSE+"\"\n", false);
			needToClose = false;
		}

		histgrams.peek().update(uri, localName, qName);
		histgrams.push(new Histgram());
		
		registerNamespaceDecs(uri, qName); 

		if (firstElement) {

			write("String openXML = \"" + TAG_OPEN + qName, false);
			firstElement = false;
		} else {
			write("+ \"" + TAG_OPEN + qName, true);			
		}

		int length = atts.getLength();
		for (int i = 0; i < length; i++) {
			
			if (atts.getLocalName(i).equals("rsidRDefault")
					|| atts.getLocalName(i).equals("rsidP")
					|| atts.getLocalName(i).equals("rsidR")
					|| atts.getLocalName(i).equals("rsidRPr")
					|| atts.getLocalName(i).equals("rsidSect")
					|| atts.getLocalName(i).equals("rsidTr")
					|| atts.getLocalName(i).equals("paraId")
					|| atts.getLocalName(i).equals("textId")
					) {
				// Ignore these
			} else {
				write(" " + atts.getQName(i)+"=" + QUOTE +atts.getValue(i)+ QUOTE, false);
				registerNamespaceDecs(atts.getURI(i), atts.getQName(i) );
			}
		}
		
		needToClose = true;

	}
	

	// @Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		if (IS_PRETTY_PRINTED) {

			if (needToClose) {
				write("/"+TAG_CLOSE+"\"", false);
				needToClose = false;
			} else if (charactersIncludeNewline) {
				// Workaround for case where source is pretty printed
				// and so has whitespace 
				write("+ \"" + TAG_OPEN+"/" + qName + TAG_CLOSE+"\"", true);
				charactersIncludeNewline= false;
			} else if (encounteredText) {
				write(TAG_OPEN+"/" + qName + TAG_CLOSE+"\"", false);
			} else {
				write("+ \"" + TAG_OPEN+"/" + qName + TAG_CLOSE+"\"", true);
			}
			
			
		} else {

			if (needToClose) {
				write("/"+TAG_CLOSE+"\"\n", false);
				needToClose = false;
			} else if (charactersIncludeNewline) {
				// Workaround for case where source is pretty printed
				// and so has whitespace 
				write("+ \"" + TAG_OPEN+"/" + qName + TAG_CLOSE+"\"\n", true);
				charactersIncludeNewline= false;
			} else if (encounteredText) {
				write(TAG_OPEN+"/" + qName + TAG_CLOSE+"\"\n", false);
			} else {
				write("+ \"" + TAG_OPEN+"/" + qName + TAG_CLOSE+"\"\n", true);
			}
			
		}
		
        histgrams.pop();		
		encounteredText = false;
	}	
	
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		
		
		// Close opening tag?
		if (needToClose) {
			write(TAG_CLOSE, false);
//			System.out.println(String.valueOf(ch) + ": " + start + ", " + length);
			
			// Write a quote, if there is no text content
			if (String.valueOf(ch).trim().length()==0) {
				
				if (IS_PRETTY_PRINTED) {
					write("\"", false);
				} else {
					write("\"\n", false);
				}
			}
			needToClose = false;
		}
		
		write((new String(ch, start, length)).replace("\\", "\\\\"), false);
		encounteredText = true;
		
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
		marshaller.marshal(documentPart.getJaxbElement(), new SAXHandlerToCodeString(out));
		out.close();
	}

}
