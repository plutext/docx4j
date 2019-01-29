package org.docx4j;

import static org.junit.Assert.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AstralCharacterHandling {

	@Test
	public void test() throws ParserConfigurationException {
		
		char[] chars = {55357, 56459};
		int codePoint = Character.codePointAt(chars, 0);

		
		// Convert it to a string
		String astral = new String(Character.toChars(codePoint));

		
		// Now show how it all falls apart 
		// Create a DOM doc containing astral char
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();	
		Document doc = db.newDocument();
		Element foo = doc.createElement("foo");
		doc.appendChild(foo);
		foo.setTextContent(astral);
		
		String actual = XmlUtils.w3CDomNodeToString(foo);  // this uses our configured serializer
		// <foo>_  so codepoint of interest is at position 5.
				
		Assert.assertEquals(codePoint, actual.codePointAt(5));
		
	}

}
