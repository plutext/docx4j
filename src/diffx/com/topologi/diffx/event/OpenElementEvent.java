/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.event;

/**
 * The event corresponding to the <code>startElement</code> SAX event.
 *
 * @author Christophe Lauret
 * @version 23 December 2004
 */
public interface OpenElementEvent extends DiffXEvent {

  /**
   * Returns the local name of the element.
   *
   * @return The local name of the element.
   */
  String getName();

  /**
   * Returns the namespace URI the element belongs to.
   *
   * @return The namespace URI the element belongs to.
   */
  String getURI();

}
