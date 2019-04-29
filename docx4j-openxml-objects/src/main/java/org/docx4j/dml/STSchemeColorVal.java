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


package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_SchemeColorVal.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_SchemeColorVal"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="bg1"/&gt;
 *     &lt;enumeration value="tx1"/&gt;
 *     &lt;enumeration value="bg2"/&gt;
 *     &lt;enumeration value="tx2"/&gt;
 *     &lt;enumeration value="accent1"/&gt;
 *     &lt;enumeration value="accent2"/&gt;
 *     &lt;enumeration value="accent3"/&gt;
 *     &lt;enumeration value="accent4"/&gt;
 *     &lt;enumeration value="accent5"/&gt;
 *     &lt;enumeration value="accent6"/&gt;
 *     &lt;enumeration value="hlink"/&gt;
 *     &lt;enumeration value="folHlink"/&gt;
 *     &lt;enumeration value="phClr"/&gt;
 *     &lt;enumeration value="dk1"/&gt;
 *     &lt;enumeration value="lt1"/&gt;
 *     &lt;enumeration value="dk2"/&gt;
 *     &lt;enumeration value="lt2"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_SchemeColorVal")
@XmlEnum
public enum STSchemeColorVal {


    /**
     * Background Color 1
     * 
     */
    @XmlEnumValue("bg1")
    BG_1("bg1"),

    /**
     * Text Color 1
     * 
     */
    @XmlEnumValue("tx1")
    TX_1("tx1"),

    /**
     * Background Color 2
     * 
     */
    @XmlEnumValue("bg2")
    BG_2("bg2"),

    /**
     * Text Color 2
     * 
     */
    @XmlEnumValue("tx2")
    TX_2("tx2"),

    /**
     * Accent Color 1
     * 
     */
    @XmlEnumValue("accent1")
    ACCENT_1("accent1"),

    /**
     * Accent Color 2
     * 
     */
    @XmlEnumValue("accent2")
    ACCENT_2("accent2"),

    /**
     * Accent Color 3
     * 
     */
    @XmlEnumValue("accent3")
    ACCENT_3("accent3"),

    /**
     * Accent Color 4
     * 
     */
    @XmlEnumValue("accent4")
    ACCENT_4("accent4"),

    /**
     * Accent Color 5
     * 
     */
    @XmlEnumValue("accent5")
    ACCENT_5("accent5"),

    /**
     * Accent Color 6
     * 
     */
    @XmlEnumValue("accent6")
    ACCENT_6("accent6"),

    /**
     * Hyperlink Color
     * 
     */
    @XmlEnumValue("hlink")
    HLINK("hlink"),

    /**
     * Followed Hyperlink Color
     * 
     */
    @XmlEnumValue("folHlink")
    FOL_HLINK("folHlink"),

    /**
     * Style Color
     * 
     */
    @XmlEnumValue("phClr")
    PH_CLR("phClr"),

    /**
     * Dark Color 1
     * 
     */
    @XmlEnumValue("dk1")
    DK_1("dk1"),

    /**
     * Light Color 1
     * 
     */
    @XmlEnumValue("lt1")
    LT_1("lt1"),

    /**
     * Dark Color 2
     * 
     */
    @XmlEnumValue("dk2")
    DK_2("dk2"),

    /**
     * Light Color 2
     * 
     */
    @XmlEnumValue("lt2")
    LT_2("lt2");
    private final String value;

    STSchemeColorVal(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STSchemeColorVal fromValue(String v) {
        for (STSchemeColorVal c: STSchemeColorVal.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
