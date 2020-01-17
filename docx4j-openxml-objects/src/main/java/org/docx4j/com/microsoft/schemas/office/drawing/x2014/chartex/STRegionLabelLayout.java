
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_RegionLabelLayout.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_RegionLabelLayout"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="none"/&gt;
 *     &lt;enumeration value="bestFitOnly"/&gt;
 *     &lt;enumeration value="showAll"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_RegionLabelLayout")
@XmlEnum
public enum STRegionLabelLayout {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("bestFitOnly")
    BEST_FIT_ONLY("bestFitOnly"),
    @XmlEnumValue("showAll")
    SHOW_ALL("showAll");
    private final String value;

    STRegionLabelLayout(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STRegionLabelLayout fromValue(String v) {
        for (STRegionLabelLayout c: STRegionLabelLayout.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
