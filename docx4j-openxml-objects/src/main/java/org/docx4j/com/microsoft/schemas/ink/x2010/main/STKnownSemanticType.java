
package org.docx4j.com.microsoft.schemas.ink.x2010.main;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_KnownSemanticType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_KnownSemanticType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="none"/&gt;
 *     &lt;enumeration value="underline"/&gt;
 *     &lt;enumeration value="strikethrough"/&gt;
 *     &lt;enumeration value="highlight"/&gt;
 *     &lt;enumeration value="scratchOut"/&gt;
 *     &lt;enumeration value="verticalRange"/&gt;
 *     &lt;enumeration value="callout"/&gt;
 *     &lt;enumeration value="enclosure"/&gt;
 *     &lt;enumeration value="comment"/&gt;
 *     &lt;enumeration value="container"/&gt;
 *     &lt;enumeration value="connector"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_KnownSemanticType")
@XmlEnum
public enum STKnownSemanticType {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("underline")
    UNDERLINE("underline"),
    @XmlEnumValue("strikethrough")
    STRIKETHROUGH("strikethrough"),
    @XmlEnumValue("highlight")
    HIGHLIGHT("highlight"),
    @XmlEnumValue("scratchOut")
    SCRATCH_OUT("scratchOut"),
    @XmlEnumValue("verticalRange")
    VERTICAL_RANGE("verticalRange"),
    @XmlEnumValue("callout")
    CALLOUT("callout"),
    @XmlEnumValue("enclosure")
    ENCLOSURE("enclosure"),
    @XmlEnumValue("comment")
    COMMENT("comment"),
    @XmlEnumValue("container")
    CONTAINER("container"),
    @XmlEnumValue("connector")
    CONNECTOR("connector");
    private final String value;

    STKnownSemanticType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STKnownSemanticType fromValue(String v) {
        for (STKnownSemanticType c: STKnownSemanticType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
