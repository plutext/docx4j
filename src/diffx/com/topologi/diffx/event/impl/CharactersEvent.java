/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.event.impl;

/**
 * An event corresponds to the "characters" SAX event.
 * 
 * <p>
 * This event can be used to represent the text content of entire element. Typically, this would
 * happen when there is no need to examine the text content of the node.
 * 
 * @author Christophe Lauret
 * @version 28 March 2010
 */
public final class CharactersEvent extends CharactersEventBase {

  /**
   * Creates a new characters event.
   * 
   * @param seq The char sequence.
   * 
   * @throws NullPointerException If the given String is <code>null</code>.
   */
  public CharactersEvent(CharSequence seq) throws NullPointerException {
    super(seq);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "characters: \""+getCharacters()+'"';
  }

}
