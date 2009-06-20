package com.topologi.diffx.sequence;

/* ============================================================================
 * ARTISTIC LICENCE
 * 
 * Preamble
 * 
 * The intent of this document is to state the conditions under which a Package
 * may be copied, such that the Copyright Holder maintains some semblance of 
 * artistic control over the development of the package, while giving the users
 * of the package the right to use and distribute the Package in a more-or-less
 * customary fashion, plus the right to make reasonable modifications.
 *
 * Definitions:
 *  - "Package" refers to the collection of files distributed by the Copyright 
 *    Holder, and derivatives of that collection of files created through 
 *    textual modification.
 *  - "Standard Version" refers to such a Package if it has not been modified, 
 *    or has been modified in accordance with the wishes of the Copyright 
 *    Holder.
 *  - "Copyright Holder" is whoever is named in the copyright or copyrights 
 *    for the package.
 *  - "You" is you, if you're thinking about copying or distributing this 
 *    Package.
 *  - "Reasonable copying fee" is whatever you can justify on the basis of 
 *    media cost, duplication charges, time of people involved, and so on. 
 *    (You will not be required to justify it to the Copyright Holder, but only 
 *    to the computing community at large as a market that must bear the fee.)
 *  - "Freely Available" means that no fee is charged for the item itself, 
 *    though there may be fees involved in handling the item. It also means 
 *    that recipients of the item may redistribute it under the same conditions
 *    they received it.
 *
 * 1. You may make and give away verbatim copies of the source form of the 
 *    Standard Version of this Package without restriction, provided that you 
 *    duplicate all of the original copyright notices and associated 
 *    disclaimers.
 *
 * 2. You may apply bug fixes, portability fixes and other modifications 
 *    derived from the Public Domain or from the Copyright Holder. A Package 
 *    modified in such a way shall still be considered the Standard Version.
 *
 * 3. You may otherwise modify your copy of this Package in any way, provided 
 *    that you insert a prominent notice in each changed file stating how and 
 *    when you changed that file, and provided that you do at least ONE of the 
 *    following:
 * 
 *    a) place your modifications in the Public Domain or otherwise make them 
 *       Freely Available, such as by posting said modifications to Usenet or 
 *       an equivalent medium, or placing the modifications on a major archive 
 *       site such as ftp.uu.net, or by allowing the Copyright Holder to 
 *       include your modifications in the Standard Version of the Package.
 * 
 *    b) use the modified Package only within your corporation or organization.
 *
 *    c) rename any non-standard executables so the names do not conflict with 
 *       standard executables, which must also be provided, and provide a 
 *       separate manual page for each non-standard executable that clearly 
 *       documents how it differs from the Standard Version.
 * 
 *    d) make other distribution arrangements with the Copyright Holder.
 *
 * 4. You may distribute the programs of this Package in object code or 
 *    executable form, provided that you do at least ONE of the following:
 * 
 *    a) distribute a Standard Version of the executables and library files, 
 *       together with instructions (in the manual page or equivalent) on where
 *       to get the Standard Version.
 *
 *    b) accompany the distribution with the machine-readable source of the 
 *       Package with your modifications.
 * 
 *    c) accompany any non-standard executables with their corresponding 
 *       Standard Version executables, giving the non-standard executables 
 *       non-standard names, and clearly documenting the differences in manual 
 *       pages (or equivalent), together with instructions on where to get 
 *       the Standard Version.
 *
 *    d) make other distribution arrangements with the Copyright Holder.
 *
 * 5. You may charge a reasonable copying fee for any distribution of this 
 *    Package. You may charge any fee you choose for support of this Package. 
 *    You may not charge a fee for this Package itself. However, you may 
 *    distribute this Package in aggregate with other (possibly commercial) 
 *    programs as part of a larger (possibly commercial) software distribution 
 *    provided that you do not advertise this Package as a product of your own.
 *
 * 6. The scripts and library files supplied as input to or produced as output 
 *    from the programs of this Package do not automatically fall under the 
 *    copyright of this Package, but belong to whomever generated them, and may
 *    be sold commercially, and may be aggregated with this Package.
 *
 * 7. C or perl subroutines supplied by you and linked into this Package shall 
 *    not be considered part of this Package.
 *
 * 8. The name of the Copyright Holder may not be used to endorse or promote 
 *    products derived from this software without specific prior written 
 *    permission.
 * 
 * 9. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED 
 *    WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF 
 *    MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * ============================================================================
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.format.DiffXFormatter;

/**
 * A sequence of events used for the Diff-X algorithm.
 * 
 * <p>This class wraps a list of <code>DiffXEvent</code>s and provide method to
 * access and modify the content of the list using strongly typed methods. 
 * 
 * @author Christophe Lauret
 * @version 14 April 2005
 */
