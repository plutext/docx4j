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

package org.docx4j.jaxb.document;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_VerticalAlignRun.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_VerticalAlignRun">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="baseline"/>
 *     &lt;enumeration value="superscript"/>
 *     &lt;enumeration value="subscript"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_VerticalAlignRun")
@XmlEnum
public enum STVerticalAlignRun {


    /**
     * Regular Vertical
     * 						Positioning
     * 
     */
    @XmlEnumValue("baseline")
    BASELINE("baseline"),

    /**
     * Superscript
     * 
     */
    @XmlEnumValue("superscript")
    SUPERSCRIPT("superscript"),

    /**
     * Subscript
     * 
     */
    @XmlEnumValue("subscript")
    SUBSCRIPT("subscript");
    private final String value;

    STVerticalAlignRun(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STVerticalAlignRun fromValue(String v) {
        for (STVerticalAlignRun c: STVerticalAlignRun.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
