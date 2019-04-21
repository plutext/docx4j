/*
 *  Copyright 2013, Plutext Pty Ltd.
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


package org.docx4j.w14;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PresetCameraType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PresetCameraType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="legacyObliqueTopLeft"/>
 *     &lt;enumeration value="legacyObliqueTop"/>
 *     &lt;enumeration value="legacyObliqueTopRight"/>
 *     &lt;enumeration value="legacyObliqueLeft"/>
 *     &lt;enumeration value="legacyObliqueFront"/>
 *     &lt;enumeration value="legacyObliqueRight"/>
 *     &lt;enumeration value="legacyObliqueBottomLeft"/>
 *     &lt;enumeration value="legacyObliqueBottom"/>
 *     &lt;enumeration value="legacyObliqueBottomRight"/>
 *     &lt;enumeration value="legacyPerspectiveTopLeft"/>
 *     &lt;enumeration value="legacyPerspectiveTop"/>
 *     &lt;enumeration value="legacyPerspectiveTopRight"/>
 *     &lt;enumeration value="legacyPerspectiveLeft"/>
 *     &lt;enumeration value="legacyPerspectiveFront"/>
 *     &lt;enumeration value="legacyPerspectiveRight"/>
 *     &lt;enumeration value="legacyPerspectiveBottomLeft"/>
 *     &lt;enumeration value="legacyPerspectiveBottom"/>
 *     &lt;enumeration value="legacyPerspectiveBottomRight"/>
 *     &lt;enumeration value="orthographicFront"/>
 *     &lt;enumeration value="isometricTopUp"/>
 *     &lt;enumeration value="isometricTopDown"/>
 *     &lt;enumeration value="isometricBottomUp"/>
 *     &lt;enumeration value="isometricBottomDown"/>
 *     &lt;enumeration value="isometricLeftUp"/>
 *     &lt;enumeration value="isometricLeftDown"/>
 *     &lt;enumeration value="isometricRightUp"/>
 *     &lt;enumeration value="isometricRightDown"/>
 *     &lt;enumeration value="isometricOffAxis1Left"/>
 *     &lt;enumeration value="isometricOffAxis1Right"/>
 *     &lt;enumeration value="isometricOffAxis1Top"/>
 *     &lt;enumeration value="isometricOffAxis2Left"/>
 *     &lt;enumeration value="isometricOffAxis2Right"/>
 *     &lt;enumeration value="isometricOffAxis2Top"/>
 *     &lt;enumeration value="isometricOffAxis3Left"/>
 *     &lt;enumeration value="isometricOffAxis3Right"/>
 *     &lt;enumeration value="isometricOffAxis3Bottom"/>
 *     &lt;enumeration value="isometricOffAxis4Left"/>
 *     &lt;enumeration value="isometricOffAxis4Right"/>
 *     &lt;enumeration value="isometricOffAxis4Bottom"/>
 *     &lt;enumeration value="obliqueTopLeft"/>
 *     &lt;enumeration value="obliqueTop"/>
 *     &lt;enumeration value="obliqueTopRight"/>
 *     &lt;enumeration value="obliqueLeft"/>
 *     &lt;enumeration value="obliqueRight"/>
 *     &lt;enumeration value="obliqueBottomLeft"/>
 *     &lt;enumeration value="obliqueBottom"/>
 *     &lt;enumeration value="obliqueBottomRight"/>
 *     &lt;enumeration value="perspectiveFront"/>
 *     &lt;enumeration value="perspectiveLeft"/>
 *     &lt;enumeration value="perspectiveRight"/>
 *     &lt;enumeration value="perspectiveAbove"/>
 *     &lt;enumeration value="perspectiveBelow"/>
 *     &lt;enumeration value="perspectiveAboveLeftFacing"/>
 *     &lt;enumeration value="perspectiveAboveRightFacing"/>
 *     &lt;enumeration value="perspectiveContrastingLeftFacing"/>
 *     &lt;enumeration value="perspectiveContrastingRightFacing"/>
 *     &lt;enumeration value="perspectiveHeroicLeftFacing"/>
 *     &lt;enumeration value="perspectiveHeroicRightFacing"/>
 *     &lt;enumeration value="perspectiveHeroicExtremeLeftFacing"/>
 *     &lt;enumeration value="perspectiveHeroicExtremeRightFacing"/>
 *     &lt;enumeration value="perspectiveRelaxed"/>
 *     &lt;enumeration value="perspectiveRelaxedModerately"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_PresetCameraType")
@XmlEnum
public enum STPresetCameraType {

    @XmlEnumValue("legacyObliqueTopLeft")
    LEGACY_OBLIQUE_TOP_LEFT("legacyObliqueTopLeft"),
    @XmlEnumValue("legacyObliqueTop")
    LEGACY_OBLIQUE_TOP("legacyObliqueTop"),
    @XmlEnumValue("legacyObliqueTopRight")
    LEGACY_OBLIQUE_TOP_RIGHT("legacyObliqueTopRight"),
    @XmlEnumValue("legacyObliqueLeft")
    LEGACY_OBLIQUE_LEFT("legacyObliqueLeft"),
    @XmlEnumValue("legacyObliqueFront")
    LEGACY_OBLIQUE_FRONT("legacyObliqueFront"),
    @XmlEnumValue("legacyObliqueRight")
    LEGACY_OBLIQUE_RIGHT("legacyObliqueRight"),
    @XmlEnumValue("legacyObliqueBottomLeft")
    LEGACY_OBLIQUE_BOTTOM_LEFT("legacyObliqueBottomLeft"),
    @XmlEnumValue("legacyObliqueBottom")
    LEGACY_OBLIQUE_BOTTOM("legacyObliqueBottom"),
    @XmlEnumValue("legacyObliqueBottomRight")
    LEGACY_OBLIQUE_BOTTOM_RIGHT("legacyObliqueBottomRight"),
    @XmlEnumValue("legacyPerspectiveTopLeft")
    LEGACY_PERSPECTIVE_TOP_LEFT("legacyPerspectiveTopLeft"),
    @XmlEnumValue("legacyPerspectiveTop")
    LEGACY_PERSPECTIVE_TOP("legacyPerspectiveTop"),
    @XmlEnumValue("legacyPerspectiveTopRight")
    LEGACY_PERSPECTIVE_TOP_RIGHT("legacyPerspectiveTopRight"),
    @XmlEnumValue("legacyPerspectiveLeft")
    LEGACY_PERSPECTIVE_LEFT("legacyPerspectiveLeft"),
    @XmlEnumValue("legacyPerspectiveFront")
    LEGACY_PERSPECTIVE_FRONT("legacyPerspectiveFront"),
    @XmlEnumValue("legacyPerspectiveRight")
    LEGACY_PERSPECTIVE_RIGHT("legacyPerspectiveRight"),
    @XmlEnumValue("legacyPerspectiveBottomLeft")
    LEGACY_PERSPECTIVE_BOTTOM_LEFT("legacyPerspectiveBottomLeft"),
    @XmlEnumValue("legacyPerspectiveBottom")
    LEGACY_PERSPECTIVE_BOTTOM("legacyPerspectiveBottom"),
    @XmlEnumValue("legacyPerspectiveBottomRight")
    LEGACY_PERSPECTIVE_BOTTOM_RIGHT("legacyPerspectiveBottomRight"),
    @XmlEnumValue("orthographicFront")
    ORTHOGRAPHIC_FRONT("orthographicFront"),
    @XmlEnumValue("isometricTopUp")
    ISOMETRIC_TOP_UP("isometricTopUp"),
    @XmlEnumValue("isometricTopDown")
    ISOMETRIC_TOP_DOWN("isometricTopDown"),
    @XmlEnumValue("isometricBottomUp")
    ISOMETRIC_BOTTOM_UP("isometricBottomUp"),
    @XmlEnumValue("isometricBottomDown")
    ISOMETRIC_BOTTOM_DOWN("isometricBottomDown"),
    @XmlEnumValue("isometricLeftUp")
    ISOMETRIC_LEFT_UP("isometricLeftUp"),
    @XmlEnumValue("isometricLeftDown")
    ISOMETRIC_LEFT_DOWN("isometricLeftDown"),
    @XmlEnumValue("isometricRightUp")
    ISOMETRIC_RIGHT_UP("isometricRightUp"),
    @XmlEnumValue("isometricRightDown")
    ISOMETRIC_RIGHT_DOWN("isometricRightDown"),
    @XmlEnumValue("isometricOffAxis1Left")
    ISOMETRIC_OFF_AXIS_1_LEFT("isometricOffAxis1Left"),
    @XmlEnumValue("isometricOffAxis1Right")
    ISOMETRIC_OFF_AXIS_1_RIGHT("isometricOffAxis1Right"),
    @XmlEnumValue("isometricOffAxis1Top")
    ISOMETRIC_OFF_AXIS_1_TOP("isometricOffAxis1Top"),
    @XmlEnumValue("isometricOffAxis2Left")
    ISOMETRIC_OFF_AXIS_2_LEFT("isometricOffAxis2Left"),
    @XmlEnumValue("isometricOffAxis2Right")
    ISOMETRIC_OFF_AXIS_2_RIGHT("isometricOffAxis2Right"),
    @XmlEnumValue("isometricOffAxis2Top")
    ISOMETRIC_OFF_AXIS_2_TOP("isometricOffAxis2Top"),
    @XmlEnumValue("isometricOffAxis3Left")
    ISOMETRIC_OFF_AXIS_3_LEFT("isometricOffAxis3Left"),
    @XmlEnumValue("isometricOffAxis3Right")
    ISOMETRIC_OFF_AXIS_3_RIGHT("isometricOffAxis3Right"),
    @XmlEnumValue("isometricOffAxis3Bottom")
    ISOMETRIC_OFF_AXIS_3_BOTTOM("isometricOffAxis3Bottom"),
    @XmlEnumValue("isometricOffAxis4Left")
    ISOMETRIC_OFF_AXIS_4_LEFT("isometricOffAxis4Left"),
    @XmlEnumValue("isometricOffAxis4Right")
    ISOMETRIC_OFF_AXIS_4_RIGHT("isometricOffAxis4Right"),
    @XmlEnumValue("isometricOffAxis4Bottom")
    ISOMETRIC_OFF_AXIS_4_BOTTOM("isometricOffAxis4Bottom"),
    @XmlEnumValue("obliqueTopLeft")
    OBLIQUE_TOP_LEFT("obliqueTopLeft"),
    @XmlEnumValue("obliqueTop")
    OBLIQUE_TOP("obliqueTop"),
    @XmlEnumValue("obliqueTopRight")
    OBLIQUE_TOP_RIGHT("obliqueTopRight"),
    @XmlEnumValue("obliqueLeft")
    OBLIQUE_LEFT("obliqueLeft"),
    @XmlEnumValue("obliqueRight")
    OBLIQUE_RIGHT("obliqueRight"),
    @XmlEnumValue("obliqueBottomLeft")
    OBLIQUE_BOTTOM_LEFT("obliqueBottomLeft"),
    @XmlEnumValue("obliqueBottom")
    OBLIQUE_BOTTOM("obliqueBottom"),
    @XmlEnumValue("obliqueBottomRight")
    OBLIQUE_BOTTOM_RIGHT("obliqueBottomRight"),
    @XmlEnumValue("perspectiveFront")
    PERSPECTIVE_FRONT("perspectiveFront"),
    @XmlEnumValue("perspectiveLeft")
    PERSPECTIVE_LEFT("perspectiveLeft"),
    @XmlEnumValue("perspectiveRight")
    PERSPECTIVE_RIGHT("perspectiveRight"),
    @XmlEnumValue("perspectiveAbove")
    PERSPECTIVE_ABOVE("perspectiveAbove"),
    @XmlEnumValue("perspectiveBelow")
    PERSPECTIVE_BELOW("perspectiveBelow"),
    @XmlEnumValue("perspectiveAboveLeftFacing")
    PERSPECTIVE_ABOVE_LEFT_FACING("perspectiveAboveLeftFacing"),
    @XmlEnumValue("perspectiveAboveRightFacing")
    PERSPECTIVE_ABOVE_RIGHT_FACING("perspectiveAboveRightFacing"),
    @XmlEnumValue("perspectiveContrastingLeftFacing")
    PERSPECTIVE_CONTRASTING_LEFT_FACING("perspectiveContrastingLeftFacing"),
    @XmlEnumValue("perspectiveContrastingRightFacing")
    PERSPECTIVE_CONTRASTING_RIGHT_FACING("perspectiveContrastingRightFacing"),
    @XmlEnumValue("perspectiveHeroicLeftFacing")
    PERSPECTIVE_HEROIC_LEFT_FACING("perspectiveHeroicLeftFacing"),
    @XmlEnumValue("perspectiveHeroicRightFacing")
    PERSPECTIVE_HEROIC_RIGHT_FACING("perspectiveHeroicRightFacing"),
    @XmlEnumValue("perspectiveHeroicExtremeLeftFacing")
    PERSPECTIVE_HEROIC_EXTREME_LEFT_FACING("perspectiveHeroicExtremeLeftFacing"),
    @XmlEnumValue("perspectiveHeroicExtremeRightFacing")
    PERSPECTIVE_HEROIC_EXTREME_RIGHT_FACING("perspectiveHeroicExtremeRightFacing"),
    @XmlEnumValue("perspectiveRelaxed")
    PERSPECTIVE_RELAXED("perspectiveRelaxed"),
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
