/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.algorithm;

/**
 * A matrix implementation which backbone is a matrix of integers.
 * 
 * @author Christophe Lauret (Allette Systems)
 * @version 7 April 2005
 */
public final class MatrixInt implements Matrix {
  //	 TODO: this class should probably not be public

  /**
   * The actual matrix storing the values.
   */
  private int[][] matrix;

  /**
   * @see com.topologi.diffx.algorithm.Matrix#setup(int, int)
   */
  public void setup(int width, int height) {
    this.matrix = new int[width][height];
  }

  /**
   * @see com.topologi.diffx.algorithm.Matrix#set(int, int, int)
   */
  public void set(int i, int j, int x) {
    this.matrix[i][j] = x;
  }

  /**
   * @see com.topologi.diffx.algorithm.Matrix#get(int, int)
   */
  public int get(int i, int j) {
    return this.matrix[i][j];
  }

  /**
   * @see com.topologi.diffx.algorithm.Matrix#incrementPathBy(int, int, int)
   */
  public void incrementPathBy(int i, int j, int n) {
    this.matrix[i][j] = this.matrix[i+1][j+1] + n;
  }

  /**
   * @see com.topologi.diffx.algorithm.Matrix#incrementByMaxPath(int, int)
   */
  public void incrementByMaxPath(int i, int j) {
    this.matrix[i][j] = Math.max(this.matrix[i+1][j], this.matrix[i][j+1]);
  }

  /**
   * @see com.topologi.diffx.algorithm.Matrix#isGreaterX(int, int)
   */
  public boolean isGreaterX(int i, int j) {
    return this.matrix[i+1][j] > this.matrix[i][j+1];
  }

  /**
   * @see com.topologi.diffx.algorithm.Matrix#isGreaterY(int, int)
   */
  public boolean isGreaterY(int i, int j) {
    return this.matrix[i+1][j] < this.matrix[i][j+1];
  }

  /**
   * @see com.topologi.diffx.algorithm.Matrix#isSameXY(int, int)
   */
  public boolean isSameXY(int i, int j) {
    return this.matrix[i+1][j] == this.matrix[i][j+1];
  }

  /**
   * Gets rid of the underlying matrix so that garbage collector can do its work.
   * 
   * @see Matrix#release()
   */
  public void release() {
    this.matrix = null;
  }

}
