/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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


package org.docx4j.wml; 

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TblStyleOverrideType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TblStyleOverrideType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="wholeTable"/>
 *     &lt;enumeration value="firstRow"/>
 *     &lt;enumeration value="lastRow"/>
 *     &lt;enumeration value="firstCol"/>
 *     &lt;enumeration value="lastCol"/>
 *     &lt;enumeration value="band1Vert"/>
 *     &lt;enumeration value="band2Vert"/>
 *     &lt;enumeration value="band1Horz"/>
 *     &lt;enumeration value="band2Horz"/>
 *     &lt;enumeration value="neCell"/>
 *     &lt;enumeration value="nwCell"/>
 *     &lt;enumeration value="seCell"/>
 *     &lt;enumeration value="swCell"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TblStyleOverrideType")
@XmlEnum
public enum STTblStyleOverrideType {


    /**
     * Whole table formatting
     * 
     */
    @XmlEnumValue("wholeTable")
    WHOLE_TABLE("wholeTable"),

    /**
     * First Row Conditional
     * 						Formatting
     * 
     */
    @XmlEnumValue("firstRow")
    FIRST_ROW("firstRow"),

    /**
     * Last table row
     * 						formatting
     * 
     */
    @XmlEnumValue("lastRow")
    LAST_ROW("lastRow"),

    /**
     * First Column Conditional
     * 						Formatting
     * 
     */
    @XmlEnumValue("firstCol")
    FIRST_COL("firstCol"),

    /**
     * Last table column
     * 						formatting
     * 
     */
    @XmlEnumValue("lastCol")
    LAST_COL("lastCol"),

    /**
     * Banded Column Conditional
     * 						Formatting
     * 
     */
    @XmlEnumValue("band1Vert")
    BAND_1_VERT("band1Vert"),

    /**
     * Even Column Stripe Conditional
     * 						Formatting
     * 
     */
    @XmlEnumValue("band2Vert")
    BAND_2_VERT("band2Vert"),

    /**
     * Banded Row Conditional
     * 						Formatting
     * 
     */
    @XmlEnumValue("band1Horz")
    BAND_1_HORZ("band1Horz"),

    /**
     * Even Row Stripe Conditional
     * 						Formatting
     * 
     */
    @XmlEnumValue("band2Horz")
    BAND_2_HORZ("band2Horz"),

    /**
     * Top right table cell
     * 						formatting
     * 
     */
    @XmlEnumValue("neCell")
    NE_CELL("neCell"),

    /**
     * Top left table cell
     * 						formatting
     * 
     */
    @XmlEnumValue("nwCell")
    NW_CELL("nwCell"),

    /**
     * Bottom right table cell
     * 						formatting
     * 
     */
    @XmlEnumValue("seCell")
    SE_CELL("seCell"),

    /**
     * Bottom left table cell
     * 						formatting
     * 
     */
    @XmlEnumValue("swCell")
    SW_CELL("swCell");
    private final String value;

    STTblStyleOverrideType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTblStyleOverrideType fromValue(String v) {
        for (STTblStyleOverrideType c: STTblStyleOverrideType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
