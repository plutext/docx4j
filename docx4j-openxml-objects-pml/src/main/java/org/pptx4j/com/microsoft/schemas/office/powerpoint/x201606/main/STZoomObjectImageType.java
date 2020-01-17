
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x201606.main;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ZoomObjectImageType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ZoomObjectImageType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="preview"/&gt;
 *     &lt;enumeration value="cover"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_ZoomObjectImageType")
@XmlEnum
public enum STZoomObjectImageType {

    @XmlEnumValue("preview")
    PREVIEW("preview"),
    @XmlEnumValue("cover")
    COVER("cover");
    private final String value;

    STZoomObjectImageType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STZoomObjectImageType fromValue(String v) {
        for (STZoomObjectImageType c: STZoomObjectImageType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
