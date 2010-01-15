/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */


package org.docx4j.dml.diagram;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ParameterId.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ParameterId">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="horzAlign"/>
 *     &lt;enumeration value="vertAlign"/>
 *     &lt;enumeration value="chDir"/>
 *     &lt;enumeration value="chAlign"/>
 *     &lt;enumeration value="secChAlign"/>
 *     &lt;enumeration value="linDir"/>
 *     &lt;enumeration value="secLinDir"/>
 *     &lt;enumeration value="stElem"/>
 *     &lt;enumeration value="bendPt"/>
 *     &lt;enumeration value="connRout"/>
 *     &lt;enumeration value="begSty"/>
 *     &lt;enumeration value="endSty"/>
 *     &lt;enumeration value="dim"/>
 *     &lt;enumeration value="rotPath"/>
 *     &lt;enumeration value="ctrShpMap"/>
 *     &lt;enumeration value="nodeHorzAlign"/>
 *     &lt;enumeration value="nodeVertAlign"/>
 *     &lt;enumeration value="fallback"/>
 *     &lt;enumeration value="txDir"/>
 *     &lt;enumeration value="pyraAcctPos"/>
 *     &lt;enumeration value="pyraAcctTxMar"/>
 *     &lt;enumeration value="txBlDir"/>
 *     &lt;enumeration value="txAnchorHorz"/>
 *     &lt;enumeration value="txAnchorVert"/>
 *     &lt;enumeration value="txAnchorHorzCh"/>
 *     &lt;enumeration value="txAnchorVertCh"/>
 *     &lt;enumeration value="parTxLTRAlign"/>
 *     &lt;enumeration value="parTxRTLAlign"/>
 *     &lt;enumeration value="shpTxLTRAlignCh"/>
 *     &lt;enumeration value="shpTxRTLAlignCh"/>
 *     &lt;enumeration value="autoTxRot"/>
 *     &lt;enumeration value="grDir"/>
 *     &lt;enumeration value="flowDir"/>
 *     &lt;enumeration value="contDir"/>
 *     &lt;enumeration value="bkpt"/>
 *     &lt;enumeration value="off"/>
 *     &lt;enumeration value="hierAlign"/>
 *     &lt;enumeration value="bkPtFixedVal"/>
 *     &lt;enumeration value="stBulletLvl"/>
 *     &lt;enumeration value="stAng"/>
 *     &lt;enumeration value="spanAng"/>
 *     &lt;enumeration value="ar"/>
 *     &lt;enumeration value="lnSpPar"/>
 *     &lt;enumeration value="lnSpAfParP"/>
 *     &lt;enumeration value="lnSpCh"/>
 *     &lt;enumeration value="lnSpAfChP"/>
 *     &lt;enumeration value="rtShortDist"/>
 *     &lt;enumeration value="alignTx"/>
 *     &lt;enumeration value="pyraLvlNode"/>
 *     &lt;enumeration value="pyraAcctBkgdNode"/>
 *     &lt;enumeration value="pyraAcctTxNode"/>
 *     &lt;enumeration value="srcNode"/>
 *     &lt;enumeration value="dstNode"/>
 *     &lt;enumeration value="begPts"/>
 *     &lt;enumeration value="endPts"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ParameterId")
@XmlEnum
public enum STParameterId {


    /**
     * Horizontal Alignment
     * 
     */
    @XmlEnumValue("horzAlign")
    HORZ_ALIGN("horzAlign"),

    /**
     * Vertical Alignment
     * 
     */
    @XmlEnumValue("vertAlign")
    VERT_ALIGN("vertAlign"),

    /**
     * Child Direction
     * 
     */
    @XmlEnumValue("chDir")
    CH_DIR("chDir"),

    /**
     * Child Alignment
     * 
     */
    @XmlEnumValue("chAlign")
    CH_ALIGN("chAlign"),

    /**
     * Secondary Child Alignment
     * 
     */
    @XmlEnumValue("secChAlign")
    SEC_CH_ALIGN("secChAlign"),

