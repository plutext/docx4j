/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.event.impl;

import java.io.IOException;

import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.event.TextEvent;
import com.topologi.diffx.xml.XMLWriter;

/**
 * A particular type of event reserved for ignored white spaces.
 *
 * <p>
 * This class can be used to ignore whitespaces for processing but preserve them for formatting.
 * This event keeps the exact formatting of the white space henceby preserving it, but will consider
 * any instances of this class to be equal regardless of their actual formatting so that the
 * algorithm ignores the differences.
 *
 * @author Christophe Lauret
 * @version 28 March 2010
 */
public final class IgnorableSpaceEvent implements TextEvent {

  /**
   * The characters for this event.
   */
  private final String characters;

  /**
   * Creates a new ignorable white space event.
   *
   * @param seq The char sequence.
   *
   * @throws NullPointerException If the given String is <code>null</code>.
   */
  public IgnorableSpaceEvent(CharSequence seq) throws NullPointerException {
    if (seq == null)
      throw new NullPointerException("The characters cannot be null, use \"\"");
    this.characters = seq.toString();
  }

  /**
   * Returns "ignorable-space".
   *
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "ignorable-space";
  }

  /**
   * Always returns the same value.
   *
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return 71;
  }

  /**
   * Returns <code>true</code> if the event is an ignorable white space, regardless of the
   * characters that it matches.
   *
   * @param o The event to compare with this event.
   *
   * @return <code>true</code> if considered equal;
   *         <code>false</code> otherwise.
   */
  @Override
  public boolean equals(Object o) {
    return o.getClass() == this.getClass();
  }

  /**
   * Returns <code>true</code> if the event is an ignorable white space, regardless of the
   * characters that it matches.
   *
   * @param e The event to compare with this event.
   *
   * @return <code>true</code> if considered equal;
   *         <code>false</code> otherwise.
   */
  @Override
  public boolean equals(DiffXEvent e) {
    if (this == e)
      return true;
    if (e.getClass() != this.getClass())
      return false;
    // always return true
    return true;
  }

  /**
   * Returns the characters that this event represents.
   *
   * <p>
   * Note: this method will return the characters as used by Java (ie. Unicode), they may not be
   * suitable for writing to an XML string.
   *
   * @return The characters that this event represents.
   */
  @Override
  public String getCharacters() {
    return this.characters;
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    // we don't need to parse the whitespace characters.
    xml.writeXML(this.characters);
  }

  @Override
  public String toXML() {
    return this.characters;
  }

  @Override
  public StringBuffer toXML(StringBuffer xml) throws NullPointerException {
    xml.append(this.characters);
    return xml;
  }

  @Override
  public int getWeight() {
    return 1;
  }

  /**
   * Ignored.
   *
   * {@inheritDoc}
   */
  @Override
  public void setWeight(int weight) {
  }

}
