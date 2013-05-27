/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.event.impl;

import java.io.IOException;

import com.topologi.diffx.event.AttributeEvent;
import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.xml.XMLWriter;

/**
 * A namespace aware implementation of the attribute event.
 *
 * @author Christophe Lauret
 * @author Jean-Baptiste Reure
 * @version 28 March 2010
 */
public final class AttributeEventNSImpl extends DiffXEventBase implements AttributeEvent {

  /**
   * The namespace URI this attribute belongs to.
   */
  private final String uri;

  /**
   * The name of the attribute.
   */
  private final String name;

  /**
   * The value of the attribute.
   */
  private final String value;

  /**
   * A suitable hashcode value.
   */
  private final int hashCode;

  /**
   * Creates a new attribute event.
   *
   * @param name The local name of the attribute.
   * @param value The value of the attribute.
   *
   * @throws NullPointerException if any of the argument is <code>null</code>.
   */
  public AttributeEventNSImpl(String name, String value) throws NullPointerException {
    if (name == null)
      throw new NullPointerException("Attribute must have a name.");
    if (value == null)
      throw new NullPointerException("The attribute value cannot be null, use \"\".");
    this.name = name;
    this.value = value;
    this.uri = null;
    this.hashCode = toHashCode(this.uri, name, value);
  }

  /**
   * Creates a new attribute event.
   *
   * @param uri The uri of the attribute.
   * @param name The local name of the attribute.
   * @param value The value of the attribute.
   *
   * @throws NullPointerException if any of the argument is <code>null</code>.
   */
  public AttributeEventNSImpl(String uri, String name, String value) throws NullPointerException {
    if (name == null)
      throw new NullPointerException("Attribute must have a name.");
    if (value == null)
      throw new NullPointerException("The attribute value cannot be null, use \"\".");
    this.name = name;
    this.value = value;
    this.uri = uri;
    this.hashCode = toHashCode(uri, name, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getURI() {
    return this.uri;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getValue() {
    return this.value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return this.hashCode;
  }

  /**
   * Returns <code>true</code> if the event is an attribute event.
   *
   * @param e The event to compare with this event.
   *
   * @return <code>true</code> if this event is equal to the specified event;
   *         <code>false</code> otherwise.
   */
  @Override
  public boolean equals(DiffXEvent e) {
    if (e.getClass() != this.getClass())
      return false;
    AttributeEventNSImpl ae = (AttributeEventNSImpl) e;
    if (!ae.name.equals(this.name))
      return false;
    if (!equals(ae.uri, this.uri))
      return false;
    if (!ae.value.equals(this.value))
      return false;
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "attribute: " + this.name + "=" + this.value + " [" + this.uri + "]";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.attribute(this.uri, this.name, this.value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StringBuffer toXML(StringBuffer xml) throws NullPointerException {
    // FIXME: no support for NS????
    xml.append(' ');
    xml.append(this.name);
    xml.append("=\"");
    xml.append(ESC.toAttributeValue(this.value));
    xml.append('"');
    return xml;
  }

  /**
   * Returns <code>true</code> if both namespace URI are <code>null</code> or equal.
   *
   * @param uri1 The first namespace URI.
   * @param uri2 The second namespace URI.
   *
   * @return <code>true</code> if both <code>null</code> or equal; <code>false</code> otherwise.
   */
  private static boolean equals(String uri1, String uri2) {
    if (uri1 == null && uri2 == null)
      return true;
    if (uri1 == null || uri2 == null)
      return false;
    return uri1.equals(uri2);
  }

  /**
   * Calculates the hashcode for this event.
   *
   * @param uri The URI.
   * @param name The attribute name.
   * @param value The attribute value.
   * @return a number suitable as a hashcode.
   */
  private static int toHashCode(String uri, String name, String value) {
    int hash = 17;
    hash = hash * 31 + (uri != null ? uri.hashCode() : 0);
    hash = hash * 31 + (name != null ? name.hashCode() : 0);
    hash = hash * 31 + (value != null ? value.hashCode() : 0);
    return hash;
  }

}
