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
 * <p>Java class for ST_TableStyleType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TableStyleType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="wholeTable"/>
 *     &lt;enumeration value="headerRow"/>
 *     &lt;enumeration value="totalRow"/>
 *     &lt;enumeration value="firstColumn"/>
 *     &lt;enumeration value="lastColumn"/>
 *     &lt;enumeration value="firstRowStripe"/>
 *     &lt;enumeration value="secondRowStripe"/>
 *     &lt;enumeration value="firstColumnStripe"/>
 *     &lt;enumeration value="secondColumnStripe"/>
 *     &lt;enumeration value="firstHeaderCell"/>
 *     &lt;enumeration value="lastHeaderCell"/>
 *     &lt;enumeration value="firstTotalCell"/>
 *     &lt;enumeration value="lastTotalCell"/>
 *     &lt;enumeration value="firstSubtotalColumn"/>
 *     &lt;enumeration value="secondSubtotalColumn"/>
 *     &lt;enumeration value="thirdSubtotalColumn"/>
 *     &lt;enumeration value="firstSubtotalRow"/>
 *     &lt;enumeration value="secondSubtotalRow"/>
 *     &lt;enumeration value="thirdSubtotalRow"/>
 *     &lt;enumeration value="blankRow"/>
 *     &lt;enumeration value="firstColumnSubheading"/>
 *     &lt;enumeration value="secondColumnSubheading"/>
 *     &lt;enumeration value="thirdColumnSubheading"/>
 *     &lt;enumeration value="firstRowSubheading"/>
 *     &lt;enumeration value="secondRowSubheading"/>
 *     &lt;enumeration value="thirdRowSubheading"/>
 *     &lt;enumeration value="pageFieldLabels"/>
 *     &lt;enumeration value="pageFieldValues"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TableStyleType")
@XmlEnum
public enum STTableStyleType {


    /**
     * Whole Table Style
     * 
     */
    @XmlEnumValue("wholeTable")
    WHOLE_TABLE("wholeTable"),

    /**
     * Header Row Style
     * 
     */
    @XmlEnumValue("headerRow")
    HEADER_ROW("headerRow"),

    /**
     * Total Row Style
     * 
     */
    @XmlEnumValue("totalRow")
    TOTAL_ROW("totalRow"),

    /**
     * First Column Style
     * 
     */
    @XmlEnumValue("firstColumn")
    FIRST_COLUMN("firstColumn"),

    /**
     * Last Column Style
     * 
     */
    @XmlEnumValue("lastColumn")
    LAST_COLUMN("lastColumn"),

    /**
     * First Row Stripe Style
     * 
     */
    @XmlEnumValue("firstRowStripe")
    FIRST_ROW_STRIPE("firstRowStripe"),

    /**
     * Second Row Stripe Style
     * 
     */
    @XmlEnumValue("secondRowStripe")
    SECOND_ROW_STRIPE("secondRowStripe"),

    /**
     * First Column Stripe Style
     * 
     */
    @XmlEnumValue("firstColumnStripe")
    FIRST_COLUMN_STRIPE("firstColumnStripe"),

    /**
     * Second Column Stipe Style
     * 
     */
    @XmlEnumValue("secondColumnStripe")
    SECOND_COLUMN_STRIPE("secondColumnStripe"),

    /**
     * First Header Row Style
     * 
     */
    @XmlEnumValue("firstHeaderCell")
    FIRST_HEADER_CELL("firstHeaderCell"),

    /**
     * Last Header Style
     * 
     */
    @XmlEnumValue("lastHeaderCell")
    LAST_HEADER_CELL("lastHeaderCell"),

    /**
     * First Total Row Style
     * 
     */
    @XmlEnumValue("firstTotalCell")
    FIRST_TOTAL_CELL("firstTotalCell"),

    /**
     * Last Total Row Style
     * 
     */
    @XmlEnumValue("lastTotalCell")
    LAST_TOTAL_CELL("lastTotalCell"),

    /**
     * First Subtotal Column Style
     * 
     */
    @XmlEnumValue("firstSubtotalColumn")
    FIRST_SUBTOTAL_COLUMN("firstSubtotalColumn"),

    /**
     * Second Subtotal Column Style
     * 
     */
    @XmlEnumValue("secondSubtotalColumn")
    SECOND_SUBTOTAL_COLUMN("secondSubtotalColumn"),

    /**
     * Third Subtotal Column Style
     * 
     */
    @XmlEnumValue("thirdSubtotalColumn")
    THIRD_SUBTOTAL_COLUMN("thirdSubtotalColumn"),

    /**
     * First Subtotal Row Style
     * 
     */
    @XmlEnumValue("firstSubtotalRow")
    FIRST_SUBTOTAL_ROW("firstSubtotalRow"),

    /**
     * Second Subtotal Row Style
     * 
     */
    @XmlEnumValue("secondSubtotalRow")
    SECOND_SUBTOTAL_ROW("secondSubtotalRow"),

    /**
     * Third Subtotal Row Style
     * 
     */
    @XmlEnumValue("thirdSubtotalRow")
    THIRD_SUBTOTAL_ROW("thirdSubtotalRow"),

    /**
     * Blank Row Style
     * 
     */
    @XmlEnumValue("blankRow")
    BLANK_ROW("blankRow"),

    /**
     * First Column Subheading Style
     * 
     */
    @XmlEnumValue("firstColumnSubheading")
    FIRST_COLUMN_SUBHEADING("firstColumnSubheading"),

    /**
     * Second Column Subheading Style
     * 
     */
    @XmlEnumValue("secondColumnSubheading")
    SECOND_COLUMN_SUBHEADING("secondColumnSubheading"),

    /**
     * Third Column Subheading Style
     * 
     */
    @XmlEnumValue("thirdColumnSubheading")
    THIRD_COLUMN_SUBHEADING("thirdColumnSubheading"),

    /**
     * First Row Subheading Style
     * 
     */
    @XmlEnumValue("firstRowSubheading")
    FIRST_ROW_SUBHEADING("firstRowSubheading"),

    /**
     * Second Row Subheading Style
     * 
     */
    @XmlEnumValue("secondRowSubheading")
    SECOND_ROW_SUBHEADING("secondRowSubheading"),

    /**
     * Third Row Subheading Style
     * 
     */
    @XmlEnumValue("thirdRowSubheading")
    THIRD_ROW_SUBHEADING("thirdRowSubheading"),

    /**
     * Page Field Labels Style
     * 
     */
    @XmlEnumValue("pageFieldLabels")
    PAGE_FIELD_LABELS("pageFieldLabels"),

    /**
     * Page Field Values Style
     * 
     */
    @XmlEnumValue("pageFieldValues")
    PAGE_FIELD_VALUES("pageFieldValues");
    private final String value;

    STTableStyleType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTableStyleType fromValue(String v) {
        for (STTableStyleType c: STTableStyleType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
