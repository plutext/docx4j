
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for background_attachment_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="background_attachment_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="scroll"/>
 *     &lt;enumeration value="fixed"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "background_attachment_Type")
@XmlEnum
public enum BackgroundAttachmentType {

    @XmlEnumValue("scroll")
    SCROLL("scroll"),
    @XmlEnumValue("fixed")
    FIXED("fixed"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    BackgroundAttachmentType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BackgroundAttachmentType fromValue(String v) {
        for (BackgroundAttachmentType c: BackgroundAttachmentType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
