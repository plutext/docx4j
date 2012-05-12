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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import com.topologi.diffx.Docx4jDriver;
import com.topologi.diffx.config.DiffXConfig;
import com.topologi.diffx.event.AttributeEvent;
import com.topologi.diffx.event.CloseElementEvent;
import com.topologi.diffx.event.OpenElementEvent;
import com.topologi.diffx.event.impl.EventFactory;
import com.topologi.diffx.event.impl.ProcessingInstructionEvent;
import com.topologi.diffx.load.text.TextTokeniser;
import com.topologi.diffx.load.text.TokeniserFactory;
import com.topologi.diffx.sequence.EventSequence;

/**
 * Loads a DOM documents as a sequence of events.
 * 
 * <p>This class implements the methods {@link Recorder#process(File)} and
 * {@link Recorder#process(String)} for convenience, but is it much more efficient
 * to feed this recorder directly with a DOM. 
 * 
 * <p>This class is not synchronised.
 * 
 * @author Christophe Lauret
 * @version 26 April 2005
 */
public final class DOMRecorder implements XMLRecorder {

  /**
   * Set to <code>true</code> to show debug info.
   */
  private static final boolean DEBUG = false;

// class attributes ---------------------------------------------------------------------

  /**
   * The DiffX configuration to use
   */
  private DiffXConfig config = new DiffXConfig(); 

// state variables ----------------------------------------------------------------------

  /**
   * The factory that will produce events according to the configuration. 
   */
  private transient EventFactory efactory = null;

  /**
   * The factory that will produce text tokenisers according to the configuration.
   */
  private transient TokeniserFactory tfactory = null;

  /**
   * The sequence of event for this recorder.
   */
  private transient EventSequence sequence = null;

  /**
   * The weight of the current element.
   */
  private transient int currentWeight = -1;

  /**
   * The stack of events' weight, should only contain <code>Integer</code>.
   */
  private transient ArrayList weights = new ArrayList();

  /**
   * Indicates whether the given document is a fragment.
   * 
   * <p>An fragment is a portion of XML that is not necessrily well-formed by
   * itself, because the namespace has been declared higher in the hierarchy, in
   * which if the DOM tree was serialised it would not produce well-formed XML.
   * 
   * <p>This option indicates that the recorder should try to generate the prefix
   * mapping without the declaration.  
   */
  private transient boolean isFragment = true;

// methods ------------------------------------------------------------------------------

  /**
   * Returns the configuration used by this recorder.
   * 
   * @return the configuration used by this recorder.
   */
  public DiffXConfig getConfig() {
    return config;
  }

  /**
   * Sets the configuration used by this recorder.
   * 
   * @param config The configuration used by this recorder.
   */
  public void setConfig(DiffXConfig config) {
    this.config = config;
  }

  /**
   * @see Recorder#process(java.io.File)
   */
  public EventSequence process(File file) throws LoadingException, IOException {
    InputStream in = new BufferedInputStream(new FileInputStream(file));
    return process(new InputSource(in));
  }

  /**
   * @see Recorder#process(java.lang.String)
   */
  public EventSequence process(String xml) throws LoadingException {
    return process(new InputSource(new StringReader(xml)));
  }

  /**
   * Runs the recorder on the specified input source.
   * 
   * @param is The input source.
   * 
   * @return The recorded sequence of events.
   * 
   * @throws LoadingException If thrown whilst parsing.
   */
  public EventSequence process(InputSource is) throws LoadingException {
    this.isFragment = false; // input source is not a fragment
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    dbFactory.setNamespaceAware(config.isNamespaceAware());
    dbFactory.setExpandEntityReferences(true);
    dbFactory.setValidating(false);
    try {
      DocumentBuilder builder = dbFactory.newDocumentBuilder();
      Document document = builder.parse(is);
      return this.process(document);
    } catch (Exception ex) {
      throw new LoadingException(ex);
    }
  }

  /**
   * Processed the given node and return the corresponding event sequence. 
   * 
   * @param node The W3C DOM node to be processed.
   * 
   * @return The recorded sequence of events.
   * 
   * @throws LoadingException If thrown whilst parsing.
   */
  public EventSequence process(Node node) throws LoadingException {
    // initialise the state variables.
    this.efactory = new EventFactory(config.isNamespaceAware());
    this.tfactory = new TokeniserFactory(config);
    this.sequence = new EventSequence();
    // start processing the nodes
    loadNode(node);
    this.isFragment = true;
    return this.sequence;
  }

// specific loaders ---------------------------------------------------------------------

  /**
   * Loads the given node in the current sequence. 
   * 
   * @param node The W3C DOM node to load.
   * 
   * @throws LoadingException If thrown whilst parsing.
   */
  private void loadNode(Node node) throws LoadingException {
    // dispatch to the correct loader
    // performance: order by occurrence
    if (node instanceof Element)
      load((Element)node);
    if (node instanceof Text)
      load((Text)node);
    else if (node instanceof Attr)
      load((Attr)node);
    else if (node instanceof Document)
      load((Document)node);
    else if (node instanceof ProcessingInstruction)
      load((ProcessingInstruction)node);
    // all other node types are ignored
  }

