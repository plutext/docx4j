package org.xlsx4j.sml;

import java.util.ArrayList;
import java.util.Collection;

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
	
    /**
     * Returns <tt>true</tt> if this list contains the specified element
     * (or a JAXBElement containing it).
     *
     * @param o element whose presence in this list is to be tested
     * @return <tt>true</tt> if this list contains the specified element
	 * @since 8.1.4
     */
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * Returns the index of the first occurrence of the specified element
     * (or a JAXBElement containing it) in this list, 
     * or -1 if this list does not contain the element.
	 * @since 8.1.4
     */
    public int indexOf(Object o) {
        if (o == null) {
        	return super.indexOf(o);
        } else {
            for (int i = 0; i < size(); i++) {
                if (o.equals(get(i))) {
                    return i;
                } else if (get(i) instanceof javax.xml.bind.JAXBElement
                		&& o.equals(((JAXBElement)get(i)).getValue())) {
                            return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified element
     * (or a JAXBElement containing it) in this list, or -1 if this list does not contain the element.
	 * @since 8.1.4
     */
    public int lastIndexOf(Object o) {
        if (o == null) {
        	return super.lastIndexOf(o);
        } else {
            for (int i = size()-1; i >= 0; i--) {
                if (o.equals(get(i))) {
                    return i;
                } else if (get(i) instanceof javax.xml.bind.JAXBElement
                		&& o.equals(((JAXBElement)get(i)).getValue())) {
                            return i;
                }
            }
        }
        return -1;
    }
	
    /**
     * Replaces the element at the specified position in this list with
     * the specified element,
     * checking for common content errors, and setting parent correctly.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
	 * @since 8.1.4
     */
    public E set(int index, E e) {

		checkParent(e);
		if (e instanceof JAXBElement /* workaround */) {
			setParent( ((JAXBElement)e).getValue() );
		} else {
			setParent(e);
		}
		return super.set(index, e);
    }    
	
    /**
     * Appends the specified element to the end of this list,
     * checking for common content errors, and setting parent correctly.
     *
     * @param e element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
	@Override
	public boolean add(E e) {
		
		checkParent(e);
		
		if (e instanceof JAXBElement /* workaround */) {
			
			setParent( ((JAXBElement)e).getValue() );
			
		} else {
			
			setParent(e);
		}
		
    	
		return super.add(e);
    }
	
    /**
     * Inserts the specified element at the specified position in this
     * list, checking for common content errors, and setting parent correctly.
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException {@inheritDoc}
	 * @since 8.1.4
     */
	@Override
    public void add(int index, E e) {

		checkParent(e);
		if (e instanceof JAXBElement /* workaround */) {
			setParent( ((JAXBElement)e).getValue() );
		} else {
			setParent(e);
		}
		
		super.add(index, e);
    }
	
    /**
     * Removes the first occurrence of the specified element 
     * (or a JAXBElement containing it) from this list,
     * if it is present.  If the list does not contain the element, it is
     * unchanged.  
     *
     * @param o element to be removed from this list, if present
     * @return <tt>true</tt> if this list contained the specified element
	 * @since 8.1.4
     */
    public boolean remove(Object o) {
        if (o == null) {
            super.remove(o);
        } else {
            for (int i = 0; i < size(); i++)
            if (o.equals(get(i))) {
                remove(i);
                return true;
            } else if (get(i) instanceof javax.xml.bind.JAXBElement
            		&& o.equals(((JAXBElement)get(i)).getValue())) {
                remove(i);
                return true;
            }
        }
        return false;
    }
	

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, checking for common content errors, and setting parent correctly.
     *
     * @param c collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
	 * @since 8.1.4
     */
	@Override
    public boolean addAll(Collection<? extends E> c) {	
		
		for (Object o : c) {
			checkParent((E)o);
			if (o instanceof JAXBElement /* workaround */) {
				setParent( ((JAXBElement)o).getValue() );
			} else {
				setParent(o);
			}
		}
		return super.addAll(c);
	}
	
    /**
     * Inserts all of the elements in the specified collection into this
     * list (checking for common content errors etc), starting at the specified position.  
     *
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param c collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException if the specified collection is null
	 * @since 8.1.4
     */
	@Override
    public boolean addAll(int index, Collection<? extends E> c) {
		
		for (Object o : c) {
			checkParent((E)o);
			if (o instanceof JAXBElement /* workaround */) {
				setParent( ((JAXBElement)o).getValue() );
			} else {
				setParent(o);
			}
		}
		return super.addAll(index, c);

    }
	
    public boolean removeAll(Collection<?> c) {
    	// For now, just warn that behaviour is inconsistent with other remove methods
    	log.warn("This method won't remove an object if it is wrapped in a JAXBElement");
    	return super.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
    	// For now, just warn that behaviour is inconsistent with other remove methods
    	log.warn("This method won't retain an object if it is wrapped in a JAXBElement");
        return super.retainAll(c);
    }
	
	
	private void checkParent(E e) {
		
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
	}
	
	private void setParent(Object o) {

		if (parent!=null
				&& (o instanceof Child)) {
			((Child)o).setParent(parent);
		}
		
	}
	
	/**
	 * @return
	 * @since 8.1.4
	 */
	public Object getParent() {
		return parent; // can be useful to understand where in the content tree you are
	}

}
