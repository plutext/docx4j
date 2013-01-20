
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_HtmlFmt.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_HtmlFmt">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="rtf"/>
 *     &lt;enumeration value="all"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_HtmlFmt")
@XmlEnum
public enum STHtmlFmt {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("rtf")
    RTF("rtf"),
    @XmlEnumValue("all")
    ALL("all");
    private final String value;

    STHtmlFmt(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STHtmlFmt fromValue(String v) {
        for (STHtmlFmt c: STHtmlFmt.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
