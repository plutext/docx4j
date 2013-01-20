
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_SheetViewType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_SheetViewType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="normal"/>
 *     &lt;enumeration value="pageBreakPreview"/>
 *     &lt;enumeration value="pageLayout"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_SheetViewType")
@XmlEnum
public enum STSheetViewType {

    @XmlEnumValue("normal")
    NORMAL("normal"),
    @XmlEnumValue("pageBreakPreview")
    PAGE_BREAK_PREVIEW("pageBreakPreview"),
    @XmlEnumValue("pageLayout")
    PAGE_LAYOUT("pageLayout");
    private final String value;

    STSheetViewType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STSheetViewType fromValue(String v) {
        for (STSheetViewType c: STSheetViewType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
