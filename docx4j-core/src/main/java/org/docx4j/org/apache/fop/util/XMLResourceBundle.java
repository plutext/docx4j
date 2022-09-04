/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
 
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.docx4j.org.apache.fop.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Stack;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.xmlgraphics.util.QName;

/**
 * This class is a ResourceBundle that loads its contents from XML files instead of properties
 * files (like PropertiesResourceBundle).
 * <p>
 * The XML format for this resource bundle implementation is the following
 * (the same as Apache Cocoon's XMLResourceBundle):
 * <pre>
 * &lt;catalogue xml:lang="en"&gt;
 *   &lt;message key="key1"&gt;Message &lt;br/&gt; Value 1&lt;/message&gt;
 *   &lt;message key="key2"&gt;Message &lt;br/&gt; Value 1&lt;/message&gt;
 *   ...
 * &lt;/catalogue&gt;
 * </pre>
 */
public class XMLResourceBundle extends ResourceBundle {

    //Note: Some code here has been copied and adapted from Apache Harmony!

    private Properties resources = new Properties();

    private Locale locale;

    private static SAXTransformerFactory tFactory
        = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

    /**
     * Creates a resource bundle from an InputStream.
     * @param in the stream to read from
     * @throws IOException if an I/O error occurs
     */
    public XMLResourceBundle(InputStream in) throws IOException {
        try {
            Transformer transformer = tFactory.newTransformer();
            StreamSource src = new StreamSource(in);
            SAXResult res = new SAXResult(new CatalogueHandler());
            transformer.transform(src, res);
        } catch (TransformerException e) {
            throw new IOException("Error while parsing XML resource bundle: " + e.getMessage());
        }
    }

    /**
     * Gets a resource bundle using the specified base name, default locale, and class loader.
     * @param baseName the base name of the resource bundle, a fully qualified class name
     * @param loader the class loader from which to load the resource bundle
     * @return a resource bundle for the given base name and the default locale
     * @throws MissingResourceException if no resource bundle for the specified base name can be
     *                                          found
     * @see java.util.ResourceBundle#getBundle(String)
     */
    public static ResourceBundle getXMLBundle(String baseName, ClassLoader loader)
                throws MissingResourceException {
        return getXMLBundle(baseName, Locale.getDefault(), loader);
    }

    /**
     * Gets a resource bundle using the specified base name, locale, and class loader.
     * @param baseName the base name of the resource bundle, a fully qualified class name
     * @param locale the locale for which a resource bundle is desired
     * @param loader the class loader from which to load the resource bundle
     * @return a resource bundle for the given base name and locale
     * @throws MissingResourceException if no resource bundle for the specified base name can be
     *                                          found
     * @see java.util.ResourceBundle#getBundle(String, Locale, ClassLoader)
     */
    public static ResourceBundle getXMLBundle(String baseName, Locale locale, ClassLoader loader)
                throws MissingResourceException {
        if (loader == null) {
            throw new NullPointerException("loader must not be null");
        }
        if (baseName == null) {
            throw new NullPointerException("baseName must not be null");
        }
        assert locale != null;
        ResourceBundle bundle;
        if (!locale.equals(Locale.getDefault())) {
            bundle = handleGetXMLBundle(baseName, "_" + locale, false, loader);
            if (bundle != null) {
                return bundle;
            }
        }
        bundle = handleGetXMLBundle(baseName, "_" + Locale.getDefault(), true, loader);
        if (bundle != null) {
            return bundle;
        }
        throw new MissingResourceException(
                baseName + " (" + locale + ')', baseName + '_' + locale, null);
    }

    static class MissingBundle extends ResourceBundle {
        public Enumeration getKeys() {
            return null;
        }

        public Object handleGetObject(String name) {
            return null;
        }
    }

    private static final ResourceBundle MISSING = new MissingBundle();
    private static final ResourceBundle MISSINGBASE = new MissingBundle();

    private static Map cache = new java.util.WeakHashMap();
    //<Object, Hashtable<String, ResourceBundle>>

