/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml.sax;

import org.xml.sax.InputSource;

import com.topologi.diffx.xml.XMLWritable;

/**
 * An XML input source implementation wrapping a XML writable object.
 * 
 * <p>This class allows a SAX application to encapsulate information
 * about an input source in a single object.</p>
 *
 * <p>Because it does not provide a byte stream, character stream or
 * public or system identifier, this class is only meant to be used
 * by the <code>XMLWritableReader</code> which will use the
 * <code>XMLWritable</code> object.
 *
 * <p>An InputSource object belongs to the application: the SAX parser
 * shall not modify it in any way.</p>
 * 
 * @see org.xml.sax.InputSource
 * @see com.topologi.diffx.xml.XMLWritable
 * @see com.topologi.diffx.xml.sax.XMLWritableReader
 * 
 * @author  Christophe Lauret
 * @version 26 May 2005
 */
public final class XMLWritableInputSource extends InputSource {

  /**
   * The wrapped XML writable object.
   */
  private final XMLWritable source;

  /**
   * Creates an XML Writable object.
   * 
   * @param object The XMLWritable object to wrap.
   */
  public XMLWritableInputSource(XMLWritable object) {
    this.source = object;
  }

  /**
   * Returns the XMLWritable object
   * 
   * @return The XMLWritable object
   */
  public XMLWritable getXMLWritable() {
    return this.source;
  }

}
