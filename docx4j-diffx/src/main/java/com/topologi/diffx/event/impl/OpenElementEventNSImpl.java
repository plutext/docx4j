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
 * The event corresponding to the <code>startElement</code> SAX event.
 *
 * @author Christophe Lauret
 * @version 27 March 2010
 */
public final class OpenElementEventNSImpl extends DiffXEventBase implements DiffXEvent, OpenElementEvent {

  /**
   * The namespace URI of the element.
   */
  private final String uri;

  /**
   * The local name of the element.
   */
  private final String name;

  /**
   * Hashcode value for this event.
   */
  private final int hashCode;

  /**
   * Creates a new open element event with the default URI.
   *
   * @see XMLConstants#NULL_NS_URI
   *
   * @param name The local name of the element
   *
   * @throws NullPointerException if the name is <code>null</code>.
   */
  public OpenElementEventNSImpl(String name) throws NullPointerException {
    if (name == null)
      throw new NullPointerException("Element must have a name.");
    this.uri = XMLConstants.NULL_NS_URI;
    this.name = name;
    this.hashCode = toHashCode(XMLConstants.NULL_NS_URI, name);
  }

  /**
   * Creates a new open element event.
   *
   * @param uri  The namespace URI of the element
   * @param name The local name of the element
   *
   * @throws NullPointerException if any of the argument is <code>null</code>.
   */
  public OpenElementEventNSImpl(String uri, String name) throws NullPointerException {
    if (uri == null)
      throw new NullPointerException("The URI cannot be null, use \"\".");
    if (name == null)
      throw new NullPointerException("Element must have a name.");
    this.uri = uri;
    this.name = name;
    this.hashCode = toHashCode(uri, name);
  }

  /**
   * @return Returns the name.
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * @return Returns the namespace URI.
   */
  @Override
  public String getURI() {
    return this.uri;
  }

  @Override
  public int hashCode() {
    return this.hashCode;
  }

  /**
   * Returns <code>true</code> if the event is a open element event.
   *
   * @param e The event to compare with this event.
   *
   * @return <code>true</code> if this event is equal to the specified event;
   *         <code>false</code> otherwise.
   */
  @Override
  public boolean equals(DiffXEvent e) {
    if (e == null) return false;
    if (e.getClass() != this.getClass()) return false;
    OpenElementEventNSImpl oee = (OpenElementEventNSImpl)e;
    if (!oee.uri.equals(this.uri)) return false;
    if (!oee.name.equals(this.name)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "openElement: "+this.name+" ["+this.uri+"]";
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement(this.uri, this.name, false);
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
    // TODO: handle namespaces
    return xml.append('<').append(this.name).append('>');
  }

  /**
   * Calculates the hashcode for this event.
   *
   * @param uri The namespace URI.
   * @param name The element name.
   * @return a number suitable as a hashcode.
   */
  private int toHashCode(String uri, String name) {
    int hash = 107;
    hash = hash * 13 + (uri != null? uri.hashCode() : 0);
    hash = hash * 13 + (name != null? name.hashCode() : 0);
    return hash;
  }

}
