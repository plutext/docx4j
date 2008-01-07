
package org.docx4j.jaxb.document;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_SectionMark.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_SectionMark">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="nextPage"/>
 *     &lt;enumeration value="nextColumn"/>
 *     &lt;enumeration value="continuous"/>
 *     &lt;enumeration value="evenPage"/>
 *     &lt;enumeration value="oddPage"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_SectionMark")
@XmlEnum
public enum STSectionMark {


    /**
     * Next Page Section Break
     * 
     */
    @XmlEnumValue("nextPage")
    NEXT_PAGE("nextPage"),

    /**
     * Column Section Break
     * 
     */
    @XmlEnumValue("nextColumn")
    NEXT_COLUMN("nextColumn"),

    /**
     * Continuous Section
     * 						Break
     * 
     */
    @XmlEnumValue("continuous")
    CONTINUOUS("continuous"),

    /**
     * Even Page Section Break
     * 
     */
    @XmlEnumValue("evenPage")
    EVEN_PAGE("evenPage"),

    /**
     * Odd Page Section Break
     * 
     */
    @XmlEnumValue("oddPage")
    ODD_PAGE("oddPage");
    private final String value;

    STSectionMark(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STSectionMark fromValue(String v) {
        for (STSectionMark c: STSectionMark.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
