
package org.docx4j.org.w3.x2003.inkML;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for brushTip.type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="brushTip.type"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="ellipse"/&gt;
 *     &lt;enumeration value="rectangle"/&gt;
 *     &lt;enumeration value="drop"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "brushTip.type")
@XmlEnum
public enum BrushTipType {

    @XmlEnumValue("ellipse")
    ELLIPSE("ellipse"),
    @XmlEnumValue("rectangle")
    RECTANGLE("rectangle"),
    @XmlEnumValue("drop")
    DROP("drop");
    private final String value;

    BrushTipType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BrushTipType fromValue(String v) {
        for (BrushTipType c: BrushTipType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
