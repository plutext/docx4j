
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for scale_to_fit_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="scale_to_fit_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="scale-to-fit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "scale_to_fit_Type")
@XmlEnum
public enum ScaleToFitType {

    @XmlEnumValue("scale-to-fit")
    SCALE_TO_FIT("scale-to-fit");
    private final String value;

    ScaleToFitType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ScaleToFitType fromValue(String v) {
        for (ScaleToFitType c: ScaleToFitType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
