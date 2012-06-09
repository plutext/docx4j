/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.dml.chart;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_DLblPos.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DLblPos">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="bestFit"/>
 *     &lt;enumeration value="b"/>
 *     &lt;enumeration value="ctr"/>
 *     &lt;enumeration value="inBase"/>
 *     &lt;enumeration value="inEnd"/>
 *     &lt;enumeration value="l"/>
 *     &lt;enumeration value="outEnd"/>
 *     &lt;enumeration value="r"/>
 *     &lt;enumeration value="t"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_DLblPos")
@XmlEnum
public enum STDLblPos {


    /**
     * Best Fit
     * 
     */
    @XmlEnumValue("bestFit")
    BEST_FIT("bestFit"),

    /**
     * Bottom
     * 
     */
    @XmlEnumValue("b")
    B("b"),

    /**
     * Center
     * 
     */
    @XmlEnumValue("ctr")
    CTR("ctr"),

    /**
     * Inside Base
     * 
     */
    @XmlEnumValue("inBase")
    IN_BASE("inBase"),

    /**
     * Inside End
     * 
     */
    @XmlEnumValue("inEnd")
    IN_END("inEnd"),

    /**
     * Left
     * 
     */
    @XmlEnumValue("l")
    L("l"),

    /**
     * Outside End
     * 
     */
    @XmlEnumValue("outEnd")
    OUT_END("outEnd"),

    /**
     * Right
     * 
     */
    @XmlEnumValue("r")
    R("r"),

    /**
     * Top
     * 
     */
    @XmlEnumValue("t")
    T("t");
    private final String value;

    STDLblPos(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDLblPos fromValue(String v) {
        for (STDLblPos c: STDLblPos.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
