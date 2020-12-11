/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.load;

import java.io.IOException;

import org.xml.sax.InputSource;

import com.topologi.diffx.sequence.EventSequence;

/**
 * Defines recorders that are specific to XML.
 * 
 * @author Christophe Lauret
 * @version 14 April 2005
 */
public interface XMLRecorder extends Recorder {

  /**
   * Runs the recorder on the specified input source.
   * 
   * @param is The input source.
   * 
   * @return The recorded sequence of events.
   * 
   * @throws LoadingException If thrown whilst parsing.
   * @throws IOException      Should I/O error occur.
   */
  EventSequence process(InputSource is) throws LoadingException, IOException;

}