public final class EventSequence {

// Class attributes ---------------------------------------------------------------------------

/**
   * The prefix mapping for the elements in this sequence.
   */
  private PrefixMapping prefixMapping = new PrefixMapping();

  /**
   * The sequence of events.
   */
  private final List<DiffXEvent> sequence;

// Constructors -------------------------------------------------------------------------------

  /**
   * Creates a new event sequence. 
   */
  public EventSequence() {
    this.sequence = new LinkedList<DiffXEvent>();
  }
  
  /**
   * Creates a new event sequence of the specified size.
   * 
   * @param size The size of the sequence.
   */
  public EventSequence(int size) {
    this.sequence = new ArrayList<DiffXEvent>(size);
  }

// List methods -------------------------------------------------------------------------------

  /**
   * Adds a sequence of events to this sequence.
   * 
   * @param seq The sequence of events to be added. 
   */
  public void addSequence(EventSequence seq) {
    for (int i = 0; i < seq.size(); i++) {
      this.sequence.add(seq.getEvent(i));
    }
  }

  /**
   * Adds an event to this sequence.
   * 
   * @param e The event to be added. 
   */
  public void addEvent(DiffXEvent e) {
    this.sequence.add(e);
  }

  /**
   * Inserts an event to this sequence at the specified position.
   * 
   * @param i The position of the event.
   * @param e The event to be added. 
   */
  public void addEvent(int i, DiffXEvent e) {
    this.sequence.add(i, e);
  }

  /**
   * Returns the event at position i.
   * 
   * @param i The position of the event.
   * 
   * @return the event at position i.
   */
  public DiffXEvent getEvent(int i) {
    return (DiffXEvent)this.sequence.get(i);
  }

  /**
   * Replaces an event of this sequence at the specified position.
   * 
   * @param index The 0-based index of the position.
   * @param e     The event to be inserted.
   * 
   * @return The event at the previous position.
   */
  public DiffXEvent setEvent(int index, DiffXEvent e) {
    return (DiffXEvent)this.sequence.set(index, e);
  }

  /**
   * Removes an event from this sequence at the specified position.
   * 
   * @param index The 0-based index of the position.
   * 
   * @return The removed event.
   */
  public DiffXEvent removeEvent(int index) {
    return (DiffXEvent)this.sequence.remove(index);
  }

  /**
   * The size of the sequence.
   * 
   * @return The number of events in the sequence.
   */
  public int size() {
    return this.sequence.size();
  }

  /**
   * Returns a event iterator for this list.
   * 
   * @return The event iterator for this sequence.
   */
  public EventIterator eventIterator() {
    return new EventIterator(this.sequence.iterator());
  }

// Object methods -----------------------------------------------------------------------------

  /**
   * @see java.lang.Object#hashCode()
   */
//  public int hashCode() {
//    return this.sequence.size();
//  }
  

  private int fHashCode;

	public int hashCode() {
		if (fHashCode == 0) {
			HashCodeBuilder builder = new HashCodeBuilder();
			for (Iterator i = this.sequence.iterator(); i.hasNext();) {
				builder.append(i.next());
			}
			fHashCode = builder.toHashCode();
		}
		return fHashCode;
	}

