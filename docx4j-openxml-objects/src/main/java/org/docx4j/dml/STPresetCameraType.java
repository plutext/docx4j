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
 * <p>Java class for ST_PresetCameraType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PresetCameraType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="legacyObliqueTopLeft"/&gt;
 *     &lt;enumeration value="legacyObliqueTop"/&gt;
 *     &lt;enumeration value="legacyObliqueTopRight"/&gt;
 *     &lt;enumeration value="legacyObliqueLeft"/&gt;
 *     &lt;enumeration value="legacyObliqueFront"/&gt;
 *     &lt;enumeration value="legacyObliqueRight"/&gt;
 *     &lt;enumeration value="legacyObliqueBottomLeft"/&gt;
 *     &lt;enumeration value="legacyObliqueBottom"/&gt;
 *     &lt;enumeration value="legacyObliqueBottomRight"/&gt;
 *     &lt;enumeration value="legacyPerspectiveTopLeft"/&gt;
 *     &lt;enumeration value="legacyPerspectiveTop"/&gt;
 *     &lt;enumeration value="legacyPerspectiveTopRight"/&gt;
 *     &lt;enumeration value="legacyPerspectiveLeft"/&gt;
 *     &lt;enumeration value="legacyPerspectiveFront"/&gt;
 *     &lt;enumeration value="legacyPerspectiveRight"/&gt;
 *     &lt;enumeration value="legacyPerspectiveBottomLeft"/&gt;
 *     &lt;enumeration value="legacyPerspectiveBottom"/&gt;
 *     &lt;enumeration value="legacyPerspectiveBottomRight"/&gt;
 *     &lt;enumeration value="orthographicFront"/&gt;
 *     &lt;enumeration value="isometricTopUp"/&gt;
 *     &lt;enumeration value="isometricTopDown"/&gt;
 *     &lt;enumeration value="isometricBottomUp"/&gt;
 *     &lt;enumeration value="isometricBottomDown"/&gt;
 *     &lt;enumeration value="isometricLeftUp"/&gt;
 *     &lt;enumeration value="isometricLeftDown"/&gt;
 *     &lt;enumeration value="isometricRightUp"/&gt;
 *     &lt;enumeration value="isometricRightDown"/&gt;
 *     &lt;enumeration value="isometricOffAxis1Left"/&gt;
 *     &lt;enumeration value="isometricOffAxis1Right"/&gt;
 *     &lt;enumeration value="isometricOffAxis1Top"/&gt;
 *     &lt;enumeration value="isometricOffAxis2Left"/&gt;
 *     &lt;enumeration value="isometricOffAxis2Right"/&gt;
 *     &lt;enumeration value="isometricOffAxis2Top"/&gt;
 *     &lt;enumeration value="isometricOffAxis3Left"/&gt;
 *     &lt;enumeration value="isometricOffAxis3Right"/&gt;
 *     &lt;enumeration value="isometricOffAxis3Bottom"/&gt;
 *     &lt;enumeration value="isometricOffAxis4Left"/&gt;
 *     &lt;enumeration value="isometricOffAxis4Right"/&gt;
 *     &lt;enumeration value="isometricOffAxis4Bottom"/&gt;
 *     &lt;enumeration value="obliqueTopLeft"/&gt;
 *     &lt;enumeration value="obliqueTop"/&gt;
 *     &lt;enumeration value="obliqueTopRight"/&gt;
 *     &lt;enumeration value="obliqueLeft"/&gt;
 *     &lt;enumeration value="obliqueRight"/&gt;
 *     &lt;enumeration value="obliqueBottomLeft"/&gt;
 *     &lt;enumeration value="obliqueBottom"/&gt;
 *     &lt;enumeration value="obliqueBottomRight"/&gt;
 *     &lt;enumeration value="perspectiveFront"/&gt;
 *     &lt;enumeration value="perspectiveLeft"/&gt;
 *     &lt;enumeration value="perspectiveRight"/&gt;
 *     &lt;enumeration value="perspectiveAbove"/&gt;
 *     &lt;enumeration value="perspectiveBelow"/&gt;
 *     &lt;enumeration value="perspectiveAboveLeftFacing"/&gt;
 *     &lt;enumeration value="perspectiveAboveRightFacing"/&gt;
 *     &lt;enumeration value="perspectiveContrastingLeftFacing"/&gt;
 *     &lt;enumeration value="perspectiveContrastingRightFacing"/&gt;
 *     &lt;enumeration value="perspectiveHeroicLeftFacing"/&gt;
 *     &lt;enumeration value="perspectiveHeroicRightFacing"/&gt;
 *     &lt;enumeration value="perspectiveHeroicExtremeLeftFacing"/&gt;
 *     &lt;enumeration value="perspectiveHeroicExtremeRightFacing"/&gt;
 *     &lt;enumeration value="perspectiveRelaxed"/&gt;
 *     &lt;enumeration value="perspectiveRelaxedModerately"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_PresetCameraType")
