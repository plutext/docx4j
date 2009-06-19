package com.topologi.diffx.sequence;

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
import java.util.Iterator;


import com.topologi.diffx.event.DiffXEvent;
import com.topologi.diffx.format.DiffXFormatter;


/**
 * The slicer takes two sequences and removes the common elements from the beginning 
 * and the end of the chain so that only the smallest sequences are passed to the 
 * DiffXAlgorithmBase.
 * 
 * <p>The slice does modify the original sequences.
 * 
 * <p>Note: Using this class may lead to problems in the execution of the Diff-X
 * algorithm and incorrect results, because it could potentially take off some parts
 * that helps the Diff-X algorithm ensuring that the XML is well-formed.  
 * 
 * @author Christophe Lauret
 * @version 15 December 2004
 */
public final class NaiveSequenceSlicer {
  // FIXME: symmetrical slicing.

// class attributes ---------------------------------------------------------------------------

  /**
   * The first sequence of events to test.
   */
  final EventSequence sequence1;

  /**
   * The second sequence of events to test.
   */
  final EventSequence sequence2;

  /**
   * The common start between the two sequences.
   */
  EventSequence start = null;

  /**
   * The common end between the two sequences.
   */
  EventSequence end = null;

// constructor --------------------------------------------------------------------------------

  /**
   * Creates a new sequence slicer.
   * 
   * @param seq0 The first sequence to slice.
   * @param seq1 The second sequence to slice.
   */
  public NaiveSequenceSlicer(EventSequence seq0, EventSequence seq1) {
    this.sequence1 = seq0;
    this.sequence2 = seq1;
  }

// methods ------------------------------------------------------------------------------------

  /**
   * Slices the start of both sequences.
   * 
   * <p>The common start sequence will be stroed in the class until the next 
   * {@link #formatStart(DiffXFormatter)} is called.
   * 
   * @return The number of common elements at the start of the sequences.
   * 
   * @throws IllegalStateException If the start buffer is not empty.
   */
  public int sliceStart() throws IllegalStateException {
    if (this.start != null)
      throw new IllegalStateException("The start buffer already contains a subsequence.");
    this.start = new EventSequence();
    int count = 0;
    Iterator i = this.sequence1.eventIterator();
    Iterator j = this.sequence2.eventIterator();
    while (i.hasNext() && j.hasNext()) {
      DiffXEvent e = (DiffXEvent)i.next();
      if (j.next().equals(e)) {
        count++;
        i.remove();
        j.remove();
        this.start.addEvent(e);
      } else return count;
    }
    return count;
  }

  /**
   * Slices the end of both sequences.
   * 
   * <p>The common end sequence will be stored in the class until the next 
   * {@link #formatEnd(DiffXFormatter)} is called.
   * 
   * @return The number of common elements at the end of the sequences.
   * 
   * @throws IllegalStateException If the end buffer is not empty.
   */
  public int sliceEnd() throws IllegalStateException {
    if (this.end != null)
      throw new IllegalStateException("The end buffer already contains a subsequence.");
    this.end = new EventSequence();
    int count = 0;
    int pos1 = this.sequence1.size() - 1;
    int pos2 = this.sequence2.size() - 1;
    while (pos1 >= 0 && pos2 >= 0) {
      DiffXEvent e1 = this.sequence1.getEvent(pos1);
      if (e1.equals(this.sequence2.getEvent(pos2))) {
        count++;
        this.sequence1.removeEvent(pos1--);
        this.sequence2.removeEvent(pos2--);
        this.end.addEvent(0, e1);
      } else break;
    }
    return count;
  }

  /**
   * Slices the start of both sequences and formats the start subsequence with the specified
   * formatter.
   * 
   * <p>Implementation note: although this is functionally equivalent to call successively the
   * methods {@link #sliceStart()} and {@link #formatStart(DiffXFormatter)}, this method is
   * optimised and passes the event directly to the formatter without using a buffer. 
   * 
   * @param formatter The formatter that will handle the output. 
   * 
   * @return The number of common elements at the start of the sequences.
   * 
   * @throws IllegalStateException If the start buffer is not empty.
   * @throws NullPointerException If the specified formatter is <code>null</code>.
   * @throws IOException If an error occurs whilst writing with the formatter.
   */
  public int sliceStart(DiffXFormatter formatter) 
     throws IllegalStateException, NullPointerException, IOException {
    if (this.start != null)
      throw new IllegalStateException("The start buffer already contains a subsequence.");
    int count = 0;
    Iterator i = this.sequence1.eventIterator();
    Iterator j = this.sequence2.eventIterator();
    while (i.hasNext() && j.hasNext()) {
      DiffXEvent e = (DiffXEvent)i.next();
      if (j.next().equals(e)) {
        count++;
        i.remove();
        j.remove();
        formatter.format(e);
      } else break;
    }
    return count;
  }

  /**
   * Slices the end of both sequences and formats the start subsequence with the specified
   * formatter.
   * 
   * <p>Implementation note: although this is exactly equivalent to successive calls to the
   * methods {@link #sliceEnd()} and {@link #formatEnd(DiffXFormatter)}. 
   * 
   * @param formatter The formatter that will handle the output.
   * 
   * @return The number of common elements at the end of the sequences.
   * 
   * @throws IllegalStateException If the end buffer is not empty.
   * @throws NullPointerException If the specified formatter is <code>null</code>.
   * @throws IOException If an error occurs whilst writing with the formatter.
   */
  public int sliceEnd(DiffXFormatter formatter) 
     throws IllegalStateException, NullPointerException, IOException {
    int count = sliceEnd();
    formatEnd(formatter);
    return count;
  }

  /**
   * Formats the start subsequence that has been buffered by this class.
   * 
   * <p>This method will clear the buffer, but will do nothing if the start buffer is
   * <code>null</code>.
   * 
   * @param formatter The formatter that will handle the output.
   * 
   * @throws NullPointerException If the specified formatter is <code>null</code>. 
   * @throws IOException If an error occurs whilst writing with the formatter.
   */
  public void formatStart(DiffXFormatter formatter) throws NullPointerException, IOException {
    if (this.start == null) return;
    for (int i = 0; i < start.size(); i++)
      formatter.format(this.start.getEvent(i));
    this.start = null;
  }

  /**
   * Formats the end subsequence that has been buffered by this class.
   * 
   * <p>This method will clear the buffer, but will do nothing if the end buffer is
   * <code>null</code>.
   * 
   * @param formatter The formatter that will handle the output.
   * 
   * @throws NullPointerException If the specified formatter is <code>null</code>.
   * @throws IOException If an error occurs whilst writing with the formatter.
   */
  public void formatEnd(DiffXFormatter formatter) throws NullPointerException, IOException {
    if (this.end == null) return;
    for (int i = 0; i < end.size(); i++)
      formatter.format(this.end.getEvent(i));
    this.end = null;
  }

  /**
   * Returns the current start sequence buffer.
   * 
   * @return The current start sequence buffer or <code>null</code> if none.
   */
  public EventSequence getStart() {
    return this.start;
  }

  /**
   * Returns the current end sequence buffer.
   * 
   * @return The current end sequence buffer or <code>null</code> if none.
   */
  public EventSequence getEnd() {
    return this.end;
  }

}
