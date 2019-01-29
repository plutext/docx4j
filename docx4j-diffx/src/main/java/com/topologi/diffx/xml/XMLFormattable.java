/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml;

/**
 * <p>An Object which implements this interface can be formatted as XML using a <code>toXML</code>
 * method as a string.
 *
 * <p>This method is provided for convenience for small object to avoid the overhead in using
 * writers.
 *
 * @author Christophe Lauret (Allette Systems)
 *
 * @version 10 December 2004
 */
public interface XMLFormattable {

  /**
   * Appends the XML representation of the object of the implementing class.
   *
   * <p>Implementations must ensure that the returned string buffer is the same
   * object as the specified string buffer.
   *
   * @param xml The string buffer to which the XML representation is appended to.
   *
   * @return The modified string buffer.
   *
   * @throws NullPointerException if the specified character sequence is <code>null</code>.
   */
  StringBuffer toXML(StringBuffer xml) throws NullPointerException;

  /**
   * <p>Returns a xml representation of the object of the implementing class.
   *
   * <p>Most implementation should use the following code to ensure consistent data with the
   * other <code>toXML</code> method:
   *
   * <pre>return this.toXML(new StringBuffer()).toString();</pre>
   *
   * @return a XML representation of the object of the implementing class.
   */
  String toXML();

}
