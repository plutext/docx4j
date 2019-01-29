/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx;

/**
 * The mother of all Diff-X exceptions.
 * 
 * <p>This class is provided for convenience to distinguish between the purely
 * DiffX exceptions and exception of a different origin.
 * 
 * @author Christophe Lauret
 * @version 27 March 2010
 */
public class DiffXException extends Exception {

  /** As per requirement by the Serializable interface. */
  private static final long serialVersionUID = 3572025323967229569L;

  /**
   * Creates a new Diff-X exception.
   */
  public DiffXException() {
    super();
  }

  /**
   * Creates a new Diff-X exception with a given message.
   * 
   * @param message The message explaining the exception.
   */
  public DiffXException(String message) {
    super(message);
  }

  /**
   * Creates a new Diff-X exception wrapping an occurring exception.
   * 
   * @param ex The exception to be wrapped.
   */
  public DiffXException(Exception ex) {
    super(ex);
  }

  /**
   * Creates a new Diff-X exception wrapping an occurring exception.
   * 
   * @param message The message explaining the exception.
   * @param ex      The exception to be wrapped.
   */
  public DiffXException(String message, Exception ex) {
    super(message, ex);
  }

}
