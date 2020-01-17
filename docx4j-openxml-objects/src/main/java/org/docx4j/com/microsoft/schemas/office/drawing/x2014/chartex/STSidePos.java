
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_SidePos.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_SidePos"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="l"/&gt;
 *     &lt;enumeration value="t"/&gt;
 *     &lt;enumeration value="r"/&gt;
 *     &lt;enumeration value="b"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_SidePos")
@XmlEnum
public enum STSidePos {

    @XmlEnumValue("l")
    L("l"),
    @XmlEnumValue("t")
    T("t"),
    @XmlEnumValue("r")
    R("r"),
    @XmlEnumValue("b")
    B("b");
    private final String value;

    STSidePos(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STSidePos fromValue(String v) {
        for (STSidePos c: STSidePos.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
