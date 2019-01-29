/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.algorithm;

import com.topologi.diffx.event.AttributeEvent;
import com.topologi.diffx.event.CloseElementEvent;
import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.event.OpenElementEvent;

/**
 * Maintains the state of open and closed elements during the processing the Diff-X
 * algorithm.
 *
 * <p>This class has two purposes, firstly to provide an object that is more specialised
 * than the generic lists and stack for use by the DiffX algorithms. Second, to delegate
 * some of the complexity of algorithm.
 * 
 * <p>This class has several methods that are similar to <code>List</code> interface
 * but does not implement it.
 * 
 * <p>This class is not synchronised and is not meant to be serializable.
 * 
 * @author Christophe Lauret
 * @version 12 May 2005
 */
public final class ElementState {

  /**
   * The stack of open elements.
   */
  private transient OpenElementEvent[] openElements;

  /**
   * The stack open elements changes.
   */
  private transient char[] openChanges;

  /**
   * The size of both lists (the number of elements they contains).
   */
  private transient int size;

  /**
   * Constructs an stack of elements with the specified initial capacity.
   *
   * @param  initialCapacity  The initial capacity of the list.
   * @throws IllegalArgumentException if the specified initial capacity is negative.
   */
  public ElementState(int initialCapacity) throws IllegalArgumentException {
    if (initialCapacity < 0)
      throw new IllegalArgumentException("Illegal Capacity: "+initialCapacity);
    this.openElements = new OpenElementEvent[initialCapacity];
    this.openChanges = new char[initialCapacity];
  }

  /**
   * Constructs an empty stack with an initial capacity of 12.
   */
  public ElementState() {
    this(12);
  }

  /**
   * Increases the capacity of this class instance, if necessary, to ensure
   * that it can hold at least the number of elements specified by the
   * minimum capacity argument.
   *
   * @param minCapacity The desired minimum capacity.
   */
  public void ensureCapacity(int minCapacity) {
    int oldCapacity = this.openElements.length;
    if (minCapacity > oldCapacity) {
      // make a copy of the old arrays.
      OpenElementEvent[] oldElements = this.openElements;
      char[] oldChanges = this.openChanges;
      int newCapacity = oldCapacity * 3 / 2 + 1;
      if (newCapacity < minCapacity) {
        newCapacity = minCapacity;
      }
      // create new arrays
      this.openElements = new OpenElementEvent[newCapacity];
      this.openChanges = new char[newCapacity];
      // copy the values of the old arrays into the new ones
      System.arraycopy(oldElements, 0, this.openElements, 0, this.size);
      System.arraycopy(oldChanges, 0, this.openChanges, 0, this.size);
    }
  }

  /**
   * Returns the number of elements in this stack.
   *
   * @return  the number of elements in this stack.
   */
  public int size() {
    return this.size;
  }

  /**
   * Tests if this list has no elements.
   *
   * @return  <code>true</code> if this list has no elements;
   *          <code>false</code> otherwise.
   */
  public boolean isEmpty() {
    return this.size == 0;
  }

  /**
   * Returns <code>true</code> if this list contains the specified element.
   *
   * @param element Element whose presence is to be tested.
   * 
   * @return  <code>true</code> if the specified element is present;
   *          <code>false</code> otherwise.
   */
  public boolean contains(OpenElementEvent element) {
    return indexOf(element) >= 0;
  }

  /**
   * Searches for the first occurrence of the given argument, testing
   * for equality using the <code>equals</code> method.
   *
   * @param element The open elemnt to find.
   * 
   * @return The index of the first occurrence of the argument in this list;
   *         returns <code>-1</code if the object is not found.
   * 
   * @see com.topologi.diffx.event.DiffXEvent#equals(DiffXEvent)
   */
  public int indexOf(OpenElementEvent element) {
    if (element == null) {
      for (int i = 0; i < this.size; i++)
        if (this.openElements[i] == null)
          return i;
    } else {
      for (int i = 0; i < this.size; i++)
        if (element.equals(this.openElements[i]))
          return i;
    }
    return -1;
  }

