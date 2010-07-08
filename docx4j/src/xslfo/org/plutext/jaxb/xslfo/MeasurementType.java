
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for measurement_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="measurement_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="px"/>
 *     &lt;enumeration value="pt"/>
 *     &lt;enumeration value="mm"/>
 *     &lt;enumeration value="in"/>
 *     &lt;enumeration value="cm"/>
 *     &lt;enumeration value="em"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "measurement_Type")
@XmlEnum
public enum MeasurementType {

    @XmlEnumValue("px")
    PX("px"),
    @XmlEnumValue("pt")
    PT("pt"),
    @XmlEnumValue("mm")
    MM("mm"),
    @XmlEnumValue("in")
    IN("in"),
    @XmlEnumValue("cm")
    CM("cm"),
    @XmlEnumValue("em")
    EM("em");
    private final String value;

    MeasurementType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MeasurementType fromValue(String v) {
        for (MeasurementType c: MeasurementType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
