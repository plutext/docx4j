/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml.esc;

/**
 * A singleton for escaping XML data when using the 'ASCII' encoding.
 *
 * @author  Christophe Lauret
 * @version 16 January 2007
 */
public final class XMLEscapeASCII extends XMLEscapeBase implements XMLEscape {

  /**
   * A static instance of the UTF8 escape class.
   */
  public static final XMLEscape ASCII_ESCAPE = new XMLEscapeASCII();

  /**
   * The encoding used for this instance.
   */
  private static final String ENCODING = "ASCII";

  /**
   * Prevent creation of instances
   */
  private XMLEscapeASCII() {
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
  @Override
  public String toAttributeValue(char[] ch, int off, int len) {
    // process the rest
    StringBuffer out = new StringBuffer(len + len / 10);
    for (int i = off; i < off+len; i++) {
      // 0x00 to 0x1F
      if (ch[i] < 0x20) {
        // tabs, new lines and line feeds: preserve
        if (ch[i] == 0x09 || ch[i] == 0x0A || ch[i] == 0x0D) {
          out.append(ch[i]);
        } else {
          doNothing();
          // 0x20 to 0x7F
        }
      } else if (ch[i] < 0x80) {
        switch (ch[i]) {
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
        // control characters (C1): prune
      } else if (ch[i] < 0xA0) {
        doNothing();
        // all other characters: use numerical character entity
      } else {
        out.append("&#x").append((int)ch[i]).append(';');
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
  @Override
  public String toElementText(char[] ch, int off, int len) {
    // process the rest
    StringBuffer out = new StringBuffer(len + len / 10);
    for (int i = off; i < off+len; i++) {
      // 0x00 to 0x1F
      if (ch[i] < 0x20) {
        // tabs, new lines and line feeds: preserve
        if (ch[i] == 0x09 || ch[i] == 0x0A || ch[i] == 0x0D) {
          out.append(ch[i]);
        } else {
          doNothing();
          // 0x20 to 0x7F
        }
      } else if (ch[i] < 0x80) {
        switch (ch[i]) {
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
        // control characters (C1): prune
      } else if (ch[i] < 0xA0) {
        doNothing();
        // all other characters: use numerical character entity
      } else {
        out.append("&#x").append((int)ch[i]).append(';');
      }
    }
    return out.toString();
  }

  /**
   * Does nothing.
   */
  private void doNothing() {
  }

}
