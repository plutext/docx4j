/*
 *  Copyright 2010-2012, Plutext Pty Ltd.
 *   
 *  This file is part of pptx4j, a component of docx4j.

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
 * <p>Java class for ST_PrintWhat.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PrintWhat"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="slides"/&gt;
 *     &lt;enumeration value="handouts1"/&gt;
 *     &lt;enumeration value="handouts2"/&gt;
 *     &lt;enumeration value="handouts3"/&gt;
 *     &lt;enumeration value="handouts4"/&gt;
 *     &lt;enumeration value="handouts6"/&gt;
 *     &lt;enumeration value="handouts9"/&gt;
 *     &lt;enumeration value="notes"/&gt;
 *     &lt;enumeration value="outline"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_PrintWhat")
@XmlEnum
public enum STPrintWhat {


    /**
     * Slides
     * 
     */
    @XmlEnumValue("slides")
    SLIDES("slides"),

    /**
     *  1 Slide / Handout Page
     * 
     */
    @XmlEnumValue("handouts1")
    HANDOUTS_1("handouts1"),

    /**
     *  2 Slides / Handout Page
     * 
     */
    @XmlEnumValue("handouts2")
    HANDOUTS_2("handouts2"),

    /**
     *  3 Slides / Handout Page
     * 
     */
    @XmlEnumValue("handouts3")
    HANDOUTS_3("handouts3"),

    /**
     *  4 Slides / Handout Page
     * 
     */
    @XmlEnumValue("handouts4")
    HANDOUTS_4("handouts4"),

    /**
     *  6 Slides / Handout Page
     * 
     */
    @XmlEnumValue("handouts6")
    HANDOUTS_6("handouts6"),

    /**
     *  9 Slides / Handout Page
     * 
     */
    @XmlEnumValue("handouts9")
    HANDOUTS_9("handouts9"),

    /**
     * Notes
     * 
     */
    @XmlEnumValue("notes")
    NOTES("notes"),

    /**
     * Outline
     * 
     */
    @XmlEnumValue("outline")
    OUTLINE("outline");
    private final String value;

    STPrintWhat(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPrintWhat fromValue(String v) {
        for (STPrintWhat c: STPrintWhat.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
