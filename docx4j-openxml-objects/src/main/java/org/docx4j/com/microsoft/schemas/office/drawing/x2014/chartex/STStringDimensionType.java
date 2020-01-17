
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_StringDimensionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_StringDimensionType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="cat"/&gt;
 *     &lt;enumeration value="colorStr"/&gt;
 *     &lt;enumeration value="entityId"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_StringDimensionType")
@XmlEnum
public enum STStringDimensionType {

    @XmlEnumValue("cat")
    CAT("cat"),
    @XmlEnumValue("colorStr")
    COLOR_STR("colorStr"),
    @XmlEnumValue("entityId")
    ENTITY_ID("entityId");
    private final String value;

    STStringDimensionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STStringDimensionType fromValue(String v) {
        for (STStringDimensionType c: STStringDimensionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
