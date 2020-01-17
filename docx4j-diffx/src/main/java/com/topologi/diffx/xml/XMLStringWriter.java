/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml;

import java.io.IOException;
import java.io.StringWriter;

/**
 * An XML which writes on to a string.
 *
 * <p>This XML writer is backed by a {@link StringWriter} and will defer the XML writer's method to
 * either a {@link XMLWriterImpl} or {@link XMLWriterNSImpl} depending on whether namespace support is
 * required.
 *
 * <p>The write methods do not throw any {@link IOException}.
 *
 * <p>If the write is not set to support namespaces, the method which require a namespace URI will
 * throw an {@link UnsupportedOperationException}.
 *
 * @author  Christophe Lauret
 * @version 7 March 2012
 */
public final class XMLStringWriter implements XMLWriter {

  /**
   * Wraps an XML Writer
   */
  private final StringWriter writer;

  /**
   * Wraps an XML Writer
   */
  private final XMLWriter xml;

  /**
   * <p>Creates a new XML string writer.
   *
   * @param namespaces Whether this XML writer should use namespaces.
   */
  public XMLStringWriter(boolean namespaces) {
    this(namespaces, false);
  }

  /**
   * <p>Create a new XML string writer.
   *
   * @param namespaces Whether this XML writer should use namespaces.
   * @param indent  Set the indentation flag.
   */
  public XMLStringWriter(boolean namespaces, boolean indent) {
    this.writer = new StringWriter();
    this.xml = namespaces? new XMLWriterNSImpl(this.writer, indent) : new XMLWriterImpl(this.writer, indent);
  }

  // XML Writer methods
  // ----------------------------------------------------------------------------------------------

  @Override
  public void xmlDecl() {
    try {
      this.xml.xmlDecl();
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void setIndentChars(String spaces) {
    this.xml.setIndentChars(spaces);
  }

  @Override
  public void writeText(char c) {
    try {
      this.xml.writeText(c);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void writeText(String text) {
    try {
      this.xml.writeText(text);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void writeText(char[] text, int off, int len) {
    try {
      this.xml.writeText(text, off, len);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void writeCDATA(String cdata) {
    try {
      this.xml.writeCDATA(cdata);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void writeXML(String text) {
    try {
      this.xml.writeXML(text);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void writeXML(char[] text, int off, int len) {
    try {
      this.xml.writeXML(text, off, len);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void writeComment(String comment) {
    try {
      this.xml.writeComment(comment);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void writePI(String target, String data) {
    try {
      this.xml.writePI(target, data);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void openElement(String name) {
    try {
      this.xml.openElement(name);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void openElement(String name, boolean hasChildren) {
    try {
      this.xml.openElement(name, hasChildren);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void openElement(String uri, String name, boolean hasChildren) {
    try {
      this.xml.openElement(uri, name, hasChildren);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void closeElement() {
    try {
      this.xml.closeElement();
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void element(String name, String text) {
    try {
      this.xml.element(name, text);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void emptyElement(String element) {
    try {
      this.xml.emptyElement(element);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void emptyElement(String uri, String element) {
    try {
      this.xml.emptyElement(element);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void attribute(String name, String value) {
    try {
      this.xml.attribute(name, value);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void attribute(String name, int value) {
    try {
      this.xml.attribute(name, value);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void attribute(String uri, String name, String value) {
    try {
      this.xml.attribute(uri, name, value);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void attribute(String uri, String name, int value) {
    try {
      this.xml.attribute(uri, name, value);
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void setPrefixMapping(String uri, String prefix) {
    this.xml.setPrefixMapping(uri, prefix);
  }

  @Override
  public void flush() {
    try {
      this.xml.flush();
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  @Override
  public void close() throws UnclosedElementException {
    try {
      this.xml.close();
    } catch (IOException ex) {
      // Will not occur
      doNothing();
    }
  }

  /**
   * Returns the XML content as a {@link String}.
   *
   * @return the XML content as a {@link String}.
   */
  @Override
  public String toString() {
    return this.writer.toString();
  }

  /**
   * Do nothing
   */
  private static void doNothing(){
  }

}
