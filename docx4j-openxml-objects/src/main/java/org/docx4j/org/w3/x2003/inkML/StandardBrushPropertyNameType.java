
package org.docx4j.org.w3.x2003.inkML;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for standardBrushPropertyName.type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="standardBrushPropertyName.type"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="width"/&gt;
 *     &lt;enumeration value="height"/&gt;
 *     &lt;enumeration value="color"/&gt;
 *     &lt;enumeration value="transparency"/&gt;
 *     &lt;enumeration value="tip"/&gt;
 *     &lt;enumeration value="rasterOp"/&gt;
 *     &lt;enumeration value="antiAliased"/&gt;
 *     &lt;enumeration value="fitToCurve"/&gt;
 *     &lt;enumeration value="ignorePressure"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "standardBrushPropertyName.type")
@XmlEnum
public enum StandardBrushPropertyNameType {

    @XmlEnumValue("width")
    WIDTH("width"),
    @XmlEnumValue("height")
    HEIGHT("height"),
    @XmlEnumValue("color")
    COLOR("color"),
    @XmlEnumValue("transparency")
    TRANSPARENCY("transparency"),
    @XmlEnumValue("tip")
    TIP("tip"),
    @XmlEnumValue("rasterOp")
    RASTER_OP("rasterOp"),
    @XmlEnumValue("antiAliased")
    ANTI_ALIASED("antiAliased"),
    @XmlEnumValue("fitToCurve")
    FIT_TO_CURVE("fitToCurve"),
    @XmlEnumValue("ignorePressure")
    IGNORE_PRESSURE("ignorePressure");
    private final String value;

    StandardBrushPropertyNameType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static StandardBrushPropertyNameType fromValue(String v) {
        for (StandardBrushPropertyNameType c: StandardBrushPropertyNameType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
