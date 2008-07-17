
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_RelFromH.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_RelFromH">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="margin"/>
 *     &lt;enumeration value="page"/>
 *     &lt;enumeration value="column"/>
 *     &lt;enumeration value="character"/>
 *     &lt;enumeration value="leftMargin"/>
 *     &lt;enumeration value="rightMargin"/>
 *     &lt;enumeration value="insideMargin"/>
 *     &lt;enumeration value="outsideMargin"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_RelFromH", namespace = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing")
@XmlEnum
public enum STRelFromH {


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
     * Column
     * 
     */
    @XmlEnumValue("column")
    COLUMN("column"),

    /**
     * Character
     * 
     */
    @XmlEnumValue("character")
    CHARACTER("character"),
    @XmlEnumValue("leftMargin")
    LEFT_MARGIN("leftMargin"),
    @XmlEnumValue("rightMargin")
    RIGHT_MARGIN("rightMargin"),
    @XmlEnumValue("insideMargin")
    INSIDE_MARGIN("insideMargin"),
    @XmlEnumValue("outsideMargin")
    OUTSIDE_MARGIN("outsideMargin");
    private final String value;

    STRelFromH(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STRelFromH fromValue(String v) {
        for (STRelFromH c: STRelFromH.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
