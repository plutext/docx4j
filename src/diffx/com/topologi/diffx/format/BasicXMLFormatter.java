/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.format;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Stack;

import com.topologi.diffx.config.DiffXConfig;
import com.topologi.diffx.event.AttributeEvent;
import com.topologi.diffx.event.CloseElementEvent;
import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.event.OpenElementEvent;
import com.topologi.diffx.event.TextEvent;
import com.topologi.diffx.event.impl.ProcessingInstructionEvent;
import com.topologi.diffx.sequence.PrefixMapping;
import com.topologi.diffx.util.Constants;
import com.topologi.diffx.xml.XMLWriter;
import com.topologi.diffx.xml.XMLWriterNSImpl;

/**
 * A XML formatter that simply uses a different namespace for any inserted or modified
 * node.
 *
 * <p>Nodes that have not changed are kept the way they are.
 *
 * <p>Nodes that have been modified will always be in a different namespace and will be
 * reported as follows:
 * <p><b>Elements:</b>
 * <pre>
 *   &lt;mod:element name="<i>elt.getName()</i>" uri="<i>elt.getURI()</i>"&gt;
 *     <i>...</i>
 *   &lt;/mod:element&gt;
 * </pre>
 *
 * <p><b>Attributes:</b>
 * <pre>
 *   &lt;mod:attribute name="<i>att.getName()</i>" uri="<i>att.getURI()</i>" value="<i>att.getValue()</i>"/&gt;
 * </pre>
 *
 * <p><b>Texts:</b>
 * <pre>
 *   &lt;mod:text&gt;<i>text.getCharacters()</i>&lt;/mod:text&gt;
 * </pre>
 *
 * @author Christophe Lauret
 * @version 17 May 2005
 */
