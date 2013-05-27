/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.algorithm;

import java.io.IOException;

import com.topologi.diffx.format.DiffXFormatter;
import com.topologi.diffx.sequence.EventSequence;

/**
 * Performs the diff comparison of sequences of events.
 * 
 * @author Christophe Lauret
 * @version 15 December 2004
 */
public interface DiffXAlgorithm {

  /**
   * Returns the length of the longest common subsequence.
   * 
   * @return the length of the longest common subsequence.
   */
  int length();

  /**
   * Performs the comparison and writes the results using the specified Diff-X formatter.
   * 
   * @param formatter The formatter that will handle the output.
   *
   * @throws IOException If thrown by the formatter.
   */
  void process(DiffXFormatter formatter) throws IOException;

  /**
   * Returns the first sequence used for the diff-x comparison.
   * 
   * @return the first sequence used for the diff-x comparison.
   */
  EventSequence getFirstSequence();

  /**
   * Returns the second sequence used for the diff-x comparison.
   * 
   * @return the second sequence used for the diff-x comparison.
   */
  EventSequence getSecondSequence();

}