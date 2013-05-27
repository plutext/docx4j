/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml;

import java.io.IOException;

/**
 * Defines a writer for XML data.
 *
 * <p>This interface provides simple methods to write XML data onto a writer.
 *
 * <p>Most implementation should wrap a writer or an output stream. Implementations can be
 * focused on performance, reliability, error reporting, etc...
 *
 * <p>For improved performance, the most efficient solution will generally to have an
 * implementation write on a buffered writer since the memory usage will generally be
 * restricted little more than the size of the buffer, and this will keep the I/O
 * operation to a minimum.
 *
 * <p>Other implementations might want to wrap a SAX content handler.
 *
 * @author Christophe Lauret (Allette Systems)
 *
 * @version 11 December 2011
 */
public interface XMLWriter {

  // Initialisation methods
  // ----------------------------------------------------------------------------------------------

  /**
   * Writes the XML declaration.
   *
   * <p>Always:
   * <pre>
   *   &lt;?xml version="1.0" encoding="<i>encoding</i>"?&gt;
   * </pre>
   *
   * <p>It is followed by a new line character if the indentation is turned on.
   *
   * @throws IOException           If an I/O exception is thrown by the underlying writer.
   * @throws IllegalStateException If this method is called after the writer has started
   *                               writing elements nodes.
   */
  void xmlDecl() throws IOException;

  /**
   * Sets the string to use for indentation.
   *
   * <p>The string must be only composed of valid spaces characters.
   *
   * <p>If the string is <code>null</code> then the indentation is turned off.
   *
   * @see Character#isSpaceChar(char)
   *
   * @param spaces The indentation string to use.
   *
   * @throws IllegalArgumentException If the indent string is not made of spaces.
   * @throws IllegalStateException    If the writer has already been used.
   */
  void setIndentChars(String spaces);

  // Text methods
  // ----------------------------------------------------------------------------------------------

  /**
   * Writes the given character correctly for the encoding of this document.
   *
   * @param c The character to write.
   *
   * @throws IOException If an I/O exception is thrown by the underlying writer.
   */
  void writeText(char c) throws IOException;

  /**
   * Writes the given text correctly for the encoding of this document.
   *
   * <p>Does nothing if the text is <code>null</code>.
   *
   * @param text The text to write
   *
   * @throws IOException If an I/O exception is thrown by the underlying writer.
   */
  void writeText(String text) throws IOException;

  /**
   * Write the given text correctly for the encoding of this document.
   *
   * @param text The text to write.
   * @param off  The offset where we should start writing the string.
   * @param len  The length of the character subarray to write.
   *
   * @throws IOException If an I/O exception is thrown by the underlying writer.
   */
  void writeText(char[] text, int off, int len) throws IOException;

  /**
   * Writes the given text as a CDATA section.
   *
   * <p>Does nothing if the text is <code>null</code>.
   *
   * @param data The data to write inside the CDATA section.
   *
   * @throws IOException If an I/O exception is thrown by the underlying writer.
   * @throws IllegalArgumentException If the implementation does not support CDATA nesting
   */
  void writeCDATA(String data) throws IOException;

  // XML methods
  // ----------------------------------------------------------------------------------------------

  /**
   * Writes the given XML data.
   *
   * <p>The text is appended as is, therefore it should be escaped properly for the
   * encoding used by the underlying stream writer.
   *
   * <p>Does nothing if the text is <code>null</code>.
   *
   * @param text The text to write.
   *
   * @throws IOException If an I/O exception is thrown by the underlying writer.
   */
  void writeXML(String text) throws IOException;

  /**
   * Write the given XML data.
   *
   * <p>The text is appended as is, therefore it should be escaped properly for the
   * encoding used by the underlying stream writer.
   *
   * @param text The text to write.
   * @param off  The offset where we should start writing the string.
   * @param len  The length of the character subarray to write.
   *
   * @throws IOException If an I/O exception is thrown by the underlying writer.
   */
  void writeXML(char[] text, int off, int len) throws IOException;

  // Comments and PIs
  // ----------------------------------------------------------------------------------------------

  /**
   * Writes an XML comment.
   *
   * <p>An XML comment is:<br>
   * <pre>
   *   &lt;!-- <i>comment</i> --&gt;
   * </pre>
   *
   * <p>Comments are not indented.
   *
   * <p>Does not write anything if the comment if <code>null</code>.
   *
   * @param comment The comment to be written
   *
   * @throws IOException If thrown by the wrapped writer.
   * @throws IllegalArgumentException If the comment contains "--".
   */
  void writeComment(String comment) throws IOException;

  /**
   * Writes an XML processing instruction.
   *
   * <p>An XML processing intruction is:<br>
   * <pre>
   *   &lt;?<i>target</i> <i>data</i>?&gt;
   * </pre>
   *
   * @param target The PI's target.
   * @param data   The PI's data.
   *
   * @throws IOException If an I/O exception occurs.
   */
  void writePI(String target, String data) throws IOException;

  // Open/close elements
  // ----------------------------------------------------------------------------------------------

  /**
   * Writes a start element tag correctly indented.
   *
   * <p>It is the same as <code>openElement(name, false)</code>
   *
   * @see #openElement(java.lang.String, boolean)
   *
   * @param name the name of the element
   *
   * @throws IOException If an I/O exception occurs.
   */
  void openElement(String name) throws IOException;

