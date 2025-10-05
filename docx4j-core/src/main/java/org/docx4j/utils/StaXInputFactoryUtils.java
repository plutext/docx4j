/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
 
 /**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.docx4j.utils;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This code comes from https://github.com/apache/tika/blob/main/tika-core/src/main/java/org/apache/tika/utils/XMLReaderUtils.java 
 * at https://github.com/apache/tika/commit/16de8cc0efcdb21785a448d1c2dbd8a1c925dfc2
 * 
 * It is potentially more comprehensive XXE hardening than what we had previously.
 * 
 * @author jharrop
 * @since 11.5.6
 */
public class StaXInputFactoryUtils {
	
	protected static Logger log = LoggerFactory.getLogger(StaXInputFactoryUtils.class);	

    public static final int DEFAULT_MAX_ENTITY_EXPANSIONS = 20;
    private static final String JAXP_ENTITY_EXPANSION_LIMIT_KEY = "jdk.xml.entityExpansionLimit";
    private static final AtomicBoolean HAS_WARNED_STAX = new AtomicBoolean(false);
    private static final AtomicBoolean HAS_WARNED_ACCESS_EXTERNAL_DTD = new AtomicBoolean(false);
	
    /**
     * Returns the StAX input factory specified in this parsing context.
     * If a factory is not explicitly specified, then a default factory
     * instance is created and returned. The default factory instance is
     * configured to be namespace-aware and to apply reasonable security
     * precautions.
     *
     * @return StAX input factory
     * @since Apache Tika 1.13
     */
    public static XMLInputFactory getXMLInputFactory() {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        if (log.isDebugEnabled()) {
        	log.debug("XMLInputFactory class {}", factory.getClass());
        	// Default in Java 17 is the Oracle/Sun StAX implementation: com.sun.xml.internal.stream.XMLInputFactoryImpl
        }

        tryToSetStaxProperty(factory, XMLInputFactory.IS_NAMESPACE_AWARE, true);  // @since 11.5.6

        //try to configure secure processing
        tryToSetStaxProperty(factory, XMLConstants.ACCESS_EXTERNAL_DTD, ""); // @since 11.5.6
        	// Woodstox 7.1.0 doesn't support that
        tryToSetStaxProperty(factory, XMLInputFactory.IS_VALIDATING, false); // @since 11.5.6
        
        // docx4j has done this for a long time
        tryToSetStaxProperty(factory, XMLInputFactory.SUPPORT_DTD, false);  // a DTD is merely ignored, its presence doesn't cause an exception
        tryToSetStaxProperty(factory, XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

        //defense in depth
        factory.setXMLResolver(IGNORING_STAX_ENTITY_RESOLVER); // @since 11.5.6
        trySetStaxSecurityManager(factory);  // @since 11.5.6
        return factory;
    }
    
    
    //BE CAREFUL with the return type. Some parsers will silently ignore an unexpected return type: CVE-2025-54988
    private static final XMLResolver IGNORING_STAX_ENTITY_RESOLVER =
            (publicID, systemID, baseURI, namespace) ->
                    UnsynchronizedByteArrayInputStream.nullInputStream();
                    
    private static volatile int MAX_ENTITY_EXPANSIONS = determineMaxEntityExpansions();
    
    private static int determineMaxEntityExpansions() {
        String expansionLimit = System.getProperty(JAXP_ENTITY_EXPANSION_LIMIT_KEY);
        if (expansionLimit != null) {
            try {
                return Integer.parseInt(expansionLimit);
            } catch (NumberFormatException e) {
                log.warn(
                        "Couldn't parse an integer for the entity expansion limit: {}; " +
                                "backing off to default: {}",
                        expansionLimit, DEFAULT_MAX_ENTITY_EXPANSIONS);
            }
        }
        return DEFAULT_MAX_ENTITY_EXPANSIONS;
    } 
    
    private static void trySetStaxSecurityManager(XMLInputFactory inputFactory) {
        //try default java entity expansion, then fallback to woodstox, then warn...once.
        try {
            inputFactory.setProperty("http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit",
                    MAX_ENTITY_EXPANSIONS);
        } catch (IllegalArgumentException e) {
            try {
                inputFactory.setProperty("com.ctc.wstx.maxEntityCount", MAX_ENTITY_EXPANSIONS);
            } catch (IllegalArgumentException e2) {
                if (HAS_WARNED_STAX.getAndSet(true) == false) {
                	log.warn("Could not set limit on maximum entity expansions for: " + inputFactory.getClass());
                }
            }

        }
    }    
    private static void tryToSetStaxProperty(XMLInputFactory factory, String key, boolean value) {
        try {
            factory.setProperty(key, value);
        } catch (IllegalArgumentException e) {
        	log.warn("StAX Feature unsupported: {}", key, e);
        }
    }

    private static void tryToSetStaxProperty(XMLInputFactory factory, String key, String value) {
        try {
            factory.setProperty(key, value);
        } catch (IllegalArgumentException e) {
        	if (key.equals(XMLConstants.ACCESS_EXTERNAL_DTD)) {
                if (HAS_WARNED_ACCESS_EXTERNAL_DTD.getAndSet(true) == false) {
            		log.warn("StAX Feature unsupported: {}", key, e);
                }        	
        	} else {
        		log.warn("StAX Feature unsupported: {}", key, e);
        	}
        }
    }    
	
}
