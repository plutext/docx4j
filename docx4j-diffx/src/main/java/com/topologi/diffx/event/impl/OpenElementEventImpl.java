/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.event.impl;

import java.io.IOException;

import javax.xml.XMLConstants;

import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.event.OpenElementEvent;
import com.topologi.diffx.xml.XMLWriter;

/**
 * A basic implementation of the close element event.
 *
 * <p>It corresponds to the <code>startElement</code> SAX event.
 *
 * <p>This implementation is not namespace aware.
 *
 * @author Christophe Lauret
 * @version 27 March 2010
 */
public final class OpenElementEventImpl extends DiffXEventBase implements OpenElementEvent {

  /**
   * The local name of the element.
   */
  private final String name;

  /**
   * Hashcode value for this event.
   */
  private final int hashCode;

  /**
   * Creates a new open element event.
   *
   * @param name The local name of the element
   *
   * @throws NullPointerException if the name is <code>null</code>.
   */
  public OpenElementEventImpl(String name) throws NullPointerException {
    if (name == null)
      throw new NullPointerException("Element must have a name.");
    this.name = name;
    this.hashCode = toHashCode(name);
  }

  /**
   * @return Returns the name.
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * @return Returns the Namespace URI.
   */
  @Override
  public String getURI() {
    return XMLConstants.NULL_NS_URI;
  }

  @Override
  public int hashCode() {
    return this.hashCode;
  }

  /**
   * Returns <code>true</code> if the event is an open element event.
   *
   * @param e The event to compare with this event.
   *
   * @return <code>true</code> if this event is equal to the specified event;
   *         <code>false</code> otherwise.
   */
  @Override
  public boolean equals(DiffXEvent e) {
    if (e.getClass() != this.getClass()) return false;
    OpenElementEventImpl oee = (OpenElementEventImpl)e;
    return oee.name.equals(this.name);
  }

  @Override
  public String toString() {
    return "openElement: "+this.name;
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement(this.name, false);
  }

  /**
   * Converts this event to an XML open tag.
   *
   * <p>Note that this method does not allow attributes to be put after this element.
   *
   * {@inheritDoc}
   */
  @Override
  public StringBuffer toXML(StringBuffer xml) {
    return xml.append('<').append(this.name).append('>');
  }

  /**
   * Calculates the hashcode for this event.
   *
   * @param s String from which the hashcode is calculated.
   * @return a number suitable as a hashcode.
   */
  private static int toHashCode(String s) {
    return 11 * 41 + (s != null? s.hashCode() : 0);
  }
}
