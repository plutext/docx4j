package org.docx4j;

import static org.junit.Assert.*;

import javax.xml.bind.JAXBElement;

import junit.framework.Assert;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.Test;

public class XmlUtilsDeepCopyTest {

	/**
	 * Test for https://github.com/plutext/docx4j/issues/212
	 * 
	 * With v3.3.0 and 3.3.1, deepCopy of a JAXBElement was broken. 
	 * 
	 * With moxY:
	 * 
	 *  java.lang.IllegalArgumentException: javax.xml.bind.UnmarshalException
	 *  
		 - with linked exception:
		[Exception [EclipseLink-25007] (Eclipse Persistence Services - 2.5.2.v20140319-9ad6abd): org.eclipse.persistence.exceptions.XMLMarshalException
		Exception Description: A descriptor for class javax.xml.bind.JAXBElement was not found in the project.  
		For JAXB, if the JAXBContext was bootstrapped using TypeMappingInfo[] you must call a marshal method that accepts TypeMappingInfo 
		as an input parameter.]

	 * With Sun/Oracle, you got a JAXBElement, but null value

	 */
	@Test
	public void testIssue212() {
		
		CTBookmark bookmark = Context.getWmlObjectFactory().createCTBookmark();
		JAXBElement<CTBookmark> el =Context.getWmlObjectFactory().createBodyBookmarkStart(bookmark);
		
		Object o = XmlUtils.deepCopy(el);
		
		//System.out.println(XmlUtils.marshaltoString(o));
		
		Assert.assertSame(el.getClass(), o.getClass());
		Assert.assertSame(el.getValue().getClass(), ((JAXBElement)o).getValue().getClass());
	}
	
	@Test
	public void testSimpleP() {

		P p = new P();
		p.getContent().add( new R());
		
		Object o = XmlUtils.deepCopy(p);
		
		Assert.assertSame(p.getClass(), o.getClass());
		Assert.assertSame(p.getContent().get(0).getClass(), ((P)o).getContent().get(0).getClass());
		
		
	}

}
