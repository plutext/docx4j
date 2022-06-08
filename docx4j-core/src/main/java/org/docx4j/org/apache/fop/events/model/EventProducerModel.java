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

package org.docx4j.org.apache.fop.events.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.xmlgraphics.util.XMLizable;

/**
 * Represents the model of an event producer with multiple event methods.
 */
public class EventProducerModel implements Serializable, XMLizable {

    private static final long serialVersionUID = 122267104123721902L;

    private String interfaceName;
    private Map methods = new java.util.LinkedHashMap();

    /**
     * Creates a new instance.
     * @param interfaceName the fully qualified interface name of the event producer
     */
    public EventProducerModel(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * Returns the fully qualified interface name of the event producer.
     * @return the fully qualified interface name
     */
    public String getInterfaceName() {
        return this.interfaceName;
    }

    /**
     * Sets the fully qualified interface name of the event producer.
     * @param name the fully qualified interface name
     */
    public void setInterfaceName(String name) {
        this.interfaceName = name;
    }

    /**
     * Adds a model instance of an event method.
     * @param method the event method model
     */
    public void addMethod(EventMethodModel method) {
        this.methods.put(method.getMethodName(), method);
    }

    /**
     * Returns the model instance of an event method for the given method name.
     * @param methodName the method name
     * @return the model instance (or null if no method with the given name exists)
     */
    public EventMethodModel getMethod(String methodName) {
        return (EventMethodModel)this.methods.get(methodName);
    }

    /**
     * Returns an iterator over the contained event producer methods.
     * @return an iterator (Iterator&lt;EventMethodModel&gt;)
     */
    public Iterator getMethods() {
        return this.methods.values().iterator();
    }

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "name", "name", "CDATA", getInterfaceName());
        String elName = "producer";
        handler.startElement("", elName, elName, atts);
        Iterator iter = getMethods();
        while (iter.hasNext()) {
            ((XMLizable)iter.next()).toSAX(handler);
        }
        handler.endElement("", elName, elName);
    }


}
