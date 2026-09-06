package org.docx4j.metafiles;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.util.Collection;

import org.docx4j.org.apache.poi.sl.draw.ImageRenderer;
import org.docx4j.org.apache.poi.util.RecordFormatException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Every renderable metafile draws onto a {@link BufferedImage} at 96 DPI without throwing,
 * and puts some ink on the page.
 *
 * This is the acceptance test for CR-011 phase 1: "the copied POI test files render to
 * PNG at a fixed DPI".
 *
 * "Ink" is measured against a magenta canvas, not a white one: several metafiles
 * legitimately paint white shapes (empty-polygon-close.wmf paints 259 white polygons),
 * which are invisible on white but are still a successful render.
 */
@RunWith(Parameterized.class)
public class MetafileRenderTest {

	@Parameters(name = "{0}")
	public static Collection<Object[]> files() {
		return MetafileTestFiles.renderableNames().stream()
			.map(n -> new Object[] { n })
			.collect(java.util.stream.Collectors.toList());
	}

	@Parameter
	public String name;

	@Test
	public void draws() throws Exception {
		byte[] data = MetafileTestFiles.bytes(name);

		if (MetafileTestFiles.throwsOnRender(name)) {
			try {
				MetafileRendering.render(name, data, MetafileRendering.PROBE_BACKGROUND);
				fail(name + ": expected rendering to be rejected; see MetafileTestFiles.THROWS_ON_RENDER");
			} catch (RecordFormatException expected) {
				assertNotNull(expected);
			}
			return;
		}

		BufferedImage img = MetafileRendering.render(name, data, MetafileRendering.PROBE_BACKGROUND);

		assertNotNull(name, img);
		assertTrue(name + ": degenerate image " + img.getWidth() + "x" + img.getHeight(),
			img.getWidth() >= MetafileRendering.MIN_PX && img.getHeight() >= MetafileRendering.MIN_PX);

		double ink = MetafileRendering.inkFraction(img, MetafileRendering.PROBE_BACKGROUND);
		if (MetafileTestFiles.rendersBlank(name)) {
			// pinned so that a future fix is noticed: see MetafileTestFiles.RENDERS_BLANK
			assertTrue(name + ": now draws something (ink fraction " + ink + ") - good news; "
					+ "remove it from MetafileTestFiles.RENDERS_BLANK", ink == 0.0);
		} else {
			assertTrue(name + ": rendered blank (ink fraction " + ink + ")", ink > 0.0);
		}
	}

	/**
	 * The content-type dispatch used in production picks a renderer which can actually
	 * load the file.
	 */
	@Test
	public void dispatchPicksAWorkingRenderer() throws Exception {
		ImageRenderer r = MetafileRendering.dispatch(null, name);
		assertNotNull(name, r);
		assertTrue(name + ": " + r.getClass().getSimpleName() + " says it can't render "
				+ MetafileTestFiles.contentTypeOf(name),
			r.canRender(MetafileTestFiles.contentTypeOf(name)));

		r.loadImage(MetafileTestFiles.bytes(name), MetafileTestFiles.contentTypeOf(name));
		assertNotNull(name, r.getDimension());
	}
}
