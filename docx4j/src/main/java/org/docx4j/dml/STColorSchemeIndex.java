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
 * <p>Java class for ST_ColorSchemeIndex.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ColorSchemeIndex">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="dk1"/>
 *     &lt;enumeration value="lt1"/>
 *     &lt;enumeration value="dk2"/>
 *     &lt;enumeration value="lt2"/>
 *     &lt;enumeration value="accent1"/>
 *     &lt;enumeration value="accent2"/>
 *     &lt;enumeration value="accent3"/>
 *     &lt;enumeration value="accent4"/>
 *     &lt;enumeration value="accent5"/>
 *     &lt;enumeration value="accent6"/>
 *     &lt;enumeration value="hlink"/>
 *     &lt;enumeration value="folHlink"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ColorSchemeIndex")
@XmlEnum
public enum STColorSchemeIndex {


    /**
     * Dark 1
     * 
     */
    @XmlEnumValue("dk1")
    DK_1("dk1"),

    /**
     * Light 1
     * 
     */
    @XmlEnumValue("lt1")
    LT_1("lt1"),

    /**
     * Dark 2
     * 
     */
    @XmlEnumValue("dk2")
    DK_2("dk2"),

    /**
     * Light 2
     * 
     */
    @XmlEnumValue("lt2")
    LT_2("lt2"),

    /**
     * Accent 1
     * 
     */
    @XmlEnumValue("accent1")
    ACCENT_1("accent1"),

    /**
     * Accent 2
     * 
     */
    @XmlEnumValue("accent2")
    ACCENT_2("accent2"),

    /**
     * Accent 3
     * 
     */
    @XmlEnumValue("accent3")
    ACCENT_3("accent3"),

    /**
     * Accent 4
     * 
     */
    @XmlEnumValue("accent4")
    ACCENT_4("accent4"),

    /**
     * Accent 5
     * 
     */
    @XmlEnumValue("accent5")
    ACCENT_5("accent5"),

    /**
     * Accent 6
     * 
     */
    @XmlEnumValue("accent6")
    ACCENT_6("accent6"),

    /**
     * Hyperlink
     * 
     */
    @XmlEnumValue("hlink")
    HLINK("hlink"),

    /**
     * Followed Hyperlink
     * 
     */
    @XmlEnumValue("folHlink")
    FOL_HLINK("folHlink");
    private final String value;

    STColorSchemeIndex(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STColorSchemeIndex fromValue(String v) {
        for (STColorSchemeIndex c: STColorSchemeIndex.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
