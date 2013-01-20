
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_SortType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_SortType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="ascending"/>
 *     &lt;enumeration value="descending"/>
 *     &lt;enumeration value="ascendingAlpha"/>
 *     &lt;enumeration value="descendingAlpha"/>
 *     &lt;enumeration value="ascendingNatural"/>
 *     &lt;enumeration value="descendingNatural"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_SortType")
@XmlEnum
public enum STSortType {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("ascending")
    ASCENDING("ascending"),
    @XmlEnumValue("descending")
    DESCENDING("descending"),
    @XmlEnumValue("ascendingAlpha")
    ASCENDING_ALPHA("ascendingAlpha"),
    @XmlEnumValue("descendingAlpha")
    DESCENDING_ALPHA("descendingAlpha"),
    @XmlEnumValue("ascendingNatural")
    ASCENDING_NATURAL("ascendingNatural"),
    @XmlEnumValue("descendingNatural")
    DESCENDING_NATURAL("descendingNatural");
    private final String value;

    STSortType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STSortType fromValue(String v) {
        for (STSortType c: STSortType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
