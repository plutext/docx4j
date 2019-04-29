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


package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TextTabAlignType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextTabAlignType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="l"/&gt;
 *     &lt;enumeration value="ctr"/&gt;
 *     &lt;enumeration value="r"/&gt;
 *     &lt;enumeration value="dec"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TextTabAlignType")
@XmlEnum
public enum STTextTabAlignType {


    /**
     * Text Tab Alignment Enum ( Left)
     * 
     */
    @XmlEnumValue("l")
    L("l"),

    /**
     * Text Tab Alignment Enum ( Center )
     * 
     */
    @XmlEnumValue("ctr")
    CTR("ctr"),

    /**
     * Text Tab Alignment Enum ( Right )
     * 
     */
    @XmlEnumValue("r")
    R("r"),

    /**
     * Text Tab Alignment Enum ( Decimal )
     * 
     */
    @XmlEnumValue("dec")
    DEC("dec");
    private final String value;

    STTextTabAlignType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextTabAlignType fromValue(String v) {
        for (STTextTabAlignType c: STTextTabAlignType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
