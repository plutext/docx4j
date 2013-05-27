/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml;

/**
 * Exception thrown when attempting to close an element when there is no
 * corresponding open element.
 * 
 * @author Christophe Lauret
 * @version 14 May 2005
 */
public final class IllegalCloseElementException extends IllegalStateException {

  /**
   * Version number for the serialised class.
   */
  static final long serialVersionUID = 7264175736386596167L;

  /**
   * Creates a new illegal close element exception.
   */
  public IllegalCloseElementException() {
    super("Attempting to close an element with no more element to close.");
  }

}
