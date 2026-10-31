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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * A revision's or comment's stamp, read as Word reads it (WD 0.46 REQ-702; docx4j-portfolio
 * {@code date_handling_across_ports.md} section 3.7 (c), Jason, 31 October 2026). Word writes two
 * attributes: {@code w:date}, the author's wall clock labelled {@code Z} although it is not UTC, and
 * {@code w16du:dateUtc} ({@code w16cex:dateUtc} for a comment), the instant. Word shows
 * {@code w:date} as it is, and ignores {@code dateUtc} (measured 31 October 2026, CR-068 phase 0).
 * So a stamp has two readings: the instant ({@link TrackedChange#getDate()}), and the author's local
 * time, with the author's UTC offset where both attributes let it be derived. Nothing here converts
 * a time to another zone.
 *
 * @since 17.3.5
 */
public final class RevisionDates {

	private static final DateTimeFormatter LOCAL = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
	private static final DateTimeFormatter OFFSET = DateTimeFormatter.ofPattern("xxx");
	/** The offsets clocks keep: UTC-12:00 to UTC+14:00. */
	private static final int MIN_OFFSET = -12 * 3600, MAX_OFFSET = 14 * 3600;

	private RevisionDates() {}

	/**
	 * The author's local time: {@code w:date}'s date and time as stored, any designator ({@code Z}, an
	 * offset) not read, a missing time 00:00:00, fractions of a second dropped; null for no date.
	 */
	public static LocalDateTime localTime(XMLGregorianCalendar date) {
		if (date == null || date.getYear() == DatatypeConstants.FIELD_UNDEFINED) return null;
		return LocalDateTime.of(date.getYear(), field(date.getMonth(), 1), field(date.getDay(), 1),
				field(date.getHour(), 0), field(date.getMinute(), 0), field(date.getSecond(), 0));
	}

	/**
	 * The author's UTC offset: {@code w:date} less {@code dateUtc}, where both are present and differ by
	 * a whole number of quarter hours from -12:00 to +14:00, as a clock's offset does; else null. A pair
	 * that fails the test, from a producer that wrote them inconsistently, gives no offset rather than a
	 * wrong one.
	 */
	public static ZoneOffset offset(XMLGregorianCalendar date, XMLGregorianCalendar dateUtc) {
		LocalDateTime local = localTime(date), utc = utc(dateUtc);
		if (local == null || utc == null) return null;
		long seconds = Duration.between(utc, local).getSeconds();
		if (seconds % (15 * 60) != 0 || seconds < MIN_OFFSET || seconds > MAX_OFFSET) return null;
		return ZoneOffset.ofTotalSeconds((int) seconds);
	}

	/**
	 * The stamp as REQ-702 writes it: the author's local time in ISO 8601's extended form with no zone
	 * designator ({@code 2026-10-31T11:00:00}), and the author's offset after it where it is known
	 * ({@code 2026-10-31T11:00:00+11:00}, {@code +00:00} for UTC); {@code dateUtc} with {@code Z} where
	 * there is no {@code w:date}; null where there is neither. The {@code Z} Word stores on {@code w:date}
	 * is never written.
	 */
	public static String text(XMLGregorianCalendar date, XMLGregorianCalendar dateUtc) {
		LocalDateTime local = localTime(date);
		if (local == null) {
			LocalDateTime utc = utc(dateUtc);
			return utc == null ? null : LOCAL.format(utc) + "Z";
		}
		ZoneOffset offset = offset(date, dateUtc);
		return offset == null ? LOCAL.format(local) : LOCAL.format(local) + OFFSET.format(offset);
	}

	/** {@code dateUtc} as a UTC clock: unzoned is UTC, an explicit offset is applied; null for none. */
	private static LocalDateTime utc(XMLGregorianCalendar dateUtc) {
		if (dateUtc == null || dateUtc.getYear() == DatatypeConstants.FIELD_UNDEFINED) return null;
		LocalDateTime local = localTime(dateUtc);
		int tz = dateUtc.getTimezone();
		return tz == DatatypeConstants.FIELD_UNDEFINED ? local : local.minusMinutes(tz);
	}

	private static int field(int value, int fallback) {
		return value == DatatypeConstants.FIELD_UNDEFINED ? fallback : value;
	}
}
