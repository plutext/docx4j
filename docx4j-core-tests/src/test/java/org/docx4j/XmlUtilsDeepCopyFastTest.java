package org.docx4j;

import jakarta.xml.bind.JAXBElement;
import org.junit.Assert;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.Test;

import javax.xml.namespace.QName;

public class XmlUtilsDeepCopyFastTest {

	@Test
	public void deepCopySimpleParagraph() {
		// Create a paragraph with a run with a text element wrapped in a JAXBElement
		P p = new P();
		R r = new R();
		p.getContent().add(r);
		Text t = new Text();
		t.setValue("Hello world");
		JAXBElement<Text> jaxbElement = new JAXBElement<>(new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "t"), Text.class, R.class, t);
		r.getContent().add(jaxbElement);

		// Copy the paragraph
		P copy = XmlUtils.deepCopyFast(p);

		// Ensure it and all its children were actually copied
		Assert.assertNotSame("p was not copied, it is the same instance as the original!", p, copy);
		R copyR = (R) copy.getContent().get(0);
		Assert.assertNotSame("r was not copied, it is the same instance as the original!", r, copyR);
		JAXBElement<Text> copyJAXBElement = (JAXBElement<Text>) copyR.getContent().get(0);
		Assert.assertNotSame("jaxbElement was not copied, it is the same instance as the original!", jaxbElement, copyJAXBElement);
		Text copyT = copyJAXBElement.getValue();
		Assert.assertNotSame("t was not copied, it is the same instance as the original!", t, copyT);
		Assert.assertEquals("Hello world", copyT.getValue());
	}
}
