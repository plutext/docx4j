
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for retrieve_position_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="retrieve_position_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="first-starting-within-page"/>
 *     &lt;enumeration value="first-including-carryover"/>
 *     &lt;enumeration value="last-starting-within-page"/>
 *     &lt;enumeration value="last-ending-within-page"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "retrieve_position_Type")
@XmlEnum
public enum RetrievePositionType {

    @XmlEnumValue("first-starting-within-page")
    FIRST_STARTING_WITHIN_PAGE("first-starting-within-page"),
    @XmlEnumValue("first-including-carryover")
    FIRST_INCLUDING_CARRYOVER("first-including-carryover"),
    @XmlEnumValue("last-starting-within-page")
    LAST_STARTING_WITHIN_PAGE("last-starting-within-page"),
    @XmlEnumValue("last-ending-within-page")
    LAST_ENDING_WITHIN_PAGE("last-ending-within-page");
    private final String value;

    RetrievePositionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RetrievePositionType fromValue(String v) {
        for (RetrievePositionType c: RetrievePositionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
