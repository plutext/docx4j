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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.util.XMLizable;

/**
 * Represents a whole event model that supports multiple event producers.
 */
public class EventModel implements Serializable, XMLizable {

    private static final long serialVersionUID = 7468592614934605082L;

    private Map producers = new java.util.LinkedHashMap();

    /**
     * Creates a new, empty event model
     */
    public EventModel() {
    }

    /**
     * Adds the model of an event producer to the event model.
     * @param producer the event producer model
     */
    public void addProducer(EventProducerModel producer) {
        this.producers.put(producer.getInterfaceName(), producer);
    }

    /**
     * Returns an iterator over the contained event producer models.
     * @return an iterator (Iterator&lt;EventProducerModel&gt;)
     */
    public Iterator getProducers() {
        return this.producers.values().iterator();
    }

    /**
     * Returns the model of an event producer with the given interface name.
     * @param interfaceName the fully qualified name of the event producer
     * @return the model instance for the event producer (or null if it wasn't found)
     */
    public EventProducerModel getProducer(String interfaceName) {
        return (EventProducerModel)this.producers.get(interfaceName);
    }

    /**
     * Returns the model of an event producer with the given interface.
     * @param clazz the interface of the event producer
     * @return the model instance for the event producer (or null if it wasn't found)
     */
    public EventProducerModel getProducer(Class clazz) {
        return getProducer(clazz.getName());
    }

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        String elName = "event-model";
        handler.startElement("", elName, elName, atts);
        Iterator iter = getProducers();
        while (iter.hasNext()) {
            ((XMLizable)iter.next()).toSAX(handler);
        }
        handler.endElement("", elName, elName);
    }

    private static void writeXMLizable(XMLizable object, File outputFile) throws IOException {
        //These two approaches do not seem to work in all environments:
        //Result res = new StreamResult(outputFile);
        //Result res = new StreamResult(outputFile.toURI().toURL().toExternalForm());
        //With an old Xalan version: file:/C:/.... --> file:\C:\.....
        OutputStream out = new java.io.FileOutputStream(outputFile);
        out = new java.io.BufferedOutputStream(out);
        Result res = new StreamResult(out);

        try {
            SAXTransformerFactory tFactory
                = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
            TransformerHandler handler = tFactory.newTransformerHandler();
            Transformer transformer = handler.getTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            handler.setResult(res);
            handler.startDocument();
            object.toSAX(handler);
            handler.endDocument();
        } catch (TransformerConfigurationException e) {
            throw new IOException(e.getMessage());
        } catch (TransformerFactoryConfigurationError e) {
            throw new IOException(e.getMessage());
        } catch (SAXException e) {
            throw new IOException(e.getMessage());
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * Saves this event model to an XML file.
     * @param modelFile the target file
     * @throws IOException if an I/O error occurs
     */
    public void saveToXML(File modelFile) throws IOException {
        writeXMLizable(this, modelFile);
    }

}
