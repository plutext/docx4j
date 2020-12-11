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
 * <p>Java class for ST_FtnPos.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_FtnPos">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="pageBottom"/>
 *     &lt;enumeration value="beneathText"/>
 *     &lt;enumeration value="sectEnd"/>
 *     &lt;enumeration value="docEnd"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_FtnPos")
@XmlEnum
public enum STFtnPos {


    /**
     * Footnotes Positioned at Page
     * 						Bottom
     * 
     */
    @XmlEnumValue("pageBottom")
    PAGE_BOTTOM("pageBottom"),

    /**
     * Footnotes Positioned Beneath
     * 						Text
     * 
     */
    @XmlEnumValue("beneathText")
    BENEATH_TEXT("beneathText"),

    /**
     * Footnotes Positioned At End of
     * 						Section
     * 
     */
    @XmlEnumValue("sectEnd")
    SECT_END("sectEnd"),

    /**
     * Footnotes Positioned At End of
     * 						Document
     * 
     */
    @XmlEnumValue("docEnd")
    DOC_END("docEnd");
    private final String value;

    STFtnPos(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STFtnPos fromValue(String v) {
        for (STFtnPos c: STFtnPos.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
