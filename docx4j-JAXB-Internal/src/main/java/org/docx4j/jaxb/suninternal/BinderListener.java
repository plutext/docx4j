package org.docx4j.jaxb.suninternal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.docx4j.jaxb.BinderListenerInterface;

import javax.xml.bind.Binder;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Unmarshaller.Listener;

/**
 * @author jharrop
 * @since 8.4.7
 */
public class BinderListener implements BinderListenerInterface {

	public void setListener(Binder binder, Listener listener) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		
		com.sun.xml.internal.bind.v2.runtime.BinderImpl binderImpl = (com.sun.xml.internal.bind.v2.runtime.BinderImpl )binder;
		
		Method method = binderImpl.getClass().getDeclaredMethod("getUnmarshaller");
		method.setAccessible(true);
		Unmarshaller u  = (Unmarshaller)method.invoke(binderImpl);	
		u.setListener(listener);
	}

}
