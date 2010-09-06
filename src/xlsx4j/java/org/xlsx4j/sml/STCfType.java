/*
 *  Copyright 2010, Plutext Pty Ltd.
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


    /**
     * Expression
     * 
     */
    @XmlEnumValue("expression")
    EXPRESSION("expression"),

    /**
     * Cell Is
     * 
     */
    @XmlEnumValue("cellIs")
    CELL_IS("cellIs"),

    /**
     * Color Scale
     * 
     */
    @XmlEnumValue("colorScale")
    COLOR_SCALE("colorScale"),

    /**
     * Data Bar
     * 
     */
    @XmlEnumValue("dataBar")
    DATA_BAR("dataBar"),

    /**
     * Icon Set
     * 
     */
    @XmlEnumValue("iconSet")
    ICON_SET("iconSet"),

    /**
     * Top 10
     * 
     */
    @XmlEnumValue("top10")
    TOP_10("top10"),

    /**
     * Unique Values
     * 
     */
    @XmlEnumValue("uniqueValues")
    UNIQUE_VALUES("uniqueValues"),

    /**
     * Duplicate Values
     * 
     */
    @XmlEnumValue("duplicateValues")
    DUPLICATE_VALUES("duplicateValues"),

    /**
     * Contains Text
     * 
     */
    @XmlEnumValue("containsText")
    CONTAINS_TEXT("containsText"),

    /**
     * Does Not Contain Text
     * 
     */
    @XmlEnumValue("notContainsText")
    NOT_CONTAINS_TEXT("notContainsText"),

    /**
     * Begins With
     * 
     */
    @XmlEnumValue("beginsWith")
    BEGINS_WITH("beginsWith"),

    /**
     * Ends With
     * 
     */
    @XmlEnumValue("endsWith")
    ENDS_WITH("endsWith"),

    /**
     * Contains Blanks
     * 
     */
    @XmlEnumValue("containsBlanks")
    CONTAINS_BLANKS("containsBlanks"),

    /**
     * Contains No Blanks
     * 
     */
    @XmlEnumValue("notContainsBlanks")
    NOT_CONTAINS_BLANKS("notContainsBlanks"),

    /**
     * Contains Errors
     * 
     */
    @XmlEnumValue("containsErrors")
    CONTAINS_ERRORS("containsErrors"),

    /**
     * Contains No Errors
     * 
     */
    @XmlEnumValue("notContainsErrors")
    NOT_CONTAINS_ERRORS("notContainsErrors"),

    /**
     * Time Period
     * 
     */
    @XmlEnumValue("timePeriod")
    TIME_PERIOD("timePeriod"),

    /**
     * Above or Below Average
     * 
     */
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
