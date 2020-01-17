
package org.docx4j.com.microsoft.schemas.office.drawing.x2018.hyperlinkcolor;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_HyperlinkColor.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_HyperlinkColor"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="hlink"/&gt;
 *     &lt;enumeration value="tx"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_HyperlinkColor")
@XmlEnum
public enum STHyperlinkColor {

    @XmlEnumValue("hlink")
    HLINK("hlink"),
    @XmlEnumValue("tx")
    TX("tx");
    private final String value;

    STHyperlinkColor(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STHyperlinkColor fromValue(String v) {
        for (STHyperlinkColor c: STHyperlinkColor.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
