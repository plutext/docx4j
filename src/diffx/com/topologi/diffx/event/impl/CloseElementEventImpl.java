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

import com.topologi.diffx.event.CloseElementEvent;
import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.event.OpenElementEvent;
import com.topologi.diffx.xml.XMLWriter;

/**
 * A basic implementation of the close element event.
 *
 * <p>It corresponds to the <code>endElement</code> SAX event.
 *
 * <p>This implementation is not namespace aware.
 *
 * @author Christophe Lauret
 * @version 17 May 2005
 */
public final class CloseElementEventImpl extends DiffXEventBase implements CloseElementEvent {

  /**
   * The corresponding open element event.
   */
  private final OpenElementEvent open;

  /**
   * Creates a new close element event.
   *
   * @param name The local name of the element
   *
   * @throws NullPointerException If the name is <code>null</code>.
   */
  public CloseElementEventImpl(String name) throws NullPointerException {
    if (name == null)
      throw new NullPointerException("Element must have a name.");
    this.open = new OpenElementEventImpl(name);
  }

  /**
   * Creates a new close element event that corresponds to the given open element.
   *
   * @param event The corresponding open element.
   *
   * @throws NullPointerException If the name is <code>null</code>.
   */
  public CloseElementEventImpl(OpenElementEvent event) throws NullPointerException {
    if (event == null)
      throw new NullPointerException("A close element must correspond to an open element.");
    this.open = event;
  }

  /**
   * @return Returns the name.
   */
  @Override
  public String getName() {
    return this.open.getName();
  }

  /**
   * Always return the empty URI.
   *
   * @see XMLConstants#NULL_NS_URI
   *
   * @return Returns the uri.
   */
  @Override
  public String getURI() {
    return XMLConstants.NULL_NS_URI;
  }

  @Override
  public OpenElementEvent getOpenElement() {
    return this.open;
  }

  /**
   * Returns <code>true</code> if the open element has the same name.
   *
   * {@inheritDoc}
   */
  @Override
  public boolean match(OpenElementEvent event) {
    if (event == null) return false;
    if (event == this.open) return true;
    return event.getName().equals(getName());
  }

  @Override
  public int hashCode() {
    return 53 + this.open.hashCode();
  }

  /**
   * Returns <code>true</code> if the event is a close element
   * and has the same name.
   *
   * @param e The event to compare with this event.
   *
   * @return <code>true</code> if this event is equal to the specified event;
   *         <code>false</code> otherwise.
   */
  @Override
  public boolean equals(DiffXEvent e) {
    if (e.getClass() != this.getClass()) return false;
    CloseElementEventImpl ce = (CloseElementEventImpl)e;
    return ce.getName().equals(getName());
  }

  @Override
  public String toString() {
    return "closeElement: "+getName();
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.closeElement();
  }

  @Override
  public StringBuffer toXML(StringBuffer xml) throws NullPointerException {
    xml.append("</").append(getName()).append('>');
    return xml;
  }

}
