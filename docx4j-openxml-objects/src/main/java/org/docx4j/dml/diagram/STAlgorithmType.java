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


package org.docx4j.dml.diagram;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_AlgorithmType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_AlgorithmType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="composite"/&gt;
 *     &lt;enumeration value="conn"/&gt;
 *     &lt;enumeration value="cycle"/&gt;
 *     &lt;enumeration value="hierChild"/&gt;
 *     &lt;enumeration value="hierRoot"/&gt;
 *     &lt;enumeration value="pyra"/&gt;
 *     &lt;enumeration value="lin"/&gt;
 *     &lt;enumeration value="sp"/&gt;
 *     &lt;enumeration value="tx"/&gt;
 *     &lt;enumeration value="snake"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_AlgorithmType")
@XmlEnum
public enum STAlgorithmType {


    /**
     * Composite
     * 
     */
    @XmlEnumValue("composite")
    COMPOSITE("composite"),

    /**
     * Connector Algorithm
     * 
     */
    @XmlEnumValue("conn")
    CONN("conn"),

    /**
     * Cycle Algorithm
     * 
     */
    @XmlEnumValue("cycle")
    CYCLE("cycle"),

    /**
     * Hierarchy Child Algorithm
     * 
     */
    @XmlEnumValue("hierChild")
    HIER_CHILD("hierChild"),

    /**
     * Hierarchy Root Algorithm
     * 
     */
    @XmlEnumValue("hierRoot")
    HIER_ROOT("hierRoot"),

    /**
     * Pyramid Algorithm
     * 
     */
    @XmlEnumValue("pyra")
    PYRA("pyra"),

    /**
     * Linear Algorithm
     * 
     */
    @XmlEnumValue("lin")
    LIN("lin"),

    /**
     * Space Algorithm
     * 
     */
    @XmlEnumValue("sp")
    SP("sp"),

    /**
     * Text Algorithm
     * 
     */
    @XmlEnumValue("tx")
    TX("tx"),

    /**
     * Snake Algorithm
     * 
     */
    @XmlEnumValue("snake")
    SNAKE("snake");
    private final String value;

    STAlgorithmType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STAlgorithmType fromValue(String v) {
        for (STAlgorithmType c: STAlgorithmType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
