
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ParentLabelLayout.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ParentLabelLayout"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="none"/&gt;
 *     &lt;enumeration value="banner"/&gt;
 *     &lt;enumeration value="overlapping"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_ParentLabelLayout")
@XmlEnum
public enum STParentLabelLayout {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("banner")
    BANNER("banner"),
    @XmlEnumValue("overlapping")
    OVERLAPPING("overlapping");
    private final String value;

    STParentLabelLayout(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STParentLabelLayout fromValue(String v) {
        for (STParentLabelLayout c: STParentLabelLayout.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
