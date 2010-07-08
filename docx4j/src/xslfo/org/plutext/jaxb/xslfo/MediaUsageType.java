
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for media_usage_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="media_usage_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="paginate"/>
 *     &lt;enumeration value="bounded-in-one-dimension"/>
 *     &lt;enumeration value="unbounded"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "media_usage_Type")
@XmlEnum
public enum MediaUsageType {

    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("paginate")
    PAGINATE("paginate"),
    @XmlEnumValue("bounded-in-one-dimension")
    BOUNDED_IN_ONE_DIMENSION("bounded-in-one-dimension"),
    @XmlEnumValue("unbounded")
    UNBOUNDED("unbounded");
    private final String value;

    MediaUsageType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MediaUsageType fromValue(String v) {
        for (MediaUsageType c: MediaUsageType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
