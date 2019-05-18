
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_GeoMappingLevel.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_GeoMappingLevel"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="dataOnly"/&gt;
 *     &lt;enumeration value="postalCode"/&gt;
 *     &lt;enumeration value="county"/&gt;
 *     &lt;enumeration value="state"/&gt;
 *     &lt;enumeration value="countryRegion"/&gt;
 *     &lt;enumeration value="countryRegionList"/&gt;
 *     &lt;enumeration value="world"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_GeoMappingLevel")
@XmlEnum
public enum STGeoMappingLevel {

    @XmlEnumValue("dataOnly")
    DATA_ONLY("dataOnly"),
    @XmlEnumValue("postalCode")
    POSTAL_CODE("postalCode"),
    @XmlEnumValue("county")
    COUNTY("county"),
    @XmlEnumValue("state")
    STATE("state"),
    @XmlEnumValue("countryRegion")
    COUNTRY_REGION("countryRegion"),
    @XmlEnumValue("countryRegionList")
    COUNTRY_REGION_LIST("countryRegionList"),
    @XmlEnumValue("world")
    WORLD("world");
    private final String value;

    STGeoMappingLevel(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STGeoMappingLevel fromValue(String v) {
        for (STGeoMappingLevel c: STGeoMappingLevel.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
