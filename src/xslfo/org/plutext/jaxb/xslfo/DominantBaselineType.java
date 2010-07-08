
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dominant_baseline_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="dominant_baseline_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="use-script"/>
 *     &lt;enumeration value="no-change"/>
 *     &lt;enumeration value="reset-size"/>
 *     &lt;enumeration value="ideographic"/>
 *     &lt;enumeration value="alphabetic"/>
 *     &lt;enumeration value="hanging"/>
 *     &lt;enumeration value="mathematical"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "dominant_baseline_Type")
@XmlEnum
public enum DominantBaselineType {

    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("use-script")
    USE_SCRIPT("use-script"),
    @XmlEnumValue("no-change")
    NO_CHANGE("no-change"),
    @XmlEnumValue("reset-size")
    RESET_SIZE("reset-size"),
    @XmlEnumValue("ideographic")
    IDEOGRAPHIC("ideographic"),
    @XmlEnumValue("alphabetic")
    ALPHABETIC("alphabetic"),
    @XmlEnumValue("hanging")
    HANGING("hanging"),
    @XmlEnumValue("mathematical")
    MATHEMATICAL("mathematical"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    DominantBaselineType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DominantBaselineType fromValue(String v) {
        for (DominantBaselineType c: DominantBaselineType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
