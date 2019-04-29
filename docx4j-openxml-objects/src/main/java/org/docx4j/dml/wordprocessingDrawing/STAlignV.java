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


package org.docx4j.dml.wordprocessingDrawing;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_AlignV.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_AlignV"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="top"/&gt;
 *     &lt;enumeration value="bottom"/&gt;
 *     &lt;enumeration value="center"/&gt;
 *     &lt;enumeration value="inside"/&gt;
 *     &lt;enumeration value="outside"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_AlignV")
@XmlEnum
public enum STAlignV {


    /**
     * Top
     * 
     */
    @XmlEnumValue("top")
    TOP("top"),

    /**
     * Bottom
     * 
     */
    @XmlEnumValue("bottom")
    BOTTOM("bottom"),

    /**
     * 
     * 						Center Alignment
     * 					
     * 
     */
    @XmlEnumValue("center")
    CENTER("center"),

    /**
     * Inside
     * 
     */
    @XmlEnumValue("inside")
    INSIDE("inside"),

    /**
     * Outside
     * 
     */
    @XmlEnumValue("outside")
    OUTSIDE("outside");
    private final String value;

    STAlignV(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STAlignV fromValue(String v) {
        for (STAlignV c: STAlignV.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
