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


package org.docx4j.dml.diagram;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_AxisType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_AxisType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="self"/&gt;
 *     &lt;enumeration value="ch"/&gt;
 *     &lt;enumeration value="des"/&gt;
 *     &lt;enumeration value="desOrSelf"/&gt;
 *     &lt;enumeration value="par"/&gt;
 *     &lt;enumeration value="ancst"/&gt;
 *     &lt;enumeration value="ancstOrSelf"/&gt;
 *     &lt;enumeration value="followSib"/&gt;
 *     &lt;enumeration value="precedSib"/&gt;
 *     &lt;enumeration value="follow"/&gt;
 *     &lt;enumeration value="preced"/&gt;
 *     &lt;enumeration value="root"/&gt;
 *     &lt;enumeration value="none"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_AxisType")
@XmlEnum
public enum STAxisType {


    /**
     * Self
     * 
     */
    @XmlEnumValue("self")
    SELF("self"),

    /**
     * Child
     * 
     */
    @XmlEnumValue("ch")
    CH("ch"),

    /**
     * Descendant
     * 
     */
    @XmlEnumValue("des")
    DES("des"),

    /**
     * Descendant or Self
     * 
     */
    @XmlEnumValue("desOrSelf")
    DES_OR_SELF("desOrSelf"),

    /**
     * Parent
     * 
     */
    @XmlEnumValue("par")
    PAR("par"),

    /**
     * Ancestor
     * 
     */
    @XmlEnumValue("ancst")
    ANCST("ancst"),

    /**
     * Ancestor or Self
     * 
     */
    @XmlEnumValue("ancstOrSelf")
    ANCST_OR_SELF("ancstOrSelf"),

    /**
     * Follow Sibling
     * 
     */
    @XmlEnumValue("followSib")
    FOLLOW_SIB("followSib"),

    /**
     * Preceding Sibling
     * 
     */
    @XmlEnumValue("precedSib")
    PRECED_SIB("precedSib"),

    /**
     * Follow
     * 
     */
    @XmlEnumValue("follow")
    FOLLOW("follow"),

    /**
     * Preceding
     * 
     */
    @XmlEnumValue("preced")
    PRECED("preced"),

    /**
     * Root
     * 
     */
    @XmlEnumValue("root")
    ROOT("root"),

    /**
     * None
     * 
     */
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    STAxisType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STAxisType fromValue(String v) {
        for (STAxisType c: STAxisType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
