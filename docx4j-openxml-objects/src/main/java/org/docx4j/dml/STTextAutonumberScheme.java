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


package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TextAutonumberScheme.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextAutonumberScheme"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="alphaLcParenBoth"/&gt;
 *     &lt;enumeration value="alphaUcParenBoth"/&gt;
 *     &lt;enumeration value="alphaLcParenR"/&gt;
 *     &lt;enumeration value="alphaUcParenR"/&gt;
 *     &lt;enumeration value="alphaLcPeriod"/&gt;
 *     &lt;enumeration value="alphaUcPeriod"/&gt;
 *     &lt;enumeration value="arabicParenBoth"/&gt;
 *     &lt;enumeration value="arabicParenR"/&gt;
 *     &lt;enumeration value="arabicPeriod"/&gt;
 *     &lt;enumeration value="arabicPlain"/&gt;
 *     &lt;enumeration value="romanLcParenBoth"/&gt;
 *     &lt;enumeration value="romanUcParenBoth"/&gt;
 *     &lt;enumeration value="romanLcParenR"/&gt;
 *     &lt;enumeration value="romanUcParenR"/&gt;
 *     &lt;enumeration value="romanLcPeriod"/&gt;
 *     &lt;enumeration value="romanUcPeriod"/&gt;
 *     &lt;enumeration value="circleNumDbPlain"/&gt;
 *     &lt;enumeration value="circleNumWdBlackPlain"/&gt;
 *     &lt;enumeration value="circleNumWdWhitePlain"/&gt;
 *     &lt;enumeration value="arabicDbPeriod"/&gt;
 *     &lt;enumeration value="arabicDbPlain"/&gt;
 *     &lt;enumeration value="ea1ChsPeriod"/&gt;
 *     &lt;enumeration value="ea1ChsPlain"/&gt;
 *     &lt;enumeration value="ea1ChtPeriod"/&gt;
 *     &lt;enumeration value="ea1ChtPlain"/&gt;
 *     &lt;enumeration value="ea1JpnChsDbPeriod"/&gt;
 *     &lt;enumeration value="ea1JpnKorPlain"/&gt;
 *     &lt;enumeration value="ea1JpnKorPeriod"/&gt;
 *     &lt;enumeration value="arabic1Minus"/&gt;
 *     &lt;enumeration value="arabic2Minus"/&gt;
 *     &lt;enumeration value="hebrew2Minus"/&gt;
 *     &lt;enumeration value="thaiAlphaPeriod"/&gt;
 *     &lt;enumeration value="thaiAlphaParenR"/&gt;
 *     &lt;enumeration value="thaiAlphaParenBoth"/&gt;
 *     &lt;enumeration value="thaiNumPeriod"/&gt;
 *     &lt;enumeration value="thaiNumParenR"/&gt;
 *     &lt;enumeration value="thaiNumParenBoth"/&gt;
 *     &lt;enumeration value="hindiAlphaPeriod"/&gt;
 *     &lt;enumeration value="hindiNumPeriod"/&gt;
 *     &lt;enumeration value="hindiNumParenR"/&gt;
 *     &lt;enumeration value="hindiAlpha1Period"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TextAutonumberScheme")
@XmlEnum
public enum STTextAutonumberScheme {


    /**
     * Autonumber Enum ( alphaLcParenBoth )
     * 
     */
    @XmlEnumValue("alphaLcParenBoth")
    ALPHA_LC_PAREN_BOTH("alphaLcParenBoth"),

    /**
     * Autonumbering Enum ( alphaUcParenBoth )
     * 
     */
    @XmlEnumValue("alphaUcParenBoth")
    ALPHA_UC_PAREN_BOTH("alphaUcParenBoth"),

    /**
     * Autonumbering Enum ( alphaLcParenR )
     * 
     */
    @XmlEnumValue("alphaLcParenR")
    ALPHA_LC_PAREN_R("alphaLcParenR"),

    /**
     * Autonumbering Enum ( alphaUcParenR )
     * 
     */
    @XmlEnumValue("alphaUcParenR")
    ALPHA_UC_PAREN_R("alphaUcParenR"),

