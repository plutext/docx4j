
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x201804.main;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ClassificationOutcomeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ClassificationOutcomeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="none"/&gt;
 *     &lt;enumeration value="hdr"/&gt;
 *     &lt;enumeration value="ftr"/&gt;
 *     &lt;enumeration value="watermark"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_ClassificationOutcomeType")
@XmlEnum
public enum STClassificationOutcomeType {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("hdr")
    HDR("hdr"),
    @XmlEnumValue("ftr")
    FTR("ftr"),
    @XmlEnumValue("watermark")
    WATERMARK("watermark");
    private final String value;

    STClassificationOutcomeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STClassificationOutcomeType fromValue(String v) {
        for (STClassificationOutcomeType c: STClassificationOutcomeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
