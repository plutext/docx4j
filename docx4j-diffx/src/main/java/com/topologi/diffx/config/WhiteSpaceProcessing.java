package com.topologi.diffx.config;

/**
 * Defines how white spaces should be processed.
 * 
 * <p>White space processing can have functional implications at all stages of diffing, including
 * when loading and formatting.
 * 
 * @author Christophe Lauret
 * @version 10 May 2010
 */
public enum WhiteSpaceProcessing {

  /**
   * All white spaces should be completely ignored, this is the most efficient processing.
   */
  IGNORE,

  /**
   * White spaces should be preserved, that is they will be loaded and returned during formatting,
   * but the algorithm can consider them equivalent and will not report differences between white
   * spaces.
   */
  PRESERVE,

  /**
   * White spaces should be preserved throughout the process and compared.
   * All white space differences will be reported by the algorithm.
   * This is the most costly processing.
   */
  COMPARE

}
