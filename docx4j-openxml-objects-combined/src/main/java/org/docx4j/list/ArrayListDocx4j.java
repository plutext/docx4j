package org.docx4j.list;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.SdtContent;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
import org.jvnet.jaxb.lang.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>Collection type for docx4j OpenXML objects, replacing JAXB's default {@link ArrayList}.
 *
 * <p>Differences from {@link ArrayList}:
 * <ul>
 *     <li>Sets parent when possible. (I.e., ensures that the parent of any Child object added to the list is set to the
 *     object which has this list as a field.)</li>
 *     <li>Sanity checks some common user errors when adding the wrong type of object to the list.</li>
 * </ul>
 *
 * <p>We tell XJC to use this collection type in the generated classes by setting the {@code collectionType} attribute
 * in annotations in the XSDs or external JAXB binding files {@code (*.xjb)}. See
 * <a href="https://docs.oracle.com/javase/tutorial/jaxb/intro/custom.html">Customizing XML Bindings</a>.
 *
 * @param <E> the type of elements in this list
 * @since 12.0.0
 * @author jharrop
 */
public class ArrayListDocx4j<E> extends ArrayList<E> {

	private static final Logger log = LoggerFactory.getLogger(ArrayListDocx4j.class);

	public ArrayListDocx4j(Object p) {
		this.parent = p;
	}

	private ArrayListDocx4j() {}
	
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
                } else if (get(i) instanceof JAXBElement
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
                } else if (get(i) instanceof JAXBElement
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
     * @param e element to be stored at the specified position
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
     * @param e element to be inserted
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
            } else if (get(i) instanceof JAXBElement
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
    	log.info("This method won't remove an object if it is wrapped in a JAXBElement");
    	return super.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
    	// For now, just warn that behaviour is inconsistent with other remove methods
    	log.info("This method won't retain an object if it is wrapped in a JAXBElement");
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
			if (parent instanceof P) {
				if (e instanceof Tbl) {
					throw new RuntimeException("You can't add a table inside a paragraph; it should be a sibling");
				} else if (e instanceof P) {
					throw new RuntimeException("You can't nest a paragraph inside a paragraph.");
				} else if (e instanceof Text) {
					throw new RuntimeException("You can't add Text directly to a paragraph; add it to a run (R) and add that.");
				}
			}
			
			if (parent instanceof R) {
				if (e instanceof R) {
					throw new RuntimeException("You can't nest a run inside a run");
				} else if (e instanceof P) {
					throw new RuntimeException("You can't nest a paragraph inside a run.");
				} else if (e instanceof Tbl) {
					throw new RuntimeException("You can't add a table inside a run.");
				}
			}
			
			if (parent instanceof SdtContent) {
				if (e instanceof SdtContent) {
					throw new RuntimeException("You can't nest a bare SdtContent directly inside SdtContent");
				}
			}
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
