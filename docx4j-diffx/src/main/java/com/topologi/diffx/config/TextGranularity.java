package com.topologi.diffx.config;

/**
 * Defines how should be tokenised and compared.
 * 
 * @author Christophe Lauret
 * @version 10 May 2010
 */
public enum TextGranularity {

  /**
   * Differences should be reported at the character level.
   */
  CHARACTER,

  /**
   * Differences should be reported at the word level.
   */
  WORD,

  /**
   * Differences should be reported for the entire text node.
   */
  TEXT

}
