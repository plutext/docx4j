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
 * <p>Java class for ST_CfType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_CfType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="expression"/>
 *     &lt;enumeration value="cellIs"/>
 *     &lt;enumeration value="colorScale"/>
 *     &lt;enumeration value="dataBar"/>
 *     &lt;enumeration value="iconSet"/>
 *     &lt;enumeration value="top10"/>
 *     &lt;enumeration value="uniqueValues"/>
 *     &lt;enumeration value="duplicateValues"/>
 *     &lt;enumeration value="containsText"/>
 *     &lt;enumeration value="notContainsText"/>
 *     &lt;enumeration value="beginsWith"/>
 *     &lt;enumeration value="endsWith"/>
 *     &lt;enumeration value="containsBlanks"/>
 *     &lt;enumeration value="notContainsBlanks"/>
 *     &lt;enumeration value="containsErrors"/>
 *     &lt;enumeration value="notContainsErrors"/>
 *     &lt;enumeration value="timePeriod"/>
 *     &lt;enumeration value="aboveAverage"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_CfType")
@XmlEnum
public enum STCfType {

    @XmlEnumValue("expression")
    EXPRESSION("expression"),
    @XmlEnumValue("cellIs")
    CELL_IS("cellIs"),
    @XmlEnumValue("colorScale")
    COLOR_SCALE("colorScale"),
    @XmlEnumValue("dataBar")
    DATA_BAR("dataBar"),
    @XmlEnumValue("iconSet")
    ICON_SET("iconSet"),
    @XmlEnumValue("top10")
    TOP_10("top10"),
    @XmlEnumValue("uniqueValues")
    UNIQUE_VALUES("uniqueValues"),
    @XmlEnumValue("duplicateValues")
    DUPLICATE_VALUES("duplicateValues"),
    @XmlEnumValue("containsText")
    CONTAINS_TEXT("containsText"),
    @XmlEnumValue("notContainsText")
    NOT_CONTAINS_TEXT("notContainsText"),
    @XmlEnumValue("beginsWith")
    BEGINS_WITH("beginsWith"),
    @XmlEnumValue("endsWith")
    ENDS_WITH("endsWith"),
    @XmlEnumValue("containsBlanks")
    CONTAINS_BLANKS("containsBlanks"),
    @XmlEnumValue("notContainsBlanks")
    NOT_CONTAINS_BLANKS("notContainsBlanks"),
    @XmlEnumValue("containsErrors")
    CONTAINS_ERRORS("containsErrors"),
    @XmlEnumValue("notContainsErrors")
    NOT_CONTAINS_ERRORS("notContainsErrors"),
    @XmlEnumValue("timePeriod")
    TIME_PERIOD("timePeriod"),
    @XmlEnumValue("aboveAverage")
    ABOVE_AVERAGE("aboveAverage");
    private final String value;

    STCfType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STCfType fromValue(String v) {
        for (STCfType c: STCfType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
