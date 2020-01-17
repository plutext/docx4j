/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */


package org.docx4j.dml.chart;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_DispBlanksAs.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DispBlanksAs"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="span"/&gt;
 *     &lt;enumeration value="gap"/&gt;
 *     &lt;enumeration value="zero"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_DispBlanksAs")
@XmlEnum
public enum STDispBlanksAs {


    /**
     * Span
     * 
     */
    @XmlEnumValue("span")
    SPAN("span"),

    /**
     * Gap
     * 
     */
    @XmlEnumValue("gap")
    GAP("gap"),

    /**
     * Zero
     * 
     */
    @XmlEnumValue("zero")
    ZERO("zero");
    private final String value;

    STDispBlanksAs(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDispBlanksAs fromValue(String v) {
        for (STDispBlanksAs c: STDispBlanksAs.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
