package com.topologi.diffx.load.text;

import com.topologi.diffx.config.DiffXConfig;

/**
 * Factory for tokenisers.
 * 
 * <p>This class is designed to returned tokenisers that corresponds to the given
 * configuration. 
 * 
 * @author Christophe Lauret
 * @version 27 April 2005
 */
public final class TokeniserFactory {

// class attributes ---------------------------------------------------------------------

  /**
   * Indicates whether the factory should generate namespace events. 
   */
  private final DiffXConfig config;

  /**
   * The tokeniser to use.
   * 
   * 0 = consider + preserve
   * 1 = consider + trash
   * 2 = ignore + preserve
   * 3 = ignore + trash 
   */
  private final transient int tokeniserChoice;

// constructors -------------------------------------------------------------------------

  /**
   * Creates a factory for tokenisers.
   * 
   * @param config The configuration to use.
   * 
   * @throws NullPointerException If the configuration is <code>null</code>.
   */
  public TokeniserFactory(DiffXConfig config) throws NullPointerException {
    if (config == null) throw new NullPointerException("Factory requires a tokeniser."); 
    this.config = config;
    this.tokeniserChoice = (this.config.isIgnoreWhiteSpace()? 2 : 0)
                         + (this.config.isPreserveWhiteSpace()? 0 : 1);
  }

// methods ------------------------------------------------------------------------------

  /**
   * Returns the text tokeniser for the specified text according to the
   * configuration of this tokeniser.
   * 
   * @param text The text to tokenise.
   * 
   * @return The open element event from the uri and name given.
   */
  public TextTokeniser makeTokeniser(CharSequence text) {
    switch(tokeniserChoice) {
    case 0: // consider + preserve
      return new TextTokeniserByWord(text);
    case 1: // consider + trash: we actually preserve for now.
      return new TextTokeniserByWord(text);
    case 2: // ignore + preserve
      return new TextTokeniserIgnoreSpace(text);
    case 3: // ignore + trash
      return new TextTokeniserNoSpace(text);
    default:
      throw new IllegalStateException("Impossible whitespace configuration: "+tokeniserChoice); 
    }
  }

  /**
   * Returns the configuration used by this factory.
   * 
   * @return the configuration used by this factory.
   */
  public DiffXConfig getConfig() {
    return this.config;
  }

}
