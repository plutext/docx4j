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

package org.docx4j.fonts.fop.fonts.substitute;

import java.util.StringTokenizer;

/**
 * Encapsulates a font attribute value
 */
public class AttributeValue extends java.util.ArrayList {

    private static final long serialVersionUID = 748610847500940557L;

    /**
     * Returns an <code>AttributeValue</code> object holding the
     * value of the specified <code>String</code>.
     *
     * @param valuesString the value to be parsed
     * @return     an <code>AttributeValue</code> object holding the value
     *             represented by the string argument.
     */
    public static AttributeValue valueOf(String valuesString) {
        AttributeValue attribute = new AttributeValue();
        StringTokenizer stringTokenizer = new StringTokenizer(valuesString, ",");
        if (stringTokenizer.countTokens() > 1) {
            while (stringTokenizer.hasMoreTokens()) {
                String token = stringTokenizer.nextToken().trim();
                AttributeValue tokenAttribute = AttributeValue.valueOf(token);
                attribute.addAll(tokenAttribute);
            }
        } else {
            String token = stringTokenizer.nextToken().trim();
            Object value = null;
            try {
                value = Integer.valueOf(token);
            } catch (NumberFormatException ex) {
                value = FontWeightRange.valueOf(token);
                if (value == null) {
                    value = token;
                }
            }
            if (value != null) {
                attribute.add(value);
            }
        }
        return attribute;
    }
}
