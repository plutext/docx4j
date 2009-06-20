package com.topologi.diffx.xml;

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
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import com.topologi.diffx.Docx4jDriver;
import com.topologi.diffx.event.AttributeEvent;

/**
 * A Namespace-aware writer for XML data.
 *
 * <p>Provides methods to generate well-formed XML data easily. wrapping a writer.
 *
 * <p>This version only supports utf-8 encoding, if writing to a file make sure that the 
 * encoding of the file output stream is "utf-8".
 *
 * <p>The recommended implementation is to use a <code>BufferedWriter</code> to write.
 *
 * <pre>
 *  Writer writer =
 *     new BufferedWriter(new OutputStreamWriter(new FileOutputStream("foo.out"),"utf-8"));
 * </pre>
 *
 * <p>This class is not synchronised.
 * 
 * @author  Christophe Lauret - Allette Systems (Australia)
 * @version 5 April 2005
 */
public final class NSAwareXMLWriter extends XMLWriterBase implements XMLWriter {

  /**
   * Set to <code>true</code> to show debug info.
   */
  private static final boolean DEBUG = false;

  /**
   * The default namespace mapped to the empty prefix.
   */
  private static final PrefixMapping DEFAULT_NS = new PrefixMapping("", ""); 

  /**
   * The root node.
   */
  private static final Element ROOT;
  static {
    List mps = new ArrayList();
    mps.add(DEFAULT_NS);
    ROOT = new Element("", true, mps);
  }

  /**
   * The current prefix mapping.
   */
  private Hashtable prefixMapping = new Hashtable();

  /**
   * The list of prefix mappings to be associated with the next element.
   */
  private List tempMapping = null;

  /**
   * A stack of elements to close the elements automatically. 
   */
  private List elements = new ArrayList(); 

  /**
   * Flag to indicate that the element open tag is not finished yet. 
   */
  private boolean isNude = false; 

  // constructors -----------------------------------------------------------------------------

  /**
   * <p>Creates a new XML writer.
   *
   * <p>Sets the depth attribute to 0 and the indentation to <code>true</code>.
   *
   * @param writer Where this writer should write the XML data.
   * 
   * @throws NullPointerException If the writer is <code>null</code>.
   */
  public NSAwareXMLWriter(Writer writer) throws NullPointerException {
    super(writer, true);
    this.elements.add(ROOT);
    this.prefixMapping.put("", "");
  }

  /**
   * <p>Create a new XML writer.
   *
   * @param writer  Where this writer should write the XML data.
   * @param indent  Set the indentation flag.
   * 
   * @throws NullPointerException If the writer is <code>null</code>.
   */
  public NSAwareXMLWriter(Writer writer, boolean indent) throws NullPointerException {
    super(writer, indent);
    this.elements.add(ROOT);
    this.prefixMapping.put("", "");
  }

// PI and comments ----------------------------------------------------------------------------

  /**
   * Write an XML comment.
   *
   * <p>An XML comment is:<br>
   * <pre>
   *   &lt;!-- <i>comment</i> --&gt;
   * </pre>
   *
   * <p>Comments are not indented.
   *
   * @param comment the comment to be written
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  public void writeComment(String comment) throws IOException {
    deNude();
    super.writeComment(comment);
  }

  /**
   * Writes an XML processing instruction.
   *
   * <p>An XML processing intruction is:<br>
   * <pre>
   *   &lt;?<i>target</i> <i>data</i>?&gt;
   * </pre>
   *
   * @param target The PI's target.
   * @param data   The PI's data.
   * 
   * @throws IOException If an I/O exception is occurs.
   */
  public void writePI(String target, String data) throws IOException {
    deNude();
    super.writePI(target, data);
  }

// writing text -------------------------------------------------------------------------------

  /**
   * @see com.topologi.diffx.xml.XMLWriter#writeText(char)
   */
  public void writeText(char c) throws IOException {
    deNude();
    writerEscape.writeText(c);
  }

