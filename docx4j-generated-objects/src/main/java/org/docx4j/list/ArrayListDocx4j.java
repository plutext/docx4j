package org.docx4j.list;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.wml.Body;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTCustomXmlBlock;
import org.docx4j.wml.CTCustomXmlRun;
import org.docx4j.wml.CTFtnEdn;
import org.docx4j.wml.CTSdtContentRun;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.Comments;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.RunIns;
import org.docx4j.wml.SdtContent;
import org.docx4j.wml.SdtContentBlock;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;
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
 *     <li>Refuses a child that is never legal under the list's parent (a run in the body, Text in a paragraph, a
 *     cell in a table; CR-064), with the fix in the message.</li>
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
	
	
	/**
	 * Refuses a child that is never legal under this list's parent (CR-064, D1 to D7), at the point of the mistake
	 * and with the fix in the message, instead of leaving it to Word's "unreadable content" repair far from the
	 * line that caused it. A wrapped child ({@code JAXBElement}) is tested by its value (D1). Each rule names a
	 * class the schema never allows under that parent, so a legal document cannot trip one on load; the lists
	 * stay otherwise open ("refuse the known-wrong, never allow-only": mc:AlternateContent, range markup and the
	 * rest are not this method's business). The parent sets are: block-level containers (Body, Tc,
	 * SdtContentBlock, CTTxbxContent, Hdr, Ftr, CTFtnEdn, Comments.Comment, CTCustomXmlBlock), run-level containers
	 * (P, P.Hyperlink, CTSimpleField, RunIns, RunDel, CTSmartTagRun, CTCustomXmlRun, CTSdtContentRun), R, Tbl, Tr.
	 *
	 * @throws IllegalArgumentException for a misplaced child; the message says what to do instead
	 */
	private void checkParent(E e0) {

		if (parent==null) {

			log.warn("null parent. how?");
			if (log.isDebugEnabled()) {
				log.debug("Null parent", new Throwable());
			}
			return;
		}

		Object e = e0 instanceof JAXBElement ? ((JAXBElement<?>) e0).getValue() : e0;

		if (parent instanceof P || isOtherRunContainer(parent)) {
			if (e instanceof Tbl) {
				throw new IllegalArgumentException("You can't add a table inside " + describe(parent) + "; it should be a sibling of the paragraph.");
			} else if (e instanceof P) {
				throw new IllegalArgumentException("You can't nest a paragraph inside " + describe(parent) + ".");
			} else if (e instanceof Text) {
				throw new IllegalArgumentException("You can't add Text directly to " + describe(parent) + "; add it to a run (R) and add that.");
			} else if (e instanceof Drawing) {
				throw new IllegalArgumentException("You can't add a Drawing directly to " + describe(parent) + "; add it to a run (R) and add that.");
			} else if (e instanceof Br) {
				throw new IllegalArgumentException("You can't add a Br directly to " + describe(parent) + "; add it to a run (R) and add that.");
			} else if (e instanceof PPr) {
				throw new IllegalArgumentException("You can't add PPr to a paragraph's content; use setPPr.");
			} else if (e instanceof String) {
				throw new IllegalArgumentException("You can't add a String to " + describe(parent) + "; put it in a Text, in a run (R), and add the run.");
			}

		} else if (parent instanceof R) {
			if (e instanceof R) {
				throw new IllegalArgumentException("You can't nest a run inside a run; add it to the paragraph.");
			} else if (e instanceof P) {
				throw new IllegalArgumentException("You can't nest a paragraph inside a run.");
			} else if (e instanceof Tbl) {
				throw new IllegalArgumentException("You can't add a table inside a run.");
			} else if (e instanceof RPr) {
				throw new IllegalArgumentException("You can't add RPr to a run's content; use setRPr.");
			} else if (e instanceof String) {
				throw new IllegalArgumentException("You can't add a String to a run; put it in a Text (wrapped by the ObjectFactory's createRT) and add that.");
			}

		} else if (isBlockContainer(parent)) {
			if (e instanceof R) {
				throw new IllegalArgumentException("You can't add a run (R) directly to " + describe(parent) + "; add it to a paragraph (P) and add that.");
			} else if (e instanceof Text) {
				throw new IllegalArgumentException("You can't add Text directly to " + describe(parent) + "; add it to a run (R) in a paragraph (P) and add the paragraph.");
			} else if (e instanceof Drawing) {
				throw new IllegalArgumentException("You can't add a Drawing directly to " + describe(parent) + "; add it to a run (R) in a paragraph (P) and add the paragraph.");
			} else if (e instanceof Br) {
				throw new IllegalArgumentException("You can't add a Br directly to " + describe(parent) + "; add it to a run (R) in a paragraph (P) and add the paragraph.");
			} else if (e instanceof Tc) {
				throw new IllegalArgumentException("You can't add a table cell (Tc) to " + describe(parent) + "; a cell goes in a row (Tr), in a table (Tbl).");
			} else if (e instanceof Tr) {
				throw new IllegalArgumentException("You can't add a table row (Tr) to " + describe(parent) + "; a row goes in a table (Tbl).");
			} else if (e instanceof SectPr) {
				throw new IllegalArgumentException("You can't add SectPr to " + describe(parent) + "'s content; use Body.setSectPr, or PPr.setSectPr on the paragraph that ends the section.");
			} else if (e instanceof TcPr) {
				throw new IllegalArgumentException("You can't add TcPr to a cell's content; use setTcPr.");
			} else if (e instanceof String) {
				throw new IllegalArgumentException("You can't add a String to " + describe(parent) + "; put it in a Text, in a run (R), in a paragraph (P), and add the paragraph.");
			}

		} else if (parent instanceof Tbl) {
			if (e instanceof Tc) {
				throw new IllegalArgumentException("You can't add a table cell (Tc) directly to a table; add it to a row (Tr) and add that.");
			} else if (e instanceof P || e instanceof R) {
				throw new IllegalArgumentException("You can't add a " + (e instanceof P ? "paragraph" : "run") + " directly to a table; add it to a cell (Tc) in a row (Tr).");
			} else if (e instanceof Tbl) {
				throw new IllegalArgumentException("You can't nest a table directly in a table; a nested table goes in a cell (Tc).");
			} else if (e instanceof TblPr) {
				throw new IllegalArgumentException("You can't add TblPr to a table's content; use setTblPr.");
			} else if (e instanceof String) {
				throw new IllegalArgumentException("You can't add a String to a table; text goes in a run (R) in a paragraph (P) in a cell (Tc).");
			}

		} else if (parent instanceof Tr) {
			if (e instanceof P || e instanceof R) {
				throw new IllegalArgumentException("You can't add a " + (e instanceof P ? "paragraph" : "run") + " directly to a table row; add it to a cell (Tc) and add that.");
			} else if (e instanceof Tbl) {
				throw new IllegalArgumentException("You can't nest a table in a table row; a nested table goes in a cell (Tc).");
			} else if (e instanceof TrPr) {
				throw new IllegalArgumentException("You can't add TrPr to a row's content; use setTrPr.");
			} else if (e instanceof String) {
				throw new IllegalArgumentException("You can't add a String to a table row; text goes in a run (R) in a paragraph (P) in a cell (Tc).");
			}
		}

		if (parent instanceof SdtContent && e instanceof SdtContent) {
			throw new IllegalArgumentException("You can't nest a bare SdtContent directly inside SdtContent; nest an Sdt (SdtBlock or SdtRun) that holds it.");
		}
	}

	private static boolean isBlockContainer(Object p) {
		return p instanceof Body || p instanceof Tc || p instanceof SdtContentBlock || p instanceof CTTxbxContent
				|| p instanceof Hdr || p instanceof Ftr || p instanceof CTFtnEdn || p instanceof Comments.Comment
				|| p instanceof CTCustomXmlBlock;
	}

	private static boolean isOtherRunContainer(Object p) {
		return p instanceof P.Hyperlink || p instanceof CTSimpleField || p instanceof RunIns || p instanceof RunDel
				|| p instanceof CTSmartTagRun || p instanceof CTCustomXmlRun || p instanceof CTSdtContentRun;
	}

	/** the parent, for a message: what a reader of the code calls it */
	private static String describe(Object p) {
		if (p instanceof P) return "a paragraph";
		if (p instanceof P.Hyperlink) return "a hyperlink";
		if (p instanceof CTSimpleField) return "a field (fldSimple)";
		if (p instanceof RunIns) return "an insertion (ins)";
		if (p instanceof RunDel) return "a deletion (del)";
		if (p instanceof CTSmartTagRun) return "a smart tag";
		if (p instanceof CTCustomXmlRun) return "a run-level customXml";
		if (p instanceof CTSdtContentRun) return "run-level sdt content";
		if (p instanceof Body) return "the body";
		if (p instanceof Tc) return "a table cell (Tc)";
		if (p instanceof SdtContentBlock) return "block-level sdt content";
		if (p instanceof CTTxbxContent) return "a text box (txbxContent)";
		if (p instanceof Hdr) return "a header";
		if (p instanceof Ftr) return "a footer";
		if (p instanceof CTFtnEdn) return "a footnote or endnote";
		if (p instanceof Comments.Comment) return "a comment";
		if (p instanceof CTCustomXmlBlock) return "a block-level customXml";
		return p.getClass().getSimpleName();
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
