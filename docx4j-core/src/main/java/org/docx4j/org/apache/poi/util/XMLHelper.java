/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */

/* ====================================================================
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

//    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==================================================================== */

package org.docx4j.org.apache.poi.util;

// Latest commit: https://github.com/apache/poi/commit/47118082ffbe065bffca3717f4d53e348c66e8b1

import static javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static javax.xml.stream.XMLInputFactory.IS_NAMESPACE_AWARE;
import static javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES;
import static javax.xml.stream.XMLInputFactory.IS_VALIDATING;
import static javax.xml.stream.XMLInputFactory.SUPPORT_DTD;
import static javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
* Helper methods for working with javax.xml classes.
*
* @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">OWASP XXE</a>
*/
@Internal
public final class XMLHelper {
 static final String FEATURE_LOAD_DTD_GRAMMAR = "http://apache.org/xml/features/nonvalidating/load-dtd-grammar";
 static final String FEATURE_LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
 static final String FEATURE_DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";
 static final String FEATURE_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
 static final String FEATURE_EXTERNAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
 static final String PROPERTY_ENTITY_EXPANSION_LIMIT = "http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit";
 static final String PROPERTY_SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
 static final String METHOD_ENTITY_EXPANSION_XERCES = "setEntityExpansionLimit";

 static final String[] SECURITY_MANAGERS = {
         //"com.sun.org.apache.xerces.internal.util.SecurityManager",
         "org.apache.xerces.util.SecurityManager"
 };


private static Logger LOG =LoggerFactory.getLogger(XMLHelper.class);	

 private static long lastLog;

 // DocumentBuilderFactory.newDocumentBuilder is thread-safe
 // see https://stackoverflow.com/questions/12455602/is-documentbuilder-thread-safe
 private static final DocumentBuilderFactory documentBuilderFactory = getDocumentBuilderFactory();

 private static final SAXParserFactory saxFactory = getSaxParserFactory();

 @FunctionalInterface
 private interface SecurityFeature {
     void accept(String name, boolean value) throws ParserConfigurationException, SAXException, TransformerException;
 }

 @FunctionalInterface
 private interface SecurityProperty {
     void accept(String name, Object value) throws SAXException;
 }

 private XMLHelper() {
 }

 /**
  * Creates a new DocumentBuilderFactory, with sensible defaults
  */
 @SuppressWarnings({"squid:S2755"})
 public static DocumentBuilderFactory getDocumentBuilderFactory() {
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     factory.setNamespaceAware(true);
     // this doesn't appear to work, and we still need to limit
     // entity expansions to 1 in trySet(XercesSecurityManager)
     factory.setExpandEntityReferences(false);
     factory.setValidating(false);
     trySet(factory::setFeature, FEATURE_SECURE_PROCESSING, true);
     trySet(factory::setAttribute, ACCESS_EXTERNAL_SCHEMA, "");
     trySet(factory::setAttribute, ACCESS_EXTERNAL_DTD, "");
     trySet(factory::setFeature, FEATURE_EXTERNAL_ENTITIES, false);
     trySet(factory::setFeature, FEATURE_PARAMETER_ENTITIES, false);
     trySet(factory::setFeature, FEATURE_LOAD_EXTERNAL_DTD, false);
     trySet(factory::setFeature, FEATURE_LOAD_DTD_GRAMMAR, false);
     trySet(factory::setFeature, FEATURE_DISALLOW_DOCTYPE_DECL, true);
     trySet((n, b) -> factory.setXIncludeAware(b), "XIncludeAware", false);

     Object manager = getXercesSecurityManager();
     if (manager == null || !trySet(factory::setAttribute, PROPERTY_SECURITY_MANAGER, manager)) {
         // separate old version of Xerces not found => use the builtin way of setting the property
         // Note: when entity_expansion_limit==0, there is no limit!
         trySet(factory::setAttribute, PROPERTY_ENTITY_EXPANSION_LIMIT, 1);
     }

     return factory;
 }

 /**
  * Creates a new document builder, with sensible defaults
  *
  * @throws IllegalStateException If creating the DocumentBuilder fails, e.g.
  *                               due to {@link ParserConfigurationException}.
  */
 public static DocumentBuilder newDocumentBuilder() {
     try {
         DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
         documentBuilder.setEntityResolver(XMLHelper::ignoreEntity);
         documentBuilder.setErrorHandler(new DocHelperErrorHandler());
         return documentBuilder;
     } catch (ParserConfigurationException e) {
         throw new IllegalStateException("cannot create a DocumentBuilder", e);
     }
 }

