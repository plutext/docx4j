/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.wml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ThemeFontEnumeration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ThemeFontEnumeration">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="majorEastAsia"/>
 *     &lt;enumeration value="majorBidi"/>
 *     &lt;enumeration value="majorAscii"/>
 *     &lt;enumeration value="majorHAnsi"/>
 *     &lt;enumeration value="minorEastAsia"/>
 *     &lt;enumeration value="minorBidi"/>
 *     &lt;enumeration value="minorAscii"/>
 *     &lt;enumeration value="minorHAnsi"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ThemeFontEnumeration")
@XmlEnum
public enum ThemeFontEnumeration {


    /**
     * Major East Asian Theme
     * 						Font
     * 
     */
    @XmlEnumValue("majorEastAsia")
    MAJOR_EAST_ASIA("majorEastAsia"),

    /**
     * Major Complex Script Theme
     * 						Font
     * 
     */
    @XmlEnumValue("majorBidi")
    MAJOR_BIDI("majorBidi"),

    /**
     * Major ASCII Theme Font
     * 
     */
    @XmlEnumValue("majorAscii")
    MAJOR_ASCII("majorAscii"),

    /**
     * Major High ANSI Theme
     * 						Font
     * 
     */
    @XmlEnumValue("majorHAnsi")
    MAJOR_H_ANSI("majorHAnsi"),

    /**
     * Minor East Asian Theme
     * 						Font
     * 
     */
    @XmlEnumValue("minorEastAsia")
    MINOR_EAST_ASIA("minorEastAsia"),

    /**
     * Minor Complex Script Theme
     * 						Font
     * 
     */
    @XmlEnumValue("minorBidi")
    MINOR_BIDI("minorBidi"),

    /**
     * Minor ASCII Theme Font
     * 
     */
    @XmlEnumValue("minorAscii")
    MINOR_ASCII("minorAscii"),

    /**
     * Minor High ANSI Theme
     * 						Font
     * 
     */
    @XmlEnumValue("minorHAnsi")
    MINOR_H_ANSI("minorHAnsi");
    private final String value;

    ThemeFontEnumeration(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ThemeFontEnumeration fromValue(String v) {
        for (ThemeFontEnumeration c: ThemeFontEnumeration.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
