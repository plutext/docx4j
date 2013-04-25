/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.algorithm;

import com.topologi.diffx.sequence.EventSequence;

/**
 * A base class for Diff-X algorithms.
 * 
 * @author Christophe Lauret
 * @version 15 December 2004
 */
public abstract class DiffXAlgorithmBase implements DiffXAlgorithm {

  // class attributes ---------------------------------------------------------------------------

  /**
   * The first sequence of events to test.
   */
  protected final EventSequence sequence1;

  /**
   * The second sequence of events to test.
   */
  protected final EventSequence sequence2;

  /**
   * Length of the first sequence to compare.
   */
  protected final int length1;

  /**
   * Length of the second sequence to compare.
   */
  protected final int length2;

  /**
   * The length of the LCS.
   */
  protected int length = -1;

  // constructor --------------------------------------------------------------------------------

  /**
   * Creates a new DiffX algorithm base class.
   * 
   * @param seq0 The first sequence to compare.
   * @param seq1 The second sequence to compare.
   */
  public DiffXAlgorithmBase(EventSequence seq0, EventSequence seq1) {
    this.sequence1 = seq0;
    this.sequence2 = seq1;
    this.length1 = seq0.size();
    this.length2 = seq1.size();
  }

  // methods ------------------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  public final EventSequence getFirstSequence() {
    return this.sequence1;
  }

  /**
   * {@inheritDoc}
   */
  public final EventSequence getSecondSequence() {
    return this.sequence2;
  }

}
