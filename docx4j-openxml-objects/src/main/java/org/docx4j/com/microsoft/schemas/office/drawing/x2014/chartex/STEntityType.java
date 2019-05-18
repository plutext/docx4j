
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_EntityType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_EntityType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="Address"/&gt;
 *     &lt;enumeration value="AdminDistrict"/&gt;
 *     &lt;enumeration value="AdminDistrict2"/&gt;
 *     &lt;enumeration value="AdminDistrict3"/&gt;
 *     &lt;enumeration value="Continent"/&gt;
 *     &lt;enumeration value="CountryRegion"/&gt;
 *     &lt;enumeration value="Locality"/&gt;
 *     &lt;enumeration value="Ocean"/&gt;
 *     &lt;enumeration value="Planet"/&gt;
 *     &lt;enumeration value="PostalCode"/&gt;
 *     &lt;enumeration value="Region"/&gt;
 *     &lt;enumeration value="Unsupported"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_EntityType")
@XmlEnum
public enum STEntityType {

    @XmlEnumValue("Address")
    ADDRESS("Address"),
    @XmlEnumValue("AdminDistrict")
    ADMIN_DISTRICT("AdminDistrict"),
    @XmlEnumValue("AdminDistrict2")
    ADMIN_DISTRICT_2("AdminDistrict2"),
    @XmlEnumValue("AdminDistrict3")
    ADMIN_DISTRICT_3("AdminDistrict3"),
    @XmlEnumValue("Continent")
    CONTINENT("Continent"),
    @XmlEnumValue("CountryRegion")
    COUNTRY_REGION("CountryRegion"),
    @XmlEnumValue("Locality")
    LOCALITY("Locality"),
    @XmlEnumValue("Ocean")
    OCEAN("Ocean"),
    @XmlEnumValue("Planet")
    PLANET("Planet"),
    @XmlEnumValue("PostalCode")
    POSTAL_CODE("PostalCode"),
    @XmlEnumValue("Region")
    REGION("Region"),
    @XmlEnumValue("Unsupported")
    UNSUPPORTED("Unsupported");
    private final String value;

    STEntityType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STEntityType fromValue(String v) {
        for (STEntityType c: STEntityType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
