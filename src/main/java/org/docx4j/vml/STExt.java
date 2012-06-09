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
 
package org.docx4j.vml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_Ext.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Ext">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="view"/>
 *     &lt;enumeration value="edit"/>
 *     &lt;enumeration value="backwardCompatible"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Ext")
@XmlEnum
public enum STExt {


    /**
     * Not renderable
     * 
     */
    @XmlEnumValue("view")
    VIEW("view"),

    /**
     * Editable
     * 
     */
    @XmlEnumValue("edit")
    EDIT("edit"),

    /**
     * Renderable
     * 
     */
    @XmlEnumValue("backwardCompatible")
    BACKWARD_COMPATIBLE("backwardCompatible");
    private final String value;

    STExt(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STExt fromValue(String v) {
        for (STExt c: STExt.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
