package com.topologi.diffx.load;

import com.topologi.diffx.DiffXException;

/**
 * Class of exceptions occuring when trying to load data for Diff-X.
 * 
 * @author Christophe Lauret
 * @version 3 February 2005
 */
public final class LoadingException extends DiffXException {

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
   * Creates a new loading exception wrapping an occuring exception.
   * 
   * @param ex The exception to be wrapped.
   */
  public LoadingException(Exception ex) {
    super(ex);
  }

  /**
   * Creates a new loading exception wrapping an occuring exception.
   * 
   * @param message The message explaining the exception.
   * @param ex The exception to be wrapped.
   */
  public LoadingException(String message, Exception ex) {
    super(message, ex);
  }

}
