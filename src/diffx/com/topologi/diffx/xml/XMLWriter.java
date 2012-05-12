package com.topologi.diffx.xml;

/* ============================================================================
 * ARTISTIC LICENCE
 * 
 * Preamble
 * 
 * The intent of this document is to state the conditions under which a Package
 * may be copied, such that the Copyright Holder maintains some semblance of 
 * artistic control over the development of the package, while giving the users
 * of the package the right to use and distribute the Package in a more-or-less
 * customary fashion, plus the right to make reasonable modifications.
 *
 * Definitions:
 *  - "Package" refers to the collection of files distributed by the Copyright 
 *    Holder, and derivatives of that collection of files created through 
 *    textual modification.
 *  - "Standard Version" refers to such a Package if it has not been modified, 
 *    or has been modified in accordance with the wishes of the Copyright 
 *    Holder.
 *  - "Copyright Holder" is whoever is named in the copyright or copyrights 
 *    for the package.
 *  - "You" is you, if you're thinking about copying or distributing this 
 *    Package.
 *  - "Reasonable copying fee" is whatever you can justify on the basis of 
 *    media cost, duplication charges, time of people involved, and so on. 
 *    (You will not be required to justify it to the Copyright Holder, but only 
 *    to the computing community at large as a market that must bear the fee.)
 *  - "Freely Available" means that no fee is charged for the item itself, 
 *    though there may be fees involved in handling the item. It also means 
 *    that recipients of the item may redistribute it under the same conditions
 *    they received it.
 *
 * 1. You may make and give away verbatim copies of the source form of the 
 *    Standard Version of this Package without restriction, provided that you 
 *    duplicate all of the original copyright notices and associated 
 *    disclaimers.
 *
 * 2. You may apply bug fixes, portability fixes and other modifications 
 *    derived from the Public Domain or from the Copyright Holder. A Package 
 *    modified in such a way shall still be considered the Standard Version.
 *
 * 3. You may otherwise modify your copy of this Package in any way, provided 
 *    that you insert a prominent notice in each changed file stating how and 
 *    when you changed that file, and provided that you do at least ONE of the 
 *    following:
 * 
 *    a) place your modifications in the Public Domain or otherwise make them 
 *       Freely Available, such as by posting said modifications to Usenet or 
 *       an equivalent medium, or placing the modifications on a major archive 
 *       site such as ftp.uu.net, or by allowing the Copyright Holder to 
 *       include your modifications in the Standard Version of the Package.
 * 
 *    b) use the modified Package only within your corporation or organization.
 *
 *    c) rename any non-standard executables so the names do not conflict with 
 *       standard executables, which must also be provided, and provide a 
 *       separate manual page for each non-standard executable that clearly 
 *       documents how it differs from the Standard Version.
 * 
 *    d) make other distribution arrangements with the Copyright Holder.
 *
 * 4. You may distribute the programs of this Package in object code or 
 *    executable form, provided that you do at least ONE of the following:
 * 
 *    a) distribute a Standard Version of the executables and library files, 
 *       together with instructions (in the manual page or equivalent) on where
 *       to get the Standard Version.
 *
 *    b) accompany the distribution with the machine-readable source of the 
 *       Package with your modifications.
 * 
 *    c) accompany any non-standard executables with their corresponding 
 *       Standard Version executables, giving the non-standard executables 
 *       non-standard names, and clearly documenting the differences in manual 
 *       pages (or equivalent), together with instructions on where to get 
 *       the Standard Version.
 *
 *    d) make other distribution arrangements with the Copyright Holder.
 *
 * 5. You may charge a reasonable copying fee for any distribution of this 
 *    Package. You may charge any fee you choose for support of this Package. 
 *    You may not charge a fee for this Package itself. However, you may 
 *    distribute this Package in aggregate with other (possibly commercial) 
 *    programs as part of a larger (possibly commercial) software distribution 
 *    provided that you do not advertise this Package as a product of your own.
 *
 * 6. The scripts and library files supplied as input to or produced as output 
 *    from the programs of this Package do not automatically fall under the 
 *    copyright of this Package, but belong to whomever generated them, and may
 *    be sold commercially, and may be aggregated with this Package.
 *
 * 7. C or perl subroutines supplied by you and linked into this Package shall 
 *    not be considered part of this Package.
 *
 * 8. The name of the Copyright Holder may not be used to endorse or promote 
 *    products derived from this software without specific prior written 
 *    permission.
 * 
 * 9. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED 
 *    WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF 
 *    MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * ============================================================================
 */

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
 * @version 17 March 2005
 */
