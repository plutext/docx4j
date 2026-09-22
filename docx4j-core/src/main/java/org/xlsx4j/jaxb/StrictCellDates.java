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
package org.xlsx4j.jaxb;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * The strict edition's date cell ({@code <c t="d"><v>2025-12-11</v></c>},
 * ISO/IEC 29500-1 18.18.11, which Excel writes in a workbook saved as Strict)
 * converted to what Excel writes in a transitional workbook: the serial number
 * of the 1900 date system, and no {@code t}. Called from
 * {@code xlsx-preprocessor.xslt} when a strict workbook is loaded; docx4j's
 * SpreadsheetML binding (ECMA-376 4th ed. transitional) has no {@code d} cell
 * type, and without the conversion the {@code t} was dropped on load and Excel
 * repaired the cell.
 * <p>
 * The 1900 system counts 1899-12-31 as 1 and has Lotus 1-2-3's 1900-02-29, so
 * a date before 1900-03-01 is one less than the days since 1899-12-30. The
 * 1904 system ({@code workbookPr/@date1904}) is not seen by a per-sheet
 * transform and is not applied: a strict workbook in the 1904 system would
 * have its dates four years and a day late.
 *
 * @since 17.2.1
 */
public class StrictCellDates {

	private static final LocalDate EPOCH_1900 = LocalDate.of(1899, 12, 30);
	private static final LocalDate LEAP_BUG_END = LocalDate.of(1900, 3, 1);

	/**
	 * @param iso an ISO 8601 date ({@code 2025-12-11}) or date-time
	 *            ({@code 2025-12-11T10:30:00}, fractional seconds allowed)
	 * @return the serial number as Excel would write it ({@code 46287},
	 *         {@code 46287.4375}); the input unchanged if it is not a date
	 */
	public static String serial(String iso) {
		if (iso == null) return null;
		String s = iso.trim();
		LocalDate date;
		LocalTime time = null;
		try {
			if (s.length() > 10 && (s.charAt(10) == 'T' || s.charAt(10) == ' ')) {
				String t = s.substring(11);
				if (t.endsWith("Z")) t = t.substring(0, t.length() - 1);
				LocalDateTime dt = LocalDateTime.of(LocalDate.parse(s.substring(0, 10)), LocalTime.parse(t));
				date = dt.toLocalDate();
				time = dt.toLocalTime();
			} else {
				date = LocalDate.parse(s);
			}
		} catch (Exception e) {
			return iso;
		}
		long days = ChronoUnit.DAYS.between(EPOCH_1900, date);
		if (date.isBefore(LEAP_BUG_END)) days -= 1;
		if (time == null || time.equals(LocalTime.MIDNIGHT)) return Long.toString(days);
		BigDecimal fraction = BigDecimal.valueOf(time.toNanoOfDay()).divide(BigDecimal.valueOf(86_400_000_000_000L), 15, RoundingMode.HALF_UP);
		return BigDecimal.valueOf(days).add(fraction).stripTrailingZeros().toPlainString();
	}

}
