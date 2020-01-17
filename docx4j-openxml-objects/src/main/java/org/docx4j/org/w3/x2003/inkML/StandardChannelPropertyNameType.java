
package org.docx4j.org.w3.x2003.inkML;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for standardChannelPropertyName.type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="standardChannelPropertyName.type"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="threshold"/&gt;
 *     &lt;enumeration value="resolution"/&gt;
 *     &lt;enumeration value="quantization"/&gt;
 *     &lt;enumeration value="noise"/&gt;
 *     &lt;enumeration value="accuracy"/&gt;
 *     &lt;enumeration value="crossCoupling"/&gt;
 *     &lt;enumeration value="skew"/&gt;
 *     &lt;enumeration value="minBandwidth"/&gt;
 *     &lt;enumeration value="peakRate"/&gt;
 *     &lt;enumeration value="distortion"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "standardChannelPropertyName.type")
@XmlEnum
public enum StandardChannelPropertyNameType {

    @XmlEnumValue("threshold")
    THRESHOLD("threshold"),
    @XmlEnumValue("resolution")
    RESOLUTION("resolution"),
    @XmlEnumValue("quantization")
    QUANTIZATION("quantization"),
    @XmlEnumValue("noise")
    NOISE("noise"),
    @XmlEnumValue("accuracy")
    ACCURACY("accuracy"),
    @XmlEnumValue("crossCoupling")
    CROSS_COUPLING("crossCoupling"),
    @XmlEnumValue("skew")
    SKEW("skew"),
    @XmlEnumValue("minBandwidth")
    MIN_BANDWIDTH("minBandwidth"),
    @XmlEnumValue("peakRate")
    PEAK_RATE("peakRate"),
    @XmlEnumValue("distortion")
    DISTORTION("distortion");
    private final String value;

    StandardChannelPropertyNameType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static StandardChannelPropertyNameType fromValue(String v) {
        for (StandardChannelPropertyNameType c: StandardChannelPropertyNameType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
