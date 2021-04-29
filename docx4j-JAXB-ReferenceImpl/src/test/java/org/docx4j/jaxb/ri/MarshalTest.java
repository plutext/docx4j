package org.docx4j.jaxb.ri;

import com.sun.xml.bind.v2.ContextFactory;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.wml.P;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class MarshalTest {

	@Test
	public void JAXBImplementationTest() throws JAXBException {

		java.lang.ClassLoader classLoader = NamespacePrefixMapperUtils.class.getClassLoader();
		JAXBContext testContext = ContextFactory.createContext("org.docx4j.relationships",classLoader, null);
		
        assertEquals("com.sun.xml.bind.v2.runtime.JAXBContextImpl", testContext.getClass().getName() );
	}
	
	@Test
	public void NamespaceMappingTest() {

		P p = Context.getWmlObjectFactory().createP();
            
        String marshalResult = XmlUtils.marshaltoString(p);
        //System.out.println(marshalResult);
        
        assertTrue(marshalResult.contains("xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main"));
            
	}
	
}
