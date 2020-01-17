
package org.docx4j.org.w3.x2003.inkML;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for brushRasterOp.type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="brushRasterOp.type"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="noOperation"/&gt;
 *     &lt;enumeration value="copyPen"/&gt;
 *     &lt;enumeration value="maskPen"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "brushRasterOp.type")
@XmlEnum
public enum BrushRasterOpType {

    @XmlEnumValue("noOperation")
    NO_OPERATION("noOperation"),
    @XmlEnumValue("copyPen")
    COPY_PEN("copyPen"),
    @XmlEnumValue("maskPen")
    MASK_PEN("maskPen");
    private final String value;

    BrushRasterOpType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BrushRasterOpType fromValue(String v) {
        for (BrushRasterOpType c: BrushRasterOpType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