  /**
	 * Returns <code>true</code> if the specified event sequence is the same
	 * as this one.
	 * 
	 * @param seq
	 *            The sequence of events to compare with this one.
	 * 
	 * @return <code>true</code> if the specified event sequence is equal to
	 *         this one; <code>false</code> otherwise.
	 */
  public boolean equals(EventSequence seq) {
    if (seq == null) return false;
    if (seq.getClass() != this.getClass()) return false;
    List sequence2 = ((EventSequence)seq).sequence;
    if (sequence.size() != sequence.size()) return false;
    boolean isEqual = true;
    DiffXEvent x1 = null;
    DiffXEvent x2 = null;
    for (int i = 0; i < sequence.size(); i++) {
      x1 = (DiffXEvent)sequence.get(i);
      x2 = (DiffXEvent)sequence2.get(i);
      if (!x1.equals(x2)) {
        System.out.println(x1 + " instance of " + x1.getClass().getName());
        System.out.println(x2 + " instance of " + x2.getClass().getName());
        return false;
      }
    }
    return true;
  }

  /**
   * Returns <code>true</code> if the specified event sequence is the same as this one.
   * 
   * <p>This methdo will redirect to the {@link #equals(EventSequence)} method if the
   * specified object is an instance of {@link EventSequence}.
   * 
   * @param o The sequence of events to compare with this one.
   * 
   * @return <code>true</code> if the specified event sequence is equal to this one;
   *         <code>false</code> otherwise. 
   */
  public boolean equals(Object o) {
    if (!(o instanceof EventSequence)) return false;
    return this.equals((EventSequence)o);
  }

  /**
   * Returns the string representation of this sequence.
   * 
   * @return The string representation of this sequence.
   */
  public String toString() {
    return "Event Sequence ["+this.size()+"]";
  }

  /**
   * Export the sequence.
   * 
   * @param w The print writer receiving the SAX events.
   */
  public void export(PrintWriter w) {
    DiffXEvent x = null; 
    for (int i = 0; i < sequence.size(); i++) {
      x = (DiffXEvent)sequence.get(i);
      w.println(x.toString());
    }
    w.flush();
  }
  
  /**
   * Send this entire EventSequence to the formatter.
   * Used by Docx4jDriver
   * 
 * @param formatter
 * @throws NullPointerException
 * @throws IOException
 */
public void format(DiffXFormatter formatter) throws NullPointerException,
			IOException {
		DiffXEvent x = null;
		for (int i = 0; i < sequence.size(); i++) {
			x = (DiffXEvent) sequence.get(i);
			formatter.format(x);
		}
	}  

  /**
	 * Maps a uri to a prefix.
	 * 
	 * @see PrefixMapping#add(String, String)
	 * 
	 * @param uri
	 *            The namespace URI to map.
	 * @param prefix
	 *            The prefix to use.
	 * 
	 * @throws NullPointerException
	 *             if the URI or prefix is <code>null</code>
	 */
  public void mapPrefix(String uri, String prefix) throws NullPointerException {
    this.prefixMapping.add(uri, prefix);
  }

  /**
   * Returns the prefix mapping for the namespace URIs in this sequence.
   * 
   * @return the prefix mapping for the namespace URIs in this sequence.
   */
  public PrefixMapping getPrefixMapping() {
    return this.prefixMapping;
  }
    
// Inner class --------------------------------------------------------------------------------

  /**
   * An iterator over the event elements in the sequences.
   * 
   * @author Christophe Lauret
   * @version 6 December 2004
   */
  public final class EventIterator implements Iterator {

    /**
     * The wrapped iterator.
     */
    private final Iterator iterator; 

    /**
     * Creates a new iterator wrapping the specified list iterator.
     * 
     * @param iterator The iterator to wrap.
     */
    private EventIterator(Iterator iterator) {
      this.iterator = iterator;
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
      return this.iterator.hasNext();
    }

    /**
     * @see java.util.Iterator#next()
     */
    public Object next() {
      return this.iterator.next();
    }

    /**
     * Returns the next event.
     * 
     * @see java.util.Iterator#next()
     * 
     * @return the next element in the iteration.
     * @throws NoSuchElementException iteration has no more event elements.
     */
    public DiffXEvent nextEvent() throws NoSuchElementException {
      return (DiffXEvent)this.iterator.next();
    }

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove() {
      this.iterator.remove();
    }

  }

}
