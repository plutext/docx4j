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


package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PresetPatternVal.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PresetPatternVal"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="pct5"/&gt;
 *     &lt;enumeration value="pct10"/&gt;
 *     &lt;enumeration value="pct20"/&gt;
 *     &lt;enumeration value="pct25"/&gt;
 *     &lt;enumeration value="pct30"/&gt;
 *     &lt;enumeration value="pct40"/&gt;
 *     &lt;enumeration value="pct50"/&gt;
 *     &lt;enumeration value="pct60"/&gt;
 *     &lt;enumeration value="pct70"/&gt;
 *     &lt;enumeration value="pct75"/&gt;
 *     &lt;enumeration value="pct80"/&gt;
 *     &lt;enumeration value="pct90"/&gt;
 *     &lt;enumeration value="horz"/&gt;
 *     &lt;enumeration value="vert"/&gt;
 *     &lt;enumeration value="ltHorz"/&gt;
 *     &lt;enumeration value="ltVert"/&gt;
 *     &lt;enumeration value="dkHorz"/&gt;
 *     &lt;enumeration value="dkVert"/&gt;
 *     &lt;enumeration value="narHorz"/&gt;
 *     &lt;enumeration value="narVert"/&gt;
 *     &lt;enumeration value="dashHorz"/&gt;
 *     &lt;enumeration value="dashVert"/&gt;
 *     &lt;enumeration value="cross"/&gt;
 *     &lt;enumeration value="dnDiag"/&gt;
 *     &lt;enumeration value="upDiag"/&gt;
 *     &lt;enumeration value="ltDnDiag"/&gt;
 *     &lt;enumeration value="ltUpDiag"/&gt;
 *     &lt;enumeration value="dkDnDiag"/&gt;
 *     &lt;enumeration value="dkUpDiag"/&gt;
 *     &lt;enumeration value="wdDnDiag"/&gt;
 *     &lt;enumeration value="wdUpDiag"/&gt;
 *     &lt;enumeration value="dashDnDiag"/&gt;
 *     &lt;enumeration value="dashUpDiag"/&gt;
 *     &lt;enumeration value="diagCross"/&gt;
 *     &lt;enumeration value="smCheck"/&gt;
 *     &lt;enumeration value="lgCheck"/&gt;
 *     &lt;enumeration value="smGrid"/&gt;
 *     &lt;enumeration value="lgGrid"/&gt;
 *     &lt;enumeration value="dotGrid"/&gt;
 *     &lt;enumeration value="smConfetti"/&gt;
 *     &lt;enumeration value="lgConfetti"/&gt;
 *     &lt;enumeration value="horzBrick"/&gt;
 *     &lt;enumeration value="diagBrick"/&gt;
 *     &lt;enumeration value="solidDmnd"/&gt;
 *     &lt;enumeration value="openDmnd"/&gt;
 *     &lt;enumeration value="dotDmnd"/&gt;
 *     &lt;enumeration value="plaid"/&gt;
 *     &lt;enumeration value="sphere"/&gt;
 *     &lt;enumeration value="weave"/&gt;
 *     &lt;enumeration value="divot"/&gt;
 *     &lt;enumeration value="shingle"/&gt;
 *     &lt;enumeration value="wave"/&gt;
 *     &lt;enumeration value="trellis"/&gt;
 *     &lt;enumeration value="zigZag"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_PresetPatternVal")
@XmlEnum
public enum STPresetPatternVal {


    /**
     *  5%
     * 
     */
    @XmlEnumValue("pct5")
    PCT_5("pct5"),

    /**
     *  10%
     * 
     */
    @XmlEnumValue("pct10")
    PCT_10("pct10"),

    /**
     *  20%
     * 
     */
    @XmlEnumValue("pct20")
    PCT_20("pct20"),

    /**
     *  25%
     * 
     */
    @XmlEnumValue("pct25")
    PCT_25("pct25"),

    /**
     *  30%
     * 
     */
    @XmlEnumValue("pct30")
    PCT_30("pct30"),

    /**
     *  40%
     * 
     */
    @XmlEnumValue("pct40")
    PCT_40("pct40"),

    /**
     *  50%
     * 
     */
    @XmlEnumValue("pct50")
    PCT_50("pct50"),

    /**
     *  60%
     * 
     */
    @XmlEnumValue("pct60")
    PCT_60("pct60"),

    /**
     *  70%
     * 
     */
    @XmlEnumValue("pct70")
    PCT_70("pct70"),

    /**
     *  75%
     * 
     */
    @XmlEnumValue("pct75")
    PCT_75("pct75"),

    /**
     *  80%
     * 
     */
    @XmlEnumValue("pct80")
    PCT_80("pct80"),

    /**
     *  90%
     * 
     */
    @XmlEnumValue("pct90")
    PCT_90("pct90"),

    /**
     * Horizontal
     * 
     */
    @XmlEnumValue("horz")
    HORZ("horz"),

    /**
     * Vertical
     * 
     */
    @XmlEnumValue("vert")
    VERT("vert"),

    /**
     * Light Horizontal
     * 
     */
    @XmlEnumValue("ltHorz")
    LT_HORZ("ltHorz"),

    /**
     * Light Vertical
     * 
     */
    @XmlEnumValue("ltVert")
    LT_VERT("ltVert"),

    /**
     * Dark Horizontal
     * 
     */
    @XmlEnumValue("dkHorz")
    DK_HORZ("dkHorz"),

