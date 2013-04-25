/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml.sax;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * A content handler wrapping another content handler and reporting information about
 * what methods are being called.
 * 
 * @author Christophe Lauret (Allette Systems)
 * @version 26 October 2005
 */
public final class ReporterHandlerProxy implements ContentHandler {

  /**
   * The handler that will receive events.
   */
  private final ContentHandler handler;

  /**
   * Creates a new handler proxy.
   * 
   * @param handler The handler that will receive events.
   */
  public ReporterHandlerProxy(ContentHandler handler) {
    this.handler = handler;
  }

  /**
   * {@inheritDoc}
   */
  public void characters(char[] ch, int start, int length) throws SAXException {
    System.err.println("characters(\"" + new String(ch, start, length) + "\");");
    this.handler.characters(ch, start, length);
  }

  /**
   * {@inheritDoc}
   */
  public void startElement(String uri, String local, String qName, Attributes atts)
      throws SAXException {
    System.err.println("startElement(\""+uri+"\", \""+local+"\", \""+qName+"\");");
    this.handler.startElement(uri, local, qName, atts);
  }

  /**
   * {@inheritDoc}
   */
  public void endElement(String uri, String local, String qName) throws SAXException {
    System.err.println("endElement(\""+uri+"\", \""+local+"\", \""+qName+"\");");
    this.handler.endElement(uri, local, qName);
  }

  /**
   * {@inheritDoc}
   */
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    System.err.println("startPrefixMapping(\"" + prefix + "\", \"" + uri + "\");");
    this.handler.startPrefixMapping(prefix, uri);
  }

  /**
   * {@inheritDoc}
   */
  public void endPrefixMapping(String prefix) throws SAXException {
    System.err.println("endPrefixMapping(\"" + prefix + "\");");
    this.handler.endPrefixMapping(prefix);
  }

  /**
   * {@inheritDoc}
   */
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    System.err.println("ignorableWhitespace(\""+new String(ch, start, length)+"\");");
    this.handler.ignorableWhitespace(ch, start, length);
  }

  /**
   * {@inheritDoc}
   */
  public void processingInstruction(String target, String data) throws SAXException {
    System.err.println("processingInstruction(\"" + target + "\", \"" + data + "\");");
    this.handler.processingInstruction(target, data);
  }

  /**
   * {@inheritDoc}
   */
  public void skippedEntity(String name) throws SAXException {
    System.err.println("skippedEntity(\""+name+"\");");
    this.handler.skippedEntity(name);
  }

  /**
   * {@inheritDoc}
   */
  public void setDocumentLocator(Locator locator) {
    this.handler.setDocumentLocator(locator);
  }

  /**
   * {@inheritDoc}
   */
  public void startDocument() throws SAXException {
    System.err.println("startDocument();");
    this.handler.startDocument();
  }

  /**
   * {@inheritDoc}
   */
  public void endDocument() throws SAXException {
    System.err.println("endDocument();");
    this.handler.endDocument();
  }

  /**
   * Returns the content handler.
   * 
   * @return the wrapped content handler.
   */
  public ContentHandler getContentHandler() {
    return this.handler;
  }

}
