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
 * <p>Java class for NumberFormat.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="NumberFormat">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="decimal"/>
 *     &lt;enumeration value="upperRoman"/>
 *     &lt;enumeration value="lowerRoman"/>
 *     &lt;enumeration value="upperLetter"/>
 *     &lt;enumeration value="lowerLetter"/>
 *     &lt;enumeration value="ordinal"/>
 *     &lt;enumeration value="cardinalText"/>
 *     &lt;enumeration value="ordinalText"/>
 *     &lt;enumeration value="hex"/>
 *     &lt;enumeration value="chicago"/>
 *     &lt;enumeration value="ideographDigital"/>
 *     &lt;enumeration value="japaneseCounting"/>
 *     &lt;enumeration value="aiueo"/>
 *     &lt;enumeration value="iroha"/>
 *     &lt;enumeration value="decimalFullWidth"/>
 *     &lt;enumeration value="decimalHalfWidth"/>
 *     &lt;enumeration value="japaneseLegal"/>
 *     &lt;enumeration value="japaneseDigitalTenThousand"/>
 *     &lt;enumeration value="decimalEnclosedCircle"/>
 *     &lt;enumeration value="decimalFullWidth2"/>
 *     &lt;enumeration value="aiueoFullWidth"/>
 *     &lt;enumeration value="irohaFullWidth"/>
 *     &lt;enumeration value="decimalZero"/>
 *     &lt;enumeration value="bullet"/>
 *     &lt;enumeration value="ganada"/>
 *     &lt;enumeration value="chosung"/>
 *     &lt;enumeration value="decimalEnclosedFullstop"/>
 *     &lt;enumeration value="decimalEnclosedParen"/>
 *     &lt;enumeration value="decimalEnclosedCircleChinese"/>
 *     &lt;enumeration value="ideographEnclosedCircle"/>
 *     &lt;enumeration value="ideographTraditional"/>
 *     &lt;enumeration value="ideographZodiac"/>
 *     &lt;enumeration value="ideographZodiacTraditional"/>
 *     &lt;enumeration value="taiwaneseCounting"/>
 *     &lt;enumeration value="ideographLegalTraditional"/>
 *     &lt;enumeration value="taiwaneseCountingThousand"/>
 *     &lt;enumeration value="taiwaneseDigital"/>
 *     &lt;enumeration value="chineseCounting"/>
 *     &lt;enumeration value="chineseLegalSimplified"/>
 *     &lt;enumeration value="chineseCountingThousand"/>
 *     &lt;enumeration value="koreanDigital"/>
 *     &lt;enumeration value="koreanCounting"/>
 *     &lt;enumeration value="koreanLegal"/>
 *     &lt;enumeration value="koreanDigital2"/>
 *     &lt;enumeration value="vietnameseCounting"/>
 *     &lt;enumeration value="russianLower"/>
 *     &lt;enumeration value="russianUpper"/>
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="numberInDash"/>
 *     &lt;enumeration value="hebrew1"/>
 *     &lt;enumeration value="hebrew2"/>
 *     &lt;enumeration value="arabicAlpha"/>
 *     &lt;enumeration value="arabicAbjad"/>
 *     &lt;enumeration value="hindiVowels"/>
 *     &lt;enumeration value="hindiConsonants"/>
 *     &lt;enumeration value="hindiNumbers"/>
 *     &lt;enumeration value="hindiCounting"/>
 *     &lt;enumeration value="thaiLetters"/>
 *     &lt;enumeration value="thaiNumbers"/>
 *     &lt;enumeration value="thaiCounting"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "NumberFormat")
@XmlEnum
public enum NumberFormat {


    /**
     * Decimal Numbers
     * 
     */
    @XmlEnumValue("decimal")
    DECIMAL("decimal"),

    /**
     * Uppercase Roman
     * 						Numerals
     * 
     */
    @XmlEnumValue("upperRoman")
    UPPER_ROMAN("upperRoman"),

    /**
     * Lowercase Roman
     * 						Numerals
     * 
     */
    @XmlEnumValue("lowerRoman")
    LOWER_ROMAN("lowerRoman"),

    /**
     * Uppercase Latin
     * 						Alphabet
     * 
     */
    @XmlEnumValue("upperLetter")
    UPPER_LETTER("upperLetter"),

    /**
     * Lowercase Latin
     * 						Alphabet
     * 
     */
    @XmlEnumValue("lowerLetter")
    LOWER_LETTER("lowerLetter"),

    /**
     * Ordinal
     * 
     */
    @XmlEnumValue("ordinal")
    ORDINAL("ordinal"),

    /**
     * Cardinal Text
     * 
     */
    @XmlEnumValue("cardinalText")
    CARDINAL_TEXT("cardinalText"),

    /**
     * Ordinal Text
     * 
     */
    @XmlEnumValue("ordinalText")
    ORDINAL_TEXT("ordinalText"),

