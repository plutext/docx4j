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

/* $Id: EncodingMode.java 731248 2009-01-04 12:59:29Z jeremias $ */

package org.docx4j.fonts.fop.fonts;

import java.io.ObjectStreamException;
import java.io.Serializable;


/**
 * This class enumerates all supported encoding modes for fonts: 
 * auto, single-byte and CID.
 */
public final class EncodingMode implements Serializable {

    private static final long serialVersionUID = 8311486102457779529L;

    /** Automatic selection of encoding mode. */
    public static final EncodingMode AUTO = new EncodingMode("auto");

    /** Single-byte encoding */
    public static final EncodingMode SINGLE_BYTE = new EncodingMode("single-byte");

    /** CID encoding */
    public static final EncodingMode CID = new EncodingMode("cid");

    private String name;

    private EncodingMode(String name) {
        this.name = name;
    }

    /**
     * Returns the encoding mode name.
     * @return the encoding mode name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the {@link EncodingMode} by name.
     * @param name the name of the encoding mode to look up
     * @return the encoding mode constant
     */
    public static EncodingMode valueOf(String name) {
        if (name.equalsIgnoreCase(EncodingMode.AUTO.getName())) {
            return EncodingMode.AUTO;
        } else if (name.equalsIgnoreCase(EncodingMode.SINGLE_BYTE.getName())) {
            return EncodingMode.SINGLE_BYTE;
        } else if (name.equalsIgnoreCase(EncodingMode.CID.getName())) {
            return EncodingMode.CID;
        } else {
            throw new IllegalArgumentException("Invalid encoding mode: " + name);
        }
    }

    private Object readResolve() throws ObjectStreamException {
        return valueOf(getName());
    }

    /** {@inheritDoc} */
    public String toString() {
        return "EncodingMode:" + getName();
    }

}