  /**
   * Returns the index of the last occurrence of the specified object in
   * this list.
   *
   * @param element The desired element.
   * 
   * @return The index of the last occurrence of the specified open element;
   *         or -1 if not found.
   */
  public int lastIndexOf(OpenElementEvent element) {
    if (element == null) {
      for (int i = this.size - 1; i >= 0; i--)
        if (this.openElements[i] == null)
          return i;
    } else {
      for (int i = this.size - 1; i >= 0; i--)
        if (element.equals(this.openElements[i]))
          return i;
    }
    return -1;
  }

  // Maintenance methods ------------------------------------------------------------------------

  /**
   * Returns the current open element.
   * 
   * @return The current open element; or <code>null</code> if none.
   */
  public OpenElementEvent current() {
    if (!isEmpty())
      return this.openElements[this.size - 1];
    else
      return null;
  }

  /**
   * Returns the change of the current open element.
   * 
   * @return The change of the current open element; or ' ' if none.
   */
  public char currentChange() {
    if (!isEmpty())
      return this.openChanges[this.size - 1];
    else
      return ' ';
  }

  /**
   * Indicates whether the specified event is a close element that
   * matches the name and URI of the current open element.
   * 
   * @param e The event to check.
   * 
   * @return <code>true</code> if it matches the current element;
   *         <code>false</code> otherwise.
   */
  public boolean matchCurrent(DiffXEvent e) {
    // cannot match if empty
    if (isEmpty()) return false;
    // cannot match if not a close element event
    if (!(e instanceof CloseElementEvent)) return false;
    // check if they match
    return ((CloseElementEvent)e).match(current());
  }

  /**
   * Updates the state from the inserted event.
   * 
   * @param e The inserted event.
   */
  public void insert(DiffXEvent e) {
    if (e instanceof OpenElementEvent) {
      push((OpenElementEvent)e, '+');
    } else if (e instanceof CloseElementEvent) {
      pop();
    }
  }

  /**
   * Updates the state from the formatted event.
   * 
   * @param e The formatted event.
   */
  public void format(DiffXEvent e) {
    if (e instanceof OpenElementEvent) {
      push((OpenElementEvent)e, '=');
    } else if (e instanceof CloseElementEvent) {
      pop();
    }
  }

  /**
   * Updates the state from the deleted event.
   * 
   * @param e The deleted event.
   */
  public void delete(DiffXEvent e) {
    if (e instanceof OpenElementEvent) {
      push((OpenElementEvent)e, '-');
    } else if (e instanceof CloseElementEvent) {
      pop();
    }
  }

  /**
   * Indicates whether the specified event is a close element that
   * matches the name and URI of the current open element.
   * 
   * @param e The event to check.
   * 
   * @return <code>true</code> if it matches the current element;
   *         <code>false</code> otherwise.
   */
  public boolean okFormat(DiffXEvent e) {
    // cannot match if not a close element event
    if (!(e instanceof CloseElementEvent)) return true;
    // cannot match if empty
    if (isEmpty()) return false;
    // check if they match
    return ((CloseElementEvent)e).match(current())
        && this.openChanges[this.size - 1] == '=';
  }

  /**
   * Indicates whether the specified event is a close element that
   * matches the name and URI of the current open element.
   * 
   * @param e The event to check.
   * 
   * @return <code>true</code> if it matches the current element;
   *         <code>false</code> otherwise.
   */
  public boolean okInsert(DiffXEvent e) {
    // cannot match if not a close element event
    if (!(e instanceof CloseElementEvent)) return true;
    // cannot match if empty
    if (isEmpty()) return false;
    // check if they match
    return ((CloseElementEvent)e).match(current())
        && this.openChanges[this.size - 1] == '+';
  }

