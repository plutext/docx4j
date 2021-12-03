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

/**
 * The EventBroadcaster is the central relay point for events. It receives events from various
 * parts of the application and forwards them to any registered EventListener.
 */
public interface EventBroadcaster {

    /**
     * Adds an event listener to the broadcaster. It is appended to the list of previously
     * registered listeners (the order of registration defines the calling order).
     * @param listener the listener to be added
     */
    void addEventListener(EventListener listener);

    /**
     * Removes an event listener from the broadcaster. If the event listener is not registered,
     * nothing happens.
     * @param listener the listener to be removed
     */
    void removeEventListener(EventListener listener);

    /**
     * Indicates whether any listeners have been registered with the broadcaster.
     * @return true if listeners are present, false otherwise
     */
    boolean hasEventListeners();

    /**
     * Broadcasts an event. This method is usually called from within the observed component.
     * @param event the event to be broadcast
     */
    void broadcastEvent(Event event);

    /**
     * Returns an event producer instance for the given interface class.
     * @param clazz the Class object identifying an {@link EventProducer} interface
     * @return the event producer instance
     */
    <T extends EventProducer> T getEventProducerFor(Class<T> clazz);

}
