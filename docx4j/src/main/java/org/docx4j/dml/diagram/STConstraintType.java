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
 * <p>Java class for ST_ConstraintType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ConstraintType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="alignOff"/>
 *     &lt;enumeration value="begMarg"/>
 *     &lt;enumeration value="bendDist"/>
 *     &lt;enumeration value="begPad"/>
 *     &lt;enumeration value="b"/>
 *     &lt;enumeration value="bMarg"/>
 *     &lt;enumeration value="bOff"/>
 *     &lt;enumeration value="ctrX"/>
 *     &lt;enumeration value="ctrXOff"/>
 *     &lt;enumeration value="ctrY"/>
 *     &lt;enumeration value="ctrYOff"/>
 *     &lt;enumeration value="connDist"/>
 *     &lt;enumeration value="diam"/>
 *     &lt;enumeration value="endMarg"/>
 *     &lt;enumeration value="endPad"/>
 *     &lt;enumeration value="h"/>
 *     &lt;enumeration value="hArH"/>
 *     &lt;enumeration value="hOff"/>
 *     &lt;enumeration value="l"/>
 *     &lt;enumeration value="lMarg"/>
 *     &lt;enumeration value="lOff"/>
 *     &lt;enumeration value="r"/>
 *     &lt;enumeration value="rMarg"/>
 *     &lt;enumeration value="rOff"/>
 *     &lt;enumeration value="primFontSz"/>
 *     &lt;enumeration value="pyraAcctRatio"/>
 *     &lt;enumeration value="secFontSz"/>
 *     &lt;enumeration value="sibSp"/>
 *     &lt;enumeration value="secSibSp"/>
 *     &lt;enumeration value="sp"/>
 *     &lt;enumeration value="stemThick"/>
 *     &lt;enumeration value="t"/>
 *     &lt;enumeration value="tMarg"/>
 *     &lt;enumeration value="tOff"/>
 *     &lt;enumeration value="userA"/>
 *     &lt;enumeration value="userB"/>
 *     &lt;enumeration value="userC"/>
 *     &lt;enumeration value="userD"/>
 *     &lt;enumeration value="userE"/>
 *     &lt;enumeration value="userF"/>
 *     &lt;enumeration value="userG"/>
 *     &lt;enumeration value="userH"/>
 *     &lt;enumeration value="userI"/>
 *     &lt;enumeration value="userJ"/>
 *     &lt;enumeration value="userK"/>
 *     &lt;enumeration value="userL"/>
 *     &lt;enumeration value="userM"/>
 *     &lt;enumeration value="userN"/>
 *     &lt;enumeration value="userO"/>
 *     &lt;enumeration value="userP"/>
 *     &lt;enumeration value="userQ"/>
 *     &lt;enumeration value="userR"/>
 *     &lt;enumeration value="userS"/>
 *     &lt;enumeration value="userT"/>
 *     &lt;enumeration value="userU"/>
 *     &lt;enumeration value="userV"/>
 *     &lt;enumeration value="userW"/>
 *     &lt;enumeration value="userX"/>
 *     &lt;enumeration value="userY"/>
 *     &lt;enumeration value="userZ"/>
 *     &lt;enumeration value="w"/>
 *     &lt;enumeration value="wArH"/>
 *     &lt;enumeration value="wOff"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ConstraintType")
@XmlEnum
public enum STConstraintType {


    /**
     * Unknown
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Alignment Offset
     * 
     */
    @XmlEnumValue("alignOff")
    ALIGN_OFF("alignOff"),

    /**
     * Beginning Margin
     * 
     */
    @XmlEnumValue("begMarg")
    BEG_MARG("begMarg"),

    /**
     * Bending Distance
     * 
     */
    @XmlEnumValue("bendDist")
    BEND_DIST("bendDist"),

    /**
     * Beginning Padding
     * 
     */
    @XmlEnumValue("begPad")
    BEG_PAD("begPad"),

    /**
     * Bottom
     * 
     */
    @XmlEnumValue("b")
    B("b"),

    /**
     * Bottom Margin
     * 
     */
    @XmlEnumValue("bMarg")
    B_MARG("bMarg"),

    /**
     * Bottom Offset
     * 
     */
    @XmlEnumValue("bOff")
    B_OFF("bOff"),

    /**
     * Center Height
     * 
     */
    @XmlEnumValue("ctrX")
    CTR_X("ctrX"),

    /**
     * Center X Offset
     * 
     */
    @XmlEnumValue("ctrXOff")
    CTR_X_OFF("ctrXOff"),

    /**
     * Center Width
     * 
     */
    @XmlEnumValue("ctrY")
    CTR_Y("ctrY"),

    /**
     * Center Y Offset
     * 
     */
    @XmlEnumValue("ctrYOff")
    CTR_Y_OFF("ctrYOff"),

    /**
     * Connection Distance
     * 
     */
    @XmlEnumValue("connDist")
    CONN_DIST("connDist"),

    /**
     * Diameter
     * 
     */
    @XmlEnumValue("diam")
    DIAM("diam"),

    /**
     * End Margin
     * 
     */
    @XmlEnumValue("endMarg")
    END_MARG("endMarg"),

    /**
     * End Padding
     * 
     */
    @XmlEnumValue("endPad")
    END_PAD("endPad"),

    /**
     * Height
     * 
     */
    @XmlEnumValue("h")
    H("h"),

    /**
     * Arrowhead Height
     * 
     */
    @XmlEnumValue("hArH")
    H_AR_H("hArH"),

