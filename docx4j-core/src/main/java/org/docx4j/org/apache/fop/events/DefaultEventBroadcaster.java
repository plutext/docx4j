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

package org.docx4j.org.apache.fop.events;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.docx4j.org.apache.fop.events.model.EventMethodModel;
import org.docx4j.org.apache.fop.events.model.EventModel;
import org.docx4j.org.apache.fop.events.model.EventModelParser;
import org.docx4j.org.apache.fop.events.model.EventProducerModel;
import org.docx4j.org.apache.fop.events.model.EventSeverity;

/**
 * Default implementation of the EventBroadcaster interface. It holds a list of event listeners
 * and can provide {@link EventProducer} instances for type-safe event production.
 */
public class DefaultEventBroadcaster implements EventBroadcaster {

    /** Holds all registered event listeners */
    protected CompositeEventListener listeners = new CompositeEventListener();

    /** {@inheritDoc} */
    public void addEventListener(EventListener listener) {
        this.listeners.addEventListener(listener);
    }

    /** {@inheritDoc} */
    public void removeEventListener(EventListener listener) {
        this.listeners.removeEventListener(listener);
    }

    /** {@inheritDoc} */
    public boolean hasEventListeners() {
        return this.listeners.hasEventListeners();
    }

    /** {@inheritDoc} */
    public void broadcastEvent(Event event) {
        this.listeners.processEvent(event);
    }

    private static List<EventModel> eventModels = new java.util.ArrayList();
    private Map proxies = new java.util.HashMap();

    /**
     * Loads an event model and returns its instance.
     * @param resourceBaseClass base class to use for loading resources
     * @return the newly loaded event model.
     */
    private static EventModel loadModel(Class resourceBaseClass) {
        String resourceName = "event-model.xml";
        InputStream in = resourceBaseClass.getResourceAsStream(resourceName);
        if (in == null) {
            throw new MissingResourceException(
                    "File " + resourceName + " not found",
                    DefaultEventBroadcaster.class.getName(), "");
        }
        try {
            return EventModelParser.parse(new StreamSource(in));
        } catch (TransformerException e) {
            throw new MissingResourceException(
                    "Error reading " + resourceName + ": " + e.getMessage(),
                    DefaultEventBroadcaster.class.getName(), "");
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Adds a new {@link EventModel} to the list of registered event models.
     * @param eventModel the event model instance
     */
    public static synchronized void addEventModel(EventModel eventModel) {
        eventModels.add(eventModel);
    }

    private static synchronized EventProducerModel getEventProducerModel(Class clazz) {
        for (Object eventModel1 : eventModels) {
            EventModel eventModel = (EventModel) eventModel1;
            EventProducerModel producerModel = eventModel.getProducer(clazz);
            if (producerModel != null) {
                return producerModel;
            }
        }
        EventModel model = loadModel(clazz);
        addEventModel(model);
        return model.getProducer(clazz);
    }

    /** {@inheritDoc} */
    public EventProducer getEventProducerFor(Class clazz) {
        if (!EventProducer.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(
                    "Class must be an implementation of the EventProducer interface: "
                    + clazz.getName());
        }
        EventProducer producer;
        producer = (EventProducer)this.proxies.get(clazz);
        if (producer == null) {
            producer = createProxyFor(clazz);
            this.proxies.put(clazz, producer);
        }
        return producer;
    }

    /**
     * Creates a dynamic proxy for the given EventProducer interface that will handle the
     * conversion of the method call into the broadcasting of an event instance.
     * @param clazz a descendant interface of EventProducer
     * @return the EventProducer instance
     */
    protected EventProducer createProxyFor(Class clazz) {
        final EventProducerModel producerModel = getEventProducerModel(clazz);
        if (producerModel == null) {
            throw new IllegalStateException("Event model doesn't contain the definition for "
                    + clazz.getName());
        }
        return (EventProducer)Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[] {clazz},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args)
                            throws Throwable {
                        String methodName = method.getName();
                        EventMethodModel methodModel = producerModel.getMethod(methodName);
                        String eventID = producerModel.getInterfaceName() + '.' + methodName;
                        if (methodModel == null) {
                            throw new IllegalStateException(
                                    "Event model isn't consistent"
                                    + " with the EventProducer interface. Please rebuild FOP!"
                                    + " Affected method: "
                                    + eventID);
                        }
                        Map params = new java.util.HashMap();
                        int i = 1;
                        for (Object o : methodModel.getParameters()) {
                            EventMethodModel.Parameter param
                                    = (EventMethodModel.Parameter) o;
                            params.put(param.getName(), args[i]);
                            i++;
                        }
                        Event ev = new Event(args[0], eventID, methodModel.getSeverity(), params);
                        broadcastEvent(ev);

                        if (ev.getSeverity() == EventSeverity.FATAL) {
                            EventExceptionManager.throwException(ev,
                                    methodModel.getExceptionClass());
                        }
                        return null;
                    }
                });
    }

}
