package com.topologi.diffx.event.lang;

import com.topologi.diffx.event.impl.WordEvent;

/**
 * A list of common words in English.
 * 
 * @author Christophe Lauret
 * @version 15 December 2004
 */
public final class CommonWordsEnglish {

  /**
   * Where we store the words.
   */
  private static final Repertory REPERTORY_EN = new Repertory();

  /**
   * Prevents creation of instances.
   */
  private CommonWordsEnglish() {
  }

  /**
   * Indicates whether the words have been loaded.
   */
  private static boolean loaded = false;

  /**
   * Indicates whether the specified is in this list.
   */
  private static void load() {
    final String[] words = {
        "a", "and", "are", "as", "at", "be", "but", "by",
        "for", "if", "in", "into", "is", "it",
        "no", "not", "of", "on", "or", "s", "such",
        "t", "that", "the", "their", "then", "there", "these",
        "they", "this", "to", "was", "will", "with"
    };
    for (int i = 0; i < words.length; i++)
      REPERTORY_EN.put(words[i]);
    loaded = true;
  }

  /**
   * Indicates whether the specified is in this list.
   * 
   * @param word The word that is being used.
   * 
   * @return <code>true</code> if the specified word is common;
   *         <code>false</code> otherwise.
   */
  public static boolean contains(String word) {
    if (!loaded) load();
    return REPERTORY_EN.contains(word);
  }

  /**
   * Returns the <code>WordEvent</code> corresponding to the specified word.
   * 
   * @param word The word to be retrieved.
   * 
   * @return The corresponding <code>WordEvent</code> or <code>null</code>.
   */
  public static WordEvent get(String word) {
    if (!loaded) load();
    return REPERTORY_EN.get(word);
  }

}
