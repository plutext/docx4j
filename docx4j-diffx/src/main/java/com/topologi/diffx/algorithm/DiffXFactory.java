/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.algorithm;

import java.lang.reflect.Constructor;

import com.topologi.diffx.sequence.EventSequence;

/**
 * Factory for creating a Diff-X algorithm instance.
 * 
 * @author  Christophe Lauret
 * @version 11 May 2010
 */
public final class DiffXFactory {

  /**
   * Private constructor.
   *
   * <p>This constructor prevents the class from being instantiated.
   */
  private DiffXFactory() {
  }

  /**
   * The classes of the arguments of the constructor.
   */
  private static final Class<?>[] ARGS = new Class<?>[]{EventSequence.class, EventSequence.class};

  /**
   * Creates a Diff-X instance using the specified class name and event sequences.
   * 
   * @param className The class name of the Diff-X algorithm implementation to use.
   * @param sequence1 The first sequence to use for the Diff-X constructor.
   * @param sequence2 The second sequence to use for the Diff-X constructor.
   * 
   * @return A Diff-X algorithm instance.
   * 
   * @throws FactoryException Should an error occur when trying to instantiate the class.
   */
  @SuppressWarnings("unchecked")
  public static DiffXAlgorithm newAlgorithm(String className, EventSequence sequence1, EventSequence sequence2)
      throws FactoryException {
    DiffXAlgorithm algorithm = null;
    try {
      Class<DiffXAlgorithm> cls = (Class<DiffXAlgorithm>)Class.forName(className);
      Constructor<DiffXAlgorithm> cons = cls.getConstructor(ARGS);
      algorithm = cons.newInstance(sequence1, sequence2);
    } catch (Exception ex) {
      throw new FactoryException(ex);
    }
    return algorithm;
  }

  /**
   * Creates a Diff-X instance using the specified class name and event sequences.
   * 
   * @param className The class name of the Diff-X implementation to use.
   * @param sequence1 The first sequence to use for the Diff-X constructor.
   * @param sequence2 The second sequence to use for the Diff-X constructor.
   * 
   * @return A Diff-X algorithm instance.
   * 
   * @deprecated use <code>newAlgorithm</code>
   * 
   * @throws FactoryException Should an error occur when trying to instantiate the class.
   */
  @Deprecated
  public static DiffXAlgorithm createDiffex(String className,
      EventSequence sequence1,
      EventSequence sequence2)
          throws FactoryException {
    return newAlgorithm(className, sequence1, sequence2);
  }

}
