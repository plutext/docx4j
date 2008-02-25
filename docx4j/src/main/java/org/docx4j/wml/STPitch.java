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
 * <p>Java class for ST_Pitch.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Pitch">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="fixed"/>
 *     &lt;enumeration value="variable"/>
 *     &lt;enumeration value="default"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Pitch")
@XmlEnum
public enum STPitch {


    /**
     * Fixed Width
     * 
     */
    @XmlEnumValue("fixed")
    FIXED("fixed"),

    /**
     * Proportional Width
     * 
     */
    @XmlEnumValue("variable")
    VARIABLE("variable"),

    /**
     * Default
     * 
     */
    @XmlEnumValue("default")
    DEFAULT("default");
    private final String value;

    STPitch(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPitch fromValue(String v) {
        for (STPitch c: STPitch.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
