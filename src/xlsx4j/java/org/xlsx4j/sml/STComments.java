
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_Comments.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Comments">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="commNone"/>
 *     &lt;enumeration value="commIndicator"/>
 *     &lt;enumeration value="commIndAndComment"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Comments")
@XmlEnum
public enum STComments {

    @XmlEnumValue("commNone")
    COMM_NONE("commNone"),
    @XmlEnumValue("commIndicator")
    COMM_INDICATOR("commIndicator"),
    @XmlEnumValue("commIndAndComment")
    COMM_IND_AND_COMMENT("commIndAndComment");
    private final String value;

    STComments(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STComments fromValue(String v) {
        for (STComments c: STComments.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
