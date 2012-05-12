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

/* $Id: FontTriplet.java 721430 2008-11-28 11:13:12Z acumiskey $ */

package org.docx4j.fonts.fop.fonts;

import java.io.Serializable;


/**
 * FontTriplet contains information on name, style and weight of one font
 */
public class FontTriplet implements Comparable, Serializable {

    /** serial version UID */
    private static final long serialVersionUID = 1168991106658033508L;

    private String name;
    private String style;
    private int weight;
    private int priority; // priority of this triplet/font mapping

    //This is only a cache
    private transient String key;

    /**
     * Creates a new font triplet (for base14 use).
     * @param name font name
     */
    public FontTriplet(String name) {
        this.name = name;
    }

    /**
     * Creates a new font triplet.
     * @param name font name
     * @param style font style (normal, italic etc.)
     * @param weight font weight (100, 200, 300...800, 900)
     */
    public FontTriplet(String name, String style, int weight) {
        this(name, style, weight, Font.PRIORITY_DEFAULT);
    }

    /**
     * Creates a new font triplet.
     * @param name font name
     * @param style font style (normal, italic etc.)
     * @param weight font weight (100, 200, 300...800, 900)
     * @param priority priority of this triplet/font mapping
     */
    public FontTriplet(String name, String style, int weight, int priority) {
        this(name);
        this.style = style;
        this.weight = weight;
        this.priority = priority;
    }

    /** @return the font name */
    public String getName() {
        return name;
    }

    /** @return the font style */
    public String getStyle() {
        return style;
    }

    /** @return the font weight */
    public int getWeight() {
        return weight;
    }

    /** @return the priority of this triplet/font mapping */
    public int getPriority() {
        return priority;
    }

    private String getKey() {
        if (this.key == null) {
            //This caches the combined key
            this.key = getName() + "," + getStyle() + "," + getWeight();
        }
        return this.key;
    }

    /** {@inheritDoc} */
    public int compareTo(Object o) {
        return getKey().compareTo(((FontTriplet)o).getKey());
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return toString().hashCode();
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else {
            if (obj instanceof FontTriplet) {
                FontTriplet other = (FontTriplet)obj;
                return (getName().equals(other.getName())
                        && getStyle().equals(other.getStyle())
                        && (getWeight() == other.getWeight()));
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    public String toString() {
        return getKey();
    }


    /**
     * Matcher interface for {@link FontTriplet}.
     */
    public interface Matcher {

        /**
         * Indicates whether the given {@link FontTriplet} matches a particular criterium.
         * @param triplet the font triplet
         * @return true if the font triplet is a match
         */
        boolean matches(FontTriplet triplet);
    }

}

