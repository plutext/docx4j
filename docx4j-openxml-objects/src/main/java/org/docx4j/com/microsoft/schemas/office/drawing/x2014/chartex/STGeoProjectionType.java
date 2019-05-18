
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_GeoProjectionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_GeoProjectionType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="mercator"/&gt;
 *     &lt;enumeration value="miller"/&gt;
 *     &lt;enumeration value="robinson"/&gt;
 *     &lt;enumeration value="albers"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_GeoProjectionType")
@XmlEnum
public enum STGeoProjectionType {

    @XmlEnumValue("mercator")
    MERCATOR("mercator"),
    @XmlEnumValue("miller")
    MILLER("miller"),
    @XmlEnumValue("robinson")
    ROBINSON("robinson"),
    @XmlEnumValue("albers")
    ALBERS("albers");
    private final String value;

    STGeoProjectionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STGeoProjectionType fromValue(String v) {
        for (STGeoProjectionType c: STGeoProjectionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