 @SuppressWarnings("squid:S2755")
 public static SAXParserFactory getSaxParserFactory() {
     try {
         SAXParserFactory factory = SAXParserFactory.newInstance();
         factory.setValidating(false);
         factory.setNamespaceAware(true);
         trySet(factory::setFeature, FEATURE_SECURE_PROCESSING, true);
         trySet(factory::setFeature, FEATURE_LOAD_DTD_GRAMMAR, false);
         trySet(factory::setFeature, FEATURE_LOAD_EXTERNAL_DTD, false);
         trySet(factory::setFeature, FEATURE_EXTERNAL_ENTITIES, false);
         trySet(factory::setFeature, FEATURE_DISALLOW_DOCTYPE_DECL, true);
         return factory;
     } catch (RuntimeException | Error re) { // NOSONAR
         // this also catches NoClassDefFoundError, which may be due to a local class path issue
         // This may occur if the code is run inside a web container or a restricted JVM
         // See bug 61170: https://bz.apache.org/bugzilla/show_bug.cgi?id=61170
         logThrowable(re, "Failed to create SAXParserFactory", "-");
         throw re;
     } catch (Exception e) {
         logThrowable(e, "Failed to create SAXParserFactory", "-");
         throw new RuntimeException("Failed to create SAXParserFactory", e);
     }
 }

 /**
  * Creates a new SAX XMLReader, with sensible defaults
  */
 public static XMLReader newXMLReader() throws SAXException, ParserConfigurationException {
     XMLReader xmlReader = saxFactory.newSAXParser().getXMLReader();
     xmlReader.setEntityResolver(XMLHelper::ignoreEntity);
     trySet(xmlReader::setFeature, FEATURE_SECURE_PROCESSING, true);
     trySet(xmlReader::setFeature, FEATURE_EXTERNAL_ENTITIES, false);
     Object manager = getXercesSecurityManager();
     if (manager == null || !trySet(xmlReader::setProperty, PROPERTY_SECURITY_MANAGER, manager)) {
         // separate old version of Xerces not found => use the builtin way of setting the property
         trySet(xmlReader::setProperty, PROPERTY_ENTITY_EXPANSION_LIMIT, 1);
     }
     return xmlReader;
 }

 /**
  * Creates a new StAX XMLInputFactory, with sensible defaults
  */
 @SuppressWarnings({"squid:S2755"})
 public static XMLInputFactory newXMLInputFactory() {
     XMLInputFactory factory = XMLInputFactory.newInstance();
     trySet(factory::setProperty, IS_NAMESPACE_AWARE, true);
     trySet(factory::setProperty, IS_VALIDATING, false);
     trySet(factory::setProperty, SUPPORT_DTD, false);
     trySet(factory::setProperty, IS_SUPPORTING_EXTERNAL_ENTITIES, false);
     return factory;
 }

 /**
  * Creates a new StAX XMLOutputFactory, with sensible defaults
  */
 public static XMLOutputFactory newXMLOutputFactory() {
     XMLOutputFactory factory = XMLOutputFactory.newInstance();
     trySet(factory::setProperty, IS_REPAIRING_NAMESPACES, true);
     return factory;
 }

 /**
  * Creates a new StAX XMLEventFactory, with sensible defaults
  */
 public static XMLEventFactory newXMLEventFactory() {
     // this method seems safer on Android than getFactory()
     return XMLEventFactory.newInstance();
 }

 @SuppressWarnings({"squid:S4435","java:S2755"})
 public static TransformerFactory getTransformerFactory() {
     TransformerFactory factory = TransformerFactory.newInstance();
     trySet(factory::setFeature, FEATURE_SECURE_PROCESSING, true);
     trySet(factory::setAttribute, ACCESS_EXTERNAL_DTD, "");
     trySet(factory::setAttribute, ACCESS_EXTERNAL_STYLESHEET, "");
     quietSet(factory::setAttribute, ACCESS_EXTERNAL_SCHEMA, "");
     return factory;
 }

