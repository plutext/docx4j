
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for show_destination_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="show_destination_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="replace"/>
 *     &lt;enumeration value="new"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "show_destination_Type")
@XmlEnum
public enum ShowDestinationType {

    @XmlEnumValue("replace")
    REPLACE("replace"),
    @XmlEnumValue("new")
    NEW("new");
    private final String value;

    ShowDestinationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ShowDestinationType fromValue(String v) {
        for (ShowDestinationType c: ShowDestinationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