    /**
     * Autonumbering Enum ( alphaLcPeriod )
     * 
     */
    @XmlEnumValue("alphaLcPeriod")
    ALPHA_LC_PERIOD("alphaLcPeriod"),

    /**
     * Autonumbering Enum ( alphaUcPeriod )
     * 
     */
    @XmlEnumValue("alphaUcPeriod")
    ALPHA_UC_PERIOD("alphaUcPeriod"),

    /**
     * Autonumbering Enum ( arabicParenBoth )
     * 
     */
    @XmlEnumValue("arabicParenBoth")
    ARABIC_PAREN_BOTH("arabicParenBoth"),

    /**
     * Autonumbering Enum ( arabicParenR )
     * 
     */
    @XmlEnumValue("arabicParenR")
    ARABIC_PAREN_R("arabicParenR"),

    /**
     * Autonumbering Enum ( arabicPeriod )
     * 
     */
    @XmlEnumValue("arabicPeriod")
    ARABIC_PERIOD("arabicPeriod"),

    /**
     * Autonumbering Enum ( arabicPlain )
     * 
     */
    @XmlEnumValue("arabicPlain")
    ARABIC_PLAIN("arabicPlain"),

    /**
     * Autonumbering Enum ( romanLcParenBoth )
     * 
     */
    @XmlEnumValue("romanLcParenBoth")
    ROMAN_LC_PAREN_BOTH("romanLcParenBoth"),

    /**
     * Autonumbering Enum ( romanUcParenBoth )
     * 
     */
    @XmlEnumValue("romanUcParenBoth")
    ROMAN_UC_PAREN_BOTH("romanUcParenBoth"),

    /**
     * Autonumbering Enum ( romanLcParenR )
     * 
     */
    @XmlEnumValue("romanLcParenR")
    ROMAN_LC_PAREN_R("romanLcParenR"),

    /**
     * Autonumbering Enum ( romanUcParenR )
     * 
     */
    @XmlEnumValue("romanUcParenR")
    ROMAN_UC_PAREN_R("romanUcParenR"),

    /**
     * Autonumbering Enum ( romanLcPeriod )
     * 
     */
    @XmlEnumValue("romanLcPeriod")
    ROMAN_LC_PERIOD("romanLcPeriod"),

    /**
     * Autonumbering Enum ( romanUcPeriod )
     * 
     */
    @XmlEnumValue("romanUcPeriod")
    ROMAN_UC_PERIOD("romanUcPeriod"),

    /**
     * Autonumbering Enum ( circleNumDbPlain )
     * 
     */
    @XmlEnumValue("circleNumDbPlain")
    CIRCLE_NUM_DB_PLAIN("circleNumDbPlain"),

    /**
     * Autonumbering Enum ( circleNumWdBlackPlain )
     * 
     */
    @XmlEnumValue("circleNumWdBlackPlain")
    CIRCLE_NUM_WD_BLACK_PLAIN("circleNumWdBlackPlain"),

    /**
     * Autonumbering Enum ( circleNumWdWhitePlain )
     * 
     */
    @XmlEnumValue("circleNumWdWhitePlain")
    CIRCLE_NUM_WD_WHITE_PLAIN("circleNumWdWhitePlain"),

    /**
     * Autonumbering Enum ( arabicDbPeriod )
     * 
     */
    @XmlEnumValue("arabicDbPeriod")
    ARABIC_DB_PERIOD("arabicDbPeriod"),

    /**
     * Autonumbering Enum ( arabicDbPlain )
     * 
     */
    @XmlEnumValue("arabicDbPlain")
    ARABIC_DB_PLAIN("arabicDbPlain"),

    /**
     * Autonumbering Enum ( ea1ChsPeriod )
     * 
     */
    @XmlEnumValue("ea1ChsPeriod")
    EA_1_CHS_PERIOD("ea1ChsPeriod"),

    /**
     * Autonumbering Enum ( ea1ChsPlain )
     * 
     */
    @XmlEnumValue("ea1ChsPlain")
    EA_1_CHS_PLAIN("ea1ChsPlain"),

