/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml;

/**
 * An Object which implements this interface can be serialized
 * as XML using the <code>XMLSerializer</code>.
 *
 * <p>It does not need to implement any method; it simply indicates
 * the XMLSerializer that it can use the public <code>getXXX</code>
 * methods to generate the XML representation of the object.
 *
 * @see XMLSerializer
 *
 * @version 7 February 2002
 * @author Christophe Lauret
 */
public interface XMLSerializable {

}
