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
 * <p>Java class for ST_SplitType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_SplitType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="auto"/&gt;
 *     &lt;enumeration value="cust"/&gt;
 *     &lt;enumeration value="percent"/&gt;
 *     &lt;enumeration value="pos"/&gt;
 *     &lt;enumeration value="val"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_SplitType")
@XmlEnum
public enum STSplitType {


    /**
     * Default Split
     * 
     */
    @XmlEnumValue("auto")
    AUTO("auto"),

    /**
     * Custom Split
     * 
     */
    @XmlEnumValue("cust")
    CUST("cust"),

    /**
     * Split by Percentage
     * 
     */
    @XmlEnumValue("percent")
    PERCENT("percent"),

    /**
     * Split by Position
     * 
     */
    @XmlEnumValue("pos")
    POS("pos"),

    /**
     * Split by Value
     * 
     */
    @XmlEnumValue("val")
    VAL("val");
    private final String value;

    STSplitType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STSplitType fromValue(String v) {
        for (STSplitType c: STSplitType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
