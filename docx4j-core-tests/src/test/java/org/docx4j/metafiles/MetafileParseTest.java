package org.docx4j.metafiles;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.List;

import org.docx4j.org.apache.poi.hemf.usermodel.HemfPicture;
import org.docx4j.org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Every metafile in src/test/resources/metafiles either parses - yielding records and sane
 * bounds - or, if it is one of the deliberately malformed fuzzer inputs, is rejected cleanly
 * rather than hanging or exhausting memory.
 *
 * CR-011 phase 1.
 */
@RunWith(Parameterized.class)
public class MetafileParseTest {

	@Parameters(name = "{0}")
	public static Collection<Object[]> files() {
		return MetafileTestFiles.allNames().stream()
			.map(n -> new Object[] { n })
			.collect(java.util.stream.Collectors.toList());
	}

	@Parameter
	public String name;

	@Test
	public void parses() throws Exception {
		if (MetafileTestFiles.isUnusable(name)) {
			failsCleanly();
		} else {
			parsesCleanly();
		}
	}

	private void parsesCleanly() throws Exception {
		byte[] data = MetafileTestFiles.bytes(name);
		assertTrue(name + ": no data", data.length > 0);

		final int recordCount;
		final Rectangle2D boundsInPoints;

		if (MetafileTestFiles.isWmf(name)) {
			HwmfPicture pic = new HwmfPicture(new ByteArrayInputStream(data));
			List<?> records = pic.getRecords();
			assertNotNull(name, records);
			recordCount = records.size();
			assertNotNull(name + ": no header", pic.getHeader());
			boundsInPoints = pic.getBoundsInPoints();
		} else {
			HemfPicture pic = new HemfPicture(new ByteArrayInputStream(data));
			List<?> records = pic.getRecords();
			assertNotNull(name, records);
			recordCount = records.size();
			assertNotNull(name + ": no header", pic.getHeader());
			boundsInPoints = pic.getBoundsInPoints();
		}

		assertTrue(name + ": expected at least one record, got " + recordCount, recordCount > 0);

		assertNotNull(name + ": no bounds", boundsInPoints);
		assertTrue(name + ": non-finite bounds " + boundsInPoints,
			Double.isFinite(boundsInPoints.getWidth()) && Double.isFinite(boundsInPoints.getHeight())
			&& Double.isFinite(boundsInPoints.getX()) && Double.isFinite(boundsInPoints.getY()));
		assertTrue(name + ": non-positive bounds " + boundsInPoints,
			boundsInPoints.getWidth() > 0 && boundsInPoints.getHeight() > 0);
		// a page is 612x792pt; metafiles are usually smaller, but allow a very generous
		// ceiling - the point is to catch garbage dimensions, not to police real content
		assertTrue(name + ": implausibly large bounds " + boundsInPoints,
			boundsInPoints.getWidth() < 1_000_000 && boundsInPoints.getHeight() < 1_000_000);
	}

	/**
	 * Parsing and then asking for the bounds must throw, promptly. Where in that sequence
	 * it throws is not pinned down: it depends on whether assertions are enabled, since
	 * POI's parsers use bare {@code assert} for several sanity checks (see
	 * {@link MetafileTestFiles#isUnusable}).
	 */
	private void failsCleanly() throws Exception {
		byte[] data = MetafileTestFiles.bytes(name);
		Throwable rejection = null;
		try {
			if (MetafileTestFiles.isWmf(name)) {
				HwmfPicture pic = new HwmfPicture(new ByteArrayInputStream(data));
				pic.getRecords();
				pic.getBoundsInPoints();
			} else {
				HemfPicture pic = new HemfPicture(new ByteArrayInputStream(data));
				pic.getRecords();
				pic.getBoundsInPoints();
			}
		} catch (RuntimeException | java.io.IOException | AssertionError expected) {
			rejection = expected;
		}
		assertNotNull(name + ": expected the parser to reject this input", rejection);
	}
}
