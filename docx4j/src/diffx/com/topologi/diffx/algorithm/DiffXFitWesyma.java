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

import java.io.IOException;

import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.event.AttributeEvent;
import com.topologi.diffx.format.DiffXFormatter;
import com.topologi.diffx.format.ShortStringFormatter;
import com.topologi.diffx.sequence.EventSequence;

/**
 * Performs the diff comparison using the LCS algorithm.
 * 
 * <p>Implementation note: this algorithm effectively detects the correct changes in the
 * sequences, but will not necessarily return events that can be serialised as well-formed
 * XML as they stand.
 * 
 * <p>Known problem in this implementation: elements that contain themselves tend to 
 * generate events that are harder to serialise as XML. 
 * 
 * <p>This class is said 'fit' because it will adapt the matrix to the sequences that it 
 * is being given in order to improve performance.
 * 
 * <p>Note: The name of this class comes from a contracted version of the features of
 * this algorithm, as explained below:
 * <ul>
 *   <li><b>We</b>ighted, each token is has a given weight;</li>
 *   <li><b>Sy</b>mmetrical, when possible, the algorithm will try to choose a path
 *      that is symmetrical in regards to the arrangement of the tokens;</li>
 *   <li><b>Ma</b>trix, this class uses a matrix for its internal representation;</li>
 *   </li>
 * </ul>
 * 
 * <p>This class is not synchronised.
 * 
 * @author Christophe Lauret
 * @version 7 April 2005
 */
public final class DiffXFitWesyma extends DiffXAlgorithmBase {

  /**
   * Set to <code>true</code> to show debug info.
   */
  private static final boolean DEBUG = false;

// state variables ----------------------------------------------------------------------------

  /**
   * Matrix storing the paths.
   */
  private transient Matrix matrix;

  /**
   * The state of the elements.
   */
  private transient ElementState estate = new ElementState();

// constructor --------------------------------------------------------------------------------

  /**
   * Creates a new DiffXAlgorithmBase.
   * 
   * @param seq0 The first sequence to compare.
   * @param seq1 The second sequence to compare.
   */
  public DiffXFitWesyma(EventSequence seq0, EventSequence seq1) {
    super(seq0, seq1);
    this.matrix = setupMatrix(seq0, seq1);
  }

// methods ------------------------------------------------------------------------------------

  /**
   * Returns the length of the longest common sequence.
   * 
   * @return the length of the longest common sequence.
   */
  public int length() {
    // case when one of the sequences is empty
    if (length1 == 0 || length2 == 0)
      this.length = 0;
    // normal case
    if (length < 0) {
      matrix.setup(length1+1, length2+1);
      // allocate storage for array L;
      for (int i = super.length1; i >= 0; i--) {
        for (int j = super.length2; j >= 0; j--) {
          // we reach the end of the sequence (fill with 0)
          if (i >= super.length1 || j >= super.length2) 
            matrix.set(i, j, 0);
          else {
            // the events are the same
            if (sequence1.getEvent(i).equals(sequence2.getEvent(j))) {
              matrix.incrementPathBy(i, j, maxWeight(sequence1.getEvent(i), sequence2.getEvent(j)));
            // different events
            } else 
              matrix.incrementByMaxPath(i, j);
          }
        }
      }
      this.length = this.matrix.get(0, 0);
    }
    if (DEBUG) {
      System.err.println();
      for (int i = 0; i < this.sequence1.size(); i++) 
        System.err.print(ShortStringFormatter.toShortString(this.sequence1.getEvent(i))+"\t");
      System.err.println();
      for (int i = 0; i < this.sequence2.size(); i++) 
        System.err.print(ShortStringFormatter.toShortString(this.sequence2.getEvent(i))+"\t");
      System.err.println(this.matrix);
    }
    return this.length;
  }

