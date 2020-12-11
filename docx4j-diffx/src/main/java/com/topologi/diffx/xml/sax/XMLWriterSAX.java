/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml.sax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.topologi.diffx.xml.IllegalCloseElementException;
import com.topologi.diffx.xml.UnclosedElementException;
import com.topologi.diffx.xml.UndeclaredNamespaceException;
import com.topologi.diffx.xml.XMLWriter;
import com.topologi.diffx.xml.XMLWriterNSImpl;

/**
 * An XML writer that generates SAX2 events.
 *
 * <p>Provides methods to generate well-formed XML data easily through SAX events
 * by wrapping a content handler.
 *
 * <p>This XML writer provides an efficient way to process XML bypassing the need to create
 * an XML stream. Instead, this class will wrap a {@link org.xml.sax.ContentHandler} and
 * invoke the SAX2 methods.
 *
 * <pre>
 *  ContentHandler myContentHandler = ...;
 *  XMLWriter saxWriter = new XMLWriterSAX(myContentHandler);
 * </pre>
 *
 * <p>This SAX event writer as the following features:
 * <ul>
 *   <li><b>http://xml.org/sax/features/namespaces</b> set to <code>true</code></li>
 *   <li><b>http://xml.org/sax/features/namespace-prefixes</b> set to <code>false</code></li>
 * </ul>
 *
 * <p>Consequently, the attributes will not contain attributes used for namespace
 * declarations (xmlns* attributes).
 *
 * <p>This implementation does not provide qualified names, and will always return "".
 *
 * <p>The ContentHandler's <code>startDocument</code> and <code>endDocument</code> methods
 * have to be called externally.
 *
 * <p>Note that the write methods do not necessarily correspond to the content handler
 * methods or at least they may not be invoked at the same time. For example, the
 * <code>attribute</code> methods will not generate any event until it is possible to
 * invoke the <code>ContentHandler#startElement</code> method.
 *
 * @author  Christophe Lauret
 * @version 15 January 2007
 */
public final class XMLWriterSAX implements XMLWriter {

  // constants ----------------------------------------------------------------------------

  /**
   * Set to <code>true</code> to show debug info.
   */
  private static final boolean DEBUG = false;

  /**
   * The default namespace mapped to the empty prefix.
   */
  private static final PrefixMapping DEFAULT_NS = new PrefixMapping("", "");

  /**
   * The root node.
   */
  private static final Element ROOT;
  static {
    List<PrefixMapping> mps = new ArrayList<PrefixMapping>();
    mps.add(DEFAULT_NS);
    ROOT = new Element("", "", true, mps);
  }

  /**
   * The new line constant.
   */
  private static final char[] NEW_LINE = new char[]{'\n'};

  // class attributes ---------------------------------------------------------------------

  /**
   * Where the XML data goes.
   */
  private final ContentHandler handler;

  /**
   * Indicates whether the xml should be indented or not.
   *
   * <p>The default is <code>true</code> (indented).
   *
   * <p>The indentation is 2 white-spaces.
   */
  private boolean indent;

  /**
   * The default indentation spaces used.
   */
  private char[] indentChars;

  // state variables ----------------------------------------------------------------------

  /**
   * Level of the depth of the xml document currently produced.
   *
   * <p>This attribute changes depending on the state of the instance.
   */
  private transient int depth;

  /**
   * Flag to indicate that the element open tag is not finished yet.
   */
  private transient boolean isNude;

  /**
   * The current prefix mapping.
   */
  private transient Hashtable<String, String> prefixMapping = new Hashtable<String, String>();

  /**
   * The list of prefix mappings to be associated with the next element.
   */
  private transient List<PrefixMapping> tempMapping;

  /**
   * A stack of elements to close the elements automatically.
   */
  private transient List<Element> elements = new ArrayList<Element>();

  /**
   * The attributes attached to the current open element.
   *
   * <p>This variable can be <code>null</code> and should be set to <code>null</code>,
   * after the <code>startElementMethod</code> has been invoked.
   */
  private transient AttributesImpl attributes = new AttributesImpl();

  // constructors -------------------------------------------------------------------------

