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
import java.io.Writer;

import com.topologi.diffx.xml.esc.XMLEscapeWriter;
import com.topologi.diffx.xml.esc.XMLEscapeWriterUTF8;

/**
 * A base implementation for XML writers.
 *
 * <p>Provides methods to generate well-formed XML data easily. wrapping a writer.
 *
 * <p>This version only supports utf-8 encoding, if writing to a file make sure that the 
 * encoding of the file output stream is "utf-8".
 *
 * <p>The recommended implementation is to use a <code>BufferedWriter</code> to write.
 *
 * <pre>
 *  Writer writer =
 *     new BufferedWriter(new OutputStreamWriter(new FileOutputStream("foo.out"),"utf-8"));
 * </pre>
 *
 * @author  Christophe Lauret
 * @version 4 April 2005
 */
abstract class XMLWriterBase implements XMLWriter {

  /**
   * Indicates whether the xml should be indented or not.
   *
   * <p>The default is <code>true</code> (indented).
   *
   * <p>The indentation is 2 white-spaces.
   */
  final boolean indent;

  /**
   * Where the XML data goes.
   */
  final Writer writer;

  /**
   * Encoding of the output xml
   */
  static final String ENCODING = "utf-8";

  /**
   * Encoding of the output xml
   */
  final XMLEscapeWriter writerEscape;

  /**
   * Level of the depth of the xml document currently produced.
   *
   * <p>This attribute changes depending on the state of the instance.
   */
  int depth = 0;

/* ----------------------- constructors ----------------------- */

  /**
   * <p>Create a new XML writer.
   *
   * <p>Sets the depth attribute to 0 and the indentation to <code>true</code>.
   *
   * @param writer Where this writer should write the XML data.
   * 
   * @throws NullPointerException If the writer is <code>null</code>.
   */
  public XMLWriterBase(Writer writer) throws NullPointerException {
    this(writer, true);
  }

  /**
   * <p>Create a new XML writer.
   *
   * @param writer  Where this writer should write the XML data.
   * @param indent  Set the indentation flag.
   * 
   * @throws NullPointerException If the writer is <code>null</code>.
   */
  public XMLWriterBase(Writer writer, boolean indent) throws NullPointerException {
    if (writer == null) 
      throw new NullPointerException("XMLWriter cannot use a null writer.");
    this.writer = writer;
    this.writerEscape = new XMLEscapeWriterUTF8(writer);
    this.indent = indent;
  }

  // common methods ---------------------------------------------------------------------------

  /**
   * Write the XML declaration.
   *
   * <p>Always:
   * <pre>
   *   &lt;?xml version="1.0" encoding="<i>encoding</i>"?&gt;
   * </pre>
   * followed by a new line character.
   * 
   * @throws IOException If thrown by the wrapped writer. 
   */
  public final void xmlDecl() throws IOException {
    this.writer.write("<?xml version=\"1.0\" encoding=\""+ENCODING+"\"?>\n");
  }

  /**
   * Write the given text correctly for the encoding of this document.
   *
   * <p>This method turn the string into an array of chars.
   *
   * <p>Do nothing if the text is <code>null</code> or empty string.
   *
   * @see XMLEscapeWriter#writeText(String)
   *
   * @param text The text to write
   *
   * @throws IOException If thrown by the wrapped writer. 
   */
  public void writeText(String text) throws IOException {
    writerEscape.writeText(text);
  }

  /**
   * Write the given text correctly for the encoding of this
   * document.
   *
   * <p>This method turn the string into an array of chars.
   *
   * @see #writeChar
   *
   * @param text The text to write
   * @param off  The offset
   * @param len  The length to be written
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  public void writeText(char[] text, int off, int len) throws IOException {
    for (int i = off; i < (off+len); i++)
      writeChar(text[i]);
  }

  /**
   * @see com.topologi.diffx.xml.XMLWriter#writeXML(java.lang.String)
   */
  public void writeXML(String text) throws IOException {
    this.writer.write(text);
  }

