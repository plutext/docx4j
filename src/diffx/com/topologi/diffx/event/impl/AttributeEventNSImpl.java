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

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.topologi.diffx.event.AttributeEvent;
import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.xml.XMLWriter;

/**
 * A namespace aware implementation of the attribute event.
 * 
 * @author Christophe Lauret, Jean-Baptiste Reure
 * @version 7 April 2005
 */
public final class AttributeEventNSImpl extends DiffXEventBase implements AttributeEvent {

  /**
   * The namespace URI this attribute belongs to.
   */
  private final String uri;

  /**
   * The name of the attribute.
   */
  private final String name;

  /**
   * The value of the attribute.
   */
  private final String value;

  /**
   * Creates a new attribute event.
   *
   * @param name  The local name of the attribute.
   * @param value The value of the attribute.
   * 
   * @throws NullPointerException if any of the argument is <code>null</code>.
   */
  public AttributeEventNSImpl(String name, String value) throws NullPointerException {
    if (name == null)
      throw new NullPointerException("Attribute must have a name.");
    if (value == null)
      throw new NullPointerException("The attribute value cannot be null, use \"\".");
    this.name = name;
    this.value = value;
    this.uri = null;
  }

  /**
   * Creates a new attribute event.
   *
   * @param uri  The uri of the attribute.
   * @param name  The local name of the attribute.
   * @param value The value of the attribute.
   * 
   * @throws NullPointerException if any of the argument is <code>null</code>.
   */
  public AttributeEventNSImpl(String uri, String name, String value) throws NullPointerException {
    if (name == null)
      throw new NullPointerException("Attribute must have a name.");
    if (value == null)
      throw new NullPointerException("The attribute value cannot be null, use \"\".");
    this.name = name;
    this.value = value;
    this.uri = uri;
  }

  /**
   * @see com.topologi.diffx.event.AttributeEvent#getName()
   */
  public String getName() {
    return this.name;
  }

  /**
   * @see com.topologi.diffx.event.AttributeEvent#getURI()
   */
  public String getURI() {
    return this.uri;
  }

  /**
   * @see com.topologi.diffx.event.AttributeEvent#getValue()
   */
  public String getValue() {
    return this.value;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
//  public int hashCode() {
//    return this.name.hashCode() + this.value.hashCode();
//  }

  private int fHashCode;  
  @Override public int hashCode() {
	    if ( fHashCode == 0) {
	  	  // you pick a hard-coded, randomly chosen, non-zero, odd number
		  // ideally different for each class
	      fHashCode = new HashCodeBuilder(17, 37).
							  append(this.name).
							  append(this.uri).
							  append(this.value).
							  toHashCode();
	    }
	    return fHashCode;
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
    AttributeEventNSImpl ae = (AttributeEventNSImpl)e;
    if (!ae.name.equals(this.name)) return false;
    if (!equals(ae.uri, this.uri)) return false;
    if (!ae.value.equals(this.value)) return false;
    return true;
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "attribute: "+this.name+"="+this.value+" ["+this.uri+"]";
  }

  /**
   * @see com.topologi.diffx.xml.XMLWritable#toXML(com.topologi.diffx.xml.XMLWriter)
   */
  public void toXML(XMLWriter xml) throws IOException {
    xml.attribute(this.uri, this.name, this.value);
  }

  /**
   * @see com.topologi.diffx.xml.XMLWritable#toXML(com.topologi.diffx.xml.XMLWriter)
   */
  public StringBuffer toXML(StringBuffer xml) throws NullPointerException {
    // FIXME: no support for NS????
    xml.append(' ');
    xml.append(this.name);
    xml.append("=\"");
    xml.append(ESC.toAttributeValue(this.value));
    xml.append('"');
    return xml;
  }

  /**
   * Returns <code>true</code> if both namespace URI are <code>null</code>
   * or equal.
   * 
   * @param uri1 The first namespace URI.
   * @param uri2 The second namespace URI.
   * 
   * @return <code>true</code> if both <code>null</code> or equal;
   *         <code>false</code> otherwise.
   */
  private static boolean equals(String uri1, String uri2) {
    if (uri1 == null && uri2 == null) return true;
    if (uri1 == null || uri2 == null) return false;
    return uri1.equals(uri2);
  }

}
