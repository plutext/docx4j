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
 * <p>Java class for ST_ItemType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ItemType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="data"/>
 *     &lt;enumeration value="default"/>
 *     &lt;enumeration value="sum"/>
 *     &lt;enumeration value="countA"/>
 *     &lt;enumeration value="avg"/>
 *     &lt;enumeration value="max"/>
 *     &lt;enumeration value="min"/>
 *     &lt;enumeration value="product"/>
 *     &lt;enumeration value="count"/>
 *     &lt;enumeration value="stdDev"/>
 *     &lt;enumeration value="stdDevP"/>
 *     &lt;enumeration value="var"/>
 *     &lt;enumeration value="varP"/>
 *     &lt;enumeration value="grand"/>
 *     &lt;enumeration value="blank"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ItemType")
@XmlEnum
public enum STItemType {


    /**
     * Data
     * 
     */
    @XmlEnumValue("data")
    DATA("data"),

    /**
     * Default
     * 
     */
    @XmlEnumValue("default")
    DEFAULT("default"),

    /**
     * Sum
     * 
     */
    @XmlEnumValue("sum")
    SUM("sum"),

    /**
     * CountA
     * 
     */
    @XmlEnumValue("countA")
    COUNT_A("countA"),

    /**
     * Average
     * 
     */
    @XmlEnumValue("avg")
    AVG("avg"),

    /**
     * Max
     * 
     */
    @XmlEnumValue("max")
    MAX("max"),

    /**
     * Min
     * 
     */
    @XmlEnumValue("min")
    MIN("min"),

    /**
     * Product
     * 
     */
    @XmlEnumValue("product")
    PRODUCT("product"),

    /**
     * Count
     * 
     */
    @XmlEnumValue("count")
    COUNT("count"),

    /**
     * stdDev
     * 
     */
    @XmlEnumValue("stdDev")
    STD_DEV("stdDev"),

    /**
     * StdDevP
     * 
     */
    @XmlEnumValue("stdDevP")
    STD_DEV_P("stdDevP"),

    /**
     * Var
     * 
     */
    @XmlEnumValue("var")
    VAR("var"),

    /**
     * VarP
     * 
     */
    @XmlEnumValue("varP")
    VAR_P("varP"),

    /**
     * Grand Total Item
     * 
     */
    @XmlEnumValue("grand")
    GRAND("grand"),

    /**
     * Blank Pivot Item
     * 
     */
    @XmlEnumValue("blank")
    BLANK("blank");
    private final String value;

    STItemType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STItemType fromValue(String v) {
        for (STItemType c: STItemType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