    /**
     * Linear Direction
     * 
     */
    @XmlEnumValue("linDir")
    LIN_DIR("linDir"),

    /**
     * Secondary Linear Direction
     * 
     */
    @XmlEnumValue("secLinDir")
    SEC_LIN_DIR("secLinDir"),

    /**
     * Start Element
     * 
     */
    @XmlEnumValue("stElem")
    ST_ELEM("stElem"),

    /**
     * Bend Point
     * 
     */
    @XmlEnumValue("bendPt")
    BEND_PT("bendPt"),

    /**
     * Connection Route
     * 
     */
    @XmlEnumValue("connRout")
    CONN_ROUT("connRout"),

    /**
     * Beginning Arrowhead Style
     * 
     */
    @XmlEnumValue("begSty")
    BEG_STY("begSty"),

    /**
     * End Style
     * 
     */
    @XmlEnumValue("endSty")
    END_STY("endSty"),

    /**
     * Connector Dimension
     * 
     */
    @XmlEnumValue("dim")
    DIM("dim"),

    /**
     * Rotation Path
     * 
     */
    @XmlEnumValue("rotPath")
    ROT_PATH("rotPath"),

    /**
     * Center Shape Mapping
     * 
     */
    @XmlEnumValue("ctrShpMap")
    CTR_SHP_MAP("ctrShpMap"),

    /**
     * Node Horizontal Alignment
     * 
     */
    @XmlEnumValue("nodeHorzAlign")
    NODE_HORZ_ALIGN("nodeHorzAlign"),

    /**
     * Node Vertical Alignment
     * 
     */
    @XmlEnumValue("nodeVertAlign")
    NODE_VERT_ALIGN("nodeVertAlign"),

    /**
     * Fallback Scale
     * 
     */
    @XmlEnumValue("fallback")
    FALLBACK("fallback"),

    /**
     * Text Direction
     * 
     */
    @XmlEnumValue("txDir")
    TX_DIR("txDir"),

    /**
     * Pyramid Accent Position
     * 
     */
    @XmlEnumValue("pyraAcctPos")
    PYRA_ACCT_POS("pyraAcctPos"),

    /**
     * Pyramid Accent Text Margin
     * 
     */
    @XmlEnumValue("pyraAcctTxMar")
    PYRA_ACCT_TX_MAR("pyraAcctTxMar"),

    /**
     * Text Block Direction
     * 
     */
    @XmlEnumValue("txBlDir")
    TX_BL_DIR("txBlDir"),

    /**
     * Text Anchor Horizontal
     * 
     */
    @XmlEnumValue("txAnchorHorz")
    TX_ANCHOR_HORZ("txAnchorHorz"),

    /**
     * Text Anchor Vertical
     * 
     */
    @XmlEnumValue("txAnchorVert")
    TX_ANCHOR_VERT("txAnchorVert"),

    /**
     * Text Anchor Horizontal With Children
     * 
     */
    @XmlEnumValue("txAnchorHorzCh")
    TX_ANCHOR_HORZ_CH("txAnchorHorzCh"),

    /**
     * Text Anchor Vertical With Children
     * 
     */
    @XmlEnumValue("txAnchorVertCh")
    TX_ANCHOR_VERT_CH("txAnchorVertCh"),

    /**
     * Parent Text Left-to-Right Alignment
     * 
     */
    @XmlEnumValue("parTxLTRAlign")
    PAR_TX_LTR_ALIGN("parTxLTRAlign"),

    /**
     * Parent Text Right-to-Left Alignment
     * 
     */
    @XmlEnumValue("parTxRTLAlign")
    PAR_TX_RTL_ALIGN("parTxRTLAlign"),

    /**
     * Shape Text Left-to-Right Alignment
     * 
     */
    @XmlEnumValue("shpTxLTRAlignCh")
    SHP_TX_LTR_ALIGN_CH("shpTxLTRAlignCh"),

