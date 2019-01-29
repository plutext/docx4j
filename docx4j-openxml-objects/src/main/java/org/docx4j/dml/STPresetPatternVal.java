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
 * &lt;simpleType name="ST_PresetPatternVal">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="pct5"/>
 *     &lt;enumeration value="pct10"/>
 *     &lt;enumeration value="pct20"/>
 *     &lt;enumeration value="pct25"/>
 *     &lt;enumeration value="pct30"/>
 *     &lt;enumeration value="pct40"/>
 *     &lt;enumeration value="pct50"/>
 *     &lt;enumeration value="pct60"/>
 *     &lt;enumeration value="pct70"/>
 *     &lt;enumeration value="pct75"/>
 *     &lt;enumeration value="pct80"/>
 *     &lt;enumeration value="pct90"/>
 *     &lt;enumeration value="horz"/>
 *     &lt;enumeration value="vert"/>
 *     &lt;enumeration value="ltHorz"/>
 *     &lt;enumeration value="ltVert"/>
 *     &lt;enumeration value="dkHorz"/>
 *     &lt;enumeration value="dkVert"/>
 *     &lt;enumeration value="narHorz"/>
 *     &lt;enumeration value="narVert"/>
 *     &lt;enumeration value="dashHorz"/>
 *     &lt;enumeration value="dashVert"/>
 *     &lt;enumeration value="cross"/>
 *     &lt;enumeration value="dnDiag"/>
 *     &lt;enumeration value="upDiag"/>
 *     &lt;enumeration value="ltDnDiag"/>
 *     &lt;enumeration value="ltUpDiag"/>
 *     &lt;enumeration value="dkDnDiag"/>
 *     &lt;enumeration value="dkUpDiag"/>
 *     &lt;enumeration value="wdDnDiag"/>
 *     &lt;enumeration value="wdUpDiag"/>
 *     &lt;enumeration value="dashDnDiag"/>
 *     &lt;enumeration value="dashUpDiag"/>
 *     &lt;enumeration value="diagCross"/>
 *     &lt;enumeration value="smCheck"/>
 *     &lt;enumeration value="lgCheck"/>
 *     &lt;enumeration value="smGrid"/>
 *     &lt;enumeration value="lgGrid"/>
 *     &lt;enumeration value="dotGrid"/>
 *     &lt;enumeration value="smConfetti"/>
 *     &lt;enumeration value="lgConfetti"/>
 *     &lt;enumeration value="horzBrick"/>
 *     &lt;enumeration value="diagBrick"/>
 *     &lt;enumeration value="solidDmnd"/>
 *     &lt;enumeration value="openDmnd"/>
 *     &lt;enumeration value="dotDmnd"/>
 *     &lt;enumeration value="plaid"/>
 *     &lt;enumeration value="sphere"/>
 *     &lt;enumeration value="weave"/>
 *     &lt;enumeration value="divot"/>
 *     &lt;enumeration value="shingle"/>
 *     &lt;enumeration value="wave"/>
 *     &lt;enumeration value="trellis"/>
 *     &lt;enumeration value="zigZag"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
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
