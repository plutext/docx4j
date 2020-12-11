
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_DataLabelPos.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DataLabelPos"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="bestFit"/&gt;
 *     &lt;enumeration value="b"/&gt;
 *     &lt;enumeration value="ctr"/&gt;
 *     &lt;enumeration value="inBase"/&gt;
 *     &lt;enumeration value="inEnd"/&gt;
 *     &lt;enumeration value="l"/&gt;
 *     &lt;enumeration value="outEnd"/&gt;
 *     &lt;enumeration value="r"/&gt;
 *     &lt;enumeration value="t"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_DataLabelPos")
@XmlEnum
public enum STDataLabelPos {

    @XmlEnumValue("bestFit")
    BEST_FIT("bestFit"),
    @XmlEnumValue("b")
    B("b"),
    @XmlEnumValue("ctr")
    CTR("ctr"),
    @XmlEnumValue("inBase")
    IN_BASE("inBase"),
    @XmlEnumValue("inEnd")
    IN_END("inEnd"),
    @XmlEnumValue("l")
    L("l"),
    @XmlEnumValue("outEnd")
    OUT_END("outEnd"),
    @XmlEnumValue("r")
    R("r"),
    @XmlEnumValue("t")
    T("t");
    private final String value;

    STDataLabelPos(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDataLabelPos fromValue(String v) {
        for (STDataLabelPos c: STDataLabelPos.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