  /**
   * <p>Creates a new XML writer.
   *
   * @param handler The SAX2 content handler to use.
   *
   * @throws NullPointerException If the handler is <code>null</code>.
   */
  public XMLWriterSAX(ContentHandler handler) throws NullPointerException {
    if (handler == null)
      throw new NullPointerException("XMLWriter cannot use a null content handler.");
    this.handler = handler;
    this.elements.add(ROOT);
    this.prefixMapping.put("", "");
  }

  // setup methods ------------------------------------------------------------------------

  /**
   * Does nothing.
   */
  @Override
  public void xmlDecl() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setIndentChars(String spaces) throws IllegalStateException, IllegalArgumentException {
    if (this.depth != 0)
      throw new IllegalStateException("To late to set the indentation characters!");
    // check that this is a valid indentation string
    if (spaces != null) {
      for (int i = 0; i < spaces.length(); i++) {
        if (!Character.isSpaceChar(spaces.charAt(i)))
          throw new IllegalArgumentException("Not a valid indentation string.");
      }
      this.indentChars = spaces.toCharArray();
    }
    // update the flags
    this.indent = spaces != null;
  }

  // write text methods -------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeText(String text) throws IOException {
    if (text == null) return;
    try {
      deNude();
      this.handler.characters(text.toCharArray(), 0, text.length());
    } catch (SAXException ex) {
      handleEx(ex);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeText(char[] text, int off, int len) throws IOException {
    try {
      deNude();
      this.handler.characters(text, off, len);
    } catch (SAXException ex) {
      handleEx(ex);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeText(char c) throws IOException {
    try {
      deNude();
      this.handler.characters(new char[] {c}, 0, 1);
    } catch (SAXException ex) {
      handleEx(ex);
    }
  }

  /**
   * Writes the string value of an object.
   *
   * <p>Does nothing if the object is <code>null</code>.
   *
   * @see Object#toString
   * @see #writeText(java.lang.String)
   *
   * @param o The object that should be written as text.
   *
   * @throws IOException If thrown by the wrapped writer.
   */
  public void writeText(Object o) throws IOException {
    // TODO: what about an XML serializable ???
    // TODO: Add to interface ???
    if (o != null) {
      this.writeText(o.toString());
    }
  }

  @Override
  public void writeCDATA(String data) throws IOException {
    if (data == null) return;
    try {
      deNude();
      this.handler.characters(data.toCharArray(), 0, data.length());
    } catch (SAXException ex) {
      handleEx(ex);
    }
  }

  // write xml methods are not supported --------------------------------------------------

  /**
   * Always throw an <code>UnsupportedOperationException</code> exception.
   *
   * {@inheritDoc}
   */
  @Override
  public void writeXML(String text) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("Cannot run unparsed XML as SAX events");
  }

  /**
   * Always throw an <code>UnsupportedOperationException</code> exception.
   *
   * {@inheritDoc}
   */
  @Override
  public void writeXML(char[] text, int off, int len)
      throws UnsupportedOperationException {
    throw new UnsupportedOperationException("Cannot run unparsed XML as SAX events");
  }

  // PI and comments ----------------------------------------------------------------------

  /**
   * Does nothing as SAX content handler do not handle comments.
   *
   * {@inheritDoc}
   */
  @Override
  public void writeComment(String comment) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writePI(String target, String data) throws IOException {
    try {
      deNude();
      this.handler.processingInstruction(target, data);
      if (this.indent) {
        newLine();
      }
    } catch (SAXException ex) {
      handleEx(ex);
    }
  }

  // attribute methods --------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override
  public void attribute(String name, String value) throws IOException {
    if (!this.isNude)
      throw new IllegalStateException("Cannot write attribute: too late!");
    this.attributes.addAttribute(name, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void attribute(String name, int value) throws IOException {
    if (!this.isNude)
      throw new IllegalStateException("Cannot write attribute: too late!");
    this.attributes.addAttribute(name, Integer.toString(value));
  }

  /**
   * Writes an attribute.
   *
   * @param uri   The namespace URI this attribute belongs to.
   * @param name  The name of the attribute.
   * @param value The value of the attribute.
   *
   * @throws IOException If thrown by the wrapped writer.
   * @throws IllegalStateException If there is no open element or text has been written.
   */
  @Override
  public void attribute(String uri, String name, String value) throws IOException {
    if (!this.isNude) throw new IllegalStateException("Cannot write attribute: too late!");
    // TODO: check declared
    this.attributes.addAttribute(uri, name, value);
  }

  /**
   * Writes an attribute.
   *
   * <p>This method for number does not require escaping.
   *
   * @param uri   The namespace URI this attribute belongs to.
   * @param name  The name of the attribute.
   * @param value The value of the attribute.
   *
   * @throws IOException If thrown by the wrapped writer.
   * @throws IllegalStateException If there is no open element or text has been written.
   */
  @Override
  public void attribute(String uri, String name, int value) throws IOException {
    if (!this.isNude) throw new IllegalStateException("Cannot write attribute: too late!");
    // TODO: check declared
    this.attributes.addAttribute(uri, name, Integer.toString(value));
  }

  // open/close specific elements ---------------------------------------------------------

  /**
   * Writes the angle bracket if the element open tag is not finished.
   *
   * @throws SAXException If thrown by the content handler
   */
  private void deNude() throws SAXException {
    if (this.isNude) {
      indent();
      Element elt = peekElement();
      // report the prefix mapping
      if (elt.mappings != null) {
        for (int i = 0; i < elt.mappings.size(); i++) {
          PrefixMapping pm = elt.mappings.get(i);
          this.handler.startPrefixMapping(pm.prefix, pm.uri);
        }
      }
      this.handler.startElement(elt.uri, elt.name, getQName(elt.uri, elt.name), this.attributes);
      this.attributes = new AttributesImpl();
      if (this.indent && elt.hasChildren) {
        newLine();
      }
      this.isNude = false;
    }
  }

  /**
   * Writes a start element tag correctly indented.
   *
   * <p>It is the same as <code>openElement("", name, false)</code>
   *
   * @see #openElement(java.lang.String, java.lang.String, boolean)
   *
   * @param name the name of the element
   *
   * @throws IOException If thrown by the wrapped writer.
   */
  @Override
  public void openElement(String name) throws IOException {
    openElement("", name, false);
  }

  /**
   * Write a start element tag correctly indented.
   *
   * <p>It is the same as <code>openElement(name, false)</code>
   *
   * @see #openElement(java.lang.String, boolean)
   *
   * @param uri  The namespace URI of this element.
   * @param name The name of the element.
   *
   * @throws IOException If thrown by the wrapped writer.
   */
  public void openElement(String uri, String name) throws IOException {
    openElement(uri, name, false);
  }

  /**
   * Writes a start element tag correctly indented.
   *
   * <p>Use the <code>hasChildren</code> parameter to specify whether this element is terminal
   * node or not, note: this affects the indenting. To produce correctly indented XML, you
   * should use the same value for this flag when closing the element.
   *
   * <p>The name can contain attributes and should be a valid xml name.
   *
   * @param name        The name of the element.
   * @param hasChildren <code>true</code> if this element has children.
   *
   * @throws IOException If thrown by the wrapped writer.
   */
  @Override
  public void openElement(String name, boolean hasChildren) throws IOException {
    openElement("", name, hasChildren);
  }

  /**
   * Writes a start element tag correctly indented.
   *
   * <p>Use the <code>hasChildren</code> parameter to specify whether this element is terminal
   * node or not, note: this affects the indenting. To produce correctly indented XML, you
   * should use the same value for this flag when closing the element.
   *
   * <p>The name can contain attributes and should be a valid xml name.
   *
   * @param uri         The namespace URI of this element.
   * @param name        The name of the element.
   * @param hasChildren true if this element has children.
   *
   * @throws IOException If thrown by the wrapped writer.
   */
  @Override
  public void openElement(String uri, String name, boolean hasChildren) throws IOException {
    try {
      deNude();
    } catch (SAXException ex) {
      handleEx(ex);
    }
    this.elements.add(new Element(uri, name, hasChildren, this.tempMapping));
    this.tempMapping = null;
    this.isNude = true;
    this.depth++;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void element(String name, String text) throws IOException {
    this.openElement(name);
    this.writeText(text);
    closeElement();
  }

  // direct access to the writer ----------------------------------------------------------

  /**
   * Does nothing.
   */
  @Override
  public void flush() {
  }

  // base class and convenience methods ---------------------------------------------------

  /**
   * Insert the correct amount of space characters depending on the depth and if
   * the <code>indent</code> flag is set to <code>true</code>.
   *
   * @throws SAXException If thrown by the SAX handler.
   */
  private void indent() throws SAXException {
    if (this.indent) {
      char[] ch = new char[this.depth * this.indentChars.length];
      for (int i = 0; i < this.depth; i++) {
        for (int j = 0; j < this.indentChars.length; j++) {
          ch[i * this.indentChars.length + j] = this.indentChars[j];
        }
      }
      this.handler.ignorableWhitespace(ch, 0, ch.length);
    }
  }

  // close specific elements ------------------------------------------------------------

  /**
   * Write an end element tag.
   *
   * @throws IOException If thrown by the wrapped writer.
   */
  @Override
  public void closeElement() throws IOException {
    this.depth--;
    // this is an empty element
    try {
      // make sure that we finish with open element
      if (this.isNude) {
        deNude();
      }
      // now we can close the element
      Element elt = popElement();
      // we reached the end of the document too early
      if (elt == ROOT) throw new IllegalCloseElementException();
      // the element contains text / has children
      if (elt.hasChildren) {
        indent();
      }
      this.handler.endElement(elt.uri, elt.name, getQName(elt.uri, elt.name));
      // restore previous mapping if necessary
      if (elt.mappings != null) {
        for (int i = 0; i < elt.mappings.size(); i++) {
          PrefixMapping pm = elt.mappings.get(i);
          this.handler.endPrefixMapping(pm.prefix);
        }
      }
      restorePrefixMapping(elt);
      // take care of the new line if the indentation is on
      if (this.indent) {
        Element parent = peekElement();
        if (parent.hasChildren && parent != ROOT) {
          newLine();
        }
      }
    } catch (SAXException ex) {
      handleEx(ex);
    }
  }

  /**
   * Same as <code>emptyElement(null, element);</code>.
   *
   * @param element the name of the element
   *
   * @throws IOException If thrown by the wrapped writer.
   */
  @Override
  public void emptyElement(String element) throws IOException {
    emptyElement(null, element);
  }

  /**
   * Write an empty element.
   *
   * <p>It is possible for the element to contain attributes,
   * however, since there is no character escaping, great care
   * must be taken not to introduce invalid characters. For
   * example:
   * <pre>
   *    &lt;<i>example test="yes"</i>/&gt;
   * </pre>
   *
   * @param uri     The namespace URI for this element.
   * @param element The name of the element.
   *
   * @throws IOException If thrown by the wrapped writer.
   */
  @Override
  public void emptyElement(String uri, String element) throws IOException {
    try {
      deNude();
      indent();
      this.handler.startElement(uri, element, "", new AttributesImpl());
      this.handler.endElement(uri, element, "");
      this.tempMapping = null;
      newLine();
    } catch (SAXException ex) {
      handleEx(ex);
    }
  }

  /**
   * Returns the last element in the list.
   *
   * @return The current element.
   */
  private Element peekElement() {
    return this.elements.get(this.elements.size() - 1);
  }

  /**
   * Removes the last element in the list.
   *
   * @return The current element.
   */
  private Element popElement() {
    return this.elements.remove(this.elements.size() - 1);
  }

  // namespace handling -----------------------------------------------------------------------

  /**
   * @see com.topologi.diffx.xml.XMLWriter#setPrefixMapping(java.lang.String, java.lang.String)
   *
   * <p>This implementation does not keep a history of the prefix mappings so it needs to be
   * reset. If a prefix is already being used it is overridden.
   *
   * @param uri    The full namespace URI.
   * @param prefix The prefix for the namespace uri.
   *
   * @throws NullPointerException if the prefix is <code>null</code>.
   */
  @Override
  public void setPrefixMapping(String uri, String prefix) throws NullPointerException {
    //do not declare again if the same mapping already exist
    if (!prefix.equals(this.prefixMapping.get(uri))) {
      // remove the previous mapping to the prefix
      removeIfNeeded(prefix);
      // create the new prefix mapping
      PrefixMapping pm = new PrefixMapping(prefix, uri);
      this.prefixMapping.put(pm.uri, pm.prefix);
      if (DEBUG) {
        System.err.println(pm.prefix+" -> "+pm.uri);
      }
      if (this.tempMapping == null) {
        this.tempMapping = new ArrayList<PrefixMapping>();
      }
      this.tempMapping.add(pm);
    }
  }

  /**
   * Returns the qualified name for this element using the specified namespace URI.
   *
   * @param uri  The namespace URI for the element.
   * @param name The name of the element or attribute.
   *
   * @return The qualified element name.
   *
   * @throws UndeclaredNamespaceException If the uri has not being previously declared.
   */
  private String getQName(String uri, String name) throws UndeclaredNamespaceException {
    String prefix = this.prefixMapping.get(uri != null? uri : "");
    if (prefix != null) {
      if (!"".equals(prefix))
        return this.prefixMapping.get(uri)+":"+name;
      else
        return name;
    } else
      throw new UndeclaredNamespaceException(uri);
  }

  /**
   * Restores the prefix mapping after closing an element.
   *
   * <p>This costly operation need only to be done if the method
   * {@link XMLWriterNSImpl#setPrefixMapping(String, String)} have been used
   * immediately before, therefore it should not happen often.
   *
   * @param elt The element that had some new mappings.
   */
  private void restorePrefixMapping(Element elt) {
    if (elt.mappings != null) {
      // for each mapping of this element
      for (int i = 0; i < elt.mappings.size(); i++) {
        PrefixMapping mpi = elt.mappings.get(i);
        if (DEBUG) {
          System.err.print(mpi.prefix+" -< ");
        }
        // find the first previous namespace mapping amongst the parents
        // that defines namespace mappings
        for (int j = this.elements.size() - 1; j > 0; j--) {
          if (this.elements.get(j).mappings != null) {
            List<PrefixMapping> mps = this.elements.get(j).mappings;
            // iterate through the define namespace mappings of the parent
            for (int k = 0; k < mps.size(); k++) {
              PrefixMapping mpk = mps.get(k);
              // if we found a namespace prefix for the namespace
              if (mpk.prefix.equals(mpi.prefix)) {
                removeIfNeeded(mpk.prefix);
                this.prefixMapping.put(mpk.uri, mpk.prefix);
                if (DEBUG) {
                  System.err.println(mpk.uri+" [R]");
                }
                j = 0; // exit from the previous loop
                break; // exit from this one
              }
            }
          }
        }
      }
    }
  }

  /**
   * Removes the mapping associated to the specified prefix.
   *
   * @param prefix The prefix which mapping should be removed.
   */
  private void removeIfNeeded(String prefix) {
    // remove the previous mapping to the prefix
    if (this.prefixMapping.containsValue(prefix)) {
      Object key = null;
      for (Enumeration<String> e = this.prefixMapping.keys(); e.hasMoreElements();) {
        key = e.nextElement();
        if (this.prefixMapping.get(key).equals(prefix)) {
          break;
        }
      }
      this.prefixMapping.remove(key); // we know key should have a value
    }
  }

  /**
   * Closes the writer.
   *
   * <p>This method only checks that it is possible to close the writer.
   *
   * @throws IOException If thrown by the wrapped writer.
   * @throws UnclosedElementException If an element has been left open.
   */
  @Override
  public void close() throws IOException, UnclosedElementException {
    Element open = peekElement();
    if (open != ROOT)
      throw new UnclosedElementException(open.name);
  }

  /**
   * Generates a new line as an ignorable white space event
   *
   * @throws SAXException If thrown by the handler.
   */
  private void newLine() throws SAXException {
    this.handler.characters(NEW_LINE, 0, 1);
  }

  /**
   * Handles the SAX Exception.
   *
   * @param ex A SAXException thrown by the content handler.
   *
   * @throws IOException If thrown by the handler.
   */
  private void handleEx(SAXException ex) throws IOException {
    throw new IOException("Exception thrown by the wrapped content handler:\n"
        +ex.getMessage());
  }

  // inner class: Element ---------------------------------------------------------------------

  /**
   * A light object to keep track of the elements
   *
   * @author Christophe Lauret (Allette Systems)
   * @version 26 May 2005
   */
  private static final class Element {

    /**
     * The namespace URI of the element.
     */
    private final String uri;

    /**
     * The local name of the element.
     */
    private final String name;

    /**
     * A list of prefix mappings for this element.
     *
     * <p>Can be <code>null</code>.
     */
    private final List<PrefixMapping> mappings;

    /**
     * Indicates whether the element has children.
     */
    private final boolean hasChildren;

    /**
     * Creates a new Element.
     *
     * @param uri         The namespace URI of the element.
     * @param name        The local name of the element.
     * @param hasChildren Whether the element has children.
     * @param mappings    The list of prefix mapping if any.
     */
    public Element(String uri, String name, boolean hasChildren, List<PrefixMapping> mappings) {
      this.uri = uri;
      this.name = name;
      this.hasChildren = hasChildren;
      this.mappings = mappings;
    }
  }

  // inner class: Prefix Mapping ----------------------------------------------------------

  /**
   * Light-weight class to represent a prefix mapping.
   *
   * <p>The class attributes cannot be <code>null</code>.
   *
   * @author Christophe Lauret (Allette Systems)
   * @version 31 August 2004
   */
  private static final class PrefixMapping {

    /**
     * The prefix associated to the URI.
     */
    private final String prefix;

    /**
     * The namespace URI.
     */
    private final String uri;

    /**
     * Creates a new prefix mapping.
     *
     * @param prefix The prefix for the URI.
     * @param uri    The full namespace URI.
     */
    public PrefixMapping(String prefix, String uri) {
      this.prefix = prefix != null? prefix : "";
      this.uri = uri != null? uri : "";
    }
  }

  // inner class: SAX Attribute List implementation ---------------------------------------

  /**
   * A SAX attribute list implementation.
   *
   * <p>Note: the type of all attributes is CDATA.
   *
   * @author Christophe Lauret
   * @version 25 May 2005
   */
  private static final class AttributesImpl implements Attributes {

    /**
     * The only type used in this class.
     */
    private static final String CDATA = "CDATA";

    /**
     * Namespace URIs of the attributes.
     */
    private final List<String> uris = new ArrayList<String>();

    /**
     * QNames of the attributes.
     */
    private final List<String> names = new ArrayList<String>();

    /**
     * Values of the attributes.
     */
    private final List<String> values = new ArrayList<String>();

    /**
     * Creates an empty attribute list.
     */
    public AttributesImpl() {
    }

    /**
     * Adds an attribute to an attribute list.
     *
     * @param name The attribute name.
     * @param value The attribute value (must not be null).
     *
     * @see #removeAttribute
     * @see org.xml.sax.DocumentHandler#startElement
     */
    public void addAttribute(String name, String value) {
      this.uris.add("");
      this.names.add(name);
      this.values.add(value);
    }

    /**
     * Adds an attribute to an attribute list.
     *
     * @param uri  The namespace URI of the attribute
     * @param name The attribute name.
     * @param value The attribute value (must not be null).
     *
     * @see #removeAttribute
     * @see org.xml.sax.DocumentHandler#startElement
     */
    public void addAttribute(String uri, String name, String value) {
      this.uris.add("");
      this.names.add(name);
      this.values.add(value);
    }

    // Attributes methods, indexed access -------------------------------------------------

    /**
     * Returns the number of attributes in the list.
     *
     * @return The number of attributes in the list.
     *
     * @see org.xml.sax.AttributeList#getLength
     */
    @Override
    public int getLength() {
      return this.names.size();
    }

    /**
     * This implementation always returns "".
     *
     * @param i The position of the attribute in the list.
     * @return The attribute qualified name as a string;
     *         or <code>null</code> if there is no attribute at that position.
     *
     * @see org.xml.sax.Attributes#getQName(int)
     */
    @Override
    public String getQName(int i) {
      if (i < 0) return null;
      try {
        // FIXME: not SAX2 compliant
        return getLocalName(i);
      } catch (ArrayIndexOutOfBoundsException e) {
        return null;
      }
    }

    /**
     * Returns the name of an attribute (by position).
     *
     * @param i The position of the attribute in the list.
     * @return The attribute name as a string;
     *         or <code>null</code> if there is no attribute at that position.
     *
     * @see org.xml.sax.Attributes#getQName(int)
     */
    @Override
    public String getLocalName(int i) {
      if (i < 0) return null;
      try {
        return this.names.get(i);
      } catch (ArrayIndexOutOfBoundsException e) {
        return null;
      }
    }

    /**
     * Returns the type of an attribute (by position).
     *
     * @param i The position of the attribute in the list.
     * @return The attribute type as "CDATA";
     *         or <code>null</code> if there is no attribute at that position.
     *
     * @see org.xml.sax.Attributes#getType(int)
     */
    @Override
    public String getType(int i) {
      if (i < 0) return null;
      try {
        return CDATA;
      } catch (ArrayIndexOutOfBoundsException e) {
        return null;
      }
    }

    /**
     * Returns the value of an attribute (by position).
     *
     * @param i The position of the attribute in the list.
     *
     * @return The attribute value as a string;
     *         or <code>null</code> if there is no attribute at that position.
     *
     * @see org.xml.sax.Attributes#getValue(int)
     */
    @Override
    public String getValue(int i) {
      if (i < 0) return null;
      try {
        return this.values.get(i);
      } catch (ArrayIndexOutOfBoundsException e) {
        return null;
      }
    }

    /**
     * Returns the namespace URI of an attribute (by position).
     *
     * @param i The position of the attribute in the list.
     *
     * @return The attribute namespace URI as a string;
     *         or <code>null</code> if there is no attribute at that position.
     *
     * @see org.xml.sax.Attributes#getValue(int)
     */
    @Override
    public String getURI(int i) {
      if (i < 0) return null;
      try {
        return this.uris.get(i);
      } catch (ArrayIndexOutOfBoundsException e) {
        return null;
      }
    }

    /**
     * Returns <code>null</code> as qualified names are not available.
     *
     * @param qName The attribute name.
     *
     * @return <code>null</code>
     *
     * @see org.xml.sax.Attributes#getType(java.lang.String)
     */
    @Override
    public String getType(String qName) {
      return null;
    }

    /**
     * Returns <code>null</code> as qualified names are not available.
     *
     * @param name The attribute name.
     *
     * @see org.xml.sax.Attributes#getValue(java.lang.String)
     */
    @Override
    public String getValue(String name) {
      return null;
    }

    /**
     * Returns the type of an attribute (by name).
     *
     * @param uri  The namespace URI of the attribute.
     * @param name The attribute name.
     * @return The attribute type as a string
     *         ("NMTOKEN" for an enumeration, and "CDATA" if no declaration was read).
     * @see org.xml.sax.Attributes#getType(java.lang.String)
     */
    @Override
    public String getType(String uri, String name) {
      return getType(getIndex(uri, name));
    }

    /**
     * Returns the value of an attribute (by name).
     *
     * @param uri  The namespace URI of the attribute.
     * @param name The attribute name.
     * @see org.xml.sax.AttributeList#getValue(java.lang.String)
     */
    @Override
    public String getValue(String uri, String name) {
      return getValue(getIndex(uri, name));
    }

    /**
     * Look up the index of an attribute by Namespace name.
     *
     * @param uri The Namespace URI, or the empty string if the name has no Namespace URI.
     * @param localName The attribute's local name.
     *
     * @return The index of the attribute, or -1 if it does not appear in the list.
     */
    @Override
    public int getIndex(String uri, String localName) {
      // try if only one attribute with that name
      int index = this.names.indexOf(localName);
      if (index == -1) return index;
      if (this.uris.get(index).equals(uri)) return index;
      // otherwise iterate
      for (int i = 0; i < this.names.size(); i++) {
        if (this.names.get(i).equals(localName) && this.uris.get(i).equals(uri)) return i;
      }
      return -1;
    }

    /**
     * Look up the index of an attribute by Namespace name.
     *
     * @param qName The index of the given name.
     *
     * @return The index of the attribute, or -1 if it does not appear in the list.
     */
    @Override
    public int getIndex(String qName) {
      // FIXME: not SAX2 compliant
      return getIndex("", qName);
    }

  }

}
