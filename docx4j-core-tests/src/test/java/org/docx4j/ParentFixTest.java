package org.docx4j;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.finders.ClassFinder;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.Document;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.Test;
import org.jvnet.jaxb2_commons.ppp.Child;

/**
 * See also org.docx4j.wml.ParentTest
 *
 */
public class ParentFixTest {

	@Test
	public void testBodyCreated() throws InvalidFormatException  {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		mdp.addParagraphOfText("hello world");

		Document doc = mdp.getJaxbElement();
		
		Body body = doc.getBody();
		
		// Set explicitly in Document
		Assert.assertEquals(doc, body.getParent());
		
        // Now, check that traversing doesn't change anything
        (new TestCallback()).walkJAXBElements(doc);
		
		// No change
		Assert.assertEquals(doc, body.getParent());
		
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
		
        // Now, check that traversing doesn't change anything
        (new TestCallback()).walkJAXBElements(doc);
		
		// No change
		Assert.assertEquals(doc, body.getParent());
		
	}

	@Test
	public void testBodyChildrenCreated() throws InvalidFormatException  {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		mdp.addParagraphOfText("hello world");

		Document doc = mdp.getJaxbElement();
		
		Body body = doc.getBody();
		P p = (P)body.getContent().get(0);
		
		Assert.assertEquals(body, p.getParent());
		
        // Now, check that traversing doesn't change anything
        (new TestCallback()).walkJAXBElements(doc);
		
		Assert.assertEquals(body, p.getParent());
		
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
		
		Assert.assertEquals(body, p.getParent());
		
        // Now, check that traversing doesn't change anything
        (new TestCallback()).walkJAXBElements(doc);
		
		Assert.assertEquals(body, p.getParent());
		
		
	}
	
	/**
	 * Prior to 3.3.1, unmarshalling a String does set parent properly, except in the known case of JAXBElement,
	 * which a traverse fixes
	 * 
	 * With 3.3.1, ArrayListWml gets this right first time
	 * 
	 * @throws JAXBException
	 */
	@Test
	public void testUnmarshalString() throws JAXBException {
		
		String openXML = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\">"
	            + "<w:r>"
	                + "<w:t>some text</w:t>"
	            +"</w:r>"
	            + "<w:bookmarkStart w:id=\"0\" w:name=\"mybm\"/>"
	            + "<w:bookmarkEnd w:id=\"0\"/>"
	        +"</w:p>";
		
		P p = (P)XmlUtils.unmarshalString(openXML);				
		R r = (R)p.getContent().get(0);	
		
		// Parent of run is set correctly
		if (r.getParent()==null) {
			System.out.println("null parent");
			Assert.fail("null parent");
		} else {
			System.out.println(r.getParent().getClass());	
			Assert.assertEquals(P.class, r.getParent().getClass());
		}
		
		// Parent of a JAXBElement?
		Child o2 = (Child)XmlUtils.unwrap(p.getContent().get(1));	
		if (o2.getParent()==null) {
			System.out.println("null parent");
			Assert.fail("null parent");
			
		} else if (o2.getParent() instanceof JAXBElement) {

			// 3.3.0: Sad but true
			// <= 3.3.0: org.docx4j.wml.CTBookmark has parent class javax.xml.bind.JAXBElement - NOT GOOD
			Assert.assertEquals(JAXBElement.class, o2.getParent().getClass() );
			
			// Traverse to fix
			// org.docx4j.wml.CTBookmark has parent class org.docx4j.wml.P - GOOD
			List foo = new ArrayList();
			foo.add(p);
			ClassFinder finder = new ClassFinder(FldChar.class); // whatever
			new TraversalUtil(foo, finder);
			
			o2 = (Child)XmlUtils.unwrap(p.getContent().get(1));	
			if (o2.getParent()==null) {
				System.out.println("null parent");
				Assert.fail("null parent");
			} else {
				System.out.println(o2.getClass().getName() + " has CORRECTED parent " + o2.getParent().getClass());			
				Assert.assertEquals(o2.getParent().getClass(),  P.class);
			}					
			
		} else /* since 3.3.1 */ {
			
			System.out.println(o2.getClass().getName() + " has parent " + o2.getParent().getClass());
			Assert.assertEquals(P.class, o2.getParent().getClass() );
		}
		
		
	}
	