  /**
   * @see com.topologi.diffx.xml.XMLWriter#writeText(String)
   */
  public void writeText(String text) throws IOException {
    if (text == null) return;
    deNude();
    writerEscape.writeText(text);
  }

  /**
   * Writes the given text correctly for the encoding of this document.
   *
   * @see com.topologi.diffx.xml.XMLWriter#writeText(char[], int, int)
   */
  public void writeText(char[] text, int off, int len) throws IOException {
    deNude();
    writerEscape.writeText(text, off, len);
  }

  /**
   * @see com.topologi.diffx.xml.XMLWriter#writeXML(java.lang.String)
   */
  public void writeXML(String text) throws IOException {
    if (text == null) return;
    deNude();
    this.writer.write(text);
  }

  /**
   * @see com.topologi.diffx.xml.XMLWriter#writeXML(char[], int, int)
   */
  public void writeXML(char[] text, int off, int len) throws IOException {
    deNude();
    this.writer.write(text, off, len);
  }

  /**
   * Writes the angle bracket if the element open tag is not finished.
   *
   * @throws IOException If thrown by the wrapped writer.
   */
  private void deNude() throws IOException {
    if (this.isNude) {
      writer.write('>');
      if (peekElement().hasChildren) writer.write('\n');
      this.isNude = false;
    }
  }

// open/close specific elements ------------------------------------------------------------

  /**
   * Writes a start element tag correctly indented.
   *
   * <p>It is the same as <code>openElement(null, name, false)</code>
   *
   * @see #openElement(java.lang.String, java.lang.String, boolean)
   *
   * @param name the name of the element
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  public void openElement(String name) throws IOException {
    openElement(null, name, false);
  }

  /**
   * Write a start element tag correctly indented.
   *
   * <p>It is the same as <code>openElement(name, false)</code>
   *
   * @see #openElement(java.lang.String, boolean)
   *
   * @param uri  The namespace URI of this element.
   * @param name The name of the element.
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  public void openElement(String uri, String name) throws IOException {
    openElement(uri, name, false);
  }

  /**
   * Writes a start element tag correctly indented.
   *
   * <p>Use the <code>hasChildren</code> parameter to specify whether this element is terminal
   * node or not, note: this affects the indenting. To produce correctly indented XML, you 
   * should use the same value for this flag when closing the element.
   *
   * <p>The name can contain attributes and should be a valid xml name.
   *
   * @param name        The name of the element.
   * @param hasChildren <code>true</code> if this element has children.
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  public void openElement(String name, boolean hasChildren) throws IOException {
    openElement(null, name, hasChildren);
  }

  /**
   * Writes a start element tag correctly indented.
   *
   * <p>Use the <code>hasChildren</code> parameter to specify whether this element is terminal 
   * node or not, note: this affects the indenting. To produce correctly indented XML, you 
   * should use the same value for this flag when closing the element.
   *
   * <p>The name can contain attributes and should be a valid xml name.
   *
   * @param uri         The namespace URI of this element.
   * @param name        The name of the element.
   * @param hasChildren true if this element has children.
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  public void openElement(String uri, String name, boolean hasChildren) throws IOException {
    deNude();
    this.indent();
    String qName = getQName(uri, name);
    this.elements.add(new Element(qName, hasChildren, this.tempMapping));
    this.writer.write('<');
    this.writer.write(qName);
    this.handleNamespaceDeclaration();
    this.isNude = true;
    this.depth++;
  }

  /**
   * Write an end element tag.
   *
   * @throws IOException If thrown by the wrapped writer.
   */
  public void closeElement() throws IOException {
    if (this.elements.size() <= 1) return;
    Element elt = popElement();
    this.depth--;
    // this is an empty element
    if (this.isNude) {
      writer.write('/');
      this.isNude = false;
    // the element contains text
    } else {
      if (elt.hasChildren) this.indent();
      this.writer.write('<');
      this.writer.write('/');
       int x = elt.qName.indexOf(' ');
      if (x < 0)
        this.writer.write(elt.qName);
      else
        this.writer.write(elt.qName.substring(0, x));
    }
    // restore previous mapping if necessary
    restorePrefixMapping(elt);
    this.writer.write('>');
    if (super.indent) this.writer.write('\n');
  }

