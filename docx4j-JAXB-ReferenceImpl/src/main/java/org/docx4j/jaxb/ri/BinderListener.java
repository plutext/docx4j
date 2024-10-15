package org.docx4j.jaxb.ri;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.docx4j.jaxb.BinderListenerInterface;

import jakarta.xml.bind.Binder;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.Unmarshaller.Listener;

/**
 * @author jharrop
 * @since 11.4.7
 */
public class BinderListener implements BinderListenerInterface {

	public void setListener(Binder binder, Listener listener) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		
		org.glassfish.jaxb.runtime.v2.runtime.BinderImpl binderImpl = (org.glassfish.jaxb.runtime.v2.runtime.BinderImpl )binder;
		
		Method method = binderImpl.getClass().getDeclaredMethod("getUnmarshaller");
		method.setAccessible(true);
		/*
			java.lang.reflect.InaccessibleObjectException: Unable to make private org.glassfish.jaxb.runtime.v2.runtime.unmarshaller.UnmarshallerImpl org.glassfish.jaxb.runtime.v2.runtime.BinderImpl.getUnmarshaller() accessible: module org.glassfish.jaxb.runtime does not "opens org.glassfish.jaxb.runtime.v2.runtime" to module org.docx4j.JAXB_ReferenceImpl
				at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:354)
				at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:297)
				at java.base/java.lang.reflect.Method.checkCanSetAccessible(Method.java:200)
				at java.base/java.lang.reflect.Method.setAccessible(Method.java:194)
				
				See https://github.com/eclipse-ee4j/jaxb-ri/issues/1822
				"Enable setUnmarshalListener on Binder"
				
				Workaround to avoid this error, launch your VM with:  --add-opens org.glassfish.jaxb.runtime/org.glassfish.jaxb.runtime.v2.runtime=org.docx4j.JAXB_ReferenceImpl
			 */
		Unmarshaller u  = (Unmarshaller)method.invoke(binderImpl);	
		u.setListener(listener);
	}

}
