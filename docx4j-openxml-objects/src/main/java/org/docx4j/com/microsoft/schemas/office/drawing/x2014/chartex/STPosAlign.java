
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PosAlign.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PosAlign"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="min"/&gt;
 *     &lt;enumeration value="ctr"/&gt;
 *     &lt;enumeration value="max"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_PosAlign")
@XmlEnum
public enum STPosAlign {

    @XmlEnumValue("min")
    MIN("min"),
    @XmlEnumValue("ctr")
    CTR("ctr"),
    @XmlEnumValue("max")
    MAX("max");
    private final String value;

    STPosAlign(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPosAlign fromValue(String v) {
        for (STPosAlign c: STPosAlign.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
