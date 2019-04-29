
package org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_StyleReferenceModifierEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_StyleReferenceModifierEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="ignoreCSTransforms"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_StyleReferenceModifierEnum")
@XmlEnum
public enum STStyleReferenceModifierEnum {

    @XmlEnumValue("ignoreCSTransforms")
    IGNORE_CS_TRANSFORMS("ignoreCSTransforms");
    private final String value;

    STStyleReferenceModifierEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STStyleReferenceModifierEnum fromValue(String v) {
        for (STStyleReferenceModifierEnum c: STStyleReferenceModifierEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
