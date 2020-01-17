/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.event.impl;

import com.topologi.diffx.event.TextEvent;

/**
 * A text event representing a word.
 *
 * @author Christophe Lauret
 * @version 27 March 2010
 */
public final class WordEvent extends CharactersEventBase implements TextEvent {

  /**
   * Creates a new word event.
   *
   * @param w The word as a string.
   *
   * @throws NullPointerException If the given String is <code>null</code>.
   */
  public WordEvent(CharSequence w) throws NullPointerException {
    super(w);
  }

  @Override
  public String toString() {
    return "word: \""+getCharacters()+'"';
  }

}
