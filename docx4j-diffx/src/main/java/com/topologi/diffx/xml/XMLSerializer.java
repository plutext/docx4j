/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.xml;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

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
  private final XMLWriter xml;

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
      if (name.lastIndexOf('.') != -1) {
        name = name.substring(name.lastIndexOf('.')+1);
      }
      if (name.lastIndexOf('$') != -1) {
        name = name.substring(name.lastIndexOf('$')+1);
      }
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
      } else if (o instanceof Collection<?>) {
        this.xml.openElement(name, ((Collection<?>)o).size() != 0);
        serializeCollection((Collection<?>)o);
        this.xml.closeElement();
        // hashtable
      } else if (o instanceof Hashtable<?,?>) {
        this.xml.openElement(name, ((Hashtable<?,?>)o).size() != 0);
        serializeHashtable((Hashtable<?,?>)o);
        this.xml.closeElement();
        // other objects
      } else {
        this.xml.openElement(name, true);
        serializeObject(o);
        this.xml.closeElement();
      }
    }
  }

  /**
   * Serialises the given Collection to xml.
   *
   * <p>Iterates over every object and call the {@link #serialize} method.
   *
   * @param c The Collection to be serialised to XML
   *
   * @throws IOException Should an I/O error occur.
   */
  public void serializeCollection(Collection<?> c) throws IOException {
    for (Object o : c) {
      serialize(o, o.getClass().getName());
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
  public void serializeHashtable(Hashtable<?, ?> h) throws IOException {
    this.xml.openElement("map", !h.isEmpty());
    for (Enumeration<?> e = h.keys(); e.hasMoreElements();) {
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
        Class<?> cls = o.getClass();
        Method[] meth = cls.getMethods();
        for (Method element : meth) {
          String methodName = element.getName();
          if (methodName.startsWith("get") && !"getClass".equals(methodName)) {
            Object retObj = element.invoke(o, args);
            String attribute = methodName.substring(3).toLowerCase();
            serialize(retObj, attribute);
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

