
package org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ConnectorChangeBit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ConnectorChangeBit"&gt;
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
@XmlType(name = "ST_ConnectorChangeBit")
@XmlEnum
public enum STConnectorChangeBit {

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

    STConnectorChangeBit(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STConnectorChangeBit fromValue(String v) {
        for (STConnectorChangeBit c: STConnectorChangeBit.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