  /**
   * Writes the diff sequence using the specified formatter.
   * 
   * @param formatter The formatter that will handle the output.
   *
   * @throws IOException If thrown by the formatter.
   */
  public void process(DiffXFormatter formatter) throws IOException {
    // handle the case when one of the two sequences is empty
    processEmpty(formatter);
    if (this.length1 == 0 || this.length2 == 0) return;
    // calculate the LCS length to fill the matrix
    length();
    int i = 0;
    int j = 0;
    DiffXEvent e1 = sequence1.getEvent(i);
    DiffXEvent e2 = sequence2.getEvent(j);
    // start walking the matrix
    while (i < super.length1 && j < super.length2) {
      e1 = sequence1.getEvent(i);
      e2 = sequence2.getEvent(j);

      // both elements are considered equal
      if (e1.equals(e2)) {
        formatter.format(e1);
        estate.format(e1);
        i++; j++;

      // element from i inserted
      } else if (matrix.isGreaterX(i, j)) {
        formatter.insert(e1);
        estate.insert(e1);
        i++;

      // element from j deleted
      } else if (matrix.isGreaterY(i, j)) {
        formatter.delete(e2);
        estate.delete(e2);
        j++;

      // elements from i inserted and j deleted
      // we have to make a choice for where we are going
      // by default, we give priority to the insert and then
      // determine which path to follow
      } else if (matrix.isSameXY(i, j)) {
        // choose whether to insert or delete first
        boolean priorityInsert = true;
        // using the open / close element
        if (estate.matchCurrent(e1))
          priorityInsert = true;
        if (estate.matchCurrent(e2))
          priorityInsert = false;          
        // give priority to attributes
        if (e1 instanceof AttributeEvent && !(e2 instanceof AttributeEvent)) {
          priorityInsert = true;
        } else if (e2 instanceof AttributeEvent && !(e1 instanceof AttributeEvent)) {
          priorityInsert  = false;
        }
        // apply priority
        if (priorityInsert) {
          estate.insert(e1);
          formatter.insert(e1);
          i++;
        } else {
          estate.delete(e1);
          formatter.delete(e2);
          j++;
        }
      }
    }
    // finish off the events from the first sequence
    while (i < super.length1) {
      estate.insert(sequence1.getEvent(i));
      formatter.insert(sequence1.getEvent(i));
      i++;
    }
    // finish off the events from the second sequence
    while (j < super.length2) {
      estate.delete(sequence2.getEvent(j));
      formatter.delete(sequence2.getEvent(j));
      j++;
    }
    // free some resources
//    matrix.release();
  }

// private helpers (probably inlined by the compiler) -----------------------------------

  /**
   * Writes the diff sequence using the specified formatter when one of 
   * the sequences is empty.
   * 
   * <p>The result becomes either only insertions (when the second sequence is
   * empty) or deletions (when the first sequence is empty).
   * 
   * @param formatter The formatter that will handle the output.
   *
   * @throws IOException If thrown by the formatter.
   */
  private void processEmpty(DiffXFormatter formatter) throws IOException {
    // the first sequence is empty, events from the second sequence have been deleted 
    if (this.length1 == 0)
      for (int i = 0; i < this.length2; i++)
        formatter.delete(sequence2.getEvent(i));
    // the second sequence is empty, events from the first sequence have been inserted
    if (this.length2 == 0)
      for (int i = 0; i < this.length1; i++)
        formatter.insert(sequence1.getEvent(i));
  }

  /**
   * Determines the most appropriate matrix to use. 
   *
   * <p>Calculates the maximum length of the shortest weighted path if both sequences
   * are totally different, which corresponds to the sum of all the events.
   *
   * @param s1 The first sequence.
   * @param s2 The second sequence.
   * 
   * @return The most appropriate matrix.
   */
  private static Matrix setupMatrix(EventSequence s1, EventSequence s2) {
    int max = 0;
    for (int i = 0; i < s1.size(); i++) max += s1.getEvent(i).getWeight();
    for (int i = 0; i < s2.size(); i++) max += s2.getEvent(i).getWeight();
    if (max > Short.MAX_VALUE)
      return new MatrixInt();
    else
      return new MatrixShort();
  }

  /**
   * Returns the max weight of the two events. 
   * 
   * @param e1 The first event.
   * @param e2 The second event.
   * 
   * @return The weight for the event.
   */
  private int maxWeight(DiffXEvent e1, DiffXEvent e2) {
    return (e1.getWeight() > e2.getWeight())? e1.getWeight() : e2.getWeight();
  }

}
