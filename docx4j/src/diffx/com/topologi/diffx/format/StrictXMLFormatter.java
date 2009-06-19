package com.topologi.diffx.format;

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
import java.io.Writer;


import com.topologi.diffx.Constants;
import com.topologi.diffx.config.DiffXConfig;
import com.topologi.diffx.event.AttributeEvent;
import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.event.CloseElementEvent;
import com.topologi.diffx.event.OpenElementEvent;
import com.topologi.diffx.event.impl.CharEvent;
import com.topologi.diffx.event.impl.SpaceEvent;
import com.topologi.diffx.event.impl.WordEvent;

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

  /**
   * Set to <code>true</code> to include the XML declaration. This attribute is
   * set to <code>false</code> when the {@link #setWriteXMLDeclaration(boolean)}
   * is called with <code>false</code> or once the XML declaration has been written. 
   */
  private transient boolean writeXMLDeclaration = true;

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
    xml.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
  }

  /**
   * @see DiffXFormatter#format(DiffXEvent) 
   */
  public void format(DiffXEvent e) throws IOException {
    // an element to open
    if (e instanceof OpenElementEvent) {
      if (this.isElementNude) denudeElement();
      // close any ins / del tag 
      if (isInserting) closeIns();
      if (isDeleting) closeDel();
      OpenElementEvent oee = (OpenElementEvent)e;
      this.xml.print('<'+oee.getName());
      if (this.declareNamespace) {
        this.xml.print(" xmlns:dfx=\""+Constants.BASE_NS+"\"");
        this.declareNamespace = false;
      }
      this.isElementNude = true;

    // an element to close
    } else if (e instanceof CloseElementEvent) {
      if (this.isElementNude) denudeElement();
      // close any ins / del tag 
      if (isInserting) closeIns();
      if (isDeleting) closeDel();
      this.xml.print(e.toXML());

    // an attribute
    } else if (e instanceof AttributeEvent) {
      if (this.isElementNude) {
        this.xml.print(e.toXML());
      } else {
        throw new IllegalStateException("Cannot write an attribute once the element is closed");
      }

    // this is text
    } else {

      // close any ins / del tag
      if (this.isElementNude) denudeElement();
      if (isInserting) closeIns();
      if (isDeleting) closeDel();

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

  /**
   * @see DiffXFormatter#insert(DiffXEvent)
   */
  public void insert(DiffXEvent e) throws IOException {
    // insert element
    if (e instanceof OpenElementEvent) {
      if (this.isElementNude) denudeElement();
      if (isDeleting) closeDel();
      OpenElementEvent oee = (OpenElementEvent)e;
      this.xml.print('<'+oee.getName());
      if (this.declareNamespace)
        this.xml.print(" xmlns:dfx=\"http://www.allette.com.au/diffex\"");
      this.xml.print(" dfx:insert=\"true\"");
      this.isElementNude = true;

    // an element to close
    } else if (e instanceof CloseElementEvent) {
      if (this.isElementNude) denudeElement();
      if (isDeleting) closeDel();
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
    // now we handle text tokens
    } else {
      
      // close any del tag
      if (this.isElementNude) denudeElement();
      if (isDeleting) closeDel();
      // a word
      if (e instanceof WordEvent) {
        if (!this.isInserting) openIns();
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
   * @see DiffXFormatter#delete(DiffXEvent)
   */
  public void delete(DiffXEvent e) throws IOException, IllegalStateException {
    // we ignore delete attributes
    if (this.isElementNude) denudeElement();
    if (isInserting) closeIns();

    // delete an element
    if (e instanceof OpenElementEvent) {
      OpenElementEvent oee = (OpenElementEvent)e;
      this.xml.print('<'+oee.getName());
      if (this.declareNamespace)
        this.xml.print(" xmlns:dfx=\"http://www.allette.com.au/diffex\"");
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
        if (!this.isDeleting) openDel();
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

  /**
   * @see com.topologi.diffx.format.DiffXFormatter#setConfig(com.topologi.diffx.DiffXConfig)
   */
  public void setConfig(DiffXConfig config) {
    this.config = config;
  }

  /**
   * @see XMLDiffXFormatter#setWriteXMLDeclaration(boolean)
   */
  public void setWriteXMLDeclaration(boolean show) {
    this.writeXMLDeclaration = show;
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