@XmlEnum
public enum STPresetCameraType {


    /**
     * Legacy Oblique Top Left
     * 
     */
    @XmlEnumValue("legacyObliqueTopLeft")
    LEGACY_OBLIQUE_TOP_LEFT("legacyObliqueTopLeft"),

    /**
     * Legacy Oblique Top
     * 
     */
    @XmlEnumValue("legacyObliqueTop")
    LEGACY_OBLIQUE_TOP("legacyObliqueTop"),

    /**
     * Legacy Oblique Top Right
     * 
     */
    @XmlEnumValue("legacyObliqueTopRight")
    LEGACY_OBLIQUE_TOP_RIGHT("legacyObliqueTopRight"),

    /**
     * Legacy Oblique Left
     * 
     */
    @XmlEnumValue("legacyObliqueLeft")
    LEGACY_OBLIQUE_LEFT("legacyObliqueLeft"),

    /**
     * Legacy Oblique Front
     * 
     */
    @XmlEnumValue("legacyObliqueFront")
    LEGACY_OBLIQUE_FRONT("legacyObliqueFront"),

    /**
     * Legacy Oblique Right
     * 
     */
    @XmlEnumValue("legacyObliqueRight")
    LEGACY_OBLIQUE_RIGHT("legacyObliqueRight"),

    /**
     * Legacy Oblique Bottom Left
     * 
     */
    @XmlEnumValue("legacyObliqueBottomLeft")
    LEGACY_OBLIQUE_BOTTOM_LEFT("legacyObliqueBottomLeft"),

    /**
     * Legacy Oblique Bottom
     * 
     */
    @XmlEnumValue("legacyObliqueBottom")
    LEGACY_OBLIQUE_BOTTOM("legacyObliqueBottom"),

    /**
     * Legacy Oblique Bottom Right
     * 
     */
    @XmlEnumValue("legacyObliqueBottomRight")
    LEGACY_OBLIQUE_BOTTOM_RIGHT("legacyObliqueBottomRight"),

    /**
     * Legacy Perspective Top Left
     * 
     */
    @XmlEnumValue("legacyPerspectiveTopLeft")
    LEGACY_PERSPECTIVE_TOP_LEFT("legacyPerspectiveTopLeft"),

    /**
     * Legacy Perspective Top
     * 
     */
    @XmlEnumValue("legacyPerspectiveTop")
    LEGACY_PERSPECTIVE_TOP("legacyPerspectiveTop"),

    /**
     * Legacy Perspective Top Right
     * 
     */
    @XmlEnumValue("legacyPerspectiveTopRight")
    LEGACY_PERSPECTIVE_TOP_RIGHT("legacyPerspectiveTopRight"),

    /**
     * Legacy Perspective Left
     * 
     */
    @XmlEnumValue("legacyPerspectiveLeft")
    LEGACY_PERSPECTIVE_LEFT("legacyPerspectiveLeft"),

    /**
     * Legacy Perspective Front
     * 
     */
    @XmlEnumValue("legacyPerspectiveFront")
    LEGACY_PERSPECTIVE_FRONT("legacyPerspectiveFront"),

    /**
     * Legacy Perspective Right
     * 
     */
    @XmlEnumValue("legacyPerspectiveRight")
    LEGACY_PERSPECTIVE_RIGHT("legacyPerspectiveRight"),

    /**
     * Legacy Perspective Bottom Left
     * 
     */
    @XmlEnumValue("legacyPerspectiveBottomLeft")
    LEGACY_PERSPECTIVE_BOTTOM_LEFT("legacyPerspectiveBottomLeft"),

    /**
     * Legacy Perspective Bottom
     * 
     */
    @XmlEnumValue("legacyPerspectiveBottom")
    LEGACY_PERSPECTIVE_BOTTOM("legacyPerspectiveBottom"),

    /**
     * Legacy Perspective Bottom Right
     * 
     */
    @XmlEnumValue("legacyPerspectiveBottomRight")
    LEGACY_PERSPECTIVE_BOTTOM_RIGHT("legacyPerspectiveBottomRight"),

    /**
     * Orthographic Front
     * 
     */
    @XmlEnumValue("orthographicFront")
    ORTHOGRAPHIC_FRONT("orthographicFront"),

