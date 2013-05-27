/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml.esc;

import java.io.IOException;
import java.io.Writer;

/**
 * A utility class for escaping XML data using the UTF-8 encoding.
 *
 * @author Christophe Lauret
 * 
 * @version 14 May 2005
 */
public final class XMLEscapeWriterUTF8 extends XMLEscapeWriterBase implements XMLEscapeWriter {

  /**
   * The encoding used for this instance.
   */
  private static final String ENCODING = "utf-8";

  /**
   * Creates a new XML escape writer using the utf-8 encoding.
   * 
   * @param writer The writer to wrap.
   * 
   * @throws NullPointerException if the writer is <code>null</code>.
   */
  public XMLEscapeWriterUTF8(Writer writer) throws NullPointerException {
    super(writer, ENCODING);
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
  public void writeAttValue(char[] ch, int off, int len) throws IOException {
    // process the rest
    char c = ' ';
    for (int i = off; i < off+len; i++) {
      c = ch[i];
      if      (c == '<') {
        super.w.write("&lt;");
      } else if (c == '>') {
        super.w.write("&gt;");
      } else if (c == '&') {
        super.w.write("&amp;");
      } else if (c == '"') {
        super.w.write("&quot;");
      } else if (c == '\'') {
        super.w.write("&apos;");
      } else if (c  > 255) {
        super.w.write("&#"+(int)c+";");
      } else if (c == '\n' || c == '\r' || c == '\t') {
        super.w.write(c);
      } else if (c < 32) {
        doNothing();
      } else if (c >= 127 && c < 160) {
        doNothing();
      } else { super.w.write(c); }
    }
  }

  /**
   * Replace characters which are invalid in element values,
   * by the corresponding entity in a given <code>String</code>.
   *
   * <p>these characters are:<br>
   * <ul>
   *  <li>'&amp' by the ampersand entity "&amp;amp"</li>
   *  <li>'&lt;' by the entity "&amp;lt;"</li>
   * </p>
   *
   * <p>Note: this function assumes that there are no entities in
   * the given String. If there are existing entities, then the
   * ampersand character will be escaped by the ampersand entity.
   *
   * {@inheritDoc}
   */
  public void writeText(char c) throws IOException {
    // process the rest
    if (c == '<') {
      super.w.write("&lt;");
    } else if (c == '>') {
      super.w.write("&gt;");
    } else if (c == '&') {
      super.w.write("&amp;");
    } else if (c == '"') {
      super.w.write("&quot;");
    } else if (c == '\'') {
      super.w.write("&apos;");
    } else if (c  > 255) {
      super.w.write("&#"+(int)c+";");
    } else if (c == '\n' || c == '\r' || c == '\t') {
      super.w.write(c);
    } else if (c < 32) {
      doNothing();
    } else if (c >= 127 && c < 160) {
      doNothing();
    } else {
      super.w.write(c);
    }
  }

  /**
   * Does nothing.
   * 
   * <p>This method exists so that we can explicitly say that we should do nothing
   * in certain conditions.
   */
  private static void doNothing() {
  }

}
