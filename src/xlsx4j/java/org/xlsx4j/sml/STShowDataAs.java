
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ShowDataAs.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ShowDataAs">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="normal"/>
 *     &lt;enumeration value="difference"/>
 *     &lt;enumeration value="percent"/>
 *     &lt;enumeration value="percentDiff"/>
 *     &lt;enumeration value="runTotal"/>
 *     &lt;enumeration value="percentOfRow"/>
 *     &lt;enumeration value="percentOfCol"/>
 *     &lt;enumeration value="percentOfTotal"/>
 *     &lt;enumeration value="index"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ShowDataAs")
@XmlEnum
public enum STShowDataAs {

    @XmlEnumValue("normal")
    NORMAL("normal"),
    @XmlEnumValue("difference")
    DIFFERENCE("difference"),
    @XmlEnumValue("percent")
    PERCENT("percent"),
    @XmlEnumValue("percentDiff")
    PERCENT_DIFF("percentDiff"),
    @XmlEnumValue("runTotal")
    RUN_TOTAL("runTotal"),
    @XmlEnumValue("percentOfRow")
    PERCENT_OF_ROW("percentOfRow"),
    @XmlEnumValue("percentOfCol")
    PERCENT_OF_COL("percentOfCol"),
    @XmlEnumValue("percentOfTotal")
    PERCENT_OF_TOTAL("percentOfTotal"),
    @XmlEnumValue("index")
    INDEX("index");
    private final String value;

    STShowDataAs(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STShowDataAs fromValue(String v) {
        for (STShowDataAs c: STShowDataAs.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
