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


package org.docx4j.vml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ImageAspect.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ImageAspect">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ignore"/>
 *     &lt;enumeration value="atMost"/>
 *     &lt;enumeration value="atLeast"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ImageAspect")
@XmlEnum
public enum STImageAspect {


    /**
     * Ignore Aspect Ratio
     * 
     */
    @XmlEnumValue("ignore")
    IGNORE("ignore"),

    /**
     * At Most
     * 
     */
    @XmlEnumValue("atMost")
    AT_MOST("atMost"),

    /**
     * At Least
     * 
     */
    @XmlEnumValue("atLeast")
    AT_LEAST("atLeast");
    private final String value;

    STImageAspect(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STImageAspect fromValue(String v) {
        for (STImageAspect c: STImageAspect.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
