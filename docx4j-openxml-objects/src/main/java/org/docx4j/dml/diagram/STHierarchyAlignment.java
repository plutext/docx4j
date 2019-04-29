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
 * <p>Java class for ST_HierarchyAlignment.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_HierarchyAlignment"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="tL"/&gt;
 *     &lt;enumeration value="tR"/&gt;
 *     &lt;enumeration value="tCtrCh"/&gt;
 *     &lt;enumeration value="tCtrDes"/&gt;
 *     &lt;enumeration value="bL"/&gt;
 *     &lt;enumeration value="bR"/&gt;
 *     &lt;enumeration value="bCtrCh"/&gt;
 *     &lt;enumeration value="bCtrDes"/&gt;
 *     &lt;enumeration value="lT"/&gt;
 *     &lt;enumeration value="lB"/&gt;
 *     &lt;enumeration value="lCtrCh"/&gt;
 *     &lt;enumeration value="lCtrDes"/&gt;
 *     &lt;enumeration value="rT"/&gt;
 *     &lt;enumeration value="rB"/&gt;
 *     &lt;enumeration value="rCtrCh"/&gt;
 *     &lt;enumeration value="rCtrDes"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_HierarchyAlignment")
@XmlEnum
public enum STHierarchyAlignment {


    /**
     * Top Left
     * 
     */
    @XmlEnumValue("tL")
    T_L("tL"),

    /**
     * Top Right
     * 
     */
    @XmlEnumValue("tR")
    T_R("tR"),

    /**
     * Top Center Children
     * 
     */
    @XmlEnumValue("tCtrCh")
    T_CTR_CH("tCtrCh"),

    /**
     * Top Center Descendants
     * 
     */
    @XmlEnumValue("tCtrDes")
    T_CTR_DES("tCtrDes"),

    /**
     * Bottom Left
     * 
     */
    @XmlEnumValue("bL")
    B_L("bL"),

    /**
     * Bottom Right
     * 
     */
    @XmlEnumValue("bR")
    B_R("bR"),

    /**
     * Bottom Center Child
     * 
     */
    @XmlEnumValue("bCtrCh")
    B_CTR_CH("bCtrCh"),

    /**
     * Bottom Center Descendant
     * 
     */
    @XmlEnumValue("bCtrDes")
    B_CTR_DES("bCtrDes"),

    /**
     * Left Top
     * 
     */
    @XmlEnumValue("lT")
    L_T("lT"),

    /**
     * Left Bottom
     * 
     */
    @XmlEnumValue("lB")
    L_B("lB"),

    /**
     * Left Center Child
     * 
     */
    @XmlEnumValue("lCtrCh")
    L_CTR_CH("lCtrCh"),

    /**
     * Left Center Descendant
     * 
     */
    @XmlEnumValue("lCtrDes")
    L_CTR_DES("lCtrDes"),

    /**
     * Right Top
     * 
     */
    @XmlEnumValue("rT")
    R_T("rT"),

    /**
     * Right Bottom
     * 
     */
    @XmlEnumValue("rB")
    R_B("rB"),

    /**
     * Right Center Children
     * 
     */
    @XmlEnumValue("rCtrCh")
    R_CTR_CH("rCtrCh"),

    /**
     * Right Center Descendants
     * 
     */
    @XmlEnumValue("rCtrDes")
    R_CTR_DES("rCtrDes");
    private final String value;

    STHierarchyAlignment(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STHierarchyAlignment fromValue(String v) {
        for (STHierarchyAlignment c: STHierarchyAlignment.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
