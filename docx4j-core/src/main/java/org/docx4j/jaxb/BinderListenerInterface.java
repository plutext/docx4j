package org.docx4j.jaxb;

import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.Binder;
//import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Unmarshaller.Listener;

public interface BinderListenerInterface {
	
    /**
     * <p>
     * Register unmarshal event callback {@link Listener} with this {@link Unmarshaller}.
     * 
     * <p>
     * There is only one Listener per Unmarshaller. Setting a Listener replaces the previous set Listener.
     * One can unregister current Listener by setting listener to {@code null}.
     * 
     * @param listener  provides unmarshal event callbacks for this {@link Unmarshaller}
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     * @throws NoSuchFieldException 
     * @since 11.4.7
     */
    public void setListener(Binder binder, Listener listener) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException;
	

}
