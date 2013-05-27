/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.event.impl;

import com.topologi.diffx.event.AttributeEvent;
import com.topologi.diffx.event.CloseElementEvent;
import com.topologi.diffx.event.OpenElementEvent;

/**
 * Factory for events.
 * 
 * <p>This class is designed to returned events that are optimised for the type of sequence
 * that it is being inserted in.
 * 
 * <p>Non-namespace aware objects are lighter than namespace aware ones.
 * 
 * @author Christophe Lauret
 * @version 27 April 2005
 */
public final class EventFactory {

  // class attributes ---------------------------------------------------------------------

  /**
   * Indicates whether the factory should generate namespace events.
   */
  private boolean isNamespaceAware = true;

  // constructors -------------------------------------------------------------------------

  /**
   * Creates a new namespace aware factory for events.
   */
  public EventFactory() {
    this.isNamespaceAware = true;
  }

  /**
   * Creates a factory for events.
   * 
   * @param isNamespaceAware <code>true</code> to create new namespace aware factory;
   *                         <code>false</code> otherwise.
   */
  public EventFactory(boolean isNamespaceAware) {
    this.isNamespaceAware = isNamespaceAware;
  }

  // methods ------------------------------------------------------------------------------

  /**
   * Returns the open element event from the uri and name given.
   * 
   * <p>If the factory is namespace aware, it returns an open element implementation
   * using the namespace URI and the name.
   * 
   * <p>If the factory is NOT namespace aware, it returns an open element implementation
   * using the specified name.
   * 
   * <p>Use this implementation if the name of the element is determined prior to the
   * call of this method.
   * 
   * @param uri  The namespace URI of the element (ignored if not namespace aware)
   * @param name The name of the element.
   * 
   * @return The open element event from the uri and name given.
   */
  public OpenElementEvent makeOpenElement(String uri, String name) {
    if (this.isNamespaceAware) return new OpenElementEventNSImpl(uri, name);
    else
      return new OpenElementEventImpl(name);
  }

  /**
   * Returns the open element event from the uri and names given.
   * 
   * <p>If the factory is namespace aware, it returns an open element implementation
   * using the namespace URI and the local name.
   * 
   * <p>If the factory is NOT namespace aware, it returns an open element implementation
   * using the qName (namespace-prefixed name).
   * 
   * @param uri       The namespace URI of the element (ignored if not namespace aware)
   * @param localName The local name of the element.
   * @param qName     The qualified name of the element.
   * 
   * @return The open element event from the uri and name given.
   */
  public OpenElementEvent makeOpenElement(String uri, String localName, String qName) {
    if (this.isNamespaceAware) return new OpenElementEventNSImpl(uri, localName);
    else
      return new OpenElementEventImpl(qName);
  }

  /**
   * Returns the close element event from the corresponding open element event.
   * 
   * @param open The corresponding open element event.
   * 
   * @return The close element event from the corresponding open element event.
   */
  public CloseElementEvent makeCloseElement(OpenElementEvent open) {
    if (this.isNamespaceAware) return new CloseElementEventNSImpl(open);
    else
      return new CloseElementEventImpl(open);
  }

  /**
   * Returns the attribute event from the name and value given.
   * 
   * <p>If the factory is namespace aware, it returns an attribute implementation
   * using the namespace URI and the name.
   * 
   * <p>If the factory is NOT namespace aware, it returns an attribute implementation
   * using the specified name.
   * 
   * <p>Use this implementation if the name of the element is determined prior to the
   * call of this method.
   * 
   * @param uri   The namespace URI of the attribute (ignored if not namespace aware)
   * @param name  The name of the attribute.
   * @param value The value of the attribute.
   * 
   * @return The open element event from the uri and name given.
   */
  public AttributeEvent makeAttribute(String uri, String name, String value) {
    if (this.isNamespaceAware) return new AttributeEventNSImpl("".equals(uri)? null : uri, name, value);
    else
      return new AttributeEventImpl(name, value);
  }

  /**
   * Returns the attribute event from the name and value given.
   * 
   * <p>If the factory is namespace aware, it returns an attribute implementation
   * using the namespace URI and the local name.
   * 
   * <p>If the factory is NOT namespace aware, it returns an attribute implementation
   * using the qName (namespace-prefixed name).
   * 
   * @param uri       The namespace URI of the attribute (ignored if not namespace aware)
   * @param localName The local name of the attribute.
   * @param qName     The qualified name of the attribute.
   * @param value     The value of the attribute.
   * 
   * @return The open element event from the uri and name given.
   */
  public AttributeEvent makeAttribute(String uri, String localName, String qName, String value) {
    if (this.isNamespaceAware) return new AttributeEventNSImpl("".equals(uri)? null : uri, localName, value);
    else
      return new AttributeEventImpl(qName, value);
  }

}
