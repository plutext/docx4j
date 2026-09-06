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

package org.docx4j.org.apache.poi.hemf.draw;

import static org.docx4j.org.apache.poi.hwmf.record.HwmfBrushStyle.BS_NULL;
import static org.docx4j.org.apache.poi.hwmf.record.HwmfBrushStyle.BS_SOLID;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.docx4j.org.apache.poi.hemf.draw.HemfDrawProperties.TransOperand;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfComment.EmfComment;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfRecord;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusRecord;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfDrawProperties;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfGraphics;
import org.docx4j.org.apache.poi.hwmf.record.HwmfBrushStyle;
import org.docx4j.org.apache.poi.hwmf.record.HwmfColorRef;
import org.docx4j.org.apache.poi.hwmf.record.HwmfMisc;
import org.docx4j.org.apache.poi.hwmf.record.HwmfObjectTableEntry;
import org.docx4j.org.apache.poi.hwmf.record.HwmfPenStyle;
import org.docx4j.org.apache.poi.util.Internal;

public class HemfGraphics extends HwmfGraphics {

    public enum EmfRenderState {
        INITIAL,
        EMF_ONLY,
        EMFPLUS_ONLY,
        EMF_DCONTEXT
    }

    private static final HwmfColorRef WHITE = new HwmfColorRef(Color.WHITE);
    private static final HwmfColorRef LTGRAY = new HwmfColorRef(new Color(0x00C0C0C0));
    private static final HwmfColorRef GRAY = new HwmfColorRef(new Color(0x00808080));
    private static final HwmfColorRef DKGRAY = new HwmfColorRef(new Color(0x00404040));
    private static final HwmfColorRef BLACK = new HwmfColorRef(Color.BLACK);

    private EmfRenderState renderState = EmfRenderState.INITIAL;
    private final Map<Integer,HwmfObjectTableEntry> plusObjectTable = new HashMap<>();
    private final Map<Integer,HemfDrawProperties> plusPropStack = new HashMap<>();

    public HemfGraphics(Graphics2D graphicsCtx, Rectangle2D bbox) {
        super(graphicsCtx,bbox);
        // add dummy entry for object ind ex 0, as emf is 1-based
        objectIndexes.set(0);
        getProperties().setBkMode(HwmfMisc.WmfSetBkMode.HwmfBkMode.TRANSPARENT);
    }

    @Override
    public HemfDrawProperties getProperties() {
        return (HemfDrawProperties)super.getProperties();
    }

    @Override
    protected HemfDrawProperties newProperties(HwmfDrawProperties oldProps) {
        return (oldProps == null)
            ? new HemfDrawProperties()
            : new HemfDrawProperties((HemfDrawProperties)oldProps);
    }

    public EmfRenderState getRenderState() {
        return renderState;
    }

    public void setRenderState(EmfRenderState renderState) {
        this.renderState = renderState;
    }

    public void draw(HemfRecord r) {
        switch (getRenderState()) {
            default:
            case EMF_DCONTEXT:
            case INITIAL:
                r.draw(this);
                break;
            case EMF_ONLY:
                if (!(r instanceof EmfComment)) {
                    r.draw(this);
                }
                break;
            case EMFPLUS_ONLY:
                if (r instanceof EmfComment) {
                    r.draw(this);
                }
                break;
        }
    }

    public void draw(HemfPlusRecord r) {
        r.draw(this);
    }

    @Internal
    public void draw(Consumer<Path2D> pathConsumer, FillDrawStyle fillDraw) {
        final HemfDrawProperties prop = getProperties();
        final boolean useBracket = prop.getUsePathBracket();

        final Path2D path;
        if (useBracket) {
            path = prop.getPath();
        } else {
            path = new Path2D.Double();
            path.setWindingRule(prop.getWindingRule());
        }

        // add dummy move-to at start, to handle invalid emfs not containing that move-to
        if (path.getCurrentPoint() == null) {
            Point2D pnt = prop.getLocation();
            path.moveTo(pnt.getX(), pnt.getY());
        }

        try {
            pathConsumer.accept(path);
        } catch (Exception e) {
            // workaround if a path has been started and no MoveTo command
            // has been specified before the first lineTo/splineTo
            final Point2D loc = prop.getLocation();
            path.moveTo(loc.getX(), loc.getY());
            pathConsumer.accept(path);
        }

        Point2D curPnt = path.getCurrentPoint();
        if (curPnt == null) {
            return;
        }

        prop.setLocation(curPnt);
        if (!useBracket) {
            switch (fillDraw) {
                case FILL:
                    super.fill(path);
                    break;
                case DRAW:
                    super.draw(path);
                    break;
                case FILL_DRAW:
                    super.fill(path);
                    super.draw(path);
                    break;
            }
        }

    }

    /**
     * Adds or sets an record of type {@link HwmfObjectTableEntry} to the object table.
     * The index must be &gt; 0
     *
     * @param entry the record to be stored
     * @param index the index to be overwritten, regardless if its content was unset before
     *
     * @see HwmfGraphics#addObjectTableEntry(HwmfObjectTableEntry)
     */
    public void addObjectTableEntry(HwmfObjectTableEntry entry, int index) {
        // in EMF the index must > 0
        if (index < 1) {
            throw new IndexOutOfBoundsException("Object table entry index in EMF must be > 0 - invalid index: "+index);
        }
        objectIndexes.set(index);
        objectTable.put(index, entry);
    }

