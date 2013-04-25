/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml.esc;

/**
 * An interface to escape XML character data.
 * 
 * <p>This interface assumes that the values to be escapes do not orignate from
 * XML text, in order words, there should not be already any entity or markup
 * in the document. If it is the case the methods in this class should also
 * escapes them. Thus "&amp;amp;" would be represented as "&amp;amp;amp;".
 * 
 * <p>Also the method will not try to escape characters that cannot be escaped.
 * 
 * <p>This interface is based on the paragraph 2.4 of the XML 1.0 Specifications.
 *
 * @author  Christophe Lauret
 * @version 7 March 2005
 */
public interface XMLEscape {

  /**
   * Returns a well-formed attribute value.
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
   * @return A well-formed value for the attribute.
   */
  String toAttributeValue(char[] ch, int off, int len);

  /**
   * Returns a well-formed attribute value.
   * 
   * <p>Method provided for convenience, using the same specifications as
   * {@link #toAttributeValue(char[], int, int)}.
   *
   * <p>This method should return <code>null</code> if the given
   * value is <code>null</code>.
   * 
   * @param value The value that needs to be attribute-escaped.
   * 
   * @return A well-formed value for the attribute.
   */
  String toAttributeValue(String value);

  /**
   * Returns a well-formed text value for the element.
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
   * @return A well-formed value for the text node.
   */
  String toElementText(char[] ch, int off, int len);

  /**
   * Returns a well-formed text value for the element.
   *
   * <p>Method provided for convenience, using the same specifications as
   * {@link #toElementText(char[], int, int)}.
   * 
   * <p>This method should return <code>null</code> if the given
   * value is <code>null</code>.
   * 
   * @param value The value that needs to be text-escaped.
   * 
   * @return A well-formed value for the text node.
   */
  String toElementText(String value);

  /**
   * Returns the encoding used by the implementing class.
   * 
   * @return The encoding used by the implementing class.
   */
  String getEncoding();

}