    /**
     * Hexadecimal Numbering
     * 
     */
    @XmlEnumValue("hex")
    HEX("hex"),

    /**
     * Chicago Manual of Style
     * 
     */
    @XmlEnumValue("chicago")
    CHICAGO("chicago"),

    /**
     * Ideographs
     * 
     */
    @XmlEnumValue("ideographDigital")
    IDEOGRAPH_DIGITAL("ideographDigital"),

    /**
     * Japanese Counting
     * 						System
     * 
     */
    @XmlEnumValue("japaneseCounting")
    JAPANESE_COUNTING("japaneseCounting"),

    /**
     * AIUEO Order Hiragana
     * 
     */
    @XmlEnumValue("aiueo")
    AIUEO("aiueo"),

    /**
     * Iroha Ordered Katakana
     * 
     */
    @XmlEnumValue("iroha")
    IROHA("iroha"),

    /**
     * Double Byte Arabic
     * 						Numerals
     * 
     */
    @XmlEnumValue("decimalFullWidth")
    DECIMAL_FULL_WIDTH("decimalFullWidth"),

    /**
     * Single Byte Arabic
     * 						Numerals
     * 
     */
    @XmlEnumValue("decimalHalfWidth")
    DECIMAL_HALF_WIDTH("decimalHalfWidth"),

    /**
     * Japanese Legal
     * 						Numbering
     * 
     */
    @XmlEnumValue("japaneseLegal")
    JAPANESE_LEGAL("japaneseLegal"),

    /**
     * Japanese Digital Ten Thousand Counting
     * 						System
     * 
     */
    @XmlEnumValue("japaneseDigitalTenThousand")
    JAPANESE_DIGITAL_TEN_THOUSAND("japaneseDigitalTenThousand"),

    /**
     * Decimal Numbers Enclosed in a
     * 						Circle
     * 
     */
    @XmlEnumValue("decimalEnclosedCircle")
    DECIMAL_ENCLOSED_CIRCLE("decimalEnclosedCircle"),

    /**
     * Double Byte Arabic Numerals
     * 						Alternate
     * 
     */
    @XmlEnumValue("decimalFullWidth2")
    DECIMAL_FULL_WIDTH_2("decimalFullWidth2"),

    /**
     * Full-Width AIUEO Order
     * 						Hiragana
     * 
     */
    @XmlEnumValue("aiueoFullWidth")
    AIUEO_FULL_WIDTH("aiueoFullWidth"),

    /**
     * Full-Width Iroha Ordered
     * 						Katakana
     * 
     */
    @XmlEnumValue("irohaFullWidth")
    IROHA_FULL_WIDTH("irohaFullWidth"),

    /**
     * Initial Zero Arabic
     * 						Numerals
     * 
     */
    @XmlEnumValue("decimalZero")
    DECIMAL_ZERO("decimalZero"),

    /**
     * Bullet
     * 
     */
    @XmlEnumValue("bullet")
    BULLET("bullet"),

    /**
     * Korean Ganada Numbering
     * 
     */
    @XmlEnumValue("ganada")
    GANADA("ganada"),

    /**
     * Korean Chosung
     * 						Numbering
     * 
     */
    @XmlEnumValue("chosung")
    CHOSUNG("chosung"),

    /**
     * Decimal Numbers Followed by a
     * 						Period
     * 
     */
    @XmlEnumValue("decimalEnclosedFullstop")
    DECIMAL_ENCLOSED_FULLSTOP("decimalEnclosedFullstop"),

    /**
     * Decimal Numbers Enclosed in
     * 						Parenthesis
     * 
     */
    @XmlEnumValue("decimalEnclosedParen")
    DECIMAL_ENCLOSED_PAREN("decimalEnclosedParen"),

    /**
     * Decimal Numbers Enclosed in a
     * 						Circle
     * 
     */
    @XmlEnumValue("decimalEnclosedCircleChinese")
    DECIMAL_ENCLOSED_CIRCLE_CHINESE("decimalEnclosedCircleChinese"),

    /**
     * Ideographs Enclosed in a
     * 						Circle
     * 
     */
    @XmlEnumValue("ideographEnclosedCircle")
    IDEOGRAPH_ENCLOSED_CIRCLE("ideographEnclosedCircle"),

    /**
     * Traditional Ideograph
     * 						Format
     * 
     */
    @XmlEnumValue("ideographTraditional")
    IDEOGRAPH_TRADITIONAL("ideographTraditional"),

    /**
     * Zodiac Ideograph Format
     * 
     */
    @XmlEnumValue("ideographZodiac")
    IDEOGRAPH_ZODIAC("ideographZodiac"),

    /**
     * Traditional Zodiac Ideograph
     * 						Format
     * 
     */
    @XmlEnumValue("ideographZodiacTraditional")
    IDEOGRAPH_ZODIAC_TRADITIONAL("ideographZodiacTraditional"),

