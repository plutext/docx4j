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
 * Performs the diff comparison using an optimized version of the linear space algorithm
 * of S.Kiran Kumar and C.Pandu Rangan.
 * 
 * <p>Implementation note: this algorithm effectively detects the correct changes in the
 * sequences, but suffers from two main problems:
 * <ul>
 *   <li>When the events are formatted directly from reading the matrix, the XML is not
 *   necessarily well-formed, this occurs mostly when some elements are swapped, because
 *   the closing tags will not necessarily reported in an order that allows the XML to
 *   be well-formed.<br>
 *   Using the {@link com.topologi.diffx.format.SmartXMLFormatter} helps in solving the
 *   problem as it maintains a stack of the elements that are being written and actually
 *   ignores the name of the closing element, so all the elements are closed properly.
 *   </li>
 * </ul>
 * 
 * <p>For S. Kiran Kumar and C. Pandu Rangan. <i>A linear space algorithm for the LCS problem</i>,
 * Acta Informatica. Volume 24 ,  Issue 3  (June 1987); Copyright Springer-Verlag 1987
 * 
 * <p>This class reuses portions of code originally written by Mikko Koivisto and Tuomo Saarni.
 *
 * <p><a href="http://dblp.uni-trier.de/rec/bibtex/journals/acta/KumarR87">
 * http://dblp.uni-trier.de/rec/bibtex/journals/acta/KumarR87</a>
 * 
 * <p><a href="http://users.utu.fi/~tuiisa/Java/">http://users.utu.fi/~tuiisa/Java/</a>
 * 
 * @author Christophe Lauret
 * @version 9 March 2005
 */
public final class DiffXKumarRangan extends DiffXAlgorithmBase {

  /**
   * Set to <code>true</code> to show debug info.
   */
  private static final boolean DEBUG = false;

  // state variables ----------------------------------------------------------------------------

  // Global integer arrays needed in the computation of the LCS
  private int[] R1, R2;
  private int[] LL, LL1, LL2;

  /**
   * Global integer variable needed in the computation of the LCS.
   */
  private int R;

  /**
   * Global integer variable needed in the computation of the LCS.
   */
  private int S;

  /**
   * A counter for the index of the second sequence when generating the diff.
   */
  private int iSeq2 = 0;

  /**
   * The length of the LCS.
   */
  private int length = -1;

  /**
   * The formatter to use for the write diff function.
   */
  private DiffXFormatter df = null;

  // constructor --------------------------------------------------------------------------------

  /**
   * Creates a new DiffXAlgorithmBase.
   * 
   * @param seq0 The first sequence to compare.
   * @param seq1 The second sequence to compare.
   */
  public DiffXKumarRangan(EventSequence seq0, EventSequence seq1) {
    super(seq0, seq1);
  }

  // methods ------------------------------------------------------------------------------------

