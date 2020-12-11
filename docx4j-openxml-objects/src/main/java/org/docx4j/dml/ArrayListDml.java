package org.docx4j.dml;


import java.util.ArrayList;

import javax.xml.bind.JAXBElement;

import org.jvnet.jaxb2_commons.ppp.Child;
//import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 6.1.1
 */
public class ArrayListDml<E> extends ArrayList<E> {
	
	private static Logger log = LoggerFactory.getLogger(ArrayListDml.class);	
	
	public ArrayListDml(Object p) {
		this.parent = p;
	}
	
	private ArrayListDml() {
		
		log.error("ArrayListDml constructor invoked without arg");
		throw new RuntimeException();		
	}
	
	private Object parent = null;
	
	@Override
	public boolean add(E e) {
		
		if (parent==null) {
			
			log.warn("null parent. how?");
			if (log.isDebugEnabled()) {
				log.debug("Null parent", new Throwable());
			}
			
		} 
		
		if (e instanceof JAXBElement /* workaround */) {
			
			setParent( ((JAXBElement)e).getValue() );
			
		} else {
			
			setParent(e);
		}
		
    	
		return super.add(e);
    }
	
	private void setParent(Object o) {

		if (parent!=null) {
			
			if (o instanceof Child) {
				((Child)o).setParent(parent);
			} else {
				log.warn(o.getClass().getName() + " does not implement Child");
			}
		}
	}
	

}
