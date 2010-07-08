
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for scaling_method_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="scaling_method_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="integer-pixels"/>
 *     &lt;enumeration value="resample-any-method"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "scaling_method_Type")
@XmlEnum
public enum ScalingMethodType {

    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("integer-pixels")
    INTEGER_PIXELS("integer-pixels"),
    @XmlEnumValue("resample-any-method")
    RESAMPLE_ANY_METHOD("resample-any-method"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    ScalingMethodType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ScalingMethodType fromValue(String v) {
        for (ScalingMethodType c: ScalingMethodType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
