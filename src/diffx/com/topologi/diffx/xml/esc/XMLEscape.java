package com.topologi.diffx.xml.esc;

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

/**
 * An interface to escape XML character data.
 * 
 * <p>This interface assumes that the values to be escapes do not orignate from
 * XML text, in order words, there should not be already any entity or markup
 * in the document. If it is the case the methods in this class should also
 * escapes them. Thus "&amp;amp;" would be represented as "&amp;amp;amp;".
 * 
 * <p>Also the method will not try to escape characters that cannot be escaped.
 * 
 * <p>This interface is based on the paragraph 2.4 of the XML 1.0 Specifications.
 *
 * @author  Christophe Lauret
 * @version 7 March 2005
 */
public interface XMLEscape {

  /**
   * Returns a well-formed attribute value.
   * 
   * <p>This method must replace any character in the specified
   * value by the corresponding numeric character reference or the
   * predefined XML general entities, if the character is not allowed
   * or not in the encoding range.
   * 
   * <p>Attribute values must not contain '&amp' or '&lt;. Quotes
   * and apostrophes must also be escaped by "&amp;quot;" and
   * "&amp;apos;" respectively.
   *
   * @param ch  The value that needs to be attribute-escaped.
   * @param off The start (offset) of the characters.
   * @param len The length of characters to.
   * 
   * @return A well-formed value for the attribute.
   */
  String toAttributeValue(char[] ch, int off, int len);

  /**
   * Returns a well-formed attribute value.
   * 
   * <p>Method provided for convenience, using the same specifications as
   * {@link #toAttributeValue(char[], int, int)}.
   *
   * <p>This method should return <code>null</code> if the given
   * value is <code>null</code>.
   *  
   * @param value The value that needs to be attribute-escaped.
   * 
   * @return A well-formed value for the attribute.
   */
  String toAttributeValue(String value);

  /**
   * Returns a well-formed text value for the element.
   *
   * <p>This method must replace any character in the specified
   * value by the corresponding numeric character reference or the
   * predefined XML general entities, if the character is not allowed
   * or not in the encoding range.
   * 
   * <p>The text of an element must not contain '&amp' or '&lt;.
   * 
   * @param ch  The value that needs to be attribute-escaped.
   * @param off The start (offset) of the characters.
   * @param len The length of characters to.
   *  
   * @return A well-formed value for the text node.
   */
  String toElementText(char[] ch, int off, int len);

  /**
   * Returns a well-formed text value for the element.
   *
   * <p>Method provided for convenience, using the same specifications as
   * {@link #toElementText(char[], int, int)}.
   * 
   * <p>This method should return <code>null</code> if the given
   * value is <code>null</code>.
   * 
   * @param value The value that needs to be text-escaped.
   *  
   * @return A well-formed value for the text node.
   */
  String toElementText(String value);

  /**
   * Returns the encoding used by the implementing class.
   * 
   * @return The encoding used by the implementing class.
   */
  String getEncoding();

  
}