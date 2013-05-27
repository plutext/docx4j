/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.event.impl;

import java.io.IOException;

import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.xml.XMLWriter;

/**
 * A branch of XML data.
 *
 * <p>A branch of XML data must start and end with the same element.
 *
 * <p>Implementation note: this class wraps an array of DiffX events and does not give
 * access to this array, so it can be considered immutable.
 *
 * @author Christophe Lauret
 * @version 27 March 2010
 */
public final class XMLBranchEvent extends DiffXEventBase implements DiffXEvent {

  /**
   * The array of Diff-X events that make up the branch.
   */
  private final DiffXEvent[] branch;

  /**
   * Pre-calculated hashcode to speed up equal comparison.
   */
  private final int hashCode;

  /**
   * Creates a new XML branch.
   *
   * @param events The array of events that make up the branch.
   */
  public XMLBranchEvent(DiffXEvent[] events) {
    this.branch = events;
    this.hashCode = toHashCode(events);
  }

  @Override
  public int hashCode() {
    return this.hashCode;
  }

  /**
   * Returns <code>true</code> if the diffX events in the branch are all equal.
   *
   * {@inheritDoc}
   */
  @Override
  public boolean equals(DiffXEvent e) {
    if (e.getClass() != this.getClass()) return false;
    if (e.hashCode() != this.hashCode) return false;
    XMLBranchEvent be = (XMLBranchEvent)e;
    // branch must have the same length
    if (this.branch.length != be.branch.length) return false;
    // every single event must be equal
    for (int i = 0; i < this.branch.length; i++) {
      if (!be.branch[i].equals(this.branch[i]))
        return false;
    }
    // if we arrive here they are equal
    return true;
  }

  /**
   * Write the DiffX events in order.
   *
   * {@inheritDoc}
   */
  @Override
  public void toXML(XMLWriter xml) throws IOException {
    for (DiffXEvent element : this.branch) {
      element.toXML(xml);
    }
  }

  @Override
  public StringBuffer toXML(StringBuffer xml) throws NullPointerException {
    for (DiffXEvent element : this.branch) {
      element.toXML(xml);
    }
    return xml;
  }

  /**
   * Calculates the hashcode for this event.
   *
   * @param events Events to calculate the value from.
   * @return a number suitable as a hashcode.
   */
  private static int toHashCode(DiffXEvent[] events) {
    int hash = 17;
    for (DiffXEvent e : events) {
      hash = hash * 13 + (e != null? e.hashCode() : 0);
    }
    return hash;
  }
}
