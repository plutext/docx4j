/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml;

/**
 * Class of exceptions thrown when a namespace is being used without being declared.
 * 
 * <p>To avoid this exception being thrown, the namespace URI must be explicitely associated
 * with a prefix before a node belonging to this namespace is serialiased. Namespaces can be
 * declared using the {@link XMLWriter#setPrefixMapping(String, String)} method.
 * 
 * @author Christophe Lauret (Allette Systems)
 * @version 14 May 2005
 */
public final class UndeclaredNamespaceException extends RuntimeException {

  /**
   * Version number for the serialised class.
   */
  static final long serialVersionUID = 8080581405972912943L;

  /**
   * Creates a new exception for undeclared namespaces.
   *
   * @param uri The namespace URI that has not been declared.
   */
  public UndeclaredNamespaceException(String uri) {
    super("The namespace URI \""+uri+"\" has not been mapped to any prefix.");
  }

}
