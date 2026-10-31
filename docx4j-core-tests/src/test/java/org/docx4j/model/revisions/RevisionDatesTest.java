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
package org.docx4j.model.revisions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * WD 0.46 REQ-702, the date-handling review's section 3.7 (c) (Jason, 31 October 2026): a stamp's
 * author's local time and offset, beside its instant, over the four cases Word was measured on (CR-068
 * phase 0, {@code manual-redline-dates.docx}).
 */
public class RevisionDatesTest {

	private static XMLGregorianCalendar x(String s) throws Exception {
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(s);
	}

	private static String text(String date, String dateUtc) throws Exception {
		return RevisionDates.text(date == null ? null : x(date), dateUtc == null ? null : x(dateUtc));
	}

	/** REQ-702's form for Word's four measured cases, and for no date. */
	@Test
	public void measuredCases() throws Exception {
		assertEquals("2026-10-31T00:00:00", text("2026-10-31T00:00:00Z", null)); // D1: the Z not written
		assertEquals("2026-10-31T00:00:00Z", text(null, "2026-10-31T00:00:00Z")); // D2: dateUtc alone
		assertEquals("2026-10-31T00:00:00+00:00", text("2026-10-31T00:00:00Z", "2026-10-31T00:00:00Z")); // D3: an author in UTC
		assertEquals("2026-10-31T11:00:00+11:00", text("2026-10-31T11:00:00Z", "2026-10-31T00:00:00Z")); // D4: Sydney, as Word writes
		assertNull(text(null, null));
	}

	/** The offset is any a clock keeps: west of UTC, half and three-quarter hours; an inconsistent pair gives none. */
	@Test
	public void offsets() throws Exception {
		assertEquals("2026-10-30T19:00:00-05:00", text("2026-10-30T19:00:00Z", "2026-10-31T00:00:00Z"));
		assertEquals("2026-10-31T05:30:00+05:30", text("2026-10-31T05:30:00Z", "2026-10-31T00:00:00Z"));
		assertEquals("2026-10-31T05:45:00+05:45", text("2026-10-31T05:45:00Z", "2026-10-31T00:00:00Z"));
		assertEquals("2026-10-31T14:00:00+14:00", text("2026-10-31T14:00:00Z", "2026-10-31T00:00:00Z"));
		assertEquals("2026-10-30T12:00:00-12:00", text("2026-10-30T12:00:00Z", "2026-10-31T00:00:00Z"));
		// not a quarter hour, or beyond any clock: no offset rather than a wrong one
		assertEquals("2026-10-31T00:07:00", text("2026-10-31T00:07:00Z", "2026-10-31T00:00:00Z"));
		assertEquals("2026-11-01T03:00:00", text("2026-11-01T03:00:00Z", "2026-10-31T00:00:00Z"));
		assertEquals("2026-10-30T11:00:00", text("2026-10-30T11:00:00Z", "2026-10-31T00:00:00Z"));
		assertNull(RevisionDates.offset(x("2026-10-31T00:07:00Z"), x("2026-10-31T00:00:00Z")));
		assertEquals(ZoneOffset.ofHoursMinutes(5, 45), RevisionDates.offset(x("2026-10-31T05:45:00Z"), x("2026-10-31T00:00:00Z")));
	}

	/** Other producers' forms: w:date's own designator is never read; dateUtc's offset is applied; a date alone is midnight. */
	@Test
	public void otherForms() throws Exception {
		assertEquals("2026-10-31T11:00:00", text("2026-10-31T11:00:00", null));
		assertEquals("2026-10-31T11:00:00", text("2026-10-31T11:00:00+05:00", null));
		assertEquals("2026-10-31T00:00:00", text("2026-10-31", null));
		assertEquals("2026-10-31T11:00:00", text("2026-10-31T11:00:00.5Z", null));
		assertEquals("2026-10-31T11:00:00+11:00", text("2026-10-31T11:00:00Z", "2026-10-31T05:00:00+05:00"));
		assertEquals("2026-10-31T00:00:00Z", text(null, "2026-10-31T05:00:00+05:00"));
		assertEquals(LocalDateTime.of(2026, 10, 31, 11, 0), RevisionDates.localTime(x("2026-10-31T11:00:00Z")));
		assertNull(RevisionDates.localTime(null));
	}

	/** TrackedChange: the author's local time and offset beside the instant, on Word's four cases. */
	@Test
	public void trackedChanges() throws Exception {
		WordprocessingMLPackage pkg = TrackedChangesWordTest.load(ManualRedlineTest.DIR + "manual-redline-dates.docx");
		List<TrackedChange> changes = TrackedChanges.in(pkg);
		assertEquals(4, changes.size());
		TrackedChange d1 = changes.get(0), d2 = changes.get(1), d3 = changes.get(2), d4 = changes.get(3);
		assertEquals(LocalDateTime.of(2026, 10, 31, 0, 0), d1.getAuthorLocalTime());
		assertNull(d1.getAuthorOffset());
		assertNull(d2.getAuthorLocalTime()); // Word shows no date
		assertNull(d2.getAuthorOffset());
		assertEquals(x("2026-10-31T00:00:00Z").toGregorianCalendar().toInstant(), d2.getDate());
		assertEquals(LocalDateTime.of(2026, 10, 31, 0, 0), d3.getAuthorLocalTime());
		assertEquals(ZoneOffset.UTC, d3.getAuthorOffset());
		assertEquals(LocalDateTime.of(2026, 10, 31, 11, 0), d4.getAuthorLocalTime());
		assertEquals(ZoneOffset.ofHours(11), d4.getAuthorOffset());
		// where the offset is known, the author's clock gives the instant, with no zone assumed
		assertEquals(d4.getDate(), d4.getAuthorLocalTime().toInstant(d4.getAuthorOffset()));
		assertEquals(d3.getDate(), d3.getAuthorLocalTime().toInstant(d3.getAuthorOffset()));
	}
}
