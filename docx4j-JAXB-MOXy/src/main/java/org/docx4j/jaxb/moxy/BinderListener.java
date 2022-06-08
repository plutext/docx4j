package org.docx4j.jaxb.moxy;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.docx4j.jaxb.BinderListenerInterface;
import org.eclipse.persistence.internal.oxm.record.SAXUnmarshaller;
import org.eclipse.persistence.jaxb.JAXBBinder;
import org.eclipse.persistence.oxm.XMLBinder;
import org.eclipse.persistence.oxm.XMLUnmarshaller;

import javax.xml.bind.Binder;
import javax.xml.bind.Unmarshaller.Listener;

/**
 * @author jharrop
 * @since 11.4.7
 */
public class BinderListener implements BinderListenerInterface {

	public void setListener(Binder binder, Listener listener) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {

		MoxyUnmarshalListener moxyListener = new MoxyUnmarshalListener(listener);
		/*
			package org.eclipse.persistence.jaxb;
			
			public class JAXBBinder extends Binder {
			
			    private XMLBinder xmlBinder;
    		 */
		
		// get xmlBinder field
		org.eclipse.persistence.jaxb.JAXBBinder jaxbBinder = (JAXBBinder)binder; 
		Field f = jaxbBinder.getClass().getDeclaredField("xmlBinder"); //NoSuchFieldException
		f.setAccessible(true);
		XMLBinder binderImpl = (XMLBinder)f.get(jaxbBinder);
		
		Field f2 = binderImpl.getClass().getDeclaredField("unmarshaller"); //NoSuchFieldException
		f2.setAccessible(true);
		org.eclipse.persistence.oxm.XMLUnmarshaller u  = (XMLUnmarshaller)f2.get(binderImpl);	
		u.setUnmarshalListener(moxyListener);
		
		// there is also SAXUnmarshaller, which wraps an unmarshaller
		Field f3 = binderImpl.getClass().getDeclaredField("saxUnmarshaller"); //NoSuchFieldException
		f3.setAccessible(true);
		org.eclipse.persistence.internal.oxm.record.SAXUnmarshaller saxUnmarshaller  = (SAXUnmarshaller)f3.get(binderImpl);
		
		Field f4 = saxUnmarshaller.getClass().getDeclaredField("xmlUnmarshaller"); //NoSuchFieldException
		f4.setAccessible(true);
		org.eclipse.persistence.internal.oxm.Unmarshaller u2  = (org.eclipse.persistence.internal.oxm.Unmarshaller)f2.get(binderImpl);	
		u2.setUnmarshalListener(moxyListener);
		
	}

}
