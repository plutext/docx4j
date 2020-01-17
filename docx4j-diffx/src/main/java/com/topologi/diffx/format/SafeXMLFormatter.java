/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.format;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Enumeration;

import com.topologi.diffx.config.DiffXConfig;
import com.topologi.diffx.config.WhiteSpaceProcessing;
import com.topologi.diffx.event.AttributeEvent;
import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.event.OpenElementEvent;
import com.topologi.diffx.event.impl.CharEvent;
import com.topologi.diffx.event.impl.CharactersEventBase;
import com.topologi.diffx.event.impl.SpaceEvent;
import com.topologi.diffx.sequence.PrefixMapping;
import com.topologi.diffx.util.Constants;
import com.topologi.diffx.xml.XMLWriter;
import com.topologi.diffx.xml.XMLWriterNSImpl;

/**
 * An XML formatter that tries to ensure that the output XML will be well-formed.
 *
 * <p>This class will always close the elements correctly by maintaining a stack of parent elements.
 *
 * <p>Implementation note: this classes uses the namespace prefixes 'dfx' and 'del', in the
 * future it should be possible to configure which prefixes to use for each namespace, but
 * in this version the namespace prefix mapping is hard-coded.
 *
 * @author Christophe Lauret
 * @version 11 May 2010
 */
public final class SafeXMLFormatter implements XMLDiffXFormatter {

  /**
   * Set to <code>true</code> to show debug info.
   */
  private static final boolean DEBUG = false;

  // class attributes ---------------------------------------------------------------------------

  /**
   * The output goes here.
   */
  private final XMLWriter xml;

  /**
   * The DiffX configuration to use
   */
  private DiffXConfig config = new DiffXConfig();

  // state variables ----------------------------------------------------------------------------

  /**
   * Set to <code>true</code> to include the XML declaration.
   *
   * <p>This attribute is set to <code>false</code> when the {@link #setWriteXMLDeclaration(boolean)}
   * is called with <code>false</code> or once the XML declaration has been written.
   */
  private transient boolean writeXMLDeclaration = true;

  // constructors -------------------------------------------------------------------------------

  /**
   * Creates a new formatter on the standard output.
   *
   * <p>This constructor is equivalent to:
   * <pre>new SmartXMLFormatter(new PrintWriter(System.out));</pre>.
   *
   * @see System#out
   *
   * @throws IOException should an I/O exception occurs.
   */
  public SafeXMLFormatter() throws IOException {
    this(new PrintWriter(System.out));
  }

  /**
   * Creates a new formatter using the specified writer.
   *
   * @param w The writer to use.
   *
   * @throws IOException should an I/O exception occurs.
   */
  public SafeXMLFormatter(Writer w) throws IOException {
    this.xml = new XMLWriterNSImpl(w, false);
    if (this.writeXMLDeclaration) {
      this.xml.xmlDecl();
      this.writeXMLDeclaration = false;
    }
    this.xml.setPrefixMapping(Constants.BASE_NS_URI, "dfx");
    this.xml.setPrefixMapping(Constants.DELETE_NS_URI, "del");
    this.xml.setPrefixMapping(Constants.INSERT_NS_URI, "ins");
  }

  // methods ------------------------------------------------------------------------------------

  @Override
  public void format(DiffXEvent e) throws IOException {
    if (DEBUG) {
      System.err.println("="+e);
    }
    e.toXML(this.xml);
    if (e instanceof CharactersEventBase)
      if (this.config.getWhiteSpaceProcessing() == WhiteSpaceProcessing.IGNORE) {
        this.xml.writeXML(" ");
      }
    this.xml.flush();
  }

  @Override
  public void insert(DiffXEvent e) throws IOException {
    if (DEBUG) {
      System.err.println("+"+e);
    }
    // insert an attribute to specify
    if (e instanceof OpenElementEvent) {
      e.toXML(this.xml);
      this.xml.attribute("dfx:insert", "true");

      // just output the new line
    } else if (e == SpaceEvent.NEW_LINE) {
      e.toXML(this.xml);

      // wrap the characters in a <ins> element
    } else if (e instanceof CharactersEventBase) {
      this.xml.openElement(Constants.BASE_NS_URI, "ins", false); //this.xml.openElement("ins", false);
      e.toXML(this.xml);
      this.xml.closeElement();
      if (this.config.getWhiteSpaceProcessing() == WhiteSpaceProcessing.IGNORE) {
        this.xml.writeXML(" ");
      }

      // display the attribute normally
    } else if (e instanceof AttributeEvent) {
      e.toXML(this.xml);
      this.xml.attribute("ins:"+((AttributeEvent)e).getName(), "true");

      // wrap the char in a <ins> element
    } else if (e instanceof CharEvent) {
      this.xml.openElement(Constants.BASE_NS_URI, "ins", false); //this.xml.openElement("ins", false);
      e.toXML(this.xml);
      this.xml.closeElement();

      // just format naturally
    } else {
      e.toXML(this.xml);
    }
    this.xml.flush();
  }

  @Override
  public void delete(DiffXEvent e) throws IOException {
    if (DEBUG) {
      System.err.println("-"+e);
    }
    // insert an attribute to specify
    if (e instanceof OpenElementEvent) {
      e.toXML(this.xml);
      this.xml.attribute("dfx:delete", "true");

      // just output the new line
    } else if (e == SpaceEvent.NEW_LINE) {
      e.toXML(this.xml);

      // wrap the characters in a <del> element
    } else if (e instanceof CharactersEventBase) {
      this.xml.openElement(Constants.BASE_NS_URI, "del", false); //this.xml.openElement("del", false);
      e.toXML(this.xml);
      this.xml.closeElement();
      if (this.config.getWhiteSpaceProcessing() == WhiteSpaceProcessing.IGNORE) {
        this.xml.writeXML(" ");
      }

      // put the attribute as part of the 'delete' namespace
    } else if (e instanceof AttributeEvent) {
      this.xml.attribute("del:"+((AttributeEvent)e).getName(), ((AttributeEvent)e).getValue());

      // wrap the char in a <del> element
    } else if (e instanceof CharEvent) {
      this.xml.openElement(Constants.BASE_NS_URI, "del", false); //this.xml.openElement("del", false);
      e.toXML(this.xml);
      this.xml.closeElement();

      // just format naturally
    } else {
      e.toXML(this.xml);
    }
    this.xml.flush();
  }

  @Override
  public void setConfig(DiffXConfig config) {
    this.config = config;
  }

  @Override
  public void setWriteXMLDeclaration(boolean show) {
    this.writeXMLDeclaration = show;
  }

  /**
   * Adds the prefix mapping to this class.
   *
   * @param mapping The prefix mapping to add.
   */
  @Override
  public void declarePrefixMapping(PrefixMapping mapping) {
    for (Enumeration<String> uris = mapping.getURIs(); uris.hasMoreElements();) {
      String uri = uris.nextElement();
      this.xml.setPrefixMapping(uri, mapping.getPrefix(uri));
    }
  }

}
