
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_BlackWhiteMode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_BlackWhiteMode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="clr"/>
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="gray"/>
 *     &lt;enumeration value="ltGray"/>
 *     &lt;enumeration value="invGray"/>
 *     &lt;enumeration value="grayWhite"/>
 *     &lt;enumeration value="blackGray"/>
 *     &lt;enumeration value="blackWhite"/>
 *     &lt;enumeration value="black"/>
 *     &lt;enumeration value="white"/>
 *     &lt;enumeration value="hidden"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_BlackWhiteMode")
@XmlEnum
public enum STBlackWhiteMode {


    /**
     * Color
     * 
     */
    @XmlEnumValue("clr")
    CLR("clr"),

    /**
     * Automatic
     * 
     */
    @XmlEnumValue("auto")
    AUTO("auto"),

    /**
     * Gray
     * 
     */
    @XmlEnumValue("gray")
    GRAY("gray"),

    /**
     * Light Gray
     * 
     */
    @XmlEnumValue("ltGray")
    LT_GRAY("ltGray"),

    /**
     * Inverse Gray
     * 
     */
    @XmlEnumValue("invGray")
    INV_GRAY("invGray"),

    /**
     * Gray and White
     * 
     */
    @XmlEnumValue("grayWhite")
    GRAY_WHITE("grayWhite"),

    /**
     * Black and Gray
     * 
     */
    @XmlEnumValue("blackGray")
    BLACK_GRAY("blackGray"),

    /**
     * Black and White
     * 
     */
    @XmlEnumValue("blackWhite")
    BLACK_WHITE("blackWhite"),

    /**
     * Black
     * 
     */
    @XmlEnumValue("black")
    BLACK("black"),

    /**
     * White
     * 
     */
    @XmlEnumValue("white")
    WHITE("white"),

    /**
     * Hidden
     * 
     */
    @XmlEnumValue("hidden")
    HIDDEN("hidden");
    private final String value;

    STBlackWhiteMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STBlackWhiteMode fromValue(String v) {
        for (STBlackWhiteMode c: STBlackWhiteMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
