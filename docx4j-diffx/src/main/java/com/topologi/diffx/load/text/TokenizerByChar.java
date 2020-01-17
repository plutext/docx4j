/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.load.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.topologi.diffx.config.TextGranularity;
import com.topologi.diffx.event.TextEvent;
import com.topologi.diffx.event.impl.CharactersEvent;
import com.topologi.diffx.event.impl.SpaceEvent;

/**
 * The tokeniser for characters events.
 * 
 * <p>This class is not synchronized.
 * 
 * @author Christophe Lauret
 * @version 10 May 2010
 */
public final class TokenizerByChar implements TextTokenizer {

  /**
   * Map characters to events in order to recycle events as they are created.
   */
  private final Map<Character, TextEvent> recycling = new HashMap<Character, TextEvent>();

  /**
   * Creates a new tokenizer.
   */
  public TokenizerByChar() {
  }

  /**
   * {@inheritDoc}
   */
  public List<TextEvent> tokenize(CharSequence seq) {
    if (seq == null) return null;
    if (seq.length() == 0) return Collections.emptyList();
    List<TextEvent> events = new ArrayList<TextEvent>(seq.length());
    Character c = null;
    for (int i=0; i < seq.length(); i++) {
      c = Character.valueOf(seq.charAt(i));
      TextEvent e = this.recycling.get(c);
      if (e == null) {
        if (Character.isWhitespace(c.charValue())) {
          e = SpaceEvent.getInstance(c);
        } else {
          e = new CharactersEvent(c+"");
        }
      }
      events.add(e);
    }
    return events;
  }

  /**
   * Always <code>TextGranularity.CHARACTER</code>.
   * 
   * {@inheritDoc}
   */
  public TextGranularity granurality() {
    return TextGranularity.CHARACTER;
  }
}
