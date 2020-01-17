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
 * <p>Java class for ST_Wrap.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Wrap">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="notBeside"/>
 *     &lt;enumeration value="around"/>
 *     &lt;enumeration value="tight"/>
 *     &lt;enumeration value="through"/>
 *     &lt;enumeration value="none"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Wrap")
@XmlEnum
public enum STWrap {


    /**
     * Default Text Wrapping Around
     * 						Frame
     * 
     */
    @XmlEnumValue("auto")
    AUTO("auto"),

    /**
     * No Text Wrapping Beside
     * 						Frame
     * 
     */
    @XmlEnumValue("notBeside")
    NOT_BESIDE("notBeside"),

    /**
     * Allow Text Wrapping Around
     * 						Frame
     * 
     */
    @XmlEnumValue("around")
    AROUND("around"),

    /**
     * Tight Text Wrapping Around
     * 						Frame
     * 
     */
    @XmlEnumValue("tight")
    TIGHT("tight"),

    /**
     * Through Text Wrapping Around
     * 						Frame
     * 
     */
    @XmlEnumValue("through")
    THROUGH("through"),

    /**
     * No Text Wrapping Around
     * 						Frame
     * 
     */
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    STWrap(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STWrap fromValue(String v) {
        for (STWrap c: STWrap.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
