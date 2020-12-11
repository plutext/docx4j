
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_IntervalClosedSide.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_IntervalClosedSide"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="l"/&gt;
 *     &lt;enumeration value="r"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_IntervalClosedSide")
@XmlEnum
public enum STIntervalClosedSide {

    @XmlEnumValue("l")
    L("l"),
    @XmlEnumValue("r")
    R("r");
    private final String value;

    STIntervalClosedSide(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STIntervalClosedSide fromValue(String v) {
        for (STIntervalClosedSide c: STIntervalClosedSide.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
