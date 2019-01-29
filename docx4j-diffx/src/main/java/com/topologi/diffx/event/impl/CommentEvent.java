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
import com.topologi.diffx.xml.XMLWriter;

/**
 * A comment event.
 *
 * @author Christophe Lauret
 * @author Jason Harrop
 *
 * @version 27 March 2010
 */
public final class CommentEvent extends DiffXEventBase implements DiffXEvent {

  /**
   * The comment string.
   */
  private final String comment;

  /**
   * Hashcode value for this event.
   */
  private final int hashCode;

  /**
   * Creates a new comment event.
   *
   * @param comment the comment string.
   *
   * @throws NullPointerException if any of the argument is <code>null</code>.
   */
  public CommentEvent(String comment) throws NullPointerException {
    this.comment = comment;
    this.hashCode = toHashcode(comment);
  }

  /**
   * Returns the comment.
   *
   * @return the comment string.
   */
  public String getComment() {
    return this.comment;
  }

  @Override
  public int hashCode() {
    return this.hashCode;
  }

  /**
   * Returns <code>true</code> if the event is a comment event.
   *
   * @param e The event to compare with this event.
   *
   * @return <code>true</code> if this event is equal to the specified event;
   *         <code>false</code> otherwise.
   */
  @Override
  public boolean equals(DiffXEvent e) {
    if (e.getClass() != this.getClass())
      return false;
    CommentEvent ce = (CommentEvent) e;
    return ce.comment == null && this.comment == null || ce.comment.equals(this.comment);
  }

  @Override
  public String toString() {
    return "comment: " + this.comment;
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.writeComment(this.comment);
  }

  @Override
  public StringBuffer toXML(StringBuffer xml) {
    // xml.append("<!--");
    xml.append(this.comment);
    // xml.append("-->");
    return xml;
  }

  /**
   * Calculates the hashcode for this event.
   *
   * @param comment The comment string.
   * @return a number suitable as a hashcode.
   */
  private int toHashcode(String comment) {
    return comment != null? 19*37 + comment.hashCode() : 19*37;
  }

}
