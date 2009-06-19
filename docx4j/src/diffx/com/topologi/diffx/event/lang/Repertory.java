package com.topologi.diffx.event.lang;

import java.util.Hashtable;

import com.topologi.diffx.event.impl.WordEvent;

/**
 * A repertory of words.
 * 
 * <p>Use this class to store word events in order to avoid creating new instances.
 * 
 * @author Christophe Lauret
 * @version 15 December 2004
 */
public final class Repertory {

  /**
   * Where the words are stored.
   */
  private Hashtable storage = new Hashtable();

  /**
   * Creates a new repertoire.  
   */
  public Repertory() {
  }

  /**
   * Adds a new word to this repertory.
   * 
   * @param word The word to be added.
   */
  public void put(String word) {
    this.storage.put(word, new WordEvent(word));
  }

  /**
   * Returns the <code>WordEvent</code> corresponding to the specified word.
   * 
   * @param word The word to be retrieved.
   * 
   * @return The corresponding <code>WordEvent</code> or <code>null</code>.
   */
  public WordEvent get(String word) {
    return (WordEvent)this.storage.get(word);
  }

  /**
   * Returns the <code>WordEvent</code> corresponding to the specified word.
   * 
   * <p>This method will add the word to the repertory if it does not already exist.
   * 
   * @param word The word to be added and retrieved.
   * 
   * @return The corresponding <code>WordEvent</code>.
   */
  public WordEvent update(String word) {
    if (!contains(word))
      this.storage.put(word, new WordEvent(word));
    return (WordEvent)this.storage.get(word);
  }

  /**
   * Returns the <code>true</code> if this repertory contains the specified word.
   * 
   * @param word The word to be searched.
   * 
   * @return <code>true</code> if this repertory contains the word;
   *         <code>false</code> otherwise.
   */
  public boolean contains(String word) {
    return this.storage.containsKey(word);
  }

  /**
   * Returns the number of words in the repertory.
   * 
   * @return The number of words in the repertory.
   */
  public int size() {
    return this.storage.size();
  }

}
