package com.topologi.diffx.event.impl;

import java.io.IOException;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.xml.XMLWriter;

/**
 * A branch of XML data.
 * 
 * <p>A branch of XML data must start and end with the same element.
 * 
 * <p>Implementation note: this class wraps an array of DiffX events and does not give
 * access to this array, so it can be considered immutable. 
 * 
 * @author Christophe Lauret
 * @version 4 April 2005
 */
public final class XMLBranchEvent extends DiffXEventBase implements DiffXEvent {

  /**
   * The array of Diff-X events that make up the branch.
   */
  private final DiffXEvent[] branch;

  /**
   * Precalculated hashcode to speed up equal comparison.
   */
  private final int hashCode;

  /**
   * Creates a new XML branch.
   * 
   * @param events The array of events that make up the branch.
   */
  public XMLBranchEvent(DiffXEvent[] events) {
    this.branch = events;
//    int tmpHash = 0;
//    for (int i = 0; i < events.length; i++)
//      tmpHash += events[i].hashCode();
//    this.hashCode = tmpHash;
    hashCode = new HashCodeBuilder(17, 37).
	  append(events).
	  toHashCode();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return this.hashCode;
  }

  /**
   * Returns <code>true</code> if the diffX events in the branch are all equal. 
   * 
   * @see DiffXEvent#equals(DiffXEvent)
   */
  public boolean equals(DiffXEvent e) {
    if (e.getClass() != this.getClass()) return false;
    if (e.hashCode() != this.hashCode) return false;
    XMLBranchEvent be = (XMLBranchEvent)e;
    // branch must have the same length
    if (this.branch.length != be.branch.length) return false;
    // every single event must be equal
    for (int i = 0; i < this.branch.length; i++)
      if (!be.branch[i].equals(this.branch[i]))
        return false;
    // if we arrive here they are equal
    return true;
  }

  /**
   * Write the DiffX events in order.
   * 
   * @see com.topologi.diffx.xml.XMLWritable#toXML(com.topologi.diffx.xml.XMLWriter)
   */
  public void toXML(XMLWriter xml) throws IOException {
    for (int i = 0; i < this.branch.length; i++)
      this.branch[i].toXML(xml);
  }

  /**
   * @see com.topologi.diffx.xml.XMLFormattable#toXML(java.lang.StringBuffer)
   */
  public StringBuffer toXML(StringBuffer xml) throws NullPointerException {
    for (int i = 0; i < this.branch.length; i++)
      this.branch[i].toXML(xml);
    return xml;
  }

}
