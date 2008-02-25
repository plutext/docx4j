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
 * <p>Java class for ST_FontFamily.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_FontFamily">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="decorative"/>
 *     &lt;enumeration value="modern"/>
 *     &lt;enumeration value="roman"/>
 *     &lt;enumeration value="script"/>
 *     &lt;enumeration value="swiss"/>
 *     &lt;enumeration value="auto"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_FontFamily")
@XmlEnum
public enum STFontFamily {


    /**
     * Novelty Font
     * 
     */
    @XmlEnumValue("decorative")
    DECORATIVE("decorative"),

    /**
     * Monospace Font
     * 
     */
    @XmlEnumValue("modern")
    MODERN("modern"),

    /**
     * Proportional Font With
     * 						Serifs
     * 
     */
    @XmlEnumValue("roman")
    ROMAN("roman"),

    /**
     * Script Font
     * 
     */
    @XmlEnumValue("script")
    SCRIPT("script"),

    /**
     * Proportional Font Without
     * 						Serifs
     * 
     */
    @XmlEnumValue("swiss")
    SWISS("swiss"),

    /**
     * No Font Family
     * 
     */
    @XmlEnumValue("auto")
    AUTO("auto");
    private final String value;

    STFontFamily(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STFontFamily fromValue(String v) {
        for (STFontFamily c: STFontFamily.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
