/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml.esc;

/**
 * A utility class for escaping XML data when using the UTF-8 encoding.
 *
 * @author  Christophe Lauret
 * @version 7 March 2005
 */
public final class XMLEscapeUTF8 extends XMLEscapeBase implements XMLEscape {

  /**
   * A static instance of the UTF8 escape class.
   */
  public static final XMLEscape UTF8_ESCAPE = new XMLEscapeUTF8();

  /**
   * The encoding used for this instance.
   */
  private static final String ENCODING = "utf-8";

  /**
   * Prevent creation of instances
   */
  private XMLEscapeUTF8() {
    super(ENCODING);
  }

  /**
   * Replaces '&lt;', '&amp;', '"' and '\'' as well an any character that is not part of
   * the standard unicode range.
   *
   * <pre>
   * Char ::= #x9 | #xA | #xD |
   *          [#x20-#xD7FF] |
   *          [#xE000-#xFFFD] |
   *          [#x10000-#x10FFFF]
   * </pre>
   *
   * {@inheritDoc}
   */
  public String toAttributeValue(char[] ch, int off, int len) {
    // process the rest
    StringBuffer out = new StringBuffer(len + len / 10);
    for (int i = off; i < off+len; i++) {
      switch (ch[i]) {
        // ignore control characters
        case 0x00:
        case 0x01:
        case 0x02:
        case 0x03:
        case 0x04:
        case 0x05:
        case 0x06:
        case 0x07:
        case 0x08:
        case 0x0B:
        case 0x0C:
        case 0x0E:
        case 0x0F:
        case 0x10:
        case 0x11:
        case 0x12:
        case 0x13:
        case 0x14:
        case 0x15:
        case 0x16:
        case 0x17:
        case 0x18:
        case 0x19:
        case 0x1A:
        case 0x1B:
        case 0x1C:
        case 0x1D:
        case 0x1E:
        case 0x1F:
        case 0x7F:
          break;
          // escape illegal characters
        case '&' :
          out.append("&amp;");
          break;
        case '<' :
          out.append("&lt;");
          break;
        case '"' :
          out.append("&quot;");
          break;
        case '\'' :
          out.append("&apos;");
          break;
          // output by default
        default:
          out.append(ch[i]);
      }
    }
    return out.toString();
  }

  /**
   * Replace characters which are invalid in element values,
   * by the corresponding entity in a given <code>String</code>.
   *
   * <p>these characters are:<br>
   * <ul>
   *   <li>'&amp' by the ampersand entity "&amp;amp"</li>
   *   <li>'&lt;' by the entity "&amp;lt;"</li>
   * </ul>
   * </p>
   *
   * <p>Empty strings or <code>null</code> return respectively
   * "" and <code>null</code>.
   *
   * <p>Note: this function assumes that there are no entities in
   * the given String. If there are existing entities, then the
   * ampersand character will be escaped by the ampersand entity.
   *
   * {@inheritDoc}
   */
  public String toElementText(char[] ch, int off, int len) {
    // process the rest
    StringBuffer out = new StringBuffer(len + len / 10);
    for (int i = off; i < off+len; i++) {
      switch (ch[i]) {
        // ignore control characters
        case 0x00:
        case 0x01:
        case 0x02:
        case 0x03:
        case 0x04:
        case 0x05:
        case 0x06:
        case 0x07:
        case 0x08:
        case 0x0B:
        case 0x0C:
        case 0x0E:
        case 0x0F:
        case 0x10:
        case 0x11:
        case 0x12:
        case 0x13:
        case 0x14:
        case 0x15:
        case 0x16:
        case 0x17:
        case 0x18:
        case 0x19:
        case 0x1A:
        case 0x1B:
        case 0x1C:
        case 0x1D:
        case 0x1E:
        case 0x1F:
        case 0x7F:
          break;
          // escape illegal characters
        case '&' :
          out.append("&amp;");
          break;
        case '<' :
          out.append("&lt;");
          break;
          // output by default
        default:
          out.append(ch[i]);
      }
    }
    return out.toString();
  }

}