    /**
     * Adds or sets an record of type {@link HwmfObjectTableEntry} to the plus object table.
     * The index must be in the range [0..63]
     *
     * @param entry the record to be stored
     * @param index the index to be overwritten, regardless if its content was unset before
     *
     * @see HwmfGraphics#addObjectTableEntry(HwmfObjectTableEntry)
     */
    public void addPlusObjectTableEntry(HwmfObjectTableEntry entry, int index) {
        // in EMF+ the index must be between 0 and 63
        if (index < 0 || index > 63) {
            throw new IndexOutOfBoundsException("Object table entry index in EMF+ must be [0..63] - invalid index: "+index);
        }
        plusObjectTable.put(index, entry);
    }

    /**
     * Gets a record which was registered earlier
     * @param index the record index
     * @return the record or {@code null} if it doesn't exist
     */
    public HwmfObjectTableEntry getObjectTableEntry(int index) {
        // in EMF the index must > 0
        if (index < 1) {
            throw new IndexOutOfBoundsException("Object table entry index in EMF must be > 0 - invalid index: "+index);
        }
        return objectTable.get(index);
    }

    public HwmfObjectTableEntry getPlusObjectTableEntry(int index) {
        // in EMF+ the index must be between 0 and 63
        if (index < 0 || index > 63) {
            throw new IndexOutOfBoundsException("Object table entry index in EMF+ must be [0..63] - invalid index: "+index);
        }
        return plusObjectTable.get(index);
    }

    @Override
    public void applyObjectTableEntry(int index) {
        if ((index & 0x80000000) != 0) {
            selectStockObject(index);
        } else {
            HwmfObjectTableEntry ote = objectTable.get(index);
            if (ote == null) {
                throw new NoSuchElementException("EMF reference exception - object table entry on index "+index+" was deleted before.");
            }
            ote.applyObject(this);
        }
    }

    public void applyPlusObjectTableEntry(int index) {
        if ((index & 0x80000000) != 0) {
            selectStockObject(index);
        } else {
            HwmfObjectTableEntry ote = plusObjectTable.get(index);
            if (ote == null) {
                throw new NoSuchElementException("EMF+ reference exception - plus object table entry on index "+index+" was deleted before.");
            }
            ote.applyObject(this);
        }
    }

    private void selectStockObject(int objectIndex) {
        final HemfDrawProperties prop = getProperties();
        switch (objectIndex) {
            case 0x80000000:
                // WHITE_BRUSH - A white, solid-color brush
                // BrushStyle: BS_SOLID
                // Color: 0x00FFFFFF
                prop.setBrushColor(WHITE);
                prop.setBrushStyle(BS_SOLID);
                break;
            case 0x80000001:
                // LTGRAY_BRUSH - A light gray, solid-color brush
                // BrushStyle: BS_SOLID
                // Color: 0x00C0C0C0
                prop.setBrushColor(LTGRAY);
                prop.setBrushStyle(BS_SOLID);
                break;
            case 0x80000002:
                // GRAY_BRUSH - A gray, solid-color brush
                // BrushStyle: BS_SOLID
                // Color: 0x00808080
                prop.setBrushColor(GRAY);
                prop.setBrushStyle(BS_SOLID);
                break;
            case 0x80000003:
                // DKGRAY_BRUSH - A dark gray, solid color brush
                // BrushStyle: BS_SOLID
                // Color: 0x00404040
                prop.setBrushColor(DKGRAY);
                prop.setBrushStyle(BS_SOLID);
                break;
            case 0x80000004:
                // BLACK_BRUSH - A black, solid color brush
                // BrushStyle: BS_SOLID
                // Color: 0x00000000
                prop.setBrushColor(BLACK);
                prop.setBrushStyle(BS_SOLID);
                break;
            case 0x80000005:
                // NULL_BRUSH - A null brush
                // BrushStyle: BS_NULL
                prop.setBrushStyle(BS_NULL);
                break;
            case 0x80000006:
                // WHITE_PEN - A white, solid-color pen
                // PenStyle: PS_COSMETIC + PS_SOLID
                // ColorRef: 0x00FFFFFF
                prop.setPenStyle(HwmfPenStyle.valueOf(0));
                prop.setPenWidth(1);
                prop.setPenColor(WHITE);
                break;
            case 0x80000007:
                // BLACK_PEN - A black, solid-color pen
                // PenStyle: PS_COSMETIC + PS_SOLID
                // ColorRef: 0x00000000
                prop.setPenStyle(HwmfPenStyle.valueOf(0));
                prop.setPenWidth(1);
                prop.setPenColor(BLACK);
                break;
            case 0x80000008:
                // NULL_PEN - A null pen
                // PenStyle: PS_NULL
                prop.setPenStyle(HwmfPenStyle.valueOf(HwmfPenStyle.HwmfLineDash.NULL.wmfFlag));
                break;
            case 0x8000000A:
                // OEM_FIXED_FONT - A fixed-width, OEM character set
                // Charset: OEM_CHARSET
                // PitchAndFamily: FF_DONTCARE + FIXED_PITCH
                break;
            case 0x8000000B:
                // ANSI_FIXED_FONT - A fixed-width font
                // Charset: ANSI_CHARSET
                // PitchAndFamily: FF_DONTCARE + FIXED_PITCH
                break;
            case 0x8000000C:
                // ANSI_VAR_FONT - A variable-width font
                // Charset: ANSI_CHARSET
                // PitchAndFamily: FF_DONTCARE + VARIABLE_PITCH
                break;
            case 0x8000000D:
                // SYSTEM_FONT - A font that is guaranteed to be available in the operating system
                break;
            case 0x8000000E:
                // DEVICE_DEFAULT_FONT
                // The default font that is provided by the graphics device driver for the current output device
                break;
            case 0x8000000F:
                // DEFAULT_PALETTE
                // The default palette that is defined for the current output device.
                break;
            case 0x80000010:
                // SYSTEM_FIXED_FONT
                // A fixed-width font that is guaranteed to be available in the operating system.
                break;
            case 0x80000011:
                // DEFAULT_GUI_FONT
                // The default font that is used for user interface objects such as menus and dialog boxes.
                break;
            case 0x80000012:
                // DC_BRUSH
                // The solid-color brush that is currently selected in the playback device context.
                break;
            case 0x80000013:
                // DC_PEN
                // The solid-color pen that is currently selected in the playback device context.
                break;
        }
    }

