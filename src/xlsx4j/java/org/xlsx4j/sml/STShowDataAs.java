/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
 *   
 *  This file is part of xlsx4j, a component of docx4j.

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
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ShowDataAs.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ShowDataAs">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="normal"/>
 *     &lt;enumeration value="difference"/>
 *     &lt;enumeration value="percent"/>
 *     &lt;enumeration value="percentDiff"/>
 *     &lt;enumeration value="runTotal"/>
 *     &lt;enumeration value="percentOfRow"/>
 *     &lt;enumeration value="percentOfCol"/>
 *     &lt;enumeration value="percentOfTotal"/>
 *     &lt;enumeration value="index"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ShowDataAs")
@XmlEnum
public enum STShowDataAs {

    @XmlEnumValue("normal")
    NORMAL("normal"),
    @XmlEnumValue("difference")
    DIFFERENCE("difference"),
    @XmlEnumValue("percent")
    PERCENT("percent"),
    @XmlEnumValue("percentDiff")
    PERCENT_DIFF("percentDiff"),
    @XmlEnumValue("runTotal")
    RUN_TOTAL("runTotal"),
    @XmlEnumValue("percentOfRow")
    PERCENT_OF_ROW("percentOfRow"),
    @XmlEnumValue("percentOfCol")
    PERCENT_OF_COL("percentOfCol"),
    @XmlEnumValue("percentOfTotal")
    PERCENT_OF_TOTAL("percentOfTotal"),
    @XmlEnumValue("index")
    INDEX("index");
    private final String value;

    STShowDataAs(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STShowDataAs fromValue(String v) {
        for (STShowDataAs c: STShowDataAs.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
