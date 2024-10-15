package org.docx4j.jaxb.moxy;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.docx4j.jaxb.BinderListenerInterface;
import org.eclipse.persistence.internal.oxm.record.SAXUnmarshaller;
import org.eclipse.persistence.jaxb.JAXBBinder;
import org.eclipse.persistence.oxm.XMLBinder;
import org.eclipse.persistence.oxm.XMLUnmarshaller;

import jakarta.xml.bind.Binder;
import jakarta.xml.bind.Unmarshaller.Listener;

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
		
		/* Might throw:
			java.lang.reflect.InaccessibleObjectException: Unable to make field private org.eclipse.persistence.oxm.XMLBinder org.eclipse.persistence.jaxb.JAXBBinder.xmlBinder accessible: module org.eclipse.persistence.moxy does not "opens org.eclipse.persistence.jaxb" to module org.docx4j.JAXB_MOXy
				at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:354)
				at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:297)
				at java.base/java.lang.reflect.Field.checkCanSetAccessible(Field.java:178)
				at java.base/java.lang.reflect.Field.setAccessible(Field.java:172)
				
			See now https://github.com/eclipse-ee4j/eclipselink/issues/2283
			"Enable setUnmarshalListener on XMLBinder"
			
			Workaround to avoid this error, launch your VM with:  
			--add-opens org.eclipse.persistence.moxy/org.eclipse.persistence.jaxb=org.docx4j.JAXB_MOXy
			--add-opens org.eclipse.persistence.core/org.eclipse.persistence.oxm=org.docx4j.JAXB_MOXy
			--add-opens org.eclipse.persistence.core/org.eclipse.persistence.internal.oxm.record=org.docx4j.JAXB_MOXy			
		 */
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