    /**
     * Isometric Top Up
     * 
     */
    @XmlEnumValue("isometricTopUp")
    ISOMETRIC_TOP_UP("isometricTopUp"),

    /**
     * Isometric Top Down
     * 
     */
    @XmlEnumValue("isometricTopDown")
    ISOMETRIC_TOP_DOWN("isometricTopDown"),

    /**
     * Isometric Bottom Up
     * 
     */
    @XmlEnumValue("isometricBottomUp")
    ISOMETRIC_BOTTOM_UP("isometricBottomUp"),

    /**
     * Isometric Bottom Down
     * 
     */
    @XmlEnumValue("isometricBottomDown")
    ISOMETRIC_BOTTOM_DOWN("isometricBottomDown"),

    /**
     * Isometric Left Up
     * 
     */
    @XmlEnumValue("isometricLeftUp")
    ISOMETRIC_LEFT_UP("isometricLeftUp"),

    /**
     * Isometric Left Down
     * 
     */
    @XmlEnumValue("isometricLeftDown")
    ISOMETRIC_LEFT_DOWN("isometricLeftDown"),

    /**
     * Isometric Right Up
     * 
     */
    @XmlEnumValue("isometricRightUp")
    ISOMETRIC_RIGHT_UP("isometricRightUp"),

    /**
     * Isometric Right Down
     * 
     */
    @XmlEnumValue("isometricRightDown")
    ISOMETRIC_RIGHT_DOWN("isometricRightDown"),

    /**
     * Isometric Off Axis 1 Left
     * 
     */
    @XmlEnumValue("isometricOffAxis1Left")
    ISOMETRIC_OFF_AXIS_1_LEFT("isometricOffAxis1Left"),

    /**
     * Isometric Off Axis 1 Right
     * 
     */
    @XmlEnumValue("isometricOffAxis1Right")
    ISOMETRIC_OFF_AXIS_1_RIGHT("isometricOffAxis1Right"),

    /**
     * Isometric Off Axis 1 Top
     * 
     */
    @XmlEnumValue("isometricOffAxis1Top")
    ISOMETRIC_OFF_AXIS_1_TOP("isometricOffAxis1Top"),

    /**
     * Isometric Off Axis 2 Left
     * 
     */
    @XmlEnumValue("isometricOffAxis2Left")
    ISOMETRIC_OFF_AXIS_2_LEFT("isometricOffAxis2Left"),

    /**
     * Isometric Off Axis 2 Right
     * 
     */
    @XmlEnumValue("isometricOffAxis2Right")
    ISOMETRIC_OFF_AXIS_2_RIGHT("isometricOffAxis2Right"),

    /**
     * Isometric Off Axis 2 Top
     * 
     */
    @XmlEnumValue("isometricOffAxis2Top")
    ISOMETRIC_OFF_AXIS_2_TOP("isometricOffAxis2Top"),

    /**
     * Isometric Off Axis 3 Left
     * 
     */
    @XmlEnumValue("isometricOffAxis3Left")
    ISOMETRIC_OFF_AXIS_3_LEFT("isometricOffAxis3Left"),

    /**
     * Isometric Off Axis 3 Right
     * 
     */
    @XmlEnumValue("isometricOffAxis3Right")
    ISOMETRIC_OFF_AXIS_3_RIGHT("isometricOffAxis3Right"),

    /**
     * Isometric Off Axis 3 Bottom
     * 
     */
    @XmlEnumValue("isometricOffAxis3Bottom")
    ISOMETRIC_OFF_AXIS_3_BOTTOM("isometricOffAxis3Bottom"),

    /**
     * Isometric Off Axis 4 Left
     * 
     */
    @XmlEnumValue("isometricOffAxis4Left")
    ISOMETRIC_OFF_AXIS_4_LEFT("isometricOffAxis4Left"),

    /**
     * Isometric Off Axis 4 Right
     * 
     */
    @XmlEnumValue("isometricOffAxis4Right")
    ISOMETRIC_OFF_AXIS_4_RIGHT("isometricOffAxis4Right"),

    /**
     * Isometric Off Axis 4 Bottom
     * 
     */
    @XmlEnumValue("isometricOffAxis4Bottom")
    ISOMETRIC_OFF_AXIS_4_BOTTOM("isometricOffAxis4Bottom"),

    /**
     * Oblique Top Left
     * 
     */
    @XmlEnumValue("obliqueTopLeft")
    OBLIQUE_TOP_LEFT("obliqueTopLeft"),

    /**
     * Oblique Top
     * 
     */
    @XmlEnumValue("obliqueTop")
    OBLIQUE_TOP("obliqueTop"),

