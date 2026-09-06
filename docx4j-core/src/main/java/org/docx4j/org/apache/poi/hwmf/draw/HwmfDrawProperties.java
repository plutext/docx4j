/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 *
 * This notice is included to meet the condition in clause 4(b) of the License.
 */

/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.docx4j.org.apache.poi.hwmf.draw;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.docx4j.org.apache.poi.hwmf.draw.HwmfGraphics.BufferedImageRenderer;
import org.docx4j.org.apache.poi.hwmf.record.HwmfBinaryRasterOp;
import org.docx4j.org.apache.poi.hwmf.record.HwmfBrushStyle;
import org.docx4j.org.apache.poi.hwmf.record.HwmfColorRef;
import org.docx4j.org.apache.poi.hwmf.record.HwmfFill.WmfSetPolyfillMode.HwmfPolyfillMode;
import org.docx4j.org.apache.poi.hwmf.record.HwmfFont;
import org.docx4j.org.apache.poi.hwmf.record.HwmfHatchStyle;
import org.docx4j.org.apache.poi.hwmf.record.HwmfMapMode;
import org.docx4j.org.apache.poi.hwmf.record.HwmfMisc.WmfSetBkMode.HwmfBkMode;
import org.docx4j.org.apache.poi.hwmf.record.HwmfPalette.PaletteEntry;
import org.docx4j.org.apache.poi.hwmf.record.HwmfPenStyle;
import org.docx4j.org.apache.poi.hwmf.record.HwmfTernaryRasterOp;
import org.docx4j.org.apache.poi.hwmf.record.HwmfText.HwmfTextAlignment;
import org.docx4j.org.apache.poi.hwmf.record.HwmfText.HwmfTextVerticalAlignment;
import org.docx4j.org.apache.poi.sl.draw.ImageRenderer;

public class HwmfDrawProperties {
    private final Rectangle2D window;
    private Rectangle2D viewport;
    private final Point2D location;
    private HwmfMapMode mapMode;
    private HwmfColorRef backgroundColor;
    private HwmfBrushStyle brushStyle;
    private HwmfColorRef brushColor;
    private HwmfHatchStyle brushHatch;
    private ImageRenderer brushBitmap;
    private final AffineTransform brushTransform = new AffineTransform();
    private double penWidth;
    private HwmfPenStyle penStyle;
    private HwmfColorRef penColor;
    private double penMiterLimit;
    private HwmfBkMode bkMode;
    private HwmfPolyfillMode polyfillMode;
    private Shape region;
    private List<PaletteEntry> palette;
    private int paletteOffset;
    private HwmfFont font;
    private HwmfColorRef textColor;
    private HwmfTextAlignment textAlignLatin;
    private HwmfTextVerticalAlignment textVAlignLatin;
    private HwmfTextAlignment textAlignAsian;
    private HwmfTextVerticalAlignment textVAlignAsian;
    private HwmfBinaryRasterOp rasterOp2;
    private HwmfTernaryRasterOp rasterOp3;
    protected Shape clip;
    protected final AffineTransform transform = new AffineTransform();

    public HwmfDrawProperties() {
        window = new Rectangle2D.Double(0, 0, 1, 1);
        viewport = null;
        location = new Point2D.Double(0,0);
        mapMode = HwmfMapMode.MM_ANISOTROPIC;
        backgroundColor = new HwmfColorRef(Color.BLACK);
        brushStyle = HwmfBrushStyle.BS_SOLID;
        brushColor = new HwmfColorRef(Color.BLACK);
        brushHatch = HwmfHatchStyle.HS_HORIZONTAL;
        penWidth = 1;
        penStyle = HwmfPenStyle.valueOf(0);
        penColor = new HwmfColorRef(Color.BLACK);
        penMiterLimit = 10;
        bkMode = HwmfBkMode.OPAQUE;
        polyfillMode = HwmfPolyfillMode.WINDING;
        textColor = new HwmfColorRef(Color.BLACK);
        textAlignLatin = HwmfTextAlignment.LEFT;
        textVAlignLatin = HwmfTextVerticalAlignment.TOP;
        textAlignAsian = HwmfTextAlignment.RIGHT;
        textVAlignAsian = HwmfTextVerticalAlignment.TOP;
        rasterOp2 = HwmfBinaryRasterOp.R2_COPYPEN;
        rasterOp3 = null; // default: PATCOPY?
        clip = null;
        font = new HwmfFont();
        font.initDefaults();
    }

