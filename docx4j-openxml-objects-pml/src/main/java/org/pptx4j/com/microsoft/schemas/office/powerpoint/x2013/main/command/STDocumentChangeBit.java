
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2013.main.command;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_DocumentChangeBit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DocumentChangeBit"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="undo"/&gt;
 *     &lt;enumeration value="redo"/&gt;
 *     &lt;enumeration value="ext"/&gt;
 *     &lt;enumeration value="custSel"/&gt;
 *     &lt;enumeration value="mod"/&gt;
 *     &lt;enumeration value="addSld"/&gt;
 *     &lt;enumeration value="delSld"/&gt;
 *     &lt;enumeration value="modSld"/&gt;
 *     &lt;enumeration value="sldOrd"/&gt;
 *     &lt;enumeration value="addMainMaster"/&gt;
 *     &lt;enumeration value="delMainMaster"/&gt;
 *     &lt;enumeration value="modMainMaster"/&gt;
 *     &lt;enumeration value="mainMasterOrd"/&gt;
 *     &lt;enumeration value="addSection"/&gt;
 *     &lt;enumeration value="delSection"/&gt;
 *     &lt;enumeration value="modSection"/&gt;
 *     &lt;enumeration value="addCmAuthor"/&gt;
 *     &lt;enumeration value="delCmAuthor"/&gt;
 *     &lt;enumeration value="modCmAuthor"/&gt;
 *     &lt;enumeration value="replTag"/&gt;
 *     &lt;enumeration value="delTag"/&gt;
 *     &lt;enumeration value="addCustShow"/&gt;
 *     &lt;enumeration value="delCustShow"/&gt;
 *     &lt;enumeration value="modCustShow"/&gt;
 *     &lt;enumeration value="modNotesMaster"/&gt;
 *     &lt;enumeration value="modHandout"/&gt;
 *     &lt;enumeration value="modShowInfo"/&gt;
 *     &lt;enumeration value="addOsfTaskPaneApp"/&gt;
 *     &lt;enumeration value="delOsfTaskPaneApp"/&gt;
 *     &lt;enumeration value="setSldSz"/&gt;
 *     &lt;enumeration value="modRtl"/&gt;
 *     &lt;enumeration value="modChgInfo"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_DocumentChangeBit")
@XmlEnum
public enum STDocumentChangeBit {

    @XmlEnumValue("undo")
    UNDO("undo"),
    @XmlEnumValue("redo")
    REDO("redo"),
    @XmlEnumValue("ext")
    EXT("ext"),
    @XmlEnumValue("custSel")
    CUST_SEL("custSel"),
    @XmlEnumValue("mod")
    MOD("mod"),
    @XmlEnumValue("addSld")
    ADD_SLD("addSld"),
    @XmlEnumValue("delSld")
    DEL_SLD("delSld"),
    @XmlEnumValue("modSld")
    MOD_SLD("modSld"),
    @XmlEnumValue("sldOrd")
    SLD_ORD("sldOrd"),
    @XmlEnumValue("addMainMaster")
    ADD_MAIN_MASTER("addMainMaster"),
    @XmlEnumValue("delMainMaster")
    DEL_MAIN_MASTER("delMainMaster"),
    @XmlEnumValue("modMainMaster")
    MOD_MAIN_MASTER("modMainMaster"),
    @XmlEnumValue("mainMasterOrd")
    MAIN_MASTER_ORD("mainMasterOrd"),
    @XmlEnumValue("addSection")
    ADD_SECTION("addSection"),
    @XmlEnumValue("delSection")
    DEL_SECTION("delSection"),
    @XmlEnumValue("modSection")
    MOD_SECTION("modSection"),
    @XmlEnumValue("addCmAuthor")
    ADD_CM_AUTHOR("addCmAuthor"),
    @XmlEnumValue("delCmAuthor")
    DEL_CM_AUTHOR("delCmAuthor"),
    @XmlEnumValue("modCmAuthor")
    MOD_CM_AUTHOR("modCmAuthor"),
    @XmlEnumValue("replTag")
    REPL_TAG("replTag"),
    @XmlEnumValue("delTag")
    DEL_TAG("delTag"),
    @XmlEnumValue("addCustShow")
    ADD_CUST_SHOW("addCustShow"),
    @XmlEnumValue("delCustShow")
    DEL_CUST_SHOW("delCustShow"),
    @XmlEnumValue("modCustShow")
    MOD_CUST_SHOW("modCustShow"),
    @XmlEnumValue("modNotesMaster")
    MOD_NOTES_MASTER("modNotesMaster"),
    @XmlEnumValue("modHandout")
    MOD_HANDOUT("modHandout"),
    @XmlEnumValue("modShowInfo")
    MOD_SHOW_INFO("modShowInfo"),
    @XmlEnumValue("addOsfTaskPaneApp")
    ADD_OSF_TASK_PANE_APP("addOsfTaskPaneApp"),
    @XmlEnumValue("delOsfTaskPaneApp")
    DEL_OSF_TASK_PANE_APP("delOsfTaskPaneApp"),
    @XmlEnumValue("setSldSz")
    SET_SLD_SZ("setSldSz"),
    @XmlEnumValue("modRtl")
    MOD_RTL("modRtl"),
    @XmlEnumValue("modChgInfo")
    MOD_CHG_INFO("modChgInfo");
    private final String value;

    STDocumentChangeBit(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDocumentChangeBit fromValue(String v) {
        for (STDocumentChangeBit c: STDocumentChangeBit.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
