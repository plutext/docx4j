
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TextFontAlignType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextFontAlignType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="t"/>
 *     &lt;enumeration value="ctr"/>
 *     &lt;enumeration value="base"/>
 *     &lt;enumeration value="b"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TextFontAlignType")
@XmlEnum
public enum STTextFontAlignType {


    /**
     * Font Alignment Enum ( Automatic
     * 						)
     * 
     */
    @XmlEnumValue("auto")
    AUTO("auto"),

    /**
     * Font Alignment Enum ( Top
     * 						)
     * 
     */
    @XmlEnumValue("t")
    T("t"),

    /**
     * Font Alignment Enum ( Center
     * 						)
     * 
     */
    @XmlEnumValue("ctr")
    CTR("ctr"),

    /**
     * Font Alignment Enum ( Baseline
     * 						)
     * 
     */
    @XmlEnumValue("base")
    BASE("base"),

    /**
     * Font Alignment Enum ( Bottom
     * 						)
     * 
     */
    @XmlEnumValue("b")
    B("b");
    private final String value;

    STTextFontAlignType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextFontAlignType fromValue(String v) {
        for (STTextFontAlignType c: STTextFontAlignType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