  /**
   * Indicates whether the specified event is a close element that
   * matches the name and URI of the current open element.
   * 
   * @param e The event to check.
   * 
   * @return <code>true</code> if it matches the current element;
   *         <code>false</code> otherwise.
   */
  public boolean okDelete(DiffXEvent e) {
    // cannot match if not a close element event
    if (!(e instanceof CloseElementEvent)) return true;
    // cannot match if empty
    if (isEmpty()) return false;
    // check if they match
    return ((CloseElementEvent)e).match(current())
        && this.openChanges[this.size - 1] == '-';
  }

  /**
   * Indicates whether the first specified event has priority over the second element.
   * 
   * It only seem to be the case when the algorithm has the choice between an attribute and another
   * element.
   * 
   * @param e1 The element assumed to have priority.
   * @param e2 The other element.
   * 
   * @return <code>true</code> if first specified event has priority over the second element;
   *         <code>false</code> otherwise.
   */
  public boolean hasPriorityOver(DiffXEvent e1, DiffXEvent e2) {
    if (e1 instanceof AttributeEvent
        && !(e2 instanceof AttributeEvent)
        && !isEmpty())
      return true;
    return false;
  }

  // Stack methods ------------------------------------------------------------------------

  /**
   * Push the specified open element and flags it with the specified change.
   * 
   * @param e The open element to push.
   * @param c The character corresponding to change.
   */
  private void push(OpenElementEvent e, char c) {
    ensureCapacity(this.size + 1);
    this.openElements[this.size] = e;
    this.openChanges[this.size] = c;
    this.size++;
  }

  /**
   * Removes the last element from the top of the stack.
   * 
   * @return The last element from the top of the stack.
   */
  public OpenElementEvent pop() {
    if (this.size > 0) {
      this.size--;
      return this.openElements[this.size];
    }
    return null;
  }

  // Positional Access Operations ---------------------------------------------------------

  /**
   * Returns the open element at the specified position in this list.
   *
   * @param  index index of element to return.
   * 
   * @return The element at the specified position in this list.
   * 
   * @throws IndexOutOfBoundsException if index is out of range
   *         <code>(index &lt; 0 || index &gt;= size())</code>.
   */
  public OpenElementEvent get(int index) throws IndexOutOfBoundsException {
    checkRange(index);
    return this.openElements[index];
  }

  /**
   * Removes the element at the specified position in this list.
   * Shifts any subsequent elements to the left (subtracts one from their
   * indices).
   *
   * @param index The index of the element to removed.
   * 
   * @return The element that was removed from the list.
   * 
   * @throws IndexOutOfBoundsException if index is out of range
   *         <code>(index &lt; 0 || index &gt;= size())</code>.
   */
  public OpenElementEvent remove(int index) throws IndexOutOfBoundsException {
    checkRange(index);
    OpenElementEvent oldValue = this.openElements[index];
    int numMoved = this.size - index - 1;
    if (numMoved > 0) {
      System.arraycopy(this.openElements, index+1, this.openElements, index, numMoved);
    }
    this.openElements[--this.size] = null; // Let gc do its work
    return oldValue;
  }

  /**
   * Removes all of the elements from this list.  The list will
   * be empty after this call returns.
   */
  public void clear() {
    // Let gc do its work
    for (int i = 0; i < this.size; i++) {
      this.openElements[i] = null;
    }
    this.size = 0;
  }

  /**
   * Checks if the given index is in range. If not, throw an appropriate
   * runtime exception.  This method does *not* check if the index is
   * negative: It is always used immediately prior to an array access,
   * which throws an ArrayIndexOutOfBoundsException if index is negative.
   * 
   * @param index The index to check.
   * 
   * @throws IndexOutOfBoundsException if index is out of range
   *         <code>(index &lt; 0 || index &gt;= size())</code>.
   */
  private void checkRange(int index) throws IndexOutOfBoundsException {
    if (index >= this.size)
      throw new IndexOutOfBoundsException("Index: "+index+", Size: "+this.size);
  }

}
