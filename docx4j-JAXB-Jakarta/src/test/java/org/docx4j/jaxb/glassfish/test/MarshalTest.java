package org.docx4j.jaxb.glassfish.test;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.wml.P;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

public class MarshalTest {

	@Test
	public void JAXBImplementationTest() throws JAXBException {

		java.lang.ClassLoader classLoader = NamespacePrefixMapperUtils.class.getClassLoader();
		JAXBContext testContext = JAXBContext.newInstance("org.docx4j.relationships",classLoader );
		
		System.out.println(testContext.getClass().getName());
		
        assertEquals("com.sun.xml.bind.v2.runtime.JAXBContextImpl", testContext.getClass().getName() );
	}
	
	@Test
	public void NamespaceMappingTest() {

		P p = Context.getWmlObjectFactory().createP();
            
        String marshalResult = XmlUtils.marshaltoString(p);
        //System.out.println(marshalResult);
        
        assertTrue(marshalResult.contains("xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main"));
        
        try {
        	Object o = XmlUtils.unmarshalString(marshalResult);
        } catch (Exception e) {
        	
        }
		System.out.println(o.getClass().getName());
        
        
	}
	
}
