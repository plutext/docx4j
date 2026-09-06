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
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import org.docx4j.org.apache.poi.util.Internal;

/**
 * An util class for saving the state of a {@link java.awt.Graphics2D} object
 */
@Internal
public class HwmfGraphicsState {
    private Color background;
    private Shape clip;
    private Color color;
    private Composite composite;
    private Font font;
    private Paint paint;
    private Stroke stroke;
    private AffineTransform trans;

    /**
     * Saves the state of the graphics2D object
     */
    public void backup(Graphics2D graphics2D) {
        background = graphics2D.getBackground();
        clip = graphics2D.getClip();
        color = graphics2D.getColor();
        composite = graphics2D.getComposite();
        font = graphics2D.getFont();
        paint = graphics2D.getPaint();
        stroke = graphics2D.getStroke();
        trans = graphics2D.getTransform();
    }

    /**
     * Retrieves the state into the graphics2D object
     */
    public void restore(Graphics2D graphics2D) {
        graphics2D.setBackground(background);
        graphics2D.setClip(clip);
        graphics2D.setColor(color);
        graphics2D.setComposite(composite);
        graphics2D.setFont(font);
        graphics2D.setPaint(paint);
        graphics2D.setStroke(stroke);
        graphics2D.setTransform(trans);
    }
}
