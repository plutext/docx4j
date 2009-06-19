package com.topologi.diffx.algorithm;

import com.topologi.diffx.DiffXException;

/**
 * Class of exceptions thrown when a factory method failed to produce
 * the desired object. 
 * 
 * @author Christophe Lauret
 * @version 3 February 2005
 */
public class FactoryException extends DiffXException {

  /**
   * Creates a new factory exception wrapping an occuring exception.
   * 
   * @param ex The exception to be wrapped.
   */
  public FactoryException(Exception ex) {
    super(ex);
  }

}