    /**
     * Height Offset
     * 
     */
    @XmlEnumValue("hOff")
    H_OFF("hOff"),

    /**
     * Left
     * 
     */
    @XmlEnumValue("l")
    L("l"),

    /**
     * Left Margin
     * 
     */
    @XmlEnumValue("lMarg")
    L_MARG("lMarg"),

    /**
     * Left Offset
     * 
     */
    @XmlEnumValue("lOff")
    L_OFF("lOff"),

    /**
     * Right
     * 
     */
    @XmlEnumValue("r")
    R("r"),

    /**
     * Right Margin
     * 
     */
    @XmlEnumValue("rMarg")
    R_MARG("rMarg"),

    /**
     * Right Offset
     * 
     */
    @XmlEnumValue("rOff")
    R_OFF("rOff"),

    /**
     * Primary Font Size
     * 
     */
    @XmlEnumValue("primFontSz")
    PRIM_FONT_SZ("primFontSz"),

    /**
     * Pyramid Accent Ratio
     * 
     */
    @XmlEnumValue("pyraAcctRatio")
    PYRA_ACCT_RATIO("pyraAcctRatio"),

    /**
     * Secondary Font Size
     * 
     */
    @XmlEnumValue("secFontSz")
    SEC_FONT_SZ("secFontSz"),

    /**
     * Sibling Spacing
     * 
     */
    @XmlEnumValue("sibSp")
    SIB_SP("sibSp"),

    /**
     * Secondary Sibling Spacing
     * 
     */
    @XmlEnumValue("secSibSp")
    SEC_SIB_SP("secSibSp"),

    /**
     * Spacing
     * 
     */
    @XmlEnumValue("sp")
    SP("sp"),

    /**
     * Stem Thickness
     * 
     */
    @XmlEnumValue("stemThick")
    STEM_THICK("stemThick"),

    /**
     * Top
     * 
     */
    @XmlEnumValue("t")
    T("t"),

    /**
     * Top Margin
     * 
     */
    @XmlEnumValue("tMarg")
    T_MARG("tMarg"),

    /**
     * Top Offset
     * 
     */
    @XmlEnumValue("tOff")
    T_OFF("tOff"),

    /**
     * User Defined A
     * 
     */
    @XmlEnumValue("userA")
    USER_A("userA"),

    /**
     * User Defined B
     * 
     */
    @XmlEnumValue("userB")
    USER_B("userB"),

    /**
     * User Defined C
     * 
     */
    @XmlEnumValue("userC")
    USER_C("userC"),

    /**
     * User Defined D
     * 
     */
    @XmlEnumValue("userD")
    USER_D("userD"),

    /**
     * User Defined E
     * 
     */
    @XmlEnumValue("userE")
    USER_E("userE"),

    /**
     * User Defined F
     * 
     */
    @XmlEnumValue("userF")
    USER_F("userF"),

    /**
     * User Defined G
     * 
     */
    @XmlEnumValue("userG")
    USER_G("userG"),

    /**
     * User Defined H
     * 
     */
    @XmlEnumValue("userH")
    USER_H("userH"),

    /**
     * User Defined I
     * 
     */
    @XmlEnumValue("userI")
    USER_I("userI"),

    /**
     * User Defined J
     * 
     */
    @XmlEnumValue("userJ")
    USER_J("userJ"),

    /**
     * User Defined K
     * 
     */
    @XmlEnumValue("userK")
    USER_K("userK"),

    /**
     * User Defined L
     * 
     */
    @XmlEnumValue("userL")
    USER_L("userL"),

    /**
     * User Defined M
     * 
     */
    @XmlEnumValue("userM")
    USER_M("userM"),

    /**
     * User Defined N
     * 
     */
    @XmlEnumValue("userN")
    USER_N("userN"),

    /**
     * User Defined O
     * 
     */
    @XmlEnumValue("userO")
    USER_O("userO"),

    /**
     * User Defined P
     * 
     */
    @XmlEnumValue("userP")
    USER_P("userP"),

    /**
     * User Defined Q
     * 
     */
    @XmlEnumValue("userQ")
    USER_Q("userQ"),

    /**
     * User Defined R
     * 
     */
    @XmlEnumValue("userR")
    USER_R("userR"),

    /**
     * User Defined S
     * 
     */
    @XmlEnumValue("userS")
    USER_S("userS"),

    /**
     * User Defined T
     * 
     */
    @XmlEnumValue("userT")
    USER_T("userT"),

    /**
     * User Defined U
     * 
     */
    @XmlEnumValue("userU")
    USER_U("userU"),

    /**
     * User Defined V
     * 
     */
    @XmlEnumValue("userV")
    USER_V("userV"),

    /**
     * User Defined W
     * 
     */
    @XmlEnumValue("userW")
    USER_W("userW"),

    /**
     * User Defined X
     * 
     */
    @XmlEnumValue("userX")
    USER_X("userX"),

    /**
     * User Defined Y
     * 
     */
    @XmlEnumValue("userY")
    USER_Y("userY"),

    /**
     * User Defined Z
     * 
     */
    @XmlEnumValue("userZ")
    USER_Z("userZ"),

    /**
     * Width
     * 
     */
    @XmlEnumValue("w")
    W("w"),

    /**
     * Arrowhead Width
     * 
     */
    @XmlEnumValue("wArH")
    W_AR_H("wArH"),

    /**
     * Width Offset
     * 
     */
    @XmlEnumValue("wOff")
    W_OFF("wOff");
    private final String value;

    STConstraintType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STConstraintType fromValue(String v) {
        for (STConstraintType c: STConstraintType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
