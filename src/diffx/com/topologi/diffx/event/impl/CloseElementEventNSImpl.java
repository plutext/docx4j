package com.topologi.diffx.event.impl;

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


import com.topologi.diffx.Constants;
import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.event.CloseElementEvent;
import com.topologi.diffx.event.OpenElementEvent;
import com.topologi.diffx.xml.XMLWriter;


/**
 * The event corresponding to the <code>startElement</code> SAX event.
 * 
 * @author Christophe Lauret (Allette Systems)
 * @version 3 April 2005
 */
public final class CloseElementEventNSImpl extends DiffXEventBase implements CloseElementEvent {

  /**
   * The corresponding open element event.
   */
  private final OpenElementEvent open;

  /**
   * Creates a new close element event with the default URI.
   * 
   * @see Constants#DEFAULT_URI
   * 
   * @param name The local name of the element
   * 
   * @throws NullPointerException If the name is <code>null</code>.
   */
  public CloseElementEventNSImpl(String name) throws NullPointerException {
    if (name == null)
      throw new NullPointerException("Element must have a name.");
    this.open = new OpenElementEventNSImpl(name);
  }

  /**
   * Creates a new close element event
   * 
   * @param uri  The namespace URI of the element    
   * @param name The local name of the element
   * 
   * @throws NullPointerException if any of the argument is <code>null</code>.
   */
  public CloseElementEventNSImpl(String uri, String name) throws NullPointerException {
    if (uri == null)
      throw new NullPointerException("The URI cannot be null, use \"\".");
    if (name == null)
      throw new NullPointerException("Element must have a name.");
    this.open = new OpenElementEventNSImpl(uri, name);
  }

  /**
   * Creates a new close element event from the corresponding open element event.
   * 
   * @param event The corresponding open element event.
   * 
   * @throws NullPointerException If the name is <code>null</code>.
   */
  public CloseElementEventNSImpl(OpenElementEvent event) throws NullPointerException {
    if (event == null)
      throw new NullPointerException("Element must have a name.");
    this.open = event;
  }

  /**
   * @return Returns the name.
    */
  public String getName() {
    return this.open.getName();
  }

  /**
   * @return Returns the uri.
   */
  public String getURI() {
    return this.open.getURI();
  }

  /**
   * @see com.topologi.diffx.event.CloseElementEvent#getOpenElement()
   */
  public OpenElementEvent getOpenElement() {
    return this.open;
  }

  /**
   * @see com.topologi.diffx.event.CloseElementEvent#match(com.topologi.diffx.event.OpenElementEvent)
   */
  public boolean match(OpenElementEvent event) {
    if (event == null) return false;
    if (event == this.open) return true;
    return (event.getURI().equals(this.getURI())
        &&  event.getName().equals(this.getName()));
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return 1 + this.open.hashCode();
  }

  /**
   * Returns <code>true</code> if the event is a  
   * 
   * @param e The event to compare with this event.
   * 
   * @return <code>true</code> if this event is equal to the specified event;
   *         <code>false</code> otherwise.
   */
  public boolean equals(DiffXEvent e) {
    if (e.getClass() != this.getClass()) return false;
    CloseElementEventNSImpl ce = (CloseElementEventNSImpl)e;
    return (ce.getURI().equals(this.getURI())
        &&  ce.getName().equals(this.getName()));
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "closeElement: "+this.getName()+" ["+this.getURI()+"]";
  }

  /**
   * @see com.topologi.diffx.xml.XMLWritable#toXML(com.topologi.diffx.xml.XMLWriter)
   */
  public void toXML(XMLWriter xml) throws IOException {
    xml.closeElement();
  }

  /**
   * @see com.topologi.diffx.xml.XMLWritable#toXML(com.topologi.diffx.xml.XMLWriter)
   */
  public StringBuffer toXML(StringBuffer xml) throws NullPointerException {
    // TODO: handle namespaces.
    xml.append("</").append(this.getName()).append('>');
    return xml;
  }
}