    /**
     * Autonumbering Enum ( ea1ChtPeriod )
     * 
     */
    @XmlEnumValue("ea1ChtPeriod")
    EA_1_CHT_PERIOD("ea1ChtPeriod"),

    /**
     * Autonumbering Enum ( ea1ChtPlain )
     * 
     */
    @XmlEnumValue("ea1ChtPlain")
    EA_1_CHT_PLAIN("ea1ChtPlain"),

    /**
     * Autonumbering Enum ( ea1JpnChsDbPeriod )
     * 
     */
    @XmlEnumValue("ea1JpnChsDbPeriod")
    EA_1_JPN_CHS_DB_PERIOD("ea1JpnChsDbPeriod"),

    /**
     * Autonumbering Enum ( ea1JpnKorPlain )
     * 
     */
    @XmlEnumValue("ea1JpnKorPlain")
    EA_1_JPN_KOR_PLAIN("ea1JpnKorPlain"),

    /**
     * Autonumbering Enum ( ea1JpnKorPeriod )
     * 
     */
    @XmlEnumValue("ea1JpnKorPeriod")
    EA_1_JPN_KOR_PERIOD("ea1JpnKorPeriod"),

    /**
     * Autonumbering Enum ( arabic1Minus )
     * 
     */
    @XmlEnumValue("arabic1Minus")
    ARABIC_1_MINUS("arabic1Minus"),

    /**
     * Autonumbering Enum ( arabic2Minus )
     * 
     */
    @XmlEnumValue("arabic2Minus")
    ARABIC_2_MINUS("arabic2Minus"),

    /**
     * Autonumbering Enum ( hebrew2Minus )
     * 
     */
    @XmlEnumValue("hebrew2Minus")
    HEBREW_2_MINUS("hebrew2Minus"),

    /**
     * Autonumbering Enum ( thaiAlphaPeriod )
     * 
     */
    @XmlEnumValue("thaiAlphaPeriod")
    THAI_ALPHA_PERIOD("thaiAlphaPeriod"),

    /**
     * Autonumbering Enum ( thaiAlphaParenR )
     * 
     */
    @XmlEnumValue("thaiAlphaParenR")
    THAI_ALPHA_PAREN_R("thaiAlphaParenR"),

    /**
     * Autonumbering Enum ( thaiAlphaParenBoth )
     * 
     */
    @XmlEnumValue("thaiAlphaParenBoth")
    THAI_ALPHA_PAREN_BOTH("thaiAlphaParenBoth"),

    /**
     * Autonumbering Enum ( thaiNumPeriod )
     * 
     */
    @XmlEnumValue("thaiNumPeriod")
    THAI_NUM_PERIOD("thaiNumPeriod"),

    /**
     * Autonumbering Enum ( thaiNumParenR )
     * 
     */
    @XmlEnumValue("thaiNumParenR")
    THAI_NUM_PAREN_R("thaiNumParenR"),

    /**
     * Autonumbering Enum ( thaiNumParenBoth )
     * 
     */
    @XmlEnumValue("thaiNumParenBoth")
    THAI_NUM_PAREN_BOTH("thaiNumParenBoth"),

    /**
     * Autonumbering Enum ( hindiAlphaPeriod )
     * 
     */
    @XmlEnumValue("hindiAlphaPeriod")
    HINDI_ALPHA_PERIOD("hindiAlphaPeriod"),

    /**
     * Autonumbering Enum ( hindiNumPeriod )
     * 
     */
    @XmlEnumValue("hindiNumPeriod")
    HINDI_NUM_PERIOD("hindiNumPeriod"),

    /**
     * Autonumbering Enum ( hindiNumParenR )
     * 
     */
    @XmlEnumValue("hindiNumParenR")
    HINDI_NUM_PAREN_R("hindiNumParenR"),

    /**
     * Autonumbering Enum ( hindiAlpha1Period )
     * 
     */
    @XmlEnumValue("hindiAlpha1Period")
    HINDI_ALPHA_1_PERIOD("hindiAlpha1Period");
    private final String value;

    STTextAutonumberScheme(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextAutonumberScheme fromValue(String v) {
        for (STTextAutonumberScheme c: STTextAutonumberScheme.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
