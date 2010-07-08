
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for retrieve_boundary_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="retrieve_boundary_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="page"/>
 *     &lt;enumeration value="page-sequence"/>
 *     &lt;enumeration value="document"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "retrieve_boundary_Type")
@XmlEnum
public enum RetrieveBoundaryType {

    @XmlEnumValue("page")
    PAGE("page"),
    @XmlEnumValue("page-sequence")
    PAGE_SEQUENCE("page-sequence"),
    @XmlEnumValue("document")
    DOCUMENT("document");
    private final String value;

    RetrieveBoundaryType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RetrieveBoundaryType fromValue(String v) {
        for (RetrieveBoundaryType c: RetrieveBoundaryType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
