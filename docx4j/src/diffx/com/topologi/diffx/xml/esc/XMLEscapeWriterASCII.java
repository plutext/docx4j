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

import java.io.IOException;
import java.io.Writer;

/**
 * A utility class for escaping XML data using the UTF-8 encoding.
 *
 * @author Christophe Lauret
 * 
 * @version 7 March 2005
 */
public final class XMLEscapeWriterASCII extends XMLEscapeWriterBase implements XMLEscapeWriter {

  /**
   * The encoding used for this instance.
   */
  private static final String ENCODING = "ASCII";

  /**
   * Creates a new XML escape writer using the utf-8 encoding.
   * 
   * @param w The writer to wrap.
   * 
   * @throws NullPointerException if the writer is <code>null</code>.
   */
  public XMLEscapeWriterASCII(Writer w) throws NullPointerException {
    super(w, ENCODING);
  }

  /**
   * Replaces '&lt;', '&amp;', '"' and '\'' as well an any character that is not part of
   * the standard unicode range.
   *
   * <pre>
   * Char ::= #x9 | #xA | #xD |
   *          [#x20-#xD7FF] |
   *          [#xE000-#xFFFD] |
   *          [#x10000-#x10FFFF]
   * </pre>
   *
   * @see XMLEscapeWriter#writeAttValue(char[], int, int)
   */
  public void writeAttValue(char[] ch, int off, int len) throws IOException {
    // process the rest
    char c = ' ';
    for (int i = off; i < (off+len); i++) {
      c = ch[i];
      if      (c == '<') w.write("&lt;"); 
      else if (c == '>') w.write("&gt;");
      else if (c == '&') w.write("&amp;");
      else if (c == '"') w.write("&quot;");
      else if (c == '\'') w.write("&apos;");
      // use character references for anything above 0xff
      else if (c  > 255) w.write("&#"+(int)c+";"); 
      // trash control 0 chars: [0x00 - 0x20] except tab, carriage return and line feed
      else if (c == '\n' || c == '\r' || c == '\t') w.write(c);
      else if (c < 32) doNothing();
      // trash control 1 chars: [0x80 - 0x9f], del: 0x7f
      else if (c >= 127 && c < 160) doNothing(); 
      else { w.write(c); }
    }
  }

  /**
   * Replace characters which are invalid in element values,
   * by the corresponding entity in a given <code>String</code>.
   *
   * <p>these characters are:<br>
   * <ul>
   *  <li>'&amp' by the ampersand entity "&amp;amp"</li>
   *  <li>'&lt;' by the entity "&amp;lt;"</li>
   * </p>
   *
   * <p>Note: this function assumes that there are no entities in
   * the given String. If there are existing entities, then the
   * ampersand character will be escaped by the ampersand entity.
   *
   * @see XMLEscapeWriter#writeText(char)
   */
  public void writeText(char c) throws IOException {
    // process the rest
    if      (c == '<') w.write("&lt;"); 
    else if (c == '>') w.write("&gt;");
    else if (c == '&') w.write("&amp;");
    else if (c == '"') w.write("&quot;");
    else if (c == '\'') w.write("&apos;");
    // use character references for anything above 0xff
    else if (c  > 255) w.write("&#"+(int)c+";"); 
    // trash control 0 chars: [0x00 - 0x20] except tab, carriage return and line feed
    else if (c == '\n' || c == '\r' || c == '\t') w.write(c);
    else if (c < 32) doNothing();
    // trash control 1 chars: [0x80 - 0x9f], del: 0x7f
    else if (c >= 127 && c < 160) doNothing(); 
    else w.write(c);
  }

  /**
   * Does nothing.
   * 
   * <p>This method exists so that we can explicitly say that we should do nothing
   * in certain conditions. 
   */
  private static void doNothing() {
    return;
  }

}