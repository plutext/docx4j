/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.fo.renderers.FORendererApacheFOP;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.images.XmlGraphicsImageStreams;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.toc.TocGenerator;
import org.docx4j.wml.Br;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STBrType;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Issue 609: a conversion must not leave the pictures it wrote to java.io.tmpdir open, since
 * on Windows docx4j then can't delete them.  Counted as this process's open file descriptors
 * into java.io.tmpdir, so only where /proc/self/fd exists.
 */
public class ImageStreamsClosedTest {

	@Test
	public void sourceReuseIsOffByDefault() throws Exception {

		Class.forName(FORendererApacheFOP.class.getName()); // its static initializer sets it
		assertEquals("true", System.getProperty(XmlGraphicsImageStreams.NO_SOURCE_REUSE));
	}

	/** The TOC's page numbers come from the area tree, where FOP only preloads the pictures */
	@Test
	public void tocUpdateLeavesNoPictureOpen() throws Exception {

		Assume.assumeTrue(new File("/proc/self/fd").isDirectory());

		WordprocessingMLPackage pkg = withPictures();
		TocGenerator toc = new TocGenerator(pkg);
		toc.generateToc(0, " TOC \\o \"1-3\" \\h \\z \\u ", true);

		Set<String> before = openInTmpdir();
		toc.updateToc();
		Set<String> opened = openInTmpdir();
		opened.removeAll(before);

		assertTrue("left open: " + opened, opened.isEmpty());
	}

	/** Loading a PNG for PDF leaks its stream in xmlgraphics-commons 2.11 (ImageLoaderRawPNG, XGC-150) */
	@Ignore("until docx4j is on an xmlgraphics-commons with XGC-150 (ImageLoaderRawPNG closes its stream)")
	@Test
	public void pdfLeavesNoPictureOpen() throws Exception {

		Assume.assumeTrue(new File("/proc/self/fd").isDirectory());

		WordprocessingMLPackage pkg = withPictures();
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);

		Set<String> before = openInTmpdir();
		Docx4J.toFO(foSettings, new ByteArrayOutputStream(), Docx4J.FLAG_NONE);
		Set<String> opened = openInTmpdir();
		opened.removeAll(before);

		assertTrue("left open: " + opened, opened.isEmpty());
	}

	/** Three headings on three pages, a PNG under the first and a JPEG under the second */
	private static WordprocessingMLPackage withPictures() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		ObjectFactory factory = Context.getWmlObjectFactory();
		for (int i = 1; i <= 3; i++) {
			mdp.addStyledParagraphOfText("Heading1", "Heading " + i);
			if (i < 3) {
				BinaryPartAbstractImage image = BinaryPartAbstractImage.createImagePart(pkg,
						picture(i == 1 ? Color.BLUE : Color.ORANGE, i == 1 ? "png" : "jpg"));
				Inline inline = image.createImageInline("picture " + i, "picture " + i, i, i + 100, false);
				Drawing drawing = factory.createDrawing();
				drawing.getAnchorOrInline().add(inline);
				mdp.getContent().add(paragraphOf(factory, drawing));
			}
			Br br = factory.createBr();
			br.setType(STBrType.PAGE);
			mdp.getContent().add(paragraphOf(factory, br));
		}
		return pkg;
	}

	private static P paragraphOf(ObjectFactory factory, Object runContent) {
		R r = factory.createR();
		r.getContent().add(runContent);
		P p = factory.createP();
		p.getContent().add(r);
		return p;
	}

	private static byte[] picture(Color colour, String format) throws Exception {

		BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(colour);
		g.fillRect(0, 0, 400, 300);
		g.dispose();
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ImageIO.write(image, format, bytes);
		return bytes.toByteArray();
	}

	/** This process's open files in java.io.tmpdir (Linux: a deleted one ends " (deleted)") */
	private static Set<String> openInTmpdir() throws Exception {

		String tmpdir = new File(System.getProperty("java.io.tmpdir")).getCanonicalPath();
		Set<String> open = new TreeSet<String>();
		try (Stream<Path> fds = Files.list(Paths.get("/proc/self/fd"))) {
			fds.forEach(fd -> {
				try {
					String target = Files.readSymbolicLink(fd).toString();
					if (target.startsWith(tmpdir + File.separator)) open.add(target);
				} catch (Exception e) {
					// closed while we looked
				}
			});
		}
		return open;
	}
}
