
package org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingDrawing;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_SizeRelFromV.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_SizeRelFromV"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="margin"/&gt;
 *     &lt;enumeration value="page"/&gt;
 *     &lt;enumeration value="topMargin"/&gt;
 *     &lt;enumeration value="bottomMargin"/&gt;
 *     &lt;enumeration value="insideMargin"/&gt;
 *     &lt;enumeration value="outsideMargin"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_SizeRelFromV")
@XmlEnum
public enum STSizeRelFromV {

    @XmlEnumValue("margin")
    MARGIN("margin"),
    @XmlEnumValue("page")
    PAGE("page"),
    @XmlEnumValue("topMargin")
    TOP_MARGIN("topMargin"),
    @XmlEnumValue("bottomMargin")
    BOTTOM_MARGIN("bottomMargin"),
    @XmlEnumValue("insideMargin")
    INSIDE_MARGIN("insideMargin"),
    @XmlEnumValue("outsideMargin")
    OUTSIDE_MARGIN("outsideMargin");
    private final String value;

    STSizeRelFromV(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STSizeRelFromV fromValue(String v) {
        for (STSizeRelFromV c: STSizeRelFromV.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
