
package org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_MarkerStyle.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_MarkerStyle"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="circle"/&gt;
 *     &lt;enumeration value="dash"/&gt;
 *     &lt;enumeration value="diamond"/&gt;
 *     &lt;enumeration value="dot"/&gt;
 *     &lt;enumeration value="plus"/&gt;
 *     &lt;enumeration value="square"/&gt;
 *     &lt;enumeration value="star"/&gt;
 *     &lt;enumeration value="triangle"/&gt;
 *     &lt;enumeration value="x"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_MarkerStyle")
@XmlEnum
public enum STMarkerStyle {

    @XmlEnumValue("circle")
    CIRCLE("circle"),
    @XmlEnumValue("dash")
    DASH("dash"),
    @XmlEnumValue("diamond")
    DIAMOND("diamond"),
    @XmlEnumValue("dot")
    DOT("dot"),
    @XmlEnumValue("plus")
    PLUS("plus"),
    @XmlEnumValue("square")
    SQUARE("square"),
    @XmlEnumValue("star")
    STAR("star"),
    @XmlEnumValue("triangle")
    TRIANGLE("triangle"),
    @XmlEnumValue("x")
    X("x");
    private final String value;

    STMarkerStyle(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STMarkerStyle fromValue(String v) {
        for (STMarkerStyle c: STMarkerStyle.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
