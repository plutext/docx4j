
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for font_selection_strategy_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="font_selection_strategy_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="character-by-character"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "font_selection_strategy_Type")
@XmlEnum
public enum FontSelectionStrategyType {

    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("character-by-character")
    CHARACTER_BY_CHARACTER("character-by-character"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    FontSelectionStrategyType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FontSelectionStrategyType fromValue(String v) {
        for (FontSelectionStrategyType c: FontSelectionStrategyType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
