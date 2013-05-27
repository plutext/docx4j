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
 * A particular type of event reserved for white spaces.
 *
 * @author Christophe Lauret
 * @version 27 March 2010
 */
public final class SpaceEvent extends CharactersEventBase implements TextEvent {

  /**
   * A static instance for the single white spaces.
   *
   * <p>Use this constant instead of creating new instances
   */
  public static final SpaceEvent SINGLE_WHITESPACE = new SpaceEvent(" ");

  /**
   * A static instance for the double white spaces.
   *
   * <p>Use this constant instead of creating new instances
   */
  public static final SpaceEvent DOUBLE_WHITESPACE = new SpaceEvent("  ");

  /**
   * A static instance for the new lines.
   *
   * <p>Use this constant instead of creating new instances
   */
  public static final SpaceEvent NEW_LINE = new SpaceEvent("\n");

  /**
   * A static instance for tabs.
   *
   * <p>Use this constant instead of creating new instances
   */
  public static final SpaceEvent TAB = new SpaceEvent("\t");

  /**
   * Creates a new word event.
   *
   * @param w The word as a string.
   *
   * @throws NullPointerException If the given String is <code>null</code>.
   */
  public SpaceEvent(CharSequence w) throws NullPointerException {
    super(w);
  }

  @Override
  public String toString() {
    return "space: \""+toString(getCharacters().toCharArray())+'"';
  }

  /**
   * Returns the white space event corresponding to the given string.
   *
   * @param space The string for the white space event.
   *
   * @return A readable string.
   */
  public static SpaceEvent getInstance(CharSequence space) {
    // check constants
    if (" ".equals(space))  return SINGLE_WHITESPACE;
    if ("  ".equals(space)) return DOUBLE_WHITESPACE;
    if ("\n".equals(space)) return NEW_LINE;
    if ("\t".equals(space)) return TAB;
    // create a new instance
    return new SpaceEvent(space);
  }

  /**
   * Returns the white space event corresponding to the given string.
   *
   * @param c The string for the white space event.
   *
   * @return A readable string.
   */
  public static SpaceEvent getInstance(char c) {
    // check constants
    if (c == ' ')  return SINGLE_WHITESPACE;
    if (c == '\n') return NEW_LINE;
    if (c == '\t') return TAB;
    // create a new instance
    // FIXME wasteful!!
    return new SpaceEvent(c+"");
  }

  /**
   * Returns the white space characters as a readable string.
   *
   * @param chars The whitespace characters
   *
   * @return A readable string.
   */
  private static String toString(char[] chars) {
    StringBuffer out = new StringBuffer();
    for (char c : chars) {
      switch(c) {
        case '\n':
          out.append("\\n");
          break;
        case '\t':
          out.append("\\t");
          break;
        default :
          out.append(c);
      }
    }
    return out.toString();
  }

}
