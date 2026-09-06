package org.docx4j.metafiles;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Golden-image regression test for the repackaged HWMF/HEMF renderer (CR-011 phase 1).
 *
 * <h3>Why the comparison is fuzzy</h3>
 *
 * A pixel-exact golden PNG would be a flaky test: Java2D's antialiasing, stroke
 * rasterisation and - above all - font availability differ between JDK versions and
 * between developer machines and CI (see the HtmlVisitorParityTest experience: a CI box
 * with only DejaVu installed renders text quite differently). So each rendering is
 * downsampled to a {@value #THUMB} x {@value #THUMB} thumbnail before comparison, and a
 * per-channel tolerance plus a maximum fraction of differing pixels is allowed. That is
 * still tight enough to catch "the renderer stopped drawing this", "everything moved" or
 * "the colours inverted", which is what this test is for.
 *
 * <h3>Regenerating the goldens</h3>
 *
 * <pre>
 * mvn test -pl docx4j-core-tests -Dtest=MetafileGoldenImageTest -Dmetafile.golden.regenerate=true
 * </pre>
 *
 * writes fresh PNGs into {@code src/test/resources/metafiles/golden/} (not into
 * target/test-classes). Look at them before committing.
 */
@RunWith(Parameterized.class)
public class MetafileGoldenImageTest {

	/** thumbnail edge, in pixels */
	public static final int THUMB = 48;

	/** per-channel difference below which two pixels count as equal */
	private static final int CHANNEL_TOLERANCE = 48;

	/** fraction of thumbnail pixels allowed to differ by more than the tolerance */
	private static final double MAX_DIFFERING_FRACTION = 0.08;

	private static final String GOLDEN_DIR = "golden";

	private static final String REGENERATE_PROPERTY = "metafile.golden.regenerate";

	/**
	 * The files a golden is kept for. Deliberately a curated subset:
	 * <ul>
	 *   <li>predominantly vector content, so the result does not depend on which fonts
	 *       the machine has;</li>
	 *   <li>covering WMF, EMF and EMF+ (the freehand and star files are
	 *       Office- and Freehand-produced EMF and EMF+ dual).</li>
	 * </ul>
	 * Text-bearing files are deliberately excluded, because a machine with a different
	 * font set lays the text out differently and the golden would flake: SimpleEMF_*.emf
	 * and 60677.wmf, and also gradient.emf / pptx.emf, whose page is mostly a
	 * "Hello World" string with a small brush-filled rectangle under it. Those are
	 * covered by {@link MetafileRenderTest} (renders, non-blank) and by the ported POI
	 * assertions in {@link HemfPictureTest} / {@link HwmfParsingTest} instead.
	 */
	private static final List<String> GOLDEN_FILES = Arrays.asList(
		"santa.wmf",
		"empty-polygon-close.wmf",
		"freehand_picture_saveas.wmf",
		"star_picture_save_as.wmf",
		"freehand_picture_saveas.emf",
		"freehand_ppt_saveas.emf",
		"star_picture_save_as.emf",
		"vector_image.emf"
	);

	@Parameters(name = "{0}")
	public static Collection<Object[]> files() {
		return GOLDEN_FILES.stream().map(n -> new Object[] { n })
			.collect(java.util.stream.Collectors.toList());
	}

	@Parameter
	public String name;

	@Test
	public void matchesGolden() throws Exception {
		// rendered on the probe background, not white: empty-polygon-close.wmf paints white
		// polygons, whose golden would otherwise be an uninformative blank square
		BufferedImage rendered = MetafileRendering.render(name, MetafileTestFiles.bytes(name),
			MetafileRendering.PROBE_BACKGROUND);
		BufferedImage thumb = thumbnail(rendered);

		if (Boolean.getBoolean(REGENERATE_PROPERTY)) {
			File out = new File(sourceGoldenDir(), goldenName());
			//noinspection ResultOfMethodCallIgnored
			out.getParentFile().mkdirs();
			ImageIO.write(thumb, "png", out);
			System.out.println("wrote golden " + out);
			return;
		}

		BufferedImage golden = readGolden();
		assertNotNull(name + ": no golden image; regenerate with -D" + REGENERATE_PROPERTY + "=true", golden);

		assertTrue(name + ": golden is " + golden.getWidth() + "x" + golden.getHeight()
				+ ", expected " + THUMB + "x" + THUMB,
			golden.getWidth() == THUMB && golden.getHeight() == THUMB);

		int differing = 0;
		for (int y = 0; y < THUMB; y++) {
			for (int x = 0; x < THUMB; x++) {
				if (!closeEnough(thumb.getRGB(x, y), golden.getRGB(x, y))) {
					differing++;
				}
			}
		}
		double fraction = differing / (double)(THUMB * THUMB);
		assertTrue(String.format("%s: %d of %d thumbnail pixels (%.1f%%) differ from the golden "
				+ "by more than %d per channel; max allowed %.1f%%. If this is an intended "
				+ "rendering change, regenerate with -D%s=true and eyeball the PNGs.",
				name, differing, THUMB * THUMB, fraction * 100, CHANNEL_TOLERANCE,
				MAX_DIFFERING_FRACTION * 100, REGENERATE_PROPERTY),
			fraction <= MAX_DIFFERING_FRACTION);
	}

	private static boolean closeEnough(int rgb1, int rgb2) {
		return Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) <= CHANNEL_TOLERANCE
			&& Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) <= CHANNEL_TOLERANCE
			&& Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF)) <= CHANNEL_TOLERANCE;
	}

	private String goldenName() {
		return name + ".png";
	}

	private BufferedImage readGolden() throws Exception {
		try (InputStream is = MetafileGoldenImageTest.class.getResourceAsStream(
				MetafileTestFiles.DIR + GOLDEN_DIR + "/" + goldenName())) {
			return (is == null) ? null : ImageIO.read(is);
		}
	}

	/**
	 * target/test-classes is a copy; regenerated goldens must land in src/test/resources
	 * so that they can be committed.
	 */
	private static File sourceGoldenDir() {
		// walk up from wherever the resources ended up (target/test-classes/metafiles under
		// Maven, src/test/resources/metafiles in an IDE) until src/test/resources/metafiles
		// is in view
		for (File d = MetafileTestFiles.resourceDir(); d != null; d = d.getParentFile()) {
			File src = new File(d, "src/test/resources/metafiles");
			if (src.isDirectory()) {
				return new File(src, GOLDEN_DIR);
			}
		}
		throw new IllegalStateException("can't find src/test/resources/metafiles from "
			+ MetafileTestFiles.resourceDir());
	}

	private static BufferedImage thumbnail(BufferedImage src) {
		BufferedImage dst = new BufferedImage(THUMB, THUMB, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = dst.createGraphics();
		try {
			g.setColor(MetafileRendering.PROBE_BACKGROUND);
			g.fillRect(0, 0, THUMB, THUMB);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.drawImage(src, 0, 0, THUMB, THUMB, null);
		} finally {
			g.dispose();
		}
		return dst;
	}
}