  /**
   * Same as <code>emptyElement(null, element);</code>
   *
   * @param element the name of the element
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  public void emptyElement(String element) throws IOException {
    emptyElement(null, element);
  }

  /**
   * Write an empty element.
   *
   * <p>It is possible for the element to contain attributes,
   * however, since there is no character escaping, great care
   * must be taken not to introduce invalid characters. For
   * example:
   * <pre>
   *    &lt;<i>example test="yes"</i>/&gt;
   * </pre>
   *
   * @param uri     The namespace URI for this element.
   * @param element The name of the element.
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  public void emptyElement(String uri, String element) throws IOException {
    deNude();
    this.indent();
    this.writer.write('<');
    this.writer.write(getQName(uri, element));
    this.handleNamespaceDeclaration();
    this.writer.write('/');
    this.writer.write('>');
    if (super.indent) this.writer.write('\n');
  }

  /**
   * Returns the last element in the list.
   * 
   * @return The current element.
   */
  private Element peekElement() {
    return ((Element)this.elements.get(this.elements.size() - 1));
  }

  /**
   * Removes the last element in the list.
   * 
   * @return The current element.
   */
  private Element popElement() {
    return ((Element)this.elements.remove(this.elements.size() - 1));
  }

// attributes ---------------------------------------------------------------------------------

  /**
   * Writes an attribute.
   *
   * @param name  The name of the attribute.
   * @param value The value of the attribute.
   * 
   * @throws IOException If thrown by the wrapped writer.
   * @throws IllegalStateException If there is no open element or text has been written. 
   */
  public void attribute(String name, String value)
      throws IOException, IllegalStateException {
    if (!this.isNude) throw new IllegalArgumentException("Cannot write attribute: too late!");
    this.writer.write(' ');
    this.writer.write(name);
    this.writer.write("=\"");
    writerEscape.writeAttValue(value);
    this.writer.write('"');
  }

  /**
   * Writes an attribute.
   *
   * <p>This method for number does not require escaping.
   *
   * @param name  The name of the attribute.
   * @param value The value of the attribute.
   * 
   * @throws IOException If thrown by the wrapped writer.
   * @throws IllegalStateException If there is no open element or text has been written. 
   */
  public void attribute(String name, int value)
      throws IOException, IllegalStateException {
    if (!this.isNude) throw new IllegalArgumentException("Cannot write attribute: too late!");
    this.writer.write(' ');
    this.writer.write(name);
    this.writer.write("=\"");
    this.writer.write(Integer.toString(value));
    this.writer.write('"');
  }

  /**
   * Writes an attribute.
   *
   * @param uri   The namespace URI this attribute belongs to.
   * @param name  The name of the attribute.
   * @param value The value of the attribute.
   * 
   * @throws IOException If thrown by the wrapped writer.
   * @throws IllegalStateException If there is no open element or text has been written. 
   */
  public void attribute(String uri, String name, String value)
      throws IOException, IllegalStateException {
    if (!this.isNude) throw new IllegalArgumentException("Cannot write attribute: too late!");
    this.writer.write(' ');
    this.writer.write(getQName(uri, name));
    this.writer.write("=\"");
    writerEscape.writeAttValue(value);
    this.writer.write('"');
    this.handleNamespaceDeclaration();
  }

