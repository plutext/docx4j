/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.algorithm;

/**
 * A matrix for the computation of the Diff-X path.
 * 
 * <p>This interface is intended to provide methods for initialising and accessing
 * the values of the matrix regardless of the storage method used.
 * 
 * <p>Implementations could use binary matrices, I/O objects, etc...
 * 
 * @author Christophe Lauret (Allette Systems)
 * @version 7 April 2005
 */
public interface Matrix {
  //	 TODO: this class should probably not be public

  /**
   * Create a matrix of the given width and height.
   * 
   * @param width  The number of columns.
   * @param height The number of rows.
   */
  void setup(int width, int height);

  // default access -----------------------------------------------------------------------

  /**
   * Sets the value of the matrix at the given position.
   * 
   * @param i The column index.
   * @param j The row index.
   * 
   * @param x The value to set.
   */
  void set(int i, int j, int x);

  /**
   * Returns the value at the given position.
   * 
   * @param i The column index.
   * @param j The row index.
   * 
   * @return The value at the given position.
   */
  int get(int i, int j);

  // to fill up the matrix ----------------------------------------------------------------

  /**
   * Increment the path.
   * 
   * <p>value(i, j) := value(i+1, j+1) + n
   * 
   * @param i The column index.
   * @param j The row index.

   * @param n The increment number.
   */
  void incrementPathBy(int i, int j, int n);

  /**
   * Increment by the maximum path.
   * 
   * <p>value(i, j) := max( value(i+1, j) , value(i, j+1) )
   * 
   * @param i The column index.
   * @param j The row index.
   */
  void incrementByMaxPath(int i, int j);

  // to determine the path ----------------------------------------------------------------

  /**
   * Returns <code>true</code> we should move on the X direction.
   * 
   * <p>if value(i+1, j) > value(i, j+1)
   * 
   * @param i The column index.
   * @param j The row index.
   * 
   * @return <code>true</code> to move to i+1;
   *         <code>false</code> otherwise.
   */
  boolean isGreaterX(int i, int j);

  /**
   * Returns <code>true</code> we should move on the X direction.
   * 
   * <p>if value(i+1, j) &lt; value(i, j+1)
   * 
   * @param i The column index.
   * @param j The row index.
   * 
   * @return <code>true</code> to move to j+1;
   *         <code>false</code> otherwise.
   */
  boolean isGreaterY(int i, int j);

  /**
   * Returns <code>true</code> we moving on the X direction is
   * equivalent to moving on the Y direction.
   * 
   * <p>if value(i+1, j) == value(i, j+1)
   * 
   * @param i The column index.
   * @param j The row index.
   * 
   * @return <code>true</code> if it is the same;
   *         <code>false</code> otherwise.
   */
  boolean isSameXY(int i, int j);

  /**
   * Releases all the resources used only by this matrix object.
   * 
   * <p>This class is not usable, until after invoking this method, unless
   * it is setup again.
   */
  void release();

}
