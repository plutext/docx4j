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

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusBrush.EmfPlusHatchStyle;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfDrawProperties;
import org.docx4j.org.apache.poi.sl.draw.ImageRenderer;

public class HemfDrawProperties extends HwmfDrawProperties {
    enum TransOperand {
        left(AffineTransform::concatenate),
        right(AffineTransform::preConcatenate);

        BiConsumer<AffineTransform,AffineTransform> fun;
        TransOperand(BiConsumer<AffineTransform,AffineTransform> fun) {
            this.fun = fun;
        }
    }

    /** Path for path bracket operations */
    protected Path2D path = null;
    protected boolean usePathBracket = false;
    private EmfPlusHatchStyle emfPlusBrushHatch;
    private ImageRenderer emfPlusImage;

    private final List<AffineTransform> transXForm = new ArrayList<>();
    private final List<TransOperand> transOper = new ArrayList<>();

    private Rectangle2D brushRect;
    private List<? extends Map.Entry<Float,Color>> brushColorsV;
    private List<? extends Map.Entry<Float,Color>> brushColorsH;

    public HemfDrawProperties() {
    }

    public HemfDrawProperties(HemfDrawProperties other) {
        super(other);
        path = (other.path != null) ? (Path2D)other.path.clone() : null;
        usePathBracket = other.usePathBracket;
        emfPlusBrushHatch = other.emfPlusBrushHatch;
        // TODO: check how to clone
        clip = other.clip;
        emfPlusImage = other.emfPlusImage;
        transXForm.addAll(other.transXForm);
        transOper.addAll(other.transOper);
        if (other.brushRect != null) {
            brushRect = (Rectangle2D)other.brushRect.clone();
        }
        if (other.brushColorsV != null) {
            brushColorsV = new ArrayList<>(other.brushColorsV);
        }
        if (other.brushColorsH != null) {
            brushColorsH = new ArrayList<>(other.brushColorsH);
        }
    }

    /**
     * @return the current path used for bracket operations
     */
    public Path2D getPath() {
        return path;
    }

    /**
     * Un-/Sets the bracket path
     * @param path the bracket path
     */
    public void setPath(Path2D path) {
        this.path = path;
    }

    /**
     * Use path (bracket) or graphics context for drawing operations
     * @return {@code true}, if the drawing should go to the path bracket,
     *      if {@code false} draw directly to the graphics context
     */
    public boolean getUsePathBracket() {
        return usePathBracket;
    }

    public void setUsePathBracket(boolean usePathBracket) {
        this.usePathBracket = usePathBracket;
    }

    public EmfPlusHatchStyle getEmfPlusBrushHatch() {
        return emfPlusBrushHatch;
    }

    public void setEmfPlusBrushHatch(EmfPlusHatchStyle emfPlusBrushHatch) {
        this.emfPlusBrushHatch = emfPlusBrushHatch;
    }

    public ImageRenderer getEmfPlusImage() {
        return emfPlusImage;
    }

    public void setEmfPlusImage(ImageRenderer emfPlusImage) {
        this.emfPlusImage = emfPlusImage;
    }

    public void addLeftTransform(AffineTransform transform) {
        addLRTransform(transform, TransOperand.left);
    }

    public void addRightTransform(AffineTransform transform) {
        addLRTransform(transform, TransOperand.right);
    }

    private static <T> T last(List<T> list) {
        return list.isEmpty() ? null : list.get(list.size()-1);
    }

    private void addLRTransform(AffineTransform transform, TransOperand lr) {
        if (transform.isIdentity() || (transform.equals(last(transXForm)) && lr.equals(last(transOper)))) {
            // some EMFs add duplicated transformations - ignore them
            return;
        }
        transXForm.add(transform);
        transOper.add(lr);
    }

    public void clearTransform() {
        transXForm.clear();
        transOper.clear();
    }

    List<AffineTransform> getTransXForm() {
        return transXForm;
    }

    List<TransOperand> getTransOper() {
        return transOper;
    }

    public Rectangle2D getBrushRect() {
        return brushRect;
    }

    public void setBrushRect(Rectangle2D brushRect) {
        this.brushRect = brushRect;
    }

    public List<? extends Map.Entry<Float, Color>> getBrushColorsV() {
        return brushColorsV;
    }

    public void setBrushColorsV(List<? extends Map.Entry<Float, Color>> brushColorsV) {
        this.brushColorsV = brushColorsV;
    }

    public List<? extends Map.Entry<Float, Color>> getBrushColorsH() {
        return brushColorsH;
    }

    public void setBrushColorsH(List<? extends Map.Entry<Float, Color>> brushColorsH) {
        this.brushColorsH = brushColorsH;
    }
}
