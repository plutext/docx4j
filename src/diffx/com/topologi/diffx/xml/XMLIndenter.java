/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A class to indent automatically some XML data.
 *
 * <p>Note: This implementation is not namespace aware, and will not handle entities other than
 * &amp;amp;, &amp;lt;, &amp;gt; or &amp;quot;.
 *
 * @author Christophe Lauret - Allette Systems (Australia)
 * @version 26 February 2005
 */
public final class XMLIndenter extends DefaultHandler implements ContentHandler {

  /**
   * The writer where the XML goes.
   */
  private final PrintWriter writer;

  // state attributes ---------------------------------------------------------------------------

  /**
   * The indentation level.
   */
  private transient int indentLevel = 0;

  /**
   * The stack of states
   */
  private transient Stack<Integer> states = new Stack<Integer>();

  /**
   * Element has neither text, nor children.
   */
  private static final Integer EMPTY = new Integer(0);

  /**
   * Element has text.
   */
  private static final Integer HAS_TEXT = new Integer(1);

  /**
   * Element has children.
   */
  private static final Integer HAS_CHILDREN = new Integer(2);

  /* ----------------------------------------- constructor --------------------------------------- */

  /**
   * Creates a new XML Indenter.
   *
   * @param w The writer to use.
   */
  private XMLIndenter(Writer w) {
    if (w instanceof PrintWriter) {
      this.writer = (PrintWriter) w;
    } else {
      this.writer = new PrintWriter(w);
    }
  }

  /* -------------------------------------- handler's methods ------------------------------------ */

  /**
   * {@inheritDoc}
   */
  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) {
    // update the state of previous element
    if (!this.states.empty()) {
      if (this.states.pop().equals(EMPTY)) {
        this.writer.println('>');
      }
      this.states.push(HAS_CHILDREN);
    }
    // always indent
    for (int i = 0; i < this.indentLevel; i++) {
      this.writer.print("  ");
    }
    // print XML data
    this.writer.print('<' + qName);
    for (int i = 0; i < atts.getLength(); i++) {
      this.writer.print(' '+atts.getQName(i)+"=\""+atts.getValue(i)+'"');
    }
    // update attributes
    this.indentLevel++;
    this.states.push(EMPTY);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void endElement(String uri, String localName, String qName) {
    this.indentLevel--;
    Object state = this.states.pop();
    if (EMPTY.equals(state)) {
      this.writer.println("/>");
    } else if (HAS_TEXT.equals(state)) {
      this.writer.println("</" + qName + '>');
    } else if (HAS_CHILDREN.equals(state)) {
      for (int i = 0; i < this.indentLevel; i++) {
        this.writer.print("  ");
      }
      this.writer.println("</" + qName + '>');
    }
  }

  /**
   * Prints the characters.
   *
   * {@inheritDoc}
   */
  @Override
  public void characters(char[] ch, int position, int offset) {
    if (this.states.peek().equals(EMPTY)) {
      this.states.pop();
      this.writer.print('>');
      this.states.push(HAS_TEXT);
    }
    this.writer.print(new String(ch, position, offset));
  }

  /**
   * Does nothing.
   *
   * {@inheritDoc}
   */
  @Override
  public void ignorableWhitespace(char[] ch, int position, int offset) {
    // do nothing.
  }

  /* ---------------------------------------- static methods ------------------------------------- */

  /**
   * Indents the given XML String.
   *
   * @param xml The XML string to indent
   *
   * @return The indented XML String.
   *
   * @throws IOException If an IOException occurs.
   * @throws SAXException If the XML is not well-formed.
   * @throws ParserConfigurationException If the parser could not be configured
   */
  public static String indent(String xml)
      throws SAXException, IOException, ParserConfigurationException {
    Writer writer = new StringWriter();
    Reader reader = new StringReader(xml);
    indent(reader, writer);
    return writer.toString();
  }

  /**
   * Indents the given XML String.
   *
   * @param r A reader on XML data
   * @param w A writer for the indented XML
   *
   * @throws IOException If an IOException occurs.
   * @throws SAXException If the XML is not well-formed.
   * @throws ParserConfigurationException If the parser could not be configured
   */
  public static void indent(Reader r, Writer w)
      throws SAXException, IOException, ParserConfigurationException {
    // create the indenter
    XMLIndenter indenter = new XMLIndenter(w);
    // initialise the SAX framework
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(false);
    factory.setValidating(false);
    InputSource source = new InputSource(r);
    // parse the XML
    SAXParser parser = factory.newSAXParser();
    parser.setProperty("http://xml.org/sax/features/external-general-entities", false);
    parser.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
    parser.setProperty("http://apache.org/xml/features/disallow-doctype-decl", true);
    
    XMLReader xmlreader = parser.getXMLReader();
    xmlreader.setContentHandler(indenter);
    xmlreader.parse(source);
  }

  /**
   * Indents the given XML String.
   *
   * @param xml The XML string to indent
   *
   * @return The indented XML String or <code>null</code> if an error occurred.
   */
  public static String indentSilent(String xml) {
    try {
      return indent(xml);
    } catch (Exception ex) {
      return null;
    }
  }

  /**
   * Indents the given XML String.
   *
   * <p>This method does not throw any exception out of convenience, instead it returns a
   * <code>boolean</code> value to indicate whether the XML indenting was performed succesfully.
   *
   * @param r A reader on XML data
   * @param w A writer for the indented XML
   *
   * @return <code>true</code> if the operation was successful, <code>false</code> if an error
   *         occurred.
   */
  public static boolean indentSilent(Reader r, Writer w) {
    try {
      indent(r, w);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

}
