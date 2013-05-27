/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml.esc;

import java.io.IOException;

/**
 * An interface to escape XML character data onto a writer.
 * 
 * <p>This interface mimics the {@link XMLEscape} interface but is designed to
 * be more efficient for writers, in other words, it wraps a writer and writes
 * directly onto it.
 * 
 * <p>This interface is based on the paragraph 2.4 of the XML 1.0 Specifications.
 *
 * @author  Christophe Lauret
 * @version 7 March 2005
 */
public interface XMLEscapeWriter {

  /**
   * Writes a well-formed attribute value.
   * 
   * <p>This method must replace any character in the specified
   * value by the corresponding numeric character reference or the
   * predefined XML general entities, if the character is not allowed
   * or not in the encoding range.
   * 
   * <p>Attribute values must not contain '&amp' or '&lt;. Quotes
   * and apostrophes must also be escaped by "&amp;quot;" and
   * "&amp;apos;" respectively.
   *
   * @param ch  The value that needs to be attribute-escaped.
   * @param off The start (offset) of the characters.
   * @param len The length of characters to.
   * 
   * @throws IOException If thrown by the underlying writer.
   */
  void writeAttValue(char[] ch, int off, int len) throws IOException;

  /**
   * Writes a well-formed attribute value.
   * 
   * <p>Method provided for convenience, using the same specifications as
   * {@link #writeAttValue(char[], int, int)}.
   *
   * <p>This method should return <code>null</code> if the given
   * value is <code>null</code>.
   * 
   * @param value The value that needs to be attribute-escaped.
   * 
   * @throws IOException If thrown by the underlying writer.
   */
  void writeAttValue(String value) throws IOException;

  /**
   * Writes a well-formed text value for the element.
   *
   * <p>This method must replace any character in the specified
   * value by the corresponding numeric character reference or the
   * predefined XML general entities, if the character is not allowed
   * or not in the encoding range.
   * 
   * <p>The text of an element must not contain '&amp' or '&lt;.
   * 
   * @param ch  The value that needs to be attribute-escaped.
   * @param off The start (offset) of the characters.
   * @param len The length of characters to.
   * 
   * @throws IOException If thrown by the underlying writer.
   */
  void writeText(char[] ch, int off, int len) throws IOException;

  /**
   * Writes the text string so that the text value for the element remains well-formed.
   *
   * <p>Method provided for convenience, using the same specifications as
   * {@link #writeText(char[], int, int)}.
   * 
   * <p>This method should do nothing if the given value is <code>null</code>.
   * 
   * @param text The text that needs to be text-escaped.
   * 
   * @throws IOException If thrown by the underlying writer.
   */
  void writeText(String text) throws IOException;

  /**
   * Writes the character so that the text value for the element remains well-formed.
   *
   * <p>This method should return <code>null</code> if the given
   * value is <code>null</code>.
   * 
   * @param c The character that needs to be text-escaped.
   * 
   * @throws IOException If thrown by the underlying writer.
   */
  void writeText(char c) throws IOException;

  /**
   * Returns the encoding used by the implementing class.
   * 
   * @return The encoding used by the implementing class.
   */
  String getEncoding();

}
