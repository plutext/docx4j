
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_QuartileMethod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_QuartileMethod"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="inclusive"/&gt;
 *     &lt;enumeration value="exclusive"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_QuartileMethod")
@XmlEnum
public enum STQuartileMethod {

    @XmlEnumValue("inclusive")
    INCLUSIVE("inclusive"),
    @XmlEnumValue("exclusive")
    EXCLUSIVE("exclusive");
    private final String value;

    STQuartileMethod(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STQuartileMethod fromValue(String v) {
        for (STQuartileMethod c: STQuartileMethod.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
