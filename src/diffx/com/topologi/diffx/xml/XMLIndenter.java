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
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
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
  private transient Stack states = new Stack();

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
   * @see org.xml.sax.ContentHandler#startElement
   */
  public void startElement(String uri, String localName, String qName, Attributes atts) {
    // update the state of previous element
    if (!states.empty()) {
      if (states.pop().equals(EMPTY))
        writer.println('>');
      states.push(HAS_CHILDREN);
    }
    // always indent
    for (int i = 0; i < indentLevel; i++)
      writer.print("  ");
    // print XML data
    writer.print('<' + qName);
    for (int i = 0; i < atts.getLength(); i++) {
      writer.print(' '+atts.getQName(i)+"=\""+atts.getValue(i)+'"');
    }
    // update attributes
    indentLevel++;
    states.push(EMPTY);
  }

  /**
   * @see org.xml.sax.ContentHandler#endElement
   */
  public void endElement(String uri, String localName, String qName) {
    this.indentLevel--;
    Object state = states.pop();
    if (EMPTY.equals(state)) {
      writer.println("/>");
    } else if (HAS_TEXT.equals(state)) {
      writer.println("</" + qName + '>');      
    } else if (HAS_CHILDREN.equals(state)) {
      for (int i = 0; i < indentLevel; i++)
        writer.print("  ");
      writer.println("</" + qName + '>');
    }
  }

  /**
   * Prints the characters.
   * 
   * @see org.xml.sax.ContentHandler#characters(char[], int, int)
   */
  public void characters(char[] ch, int position, int offset) {
    if (states.peek().equals(EMPTY)) {
      states.pop();
      writer.print('>');
      states.push(HAS_TEXT);
    }
    writer.print(new String(ch, position, offset));
 }

  /**
   * Does nothing.
   * 
   * @see org.xml.sax.ContentHandler
   */
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
    XMLReader xmlreader = factory.newSAXParser().getXMLReader();
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