public interface XMLWriter {

// text methods -------------------------------------------------------------------------

  /**
   * Writes the given text correctly for the encoding of this document.
   *
   * <p>Does nothing if the text is <code>null</code>.
   *
   * @param c The character to write.
   *
   * @throws IOException If an I/O exception occurs.
   */
  void writeText(char c) throws IOException;

  /**
   * Writes the given text correctly for the encoding of this document.
   *
   * <p>This method turns the string into an array of chars.
   *
   * <p>Does nothing if the text is <code>null</code>.
   *
   * @param text The text to write.
   *
   * @throws IOException If an I/O exception occurs.
   */
  void writeText(String text) throws IOException;

  /**
   * Write the given text correctly for the encoding of this document.
   *
   * @param text The text to write.
   * @param off  The offset where we should start writing the string.
   * @param len  The length of the character subarray to write.
   * 
   * @throws IOException If an I/O exception occurs.
   */
  void writeText(char[] text, int off, int len) throws IOException;

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
   * @throws IOException If an I/O exception occurs.
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
   * @throws IOException If an I/O exception occurs.
   */
  void writeXML(char[] text, int off, int len) throws IOException;

// comments and PIs -------------------------------------------------------------------------

  /**
   * Writes the XML declaration.
   * 
   * <p>Always:
   * <pre>
   *   &lt;?xml version="1.0" encoding="<i>encoding</i>"?&gt;
   * </pre>
   * followed by a new line character.
   * 
   * @throws IOException           If an I/O exception occurs.
   * @throws IllegalStateException If this method is called after the writer has started 
   *                               writing elements nodes.
   */
  void xmlDecl() throws IOException, IllegalStateException;

  /**
   * Writes an XML comment.
   *
   * <p>An XML comment is:<br>
   * <pre>
   *   &lt;!-- <i>comment</i> --&gt;
   * </pre>
   *
   * @param comment The comment to be written.
   * 
   * @throws IOException If an I/O exception occurs.
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

  // open/close specific elements ---------------------------------------------------------

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
  void openElement(String uri, String name, boolean hasChildren)
    throws IOException, UnsupportedOperationException;
  
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

  /**
   * Opens element, inserts text node and closes.
   *
   * @param name The name of the element.
   * @param text The text of the element.
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  void element(String name, String text) throws IOException;

  /**
   * Write an empty element.
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
  void emptyElement(String element)
    throws IOException, UnsupportedOperationException;

  /**
   * Write an empty element.
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
  void emptyElement(String uri, String element)
    throws IOException, UnsupportedOperationException;

  // attributes -------------------------------------------------------------------------------

  /**
   * Writes an attribute.
   *
   * @param name  The name of the attribute.
   * @param value The value of the attribute.
   * 
   * @throws IOException If thrown by the wrapped writer.
   * @throws IllegalStateException If there is no open element or text has been written. 
   */
  void attribute(String name, String value) throws IOException, IllegalStateException;

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
  void attribute(String name, int value) throws IOException, IllegalStateException;

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
  void attribute(String uri, String name, String value) 
    throws IOException, IllegalStateException, UnsupportedOperationException;

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
  void attribute(String uri, String name, int value) 
    throws IOException, IllegalStateException, UnsupportedOperationException;

  // namespace handling -----------------------------------------------------------------------

  /**
   * Sets a prefix mapping.
   * 
   * @param uri    The namespace URI.
   * @param prefix The new prefix for the namespace URI.
   * 
   * @throws UnsupportedOperationException If the implementing class does not handle namespace.
   */
  void setPrefixMapping(String uri, String prefix) throws UnsupportedOperationException;

  // direct access to the writer --------------------------------------------------------------

  /**
   * Flush the writer.
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  void flush() throws IOException;

  /**
   * Close the writer.
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  void close() throws IOException;

}