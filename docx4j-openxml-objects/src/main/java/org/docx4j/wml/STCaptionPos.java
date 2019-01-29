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
 * <p>Java class for ST_CaptionPos.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_CaptionPos">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="above"/>
 *     &lt;enumeration value="below"/>
 *     &lt;enumeration value="left"/>
 *     &lt;enumeration value="right"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_CaptionPos")
@XmlEnum
public enum STCaptionPos {


    /**
     * Position Caption Above
     * 						Object
     * 
     */
    @XmlEnumValue("above")
    ABOVE("above"),

    /**
     * Position Caption Below
     * 						Object
     * 
     */
    @XmlEnumValue("below")
    BELOW("below"),

    /**
     * Position Caption Left Of
     * 						Object
     * 
     */
    @XmlEnumValue("left")
    LEFT("left"),

    /**
     * Position Caption Right Of
     * 						Object
     * 
     */
    @XmlEnumValue("right")
    RIGHT("right");
    private final String value;

    STCaptionPos(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STCaptionPos fromValue(String v) {
        for (STCaptionPos c: STCaptionPos.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
