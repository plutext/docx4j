
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_RelFromV.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_RelFromV">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="margin"/>
 *     &lt;enumeration value="page"/>
 *     &lt;enumeration value="paragraph"/>
 *     &lt;enumeration value="line"/>
 *     &lt;enumeration value="topMargin"/>
 *     &lt;enumeration value="bottomMargin"/>
 *     &lt;enumeration value="insideMargin"/>
 *     &lt;enumeration value="outsideMargin"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_RelFromV", namespace = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing")
@XmlEnum
public enum STRelFromV {


    /**
     * Page Margin
     * 
     */
    @XmlEnumValue("margin")
    MARGIN("margin"),

    /**
     * Page Edge
     * 
     */
    @XmlEnumValue("page")
    PAGE("page"),

    /**
     * Paragraph
     * 
     */
    @XmlEnumValue("paragraph")
    PARAGRAPH("paragraph"),

    /**
     * Line
     * 
     */
    @XmlEnumValue("line")
    LINE("line"),
    @XmlEnumValue("topMargin")
    TOP_MARGIN("topMargin"),
    @XmlEnumValue("bottomMargin")
    BOTTOM_MARGIN("bottomMargin"),
    @XmlEnumValue("insideMargin")
    INSIDE_MARGIN("insideMargin"),
    @XmlEnumValue("outsideMargin")
    OUTSIDE_MARGIN("outsideMargin");
    private final String value;

    STRelFromV(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STRelFromV fromValue(String v) {
        for (STRelFromV c: STRelFromV.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