  /**
   * Writes an attribute.
   *
   * <p>This method for number does not require escaping.
   *
   * @param uri   The namespace URI this attribute belongs to.
   * @param name  The name of the attribute.
   * @param value The value of the attribute.
   * 
   * @throws IOException If thrown by the wrapped writer.
   * @throws IllegalStateException If there is no open element or text has been written. 
   */
  public void attribute(String uri, String name, int value)
      throws IOException, IllegalStateException {
    if (!this.isNude) throw new IllegalArgumentException("Cannot write attribute: too late!");
    this.writer.write(' ');
    this.writer.write(getQName(uri, name));
    this.writer.write("=\"");
    this.writer.write(Integer.toString(value));
    this.writer.write('"');
    this.handleNamespaceDeclaration();
  }

  // namespace handling -----------------------------------------------------------------------

  /**
   * @see com.topologi.diffx.xml.XMLWriter#setPrefixMapping(java.lang.String, java.lang.String)
   * 
   * <p>This implementation does not keep a history of the prefix mappings so it needs to be
   * reset. If a prefix is already being used it is overridden.
   * 
   * @param uri    The full namespace URI.
   * @param prefix The prefix for the namespace uri.
   * 
   * @throws NullPointerException if the prefix is <code>null</code>.
   */
  public void setPrefixMapping(String uri, String prefix) throws NullPointerException {
    //do not declare again if the same mapping already exist
    if (!prefix.equals(this.prefixMapping.get(uri))) {
      // remove the previous mapping to the prefix
      removeIfNeeded(prefix);
      // create the new prefix mapping
      PrefixMapping pm = new PrefixMapping(prefix, uri);
      this.prefixMapping.put(pm.uri, pm.prefix);
      if (DEBUG) System.err.println(pm.prefix+" -> "+pm.uri);
      if (this.tempMapping == null) this.tempMapping = new ArrayList();
      this.tempMapping.add(pm);
    }
  }

  /**
   * Returns the qualified name for this element using the specified namespace URI.
   * 
   * @param uri  The namespace URI for the element.
   * @param name The name of the element or attribute.
   * 
   * @return The qualified element name.
   * 
   * @throws UndeclaredNamespaceException If the uri has not being previously declared.
   */
  private String getQName(String uri, String name) throws UndeclaredNamespaceException {
    String prefix = (String)this.prefixMapping.get((uri != null)? uri : "");
    if (prefix == null) {
    	if (uri.equals("http://www.w3.org/XML/1998/namespace")) {
    		// eg xml:space .. 
    		// per the namespace recommendation, this namespace may, but need not, be declared
    		// It is bound by definition to ...
        	return "xml:"+name;    	    		
    	} else {
    		Docx4jDriver.log("FIX ME - com.topologi.diffx.xml.NSAwareXMLWriter.getQName(null, " + name + ") @599)");    	
    		throw new UndeclaredNamespaceException(uri + " for " + name);
    	}
    } else if ("".equals(prefix)) {
        return name;    	
    } else {    	
        try {
			return this.prefixMapping.get(uri)+":"+name;
		} catch (RuntimeException e) {
			System.out.println("uri: " + uri + ", name: " + name);
			// uri=null
			// seems to occur due to or resulting from xmlns:xmlns=""
			// and later causes error: Non-default namespace can not map to empty URI (as per Namespace 1.0 # 2) in XML 1.0 documents
			// Workaround is to ignore such things in 
			// DOMRecoder.load(AttributeEvent @line 456

			e.printStackTrace();
			return name;
		}
    } 
  }

  /**
   * Handles the namespace declaration and updates the prefix mappings.
   * 
   * @throws IOException If thrown by the wrapped writer.
   */
  private void handleNamespaceDeclaration() throws IOException {
    if (this.tempMapping != null) {
      PrefixMapping pm = null;
      for (int i = 0; i < tempMapping.size(); i++) {
        pm = (PrefixMapping)tempMapping.get(i);
        this.writer.write(" xmlns");
        // specify a prefix if different from ""
        if (!"".equals(pm.prefix)) {
          this.writer.write(':');
          this.writer.write(pm.prefix);
        }
        this.writer.write("=\"");
        this.writer.write(pm.uri);
        this.writer.write("\"");
      }
      this.tempMapping = null;
    }
  }

