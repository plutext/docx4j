
package org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_GroupShapeChangeBit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_GroupShapeChangeBit"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="add"/&gt;
 *     &lt;enumeration value="del"/&gt;
 *     &lt;enumeration value="mod"/&gt;
 *     &lt;enumeration value="ord"/&gt;
 *     &lt;enumeration value="topLvl"/&gt;
 *     &lt;enumeration value="modVis"/&gt;
 *     &lt;enumeration value="replST"/&gt;
 *     &lt;enumeration value="delST"/&gt;
 *     &lt;enumeration value="replId"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_GroupShapeChangeBit")
@XmlEnum
public enum STGroupShapeChangeBit {

    @XmlEnumValue("add")
    ADD("add"),
    @XmlEnumValue("del")
    DEL("del"),
    @XmlEnumValue("mod")
    MOD("mod"),
    @XmlEnumValue("ord")
    ORD("ord"),
    @XmlEnumValue("topLvl")
    TOP_LVL("topLvl"),
    @XmlEnumValue("modVis")
    MOD_VIS("modVis"),
    @XmlEnumValue("replST")
    REPL_ST("replST"),
    @XmlEnumValue("delST")
    DEL_ST("delST"),
    @XmlEnumValue("replId")
    REPL_ID("replId");
    private final String value;

    STGroupShapeChangeBit(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STGroupShapeChangeBit fromValue(String v) {
        for (STGroupShapeChangeBit c: STGroupShapeChangeBit.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