    /**
     * Taiwanese Counting
     * 						System
     * 
     */
    @XmlEnumValue("taiwaneseCounting")
    TAIWANESE_COUNTING("taiwaneseCounting"),

    /**
     * Traditional Legal Ideograph
     * 						Format
     * 
     */
    @XmlEnumValue("ideographLegalTraditional")
    IDEOGRAPH_LEGAL_TRADITIONAL("ideographLegalTraditional"),

    /**
     * Taiwanese Counting Thousand
     * 						System
     * 
     */
    @XmlEnumValue("taiwaneseCountingThousand")
    TAIWANESE_COUNTING_THOUSAND("taiwaneseCountingThousand"),

    /**
     * Taiwanese Digital Counting
     * 						System
     * 
     */
    @XmlEnumValue("taiwaneseDigital")
    TAIWANESE_DIGITAL("taiwaneseDigital"),

    /**
     * Chinese Counting System
     * 
     */
    @XmlEnumValue("chineseCounting")
    CHINESE_COUNTING("chineseCounting"),

    /**
     * Chinese Legal Simplified
     * 						Format
     * 
     */
    @XmlEnumValue("chineseLegalSimplified")
    CHINESE_LEGAL_SIMPLIFIED("chineseLegalSimplified"),

    /**
     * Chinese Counting Thousand
     * 						System
     * 
     */
    @XmlEnumValue("chineseCountingThousand")
    CHINESE_COUNTING_THOUSAND("chineseCountingThousand"),

    /**
     * Korean Digital Counting
     * 						System
     * 
     */
    @XmlEnumValue("koreanDigital")
    KOREAN_DIGITAL("koreanDigital"),

    /**
     * Korean Counting System
     * 
     */
    @XmlEnumValue("koreanCounting")
    KOREAN_COUNTING("koreanCounting"),

    /**
     * Korean Legal Numbering
     * 
     */
    @XmlEnumValue("koreanLegal")
    KOREAN_LEGAL("koreanLegal"),

    /**
     * Korean Digital Counting System
     * 						Alternate
     * 
     */
    @XmlEnumValue("koreanDigital2")
    KOREAN_DIGITAL_2("koreanDigital2"),

    /**
     * Vietnamese Numerals
     * 
     */
    @XmlEnumValue("vietnameseCounting")
    VIETNAMESE_COUNTING("vietnameseCounting"),

    /**
     * Lowercase Russian
     * 						Alphabet
     * 
     */
    @XmlEnumValue("russianLower")
    RUSSIAN_LOWER("russianLower"),

    /**
     * Uppercase Russian
     * 						Alphabet
     * 
     */
    @XmlEnumValue("russianUpper")
    RUSSIAN_UPPER("russianUpper"),

    /**
     * No Numbering
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Number With Dashes
     * 
     */
    @XmlEnumValue("numberInDash")
    NUMBER_IN_DASH("numberInDash"),

    /**
     * Hebrew Numerals
     * 
     */
    @XmlEnumValue("hebrew1")
    HEBREW_1("hebrew1"),

    /**
     * Hebrew Alphabet
     * 
     */
    @XmlEnumValue("hebrew2")
    HEBREW_2("hebrew2"),

    /**
     * Arabic Alphabet
     * 
     */
    @XmlEnumValue("arabicAlpha")
    ARABIC_ALPHA("arabicAlpha"),

    /**
     * Arabic Abjad Numerals
     * 
     */
    @XmlEnumValue("arabicAbjad")
    ARABIC_ABJAD("arabicAbjad"),

    /**
     * Hindi Vowels
     * 
     */
    @XmlEnumValue("hindiVowels")
    HINDI_VOWELS("hindiVowels"),

    /**
     * Hindi Consonants
     * 
     */
    @XmlEnumValue("hindiConsonants")
    HINDI_CONSONANTS("hindiConsonants"),

    /**
     * Hindi Numbers
     * 
     */
    @XmlEnumValue("hindiNumbers")
    HINDI_NUMBERS("hindiNumbers"),

    /**
     * Hindi Counting System
     * 
     */
    @XmlEnumValue("hindiCounting")
    HINDI_COUNTING("hindiCounting"),

    /**
     * Thai Letters
     * 
     */
    @XmlEnumValue("thaiLetters")
    THAI_LETTERS("thaiLetters"),

    /**
     * Thai Numerals
     * 
     */
    @XmlEnumValue("thaiNumbers")
    THAI_NUMBERS("thaiNumbers"),

    /**
     * Thai Counting System
     * 
     */
    @XmlEnumValue("thaiCounting")
    THAI_COUNTING("thaiCounting");
    private final String value;

    NumberFormat(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NumberFormat fromValue(String v) {
        for (NumberFormat c: NumberFormat.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