    @Override
    protected Paint getHatchedFill() {
        // TODO: use EmfPlusHatchBrushData
        return super.getHatchedFill();
    }

    @Override
    public void updateWindowMapMode() {
        super.updateWindowMapMode();
        HemfDrawProperties prop = getProperties();

        List<AffineTransform> transXform = prop.getTransXForm();
        List<TransOperand> transOper = prop.getTransOper();
        assert(transXform.size() == transOper.size());

        AffineTransform tx = graphicsCtx.getTransform();
        Iterator<AffineTransform> iter = transXform.iterator();
        transOper.forEach(to -> to.fun.accept(tx, iter.next()));

        graphicsCtx.setTransform(tx);
    }

    @Override
    public void fill(Shape shape) {
        HemfDrawProperties prop = getProperties();
        if (prop.getBrushStyle() == HwmfBrushStyle.BS_NULL) {
            return;
        }

        Composite old = graphicsCtx.getComposite();
        graphicsCtx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
//        if (prop.getBkMode() == HwmfMisc.WmfSetBkMode.HwmfBkMode.OPAQUE) {
//            graphicsCtx.setPaint(prop.getBackgroundColor().getColor());
//            graphicsCtx.fill(shape);
//        }

        graphicsCtx.setPaint(getFill());
        graphicsCtx.fill(shape);
        graphicsCtx.setComposite(old);
    }


    @Override
    protected Paint getLinearGradient() {
        HemfDrawProperties prop = getProperties();
        Rectangle2D rect = prop.getBrushRect();
        List<? extends Map.Entry<Float, Color>> colorsH = prop.getBrushColorsH();
        assert(rect != null && colorsH != null);

        // TODO: handle ColorsV list with a custom GradientPaint
        // for an idea on how to handle 2d-gradients google "bilinear color interpolation".
        // basically use two linear interpolations for x/y or vertical/horizontal axis.
        // the resulting two colors need to be interpolated by 50%.

        return new LinearGradientPaint(
            new Point2D.Double(rect.getMinX(),rect.getCenterY()),
            new Point2D.Double(rect.getMaxX(),rect.getCenterY()),
            toArray(colorsH.stream().map(Map.Entry::getKey), colorsH.size()),
            colorsH.stream().map(Map.Entry::getValue).toArray(Color[]::new),
            MultipleGradientPaint.CycleMethod.NO_CYCLE,
            MultipleGradientPaint.ColorSpaceType.SRGB,
            prop.getBrushTransform()
        );
    }

    private static float[] toArray(Stream<? extends Number> numbers, int size) {
        float[] arr = new float[size];
        final int[] i = {0};
        numbers.forEach(n -> arr[i[0]++] = n.floatValue());
        return arr;
    }

    /**
     * Saves the current properties to the plus stack
     */
    public void savePlusProperties(int index) {
        final HemfDrawProperties p = getProperties();
        assert(p != null);
        p.setTransform(graphicsCtx.getTransform());
        p.setClip(graphicsCtx.getClip());
        plusPropStack.put(index,p);
        prop = newProperties(p);
    }

    /**
     * Restores the properties from the plus stack
     *
     * @param index the index of the previously saved properties
     */
    public void restorePlusProperties(int index) {
        if (!plusPropStack.containsKey(index)) {
            return;
        }

        prop = new HemfDrawProperties(plusPropStack.get(index));

        graphicsCtx.setTransform(prop.getTransform());
        graphicsCtx.setClip(prop.getClip());
    }

}
