/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.event.impl;

import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.xml.esc.XMLEscape;
import com.topologi.diffx.xml.esc.XMLEscapeUTF8;

/**
 * A base class for DiffX events.
 *
 * <p>
 * This class is purely provided for convenience and consistency, it is best, althouhg not strictly
 * required, that most <code>DiffXEvent</code> implementations extend this class.
 *
 * @author Christophe Lauret
 * @version 3 February 2005
 */
abstract class DiffXEventBase implements DiffXEvent {

  /**
   * For use by the events to escape XML chars.
   */
  static final XMLEscape ESC = XMLEscapeUTF8.UTF8_ESCAPE;

  /**
   *
   */
  int weight = 1;

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract int hashCode();

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract boolean equals(DiffXEvent e);

  /**
   * Invokes the {@link DiffXEvent#equals(DiffXEvent)} method if the specified object if not
   * <code>null</code> and is an instance of {@link DiffXEvent}.
   *
   * @param o The object to compare.
   *
   * @return <code>true</code> if the specified object is equal;
   *         <code>false</code> otherwise.
   */
  @Override
  public final boolean equals(Object o) {
    if (o == null)
      return false;
    if (!(o instanceof DiffXEvent))
      return false;
    return equals((DiffXEvent) o);
  }

  @Override
  public String toXML() {
    return this.toXML(new StringBuffer()).toString();
  }

  @Override
  public int getWeight() {
    return this.weight;
  }

  @Override
  public void setWeight(int weight) {
    this.weight = weight;
  }

}
