
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2013.main.command;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_MainMasterChangeBit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_MainMasterChangeBit"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="addSp"/&gt;
 *     &lt;enumeration value="delSp"/&gt;
 *     &lt;enumeration value="modSp"/&gt;
 *     &lt;enumeration value="spOrd"/&gt;
 *     &lt;enumeration value="new"/&gt;
 *     &lt;enumeration value="add"/&gt;
 *     &lt;enumeration value="del"/&gt;
 *     &lt;enumeration value="mod"/&gt;
 *     &lt;enumeration value="ord"/&gt;
 *     &lt;enumeration value="replId"/&gt;
 *     &lt;enumeration value="modTransition"/&gt;
 *     &lt;enumeration value="modMedia"/&gt;
 *     &lt;enumeration value="setBg"/&gt;
 *     &lt;enumeration value="setFolMasterAnim"/&gt;
 *     &lt;enumeration value="setFolMasterObjs"/&gt;
 *     &lt;enumeration value="modClrScheme"/&gt;
 *     &lt;enumeration value="addAnim"/&gt;
 *     &lt;enumeration value="delAnim"/&gt;
 *     &lt;enumeration value="modAnim"/&gt;
 *     &lt;enumeration value="replTag"/&gt;
 *     &lt;enumeration value="delTag"/&gt;
 *     &lt;enumeration value="setClrOvrMap"/&gt;
 *     &lt;enumeration value="delDesignElem"/&gt;
 *     &lt;enumeration value="modShow"/&gt;
 *     &lt;enumeration value="addSldLayout"/&gt;
 *     &lt;enumeration value="delSldLayout"/&gt;
 *     &lt;enumeration value="modSldLayout"/&gt;
 *     &lt;enumeration value="sldLayoutOrd"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_MainMasterChangeBit")
@XmlEnum
public enum STMainMasterChangeBit {

    @XmlEnumValue("addSp")
    ADD_SP("addSp"),
    @XmlEnumValue("delSp")
    DEL_SP("delSp"),
    @XmlEnumValue("modSp")
    MOD_SP("modSp"),
    @XmlEnumValue("spOrd")
    SP_ORD("spOrd"),
    @XmlEnumValue("new")
    NEW("new"),
    @XmlEnumValue("add")
    ADD("add"),
    @XmlEnumValue("del")
    DEL("del"),
    @XmlEnumValue("mod")
    MOD("mod"),
    @XmlEnumValue("ord")
    ORD("ord"),
    @XmlEnumValue("replId")
    REPL_ID("replId"),
    @XmlEnumValue("modTransition")
    MOD_TRANSITION("modTransition"),
    @XmlEnumValue("modMedia")
    MOD_MEDIA("modMedia"),
    @XmlEnumValue("setBg")
    SET_BG("setBg"),
    @XmlEnumValue("setFolMasterAnim")
    SET_FOL_MASTER_ANIM("setFolMasterAnim"),
    @XmlEnumValue("setFolMasterObjs")
    SET_FOL_MASTER_OBJS("setFolMasterObjs"),
    @XmlEnumValue("modClrScheme")
    MOD_CLR_SCHEME("modClrScheme"),
    @XmlEnumValue("addAnim")
    ADD_ANIM("addAnim"),
    @XmlEnumValue("delAnim")
    DEL_ANIM("delAnim"),
    @XmlEnumValue("modAnim")
    MOD_ANIM("modAnim"),
    @XmlEnumValue("replTag")
    REPL_TAG("replTag"),
    @XmlEnumValue("delTag")
    DEL_TAG("delTag"),
    @XmlEnumValue("setClrOvrMap")
    SET_CLR_OVR_MAP("setClrOvrMap"),
    @XmlEnumValue("delDesignElem")
    DEL_DESIGN_ELEM("delDesignElem"),
    @XmlEnumValue("modShow")
    MOD_SHOW("modShow"),
    @XmlEnumValue("addSldLayout")
    ADD_SLD_LAYOUT("addSldLayout"),
    @XmlEnumValue("delSldLayout")
    DEL_SLD_LAYOUT("delSldLayout"),
    @XmlEnumValue("modSldLayout")
    MOD_SLD_LAYOUT("modSldLayout"),
    @XmlEnumValue("sldLayoutOrd")
    SLD_LAYOUT_ORD("sldLayoutOrd");
    private final String value;

    STMainMasterChangeBit(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STMainMasterChangeBit fromValue(String v) {
        for (STMainMasterChangeBit c: STMainMasterChangeBit.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