    /**
     * Oblique Top Right
     * 
     */
    @XmlEnumValue("obliqueTopRight")
    OBLIQUE_TOP_RIGHT("obliqueTopRight"),

    /**
     * Oblique Left
     * 
     */
    @XmlEnumValue("obliqueLeft")
    OBLIQUE_LEFT("obliqueLeft"),

    /**
     * Oblique Right
     * 
     */
    @XmlEnumValue("obliqueRight")
    OBLIQUE_RIGHT("obliqueRight"),

    /**
     * Oblique Bottom Left
     * 
     */
    @XmlEnumValue("obliqueBottomLeft")
    OBLIQUE_BOTTOM_LEFT("obliqueBottomLeft"),

    /**
     * Oblique Bottom
     * 
     */
    @XmlEnumValue("obliqueBottom")
    OBLIQUE_BOTTOM("obliqueBottom"),

    /**
     * Oblique Bottom Right
     * 
     */
    @XmlEnumValue("obliqueBottomRight")
    OBLIQUE_BOTTOM_RIGHT("obliqueBottomRight"),

    /**
     * Perspective Front
     * 
     */
    @XmlEnumValue("perspectiveFront")
    PERSPECTIVE_FRONT("perspectiveFront"),

    /**
     * Perspective Left
     * 
     */
    @XmlEnumValue("perspectiveLeft")
    PERSPECTIVE_LEFT("perspectiveLeft"),

    /**
     * Perspective Right
     * 
     */
    @XmlEnumValue("perspectiveRight")
    PERSPECTIVE_RIGHT("perspectiveRight"),

    /**
     * Orthographic Above
     * 
     */
    @XmlEnumValue("perspectiveAbove")
    PERSPECTIVE_ABOVE("perspectiveAbove"),

    /**
     * Perspective Below
     * 
     */
    @XmlEnumValue("perspectiveBelow")
    PERSPECTIVE_BELOW("perspectiveBelow"),

    /**
     * Perspective Above Left Facing
     * 
     */
    @XmlEnumValue("perspectiveAboveLeftFacing")
    PERSPECTIVE_ABOVE_LEFT_FACING("perspectiveAboveLeftFacing"),

    /**
     * Perspective Above Right Facing
     * 
     */
    @XmlEnumValue("perspectiveAboveRightFacing")
    PERSPECTIVE_ABOVE_RIGHT_FACING("perspectiveAboveRightFacing"),

    /**
     * Perspective Contrasting Left Facing
     * 
     */
    @XmlEnumValue("perspectiveContrastingLeftFacing")
    PERSPECTIVE_CONTRASTING_LEFT_FACING("perspectiveContrastingLeftFacing"),

    /**
     * Perspective Contrasting Right Facing
     * 
     */
    @XmlEnumValue("perspectiveContrastingRightFacing")
    PERSPECTIVE_CONTRASTING_RIGHT_FACING("perspectiveContrastingRightFacing"),

    /**
     * Perspective Heroic Left Facing
     * 
     */
    @XmlEnumValue("perspectiveHeroicLeftFacing")
    PERSPECTIVE_HEROIC_LEFT_FACING("perspectiveHeroicLeftFacing"),

    /**
     * Perspective Heroic Right Facing
     * 
     */
    @XmlEnumValue("perspectiveHeroicRightFacing")
    PERSPECTIVE_HEROIC_RIGHT_FACING("perspectiveHeroicRightFacing"),

    /**
     * Perspective Heroic Extreme Left Facing
     * 
     */
    @XmlEnumValue("perspectiveHeroicExtremeLeftFacing")
    PERSPECTIVE_HEROIC_EXTREME_LEFT_FACING("perspectiveHeroicExtremeLeftFacing"),

    /**
     * Perspective Heroic Extreme Right Facing
     * 
     */
    @XmlEnumValue("perspectiveHeroicExtremeRightFacing")
    PERSPECTIVE_HEROIC_EXTREME_RIGHT_FACING("perspectiveHeroicExtremeRightFacing"),

    /**
     * Perspective Relaxed
     * 
     */
    @XmlEnumValue("perspectiveRelaxed")
    PERSPECTIVE_RELAXED("perspectiveRelaxed"),

    /**
     * Perspective Relaxed Moderately
     * 
     */
    @XmlEnumValue("perspectiveRelaxedModerately")
    PERSPECTIVE_RELAXED_MODERATELY("perspectiveRelaxedModerately");
    private final String value;

    STPresetCameraType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPresetCameraType fromValue(String v) {
        for (STPresetCameraType c: STPresetCameraType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