    /**
     * Shape Text Right-to-Left Alignment
     * 
     */
    @XmlEnumValue("shpTxRTLAlignCh")
    SHP_TX_RTL_ALIGN_CH("shpTxRTLAlignCh"),

    /**
     * Auto Text Rotation
     * 
     */
    @XmlEnumValue("autoTxRot")
    AUTO_TX_ROT("autoTxRot"),

    /**
     * Grow Direction
     * 
     */
    @XmlEnumValue("grDir")
    GR_DIR("grDir"),

    /**
     * Flow Direction
     * 
     */
    @XmlEnumValue("flowDir")
    FLOW_DIR("flowDir"),

    /**
     * Continue Direction
     * 
     */
    @XmlEnumValue("contDir")
    CONT_DIR("contDir"),

    /**
     * Breakpoint
     * 
     */
    @XmlEnumValue("bkpt")
    BKPT("bkpt"),

    /**
     * Offset
     * 
     */
    @XmlEnumValue("off")
    OFF("off"),

    /**
     * Hierarchy Alignment
     * 
     */
    @XmlEnumValue("hierAlign")
    HIER_ALIGN("hierAlign"),

    /**
     * Breakpoint Fixed Value
     * 
     */
    @XmlEnumValue("bkPtFixedVal")
    BK_PT_FIXED_VAL("bkPtFixedVal"),

    /**
     * Start Bullets At Level
     * 
     */
    @XmlEnumValue("stBulletLvl")
    ST_BULLET_LVL("stBulletLvl"),

    /**
     * Start Angle
     * 
     */
    @XmlEnumValue("stAng")
    ST_ANG("stAng"),

    /**
     * Span Angle
     * 
     */
    @XmlEnumValue("spanAng")
    SPAN_ANG("spanAng"),

    /**
     * Aspect Ratio
     * 
     */
    @XmlEnumValue("ar")
    AR("ar"),

    /**
     * Line Spacing Parent
     * 
     */
    @XmlEnumValue("lnSpPar")
    LN_SP_PAR("lnSpPar"),

    /**
     * Line Spacing After Parent Paragraph
     * 
     */
    @XmlEnumValue("lnSpAfParP")
    LN_SP_AF_PAR_P("lnSpAfParP"),

    /**
     * Line Spacing Children
     * 
     */
    @XmlEnumValue("lnSpCh")
    LN_SP_CH("lnSpCh"),

    /**
     * Line Spacing After Children Paragraph
     * 
     */
    @XmlEnumValue("lnSpAfChP")
    LN_SP_AF_CH_P("lnSpAfChP"),

    /**
     * Route Shortest Distance
     * 
     */
    @XmlEnumValue("rtShortDist")
    RT_SHORT_DIST("rtShortDist"),

    /**
     * Text Alignment
     * 
     */
    @XmlEnumValue("alignTx")
    ALIGN_TX("alignTx"),

    /**
     * Pyramid Level Node
     * 
     */
    @XmlEnumValue("pyraLvlNode")
    PYRA_LVL_NODE("pyraLvlNode"),

    /**
     * Pyramid Accent Background Node
     * 
     */
    @XmlEnumValue("pyraAcctBkgdNode")
    PYRA_ACCT_BKGD_NODE("pyraAcctBkgdNode"),

    /**
     * Pyramid Accent Text Node
     * 
     */
    @XmlEnumValue("pyraAcctTxNode")
    PYRA_ACCT_TX_NODE("pyraAcctTxNode"),

    /**
     * Source Node
     * 
     */
    @XmlEnumValue("srcNode")
    SRC_NODE("srcNode"),

    /**
     * Destination Node
     * 
     */
    @XmlEnumValue("dstNode")
    DST_NODE("dstNode"),

    /**
     * Beginning Points
     * 
     */
    @XmlEnumValue("begPts")
    BEG_PTS("begPts"),

    /**
     * End Points
     * 
     */
    @XmlEnumValue("endPts")
    END_PTS("endPts");
    private final String value;

    STParameterId(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STParameterId fromValue(String v) {
        for (STParameterId c: STParameterId.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
