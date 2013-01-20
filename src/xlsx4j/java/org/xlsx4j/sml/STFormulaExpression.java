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
 * <p>Java class for ST_FormulaExpression.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_FormulaExpression">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ref"/>
 *     &lt;enumeration value="refError"/>
 *     &lt;enumeration value="area"/>
 *     &lt;enumeration value="areaError"/>
 *     &lt;enumeration value="computedArea"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_FormulaExpression")
@XmlEnum
public enum STFormulaExpression {

    @XmlEnumValue("ref")
    REF("ref"),
    @XmlEnumValue("refError")
    REF_ERROR("refError"),
    @XmlEnumValue("area")
    AREA("area"),
    @XmlEnumValue("areaError")
    AREA_ERROR("areaError"),
    @XmlEnumValue("computedArea")
    COMPUTED_AREA("computedArea");
    private final String value;

    STFormulaExpression(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STFormulaExpression fromValue(String v) {
        for (STFormulaExpression c: STFormulaExpression.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
