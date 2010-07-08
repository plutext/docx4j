
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for text_decoration_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="text_decoration_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="underline"/>
 *     &lt;enumeration value="no-underline"/>
 *     &lt;enumeration value="overline"/>
 *     &lt;enumeration value="no-overline"/>
 *     &lt;enumeration value="line-through"/>
 *     &lt;enumeration value="no-line-through"/>
 *     &lt;enumeration value="blink"/>
 *     &lt;enumeration value="no-blink"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "text_decoration_Type")
@XmlEnum
public enum TextDecorationType {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("underline")
    UNDERLINE("underline"),
    @XmlEnumValue("no-underline")
    NO_UNDERLINE("no-underline"),
    @XmlEnumValue("overline")
    OVERLINE("overline"),
    @XmlEnumValue("no-overline")
    NO_OVERLINE("no-overline"),
    @XmlEnumValue("line-through")
    LINE_THROUGH("line-through"),
    @XmlEnumValue("no-line-through")
    NO_LINE_THROUGH("no-line-through"),
    @XmlEnumValue("blink")
    BLINK("blink"),
    @XmlEnumValue("no-blink")
    NO_BLINK("no-blink"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    TextDecorationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TextDecorationType fromValue(String v) {
        for (TextDecorationType c: TextDecorationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