  /**
   * Calculates the length of LCS and returns it.
   * 
   * <p>If the length is calculated already it'll not be calculated repeatedly.
   * 
   * <p>This algorithm starts from the length of the first sequence as the maximum possible
   * LCS and reduces the length for every difference with the second sequence.
   *
   * <p>The time complexity is O(n(m-p)) and the space complexity is O(n+m).
   *
   * @return The length of LCS
   */
  public int length() {
    if (this.length < 0) {
      this.length = calculateLength();
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
    this.length = calculateLength();
    this.df = formatter;
    // execute the LCS algorithm for the complete sequences
    generateLCS(0, this.length1 - 1, 0, this.length2 - 1, this.length1, this.length2, this.length);
  }

  // helpers ------------------------------------------------------------------------------------

  /**
   * Initialises the state variables.
   */
  private void init() {
    this.R1 = new int[this.length2+1];
    this.R2 = new int[this.length2+1];
    this.LL = new int[this.length2+1];
    this.LL1 = new int[this.length2+1];
    this.LL2 = new int[this.length2+1];
    this.iSeq2 = 0;
  }

  /**
   * Calculates the LCS length.
   * 
   * @return The LCS length.
   */
  private int calculateLength() {
    init();
    this.R = 0;
    this.S = this.length1 + 1;
    // iterate for every difference with the first sequence
    while (this.S > this.R) {
      this.S--;
      // fill up R2 up to the first difference using the entire sequences
      fillOne(0, this.length1 - 1, 0, this.length2 - 1, this.length1, this.length2, 1);
      // copy the content of R2 to R1 up to R
      copyUpTo(this.R2, this.R1, this.R);
    }
    // both R1 and R2 now contain the indexes(+1) of the first sequence that form the LCS
    if (DEBUG) {
      System.err.println("LCS length="+this.S);
    }
    return this.S;
  }

  /**
   * This is used to find the index from where the longest common subsequence so far can
   * be found.
   *
   * <p>The time complexity is O(n+m) and the space complexity is O(n).
   *
   * @param start1 The start index of the first sequence.
   * @param end1   The last index of the first sequence.
   * @param start2 The start index of the second sequence.
   * @param end2   The last index of the second sequence.
   * @param m      The length of the first sequence.
   * @param n      The length of the second sequence.
   * @param sign   This is used to mark wether to start from the beginning of the string
   *               or from the end of the string.
   */
  private void fillOne(int start1, int end1, int start2, int end2, int m, int n, int sign) {
    int j = 1;
    int i = this.S;
    boolean over = false;
    this.R2[0] = n+1;

    int lower2 = 0;
    int position2 = 0;
    int temp = 0;

    while (i > 0 & !over) {
      if (j > this.R) {
        lower2 = 0;
      } else {
        lower2 = this.R1[j];
      }
      position2 = this.R2[j - 1] - 1;

      // The real index in the global char table is current_index * sign + beginning
      // index of the subchararray
      while (position2 > lower2 && !this.sequence1.getEvent((i - 1) * sign + start1)
          .equals(this.sequence2.getEvent((position2 - 1) * sign + start2))) {
        position2--;
      }
      temp = Math.max(position2, lower2);
      if (temp == 0) {
        over = true;
      } else {
        this.R2[j] = temp;
        i--;
        j++;
      }
    }
    this.R = j - 1;
  }

  /**
   * Uses integer arrays to keep track where the longest common subsequence that can be found.
   *
   * <p>The time complexity is O(n(waste+1)) and the space complexity is O(n+m).
   *
   * @param start1 The start index of the first sequence.
   * @param end1   The last index of the first sequence.
   * @param start2 The start index of the second sequence.
   * @param end2   The last index of the second sequence.
   * @param m      The length of the first sequence.
   * @param n      The length of the second sequence.
   * @param sign   This is used to mark wether to start from the beginning of the string
   *               or from the end of the string.
   * @param waste  The length of characters not included in the LCS between indexes start1 and end1.
   *               Similarly between indexes start2 and end2.
   *
   * @return       Integer array consisting of the ???.
   */
  private int[] calMid(int start1, int end1, int start2, int end2, int m, int n, int sign, int waste) {
    this.LL = new int[n+1];
    this.R = 0;
    for (this.S = m; this.S >= m - waste; this.S--) {
      fillOne(start1, end1, start2, end2, m, n, sign);
      copyUpTo(this.R2, this.R1, this.R);
    }
    copyUpTo(this.R1, this.LL, this.R);
    return this.LL;
  }

  /**
   * Computes the LCS and returns it in character array.
   *
   * The time complexity is O(n(m-p)) and the space complexity is O(n+m).
   *
   * @param start1 The start index of the first sequence.
   * @param end1   The last index of the first sequence.
   * @param start2 The start index of the first sequence.
   * @param end2   The last index of the first sequence.
   * @param m      The length of the first sequence.
   * @param n      The length of the second sequence.
   * @param lcs    The length of LCS between indexes start1 and end1.
   *                Similarly between indexes b_start and b-loppu. (???)
   * 
   * @throws IOException If thrown by the formatter
   */
  private void generateLCS(int start1, int end1, int start2, int end2, int m, int n, int lcs)
      throws IOException {

    // Solves the base case, waste is less than 2 characters
    if (m - lcs < 2) {
      getLCSMinimumWaste(start1, end1, start2, end2, m, n, lcs);

      // Waste is more than 1 character, process recursively
    } else {
      getLCSMoreWaste(start1, end1, start2, end2, m, n, lcs);

    }
  }

  /**
   * Computes the longest common subsequence for the specified boundaries when the waste
   * is (strictly) less than 2 events.
   *
   * <p>This method is iterative; NOT recursive.
   *
   * @param start1 The start 0-based index of the first sequence.
   * @param end1   The last 0-based index of the first sequence.
   * @param start2 The start 0-based index of the second sequence.
   * @param end2   The last 0-based index of the second sequence.
   * @param m      The length of the first sequence.
   * @param n      The length of the second sequence.
   * @param lcs    The length of LCS between indexes start1 and end1.
   * 
   * @throws IOException If thrown by the formatter.
   */
  private void getLCSMinimumWaste(int start1, int end1, int start2, int end2, int m, int n, int lcs)
      throws IOException {
    // number of diffs with the first subsequence
    int waste = m - lcs;

    // contains the relative 1-based index of the event in the second sequence in reverse order
    this.LL = calMid(start1, end1, start2, end2, m, n, 1, waste);
    if (DEBUG) {
      System.err.println("SEQ1={"+start1+" -> "+end1+"} SEQ2={"+start2+" -> "+end2+"}");
    }
    if (DEBUG) {
      printState(0x10000);
    }
    int i = 0;

    // start in order for the first subsequence
    // and get the index of the second subsequence
    while (i < lcs && this.sequence1.getEvent(i + start1)
        .equals(this.sequence2.getEvent(this.LL[lcs - i] - 1 + start2))) {
      this.df.format(this.sequence1.getEvent(i + start1));
      this.iSeq2++;
      // removed events from the second subsequence
      i++;
      if (i < lcs) {
        writeDeleted(this.LL[lcs - i] - 1 + start2);
      }
    }

    // possibly an event from the first subsequence to insert
    if (i < m) {
      this.df.insert(this.sequence1.getEvent(i + start1));
    }

    // we should take care of the removed events from the second subsequence now (?)
    if (i < lcs) {
      writeDeleted(this.LL[lcs - i] - 1 + start2);
    }
    i++;

    // the second part of the first subsequence

    while (i < m) {
      this.df.format(this.sequence1.getEvent(i + start1));
      this.iSeq2++;
      writeDeleted(this.LL[m - i] - 1 + start2);
      i++;
    }

    // finish writing the missing events from the second subsequence
    writeDeleted(this.LL[0] - 1 + start2);
  }

  /**
   * Computes the longest common subsequence for the specified boundaries when the waste
   * is more than 1 character.
   *
   * <p>This method is recursive and will process ech subsequence with the LCS algorithm.
   *
   * @param start1 The start 0-based index of the first sequence.
   * @param end1   The last 0-based index of the first sequence.
   * @param start2 The start 0-based index of the second sequence.
   * @param end2   The last 0-based index of the second sequence.
   * @param m      The length of the first sequence.
   * @param n      The length of the second sequence.
   * @param lcs    The length of LCS between indexes start1 and end1.
   * 
   * @throws IOException If thrown by the formatter.
   */
  private void getLCSMoreWaste(int start1, int end1, int start2, int end2, int m, int n, int lcs)
      throws IOException {
    // The indexes of the perfect cut
    int u, v;

    int r1, r2;

    int waste1 = (int)Math.ceil((m - lcs) / 2.0f);
    this.LL1 = calMid(end1, start1, end2, start2, m, n, -1, waste1);

    // Saves the value changed in calmid from global variable R to variable r1
    r1 = this.R;
    for (int j = 0; j <= r1; j++) {
      this.LL1[j] = n + 1 - this.LL1[j];
    }

    int waste2 = (int)Math.floor((m - lcs) / 2.0f);
    this.LL2 = calMid(start1, end1, start2, end2, m, n, 1, waste2);

    // Saves the value changed in calmid from global variable R to variable r2
    r2 = this.R;

    int k = Math.max(r1, r2);

    while (k > 0) {
      if (k <= r1 && lcs - k <= r2 && this.LL1[k] < this.LL2[lcs - k]) {
        break;
      } else {
        k--;
      }
    }

    u = k + waste1;
    v = this.LL1[k];

    // recursively call the LCS method to process the two subsequences
    generateLCS(start1, start1 + u - 1, start2, start2 + v - 1, u - 1+1, v - 1+1, u - waste1);
    generateLCS(start1 + u, end1, start2 + v, end2, end1 - start1 + 1 - u, end2 - start2 + 1 - v, m - u - waste2);
  }

  /**
   * Write the deleted events to the formatter.
   * 
   * @param jSeq2 The index of the LL array for the next event of the second sequence.
   * 
   * @throws IOException If thrown by the formatter.
   */
  private void writeDeleted(int jSeq2) throws IOException {
    //    if (DEBUG) System.err.println("next="+jSeq2+" current="+iSeq2);
    while (jSeq2 > this.iSeq2) {
      this.df.delete(this.sequence2.getEvent(this.iSeq2++));
    }
  }

  /**
   * Prints the state of this object, that is the values of all of the state variables to
   * <code>System.err</code>. This is for debugging purposes only.
   * 
   * @param f The flags
   */
  private void printState(int f) {
    if ((f & 0x0000001) == 0x0000001) {
      System.err.println("  R="+this.R);
    }
    if ((f & 0x0000010) == 0x0000010) {
      System.err.println("  S="+this.S);
    }
    if ((f & 0x0000011) > 0) {
      System.err.println();
    }
    // The arrays for R1 and R2
    if ((f & 0x0000100) == 0x0000100) {
      System.err.print(" R1={");
      for (int element : this.R1) {
        System.err.print(" "+element);
      }
      System.err.println(" }");
    }
    if ((f & 0x0001000) == 0x0001000) {
      System.err.print(" R2={");
      for (int element : this.R2) {
        System.err.print(" "+element);
      }
      System.err.println(" }");
    }
    if ((f & 0x0010000) == 0x0010000) {
      System.err.print(" LL={");
      for (int element : this.LL) {
        System.err.print(" "+element);
      }
      System.err.println(" }");
    }
    if ((f & 0x0100000) == 0x0100000) {
      System.err.print(" LL1={");
      for (int element : this.LL1) {
        System.err.print(" "+element);
      }
      System.err.println(" }");
    }
    if ((f & 0x1000000) == 0x1000000) {
      System.err.print(" LL2={");
      for (int element : this.LL2) {
        System.err.print(" "+element);
      }
      System.err.println(" }");
    }
  }

  // static helpers -----------------------------------------------------------------------------

  /**
   * Copies the first array into the second one up to the specified index (included).
   * 
   * @param a   The first array.
   * @param b   The second array.
   * @param len The 0-based index of the last copied value.
   */
  private static void copyUpTo(int[] a, int[] b, int len) {
    for (int i = 0; i <= len; i++) {
      b[i] = a[i];
    }
  }

}
