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

package org.docx4j.fonts.fop.fonts;

import java.util.Locale;

/**
 * This enumerates the embedding mode of fonts; full; subset; auto (auto defaults to full for
 * Type 1 fonts and subset for TrueType fonts.
 */
public enum EmbeddingMode {
    /** Default option: assumes FULL for Type 1 fonts and SUBSET for TrueType fonts. */
    AUTO,
    /** Full font embedding: This means the whole of the font is written to file. */
    FULL,
    /** Subset font embedding: Only the mandatory tables and a subset of glyphs are written
     * to file.*/
    SUBSET;

    /**
     * Returns the name of this embedding mode.
     * @return the name of this embedding mode in lower case.
     */
    public String getName() {
        return this.toString().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Returns the embedding mode corresponding to the given name.
     * @param value the name of an embedding mode (not case sensitive)
     * @return the corresponding embedding mode
     */
    public static EmbeddingMode getValue(String value) {
        for (EmbeddingMode mode : EmbeddingMode.values()) {
            if (mode.toString().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Invalid embedding-mode: " + value);
    }
}
