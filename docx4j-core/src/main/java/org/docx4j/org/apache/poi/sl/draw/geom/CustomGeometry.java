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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;

/**
 * Definition of a custom geometric shape
 *
 *
 * <p>Java class for CT_CustomGeometry2D complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CT_CustomGeometry2D"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="avLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GeomGuideList" minOccurs="0"/&gt;
 *         &lt;element name="gdLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GeomGuideList" minOccurs="0"/&gt;
 *         &lt;element name="ahLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AdjustHandleList" minOccurs="0"/&gt;
 *         &lt;element name="cxnLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ConnectionSiteList" minOccurs="0"/&gt;
 *         &lt;element name="rect" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GeomRect" minOccurs="0"/&gt;
 *         &lt;element name="pathLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Path2DList"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
public final class CustomGeometry implements Iterable<PathIf>{
    final List<AdjustValueIf> adjusts = new ArrayList<>();
    final List<GuideIf> guides = new ArrayList<>();
    final List<PathIf> paths = new ArrayList<>();
    final List<AdjustHandle> handles = new ArrayList<>();
    final List<ConnectionSiteIf> connections = new ArrayList<>();
    Path textBounds;

    public void addAdjustGuide(AdjustValueIf guide) {
        adjusts.add(guide);
    }

    public void addGeomGuide(GuideIf guide) {
        guides.add(guide);
    }

    public void addAdjustHandle(AdjustHandle handle) {
        handles.add(handle);
    }

    public void addConnectionSite(ConnectionSiteIf connection) {
        connections.add(connection);
    }

    public void addPath(PathIf path) {
        paths.add(path);
    }

    public void setTextBounds(String left, String top, String right, String bottom) {
        textBounds = new Path();
        textBounds.addCommand(moveTo(left,top));
        textBounds.addCommand(lineTo(right, top));
        textBounds.addCommand(lineTo(right, bottom));
        textBounds.addCommand(lineTo(left, bottom));
        textBounds.addCommand(new ClosePathCommand());
    }

    private static MoveToCommand moveTo(String x, String y) {
        AdjustPoint pt = new AdjustPoint();
        pt.setX(x);
        pt.setY(y);
        MoveToCommand cmd = new MoveToCommand();
        cmd.setPt(pt);
        return cmd;
    }

    private static LineToCommand lineTo(String x, String y) {
        AdjustPoint pt = new AdjustPoint();
        pt.setX(x);
        pt.setY(y);
        LineToCommand cmd = new LineToCommand();
        cmd.setPt(pt);
        return cmd;
    }


    @Override
    public Iterator<PathIf> iterator() {
        return paths.iterator();
    }

    /**
     * @since POI 5.2.0
     */
    @Override
    public Spliterator<PathIf> spliterator() {
        return paths.spliterator();
    }

    public Path getTextBounds(){
        return textBounds;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomGeometry)) return false;
        CustomGeometry that = (CustomGeometry) o;
        return Objects.equals(adjusts, that.adjusts) &&
                Objects.equals(guides, that.guides) &&
                Objects.equals(handles, that.handles) &&
                Objects.equals(connections, that.connections) &&
                Objects.equals(textBounds, that.textBounds) &&
                Objects.equals(paths, that.paths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adjusts, guides, handles, connections, textBounds, paths);
    }
}
