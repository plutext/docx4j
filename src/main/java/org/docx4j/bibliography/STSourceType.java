
package org.docx4j.bibliography;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_SourceType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_SourceType">
 *   &lt;restriction base="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String">
 *     &lt;enumeration value="ArticleInAPeriodical"/>
 *     &lt;enumeration value="Book"/>
 *     &lt;enumeration value="BookSection"/>
 *     &lt;enumeration value="JournalArticle"/>
 *     &lt;enumeration value="ConferenceProceedings"/>
 *     &lt;enumeration value="Report"/>
 *     &lt;enumeration value="SoundRecording"/>
 *     &lt;enumeration value="Performance"/>
 *     &lt;enumeration value="Art"/>
 *     &lt;enumeration value="DocumentFromInternetSite"/>
 *     &lt;enumeration value="InternetSite"/>
 *     &lt;enumeration value="Film"/>
 *     &lt;enumeration value="Interview"/>
 *     &lt;enumeration value="Patent"/>
 *     &lt;enumeration value="ElectronicSource"/>
 *     &lt;enumeration value="Case"/>
 *     &lt;enumeration value="Misc"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_SourceType")
@XmlEnum
public enum STSourceType {


    /**
     * Article in a Periodical
     * 
     */
    @XmlEnumValue("ArticleInAPeriodical")
    ARTICLE_IN_A_PERIODICAL("ArticleInAPeriodical"),

    /**
     * Book
     * 
     */
    @XmlEnumValue("Book")
    BOOK("Book"),

    /**
     * Book Section
     * 
     */
    @XmlEnumValue("BookSection")
    BOOK_SECTION("BookSection"),

    /**
     * Journal Article
     * 
     */
    @XmlEnumValue("JournalArticle")
    JOURNAL_ARTICLE("JournalArticle"),

    /**
     * Conference Proceedings
     * 
     */
    @XmlEnumValue("ConferenceProceedings")
    CONFERENCE_PROCEEDINGS("ConferenceProceedings"),

    /**
     * Reporter
     * 
     */
    @XmlEnumValue("Report")
    REPORT("Report"),

    /**
     * Sound Recording
     * 
     */
    @XmlEnumValue("SoundRecording")
    SOUND_RECORDING("SoundRecording"),

    /**
     * Performance
     * 
     */
    @XmlEnumValue("Performance")
    PERFORMANCE("Performance"),

    /**
     * Art
     * 
     */
    @XmlEnumValue("Art")
    ART("Art"),

    /**
     * Document from Internet Site
     * 
     */
    @XmlEnumValue("DocumentFromInternetSite")
    DOCUMENT_FROM_INTERNET_SITE("DocumentFromInternetSite"),

    /**
     * Internet Site
     * 
     */
    @XmlEnumValue("InternetSite")
    INTERNET_SITE("InternetSite"),

    /**
     * Film
     * 
     */
    @XmlEnumValue("Film")
    FILM("Film"),

    /**
     * Interview
     * 
     */
    @XmlEnumValue("Interview")
    INTERVIEW("Interview"),

    /**
     * Patent
     * 
     */
    @XmlEnumValue("Patent")
    PATENT("Patent"),

    /**
     * Electronic Source
     * 
     */
    @XmlEnumValue("ElectronicSource")
    ELECTRONIC_SOURCE("ElectronicSource"),

    /**
     * Case
     * 
     */
    @XmlEnumValue("Case")
    CASE("Case"),

    /**
     * Miscellaneous
     * 
     */
    @XmlEnumValue("Misc")
    MISC("Misc");
    private final String value;

    STSourceType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STSourceType fromValue(String v) {
        for (STSourceType c: STSourceType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
