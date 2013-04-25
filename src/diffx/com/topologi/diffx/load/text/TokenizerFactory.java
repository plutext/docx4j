/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.load.text;

import com.topologi.diffx.config.DiffXConfig;
import com.topologi.diffx.config.TextGranularity;

/**
 * Factory for tokenizers.
 * 
 * <p>This class is designed to returned tokenizers that corresponds to the given configuration.
 * 
 * @author Christophe Lauret
 * @version 10 May 2010
 */
public final class TokenizerFactory {

  /**
   * Creates a factory for tokenizers.
   * 
   * @throws NullPointerException If the configuration is <code>null</code>.
   */
  private TokenizerFactory() {
  }

  /**
   * Returns the text tokenizer.
   * 
   * @param config The configuration to use.
   * 
   * @return the corresponding tokenizer.
   * 
   * @throws NullPointerException If the configuration is <code>null</code>.
   */
  public static TextTokenizer get(DiffXConfig config) {
    if (config == null) throw new NullPointerException("The config should be specified");
    TextGranularity granularity = config.getGranularity();
    switch (granularity) {
      case CHARACTER: return new TokenizerByChar();
      case WORD: return new TokenizerByWord(config.getWhiteSpaceProcessing());
      case TEXT: return new TokenizerByText(config.getWhiteSpaceProcessing());
      default:
        throw new IllegalArgumentException("Unsupported text granularity "+granularity);
    }
  }

}