    /**
     * Dark Vertical
     * 
     */
    @XmlEnumValue("dkVert")
    DK_VERT("dkVert"),

    /**
     * Narrow Horizontal
     * 
     */
    @XmlEnumValue("narHorz")
    NAR_HORZ("narHorz"),

    /**
     * Narrow Vertical
     * 
     */
    @XmlEnumValue("narVert")
    NAR_VERT("narVert"),

    /**
     * Dashed Horizontal
     * 
     */
    @XmlEnumValue("dashHorz")
    DASH_HORZ("dashHorz"),

    /**
     * Dashed Vertical
     * 
     */
    @XmlEnumValue("dashVert")
    DASH_VERT("dashVert"),

    /**
     * Cross
     * 
     */
    @XmlEnumValue("cross")
    CROSS("cross"),

    /**
     * Downward Diagonal
     * 
     */
    @XmlEnumValue("dnDiag")
    DN_DIAG("dnDiag"),

    /**
     * Upward Diagonal
     * 
     */
    @XmlEnumValue("upDiag")
    UP_DIAG("upDiag"),

    /**
     * Light Downward Diagonal
     * 
     */
    @XmlEnumValue("ltDnDiag")
    LT_DN_DIAG("ltDnDiag"),

    /**
     * Light Upward Diagonal
     * 
     */
    @XmlEnumValue("ltUpDiag")
    LT_UP_DIAG("ltUpDiag"),

    /**
     * Dark Downward Diagonal
     * 
     */
    @XmlEnumValue("dkDnDiag")
    DK_DN_DIAG("dkDnDiag"),

    /**
     * Dark Upward Diagonal
     * 
     */
    @XmlEnumValue("dkUpDiag")
    DK_UP_DIAG("dkUpDiag"),

    /**
     * Wide Downward Diagonal
     * 
     */
    @XmlEnumValue("wdDnDiag")
    WD_DN_DIAG("wdDnDiag"),

    /**
     * Wide Upward Diagonal
     * 
     */
    @XmlEnumValue("wdUpDiag")
    WD_UP_DIAG("wdUpDiag"),

    /**
     * Dashed Downward Diagonal
     * 
     */
    @XmlEnumValue("dashDnDiag")
    DASH_DN_DIAG("dashDnDiag"),

    /**
     * Dashed Upward DIagonal
     * 
     */
    @XmlEnumValue("dashUpDiag")
    DASH_UP_DIAG("dashUpDiag"),

    /**
     * Diagonal Cross
     * 
     */
    @XmlEnumValue("diagCross")
    DIAG_CROSS("diagCross"),

    /**
     * Small Checker Board
     * 
     */
    @XmlEnumValue("smCheck")
    SM_CHECK("smCheck"),

    /**
     * Large Checker Board
     * 
     */
    @XmlEnumValue("lgCheck")
    LG_CHECK("lgCheck"),

    /**
     * Small Grid
     * 
     */
    @XmlEnumValue("smGrid")
    SM_GRID("smGrid"),

    /**
     * Large Grid
     * 
     */
    @XmlEnumValue("lgGrid")
    LG_GRID("lgGrid"),

    /**
     * Dotted Grid
     * 
     */
    @XmlEnumValue("dotGrid")
    DOT_GRID("dotGrid"),

    /**
     * Small Confetti
     * 
     */
    @XmlEnumValue("smConfetti")
    SM_CONFETTI("smConfetti"),

    /**
     * Large Confetti
     * 
     */
    @XmlEnumValue("lgConfetti")
    LG_CONFETTI("lgConfetti"),

    /**
     * Horizontal Brick
     * 
     */
    @XmlEnumValue("horzBrick")
    HORZ_BRICK("horzBrick"),

    /**
     * Diagonal Brick
     * 
     */
    @XmlEnumValue("diagBrick")
    DIAG_BRICK("diagBrick"),

    /**
     * Solid Diamond
     * 
     */
    @XmlEnumValue("solidDmnd")
    SOLID_DMND("solidDmnd"),

    /**
     * Open Diamond
     * 
     */
    @XmlEnumValue("openDmnd")
    OPEN_DMND("openDmnd"),

    /**
     * Dotted Diamond
     * 
     */
    @XmlEnumValue("dotDmnd")
    DOT_DMND("dotDmnd"),

    /**
     * Plaid
     * 
     */
    @XmlEnumValue("plaid")
    PLAID("plaid"),

    /**
     * Sphere
     * 
     */
    @XmlEnumValue("sphere")
    SPHERE("sphere"),

    /**
     * Weave
     * 
     */
    @XmlEnumValue("weave")
    WEAVE("weave"),

    /**
     * Divot
     * 
     */
    @XmlEnumValue("divot")
    DIVOT("divot"),

    /**
     * Shingle
     * 
     */
    @XmlEnumValue("shingle")
    SHINGLE("shingle"),

    /**
     * Wave
     * 
     */
    @XmlEnumValue("wave")
    WAVE("wave"),

    /**
     * Trellis
     * 
     */
    @XmlEnumValue("trellis")
    TRELLIS("trellis"),

    /**
     * Zig Zag
     * 
     */
    @XmlEnumValue("zigZag")
    ZIG_ZAG("zigZag");
    private final String value;

    STPresetPatternVal(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPresetPatternVal fromValue(String v) {
        for (STPresetPatternVal c: STPresetPatternVal.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
