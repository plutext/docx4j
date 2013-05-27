/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.event.impl;

import java.io.IOException;

import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.event.TextEvent;
import com.topologi.diffx.xml.XMLWriter;

/**
 * A base class for all the characters events "characters" SAX event.
 * 
 * @author Christophe Lauret
 * @version 28 March 2010
 */
public abstract class CharactersEventBase extends DiffXEventBase implements TextEvent {

  /**
   * The characters for this event.
   */
  private final String characters;

  /**
   * A suitable hashCode for this event.
   */
  private final int hashCode;

  /**
   * Creates a new characters event.
   * 
   * @param seq The char sequence.
   * 
   * @throws NullPointerException If the given String is <code>null</code>.
   */
  public CharactersEventBase(CharSequence seq) throws NullPointerException {
    if (seq == null)
      throw new NullPointerException("The characters cannot be null, use \"\"");
    this.characters = seq.toString();
    this.hashCode = toHashCode(seq);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int hashCode() {
    return this.hashCode;
  }

  /**
   * Returns <code>true</code> if the event is a character event and its content is equivalent.
   * 
   * @param e The event to compare with this event.
   * 
   * @return <code>true</code> if considered equal; <code>false</code> otherwise.
   */
  @Override
  public final boolean equals(DiffXEvent e) {
    if (this == e)
      return true;
    if (e.getClass() != this.getClass())
      return false;
    CharactersEventBase ce = (CharactersEventBase) e;
    return ce.characters.equals(this.characters);
  }

  /**
   * Returns the characters that this event represents.
   * 
   * <p>
   * Note: this method will return the characters as used by Java (ie. Unicode), they may not be
   * suitable for writing to an XML string.
   * 
   * @return The characters that this event represents.
   */
  public final String getCharacters() {
    return this.characters;
  }

  /**
   * {@inheritDoc}
   */
  public final void toXML(XMLWriter xml) throws IOException {
    xml.writeText(this.characters);
  }

  /**
   * {@inheritDoc}
   */
  public final StringBuffer toXML(StringBuffer xml) throws NullPointerException {
    xml.append(ESC.toElementText(this.characters));
    return xml;
  }

  /**
   * Calculates the hashcode value from a string.
   * 
   * @param s A string from which to calculate the hashcode.
   * @return a suitable hashcode value.
   */
  private static int toHashCode(CharSequence s) {
    return 13 * 47 + (s != null ? s.hashCode() : 0);
  }

}
