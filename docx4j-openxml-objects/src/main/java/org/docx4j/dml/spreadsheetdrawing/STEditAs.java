/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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


package org.docx4j.dml.spreadsheetdrawing;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_EditAs.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_EditAs"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="twoCell"/&gt;
 *     &lt;enumeration value="oneCell"/&gt;
 *     &lt;enumeration value="absolute"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_EditAs")
@XmlEnum
public enum STEditAs {


    /**
     * Move and Resize With Anchor Cells
     * 
     */
    @XmlEnumValue("twoCell")
    TWO_CELL("twoCell"),

    /**
     * Move With Cells but Do Not Resize
     * 
     */
    @XmlEnumValue("oneCell")
    ONE_CELL("oneCell"),

    /**
     * Do Not Move or Resize With Underlying Rows/Columns
     * 
     */
    @XmlEnumValue("absolute")
    ABSOLUTE("absolute");
    private final String value;

    STEditAs(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STEditAs fromValue(String v) {
        for (STEditAs c: STEditAs.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