  /**
   * Loads the given document in the current sequence. 
   * 
   * @param document The W3C DOM document node to load.
   * 
   * @throws LoadingException If thrown whilst parsing.
   */
  private void load(Document document) throws LoadingException {
    load(document.getDocumentElement());
  }

  /**
   * Loads the given element in the current sequence. 
   * 
   * @param element The W3C DOM element node to load.
   * 
   * @throws LoadingException If thrown whilst parsing.
   */
  private void load(Element element) throws LoadingException {
    if (currentWeight > 0) weights.add(new Integer(currentWeight));
    currentWeight = 1;
    // namespace handling
    OpenElementEvent open = null;
    // namespace aware configuration
    if (config.isNamespaceAware()) {
      String uri = (element.getNamespaceURI() == null)? "" : element.getNamespaceURI();
      String name = element.getLocalName();
      if (isFragment) {
        String prefix = element.getPrefix();
        if (prefix != null && isFragment)
          this.sequence.mapPrefix(uri, prefix);
      }
      open = efactory.makeOpenElement(uri, name);
    // not namespace aware
    } else {
      open = efactory.makeOpenElement(null, element.getNodeName());
    }

    this.sequence.addEvent(open);
    NamedNodeMap atts = element.getAttributes();
    // only 1 attribute, just load it
    if (atts.getLength() == 1) {
      load((Attr)atts.item(0));
    // several attributes sort them in alphabetical order
    // TODO: also use URI
    } else if (atts.getLength() > 1) {
      String[] names = new String[atts.getLength()];
      for (int i = 0; i < atts.getLength(); i++) {
        Attr attr = (Attr)atts.item(i);
        names[i] = attr.getName();
      }
      Arrays.sort(names);
      for (int i = 0; i < names.length; i++)
        load((Attr)atts.getNamedItem(names[i]));
    }
    // load all the child nodes
    NodeList list = element.getChildNodes();
    for (int i = 0; i < list.getLength(); i++)
      loadNode(list.item(i));
    CloseElementEvent close = efactory.makeCloseElement(open);
    this.sequence.addEvent(close);
    // handle the weights
    close.setWeight(this.currentWeight);
    open.setWeight(this.currentWeight);
    this.currentWeight += popWeight();
  }

  /**
   * Loads the given text in the current sequence depending on the configuration. 
   * 
   * @param text The W3C DOM text node to load.
   * 
   * @throws LoadingException If thrown whilst parsing.
   */
  private void load(Text text) throws LoadingException {
    TextTokeniser ct = tfactory.makeTokeniser(text.getData());
    for (int i = 0; i < ct.countTokens(); i++) {
      sequence.addEvent(ct.nextToken());
      this.currentWeight++;
    }
  }

  /**
   * Loads the given processing instruction in the current sequence. 
   * 
   * @param pi The W3C DOM PI node to load.
   * 
   * @throws LoadingException If thrown whilst parsing.
   */
  private void load(ProcessingInstruction pi) throws LoadingException {
    sequence.addEvent(new ProcessingInstructionEvent(pi.getTarget(), pi.getData()));
    this.currentWeight++;
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
   * @param element The element which attributes have to be handled.
   */
  private void handleAttributes(Element element) {
    NamedNodeMap atts = element.getAttributes();
    // only 1 attribute, just load it
    if (atts.getLength() == 1) {
      load((Attr)atts.item(0));
    // several attributes sort them in alphabetical order
    } else if (atts.getLength() > 1) {
      AttributeEvent[] events = new AttributeEvent[atts.getLength()];
      for (int i = 0; i < atts.getLength(); i++) {
        Attr attr = (Attr)atts.item(i);
        events[i] = efactory.makeAttribute(attr.getNamespaceURI(), 
                                           attr.getLocalName(),
                                           attr.getNodeName(),
                                           attr.getValue());
      }
      Arrays.sort(events, new AttributeComparator());
      for (int i = 0; i < events.length; i++)
        load(events[i]);
    }
  }

  /**
   * Loads the given attribute in the current sequence. 
   * 
   * @param attr The W3C DOM attribute node to load.
   */
  private void load(Attr attr) {
    load(efactory.makeAttribute(attr.getNamespaceURI(),
                                attr.getLocalName(),
                                attr.getNodeName(),
                                attr.getValue())); 
  }

  /**
   * Loads the given attribute in the current sequence. 
   * 
   * @param e An attribute event.
   */
  private void load(AttributeEvent e) {
	  
    // a namespace declaration, translate the event into a prefix mapping
    if ("http://www.w3.org/2000/xmlns/".equals(e.getURI())) {
    	
    	//Docx4jDriver.log("Encountered namespace declaration: " + e.getValue() );
    	
    	// Trap/handle xmlns:xmlns="",
    	// which JAXB seems to produce 
    	// and will later cause errors since
    	// Non-default namespace can not map to empty URI (as per Namespace 1.0 # 2) in XML 1.0 documents
    	if (e.getName().equals("xmlns") &&
    			e.getValue().equals("") ) {
    		Docx4jDriver.log("Ignoring xmlns:xmlns='' ");
    		return;
    	}
    	
      this.sequence.mapPrefix(e.getValue(), e.getName()); 
    
    // a regular attribute
    } else {
    	//System.out.print(e.getURI());
      e.setWeight(2);
      this.currentWeight += 2;
      this.sequence.addEvent(e);
    }
  }
}
