
package org.docx4j.com.microsoft.schemas.office.drawing.x2016.ink;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_InkEffectsType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_InkEffectsType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="none"/&gt;
 *     &lt;enumeration value="pencil"/&gt;
 *     &lt;enumeration value="rainbow"/&gt;
 *     &lt;enumeration value="galaxy"/&gt;
 *     &lt;enumeration value="gold"/&gt;
 *     &lt;enumeration value="silver"/&gt;
 *     &lt;enumeration value="lava"/&gt;
 *     &lt;enumeration value="ocean"/&gt;
 *     &lt;enumeration value="rosegold"/&gt;
 *     &lt;enumeration value="bronze"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_InkEffectsType", namespace = "http://schemas.microsoft.com/office/drawing/2016/ink")
@XmlEnum
public enum STInkEffectsType {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("pencil")
    PENCIL("pencil"),
    @XmlEnumValue("rainbow")
    RAINBOW("rainbow"),
    @XmlEnumValue("galaxy")
    GALAXY("galaxy"),
    @XmlEnumValue("gold")
    GOLD("gold"),
    @XmlEnumValue("silver")
    SILVER("silver"),
    @XmlEnumValue("lava")
    LAVA("lava"),
    @XmlEnumValue("ocean")
    OCEAN("ocean"),
    @XmlEnumValue("rosegold")
    ROSEGOLD("rosegold"),
    @XmlEnumValue("bronze")
    BRONZE("bronze");
    private final String value;

    STInkEffectsType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STInkEffectsType fromValue(String v) {
        for (STInkEffectsType c: STInkEffectsType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
