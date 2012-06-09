/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.dml.diagram;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ContinueDirection.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ContinueDirection">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="revDir"/>
 *     &lt;enumeration value="sameDir"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ContinueDirection")
@XmlEnum
public enum STContinueDirection {


    /**
     * Reverse Direction
     * 
     */
    @XmlEnumValue("revDir")
    REV_DIR("revDir"),

    /**
     * Same Direction
     * 
     */
    @XmlEnumValue("sameDir")
    SAME_DIR("sameDir");
    private final String value;

    STContinueDirection(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STContinueDirection fromValue(String v) {
        for (STContinueDirection c: STContinueDirection.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