    private static ResourceBundle handleGetXMLBundle(String base, String locale,
            boolean loadBase, final ClassLoader loader) {
        XMLResourceBundle bundle = null;
        String bundleName = base + locale;
        Object cacheKey = loader != null ? (Object) loader : (Object) "null";
        Hashtable loaderCache; //<String, ResourceBundle>
        synchronized (cache) {
            loaderCache = (Hashtable)cache.get(cacheKey);
            if (loaderCache == null) {
                loaderCache = new Hashtable();
                cache.put(cacheKey, loaderCache);
            }
        }
        ResourceBundle result = (ResourceBundle)loaderCache.get(bundleName);
        if (result != null) {
            if (result == MISSINGBASE) {
                return null;
            }
            if (result == MISSING) {
                if (!loadBase) {
                    return null;
                }
                String extension = strip(locale);
                if (extension == null) {
                    return null;
                }
                return handleGetXMLBundle(base, extension, loadBase, loader);
            }
            return result;
        }

        final String fileName = bundleName.replace('.', '/') + ".xml";
        InputStream stream = (InputStream)AccessController
                .doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        return loader == null
                            ? ClassLoader.getSystemResourceAsStream(fileName)
                            : loader.getResourceAsStream(fileName);
                    }
                });
        if (stream != null) {
            try {
                try {
                    bundle = new XMLResourceBundle(stream);
                } finally {
                    stream.close();
                }
                bundle.setLocale(locale);
            } catch (IOException e) {
                throw new MissingResourceException(e.getMessage(), base, null);
            }
        }

        String extension = strip(locale);
        if (bundle != null) {
            if (extension != null) {
                ResourceBundle parent = handleGetXMLBundle(base, extension, true,
                        loader);
                if (parent != null) {
                    bundle.setParent(parent);
                }
            }
            loaderCache.put(bundleName, bundle);
            return bundle;
        }

        if (extension != null) {
            ResourceBundle fallback = handleGetXMLBundle(base, extension, loadBase, loader);
            if (fallback != null) {
                loaderCache.put(bundleName, fallback);
                return fallback;
            }
        }
        loaderCache.put(bundleName, loadBase ? MISSINGBASE : MISSING);
        return null;
    }

    private void setLocale(String name) {
        String language = "";
        String country = "";
        String variant = "";
        if (name.length() > 1) {
            int nextIndex = name.indexOf('_', 1);
            if (nextIndex == -1) {
                nextIndex = name.length();
            }
            language = name.substring(1, nextIndex);
            if (nextIndex + 1 < name.length()) {
                int index = nextIndex;
                nextIndex = name.indexOf('_', nextIndex + 1);
                if (nextIndex == -1) {
                    nextIndex = name.length();
                }
                country = name.substring(index + 1, nextIndex);
                if (nextIndex + 1 < name.length()) {
                    variant = name.substring(nextIndex + 1);
                }
            }
        }
        this.locale = new Locale(language, country, variant);
    }

    private static String strip(String name) {
        int index = name.lastIndexOf('_');
        if (index != -1) {
            return name.substring(0, index);
        }
        return null;
    }

    private Enumeration getLocalKeys() {
        return (Enumeration)resources.propertyNames();
    }

    /** {@inheritDoc} */
    public Locale getLocale() {
        return this.locale;
    }

    /** {@inheritDoc} */
    public Enumeration getKeys() {
        if (parent == null) {
            return getLocalKeys();
        }
        return new Enumeration() {
            private Enumeration local = getLocalKeys();
            private Enumeration pEnum = parent.getKeys();

            private Object nextElement;

            private boolean findNext() {
                if (nextElement != null) {
                    return true;
                }
                while (pEnum.hasMoreElements()) {
                    Object next = pEnum.nextElement();
                    if (!resources.containsKey(next)) {
                        nextElement = next;
                        return true;
                    }
                }
                return false;
            }

            public boolean hasMoreElements() {
                if (local.hasMoreElements()) {
                    return true;
                }
                return findNext();
            }

            public Object nextElement() {
                if (local.hasMoreElements()) {
                    return local.nextElement();
                }
                if (findNext()) {
                    Object result = nextElement;
                    nextElement = null;
                    return result;
                }
                // Cause an exception
                return pEnum.nextElement();
            }
        };
    }

    /** {@inheritDoc} */
    protected Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException("key must not be null");
        }
        return resources.get(key);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "XMLResourceBundle: " + getLocale();
    }

    private class CatalogueHandler extends DefaultHandler {

        private static final String CATALOGUE = "catalogue";
        private static final String MESSAGE = "message";

        private StringBuffer valueBuffer = new StringBuffer();
        private Stack elementStack = new Stack();
        private String currentKey;

        private boolean isOwnNamespace(String uri) {
            return (uri != null && uri.isEmpty());
        }

        private QName getParentElementName() {
            return (QName)elementStack.peek();
        }

        /** {@inheritDoc} */
        public void startElement(String uri, String localName, String qName,
                Attributes atts) throws SAXException {
            super.startElement(uri, localName, qName, atts);
            QName elementName = new QName(uri, qName);
            if (isOwnNamespace(uri)) {
                if (CATALOGUE.equals(localName)) {
                    //nop
                } else if (MESSAGE.equals(localName)) {
                    if (!CATALOGUE.equals(getParentElementName().getLocalName())) {
                        throw new SAXException(MESSAGE + " must be a child of " + CATALOGUE);
                    }
                    this.currentKey = atts.getValue("key");
                } else {
                    throw new SAXException("Invalid element name: " + elementName);
                }
            } else {
                //ignore
            }
            this.valueBuffer.setLength(0);
            elementStack.push(elementName);
        }

        /** {@inheritDoc} */
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            elementStack.pop();
            if (isOwnNamespace(uri)) {
                if (CATALOGUE.equals(localName)) {
                    //nop
                } else if (MESSAGE.equals(localName)) {
                    if (this.currentKey == null) {
                        throw new SAXException(
                                "current key is null (attribute 'key' might be mistyped)");
                    }
                    resources.put(this.currentKey, this.valueBuffer.toString());
                    this.currentKey = null;
                }
            } else {
                //ignore
            }
            this.valueBuffer.setLength(0);
        }

        /** {@inheritDoc} */
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            valueBuffer.append(ch, start, length);
        }

    }

}
