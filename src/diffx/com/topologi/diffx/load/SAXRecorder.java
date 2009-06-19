package com.topologi.diffx.load;

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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.topologi.diffx.config.DiffXConfig;
import com.topologi.diffx.event.AttributeEvent;
import com.topologi.diffx.event.CloseElementEvent;
import com.topologi.diffx.event.impl.EventFactory;
import com.topologi.diffx.event.impl.ProcessingInstructionEvent;
import com.topologi.diffx.event.OpenElementEvent;
import com.topologi.diffx.event.lang.Repertory;
import com.topologi.diffx.load.text.TextTokeniser;
import com.topologi.diffx.load.text.TokeniserFactory;
import com.topologi.diffx.sequence.EventSequence;

/**
 * Records the SAX events in an {@link com.topologi.diffx.sequence.EventSequence}.
 * 
 * <p>It is possible to specify the name of the XML reader implementation class.
 * By default this class will try to use the Crimson parser
 * <code>org.apache.crimson.parser.XMLReaderImpl</code>.  
 * 
 * <p>The XML reader implementation must support the following features settings
 * <pre>
 *   http://xml.org/sax/features/validation         => false
 *   http://xml.org/sax/features/namespaces         => true | false
 *   http://xml.org/sax/features/namespace-prefixes => true | false 
 * </pre>
 * 
 * @author Christophe Lauret, Jean-Baptiste Reure
 * @version 29 April 2005
 */
public final class SAXRecorder implements XMLRecorder {

// static variables -----------------------------------------------------------------------
  
  /**
   * Set to <code>true</code> to show debug info.
   */
  private static final boolean DEBUG = true;

  /**
   * The default XML reader implementation to use.
   */
  //private static final String DEFAULT_XML_READER = "org.apache.crimson.parser.XMLReaderImpl";
  
  private static final String DEFAULT_XML_READER = "com.sun.org.apache.xerces.internal.parsers.SAXParser";

  /**
   * The XML reader.
   */
  private static XMLReader reader;

  /**
   * The name of the XML reader implementation class to use.
   */
  private static String readerClassName = DEFAULT_XML_READER;

  /**
   * Indicates whether a new reader instance should be created because the specified
   * class name has changed. 
   */
  private static boolean newReader = true;

// class attributes -----------------------------------------------------------------------

  /**
   * The DiffX configuration to use
   */
  private DiffXConfig config = new DiffXConfig(); 

  /**
   * The sequence of event for this recorder.
   */
  private transient EventSequence sequence = null;

// methods implementing XMLRecorder -------------------------------------------------------

  /**
   * Runs the recorder on the specified file.
   *
   * <p>This method will count on the {@link InputSource} to guess the correct encoding.
   * 
   * @param file The file to process.
   * 
   * @return The recorded sequence of events. 
   * 
   * @throws LoadingException If thrown whilst parsing.
   * @throws IOException      Should I/O error occur.
   */
  public EventSequence process(File file) throws LoadingException, IOException {
    InputStream in = new BufferedInputStream(new FileInputStream(file));
    EventSequence seq = null;
    seq = process(new InputSource(in));      
    in.close();
    in = null;
    return seq;
  }

  /**
   * Runs the recorder on the specified string.
   * 
   * <p>This method is provided for convenience. It is best to only use this method for
   * short strings.
   * 
   * @param xml The XML string to process.
   * 
   * @return The recorded sequence of events.
   * 
   * @throws LoadingException If thrown whilst parsing.
   * @throws IOException      Should I/O error occur.
   */
  public EventSequence process(String xml) throws LoadingException, IOException {
    return this.process(new InputSource(new StringReader(xml)));
  }

  /**
   * Runs the recorder on the specified input source.
   * 
   * @param is The input source.
   * 
   * @return The recorded sequence of events.
   * 
   * @throws LoadingException If thrown whilst parsing.
   * @throws IOException      Should I/O error occur.
   */
  public EventSequence process(InputSource is) throws LoadingException, IOException {
    if (reader == null || newReader) init();
    reader.setContentHandler(new RecorderHandler());
    reader.setErrorHandler(new RecorderErrorHandler());
    try {
      reader.setFeature("http://xml.org/sax/features/namespaces", 
                         config.isNamespaceAware());
      reader.setFeature("http://xml.org/sax/features/namespace-prefixes", 
                         config.isReportPrefixDifferences());
      reader.parse(is);
    } catch (SAXException ex) {
      throw new LoadingException(ex);
    }
    return this.sequence;
  }

  /**
   * Returns the configuration used by this recorder.
   * 
   * @return the configuration used by this recorder.
   */
  public DiffXConfig getConfig() {
    return this.config;
  }

  /**
   * Sets the configuration used by this recorder.
   * 
   * @param config The configuration used by this recorder.
   */
  public void setConfig(DiffXConfig config) {
    this.config = config;
  }

// other methods ------------------------------------------------------------------------------

  /**
   * Returns the name XMLReader class used by the SAXRecorders.
   * 
   * @return the name XMLReader class used by the SAXRecorders.
   */
  public static String getXMLReaderClass() {
    return readerClassName;
  }

  /**
   * Sets the name of the XML reader class to use.
   * 
   * <p>Use <code>null</code> to reset the XML reader class and use the default XML reader.
   * 
   * <p>A new reader will be created only if the specified class is different from the
   * current one. 
   * 
   * @param className The name of the XML reader class to use;
   *                  or <code>null</code> to reset the XML reader.
   */
  public static void setXMLReaderClass(String className) {
    // if the className is null reset to default
    if (className == null)
      className = DEFAULT_XML_READER;
     // reload only if different from the current one.
    newReader = !className.equals(readerClassName);
    readerClassName = className;
  }

