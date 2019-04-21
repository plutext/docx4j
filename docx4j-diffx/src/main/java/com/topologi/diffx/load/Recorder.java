/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.load;

import java.io.File;
import java.io.IOException;

import com.topologi.diffx.sequence.EventSequence;

/**
 * A class implementing this interface must be able to produce a sequence of event
 * from a specified input.
 * 
 * @author Christophe Lauret
 * @version 8 March 2005
 */
public interface Recorder {

  /**
   * Runs the recorder on the specified file.
   *
   * @param file The file to process.
   * 
   * @return The recorded sequence of events.
   * 
   * @throws LoadingException If thrown while parsing.
   * @throws IOException      Should I/O error occur.
   */
  EventSequence process(File file) throws LoadingException, IOException;

  /**
   * Runs the recorder on the specified string.
   * 
   * @param xml The string to process.
   * 
   * @return The recorded sequence of events.
   * 
   * @throws LoadingException If thrown while parsing.
   * @throws IOException      Should I/O error occur.
   */
  EventSequence process(String xml) throws LoadingException, IOException;

}
