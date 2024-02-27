package org.docx4j.jaxb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * @author jharrop
 * @since 11.4.7
 */
public class BinderListenerUtils {
	
	private static Logger log = LoggerFactory.getLogger(BinderListenerUtils.class);		
	
	private static JAXBContext testContext;
	
	private static BinderListenerInterface binderListener;
	
	private static boolean haveTried = false;
	
	public static synchronized BinderListenerInterface getBinderListener() throws JAXBException {
		
		if (binderListener!=null) return binderListener;
		
		if (haveTried) return null;
		// will be true soon..
		haveTried = true;
				
		if (testContext==null) {
			java.lang.ClassLoader classLoader = BinderListenerUtils.class.getClassLoader();
			testContext = JAXBContext.newInstance("org.docx4j.relationships",classLoader );
		}
		
		if (testContext==null) {
			throw new JAXBException("Couldn't create context for org.docx4j.relationships.  Everything is broken!");
		}
		
		if (log.isDebugEnabled()) {
			log.debug("testContext: " + testContext.getClass().getName());
		}
		
		if (testContext.getClass().getName().equals("org.eclipse.persistence.jaxb.JAXBContext")) {
			log.info("Using MOXy NamespacePrefixMapper");
			try {
				Class c = Class.forName("org.docx4j.jaxb.moxy.BinderListener");
				binderListener = (BinderListenerInterface)c.newInstance();
				return binderListener;
			} catch (Exception e) {
				throw new JAXBException("Can't create org.docx4j.jaxb.moxy.BinderListener", e);
			}
		}
		if (testContext.getClass().getName().equals("com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl")) {
			log.info("Using com.sun.xml.internal BinderListener");
			try {
				Class c = Class.forName("org.docx4j.jaxb.suninternal.BinderListener");
				binderListener = (BinderListenerInterface)c.newInstance();
				return binderListener;
			} catch (Exception e) {
				throw new JAXBException("Can't create org.docx4j.jaxb.suninternal.BinderListener", e);
			}
		}		
		Marshaller m=testContext.createMarshaller();		
		return tryUsingRI(m);			

	}


	private static BinderListenerInterface tryUsingRI(Marshaller m)
			throws JAXBException {
		try {
			Class c = Class.forName("org.docx4j.jaxb.ri.BinderListener");
			binderListener = (BinderListenerInterface)c.newInstance();
			
			log.info("Using ri.BinderListener, which is suitable for the JAXB RI");
			return binderListener;
		} catch (Exception e) {
			//log.error("JAXB: Can't instantiate JAXB Reference Implementation", e);
			throw new JAXBException("JAXB: Can't instantiate JAXB Reference Implementation", e);
		}
	}

	
    
    
}
