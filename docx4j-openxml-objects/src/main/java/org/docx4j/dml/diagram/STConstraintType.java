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
 * &lt;simpleType name="ST_ConstraintType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="none"/&gt;
 *     &lt;enumeration value="alignOff"/&gt;
 *     &lt;enumeration value="begMarg"/&gt;
 *     &lt;enumeration value="bendDist"/&gt;
 *     &lt;enumeration value="begPad"/&gt;
 *     &lt;enumeration value="b"/&gt;
 *     &lt;enumeration value="bMarg"/&gt;
 *     &lt;enumeration value="bOff"/&gt;
 *     &lt;enumeration value="ctrX"/&gt;
 *     &lt;enumeration value="ctrXOff"/&gt;
 *     &lt;enumeration value="ctrY"/&gt;
 *     &lt;enumeration value="ctrYOff"/&gt;
 *     &lt;enumeration value="connDist"/&gt;
 *     &lt;enumeration value="diam"/&gt;
 *     &lt;enumeration value="endMarg"/&gt;
 *     &lt;enumeration value="endPad"/&gt;
 *     &lt;enumeration value="h"/&gt;
 *     &lt;enumeration value="hArH"/&gt;
 *     &lt;enumeration value="hOff"/&gt;
 *     &lt;enumeration value="l"/&gt;
 *     &lt;enumeration value="lMarg"/&gt;
 *     &lt;enumeration value="lOff"/&gt;
 *     &lt;enumeration value="r"/&gt;
 *     &lt;enumeration value="rMarg"/&gt;
 *     &lt;enumeration value="rOff"/&gt;
 *     &lt;enumeration value="primFontSz"/&gt;
 *     &lt;enumeration value="pyraAcctRatio"/&gt;
 *     &lt;enumeration value="secFontSz"/&gt;
 *     &lt;enumeration value="sibSp"/&gt;
 *     &lt;enumeration value="secSibSp"/&gt;
 *     &lt;enumeration value="sp"/&gt;
 *     &lt;enumeration value="stemThick"/&gt;
 *     &lt;enumeration value="t"/&gt;
 *     &lt;enumeration value="tMarg"/&gt;
 *     &lt;enumeration value="tOff"/&gt;
 *     &lt;enumeration value="userA"/&gt;
 *     &lt;enumeration value="userB"/&gt;
 *     &lt;enumeration value="userC"/&gt;
 *     &lt;enumeration value="userD"/&gt;
 *     &lt;enumeration value="userE"/&gt;
 *     &lt;enumeration value="userF"/&gt;
 *     &lt;enumeration value="userG"/&gt;
 *     &lt;enumeration value="userH"/&gt;
 *     &lt;enumeration value="userI"/&gt;
 *     &lt;enumeration value="userJ"/&gt;
 *     &lt;enumeration value="userK"/&gt;
 *     &lt;enumeration value="userL"/&gt;
 *     &lt;enumeration value="userM"/&gt;
 *     &lt;enumeration value="userN"/&gt;
 *     &lt;enumeration value="userO"/&gt;
 *     &lt;enumeration value="userP"/&gt;
 *     &lt;enumeration value="userQ"/&gt;
 *     &lt;enumeration value="userR"/&gt;
 *     &lt;enumeration value="userS"/&gt;
 *     &lt;enumeration value="userT"/&gt;
 *     &lt;enumeration value="userU"/&gt;
 *     &lt;enumeration value="userV"/&gt;
 *     &lt;enumeration value="userW"/&gt;
 *     &lt;enumeration value="userX"/&gt;
 *     &lt;enumeration value="userY"/&gt;
 *     &lt;enumeration value="userZ"/&gt;
 *     &lt;enumeration value="w"/&gt;
 *     &lt;enumeration value="wArH"/&gt;
 *     &lt;enumeration value="wOff"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
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
