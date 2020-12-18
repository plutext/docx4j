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

package org.docx4j.fonts.fop.fonts;

/**
 * This class enumerates all supported encoding modes for fonts: auto, single-byte and CID.
 */
public enum EncodingMode {

    /** Automatic selection of encoding mode. */
    AUTO("auto"),

    /** Single-byte encoding */
    SINGLE_BYTE("single-byte"),

    /** CID encoding */
    CID("cid");

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
    public static EncodingMode getValue(String name) {
        for (EncodingMode em : EncodingMode.values()) {
            if (name.equalsIgnoreCase(em.getName())) {
                return em;
            }
        }
        throw new IllegalArgumentException("Invalid encoding mode: " + name);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "EncodingMode: " + getName();
    }

}
