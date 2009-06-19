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
import java.util.ArrayList;
import java.util.List;

/**
 * A simple writer for XML data that does not support namespaces.
 *
 * <p>Provides methods to generate well-formed XML data easily, wrapping a writer.
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
 * <p>This class is not synchronised and does not support namespaces, and will therefore
 * throw an unsupported operation exception for each call to a method that uses namespaces. 
 * 
 * @author  Christophe Lauret
 * @version 18 March 2005
 */
public final class SimpleXMLWriter extends XMLWriterBase implements XMLWriter {

  /**
   * The root node.
   */
  private static final Element ROOT;
  static {
    ROOT = new Element("", true);
  }

  /**
   * A stack of elements to close the elements automatically. 
   */
  private List elements = new ArrayList(); 

  /**
   * Flag to indicate that the element open tag is not finished yet. 
   */
  private boolean isNude = false; 

  // constructors -----------------------------------------------------------------------------

  /**
   * <p>Creates a new XML writer.
   *
   * <p>Sets the depth attribute to 0 and the indentation to <code>true</code>.
   *
   * @param writer Where this writer should write the XML data.
   * 
   * @throws NullPointerException If the writer is <code>null</code>.
   */
  public SimpleXMLWriter(Writer writer) throws NullPointerException {
    super(writer, true);
    this.elements.add(ROOT);
  }

  /**
   * <p>Create a new XML writer.
   *
   * @param writer  Where this writer should write the XML data.
   * @param indent  Set the indentation flag.
   * 
   * @throws NullPointerException If the writer is <code>null</code>.
   */
  public SimpleXMLWriter(Writer writer, boolean indent) throws NullPointerException {
    super(writer, indent);
    this.elements.add(ROOT);
  }

// PI and comments ----------------------------------------------------------------------------

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
   */
  public void writeComment(String comment) throws IOException {
    deNude();
    super.writeComment(comment);
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
    deNude();
    super.writePI(target, data);
  }

// writing text -------------------------------------------------------------------------------

  /**
   * @see com.topologi.diffx.xml.XMLWriter#writeText(char)
   */
  public void writeText(char c) throws IOException {
    deNude();
    writerEscape.writeText(c);
  }

  /**
   * @see com.topologi.diffx.xml.XMLWriter#writeText(String)
   */
  public void writeText(String text) throws IOException {
    if (text == null) return;
    deNude();
    writerEscape.writeText(text);
  }

  /**
   * Writes the given text correctly for the encoding of this document.
   *
   * @see com.topologi.diffx.xml.XMLWriter#writeText(char[], int, int)
   */
  public void writeText(char[] text, int off, int len) throws IOException {
    deNude();
    writerEscape.writeText(text, off, len);
  }

  /**
   * @see com.topologi.diffx.xml.XMLWriter#writeXML(java.lang.String)
   */
  public void writeXML(String text) throws IOException {
    if (text == null) return;
    deNude();
    this.writer.write(text);
  }

  /**
   * @see com.topologi.diffx.xml.XMLWriter#writeXML(char[], int, int)
   */
  public void writeXML(char[] text, int off, int len) throws IOException {
    deNude();
    this.writer.write(text, off, len);
  }

  /**
   * Writes the angle bracket if the element open tag is not finished.
   *
   * @throws IOException If thrown by the wrapped writer.
   */
  private void deNude() throws IOException {
    if (this.isNude) {
      writer.write('>');
      if (peekElement().hasChildren) writer.write('\n');
      this.isNude = false;
    }
  }

// open/close specific elements ------------------------------------------------------------

  /**
   * Writes a start element tag correctly indented.
   *
   * <p>It is the same as <code>openElement(null, name, false)</code>
   *
   * @see #openElement(java.lang.String, java.lang.String, boolean)
   *
   * @param name The name of the element
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  public void openElement(String name) throws IOException {
    openElement(name, false);
  }

  /**
   * Writes a start element tag correctly indented.
   *
   * <p>Use the <code>hasChildren</code> parameter to specify whether this element is 
   * terminal node or not, which affects the indenting.
   *
   * <p>The name can contain attributes and should be a valid xml name.
   *
   * @param name        The name of the element.
   * @param hasChildren <code>true</code> if this element has children.
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  public void openElement(String name, boolean hasChildren) throws IOException {
    deNude();
    this.indent();
    this.elements.add(new Element(name, hasChildren));
    this.writer.write('<');
    this.writer.write(name);
    this.isNude = true;
    this.depth++;
  }

  /**
   * Write the end element tag.
   *
   * @throws IOException If thrown by the wrapped writer.
   */
  public void closeElement() throws IOException {
    Element elt = popElement();
    this.depth--;
    // this is an empty element
    if (this.isNude) {
      writer.write('/');
      this.isNude = false;
    // the element contains text
    } else {
      if (elt.hasChildren) this.indent();
      this.writer.write('<');
      this.writer.write('/');
      int x = elt.name.indexOf(' ');
      if (x < 0)
        this.writer.write(elt.name);
      else
        this.writer.write(elt.name.substring(0, x));
    }
    this.writer.write('>');
//    this.writer.write('\n');
  }

