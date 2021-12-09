package org.docx4j.convert.out.fo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

public class Context {
	
	private static Logger log = LoggerFactory.getLogger(Context.class);
	
	
	private static JAXBContext jcXslFo;
	
	public static JAXBContext getXslFoContext() {
		if (jcXslFo==null) {
			try {	
				Context tmp = new Context();
				java.lang.ClassLoader classLoader = tmp.getClass().getClassLoader();

				jcXslFo = JAXBContext.newInstance("org.plutext.jaxb.xslfo",classLoader );
				
			} catch (JAXBException ex) {
	      log.error("Cannot determine XSL-FO context", ex);
			}						
		}
		return jcXslFo;		
	}
	

}
