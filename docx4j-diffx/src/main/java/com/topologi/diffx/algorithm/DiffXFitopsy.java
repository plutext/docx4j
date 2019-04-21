/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.algorithm;

import java.io.IOException;

import com.topologi.diffx.event.AttributeEvent;
import com.topologi.diffx.event.DiffXEvent;
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
 * @version 8 April 2005
 */
public final class DiffXFitopsy extends DiffXAlgorithmBase {

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
  public DiffXFitopsy(EventSequence seq0, EventSequence seq1) {
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
    if (this.length1 == 0 || this.length2 == 0) {
      this.length = 0;
    }
    // normal case
    if (this.length < 0) {
      this.matrix.setup(this.length1+1, this.length2+1);
      // allocate storage for array L;
      for (int i = super.length1; i >= 0; i--) {
        for (int j = super.length2; j >= 0; j--) {
          // we reach the end of the sequence (fill with 0)
          if (i >= super.length1 || j >= super.length2) {
            this.matrix.set(i, j, 0);
          } else {
            // the events are the same
            if (this.sequence1.getEvent(i).equals(this.sequence2.getEvent(j))) {
              this.matrix.incrementPathBy(i, j, 1);
              // different events
            } else {
              this.matrix.incrementByMaxPath(i, j);
            }
          }
        }
      }
      this.length = this.matrix.get(0, 0);
    }
    if (DEBUG) {
      System.err.println();
      for (int i = 0; i < this.sequence1.size(); i++) {
        System.err.print(ShortStringFormatter.toShortString(this.sequence1.getEvent(i))+"\t");
      }
      System.err.println();
      for (int i = 0; i < this.sequence2.size(); i++) {
        System.err.print(ShortStringFormatter.toShortString(this.sequence2.getEvent(i))+"\n");
      }
      System.err.println();
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
    DiffXEvent e1 = this.sequence1.getEvent(i);
    DiffXEvent e2 = this.sequence2.getEvent(j);
    // start walking the matrix
    while (i < super.length1 && j < super.length2) {
      e1 = this.sequence1.getEvent(i);
      e2 = this.sequence2.getEvent(j);
      // we can only insert or delete, priority to insert
      if (this.matrix.isGreaterX(i, j)) {
        // follow the natural path and insert
        if (this.estate.okInsert(e1)) {
          if (DEBUG) {
            System.err.print(" >i +"+ShortStringFormatter.toShortString(e1));
          }
          formatter.insert(e1);
          this.estate.insert(e1);
          i++;

          // if we can format checking at the stack, let's do it
        } else if (e1.equals(e2) && this.estate.okFormat(e1)) {
          if (DEBUG) {
            System.err.print(" <f "+ShortStringFormatter.toShortString(e1));
          }
          formatter.format(e1);
          this.estate.format(e1);
          i++; j++;

          // go counter current and delete
        } else if (this.estate.okDelete(e2)) {
          if (DEBUG) {
            System.err.print(" >d -"+ShortStringFormatter.toShortString(e2));
          }
          formatter.delete(e2);
          this.estate.delete(e2);
          j++;

        } else {
          if (DEBUG) {
            System.err.print("\n(i) case greater X");
          }
          if (DEBUG) {
            printLost(i, j);
          }
          break;
        }

        // we can only insert or delete, priority to delete
      } else if (this.matrix.isGreaterY(i, j)) {
        // follow the natural and delete
        if (this.estate.okDelete(e2)) {
          if (DEBUG) {
            System.err.print(" <d -"+ShortStringFormatter.toShortString(e2));
          }
          formatter.delete(e2);
          this.estate.delete(e2);
          j++;

          // if we can format checking at the stack, let's do it
        } else if (e1.equals(e2) && this.estate.okFormat(e1)) {
          if (DEBUG) {
            System.err.print(" <f "+ShortStringFormatter.toShortString(e1));
          }
          formatter.format(e1);
          this.estate.format(e1);
          i++; j++;

          // insert (counter-current)
        } else if (this.estate.okInsert(e1)) {
          if (DEBUG) {
            System.err.print(" <i +"+ShortStringFormatter.toShortString(e2));
          }
          formatter.insert(e1);
          this.estate.insert(e1);
          i++;

        } else {
          if (DEBUG) {
            System.err.println("\n(i) case greater Y");
          }
          if (DEBUG) {
            printLost(i, j);
          }
          break;
        }

        // elements from i inserted and j deleted
        // we have to make a choice for where we are going
      } else if (this.matrix.isSameXY(i, j)) {
        // if we can format checking at the stack, let's do it
        if (e1.equals(e2) && this.estate.okFormat(e1)) {
          if (DEBUG) {
            System.err.print(" =f "+ShortStringFormatter.toShortString(e1));
          }
          formatter.format(e1);
          this.estate.format(e1);
          i++; j++;

          // we can insert the closing tag
        } else if (this.estate.okInsert(e1)
            && !(e2 instanceof AttributeEvent && !(e1 instanceof AttributeEvent))) {
          if (DEBUG) {
            System.err.print(" =i +"+ShortStringFormatter.toShortString(e1));
          }
          this.estate.insert(e1);
          formatter.insert(e1);
          i++;

          // we can delete the closing tag
        } else if (this.estate.okDelete(e2)
            && !(e1 instanceof AttributeEvent && !(e2 instanceof AttributeEvent))) {
          if (DEBUG) {
            System.err.print(" =d -"+ShortStringFormatter.toShortString(e2));
          }
          formatter.delete(e2);
          this.estate.delete(e2);
          j++;

        } else {
          if (DEBUG) {
            System.err.println("\n(i) case same");
          }
          if (DEBUG) {
            printLost(i, j);
          }
          break;
        }
      } else {
        if (DEBUG) {
          System.err.println("\n(i) case ???");
        }
        if (DEBUG) {
          printLost(i, j);
        }
        break;
      }
      if (DEBUG) {
        System.err.println("    stack="+this.estate.currentChange()+ShortStringFormatter.toShortString(this.estate.current()));
      }
    }

    // finish off the events from the first sequence
    while (i < super.length1) {
      this.estate.insert(this.sequence1.getEvent(i));
      formatter.insert(this.sequence1.getEvent(i));
      i++;
    }
    // finish off the events from the second sequence
    while (j < super.length2) {
      this.estate.delete(this.sequence2.getEvent(j));
      formatter.delete(this.sequence2.getEvent(j));
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
    if (this.length1 == 0) {
      for (int i = 0; i < this.length2; i++) {
        formatter.delete(this.sequence2.getEvent(i));
      }
    }
    // the second sequence is empty, events from the first sequence have been inserted
    if (this.length2 == 0) {
      for (int i = 0; i < this.length1; i++) {
        formatter.insert(this.sequence1.getEvent(i));
      }
    }
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
    for (int i = 0; i < s1.size(); i++) {
      max += s1.getEvent(i).getWeight();
    }
    for (int i = 0; i < s2.size(); i++) {
      max += s2.getEvent(i).getWeight();
    }
    if (max > Short.MAX_VALUE)
      return new MatrixInt();
    else
      return new MatrixShort();
  }

  /**
   * Print information when the algorithm gets lost in the matrix,
   * ie when it does not know which direction to follow.
   * 
   * @param i The X position.
   * @param j The Y position.
   */
  private void printLost(int i, int j) {
    DiffXEvent e1 = this.sequence1.getEvent(i);
    DiffXEvent e2 = this.sequence2.getEvent(j);
    System.err.println("(!) Ambiguous choice in ("+i+","+j+")");
    System.err.println(" ? +"+ShortStringFormatter.toShortString(e1));
    System.err.println(" ? -"+ShortStringFormatter.toShortString(e2));
    System.err.println(" current="+ShortStringFormatter.toShortString(this.estate.current()));
    System.err.println(" value in X+1="+this.matrix.get(i+1, j));
    System.err.println(" value in Y+1="+this.matrix.get(i, j+1));
    System.err.println(" equals="+e1.equals(e2));
    System.err.println(" greaterX="+this.matrix.isGreaterX(i, j));
    System.err.println(" greaterY="+this.matrix.isGreaterY(i, j));
    System.err.println(" sameXY="+this.matrix.isSameXY(i, j));
    System.err.println(" okFormat1="+this.estate.okFormat(e1));
    System.err.println(" okFormat2="+this.estate.okFormat(e2));
    System.err.println(" okInsert="+this.estate.okInsert(e1));
    System.err.println(" okDelete="+this.estate.okDelete(e2));

  }

}
