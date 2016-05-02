package org.docx4j.wml;

import java.util.ArrayList;

import javax.xml.bind.JAXBElement;

import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection for P content; does 2 things above/beyond JAXB's default collection 
 * type (arrayList):
 * 
 * 1.  Sanity check common user errors adding the wrong stuff
 * 
 * 2.  Sets parent correctly
 * 
 * See further http://stackoverflow.com/questions/7924079/what-is-the-default-list-implementation-in-jaxb2-and-how-do-i-change-it
 * 
 * 
 * @author jharrop
 *
 * @param <E>
 * 
 * @since 3.3.1
 */
public class ArrayListWml<E> extends ArrayList<E> {
	
	private static Logger log = LoggerFactory.getLogger(ArrayListWml.class);	
	
	public ArrayListWml(Object p) {
		this.parent = p;
	}
	
	private ArrayListWml() {}
	
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
			if (parent instanceof P) {
				if (e instanceof Tbl) {
					throw new RuntimeException("You can't add a table inside a paragraph; it should be a sibling");
				} else if (e instanceof P) {
					throw new RuntimeException("You can't nest a paragraph inside a paragraph.");
				} else if (e instanceof Text) {
					throw new RuntimeException("You can't add Text directly to a paragraph; add it to a run (R) and add that.");
				}
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

		if (parent!=null
				&& (o instanceof Child)) {
			((Child)o).setParent(parent);
		}
		
	}
	

}
