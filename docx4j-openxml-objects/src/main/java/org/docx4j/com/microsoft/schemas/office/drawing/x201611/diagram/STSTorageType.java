
package org.docx4j.com.microsoft.schemas.office.drawing.x201611.diagram;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_STorageType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_STorageType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="sibTrans"/&gt;
 *     &lt;enumeration value="parTrans"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_STorageType")
@XmlEnum
public enum STSTorageType {

    @XmlEnumValue("sibTrans")
    SIB_TRANS("sibTrans"),
    @XmlEnumValue("parTrans")
    PAR_TRANS("parTrans");
    private final String value;

    STSTorageType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STSTorageType fromValue(String v) {
        for (STSTorageType c: STSTorageType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