  /**
   * @see com.topologi.diffx.xml.XMLWriter#writeXML(char[], int, int)
   */
  public void writeXML(char[] text, int off, int len) throws IOException {
    this.writer.write(text, off, len);
  }

  /**
   * Write the given character correctly.
   *
   * <ul>
   *   <li>'&lt;' escaped as '&amp;lt;'
   *   <li>'&gt;' escaped as '&amp;gt;'
   *   <li>'&amp;' escaped as '&amp;amp;'
   *   <li>Characters above 255 escaped as a character 
   *       reference &#<i>decimal_value</i>;
   *   <li>Control Characters 0 (less than 0x20) ignored,
   *       except line feed, carriage return and tabulations.
   *   <li>Control Characters 1 (between 0x80 and 0x9f) ignored.
   *   <li>Backspace character 0x7f ignored.
   *  </ul>
   *
   * @param c the character to write
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  public void writeChar(char c) throws IOException {
    writerEscape.writeText(c);
  }

  /**
   * Write the string value of an object.
   *
   * <p>Do nothing if the object is <code>null</code>.
   *
   * @see Object#toString
   * @see #writeText(java.lang.String)
   *
   * @param o The object that should be written as text.
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  public final void writeText(Object o) throws IOException {
    if (o != null)
      this.writeText(o.toString());
  }

  /**
   * Write an XML comment.
   *
   * <p>An XML comment is:<br>
   * <pre>
   *   &lt;!-- <i>comment</i> --&gt;
   * </pre>
   *
   * <p>Comments are not indented.
   *
   * @param comment the comment to be written
   * 
   * @throws IOException If thrown by the wrapped writer.
   * @throws IllegalArgumentException If the comment contains "--".
   */
  public void writeComment(String comment) throws IOException, IllegalArgumentException {
    if (comment != null && comment.indexOf("--") >= 0)
      throw new IllegalArgumentException("A comment should not contain '--'.");
    if (indent) this.writer.write('\n');
    this.writer.write("<!-- ");
    this.writer.write(comment);
    this.writer.write(" -->");
    if (indent) this.writer.write('\n');
  }

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
   * @throws IOException If an I/O exception is occurs.
   */
  public void writePI(String target, String data) throws IOException {
    this.writer.write("<?");
    this.writer.write(target);
    this.writer.write(' ');
    this.writer.write(data);
    this.writer.write("?>");
    if (indent) this.writer.write('\n');
  }

  /**
   * Insert spaces depending on the depth and if the <code>indent</code> flag
   * is set to true.
   *
   * <p>Insert two spaces per level of depth.
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  void indent() throws IOException {
    // TODO: allow different indent style
    if (indent) for (int i = 0; i < depth; i++) this.writer.write("  ");
  }

  // open/close specific elements -------------------------------------------------------------

  /**
   * Opens element, inserts text node and closes.
   * 
   * <p>The same as:
   * <pre>
   *   openElement(name);
   *   writeText(text);
   *   closeElement();
   * </pre>
   *
   * @param name The name of the element
   * @param text The text of the element
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  public void element(String name, String text) throws IOException {
    this.openElement(name);
    this.writeText(text);
    this.closeElement();
  }

  // direct access to the writer --------------------------------------------------------------

  /**
   * Write the given string directly.
   *
   * @param s the string to write
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  public final void write(String s) throws IOException {
    this.writer.write(s);
  }

  /**
   * Write the given character directly.
   *
   * @param c the character to write
   *
   * @throws IOException If thrown by the wrapped writer.
   */
  public final void write(char c) throws IOException {
    this.writer.write(c);
  }

  /**
   * Flush the writer.
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  public final void flush() throws IOException {
    this.writer.flush();
  }

  /**
   * Close the writer.
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  public final void close() throws IOException {
    this.writer.close();
  }

  /**
   * Does nothing.
   * 
   * <p>This method exists so that we can explicitly say that we should do nothing
   * in certain conditions. 
   */
  static final void doNothing() {
  	return;
  }

}