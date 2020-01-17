
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TickMarksType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TickMarksType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="in"/&gt;
 *     &lt;enumeration value="out"/&gt;
 *     &lt;enumeration value="cross"/&gt;
 *     &lt;enumeration value="none"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TickMarksType")
@XmlEnum
public enum STTickMarksType {

    @XmlEnumValue("in")
    IN("in"),
    @XmlEnumValue("out")
    OUT("out"),
    @XmlEnumValue("cross")
    CROSS("cross"),
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    STTickMarksType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTickMarksType fromValue(String v) {
        for (STTickMarksType c: STTickMarksType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
