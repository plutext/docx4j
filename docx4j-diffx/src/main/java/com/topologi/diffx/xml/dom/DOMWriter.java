/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml.dom;

import org.w3c.dom.Document;

import com.topologi.diffx.xml.XMLWriter;

/**
 * An XML writer which output is a DOM tree.
 * 
 * @author Christophe Lauret
 * @version 7 June 2005
 */
public interface DOMWriter extends XMLWriter {

  /**
   * Returns the DOM document produced by the XML Writer.
   * 
   * @return The DOM document produced by the XML Writer.
   */
  Document getDocument();

}