	/**
	 * Prior to 3.3.1, Objects added manually don't have their parent set automatically.
	 * But traversal fixes this. 
	 * 
	 * With 3.3.1, ArrayListWml gets this right first time
	 * 
	 */
	@Test
	public void testCreatedViaFactory() {
		
		org.docx4j.wml.ObjectFactory wmlObjectFactory = new org.docx4j.wml.ObjectFactory();

		P p = wmlObjectFactory.createP(); 

		// Create object for r
		    R r = wmlObjectFactory.createR(); 
		    p.getContent().add( r); 
		        // Create object for t (wrapped in JAXBElement) 
		        Text text = wmlObjectFactory.createText(); 
		        JAXBElement<org.docx4j.wml.Text> textWrapped = wmlObjectFactory.createRT(text); 
		        r.getContent().add( textWrapped); 
		            text.setValue( "some text"); 

		            // Create object for bookmarkStart (wrapped in JAXBElement) 
		    CTBookmark bookmark = wmlObjectFactory.createCTBookmark(); 
		    JAXBElement<org.docx4j.wml.CTBookmark> bookmarkWrapped = wmlObjectFactory.createPBookmarkStart(bookmark); 
		    p.getContent().add( bookmarkWrapped); 
		        bookmark.setName( "mybm"); 
		        bookmark.setId( BigInteger.valueOf( 0) ); 
		    // Create object for bookmarkEnd (wrapped in JAXBElement) 
		    CTMarkupRange markuprange = wmlObjectFactory.createCTMarkupRange(); 
		    JAXBElement<org.docx4j.wml.CTMarkupRange> markuprangeWrapped = wmlObjectFactory.createPBookmarkEnd(markuprange); 
		    p.getContent().add( markuprangeWrapped); 
		        markuprange.setId( BigInteger.valueOf( 0) ); 

		        // Its not set automatically
//				if (r.getParent()==null) {
//					System.out.println("null parent");
//					Assert.fail("null parent");
//				} else 
				{
					System.out.println(r.getParent().getClass());	
					Assert.assertEquals(P.class, r.getParent().getClass());
				}

		// Traverse to fix: uncomment for 3.3.0
//		List foo = new ArrayList();
//		foo.add(p);
//		ClassFinder finder = new ClassFinder(FldChar.class); // whatever
//		new TraversalUtil(foo, finder);
		
		// Test bookmark
		Child o2 = (Child)XmlUtils.unwrap(p.getContent().get(1));	
		if (o2.getParent()==null) {
			System.out.println("null parent");
			Assert.fail("null parent");
		} else {
			System.out.println(o2.getClass().getName() + " has parent " + o2.getParent().getClass());			
			Assert.assertEquals(o2.getParent().getClass(),  P.class);
		}		

		// Test run
		o2 = (Child)XmlUtils.unwrap(p.getContent().get(0));	// run
		if (o2.getParent()==null) {
			System.out.println("null parent");
			Assert.fail("null parent");
		} else {
			System.out.println(o2.getClass().getName() + " has parent " + o2.getParent().getClass());			
			Assert.assertEquals(o2.getParent().getClass(),  P.class);
		}		

		// Traverse: shouldn't change anything 
		List foo = new ArrayList();
		foo.add(p);
		ClassFinder finder = new ClassFinder(FldChar.class); // whatever
		new TraversalUtil(foo, finder);
		
		// Test bookmark
		o2 = (Child)XmlUtils.unwrap(p.getContent().get(1));	
		if (o2.getParent()==null) {
			System.out.println("null parent");
			Assert.fail("null parent");
		} else {
			System.out.println(o2.getClass().getName() + " has parent " + o2.getParent().getClass());			
			Assert.assertEquals(o2.getParent().getClass(),  P.class);
		}		

		// Test run
		o2 = (Child)XmlUtils.unwrap(p.getContent().get(0));	// run
		if (o2.getParent()==null) {
			System.out.println("null parent");
			Assert.fail("null parent");
		} else {
			System.out.println(o2.getClass().getName() + " has parent " + o2.getParent().getClass());			
			Assert.assertEquals(o2.getParent().getClass(),  P.class);
		}		
		
	}

	class TestCallback extends CallbackImpl {

		@Override
		public List<Object> apply(Object o) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}	
}
