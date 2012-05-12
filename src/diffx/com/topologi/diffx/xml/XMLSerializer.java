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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * This class provides methods to serialize objects to XML.
 *
 * <p>There is no mechanism to prevent infinite loops if some objects (lists,...) reference 
 * themselves.
 *
 * <p>The underlying XML document is generated using an XML string buffer.
 *
 * @version 7 March 2005
 * @author Christophe Lauret
 */
public final class XMLSerializer {

  // TODO: make an interface out of this class.
  
  // TODO: make the dateformat a class attribute.
  
  /**
   * Date formatter.
   */
  private static final DateFormat DF = new SimpleDateFormat("dd/MM/yyyy");

  /**
   * Used to store the xml document of this class.
   *
   * <p>Classes extending this class should use their constructors to set the size of the 
   * <code>XMLStringBuffer</code>.
   */
  private XMLWriter xml;

  /**
   * Creates a new XML serializer using the specified XML writer. 
   *
   * @param xml The XML string buffer to be used
   */
  public XMLSerializer(XMLWriter xml) {
    this.xml = xml;
  }

  /**
   * Returns the underlying XML document.
   *
   * @return the xml stringbuffer
   */
  public XMLWriter getXML() {
    return this.xml;
  }

  /**
   * Serialises the given object using the given name as element name.
   * 
   * <p>This implementation is recursive. It calls itself for fields which are not of 
   * primitive type.
   *
   * @param o     Object to be serialised as xml
   * @param name  Name of object
   * 
   * @throws IOException Should an I/O error occur.
   */
  public void serialize(Object o, String name) throws IOException {
    if (o != null) {
      // get rid of some nasty symbols from qualified names and inner classes
      if (name.lastIndexOf('.') != -1) name = name.substring(name.lastIndexOf('.')+1);
      if (name.lastIndexOf('$') != -1) name = name.substring(name.lastIndexOf('$')+1);
      name = name.toLowerCase();
      // numbers
      if (o instanceof Number) {
        this.xml.openElement(name, false);
        this.xml.writeText(o.toString());
        this.xml.closeElement();
      // strings
      } else if (o instanceof String) {
        this.xml.openElement(name, false);
        this.xml.writeText(o.toString());
        this.xml.closeElement();
      // characters
      } else if (o instanceof Character) {
        this.xml.openElement(name, false);
        this.xml.writeText(((Character)o).charValue());
        this.xml.closeElement();
      // boolean
      } else if (o instanceof Boolean) {
        this.xml.openElement(name, false);
        this.xml.writeText(o.toString());
        this.xml.closeElement();
      // dates
      } else if (o instanceof Date) {
        this.xml.openElement(name, false);
        this.xml.writeText(DF.format((Date)o));
        this.xml.closeElement();
      // collection
      } else if (o instanceof Collection) {
        this.xml.openElement(name, (((Collection)o).size() != 0));
        this.serializeCollection((Collection)o);
        this.xml.closeElement();
      // hashtable
      } else if (o instanceof Hashtable) {
        this.xml.openElement(name, (((Hashtable)o).size() != 0));
        this.serializeHashtable((Hashtable)o);
        this.xml.closeElement();
      // other objects
      } else {
        this.xml.openElement(name, true);
        this.serializeObject(o);
        this.xml.closeElement();
      }
    }
  }

  /**
   * Serialises the given Collection to xml.
   *
   * <p>Iterates over every object and call the {@link #serialize } method.
   * 
   * @param c The Collection to be serialised to XML
   * 
   * @throws IOException Should an I/O error occur.
   */
  public void serializeCollection(Collection c) throws IOException {
    for (Iterator iter = c.iterator(); iter.hasNext();) {
      Object o = iter.next();
      this.serialize(o, o.getClass().getName());
    }
  }

  /**
   * Serialise the given <code>Hashtable</code> to xml.
   *
   * <p>This methods only works if the {@link Hashtable} contains <code>String</code> 
   * objects.
   *
   * @param h The hashtable to be serialized to XML
   * 
   * @throws IOException Should an I/O error occur.
   */
  public void serializeHashtable(Hashtable h) throws IOException {
    this.xml.openElement("map", !h.isEmpty());
    for (Enumeration e = h.keys(); e.hasMoreElements();) {
      Object key = e.nextElement();
      Object value = h.get(key);
      this.xml.openElement("element");
      this.xml.openElement("key");
      serialize(key, "key");
      this.xml.closeElement();
      this.xml.openElement("value");
      serialize(value, "value");
      this.xml.closeElement();
      this.xml.closeElement();
    }
    this.xml.closeElement();
  }

  /**
   * Serialises the given object to xml by using the public methods <code>getXXX()</code>.
   *
   * <p>This method calls every <code>getXXX()</code> method from the object to get the 
   * returned object and then calls the {@link #serialize(Object, String)} method with 
   * the returned object and the name <i>xxx</i> in lower case.
   *
   * @param o The object to be serialised as XML
   * 
   * @throws IOException Should an I/O error occur.
   */
  public void serializeObject(Object o) throws IOException {
    if (o instanceof XMLSerializable) {
      try {
        Object[] args = new Object[0]; // required by the invoke method
        Class cls = o.getClass();
        Method[] meth = cls.getMethods();
        for (int i = 0; i < meth.length; i++) {
          String methodName = meth[i].getName();
          if (methodName.startsWith("get") && !(methodName.equals("getClass"))) {
            Object retObj = meth[i].invoke(o, args);
            String attribute = methodName.substring(3).toLowerCase();
            this.serialize(retObj, attribute);
          }
        }
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      } catch (InvocationTargetException ex) {
        ex.getTargetException().printStackTrace();
      }
    } else if (o instanceof XMLWritable) {
      ((XMLWritable)o).toXML(this.xml);
    }
  }

}

