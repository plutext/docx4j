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
 * <p>Java class for ST_Zoom.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Zoom">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="fullPage"/>
 *     &lt;enumeration value="bestFit"/>
 *     &lt;enumeration value="textFit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Zoom")
@XmlEnum
public enum STZoom {


    /**
     * No Preset Magnification
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Display One Full Page
     * 
     */
    @XmlEnumValue("fullPage")
    FULL_PAGE("fullPage"),

    /**
     * Display Page Width
     * 
     */
    @XmlEnumValue("bestFit")
    BEST_FIT("bestFit"),

    /**
     * Display Text Width
     * 
     */
    @XmlEnumValue("textFit")
    TEXT_FIT("textFit");
    private final String value;

    STZoom(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STZoom fromValue(String v) {
        for (STZoom c: STZoom.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