 public static Transformer newTransformer() throws TransformerConfigurationException {
     Transformer serializer = getTransformerFactory().newTransformer();
     // TODO set encoding from a command argument
     serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
     serializer.setOutputProperty(OutputKeys.INDENT, "no");
     serializer.setOutputProperty(OutputKeys.METHOD, "xml");
     return serializer;
 }

 @SuppressWarnings("java:S2755")
 public static SchemaFactory getSchemaFactory() {
     SchemaFactory factory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
     trySet(factory::setFeature, FEATURE_SECURE_PROCESSING, true);
     trySet(factory::setProperty, ACCESS_EXTERNAL_DTD, "");
     trySet(factory::setProperty, ACCESS_EXTERNAL_STYLESHEET, "");
     trySet(factory::setProperty, ACCESS_EXTERNAL_SCHEMA, "");
     return factory;
 }


 private static Object getXercesSecurityManager() {
     // Try built-in JVM one first, standalone if not
     for (String securityManagerClassName : SECURITY_MANAGERS) {
         try {
             Object mgr = Class.forName(securityManagerClassName).getDeclaredConstructor().newInstance();
             Method setLimit = mgr.getClass().getMethod(METHOD_ENTITY_EXPANSION_XERCES, Integer.TYPE);
             setLimit.invoke(mgr, 1);
             // Stop once one can be setup without error
             return mgr;
         } catch (ClassNotFoundException ignored) {
             // continue without log, this is expected in some setups
         } catch (Throwable e) {     // NOSONAR - also catch things like NoClassDefError here
             logThrowable(e, "SAX Feature unsupported", securityManagerClassName);
         }
     }

     return null;
 }

 @SuppressWarnings("UnusedReturnValue")
 private static boolean trySet(SecurityFeature feature, String name, boolean value) {
     try {
         feature.accept(name, value);
         return true;
     } catch (Exception e) {
         logThrowable(e, "SAX Feature unsupported", name);
     } catch (Error ame) {
         logThrowable(ame, "Cannot set SAX feature because outdated XML parser in classpath", name);
     }
     return false;
 }

 private static boolean trySet(SecurityProperty property, String name, Object value) {
     try {
         property.accept(name, value);
         return true;
     } catch (Exception e) {
         logThrowable(e, "SAX Feature unsupported", name);
     } catch (Error ame) {
         // ignore all top error object - GraalVM in native mode is not coping with java.xml error message resources
         logThrowable(ame, "Cannot set SAX feature because outdated XML parser in classpath", name);
     }
     return false;
 }

 private static boolean quietSet(SecurityProperty property, String name, Object value) {
     try {
         property.accept(name, value);
         return true;
     } catch (Exception|Error e) {
         // ok to ignore
     }
     return false;
 }

 private static void logThrowable(Throwable t, String message, String name) {
     if (System.currentTimeMillis() > lastLog + TimeUnit.MINUTES.toMillis(5)) {
         LOG.warn("{} [log suppressed for 5 minutes]{}", message, name, t);
         lastLog = System.currentTimeMillis();
     }
 }

 private static class DocHelperErrorHandler implements ErrorHandler {

     public void warning(SAXParseException exception) {
         printError(Level.WARN, exception);
     }

     public void error(SAXParseException exception) {
         printError(Level.ERROR, exception);
     }

     public void fatalError(SAXParseException exception) throws SAXException {
         printError(Level.ERROR, exception);
         throw exception;
     }

     /**
      * Prints the error message.
      */
     private void printError(Level type, SAXParseException ex) {
         String systemId = ex.getSystemId();
         if (systemId != null) {
             int index = systemId.lastIndexOf('/');
             if (index != -1) {
                 systemId = systemId.substring(index + 1);
             }
         }
         String message = (systemId == null ? "" : systemId) +
                 ':' + ex.getLineNumber() +
                 ':' + ex.getColumnNumber() +
                 ':' + ex.getMessage();

//         LOG.atLevel(type).withThrowable(ex).log(message);
         if (LOG.isDebugEnabled()) {
        	 LOG.debug(message, ex);
         } else if(LOG.isInfoEnabled()) {
        	 LOG.info(message, ex);        	 
         }
        		 
     }
     
 }

 private static InputSource ignoreEntity(String publicId, String systemId) {
     return new InputSource(new StringReader(""));
 }
}