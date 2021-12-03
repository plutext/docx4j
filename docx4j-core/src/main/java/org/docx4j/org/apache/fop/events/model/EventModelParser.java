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

import java.util.Stack;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.docx4j.org.apache.fop.util.DefaultErrorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a parser for the event model XML.
 */
public final class EventModelParser {

    private EventModelParser() {
    }

    /** Logger instance */
    private static final Logger LOG = LoggerFactory.getLogger(EventModelParser.class);

    private static SAXTransformerFactory tFactory
        = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

    /**
     * Parses an event model file into an EventModel instance.
     * @param src the Source instance pointing to the XML file
     * @return the created event model structure
     * @throws TransformerException if an error occurs while parsing the XML file
     */
    public static EventModel parse(Source src)
            throws TransformerException {
        Transformer transformer = tFactory.newTransformer();
        transformer.setErrorListener(new DefaultErrorListener(LOG));

        EventModel model = new EventModel();
        SAXResult res = new SAXResult(getContentHandler(model));

        transformer.transform(src, res);
        return model;
    }

    /**
     * Creates a new ContentHandler instance that you can send the event model XML to. The parsed
     * content is accumulated in the model structure.
     * @param model the EventModel
     * @return the ContentHandler instance to receive the SAX stream from the XML file
     */
    public static ContentHandler getContentHandler(EventModel model) {
        return new Handler(model);
    }

    private static class Handler extends DefaultHandler {

        private EventModel model;
        private Stack objectStack = new Stack();

        public Handler(EventModel model) {
            this.model = model;
        }

        /** {@inheritDoc} */
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws SAXException {
            try {
                if ("event-model".equals(localName)) {
                    if (objectStack.size() > 0) {
                        throw new SAXException("event-model must be the root element");
                    }
                    objectStack.push(model);
                } else if ("producer".equals(localName)) {
                    EventProducerModel producer = new EventProducerModel(
                            attributes.getValue("name"));
                    EventModel parent = (EventModel)objectStack.peek();
                    parent.addProducer(producer);
                    objectStack.push(producer);
                } else if ("method".equals(localName)) {
                    EventSeverity severity = EventSeverity.valueOf(attributes.getValue("severity"));
                    String ex = attributes.getValue("exception");
                    EventMethodModel method = new EventMethodModel(
                            attributes.getValue("name"), severity);
                    if (ex != null && ex.length() > 0) {
                        method.setExceptionClass(ex);
                    }
                    EventProducerModel parent = (EventProducerModel)objectStack.peek();
                    parent.addMethod(method);
                    objectStack.push(method);
                } else if ("parameter".equals(localName)) {
                    String className = attributes.getValue("type");
                    Class type;
                    try {
                        type = Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new SAXException("Could not find Class for: " + className, e);
                    }
                    String name = attributes.getValue("name");
                    EventMethodModel parent = (EventMethodModel)objectStack.peek();
                    objectStack.push(parent.addParameter(type, name));
                } else {
                    throw new SAXException("Invalid element: " + qName);
                }
            } catch (ClassCastException cce) {
                throw new SAXException("XML format error: " + qName, cce);
            }
        }

        /** {@inheritDoc} */
        public void endElement(String uri, String localName, String qName) throws SAXException {
            objectStack.pop();
        }

    }

}