    public HwmfDrawProperties(HwmfDrawProperties other) {
        this.window = (other.window == null) ? null : (Rectangle2D)other.window.clone();
        this.viewport = (other.viewport == null) ? null : (Rectangle2D)other.viewport.clone();
        this.location = (Point2D)other.location.clone();
        this.mapMode = other.mapMode;
        this.backgroundColor = (other.backgroundColor == null) ? null : other.backgroundColor.copy();
        this.brushStyle = other.brushStyle;
        this.brushColor = other.brushColor.copy();
        this.brushHatch = other.brushHatch;
        this.brushBitmap = other.brushBitmap;
        this.brushTransform.setTransform(other.brushTransform);
        this.penWidth = other.penWidth;
        this.penStyle = (other.penStyle == null) ? null : other.penStyle.copy();
        this.penColor = (other.penColor == null) ? null : other.penColor.copy();
        this.penMiterLimit = other.penMiterLimit;
        this.bkMode = other.bkMode;
        this.polyfillMode = other.polyfillMode;
        if (other.region instanceof Rectangle2D) {
            this.region = other.region.getBounds2D();
        } else if (other.region instanceof Area) {
            this.region = new Area(other.region);
        }
        this.palette = other.palette;
        this.paletteOffset = other.paletteOffset;
        this.font = other.font;
        this.textColor = (other.textColor == null) ? null : other.textColor.copy();
        this.textAlignLatin = other.textAlignLatin;
        this.textVAlignLatin = other.textVAlignLatin;
        this.textAlignAsian = other.textAlignAsian;
        this.textVAlignAsian = other.textVAlignAsian;
        this.rasterOp2 = other.rasterOp2;
        this.rasterOp3 = other.rasterOp3;
        this.transform.setTransform(other.transform);
        this.clip = other.clip;
    }

    public void setViewportExt(double width, double height) {
        if (viewport == null) {
            viewport = (Rectangle2D)window.clone();
        }
        double x = viewport.getX();
        double y = viewport.getY();
        double w = (width != 0) ? width : viewport.getWidth();
        double h = (height != 0) ? height : viewport.getHeight();
        viewport.setRect(x, y, w, h);
    }

    public void setViewportOrg(double x, double y) {
        if (viewport == null) {
            viewport = (Rectangle2D)window.clone();
        }
        double w = viewport.getWidth();
        double h = viewport.getHeight();
        viewport.setRect(x, y, w, h);
    }

    public Rectangle2D getViewport() {
        return (viewport == null) ? null : (Rectangle2D)viewport.clone();
    }

    public void setWindowExt(double width, double height) {
        double x = window.getX();
        double y = window.getY();
        double w = (width != 0) ? width : window.getWidth();
        double h = (height != 0) ? height : window.getHeight();
        window.setRect(x, y, w, h);
    }

    public void setWindowOrg(double x, double y) {
        double w = window.getWidth();
        double h = window.getHeight();
        window.setRect(x, y, w, h);
    }

    public Rectangle2D getWindow() {
        return (Rectangle2D)window.clone();
    }

    public void setLocation(double x, double y) {
        location.setLocation(x, y);
    }

    public void setLocation(Point2D point) {
        location.setLocation(point);
    }

    public Point2D getLocation() {
        return (Point2D)location.clone();
    }

    public void setMapMode(HwmfMapMode mapMode) {
        this.mapMode = mapMode;
    }

    public HwmfMapMode getMapMode() {
        return mapMode;
    }

    public HwmfBrushStyle getBrushStyle() {
        return brushStyle;
    }

    public void setBrushStyle(HwmfBrushStyle brushStyle) {
        this.brushStyle = brushStyle;
    }

    public HwmfHatchStyle getBrushHatch() {
        return brushHatch;
    }

    public void setBrushHatch(HwmfHatchStyle brushHatch) {
        this.brushHatch = brushHatch;
    }

    public HwmfColorRef getBrushColor() {
        return brushColor;
    }

    public void setBrushColor(HwmfColorRef brushColor) {
        this.brushColor = brushColor;
    }

    public HwmfBkMode getBkMode() {
        return bkMode;
    }

    public void setBkMode(HwmfBkMode bkMode) {
        this.bkMode = bkMode;
    }

    public HwmfPenStyle getPenStyle() {
        return penStyle;
    }

    public void setPenStyle(HwmfPenStyle penStyle) {
        this.penStyle = penStyle;
    }

    public HwmfColorRef getPenColor() {
        return penColor;
    }

    public void setPenColor(HwmfColorRef penColor) {
        this.penColor = penColor;
    }

