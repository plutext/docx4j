/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; in addition, where the width is 0, a
 * vertical angle is converted to 90 degrees, not 0, so an arc on a flat
 * ellipse runs along it.
 *
 * This notice is included to meet the condition in clause 4(b) of the License.
 */

/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.docx4j.org.apache.poi.sl.draw.geom;

import java.util.Objects;

import org.docx4j.org.apache.poi.util.Internal;

/**
 * ArcTo command within a shape path in DrawingML:
 * {@code &lt;arcTo wR="wr" hR="hr" stAng="stAng" swAng="swAng"/&gt;}
 * <p>
 * Where {@code wr} and {@code wh} are the height and width radii
 * of the supposed circle being used to draw the arc.  This gives the circle
 * a total height of (2 * hR)  and a total width of (2 * wR)
 * <p>
 * stAng is the {@code start} angle and {@code swAng} is the swing angle
 * <p>
 * Java class for CT_Path2DArcTo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CT_Path2DArcTo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="wR" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjCoordinate" /&gt;
 *       &lt;attribute name="hR" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjCoordinate" /&gt;
 *       &lt;attribute name="stAng" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjAngle" /&gt;
 *       &lt;attribute name="swAng" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjAngle" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
// @XmlAccessorType(XmlAccessType.FIELD)
// @XmlType(name = "CT_Path2DArcTo")
public class ArcToCommand implements ArcToCommandIf {

    // @XmlAttribute(name = "wR", required = true)
    private String wr;
    // @XmlAttribute(name = "hR", required = true)
    private String hr;
    // @XmlAttribute(name = "stAng", required = true)
    private String stAng;
    // @XmlAttribute(name = "swAng", required = true)
    private String swAng;

    @Override
    public void setHR(String hr) {
        this.hr = hr;
    }

    @Override
    public String getHR() {
        return hr;
    }

    @Override
    public String getStAng() {
        return stAng;
    }

    @Override
    public String getWR() {
        return wr;
    }

    @Override
    public void setWR(String wr) {
        this.wr = wr;
    }

    @Override
    public void setStAng(String stAng) {
        this.stAng = stAng;
    }

    @Override
    public String getSwAng() {
        return swAng;
    }

    @Override
    public void setSwAng(String swAng) {
        this.swAng = swAng;
    }

    /**
     * Arc2D angles are skewed, OOXML aren't ... so we need to unskew them<p>
     *
     * Furthermore, ooxml angle starts at the X-axis and increases clock-wise,
     * whereas Arc2D api states
     * "45 degrees always falls on the line from the center of the ellipse to
     * the upper right corner of the framing rectangle"
     * so we need to reverse it
     *
     * <pre>
     * AWT:                      OOXML:
     *            |90/-270                     |270/-90 (16200000)
     *            |                            |
     * +/-180-----------0           +/-180-----------0
     *            |               (10800000)   |
     *            |270/-90                     |90/-270 (5400000)
     * </pre>
     *
     * @param ooAngle the angle in OOXML units divided by 60000
     * @param width the width of the bounding box
     * @param height the height of the bounding box
     *
     * @return the angle in degrees
     *
     * @see <a href="http://www.onlinemathe.de/forum/Problem-bei-Winkelberechnungen-einer-Ellipse">unskew angle</a>
     **/
    @Internal
    public static double convertOoxml2AwtAngle(double ooAngle, double width, double height) {
        double aspect = (height / width);
        // reverse angle for awt
        double awtAngle = -ooAngle;
        // normalize angle, in case it's < -360 or > 360 degrees
        double awtAngle2 = awtAngle%360.;
        double awtAngle3 = awtAngle-awtAngle2;
        // because of tangens nature, the values left [90°-270°] and right [270°-90°] of the axis are mirrored/the same
        // and the result of atan2 need to be justified
        switch ((int)(awtAngle2 / 90)) {
            case -3:
                // -270 to -360
                awtAngle3 -= 360;
                awtAngle2 += 360;
                break;
            case -2:
            case -1:
                // -90 to -270
                awtAngle3 -= 180;
                awtAngle2 += 180;
                break;
            default:
            case 0:
                // -90 to 90
                break;
            case 2:
            case 1:
                // 90 to 270
                awtAngle3 += 180;
                awtAngle2 -= 180;
                break;
            case 3:
                // 270 to 360
                awtAngle3 += 360;
                awtAngle2 -= 360;
                break;
        }

        // skew; docx4j: where the width is 0 (an infinite aspect), atan2 gives 0 for a vertical angle too, where the
        // point is the end of the line, at 90 degrees
        double skewed;
        if (Double.isInfinite(aspect)) {
            skewed = (Math.abs(awtAngle2) == 90) ? awtAngle2 : 0;
        } else {
            skewed = Math.toDegrees(Math.atan2(Math.tan(Math.toRadians(awtAngle2)), aspect));
        }
        awtAngle = skewed + awtAngle3;
        return awtAngle;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArcToCommand)) return false;
        ArcToCommand that = (ArcToCommand) o;
        return Objects.equals(wr, that.wr) &&
                Objects.equals(hr, that.hr) &&
                Objects.equals(stAng, that.stAng) &&
                Objects.equals(swAng, that.swAng);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wr, hr, stAng, swAng);
    }
}
