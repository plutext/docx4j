/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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


package org.docx4j.wml; 

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PageBorderOffset.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PageBorderOffset">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="page"/>
 *     &lt;enumeration value="text"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_PageBorderOffset")
@XmlEnum
public enum STPageBorderOffset {


    /**
     * Page Border Is Positioned Relative to Page
     * 						Edges
     * 
     */
    @XmlEnumValue("page")
    PAGE("page"),

    /**
     * Page Border Is Positioned Relative to Text
     * 						Extents
     * 
     */
    @XmlEnumValue("text")
    TEXT("text");
    private final String value;

    STPageBorderOffset(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPageBorderOffset fromValue(String v) {
        for (STPageBorderOffset c: STPageBorderOffset.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
