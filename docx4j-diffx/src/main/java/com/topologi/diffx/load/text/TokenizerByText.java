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
import java.util.List;

import com.topologi.diffx.config.TextGranularity;
import com.topologi.diffx.config.WhiteSpaceProcessing;
import com.topologi.diffx.event.TextEvent;
import com.topologi.diffx.event.impl.CharactersEvent;
import com.topologi.diffx.event.impl.IgnorableSpaceEvent;
import com.topologi.diffx.event.impl.SpaceEvent;

/**
 * The tokeniser for characters events.
 * 
 * <p>This class is not synchronized.
 * 
 * @author Christophe Lauret
 * @version 11 May 2010
 */
public final class TokenizerByText implements TextTokenizer {

  /**
   * Define the whitespace processing.
   */
  private final WhiteSpaceProcessing whitespace;

  /**
   * Creates a new tokenizer.
   * 
   * @param whitespace the whitespace processing for this tokenizer.
   * 
   * @throws NullPointerException if the white space processing is not specified.
   */
  public TokenizerByText(WhiteSpaceProcessing whitespace) {
    if (whitespace == null) throw new NullPointerException("the white space processing must be specified.");
    this.whitespace = whitespace;
  }

  /**
   * {@inheritDoc}
   */
  public List<TextEvent> tokenize(CharSequence seq) {
    if (seq == null) return null;
    if (seq.length() == 0) return Collections.emptyList();
    int x = TokenizerUtils.getLeadingWhiteSpace(seq);
    int y = TokenizerUtils.getTrailingWhiteSpace(seq);
    // no leading or trailing spaces return a singleton in all configurations
    if (x == 0 && y == 0) {
      TextEvent e = new CharactersEvent(seq);
      return Collections.singletonList(e);
    }
    // The text node is only white space (white space = trailing space)
    if (x == seq.length()) {
      switch (this.whitespace) {
        case COMPARE:
          return Collections.singletonList((TextEvent)SpaceEvent.getInstance(seq.toString()));
        case PRESERVE:
          return Collections.singletonList((TextEvent)new IgnorableSpaceEvent(seq.toString()));
        case IGNORE:
          return Collections.emptyList();
        default:
      }
      TextEvent e = new CharactersEvent(seq);
      return Collections.singletonList(e);
    }
    // some trailing or leading whitespace, behaviour changes depending on whitespace processing
    List<TextEvent> events = null;
    switch (this.whitespace) {
      case COMPARE:
        events = new ArrayList<TextEvent>(1 + (x > 0 ? 1 : 0) + (y > 0 ? 1 : 0));
        if (x > 0) {
          events.add(SpaceEvent.getInstance(seq.subSequence(0, x)));
        }
        events.add(new CharactersEvent(seq.subSequence(x, seq.length()-y)));
        if (y > 0) {
          events.add(SpaceEvent.getInstance(seq.subSequence(seq.length()-y, seq.length())));
        }
        break;
      case PRESERVE:
        events = new ArrayList<TextEvent>(1 + (x > 0 ? 1 : 0) + (y > 0 ? 1 : 0));
        if (x > 0) {
          events.add(new IgnorableSpaceEvent(seq.subSequence(0, x)));
        }
        events.add(new CharactersEvent(seq.subSequence(x, seq.length()-y)));
        if (y > 0) {
          events.add(new IgnorableSpaceEvent(seq.subSequence(seq.length()-y, seq.length())));
        }
        break;
      case IGNORE:
        TextEvent e = new CharactersEvent(seq.subSequence(x, seq.length()-y));
        events = Collections.singletonList(e);
        break;
      default:
    }
    return events;
  }

  /**
   * Always <code>TextGranularity.CHARACTER</code>.
   * 
   * {@inheritDoc}
   */
  public TextGranularity granurality() {
    return TextGranularity.TEXT;
  }

}
