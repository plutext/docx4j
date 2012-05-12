package com.topologi.diffx.algorithm;

/* ============================================================================
 * ARTISTIC LICENCE
 * 
 * Preamble
 * 
 * The intent of this document is to state the conditions under which a Package
 * may be copied, such that the Copyright Holder maintains some semblance of 
 * artistic control over the development of the package, while giving the users
 * of the package the right to use and distribute the Package in a more-or-less
 * customary fashion, plus the right to make reasonable modifications.
 *
 * Definitions:
 *  - "Package" refers to the collection of files distributed by the Copyright 
 *    Holder, and derivatives of that collection of files created through 
 *    textual modification.
 *  - "Standard Version" refers to such a Package if it has not been modified, 
 *    or has been modified in accordance with the wishes of the Copyright 
 *    Holder.
 *  - "Copyright Holder" is whoever is named in the copyright or copyrights 
 *    for the package.
 *  - "You" is you, if you're thinking about copying or distributing this 
 *    Package.
 *  - "Reasonable copying fee" is whatever you can justify on the basis of 
 *    media cost, duplication charges, time of people involved, and so on. 
 *    (You will not be required to justify it to the Copyright Holder, but only 
 *    to the computing community at large as a market that must bear the fee.)
 *  - "Freely Available" means that no fee is charged for the item itself, 
 *    though there may be fees involved in handling the item. It also means 
 *    that recipients of the item may redistribute it under the same conditions
 *    they received it.
 *
 * 1. You may make and give away verbatim copies of the source form of the 
 *    Standard Version of this Package without restriction, provided that you 
 *    duplicate all of the original copyright notices and associated 
 *    disclaimers.
 *
 * 2. You may apply bug fixes, portability fixes and other modifications 
 *    derived from the Public Domain or from the Copyright Holder. A Package 
 *    modified in such a way shall still be considered the Standard Version.
 *
 * 3. You may otherwise modify your copy of this Package in any way, provided 
 *    that you insert a prominent notice in each changed file stating how and 
 *    when you changed that file, and provided that you do at least ONE of the 
 *    following:
 * 
 *    a) place your modifications in the Public Domain or otherwise make them 
 *       Freely Available, such as by posting said modifications to Usenet or 
 *       an equivalent medium, or placing the modifications on a major archive 
 *       site such as ftp.uu.net, or by allowing the Copyright Holder to 
 *       include your modifications in the Standard Version of the Package.
 * 
 *    b) use the modified Package only within your corporation or organization.
 *
 *    c) rename any non-standard executables so the names do not conflict with 
 *       standard executables, which must also be provided, and provide a 
 *       separate manual page for each non-standard executable that clearly 
 *       documents how it differs from the Standard Version.
 * 
 *    d) make other distribution arrangements with the Copyright Holder.
 *
 * 4. You may distribute the programs of this Package in object code or 
 *    executable form, provided that you do at least ONE of the following:
 * 
 *    a) distribute a Standard Version of the executables and library files, 
 *       together with instructions (in the manual page or equivalent) on where
 *       to get the Standard Version.
 *
 *    b) accompany the distribution with the machine-readable source of the 
 *       Package with your modifications.
 * 
 *    c) accompany any non-standard executables with their corresponding 
 *       Standard Version executables, giving the non-standard executables 
 *       non-standard names, and clearly documenting the differences in manual 
 *       pages (or equivalent), together with instructions on where to get 
 *       the Standard Version.
 *
 *    d) make other distribution arrangements with the Copyright Holder.
 *
 * 5. You may charge a reasonable copying fee for any distribution of this 
 *    Package. You may charge any fee you choose for support of this Package. 
 *    You may not charge a fee for this Package itself. However, you may 
 *    distribute this Package in aggregate with other (possibly commercial) 
 *    programs as part of a larger (possibly commercial) software distribution 
 *    provided that you do not advertise this Package as a product of your own.
 *
 * 6. The scripts and library files supplied as input to or produced as output 
 *    from the programs of this Package do not automatically fall under the 
 *    copyright of this Package, but belong to whomever generated them, and may
 *    be sold commercially, and may be aggregated with this Package.
 *
 * 7. C or perl subroutines supplied by you and linked into this Package shall 
 *    not be considered part of this Package.
 *
 * 8. The name of the Copyright Holder may not be used to endorse or promote 
 *    products derived from this software without specific prior written 
 *    permission.
 * 
 * 9. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED 
 *    WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF 
 *    MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * ============================================================================
 */

/**
 * A matrix implementation which backbone is a matrix of short numbers.
 * 
 * @author Christophe Lauret (Allette Systems)
 * @version 7 April 2005
 */
final class MatrixShort implements Matrix {

  /**
   * The actual matrix storing the values.
   */
  private short[][] matrix;

  /**
   * @see com.topologi.diffx.algorithm.Matrix#setup(int, int)
   */
  public void setup(int width, int height) {
    this.matrix = new short[width][height];
  }

  /**
   * @see com.topologi.diffx.algorithm.Matrix#set(int, int, int)
   */
  public void set(int i, int j, int x) {
    this.matrix[i][j] = (short)x;
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
    this.matrix[i][j] = (short)(this.matrix[i+1][j+1] + n);
  }

  /**
   * @see com.topologi.diffx.algorithm.Matrix#incrementByMaxPath(int, int)
   */
  public void incrementByMaxPath(int i, int j) {
    this.matrix[i][j] = max(this.matrix[i+1][j], this.matrix[i][j+1]);
  }

  /**
   * @see com.topologi.diffx.algorithm.Matrix#isGreaterX(int, int)
   */
  public boolean isGreaterX(int i, int j) {
    return matrix[i+1][j] > matrix[i][j+1];
  }

  /**
   * @see com.topologi.diffx.algorithm.Matrix#isGreaterY(int, int)
   */
  public boolean isGreaterY(int i, int j) {
    return matrix[i+1][j] < matrix[i][j+1];
  }

  /**
   * @see com.topologi.diffx.algorithm.Matrix#isSameXY(int, int)
   */
  public boolean isSameXY(int i, int j) {
    return matrix[i+1][j] == matrix[i][j+1];
  }

  /**
   * Returns the maximum of the two values. 
   * 
   * @param a The first value to compare.
   * @param b The second value to compare.
   * 
   * @return The maximum of the two values.
   */
  private static short max(short a, short b) {
    return (a >= b)? a : b;
  }

  
  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuffer out = new StringBuffer();
    for (int j = 0; j < matrix[0].length; j++) {
      for (int i = 0; i < matrix.length; i++) {
        out.append(matrix[i][j]+"\t");
      }
      out.append('\n');
    }
    return out.toString();
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
