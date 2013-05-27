/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.event;

/**
 * An event for attributes.
 * 
 * @author Christophe Lauret
 * @version 3 April 2005
 */
public interface AttributeEvent extends DiffXEvent {

  /**
   * Returns the local name of the attribute.
   * 
   * <p>This method should never return <code>null</code>.
   * 
   * @return The local name of the attribute.
   */
  String getName();

  /**
   * Returns the value of the attribute.
   * 
   * <p>This method should never return <code>null</code>.
   * 
   * @return The value of the attribute.
   */
  String getValue();

  /**
   * Returns the namespace URI the attribute belongs to.
   *
   * <p>This method should return <code>null</code> if the implementation
   * is not namespace aware or if the attribute is not bound to any namespace.
   * 
   * @return The namespace URI the attribute belongs to or <code>null</code>.
   */
  String getURI();

}
