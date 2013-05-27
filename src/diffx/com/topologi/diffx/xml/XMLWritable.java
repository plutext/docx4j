/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml;

import java.io.IOException;

/**
 * <p>An Object which implements this interface can be written as XML using a
 * {@link XMLWriter} instance.
 *
 * @author Christophe Lauret (Allette Systems)
 *
 * @version 14 September 2004
 */
public interface XMLWritable {

  /**
   * Writes the XML representation of the implementing instance using the specified
   * {@link XMLWriter}.
   *
   * @param xml The XMLWriter to use.
   *
   * @throws IOException IF an I/O exception occurs whilst writing.
   */
  void toXML(XMLWriter xml) throws IOException;

}
