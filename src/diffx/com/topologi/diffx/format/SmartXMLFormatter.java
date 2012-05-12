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
import java.util.Enumeration;


import com.topologi.diffx.Constants;
import com.topologi.diffx.config.DiffXConfig;
import com.topologi.diffx.event.AttributeEvent;
import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.event.impl.CharEvent;
import com.topologi.diffx.event.impl.CharactersEventBase;
import com.topologi.diffx.event.OpenElementEvent;
import com.topologi.diffx.event.impl.SpaceEvent;
import com.topologi.diffx.sequence.PrefixMapping;
import com.topologi.diffx.xml.NSAwareXMLWriter;
import com.topologi.diffx.xml.XMLWriter;

/**
 * An XML formatter that tries to rectify the errors affecting the well-formedness of the XML.
 * 
 * <p>This class will always close the elements correctly by maintaining a stack of parent
 * elements.
 * 
 * <p>Implementation note: this classes uses the namespace prefixes 'dfx' and 'del', in the 
 * future it should be possible to configure which prefixes to use for each namespace, but 
 * in this version the namespace prefix mapping is hardcoded.
 * 
 * @author Christophe Lauret
 * @version 3 April 2005
 */
public final class SmartXMLFormatter implements XMLDiffXFormatter {

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
   * Set to <code>true</code> to include the XML declaration. This attribute is
   * set to <code>false</code> when the {@link #setWriteXMLDeclaration(boolean)}
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
   * @see #SmartXMLFormatter(Writer)
   * 
   * @throws IOException should an I/O exception occurs.
   */
  public SmartXMLFormatter() throws IOException {
    this(new PrintWriter(System.out));
  }

  /**
   * Creates a new formatter using the specified writer.
   * 
   * @param w The writer to use.
   * 
   * @throws IOException should an I/O exception occurs.
   */
  public SmartXMLFormatter(Writer w) throws IOException {
    this.xml = new NSAwareXMLWriter(w, false);  //indent
    this.xml.xmlDecl();
    this.xml.setPrefixMapping(Constants.BASE_NS, "dfx");
    this.xml.setPrefixMapping(Constants.DELETE_NS, "del");
    this.xml.setPrefixMapping("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w");
    this.xml.setPrefixMapping("http://schemas.openxmlformats.org/drawingml/2006/main", "a");
    this.xml.setPrefixMapping("http://schemas.openxmlformats.org/drawingml/2006/picture", "pic");
    this.xml.setPrefixMapping("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "r");    
    this.xml.setPrefixMapping("urn:schemas-microsoft-com:vml", "v");
    this.xml.setPrefixMapping("urn:schemas-microsoft-com:office:word", "w10"); 
    this.xml.setPrefixMapping("http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", "wp");    
    
    
  }

// methods ------------------------------------------------------------------------------------

  /**
   * @see DiffXFormatter#format(DiffXEvent) 
   */
  public void format(DiffXEvent e) throws IOException {
    if (DEBUG) System.err.println("="+e);
    e.toXML(xml);
    if (e instanceof CharactersEventBase)
      if (config.isIgnoreWhiteSpace() && !config.isPreserveWhiteSpace())
        this.xml.writeXML(" ");
    this.xml.flush();
  }

  /**
   * @see DiffXFormatter#insert(DiffXEvent)
   */
  public void insert(DiffXEvent e) throws IOException {
    if (DEBUG) System.err.println("+"+e);
    // insert an attribute to specify
    if (e instanceof OpenElementEvent) {
      e.toXML(this.xml);
      this.xml.attribute("dfx:insert", "true");

    // just output the new line
    } else if (e == SpaceEvent.NEW_LINE) {
      e.toXML(this.xml);

    // wrap the characters in a <ins> element
    } else if (e instanceof CharactersEventBase) {
      this.xml.openElement("ins", false);
      e.toXML(this.xml);
      this.xml.closeElement();
      if (config.isIgnoreWhiteSpace() && !config.isPreserveWhiteSpace())
        this.xml.writeXML(" ");

    // display the attribute normally
    } else if (e instanceof AttributeEvent) {
      e.toXML(this.xml);

    // wrap the char in a <ins> element
    } else if (e instanceof CharEvent) {
      this.xml.openElement("ins", false);
      e.toXML(this.xml);
      this.xml.closeElement();

    // just format naturally
    } else {
      e.toXML(this.xml);
    }
    this.xml.flush();
  }

  /**
   * @see DiffXFormatter#delete(DiffXEvent)
   */
  public void delete(DiffXEvent e) throws IOException {
    if (DEBUG) System.err.println("-"+e);
    // insert an attribute to specify
    if (e instanceof OpenElementEvent) {
      e.toXML(this.xml);
      this.xml.attribute("dfx:delete", "true");

      // just output the new line
    } else if (e == SpaceEvent.NEW_LINE) {
      e.toXML(this.xml);

    // wrap the characters in a <del> element
    } else if (e instanceof CharactersEventBase) {
      this.xml.openElement("del", false);
      e.toXML(this.xml);
      this.xml.closeElement();
      if (config.isIgnoreWhiteSpace() && !config.isPreserveWhiteSpace())
        this.xml.writeXML(" ");

    // put the attribute as part of the 'delete' namespace
    } else if (e instanceof AttributeEvent) {
      this.xml.attribute("del:"+((AttributeEvent)e).getName(), ((AttributeEvent)e).getValue());

    // wrap the char in a <del> element
    } else if (e instanceof CharEvent) {
      this.xml.openElement("del", false);
      e.toXML(this.xml);
      this.xml.closeElement();

    // just format naturally
    } else {
      e.toXML(this.xml);
    }
    this.xml.flush();
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

  /**
   * Adds the prefix mapping to this class.
   * 
   * @param mapping The prefix mapping to add.
   */
  public void declarePrefixMapping(PrefixMapping mapping) {
    for (Enumeration uris = mapping.getURIs(); uris.hasMoreElements();) {
      String uri = (String)uris.nextElement();
      this.xml.setPrefixMapping(uri, mapping.getPrefix(uri));
    }
  }

// private helpers ----------------------------------------------------------------------------

}
