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
package org.docx4j.anon;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * What stands in for a removed object. An object's footprint survives, because
 * page flow is what the anonymised document is for, but the footprint says
 * what it was: an OLE object or ActiveX control leaves its picture, and the
 * picture is a generated image labelled "OLE object removed by
 * docx4j anon"; an altChunk, which has no footprint of its own, leaves a
 * marker paragraph. The words of the markers are in {@link Verify}'s allowed
 * vocabulary.
 *
 * @since 17.2.0
 */
class Placeholders {

	private static final Logger log = LoggerFactory.getLogger(Placeholders.class);

	static final String OBJECT_REMOVED = "OLE object removed";
	static final String ALTCHUNK_REMOVED = "[altChunk removed by docx4j anon]";
	static final String BY = "by docx4j anon";

	private static byte[] objectRemovedPng;

	/**
	 * A 480x240 PNG, light grey with a border, labelled {@link #OBJECT_REMOVED} /
	 * {@link #BY}; Word scales it to the object's extent. Falls back to the 2x2
	 * pixels if the image cannot be drawn (no java.desktop at run time).
	 */
	static synchronized byte[] objectRemovedPng() {
		if (objectRemovedPng != null) return objectRemovedPng;
		try {
			int w = 480, h = 240;
			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = img.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setColor(new Color(0xEE, 0xEE, 0xEE));
			g.fillRect(0, 0, w, h);
			g.setColor(new Color(0x88, 0x88, 0x88));
			g.setStroke(new BasicStroke(4));
			g.drawRect(2, 2, w - 4, h - 4);
			g.setColor(new Color(0x33, 0x33, 0x33));
			centred(g, OBJECT_REMOVED, new Font(Font.SANS_SERIF, Font.BOLD, 36), w, h / 2 - 12);
			centred(g, BY, new Font(Font.SANS_SERIF, Font.PLAIN, 24), w, h / 2 + 36);
			g.dispose();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(img, "png", baos);
			objectRemovedPng = baos.toByteArray();
		} catch (Throwable t) {
			log.warn("cannot draw the placeholder image (" + t + "); using the 2x2 pixels");
			objectRemovedPng = MediaReplacer.PNG_IMAGE_DATA;
		}
		return objectRemovedPng;
	}

	private static void centred(Graphics2D g, String text, Font font, int width, int baseline) {
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics();
		g.drawString(text, (width - fm.stringWidth(text)) / 2, baseline);
	}

	/** The paragraph an altChunk becomes. */
	static P altChunkRemoved() {
		ObjectFactory f = new ObjectFactory();
		P p = f.createP();
		R r = f.createR();
		Text t = f.createText();
		t.setValue(ALTCHUNK_REMOVED);
		r.getContent().add(f.createRT(t));
		p.getContent().add(r);
		return p;
	}

}
