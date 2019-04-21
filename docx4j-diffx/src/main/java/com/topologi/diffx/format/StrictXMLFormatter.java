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
import com.topologi.diffx.event.AttributeEvent;
import com.topologi.diffx.event.CloseElementEvent;
import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.event.OpenElementEvent;
import com.topologi.diffx.event.impl.CharEvent;
import com.topologi.diffx.event.impl.SpaceEvent;
import com.topologi.diffx.event.impl.WordEvent;
import com.topologi.diffx.sequence.PrefixMapping;
import com.topologi.diffx.util.Constants;

/**
 * A simple XML formatter that writes strictly what it is given.
 *
 * <p>This formatter will write the events exactly in the order in which they are given,
 * in other words, there is no way to prevent this class from writing malformed XML.
 * On other hand, the {@link com.topologi.diffx.format.SmartXMLFormatter} will close
 * XML elements automatically, therefore rectifying a lot of the errors that lead to
 * malformed XML.
 *
 * @author Christophe Lauret
 * @version 3 April 2005
 */
public final class StrictXMLFormatter implements XMLDiffXFormatter {

  // class attributes ---------------------------------------------------------------------------

  /**
   * Thw output goes here.
   */
  private final PrintWriter xml;

  /**
   * The open 'del' tag, that is the element start tag that we put before test is being deleted.
   */
  private String openDel = "<del>";

  /**
   * The close 'del' tag, that is the element end tag that we put after test is being deleted.
   */
  private String closeDel = "</del>";

  /**
   * The open 'ins' tag, that is the element start tag that we put before test is being inserted.
   */
  private String openIns = "<ins>";

  /**
   * The close 'ins' tag, that is the element end tag that we put after test is being inserted.
   */
  private String closeIns = "</ins>";

  /**
   * The DiffX configuration to use
   */
  private DiffXConfig config = new DiffXConfig();

  // state variables ----------------------------------------------------------------------------

  /**
   * Set to <code>false</code> once the prefix mapping has been declared.
   *
   * <pre>
   *   xmlns:dfx="http://www.allette.com.au/diffex"
   * </pre>
   */
  private transient boolean declareNamespace = true;

  /**
   * Set to <code>true</code> to indicate that there is an open 'ins' tag.
   */
  private transient boolean isInserting = false;

  /**
   * Set to <code>true</code> to indicate that there is an open 'del' tag.
   */
  private transient boolean isDeleting = false;

  /**
   * Set to <code>true</code> to indicate that there is an open element tag
   * that does not have its right angle bracket.
   */
  private transient boolean isElementNude = false;



  // constructors -------------------------------------------------------------------------------

  /**
   * Creates a new formatter on the standard output.
   */
  public StrictXMLFormatter() {
    this.xml = new PrintWriter(System.out);
    init();
  }

  /**
   * Creates a new formatter using the specified writer.
   *
   * @param w The writer to use.
   */
  public StrictXMLFormatter(Writer w) {
    this.xml = new PrintWriter(w);
    init();
  }

  // methods ------------------------------------------------------------------------------------

  /**
   * Writes the XML declaration.
   */
  private void init() {
    this.xml.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
  }

  @Override
  public void format(DiffXEvent e) throws IOException {
    // an element to open
    if (e instanceof OpenElementEvent) {
      if (this.isElementNude) {
        denudeElement();
      }
      // close any ins / del tag
      if (this.isInserting) {
        closeIns();
      }
      if (this.isDeleting) {
        closeDel();
      }
      OpenElementEvent oee = (OpenElementEvent)e;
      this.xml.print('<'+oee.getName());
      if (this.declareNamespace) {
        this.xml.print(" xmlns:dfx=\""+Constants.BASE_NS_URI+"\"");
        this.declareNamespace = false;
      }
      this.isElementNude = true;

      // an element to close
    } else if (e instanceof CloseElementEvent) {
      if (this.isElementNude) {
        denudeElement();
      }
      // close any ins / del tag
      if (this.isInserting) {
        closeIns();
      }
      if (this.isDeleting) {
        closeDel();
      }
      this.xml.print(e.toXML());

      // an attribute
    } else if (e instanceof AttributeEvent) {
      if (this.isElementNude) {
        this.xml.print(e.toXML());
      } else
        throw new IllegalStateException("Cannot write an attribute once the element is closed");

      // this is text
    } else {

      // close any ins / del tag
      if (this.isElementNude) {
        denudeElement();
      }
      if (this.isInserting) {
        closeIns();
      }
      if (this.isDeleting) {
        closeDel();
      }

      // a character sequence
      if (e instanceof WordEvent || e instanceof SpaceEvent) {
        this.xml.print(e.toXML());

        // a single character
      } else if (e instanceof CharEvent) {
        this.xml.print(((CharEvent)e).c);
      }

    }
    this.xml.flush();
  }