  /**
   * Same as <code>emptyElement(null, element);</code>.
   * 
   * <p>It is possible for the element to contain attributes,
   * however, since there is no character escaping, great care
   * must be taken not to introduce invalid characters. For
   * example:
   * <pre>
   *    &lt;<i>example test="yes"</i>/&gt;
   * </pre>
   *
   * @param element the name of the element
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  public void emptyElement(String element) throws IOException {
    deNude();
    this.indent();
    this.writer.write('<');
    this.writer.write(element);
    this.writer.write('/');
    this.writer.write('>');
    this.writer.write('\n');
  }

  /**
   * Returns the last element in the list.
   * 
   * @return The current element.
   */
  private Element peekElement() {
    return ((Element)this.elements.get(this.elements.size() - 1));
  }

  /**
   * Removes the last element in the list.
   * 
   * @return The current element.
   */
  private Element popElement() {
    return ((Element)this.elements.remove(this.elements.size() - 1));
  }

// attributes ---------------------------------------------------------------------------------

  /**
   * Writes an attribute.
   *
   * @param name  The name of the attribute.
   * @param value The value of the attribute.
   * 
   * @throws IOException If thrown by the wrapped writer.
   * @throws IllegalStateException If there is no open element or text has been written. 
   */
  public void attribute(String name, String value)
      throws IOException, IllegalStateException {
    if (!this.isNude) throw new IllegalArgumentException("Cannot write attribute: too late!");
    this.writer.write(' ');
    this.writer.write(name);
    this.writer.write("=\"");
    writerEscape.writeAttValue(value);
    this.writer.write('"');
  }

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
  public void attribute(String name, int value)
      throws IOException, IllegalStateException {
    if (!this.isNude) throw new IllegalArgumentException("Cannot write attribute: too late!");
    this.writer.write(' ');
    this.writer.write(name);
    this.writer.write("=\"");
    this.writer.write(Integer.toString(value));
    this.writer.write('"');
  }

  // unsupported operations -------------------------------------------------------------------

  /**
   * Not supported.
   *
   * @param uri  This parameter is ignored.
   * @param name This parameter is ignored.
   * 
   * @throws UnsupportedOperationException This class does not handle namespaces.
   */
  public void openElement(String uri, String name) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("This class does not handle namespaces.");
  }

  /**
   * Not supported.
   *
   * @param uri         This parameter is ignored.
   * @param name        This parameter is ignored.
   * @param hasChildren This parameter is ignored.
   * 
   * @throws UnsupportedOperationException This class does not handle namespaces.
   */
  public void openElement(String uri, String name, boolean hasChildren)
    throws UnsupportedOperationException {
    throw new UnsupportedOperationException("This class does not handle namespaces.");
  }

  /**
   * Not supported.
   *
   * @param uri     This parameter is ignored.
   * @param element This parameter is ignored.
   * 
   * @throws UnsupportedOperationException This class does not handle namespaces.
   */
  public void emptyElement(String uri, String element) 
      throws UnsupportedOperationException {
    throw new UnsupportedOperationException("This class does not handle namespaces");
  }

  /**
   * Not supported.
   * 
   * @param uri    This parameter is ignored.
   * @param prefix This parameter is ignored.
   * 
   * @throws UnsupportedOperationException This class does not handle namespaces.
   */
  public void setPrefixMapping(String uri, String prefix)
      throws UnsupportedOperationException {
    throw new UnsupportedOperationException("This class does not handle namespaces");
  }

  /**
   * Not supported.
   * 
   * @param uri    This parameter is ignored.
   * @param name  The name of the attribute.
   * @param value The value of the attribute.

   * 
   * @throws UnsupportedOperationException This class does not handle namespaces.
   */
  public void attribute(String uri, String name, String value) 
      throws UnsupportedOperationException {
    throw new UnsupportedOperationException("This class does not handle namespaces");
  }

  /**
   * Not supported.
   * 
   * @param uri    This parameter is ignored.
   * @param name  The name of the attribute.
   * @param value The value of the attribute.
   * 
   * @throws UnsupportedOperationException This class does not handle namespaces.
   */
  public void attribute(String uri, String name, int value) 
     throws UnsupportedOperationException {
    throw new UnsupportedOperationException("This class does not handle namespaces");
  }

  // inner class: Element --------------------------------------------------------------------- 

  /**
   * A light object to keep track of the element.
   * 
   * <p>This object does not support namespaces.
   * 
   * @author Christophe Lauret
   * @version 7 March 2005
   */
  private static final class Element {

    /**
     * The fully qualified name of the element.
     */
    private String name;

    /**
     * Indicates whether the element has children. 
     */
    private boolean hasChildren;

    /**
     * Creates a new Element.
     * 
     * @param name       The qualified name of the element.
     * @param hasChildren Whether the element has children.
     */
    public Element(String name, boolean hasChildren) {
      this.name = name;
      this.hasChildren = hasChildren;
    }
  }

}