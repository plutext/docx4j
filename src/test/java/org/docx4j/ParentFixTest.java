package org.docx4j;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.junit.Test;

public class ParentFixTest {

	@Test
	public void testBodyCreated() throws InvalidFormatException  {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		mdp.addParagraphOfText("hello world");

		Document doc = mdp.getJaxbElement();
		
		Body body = doc.getBody();
		
		// Won't be set
		if (body.getParent()!=null) {
			Assert.assertEquals(doc, body.getParent());
		}
		
		// Use TraversalUtil which "fixes" parent?
		mdp.getStylesInUse();
		
		// Might be set?
		if (body.getParent()!=null) {
			// If it is
			Assert.assertEquals(doc, body.getParent());
		}
		
	}

	@Test
	public void testBodyLoaded() throws Docx4JException, IOException  {
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		mdp.addParagraphOfText("hello world");
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.save(wordMLPackage, baos);
		
		byte[] bytes = baos.toByteArray();
		
		
		// Now load it
		wordMLPackage 
			= Docx4J.load(
				new ByteArrayInputStream(bytes));
		
		mdp = wordMLPackage.getMainDocumentPart();

		Document doc = mdp.getJaxbElement();
		
		Body body = doc.getBody();
		
		// Expect it to be set
		Assert.assertEquals(doc, body.getParent());
		
		// Use TraversalUtil which "fixes" parent?
		mdp.getStylesInUse();
		
		// Might be set?
		if (body.getParent()!=null) {
			// If it is
			Assert.assertEquals(doc, body.getParent());
		}
		
	}

	@Test
	public void testBodyChildrenCreated() throws InvalidFormatException  {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		mdp.addParagraphOfText("hello world");

		Document doc = mdp.getJaxbElement();
		
		Body body = doc.getBody();
		P p = (P)body.getContent().get(0);
		
		// Won't be set
		if (p.getParent()!=null) {
			Assert.assertEquals(body, p.getParent());
		}
		
		// Use TraversalUtil which "fixes" parent?
		mdp.getStylesInUse();
		
		// Might be set?
		if (p.getParent()!=null) {
			Assert.assertEquals(body, p.getParent());
		}
		
	}

	@Test
	public void testBodyChildrenLoaded() throws Docx4JException, IOException  {
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		mdp.addParagraphOfText("hello world");
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.save(wordMLPackage, baos);
		
		byte[] bytes = baos.toByteArray();
		
		
		// Now load it
		wordMLPackage 
			= Docx4J.load(
				new ByteArrayInputStream(bytes));
		
		mdp = wordMLPackage.getMainDocumentPart();

		Document doc = mdp.getJaxbElement();
		Body body = doc.getBody();
		P p = (P)body.getContent().get(0);
		
		// Won't be set
		if (p.getParent()!=null) {
			Assert.assertEquals(body, p.getParent());
		}
		
		// Use TraversalUtil which "fixes" parent?
		mdp.getStylesInUse();
		
		// Might be set?
		if (p.getParent()!=null) {
			Assert.assertEquals(body, p.getParent());
		}
		
		
	}
	
}
