
package org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingDrawing;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_SizeRelFromH.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_SizeRelFromH"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="margin"/&gt;
 *     &lt;enumeration value="page"/&gt;
 *     &lt;enumeration value="leftMargin"/&gt;
 *     &lt;enumeration value="rightMargin"/&gt;
 *     &lt;enumeration value="insideMargin"/&gt;
 *     &lt;enumeration value="outsideMargin"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_SizeRelFromH")
@XmlEnum
public enum STSizeRelFromH {

    @XmlEnumValue("margin")
    MARGIN("margin"),
    @XmlEnumValue("page")
    PAGE("page"),
    @XmlEnumValue("leftMargin")
    LEFT_MARGIN("leftMargin"),
    @XmlEnumValue("rightMargin")
    RIGHT_MARGIN("rightMargin"),
    @XmlEnumValue("insideMargin")
    INSIDE_MARGIN("insideMargin"),
    @XmlEnumValue("outsideMargin")
    OUTSIDE_MARGIN("outsideMargin");
    private final String value;

    STSizeRelFromH(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STSizeRelFromH fromValue(String v) {
        for (STSizeRelFromH c: STSizeRelFromH.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