  /**
   * Writes a start element tag correctly indented.
   *
   * <p>Use the <code>hasChildren</code> parameter to specify whether this element is terminal
   * node or not, note: this affects the indenting. To produce correctly indented XML, you
   * should use the same value for this flag when closing the  element.
   *
   * <p>The name can contain attributes and should be a valid xml name.
   *
   * @param name        The name of the element
   * @param hasChildren true if this element has children
   *
   * @throws IOException If an I/O exception occurs.
   */
  void openElement(String name, boolean hasChildren) throws IOException;

  /**
   * Writes a start element tag correctly indented.
   *
   * <p>Use the <code>hasChildren</code> parameter to specify whether this element is terminal
   * node or not, note: this affects the indenting. To produce correctly indented XML, you
   * should use the same value for this flag when closing the  element.
   *
   * <p>The name can contain attributes and should be a valid xml name.
   *
   * @param uri         The namespace URI of the element.
   * @param name        The name of the element.
   * @param hasChildren true if this element has children.
   *
   * @throws IOException If an I/O exception occurs.
   * @throws UnsupportedOperationException If the implementing class does not handle namespace.
   */
  void openElement(String uri, String name, boolean hasChildren) throws IOException;

  /**
   * Close the element automatically.
   *
   * <p>The element is closed symmetrically to the
   * {@link #openElement(String, String, boolean)} method if the XML writer is namespace
   * aware or the {@link #openElement(String, boolean)}method.
   *
   * @throws IOException If an I/O exception occurs.
   */
  void closeElement() throws IOException;

  // Element shortcuts
  // ----------------------------------------------------------------------------------------------

  /**
   * Opens element, inserts text node and closes.
   *
   * <p>This method should behave like:
   * <pre>
   *   this.openElement(name, false);
   *   this.writeText(text);
   *   this.closeElement();
   * </pre>
   *
   * @param name The name of the element.
   * @param text The text of the element.
   *
   * @throws IOException If thrown by the wrapped writer.
   */
  void element(String name, String text) throws IOException;

  /**
   * Writes an empty element.
   *
   * <p>It is possible for the element to contain attributes, however, since there is no character
   * escaping, great care must be taken not to introduce invalid characters. For example:
   * <pre>
   *    &lt;<i>example test="yes"</i>/&gt;
   * </pre>
   *
   * @param element the name of the element
   *
   * @throws IOException If an I/O exception occurs.
   * @throws UnsupportedOperationException If the implementing class does not handle namespace.
   */
  void emptyElement(String element) throws IOException;

  /**
   * Writes an empty element.
   *
   * <p>It is possible for the element to contain attributes, however, since there is no character
   * escaping, great care must be taken not to introduce invalid characters. For example:
   * <pre>
   *    &lt;<i>example test="yes"</i>/&gt;
   * </pre>
   *
   * @param uri     The namespace URI of the element.
   * @param element The name of the element.
   *
   * @throws IOException If an I/O exception occurs.
   * @throws UnsupportedOperationException If the implementing class does not handle namespace.
   */
  void emptyElement(String uri, String element) throws IOException;

  // Attributes
  // ----------------------------------------------------------------------------------------------

  /**
   * Writes an attribute.
   *
   * @param name  The name of the attribute.
   * @param value The value of the attribute.
   *
   * @throws IOException If thrown by the wrapped writer.
   * @throws IllegalStateException If there is no open element or text has been written.
   */
  void attribute(String name, String value) throws IOException;

  /**
   * Writes an attribute.
   *
   * <p>This method for number does not require escaping.
   *
   * @param name  The name of the attribute.
   * @param value The value of the attribute.
   *
   * @throws IOException If thrown by the wrapped writer.
   * @throws IllegalStateException If there is no open element or text has been written.
   */
  void attribute(String name, int value) throws IOException;

  /**
   * Writes an attribute.
   *
   * @param uri   The uri of the attribute.
   * @param name  The name of the attribute.
   * @param value The value of the attribute.
   *
   * @throws IOException If thrown by the wrapped writer.
   * @throws IllegalStateException If there is no open element or text has been written.
   * @throws UnsupportedOperationException If the implementing class does not handle namespace.
   */
  void attribute(String uri, String name, String value) throws IOException;

  /**
   * Writes an attribute.
   *
   * <p>This method for number does not require escaping.
   *
   * @param uri   The uri of the attribute.
   * @param name  The name of the attribute.
   * @param value The value of the attribute.
   *
   * @throws IOException If thrown by the wrapped writer.
   * @throws IllegalStateException If there is no open element or text has been written.
   * @throws UnsupportedOperationException If the implementing class does not handle namespace.
   */
  void attribute(String uri, String name, int value) throws IOException;

  // Namespace handling
  // ----------------------------------------------------------------------------------------------

  /**
   * Sets a prefix mapping.
   *
   * @param uri    The namespace URI.
   * @param prefix The new prefix for the namespace URI.
   *
   * @throws UnsupportedOperationException If the implementing class does not handle namespace.
   */
  void setPrefixMapping(String uri, String prefix);

  // Direct access to the writer
  // ----------------------------------------------------------------------------------------------

  /**
   * Flush the writer.
   *
   * @throws IOException If thrown by the wrapped writer.
   */
  void flush() throws IOException;

  /**
   * Close the writer.
   *
   * @throws IOException              If thrown by the wrapped writer.
   * @throws UnclosedElementException If there is still an open element.
   */
  void close() throws IOException;

}
