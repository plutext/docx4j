/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    If you need the right to use it under a different license, please
    contact Plutext.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TextAlignType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextAlignType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="l"/>
 *     &lt;enumeration value="ctr"/>
 *     &lt;enumeration value="r"/>
 *     &lt;enumeration value="just"/>
 *     &lt;enumeration value="justLow"/>
 *     &lt;enumeration value="dist"/>
 *     &lt;enumeration value="thaiDist"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TextAlignType")
@XmlEnum
public enum STTextAlignType {


    /**
     * Text Alignment Enum ( Left
     * 						)
     * 
     */
    @XmlEnumValue("l")
    L("l"),

    /**
     * Text Alignment Enum ( Center
     * 						)
     * 
     */
    @XmlEnumValue("ctr")
    CTR("ctr"),

    /**
     * Text Alignment Enum ( Right
     * 						)
     * 
     */
    @XmlEnumValue("r")
    R("r"),

    /**
     * Text Alignment Enum ( Justified
     * 						)
     * 
     */
    @XmlEnumValue("just")
    JUST("just"),

    /**
     * Text Alignment Enum ( Justified Low
     * 						)
     * 
     */
    @XmlEnumValue("justLow")
    JUST_LOW("justLow"),

    /**
     * Text Alignment Enum ( Distributed
     * 						)
     * 
     */
    @XmlEnumValue("dist")
    DIST("dist"),

    /**
     * Text Alignment Enum ( Thai Distributed
     * 						)
     * 
     */
    @XmlEnumValue("thaiDist")
    THAI_DIST("thaiDist");
    private final String value;

    STTextAlignType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextAlignType fromValue(String v) {
        for (STTextAlignType c: STTextAlignType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
