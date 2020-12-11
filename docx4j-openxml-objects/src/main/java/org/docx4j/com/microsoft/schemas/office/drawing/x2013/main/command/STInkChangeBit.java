
package org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_InkChangeBit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_InkChangeBit"&gt;
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
 *     &lt;enumeration value="reco"/&gt;
 *     &lt;enumeration value="modStrokes"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_InkChangeBit")
@XmlEnum
public enum STInkChangeBit {

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
    REPL_ID("replId"),
    @XmlEnumValue("reco")
    RECO("reco"),
    @XmlEnumValue("modStrokes")
    MOD_STROKES("modStrokes");
    private final String value;

    STInkChangeBit(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STInkChangeBit fromValue(String v) {
        for (STInkChangeBit c: STInkChangeBit.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
