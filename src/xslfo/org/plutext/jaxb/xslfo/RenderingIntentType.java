
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for rendering_intent_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="rendering_intent_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="perceptual"/>
 *     &lt;enumeration value="relative-colorimetric"/>
 *     &lt;enumeration value="saturation"/>
 *     &lt;enumeration value="absolute-colorimetric"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "rendering_intent_Type")
@XmlEnum
public enum RenderingIntentType {

    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("perceptual")
    PERCEPTUAL("perceptual"),
    @XmlEnumValue("relative-colorimetric")
    RELATIVE_COLORIMETRIC("relative-colorimetric"),
    @XmlEnumValue("saturation")
    SATURATION("saturation"),
    @XmlEnumValue("absolute-colorimetric")
    ABSOLUTE_COLORIMETRIC("absolute-colorimetric"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    RenderingIntentType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RenderingIntentType fromValue(String v) {
        for (RenderingIntentType c: RenderingIntentType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
