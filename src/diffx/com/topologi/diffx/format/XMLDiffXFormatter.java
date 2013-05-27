/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.format;

import com.topologi.diffx.sequence.PrefixMapping;

/**
 * An interface for formatting the output of the Diff-X algorithm as XML.
 * 
 * <p>This interface defines some additional methods that are specific to XML.
 * 
 * @author Christophe Lauret
 * @version 17 May 2005
 */
public interface XMLDiffXFormatter extends DiffXFormatter {

  /**
   * Set whether the formatter should include the XML declaration or not.
   * 
   * @param show <code>true</code> to get the formatter to write the XML declaration;
   *             <code>false</code> otherwise.
   */
  void setWriteXMLDeclaration(boolean show);

  /**
   * Adds the specified prefix mapping to the formatter so that they can be dclared when
   * needed.
   * 
   * @param mapping The prefix mapping to add.
   */
  void declarePrefixMapping(PrefixMapping mapping);

}
