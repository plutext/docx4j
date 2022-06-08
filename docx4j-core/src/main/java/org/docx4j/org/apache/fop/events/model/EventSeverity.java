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

import java.io.ObjectStreamException;
import java.io.Serializable;

/** Enumeration class for event severities. */
public final class EventSeverity implements Serializable {

    private static final long serialVersionUID = 4108175215810759243L;

    /** info level */
    public static final EventSeverity INFO = new EventSeverity("INFO");
    /** warning level */
    public static final EventSeverity WARN = new EventSeverity("WARN");
    /** error level */
    public static final EventSeverity ERROR = new EventSeverity("ERROR");
    /** fatal error */
    public static final EventSeverity FATAL = new EventSeverity("FATAL");

    private String name;

    /**
     * Constructor to add a new named item.
     * @param name Name of the item.
     */
    private EventSeverity(String name) {
        this.name = name;
    }

    /** @return the name of the enumeration */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the enumeration/singleton object based on its name.
     * @param name the name of the enumeration value
     * @return the enumeration object
     */
    public static EventSeverity valueOf(String name) {
        if (INFO.getName().equalsIgnoreCase(name)) {
            return INFO;
        } else if (WARN.getName().equalsIgnoreCase(name)) {
            return WARN;
        } else if (ERROR.getName().equalsIgnoreCase(name)) {
            return ERROR;
        } else if (FATAL.getName().equalsIgnoreCase(name)) {
            return FATAL;
        } else {
            throw new IllegalArgumentException("Illegal value for enumeration: " + name);
        }
    }

    private Object readResolve() throws ObjectStreamException {
        return valueOf(getName());
    }

    /** {@inheritDoc} */
    public String toString() {
        return "EventSeverity:" + name;
    }

}
