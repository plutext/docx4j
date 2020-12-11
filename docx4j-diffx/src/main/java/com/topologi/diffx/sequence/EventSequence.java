/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.sequence;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.format.DiffXFormatter;

/**
 * A sequence of events used for the Diff-X algorithm.
 *
 * <p>This class wraps a list of <code>DiffXEvent</code>s and provide method to
 * access and modify the content of the list using strongly typed methods.
 *
 * @author Christophe Lauret
 * @version 6 December 2008
 *
 * @since 0.7
 */
public final class EventSequence {

  // Class attributes ---------------------------------------------------------------------------

  /**
   * The prefix mapping for the elements in this sequence.
   */
  private final PrefixMapping prefixMapping = new PrefixMapping();

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

  /**
   * Returns the sequence of events.
   *
   * @return the sequence of events.
   */
  public List<DiffXEvent> events() {
    return this.sequence;
  }

  // Object methods -----------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return this.sequence.size();
  }

  /**
   * Returns <code>true</code> if the specified event sequence is the same as this one.
   *
   * @param seq The sequence of events to compare with this one.
   *
   * @return <code>true</code> if the specified event sequence is equal to this one;
   *         <code>false</code> otherwise.
   */
  public boolean equals(EventSequence seq) {
    if (seq == null) return false;
    if (seq.getClass() != this.getClass()) return false;
    List<DiffXEvent> sequence2 = seq.sequence;
    if (this.sequence.size() != this.sequence.size()) return false;
    DiffXEvent x1 = null;
    DiffXEvent x2 = null;
    for (int i = 0; i < this.sequence.size(); i++) {
      x1 = this.sequence.get(i);
      x2 = sequence2.get(i);
      if (!x1.equals(x2)) return false;
    }
    return true;
  }

  /**
   * Returns <code>true</code> if the specified event sequence is the same as this one.
   *
   * <p>This method will redirect to the {@link #equals(EventSequence)} method if the
   * specified object is an instance of {@link EventSequence}.
   *
   * @param o The sequence of events to compare with this one.
   *
   * @return <code>true</code> if the specified event sequence is equal to this one;
   *         <code>false</code> otherwise.
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof EventSequence)) return false;
    return this.equals((EventSequence)o);
  }

  /**
   * Returns the string representation of this sequence.
   *
   * @return The string representation of this sequence.
   */
  @Override
  public String toString() {
    return "Event Sequence ["+size()+"]";
  }

  /**
   * Export the sequence.
   *
   * @param w The print writer receiving the SAX events.
   */
  public void export(PrintWriter w) {
    DiffXEvent x = null;
    for (int i = 0; i < this.sequence.size(); i++) {
      x = (DiffXEvent)this.sequence.get(i);
      w.println(x.toString());
    }
    w.flush();
  }

  /**
   * Maps a namespace URI to a prefix.
   *
   * @see PrefixMapping#add(String, String)
   *
   * @param uri    The namespace URI to map.
   * @param prefix The prefix to use.
   *
   * @throws NullPointerException if the URI or prefix is <code>null</code>
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
  public final class EventIterator implements Iterator<DiffXEvent> {

    /**
     * The wrapped iterator.
     */
    private final Iterator<DiffXEvent> iterator;

    /**
     * Creates a new iterator wrapping the specified list iterator.
     *
     * @param iterator The iterator to wrap.
     */
    private EventIterator(Iterator<DiffXEvent> iterator) {
      this.iterator = iterator;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
      return this.iterator.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    public DiffXEvent next() {
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
