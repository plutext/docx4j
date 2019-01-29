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


package org.docx4j.vml.officedrawing;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ConnectorType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ConnectorType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="straight"/>
 *     &lt;enumeration value="elbow"/>
 *     &lt;enumeration value="curved"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ConnectorType")
@XmlEnum
public enum STConnectorType {


    /**
     * No Connector
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Straight Connector
     * 
     */
    @XmlEnumValue("straight")
    STRAIGHT("straight"),

    /**
     * Elbow Connector
     * 
     */
    @XmlEnumValue("elbow")
    ELBOW("elbow"),

    /**
     * Curved Connector
     * 
     */
    @XmlEnumValue("curved")
    CURVED("curved");
    private final String value;

    STConnectorType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STConnectorType fromValue(String v) {
        for (STConnectorType c: STConnectorType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
