/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml.esc;

/**
 * A base implementation for the XML escape classes.
 * 
 * @author Christophe Lauret - Allette Systems (Australia)
 * @version 3 September 2004
 */
abstract class XMLEscapeBase implements XMLEscape {

  /**
   * The encoding for the implementation.
   */
  private final String encoding;

  /**
   * Creates a new XML Escape.
   * 
   * @param encoding The encoding used.
   */
  XMLEscapeBase(String encoding) {
    this.encoding = encoding;
  }

  /**
   * Default implementation calling the {@link XMLEscape#toAttributeValue(char[], int, int)}.
   * 
   * {@inheritDoc}
   */
  public final String toAttributeValue(String value) {
    if (value == null || "".equals(value)) return value;
    return toAttributeValue(value.toCharArray(), 0, value.length());
  }

  /**
   * Default implementation calling the {@link XMLEscape#toAttributeValue(char[], int, int)}.
   * 
   * {@inheritDoc}
   */
  public final String toElementText(String value) {
    if (value == null || "".equals(value)) return value;
    return toElementText(value.toCharArray(), 0, value.length());
  }

  /**
   * Returns the encoding used.
   * 
   * {@inheritDoc}
   */
  public final String getEncoding() {
    return this.encoding;
  }

}