    public double getPenWidth() {
        return penWidth;
    }

    public void setPenWidth(double penWidth) {
        this.penWidth = penWidth;
    }

    public double getPenMiterLimit() {
        return penMiterLimit;
    }

    public void setPenMiterLimit(double penMiterLimit) {
        this.penMiterLimit = penMiterLimit;
    }

    public HwmfColorRef getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(HwmfColorRef backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public HwmfPolyfillMode getPolyfillMode() {
        return polyfillMode;
    }

    public void setPolyfillMode(HwmfPolyfillMode polyfillMode) {
        this.polyfillMode = polyfillMode;
    }

    public ImageRenderer getBrushBitmap() {
        return brushBitmap;
    }

    public void setBrushBitmap(ImageRenderer brushBitmap) {
        this.brushBitmap = brushBitmap;
    }

    public void setBrushBitmap(BufferedImage brushBitmap) {
        this.brushBitmap = (brushBitmap == null) ? null : new BufferedImageRenderer(brushBitmap);
    }

    /**
     * Gets the last stored region
     *
     * @return the last stored region
     */
    public Shape getRegion() {
        return region;
    }

    /**
     * Sets a region for further usage
     *
     * @param region a region object which is usually a rectangle
     */
    public void setRegion(Shape region) {
        this.region = region;
    }

    /**
     * Returns the current palette.
     * Callers may modify the palette.
     *
     * @return the current palette or null, if it hasn't been set
     */
    public List<PaletteEntry> getPalette() {
        return palette;
    }

    /**
     * Sets the current palette.
     * It's the callers duty to set a modifiable copy of the palette.
     */
    public void setPalette(List<PaletteEntry> palette) {
        this.palette = palette;
    }

    public int getPaletteOffset() {
        return paletteOffset;
    }

    public void setPaletteOffset(int paletteOffset) {
        this.paletteOffset = paletteOffset;
    }

    public HwmfColorRef getTextColor() {
        return textColor;
    }

    public void setTextColor(HwmfColorRef textColor) {
        this.textColor = textColor;
    }

    public HwmfFont getFont() {
        return font;
    }

    public void setFont(HwmfFont font) {
        this.font = font;
    }

    public HwmfTextAlignment getTextAlignLatin() {
        return textAlignLatin;
    }

    public void setTextAlignLatin(HwmfTextAlignment textAlignLatin) {
        this.textAlignLatin = textAlignLatin;
    }

    public HwmfTextVerticalAlignment getTextVAlignLatin() {
        return textVAlignLatin;
    }

    public void setTextVAlignLatin(HwmfTextVerticalAlignment textVAlignLatin) {
        this.textVAlignLatin = textVAlignLatin;
    }

    public HwmfTextAlignment getTextAlignAsian() {
        return textAlignAsian;
    }

    public void setTextAlignAsian(HwmfTextAlignment textAlignAsian) {
        this.textAlignAsian = textAlignAsian;
    }

    public HwmfTextVerticalAlignment getTextVAlignAsian() {
        return textVAlignAsian;
    }

    public void setTextVAlignAsian(HwmfTextVerticalAlignment textVAlignAsian) {
        this.textVAlignAsian = textVAlignAsian;
    }

    /**
     * @return the current active winding rule ({@link Path2D#WIND_EVEN_ODD} or {@link Path2D#WIND_NON_ZERO})
     */
    public int getWindingRule() {
        return getPolyfillMode().awtFlag;
    }

    public HwmfTernaryRasterOp getRasterOp3() {
        return rasterOp3;
    }

    public void setRasterOp3(HwmfTernaryRasterOp rasterOp3) {
        this.rasterOp3 = rasterOp3;
    }

    public AffineTransform getTransform() {
        return transform;
    }

    public void setTransform(AffineTransform transform) {
        this.transform.setTransform(transform);
    }

    public Shape getClip() {
        return clip;
    }

    public void setClip(Shape clip) {
        this.clip = clip;
    }

    public AffineTransform getBrushTransform() {
        return brushTransform;
    }

    public void setBrushTransform(AffineTransform brushTransform) {
        if (brushTransform == null) {
            this.brushTransform.setToIdentity();
        } else {
            this.brushTransform.setTransform(brushTransform);
        }
    }
    public HwmfBinaryRasterOp getRasterOp2() {
        return rasterOp2;
    }

    public void setRasterOp2(HwmfBinaryRasterOp rasterOp2) {
        this.rasterOp2 = rasterOp2;
    }
}
