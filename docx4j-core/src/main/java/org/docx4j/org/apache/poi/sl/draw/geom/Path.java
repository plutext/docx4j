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

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.docx4j.org.apache.poi.sl.usermodel.PaintStyle.PaintModifier;

/**
 * Specifies a creation path consisting of a series of moves, lines and curves
 * that when combined forms a geometric shape
 *
 * <p>Java class for CT_Path2D complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CT_Path2D"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="close" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Path2DClose"/&gt;
 *         &lt;element name="moveTo" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Path2DMoveTo"/&gt;
 *         &lt;element name="lnTo" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Path2DLineTo"/&gt;
 *         &lt;element name="arcTo" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Path2DArcTo"/&gt;
 *         &lt;element name="quadBezTo" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Path2DQuadBezierTo"/&gt;
 *         &lt;element name="cubicBezTo" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Path2DCubicBezierTo"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="w" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" default="0" /&gt;
 *       &lt;attribute name="h" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" default="0" /&gt;
 *       &lt;attribute name="fill" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PathFillMode" default="norm" /&gt;
 *       &lt;attribute name="stroke" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="extrusionOk" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
// @XmlAccessorType(XmlAccessType.FIELD)
// @XmlType(name = "CT_Path2D", propOrder = {"closeOrMoveToOrLnTo"})
public final class Path implements PathIf {

    // @XmlElements({
    //     @XmlElement(name = "close", type = CTPath2DClose.class),
    //     @XmlElement(name = "moveTo", type = CTPath2DMoveTo.class),
    //     @XmlElement(name = "lnTo", type = CTPath2DLineTo.class),
    //     @XmlElement(name = "arcTo", type = CTPath2DArcTo.class),
    //     @XmlElement(name = "quadBezTo", type = CTPath2DQuadBezierTo.class),
    //     @XmlElement(name = "cubicBezTo", type = CTPath2DCubicBezierTo.class)
    // })
    private final List<PathCommand> commands = new ArrayList<>();
    // @XmlAttribute(name = "fill")
    private PaintModifier fill = PaintModifier.NORM;
    // @XmlAttribute(name = "stroke")
    private boolean stroke = true;
    // @XmlAttribute(name = "extrusionOk")
    private boolean extrusionOk = false;
    // @XmlAttribute(name = "w")
    private long w = -1;
    // @XmlAttribute(name = "h")
    private long h = -1;



    @Override
    public void addCommand(PathCommand cmd){
        commands.add(cmd);
    }

    /**
     * Convert the internal represenation to java.awt.geom.Path2D
     */
    @Override
    public Path2D.Double getPath(Context ctx) {
        Path2D.Double path = new Path2D.Double();
        for(PathCommand cmd : commands) {
            cmd.execute(path, ctx);
        }
        return path;
    }

    @Override
    public boolean isStroked(){
        return stroke;
    }

    @Override
    public void setStroke(boolean stroke) {
        this.stroke = stroke;
    }

    @Override
    public boolean isFilled(){
        return fill != PaintModifier.NONE;
    }

    @Override
    public PaintModifier getFill() {
        return fill;
    }

    @Override
    public void setFill(PaintModifier fill) {
        this.fill = fill;
    }

    @Override
    public long getW(){
        return w;
    }

    @Override
    public void setW(long w) {
        this.w = w;
    }

    @Override
    public long getH(){
        return h;
    }

    @Override
    public void setH(long h) {
        this.h = h;
    }

    @Override
    public boolean isExtrusionOk() {
        return extrusionOk;
    }

    @Override
    public void setExtrusionOk(boolean extrusionOk) {
        this.extrusionOk = extrusionOk;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Path)) return false;
        Path ctPath2D = (Path) o;
        return Objects.equals(commands, ctPath2D.commands) &&
                Objects.equals(w, ctPath2D.w) &&
                Objects.equals(h, ctPath2D.h) &&
                fill == ctPath2D.fill &&
                Objects.equals(stroke, ctPath2D.stroke) &&
                Objects.equals(extrusionOk, ctPath2D.extrusionOk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commands, w, h, fill.ordinal(), stroke, extrusionOk);
    }
}