  /**
   * Initialises the XML reader using the defined class name.
   *
   * @throws LoadingException If one of the features could not be set.
   */
  private static void init() throws LoadingException {
    try {
      reader = XMLReaderFactory.createXMLReader(readerClassName);
      reader.setFeature("http://xml.org/sax/features/validation", false);
    } catch (SAXException ex) {
      throw new LoadingException(ex);
    }
  }

// static inner class for processing the XML files --------------------------------------------

  /**
   * A SAX2 handler that records XML events.
   * 
   * <p>This class is an inner class as there is no reason to expose its method to the
   * public API. 
   * 
   * @author Christophe Lauret, Jean-Baptiste Reure
   * @version 27 April 2005
   */
  private final class RecorderHandler extends DefaultHandler {

    /**
     * A buffer for character data.
     */
    private StringBuffer ch = new StringBuffer();

    /**
     * The comparator in order to sort attribute correctly.
     */
    private AttributeComparator comparator = new AttributeComparator();

    /**
     * A repertory of words.
     */
    private transient Repertory repertory = new Repertory();

    /**
     * The weight of the current element.
     */
    private transient int currentWeight = -1;

    /**
     * The last open element event, should only contain
     * <code>OpenElementEvent</code>s.
     */
    private transient ArrayList openElements = new ArrayList();

    /**
     * The stack of weight, should only contain <code>Integer</code>.
     */
    private transient ArrayList weights = new ArrayList();

    /**
     * The factory that will produce events according to the configuration. 
     */
    private transient EventFactory efactory = null;

    /**
     * The factory that will produce text tokenisers according to the configuration.
     */
    private transient TokeniserFactory tfactory = null;

    /**
     * @see org.xml.sax.ContentHandler#startDocument() 
     */
    public void startDocument() {
      sequence = new EventSequence();
      this.efactory = new EventFactory(config.isNamespaceAware());
      this.tfactory = new TokeniserFactory(config);
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes atts) {
      recordCharacters();
      if (currentWeight > 0) weights.add(new Integer(currentWeight));
      currentWeight = 1;
      OpenElementEvent open = efactory.makeOpenElement(uri, localName, qName);
      this.openElements.add(open);
      sequence.addEvent(open);
      handleAttributes(atts);
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String uri, String localName, String qName) {
      recordCharacters();
      OpenElementEvent open = popLastOpenElement();
      open.setWeight(this.currentWeight);
      CloseElementEvent close = efactory.makeCloseElement(open);
      close.setWeight(this.currentWeight);
      sequence.addEvent(close);
      // calculate weights
      this.currentWeight += popWeight();
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] buf, int pos, int len) {
      ch.append(buf, pos, len);
    }

    /**
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] buf1, int pos, int len) {
      // this method is only useful if the XML provides a Schema or DTD
      // to define in which cases whitespaces can be considered ignorable.
      // By default, all white spaces are significant and therefore reported
      // by the characters method.
    }

    /**
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    public void processingInstruction(String target, String data) {
      sequence.addEvent(new ProcessingInstructionEvent(target, data));
      this.currentWeight++;
    }

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
    }

    /**
     * Records the characters which are in the buffer.
     */
    private void recordCharacters() {
      if (this.ch != null) {
        TextTokeniser ct = tfactory.makeTokeniser(this.ch);
        ct.useRepertory(this.repertory);
        for (int i = 0; i < ct.countTokens(); i++) {
          sequence.addEvent(ct.nextToken());
          this.currentWeight++;
        }
        this.ch.setLength(0);
      }
    }

    /**
     * Returns the last open element and remove it from the stack.
     * 
     * @return The last open element.
     */
    private OpenElementEvent popLastOpenElement() {
      return (OpenElementEvent)this.openElements.remove(this.openElements.size() - 1);
    }

    /**
     * Returns the last weight and remove it from the stack.
     * 
     * @return The weight on top of the stack.
     */
    private int popWeight() {
      if (this.weights.size() > 0)
        return ((Integer)this.weights.remove(this.weights.size() - 1)).intValue();
      else
        return 0;
    }

    /**
     * Handles the attributes, will add them to the sequence in order if any.
     * 
     * @param atts The attributes to handle.
     */
    private void handleAttributes(Attributes atts) {
      // only one attribute
      if (atts.getLength() == 1) {
        sequence.addEvent(efactory.makeAttribute(atts.getURI(0),
                                                 atts.getLocalName(0),
                                                 atts.getQName(0),
                                                 atts.getValue(0)));
      // several attributes
      } else if (atts.getLength() > 1) {
        // store all the attributes
        AttributeEvent[] attEvents = new AttributeEvent[atts.getLength()];
        for (int i = 0; i < atts.getLength(); i++) {
          attEvents[i] = efactory.makeAttribute(atts.getURI(i),
                                                atts.getLocalName(i),
                                                atts.getQName(i),
                                                atts.getValue(i));
          attEvents[i].setWeight(2);
          this.currentWeight += 2;
        }
        // sort them
        Arrays.sort(attEvents, comparator);
        // add them to the sequence
        for (int i = 0; i < attEvents.length; i++)
          sequence.addEvent(attEvents[i]);
      }
    }

  }

  /**
   * A tight error handler that will throw an exception for any error type. 
   * 
   * ErrorHandler used only so that namepsace related errors are reported ???
   * (they are error type and not fatal error).
   * 
   * @author Jean-baptiste Reure
   * @version 18 March 2005
   */
  private final class RecorderErrorHandler implements ErrorHandler {

    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException ex) throws SAXException {
      throw ex;
    }

    /**
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException ex) throws SAXException {
      throw ex;
    }

    /**
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException ex) throws SAXException {
      throw ex;
    }
  }

}