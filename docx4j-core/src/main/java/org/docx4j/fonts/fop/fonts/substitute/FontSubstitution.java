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

/* $Id: FontSubstitution.java 679326 2008-07-24 09:35:34Z vhennebert $ */

package org.docx4j.fonts.fop.fonts.substitute;

/**
 * Encapsulates a pair of substitution qualifiers
 */
public class FontSubstitution {

    private FontQualifier fromQualifier;
    private FontQualifier toQualifier;

    /**
     * Main constructor
     *
     * @param fromQualifier the substitution from qualifier
     * @param toQualifier the substitution to qualifier
     */
    public FontSubstitution(FontQualifier fromQualifier, FontQualifier toQualifier) {
        this.fromQualifier = fromQualifier;
        this.toQualifier = toQualifier;
    }

    /**
     * @return the substitution from qualifier
     */
    public FontQualifier getFromQualifier() {
        return fromQualifier;
    }

    /**
     * @return the substitution to qualifier
     */
    public FontQualifier getToQualifier() {
        return toQualifier;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "from=" + fromQualifier + ", to=" + toQualifier;
    }
}
