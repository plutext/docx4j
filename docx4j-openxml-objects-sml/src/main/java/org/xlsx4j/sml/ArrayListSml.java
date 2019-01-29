package org.xlsx4j.sml;

import java.util.ArrayList;

import javax.xml.bind.JAXBElement;

import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection which sets parent correctly
 * 
 * @author jharrop
 *
 * @param <E>
 * 
 * @since 3.3.3
 */
public class ArrayListSml<E> extends ArrayList<E> {
	
	private static Logger log = LoggerFactory.getLogger(ArrayListSml.class);	
	
	public ArrayListSml(Object o) {
		this.parent = o;
	}
	
	private ArrayListSml() {}
	
	private Object parent = null;
	
	@Override
	public boolean add(E e) {
		
		if (parent==null) {
			
			log.warn("null parent. how?");
			if (log.isDebugEnabled()) {
				log.debug("Null parent", new Throwable());
			}
			
		} else {
			
			// Sanity check common user error
//			if (parent instanceof SheetData) {				
//			}
//			
//			if (parent instanceof Row) {
//			}
			
		}
		
		if (e instanceof JAXBElement /* workaround */) {
			
			setParent( ((JAXBElement)e).getValue() );
			
		} else {
			
			setParent(e);
		}
		
    	
		return super.add(e);
    }
	
	private void setParent(Object o) {

		if (parent!=null
				&& (o instanceof Child)) {
			((Child)o).setParent(parent);
		}
		
	}
	

}
