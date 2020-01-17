/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.load;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.topologi.diffx.event.OpenElementEvent;
import com.topologi.diffx.event.TextEvent;
import com.topologi.diffx.event.impl.EventFactory;
import com.topologi.diffx.event.impl.ProcessingInstructionEvent;
import com.topologi.diffx.load.text.TextTokenizer;
import com.topologi.diffx.load.text.TokenizerFactory;
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
 * @author Christophe Lauret
 * @author Jean-Baptiste Reure
 * 
 * @version 17 October 2006
 */
public final class SAXRecorder implements XMLRecorder {

  // static variables -------------------------------------------------------------------------------

  /**
   * The XML reader.
   */
  private static XMLReader reader;

  /**
   * The default XML reader in use.
   */
  private static final String DEFAULT_XML_READER;
  static {
    String className;
    try {
      className = XMLReaderFactory.createXMLReader().getClass().getName();
    } catch (SAXException ex) {
      // FIXME: Exception handling!!!
      //      className = XMLReaderImpl.class.getName();
      className = "";
    }
    DEFAULT_XML_READER = className;
  }

  /**
   * The XML reader class in use (set to the deafult XML reader).
   */
  private static String readerClassName = DEFAULT_XML_READER;

  /**
   * Indicates whether a new reader instance should be created because the specified class name
   * has changed.
   */
  private static boolean newReader = true;

  // class attributes -------------------------------------------------------------------------------

  /**
   * The DiffX configuration to use
   */
  private DiffXConfig config = new DiffXConfig();

  /**
   * The sequence of event for this recorder.
   */
  protected transient EventSequence sequence;

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
   * @throws LoadingException If thrown while parsing.
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
   * @throws LoadingException If thrown while parsing.
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
    if (reader == null || newReader) {
      init();
    }
    reader.setContentHandler(new RecorderHandler());
    reader.setErrorHandler(new RecorderErrorHandler());
    try {
      reader.setFeature("http://xml.org/sax/features/namespaces", this.config.isNamespaceAware());
      reader.setFeature("http://xml.org/sax/features/namespace-prefixes", this.config.isReportPrefixDifferences());
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
   * <p>A new reader will be created only if the specified class is different from the current one.
   * 
   * @param className The name of the XML reader class to use;
   *                  or <code>null</code> to reset the XML reader.
   */
  public static void setXMLReaderClass(String className) {
    // if the className is null reset to default
    if (className == null) {
      className = DEFAULT_XML_READER;
    }
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
    private final StringBuffer ch = new StringBuffer();

    /**
     * The comparator in order to sort attribute correctly.
     */
    private final AttributeComparator comparator = new AttributeComparator();

    /**
     * The weight of the current element.
     */
    private transient int currentWeight = -1;

    /**
     * The last open element event, should only contain <code>OpenElementEvent</code>s.
     */
    private transient List<OpenElementEvent> openElements = new ArrayList<OpenElementEvent>();

    /**
     * The stack of weight, should only contain <code>Integer</code>.
     */
    private transient List<Integer> weights = new ArrayList<Integer>();

    /**
     * The factory that will produce events according to the configuration.
     */
    private transient EventFactory efactory;

    /**
     * The text tokenizer according to the configuration.
     */
    private transient TextTokenizer tokenizer;

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    @Override
    public void startDocument() {
      SAXRecorder.this.sequence = new EventSequence();
      this.efactory = new EventFactory(SAXRecorder.this.config.isNamespaceAware());
      this.tokenizer = TokenizerFactory.get(SAXRecorder.this.config);
      SAXRecorder.this.sequence.mapPrefix("http://www.w3.org/XML/1998/namespace", "xml");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
      SAXRecorder.this.sequence.mapPrefix(uri, prefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) {
      recordCharacters();
      if (this.currentWeight > 0) {
        this.weights.add(new Integer(this.currentWeight));
      }
      this.currentWeight = 1;
      OpenElementEvent open = this.efactory.makeOpenElement(uri, localName, qName);
      this.openElements.add(open);
      SAXRecorder.this.sequence.addEvent(open);
      handleAttributes(atts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String uri, String localName, String qName) {
      recordCharacters();
      OpenElementEvent open = popLastOpenElement();
      open.setWeight(this.currentWeight);
      CloseElementEvent close = this.efactory.makeCloseElement(open);
      close.setWeight(this.currentWeight);
      SAXRecorder.this.sequence.addEvent(close);
      // calculate weights
      this.currentWeight += popWeight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(char[] buf, int pos, int len) {
      this.ch.append(buf, pos, len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ignorableWhitespace(char[] buf1, int pos, int len) {
      // this method is only useful if the XML provides a Schema or DTD
      // to define in which cases whitespaces can be considered ignorable.
      // By default, all white spaces are significant and therefore reported
      // by the characters method.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processingInstruction(String target, String data) {
      SAXRecorder.this.sequence.addEvent(new ProcessingInstructionEvent(target, data));
      this.currentWeight++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException {
    }

    /**
     * Records the characters which are in the buffer.
     */
    private void recordCharacters() {
      if (this.ch != null) {
        List<TextEvent> events = this.tokenizer.tokenize(this.ch);
        for (TextEvent e : events) {
          SAXRecorder.this.sequence.addEvent(e);
        }
        this.currentWeight += events.size();
        this.ch.setLength(0);
      }
    }

    /**
     * Returns the last open element and remove it from the stack.
     * 
     * @return The last open element.
     */
    private OpenElementEvent popLastOpenElement() {
      return this.openElements.remove(this.openElements.size() - 1);
    }

    /**
     * Returns the last weight and remove it from the stack.
     * 
     * @return The weight on top of the stack.
     */
    private int popWeight() {
      if (this.weights.size() > 0)
        return this.weights.remove(this.weights.size() - 1).intValue();
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
        SAXRecorder.this.sequence.addEvent(this.efactory.makeAttribute(atts.getURI(0),
            atts.getLocalName(0),
            atts.getQName(0),
            atts.getValue(0)));
        // several attributes
      } else if (atts.getLength() > 1) {
        // store all the attributes
        AttributeEvent[] attEvents = new AttributeEvent[atts.getLength()];
        for (int i = 0; i < atts.getLength(); i++) {
          attEvents[i] = this.efactory.makeAttribute(atts.getURI(i),
              atts.getLocalName(i),
              atts.getQName(i),
              atts.getValue(i));
          attEvents[i].setWeight(2);
          this.currentWeight += 2;
        }
        // sort them
        Arrays.sort(attEvents, this.comparator);
        // add them to the sequence
        for (AttributeEvent attEvent : attEvents) {
          SAXRecorder.this.sequence.addEvent(attEvent);
        }
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
   * @version 17 May 2005
   */
  private static final class RecorderErrorHandler implements ErrorHandler {

    /**
     * {@inheritDoc}
     */
    public void error(SAXParseException ex) throws SAXException {
      throw ex;
    }

    /**
     * {@inheritDoc}
     */
    public void fatalError(SAXParseException ex) throws SAXException {
      throw ex;
    }

    /**
     * {@inheritDoc}
     */
    public void warning(SAXParseException ex) throws SAXException {
      throw ex;
    }
  }

}
