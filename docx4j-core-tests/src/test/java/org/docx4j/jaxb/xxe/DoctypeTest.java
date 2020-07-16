package org.docx4j.jaxb.xxe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.docx4j.XmlUtils;
import org.docx4j.wml.Document;
import org.junit.Test;

public class DoctypeTest {

	/**
	 * Test that our DocumentBuilderFactory is suitable configured.  unmarshalString uses that.
	 */
	@Test
	public void documentBuilderFactoryTest() {
		
		String openXML = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<!DOCTYPE test [<!ENTITY xxe SYSTEM 'file:///etc/passwd'>]>"
			+ "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" >"
                + "<w:body>"
	                + "<w:p>"
	                        + "<w:r>"
	                                + "<w:t>Here it is: </w:t>"
	                        + "</w:r>"
	                + "</w:p>"
	                + "<w:p>"
                    + "<w:r>"
                            + "<w:t>&xxe;</w:t>"
                    + "</w:r>"
            + "</w:p>"
		        + "</w:body>"
		+ "</w:document>";
		
		Document document;
		try {
			
			// StreamSource(Reader reader) is vulnerable to XXE
			document = (Document)XmlUtils.unmarshalString(openXML);
			// should not get here...
			System.out.println(XmlUtils.marshaltoString(document));
			assertFalse("Entity should not get expanded", XmlUtils.marshaltoString(document).contains("bin"));
			fail("Exception should have been thrown");
		} catch (/* we expect */ JAXBException e) {
			// We expect failure here
			//e.getLinkedException().printStackTrace();
			assertTrue("expected SAXParseException", e.getLinkedException().getClass().getName().equals("org.xml.sax.SAXParseException"));
			String message = e.getLinkedException().getMessage();
			assertTrue("DOCTYPE should be disallowed, but " + message, message.contains("DOCTYPE is disallowed") || message.contains("DOCTYPE ist nicht zulässig") ); // mvn surefire seems to use German locale?!
			return;
		}
		fail("Exception should have been thrown");
		
	}

	/**
	 * Test we suitably configure an XMLStreamReader (this is another approach to avoiding StreamSource. 
	 */
	@Test
	public void xmlStreamReaderTest() {
		
		if (!sunXalanPresent()) return;
		
		String openXML = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<!DOCTYPE test [<!ENTITY xxe SYSTEM 'file:///etc/passwd'>]>"
			+ "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" >"
                + "<w:body>"
	                + "<w:p>"
	                        + "<w:r>"
	                                + "<w:t>Here it is: </w:t>"
	                        + "</w:r>"
	                + "</w:p>"
	                + "<w:p>"
                    + "<w:r>"
                            + "<w:t>&xxe;</w:t>"
                    + "</w:r>"
            + "</w:p>"
		        + "</w:body>"
		+ "</w:document>";
		
		try (InputStream is = IOUtils.toInputStream(openXML, "UTF-8")) {

			// Guard against XXE
			XMLInputFactory xif = XMLInputFactory.newInstance();
			xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
			xif.setProperty(XMLInputFactory.SUPPORT_DTD, false); // a DTD is merely ignored, its presence doesn't cause an exception
			XMLStreamReader xsr = null;
			xsr = xif.createXMLStreamReader(is);
			String result = identity(xsr); // expect: The entity "xxe" was referenced, but not declared.	
			System.out.println(result);
			fail("Exception should have been thrown");			
		} catch (/* we expect */ TransformerException e) {
			// the DTD would've been ignored, so we expect to get
			// ERROR:  'ParseError at [row,col]:[1,273]
			// Message: The entity "xxe" was referenced, but not declared.			
			e.getException().printStackTrace();
			assertTrue("expected SAXParseException",
					e.getException().getClass().getName().equals("javax.xml.stream.XMLStreamException"));
			return;
		} catch (Exception e1) {
			e1.printStackTrace();
			fail("expected a TransformerException");

			// should not get here...
		}
		fail("Exception should have been thrown");
		
	}
	
	private static String identity(XMLStreamReader reader) throws XMLStreamException, TransformerFactoryConfigurationError, TransformerException {
		
		/* org.docx4j.org.apache.xalan.transformer.TransformerIdentityImpl.transform results in:
		 * 
			javax.xml.transform.TransformerException: Can't transform a Source of type javax.xml.transform.stax.StAXSource
				at org.docx4j.org.apache.xalan.transformer.TransformerIdentityImpl.transform(TransformerIdentityImpl.java:423)
				
			so instead:- 
				
			 */
		String propVal = System.getProperty("javax.xml.transform.TransformerFactory");
		System.setProperty("javax.xml.transform.TransformerFactory",
				"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
		
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter stringWriter = new StringWriter();
        transformer.transform(new StAXSource(reader), new StreamResult(stringWriter));

        // reset
        if (propVal!=null) {
			System.setProperty("javax.xml.transform.TransformerFactory", propVal);
        }

        return stringWriter.toString();
	}
	
	private static boolean sunXalanPresent() {
		
			
			Class<?> sunXalanClass = null;
		    try {
		    	sunXalanClass = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
		    	return true;
		    } catch (Exception e) {
		    	return false;
		    }		
			
	}	
	
}