  /**
   * Restores the prefix mapping after closing an element.
   * 
   * <p>This costly operation need only to be done if the method
   * {@link NSAwareXMLWriter#setPrefixMapping(String, String)} have been used
   * immediately before, therefore it should not happen often.
   * 
   * @param elt The element that had some new mappings.
   */
  private void restorePrefixMapping(Element elt) {
		if (elt.mappings != null) {
			// for each mapping of this element
			for (int i = 0; i < elt.mappings.size(); i++) {
				PrefixMapping mpi = (PrefixMapping) elt.mappings.get(i);
				if (DEBUG)
					System.err.print(mpi.prefix + " -< ");
				// find the first previous namespace mapping amongst the parents
				// that defines namespace mappings
				for (int j = elements.size() - 1; j > 0; j--) {
					if (((Element) this.elements.get(j)).mappings != null) {
						List mps = ((Element) elements.get(j)).mappings;
						// iterate through the define namespace mappings of the
						// parent
						for (int k = 0; k < mps.size(); k++) {
							PrefixMapping mpk = (PrefixMapping) mps.get(k);
							// if we found a namespace prefix for the namespace
							if (mpk.prefix.equals(mpi.prefix)) {
								removeIfNeeded(mpk.prefix);
								this.prefixMapping.put(mpk.uri, mpk.prefix);
								if (DEBUG)
									System.err.println(mpk.uri + " [R]");
								j = 0; // exit from the previous loop
								break; // exit from this one
							}
						}
					}
				}
			}
		}
	}

  /**
	 * Removes the mapping associated to the specified prefix.
	 * 
	 * @param prefix
	 *            The prefix which mapping should be removed.
	 */
  private void removeIfNeeded(String prefix) {
    // remove the previous mapping to the prefix
    if (this.prefixMapping.containsValue(prefix)) {
      Object key = null;
      for (Enumeration e = this.prefixMapping.keys(); e.hasMoreElements();) {
        key = e.nextElement();
        if (this.prefixMapping.get(key).equals(prefix)) 
          break;
      }
      this.prefixMapping.remove(key); // we know key should have a value
	  if (DEBUG) System.err.println("Removed " + prefix);
      
    }
  }
  
  // inner class: Element --------------------------------------------------------------------- 

  /**
   * A light object to keep track of the elements
   * 
   * @author Christophe Lauret (Allette Systems)
   * @version 31 August 2004
   */
  private static final class Element {

    /**
     * The fully qualified name of the element.
     */
    private String qName;

    /**
     * Indicates whether the element has children. 
     */
    private boolean hasChildren;

    /**
     * A list of prefix mappings for this element.
     * 
     * <p>Can be <code>null</code>.
     */
    private final List mappings;

    /**
     * Creates a new Element.
     * 
     * @param qName       The qualified name of the element.
     * @param hasChildren Whether the element has children.
     * @param mappings    The list of prefix mapping if any.
     */
    public Element(String qName, boolean hasChildren, List mappings) {
      this.qName = qName;
      this.hasChildren = hasChildren;
      this.mappings = mappings;
    }
  }

  // inner class: Prefix Mapping --------------------------------------------------------------

  /**
   * Light-weight class to represent a prefix mapping.
   *
   * <p>The class attributes cannot be <code>null</code>. 
   *
   * @author Christophe Lauret (Allette Systems)
   * @version 31 August 2004
   */
  private static final class PrefixMapping {

    /**
     * The prefix associated to the URI. 
     */
    private final String prefix;

    /**
     * The namespace URI.
     */
    private final String uri;

    /**
     * Creates a new prefix mapping.
     * 
     * @param prefix The prefix for the URI. 
     * @param uri    The full namespace URI.
     */
    public PrefixMapping(String prefix, String uri) {
      this.prefix = (prefix != null)? prefix : "";
      this.uri = (uri != null)? uri : ""; 
    }
  }

}