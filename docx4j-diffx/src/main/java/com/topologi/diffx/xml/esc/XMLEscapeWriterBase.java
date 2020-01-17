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
 * A base implementation for the XML writer escape classes.
 * 
 * @author Christophe Lauret
 * 
 * @version 14 May 2005
 */
abstract class XMLEscapeWriterBase implements XMLEscapeWriter {

  /**
   * The encoding for the implementation.
   */
  private final String encoding;

  /**
   * The wrapped writer.
   */
  final Writer w;

  /**
   * Creates a new XML escape for writers.
   * 
   * @param writer   The writer to wrap.
   * @param encoding The underlying character encoding for the writer.
   * 
   * @throws NullPointerException If the specified writer is <code>null</code>.
   */
  XMLEscapeWriterBase(Writer writer, String encoding) {
    if (writer == null)
      throw new NullPointerException("Cannot construct XML escape for null writer.");
    this.w = writer;
    this.encoding = encoding;
  }

  /**
   * Default implementation calling the {@link XMLEscapeWriter#writeAttValue(char[], int, int)}.
   * 
   * {@inheritDoc}
   */
  public final void writeAttValue(String value) throws IOException {
    if (value == null || "".equals(value)) return;
    writeAttValue(value.toCharArray(), 0, value.length());
  }

  /**
   * Default implementation calling the {@link XMLEscapeWriter#writeAttValue(char[], int, int)}.
   * 
   * {@inheritDoc}
   */
  public final void writeText(String value) throws IOException {
    if (value == null || "".equals(value)) return;
    writeText(value.toCharArray(), 0, value.length());
  }

  /**
   * Replace characters which are invalid in element values, by the corresponding
   * entity.
   * 
   * <p>This method calls {@link XMLEscapeWriter#writeText(char)} for each character.
   *
   * {@inheritDoc}
   */
  public void writeText(char[] ch, int off, int len) throws IOException {
    for (int i = off; i < off+len; i++) {
      writeText(ch[i]);
    }
  }

  /**
   * Returns the encoding for this writer.
   * 
   * {@inheritDoc}
   */
  public final String getEncoding() {
    return this.encoding;
  }

}
