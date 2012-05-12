
package org.docx4j.sharedtypes;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_CalendarType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_CalendarType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="gregorian"/>
 *     &lt;enumeration value="gregorianUs"/>
 *     &lt;enumeration value="gregorianMeFrench"/>
 *     &lt;enumeration value="gregorianArabic"/>
 *     &lt;enumeration value="hijri"/>
 *     &lt;enumeration value="hebrew"/>
 *     &lt;enumeration value="taiwan"/>
 *     &lt;enumeration value="japan"/>
 *     &lt;enumeration value="thai"/>
 *     &lt;enumeration value="korea"/>
 *     &lt;enumeration value="saka"/>
 *     &lt;enumeration value="gregorianXlitEnglish"/>
 *     &lt;enumeration value="gregorianXlitFrench"/>
 *     &lt;enumeration value="none"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_CalendarType", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes")
@XmlEnum
public enum STCalendarType {


    /**
     * Gregorian
     * 
     */
    @XmlEnumValue("gregorian")
    GREGORIAN("gregorian"),

    /**
     * Gregorian English Calendar
     * 
     */
    @XmlEnumValue("gregorianUs")
    GREGORIAN_US("gregorianUs"),

    /**
     * Gregorian Middle East French Calendar
     * 
     */
    @XmlEnumValue("gregorianMeFrench")
    GREGORIAN_ME_FRENCH("gregorianMeFrench"),

    /**
     * Gregorian Arabic Calendar
     * 
     */
    @XmlEnumValue("gregorianArabic")
    GREGORIAN_ARABIC("gregorianArabic"),

    /**
     * Hijri
     * 
     */
    @XmlEnumValue("hijri")
    HIJRI("hijri"),

    /**
     * Hebrew
     * 
     */
    @XmlEnumValue("hebrew")
    HEBREW("hebrew"),

    /**
     * Taiwan
     * 
     */
    @XmlEnumValue("taiwan")
    TAIWAN("taiwan"),

    /**
     * Japanese Emperor Era
     * 
     */
    @XmlEnumValue("japan")
    JAPAN("japan"),

    /**
     * Thai
     * 
     */
    @XmlEnumValue("thai")
    THAI("thai"),

    /**
     * Korean Tangun Era
     * 
     */
    @XmlEnumValue("korea")
    KOREA("korea"),

    /**
     * Saka Era
     * 
     */
    @XmlEnumValue("saka")
    SAKA("saka"),

    /**
     * Gregorian Transliterated English
     * 
     */
    @XmlEnumValue("gregorianXlitEnglish")
    GREGORIAN_XLIT_ENGLISH("gregorianXlitEnglish"),

    /**
     * Gregorian Transliterated French
     * 
     */
    @XmlEnumValue("gregorianXlitFrench")
    GREGORIAN_XLIT_FRENCH("gregorianXlitFrench"),

    /**
     * No Calendar Type
     * 
     */
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    STCalendarType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STCalendarType fromValue(String v) {
        for (STCalendarType c: STCalendarType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
