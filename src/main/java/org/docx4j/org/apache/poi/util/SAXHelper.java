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

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.docx4j.org.apache.poi.util;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * Provides handy methods for working with SAX parsers and readers
 */
public final class SAXHelper {

	private static Logger logger = LoggerFactory.getLogger(SAXHelper.class);	
	

    private SAXHelper() {}

    /**
     * Creates a new SAX XMLReader, with sensible defaults
     */
    public static synchronized XMLReader newXMLReader() throws SAXException, ParserConfigurationException {
        XMLReader xmlReader = saxFactory.newSAXParser().getXMLReader();
        xmlReader.setEntityResolver(IGNORING_ENTITY_RESOLVER);
        trySetSAXFeature(xmlReader, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        trySetXercesSecurityManager(xmlReader);
        return xmlReader;
    }
    
    static final EntityResolver IGNORING_ENTITY_RESOLVER = new EntityResolver() {
        @Override
        public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException, IOException {
            return new InputSource(new StringReader(""));
        }
    };
    
    private static final SAXParserFactory saxFactory;
    static {
        saxFactory = SAXParserFactory.newInstance();
        saxFactory.setValidating(false);
        saxFactory.setNamespaceAware(true);
    }
            
    private static void trySetSAXFeature(XMLReader xmlReader, String feature, boolean enabled) {
        try {
            xmlReader.setFeature(feature, enabled);
        } catch (Exception e) {
            logger.warn("SAX Feature unsupported "+ feature, e);
        } catch (AbstractMethodError ame) {
            logger.warn( "Cannot set SAX feature because outdated XML parser in classpath" + feature, ame);
        }
    }
    
    private static void trySetXercesSecurityManager(XMLReader xmlReader) {
        // Try built-in JVM one first, standalone if not
        for (String securityManagerClassName : new String[] {
                "com.sun.org.apache.xerces.internal.util.SecurityManager",
                "org.apache.xerces.util.SecurityManager"
        }) {
            try {
                Object mgr = Class.forName(securityManagerClassName).newInstance();
                Method setLimit = mgr.getClass().getMethod("setEntityExpansionLimit", Integer.TYPE);
                setLimit.invoke(mgr, 4096);
                xmlReader.setProperty("http://apache.org/xml/properties/security-manager", mgr);
                // Stop once one can be setup without error
                return;
            } catch (Exception e) {
                logger.warn( "SAX Security Manager could not be setup", e);
            }
        }
    }
}
