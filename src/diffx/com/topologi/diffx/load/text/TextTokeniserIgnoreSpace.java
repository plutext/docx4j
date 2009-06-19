package com.topologi.diffx.load.text;

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

import java.util.NoSuchElementException;


import com.topologi.diffx.event.TextEvent;
import com.topologi.diffx.event.impl.IgnorableSpaceEvent;
import com.topologi.diffx.event.impl.WordEvent;
import com.topologi.diffx.event.lang.Repertory;

/**
 * A tokeniser for text data.
 * 
 * <p>This tokeniser should be used when the white spaces are ignored but need to be preserved.
 * 
 * @author Christophe Lauret
 * @version 23 December 2004
 */
public final class TextTokeniserIgnoreSpace implements TextTokeniser {

// constants ----------------------------------------------------------------------------------

  /**
   * Constant for text bytes.
   */
  private static final byte TEXT = 0;

  /**
   * Constant for the white spaces except new lines.
   */
  private static final byte SPACE = 1;

// class attributes ---------------------------------------------------------------------------

  /**
   * The underlying character sequence.
   */
  private final CharSequence seq;

// state fields -------------------------------------------------------------------------------

  /**
   * The number of tokens.
   */
  private transient int max = -1;

  /**
   * The marker, corresponds to the first character of the next token to be returned.
   */
  private transient int marker = 0;

  /**
   * A repertory of words to use, to reuse words that have already been created.
   */
  private transient Repertory repertory = null;

// methods and constructors -------------------------------------------------------------------

  /**
   * Creates a new tokeniser.
   * 
   * @param cs The character sequence to tokenise.
   * 
   * @throws NullPointerException If the specified character sequence is <code>null</code>.
   */
  public TextTokeniserIgnoreSpace(CharSequence cs) throws NullPointerException {
    if (cs == null) throw new NullPointerException("The string buffer cannot be null.");
    this.seq = cs;
  }

  /**
   * Calculates the number of times that this tokenizer's <code>nextToken</code> method can be 
   * called before it generates an exception.
   * 
   * @return The number of tokens.
   */
  public int countTokens() {
    // no tokens for empty char sequences
    if (this.seq.length() == 0) return 0;
    // already calculated
    if (this.max >= 0) return this.max;
    // count the number of different types
    byte type = -1;
    int counter = 0;
    for (int i = 0; i < this.seq.length(); i++)
      if (type != getType(seq.charAt(i))) {
        type = getType(seq.charAt(i));
        counter++;
      }
    this.max = counter;
    return counter;
  }

  /**
   * Returns the following token.
   * 
   * @return The character event.
   * 
   * @throws NoSuchElementException If the last token has already been returned.
   */
  public TextEvent nextToken() throws NoSuchElementException {
    // there are no more tokens
    if (this.marker == this.seq.length())
      throw new NoSuchElementException("All tokens have been returned.");
    // get the next token
    byte type = getType(this.seq.charAt(this.marker));
    // otherwise, ew go through each char individually
    for (int i = this.marker; i < this.seq.length(); i++) {
      if (type != getType(this.seq.charAt(i))) {
        TextEvent e = newToken(type, this.marker, i);
        this.marker = i;
        return e;
      }
    }
    // the last token
    TextEvent e = newToken(type, this.marker, this.seq.length());
    this.marker = this.seq.length();
    return e;
  }

  /**
   * Specifies a repertory to use for this tokeniser.
   * 
   * @param rep The repertory to use.
   */
  public void useRepertory(Repertory rep) {
    this.repertory = rep;
  }

  /**
   * Returns the type corresponding to the specified character.
   * 
   * @param c The character to test.
   * 
   * @return The corresponding type.
   */
  private static byte getType(char c) {
    return (Character.isWhitespace(c))? SPACE : TEXT;
  }

  /**
   * Returns the type corresponding to the specified character.
   * 
   * @param type  The type of characters event.
   * @param start The start of the subsequence to take.
   * @param end   The end of the subsequence to take.
   * 
   * @return The corresponding event.
   */
  private TextEvent newToken(byte type, int start, int end) {
    switch (type) {
      case TEXT:
        String word = this.seq.subSequence(start, end).toString();
        if (this.repertory == null)
          return new WordEvent(word);
        else
          return this.repertory.update(word);
      case SPACE:
        return new IgnorableSpaceEvent(this.seq.subSequence(start, end));
      default:
        throw new NoSuchElementException("Cannot create token of unknown type.");
    }
  }

}
