/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.event;

/**
 * The event corresponding to the <code>endElement</code> SAX event.
 *
 * @author Christophe Lauret (Allette Systems)
 * @version 3 April 2005
 */
public interface CloseElementEvent extends DiffXEvent {

  /**
   * Returns the local name of the element.
   *
   * @return The local name of the element.
   */
  String getName();

  /**
   * Returns the namespace URI the element belongs to.
   *
   * <p>This method should return <code>null</code> if the implementation
   * is not namespace aware.
   *
   * @return The namespace URI the element belongs to.
   */
  String getURI();

  /**
   * Returns the corresponding event element.
   *
   * @return The corresponding event element.
   */
  OpenElementEvent getOpenElement();

  /**
   * Indicates whether the specified open element event matches this close
   * element event.
   *
   * <p>This method first checks whether the open element event is the same as
   * event returned by the {@link #getOpenElement()} method, if not it simply
   * compares the name of the element and the namespace URI it belongs to.
   *
   * @param event The open element event to test.
   *
   * @return <code>true</code> if there is a match;
   *         <code>false</code> otherwise.
   */
  boolean match(OpenElementEvent event);

}
