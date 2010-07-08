
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for use_font_metrics_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="use_font_metrics_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="use-font-metrics"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "use_font_metrics_Type")
@XmlEnum
public enum UseFontMetricsType {

    @XmlEnumValue("use-font-metrics")
    USE_FONT_METRICS("use-font-metrics");
    private final String value;

    UseFontMetricsType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UseFontMetricsType fromValue(String v) {
        for (UseFontMetricsType c: UseFontMetricsType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
