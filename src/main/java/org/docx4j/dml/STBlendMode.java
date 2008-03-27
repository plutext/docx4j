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
 * <p>Java class for ST_BlendMode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_BlendMode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="over"/>
 *     &lt;enumeration value="mult"/>
 *     &lt;enumeration value="screen"/>
 *     &lt;enumeration value="darken"/>
 *     &lt;enumeration value="lighten"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_BlendMode")
@XmlEnum
public enum STBlendMode {


    /**
     * Overlay
     * 
     */
    @XmlEnumValue("over")
    OVER("over"),

    /**
     * Multiply
     * 
     */
    @XmlEnumValue("mult")
    MULT("mult"),

    /**
     * Screen
     * 
     */
    @XmlEnumValue("screen")
    SCREEN("screen"),

    /**
     * Darken
     * 
     */
    @XmlEnumValue("darken")
    DARKEN("darken"),

    /**
     * Lighten
     * 
     */
    @XmlEnumValue("lighten")
    LIGHTEN("lighten");
    private final String value;

    STBlendMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STBlendMode fromValue(String v) {
        for (STBlendMode c: STBlendMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
