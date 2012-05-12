package com.topologi.diffx;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.topologi.diffx.algorithm.DiffXAlgorithm;
import com.topologi.diffx.algorithm.DiffXFitopsy;
import com.topologi.diffx.config.DiffXConfig;
import com.topologi.diffx.format.SmartXMLFormatter;
import com.topologi.diffx.load.Recorder;
import com.topologi.diffx.load.SAXRecorder;
import com.topologi.diffx.load.DOMRecorder;
import com.topologi.diffx.sequence.EventSequence;
import com.topologi.diffx.sequence.SequenceSlicer;

/**
 * Utility class to centralise the access to this API from the command line.
 * 
 * @author Christophe Lauret
 * @version 26 April 2005
 */
public final class Main {

  /**
   * Prevents creation of instances.
   */
  private Main() {
  }

// equivalent methods -------------------------------------------------------------------  
  
  /**
   * Returns <code>true</code> if the two specified files are XML equivalent by looking at the
   * sequance SAX events reported an XML reader.
   * 
   * @param xml1 The first XML stream to compare.
   * @param xml2 The first XML stream to compare.
   * 
   * @return <code>true</code> If the XML are considered equivalent;
   *         <code>false</code> otherwise.
   * 
   * @throws DiffXException Should a Diff-X exception occur.
   * @throws IOException    Should an I/O exception occur.
   */
  public static boolean equivalent(File xml1, File xml2) 
      throws DiffXException, IOException {
    Recorder recorder = new SAXRecorder();
    EventSequence seq0 = recorder.process(xml1);
    EventSequence seq1 = recorder.process(xml2);
    return seq0.equals(seq1);
  }

  /**
   * Returns <code>true</code> if the two specified inputstream are equivalent by looking at the
   * sequance SAX events reported an XML reader.
   *
   * @param xml1 The first XML stream to compare.
   * @param xml2 The first XML stream to compare.
   *   
   * @return <code>true</code> If the XML are considered equivalent;
   *         <code>false</code> otherwise.
   * 
   * @throws DiffXException Should a Diff-X exception occur.
   * @throws IOException    Should an I/O exception occur.
   */
  public static boolean equivalent(InputStream xml1, InputStream xml2)
      throws DiffXException, IOException {
    SAXRecorder recorder = new SAXRecorder();
    EventSequence seq0 = recorder.process(new InputSource(xml1));
    EventSequence seq1 = recorder.process(new InputSource(xml2));
    return seq0.equals(seq1);
  }

  /**
   * Returns <code>true</code> if the two specified readers are equivalent by looking at the
   * sequance SAX events reported an XML reader.
   *
   * @param xml1 The first XML stream to compare.
   * @param xml2 The first XML stream to compare.
   *  
   * @return <code>true</code> If the XML are considered equivalent;
   *         <code>false</code> otherwise.
   * 
   * @throws DiffXException If a DiffX exception is reported by the recorders.
   * @throws IOException    Should an I/O exception occur.
   */
  public static boolean equivalent(Reader xml1, Reader xml2)
      throws DiffXException, IOException {
    SAXRecorder recorder = new SAXRecorder();
    EventSequence seq0 = recorder.process(new InputSource(xml1));
    EventSequence seq1 = recorder.process(new InputSource(xml2));
    return seq0.equals(seq1);
  }

// diff methods -------------------------------------------------------------------------

  /**
   * Compares the two specified xml files and prints the diff onto the given writer. 
   *
   * @param xml1   The first XML node to compare.
   * @param xml2   The first XML node to compare.
   * @param out    Where the output goes.
   * @param config The DiffX configuration to use.
   * 
   * @throws DiffXException Should a Diff-X exception occur.
   * @throws IOException    Should an I/O exception occur.
   */
  public static void diff(Node xml1, Node xml2, Writer out, DiffXConfig config)
      throws DiffXException, IOException {
    // records the events from the XML
    DOMRecorder loader = new DOMRecorder();
    if (config != null) loader.setConfig(config);
    EventSequence seq1 = loader.process(xml1);
    EventSequence seq2 = loader.process(xml2);
    // start slicing
    diff(seq1, seq2, out, config);
  }