  @Override
  public void insert(DiffXEvent e) throws IOException {
    // insert element
    if (e instanceof OpenElementEvent) {
      if (this.isElementNude) {
        denudeElement();
      }
      if (this.isDeleting) {
        closeDel();
      }
      OpenElementEvent oee = (OpenElementEvent)e;
      this.xml.print('<'+oee.getName());
      if (this.declareNamespace) {
        this.xml.print(" xmlns:dfx=\"http://www.allette.com.au/diffex\"");
      }
      this.xml.print(" dfx:insert=\"true\"");
      this.isElementNude = true;

      // an element to close
    } else if (e instanceof CloseElementEvent) {
      if (this.isElementNude) {
        denudeElement();
      }
      if (this.isDeleting) {
        closeDel();
      }
      this.xml.print("</");
      this.xml.print(((CloseElementEvent)e).getName());
      this.xml.print('>');

    } else if (e instanceof AttributeEvent) {
      if (this.isElementNude) {
        this.xml.print(" ");
        this.xml.print(((AttributeEvent)e).getName());
        this.xml.print("=\"");
        this.xml.print(((AttributeEvent)e).getValue());
        this.xml.print('"');
      } else {
        throw new IllegalStateException("Cannot insert an attribute once the element is closed");
      }
    } else {

      // close any del tag
      if (this.isElementNude) {
        denudeElement();
      }
      if (this.isDeleting) {
        closeDel();
      }
      // a word
      if (e instanceof WordEvent) {
        if (!this.isInserting) {
          openIns();
        }
        this.xml.print(e.toXML());

        // a white space
      } else if (e instanceof SpaceEvent) {
        this.xml.print(e.toXML());

        // wrap the char in a <ins> element
      } else if (e instanceof CharEvent) {
        this.xml.print(((CharEvent)e).c);
      }

    }
    this.xml.flush();
  }

  @Override
  public void delete(DiffXEvent e) throws IOException, IllegalStateException {
    // we ignore delete attributes
    if (this.isElementNude) {
      denudeElement();
    }
    if (this.isInserting) {
      closeIns();
    }

    // delete an element
    if (e instanceof OpenElementEvent) {
      OpenElementEvent oee = (OpenElementEvent)e;
      this.xml.print('<'+oee.getName());
      if (this.declareNamespace) {
        this.xml.print(" xmlns:dfx=\"http://www.allette.com.au/diffex\"");
      }
      this.xml.print(" dfx:delete=\"true\"");
      this.xml.print('>');

      // an element to close
    } else if (e instanceof CloseElementEvent) {
      this.xml.print("</");
      this.xml.print(((CloseElementEvent)e).getName());
      this.xml.print('>');

      // text
    } else {

      // a word
      if (e instanceof WordEvent) {
        if (!this.isDeleting) {
          openDel();
        }
        this.xml.print(e.toXML());

        // a white space
      } else if (e instanceof SpaceEvent) {
        this.xml.print(e.toXML());

        // wrap the char in a <ins> element
      } else if (e instanceof CharEvent) {
        this.xml.print(((CharEvent)e).c);
      }
    }
    this.xml.flush();
  }

  /**
   * Sets the open and end tags for inserted text.
   *
   * <p>The default values are "&lt;ins:&gt;" and "&lt;/ins:&gt;" respectively.
   *
   * @param start The open tag for inserts.
   * @param end   The close tag for inserts.
   *
   * @throws NullPointerException If any of the tags is <code>null</code>.
   */
  public void setInsertTags(String start, String end) throws NullPointerException {
    if (start == null)
      throw new NullPointerException("The start element for inserted text must have a value");
    if (end == null)
      throw new NullPointerException("The start element for inserted text must have a value");
    this.openIns = start;
    this.closeIns = end;
  }

  /**
   * Sets the open and end tags for deleted text.
   *
   * <p>The default values are "&lt;del:&gt;" and "&lt;/del:&gt;" respectively.
   *
   * @param start The open tag for deletions.
   * @param end   The close tag for deletions.
   *
   * @throws NullPointerException If any of the tags is <code>null</code>.
   */
  public void setDeleteTags(String start, String end) throws NullPointerException {
    if (start == null)
      throw new NullPointerException("The start element for deleted text must have a value");
    if (end == null)
      throw new NullPointerException("The start element for deleted text must have a value");
    this.openDel = start;
    this.closeDel = end;
  }

  @Override
  public void setConfig(DiffXConfig config) {
    this.config = config;
  }

  @Override
  public void setWriteXMLDeclaration(boolean show) {
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
      mapping.getPrefix(uri);
      // TODO: does nothing !!!
    }
  }

  // private helpers ----------------------------------------------------------------------------

  /**
   * Opens the 'ins' element, and update the state flags.
   */
  private void openIns() {
    this.xml.print(this.openIns);
    this.isInserting = true;
  }

  /**
   * Opens the 'del' element, and update the state flags.
   */
  private void openDel() {
    this.xml.print(this.openDel);
    this.isDeleting = true;
  }

  /**
   * Closes the 'ins' element, and update the state flags.
   */
  private void closeIns() {
    this.xml.print(this.closeIns);
    this.isInserting = false;
  }

  /**
   * Closes the 'del' element, and update the state flags.
   */
  private void closeDel() {
    this.xml.print(this.closeDel);
    this.isDeleting = false;
  }

  /**
   * Closes the 'del' element, and update the state flags.
   */
  private void denudeElement() {
    this.xml.print(">");
    this.isElementNude = false;
  }

}
