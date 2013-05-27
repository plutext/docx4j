/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import com.topologi.diffx.event.impl.LineEvent;
import com.topologi.diffx.sequence.EventSequence;

/**
 * Records the line events in a text.
 * 
 * @author Christophe Lauret
 * 
 * @version 17 October 2006
 */
public final class TextRecorder implements Recorder {

  /**
   * Runs the recorder on the specified file.
   *
   * <p>This method will count on the {@link org.xml.sax.InputSource} to guess the correct encoding.
   * 
   * @param file The file to process.
   * 
   * @return The recorded sequence of events.
   * 
   * @throws LoadingException If thrown whilst parsing.
   * @throws IOException      Should I/O error occur.
   */
  public EventSequence process(File file) throws LoadingException, IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = reader.readLine();
    int count = 0;
    EventSequence seq = new EventSequence();
    while (line != null) {
      seq.addEvent(new LineEvent(line, ++count));
      line = reader.readLine();
    }
    reader.close();
    return seq;
  }

  /**
   * Runs this recorder on the specified string.
   * 
   * @param text The text string to process.
   * 
   * @return The recorded sequence of events.
   * 
   * @throws LoadingException If thrown whilst parsing.
   * @throws IOException      Should I/O error occur.
   */
  public EventSequence process(String text) throws LoadingException, IOException {
    BufferedReader reader = new BufferedReader(new StringReader(text));
    String line = reader.readLine();
    int count = 0;
    EventSequence seq = new EventSequence();
    while (line != null) {
      seq.addEvent(new LineEvent(line, ++count));
      line = reader.readLine();
    }
    return seq;
  }

}
