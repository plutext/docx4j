package org.docx4j.metafiles;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Locates the WMF/EMF inputs in {@code src/test/resources/metafiles}.
 *
 * See that directory's README.md for where each file came from.
 *
 * @since 17.0.6 (CR-011 phase 1)
 */
public class MetafileTestFiles {

	public static final String DIR = "/metafiles/";

	/** mime type as the repackaged POI code expects it */
	public static final String WMF_CONTENT_TYPE = "image/x-wmf";
	public static final String EMF_CONTENT_TYPE = "image/x-emf";

	/**
	 * Files docx4j cannot get a picture out of: parsing them, or asking the result for its
	 * bounds, throws. Mostly fuzzer findings and POI bug reproducers kept as regression
	 * inputs - the requirement is that they fail <em>cleanly</em> (an exception, promptly),
	 * not that they fail at a particular step.
	 * <p>
	 * Exactly where each one fails depends on whether Java assertions are enabled: POI's
	 * parsers use a bare {@code assert} for a number of "this cannot be right" checks
	 * (about 20 across the copied packages), so with {@code -ea} - which Surefire turns on -
	 * a malformed file can raise {@code AssertionError} well before the check that would
	 * otherwise have caught it, and in production (assertions off) those checks do nothing
	 * at all. Hence one list rather than separate "fails to parse" / "parses but has no
	 * bounds" lists, and hence {@link MetafileParseTest} accepting {@code AssertionError}
	 * as a clean rejection.
	 * <ul>
	 *   <li>{@code 61294.emf}, {@code 61338.wmf}: POI bug reports, infinite loops; the
	 *       parser now throws.</li>
	 *   <li>the {@code clusterfuzz-*} and {@code crash-*} files: fuzzer findings.
	 *       {@code ...-6060921738035200.wmf} is 18 bytes - a truncated header with no
	 *       window records.</li>
	 *   <li>{@code VHZ2...emf} is not malformed as such, but its first parsed record is an
	 *       {@code EmfPolyline} rather than the EMF header, so {@code HemfPicture.getHeader()}
	 *       - which casts {@code records.get(0)} - throws, and with it getBounds()/getSize().
	 *       An upstream POI quirk carried over by the repackaging; POI's own tests never
	 *       call getHeader() on this file.</li>
	 * </ul>
	 */
	private static final List<String> UNUSABLE = Collections.unmodifiableList(Arrays.asList(
		"61294.emf",
		"61338.wmf",
		"clusterfuzz-testcase-minimized-6701721724125184.wmf",
		"clusterfuzz-testcase-minimized-POIFileHandlerFuzzer-6060921738035200.wmf",
		"clusterfuzz-testcase-minimized-POIFileHandlerFuzzer-6466833057382400.emf",
		"crash-7b60e9fe792eaaf1bba8be90c2b62f057cfff142.emf",
		"VHZ2NYFUYUUJNGLABL26ORTQZA76FJEW.emf"
	));

	public static boolean isUnusable(String name) {
		return UNUSABLE.contains(name);
	}

	/**
	 * Files which parse and have bounds, but which the repackaged POI code draws nothing
	 * from. Pinned here so that a future improvement is noticed rather than silently
	 * absorbed.
	 * <ul>
	 *   <li>{@code wrench.emf}: 35 {@code polyPolygon16} records parse and replay without
	 *       throwing, yet nothing lands on the canvas (checked against a magenta canvas,
	 *       so it is not white-on-white). An upstream rendering gap, inherited by the
	 *       repackaging; chase it under CR-011 phase 4.</li>
	 * </ul>
	 */
	private static final List<String> RENDERS_BLANK = Collections.unmodifiableList(Arrays.asList(
		"wrench.emf"
	));

	public static boolean rendersBlank(String name) {
		return RENDERS_BLANK.contains(name);
	}

	/**
	 * Files whose records parse, but where drawing throws.
	 * <ul>
	 *   <li>{@code file-45.wmf} contains a {@code dibStretchBlt} whose DIB header declares
	 *       dimensions past {@code HwmfBitmapDib.MAX_HEIGHT_WIDTH}, so building the
	 *       placeholder image is refused (POI 22531fe638, back-ported - without it the
	 *       renderer would try to allocate a huge BufferedImage). {@code HwmfPicture.draw}
	 *       does not swallow per-record exceptions the way {@code HemfPicture.draw} does,
	 *       so the whole picture fails. Both behaviours are upstream POI's; CR-011 phase 2
	 *       has to catch this at the docx4j boundary when it wires rendering into the
	 *       picture pipeline.</li>
	 * </ul>
	 */
	private static final List<String> THROWS_ON_RENDER = Collections.unmodifiableList(Arrays.asList(
		"file-45.wmf"
	));

	public static boolean throwsOnRender(String name) {
		return THROWS_ON_RENDER.contains(name);
	}

	public static boolean isWmf(String name) {
		return name.toLowerCase(Locale.ROOT).endsWith(".wmf");
	}

	public static String contentTypeOf(String name) {
		return isWmf(name) ? WMF_CONTENT_TYPE : EMF_CONTENT_TYPE;
	}

	/** every .wmf / .emf in the metafiles resource directory, sorted by name */
	public static List<String> allNames() {
		URL url = MetafileTestFiles.class.getResource(DIR);
		if (url == null) {
			throw new IllegalStateException("test resource directory " + DIR + " not on the classpath");
		}
		File dir;
		try {
			dir = new File(url.toURI());
		} catch (URISyntaxException | IllegalArgumentException e) {
			throw new IllegalStateException("test resources are not on the filesystem: " + url, e);
		}
		String[] names = dir.list((d, n) -> {
			String l = n.toLowerCase(Locale.ROOT);
			return l.endsWith(".wmf") || l.endsWith(".emf");
		});
		if (names == null || names.length == 0) {
			throw new IllegalStateException("no metafiles found in " + dir);
		}
		List<String> list = new ArrayList<>(Arrays.asList(names));
		Collections.sort(list);
		return list;
	}

	/** the names which are expected to parse and to yield a drawable page */
	public static List<String> renderableNames() {
		List<String> list = new ArrayList<>(allNames());
		list.removeIf(MetafileTestFiles::isUnusable);
		return list;
	}

	public static InputStream open(String name) {
		InputStream is = MetafileTestFiles.class.getResourceAsStream(DIR + name);
		if (is == null) {
			throw new IllegalStateException("missing test resource " + DIR + name);
		}
		return is;
	}

	public static byte[] bytes(String name) throws IOException {
		try (InputStream is = open(name)) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			int r;
			while ((r = is.read(buf)) > 0) {
				bos.write(buf, 0, r);
			}
			return bos.toByteArray();
		}
	}

	/** POI's POITestCase.assertContains, which docx4j does not have */
	public static void assertContains(String haystack, String needle) {
		org.junit.Assert.assertTrue("expected to find [" + needle + "] in [" + haystack + "]",
			haystack != null && haystack.contains(needle));
	}

	/** the directory the metafiles live in, for tests which write output next to them */
	public static File resourceDir() {
		URL url = MetafileTestFiles.class.getResource(DIR);
		try {
			return new File(url.toURI());
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}
}
