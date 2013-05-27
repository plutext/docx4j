/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.load;

import com.topologi.diffx.DiffXException;

/**
 * Class of exceptions occurring when trying to load data for Diff-X.
 * 
 * @author Christophe Lauret
 * @version 14 May 2005
 */
public final class LoadingException extends DiffXException {

  /**
   * Version number for the serialised class.
   */
  private static final long serialVersionUID = -5026953481292613087L;

  /**
   * Creates a new Loading exception.
   */
  public LoadingException() {
    super();
  }

  /**
   * Creates a new loading exception with a given message.
   * 
   * @param message The message explaining the exception.
   */
  public LoadingException(String message) {
    super(message);
  }

  /**
   * Creates a new loading exception wrapping an occurring exception.
   * 
   * @param ex The exception to be wrapped.
   */
  public LoadingException(Exception ex) {
    super(ex);
  }

  /**
   * Creates a new loading exception wrapping an occurring exception.
   * 
   * @param message The message explaining the exception.
   * @param ex The exception to be wrapped.
   */
  public LoadingException(String message, Exception ex) {
    super(message, ex);
  }

}
