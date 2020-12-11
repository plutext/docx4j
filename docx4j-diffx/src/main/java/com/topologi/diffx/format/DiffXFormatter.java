/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.format;

import java.io.IOException;

import com.topologi.diffx.config.DiffXConfig;
import com.topologi.diffx.event.DiffXEvent;

/**
 * An interface for formatting the output of the Diff-X algorithm.
 * 
 * @author Christophe Lauret
 * @version 15 December 2004
 */
public interface DiffXFormatter {

  /**
   * Formats the specified event.
   * 
   * @param e The event to format
   * 
   * @throws IOException Should an I/O exception occurs while formatting.
   * @throws IllegalStateException If the formatter is not in a state to run this method.
   */
  void format(DiffXEvent e) throws IOException, IllegalStateException;

  /**
   * Formats the specified inserted event.
   * 
   * @param e The event to format
   * 
   * @throws IOException Should an I/O exception occurs while formatting.
   * @throws IllegalStateException If the formatter is not in a state to run this method.
   */
  void insert(DiffXEvent e) throws IOException, IllegalStateException;

  /**
   * Formats the specified deleted event.
   * 
   * @param e The event to format
   * 
   * @throws IOException           Should an I/O exception occurs while formatting.
   * @throws IllegalStateException If the formatter is not in a state to run this method.
   */
  void delete(DiffXEvent e) throws IOException, IllegalStateException;

  /**
   * Sets the configuration to use with this formatter.
   * 
   * @param config The configuration to use.
   */
  void setConfig(DiffXConfig config);

}
