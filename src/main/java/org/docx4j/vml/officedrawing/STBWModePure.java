/*
 *  Copyright 2007-2009, Plutext Pty Ltd.
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
 * <p>Java class for ST_BWModePure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_BWModePure">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="color"/>
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="grayscale"/>
 *     &lt;enumeration value="lighGrayscale"/>
 *     &lt;enumeration value="grayOutline"/>
 *     &lt;enumeration value="highContrast"/>
 *     &lt;enumeration value="black"/>
 *     &lt;enumeration value="white"/>
 *     &lt;enumeration value="hide"/>
 *     &lt;enumeration value="numModes"/>
 *     &lt;enumeration value="blackTextAndLines"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_BWModePure")
@XmlEnum
public enum STBWModePure {

    @XmlEnumValue("color")
    COLOR("color"),
    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("grayscale")
    GRAYSCALE("grayscale"),
    @XmlEnumValue("lighGrayscale")
    LIGH_GRAYSCALE("lighGrayscale"),
    @XmlEnumValue("grayOutline")
    GRAY_OUTLINE("grayOutline"),
    @XmlEnumValue("highContrast")
    HIGH_CONTRAST("highContrast"),
    @XmlEnumValue("black")
    BLACK("black"),
    @XmlEnumValue("white")
    WHITE("white"),
    @XmlEnumValue("hide")
    HIDE("hide"),
    @XmlEnumValue("numModes")
    NUM_MODES("numModes"),
    @XmlEnumValue("blackTextAndLines")
    BLACK_TEXT_AND_LINES("blackTextAndLines");
    private final String value;

    STBWModePure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STBWModePure fromValue(String v) {
        for (STBWModePure c: STBWModePure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
