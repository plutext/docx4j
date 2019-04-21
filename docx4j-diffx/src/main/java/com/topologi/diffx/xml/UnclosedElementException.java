/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml;

/**
 * Exception thrown when an XML writer is closed but there is still and open
 * element tag without it closing element.
 * 
 * <p>This exception simply notifies that the XML will not be well-formed, if
 * writer is closed before the remaining open elements are closed.
 * 
 * @author Christophe Lauret
 * @version 14 May 2005
 */
public final class UnclosedElementException extends IllegalStateException {

  /**
   * Version number for the serialised class.
   */
  static final long serialVersionUID = -186657976801720211L;

  /**
   * Create a new unclosed element exception.
   * 
   * @param name The name of the unclosed element.
   */
  public UnclosedElementException(String name) {
    super("Attempting to close the XML Writer while element "+name+" has not been closed.");
  }

}