  /**
   * Compares the two specified xml files and prints the diff onto the given writer. 
   *
   * @param xml1   The first XML reader to compare.
   * @param xml2   The first XML reader to compare.
   * @param out    Where the output goes.
   * @param config The DiffX configuration to use.
   * 
   * @throws DiffXException Should a Diff-X exception occur.
   * @throws IOException    Should an I/O exception occur.
   */
  public static void diff(Reader xml1, Reader xml2, Writer out, DiffXConfig config)
      throws DiffXException, IOException {
    // records the events from the XML
    SAXRecorder recorder = new SAXRecorder();
    if (config != null) recorder.setConfig(config);
    EventSequence seq1 = recorder.process(new InputSource(xml1));
    EventSequence seq2 = recorder.process(new InputSource(xml2));
    // start slicing
    diff(seq1, seq2, out, config);
  }
  
  // NB: The signatures which takes Reader objects appear to be broken!!

  /**
   * Compares the two specified xml files and prints the diff onto the given writer. 
   *
   * @param xml1 The first XML reader to compare.
   * @param xml2 The first XML reader to compare.
   * @param out  Where the output goes
   *   
   * @throws DiffXException Should a Diff-X exception occur.
   * @throws IOException    Should an I/O exception occur.
   */
  public static void diff(Reader xml1, Reader xml2, Writer out)
      throws DiffXException, IOException {
    // records the events from the XML
    SAXRecorder recorder = new SAXRecorder();
    EventSequence seq1 = recorder.process(new InputSource(xml1));
    EventSequence seq2 = recorder.process(new InputSource(xml2));
    // start slicing
    diff(seq1, seq2, out, new DiffXConfig());
  }

  /**
   * Compares the two specified xml files and prints the diff onto the given writer. 
   *
   * @param xml1 The first XML input stream to compare.
   * @param xml2 The first XML input stream to compare.
   * @param out  Where the output goes
   * 
   * @throws DiffXException Should a Diff-X exception occur.
   * @throws IOException    Should an I/O exception occur.
   */
  public static void diff(InputStream xml1, InputStream xml2, OutputStream out)
      throws DiffXException, IOException {
    // records the events from the XML
    SAXRecorder recorder = new SAXRecorder();
    EventSequence seq1 = recorder.process(new InputSource(xml1));
    EventSequence seq2 = recorder.process(new InputSource(xml2));
    diff(seq1, seq2, new OutputStreamWriter(out), new DiffXConfig());
  }

  /**
   * Compares the two specified xml files and prints the diff onto the given writer. 
   *
   * @param seq1   The first XML reader to compare.
   * @param seq2   The first XML reader to compare.
   * @param out    Where the output goes.
   * @param config The DiffX configuration to use.
   * 
   * @throws DiffXException Should a Diff-X exception occur.
   * @throws IOException    Should an I/O exception occur.
   */
  private static void diff(EventSequence seq1, EventSequence seq2, Writer out, DiffXConfig config)
      throws DiffXException, IOException {
    SmartXMLFormatter formatter = new SmartXMLFormatter(out);
    if (config != null) formatter.setConfig(config);
    
    diff(seq1, seq2, formatter, config);
  }

  public static void diff(EventSequence seq1, EventSequence seq2, SmartXMLFormatter formatter , DiffXConfig config)
  	throws DiffXException, IOException {
	    formatter.declarePrefixMapping(seq1.getPrefixMapping());
	    formatter.declarePrefixMapping(seq2.getPrefixMapping());
	    
	    if (config != null) formatter.setConfig(config);
	    SequenceSlicer slicer = new SequenceSlicer(seq1, seq2);
	    slicer.slice();
	    slicer.formatStart(formatter);
	    DiffXAlgorithm df = new DiffXFitopsy(seq1, seq2);
	    df.process(formatter);
	    slicer.formatEnd(formatter);
  }
  
// command line ------------------------------------------------------------------------- 

  /**
   * Main entry point from the command line. 
   * 
   * @param args The command-line arguments
   * 
   * @throws Exception If anything wrong happens.
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 2) usage();
    try {
      File xml1 = new File(args[0]);
      File xml2 = new File(args[1]);
      diff(new FileInputStream(xml1), new FileInputStream(xml2), System.out);
    } catch (Throwable ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Displays the usage on the System.err console
   */
  public static void usage() {
    System.err.println("Compare the SAX events returned by two XML files.");
    System.err.println("usage:");
    System.err.println("  Main [xml_file] [xml_file]");
    System.err.println("where:");
    System.err.println("  xml_file = Path to an XML file");
    System.exit(1);
  }

}
