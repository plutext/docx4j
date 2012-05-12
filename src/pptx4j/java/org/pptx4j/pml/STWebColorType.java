/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.pptx4j.pml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_WebColorType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_WebColorType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="browser"/>
 *     &lt;enumeration value="presentationText"/>
 *     &lt;enumeration value="presentationAccent"/>
 *     &lt;enumeration value="whiteTextOnBlack"/>
 *     &lt;enumeration value="blackTextOnWhite"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_WebColorType")
@XmlEnum
public enum STWebColorType {


    /**
     * 
     * 						Non-specific Colors
     * 					
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * 
     * 						Browser Colors
     * 					
     * 
     */
    @XmlEnumValue("browser")
    BROWSER("browser"),

    /**
     * 
     * 						Presentation Text Colors
     * 					
     * 
     */
    @XmlEnumValue("presentationText")
    PRESENTATION_TEXT("presentationText"),

    /**
     * 
     * 						Presentation Accent Colors
     * 					
     * 
     */
    @XmlEnumValue("presentationAccent")
    PRESENTATION_ACCENT("presentationAccent"),

    /**
     * 
     * 						White Text on Black Colors
     * 					
     * 
     */
    @XmlEnumValue("whiteTextOnBlack")
    WHITE_TEXT_ON_BLACK("whiteTextOnBlack"),

    /**
     * 
     * 						Black Text on White Colors
     * 					
     * 
     */
    @XmlEnumValue("blackTextOnWhite")
    BLACK_TEXT_ON_WHITE("blackTextOnWhite");
    private final String value;

    STWebColorType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STWebColorType fromValue(String v) {
        for (STWebColorType c: STWebColorType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
