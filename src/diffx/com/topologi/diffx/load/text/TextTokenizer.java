/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.load.text;

import java.util.List;

import com.topologi.diffx.config.TextGranularity;
import com.topologi.diffx.event.TextEvent;

/**
 * An interface for text tokenizers.
 * 
 * <p>Text tokenisers are used to return a list of {@link com.topologi.diffx.event.TextEvent}
 * from a piece of text.
 * 
 * @author Christophe Lauret
 * @version 3 February 2005
 */
public interface TextTokenizer {

  /**
   * Returns the list of {@link TextEvent} corresponding to the specified character sequence.
   * 
   * @param seq the character sequence to tokenize.
   * @return the corresponding list.
   */
  List<TextEvent> tokenize(CharSequence seq);

  /**
   * Returns the text granularity of this tokenizer.
   * 
   * @return the text granularity of this tokenizer.
   */
  TextGranularity granurality();

}
