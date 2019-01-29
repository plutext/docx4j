/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.algorithm;

import com.topologi.diffx.DiffXException;

/**
 * Class of exceptions thrown when a factory method failed to produce
 * the desired object.
 * 
 * @author Christophe Lauret
 * @version 14 May 2005
 */
public final class FactoryException extends DiffXException {

  /**
   * Version number for the serialised class.
   */
  private static final long serialVersionUID = -4029990831933233646L;

  /**
   * Creates a new factory exception wrapping an occuring exception.
   * 
   * @param ex The exception to be wrapped.
   */
  public FactoryException(Exception ex) {
    super(ex);
  }

}