public final class BasicXMLFormatter implements XMLDiffXFormatter {

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
   * Set to <code>true</code> to include the XML declaration. This attribute is
   * set to <code>false</code> when the {@link #setWriteXMLDeclaration(boolean)}
   * is called with <code>false</code> or once the XML declaration has been written.
   */
  private transient boolean writeXMLDeclaration = true;

  /**
   * Indicates whether the XML writer has been setup already.
   */
  private transient boolean isSetup = false;

  /**
   * Indicates whether some text is being inserted or removed.
   *
   * 0 = indicate format or no open text element.
   * +1 = indicates an insert open text element.
   * -1 = indicates an delete open text element.
   */
  private transient short textFormat = 0;

  /**
   * A stack of attributes to insert.
   */
  private transient Stack<AttributeEvent> insAttributes = new Stack<AttributeEvent>();

  /**
   * A stack of attributes to delete.
   */
  private transient Stack<AttributeEvent> delAttributes = new Stack<AttributeEvent>();

  // constructors -------------------------------------------------------------------------------

  /**
   * Creates a new formatter using the specified writer.
   *
   * @param w The writer to use.
   *
   * @throws NullPointerException If the specified writer is <code>null</code>.
   */
  public BasicXMLFormatter(Writer w) throws NullPointerException {
    if (w == null)
      throw new NullPointerException("The XML formatter requires a writer");
    this.xml = new XMLWriterNSImpl(w, false);
  }

  // methods ------------------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override
  public void format(DiffXEvent e) throws IOException {
    if (!this.isSetup) {
      setUpXML();
    }
    endTextChange();
    if (!(e instanceof AttributeEvent)) {
      flushAttributes();
    }
    e.toXML(this.xml);
    if (e instanceof TextEvent)
      if (this.config.isIgnoreWhiteSpace() && !this.config.isPreserveWhiteSpace()) {
        this.xml.writeXML(" ");
      }
    this.xml.flush();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void insert(DiffXEvent e) throws IOException {
    change(e, +1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(DiffXEvent e) throws IOException {
    change(e, -1);
  }

  /**
   * Reports a change in XML.
   *
   * @param e   The diff-x event that has been inserted or deleted.
   * @param mod The modification flag (positive for inserts, negative for deletes).
   *
   * @throws IOException an I/O exception if an error occurs.
   */
  private void change(DiffXEvent e, int mod) throws IOException {
    if (!this.isSetup) {
      setUpXML();
    }
    // change in element
    if (e instanceof OpenElementEvent) {
      flushAttributes();
      endTextChange();
      this.xml.openElement(mod > 0? Constants.INSERT_NS_URI : Constants.DELETE_NS_URI, "element", false);
      this.xml.attribute("name", ((OpenElementEvent)e).getName());
      this.xml.attribute("ns-uri", ((OpenElementEvent)e).getURI());

      // change in element
    } else if (e instanceof CloseElementEvent) {
      flushAttributes();
      endTextChange();
      this.xml.closeElement();

      // change in text
    } else if (e instanceof TextEvent) {
      flushAttributes();
      switchTextChange(mod);
      e.toXML(this.xml);
      if (this.config.isIgnoreWhiteSpace() && !this.config.isPreserveWhiteSpace()) {
        this.xml.writeXML(" ");
      }

      // put the attribute as part of the 'delete' namespace
    } else if (e instanceof AttributeEvent) {
      if (mod > 0) {
        this.insAttributes.push((AttributeEvent)e);
      } else {
        this.delAttributes.push((AttributeEvent)e);
      }

      // put the attribute as part of the 'delete' namespace
    } else if (e instanceof ProcessingInstructionEvent) {
      flushAttributes();
      endTextChange();
      this.xml.openElement(mod > 0? Constants.INSERT_NS_URI : Constants.DELETE_NS_URI, "processing-instruction", false);
      this.xml.attribute("data", ((ProcessingInstructionEvent)e).getData());
      this.xml.attribute("target", ((ProcessingInstructionEvent)e).getTarget());

      // just format naturally
    } else {
      flushAttributes();
      endTextChange();
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

  // private helpers ----------------------------------------------------------------------------

  /**
   * Set up the XML.
   *
   * @throws IOException an I/O exception if an error occurs.
   */
  private void setUpXML() throws IOException {
    if (this.writeXMLDeclaration) {
      this.xml.xmlDecl();
    }
    this.xml.setPrefixMapping(Constants.DELETE_NS_URI, "del");
    this.xml.setPrefixMapping(Constants.INSERT_NS_URI, "ins");
    this.writeXMLDeclaration = false;
    this.isSetup = true;
  }

  /**
   * Formats the end of a text change.
   *
   * @throws IOException If throws by XMl writer.
   */
  private void endTextChange() throws IOException {
    if (this.textFormat != 0) {
      this.xml.closeElement();
      this.textFormat = 0;
    }
  }

  /**
   * Switch between text changes.
   *
   * @param mod The modification flag (positive for inserts, negative for deletes).
   *
   * @throws IOException If throws by XMl writer.
   */
  private void switchTextChange(int mod) throws IOException {
    // insert
    if (mod > 0) {
      if (this.textFormat < 0) {
        this.xml.closeElement();
      }
      if (this.textFormat <= 0) {
        this.xml.openElement(Constants.INSERT_NS_URI, "text", false);
        this.textFormat = +1;
      }
      // delete
    } else {
      if (this.textFormat > 0) {
        this.xml.closeElement();
      }
      if (this.textFormat >= 0) {
        this.xml.openElement(Constants.DELETE_NS_URI, "text", false);
        this.textFormat = -1;
      }
    }
  }

  /**
   * Writes any attribute that has not be written.
   *
   * @throws IOException Should an I/O error occur.
   */
  private void flushAttributes() throws IOException {
    flushAttributes(this.insAttributes, Constants.INSERT_NS_URI);
    flushAttributes(this.delAttributes, Constants.DELETE_NS_URI);
  }

  /**
   * Writes any attribute that has not be written.
   *
   * @param atts The attribute stack.
   * @param uri  The Namespace URI required
   *
   * @throws IOException Should an I/O error occur.
   */
  private void flushAttributes(Stack<AttributeEvent> atts, String uri) throws IOException {
    while (!atts.empty()) {
      AttributeEvent att = atts.pop();
      this.xml.openElement(uri, "attribute", false);
      this.xml.attribute("name", att.getName());
      if (att.getURI() != null) {
        this.xml.attribute("ns-uri", att.getURI());
      }
      this.xml.attribute("value", att.getValue());
      this.xml.closeElement();
    }
  }

}
