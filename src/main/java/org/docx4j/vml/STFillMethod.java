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


package org.docx4j.vml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_FillMethod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_FillMethod">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="linear"/>
 *     &lt;enumeration value="sigma"/>
 *     &lt;enumeration value="any"/>
 *     &lt;enumeration value="linear sigma"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_FillMethod")
@XmlEnum
public enum STFillMethod {


    /**
     * No Gradient Fill
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Linear Fill
     * 
     */
    @XmlEnumValue("linear")
    LINEAR("linear"),

    /**
     * Sigma Fill
     * 
     */
    @XmlEnumValue("sigma")
    SIGMA("sigma"),

    /**
     * Application Default Fill
     * 
     */
    @XmlEnumValue("any")
    ANY("any"),

    /**
     * Linear Sigma Fill
     * 
     */
    @XmlEnumValue("linear sigma")
    LINEAR_SIGMA("linear sigma");
    private final String value;

    STFillMethod(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STFillMethod fromValue(String v) {
        for (STFillMethod c: STFillMethod.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
