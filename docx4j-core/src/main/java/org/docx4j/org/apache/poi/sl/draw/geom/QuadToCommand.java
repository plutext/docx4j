/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
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

/**
 * <p>Java class for CT_Path2DQuadBezierTo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CT_Path2DQuadBezierTo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="pt" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AdjPoint2D" maxOccurs="2" minOccurs="2"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
// @XmlAccessorType(XmlAccessType.FIELD)
// @XmlType(name = "CT_Path2DQuadBezierTo", propOrder = {"pt"})
public final class QuadToCommand implements QuadToCommandIf {

    // @XmlElement(required = true)
    private final AdjustPoint pt1 = new AdjustPoint();
    // @XmlElement(required = true)
    private final AdjustPoint pt2 = new AdjustPoint();

    @Override
    public AdjustPoint getPt1() {
        return pt1;
    }

    @Override
    public void setPt1(AdjustPointIf pt1) {
        if (pt1 != null) {
            this.pt1.setX(pt1.getX());
            this.pt1.setY(pt1.getY());
        }
    }

    @Override
    public AdjustPoint getPt2() {
        return pt2;
    }

    @Override
    public void setPt2(AdjustPointIf pt2) {
        if (pt2 != null) {
            this.pt2.setX(pt2.getX());
            this.pt2.setY(pt2.getY());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuadToCommand)) return false;
        QuadToCommand that = (QuadToCommand) o;
        return Objects.equals(pt1, that.pt1) &&
                Objects.equals(pt2, that.pt2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pt1, pt2);
    }

}
