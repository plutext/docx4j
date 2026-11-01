/*
 *  Copyright 2026, Plutext Pty Ltd.
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
package org.docx4j.metafiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.docx4j.org.apache.poi.sl.draw.BitmapImageRenderer;
import org.junit.Test;

/**
 * POI dfa53082b3 (back-ported November 2026): a bitmap whose image, crop or anchor has no size
 * draws nothing. Before it, a crop to 0 put an infinite scale into the transform, which Batik's
 * SVGGraphics2D writes as invalid SVG (POI's own test needs a slideshow file; this one draws
 * straight onto a canvas).
 */
public class BitmapImageRendererTest {

    @Test
    public void cropToZeroDrawsNothing() throws Exception {
        assertDrawsNothing(new Rectangle2D.Double(0, 0, 10, 10), new Insets(0, 50_000, 0, 50_000));
        assertDrawsNothing(new Rectangle2D.Double(0, 0, 10, 10), new Insets(50_000, 0, 50_000, 0));
    }

    @Test
    public void zeroAnchorDrawsNothing() throws Exception {
        assertDrawsNothing(new Rectangle2D.Double(0, 0, 0, 10), new Insets(0, 0, 0, 0));
    }

    private static void assertDrawsNothing(Rectangle2D anchor, Insets clip) throws Exception {
        BufferedImage black = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);   // all black
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(black, "png", png);
        BitmapImageRenderer renderer = new BitmapImageRenderer();
        renderer.loadImage(png.toByteArray(), "image/png");

        BufferedImage canvas = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        try {
            assertTrue(renderer.drawImage(g, anchor, clip));
        } finally {
            g.dispose();
        }
        for (int x = 0; x < 10; x++) for (int y = 0; y < 10; y++) assertEquals(0, canvas.getRGB(x, y));
    }
}
