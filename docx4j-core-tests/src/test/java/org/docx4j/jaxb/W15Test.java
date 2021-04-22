package org.docx4j.jaxb;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.org.apache.xml.security.c14n.CanonicalizationException;
import org.docx4j.org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.docx4j.wml.Body;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.junit.Test;
import org.xml.sax.SAXException;

public class W15Test {

	/**
	 * Example of setting mc:Ignorable="w15",
	 * which you must do if you add eg w15:collapsed
	 * and want Word <=14 to be happy.
	 * 
	 * @throws InvalidFormatException
	 * @throws JAXBException
	 */
	@Test
	public void testIgnorableSet() throws InvalidFormatException, JAXBException {

		Document document = new Document();
		
		Body body = new Body();
		document.setBody(body);
		
		P p = new P();
		PPr ppr = new PPr();
		p.setPPr(ppr);
		body.getContent().add(p);
		
		ppr.setCollapsed(new BooleanDefaultTrue() );
		/*
		 * if you do setCollapsed, you must do:
		 */
		document.setIgnorable("w15");
		/*
		 * otherwise, Word 14 and lower will complain
		 * that the docx is invalid!
		 * 
		 * Similar consideration apply if you set w14 attributes 
		 * eg <w:p w14:textId="3313beef" w14:paraId="3313beef">
		 */
		
//		System.out.println(XmlUtils.marshaltoString(body, true, true));
		
		MainDocumentPart mdp = new MainDocumentPart(); 
		mdp.setJaxbElement(document);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		mdp.marshal(baos);
		
		String result = (new String(baos.toByteArray()));
		
		System.out.println(result);
		
		assertTrue(result.contains("mc:Ignorable=\"w15\"") );
		
		// Also need the namespace declaration at that level
		int startPos = result.indexOf("<w:document");
		String startTag= result.substring(startPos, result.indexOf(">", startPos));
		//System.out.println(startTag);
		
		// xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml"
		assertTrue(startTag.contains("xmlns:w15=") );
		
	}


	@Test
	public void testSettingsWithCanonicalisation() throws InvalidFormatException, JAXBException {
		
		// this test fails in docx4j 3.3.1
		
		Docx4jProperties.setProperty("docx4j.jaxb.marshal.canonicalize", true);
		testSettings();
	}

	@Test
	public void testSettingsWithoutCanonicalisation() throws InvalidFormatException, JAXBException {
		
		Docx4jProperties.setProperty("docx4j.jaxb.marshal.canonicalize", false);
		testSettings();
	}
	
	public void testSettings() throws InvalidFormatException, JAXBException {
		    
	    String openXML = 
	    	"<w:settings mc:Ignorable=\"w15\" "
			    		+ "xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\"  "
//			    		+ "xmlns:w15=\"http://schemas.microsoft.com/office/word/2012/wordml\"  "
			    		+ "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
//			    		+ "xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\"  "
//			    		+ "xmlns:o=\"urn:schemas-microsoft-com:office:office\" "
//			    		+ "xmlns:v=\"urn:schemas-microsoft-com:vml\" "
			    		+ ">"
			    		
            + "<w:zoom w:percent=\"120\"/>"
            + "<w:defaultTabStop w:val=\"720\"/>"

            + "<w:decimalSymbol w:val=\".\"/>"
            + "<w:listSeparator w:val=\",\"/>"			    		
			    		
	            + "<w15:chartTrackingRefBased xmlns:w15=\"http://schemas.microsoft.com/office/word/2012/wordml\"/>"
	            + "<w15:docId w15:val=\"{78FB31FD-2D0A-4042-95DA-DFD2E5520F96}\" xmlns:w15=\"http://schemas.microsoft.com/office/word/2012/wordml\" />"
	        +"</w:settings>";
	    
		Object o = XmlUtils.unmarshalString(openXML);
		
		DocumentSettingsPart dsp = new DocumentSettingsPart();
		dsp.setContents( (CTSettings)XmlUtils.unwrap(o));
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		dsp.marshal(baos);
		
		String result = (new String(baos.toByteArray()));
		
		System.out.println(result);
		
		// mc:Ignorable=" w15"
		int startPos = result.indexOf("mc:Ignorable");
		int valPos = result.indexOf("\"", startPos);
		String attVal= result.substring(valPos, result.indexOf("\"", valPos+1)+1);
		//System.out.println(attVal);
		assertTrue(attVal.contains("w15") );
		
		// Also need the namespace declaration at that level
		startPos = result.indexOf("<w:settings");
		String startTag= result.substring(startPos, result.indexOf(">", startPos));
		//System.out.println(startTag);		
		// xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml"
		assertTrue(startTag.contains("xmlns:w15=") );
		
	}
	
	@Test
	public void testCanonicalisation() throws SAXException, IOException, InvalidCanonicalizerException, CanonicalizationException  {

		InputStream resourceAsStream = W15Test.class.getClassLoader().getResourceAsStream("document_with_ignorable.xml");
		
		DocumentBuilder builder = XmlUtils.getNewDocumentBuilder();
		org.w3c.dom.Document doc = builder.parse(resourceAsStream);
		
		byte[] bytes = XmlUtils.trimNamespaces(doc, "w14");

		assertTrue(new String(bytes).contains("xmlns:w14=") );
		
	}
		
}
