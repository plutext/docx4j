/*
 *  Copyright 2013, Plutext Pty Ltd.
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


package org.docx4j.w14;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_SchemeColorVal.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_SchemeColorVal">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="bg1"/>
 *     &lt;enumeration value="tx1"/>
 *     &lt;enumeration value="bg2"/>
 *     &lt;enumeration value="tx2"/>
 *     &lt;enumeration value="accent1"/>
 *     &lt;enumeration value="accent2"/>
 *     &lt;enumeration value="accent3"/>
 *     &lt;enumeration value="accent4"/>
 *     &lt;enumeration value="accent5"/>
 *     &lt;enumeration value="accent6"/>
 *     &lt;enumeration value="hlink"/>
 *     &lt;enumeration value="folHlink"/>
 *     &lt;enumeration value="dk1"/>
 *     &lt;enumeration value="lt1"/>
 *     &lt;enumeration value="dk2"/>
 *     &lt;enumeration value="lt2"/>
 *     &lt;enumeration value="phClr"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_SchemeColorVal")
@XmlEnum
public enum STSchemeColorVal {

    @XmlEnumValue("bg1")
    BG_1("bg1"),
    @XmlEnumValue("tx1")
    TX_1("tx1"),
    @XmlEnumValue("bg2")
    BG_2("bg2"),
    @XmlEnumValue("tx2")
    TX_2("tx2"),
    @XmlEnumValue("accent1")
    ACCENT_1("accent1"),
    @XmlEnumValue("accent2")
    ACCENT_2("accent2"),
    @XmlEnumValue("accent3")
    ACCENT_3("accent3"),
    @XmlEnumValue("accent4")
    ACCENT_4("accent4"),
    @XmlEnumValue("accent5")
    ACCENT_5("accent5"),
    @XmlEnumValue("accent6")
    ACCENT_6("accent6"),
    @XmlEnumValue("hlink")
    HLINK("hlink"),
    @XmlEnumValue("folHlink")
    FOL_HLINK("folHlink"),
    @XmlEnumValue("dk1")
    DK_1("dk1"),
    @XmlEnumValue("lt1")
    LT_1("lt1"),
    @XmlEnumValue("dk2")
    DK_2("dk2"),
    @XmlEnumValue("lt2")
    LT_2("lt2"),
    @XmlEnumValue("phClr")
    PH_CLR("phClr");
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
