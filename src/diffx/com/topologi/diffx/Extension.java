package com.topologi.diffx;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.docx4j.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.topologi.diffx.algorithm.DiffXAlgorithm;
import com.topologi.diffx.algorithm.GuanoAlgorithm;
import com.topologi.diffx.config.DiffXConfig;
import com.topologi.diffx.config.TextGranularity;
import com.topologi.diffx.config.WhiteSpaceProcessing;
import com.topologi.diffx.format.SafeXMLFormatter;
import com.topologi.diffx.load.DOMRecorder;
import com.topologi.diffx.sequence.EventSequence;
import com.topologi.diffx.sequence.SequenceSlicer;

/**
 * To use Diff-X as an XSLT extension.
 * 
 * <p>In Saxon, declare the namespace as:
 * <pre>{@code
 * <xsl:stylesheet version="2.0"
 *    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 *    xmlns:diffx="com.topologi.diffx.Extension"
 *    extension-element-prefixes="diffx"
 * >
 * }</pre>
 *
 * <p>Diff-X can be called within XSLT with:
 * <pre>{@code
 * <xsl:copy-of select="diffx:diff(/node1/to/compare, /node2/to/compare, 'IGNORE', 'TEXT')"/>
 * }</pre>
 * 
 * Note: the method signatures requires DOM arguments, include the <code>Saxon-DOM</code> jar
 * on your classpath to use this extension function with Saxon.
 * 
 * @author Christophe Lauret
 * @version 18 May 2010
 */
public final class Extension {

  /**
   * Maps the DOM builder factory to use with the given DOM package.
   * 
   * <p>This is because some XSLT processors will only accept certain types DOM objects.
   */
  private static final Map<String, String> BUILDERS = new Hashtable<String, String>();
  static {
//    BUILDERS.put("net.sf.saxon.dom", "net.sf.saxon.dom.DocumentBuilderFactoryImpl");
  }

  /**
   * Compares the two specified <code>Node</code>s and returns the diff as a node.
   *
   * <p>Only the first node in the node list is sequenced.
   *
   * @param xml1        The first XML node to compare.
   * @param xml2        The second XML node to compare.
   * @param whitespace  The white space processing (a valid {@link WhiteSpaceProcessing} value).
   * @param granularity The text granularity (a valid {@link TextGranularity} value).
   * 
   * @throws DiffXException Should a Diff-X exception occur.
   * @throws IOException    Should an I/O exception occur.
   */
  public static Node diff(Node xml1, Node xml2, String whitespace, String granularity)
      throws DiffXException, IOException {

    // Get the config
    DiffXConfig config = toConfig(whitespace, granularity);

    // Get Sequences
    DOMRecorder loader = new DOMRecorder();
    loader.setConfig(config);
    EventSequence seq1 = loader.process(xml1);
    EventSequence seq2 = loader.process(xml2);
    if (seq1.size() == 0 && seq1.size() == 0) return null;

    // Start comparing
    StringWriter out = new StringWriter();
    diff(seq1, seq2, out, config);

    // Return a node
    try {
      String factory = getFactoryClass(xml1, xml2);
      Node node = toNode(out.toString(), config, factory);
      return node;
    } catch (Exception ex) {
      throw new DiffXException("Could not generate Node from Diff-X result", ex);
    }
  }

  // private helpers ------------------------------------------------------------------------------

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
    SafeXMLFormatter formatter = new SafeXMLFormatter(out);
    formatter.declarePrefixMapping(seq1.getPrefixMapping());
    formatter.declarePrefixMapping(seq2.getPrefixMapping());
    if (config != null) {
      formatter.setConfig(config);
    }
    SequenceSlicer slicer = new SequenceSlicer(seq1, seq2);
    slicer.slice();
    slicer.formatStart(formatter);
    DiffXAlgorithm df = new GuanoAlgorithm(seq1, seq2);
    df.process(formatter);
    slicer.formatEnd(formatter);
  }

  /**
   * Returns the Diff-X config for the specified argument as String.
   * 
   * @param whitespace  A valid white space processing value.
   * @param granularity A valid text granularity value.
   * 
   * @return the Diff-X config for the specified arguments as String.
   */
  private static DiffXConfig toConfig(String whitespace, String granularity) {
    WhiteSpaceProcessing ws = WhiteSpaceProcessing.valueOf(whitespace);
    TextGranularity tg = TextGranularity.valueOf(granularity);
    return new DiffXConfig(ws, tg);
  }

  /**
   * Returns a node for the specified string value.
   * 
   * @param xml     The XML to parse.
   * @param config  The DiffX configuration to use.
   * @param factory The class name of the DOM builder factory.
   * 
   * @return the corresponding document node.
   */
  private static Node toNode(String xml, DiffXConfig config, String factory) throws IOException, ParserConfigurationException, SAXException {
	  
//    DocumentBuilderFactory dbFactory = factory == null ? DocumentBuilderFactory.newInstance()
//        : DocumentBuilderFactory.newInstance(factory, Extension.class.getClassLoader());  
    
	// Avoid build error - see https://code.google.com/p/wo-diffx/issues/detail?id=7  
    Document document = XmlUtils.getNewDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    return document.getDocumentElement();
  }

  /**
   * Returns the factory class to use based on the given <code>NodeList</code>s.
   * 
   * @param xml1 the first node list.
   * @param xml2 the second node list.
   */
  private static String getFactoryClass(Node xml1, Node xml2) {
    Package pkg = xml1 != null? xml1.getClass().getPackage()
        : xml2 != null? xml2.getClass().getPackage()
            : null;
        return BUILDERS.get(pkg.getName());
  }

}
