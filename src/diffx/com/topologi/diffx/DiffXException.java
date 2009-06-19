package com.topologi.diffx;

/**
 * The mother of all Diff-X exceptions.
 * 
 * <p>This class is provided for convenience to distinguish between the purely
 * DiffX exceptions and exception of a different origin.
 * 
 * @author Christophe Lauret
 * @version 3 February 2005
 */
public abstract class DiffXException extends Exception {

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
   * Creates a new Diff-X exception wrapping an occuring exception.
   * 
   * @param ex The exception to be wrapped.
   */
  public DiffXException(Exception ex) {
    super(ex);
  }

  /**
   * Creates a new Diff-X exception wrapping an occuring exception.
   * 
   * @param message The message explaining the exception.
   * @param ex      The exception to be wrapped.
   */
  public DiffXException(String message, Exception ex) {
    super(message, ex);
  }

}
