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
 * <p>Java class for ST_ChapterSep.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ChapterSep">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="hyphen"/>
 *     &lt;enumeration value="period"/>
 *     &lt;enumeration value="colon"/>
 *     &lt;enumeration value="emDash"/>
 *     &lt;enumeration value="enDash"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ChapterSep")
@XmlEnum
public enum STChapterSep {


    /**
     * Hyphen Chapter
     * 						Separator
     * 
     */
    @XmlEnumValue("hyphen")
    HYPHEN("hyphen"),

    /**
     * Period Chapter
     * 						Separator
     * 
     */
    @XmlEnumValue("period")
    PERIOD("period"),

    /**
     * Colon Chapter Separator
     * 
     */
    @XmlEnumValue("colon")
    COLON("colon"),

    /**
     * Em Dash Chapter
     * 						Separator
     * 
     */
    @XmlEnumValue("emDash")
    EM_DASH("emDash"),

    /**
     * En Dash Chapter
     * 						Separator
     * 
     */
    @XmlEnumValue("enDash")
    EN_DASH("enDash");
    private final String value;

    STChapterSep(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STChapterSep fromValue(String v) {
        for (STChapterSep c: STChapterSep.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
